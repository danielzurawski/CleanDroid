/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cleandroid.occupancyGridMap;

import java.util.LinkedList;

/**
 * This class provides patfh finding algorithms used for the robots navigation.
 * @author Team BLUE 
 */
public class PathFinder {

	LinkedList<GridCell> path;
	
	/**
     * BFS (BreadthFirstSearch) to determine the path. This method assumes that
     * you are suppling the startNode, as well as the goalNode
     *
     * @param startNode
     * @param goalNode
     * @return constructPath(goalNode) outcome of the constructPath method
     */
	public LinkedList<GridCell> search(GridCell startCell, GridCell goalCell) {
        
		// list of visited nodes
        LinkedList<GridCell> closedList = new LinkedList<GridCell>();
        
        // list of nodes to visit (sorted)
        LinkedList<GridCell> openList = new LinkedList<GridCell>();
        
        openList.add(startCell);
        startCell.setParent(null);

        while (!openList.isEmpty()) {

        	// Remove the first element of the open List and assign it to a cell variable, we will use this then to evaluate it if it equals to the goal cell.
            GridCell cell = (GridCell) openList.removeFirst();
            
            // Check if the first cell from the open list equals to the goalCell
            if (cell.equals(goalCell)){
                
            	// Gratz, you have just found your path, now construct it
                return constructPath(goalCell);
            } else {
                
            	closedList.add(cell);
                
                // add neighbors to the open list
                for (int j = 0; j < cell.getNeighbourCells().size(); j++) {
                	//System.out.println("Entered for loop getNeighbourCells. Content of j: " + cell.getNeighbourCells().get(j));
                	GridCell neighbourCell = (GridCell) cell.getNeighbourCells().get(j);
                	if (!closedList.contains(neighbourCell) && !openList.contains(neighbourCell)) {
                		neighbourCell.setParent(cell);
                		openList.add(neighbourCell);
                	}
                }
            }
        }
        
        return null;
    }

    public LinkedList<GridCell> constructPath(GridCell cell) {

        path = new LinkedList<GridCell>();
        while (cell.getParent() != null) {
            path.addFirst(cell);
            cell = cell.getParent();
        }
        return path;
    }

    

}