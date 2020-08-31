package uk.ac.cf.milling.utils.plotting;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Sphere;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.lights.Light;

import uk.ac.cf.milling.utils.db.SettingUtils;

public class Plotter3D_V3 {
	public static JPanel getChartPanel(boolean[][][] part) {

		Chart chart = AWTChartComponentFactory.chart(Quality.Nicest, IChartComponentFactory.Toolkit.newt);
		List<? extends AbstractDrawable> drawables = getPartShape(part);
		for (AbstractDrawable drawable : drawables) {
			chart.getScene().add(drawable,false); //for performance views are refreshed once at the end thus false
		}

		int maxIndex = Math.max(part.length, Math.max(part[0].length, part[0][0].length));
		chart.getView().getBounds().setXmax(maxIndex);
		chart.getView().getBounds().setYmax(maxIndex);
		chart.getView().getBounds().setZmax(maxIndex);

		Sphere sphere = new Sphere(new Coord3d(0.0,0.0,0.0), 5, 32, new Color(10, 10, 10)); //test to see where light is
		chart.getScene().add(sphere);
		Coord3d lightPosition = new Coord3d(maxIndex, maxIndex, maxIndex);
		addLight(chart, lightPosition);
		sphere.setPosition(lightPosition);

		//TODO add shift in all dimensions
		new NewtCameraMouseControllerZoom(chart);
		chart.addKeyboardCameraController();


		Component canvas = (java.awt.Component) chart.getCanvas();

		JPanel chartPanel = new JPanel(new BorderLayout());

		//		Border b = BorderFactory.createLineBorder(java.awt.Color.GREEN);
		//		chartPanel.setBorder(b);
		chartPanel.add(canvas, BorderLayout.CENTER);
		PlotterUtils.registerChart(chartPanel, chart);
		return chartPanel;

	}

	private static void addLight(Chart chart, Coord3d position){
		Light light = new Light(0);
		light.setPosition(position);
		light.setAmbiantColor(Color.GRAY);
		light.setDiffuseColor(Color.GRAY);
		light.setSpecularColor(Color.CYAN);
		chart.getScene().add( light );
	}

	private static List<? extends AbstractDrawable> getPartShape(boolean[][][] part) {

		//Generate coordinates
		int xSize = part.length;
		int ySize = part[0].length;
		int zSize = part[0][0].length;
		double elemSize = SettingUtils.getElementSize();


		//Generate a matrix to store the z(x,y) values for top surface
		int[][] topZIndexes = new int[xSize][ySize];

		System.out.println("Identifying top surface");
		//Iterate over every cell to find z 
		//for the elements that are false (not machined)
		for (int xIndex = 0; xIndex < xSize; xIndex++) {
			for (int yIndex = 0; yIndex < ySize; yIndex++) {
				for (int zIndex = 0; zIndex < zSize; zIndex++) {
					if (!part[xIndex][yIndex][zIndex]) {
						//in jzy3d library z = f(x,y) so only one z considered for each x,y set
						if (topZIndexes[xIndex][yIndex] < zIndex) topZIndexes[xIndex][yIndex] = zIndex;
					}
				}
			}
		}
		// TODO increment by elemsize so the graph is not distorted
		// also change the iterator so it is normally ++ instead of --
		System.out.println("Building top surface");

		List<Polygon> polygons = new ArrayList<Polygon>();
		for (int xIndex = 0; xIndex < xSize-1; xIndex++) {
			for (int yIndex = 0; yIndex < ySize-1; yIndex++) {
				double x = xIndex * elemSize;
				double y = yIndex * elemSize;
				
				Polygon polygon = new Polygon();
				polygon.add(new Point(new Coord3d(xIndex, yIndex, topZIndexes[xIndex][yIndex])), false);
				polygon.add(new Point(new Coord3d(xIndex, yIndex+1, topZIndexes[xIndex][yIndex+1])), false);
				polygon.add(new Point(new Coord3d(xIndex+1, yIndex+1, topZIndexes[xIndex+1][yIndex+1])), false);
				polygon.add(new Point(new Coord3d(xIndex+1, yIndex, topZIndexes[xIndex+1][yIndex])), false);
				polygons.add(polygon);
			}
		}
		System.out.println("Surface created");

		//Build bottom surface
		Polygon polygonBottom = new Polygon();
		polygonBottom.add(new Point(new Coord3d(0, 0, 0)), false);
		polygonBottom.add(new Point(new Coord3d(xSize, 0, 0)), false);
		polygonBottom.add(new Point(new Coord3d(xSize, ySize, 0)), false);
		polygonBottom.add(new Point(new Coord3d(0, ySize, 0)), false);
		polygons.add(polygonBottom);

		//Build side 1
		Polygon polygonSide1 = new Polygon();
		for (int x = xSize-1; x >= 0; x--) {
			polygonSide1.add(new Point(new Coord3d(x, 0, topZIndexes[x][0])), false);
		}
		polygonSide1.add(new Point(new Coord3d(0, 0, 0)), false);
		polygonSide1.add(new Point(new Coord3d(xSize-1, 0, 0)), false);
		polygons.add(polygonSide1);

		//Build side 2
		Polygon polygonSide2 = new Polygon();
		for (int y = (ySize-1); y >= 0; y--) {
			polygonSide2.add(new Point(new Coord3d(xSize-1, y, topZIndexes[xSize-1][y])), false);
		}
		polygonSide2.add(new Point(new Coord3d(xSize-1, 0, 0)), false);
		polygonSide2.add(new Point(new Coord3d(xSize-1, ySize-1, 0)), false);
		polygons.add(polygonSide2);

		//Build side 3
		Polygon polygonSide3 = new Polygon();
		for (int x = xSize-1; x >= 0; x--) {
			polygonSide3.add(new Point(new Coord3d(x, ySize-1, topZIndexes[x][ySize-1])), false);
		}
		polygonSide3.add(new Point(new Coord3d(0, ySize-1, 0)), false);
		polygonSide3.add(new Point(new Coord3d(xSize-1, ySize-1, 0)), false);
		polygons.add(polygonSide3);

		//Build side 4
		Polygon polygonSide4 = new Polygon();
		polygonSide4.add(new Point(new Coord3d(0, ySize-1, 0)), false);
		polygonSide4.add(new Point(new Coord3d(0, 0, 0)), false);
		for (int y = 0; y < ySize; y++) {
			polygonSide4.add(new Point(new Coord3d(0, y, topZIndexes[0][y])), false);
		}
		polygons.add(polygonSide4);

		for (Polygon polygon : polygons) {
			//			polygon.setColor(new Color(0, 0, 0));
			polygon.updateBounds();
			polygon.setFaceDisplayed(true);
			polygon.setWireframeDisplayed(true);
			polygon.setWireframeColor(Color.BLACK);
		}

		//		Shape surface = new Shape(polygons);
		//		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new org.jzy3d.colors.Color(1,1,1,1f)));
		//	    surface.setWireframeDisplayed(true);
		//	    surface.setWireframeColor(org.jzy3d.colors.Color.BLACK);


		return polygons;
	}

	/**
	 * @param relation
	 * @param getxLoad
	 * @param getyLoad
	 * @param getzLoad
	 * @param getxLoad2
	 * @param getyLoad2
	 * @param getzLoad2
	 * @param param2
	 */
	public static JPanel generate3AxisDiffPlot(int[][] relation, double[] getxLoad, double[] getyLoad, double[] getzLoad,
			double[] getxLoad2, double[] getyLoad2, double[] getzLoad2, String param2) {
		// TODO Auto-generated method stub
		return new JPanel();
	}
	/**
	 * @param relation
	 * @param getxLoad
	 * @param getyLoad
	 * @param getzLoad
	 * @param getxLoad2
	 * @param getyLoad2
	 * @param getzLoad2
	 * @param param2
	 */
	public static JPanel generate3AxisPlots(int[][] relation, double[] getxLoad, double[] getyLoad, double[] getzLoad,
			double[] getxLoad2, double[] getyLoad2, double[] getzLoad2, String param2) {
		// TODO Auto-generated method stub
		return new JPanel();
	}

}
