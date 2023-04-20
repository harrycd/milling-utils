/**
 * 
 */
package uk.ac.cf.milling.utils.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import uk.ac.cf.milling.objects.CuttingTool;
import uk.ac.cf.milling.objects.MLModel;
import uk.ac.cf.milling.utils.data.DataManipulationUtils;
import uk.ac.cf.milling.utils.data.IoUtils;
import uk.ac.cf.milling.utils.db.CarouselUtils;
import uk.ac.cf.milling.utils.db.MLModelUtils;
import uk.ac.cf.milling.utils.learning.Models;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * @author Theocharis Alexopoulos
 * @date 17 Apr 2023
 *
 */
public class DerivativeCalculator {

	/**
	 * Calculates the results of the available relevant MLModels and adds a new column in the
	 * provided csv file for each calculated derivative parameter.
	 * @param csvFilePath - The data file containing input data for the models
	 * @param materialId
	 */
	public static void addDerivativeData(String csvFilePath, int materialId) {
		String[] csvHeaders = IoUtils.getCSVTitles(csvFilePath);
		double[][] csvValues = IoUtils.getCSVValues(csvFilePath); //double[rows][columns] 
		csvValues = DataManipulationUtils.transpose2DArrayValues(csvValues); //double[columns][rows] 
		int toolColumnIndex = IntStream
				.range(0, csvHeaders.length)
				.filter(i -> csvHeaders[i].equals("T"))
				.findFirst()
				.orElse(-1);
		
		int[] positions = Arrays.stream(csvValues[toolColumnIndex])
				.distinct()
				.mapToInt(x -> (int) x)
				.toArray();
		
		List<CuttingTool> loadedTools = CarouselUtils.getCarouselPocketTools(positions);
		for (CuttingTool tool : loadedTools) {
			List<MLModel> mlModels = getRelevantMLModels(csvHeaders, materialId, tool.getToolSeries());
			for(MLModel mlModel : mlModels) {
				addDerivativeData(csvFilePath, mlModel);
			}
		}
	}

	private static void addDerivativeData(String csvFilePath, MLModel mlModel) {
		double[] values = null;
		try {
			DataSource dataSource = new DataSource(csvFilePath);
			Instances instances = dataSource.getDataSet();
			List<String> relevantAttributes = new ArrayList<String>(Arrays.asList(mlModel.getInputNames()));
			relevantAttributes.add(mlModel.getTargetName());
			instances = Models.extractAttributes(instances, relevantAttributes);
			
			instances.setClass(instances.attribute(mlModel.getTargetName()));
			
			Classifier clf = (Classifier) SerializationHelper.read(mlModel.getMLModelPath());
			values = new double[instances.numInstances()];
			
			// Use the retrieved classifier to calculate the derivative value
			// TODO IMPORTANT!!! The classifier will use as input the attribute with index equal 
			// to the index of inputs when the model was trained. Therefore it must be ensured that  
			// all inputs have the correct index. Currently, the indexes are correct because of the
			// way that the csv file is built but this may not be the case for multiple inputs from
			// different datafiles.
			for (int i = 0; i < instances.numInstances(); i++) {
				values[i] = clf.classifyInstance(instances.get(i));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Add the derivative value to the data source csv file
		IoUtils.addColumnToCSVFile(csvFilePath, mlModel.getTargetName()+"_est", values);
		
	}
	

	/**
	 * Searches for and returns the MLModels for which input and target data is available in the
	 * provided csv file. If none are found the result will be an empty List.
	 * @param csvHeaders - the parameter names contained in the header of the csv file
	 * @param materialId - the materialId of the MLModels to get
	 * @param toolSeries - the toolSeries of the MLModels to get
	 * @return a List<MLModel> with MLModels for whom both input and target parameters are available
	 */
	private static List<MLModel> getRelevantMLModels(String[] csvHeaders, int materialId, String toolSeries) {
		
		List<MLModel> mlModels = MLModelUtils.getMLModels(materialId, toolSeries);
		List<MLModel> relevantMLModels = new ArrayList<MLModel>();
		
		for (MLModel mlModel : mlModels) {
			String paramsStr = mlModel.getInputNamesStringified() + "," + mlModel.getTargetName();
			String[] params = paramsStr.split(",");
			
			// if all params are in the csvHeaders then all mlModel inputs and target are available
			if (Arrays.asList(csvHeaders).containsAll(Arrays.asList(params))) {
				relevantMLModels.add(mlModel);
			}
		}
		
		return mlModels;

	}

}
