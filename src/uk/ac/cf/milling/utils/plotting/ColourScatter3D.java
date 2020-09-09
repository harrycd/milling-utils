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
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

/**
 * @author Theocharis Alexopoulos
 * @date 31 Aug 2020
 *
 */
public class ColourScatter3D {
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
        
        
        /*
         * Just a test to remove outliers in order to improve the colour difference among points
         */
        double[] temp = paramValues.clone();
        Arrays.sort(temp);
//        double median = temp[(int)temp.length/2];
        double median = Arrays.stream(paramValues).average().orElse(Double.NaN);
//        double minValue = temp[0];
        double maxValue = temp[temp.length - 1];

        float r = 0;
        float g = 0;
        float b = 0;
        for (int i = 0; i < pointCount; i++) {
        	points[i] = new Coord3d(toolCoordinates[0][i], toolCoordinates[1][i], toolCoordinates[2][i]);
        	double value = paramValues[i];
        	if (value <= median) {
        		r = 0;
        		g = (float) (value/median);
        		b = (float) ((-value/median) + 1);
        	} else {
        		r = (float) (value/(maxValue));
        		g = (float) ((-value/maxValue) + 1);
        		b = 0;
        	}
        	colors[i] = new Color(r, g, b, 1.0f);
        }
/*		
        double paramValueMax = stats.getMax();
        
        for (int i = 0; i < pointCount; i++) {
        	points[i] = new Coord3d(toolCoordinates[0][i], toolCoordinates[1][i], toolCoordinates[2][i]);
        	float rgbFactor = (float) (paramValues[i]/paramValueMax);
        	colors[i] = new Color(rgbFactor, 0, 1-rgbFactor, 1.0f);
        }
*/		Scatter scatter = new Scatter(points, colors);
		scatter.setWidth(1.5f);
		chart.getScene().add(scatter);
		
		//Fix distortion from non square box
		BoundingBox3d bounds = chart.getView().getBounds();
		float allMax = Math.max(Math.max(bounds.getXmax(), bounds.getYmax()), bounds.getZmax());
		float allMin = Math.min(Math.min(bounds.getXmin(), bounds.getYmin()), bounds.getZmin());
		
		bounds.setXmin(allMin);
		bounds.setXmax(allMax);
		bounds.setYmin(allMin);
		bounds.setYmax(allMax);
		bounds.setZmin(allMin);
		bounds.setZmax(allMax);
		chart.getView().setSquared(false);
		
		//Add zoom and shift functionality
		new ChartXYZZoom(chart);
		chart.addKeyboardCameraController();		
		
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

