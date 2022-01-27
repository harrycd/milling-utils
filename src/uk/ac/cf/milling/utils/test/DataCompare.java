/**
 * 
 */
package uk.ac.cf.milling.utils.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import uk.ac.cf.milling.utils.learning.DataSynchronisation;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class DataCompare {

	/**
	 * @param x0 - x coordinates of reference file
	 * @param y0 - y coordinates of reference file
	 * @param z0 - z coordinates of reference file
	 * @param x1 - x coordinates of comparison file
	 * @param y1 - y coordinates of comparison file
	 * @param z1 - z coordinates of comparison file
	 * @return the relation between the two coordinate sets
	 */
	public static int[][] compare(double[] x0, double[] y0, double[] z0, double[] x1, double[] y1, double[] z1 ) {
		
		long startTime = System.currentTimeMillis();
		long stepTime = startTime;

		double[][] curve0 = new double[3][x0.length];
		curve0[0] = x0;
		curve0[1] = y0;
		curve0[2] = z0;

		double[][] curve1 = new double[3][x1.length];
		curve1[0] = x1;
		curve1[1] = y1;
		curve1[2] = z1;

		int[][] relation = DataSynchronisation.getCurveRelation(curve0, curve1);
		
		System.out.println("DTW took: " + (System.currentTimeMillis() - stepTime) + "msec");
		stepTime = System.currentTimeMillis();

		printRelation(relation, curve0, curve1);
		System.out.println("Printing relation to file took: " + (System.currentTimeMillis() - stepTime) + "msec");
		
//		System.out.println("Total processing time: " + (System.currentTimeMillis() - startTime) + "msec");
		
		return relation;
	}

	private static void printRelation(int[][] relation, double[][] curve1, double[][] curve2){

		int length = relation[0].length;

		String str;
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("relationData.txt"));
			for (int i=0; i<length; i++){
				str = (relation[0][i] + "\t" + relation[1][i]+ 
						"\t" + "\t" + curve1[0][relation[0][i]] + "\t" + curve1[1][relation[0][i]] + "\t" + curve1[2][relation[0][i]]+
						"\t" + "\t" + curve2[0][relation[1][i]] + "\t" + curve2[1][relation[1][i]] + "\t" + curve2[2][relation[1][i]]+
						"\n");
				bw.write(str);
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
