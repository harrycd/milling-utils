package uk.ac.cf.milling.utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.cf.milling.objects.KPIs;
import uk.ac.cf.milling.objects.SimulatorConfig;

/**
 * @author Theocharis Alexopoulos
 * This class contains methods for utilities needed to translate the GCode file information in String
 * format to analysis file information. Analysis file contains for every timestep the position of the
 * tool, its spindle speed and other information related to KPIs calculation. 
 */
public class GCodeAnalyser {

	/**
	 * This method is used as the first step of convert the GCode file to blocks of strings in an array
	 * that will help further steps to identify the type of command given on each step. 
	 * @param pathToFile - The path to gcode file
	 * @return a list of arrays in which each element is a gcode command 
	 * Example:
	 * 
	 * lines of GCode file: 
	 * G00 X0.0 Y0.0
	 * G01 X1.0 F60
	 * 
	 * returned array:
	 * entries[0][0]: G00
	 * entries[0][1]: X0.0
	 * entries[0][2]: Y0.0
	 * entries[1][0]: G01
	 * entries[1][1]: X1.0
	 * entries[1][2]: F60
	 * 
	 */
	public List<String[]> parseGCode(String pathToFile){
		List<String[]> entries = new ArrayList<String[]>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(pathToFile));
			String line;

			while ((line = br.readLine()) != null) {
				if (!line.isEmpty() && !line.substring(0, 1).equals("(")){
					String[] entry = line.split(" |(?<=(\\d)(?=[a-zA-Z]))|\\.(?=[a-zA-Z])");
					entries.add(entry);
				}
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return entries;
	}

	/**
	 * @param blocks - list of blocks containing the g code to execute
	 * @param outputPath - the path to place output file for post processing (empty for no output file)
	 * @return the calculated KPIs
	 */
	public KPIs analyseGCode(List<String[]> blocks, SimulatorConfig config){
		KPIs kpis = new KPIs();
		double timeStep = SettingUtils.getTimeStep();

		double xStart = 0.0; // initial X coordinate of tool		
		double yStart = 0.0; // initial Y coordinate of tool
		double zStart = 1000.0; // initial Z coordinate of tool
		double xEnd = 0.0; // target X coordinate of tool		
		double yEnd = 0.0; // target Y coordinate of tool
		double zEnd = 1000.0; // target Z coordinate of tool
		
		double radius = 0.0; // the radius of arc path
		double arcI = 0.0; // the X coordinate of the center point of arc
		double arcJ = 0.0; // the Y coordinate of the center point of arc
		double arcK = 0.0; // the Z coordinate of the center point of arc
		boolean planeYZ = false;
		boolean planeXZ = false;
		boolean planeXY = false;

		boolean incrPosXYZ = false; //incremental positioning for tool position
		boolean incrPosIJK = false; //incremental positioning for arc centre

		int gValue = -1; // action to perform
		int group01 = -1; // group 1 modal modes

		double feedRate = 0; //The feed rate of the machine
		String pocketId = ""; // The ID of the carousel pocket
		double spindleSpeed = 0.0; // The spindle speed

		double totalDistance = 0; //Total distance that the tool travels to run the gcode
		double totalTime = 0; //Total time to run the gcode
		
		StringBuilder outputBuilder = new StringBuilder();
		outputBuilder.append("t,X,Y,Z,T,SS\n");

		// Iterate over every block
		for (String[] block:blocks){
			//Populate the block words
			wordsLoop:
				for (String word:block){
					try {
						String wordType = word.substring(0, 1);
						double wordValue = 0;
						if (word.length()>1){
							wordValue = Double.parseDouble(word.substring(1));
						}

						// Store information to the relevant variable
						switch (wordType){
						case "(" :
							//This is a comment and the rest of the line should be ignored
							break wordsLoop;
						case "X" :
							xEnd = wordValue;
							break;
						case "Y" :
							yEnd = wordValue;
							break;
						case "Z" :
							zEnd = wordValue;
							break;
						case "G" :
							//TODO discarding information if the G word contains double!
							gValue = (int) wordValue;
							if (Groups.contains(Groups.group01, gValue)){
								group01 = gValue;
								break;
							}
							if (gValue == 17){
								planeXY = true;
								planeXZ = false;
								planeYZ = false;
								break;
							}
							if (gValue == 18){
								planeXY = false;
								planeXZ = true;
								planeYZ = false;
								break;
							}
							if (gValue == 19){
								planeXY = false;
								planeXZ = false;
								planeYZ = true;
								break;
							}
							if (gValue == 90){ 
								incrPosXYZ = false;
								break;
							}
							if (gValue == 91){ 
								incrPosXYZ = true;
								break;
							}
								System.out.println(gValue + " : " + Arrays.toString(block));
							break;
						case "F" :
							feedRate = wordValue/60; //inches or mm per second
							break;
						case "R" :
							radius = wordValue;
							break;
						case "I" :
							arcI = wordValue;
							break;
						case "J" :
							arcJ = wordValue;
							break;
						case "K" :
							arcK = wordValue;
							break;
						case "S" :
							spindleSpeed = wordValue; // RPM
							break;
						case "T" :
							pocketId = word.substring(1); // RPM
							break;
						}
					} catch (Exception e){
						System.out.print("Command causing errors: " + word);
						e.printStackTrace();
					}
				}

			// Translate to actions the values that have been parsed and calculate the KPIs
	
			if (group01 == 0 || group01 == 1){ //Linear movement
				
				// Check if there is movement
				if (incrPosXYZ){
					if (xEnd == 0 && yEnd == 0 & zEnd == 0) continue;
				} else {
					if (xStart == xEnd && yStart == yEnd && zStart == zEnd) continue;
				}

				// Adjust the feed rate if it is G0
				if (group01 == 0) feedRate = config.getMaxFeedRate();
				if (incrPosXYZ){
					xEnd += xStart;
					yEnd += yStart;
					zEnd += zStart;
				}
				
				// Calculate the results of linear movement
				Result linearResult = analyseLinearMovement(feedRate, timeStep, xStart, yStart, zStart, xEnd, yEnd, zEnd);
				
				// Add results to the string builder (set the initial total Time in the result
				linearResult.totalDistance = totalDistance;
				linearResult.totalTime = totalTime;
				updateOutput(outputBuilder, linearResult, pocketId, spindleSpeed);
				
				// Update the totalDistance and totalTime
				totalDistance += linearResult.blockDistance;
				totalTime += linearResult.blockTime;
			}

			else if (group01 == 2 || group01 == 3){ //Arc movement
				
				// Check if there is movement
				if (incrPosIJK){
					if (arcI == 0 && arcJ == 0 && arcK == 0) continue;
				} else {
					if (arcI == xEnd && arcJ == yEnd && arcK == zEnd) continue;
				}
				
				// Check if it G02 command (clockwise) or G03 (counterclockwise)
				boolean clockwise = group01 == 2;
				
				// Analyse the arc movement of the tool
				Result arcResult = analyseArcMovement(clockwise, feedRate, timeStep, xStart, yStart, zStart, xEnd, yEnd, zEnd, arcI, arcJ, arcK);
				
				// Add results to the string builder (set the initial total Time in the result)
				arcResult.totalDistance = totalDistance;
				arcResult.totalTime = totalTime;
				updateOutput(outputBuilder, arcResult, pocketId, spindleSpeed);

				// Update the totalDistance and totalTime
				totalDistance += arcResult.blockDistance;
				totalTime += arcResult.blockTime;
			}
	
	
			// Reset values for next block
			if (incrPosXYZ){
				xStart += xEnd;
				yStart += yEnd;
				zStart += zEnd;
			} else {
				xStart = xEnd;
				yStart = yEnd;
				zStart = zEnd;
			}
			
			// The values I J K have to be specified every time
			if (incrPosIJK){
				arcI = 0;
				arcJ = 0;
				arcK = 0;
			} else {
				arcI = xEnd;
				arcJ = yEnd;
				arcK = zEnd;
			}
			
		}
		
		kpis.setDistance(totalDistance);
		kpis.setTime(totalTime);
		kpis.setAnalysisData(outputBuilder.toString());
		return kpis;
	}


	/**
	 * @param feedRate
	 * @param timeStep
	 * @param xStart
	 * @param yStart
	 * @param zStart
	 * @param xEnd
	 * @param yEnd
	 * @param zEnd
	 * @return
	 */
	private Result analyseLinearMovement(double feedRate, double timeStep, 
			double xStart, double yStart, double zStart,
			double xEnd, double yEnd, double zEnd) {

		double blockDistance = calculateLinearDistance(xStart, yStart, zStart, xEnd, yEnd, zEnd);
		double blockTime = calculateTime(blockDistance, feedRate);
		
		
		// Create output of linear movement for the analysis file
		// feedRateX / feedRate = distanceX / distance
		double feedRateX = feedRate * (xEnd - xStart) / blockDistance;
		double feedRateY = feedRate * (yEnd - yStart) / blockDistance;
		double feedRateZ = feedRate * (zEnd - zStart) / blockDistance;
		int steps =  1 + (int)(blockTime / timeStep);
		double[] timeSteps = new double[steps];
		double[] xCoords = new double[steps];
		double[] yCoords = new double[steps];
		double[] zCoords = new double[steps];
		
		for (int step = 0; step < steps; step++){
			timeSteps[step] = step * timeStep;
			xCoords[step] = xStart+feedRateX * timeSteps[step];
			yCoords[step] = yStart+feedRateY * timeSteps[step];
			zCoords[step] = zStart+feedRateZ * timeSteps[step];
		}
		
		return new Result(blockTime, blockDistance, timeSteps, xCoords, yCoords, zCoords);
	}

	/**
	 * @param outputBuilder
	 * @param arcResult
	 * @param carouselPocketId
	 * @param spindleSpeed
	 */
	double previousTotalTime = 0.0; //This is needed to avoid duplicates in output file
	private void updateOutput(StringBuilder outputBuilder, Result arcResult, String carouselId, double spindleSpeed) {
		int steps = arcResult.timeSteps.length;
		double currentTotalTime = 0.0;
		for (int step = 0; step < steps; step++){
			currentTotalTime = arcResult.totalTime + arcResult.timeSteps[step];
			// Avoid cases of duplicate when previous block analysis stops at the point that current analysis starts
			// Duplicate occurs when the total time of a block is a multiple of timeStep with an integer
			// (in other words the totalTime%timeStep == 0)
			if (currentTotalTime == previousTotalTime) continue;
			outputBuilder.append( 
					(currentTotalTime) + "," +
					(arcResult.xCoords[step]) + "," + 
					(arcResult.yCoords[step]) + "," + 
					(arcResult.zCoords[step]) + "," +
					carouselId + "," +
					spindleSpeed + "\n" );
		}
		previousTotalTime = arcResult.totalTime + arcResult.timeSteps[steps-1];
	}

	/**
	 * @param clockwise
	 * @param feedRate
	 * @param timeStep
	 * @param xStart
	 * @param yStart
	 * @param xEnd
	 * @param yEnd
	 * @param arcI
	 * @param arcJ
	 */
	private Result analyseArcMovement(
			boolean clockwise, double feedRate, double timeStep, 
			double xStart, double yStart, double zStart, 
			double xEnd, double yEnd, double zEnd,
			double arcI, double arcJ, double arcK) {
		
		//TODO Support for movement on Z axis
		if (zStart != zEnd){
			System.out.println("Only XY plane arcs are supported.");
			return new Result();
		}
		
		double angleStart = Math.atan((yStart - arcJ)/(xStart - arcI)) ;
		double angleEnd = Math.atan((yEnd - arcJ)/(xEnd - arcI)) ;
		double radius = Math.sqrt((xStart - arcI) * (xStart - arcI) + (yStart - arcJ)*(yStart - arcJ));
		
		double angleRate = feedRate / radius;
		double angleStep = angleRate * timeStep;
		
		if (clockwise){
			if (angleEnd >= angleStart) angleStart += 2*Math.PI;
			int steps = 1+ (int) ((angleStart - angleEnd) / angleStep);
			double[] timeSteps = new double[steps];
			double[] xCoords = new double[steps];
			double[] yCoords = new double[steps];
			double[] zCoords = new double[steps];
			int step = 0;
			
			for (double a = angleStart; a >= angleEnd; a -= angleStep){
				timeSteps[step] = timeStep * step;
				xCoords[step] = arcI + radius * Math.cos(a);
				yCoords[step] = arcJ + radius * Math.sin(a);
				zCoords[step] = zStart;
				step++;
			}
			double blockDistance = (angleStart - angleEnd) * radius;
			double blockTime = calculateTime(blockDistance, feedRate);
			return new Result(blockTime, blockDistance, timeSteps, xCoords, yCoords, zCoords);
		} 
		
		if (!clockwise){
			if (angleEnd <= angleStart) angleEnd += 2*Math.PI;
			int steps = 1 + (int) ((angleEnd - angleStart) / angleStep);
			double[] timeSteps = new double[steps];
			double[] xCoords = new double[steps];
			double[] yCoords = new double[steps];
			double[] zCoords = new double[steps];
			int step = 0;
			
			for (double a = angleStart; a <= angleEnd; a += angleStep){
				timeSteps[step] = timeStep * step;
				xCoords[step] = arcI + radius * Math.cos(a);
				yCoords[step] = arcJ + radius * Math.sin(a);
				zCoords[step] = zStart;
				step++;
			}
			double blockDistance = (angleEnd - angleStart) * radius;
			double blockTime = calculateTime(blockDistance, feedRate);
			return new Result(blockTime, blockDistance, timeSteps, xCoords, yCoords, zCoords);
		}
		
		return new Result();
		
	}

	/**
	 * @param xStart - Starting x coordinate (zero if incremental positioning is used)
	 * @param yStart - Starting y coordinate (zero if incremental positioning is used)
	 * @param zStart - Starting z coordinate (zero if incremental positioning is used)
	 * @param xEnd - Target end x coordinate 
	 * @param yEnd - Target end y coordinate 
	 * @param zEnd - Target end z coordinate
	 * @return the linear 3D length of the tool path 
	 */
	private double calculateLinearDistance(
			double xStart, double yStart, double zStart, 
			double xEnd, double yEnd, double zEnd){
		//Calculate distance = sqrt((x2-x1)^2 + (y2-y1)^2 +...) 
		double distance = Math.sqrt(
				Math.pow((xEnd-xStart), 2) +
				Math.pow((yEnd-yStart), 2) +
				Math.pow((zEnd-zStart), 2)
				);
		return distance;
	}


	/**
	 * @param xStart - Starting x coordinate (zero if incremental positioning is used)
	 * @param yStart - Starting y coordinate (zero if incremental positioning is used)
	 * @param zStart - Starting z coordinate (zero if incremental positioning is used)
	 * @param xEnd - Target end x coordinate 
	 * @param yEnd - Target end y coordinate 
	 * @param zEnd - Target end z coordinate
	 * @param arcI - The x coordinate of the centre of the circle that the arc belongs to
	 * @param arcJ - The y coordinate of the centre of the circle that the arc belongs to
	 * @param arcK - The z coordinate of the centre of the circle that the arc belongs to
	 * @param planeYZ - True if planeX is the selected plane
	 * @param planeXZ - True if planeY is the selected plane
	 * @param planeXY - True if planeZ is the selected plane
	 * @return the 3D length of the tool path
	 */
//	public double calculateArcDistance(
//			double xStart, double yStart, double zStart, 
//			double xEnd, double yEnd, double zEnd,
//			double arcI, double arcJ, double arcK,
//			boolean planeXY, boolean planeXZ, boolean planeYZ){
//
//		double radius = 0;
//		if (planeXY){
//			radius = Math.sqrt((xEnd-xStart)*(xEnd-xStart)+(yEnd-yStart)*(yEnd-yStart));
//		}
//		if (planeXZ){
//			radius = Math.sqrt((xEnd-xStart)*(xEnd-xStart)+(zEnd-zStart)*(zEnd-zStart));
//		}
//		if (planeYZ){
//			radius = Math.sqrt((zEnd-zStart)*(zEnd-zStart)+(yEnd-yStart)*(yEnd-yStart));
//		}
//
//		return calculateArcDistance(xStart, yStart, zStart, xEnd, yEnd, zEnd, radius, planeXY, planeXZ, planeYZ);
//	}


	/**
	 * @param xStart - Starting x coordinate (zero if incremental positioning is used)
	 * @param yStart - Starting y coordinate (zero if incremental positioning is used)
	 * @param zStart - Starting z coordinate (zero if incremental positioning is used)
	 * @param xEnd - Target end x coordinate 
	 * @param yEnd - Target end y coordinate 
	 * @param zEnd - Target end z coordinate
	 * @param radius - The radius of the circle that the arc belongs to
	 * @return the 3D length of the tool path
	 */
//	public double calculateArcDistance(
//			double xStart, double yStart, double zStart, 
//			double xEnd, double yEnd, double zEnd,
//			double radius,
//			boolean planeXY, boolean planeXZ, boolean planeYZ){
//
//		double linearDistance = Math.sqrt((xEnd-xStart)*(xEnd-xStart)+(yEnd-yStart)*(yEnd-yStart));
//		if (radius > 0){ //arc less than 180
//			return 2 * radius * Math.asin(linearDistance / (2*radius));
//		} else { //arc more than 180 degrees
//			radius = -radius;
//			return 2 * Math.PI * radius - 2 * radius * Math.asin(linearDistance / (2*radius));
//		}
//
//	}


	/**
	 * @param distance - the length of the tool path
	 * @param feedRate - the feed rate of the tool
	 * @return the time that to cut along the whole path
	 */
	private double calculateTime(double distance, double feedRate) {
		if (feedRate != 0 ){
			return distance/feedRate;
		}
		return 0.0;
	}
	
	class Result {
		double blockTime = 0;
		double blockDistance = 0;
		double totalTime = 0;
		double totalDistance = 0;
		double[] xCoords = null;
		double[] yCoords = null;
		double[] zCoords = null;
		double[] timeSteps = null;
		
		/**
		 * This is the result provided by linear and arc analysers
		 */
		public Result(
				double blockTime,	double blockDistance,
				double[] timeSteps,
				double[] xCoords, double[] yCoords,	double[] zCoords) {
			
			this.blockTime = blockTime;
			this.blockDistance = blockDistance;
			this.timeSteps = timeSteps;
			this.xCoords = xCoords;
			this.yCoords = yCoords;
			this.zCoords = zCoords;
		}

		/**
		 * 
		 */
		public Result() {
			//In case of exception use this constructor to return an empty Result
		}
	}
}

class Groups {
	
	public static final int[] group01 = {0, 1, 2, 3, 12, 13};
	public static final int[] group32 = {90, 91};
	

	public static boolean contains(final int[] group, final int value) {
		for (final int member : group)
			if (member == value) return true;
	    
		return false;
	}
}