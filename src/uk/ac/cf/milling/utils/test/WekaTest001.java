/**
 * 
 */
package uk.ac.cf.milling.utils.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cf.milling.utils.data.DataManipulationUtils;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SimpleLinearRegression;
//import weka.classifiers.meta.AutoWEKAClassifier;
import weka.classifiers.meta.Bagging;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Thesis results verification tests. Generate a master dataset for other tests.
 * @author Theocharis Alexopoulos
 *
 */
public class WekaTest001 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		//Add all datafiles here so it is easier to refer to.
		Map<String, String> data = new HashMap<>();
		data.put("A0935", "C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CylWearTest_180510\\posi_20180510_0935_clean_smooth_data.csv");
//		data.put("A1009", "C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CylWearTest_180510\\posi_20180510_1009.csv_clean_smooth.csv");
//		data.put("A1043", "C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CylWearTest_180510\\posi_20180510_1043.csv_clean_smooth.csv");
//		data.put("A1117", "C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CylWearTest_180510\\posi_20180510_1117.csv_clean_smooth.csv");
//		data.put("A1149", "C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CylWearTest_180510\\posi_20180510_1149.csv_clean_smooth.csv");
//		data.put("A1222", "C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CylWearTest_180510\\posi_20180510_1222.csv_clean_smooth.csv");
//		data.put("B0922", "C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CK041044\\posi_20180531_0922.csv_clean_smooth.csv");
//		data.put("B0958", "C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CK041044\\posi_20180531_0958.csv_clean_smooth.csv");
//		data.put("B1034", "C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CK041044\\posi_20180531_1034.csv_clean_smooth.csv");
//		data.put("B1109", "C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CK041044\\posi_20180531_1109.csv_clean_smooth.csv");
		
		
		//iterate over all combinations
		for (Map.Entry<String, String> entry : data.entrySet()) {
	        System.out.println(entry.getKey() + ":" + entry.getValue());
	        runExperiment(data.get("A0935"), entry.getValue(), "train_A0935-test_" +  entry.getKey());
	        
	    }
		
		
		
		
	}
	
	static void runExperiment(String trainSetFilePath, String testSetFilePath, String resultsFilePath) throws Exception {
		Instances trainSet = prepareInstances(trainSetFilePath);
		Instances testSet = prepareInstances(testSetFilePath);
		
//		Classifier model = trainBaggingModel(trainSet);
//		Classifier model = trainAutoWekaModel(trainSet);
		Classifier model = trainSimpleLinearRegressionModel(trainSet);
		
		
		// Save and retrieve the model
		//Classifier clsf = (Classifier) SerializationHelper.read("C:\\Users\\Alexo\\Desktop\\model");
		
		writeResults(testSet, model, resultsFilePath);

	}

	/**
	 * @param filepath
	 * @return
	 * @throws Exception
	 */
	public static Instances prepareInstances(String filepath) throws Exception {
		// Create the list of attributes to keep in Instances (SL and MRR)
		List<String> relevantAttributes = new ArrayList<String>();
		relevantAttributes.add("SL");
		relevantAttributes.add("MRR");
		
		DataSource dataSource = new DataSource(filepath);
		Instances instances = dataSource.getDataSet();
		instances = extractAttributes(instances, relevantAttributes);
		instances.setClass(instances.attribute("SL"));
		
		// Smoothen the values
		//applySMA(instances, 0, 40);
		//applySMA(instances, 1, 40);
		
		return instances;
	}

	static Bagging trainBaggingModel(Instances data) throws Exception {
		
		// Train the bagging model
		Bagging clsf = new Bagging();
		clsf.buildClassifier(data);
		
		return clsf;
		
	}
	
	static Classifier trainSimpleLinearRegressionModel(Instances data) throws Exception {
		Classifier slr = new SimpleLinearRegression();
		slr.buildClassifier(data);
		
		return slr;
	}

//	static AutoWEKAClassifier trainAutoWekaModel(Instances data) throws Exception {
//		AutoWEKAClassifier awclf = new AutoWEKAClassifier();
//		awclf.setParallelRuns(1);
//		awclf.setTimeLimit(5);
//		awclf.buildClassifier(data);
//		
//		return awclf;
//	}
	
	private static void writeResults(Instances instances, Classifier clsf, String filename) throws Exception {
		// Add the results to the Instances
		int instCount = instances.numInstances();
		Attribute att = new Attribute("SL_Model");
		int attIndex = 2;
		
		double[] values = new double[instCount];
		for (int i = 0; i < instCount; i++) {
			values[i] = clsf.classifyInstance(instances.get(i));
		}
		
		instances.insertAttributeAt(att, attIndex);
		for (int i = 0; i < instCount; i++) {
			Instance instance = instances.get(i);
			instance.setValue(attIndex, values[i]);
			instances.set(i, instance);
		}
		
		DataSink.write("C:\\Users\\Alexo\\Desktop\\VMC\\" + filename + ".csv", instances);
	}
	
	/**
	 * Creates a new Instances containing only the Attributes in the List
	 * 
	 * @param instances - the instances to extract attributes from
	 * @param attributesToKeep- the List of attributes to keep
	 * @return a new Instances containing only the attributes in the List
	 */
	private static Instances extractAttributes(Instances instances, List<String> attributesToKeep) {
		
		Instances subset = new Instances(instances);

		int numAttributes = subset.numAttributes();
		
		for (int i = numAttributes-1; i >= 0; i--) {
			if (! (attributesToKeep.contains(instances.attribute(i).name()))) {
				subset.deleteAttributeAt(i);
			}
		}
			
		return subset;
		
	}
	
	/**
	 * @param data - the instances containing all data
	 * @param attributeIndex - Attribute to smoothen
	 * @param windowSize - simple moving average period
	 */
	private static void applySMA(Instances data, int attributeIndex, int windowSize) {
		int instCount = data.numInstances();
		double[] values = new double[instCount];
		
		for (int i = 0; i < instCount; i++) {
			values[i] = data.get(i).value(attributeIndex);
		}
		
		values = DataManipulationUtils.getSimpleMovingAverage(values, windowSize);
		
		for (int i = 0; i < instCount; i++) {
			Instance inst = data.get(i);
			inst.setValue(attributeIndex, values[i]);
			data.set(i, inst);
		}
	}
	
}
