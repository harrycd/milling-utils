/**
 * 
 */
package uk.ac.cf.milling.utils.plotting;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

/**
 * @author Theocharis Alexopoulos
 * @date 31 Aug 2020
 *
 */
public class Parameter3DColourMap {
	/**
	 * @param toolCoordinates - coordinates of tool tip position array[coordinate][value]
	 * @param paramName - name of the parameter to plot
	 * @param paramValues - double[] with the value of the parameter to plot 
	 * @return a JPanel containing the 3D plot
	 */
	public static JPanel getParameter3DColourMap(double[][] toolCoordinates, String paramName, double[] paramValues) {
		Chart chart = AWTChartComponentFactory.chart(Quality.Nicest, IChartComponentFactory.Toolkit.newt);
		int pointCount = toolCoordinates[0].length;
		if (pointCount != paramValues.length) {
			System.out.println("Tool coordinates and parameter values arrays mismatch.");
			return new JPanel();
		}
		
		Coord3d[] points = new Coord3d[pointCount];
        Color[]   colors = new Color[pointCount];
        
        DoubleSummaryStatistics stats = Arrays.stream(paramValues).summaryStatistics();
        //alternatively : Arrays.stream(paramValues).reduce(Double::max);
        double paramValueMax = stats.getMax();
        
        for (int i = 0; i < pointCount; i++) {
   			points[i] = new Coord3d(toolCoordinates[0][i], toolCoordinates[1][i], toolCoordinates[2][i]);
   			int rgbFactor = (int) (255*paramValues[i]/paramValueMax);
   			colors[i] = new Color(rgbFactor, 50, 255-rgbFactor);
        }
		
		Scatter scatter = new Scatter(points, colors);
		chart.getScene().add(scatter);
		
		return getChartPanel(chart);
	}
	
	private static JPanel getChartPanel(Chart chart) {
		Component canvas = (java.awt.Component) chart.getCanvas();

		JPanel chartPanel = new JPanel(new BorderLayout());
		
		Border b = BorderFactory.createLineBorder(java.awt.Color.black);
		chartPanel.setBorder(b);
		chartPanel.add(canvas, BorderLayout.CENTER);
		return chartPanel;
	}

}

