/**
 * Contains utilities to plot data for verification
 */
package uk.ac.cf.milling.utils.plotting;

import java.awt.Component;

import javax.swing.JFrame;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.rendering.canvas.Quality;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class PlotUtilsold {
	
	/**
	 * @param x - array containing x coordinates
	 * @param y - array containing y coordinates
	 * @param z - array containing z coordinates
	 */
	public static void plot(double x[], double y[], double z[]){
		Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, IChartComponentFactory.Toolkit.swing);
		chart.addMouseCameraController();
		Coord3d[] points = create3dPoints(x,y,z);
		LineStrip line = createLineStrip(points, new Color(200, 200, 200));
		chart.getScene().getGraph().add(line);
		showChartFrame(chart);
	}
	
	/**
	 * @param data the coordinates of points. The internal list expects double[] x, double[] y, double[] z>>  
	 */
	public static void plot(double[][] data){
		Chart chart = AWTChartComponentFactory.chart(Quality.Nicest, IChartComponentFactory.Toolkit.newt);
//		Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, IChartComponentFactory.Toolkit.swing);
		chart.addMouseCameraController();
//		int numOfDatasets = data.length/3;
		for (int i = 0; i < data.length; i+=3){
			Coord3d[] points = create3dPoints(data[i], data[i+1], data[i+2]);
			LineStrip line = createLineStrip(points, new Color((int)(255*Math.random()), (int)(255*Math.random()), (int)(255*Math.random())));
			chart.getScene().getGraph().add(line);
		}
		showChartFrame(chart);
	}
	
	/**
	 * @param y - array containing y values 
	 * @return array of Coord3d points where x:{0, 1, 2...}, y:{y values}, z:{0, 0, 0...}
	 */
	public static Coord3d[] createPoints(double[] y){
		int size = y.length;
		Coord3d[] points = new Coord3d[size];
		for (int i = 0; i < size; i++){
			points[i] = new Coord3d(i, y[i], 0);
		}
		return points;
	}

	/**
	 * @param x - array containing x coordinate values
	 * @param y - array containing y coordinate values
	 * @return array of Coord3d points where x:{x values}, y:{y values}, z:{0, 0, 0...}
	 */
	public static Coord3d[] createPoints(double[] x, double[] y){
		int size = x.length;
		if (size != y.length){
			System.out.println("The size of x array does not match the size of y array.");
			Coord3d[] points = {new Coord3d(0.0, 0.0, 0.0)};
			return points;
		}
		Coord3d[] points = new Coord3d[size];
		for (int i = 0; i < size; i++){
			points[i] = new Coord3d(x[i], y[i], 0);
		}
		return points;
	}

	/**
	 * @param x - array containing x coordinate values
	 * @param y - array containing y coordinate values
	 * @param y - array containing z coordinate values
	 * @return array of Coord3d points where x:{x values}, y:{y values}, z:{0, 0, 0...}
	 */
	public static Coord3d[] create3dPoints(double[] x, double[] y, double[] z){
		int size = x.length;
		if (size != y.length || size != z.length){
			System.out.println("The size of x array does not match the size of y array.");
			Coord3d[] points = {new Coord3d(0.0, 0.0, 0.0)};
			return points;
		}
		Coord3d[] points = new Coord3d[size];
		for (int i = 0; i < size; i++){
			points[i] = new Coord3d(x[i], y[i], z[i]);
		}
		return points;
	}

	/**
	 * @param x - array containing x coordinate values
	 * @param y - array containing y coordinate values
	 * @return array of Coord2d points where x:{x values}, y:{y values}
	 */
	public static Coord2d[] create2dPoints(double[] x, double[] y){
		int size = x.length;
		if (size != y.length){
			System.out.println("The size of x array does not match the size of y array.");
			Coord2d[] points = {new Coord2d(0.0, 0.0)};
			return points;
		}
		Coord2d[] points = new Coord2d[size];
		for (int i = 0; i < size; i++){
			points[i] = new Coord2d(x[i], y[i]);
		}
		return points;
	}
	
	/**
	 * @param points
	 * @return
	 */
	public static LineStrip createLineStrip(Coord3d[] points, Color color) {
		LineStrip line = new LineStrip();
		for (Coord3d point:points){
			line.add(new Point((point), color));
		}
		return line;
	}
	
	/**
	 * @param chart - the chart to display in a separate frame
	 */
	public static void showChartFrame(Chart chart){
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		/* 
		 * panel solution
		 */
//		JPanel panel = getChartPanel(chart);
//		panel.setPreferredSize(new java.awt.Dimension(800, 600)); // Set default size for panel3d
//		panel.setLayout(new java.awt.BorderLayout());
//		panel.add((Component)chart.getCanvas(), BorderLayout.CENTER); // Add chart in CENTER so that it will be resized automatically
//		frame.add(panel, BorderLayout.CENTER); // Add the panel3d in CENTER so that it will be resized automatically
		
		frame.add((Component)chart.getCanvas());
		
		frame.pack();
		frame.setVisible(true);
		
	}
	
//	private static JPanel getChartPanel(Chart chart) {
//		Component canvas = (Component) chart.getCanvas();
//
//		JPanel chartPanel = new JPanel(new BorderLayout());
//		
//		Border b = BorderFactory.createLineBorder(java.awt.Color.black);
//		chartPanel.setBorder(b);
//		chartPanel.add(canvas, BorderLayout.CENTER);
//		return chartPanel;
//	}

}
