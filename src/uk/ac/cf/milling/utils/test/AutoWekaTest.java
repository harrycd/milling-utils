/**
 * 
 */
package uk.ac.cf.milling.utils.test;

import weka.classifiers.meta.AutoWEKAClassifier;
import weka.core.Instances;

/**
 * Independently running tests for AutoWEKA.<br>
 * Multiple integration issues with latest version of WEKA.
 * @author Theocharis Alexopoulos
 *
 */
public class AutoWekaTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Instances trainSet = WekaTest001.prepareInstances("C:\\Users\\Alexo\\OneDrive\\PhD - Work\\NC and Data Files\\CylWearTest_180510\\posi_20180510_0935.csv_clean_smooth_data.csv");
		runAutoWeka(trainSet);
	}
	
	private static void runAutoWeka(Instances instances) throws Exception {
		AutoWEKAClassifier clsf = new AutoWEKAClassifier();
		clsf.buildClassifier(instances);
		System.out.println(clsf.totalTried);
		System.out.println(clsf);
	}

}
