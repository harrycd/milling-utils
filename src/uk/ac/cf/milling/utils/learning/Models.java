/**
 * 
 */
package uk.ac.cf.milling.utils.learning;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cf.milling.objects.CuttingTool;
import uk.ac.cf.milling.utils.data.DataManipulationUtils;
import uk.ac.cf.milling.utils.db.CarouselUtils;
import uk.ac.cf.milling.utils.db.MLModelUtils;
import uk.ac.cf.milling.utils.db.MaterialUtils;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Contains utility methods to create, train and manage ML models.
 * @author Theocharis Alexopoulos
 *
 */
public class Models {
	
	/**
	 * Trains a MLModel using Weka. The model is identified by the material,
	 * inputs, target and CuttingTool (obtained by the CSV file)
	 * Warning! This version uses only Bagging classifier without testing
	 * for overfitting or for a better classifier (TODO in future version)
	 * @param materialId - materialId the model is related to
	 * @param dataFilePath - CSV file containing the dataset to train the model
	 * @param inputNames - input parameter names the model is related to
	 * @param targetName - target parameter name the model is related to
	 */
	public static void trainModel(int materialId, String dataFilePath, String[] inputNames, String targetName) {
		
		try {
			DataSource dataSource = new DataSource(dataFilePath);
			Instances instancesAll = dataSource.getDataSet();
			Map<Double, Instances> instancesSplitted = splitInstances(instancesAll);
			
			for (Map.Entry<Double, Instances> entry : instancesSplitted.entrySet()) {
			    //System.out.println(entry.getKey() + "/" + entry.getValue());
				CuttingTool tool = CarouselUtils.getCarouselPocketTool((entry.getKey()).intValue());
				if (tool == null) continue;
				
				String toolSeries = tool.getToolSeries();
				System.out.println("Training model for cutting tool series: " + toolSeries);

				Instances instances = entry.getValue();
				instances = prepareInstances(instances, inputNames, targetName);
				
				// TODO AutoWEKA to select best classifier
				Classifier clf = new Bagging();
//				Classifier clf = new SimpleLinearRegression();
				clf.buildClassifier(instances);
				
				String materialName = MaterialUtils.getMaterial(materialId).getMaterialName();
				String mlModelFilePath = "MLModel" + File.separator 
						+ materialName + "-" + toolSeries + "-" + targetName;
				
				SerializationHelper.write(mlModelFilePath, clf);
				
				MLModelUtils.createOrUpdateMLModel(materialId, toolSeries, inputNames, targetName, mlModelFilePath, instances.numInstances()); 
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * @param filepath
	 * @return
	 * @throws Exception
	 */
	public static Instances prepareInstances(Instances instances, String[] inputNames, String targetName) throws Exception {
		// Create the list of attributes to keep in Instances (SL and MRR)
		List<String> relevantAttributes = new ArrayList<String>(Arrays.asList(inputNames));
		relevantAttributes.add(targetName);

		instances = extractAttributes(instances, relevantAttributes);
		instances.setClass(instances.attribute(targetName));

		// Smoothen the values
		// TODO This is not needed when signal noise is irrelevant or already removed
		for (int i = 0; i < relevantAttributes.size(); i++) {
			applySMA(instances, i, 40);
		}

		return instances;
	}
	
	/**
	 * Separates the instances based on attribute T (cutting tool carousel position id)
	 * @param instancesAll - All instanced generated from the dataset
	 * @return a Map<Double, Instances> whose key is the value of T attribute and the value 
	 * is the relevant instances 
	 */
	private static Map<Double, Instances> splitInstances(Instances instancesAll) {
		Attribute attr = instancesAll.attribute("T");
		
		Map<Double, Instances> map = new HashMap<Double, Instances>();
		
		for (Instance instance : instancesAll) {
			if (!map.containsKey(instance.value(attr))) {
				map.put(instance.value(attr), new Instances(instancesAll, 0));
			}
			
			map.get(instance.value(attr)).add(instance);
		}

		return map;
	}

	/**
	 * Creates a new Instances containing only the Attributes in the List
	 * 
	 * @param instances - the instances to extract attributes from
	 * @param attributesToKeep- the List of attributes to keep
	 * @return a new Instances containing only the attributes in the List
	 */
	public static Instances extractAttributes(Instances instances, List<String> attributesToKeep) {
		
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
		
		// convert to double array
		for (int i = 0; i < instCount; i++) {
			values[i] = data.get(i).value(attributeIndex);
		}
		
		values = DataManipulationUtils.getSimpleMovingAverage(values, windowSize);
		
		// load values back to the instance
		for (int i = 0; i < instCount; i++) {
			Instance inst = data.get(i);
			inst.setValue(attributeIndex, values[i]);
			data.set(i, inst);
		}
	}
	

}
