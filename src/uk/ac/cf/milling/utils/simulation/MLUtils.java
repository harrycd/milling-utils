/**
 * 
 */
package uk.ac.cf.milling.utils.simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import com.dtw.FastDTW;
import com.dtw.TimeWarpInfo;
import com.dtw.WarpPath;
import com.timeseries.TimeSeries;
import com.timeseries.TimeSeriesPoint;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;

import uk.ac.cf.milling.objects.CuttingTool;
import uk.ac.cf.milling.objects.LearningSet;
import uk.ac.cf.milling.utils.data.DataManipulationUtils;
import uk.ac.cf.milling.utils.data.IoUtils;
import uk.ac.cf.milling.utils.db.CarouselUtils;
import uk.ac.cf.milling.utils.db.LearningSetUtils;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class MLUtils {
	@Deprecated
	public static int[][] getCurveRelation_old(double[][] curve1, double[][] curve2){
		//write csv with data
		String tempFile1 = "Curve_Temp_01.csv";
		String tempFile2 = "Curve_Temp_02.csv";
		
		IoUtils.writeCSVFile(tempFile1, curve1);
		IoUtils.writeCSVFile(tempFile2, curve2);
		
		int radius = 50;
		String distanceFunction = "EuclideanDistance";

		final TimeSeries tsI = new TimeSeries(tempFile1, false, false, ',');
		final TimeSeries tsJ = new TimeSeries(tempFile2, false, false, ',');

		final DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName(distanceFunction);

		final TimeWarpInfo info = FastDTW.getWarpInfoBetween(tsI, tsJ, radius, distFn);
		
		int[][] relation = new int[curve1[0].length][];
		
		List<Integer> relationList1 = info.getPath().getTsIindexes();
		List<Integer> relationList2 = info.getPath().getTsJindexes();
		
		relation[0] = relationList1.stream().mapToInt(i -> i).toArray();
		relation[1] = relationList2.stream().mapToInt(i -> i).toArray();
		
		try {
			Files.delete(Paths.get(tempFile1));
			Files.delete(Paths.get(tempFile2));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return relation;
	}
	
	/**
	 * @param curve1 - array[param index][param values] 
	 * 		holding the values of the first synchronisation parameter
	 * @param curve2- array[param index][param values] 
	 * 		holding the values of the second synchronisation parameter
	 * Example array[2][100] has 2 parameters with 100 values for each parameter
	 * @return int[][] the FastDTW relation between the two (containing the related indexes)
	 */
	public static int[][] getCurveRelation(double[][] curve1, double[][] curve2) {
		
		//Check that the number of parameters is the same
		if (curve1.length != curve2.length) {
			System.out.println("The synchronisation parameter sets have different number of parameters");
			return new int[0][0];
		}
		
		int paramCount = curve1.length;
		int length1 = curve1[0].length;
		int length2 = curve2[0].length;
		TimeSeries ts1 = new TimeSeries(paramCount);
		TimeSeries ts2 = new TimeSeries(paramCount);
		
		for (int valueIndex = 0; valueIndex < length1; valueIndex++ ) {
			
			double[] pointValues = new double[paramCount];
			
			for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
				pointValues[paramIndex] = curve1[paramIndex][valueIndex];
			}
			
			TimeSeriesPoint point = new TimeSeriesPoint(pointValues);
			ts1.addLast(valueIndex, point); //valueIndex is used as time. The method to work simply needs a value that increases to ensure that measurements are sorted.
		}

		for (int valueIndex = 0; valueIndex < length2; valueIndex++ ) {
			
			double[] pointValues = new double[paramCount];
			
			for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
				pointValues[paramIndex] = curve2[paramIndex][valueIndex];
			}
			
			TimeSeriesPoint point = new TimeSeriesPoint(pointValues);
			ts2.addLast(valueIndex, point); //valueIndex is used as time. The method to work simply needs a value that increases to ensure that measurements are sorted.
		}
		
		final WarpPath path = FastDTW.getWarpPathBetween(ts1, ts2, 50, DistanceFunctionFactory.EUCLIDEAN_DIST_FN);
//		final WarpPath path = DTW.getWarpPathBetween(ts1, ts2, DistanceFunctionFactory.EUCLIDEAN_DIST_FN);
		List<Integer> relationList1 = path.getTsIindexes();
		List<Integer> relationList2 = path.getTsJindexes();
		
		int[][] relation = new int[2][];
		relation[0] = relationList1.stream().mapToInt(i -> i).toArray();
		relation[1] = relationList2.stream().mapToInt(i -> i).toArray();
		
		return relation;
	}
	
	/**
	 * @param series1 - array of original values
	 * @param series2 - array of values to warp
	 * @param searchRadius - radius
	 * @param distanceFunction - EuclideanDistance/
	 * @return  
	 */
	public static TimeWarpInfo warpCurves(double[] series1, double[] series2, int searchRadius, String distanceFunction )
    {
		final TimeSeries tsI = new TimeSeries(series1);
		final TimeSeries tsJ = new TimeSeries(series2);

		final DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName(distanceFunction); 

		final TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(tsI, tsJ, searchRadius, distFn);

//		System.out.println("Warp Distance: " + info.getDistance());
//		System.out.println("Warp Path:     " + info.getPath());
		
		info.hashCode();
		
		return info;

    }
	
	/**
	 * Uses recorded data to train the model that calculates the target parameter
	 * @param materialId - material of billet used when data was recorded 
	 * @param dataFilePath - csv file path
	 * @param inputNames - model input parameters
	 * @param targetName - model output parameter (calculated parameter)
	 */
	public static void trainLearningFactors(int materialId, String dataFilePath, String[] inputNames, String targetName) {
		
		//read and parse the file
		int[] inputIndexes = IoUtils.getCSVTitleIndexes(dataFilePath, inputNames);
		int targetIndex = IoUtils.getCSVTitleIndex(dataFilePath, targetName);
		int toolIndex = IoUtils.getCSVTitleIndex(dataFilePath, "T");
		double[][] values = IoUtils.getCSVValues(dataFilePath);	//  values[samples][titles]
		values = DataManipulationUtils.transpose2DArrayValues(values); // values[titles][samples]
		
		//find the carousel positions used (to retrieve relevant cutting tools) 
		double[] carouselPositions = Arrays.stream(values[toolIndex]).distinct().toArray();
		
		//create a map of position - cuttingTool
		Map<Double, CuttingTool> tools = new HashMap<Double, CuttingTool>();
		for (double carouselPosition : carouselPositions) {
			tools.put(carouselPosition, CarouselUtils.getLoadedTool((int)carouselPosition));
		}
		
		//create the input and target arrays
		double[] targetValues = values[targetIndex];
		
		double[][] inputValues = new double[inputNames.length][];
		for (int i = 0; i < inputNames.length; i++) {
			inputValues[i] = values[inputIndexes[i]];
		}
		
		// calculate factors for sample batches with the same cutting tool. 
		// Method: 
		// Iterate over values. 
		// As soon as tool changes pause iteration and calculate factors for the batch of samples with the same cutting tool
		// Continue until end of dataset is reached
		
		int sampleBatchStart = 0;
		double currentCarouselPosition = values[toolIndex][0];
		
		int totalSampleCount = values[0].length;
		for (int i = 0; i < totalSampleCount; i++) {
			
			// Skip if the tool remains the same and if it is not the last sample
			if (currentCarouselPosition == values[toolIndex][i] && i != totalSampleCount-1) continue;
			//The following code runs if the cutting tool changes
			
			CuttingTool cuttingTool = CarouselUtils.getCarouselPocketTool((int) currentCarouselPosition);
			if (cuttingTool == null) {
				System.out.println("Carousel position " + currentCarouselPosition + " empty or cutting tool not found");
				continue;
			}
			
			double[] targetValuesPart = Arrays.copyOfRange(targetValues, sampleBatchStart, i+1);
			double[][] inputValuesPart = copyOfDoubleRange(inputValues, sampleBatchStart, i+1);
			System.out.println("New tool at csv row: " + i);
			
			LearningSet learningSet = LearningSetUtils.getOrCreateLearningSet(materialId, cuttingTool.getToolSeries(), targetName, inputNames);
			trainLearningSet(targetValuesPart, inputNames, inputValuesPart, learningSet);
			
			sampleBatchStart = i;
			currentCarouselPosition = values[toolIndex][i];
		}
	}
	
	/**
	 * @param arr - the source double[][] array
	 * @param rangeStart - initial index of the range to be copied, inclusive
	 * @param rangeEnd -  the final index of the range to be copied, exclusive.(This index may lie outside the array.)
	 * @return a double[arr.length][range] which is a sub array of arr based on the defined range.
	 */
	private static double[][] copyOfDoubleRange(double[][] arr, int rangeStart, int rangeEnd) {
		double[][] rangeArr = new double[arr.length][rangeEnd - rangeStart];
		for (int i = rangeStart,iR = 0; i < rangeEnd; i++, iR++) {
			for (int j = 0; j < arr.length; j++) {
				rangeArr[j][iR] = arr[j][i]; 
			}
		}
		return rangeArr;
	}

	/**
	 * @param targetValuesPart
	 * @param inputNames 
	 * @param inputValuesPart
	 * @param cuttingTool
	 * @param materialId
	 */
	private static void trainLearningSet(double[] targetValuesPart, String[] inputNames, double[][] inputValuesPart, LearningSet learningSet) 
	{
		inputValuesPart = DataManipulationUtils.transpose2DArrayValues(inputValuesPart); //values[samples][titles]
		
		// Remove samples where target = 0 (to avoid 0 = a*0 + c) 
		int cleanTargetLength = 0;
		for (int i = 0; i < targetValuesPart.length; i++) {
			if (targetValuesPart[i] != 0)
				cleanTargetLength++;
		}
		double[] cleanTargetValues = new double[cleanTargetLength];
		double[][] cleanInputValues = new double[cleanTargetLength][inputValuesPart[0].length];

		for (int i = 0, cleanIndex = 0; i < targetValuesPart.length; i++) {
			if (targetValuesPart[i] != 0) {
				cleanTargetValues[cleanIndex] = targetValuesPart[i];
				for (int j = 0; j < inputValuesPart[0].length; j++) {
					cleanInputValues[cleanIndex][j] = inputValuesPart[i][j];
				}
				cleanIndex++;
			}
		}
		
		double[] regressionParameters = getRegressionParameters(cleanTargetValues, cleanInputValues);

		if (regressionParameters != null) {
			Map<String, Double> inputs = new HashMap<String, Double>();
			if (regressionParameters.length > 1) {
				inputs.put("constant", regressionParameters[0]);
				for (int i = 1; i < regressionParameters.length; i++) {
					inputs.put(inputNames[i-1], regressionParameters[i]);
				}
			}
			
			long oldSampleCount = learningSet.getSampleCounter();
			long newSampleCount = targetValuesPart.length;
			Map<String, Double> mergedInputs = mergeInputs(inputs, newSampleCount, learningSet.getInputs(), oldSampleCount);
			learningSet.setInputs((mergedInputs == null) ? inputs : mergedInputs);
			
			if (identicalKeys(inputs, learningSet.getInputs())) {
				//for every input update in a weighed manner 
			}
			learningSet.setSampleCounter(learningSet.getSampleCounter() + targetValuesPart.length);
			LearningSetUtils.updateLearningSet(learningSet);
		}
		
	}

	/**
	 * @param newInputs
	 * @param newSampleCount
	 * @param oldInputs
	 * @param oldSampleCount
	 * @return
	 */
	private static Map<String, Double> mergeInputs(
			Map<String, Double> newInputs, long newSampleCount,
			Map<String, Double> oldInputs, long oldSampleCount) 
	{
		Map<String, Double> mergedInputs = null;
		
		if (identicalKeys(newInputs, oldInputs)) {
			mergedInputs = new HashMap<String, Double>();
			for (Map.Entry<String, Double> input : newInputs.entrySet()) {
				double oldValue = oldInputs.get(input.getKey());
				double newValue = input.getValue();
				mergedInputs.put(
						input.getKey(), 
						(oldValue * oldSampleCount + newValue * newSampleCount) / (oldSampleCount + newSampleCount)
						);
			}
		}
		return newInputs;
	}

	/**
	 * @param inputs
	 * @param inputs2
	 * @return true if the HashMaps' keys are identical
	 */
	private static boolean identicalKeys(Map<String, Double> inputs, Map<String, Double> inputs2) {
		if (inputs.size() != inputs2.size()) return false;
		
		for (String input : inputs.keySet()) {
			if (!inputs2.containsKey(input)) return false;
		}
		return true;
	}

	/**
	 * @param y - a double[] array containing the y
	 * @param x - a double[][] containing x (x[samples/rows][parameters/columns])  
	 * @return the calculated polynomial regression factors (beta)
	 */
	private static double[] getRegressionParameters(double[] y, double[][] x) {
		double[] regressionParameters = null;
		try {
			//ensure that there are enough samples for regression
			if (y.length > 0 && Arrays.stream(y).distinct().count() > x[0].length + 1) {
				OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
				regression.newSampleData(y, x);
				regressionParameters = regression.estimateRegressionParameters();
				System.out.println("Modelled trained with " + y.length + " samples successfully.");
			} else {
				throw new Exception("There are not enough non zero samples");
			}
		} catch (Exception e) {
			System.out.println(y.length + " samples rejected because: " + e.getMessage());
		}
		return regressionParameters;
	}
}
