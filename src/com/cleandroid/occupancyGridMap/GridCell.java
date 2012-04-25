package com.cleandroid.occupancyGridMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * This is class encapsulated data related to each individual cell on the GridMap. 
 * @author Team Blue 
 */
public class GridCell implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Size of the cell in the GridMap.
	 */
	public static final double size = 0.25;
	
	// Type can be either 1 for obstacle or 0 for reachable
	/**
	 * Type of the cell (0 unvisited, -1 obstacle, 1 covered, 2 current, 3 unknown, 7 charging station)
	 */
	private int type;
	
	/**
	 * X coordinate of the cell.
	 */
	private double X;
	
	/**
	 * Y coordinate of the cell.
	 */
	private double Y;
	
	/**
	 * X index of the cell on the GridMap.
	 */
	private int xIndex;
	
	/**
	 * Y index of the cell on the GridMap.
	 */
	private int yIndex;
	
	/**
	 * Boolean visited - has the cell been visited?
	 */
	private boolean visited = false;
	
	/**
	 * Boolean - is the cell currently selected? (cell selection)
	 */
	private boolean isSelected;
	
	/**
	 * Parent of the cell. (for pathfindong)
	 */
	private GridCell parent;
	
	/**
	 * A list of neighbour cells to this cell. This is used exclusively for the path finding algorithm.
	 */
	private ArrayList<GridCell> neighbourCells = new ArrayList<GridCell>();
	
	/**
	 * Date and time of when the cell has been covered.
	 */
	private Date lastCleaned;

	/**
	 * An empty constructor, if a generic GridCell has to be constructed.
	 */
	public GridCell() {}

	/**
	 * Generic GridCell constructor with the type parameter.
	 * @param type - type of the cell (0 unvisited, -1 obstacle, 1 covered, 2 current, 3 unknown, 7 charging station)
	 */
	public GridCell(int type) {
		this.type = type;
	}

	/**
	 * Complete constructor for the GridCell
	 * @param type - type of the cell (0 unvisited, -1 obstacle, 1 covered, 2 current, 3 unknown, 7 charging station)
	 * @param x - double x coordinate of the cell
	 * @param y - double y coordinate of the cell
	 * @param visited - boolean has the cell been visited?
	 */
	public GridCell(int type, double x, double y, boolean visited) {
		this.type = type;
		this.X = x;
		this.Y = y;
		this.visited = visited;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public double getY() {
		return Y;
	}

	public void setY(double y) {
		Y = y;
	}

	public double getX() {
		return X;
	}

	public void setX(double x) {
		X = x;
	}

	public GridCell getParent() {
		return this.parent;
	}

	public void setParent(GridCell cell) {
		this.parent = cell;
	}

	public String toString() {
		return "X cord: " + X + ", Y cord: " + Y + ", Y index: " + yIndex + ", X index: " + xIndex;
	}

	/**
	 * Method that adds a neighbour cell to the neighbourCells list for the GridCell. 
	 * If the cell doesn't contain that neighbour yet then add it, if it does, then discard the operation.
	 * @param neighbour - GridCell neighbour to be added to relation
	 */
	public void addNeighbourCell(GridCell neighbour) {
		if(!neighbourCells.contains(neighbour)){
			neighbourCells.add(neighbour);
		}
	}
	
	/**
	 * A method that provides functionality for removing a specific GridCell that is a neighbour of a cell.
	 * @param neighbour GridCell neighbour cell to be removed
	 */
	public void removeNeighbourCell(GridCell neighbour){
		neighbourCells.remove(neighbour);
	}

	public ArrayList<GridCell> getNeighbourCells() {
		return this.neighbourCells;
	}

	public int getxIndex() {
		return xIndex;
	}

	public void setxIndex(int xIndex) {
		this.xIndex = xIndex;
	}

	public int getyIndex() {
		return yIndex;
	}

	public void setyIndex(int yIndex) {
		this.yIndex = yIndex;
	}
	
	public Date getLastCleaned() {
		return lastCleaned;
	}

	public void setLastCleaned(Date date) {
		this.lastCleaned = date;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
}
