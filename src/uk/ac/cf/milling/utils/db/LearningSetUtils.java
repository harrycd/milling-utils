/**
 * 
 */
package uk.ac.cf.milling.utils.db;

import java.util.HashMap;
import java.util.Map;

import uk.ac.cf.milling.db.LearningSetDB;
import uk.ac.cf.milling.objects.LearningSet;

/**
 * @author Theocharis Alexopoulos
 * @date 27 Aug 2020
 *
 */
public class LearningSetUtils {
	
	public static int addLearningSet(LearningSet learningSet) {
		return new LearningSetDB().addLearningSet(learningSet);
	}
	
	public static LearningSet getLearningSet(int learningSetId) {
		return new LearningSetDB().getLearningSet(learningSetId);
	}
	
	public static LearningSet getLearningSet(int materialId, String toolSeries, String targetName) {
		return new LearningSetDB().getLearningSet(materialId, toolSeries, targetName);
	}

	/**
	 * @param materialId - material id of the learning set to retrieve
	 * @param toolSeries - tool series of the learning set to retrieve
	 * @param targetName - target parameter name of the learning set to retrieve
	 * @return the unique learning set defined by the materialId, toolSeries, targetName.
	 * If the learning set doesn't exist a new one is created containing only th
	 */
	public static LearningSet getOrCreateLearningSet(int materialId, String toolSeries, String targetName, String[] inputNames) {
		LearningSet learningSet = getLearningSet(materialId, toolSeries, targetName);
		if (learningSet.getLearningSetId() == 0) {
			learningSet.setMaterialId(materialId);
			learningSet.setToolSeries(toolSeries);
			learningSet.setTargetName(targetName);
			
			//Inputs needed to create full structure in database
			Map<String, Double> inputs = new HashMap<String, Double>();
			for (String inputName : inputNames) inputs.put(inputName, 0.0);
			learningSet.setInputs(inputs);
			
			int learningSetId = LearningSetUtils.addLearningSet(learningSet);
			learningSet.setLearningSetId(learningSetId);
		}
		return learningSet;
	}
	
	public static void updateLearningSet(LearningSet learningSet) {
		new LearningSetDB().updateLearningSet(learningSet);
	}
	
	public static void deleteLearningSet(int learningSetId) {
		new LearningSetDB().deleteLearningSet(learningSetId);
	}


}
