/**
 * 
 */
package uk.ac.cf.milling.utils.db;

import java.util.List;

import uk.ac.cf.milling.db.MLModelDB;
import uk.ac.cf.milling.objects.MLModel;

/**
 * Methods accessing persistence layer for ML model creation and management.
 * @author Theocharis Alexopoulos
 *
 */
public class MLModelUtils {
	
	/**
	 * Adds a new MLModel to the database
	 * @param mlModel - the MLModel instance to add
	 * @return the database ID of the newly created MLModel instance
	 */
	public static int addMLModel(MLModel mlModel) {
		return new MLModelDB().addMLModel(mlModel);
	}
	
	/**
	 * It creates a new MLModel in the database if mlModelId==0 otherwise updates the existing MLModel
	 * @param mlModel - the MLModel instance to add or update
	 */
	public static void addOrUpdateModel(MLModel mlModel) {
		if (mlModel.getMLModelId() == 0) {
			addMLModel(mlModel);
		} else {
			updateMLModel(
					mlModel.getMLModelId(), 
					mlModel.getMaterialId(), 
					mlModel.getToolSeries(),
					mlModel.getInputNamesStringified(),
					mlModel.getTargetName(), 
					mlModel.getMLModelPath(), 
					mlModel.getSampleCounter());
		}
	}
	
	/**
	 * Retrieves from the database the MLModel with the specified mlModelId
	 * @param mlModelId - the ID of the MLModel to retrieve
	 * @return the retrieved MLModel
	 */
	public static MLModel getMLModel(int mlModelId) {
		return new MLModelDB().getMLModel(mlModelId);
	}
	
	/**
	 * @param materialId
	 * @param toolSeries
	 * @param targetName
	 * @return
	 */
	public static MLModel getMLModel(int materialId, String toolSeries, String targetName) {
		return new MLModelDB().getMLModel(materialId, toolSeries, targetName);
	}
	
	/**
	 * Retrieves from the database MLModels that related to the specified material and toolSeries
	 * @param materialId - the materialId of the MLModel to retrieve
	 * @param toolSeries - the toolSeries of the MLModel to retrieve
	 * @return a List<MLModel> containing the MLModels with the specified materialId and toolSeries
	 */
	public static List<MLModel> getMLModels(int materialId, String toolSeries){
		return new MLModelDB().getMLModels(materialId, toolSeries);
	}
	
	/**
	 * @param materialId
	 * @param toolSeries
	 * @param inputNames
	 * @param targetName
	 * @param mlModelPath
	 * @param sampleCounter
	 */
	public static void createOrUpdateMLModel(int materialId, String toolSeries, String[] inputNames,
			String targetName, String mlModelPath, long sampleCounter) {
		
		MLModel mlModel = getMLModel(materialId, toolSeries, targetName);
		
		if (mlModel.getMLModelId() == 0) { //then it is a new MLModel()
			mlModel.setMaterialId(materialId);
			mlModel.setToolSeries(toolSeries);
			mlModel.setTargetName(targetName);
		} 

		mlModel.setInputNames(inputNames);
		mlModel.setMLModelPath(mlModelPath);
		mlModel.setSampleCounter(sampleCounter);

		addOrUpdateModel(mlModel);
		
	}
	
	/**
	 * @param mlModelId
	 * @param materialId
	 * @param toolSeries
	 * @param inputNames
	 * @param targetName
	 * @param mlModelPath
	 * @param sampleCounter
	 */
	public static void updateMLModel(
			int mlModelId, 
			int materialId, 
			String toolSeries, 
			String inputNames,
			String targetName, 
			String mlModelPath, 
			long sampleCounter) {
		
		new MLModelDB().updateMLModel(mlModelId, materialId, toolSeries, inputNames, targetName, mlModelPath, sampleCounter);
	}
	

	/**
	 * Deletes from the database the MLModel with the specified mlModelId
	 * @param mlModelId - the mlModelId of the MLModel to delete
	 */
	public static void deleteMLModel(int mlModelId) {
		new MLModelDB().deleteMLModel(mlModelId);
	}


}
