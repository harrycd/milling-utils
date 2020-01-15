/**
 * 
 */
package uk.ac.cf.milling.utils;

import uk.ac.cf.milling.db.DB;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class DatabaseUtils {
	public static void createDB(String dbFileName, String elementSize, String timeStep){
		new DB().initialiseDB(dbFileName);
		SettingUtils.updateSetting("elementSize", elementSize);
		SettingUtils.updateSetting("timeStep", timeStep);
	}
}
