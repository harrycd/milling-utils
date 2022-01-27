/**
 * 
 */
package uk.ac.cf.milling.utils.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cf.milling.objects.KPIs;
import uk.ac.cf.milling.utils.db.SettingUtils;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class DataManipulationUtils {
	/**
	 * @param kpis
	 * @return an array containing min and max Non Zero value of tool path.
	 * The array is limits[xmin][xmax][ymin][ymax][zmin][zmax]
	 */
	public static double[][] printToolPathLimits(KPIs kpis){
		double[][] limits = {{Double.MAX_VALUE,-Double.MAX_VALUE},{Double.MAX_VALUE,-Double.MAX_VALUE},{Double.MAX_VALUE,-Double.MAX_VALUE}};
		double[] x = kpis.getToolX();
		double[] y = kpis.getToolY();
		double[] z = kpis.getToolZ();
		for (double d:x){
			if (d == 0) continue;
			if (d < limits[0][0]) limits[0][0] = d; 
			if (d > limits[0][1]) limits[0][1] = d; 
		}
		for (double d:y){
			if (d == 0) continue;
			if (d < limits[1][0]) limits[1][0] = d; 
			if (d > limits[1][1]) limits[1][1] = d; 
		}
		for (double d:z){
			if (d == 0) continue;
			if (d < limits[2][0]) limits[2][0] = d; 
			if (d > limits[2][1]) limits[2][1] = d; 
		}
		System.out.println("Tool path space :");
		System.out.println("Xmin: " + limits[0][0] + "\t Xmax: " + limits[0][1]);
		System.out.println("Ymin: " + limits[1][0] + "\t Ymax: " + limits[1][1]);
		System.out.println("Zmin: " + limits[2][0] + "\t Zmax: " + limits[2][1]);
		return limits;
	}
	
	/**
	 * @param inputFilePath - path to the datafile which timestep is to be calculated 
	 * @return the calculated timestep
	 */
	public static double calculateTimeStep(String inputFilePath) {
		List<String[]> entries = IoUtils.readCSVFile(inputFilePath,0);

		//find column that contains time
		int timeColumnIndex = 0;
		for (int index = 0; index < entries.get(0).length; index++) {
			if (entries.get(0)[index].equals("t")) {
				timeColumnIndex = index;
				break;
			}
		}
		
		double totalTime = Double.parseDouble(entries.get(entries.size()-1)[timeColumnIndex]);
		return totalTime/(entries.size()-1);
	}
//	public static KPIs parseAnalysisFileDepr(String analysisFilePath){
//		KPIs kpis = new KPIs();
//		
//		//Read the analysis file
//		List<String> datablocks = IoUtils.readAnalysisFile(analysisFilePath);
//		int listSize = datablocks.size();
//		
//		//Create the arrays to store file data
//		float[] timePoint = new float[listSize];
//		double[] xTool = new double[listSize];
//		double[] yTool = new double[listSize];
//		double[] zTool = new double[listSize];
//		String[] toolId = new String[listSize];
//		double[] spindleSpeed = new double[listSize];
//		
//		// Iterate over the lines of the file and parse the values
//		for (int i = 0; i < listSize; i++){
//			String[] block = datablocks.get(i).split("\t");
//			timePoint[i] = Float.parseFloat(block[0]);
//			xTool[i] = Double.parseDouble(block[1]);
//			yTool[i] = Double.parseDouble(block[2]);
//			zTool[i] = Double.parseDouble(block[3]);
//			toolId[i] = block[4];
//			spindleSpeed[i] = Double.parseDouble(block[5]);
//		}
//		
//		// Set the kpi parameters so data can be saved.
//		kpis.setTimePoints(timePoint);
//		kpis.setToolX(xTool);
//		kpis.setToolY(yTool);
//		kpis.setToolZ(zTool);
//		kpis.setCarouselPocketId(toolId);
//		kpis.setSpindleSpeed(spindleSpeed);
//		
//		return kpis;
//	}
//	
//	/**
//	 * @param jacobFilePath - the path to the input file
//	 * @return the updated KPIs after parsing the file
//	 */
//	public static KPIs parseJacobFileDepr(String jacobFilePath){
//		
//		KPIs kpis = new KPIs();
//		int firstDataRowIndex = 0;
//		int timeColumnIndex = 0;
//		int xColumnIndex = 0;
//		int yColumnIndex = 0;
//		int zColumnIndex = 0;
//		int toolIdIndex = 0;
//		int spindleSpeedColumnIndex = 0;
//		int spindleLoadIndex = 0;
//		int xLoadIndex = 0;
//		int yLoadIndex = 0;
//		int zLoadIndex = 0;
//		
//		//Read the analysis file
//		List<String[]> dataBlocks = IoUtils.readCSVFile(jacobFilePath, 0);
//		String[] formatVersion = dataBlocks.get(0);
//		
//		// Configure parsing based on the format of the file
//		if (formatVersion[0].equals("*LL1HB0C4J*")){
//			/*
//			 * The labels in the file correspond to the following
//			 * t             total program time                 %08.3f        (s)
//			 * T             tool in spindle                    %03i          ---
//			 * Tt            tool usage time                    %08.3f        (s)
//			 * RS            Spindle rotational speed          %09.3f        (0.001rpm)
//			 * FR            Resultant feed-rate (XYZ)         %07.1f        (0.1mm/min)
//			 * SL            Spindle motor load                %03i          (%)
//			 * XL            X-axis motor load                 %03i          (%)
//			 * YL            Y-axis motor load                 %03i          (%)
//			 * ZL            Z-axis motor load                 %03i          (%)
//			 * X             X-axis position                   %09.3f        (mm)
//			 * Y             Y-axis position                   %09.3f        (mm)
//			 * Z             Z-axis position                   %09.3f        (mm)
//			 * 
//			 */
//			firstDataRowIndex = 9;
//			timeColumnIndex = 0;
//			xColumnIndex = 9;
//			yColumnIndex = 10;
//			zColumnIndex = 11;
//			toolIdIndex = 1;
//			spindleSpeedColumnIndex = 3;
//			
//			spindleLoadIndex = 5;
//			xLoadIndex = 6;
//			yLoadIndex = 7;
//			zLoadIndex = 8;
//			
//			
//			// Remove the title rows to keep only the data
//			for (int i = 0; i < firstDataRowIndex; i++){
//				dataBlocks.remove(0);
//			}
//		}
//		else {
//			return kpis;
//		}
//		
//		
//		/*
//		 * Parse the List based on the configuration
//		 * The section below should be common for all file formats
//		 */
//		
//		int listSize = dataBlocks.size();
//		
//		//Create the arrays to store file data
//		float[] timePoint = new float[listSize];
//		String[] pocketId = new String[listSize];
////		String currentPocketId = "";
//		double toolLength = 0;
//		double[] xTool = new double[listSize];
//		double[] yTool = new double[listSize];
//		double[] zTool = new double[listSize];
//		
//		double[] spindleSpeed = new double[listSize];
//		
//		double[] spindleLoad = new double[listSize];
//		double[] xLoad = new double[listSize];
//		double[] yLoad = new double[listSize];
//		double[] zLoad = new double[listSize];
//		
////		CuttingTool tool = null;
//		
//		// To display progress
//		JProgressBar progressBar = SettingUtils.getProgressBar();
//		progressBar.setVisible(true);
//		int i = 0;
//		int fivePerCentBlock = (int) (listSize * 0.05);
//		boolean repetitiveError = false;
//
//		// Iterate over the lines of the file and parse the values
//		for (String[] block : dataBlocks){
//			// Progress calculation
//			if (i % fivePerCentBlock == 0) {
//				progressBar.setValue(100*i/listSize);
//			}
//			
//			timePoint[i] = Float.parseFloat(block[timeColumnIndex]);
//			pocketId[i] = block[toolIdIndex];
//			
//			if (!pocketId[i].equals(currentPocketId)){
//				
//				tool = null;
//				
//				if (Integer.parseInt(pocketId[i]) != 0){
//					tool = CarouselUtils.getCarouselPocketTool(Integer.parseInt(pocketId[i]));
//				}
//				
//				if (tool == null) {
//					if (repetitiveError){
//						//don't print anything because the log is cluttered
//					} else {
//						System.out.print("Unable to process data file at line(s): " + i + "-");
//						repetitiveError = true;
//					}
//					i++;
//					continue;
//				}
//				toolLength = tool.getToolLength();
//				currentPocketId = pocketId[i];
//			}
//			xTool[i] = Double.parseDouble(block[xColumnIndex]);
//			yTool[i] = Double.parseDouble(block[yColumnIndex]);
//			zTool[i] = toolLength + Double.parseDouble(block[zColumnIndex]);
//			spindleSpeed[i] = Double.parseDouble(block[spindleSpeedColumnIndex]);
//			
//			xLoad[i] = Double.parseDouble(block[xLoadIndex]);
//			yLoad[i] = Double.parseDouble(block[yLoadIndex]);
//			zLoad[i] = Double.parseDouble(block[zLoadIndex]);
//			spindleLoad[i] = Double.parseDouble(block[spindleLoadIndex]);
//			
//			if (repetitiveError){
//				System.out.println(i + " Continuing...");
//				repetitiveError = false;
//			}
//			i++;
//		}
//		if (repetitiveError) System.out.print(i + "\n");
//		progressBar.setVisible(false);
//		
//		// Set the kpi parameters so data can be saved.
//		kpis.setTimePoints(timePoint);
//		kpis.setToolX(xTool);
//		kpis.setToolY(yTool);
//		kpis.setToolZ(zTool);
//		kpis.setCarouselPocketId(pocketId);
//		kpis.setSpindleSpeed(spindleSpeed);
//		kpis.setxLoad(xLoad);
//		kpis.setyLoad(yLoad);
//		kpis.setzLoad(zLoad);
//		kpis.setSpindleLoad(spindleLoad);
//		
//		return kpis;
//	}
	
	/**
	 * @param filePath - the path of the csv file containing data
	 * @return KPIs from the specified file
	 */
	public static KPIs parseDataFile(String filePath){
		//Get all data from file
		List<String[]> entries = IoUtils.readCSVFile(filePath,0);

		//identify the column index for each parameter
		Map<String, Integer> indexes = getIndexes(entries.get(0));
		int xIndex = indexes.getOrDefault("X", -1);
		int yIndex = indexes.getOrDefault("Y", -1);
		int zIndex = indexes.getOrDefault("Z", -1);
		
		int xLoadIndex = indexes.getOrDefault("XL", -1);
		int yLoadIndex = indexes.getOrDefault("YL", -1);
		int zLoadIndex = indexes.getOrDefault("ZL", -1);
		
		int sLoadIndex = indexes.getOrDefault("SL", -1);
		int sSpeedIndex = indexes.getOrDefault("SS", -1);
		
		int timeIndex = indexes.getOrDefault("t", -1);
		int pocketIndex = indexes.getOrDefault("T", -1);
		int mRRIndex = indexes.getOrDefault("MRR", -1);
		
		//remove title line to keep only data
		entries.remove(0);
		
		//Initialise the arrays to store data
		int listSize = entries.size();
		double[] xTool = new double[listSize];
		double[] yTool = new double[listSize];
		double[] zTool = new double[listSize];
		
		double[] xLoad = new double[listSize];
		double[] yLoad = new double[listSize];
		double[] zLoad = new double[listSize];
		
		double[] spindleSpeed = new double[listSize];
		double[] spindleLoad = new double[listSize];
		
		double[] timePoints = new double[listSize];
		String[] pocketIds = new String[listSize];
		double[] mrr = new double[listSize];
		
		int counter = 0;
		
		for (String[] entry:entries){
			if (xIndex > -1){
				xTool[counter] = Double.parseDouble(entry[xIndex]);
				yTool[counter] = Double.parseDouble(entry[yIndex]);
				zTool[counter] = Double.parseDouble(entry[zIndex]);
			}
			
			if (xLoadIndex > -1){
				xLoad[counter] = Double.parseDouble(entry[xLoadIndex]);
				yLoad[counter] = Double.parseDouble(entry[yLoadIndex]);
				zLoad[counter] = Double.parseDouble(entry[zLoadIndex]);
			}
			
			if (sLoadIndex > -1)
				spindleLoad[counter] = Double.parseDouble(entry[sLoadIndex]);

			if (sSpeedIndex > -1)
				spindleSpeed[counter] = Double.parseDouble(entry[sSpeedIndex]);
			
			if (timeIndex > -1)
				timePoints[counter] = Float.parseFloat(entry[timeIndex]);
			
			if (pocketIndex > -1)
				pocketIds[counter] = entry[pocketIndex];
			
			if (mRRIndex > -1)
				mrr[counter] = Double.parseDouble(entry[mRRIndex]);
			
			counter++;
		}
		
		// Set the kpi parameters so data can be saved.
		KPIs kpis = new KPIs();
		kpis.setTimePoints(timePoints);
		kpis.setToolX(xTool);
		kpis.setToolY(yTool);
		kpis.setToolZ(zTool);
		kpis.setCarouselPocketId(pocketIds);
		kpis.setSpindleSpeed(spindleSpeed);
		kpis.setxLoad(xLoad);
		kpis.setyLoad(yLoad);
		kpis.setzLoad(zLoad);
		kpis.setSpindleLoad(spindleLoad);
		return kpis;
		
	}
	
	/**
	 * @param filePath - the data file path to clean
	 * 
	 * Removes data entries that have the same tool coordinates.
	 * It recalculates all other data parameters
	 */
	/**
	 * 
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getCleanFromDuplicateCoordinatesData(String filePath) {
		KPIs kpis = DataManipulationUtils.parseDataFile(filePath);
		double[] x = kpis.getToolX();
		double[] y = kpis.getToolY();
		double[] z = kpis.getToolZ();
		double[] t = kpis.getTimePoints();
		double[] ss = kpis.getSpindleSpeed();
		double[] xl = kpis.getxLoad();
		double[] yl = kpis.getyLoad();
		double[] zl = kpis.getzLoad();
		double[] sl = kpis.getSpindleLoad();
		String[] c = kpis.getCarouselPocketId();
		
		
		// Needed for calculation of truncated data
		int truncatedRecords = 0;
		double tTrunc = t[0];
		double ssTrunc = ss[0];
		double xlTrunc = xl[0];
		double ylTrunc = yl[0];
		double zlTrunc = zl[0];
		double slTrunc = sl[0];
		
		int dataSize = x.length;
		
		StringBuffer sb = new StringBuffer();
		sb.append("t,X,Y,Z,SS,XL,YL,ZL,SL,T\n");
		
		for (int i = 1, j = 0; i < dataSize; i++, j++) {

			//Check if position changed. If yes, then write truncated data summary
			if (x[i] != x[j] || y[i] != y[j] || z[i] != z[j]) {
				sb.append(
						tTrunc + ","+
						x[j] + "," + 
						y[j] + "," + 
						z[j] + "," +
						ssTrunc + "," +
						xlTrunc + "," +
						ylTrunc + "," +
						zlTrunc + "," +
						slTrunc + "," +
						c[j] + "\n"
				);
				truncatedRecords = 0;
				tTrunc = 0;
			}
			
			// Calculate the way that data is truncated
			truncatedRecords++;

			//for time point keep average of time points
			tTrunc = (tTrunc * (truncatedRecords-1) + t[i]) / truncatedRecords;
			
			//spindle speed keep average
			ssTrunc = (ssTrunc * (truncatedRecords-1) + ss[i]) / truncatedRecords;
			
			//x axis load keep average
			xlTrunc = (xlTrunc * (truncatedRecords-1) + xl[i]) / truncatedRecords;
			
			//y axis load keep average
			ylTrunc = (ylTrunc * (truncatedRecords-1) + yl[i]) / truncatedRecords;
			
			//z axis load keep average
			zlTrunc = (zlTrunc * (truncatedRecords-1) + zl[i]) / truncatedRecords;
			
			//spindle load keep average
			slTrunc = (slTrunc * (truncatedRecords-1) + sl[i]) / truncatedRecords;
		}
		// write the last truncated lines
		sb.append(
				tTrunc + ","+
				x[dataSize-1] + "," + 
				y[dataSize-1] + "," + 
				z[dataSize-1] + "," +
				ssTrunc + "," +
				xlTrunc + "," +
				ylTrunc + "," +
				zlTrunc + "," +
				slTrunc + "," +
				c[dataSize-1] + "\n"
		);
		
		return sb.toString();
	}
	
	
	/**
	 * Fills gaps in toolpath. Gaps occur when the coordinates of two samples
	 * are so distant that material remains between the two points. This can
	 * happen when the machine is cutting very fast or when the element size
	 * is very small.
	 * 
	 * The gaps are filled so maximum distance between two samples is elemsize
	 * 
	 * Interpolation is linear.
	 * 
	 * @param filePath - path to the csv file to fill gaps in toolpath
	 * 
	 */
	public static void interpolateToolpathPoints(String filePath) {
		
		double elemsize = SettingUtils.getElementSize();
		String[] titles = IoUtils.getCSVTitles(filePath);
		double[][] data = IoUtils.getCSVValues(filePath);

		List<String> params = new ArrayList<String>();
		params.add("t");
		params.add("X");
		params.add("Y");
		params.add("Z");
		List<Integer> indexes = findTitleIndex(params, titles);
		
		int tIndex = indexes.get(0);
		int xIndex = indexes.get(1);
		int yIndex = indexes.get(2);
		int zIndex = indexes.get(3);
		
		List<String[]> entries = new ArrayList<String[]>();
		entries.add(titles); //add CSV titles
		entries.add(strignify(data[0])); //add the toolpath starting point
		
		//Loop through all data and find gaps in toolpath
		for (int i = 1; i < data.length; i++) {
			
			//calculate distance
			double tDiff = data[i][tIndex] - data[i-1][tIndex];
			double xDist = data[i][xIndex] - data[i-1][xIndex];
			double yDist = data[i][yIndex] - data[i-1][yIndex];
			double zDist = data[i][zIndex] - data[i-1][zIndex];
			
			double maxDist = Math.max(xDist, Math.max(yDist, zDist));
			if (maxDist > elemsize) {
				int segments = (int) (1 + maxDist/elemsize);
				
				double tStart = data[i-1][tIndex];
//				double tEnd = data[i][tIndex];
				
				double xStart = data[i-1][xIndex];
				double xEnd = data[i][xIndex];
				
				double yStart = data[i-1][yIndex];
				double yEnd = data[i][yIndex];
				
				double zStart = data[i-1][zIndex];
				double zEnd = data[i][zIndex];
				
				double tIncr = tDiff / segments;
				double xIncr = (xEnd - xStart) / segments;
				double yIncr = (yEnd - yStart) / segments;
				double zIncr = (zEnd - zStart) / segments;
				
				for (int j = 1; j < segments; j++) {
					double[] dim = data[i];
					
					dim[tIndex] = tStart + j * tIncr;
					
					dim[xIndex] = xStart + j * xIncr;
					dim[yIndex] = yStart + j * yIncr;
					dim[zIndex] = zStart + j * zIncr;
					
					entries.add(strignify(dim));
				}
				
			} else {
				entries.add(strignify(data[i]));
			}
		}

		filePath = filePath.substring(0, filePath.length() - 4) + "_smooth.csv";
		IoUtils.writeCSVFile(filePath, entries);
		
	}
	
	/**
	 * @param ds - double[] array to convert
	 * @return the converted double[] to String[] array
	 */
	private static String[] strignify(double[] ds) {
		String[] result = new String[ds.length];
		
		for (int i = 0; i < ds.length; i++) {
			result[i] = Double.toString(ds[i]);
		}
		return result;
	}

	/**
	 * @param params - parameters to look for
	 * @param titles - titles that the parameters should be found in
	 * @return 
	 */
	public static List<Integer> findTitleIndex(List<String> params, String[] titles){

		List<Integer> indexes = new ArrayList<Integer>();

		for (String param : params) {
			indexes.add(findTitleIndex(param, titles));
		}

		return indexes;
	}
	
	/**
	 * @param param - parameter title to look for
	 * @param titles - array containing String titles
	 * @return the index of specified parameter. Returns -1 if parameter title not found.
	 */
	public static int findTitleIndex(String param, String[] titles) {
		for (int index = 0; index < titles.length; index++) {
			if (param.equals(titles[index])){
				return index;
			}
		}
		return -1;
	}
	
	/**
	 * Returns all values of a specified second dimension of an array
	 * @param array - double[][] array to copy second dimension from
	 * @param index - index of second dimension to copy
	 */
	public static double[] copySecondDimension(double[][] array, int index) {
		double[] dim = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			dim[i] = array[i][index];
		}
		return dim;
	}

	/**
	 * @param values - a double[][] array
	 * @return the double[][] transpose of the input array
	 */
	public static double[][] transpose2DArrayValues(double[][] values){
		double[][] transpose = null;
		try {
			if (values == null || values[0] == null) 
				throw new Exception("The source array is null or empty");

			int rows = values.length;
			int columns = values[0].length;

			transpose = new double[columns][rows];
			
			for (int row = 0; row < rows; row++) {
				for (int column = 0; column < columns; column++) {
					transpose[column][row] = values[row][column];
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return transpose;
	}
	
	/**
	 * @param titles String array containing the titles
	 * @return a Map containing the titles as key and their index as value
	 */
	private static Map<String, Integer> getIndexes(String[] titles){
		Map<String, Integer> indexes = new HashMap<String, Integer>();
		for (int i = 0; i < titles.length; i++){
			indexes.put(titles[i], i);
		}
		return indexes;
	}
	
	 /**
	 * @param arr - array containing String values
	 * @param val - the value to find in the Array
	 * @return true if array contains the provided value
	 */
	public static boolean containsEqual(String arr[], String val)
	   {
	      for (int i = 0; i < arr.length; i++)
	         if (arr[i].equals(val))
	            return true;

	      return false;
	   }
	
	/**
	 * @param values - double array with original values
	 * @param averagingPeriod - number of samples to calculate the average from
	 * @return a new double array containing the CMA
	 */
	public static double[] getCenteredMovingAverage(double[] values, int averagingPeriod) {
		return getDisplacedMovingAverage(values, averagingPeriod, (int) -averagingPeriod/2);
	}
	
	/**
	 * @param values - double array with original values
	 * @param averagingPeriod - number of samples to calculate the average from
	 * @param displacement - shift of moving average
	 * @return a new double array containing the DMA 
	 */
	public static double[] getDisplacedMovingAverage(double[] values, int averagingPeriod, int displacement) {
		values = getSimpleMovingAverage(values, averagingPeriod);
		int vLength = values.length;
		double[] dma = new double[vLength];
		int srcPos = averagingPeriod -1;
		int destPos = srcPos + displacement;
		int copyLength = (displacement > 0) ? vLength-srcPos-displacement : vLength-srcPos;
		System.arraycopy(values, srcPos, dma, destPos, copyLength);
		
		return dma;
	}
	
	/**
	 * @param values - a double array with original values
	 * @param averagingPeriod - number of samples used to calculate the average
	 * @return a double array with the smoothened values
	 */
	public static double[] getSimpleMovingAverage(double[] values, int averagingPeriod) {
		int length = values.length;
		double[] sma = new double[length];
		//Calculate the sma
		for (int i = averagingPeriod-1; i < length; i++){
			for (int j = 0; j < averagingPeriod; j++){
				sma[i] += values[i-j];
			}
			sma[i] /= averagingPeriod;
		}
		return sma;
	}

}
