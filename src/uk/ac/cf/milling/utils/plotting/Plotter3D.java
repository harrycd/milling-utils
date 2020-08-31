/**
 * 
 */
package uk.ac.cf.milling.utils.plotting;

import javax.swing.JPanel;

import uk.ac.cf.milling.objects.Billet;

/**
 * @author Theocharis Alexopoulos
 * @date 30 Aug 2020
 *
 */
public class Plotter3D {
	public static JPanel getV1ChartPanel(boolean[][][] part2plot, Billet billet2plot) {
		return Plotter3D_V1.getChartPanel(part2plot, billet2plot);
	}
	
	public static JPanel getV2ChartPanel(boolean[][][] part) {
		return Plotter3D_V2.getChartPanel(part);
	}

	public static JPanel getV3ChartPanel(boolean[][][] part) {
		return Plotter3D_V3.getChartPanel(part);
	}

	public static JPanel getV4ChartPanel(boolean[][][] part) {
		return Plotter3D_V4.getChartPanel(part);
	}
	
//	public static JPanel getParameterColourMapPanel(double toolCoordinates, String paramName, double[] paramValues) {
//		
//	}

}
