package uk.ac.cf.milling.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class Plotter3D_V2 {
	
	public static JPanel getChartPanel(boolean[][][] part) {
		
		Chart chart = AWTChartComponentFactory.chart(Quality.Nicest, IChartComponentFactory.Toolkit.newt);

		List<Shape> surfaces = getPartShape(part);
        for (Shape surface:surfaces) {
        	chart.getScene().add(surface);
        }

        chart.addMouseCameraController();
//	        chart.addKeyboardCameraController();
        
        Component canvas = (java.awt.Component) chart.getCanvas();

		JPanel chartPanel = new JPanel(new BorderLayout());
		
		Border b = BorderFactory.createLineBorder(java.awt.Color.black);
		chartPanel.setBorder(b);
		chartPanel.add(canvas, BorderLayout.CENTER);
		return chartPanel;

	}
	
	private static List<Shape> getPartShape(boolean[][][] part) {
		//Generate coordinates
		int xSize = part.length;
		int ySize = part[0].length;
		int zSize = part[0][0].length;
		double elemSize = SettingUtils.getElementSize();
		int zHighest = Integer.MIN_VALUE;
		int zLowest = Integer.MAX_VALUE;
		
		List<Coord3d> coordinatesTop = new ArrayList<Coord3d>();
		List<Coord3d> coordinatesBottom = new ArrayList<Coord3d>();
		
		//Iterate over every cell and generate a coordinate 
		//for the elements that are false (not machined)
		for (int xIndex = 0; xIndex < xSize; xIndex++) {
			for (int yIndex = 0; yIndex < ySize; yIndex++) {
				for (int zIndex = 0; zIndex < zSize; zIndex++) {
					if (!part[xIndex][yIndex][zIndex]) {
						//in jzy3d library z = f(x,y) so only one z considered for each x,y set
						if (zHighest < zIndex) zHighest = zIndex;
						if (zLowest > zIndex) zLowest = zIndex;
					}
				}
				coordinatesTop.add(new Coord3d(xIndex*elemSize, yIndex*elemSize, zHighest*elemSize));
				zHighest = Integer.MIN_VALUE;

				coordinatesBottom.add(new Coord3d(xIndex*elemSize, yIndex*elemSize, zLowest*elemSize));
				zLowest = Integer.MAX_VALUE;
			}
		}
		
		Shape surfaceTop = Builder.buildDelaunay(coordinatesTop);
//		surfaceTop.setColorMapper(new ColorMapper(new ColorMapRainbow(), surfaceTop.getBounds().getZmin(), surfaceTop.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
		surfaceTop.setColor(new Color(100, 10, 10));
        surfaceTop.setFaceDisplayed(true);
		surfaceTop.setWireframeDisplayed(true);

		Shape surfaceBottom = Builder.buildDelaunay(coordinatesBottom);
		surfaceBottom.setColor(new Color(10, 100, 100));
		surfaceBottom.setFaceDisplayed(true);
		surfaceBottom.setWireframeDisplayed(true);
		
		List<Shape> surfaces = new ArrayList<Shape>();
		surfaces.add(surfaceTop);
		surfaces.add(surfaceBottom);
		return surfaces;
	}


}
