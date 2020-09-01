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
import org.jzy3d.plot3d.rendering.canvas.Quality;

import uk.ac.cf.milling.utils.db.SettingUtils;

public class Plotter3D_V4 {
	public static JPanel getChartPanel(boolean[][][] part) {

		Chart chart = AWTChartComponentFactory.chart(Quality.Nicest, IChartComponentFactory.Toolkit.newt);
		chart.getView().setBackgroundColor(Color.BLACK);
		chart.getAxeLayout().setXTickColor(Color.YELLOW);
		chart.getAxeLayout().setYTickColor(Color.YELLOW);
		chart.getAxeLayout().setZTickColor(Color.YELLOW);
		chart.getView().getAxe().getLayout().setGridColor(Color.BLACK);
		chart.getView().getAxe().getLayout().setFaceDisplayed(false);
		chart.getView().setSquared(false);

		List<? extends AbstractDrawable> drawables = getPartShape(part);
		for (AbstractDrawable drawable : drawables) {
			chart.getScene().add(drawable,false); //for performance views are refreshed once at the end thus false
		}

		int maxIndex = Math.max(part.length, Math.max(part[0].length, part[0][0].length));
		chart.getView().getBounds().setXmax(maxIndex);
		chart.getView().getBounds().setYmax(maxIndex);
		chart.getView().getBounds().setZmax(maxIndex);

		//		Coord3d lightP, lightPosition);

		//TODO add shift in all dimensions
		new ChartXYZZoom(chart);
		chart.addKeyboardCameraController();


		Component canvas = (java.awt.Component) chart.getCanvas();

		JPanel chartPanel = new JPanel(new BorderLayout());

		//		Border b = BorderFactory.createLineBorder(java.awt.Color.GREEN);
		//		chartPanel.setBorder(b);
		chartPanel.add(canvas, BorderLayout.CENTER);
		PlotterUtils.registerChart(chartPanel, chart);
		return chartPanel;

	}

	private static List<? extends AbstractDrawable> getPartShape(boolean[][][] part) {

		//Generate coordinates
		int xSize = part.length;
		int ySize = part[0].length;
		int zSize = part[0][0].length;

		//Generate a matrix to store the z(x,y) values for top surface
		int[][] topZIndexes = new int[xSize][ySize];

		System.out.print("Identifying top surface...");
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
		System.out.println("done");

		//TODO delete the following line
//		IoUtils.writeCSVFile("topZIndexes.csv", topZIndexes);

		List<Polygon> polygons = new ArrayList<Polygon>();

		//Build polygons of top surface
		System.out.print("Building top surface...");

		//keep track of which elements have been drawn. This is needed because
		//neighbouring polygons will be merged.
		boolean[][] isDrawn = new boolean[topZIndexes.length][topZIndexes[0].length];

		for (int xIndex = 0; xIndex < xSize-1; xIndex++) {
			for (int yIndex = 0; yIndex < ySize-1; yIndex++) {
				if (!isDrawn[xIndex][yIndex]) { //skip if this polygon has been drawn

					List<int[]> neighbourhood = new ArrayList<int[]>();

					List<int[]> neighbours = new ArrayList<int[]>();

					//Add the first polygon to initialise the process
					int[] polygonI = new int[] {xIndex, yIndex};
					neighbours.add(polygonI);

					while (neighbours.size() > 0) { //run until all neighbours are found and added to neighbourhood
						polygonI = neighbours.get(0);
						neighbours.remove(0); //remove it so the list can be reduced to 0

						//Avoid going back and forth to the same polygons 
						//by checking if polygon has been drawn
						if (isDrawn[polygonI[0]][polygonI[1]]) continue;

						neighbourhood.add(polygonI);
						isDrawn[polygonI[0]][polygonI[1]] = true; //indicate that the polygon is added

						//find more neighbours to continue iteration
						neighbours.addAll(findNeighbours(polygonI, topZIndexes)); 
					}

					Polygon polygon = convertToPolygon(neighbourhood, topZIndexes[polygonI[0]][polygonI[1]]);
					polygons.add(polygon);
				}
			}
		}
		System.out.println("done");
		
		double elemSize = SettingUtils.getElementSize();

		//Build bottom surface
		Polygon polygonBottom = new Polygon();
		polygonBottom.add(new Point(new Coord3d(0				, 0				, 0)), false);
		polygonBottom.add(new Point(new Coord3d(xSize * elemSize, 0				, 0)), false);
		polygonBottom.add(new Point(new Coord3d(xSize * elemSize, ySize*elemSize, 0)), false);
		polygonBottom.add(new Point(new Coord3d(0				, ySize*elemSize, 0)), false);
		polygons.add(polygonBottom);

		//Build side 1
		Polygon polygonSide1 = new Polygon();
		for (int x = xSize-1; x >= 0; x--) {
			polygonSide1.add(new Point(new Coord3d(x*elemSize			, 0		, topZIndexes[x][0] * elemSize)), false);
		}
			polygonSide1.add(new Point(new Coord3d(0				  	, 0		, 0)), false);
			polygonSide1.add(new Point(new Coord3d((xSize-1)*elemSize 	, 0		, 0)), false);
		polygons.add(polygonSide1);

		//Build side 2
		Polygon polygonSide2 = new Polygon();
		for (int y = (ySize-1); y >= 0; y--) {
			polygonSide2.add(new Point(new Coord3d((xSize-1)*elemSize, y * elemSize			, topZIndexes[xSize-1][y] * elemSize)), false);
		}
			polygonSide2.add(new Point(new Coord3d((xSize-1)*elemSize, 0					, 0)), false);
			polygonSide2.add(new Point(new Coord3d((xSize-1)*elemSize, (ySize-1) * elemSize	, 0)), false);
		polygons.add(polygonSide2);

		//Build side 3
		Polygon polygonSide3 = new Polygon();
		for (int x = xSize-1; x >= 0; x--) {
			polygonSide3.add(new Point(new Coord3d(x*elemSize			, (ySize-1)*elemSize, 	topZIndexes[x][ySize-1]*elemSize)), false);
		}
			polygonSide3.add(new Point(new Coord3d(0					, (ySize-1)*elemSize, 									0)), false);
			polygonSide3.add(new Point(new Coord3d((xSize-1)*elemSize	, (ySize-1)*elemSize, 									0)), false);
		polygons.add(polygonSide3);

		//Build side 4
		Polygon polygonSide4 = new Polygon();
			polygonSide4.add(new Point(new Coord3d(0, (ySize-1)*elemSize, 							0)), false);
			polygonSide4.add(new Point(new Coord3d(0, 0					, 							0)), false);
		for (int y = 0; y < ySize; y++) {
			polygonSide4.add(new Point(new Coord3d(0, y*elemSize		,  topZIndexes[0][y]*elemSize)), false);
		}
		polygons.add(polygonSide4);
		
		//The following randomisation of polygons is not necessary and may be slower but enhances visuals
//		Random rand = new Random();
		
		for (Polygon polygon : polygons) {
//			polygon.setColor(new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
			polygon.updateBounds();
			polygon.setFaceDisplayed(false);
			polygon.setWireframeDisplayed(true);
			polygon.setWireframeColor(Color.GRAY);
		}

		return polygons;
	}

	/**
	 * @param neighbourhood
	 * @param topZIndex 
	 * @return
	 */
	private static Polygon convertToPolygon(List<int[]> neighbourhood, int topZIndex) {
		if (neighbourhood.size() < 3 && neighbourhood.size() > 0) {
			Polygon polygon = convertToPolygon(neighbourhood.get(0), topZIndex);
			return polygon;
			//TODO missing scenario of neighbourhood.size() == 2
		}
		//NOTE: Issue with the following method:
		//currently each element is a point. However elements have a square surface
		//and the point is the centre of gravity.
		
		//The following part of code will walk the perimeter of the polygon

		//Step 1: Find a point to begin. The point should be a vertex of the polygon 
		List<int[]> steps = new ArrayList<int[]>();
		int[] startingPoint = findStartingPoint(neighbourhood);
		steps.add(startingPoint);


		//Step 2: Walk the perimeter of the polygon until reach of starting point
		//For initialisation purposes, we assume that we arrived to current point from [x-1, y] position
		//This is selected as starting point is selected based on neighbour with min(x)
		int[] previousPoint = new int[] {startingPoint[0]-1, startingPoint[1]}; 
		int[] currentPoint = new int[] {startingPoint[0], startingPoint[1]};
		int[] nextPoint = findNextPerimeterPoint(currentPoint, previousPoint, neighbourhood);
		
		int previousDirection = -1; //initialisation different from zero that is default
		int direction = 0;
		
		while (!check2ElementArrayEquality(nextPoint, startingPoint)) {
			
			// Check if the point is in the same direction. In this case, 
			// only first and last points are kept as everything else is points
			// on a line. Therefore, last point is substituted by the new one
			direction = getStepDirection(currentPoint, nextPoint);
			if (previousDirection == direction) {
				steps.remove(steps.size() - 1);
			} 
			steps.add(new int[] {nextPoint[0], nextPoint[1]});
			previousDirection = direction;

			//copy current to history and next to current 
			previousPoint[0] = currentPoint[0];
			previousPoint[1] = currentPoint[1];
			currentPoint[0] = nextPoint[0];
			currentPoint[1] = nextPoint[1];

			nextPoint = findNextPerimeterPoint(currentPoint, previousPoint, neighbourhood);
		}
		
		//Step 3: Construct the polygon
		Polygon polygon = new Polygon();
		double elemSize = SettingUtils.getElementSize();
		for (int[] step : steps) {
			//Old implementation not considering elemSize
			//polygon.add(new Point(new Coord3d(step[0], step[1]  , topZIndex)), false);
			Coord3d coordinates = new Coord3d(step[0]*elemSize, step[1]*elemSize  , topZIndex*elemSize);
			polygon.add(new Point(coordinates), false);
		}
		
		
//		ExcelWriters.writeCoordinatesToExcel("polygon.xlsx", steps , 1);
		return polygon;
	}

	/**
	 * @param previousPoint
	 * @param currentPoint
	 * @return
	 */
	private static int getStepDirection(int[] previousPoint, int[] currentPoint) {
		if (currentPoint[0] - previousPoint[0] ==  1) return 1;
		if (currentPoint[0] - previousPoint[0] == -1) return 2;
		if (currentPoint[1] - previousPoint[1] ==  1) return 3;
		if (currentPoint[1] - previousPoint[1] == -1) return 4;
		//If not caught above then non valid step
		System.out.println("Error: Invalid step");
		return 0;
	}

	/**
	 * @param vertex - vertex with min(x) and min(y) coordinate
	 * @param topZIndex - index of polygon in Z axis
	 * @return generated polygon
	 */
	private static Polygon convertToPolygon(int[] vertex, int topZIndex) {
		Polygon polygon = new Polygon();
		double elemSize = SettingUtils.getElementSize();
		double xCoord = vertex[0] * elemSize;
		double yCoord = vertex[1] * elemSize;
		
		//Old implementation not considering element Size
//		polygon.add(new Point(new Coord3d(vertex[0]  , vertex[1]  , topZIndex)), false);
//		polygon.add(new Point(new Coord3d(vertex[0]+1, vertex[1]  , topZIndex)), false);
//		polygon.add(new Point(new Coord3d(vertex[0]+1, vertex[1]+1, topZIndex)), false);
//		polygon.add(new Point(new Coord3d(vertex[0]  , vertex[1]+1, topZIndex)), false);

		polygon.add(new Point(new Coord3d(xCoord  			, yCoord  		  , topZIndex * elemSize)), false);
		polygon.add(new Point(new Coord3d(xCoord + elemSize	, yCoord  		  , topZIndex * elemSize)), false);
		polygon.add(new Point(new Coord3d(xCoord + elemSize	, yCoord+elemSize , topZIndex * elemSize)), false);
		polygon.add(new Point(new Coord3d(xCoord  			, yCoord+elemSize , topZIndex * elemSize)), false);
		return polygon;
	}

	/**
	 * @param neighbourhood
	 * @return
	 */
	private static int[] findStartingPoint(List<int[]> neighbourhood) {
		//Find min(x) and for that x min(y)
		int px = Integer.MAX_VALUE; //starting vertex x coordinate
		int py = Integer.MAX_VALUE; //starting vertex y coordinate
		for (int[] point : neighbourhood) {
			if (px > point[0]) { //check for min(x)
				px = point[0];
				py = point[1];
			} else if (px == point[0]) { //if x is the same check for min(y)
				if (py > point[1]) {
					py = point[1];
				}
			}
		}
		return new int[] {px,py};
	}

	/**
	 * @param currentPoint - the current position
	 * @param previousPoint - point before coming to current position
	 * @param neighbourhood - available points to walk to
	 */
	private static int[] findNextPerimeterPoint(int[] currentPoint, int[] previousPoint, List<int[]> neighbourhood) {
		int[] previousDirection = new int[] {currentPoint[0] - previousPoint[0], currentPoint[1] - previousPoint[1]};


		int searchAttempts = 0;
		while (searchAttempts < 4) { //4 are the possible directions from current point
			int[] nextDirection = getNextDirection(previousDirection);
			int[] nextPoint = new int[] {currentPoint[0] + nextDirection[0], currentPoint[1] + nextDirection[1]};
			for (int[] neighbour : neighbourhood) {
				if (check2ElementArrayEquality(neighbour, nextPoint)) {
					return neighbour;
				} 
			}

			//Failed to find in this direction so the checked becomes previous and the loop continues to seek for next
			//Negative symbol is to indicate that we are coming from the checked direction and not going to.
			previousDirection[0] = -nextDirection[0];
			previousDirection[1] = -nextDirection[1];
		}

		System.out.println("Something went wrong and no suitable neighbouring point found");
		return null;

	}

	/**
	 * The cyclic order to use in order to find the next point is 
	 * 1. y-1
	 * 2. x+1
	 * 3. y+1
	 * 4. x-1
	 * 
	 * @param previousDirection - direction from which arrived to current point
	 * @return direction to look for the next point
	 */
	private static int[] getNextDirection(int[] previousDirection) {
		if (previousDirection[0] ==  1 && previousDirection[1] ==  0) return new int[] { 0,-1};
		if (previousDirection[0] ==  0 && previousDirection[1] ==  1) return new int[] { 1, 0};
		if (previousDirection[0] == -1 && previousDirection[1] ==  0) return new int[] { 0, 1};
		if (previousDirection[0] ==  0 && previousDirection[1] == -1) return new int[] {-1, 0};

		//if none of the above is true then the previous direction is not a valid one
		System.out.println("[" + previousDirection[0] + "," +previousDirection[1] + "]" + " direction is not a valid one");
		return null;
	}


	/**
	 * @param array1
	 * @param array2
	 * @return true if the two arrays have the same 2 first elements
	 */
	private static boolean check2ElementArrayEquality(int[] array1, int[] array2) {
		if (array1[0] == array2[0] && array1[1] == array2[1]) return true;
		return false;
	}

	/**
	 * @param polygonI - an int[2] containing the xy index of reference polygon to check 
	 * if it has neighbours with similar zIndex
	 * 
	 * @param zIndexes - zIndex of every element of the part
	 */
	private static List<int[]> findNeighbours(int[] polygonI, int[][] zIndexes) {

		List<int[]> neighbours = new ArrayList<int[]>();

		int xmax = zIndexes.length; //xmin=0
		int ymax = zIndexes[0].length; //ymin=0
		
		// Neighbour (x-1, y)
		if (polygonI[0]-1 > 0) { //check if out of bounds
			if (zIndexes[polygonI[0]][polygonI[1]] == zIndexes[polygonI[0]-1][polygonI[1]  ]) {
				//Construct the neighbour polygon
				neighbours.add( new int[] { polygonI[0]-1, polygonI[1]  });
			}
			
		}

		// Neighbour (x, y-1)
		if (polygonI[1]-1 > 0) { //check if out of bounds
			//Construct the neighbour polygon
			if (zIndexes[polygonI[0]][polygonI[1]] == zIndexes[polygonI[0]  ][polygonI[1]-1]) {
				//Construct the neighbour polygon
				neighbours.add( new int[] { polygonI[0]  , polygonI[1]-1});
			}
		}

		// Neighbour (x+1, y)
		if (polygonI[0]+1 < xmax) { //check if out of bounds
			//Construct the neighbour polygon
			if (zIndexes[polygonI[0]][polygonI[1]] == zIndexes[polygonI[0]+1][polygonI[1]  ]) {
				//Construct the neighbour polygon
				neighbours.add( new int[] { polygonI[0]+1, polygonI[1]  });
			}
		}

		// Neighbour (x, y+1)
		if (polygonI[1]+1 < ymax) { //check if out of bounds
			//Construct the neighbour polygon
			if (zIndexes[polygonI[0]][polygonI[1]] == zIndexes[polygonI[0]  ][polygonI[1]+1]) {
				//Construct the neighbour polygon
				neighbours.add( new int[] { polygonI[0]  , polygonI[1]+1});
			}
		}
		return neighbours;
	}

}
