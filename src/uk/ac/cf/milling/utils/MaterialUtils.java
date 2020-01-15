/**
 * 
 */
package uk.ac.cf.milling.utils;

import java.util.List;

import uk.ac.cf.milling.db.MaterialDB;
import uk.ac.cf.milling.objects.Material;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class MaterialUtils {
	/**
	 * @param materialName - The name of the material to add
	 * @param torqueFactor - A factor used in the calculation of the torque while machining this material
	 * @return the id of the newly added material
	 */
	public static int addMaterial(String materialName, double torqueFactor){
		return new MaterialDB().addMaterial(materialName, torqueFactor);
	}
	
	/**
	 * @param materialId - The id of the material to retrieve
	 * @return
	 */
	public static Material getMaterial(int materialId){
		return new MaterialDB().getMaterial(materialId);
	}

	/**
	 * @return a List of all materials stored in the database
	 */
	public static List<Material> getAllMaterials(){
		return new MaterialDB().getAllMaterials();
	}
	
	/**
	 * @param materialId - the id of the material to update
	 * @param materialName - The new name to update the material with
	 * @param torqueFactor - The new factor used in the calculation of the torque while machining this material
	 */
	public static void updateMaterial(int materialId, String materialName, double torqueFactor){
		new MaterialDB().updateMaterial(materialId, materialName, torqueFactor);
	}
	
	/**
	 * @param materialId - the if of the material to delete
	 */
	public static void deleteMaterial(int materialId){
		new MaterialDB().deleteMaterial(materialId);
	}

}
