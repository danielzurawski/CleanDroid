package com.cleandroid;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import javaclient3.Position2DInterface;
import javaclient3.RangerInterface;
import com.cleandroid.occupancyGridMap.GridCell;
import com.cleandroid.occupancyGridMap.GridMap;
import com.cleandroid.occupancyGridMap.PathFinder;

/**
 * SLAM implementation for the CleanDroids robotic vacuum cleaner. This is the core component of the Robot Control Software 
 * and it provides method for robots localisation, mapping, adjusting and recovering in an unknown environment. 
 * @author Team BLUE
 *
 */
public class Slam2 extends Robot implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The current map in which the robot operates.
	 */
	private GridMap map;
	
	/**
	 * The currents map chargingStation to which the robot returns after a specific action or the cleaning has been performed. 
	 */
	private GridCell chargingStation;
	
	/**
	 * An instance of the PathFinder class which provides a search algorithm (Breadth-First-Search) for the robots navigation.
	 */
	private PathFinder pathFinder;
	
	/**
	 * A list of cells to be visited in order after a search has been performed.
	 */
	private LinkedList<GridCell> toVisitInOrder;
	
	/**
	 * A list of maintained unvisited cells, yet to be visited.
	 */
	private ArrayList<GridCell> unvisitedCells;
	
	/**
	 * Robots current index X within the GridMap.
	 */
	private int currentIndexX;
	
	/**
	 * Robots current index Y within the GridMap.
	 */
	private int currentIndexY;
	
	/**
	 * Count of thread executions (the occupying state counter).
	 */
	private int step;
	
	/**
	 * Current time.
	 */
	private Date time;
	
	/**
	 * Start time of the Slam execution.
	 */
	private String startTime;
	
	/**
	 * Linked list of currently selected cells.
	 */
	private LinkedList<GridCell> selectedCells;
	
	/**
	 * The distance thread used for determining the robots current state and calling the localize method if the state allows.
	 */
	private DistanceThread distanceThread;
	
	/**
	 * Collision recovery module thread which tries to recover the robot after a collision.
	 */
	private CollisionDetector collisionDetector;
	
	/**
	 * Mark surrounder thread which marks the surrounding with 0 or 1s, maintains the neighbour relations between the cells (for pathfinding) and maintains a list of unvisited cells.
	 */
	private MarkSurrounder markSurrounder;
	
	/**
	 * Flag for stopping the robots Slam (occupying state).
	 */
	private boolean stop = false;
	
	/**
	 * Flag for the recovering state.
	 */
	private boolean recovering = false;
	
	/**
	 * Flag to indicate whether Slam has been already started.
	 */
	private boolean started = false;
	
	/**
	 * Flag for the returning to base state.
	 */
	private boolean returnToBase = false;
	
	/**
	 * Flag for the area to clean selected state.
	 */
	private boolean areaToCleanSelected = false;
	
	/**
	 * Flag for the evaluating environment state.
	 */
	private boolean evaluatingEnvironment = false;
	
	/**
	 * Flag for the adjusting state - this is when the robot is adjusting to stand precisely in the middle of a current cell.
	 */
	private boolean adjusting = false;
	
	/**
	 * Flag for the following path state.
	 */
	private boolean followingPath = false;
	
	/**
	 * Flag for the turning state. 
	 */
	private boolean turning = false;
	
	/**
	 * Flag for when the robot is stalled.
	 */
	private boolean stalled = false;
	
	/**
	 * Flag for when the robot is resizing the map, because the environment was larger than we expected.
	 */
	private boolean resizingMap = false;
	
	private boolean pathNotFound = false;
	private double tempX;
	private double tempY;

	/**
	 * Slam2 constructor that initializes the environment map, various lists
	 */
	public Slam2() {
		map = new GridMap(251, 251);
		pathFinder = new PathFinder();
		unvisitedCells = new ArrayList<GridCell>();
		toVisitInOrder = new LinkedList<GridCell>();
		selectedCells = new LinkedList<GridCell>();

		currentIndexX = 125;
		currentIndexY = 125;

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		startTime = dateFormat.format(date);

		while (!pos2D.isDataReady());
		x = pos2D.getX();
		y = pos2D.getY();
		tempX = pos2D.getX() - 125 * 0.25;
		tempY = pos2D.getY() + 125 * 0.25;
		heading = determineYaw(pos2D.getYaw());
		stall = pos2D.getData().getStall();
		
		for (int i = 0; i < 251; i++) {
			for (int j = 0; j < 251; j++) {
				map.getGrid()[i][j] = new GridCell(3);

				map.getGrid()[i][j].setyIndex(i);
				map.getGrid()[i][j].setxIndex(j);

				map.getGrid()[i][j].setX(tempX + 0.25 * j);
				map.getGrid()[i][j].setY(tempY - 0.25 * i);
			}
		}

		map.getGrid()[currentIndexY][currentIndexX].setType(7);
		setChargingStation(map.getGrid()[currentIndexY][currentIndexX]);

		map.setCurrentIndexX(currentIndexX);
		map.setCurrentIndexY(currentIndexY);

		refresh4Sides();
		markSurrounding();

		map.getGrid()[currentIndexY][currentIndexX].setX(pos2D.getX());
		map.getGrid()[currentIndexY][currentIndexX].setY(pos2D.getY());
		step = 0;

		System.out.printf(
				"front: %6.2f, left: %6.2f, right: %6.2f, back: %6.2f\n",
				front, left, right, back);

		System.out.println("YAW: " + heading);

		printMap();

		// distanceThread = new DistanceThread(this);
		//marker = new MarkSurroundingThread(this);
		distanceThread = new DistanceThread(this);
		collisionDetector = new CollisionDetector(this);
		dataPool();
	}

	/**
	 * Flag for returning to the base
	 * 
	 * @param base
	 */
	public void setReturnToBase(boolean base) {
		if (base) {
			setStop(true);
		}
		this.returnToBase = base;
	}

	/**
	 * Flag for cleaning the selected area
	 * 
	 * @param set
	 */
	public void setAreaToCleanSelected(boolean set) {
		//setStop(true);
		this.areaToCleanSelected = set;
	}

	/**
	 * Slam2 run method which starts all of the dependencies (threads that have to run concurrently).
	 * Monitors the robots states and acts accordingly to the currently selected state. 
	 */
	public void run() {

		distanceThread.start();
		markSurrounder = new MarkSurrounder(this);
		collisionDetector.start();

		while (true) {
			while (!stop && !recovering) {
				System.out.println("Occupying...");
				occupy();
				printMap();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			pos2D.setSpeed(0, 0);

			if (returnToBase) {

				returnToBase();
				setReturnToBase(false);
			}
			if (areaToCleanSelected) {
				// this.setSelection(x1ToBeCleaned, y1ToBeCleaned,
				// x2ToBeCleaned, y2ToBeCleaned);
				if (selectedCells == null) {
					System.out.println("why didnt it selection");
				} else {
					cleanSelectedArea();
				}
			}

			try {
				Thread.sleep(10);
				// Thread.sleep(800);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Method printing out the robots current internal map with the focus on the first 18 rows and 18 columns.
	 * It also indicates the robot current position, as well as prints the row and column numbers.
	 */
	public void printMap() {
		System.out.println("\n Step = " + step + " X: " + x + ", Y: " + x);

		for (int i = currentIndexY - 9; i < currentIndexY + 9; i++) {
			for (int j = currentIndexX - 9; j < currentIndexX + 9; j++) {
				if (i == currentIndexY - 9 && j == currentIndexX - 9) {
					System.out.print("     ");
				} else if (i == currentIndexY - 9) {
					System.out.printf("[%3d]", j);
				} else if (j == currentIndexX - 9) {
					System.out.printf("[%3d]", i);
				} else {
					// System.out.printf(", I: " + i + ", J: " + j);
					try {
						System.out.printf("%5d", map.getGrid()[i][j].getType());
					} catch (Exception e) {
						System.out.println("Error occurs at I: " + i + " J: "
								+ j);
					}

				}
			}
			System.out.println();
		}
	}

	/**
	 * The method that checks the surrounding of the robot according to the following
	 * consequence: EAST, WEST, SOUTH, NORTH
	 * which maintains the zigzag movement
	 */
	public void occupy() {
		// east

		if (map.getGrid()[currentIndexY][currentIndexX + 1].getType() == 0) {

			// unvisitedCells
			// .remove(map.getGrid()[currentIndexY][currentIndexX + 1]);

			this.moveTo(map.getGrid()[currentIndexY][currentIndexX + 1]);

		}

		// west
		else if (map.getGrid()[currentIndexY][currentIndexX - 1].getType() == 0) {

			// unvisitedCells
			// .remove(map.getGrid()[currentIndexY][currentIndexX - 1]);

			this.moveTo(map.getGrid()[currentIndexY][currentIndexX - 1]);

		}

		// south
		else if (map.getGrid()[currentIndexY + 1][currentIndexX].getType() == 0) {

			// unvisitedCells
			// .remove(map.getGrid()[currentIndexY + 1][currentIndexX]);

			this.moveTo(map.getGrid()[currentIndexY + 1][currentIndexX]);

		}

		// north
		else if (map.getGrid()[currentIndexY - 1][currentIndexX].getType() == 0) {

			// unvisitedCells
			// .remove(map.getGrid()[currentIndexY - 1][currentIndexX]);

			this.moveTo(map.getGrid()[currentIndexY - 1][currentIndexX]);

		}

		// IF THERE ARE NO UNVISITED CELLS AROUND THE ROBOT

		else {

			pos2D.setSpeed(0, 0);

			System.out.println("Searching for a path...");
			toVisitInOrder = pathFinder.search(
					map.getGrid()[currentIndexY][currentIndexX],
					unvisitedCells.get(unvisitedCells.size() - 1));
			int temp = 2;
			System.out.print("Path found: ");

			while (toVisitInOrder == null && unvisitedCells.size() >= temp) {
				toVisitInOrder = pathFinder.search(
						map.getGrid()[currentIndexY][currentIndexX],
						unvisitedCells.get(unvisitedCells.size() - temp));
				temp++;
			}
			if (toVisitInOrder != null) {
				this.followPath(toVisitInOrder);
			}
		}
		
		if(recovering){
			return;
		}
	}

	/**
	 * Method returning a sonar that points towards the EAST direction, regardless of the robots heading.
	 * @return sonar readout regardless of the robots heading.
	 */
	public double east() {
		if (heading == EAST) {
			return front;
		} else if (heading == SOUTH) {
			return left;
		} else if (heading == WEST) {
			return back;
		} else {
			return right;
		}
	}

	/**
	 * Method returning a sonar that points towards the SOUTH direction, regardless of the robots heading.
	 * @return sonar readout regardless of the robots heading.
	 */
	public double south() {
		if (heading == EAST) {
			return right;
		} else if (heading == SOUTH) {
			return front;
		} else if (heading == WEST) {
			return left;
		} else {
			return back;
		}
	}

	/**
	 * Method returning a sonar that points towards the WEST direction, regardless of the robots heading.
	 * @return sonar readout regardless of the robots heading.
	 */
	public double west() {
		if (heading == EAST) {
			return back;
		} else if (heading == SOUTH) {
			return right;
		} else if (heading == WEST) {
			return front;
		} else {
			return left;
		}
	}

	/**
	 * Method returning a sonar that points towards the NORTH direction, regardless of the robots heading.
	 * @return sonar readout regardless of the robots heading.
	 */
	public double north() {
		if (heading == EAST) {
			return left;
		} else if (heading == SOUTH) {
			return back;
		} else if (heading == WEST) {
			return right;
		} else {
			return front;
		}
	}

	/*
	 * Scans the surrounding and marks cells around the robot accordingly based
	 * on their types. 
	 * This method runs the threads once and it it meant to be used when the MarkSurrounder thread cannot run in the background (in special case states of the robot).
	 */
	public void markSurrounding() {

		// refresh4Sides();
		// while(!pos2D.isDataReady());

		map.getGrid()[currentIndexY][currentIndexX].setType(2);

		/*
		 * System.out.printf(
		 * "front: %6.2f, left: %6.2f, right: %6.2f, back: %6.2f\n", front,
		 * left, right, back);
		 */

		tempX = x;
		tempY = y;

		// ///////////////////////////// EAST \\\\\\\\\\\\\\\\\\\\\\\\\\\\

		Thread eastThread = new Thread() {
			public void run() {

				if (east() > 0.262 + round(map.getGrid()[currentIndexY][currentIndexX]
						.getX() - tempX)) {

					if (!map.getGrid()[currentIndexY][currentIndexX + 1]
							.isVisited()
							&& map.getGrid()[currentIndexY][currentIndexX + 1]
									.getType() != 5
							&& map.getGrid()[currentIndexY][currentIndexX + 1]
									.getType() != 7) {

						map.getGrid()[currentIndexY][currentIndexX + 1]
								.setType(0);

						if (!(unvisitedCells
								.contains(map.getGrid()[currentIndexY][currentIndexX + 1]))) {
							unvisitedCells
									.add(map.getGrid()[currentIndexY][currentIndexX + 1]);
						}
					} else {
						map.getGrid()[currentIndexY][currentIndexX + 1]
								.setType(1);
					}

					map.getGrid()[currentIndexY][currentIndexX]
							.addNeighbourCell(map.getGrid()[currentIndexY][currentIndexX + 1]);

					if (getMap().getGrid()[currentIndexY - 1][currentIndexX + 1]
							.getType() != -1
							&& getMap().getGrid()[currentIndexY - 1][currentIndexX + 1]
									.getType() != 3) {

						map.getGrid()[currentIndexY][currentIndexX + 1]
								.addNeighbourCell(getMap().getGrid()[currentIndexY - 1][currentIndexX + 1]);

					}

					if (getMap().getGrid()[currentIndexY + 1][currentIndexX + 1]
							.getType() != -1
							&& getMap().getGrid()[currentIndexY + 1][currentIndexX + 1]
									.getType() != 3) {
						getMap().getGrid()[currentIndexY][currentIndexX + 1]
								.addNeighbourCell(getMap().getGrid()[currentIndexY + 1][currentIndexX + 1]);
					}

				} else {
					map.getGrid()[currentIndexY][currentIndexX + 1].setType(-1);

					unvisitedCells
							.remove(map.getGrid()[currentIndexY][currentIndexX + 1]);

					map.getGrid()[currentIndexY][currentIndexX]
							.removeNeighbourCell(map.getGrid()[currentIndexY][currentIndexX + 1]);

				}
			}
		};

		// ////////////////////////// WEST \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		Thread westThread = new Thread() {
			public void run() {

				if (west() > 0.262 + round(tempX
						- map.getGrid()[currentIndexY][currentIndexX].getX())) {

					if (!map.getGrid()[currentIndexY][currentIndexX - 1]
							.isVisited()
							&& map.getGrid()[currentIndexY][currentIndexX - 1]
									.getType() != 5
							&& map.getGrid()[currentIndexY][currentIndexX - 1]
									.getType() != 7) {

						map.getGrid()[currentIndexY][currentIndexX - 1]
								.setType(0);

						if (!(unvisitedCells
								.contains(map.getGrid()[currentIndexY][currentIndexX - 1]))) {
							unvisitedCells
									.add(map.getGrid()[currentIndexY][currentIndexX - 1]);
						}

					} else {
						map.getGrid()[currentIndexY][currentIndexX - 1]
								.setType(1);
					}

					map.getGrid()[currentIndexY][currentIndexX]
							.addNeighbourCell(map.getGrid()[currentIndexY][currentIndexX - 1]);

					if (getMap().getGrid()[currentIndexY - 1][currentIndexX - 1]
							.getType() != -1
							&& getMap().getGrid()[currentIndexY - 1][currentIndexX - 1]
									.getType() != 3) {

						getMap().getGrid()[currentIndexY][currentIndexX - 1]
								.addNeighbourCell(getMap().getGrid()[currentIndexY - 1][currentIndexX - 1]);

					}

					if (getMap().getGrid()[currentIndexY + 1][currentIndexX - 1]
							.getType() != -1
							&& getMap().getGrid()[currentIndexY + 1][currentIndexX - 1]
									.getType() != 3) {

						getMap().getGrid()[currentIndexY][currentIndexX - 1]
								.addNeighbourCell(getMap().getGrid()[currentIndexY + 1][currentIndexX - 1]);

					}

				} else {

					map.getGrid()[currentIndexY][currentIndexX - 1].setType(-1);

					unvisitedCells
							.remove(map.getGrid()[currentIndexY][currentIndexX - 1]);

					map.getGrid()[currentIndexY][currentIndexX]
							.removeNeighbourCell(map.getGrid()[currentIndexY][currentIndexX - 1]);

				}
			}
		};

		// ////////////////////////// SOUTH \\\\\\\\\\\\\\\\\\\\\\\\\\

		Thread southThread = new Thread() {
			public void run() {

				if (south() > 0.262 + round(tempY
						- map.getGrid()[currentIndexY][currentIndexX].getY())) {

					if (!map.getGrid()[currentIndexY + 1][currentIndexX]
							.isVisited()
							&& map.getGrid()[currentIndexY + 1][currentIndexX]
									.getType() != 5
							&& map.getGrid()[currentIndexY + 1][currentIndexX]
									.getType() != 7) {

						map.getGrid()[currentIndexY + 1][currentIndexX]
								.setType(0);

						if (!(unvisitedCells
								.contains(map.getGrid()[currentIndexY + 1][currentIndexX]))) {
							unvisitedCells
									.add(map.getGrid()[currentIndexY + 1][currentIndexX]);
						}

					} else {
						map.getGrid()[currentIndexY + 1][currentIndexX]
								.setType(1);
					}

					map.getGrid()[currentIndexY][currentIndexX]
							.addNeighbourCell(map.getGrid()[currentIndexY + 1][currentIndexX]);

					if (getMap().getGrid()[currentIndexY + 1][currentIndexX + 1]
							.getType() != -1
							&& getMap().getGrid()[currentIndexY + 1][currentIndexX + 1]
									.getType() != 3) {

						getMap().getGrid()[currentIndexY + 1][currentIndexX]
								.addNeighbourCell(getMap().getGrid()[currentIndexY + 1][currentIndexX + 1]);

					}

					if (getMap().getGrid()[currentIndexY + 1][currentIndexX - 1]
							.getType() != -1
							&& getMap().getGrid()[currentIndexY + 1][currentIndexX - 1]
									.getType() != 3) {

						getMap().getGrid()[currentIndexY + 1][currentIndexX]
								.addNeighbourCell(getMap().getGrid()[currentIndexY + 1][currentIndexX - 1]);

					}

				} else {

					map.getGrid()[currentIndexY + 1][currentIndexX].setType(-1);

					unvisitedCells
							.remove(map.getGrid()[currentIndexY + 1][currentIndexX]);

					map.getGrid()[currentIndexY][currentIndexX]
							.removeNeighbourCell(map.getGrid()[currentIndexY + 1][currentIndexX]);

				}
			}
		};

		// ///////////////////////// NORTH \\\\\\\\\\\\\\\\\\\\\\\\\

		Thread northThread = new Thread() {
			public void run() {

				if (north() > 0.262
						+ round(map.getGrid()[currentIndexY][currentIndexX]
								.getY()) - tempY) {

					if (!map.getGrid()[currentIndexY - 1][currentIndexX]
							.isVisited()
							&& map.getGrid()[currentIndexY + 1][currentIndexX]
									.getType() != 5
							&& map.getGrid()[currentIndexY + 1][currentIndexX]
									.getType() != 7) {

						map.getGrid()[currentIndexY - 1][currentIndexX]
								.setType(0);

						if (!(unvisitedCells
								.contains(map.getGrid()[currentIndexY - 1][currentIndexX]))) {
							unvisitedCells
									.add(map.getGrid()[currentIndexY - 1][currentIndexX]);
						}

					} else {
						map.getGrid()[currentIndexY - 1][currentIndexX]
								.setType(1);
					}

					map.getGrid()[currentIndexY][currentIndexX]
							.addNeighbourCell(map.getGrid()[currentIndexY - 1][currentIndexX]);

					if (getMap().getGrid()[currentIndexY - 1][currentIndexX + 1]
							.getType() != -1
							&& getMap().getGrid()[currentIndexY - 1][currentIndexX + 1]
									.getType() != 3) {

						getMap().getGrid()[currentIndexY - 1][currentIndexX]
								.addNeighbourCell(getMap().getGrid()[currentIndexY - 1][currentIndexX + 1]);

					}

					if (getMap().getGrid()[currentIndexY - 1][currentIndexX - 1]
							.getType() != -1
							&& getMap().getGrid()[currentIndexY - 1][currentIndexX - 1]
									.getType() != 3) {

						getMap().getGrid()[currentIndexY - 1][currentIndexX]
								.addNeighbourCell(getMap().getGrid()[currentIndexY - 1][currentIndexX - 1]);

					}

				} else {

					map.getGrid()[currentIndexY - 1][currentIndexX].setType(-1);

					unvisitedCells
							.remove(map.getGrid()[currentIndexY - 1][currentIndexX]);

					map.getGrid()[currentIndexY][currentIndexX]
							.removeNeighbourCell(map.getGrid()[currentIndexY - 1][currentIndexX]);

				}
			}
		};

		eastThread.start();
		westThread.start();
		southThread.start();
		northThread.start();

	}

	/**
	 * This method determines whether the robot should move to the front or back in order to reach a cell.
	 * @param cell the target cell for the robot movement
	 */
	public void moveTo(GridCell cell) {

		double x1 = cell.getX();
		double y1 = cell.getY();

		// while(!pos2D.isDataReady());

		double robotX = x;
		double robotY = y;

		double yaw = Math.atan2(y1 - robotY, x1 - robotX);
		double tempDistance = round(distance(x1, y1, robotX, robotY));

		int direction = determineYaw(yaw);

		System.out.println("CurrentX: " + currentIndexX + ", CurrentY: "
				+ currentIndexY + ";  CellX: " + cell.getxIndex() + ", CellY: "
				+ cell.getyIndex());

		// refresh4Sides();

		// markSurrounding();

		if ((left < 0.15 || right < 0.15 || front < 0.05)
				&& direction == (heading + 2) % 4) {

			// System.out.println("Moving back, heading: " + heading);
			moveBack(robotX, robotY, cell, tempDistance);

		} else if (right < 0.15 && direction != heading) {
			pos2D.setSpeed(0, 0);
			System.out.println("Line: 675");
			adjust();

			turnTo((heading + 1) % 4);
			// System.out.println("Moving back, heading: " + heading);
			// markSurrounding();
			moveBack(robotX, robotY, cell, tempDistance);
		} else if (left < 0.15 && direction != heading) {
			pos2D.setSpeed(0, 0);
			System.out.println("Line: 684");
			adjust();

			turnTo((heading + 3) % 4);
			// markSurrounding();
			// System.out.println("Moving back, heading: " + heading);
			moveBack(robotX, robotY, cell, tempDistance);

		} else if (direction == (heading + 2) % 4) {
			pos2D.setSpeed(0, 0);
			// System.out.println("Moving back, heading: " + heading);
			turnTo((heading + 2) % 4);
			// markSurrounding();
			moveFront(robotX, robotY, cell, tempDistance);
		} else if (direction != heading) {

			System.out.println("Line: 700");
			adjust();

			turnTo(direction);
			//markSurrounding();
			//if (cell.getType() != -1) {
				// System.out.println("Moving front, heading: " + heading);
				moveFront(robotX, robotY, cell, tempDistance);
			//}
		} else {
			// turnTo(direction);
			// markSurrounding();
			// System.out.println("Moving front, heading: " + heading);
			moveFront(robotX, robotY, cell, tempDistance);

		}

		//this.markSurrounding();
	}

	public void refresh4Sides() {
		double[] sonarValues;

		while (!sonar.isDataReady())
			;
		sonarValues = sonar.getData().getRanges();

		front = min(sonarValues[12] - 0.2, sonarValues[3] - 0.4,
				sonarValues[4] - 0.4,
				sonarValues[10] * Math.cos(Math.toRadians(5)) - 0.1,
				sonarValues[11] * Math.cos(Math.toRadians(5)) - 0.1);

		left = min(sonarValues[13] - 0.2, sonarValues[5] - 0.1,
				sonarValues[6] - 0.1, 1000, 1000);

		back = min(sonarValues[0], sonarValues[1], sonarValues[2], 1000, 1000);

		right = min(sonarValues[14] - 0.2, sonarValues[7] - 0.1,
				sonarValues[8] - 0.1, 1000, 1000);
	}

	/**
	 * Method moves robot forward towards the given cell, using the robots current X and Y coordinate and a temporary distance to be moved.
	 * The robot also checks if at any point of the movement, a stall has occurred to break the movement and perform a recovery using a background recovery thread. 
	 * @param robotX - double value the robots pos2D X coordinate
	 * @param robotY - double value the robots pos2D Y coordinate
	 * @param cell - the target cell
	 * @param tempDistance - the temporary distance the robot has to move 
	 */
	public void moveFront(double robotX, double robotY, GridCell cell,
			double tempDistance) {

		double distance = round(distance(robotX, robotY, x, y));

		if (!followingPath && heading == EAST) {
			// System.out.println("Heading is EVEN");

			while (x < cell.getX()) {

				// refresh4Sides();
				pos2D.setSpeed(front / 4, 0);
				//System.out.println("EAST Speed is: " + front/4);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// while (!pos2D.isDataReady());
				if(recovering || front<=0 || getStall() == 1) {
					return;
				}
			}
		} else if (!followingPath && heading == WEST) {
			// System.out.println("Heading is WEST");

			while (x > cell.getX()) {

				// refresh4Sides();
				pos2D.setSpeed(front / 4, 0);
				//System.out.println("WEST Speed is: " + front/4);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// while (!pos2D.isDataReady());
				if(recovering || front<=0 || getStall() == 1){
					return;
				}
			}

		} else if (!followingPath && heading == SOUTH) {
			System.out.println("Heading is SOUTH");

			while (round2(y) != round2(cell.getY()) ) {
				// if(pos2D.isDataReady()){

				// while(!pos2D.isDataReady());
				//distance = round(distance(robotX, robotY, x, y));

				pos2D.setSpeed(y - cell.getY(), 0);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if(recovering || getStall() == 1){
					return;
				}
				// }
			}
			pos2D.setSpeed(0, 0);

		}
		else if (!followingPath && heading == NORTH) {
			System.out.println("Heading is NORTH");

			while ( round2(cell.getY()) > round2(y)) {
				// if(pos2D.isDataReady()){

				// while(!pos2D.isDataReady());
				//distance = round(distance(robotX, robotY, x, y));

				pos2D.setSpeed(cell.getY() - y, 0);
				

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if(recovering || getStall() == 1){
					return;
				}
				// }
			}
			pos2D.setSpeed(0, 0);

		}else if (followingPath) {

			System.out
					.println("Heading is even, but it seems following the path, the heading is: "
							+ heading);

			while (distance < tempDistance) {
				
				distance = round(distance(robotX, robotY, x, y));
				
				if(front <= 0){
					pos2D.setSpeed(0.07, 0);
					System.out.println("Speed is: 0.07");
				}else{
					pos2D.setSpeed(front / 6, 0);
					//System.out.println("Speed is: ... " + front/6);
				}	

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(recovering || getStall() == 1){
					return;
				}
			
			}
		}
	}

	/**
	 * Method moves robot backward towards the given cell, using the robots current X and Y coordinate and a temporary distance to be moved.
	 * The robot also checks if at any point of the movement, a stall has occurred to break the movement and perform a recovery using a background recovery thread. 
	 * @param robotX - double value the robots pos2D X coordinate
	 * @param robotY - double value the robots pos2D Y coordinate
	 * @param cell - the target cell
	 * @param tempDistance - the temporary distance the robot has to move 
	 */
	public void moveBack(double robotX, double robotY, GridCell cell,
			double tempDistance) {
		double distance = round(distance(robotX, robotY, x, y));

		if (!followingPath && heading == EAST) {
			// System.out.println("Heading is EVEN");

			while (x > cell.getX()) {

				// refresh4Sides();

				pos2D.setSpeed(-back / 9, 0);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// while (!pos2D.isDataReady());
				if(recovering || back <= 0 || getStall() == 1){
					return;
				}
			}
		} else if (!followingPath && heading == WEST) {
			// System.out.println("Heading is WEST");

			while (x < cell.getX()) {

				// refresh4Sides();

				pos2D.setSpeed(-back / 9, 0);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// while (!pos2D.isDataReady());

				if(recovering || back <= 0 || getStall() == 1){
					return;
				}
			}

		} 
		else if (!followingPath && heading == SOUTH) {
			System.out.println("Heading is SOUTH");
			while (round2(y) < round2(cell.getY())) {

				//distance = round(distance(robotX, robotY, x, y));

				//refresh4Sides();
				pos2D.setSpeed(-(cell.getY() - y), 0);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if(recovering || getStall() == 1){
					return;
				}

			}
			pos2D.setSpeed(0, 0);
		} 
		else if (!followingPath && heading == NORTH) {
			System.out.println("Heading is NORTH");
			while (round2(y) > round2(cell.getY())) {

				//distance = round(distance(robotX, robotY, x, y));

				//refresh4Sides();
				pos2D.setSpeed(-(y - cell.getY()), 0);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if(recovering || getStall() == 1){
					return;
				}

			}
			pos2D.setSpeed(0, 0);
		} 
		else if(followingPath) {
			while (distance < tempDistance) {

				distance = round(distance(robotX, robotY, x, y));
				
				if(back <= 0){
					pos2D.setSpeed(-0.07, 0);
					System.out.println("Speed is: -0.07");
				}else{
					pos2D.setSpeed(-back / 9, 0);
					//System.out.println("Speed is: ... -" + back);
				}	
				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if(recovering || getStall() == 1){
					return;
				}

			}

			// pos2D.setSpeed(0,0);
		}
	}

	/*
	 * Method recover tries to recover the robot from a collision based on the position in which the robot finds itself.
	 */
	public void recover() {
		//turning = false;
		pos2D.setSpeed(0, 0);
		followingPath = false;
		stalled = true;
		recovering = true;

		heading = determineYaw(yaw);
		
		double speed = 0;
		double turn = 0;
		
		if (frontForCrash > back) {
			speed = 1;
		}

		else {
			speed = - 1;
		}
		
		if(Math.round(Math.toDegrees(yaw))%90 != 0){
			if(left > right){
				turn = right;
			}
			else{
				turn = -left;
			}
		}
		System.out.println("Front is: "+front+"; Back is: "+back+"; Speed is: "+speed+"Turn is: " + turn);
		pos2D.setSpeed(speed, turn);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		pos2D.setSpeed(0, 0);

		adjust();
		
		recovering = false;
		stalled = false;
		turning = false;
	}

	/*
	 * Finds out where exactly the robot is on the currently used occupancy grid map according to the
	 * coordinates on the Player/Stage
	 */
	public void localize(GridMap map) {

		// pos2D.setSpeed(0, 0);
		// int currentIndexX = map.getCurrentIndexX();
		// int currentIndexY = map.getCurrentIndexY();

		double x1 = map.getGrid()[0][0].getX() - 0.125;
		double y1 = map.getGrid()[0][0].getY() + 0.125;

		int xIndex = (int) Math.floor((x - x1) / 0.25);
		int yIndex = (int) Math.floor((y1 - y) / 0.25);

		if ((xIndex >= 11 && xIndex <= map.getGrid()[0].length - 11)
				&& (yIndex >= 11 && yIndex <= map.getGrid().length - 11)) {

			if (map.getGrid()[currentIndexY][currentIndexX].getType() != 7) {
				map.getGrid()[currentIndexY][currentIndexX].setType(1);
				if (this.getAreaToCleanSelected()) {
					map.getGrid()[currentIndexY][currentIndexX].setSelected(false);
				}
			}

			Date date = new Date();

			map.getGrid()[currentIndexY][currentIndexX].setLastCleaned(date);

			map.getGrid()[currentIndexY][currentIndexX].setVisited(true);

			unvisitedCells.remove(map.getGrid()[currentIndexY][currentIndexX]);

			setCurrentX(xIndex);
			setCurrentY(yIndex);

			map.setCurrentIndexX(xIndex);
			map.setCurrentIndexY(yIndex);

			currentCell = this.getGridMap()[yIndex][xIndex];

			map.getGrid()[yIndex][xIndex].setType(2);

			return;

		}

		if (xIndex < 11 || xIndex > map.getGrid()[0].length - 11) {
			this.resizingMap = true;

			map.resizeMap(map.getCurrentMaxY(), map.getCurrentMaxX() + 50);
			map.setCurrentMaxX(map.getCurrentMaxX() + 50);

			System.out.println("Map has been resized: " + "Current max X: "
					+ map.getCurrentMaxX() + "Current max Y: "
					+ map.getCurrentMaxY());

			this.resizingMap = false;
		}

		if (yIndex < 11 || yIndex > map.getGrid().length - 11) {
			this.resizingMap = true;

			map.resizeMap(map.getCurrentMaxY() + 50, map.getCurrentMaxX());
			map.setCurrentMaxY(map.getCurrentMaxY() + 50);

			System.out.println("Map has been resized: " + "Current max X: "
					+ map.getCurrentMaxX() + "Current max Y: "
					+ map.getCurrentMaxY());

			this.resizingMap = false;
		}

		localize(map);
	}

	/**
	 * Localize2 locates the robot on the given occupancy grid map using the given map and the position2d interface 
	 * to determine its current location using coordinates.
	 * @param map - GridMap
	 * @return return map GridMap with the fields currentIndexX and currentIndexY adjusted to robots current location.
	 */
	public GridMap localize2(GridMap map) {

		// pos2D.setSpeed(0, 0);
		int currentIndexX = map.getCurrentIndexX();
		int currentIndexY = map.getCurrentIndexY();

		// while (!pos2D.isDataReady());

		double x1 = map.getGrid()[0][0].getX() - 0.125;
		double y1 = map.getGrid()[0][0].getY() + 0.125;

		int xIndex = (int) Math.floor((x - x1) / 0.25);
		int yIndex = (int) Math.floor((y1 - y) / 0.25);

		if ((xIndex >= 3 && xIndex <= map.getGrid()[0].length - 3)
				&& (yIndex >= 3 && yIndex <= map.getGrid().length - 3)) {
			map.getGrid()[currentIndexY][currentIndexX].setType(1);
			map.getGrid()[currentIndexY][currentIndexX].setVisited(true);
			map.setCurrentIndexX(xIndex);
			map.setCurrentIndexY(yIndex);
			map.getGrid()[yIndex][xIndex].setType(2);

			/*
			 * currentCell = this.getGridMap()[currentIndexY][currentIndexX];
			 * setCurrentX(this.getMap().getCurrentIndexX());
			 * setCurrentY(this.getMap().getCurrentIndexY());
			 */
			return map;
		}

		if (xIndex < 3 || xIndex > map.getGrid()[0].length - 3) {
			this.resizingMap = true;

			map.resizeMap(map.getCurrentMaxY(), map.getCurrentMaxX() + 50);
			map.setCurrentMaxX(map.getCurrentMaxX() + 50);

			System.out.println("Map has been resized: " + "Current max X: "
					+ map.getCurrentMaxX() + "Current max Y: "
					+ map.getCurrentMaxY());

			this.resizingMap = false;
		}

		if (yIndex < 3 || yIndex > map.getGrid().length - 3) {
			this.resizingMap = true;

			map.resizeMap(map.getCurrentMaxY() + 50, map.getCurrentMaxX());
			map.setCurrentMaxY(map.getCurrentMaxY() + 50);
			
			System.out.println("Map has been resized: " + "Current max X: "
					+ map.getCurrentMaxX() + "Current max Y: "
					+ map.getCurrentMaxY());

			this.resizingMap = false;
		}

		localize(map);
		return map;
	}

	
	private GridCell currentCell = new GridCell();
	/*
	 * Adjusts the robot appropriately so that the robot will be located at the
	 * center of the cell which has the closest center coordinates to the
	 * robot's coordinates and sets the Yaw of the robot to one of EAST, WEST,
	 * SOUTH and NORTH by evaluating which one will be the safest to turn
	 * afterwards
	 */
	public void adjust() {
		adjusting = true;
		double speedRate = 1.35;

		localize(map);

		setCurrentX(map.getCurrentIndexX());
		setCurrentY(map.getCurrentIndexY());

		// int currentIndexX = getCurrentX();
		// int currentIndexY = getCurrentY();

		// ///////////////////////////// ABSOLUTELY NEW APPROACH
		// \\\\\\\\\\\\\\\\\\\\\\\\\
		System.out.println("Adjusting");
		System.out.println("currentX: " + currentIndexX + ", currentY: "
				+ currentIndexY);

		// while (!pos2D.isDataReady());

		if (round2(x) == round2(currentCell.getX()) || round2(y) == round2(currentCell.getY())) {
			adjusting = false;
			System.out.println("Has just exited from adjust and the ADJUSTING is: "+adjusting);
			return;
		}

			System.out.println("The heading in the adjust method is: "
					+ heading);

			// /////////////////////// DIRECTION IS EAST

			if (heading == EAST) {

				double d1 = map.getGrid()[currentIndexY][currentIndexX + 1].getX() - x;
				double d2 = front + 0.2;
				double d3 = x - map.getGrid()[currentIndexY][currentIndexX - 1].getX();
				double d4 = back + 0.2;

				double d5 = map.getGrid()[currentIndexY][currentIndexX].getX() - x;
				double d6 = x - map.getGrid()[currentIndexY][currentIndexX].getX();

				
				if ((back + d5 > 0.04) && (front + d6 > 0.04)) {
					
					System.out.println("Y's are equal in EAST");
					
					double speed = map.getGrid()[currentIndexY][currentIndexX].getX() - x;

					while (round2(map.getGrid()[currentIndexY][currentIndexX].getX()) != round2(x)) {

						speed = map.getGrid()[currentIndexY][currentIndexX].getX() - x;
						
						pos2D.setSpeed(round(speed / speedRate), 0);
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}
						
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
				} else if (d2 - d1 > 0.24) {
					System.out.println("has entered front while loop in EAST");

					double speed = map.getGrid()[currentIndexY][currentIndexX + 1]
							.getX() - x;

					while (round(map.getGrid()[currentIndexY][currentIndexX + 1]
							.getX()) > round(x)) {
						//System.out.println("Something is not quite right here! Line 1279");
						speed = map.getGrid()[currentIndexY][currentIndexX + 1].getX() - x;
						
						pos2D.setSpeed(round(speed / speedRate), 0);
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}

						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					pos2D.setSpeed(0, 0);

				}

				else if (d4 - d3 > 0.24) {

					System.out.println("has entered back while loop in EAST");

					double speed = x - map.getGrid()[currentIndexY][currentIndexX - 1].getX();

					//System.out.println("Something is not quite right here! Line 1306");
					
					while (round(map.getGrid()[currentIndexY][currentIndexX - 1]
							.getX()) < round(x)) {

						speed = x - map.getGrid()[currentIndexY][currentIndexX - 1].getX();
						pos2D.setSpeed(-round(speed / speedRate), 0);
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}
						
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					pos2D.setSpeed(0, 0);

				}

				System.out.println("d4-d3 = " + (d4 - d3));

				localize(map);
				setCurrentX(map.getCurrentIndexX());
				setCurrentY(map.getCurrentIndexY());
				//markSurrounding();

				if (round3(y) != round3(currentCell.getY())) {
					if ((right > left && left >= 0.15) || (right < left && right < 0.15)) {
						turnTo(SOUTH);
						System.out.println("Line: 1331");
						adjust();
					} else {
						turnTo(NORTH);
						System.out.println("Line: 1335");
						adjust();
					}
				}

			}

			// //////////////////// DIRECTION IS WEST

			else if (heading == WEST) {

				double d1 = x
						- map.getGrid()[currentIndexY][currentIndexX - 1]
								.getX();
				double d2 = front + 0.2;
				double d3 = map.getGrid()[currentIndexY][currentIndexX + 1]
						.getX() - x;
				double d4 = back + 0.2;

				double d5 = x
						- map.getGrid()[currentIndexY][currentIndexX].getX();
				double d6 = map.getGrid()[currentIndexY][currentIndexX].getX()
						- x;

				if ((back + d5 > 0.04) && (front + d6 > 0.04)) {

					System.out.println("Y's are equal in WEST");

					double speed = x - map.getGrid()[currentIndexY][currentIndexX].getX();

					while (round2(map.getGrid()[currentIndexY][currentIndexX].getX()) != round2(x)) {

						speed = x - map.getGrid()[currentIndexY][currentIndexX].getX();
						pos2D.setSpeed(round(speed / speedRate), 0);
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}
						
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
				} else if (d2 - d1 > 0.24) {
					System.out.println("has entered front while loop in WEST");

					double speed = x
							- map.getGrid()[currentIndexY][currentIndexX - 1]
									.getX();

					//System.out.println("Something is not quite right here! Line 1399");
					while (round(map.getGrid()[currentIndexY][currentIndexX - 1].getX()) < round(x)) {

						speed = x - map.getGrid()[currentIndexY][currentIndexX - 1].getX();
						pos2D.setSpeed(round(speed / speedRate), 0);
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}
						
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

					pos2D.setSpeed(0, 0);

				}

				else if (d4 - d3 > 0.24) {

					System.out.println("has entered back while loop in WEST");

					double speed = map.getGrid()[currentIndexY][currentIndexX + 1]
							.getX() - x;

					//System.out.println(" is not quite right here! Line 1430");
					while (round(map.getGrid()[currentIndexY][currentIndexX + 1].getX()) > round(x)) {

						speed = map.getGrid()[currentIndexY][currentIndexX + 1].getX() - x;
						pos2D.setSpeed(-round(speed / speedRate), 0);
						
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}
						
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

					pos2D.setSpeed(0, 0);

				}

				localize(map);
				setCurrentX(map.getCurrentIndexX());
				setCurrentY(map.getCurrentIndexY());
				//markSurrounding();

				if (round3(y) != round3(currentCell.getY())) {
					if ((right < left && right >= 0.15)
							|| (right > left && left < 0.15)) {
						turnTo(SOUTH);
						System.out.println("Line: 1453");
						adjust();
					} else {
						turnTo(NORTH);
						System.out.println("Line: 1457");
						adjust();
					}
				}

			}

			// /////////////// DIRECTION IS SOUTH

			else if (heading == SOUTH) {

				double d1 = y
						- map.getGrid()[currentIndexY + 1][currentIndexX]
								.getY();
				double d2 = front + 0.2;
				double d3 = map.getGrid()[currentIndexY - 1][currentIndexX]
						.getY() - y;
				double d4 = back + 0.2;

				double d5 = y
						- map.getGrid()[currentIndexY][currentIndexX].getY();
				double d6 = map.getGrid()[currentIndexY][currentIndexX].getY() - y;

				if ((back + d5 > 0.04) && (front + d6 > 0.04)) {

					System.out.println("X's are equal in SOUTH");

					double speed = y
							- map.getGrid()[currentIndexY][currentIndexX]
									.getY();

					while (round2(map.getGrid()[currentIndexY][currentIndexX]
							.getY()) != round2(y)) {

						speed = y - map.getGrid()[currentIndexY][currentIndexX].getY();
						pos2D.setSpeed(round(speed / speedRate), 0);
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}
						
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
				} else if (d2 - d1 > 0.24) {
					System.out.println("has entered front while loop in SOUTH");

					double speed = y - map.getGrid()[currentIndexY + 1][currentIndexX].getY();

					//System.out.println("Something is not quite right here! Line 1524");
					while (round(map.getGrid()[currentIndexY + 1][currentIndexX].getY()) < round(y)) {

						speed = y - map.getGrid()[currentIndexY + 1][currentIndexX].getY();
						pos2D.setSpeed(round(speed / speedRate), 0);
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}
						
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

					pos2D.setSpeed(0, 0);

				}

				else if (d4 - d3 > 0.24) {
					System.out.println("has entered back while loop in SOUTH");

					double speed = map.getGrid()[currentIndexY - 1][currentIndexX]
							.getY() - y;

					//System.out.println("Something is not quite right here! Line 1554");
					while (round(map.getGrid()[currentIndexY - 1][currentIndexX].getY()) > round(y)) {

						speed = map.getGrid()[currentIndexY - 1][currentIndexX].getY() - y;
						pos2D.setSpeed(-round(speed / speedRate), 0);
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}
						
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					pos2D.setSpeed(0, 0);

				}

				localize(map);
				setCurrentX(map.getCurrentIndexX());
				setCurrentY(map.getCurrentIndexY());
				//markSurrounding();

				if (round3(x) != round3(currentCell.getX())) {
					if ((right < left && right >= 0.15)
							|| (right > left && left < 0.15)) {
						turnTo(EAST);
						System.out.println("Line: 1572");
						adjust();
					} else {
						turnTo(WEST);
						System.out.println("Line: 1576");
						adjust();
					}
				}

			}

			// ///////////////////////// DIRECTION IS NORTH

			else if (heading == NORTH) {

				double d1 = map.getGrid()[currentIndexY - 1][currentIndexX]
						.getY() - y;
				double d2 = front + 0.2;
				double d3 = y
						- map.getGrid()[currentIndexY + 1][currentIndexX]
								.getY();
				double d4 = back + 0.2;

				double d5 = map.getGrid()[currentIndexY][currentIndexX].getY()
						- y;
				double d6 = y
						- map.getGrid()[currentIndexY][currentIndexX].getY();

				if ((back + d5 > 0.04) && (front + d6 > 0.04)) {

					System.out.println("X's are equal in NORTH");

					double speed = map.getGrid()[currentIndexY][currentIndexX]
							.getY() - y;

					while (round2(map.getGrid()[currentIndexY][currentIndexX].getY()) != round2(y)) {

						speed = map.getGrid()[currentIndexY][currentIndexX].getY() - y;
						
						System.out.println("Adjust NORTH Speed: " + round(speed/speedRate));
						pos2D.setSpeed(round(speed / speedRate), 0);
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}
						
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
				} 
				else if (d2 - d1 > 0.24) {
					System.out.println("has entered front while loop in NORTH");

					double speed = map.getGrid()[currentIndexY - 1][currentIndexX]
							.getY() - y;

					//System.out.println("Something is not quite right here! Line 1645");
					while (round(map.getGrid()[currentIndexY - 1][currentIndexX].getY()) > round(y)) {

						speed = map.getGrid()[currentIndexY - 1][currentIndexX].getY() - y;
						
						System.out.println("Adjust NORTH 1 Speed: " + round(speed/speedRate));
						pos2D.setSpeed(round(speed / speedRate), 0);
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}
						
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

					pos2D.setSpeed(0, 0);

				}

				else if (d4 - d3 > 0.24) {
					System.out.println("has entered back while loop in NORTH");

					double speed = y - map.getGrid()[currentIndexY + 1][currentIndexX].getY();

					//System.out.println("Something is not quite right here! Line 1675");
					while (round(map.getGrid()[currentIndexY + 1][currentIndexX].getY()) < round(y)) {

						speed = y - map.getGrid()[currentIndexY + 1][currentIndexX].getY();
						
						System.out.println("Adjust NORTH Speed: -" + round(speed/speedRate));
						pos2D.setSpeed(-round(speed / speedRate), 0);
						
						if(recovering){
							pos2D.setSpeed(0, 0);
							return;
						}
						
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

					pos2D.setSpeed(0, 0);

				}

				localize(map);
				setCurrentX(map.getCurrentIndexX());
				setCurrentY(map.getCurrentIndexY());
				//markSurrounding();

				if (round3(x) != round3(currentCell.getX())) {
					if ((right > left && left < 0.15)
							|| (right < left && right >= 0.15)) {
						turnTo(WEST);
						System.out.println("Line: 1692");
						adjust();
					} else {
						turnTo(EAST);
						System.out.println("Line: 1696");
						adjust();
					}
				}

			}
			adjusting = false;
			System.out.println("Has just exited from last line of adjust and the ADJUSTING is: "+adjusting);
	}
	
	/**
	 * Data pool thread which updates robots X, Y, YAW and STALL fields, as well as the sonar readouts. 
	 */
	protected void dataPool() {
		Thread refreshPool = new Thread() {
			public void run() {
				double[] sonarValues;
				while (true) {
					if (pos2D.isDataReady()) {
						x = pos2D.getX();
						y = pos2D.getY();
						yaw = pos2D.getYaw();
						stall = pos2D.getData().getStall();
					}

					if (sonar.isDataReady()) {
						sonarValues = sonar.getData().getRanges();

						front = round(min(sonarValues[12] - 0.2,
								sonarValues[3] - 0.4, sonarValues[4] - 0.4,
								sonarValues[10] * Math.cos(Math.toRadians(5))
										- 0.1,
								sonarValues[11] * Math.cos(Math.toRadians(5))
										- 0.1));

						left = round(min(sonarValues[13] - 0.2, sonarValues[5] - 0.1,
								sonarValues[6] - 0.1, 1000, 1000));

						back = round(min(sonarValues[0], sonarValues[1],
								sonarValues[2], 1000, 1000));

						right = round(min(sonarValues[14] - 0.2,
								sonarValues[7] - 0.1, sonarValues[8] - 0.1,
								1000, 1000));
						
						frontForCrash = round(min(sonarValues[12] - 0.2,
								1000, 1000,
								sonarValues[10] * Math.cos(Math.toRadians(5))
										- 0.1,
								sonarValues[11] * Math.cos(Math.toRadians(5))
										- 0.1));
					}

					try {
						sleep(5);
					}

					catch (InterruptedException e) {
					}
				}
			}
		};
		refreshPool.start();
	}

	/**
	 * Method to cause the robot to turn towards given direction.
	 * 
	 * @param direction
	 *            the given direction.
	 */
	public void turnTo(int direction) {
		turning = true;

		if (direction == heading) {
			turning = false;
			return;
		}

		double turnDirection = 1;
		if (((heading == NORTH) && (direction == EAST))
				|| ((heading == EAST) && (direction == SOUTH))
				|| ((heading == WEST) && (direction == NORTH))
				|| ((heading == SOUTH) && (direction == WEST))) {
			turnDirection = -1;
		} else
			turnDirection = 1;

		long targetYaw;
		if (direction == NORTH) {
			targetYaw = roundedNumber(Math.PI / 2);
		} else if (direction == WEST) {
			targetYaw = roundedNumber(Math.PI);
		} else if (direction == SOUTH) {
			targetYaw = roundedNumber(-Math.PI / 2);
		} else if (direction == EAST) {
			targetYaw = 0;
		} else
			throw (new IllegalArgumentException(
					"MazeRunner.turnTo: direction argument is invalid."));

		double difference = Math.abs(roundedNumber(yaw) - targetYaw);
		if (heading != SOUTH) {
			while (difference > 0) {

				difference = Math.abs(roundedNumber(yaw) - targetYaw);
				pos2D.setSpeed(0, ((turnDirection * difference) / turnRate) / ACCURACY);
				
				if(recovering){
					turning = false;
					return;
				}
				
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} else {
			while (difference > 0) {

				if (direction == NORTH) {

					difference = Math.abs(roundedNumber(yaw) - targetYaw);
					pos2D.setSpeed(0, ((turnDirection * difference) / turnRate)
							/ ACCURACY);

				} else {

					difference = Math.abs(roundedNumber(yaw) + targetYaw);
					pos2D.setSpeed(0, ((turnDirection * difference) / turnRate)
							/ ACCURACY);

				}

				if(recovering){
					turning = false;
					return;
				}
				
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

		turning = false;
		pos2D.setSpeed(0, 0);
		heading = direction;
	}

	/**
	 * Method to determine robots heading direction using robots current yaw setting. 
	 * @param yaw - double robots yaw
	 * @return int direction
	 */
	public int determineYaw(double yaw) {
		int direction = 0;
		
		if (Math.toDegrees(yaw) > -45 && Math.toDegrees(yaw) <= 45) {
			direction = EAST;
		} else if (Math.toDegrees(yaw) > 45 && Math.toDegrees(yaw) <= 135) {
			direction = NORTH;
		} else if (Math.toDegrees(yaw) > 135 || Math.toDegrees(yaw) <= -135) {
			direction = WEST;
		} else if (Math.toDegrees(yaw) > -135 && Math.toDegrees(yaw) <= -45) {
			direction = SOUTH;
		}

		return direction;
	}

	/**
	 * Method used by the interface to select cells that are within the boundaries of the drag and select selection.
	 * It adds the selected points to the selectedCells linked list and changes the boolean value SELECTED of each selected cell to true.
	 * @param x1 - the x coordinate of the top drag and select point
	 * @param y1 - the y coordinate of the top drag and select point
	 * @param x2 - the x coordinate of the second drag and select point
	 * @param y2 - the y coordinate of the second drag and select point
	 */
	public void setSelection(int x1, int y1, int x2, int y2) {
		for (int i = y1; i <= y2; i++) {

			for (int j = x1; j <= x2; j++) {

				if (i >= 0 && i < getMap().getGrid().length && j >= 0
						&& j < getMap().getGrid()[0].length) {

					if (getMap().getGrid()[i][j].getType() == 1
							|| getMap().getGrid()[i][j].getType() == 0) {

						selectedCells.add(getMap().getGrid()[i][j]);
						System.out.println("Selection added to selectedCells!!");
						
						getMap().getGrid()[i][j].setSelected(true);

					}
				}
			}
		}
	}

	/**
	 * Method used to command the robot to clean selected area. 
	 * This method iterates through a path that is constructed out of a list of currently selected cells.
	 */
	public void cleanSelectedArea() {

		/**
		 * Fail safe method for finding the path if the goal is not well defined. 
		 * Iterate through the selectedCells starting from the right-most element 
		 * and as soon as you find a goal cell and a path that is valid (doesn't return null) perform the rest of the body of the method.
		 */
		for(int i=selectedCells.size()-1; i>0; i--){
			toVisitInOrder = pathFinder.search(getMap().getGrid()[currentIndexY][currentIndexX], selectedCells.get(i));
			System.out.println("");
			if(toVisitInOrder != null){
				break;
			}
		}
		/*
		toVisitInOrder = pathFinder.search(
				getMap().getGrid()[currentIndexY][currentIndexX],
				selectedCells.get(selectedCells.size() - 1));
		*/
		//System.out.println("Size of TOVISITINORDER: " + toVisitInOrder.size());
		
		// Follow a path to the selection
		this.followPath(toVisitInOrder);
		System.out.println("Clean selected area - selected Cells: ");

		// Follow a path to cover the selected cells
		while (!selectedCells.isEmpty()) {
			GridCell cell = selectedCells.removeFirst();
			System.out.println("Next to visit: " + cell.toString());
			toVisitInOrder = pathFinder.search(
					getMap().getGrid()[currentIndexY][currentIndexX], cell);
			this.followPath(toVisitInOrder);
		}
		
		this.setAreaToCleanSelected(false);
		this.setReturnToBase(true);
	}

	/**
	 * Method updating the currently selected cells using a given new selection.
	 * @param selection list of selected cells.
	 */
	public void updateSelectedCells(LinkedList<GridCell> selection) {

		int x = 0;
		int y = 0;
		this.selectedCells = selection;
		
		int j = 0;
		for (int i = j; i < selectedCells.size(); i++) {
			x = selectedCells.get(i).getxIndex();
			y = selectedCells.get(i).getyIndex();
			j++;
			System.out.println("cell x " + x + " y " + y);
			getMap().getGrid()[y][x].setSelected(true);
			
		}
		System.out.println("looped till " + j);
		setAreaToCleanSelected(true);
	}

	/**
	 * Command the robot to return to the base. Perform search on how to get
	 * from the current cell to the stored "charging station" cell and visit
	 * them in order.
	 */
	public void returnToBase() {
		// toVisitInOrder = new LinkedList<GridCell>();
		System.out.println("For CHARGING X :" + chargingStation.getxIndex()
				+ " Y:" + chargingStation.getyIndex());
		toVisitInOrder = pathFinder.search(
				map.getGrid()[currentIndexY][currentIndexX], chargingStation);

		System.out.println("currentIndexY" + currentIndexY + " currentIndexX "
				+ currentIndexX + " charging station type: "
				+ chargingStation.getType());
		System.out.print("Path found: ");

		this.followPath(toVisitInOrder);

		System.out.println("Line: 2037");
		adjust();

		pos2D.setSpeed(0, 0);

		for (int i = 0; i < map.getGrid().length; i++) {
			for (int j = 0; j < map.getGrid()[0].length; j++) {
				if (map.getGrid()[i][j].getType() == 1) {
					map.getGrid()[i][j].setType(0);
					map.getGrid()[i][j].setVisited(false);
				}
			}
		}
		SaveLoad saveLoad = new SaveLoad(this);
		saveLoad.serializeMap();
	}

	/**
	 * Method used to iterate the robot through an ordered list of cells 
	 * @param toVisitInOrder - list of cells to be visited in order
	 */
	public void followPath(LinkedList<GridCell> toVisitInOrder) {
		followingPath = true;

		if (!(toVisitInOrder.size() > 0) && !(this.getReturnToBase())) {
			this.setReturnToBase(true);
		} else if (!(toVisitInOrder.size() > 0) && this.getReturnToBase()) {
			this.setReturnToBase(false);
			this.setStop(true);
			followingPath = false;
			pathNotFound = true;
		} else {
			System.out.println(toVisitInOrder.size());
			for (GridCell c : toVisitInOrder) {

				System.out.println("Cells to visit: ");
				for (int i = 0; i < toVisitInOrder.size(); i++) {
					System.out.print("X[" + i + "]: "
							+ toVisitInOrder.get(i).getxIndex() + " Y[" + i
							+ "]: " + toVisitInOrder.get(i).getyIndex() + ";  ");
				}
				System.out.println();
				// markSurrounding();cl
				step++;

				// If there is obstacle on path, terminate and plan the path
				// again. If the obstacle is in unvisited cells, it also removes
				// the obstacle cell from the unvisitedCells list.
				if (c.getType() == -1 || recovering) {
					if (unvisitedCells.contains(c)) {
						unvisitedCells.remove(c);
					}
					followingPath = false;
					return;
				}
				moveTo(c);
				selectedCells.remove(c);
				System.out.printf(
						"front: %6.2f, left: %6.2f, right: %6.2f, back: %6.2f\n",
						front, left, right, back);
				printMap();
			}

			followingPath = false;
		}
		
		
	}

	/**
	 * Method providing functionality for reseting the map selection. This
	 * method sets the individual GridCells to unselected.
	 */
	public void resetMapSelection() {
		for (int i = 0; i < getMap().getGrid().length; i++) {
			for (int j = 0; j < getMap().getGrid()[0].length; j++) {
				if (getMap().getGrid()[i][j].isSelected()) {
					getMap().getGrid()[i][j].setSelected(false);
					selectedCells.clear();
				}
			}
		}

	}

	/**
	 * Method used to add a GridCell object to a list of unvisited cells.
	 * @param cell GridCell object
	 */
	public void addToUnvisitedCells(GridCell cell) {
		if (!(unvisitedCells.contains(cell))) {
			unvisitedCells.add(cell);
		}
	}

	/**
	 * Method used to calculate a distance between the coordinates of the first point and the second.
	 * @param x1 - x coordinate of the first point
	 * @param y1 - y coordinate of the first point 
	 * @param x2 - x coordinate of the second point
	 * @param y2 - y coordinate of the second point
	 * @return distance between the 
	 */
	public double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	/**
	 * Method to round a double value to precision of 5 decimal points.
	 * @param d - double value
	 * @return rounded doube value.
	 */
	public double round(double d) {
		d = d * 100000;

		double r = Math.round(d);

		return r / 100000;

	}

	/**
	 * Method which returns a double rounded to three decimal points
	 * @param d - double value
	 * @return double value rounded to three decimal points
	 */
	public double round2(double d) {
		d = d * 1000;

		double r = Math.round(d);

		return r / 1000;

	}

	/**
	 * Method returning a double, rounded to two decimal points.
	 * @param d - double value
	 * @return double value rounded to two decimal points.
	 */
	public double round3(double d) {
		d = d * 100;

		double r = Math.round(d);

		return r / 100;

	}
	
	/**
	 * Method used to round the robots yaw using a constant precision.
	 * @param yaw - double robots yaw
	 * @return robots rounded yaw. 
	 */
	private long roundedNumber(double yaw) {
		return (Math.round(yaw * ACCURACY));
	}

	public RangerInterface getSonar() {
		return sonar;
	}

	public int getCurrentIndexX() {
		return this.currentIndexX;
	}

	public int getCurrentIndexY() {
		return this.currentIndexY;
	}

	public void setCurrentX(int currentX) {
		this.currentIndexX = currentX;
	}

	public void setCurrentY(int currentY) {
		this.currentIndexY = currentY;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 * Stop the robots Occupying state
	 * @param stop boolean
	 */
	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public boolean getStop() {
		return this.stop;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	/**
	 * Set the map in which the robot currently operates and set its charging station.
	 * @param map GridMap
	 */
	public void setMap(GridMap map) {

		// currentIndexX = (map.getGrid()[0].length / 2);
		// currentIndexY = (map.getGrid().length / 2);

		map.getGrid()[currentIndexY][currentIndexX].setType(7);

		setChargingStation(map.getGrid()[currentIndexY][currentIndexX]);

		this.map = map;

	}
	
	/**
	 * Method returning an internal array of the GridMap
	 * @return two dimensional array of GridCell
	 */
	public GridCell[][] getGridMap() {
		return map.getGrid();
	}

	/**
	 * Method returning a map in which the robot currently operates.
	 * @return map GridMap
	 */
	public GridMap getMap() {
		return map;
	}

	/**
	 * Method returning the position2D interface.
	 * @return pos2D
	 */
	public Position2DInterface getPos2D() {
		return pos2D;
	}

	/**
	 * Getter for the robots adjusting state.
	 * @return boolean adjusting.
	 */
	public boolean isAdjusting() {
		return adjusting;
	}

	/**
	 * Setter for robots Adjusting state.
	 * @param boolean adjusting
	 */
	public void setAdjusting(boolean adjusting) {
		this.adjusting = adjusting;
	}

	/**
	 * Getter for robots turning state.
	 * @return boolean turning.
	 */
	public boolean isTurning() {
		return turning;
	}

	/**
	 * Setter for robots turning state. 
	 * @param boolean turning 
	 */
	public void setTurning(boolean turning) {
		this.turning = turning;
	}

	/**
	 * Getter for the Stalled state of the robot.
	 * @return boolean stalled.
	 */
	public boolean isStalled() {
		return stalled;
	}

	/**
	 * Method which sets the state of the robot to stalled.
	 * @param stalled - boolean
	 */
	public void setStalled(boolean stalled) {
		this.stalled = stalled;
	}

	/**
	 * Getter for the AreaToCleanSelected state.
	 * @return boolean areaToCleanSelected.
	 */
	public boolean getAreaToCleanSelected() {
		return areaToCleanSelected;
	}

	/**
	 * Method which returns a list of currently selected cells.
	 * @return GridCell selectedCells - linked list of selected cells.
	 */
	public LinkedList<GridCell> getSelectedCells() {
		return selectedCells;
	}

	/**
	 * Getter for the Returning to base state.
	 * @return boolean returnToBase.
	 */
	public boolean getReturnToBase() {
		return returnToBase;
	}

	/**
	 * Getter for robots EvaluatingEnvironment state.
	 * @return boolean - isEvaluatingEnvironment
	 */
	public boolean isEvaluatingEnvironment() {
		return evaluatingEnvironment;
	}

	/**
	 * Setter for the robots EvaluatingEnvironment state.
	 * @param evaluatingEnvironment - boolean
	 */
	public void setEvaluatingEnvironment(boolean evaluatingEnvironment) {
		this.evaluatingEnvironment = evaluatingEnvironment;
	}

	/**
	 * 
	 * @return - GridCell robots charging station.
	 */
	public GridCell getChargingStation() {
		return chargingStation;
	}

	public void setChargingStation(GridCell chargingStation) {
		this.chargingStation = chargingStation;
	}

	public boolean isResizingMap() {
		return resizingMap;
	}

	public void setResizingMap(boolean resizingMap) {
		this.resizingMap = resizingMap;
	}

	public ArrayList<GridCell> getUnvisitedCells() {
		return unvisitedCells;
	}

	public void setUnvisitedCells(ArrayList<GridCell> unvisitedCells) {
		this.unvisitedCells = unvisitedCells;
	}

	public boolean isRecovering() {
		return recovering;
	}

	public void setRecovering(boolean recovering) {
		this.recovering = recovering;
	}

	public boolean isPathNotFound() {
		return pathNotFound;
	}

	public void setPathNotFound(boolean pathNotFound) {
		this.pathNotFound = pathNotFound;
	}
}