/**
 * 
 */
package uk.ac.cf.milling.utils.test;

import uk.ac.cf.milling.objects.KPIs;
import uk.ac.cf.milling.utils.data.DataManipulationUtils;
import uk.ac.cf.milling.utils.data.IoUtils;

/**
 * Verification tests for dealing with duplicate monitoring data.
 * @author Theocharis Alexopoulos
 *
 */
public class DuplicatePositionMerger {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filePath = "C:\\Users\\Alexo\\OneDrive\\PhD\\NC and Data Files\\CylWearTest_180510\\posi_20180510_0935.csv";
		
		cleanDataFile(filePath);
	}
	
	public static void cleanDataFile(String filePath) {
		KPIs kpis = DataManipulationUtils.parseDataFile(filePath);
		double[] x = kpis.getToolX();
		double[] y = kpis.getToolY();
		double[] z = kpis.getToolZ();
		double[] t = kpis.getTimePoints();
		double[] ss = kpis.getSpindleSpeed();
		double[] xl = kpis.getxLoad();
		double[] yl = kpis.getyLoad();
		double[] zl = kpis.getzLoad();
		double[] sl = kpis.getSpindleLoad();
		String[] c = kpis.getCarouselPocketId();
		
		
		// Needed for calculation of truncated data
		int truncatedRecords = 0;
		double tTrunc = t[0];
		double ssTrunc = ss[0];
		double xlTrunc = xl[0];
		double ylTrunc = yl[0];
		double zlTrunc = zl[0];
		double slTrunc = sl[0];
		
		int dataSize = x.length;
		
		StringBuffer sb = new StringBuffer();
		sb.append("t,X,Y,Z,SS,XL,YL,ZL,SL,T\n");
		
		for (int i = 1, j = 0; i < dataSize; i++, j++) {

			//Check if position changed. If yes, then write truncated data summary
			if (x[i] != x[j] || y[i] != y[j] || z[i] != z[j]) {
				sb.append(
						tTrunc + ","+
						x[j] + "," + 
						y[j] + "," + 
						z[j] + "," +
						ssTrunc + "," +
						xlTrunc + "," +
						ylTrunc + "," +
						zlTrunc + "," +
						slTrunc + "," +
						c[j] + "\n"
				);
				truncatedRecords = 0;
				tTrunc = 0;
			}
			
			// Calculate the way that data is truncated
			truncatedRecords++;

			//for time point keep average of time points
			tTrunc = (tTrunc * (truncatedRecords-1) + t[i]) / truncatedRecords;
			
			//spindle speed keep average
			ssTrunc = (ssTrunc * (truncatedRecords-1) + ss[i]) / truncatedRecords;
			
			//x axis load keep average
			xlTrunc = (xlTrunc * (truncatedRecords-1) + xl[i]) / truncatedRecords;
			
			//y axis load keep average
			ylTrunc = (ylTrunc * (truncatedRecords-1) + yl[i]) / truncatedRecords;
			
			//z axis load keep average
			zlTrunc = (zlTrunc * (truncatedRecords-1) + zl[i]) / truncatedRecords;
			
			//spindle load keep average
			slTrunc = (slTrunc * (truncatedRecords-1) + sl[i]) / truncatedRecords;
		}
		// write the last truncated lines
		sb.append(tTrunc + "," + x[dataSize-1] + "," + y[dataSize-1] + "," + z[dataSize-1] +"\n");
		
		IoUtils.writeFile(filePath + "_clean.csv", sb.toString());
	}
}
