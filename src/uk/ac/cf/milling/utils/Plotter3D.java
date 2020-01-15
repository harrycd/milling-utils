/**
 * 
 */
package uk.ac.cf.milling.utils;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import uk.ac.cf.milling.objects.Billet;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class Plotter3D {

	
	public static JPanel getChartPanel(boolean[][][] part2plot, Billet billet2plot) {
		return getChartPanel(getChart(part2plot, billet2plot));
	}
	
	public static JPanel getChartPanel(Chart chart) {
		Component canvas = (java.awt.Component) chart.getCanvas();

		JPanel chartPanel = new JPanel(new BorderLayout());
		
		Border b = BorderFactory.createLineBorder(java.awt.Color.black);
		chartPanel.setBorder(b);
		chartPanel.add(canvas, BorderLayout.CENTER);
		return chartPanel;
	}
	
	public static Chart getChart(boolean[][][] part2plot, Billet billet2plot) {

		int sizeX = part2plot.length;
		int sizeY = part2plot[0].length;
		int sizeZ = part2plot[0][0].length;
		Coord3d[] coordinates = new Coord3d[(sizeX*sizeY*sizeZ)];
		int counter = 0;

		float xf = 0.0f;
		float yf = 0.0f;
		float zf = 0.0f;
		
		float xBilletMin = (float) billet2plot.getXBilletMin();
		float yBilletMin = (float) billet2plot.getYBilletMin();
		float zBilletMin = (float) billet2plot.getZBilletMin();
		float xBilletMax = (float) billet2plot.getXBilletMax();
		float yBilletMax = (float) billet2plot.getYBilletMax();
		float zBilletMax = (float) billet2plot.getZBilletMax();
		float elemSize = (float) SettingUtils.getElementSize();
		
		//Generate the array of points to display
		for (int x = 0; x < sizeX; x++){
			for (int y = 0; y < sizeY; y++){
				for (int z = 0; z < sizeZ; z++){
					if (!part2plot[x][y][z]){
						xf = xBilletMin + x * elemSize;
						yf = yBilletMin + y * elemSize;
						zf = zBilletMin + z * elemSize;
						coordinates[counter] = new Coord3d(xf,yf,zf);
						counter++;
					}
				}
			}
		}

		//The created array has 0 values (not all (x,y,z) will be displayed
		//Trim the array
		int size = counter-1;
		Coord3d[] coords = new Coord3d[size];
		for(int i=0; i<size; i++){
			coords[i] = coordinates[i];
		}
		coordinates = null;
		Scatter scatter = new Scatter(coords);

		//Set the correct bounds so the plot is not distorted (graph always a box)
		float boxSize = Math.max((xBilletMax - xBilletMin), Math.max(yBilletMax - yBilletMin, zBilletMax - zBilletMin));
		scatter.getBounds().setXmin( xBilletMin );
		scatter.getBounds().setXmax( xBilletMin + boxSize );
		scatter.getBounds().setYmin( yBilletMin );
		scatter.getBounds().setYmax( yBilletMin + boxSize );
		scatter.getBounds().setZmin( zBilletMin );
		scatter.getBounds().setZmax( zBilletMin + boxSize );

		//set the width of each point so it is visible
		scatter.setWidth(1);
		Chart chart = AWTChartComponentFactory.chart(Quality.Nicest, IChartComponentFactory.Toolkit.newt);
		chart.getScene().add(scatter);
		chart.getAxeLayout().setXAxeLabel("X");
		chart.getAxeLayout().setYAxeLabel("Y");
		chart.getAxeLayout().setZAxeLabel("Z");
		
		chart.addMouseCameraController();
		
		return chart;
	}

	/**
	 * @param part - a boolean[][][] representing the initial part
	 * @return a part that has all its internal, non visible, points removed
	 */
	public static boolean[][][] removeHiddenPoints(boolean[][][] part){
		int xLength = part.length;
		int yLength = part[0].length;
		int zLength = part[0][0].length;

		boolean[][][] cleanPart = new boolean[xLength][yLength][zLength];
		for (int x=0; x<xLength; x++){
			for (int y=0; y<yLength; y++){
				for (int z=0; z<zLength; z++){
					cleanPart[x][y][z] = part[x][y][z];
				}
			}
		}

		int inSizeX = part.length - 1;
		int inSizeY = part[0].length - 1;
		int inSizeZ = part[0][0].length - 1;

		for (int x = 1; x < inSizeX; x++){
			for (int y = 1; y < inSizeY; y++){
				for (int z = 1; z < inSizeZ; z++){
					//if not true continue because it will not be plotted anyway
					if(part[x][y][z])
						continue;

					if(
							!part[x-1][y][z] && 
							!part[x+1][y][z] &&
							!part[x][y-1][z] &&
							!part[x][y+1][z] &&
							!part[x][y][z-1] &&
							!part[x][y][z+1] ){
						cleanPart[x][y][z] = true;
					}
				}
			}
		}

		return cleanPart;
	}

}
