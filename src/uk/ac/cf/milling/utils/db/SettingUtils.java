/**
 * 
 */
package uk.ac.cf.milling.utils.db;

import javax.swing.JProgressBar;

import uk.ac.cf.milling.db.SettingsDB;
import uk.ac.cf.milling.objects.SettingsSingleton;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class SettingUtils {
	//Kept as reference to avoid checking for correct spelling
	public static String ELEMENT_SIZE = "elementSize";
	public static String TIME_STEP = "timeStep";
	
	/**
	 * @param settingId - the setting to retrieve
	 * @return the value of the specified setting
	 */
	public static String getSetting(String settingId){
		return new SettingsDB().getSetting(settingId);
	}
	
	/**
	 * @param settingId - setting to update
	 * @param value - value of the specified setting
	 */
	public static void updateSetting(String settingId, String value ) {
		new SettingsDB().updateSetting(settingId, value);
	}
	
	/**
	 * @return the file path for the database currently in use
	 */
	public static String getDbFilePath(){
		return SettingsSingleton.getInstance().dbFilePath;
	}
	
	/**
	 * @param dbFilePath - the file path of the database currently in use
	 */
	public static void setDbFilePath(String dbFilePath){
		SettingsSingleton.getInstance().dbFilePath = dbFilePath;
	}
	
	/**
	 * @return The reference to the progress bar displayed on the GUI
	 */
	public static JProgressBar getProgressBar(){
		return SettingsSingleton.getInstance().progressBar;
	}
	
	/**
	 * @param progressBar - JProgressbar to keep reference so status can be updated
	 */
	public static void setProgressBar(JProgressBar progressBar){
		SettingsSingleton.getInstance().progressBar = progressBar;
	}

	
	/*
	 * Convenience functions
	 */
	
	public static double getElementSize(){
		return Double.parseDouble(getSetting(ELEMENT_SIZE));
	}
	
	public static double getTimeStep(){
		return Double.parseDouble(getSetting(TIME_STEP));
	}

	public static void setTimeStep(String timeStep){
		updateSetting(TIME_STEP, timeStep);
	}
	
}
