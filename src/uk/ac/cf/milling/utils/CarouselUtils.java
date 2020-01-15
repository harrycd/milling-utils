/**
 * 
 */
package uk.ac.cf.milling.utils;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cf.milling.db.CarouselDB;
import uk.ac.cf.milling.db.CuttingToolDB;
import uk.ac.cf.milling.objects.CuttingTool;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class CarouselUtils {
	/**
	 * @param position - the pocket number of the specified tool
	 * @param toolId - the tool to load on that position
	 */
	public static void addCarouselPocket(int position, int toolId){
		new CarouselDB().addCarouselPocket(position, toolId);
	}
	
	/**
	 * @param position - the pocket number of the carousel
	 * @return the id of the tool loaded in the specified pocket
	 */
	public static int getCarouselPocketToolId(int position){
		return new CarouselDB().getCarouselPocketToolId(position);
	}
	
	/**
	 * @param position - the pocket number of the carousel
	 * @return the cutting tool in the specified pocket
	 */
	public static CuttingTool getCarouselPocketTool(int position){
		int toolId = new CarouselDB().getCarouselPocketToolId(position);
		if (toolId == 0) {
//			System.out.println("ERROR: There is no tool in the specified pocket. (pocketId: " + position + ")");
			return null;
		}
		
		CuttingTool cuttingTool = new CuttingToolDB().getCuttingTool(toolId);
		cuttingTool.setProfiles(CuttingToolProfileUtils.getCuttingToolProfiles(toolId));
		return cuttingTool;
	}
	
	/**
	 * @param toolId - the id of the tool to search for
	 * @return the pocket number of the carousel that the tool was found (0 if not found)
	 */
	public static int getCarouselPocketPosition(int toolId){
		return new CarouselDB().getCarouselPocketPosition(toolId);
	}
	
	/**
	 * @return the list of toolIds for the corresponding pocket position
	 */
	public static List<CuttingTool> getLoadedTools(){
		return new CarouselDB().getLoadedTools();
	}
	
	/**
	 * @return
	 */
	public static List<CuttingTool> getAllPockets() {
		List<CuttingTool> tools = new ArrayList<CuttingTool>();
		CarouselDB db = new CarouselDB();
		List<Integer[]> pockets = db.getAllCarouselPockets();
		List<CuttingTool> loadedTools = db.getLoadedTools();
		
		for (Integer[] pocket:pockets){
			if (pocket[1] != 0){
				boolean toolFound = false;
				//find in loaded tools the tool
				for (CuttingTool loadedTool : loadedTools){
					if (loadedTool.getToolId() == pocket[1]){
						tools.add(loadedTool);
						toolFound = true;
						break;
					}
				}
				if(!toolFound) tools.add(new CuttingTool());
			} else {
				tools.add(new CuttingTool());
			}
		}
		return tools;
	}
	
	/**
	 * @param position - the pocket number of the specified tool
	 * @param toolId - the tool to load on that position
	 */
	public static void updateCarouselPocket(int position, int toolId){
		new CarouselDB().updateCarouselPocket(position, toolId);
	}
	
	/**
	 * @param position - the pocket of the carousel to remove
	 */
	public static void deleteCarouselPocket(int position){
		new CarouselDB().deleteCarouselPocket(position);
	}

}
