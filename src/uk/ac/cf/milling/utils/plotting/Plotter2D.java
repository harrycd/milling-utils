/**
 * 
 */
package uk.ac.cf.milling.utils.plotting;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart2d.Chart2d;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.plot2d.primitives.Serie2d;
import org.jzy3d.plot3d.primitives.axes.layout.providers.SmartTickProvider;
import org.jzy3d.plot3d.rendering.view.modes.ViewBoundMode;

/**
 * A frame to show a list of charts
 * @author Theocharis Alexopoulos
 *
 */

public class Plotter2D {

	public static JScrollPane getAllChartsPanel(List<Chart> charts){

		JPanel chartsPanel = new JPanel();
		JScrollPane chartsPanelScroll = new JScrollPane(chartsPanel);
		chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));

		for (Chart chart : charts) {
			JPanel chartPanel = getChartPanel(chart); 
			chartPanel.setMinimumSize(new Dimension(100,500));
			chartsPanel.add(chartPanel);
		}
		return chartsPanelScroll;
	}

	public static JPanel getChartPanel(Chart chart) {
		Component canvas = (java.awt.Component) chart.getCanvas();

		JPanel chartPanel = new JPanel(new BorderLayout());

		Border b = BorderFactory.createLineBorder(java.awt.Color.black);
		chartPanel.setBorder(b);
		chartPanel.add(canvas, BorderLayout.CENTER);
		PlotterUtils.registerChart(chartPanel, chart);
		return chartPanel;
	}

	/**
	 * @param dataX
	 * @param dataY
	 * @return
	 */
	public static JPanel get2dPlotPanel(float[] dataX, double[] dataY) {
		Chart chart = get2dPlot(dataX, dataY);
		//		chart.stopAnimator();
		JPanel chartPanel = getChartPanel(chart); 
		return chartPanel;
	}

	/**
	 * @param dataX
	 * @param dataY
	 * @return
	 */
	public static JPanel get2dPlotPanel(double[] dataX, double[] dataY) {
		Chart chart = get2dPlot(dataX, dataY);
		//		chart.stopAnimator();
		JPanel chartPanel = getChartPanel(chart); 
		PlotterUtils.registerChart(chartPanel, chart);
		return chartPanel;
	}

	/**
	 * Add a plot with a single curve showing which is values0 - values1
	 * @param relation
	 * @param values0
	 * @param values1
	 * @param title
	 * @param ma - moving average to smooth the curve
	 */
	public static JPanel generateDiffPlot(int[][] relation, double[] values0, double[] values1 , String title, int sma, String xAxisTitle, String yAxisTitle){
		title += " diff";
		//Plot the spindle load diff
		Chart2d chart = new Chart2d();

		Serie2d sirie = chart.getSerie(title, Serie2d.Type.LINE);

		Color color = new Color(0, 0, 255);
		int relationLength = relation[0].length;

		double[] yPoints = new double[relationLength];
		float ymin = Float.MAX_VALUE;
		float ymax = Float.MIN_VALUE;

		//Calculate the difference
		for (int i = 0; i < relationLength; i++){
			yPoints[i] = values1[relation[1][i]] - values0[relation[0][i]];
			if (yPoints[i] < ymin) ymin = (float) yPoints[i];
			if (yPoints[i] > ymax) ymax = (float) yPoints[i];
		}

		//Smoothen the values
		if (sma > 1){
			yPoints = smoothen(yPoints, sma);
			title += " sma" + sma;
		}

		//Generate the plot points
		for (int i = 0; i < relationLength; i++){
			sirie.add(new Coord2d(i, yPoints[i]), color);
		}

		chart.getAxeLayout().setYTickProvider(new SmartTickProvider(20));
		chart.getAxeLayout().setXAxeLabel(xAxisTitle);
		chart.getAxeLayout().setYAxeLabel(yAxisTitle);
		chart.getView().setBoundManual(new BoundingBox3d(0, relationLength, ymin, ymax, 0, 1));
		chart.getView().setBoundMode(ViewBoundMode.MANUAL);

		return Plotter2D.getChartPanel(chart);
	}





	/**
	 * Add a plot in results that shows 2 curves, one for each dataset
	 * @param relation
	 * @param values0
	 * @param values1
	 * @param title
	 * @param sma
	 */
	public static JPanel generatePlots(int[][] relation, double[] values0, double[] values1, String title, int sma, String xAxisTitle, String yAxisTitle){
		//Plot the spindle load
		Chart2d chart = new Chart2d();

		Serie2d serie = chart.getSerie(title, Serie2d.Type.LINE);

		Color color0 = new Color(200, 0, 0);

		int relationLength = relation[0].length;

		//If moving average is > 1 then smoothen the values
		if (sma > 1) {
			values0 = smoothen(values0, sma);
			values1 = smoothen(values1, sma);
			title += " sma" + sma;  
		}

		for (int i = 0; i < relationLength; i++){
			serie.add(new Coord2d(i, values0[relation[0][i]]), color0);
		}

		//add a white line to connect the end of previous graph to the beginning of the next one
		//This is because there cannot be 2 separate serie in one chart
		serie.add(new Coord2d(relationLength-1, values0[relation[0][relationLength-1]]), new Color(150, 150, 150));
		serie.add(new Coord2d(				 0, values1[relation[1][0]]				  ), new Color(150, 150, 150));

		Color color1 = new Color(0, 200, 0);
		for (int i = 0; i < relationLength; i++){
			serie.add(new Coord2d(i, values1[relation[1][i]]), color1);
		}
		chart.getAxeLayout().setYTickProvider(new SmartTickProvider(20));
		chart.getAxeLayout().setXAxeLabel(xAxisTitle);
		chart.getAxeLayout().setYAxeLabel(yAxisTitle);

		return Plotter2D.getChartPanel(chart);
	}

	/**
	 * @param relations
	 * @param referenceDataset
	 * @param datasets
	 * @param title
	 * @param sma
	 * @param xAxisTitle
	 * @param yAxisTitle
	 * @return
	 */
	public static JPanel generatePlots(List<int[][]> relations, 
			double[] referenceDataset, List<double[]> datasets, 
			String title, int sma, String xAxisTitle, String yAxisTitle){

		//Verify input data
		if (relations.size() != datasets.size()) {
			System.out.println("Dataset and relation lists size mismatch.");
			return new JPanel();
		}

		Chart2d chart = new Chart2d();

		Serie2d series = chart.getSerie(title, Serie2d.Type.LINE);

		Color colorReference = new Color(0, 0, 200);
		Color colorDatasets = new Color(200, 0, 0);


		//If moving average is > 1 then smoothen the values
		if (sma > 1) {
			referenceDataset = smoothen(referenceDataset, sma);
			for (double[] dataset:datasets) {
				dataset = smoothen(dataset, sma);
			}
		}
		title += " sma" + sma;

		// First add the reference dataset
		for (int i = 0; i < referenceDataset.length; i++){
			series.add(new Coord2d(i, referenceDataset[i]), colorReference);
		}

		// Add remaining datasets with different colour
		for (int datasetIndex = 0; datasetIndex < datasets.size(); datasetIndex++) {
			int[][] relation = relations.get(datasetIndex);
			double[] dataset = datasets.get(datasetIndex);

			for (int i = 0; i < relation[0].length; i++){
				series.add(new Coord2d(relation[0][i], dataset[relation[1][i]]), colorDatasets);
			}
		}

		return Plotter2D.getChartPanel(chart);
	}


	/**
	 * @param values - a double array to smoothen
	 * @param movingAverage - the simple moving average to use
	 * @return a double array with the smoothened values
	 */
	private static double[] smoothen(double[] values, int movingAverage) {
		int length = values.length;
		double[] valuesSmooth = new double[length];
		//Smoothen the values
		for (int i = movingAverage; i < length; i++){
			for (int j = 0; j < movingAverage; j++){
				valuesSmooth[i] += values[i-j];
			}
			valuesSmooth[i] /= movingAverage;
		}
		return valuesSmooth;
	}



	/**
	 * @param dataY - a long[] array containing the data to plot
	 * @return a chart containing a plot of provided points with x axis distance = 1
	 */
	public static Chart2d get2dPlot(long[] dataY){
		Chart2d chart = new Chart2d();
		Serie2d series = chart.getSerie("series", Serie2d.Type.LINE);
		int length = dataY.length;
		for (int i = 0; i < length; i++){
			series.add(i, dataY[i]);
		}
		series.setColor(Color.BLUE);
		return chart;
	}

	/**
	 * @param dataX - a long[] containing the x axis data
	 * @param dataY - a long[] containing the y axis data
	 * @return a chart where the input data sets have been plotted
	 */
	public static Chart2d get2dPlot(long[] dataX, long[] dataY){
		Chart2d chart = new Chart2d();
		if (dataX.length != dataY.length){
			System.out.println("The data arrays do not match (different length)");
			return chart;
		}
		Serie2d series = chart.getSerie("series", Serie2d.Type.LINE);
		int length = dataX.length;
		for (int i = 0; i < length; i++){
			series.add(dataX[i], dataY[i]);
		}
		series.setColor(Color.BLUE);
		return chart;
	}

	/**
	 * @param dataX - a double[] containing the x axis data
	 * @param dataY - a long[] containing the y axis data
	 * @return a chart where the input data sets have been plotted
	 */
	public static Chart2d get2dPlot(float[] dataX, long[] dataY){
		Chart2d chart = new Chart2d();
		if (dataX.length != dataY.length){
			System.out.println("The data arrays do not match (different length)");
			return chart;
		}
		Serie2d series = chart.getSerie("series", Serie2d.Type.LINE);
		int length = dataX.length;
		for (int i = 0; i < length; i++){
			series.add(dataX[i], dataY[i]);
		}
		series.setColor(Color.BLUE);
		return chart;
	}

	/**
	 * @param dataX - a double[] containing the x axis data
	 * @param dataY - a long[] containing the y axis data
	 * @return a chart where the input data sets have been plotted
	 */
	public static Chart2d get2dPlot(float[] dataX, double[] dataY){
		Chart2d chart = new Chart2d();
		if (dataX.length != dataY.length){
			System.out.println("The data arrays do not match (different length)");
			return chart;
		}
		Serie2d series = chart.getSerie("series", Serie2d.Type.LINE);
		int length = dataX.length;
		for (int i = 0; i < length; i++){
			series.add(dataX[i], dataY[i]);
		}
		series.setColor(Color.BLUE);
		return chart;
	}

	/**
	 * @param dataX - a float representing the fixed x axis distance between 2 points 
	 * @param dataY - a long[] containing the y axis data
	 * @return a chart where the input data sets have been plotted
	 */
	public static Chart2d get2dPlot(double xStep, long[] dataY){
		Chart2d chart = new Chart2d();
		Serie2d series = chart.getSerie("series", Serie2d.Type.LINE);
		int length = dataY.length;
		for (int i = 0; i < length; i++){
			series.add(xStep*i, dataY[i]);
		}
		series.setColor(Color.BLUE);
		return chart;
	}

	/**
	 * @param dataX - a float representing the fixed x axis distance between 2 points 
	 * @param dataY - a long[] containing the y axis data
	 * @return a chart where the input data sets have been plotted
	 */
	public static Chart2d get2dPlot(double xStep, double[] dataY){
		Chart2d chart = new Chart2d();
		Serie2d series = chart.getSerie("series", Serie2d.Type.LINE);
		int length = dataY.length;
		for (int i = 0; i < length; i++){
			series.add(xStep*i, dataY[i]);
		}
		series.setColor(Color.BLUE);
		return chart;
	}

	/**
	 * @param axialProfilesDataX
	 * @param axialProfilesDataY
	 * @return
	 */
	public static Chart get2dPlot(double[] dataX, double[] dataY) {
		Chart2d chart = new Chart2d();
		Serie2d series = chart.getSerie("series", Serie2d.Type.LINE);
		int length = dataY.length;
		for (int i = 0; i < length; i++){
			series.add(dataX[i], dataY[i]);
		}
		series.setColor(Color.BLACK);
		return chart;
	}

	/**
	 * @param axialProfilesDataX
	 * @param axialProfilesDataY
	 * @return
	 */
	public static Chart get2dPlot(double[] dataX, double[][] dataY) {
		Chart2d chart = new Chart2d();

		Random r = new Random();

		for (int it = 0; it < dataY.length; it++){
			Serie2d series = chart.getSerie("series", Serie2d.Type.LINE);
			int length = dataY[it].length;
			for (int i = 0; i < length; i++){
				series.add(dataX[i], dataY[it][i]);
			}
			series.setColor(new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()));
		}
		return chart;
	}

	/**
	 * @param panels
	 * @return
	 */
	public static JScrollPane getScrollPanel(List<JPanel> panels) {
		JPanel chartsPanel = new JPanel();
		JScrollPane chartsPanelScroll = new JScrollPane(chartsPanel);
		chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));

		for (JPanel panel : panels) {
			panel.setMinimumSize(new Dimension(100,500));
			chartsPanel.add(panel);
		}
		PlotterUtils.registerChart(chartsPanelScroll, panels);
		return chartsPanelScroll;
	}

}

