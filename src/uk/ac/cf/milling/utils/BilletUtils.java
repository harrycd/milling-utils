/**
 * 
 */
package uk.ac.cf.milling.utils;

import java.util.List;

import uk.ac.cf.milling.db.BilletDB;
import uk.ac.cf.milling.objects.Billet;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class BilletUtils {
	/**
	 * @param billetName - The name of the billet to add
	 * @param materialId - Id of the material the billet is made of
	 * @param billetXMin - Min X coordinate of the billet when placed on table
	 * @param billetXMax - Max X coordinate of the billet when placed on table
	 * @param billetYMin - Min Y coordinate of the billet when placed on table
	 * @param billetYMax - Max Y coordinate of the billet when placed on table
	 * @param billetZMin - Min Z coordinate of the billet when placed on table
	 * @param billetZMax - Max Z coordinate of the billet when placed on table
	 * @return the newly assigned id for the billet
	 */
	public static int addBillet(String billetName, int materialId, double billetXMin, double billetXMax, double billetYMin, double billetYMax, double billetZMin, double billetZMax){
		return new BilletDB().addBillet(billetName, materialId, billetXMin, billetXMax, billetYMin, billetYMax, billetZMin, billetZMax);
	}
	
	/**
	 * @param billetId - The id of the billet to retrieve
	 * @return the retrieved billet
	 */
	public static Billet getBillet(int billetId){
		return new BilletDB().getBillet(billetId);
	}

	/**
	 * @return a List of all billets stored in the database
	 */
	public static List<Billet> getAllBillets(){
		return new BilletDB().getAllBillets();
	}
	
	/**
	 * @param billetId - the id of the billet to update
	 * @param billetName - The new name to update the billet with
	 * @param torqueFactor - The new factor used in the calculation of the torque while machining this billet
	 */
	public static void updateBillet(int billetId, String billetName, int materialId, double billetXMin, double billetXMax, double billetYMin, double billetYMax, double billetZMin, double billetZMax){
		new BilletDB().updateBillet(billetId, billetName, materialId, billetXMin, billetXMax, billetYMin, billetYMax, billetZMin, billetZMax);
	}
	
	/**
	 * @param billetId - the if of the billet to delete
	 */
	public static void deleteBillet(int billetId){
		new BilletDB().deleteBillet(billetId);
	}

}
