/**
 * 
 */
package uk.ac.cf.milling.utils.db;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cf.milling.db.CuttingToolProfileDB;
import uk.ac.cf.milling.objects.CuttingTool;
import uk.ac.cf.milling.objects.CuttingToolProfile;

/**
 * Methods accessing persistence layer for CuttingToolProfiles creation and management.
 * @author Theocharis Alexopoulos
 *
 */
public class CuttingToolProfileUtils {
	/**
	 * @param toolId - the tool that the profile refers to
	 * @param distanceFromNose - [mm] distance from the nose of the cutting tool
	 * @param insertionsPerTool - number of times that each tooth penetrated the material
	 * @param materialRemoved - [mm] length of travel in the material
	 */
	public static void addCuttingToolProfile(int toolId, double distanceFromNose, double distanceFromCentre, int insertionsPerTooth, double materialRemoved, boolean axialProfile, boolean radialProfile){
		new CuttingToolProfileDB().addCuttingToolProfile(toolId, distanceFromNose, distanceFromCentre, insertionsPerTooth, materialRemoved, axialProfile, radialProfile);
	}
	
	/**
	 * @param profiles - cutting tool profiles to add as batch
	 */
	public static void addCuttingToolProfiles(List<CuttingToolProfile> profiles){
		new CuttingToolProfileDB().addCuttingToolProfiles(profiles);
	}
	
	/**
	 * @param toolId - the tool that the profile refers to
	 * @param distanceFromNose - distance from the nose of the tool
	 * @param distanceFromCentre - distance from the central axis of the tool
	 * @return the profile at the specified position
	 */
	public static CuttingToolProfile getCuttingToolProfile(int toolId, double distanceFromNose, double distanceFromCentre){
		return new CuttingToolProfileDB().getCuttingToolProfile(toolId, distanceFromNose, distanceFromCentre);
	}
	
	/**
	 * @param toolId - tool to return the profiles for
	 * @param axialProfile - if this is an axial profile
	 * @param radialProfile - if this is a radial profile
	 * @return - the cutting tool profile for the specified tool
	 */
	public static List<CuttingToolProfile> getCuttingToolProfiles(int toolId, boolean axialProfile, boolean radialProfile){
		return new CuttingToolProfileDB().getCuttingToolProfiles(toolId, axialProfile, radialProfile);
	}
	
	/**
	 * @param toolId - tool to return the profiles for
	 * @return - all cutting tool profiles for the specified tool
	 */
	public static List<CuttingToolProfile> getCuttingToolProfiles(int toolId){
		return new CuttingToolProfileDB().getCuttingToolProfiles(toolId);
	}
	
	/**
	 * @param toolId - the tool that the profile refers to
	 * @param distanceFromNose - distance from the nose of the cutting tool
	 * @param distanceFromCentre - local radius of the tool
	 * @param insertionsPerTool - number of times that each tooth penetrated the material
	 * @param materialRemoved - length of travel in the material
	 */
	public static void updateCuttingToolProfile(int toolId, double distanceFromNose, double distanceFromCentre, int insertionsPerTooth, double materialRemoved){
		new CuttingToolProfileDB().updateCuttingToolProfile(toolId, distanceFromNose, distanceFromCentre, insertionsPerTooth, materialRemoved);
	}
	
	/**
	 * @param profiles - profiles to update as batch
	 */
	public static void updateCuttingToolProfiles(List<CuttingToolProfile> profiles){
		new CuttingToolProfileDB().updateCuttingToolProfiles(profiles);
	}
	
	/**
	 * @param cuttingTools - list of cutting tools to update their profile in the database
	 */
	public static void updateCuttingToolProfilesFromTool(List<CuttingTool> cuttingTools){
		List<CuttingToolProfile> profiles = new ArrayList<CuttingToolProfile>();
		for (CuttingTool cuttingTool:cuttingTools){
			profiles.addAll(cuttingTool.getProfiles());
		}
		updateCuttingToolProfiles(profiles);
	}
	
	/**
	 * @param toolId - the tool that the profile refers to
	 * @param distanceFromNose - distance from the nose of the cutting tool
	 * @param distanceFromCentre - local radius / distance from the centre of the tool
	 */
	public static void deleteCuttingToolProfile(int toolId, double distanceFromNose, double distanceFromCentre){
		new CuttingToolProfileDB().deleteCuttingToolProfile(toolId, distanceFromNose, distanceFromCentre);
	}
	
	/**
	 * @param toolId - the tool to remove related profiles
	 */
	public static void deleteCuttingToolProfiles(int toolId){
		new CuttingToolProfileDB().deleteCuttingToolProfiles(toolId);
	}
}
