/**
 * 
 */
package uk.ac.cf.milling.utils.db;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cf.milling.db.CuttingToolDB;
import uk.ac.cf.milling.objects.CuttingTool;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class CuttingToolUtils {
	/**
	 * @param toolName - description for the specific tool
	 * @param toolType - type of tool (endmill, ball nose etc.)
	 * @param toolSeries - series (brand code) of tool
	 * @param toolTeeth - number of teeth that the tool has
	 * @param toolLength - the total length of the cutting tool
	 */
	public static int addCuttingTool(String toolName, String toolType, String toolSeries, int toolTeeth, double toolLength){
		return new CuttingToolDB().addCuttingTool(toolName, toolType, toolSeries, toolTeeth, toolLength);
	}
	
	/**
	 * @param toolId - the id of the tool to retrieve
	 * @return the retrieved tool
	 */
	public static CuttingTool getCuttingTool(int toolId){
		CuttingTool cuttingTool = new CuttingToolDB().getCuttingTool(toolId);
		cuttingTool.setProfiles(CuttingToolProfileUtils.getCuttingToolProfiles(toolId));
		return cuttingTool;
	}
	
	/**
	 * @return a List<CuttinTool> containing all tools in database with the attached profiles
	 */
	public static List<CuttingTool> getAllCuttingTools(){
		List<CuttingTool> tools = new CuttingToolDB().getAllCuttingTools();
		for (CuttingTool tool:tools){
			tool.setProfiles(CuttingToolProfileUtils.getCuttingToolProfiles(tool.getToolId()));
		}
		return tools;
	}
	
	/**
	 * @param toolIds
	 * @return
	 */
	public static List<CuttingTool> getCuttingTools(List<Integer> toolIds) {
		List<CuttingTool> tools = new ArrayList<CuttingTool>();
		
		for (int toolId:toolIds){
			tools.add(CuttingToolUtils.getCuttingTool(toolId));
		}
		return tools;
	}
	
	/**
	 * @param toolId - the id of the tool to update
	 * @param toolName - description for the tool
	 * @param toolType - type of tool (endmill, ball nose etc.)
	 * @param toolSeries - series (brand code) of tool
	 * @param toolTeeth - number of teeth that the tool has
	 * @param toolLength TODO
	 */
	public static void updateCuttingTool(int toolId, String toolName, String toolType, String toolSeries, int toolTeeth, double toolLength ) {
		new CuttingToolDB().updateCuttingTool(toolId, toolName, toolType, toolSeries, toolTeeth, toolLength);
	}
	
	/**
	 * @param toolId - the id of the tool to delete
	 */
	public static void deleteCuttingTool(int toolId){
		CuttingToolProfileUtils.deleteCuttingToolProfiles(toolId);
		new CuttingToolDB().deleteCuttingTool(toolId);
	}
}
