/**
 * 
 */
package uk.ac.cf.milling.utils.learning;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import uk.ac.cf.milling.objects.CuttingTool;
import uk.ac.cf.milling.objects.LearningSet;
import uk.ac.cf.milling.utils.data.DataManipulationUtils;
import uk.ac.cf.milling.utils.data.IoUtils;
import uk.ac.cf.milling.utils.db.CarouselUtils;
import uk.ac.cf.milling.utils.db.LearningSetUtils;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Contains utility methods to create, train and manage ML models.
 * @author Theocharis Alexopoulos
 *
 */
public class Models {
	public static int LINEAR_REGRESSION = 1;
	public static int OLS_MULTIPLE_LINEAR_REGRESSION = 2;
	public static int BAGGING = 3;
	
	/**
	 * @param materialId
	 * @param dataFilePath
	 * @param inputNames
	 * @param targetName
	 * @param modelId - ID of selected model (use Models.static_int_parameters)
	 */
	public static void trainModel(int materialId, String dataFilePath, String[] inputNames, String targetName, int modelId) {

		//read and parse the file
		int[] inputIndexes = IoUtils.getCSVTitleIndexes(dataFilePath, inputNames);
		int targetIndex = IoUtils.getCSVTitleIndex(dataFilePath, targetName);
		int toolIndex = IoUtils.getCSVTitleIndex(dataFilePath, "T"); //TODO This should have a global definition
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

//			trainLearningSet(targetValuesPart, inputNames, inputValuesPart, learningSet);
			
			// TODO To do the learning:
			// 1. retrieve the learningSet
			// 2. train the set with the new data
			// 3. write set back to the database

			//			LearningSet learningSet = LearningSetUtils.getOrCreateLearningSet(materialId, cuttingTool.getToolSeries(), targetName, inputNames);

			sampleBatchStart = i;
			currentCarouselPosition = values[toolIndex][i];
		}
	}
	
	private void runWekaBagging(Instances data) throws Exception {
		Bagging clsf = new Bagging();
		clsf.buildClassifier(data);
		
		Instance i = data.get(10);
		double out = clsf.classifyInstance(i);
		System.out.println(out);
		
	}
	
	
	
	
	/* --- Old implementation --- */

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
