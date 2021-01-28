/**
 * 
 */
package uk.ac.cf.milling.utils.simulation;

import uk.ac.cf.milling.objects.KPIs;
import uk.ac.cf.milling.objects.SimulatorConfig;
import uk.ac.cf.milling.utils.db.SettingUtils;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class KPIUtils {
	public static void updateKPIs(KPIs oldKPIs, KPIs newKPIs){
		if (newKPIs == null) {
			System.out.println("Simulator error: Any results may contain errors.");
			return;
		}
		if (newKPIs.getPart() != null) oldKPIs.setPart(newKPIs.getPart());
		if (newKPIs.getDistance() != 0) oldKPIs.setDistance(newKPIs.getDistance());
		if (!newKPIs.getDistanceUnits().equals("")) oldKPIs.setDistanceUnits(newKPIs.getDistanceUnits());
		if (newKPIs.getTime() != 0) oldKPIs.setTime(newKPIs.getTime());
		if (!newKPIs.getTimeUnits().equals("")) oldKPIs.setTimeUnits(newKPIs.getTimeUnits());
		if (!newKPIs.getAnalysisData().equals("")) oldKPIs.setAnalysisData(newKPIs.getAnalysisData());
		if (newKPIs.getMr() != null) oldKPIs.setMr(newKPIs.getMr());
		if (newKPIs.getMrr() != null) oldKPIs.setMrr(newKPIs.getMrr());
		if (newKPIs.getTimePoints() != null) oldKPIs.setTimePoints(newKPIs.getTimePoints());

		if (newKPIs.getTools() != null) oldKPIs.setTools(newKPIs.getTools());
		if (newKPIs.getCarouselPocketId() != null) oldKPIs.setCarouselPocketId(newKPIs.getCarouselPocketId());
		
		if (newKPIs.getToolX() != null) oldKPIs.setToolX(newKPIs.getToolX());
		if (newKPIs.getToolY() != null) oldKPIs.setToolY(newKPIs.getToolY());
		if (newKPIs.getToolZ() != null) oldKPIs.setToolZ(newKPIs.getToolZ());
		if (newKPIs.getSpindleSpeed() != null) oldKPIs.setSpindleSpeed(newKPIs.getSpindleSpeed());
		
		if (newKPIs.getxLoad() != null) oldKPIs.setxLoad(newKPIs.getxLoad());
		if (newKPIs.getyLoad() != null) oldKPIs.setyLoad(newKPIs.getyLoad());
		if (newKPIs.getzLoad() != null) oldKPIs.setzLoad(newKPIs.getzLoad());
		if (newKPIs.getSpindleLoad() != null) oldKPIs.setSpindleLoad(newKPIs.getSpindleLoad());
	}
	
	/**
	 * @return the material removal rate in mm3 per sec
	 */
//	public static double[] getMrrMM3perSec(KPIs kpis, SimulatorConfig config){
//		/**
//		 * @param mrr - the number of elements machined during the period between previous and current timepoint
//		 * @param timePoints - the array of timepoints that the tool state (position and parameters) is observed
//		 * @return the average removal rate in elements per time unit
//		 */
//		
//		long[] mrr = kpis.getMrr();
//		int length = mrr.length;
//		double[] mrrMM3 = new double[length];
//		
//		double elementSize = SettingUtils.getElementSize();
//		
//		float[] timePoints = kpis.getTimePoints();
//		float timePoint = 0; // current point in time (time from process start)
//		float timeDiff = 0; // difference between current and previous point
//		int index = 0;	//index used to avoid duplicate timepoints
//		
//		// Calculate the volume per second removed
//		for (int i = 0; i < length; i++){
//			timeDiff = timePoints[i] - timePoint;
//			if (timeDiff > 0) {
//				timePoint = timePoints[index];
//				if (mrr[index] > 0) { //do calculation only when necessary
//					mrrMM3[index] = elementSize * elementSize * elementSize * mrr[index] / timeDiff;
//				}
//				index++;
//			}
//		}
//		return mrrMM3;
//	}
	
	/**
	 * @param kpis - the kpis to produce an analysis file from
	 * @return a string containing analysis file contents
	 */
	public static String generateAnalysisFile(KPIs kpis) {
		StringBuilder sb = new StringBuilder();
		
		// Flags
		boolean timePointsFlag = false;
		
		boolean xToolFlag = false;
		boolean yToolFlag = false;
		boolean zToolFlag = false;
		boolean spindleSpeedFlag = false;
		
		boolean xLoadFlag = false;
		boolean yLoadFlag = false;
		boolean zLoadFlag = false;
		boolean spindleLoadFlag = false;

		boolean carouselFlag = false;
		
		boolean mrFlag = false;
		boolean mrrFlag = false;
		
		// Set flag for data available on kpis
		if (kpis.getTimePoints() != null) timePointsFlag = true;
		if (kpis.getToolX() != null) xToolFlag = true;
		if (kpis.getToolY() != null) yToolFlag = true;
		if (kpis.getToolZ() != null) zToolFlag = true;
		if (kpis.getSpindleSpeed() != null) spindleSpeedFlag = true;
		if (kpis.getxLoad() != null) xLoadFlag = true;
		if (kpis.getyLoad() != null) yLoadFlag = true;
		if (kpis.getzLoad() != null) zLoadFlag = true;
		if (kpis.getSpindleLoad() != null) spindleLoadFlag = true;
		if (kpis.getCarouselPocketId() != null) carouselFlag = true;
		if (kpis.getMr() != null) mrFlag=true;
		if (kpis.getMrr() != null) mrrFlag=true;
		
		//Write the title line based on the flag information
		sb.append(timePointsFlag ? "t," : "");
		sb.append(xToolFlag ? "X," : "");
		sb.append(yToolFlag ? "Y," : "");
		sb.append(zToolFlag ? "Z," : "");
		sb.append(spindleSpeedFlag ? "SS," : "");
		sb.append(xLoadFlag ? "XL," : "");
		sb.append(yLoadFlag ? "YL," : "");
		sb.append(zLoadFlag ? "ZL," : "");
		sb.append(spindleLoadFlag ? "SL," : "");
		sb.append(carouselFlag ? "T," : "");
		sb.append(mrFlag ? "MR," : "");
		sb.append(mrrFlag ? "MRR," : "");
		sb.replace(sb.length()-1, sb.length(), "\n");
		
		//Add data lines to string builder
		int length = kpis.getToolX().length;
		for (int index = 0; index < length; index++) {
			if (timePointsFlag) sb.append(kpis.getTimePoints()[index] + ",");
			if (xToolFlag) sb.append(kpis.getToolX()[index] + ",");
			if (yToolFlag) sb.append(kpis.getToolY()[index] + ",");
			if (zToolFlag) sb.append(kpis.getToolZ()[index] + ",");
			if (spindleSpeedFlag) sb.append(kpis.getSpindleSpeed()[index] + ",");
			if (xLoadFlag) sb.append(kpis.getxLoad()[index] + ",");
			if (yLoadFlag) sb.append(kpis.getyLoad()[index] + ",");
			if (zLoadFlag) sb.append(kpis.getzLoad()[index] + ",");
			if (spindleLoadFlag) sb.append(kpis.getSpindleLoad()[index] + ",");
			if (carouselFlag) sb.append(kpis.getCarouselPocketId()[index] + ",");
			if (mrFlag) sb.append(kpis.getMr()[index] + ",");
			if (mrrFlag) sb.append(kpis.getMrr()[index] + ",");
			sb.replace(sb.length()-1, sb.length(), "\n");
		}
		return sb.toString();
	}

}
