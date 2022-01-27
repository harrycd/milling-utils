/**
 * 
 */
package uk.ac.cf.milling.utils.test;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cf.milling.utils.data.DataManipulationUtils;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * @author Theocharis Alexopoulos
 * @date 21 Jan 2022
 *
 */
public class WekaTest001 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		DataSource source = new DataSource("C:\\Users\\Alexo\\Desktop\\wekatest.csv");
		
		
		
		Instances trainSet = prepareInstances("C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CylWearTest_180510\\posi_20180510_0935.csv_clean_smooth_data.csv");
		Instances testSet = prepareInstances("C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CK041044\\posi_20180531_1109.csv_clean_smooth_data.csv");
		
		Classifier model = trainBaggingModel(trainSet);
		
		// Save and retrieve the model
		SerializationHelper.write("C:\\Users\\Alexo\\Desktop\\model", model);
		Classifier clsf = (Classifier) SerializationHelper.read("C:\\Users\\Alexo\\Desktop\\model");
		
		writeResults(testSet, clsf, "Train.0935-Test.1109.test");
		
	}

	/**
	 * @param filepath
	 * @return
	 * @throws Exception
	 */
	private static Instances prepareInstances(String filepath) throws Exception {
		// Create the list of attributes to keep in Instances (SL and MRR)
		List<String> relevantAttributes = new ArrayList<String>();
		relevantAttributes.add("SL");
		relevantAttributes.add("MRR");
		
		DataSource dataSource = new DataSource(filepath);
		Instances instances = dataSource.getDataSet();
		instances = extractAttributes(instances, relevantAttributes);
		instances.setClass(instances.attribute("SL"));
		
		// Smoothen the values
		applySMA(instances, 0, 40);
		applySMA(instances, 1, 40);
		
		return instances;
	}

	static Bagging trainBaggingModel(Instances data) throws Exception {
		

		
		// Train the bagging model
		Bagging clsf = new Bagging();
		clsf.buildClassifier(data);
		
		return clsf;
		
	}
	
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
		
		DataSink.write("C:\\Users\\Alexo\\Desktop\\" + filename + ".csv", instances);
	}
	
	/**
	 * Creates a new Instances with containing only the Attributes in the List
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
	 * @param averagingPeriod - simple moving average period
	 */
	private static void applySMA(Instances data, int attributeIndex, int averagingPeriod) {
		int instCount = data.numInstances();
		double[] values = new double[instCount];
		
		for (int i = 0; i < instCount; i++) {
			values[i] = data.get(i).value(attributeIndex);
		}
		
		values = DataManipulationUtils.getSimpleMovingAverage(values, averagingPeriod);
		
		for (int i = 0; i < instCount; i++) {
			Instance inst = data.get(i);
			inst.setValue(attributeIndex, values[i]);
			data.set(i, inst);
		}
	}
}
