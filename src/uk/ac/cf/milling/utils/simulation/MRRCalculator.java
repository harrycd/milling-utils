/**
 * 
 */
package uk.ac.cf.milling.utils.simulation;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JProgressBar;

import uk.ac.cf.milling.objects.Billet;
import uk.ac.cf.milling.objects.CuttingTool;
import uk.ac.cf.milling.objects.CuttingToolProfile;
import uk.ac.cf.milling.objects.KPIs;
import uk.ac.cf.milling.objects.SimulatorConfig;
import uk.ac.cf.milling.utils.db.CarouselUtils;
import uk.ac.cf.milling.utils.db.SettingUtils;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class MRRCalculator {

	/**
	 * @param args
	 */
	public static KPIs calculateMRR(KPIs kpis, SimulatorConfig config) {

		Billet billet = config.getBillet();
		//These are the columns of the temporary file parsed separately.
		float[] timePoints = kpis.getTimePoints();
		double[] xSpindles = kpis.getToolX(); 
		double[] ySpindles = kpis.getToolY(); 
		double[] zSpindles = kpis.getToolZ();
		double[] spindleSpeed = kpis.getSpindleSpeed();
		
		String[] carouselPocketIds = kpis.getCarouselPocketId(); //The position on the carousel (not the actual carouselPocketId)
		
		
		int analysisFileLines = timePoints.length;

		// Accuracy of calculation
		double elemSize = SettingUtils.getElementSize(); //mm

		//For performance purposes the cutting tool parameters are kept separately
		//The initialisation is needed here so cutting tool can be accessed throughout the method
		List<CuttingTool> usedCuttingTools = new ArrayList<CuttingTool>();
		CuttingTool cuttingTool = null;
		String carouselPocketId = ""; 
		double toolLocalRadius = 0;
		List<CuttingToolProfile> cuttingToolAxialProfile = null;
		List<CuttingToolProfile> cuttingToolRadialProfile = null;
		double teeth_x_TimeStep_div_by_60 = 0; // Used to calculate insertions per tool
		
		/*
		 * Billet: The billet is a boolean[][][] matrix that has true for machined elements and false for non machined
		 * The calculator takes into consideration the minimum and maximum (x,y,z) coordinates.
		 * This way it knows the limits of the billet in space
		 * Then it generates a mesh of points starting from the minimum (x,y,z) and expanding in a cubic way
		 * until they fill the whole billet.
		 * The number of elements comprising each dimension of the billet is 
		 * (xBilletMax - xBilletMin)/elemSize where elemSize is the size of each element
		 * 
		 * Billet variable is the most memory hungry part of the app.   
		 */
		
		// Billet parameters
		double xBilletMin = billet.getXBilletMin();
//		double xBilletMax = billet.getXBilletMax();
		double yBilletMin = billet.getYBilletMin();
//		double yBilletMax = billet.getYBilletMax();
		double zBilletMin = billet.getZBilletMin();
		double zBilletMax = billet.getZBilletMax();
		

		// Initialise material removal rate array
		long[] mrr = new long[analysisFileLines];

		//Generate the mesh and assign it to billet.part
		billet.generateMesh();

		// Number of elements in each dimension
		boolean[][][] part = billet.getPart();
		int xBilletElCount = part.length;
		int yBilletElCount = part[0].length;
		int zBilletElCount = part[0][0].length;

		// To display progress
		JProgressBar progressBar = SettingUtils.getProgressBar();
		int fivePerCentLine = (int) (analysisFileLines * 0.05);
		if (fivePerCentLine == 0) fivePerCentLine = 1; //to avoid division by zero
		
		/*
		 * Declare variables outside the loop to improve performance
		 */
		
		float timePoint = 0;//Current point in time (seconds from process start)
		float timeDiff = 0;	//The difference between current and previous time point
		double xSpindle = 0; 	//The x coordinate of the tool
		double ySpindle = 0; 	//The y coordinate of the tool
		double zSpindle = 0; 	//The z coordinate of the tool
		
		double dxTool = 0; 	//The x axis difference between previous and current position 
		double dyTool = 0; 	//The y axis difference between previous and current position 
		double dzTool = 0; 	//The z axis difference between previous and current position 
		double dxyzTool = 0;//The total distance the tool moved from previous position
		double axialRatio = 0; 	// How much of the usage is axial
		double radialRatio = 0;	// How much of the usage is radial
		int radialToolEl = 0; 	// The index of the radial element
		int insertions = 0; 	//The number of insertions per tooth
		
		boolean errorReported = false;
		
		
		// Iterate through every block and calculate the MRR and the billet machined elements
		for (int line = 0; line < analysisFileLines; line++){
			// Progress calculation
			if (line%fivePerCentLine == 0) {
				progressBar.setValue(100*line/analysisFileLines);
			}
			
			timeDiff = timePoints[line] - timePoint; //timePoint here has the previous value
			timePoint = timePoints[line];
			//To avoid checking if current is the 1st line, value of xyzTool is used to represent the previous
			dxTool = xSpindles[line] - xSpindle; 
			dyTool = ySpindles[line] - ySpindle;
			dzTool = Math.abs(zSpindles[line] - zSpindle);
			xSpindle = xSpindles[line]; 
			ySpindle = ySpindles[line]; 
			zSpindle = zSpindles[line];
			
			dxyzTool = Math.sqrt(dxTool*dxTool + dyTool*dyTool + dzTool*dzTool);
			radialRatio = dzTool / dxyzTool;
			axialRatio = 1 - radialRatio;
			
			
			// Load the new cutting tool and profile if pocketId has changed
			if (!carouselPocketId.equals(carouselPocketIds[line])){
				
				//This checks if cutting tool field is empty
				cuttingTool = null;
				if(!carouselPocketIds[line].equals(""))
					cuttingTool = CarouselUtils.getCarouselPocketTool((int) Double.parseDouble(carouselPocketIds[line]));
				
				if (cuttingTool == null){
					if (!errorReported) {
						System.err.println("Simulation error: Carousel pocket " + carouselPocketIds[line] + " is empty or does not exist!");
						errorReported = true;
					}
					
					continue;
				}
				errorReported = false;
				carouselPocketId = carouselPocketIds[line];
				cuttingToolAxialProfile = cuttingTool.getAxialProfile();
				cuttingToolRadialProfile = cuttingTool.getRadialProfile();
				
				teeth_x_TimeStep_div_by_60 = cuttingTool.getToolTeeth() * timeDiff / 60;
				
				if(!usedCuttingTools.contains(cuttingTool))
					usedCuttingTools.add(cuttingTool);
			}
			
			//Check if tool has reached the billet. If not continue to next block
			//The nose of the tool is below zTool
			double zToolNoseCoord = zSpindle - cuttingToolAxialProfile.get(cuttingToolAxialProfile.size() -1).getDistanceFromNose();
			if ( zToolNoseCoord > zBilletMax) continue;
			
			// Iterate over every z coordinate of tool
			// Declaring variables outside the loop to improve performance
			int zToolElCount = cuttingToolAxialProfile.size(); // the number of elements comprising tool z axis
			double xToolElCoord = 0; 		// the x coordinate of the element to examine if it machines the billet
			double xToolCoordMin = 0; 	// the min x coordinate of tool at the examined z coordinate
			double xToolCoordMax = 0; 	// the max x coordinate of tool at the examined z coordinate 

			double yToolElCoord = 0; 		// the y coordinate of the element to examine if it machines the billet
			double yToolCoordMin = 0; 	// the min y coordinate of tool at the examined z coordinate
			double yToolCoordMax = 0; 	// the max y coordinate of tool at the examined z coordinate
			
			double zToolElCoord = zSpindle;	// z coordinate of the element to examine if it machines the billet
//			double zToolCoordMin = toolZ;	// the min z coordinate of tool the examined tool position
//			double zToolCoordMax = toolZ + toolAxialProfile.get(toolAxialProfile.size()-1).getDistanceFromBase();	// the max z coordinate of tool at the examined tool position
			
			double xDistFromToolCentre = 0; 		// distance on x axis between examined tool element and tool position
			double yDistFromToolCentre = 0; 		// distance on y axis between examined tool element and tool position
			double radialDistFromToolCentre = 0; 	// radial distance between examined tool element and tool central axis
			
			int xPartEl = 0; // X position of the part's element
			int yPartEl = 0; // Y position of the part's element
			int zPartEl = 0; // Z position of the part's element

			for (int zToolEl = 0; zToolEl < zToolElCount; zToolEl++, zToolElCoord -= elemSize){
				//radius of the tool at this specific z coordinate
				toolLocalRadius = cuttingToolAxialProfile.get(zToolEl).getDistanceFromCentre();
				
				//look if for the specified z coordinate, the billet elements fall within the tool elements
				//Initially, a square profile of the tool is created and if the billet falls within the square limits
				//more detailed search is done to check if it falls within circle limits.
				xToolCoordMin = xSpindle - toolLocalRadius;
				xToolCoordMax = xSpindle + toolLocalRadius;
				xToolElCoord = xToolCoordMin; //resets the coordinate to min when restarting iteration

				yToolCoordMin = ySpindle - toolLocalRadius;
				yToolCoordMax = ySpindle + toolLocalRadius;
				yToolElCoord = yToolCoordMin; //resets the coordinate to min when restarting iteration
				

				while (xToolElCoord <= xToolCoordMax){
					while (yToolElCoord <= yToolCoordMax){
						xDistFromToolCentre = xToolElCoord - xSpindle;
						yDistFromToolCentre = yToolElCoord - ySpindle;
						radialDistFromToolCentre = xDistFromToolCentre * xDistFromToolCentre + yDistFromToolCentre * yDistFromToolCentre; //SQRT is not needed because x > y => sqrt(x) > sqrt(y) when x,y > 0
						if ((radialDistFromToolCentre) <= toolLocalRadius * toolLocalRadius){ //Check if within tool radius
							xPartEl = (int) ((xToolElCoord - xBilletMin)/elemSize);
							yPartEl = (int) ((yToolElCoord - yBilletMin)/elemSize);
							zPartEl = (int) ((zToolElCoord - zBilletMin)/elemSize);
							if (
									xPartEl >= 0 && xPartEl < xBilletElCount &&	//Is within billet array for x axis
									yPartEl >= 0 && yPartEl < yBilletElCount &&	//Is within billet array for y axis
									zPartEl >= 0 && zPartEl < zBilletElCount &&	//Is within billet array for z axis
									!part[xPartEl][yPartEl][zPartEl]){ 			//Check if already machined
											
								/*
								 *  Here add code to run whenever an element is machined
								 */
								// Change status of billet element to machined
								part[xPartEl][yPartEl][zPartEl] = true;
								
								//Add the machined element to the material removal array
								mrr[line] += 1;
								
								//Add the machined element to the tool usage
								radialToolEl = (int)(Math.sqrt(radialDistFromToolCentre)/elemSize);
								cuttingToolAxialProfile.get(zToolEl).appendMaterialRemoved(elemSize * axialRatio);
								cuttingToolRadialProfile.get(radialToolEl).appendMaterialRemoved(elemSize * radialRatio);
								
								//Record the insertions per tooth on the respective axis of movement
								insertions = (int) (spindleSpeed[line] * teeth_x_TimeStep_div_by_60);
								cuttingToolAxialProfile.get(zToolEl).appendInsertionsPerTooth((int) (insertions * axialRatio));
								cuttingToolRadialProfile.get(radialToolEl).appendInsertionsPerTooth((int) (insertions * radialRatio));
								/*
								 * insertions for radial profile are less towards the centre
								 * this is because of central elements machining less billet elements
								 * and therefore the point of the code where insertions are appended 
								 * to radial usage is not reached. this is correct based on algorithm
								 * but wrong according to real machining. In general this should not 
								 * affect the final result as milling toold do not typically wear at
								 * the centre of the nose
								 */
							}
						}
						yToolElCoord += elemSize;
					}
					xToolElCoord += elemSize;
					yToolElCoord = yToolCoordMin; //reset
				}
			}
			timePoints[line] = timePoint;
		}
//		tools = normaliseToolMRR(tools, timePoints);
		
		kpis.setMrr(mrr);
		kpis.setPart(part);
		kpis.setTimePoints(timePoints);
		kpis.setTools(usedCuttingTools);
		return kpis;
	}
}
