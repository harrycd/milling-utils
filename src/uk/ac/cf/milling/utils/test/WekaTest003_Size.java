/**
 * 
 */
package uk.ac.cf.milling.utils.test;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cf.milling.utils.data.DataManipulationUtils;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Thesis results verification tests. Verify SMA algorithm model and outputs.
 * @author Theocharis Alexopoulos
 *
 */
public class WekaTest003_Size {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String filepath = "C:\\Users\\Alexo\\Desktop\\VMC\\sizetest4x.csv";
		
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
		
//		Bagging baggingClsf = new Bagging(); 
//		baggingClsf.buildClassifier(instances);
		
		SimpleLinearRegression slr = new SimpleLinearRegression();
		slr.buildClassifier(instances);
		
		MultilayerPerceptron nn = new MultilayerPerceptron();
		nn.setHiddenLayers("5");
//		System.out.println(nn.getHiddenLayers());
		nn.buildClassifier(instances);
		
		// Save the classifier
		SerializationHelper.write("C:\\Users\\Alexo\\Desktop\\VMC\\slr4x.model", slr);
		SerializationHelper.write("C:\\Users\\Alexo\\Desktop\\VMC\\nn4x.model", nn);

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
