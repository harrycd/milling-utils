/**
 * 
 */
package uk.ac.cf.milling.utils.db;

import java.util.List;

import uk.ac.cf.milling.db.NcDB;
import uk.ac.cf.milling.objects.Nc;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class NcUtils {
	/**
	 * @param ncPath - file path of the numerical control file
	 * @param analysisPath - file path of the simulator analysis file
	 * @param monitoringPath - file path of the file containing machine monitoring data
	 * @return the id of the numerical control file
	 */
	public static int addNcFile(String ncPath, String analysisPath, String monitoringPath, int billetId){
		return new NcDB().addNcFile(ncPath, analysisPath, monitoringPath, billetId);
	}

	/**
	 * Attempts to retrieve the ncId for the NC with the specified file path.
	 * If retrieval is not successful, a new NC is created and its id is returned
	 * 
	 * @param ncPath - file path of the numerical control file
	 * @return the id of the numerical control file retrieved or created
	 */
	public static Nc getCreateNcFile(String ncPath){
		NcDB ncDB = new NcDB();
		Nc nc = ncDB.getNc(ncPath);
		if (nc.getNcId() == 0) {
			//if no entry found then create one
			int ncId = new NcDB().addNcFile(ncPath, "", "", 0);
			return ncDB.getNc(ncId);
		} else {
			//else if entry exists return the existent entry
			return nc;
		}
	}
	
	/**
	 * @param ncId - id of numerical control file
	 * @return the Nc object specified by the id
	 */
	public static Nc getNc(int ncId){
		return new NcDB().getNc(ncId);
	}
	
	/**
	 * @param ncPath - file path of numerical control file
	 * @return the Nc object specified by the id
	 */
	public static Nc getNc(String ncPath){
		return new NcDB().getNc(ncPath);
	}
	
	/**
	 * @return a list of all NC programs in the database
	 */
	public static List<Nc> getNcs(){
		return new NcDB().getNcs();
	}
	
	/**
	 * @param ncId - id of numerical control file
	 * @param ncPath - file path of the numerical control file
	 * @param analysisPath - file path of the simulator analysis file
	 * @param monitoringPath - file path of the file containing machine monitoring data
	 * @param billetId - Id of the billet used to produce analysis file
	 */
	public static void updateNc(int ncId, String ncPath, String analysisPath, String monitoringPath, int billetId){
		new NcDB().updateNc(ncId, ncPath, analysisPath, monitoringPath, billetId);
	}
	
	/**
	 * @param ncId - id of numerical control file
	 * @param ncPath - file path of the numerical control file
	 * @param billetId - Id of the billet used to produce analysis file
	 */
	public static void updateNcPath(int ncId, String ncPath, int billetId) {
		new NcDB().updateNcPath(ncId, ncPath, billetId);
	}

	/**
	 * @param ncId - id of numerical control file
	 * @param analysisPath - file path of the simulator analysis file
	 * @param monitoringPath - file path of the file containing machine monitoring data
	 * @param billetId - Id of the billet used to produce analysis file
	 */
	public static void updateNcAnalysis(int ncId, String analysisPath, int billetId){
		new NcDB().updateNcAnalysis(ncId, analysisPath, billetId);
	}
	
	/**
	 * @param ncId - id of nc to delete
	 */
	public static void deleteNc(int ncId){
		new NcDB().deleteNc(ncId);
	}
	
}
