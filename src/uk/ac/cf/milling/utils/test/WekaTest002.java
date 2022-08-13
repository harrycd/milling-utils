/**
 * 
 */
package uk.ac.cf.milling.utils.test;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cf.milling.utils.data.DataManipulationUtils;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.meta.Bagging;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Thesis results verification tests. Verify bagging algorithm model and outputs.
 * @author Theocharis Alexopoulos
 * @date 17 Apr 2022
 *
 */
public class WekaTest002 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String filepath = "C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CylWearTest_180510\\posi_20180510_0935.csv_clean_smooth.csv";
		
		// Create the list of attributes to keep in Instances (SL and MRR)
		List<String> relevantAttributes = new ArrayList<String>();
		relevantAttributes.add("SL");
		relevantAttributes.add("MRR");
		
		// Get the instances from the dataset
		DataSource dataSource = new DataSource(filepath);
		Instances instances = dataSource.getDataSet();
		instances = extractAttributes(instances, relevantAttributes);
		instances.setClass(instances.attribute("SL"));
		
		// Smoothen the values
		applySMA(instances, 0, 40);
		applySMA(instances, 1, 40);
		
		// Create a new empty instances
		Instances testInstances = new Instances(instances);
		testInstances.setClass(instances.attribute("SL"));
		testInstances.delete();
		
		// 
		int sampleCount = instances.size();
		double[] baggingValues = new double[sampleCount];
		double[] slrValues = new double[sampleCount];
		
		for (int i = 0; i < sampleCount; i+=50) {
			// Add a batch of samples in the testInstances
			for (int index = i; (index < i+50) && (index < sampleCount); index++) {
				testInstances.add(instances.get(index));
			}
			
			// Train the models with the samples available in testInstances
			Bagging baggingClsf = new Bagging(); baggingClsf.buildClassifier(testInstances);
			SimpleLinearRegression slrClsf = new SimpleLinearRegression(); slrClsf.buildClassifier(testInstances);
			
			for (int j = i; (j < i+50) && (j < sampleCount); j++) {
				baggingValues[j] = baggingClsf.classifyInstance(instances.get(j));
				slrValues[j] = slrClsf.classifyInstance(instances.get(j));
//				if (baggingValues[j] > 0) System.out.println(j + " : " + baggingValues[j]);
			}
		}
		
//		for (int j=0; j<baggingValues.length; j++) {
//			if (baggingValues[j] > 0) System.out.println(j + " : " + baggingValues[j]);
//		}
		
		// Add values to the instances
		instances.insertAttributeAt(new Attribute("SL_Bagging"), 0);
		instances.insertAttributeAt(new Attribute("SL_SLR"), 0);

		for (int i = 0; i < baggingValues.length; i++) {
			Instance instance = instances.get(i);
			instance.setValue(0, slrValues[i]);
			instance.setValue(1, baggingValues[i]);
			instances.set(i, instance);
		}
		
		DataSink.write("C:\\Users\\Alexo\\Desktop\\VMC\\modelTest.csv", instances);
		DataSink.write("C:\\Users\\Alexo\\Desktop\\VMC\\modelTestTemp.csv", testInstances);
		
		

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
