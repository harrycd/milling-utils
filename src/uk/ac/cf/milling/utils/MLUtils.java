/**
 * 
 */
package uk.ac.cf.milling.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.dtw.DTW;
import com.dtw.FastDTW;
import com.dtw.TimeWarpInfo;
import com.dtw.WarpPath;
import com.timeseries.TimeSeries;
import com.timeseries.TimeSeriesPoint;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class MLUtils {
	@Deprecated
	public static int[][] getCurveRelation_old(double[][] curve1, double[][] curve2){
		//write csv with data
		String tempFile1 = "Curve_Temp_01.csv";
		String tempFile2 = "Curve_Temp_02.csv";
		
		IoUtils.writeCSVFile(tempFile1, curve1);
		IoUtils.writeCSVFile(tempFile2, curve2);
		
		int radius = 50;
		String distanceFunction = "EuclideanDistance";

		final TimeSeries tsI = new TimeSeries(tempFile1, false, false, ',');
		final TimeSeries tsJ = new TimeSeries(tempFile2, false, false, ',');

		final DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName(distanceFunction);

		final TimeWarpInfo info = FastDTW.getWarpInfoBetween(tsI, tsJ, radius, distFn);
		
		int[][] relation = new int[curve1[0].length][];
		
		List<Integer> relationList1 = info.getPath().getTsIindexes();
		List<Integer> relationList2 = info.getPath().getTsJindexes();
		
		relation[0] = relationList1.stream().mapToInt(i -> i).toArray();
		relation[1] = relationList2.stream().mapToInt(i -> i).toArray();
		
		try {
			Files.delete(Paths.get(tempFile1));
			Files.delete(Paths.get(tempFile2));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return relation;
	}
	
	/**
	 * @param curve1 - array[param index][param values] 
	 * 		holding the values of the first synchronisation parameter
	 * @param curve2- array[param index][param values] 
	 * 		holding the values of the second synchronisation parameter
	 * Example array[2][100] has 2 parameters with 100 values for each parameter
	 * @return int[][] the FastDTW relation between the two (containing the related indexes)
	 */
	public static int[][] getCurveRelation(double[][] curve1, double[][] curve2) {
		
		//Check that the number of parameters is the same
		if (curve1.length != curve2.length) {
			System.out.println("The synchronisation parameter sets have different number of parameters");
			return new int[0][0];
		}
		
		int paramCount = curve1.length;
		int length1 = curve1[0].length;
		int length2 = curve2[0].length;
		TimeSeries ts1 = new TimeSeries(paramCount);
		TimeSeries ts2 = new TimeSeries(paramCount);
		
		for (int valueIndex = 0; valueIndex < length1; valueIndex++ ) {
			
			double[] pointValues = new double[paramCount];
			
			for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
				pointValues[paramIndex] = curve1[paramIndex][valueIndex];
			}
			
			TimeSeriesPoint point = new TimeSeriesPoint(pointValues);
			ts1.addLast(valueIndex, point); //valueIndex is used as time. The method to work simply needs a value that increases to ensure that measurements are sorted.
		}

		for (int valueIndex = 0; valueIndex < length2; valueIndex++ ) {
			
			double[] pointValues = new double[paramCount];
			
			for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
				pointValues[paramIndex] = curve2[paramIndex][valueIndex];
			}
			
			TimeSeriesPoint point = new TimeSeriesPoint(pointValues);
			ts2.addLast(valueIndex, point); //valueIndex is used as time. The method to work simply needs a value that increases to ensure that measurements are sorted.
		}
		
		final WarpPath path = FastDTW.getWarpPathBetween(ts1, ts2, 50, DistanceFunctionFactory.EUCLIDEAN_DIST_FN);
//		final WarpPath path = DTW.getWarpPathBetween(ts1, ts2, DistanceFunctionFactory.EUCLIDEAN_DIST_FN);
		List<Integer> relationList1 = path.getTsIindexes();
		List<Integer> relationList2 = path.getTsJindexes();
		
		int[][] relation = new int[2][];
		relation[0] = relationList1.stream().mapToInt(i -> i).toArray();
		relation[1] = relationList2.stream().mapToInt(i -> i).toArray();
		
		return relation;
	}
	
	/**
	 * @param series1 - array of original values
	 * @param series2 - array of values to warp
	 * @param searchRadius - radius
	 * @param distanceFunction - EuclideanDistance/
	 * @return  
	 */
	public static TimeWarpInfo warpCurves(double[] series1, double[] series2, int searchRadius, String distanceFunction )
    {
		final TimeSeries tsI = new TimeSeries(series1);
		final TimeSeries tsJ = new TimeSeries(series2);

		final DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName(distanceFunction); 

		final TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(tsI, tsJ, searchRadius, distFn);

//		System.out.println("Warp Distance: " + info.getDistance());
//		System.out.println("Warp Path:     " + info.getPath());
		
		info.hashCode();
		
		return info;

    }
}
