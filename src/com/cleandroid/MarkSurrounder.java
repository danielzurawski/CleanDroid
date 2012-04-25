package com.cleandroid;

import java.util.ArrayList;

import com.cleandroid.occupancyGridMap.GridCell;
import com.cleandroid.occupancyGridMap.GridMap;

public class MarkSurrounder{
	/**
	 * Robots currently operating SLAM algorithm
	 */
	private Slam2 slam;
	/**
	 * Robots list of unvisitedCells.
	 */
	private ArrayList<GridCell> unvisitedCells;
	
	/**
	 * Start four threads that will concurrently monitor robots surroundings four directions - EAST, WEST, SOUTH and NORTH. 
	 * These threads maintain neighbour relations between the cells (for the search algorithm), as well as mark the cells as either 0 (unvisited but reachable) or 1 (visited but reachable).
	 * They also maintain a list of unvisited cells for the Slam2.
	 * @param slam Slam2 - robots currently running SLAM algorithm
	 */
	public MarkSurrounder(Slam2 slam){
		this.slam = slam;
		this.unvisitedCells = slam.getUnvisitedCells();
		
		checkEast();
		checkWest();
		checkSouth();
		checkNorth();
	}
	
	/**
	 * Check east thread that marks the east of the robot.
	 */
	public void checkEast(){
		Thread eastThread = new Thread(){
			public void run(){
				while(true){
					if (!slam.getStop() || slam.getReturnToBase() || slam.getAreaToCleanSelected()) {
						if(!slam.isAdjusting() && !slam.isResizingMap()){
							if(!slam.isTurning() && !slam.isStalled()){		
								double tempX = slam.getX();
								double tempY = slam.getY();
								
								if (slam.east() > 0.262 + slam.round(slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()]
										.getX() - tempX)) {
				
									if (!slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1].isVisited()
											&& slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1]
													.getType() != 5
											&& slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1]
													.getType() != 7) {
				
										slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1].setType(0);
				
										if (!(unvisitedCells
												.contains(slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1]))) {
											unvisitedCells
													.add(slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1]);
										}
									} else {
										slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1].setType(1);
									}
				
									slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()].addNeighbourCell(slam.getMap()
											.getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1]);
				
									if (slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() + 1]
											.getType() != -1
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() + 1]
													.getType() != 3) {
				
										slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1]
												.addNeighbourCell(slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() + 1]);
				
									}
				
									if (slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() + 1]
											.getType() != -1
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() + 1]
													.getType() != 3) {
										slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1]
												.addNeighbourCell(slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() + 1]);
									}
				
								} else {
									slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1].setType(-1);
				
									unvisitedCells
											.remove(slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1]);
				
									slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()].removeNeighbourCell(slam.getMap()
											.getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() + 1]);
				
								}
								
							}
						}
					}
					
					try {
						Thread.sleep(70);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		// Start the thread
		eastThread.start();
	}
	
	/**
	 * Check west thread which marks the west of the robot.
	 */
	public void checkWest(){
		Thread westThread = new Thread(){
			public void run(){
				while(true){	
					if (!slam.getStop() || slam.getReturnToBase() || slam.getAreaToCleanSelected()) {
						if(!slam.isAdjusting() && !slam.isResizingMap()){
							if(!slam.isTurning() && !slam.isStalled()){
								double tempX = slam.getX();
								double tempY = slam.getY();
								
								if (slam.west() > 0.262 + slam.round(tempX
										- slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()].getX())) {
				
									if (!slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1].isVisited()
											&& slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1]
													.getType() != 5
											&& slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1]
													.getType() != 7) {
				
										slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1].setType(0);
				
										if (!(unvisitedCells
												.contains(slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1]))) {
											unvisitedCells
													.add(slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1]);
										}
				
									} else {
										slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1].setType(1);
									}
				
									slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()].addNeighbourCell(slam.getMap()
											.getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1]);
				
									if (slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() - 1]
											.getType() != -1
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() - 1]
													.getType() != 3) {
				
										slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1]
												.addNeighbourCell(slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() - 1]);
				
									}
				
									if (slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() - 1]
											.getType() != -1
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() - 1]
													.getType() != 3) {
				
										slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1]
												.addNeighbourCell(slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() - 1]);
				
									}
				
								} else {
				
									slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1].setType(-1);
				
									unvisitedCells
											.remove(slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1]);
				
									slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()].removeNeighbourCell(slam.getMap()
											.getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX() - 1]);
				
								}
								
							}
						}
					}
					
					try {
						Thread.sleep(70);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		};
		
		// Start the thread
		westThread.start();
	}
	
	/**
	 * Check south which marks the south of the robot.
	 */
	public void checkSouth(){
		Thread southThread = new Thread(){
			public void run(){
				while(true){
					if (!slam.getStop() || slam.getReturnToBase() || slam.getAreaToCleanSelected()) {
						if(!slam.isAdjusting() && !slam.isResizingMap()){
							if(!slam.isTurning() && !slam.isStalled()){
								double tempX = slam.getX();
								double tempY = slam.getY();
								
								if (slam.south() > 0.262 + slam.round(tempY
										- slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()].getY())) {
				
									if (!slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()].isVisited()
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()]
													.getType() != 5
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()]
													.getType() != 7) {
				
										slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()].setType(0);
				
										if (!(unvisitedCells
												.contains(slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()]))) {
											unvisitedCells
													.add(slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()]);
										}
				
									} else {
										slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()].setType(1);
									}
				
									slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()].addNeighbourCell(slam.getMap()
											.getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()]);
				
									if (slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() + 1]
											.getType() != -1
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() + 1]
													.getType() != 3) {
				
										slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()]
												.addNeighbourCell(slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() + 1]);
				
									}
				
									if (slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() - 1]
											.getType() != -1
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() - 1]
													.getType() != 3) {
				
										slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()]
												.addNeighbourCell(slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX() - 1]);
				
									}
				
								} else {
				
									slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()].setType(-1);
				
									unvisitedCells
											.remove(slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()]);
				
									slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()].removeNeighbourCell(slam.getMap()
											.getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()]);
				
								}
							}
						}
					}
					
					try {
						Thread.sleep(70);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		};
		
		// Start the thread.
		southThread.start();
	}
	
	/**
	 * Check north which marks the north of the robot.
	 */
	public void checkNorth(){
		Thread northThread = new Thread(){
			public void run(){
				while(true){	
					if (!slam.getStop() || slam.getReturnToBase() || slam.getAreaToCleanSelected()) {
						if(!slam.isAdjusting() && !slam.isResizingMap()){
							if(!slam.isTurning() && !slam.isStalled()){
								double tempX = slam.getX();
								double tempY = slam.getY();
								
								if (slam.north() > 0.262
										+ slam.round(slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()].getY())
										- tempY) {
				
									if (!slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX()].isVisited()
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()]
													.getType() != 5
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() + 1][slam.getCurrentIndexX()]
													.getType() != 7) {
				
										slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX()].setType(0);
				
										if (!(unvisitedCells
												.contains(slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX()]))) {
											unvisitedCells
													.add(slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX()]);
										}
				
									} else {
										slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX()].setType(1);
									}
				
									slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()].addNeighbourCell(slam.getMap()
											.getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX()]);
				
									if (slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() + 1]
											.getType() != -1
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() + 1]
													.getType() != 3) {
				
										slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX()]
												.addNeighbourCell(slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() + 1]);
				
									}
				
									if (slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() - 1]
											.getType() != -1
											&& slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() - 1]
													.getType() != 3) {
				
										slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX()]
												.addNeighbourCell(slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX() - 1]);
				
									}
				
								} else {
				
									slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX()].setType(-1);
				
									unvisitedCells
											.remove(slam.getMap().getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX()]);
				
									slam.getMap().getGrid()[slam.getCurrentIndexY()][slam.getCurrentIndexX()].removeNeighbourCell(slam.getMap()
											.getGrid()[slam.getCurrentIndexY() - 1][slam.getCurrentIndexX()]);
				
								}
							}
						}
					}
					
					try {
						Thread.sleep(70);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		// Start the thread.
		northThread.start();
	}
}
