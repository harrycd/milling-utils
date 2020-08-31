/**
 * 
 */
package uk.ac.cf.milling.utils.db;

import uk.ac.cf.milling.db.DB;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class DBUtils {
	public static void createDB(String dbFileName){
		new DB().initialiseDB(dbFileName);
	}
}
