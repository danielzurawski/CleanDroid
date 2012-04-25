package com.cleandroid.occupancyGridMap;

import java.io.Serializable;

/**
 * 
 * @author Team Blue - This is a GridMap class that contains the two
 *         dimensional array grid that represents the occupancy grid map of the
 *         environment
 */
public class GridMap implements Serializable {

	private static final long serialVersionUID = 4492764423187988185L;
	
	// Map representation of the environment
	public GridCell[][] grid;
	
	/**
	 * The current MaxY index used by the GUI classes for focusing correctly onto the drawn map.
	 */
	private int currentMaxY;
	
	/**
	 * The current MaxX index used by the GUI classes for focusing correctly onto the drawn map.
	 */
	private int currentMaxX;
	
	/**
	 * Robot's current Index X on the GridMap.
	 */
	private int currentIndexX;
	
	/**
	 * Robot's current Index Y on the GridMap.
	 */
	private int currentIndexY;
	
	private Point minPoint;
	private Point maxPoint;
	private Point mapPoint;

	/**
	 * GridMap constructor, allows you to specify the dimensions of the GridMap
	 * to be constructed - the map is implemented using a two dimensional array
	 * 
	 * 
	 * @param firstDimension
	 *            this is the Y-axis dimension of the array
	 * @param secondDimension
	 *            this is the X-axis dimension of the array
	 */
	public GridMap(int firstDimension, int secondDimension) {
		grid = new GridCell[firstDimension][secondDimension];
		setCurrentMaxY(firstDimension);
		setCurrentMaxX(secondDimension);

	}

	/**
	 * Method provides a resize functionality for the map two-dimensional array
	 * @param newHeight - int the new height of the map after resizing
	 * @param newWidth  - int the new width of the map after resizing
	 */
	public void resizeMap(int newHeight, int newWidth) {

		GridCell[][] resizedMap = new GridCell[newHeight][newWidth];

		int changingHeight = ((newHeight - grid.length) / 2);
		int changingWidth = ((newWidth - grid[0].length) / 2);

		double tempX = grid[0][0].getX() - changingWidth * 0.25;
		double tempY = grid[0][0].getY() + changingHeight * 0.25;

		for (int i = 0; i < resizedMap.length; i++) {
			for (int j = 0; j < resizedMap[0].length; j++) {

				resizedMap[i][j] = new GridCell(3);
				resizedMap[i][j].setyIndex(i);
				resizedMap[i][j].setxIndex(j);

				resizedMap[i][j].setX(tempX + j * 0.25);
				resizedMap[i][j].setY(tempY - i * 0.25);

			}
		}

		System.out.println("Changing Height: " + changingHeight
				+ "Changing Width:" + changingWidth);

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				resizedMap[i + changingHeight][j + changingWidth] = grid[i][j];
				resizedMap[i + changingHeight][j + changingWidth].setyIndex(i
						+ changingHeight);
				resizedMap[i + changingHeight][j + changingWidth].setxIndex(j
						+ changingWidth);

				// resizedMap[i][j].setType(grid[i-changingHeight][j-changingWidth].getType());
			}
		}

		maxPoint.setX(changingWidth + maxPoint.getX());
		minPoint.setX(changingWidth + minPoint.getX());

		maxPoint.setY(changingHeight + maxPoint.getY());
		minPoint.setY(changingHeight + minPoint.getY());

		this.setGrid(resizedMap);
	}

	public GridCell[][] getGrid() {
		return grid;
	}

	public void setGrid(GridCell[][] grid) {
		this.grid = grid;
	}

	public Point getMapPoint() {
		return mapPoint;
	}
	public int getCurrentMaxY() {
		return currentMaxY;
	}

	public void setCurrentMaxY(int currentMaxY) {
		this.currentMaxY = currentMaxY;
	}

	public int getCurrentMaxX() {
		return currentMaxX;
	}

	public void setCurrentMaxX(int currentMaxX) {
		this.currentMaxX = currentMaxX;
	}

	public int getCurrentIndexX() {
		return currentIndexX;
	}

	public void setCurrentIndexX(int currentIndexX) {
		this.currentIndexX = currentIndexX;
	}

	public int getCurrentIndexY() {
		return currentIndexY;
	}

	public void setCurrentIndexY(int currentIndexY) {
		this.currentIndexY = currentIndexY;
	}

	public Point getMaxPoint() {
		return this.maxPoint;
	}

	public void setMaxPoint(Point point) {

		this.maxPoint = point;
	}

	public Point getMinPoint() {
		return this.minPoint;
	}

	public void setMinPoint(Point point) {
		this.minPoint = point;
	}

	public void setMapPoint(Point point) {
		this.mapPoint = point;
	}

}
