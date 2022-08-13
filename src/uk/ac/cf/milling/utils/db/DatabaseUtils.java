/**
 * 
 */
package uk.ac.cf.milling.utils.db;

import uk.ac.cf.milling.db.DB;

/**
 * High level access to database creation.
 * @author Theocharis Alexopoulos
 *
 */
public class DatabaseUtils {
	public static void createDB(String dbFileName, String elementSize, String timeStep){
		new DB().initialiseDB(dbFileName);
		SettingUtils.updateSetting("elementSize", elementSize);
		SettingUtils.updateSetting("timeStep", timeStep);
	}
	
	public static void initialiseDB(String dbFileName) {
		new DB().initialiseDB(dbFileName);
	}
}
