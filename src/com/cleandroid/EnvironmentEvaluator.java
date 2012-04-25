package com.cleandroid;

import java.awt.Dimension;

import com.cleandroid.occupancyGridMap.GridMap;
import com.cleandroid.occupancyGridMap.Point;

/**
 * Thread to be executed at the beginning of the cleaning cycle to determine if
 * the robot operates in a new or a previously vacuumed environment. This class
 * provides concurrent functionality for setting a previously cleaned map if the
 * robot operates within it.
 * 
 * @author Team BLUE
 * 
 */
public class EnvironmentEvaluator extends Thread {

	/**
	 * Reference to the currently operating Slam2 object
	 */
	public Slam2 slam2;
	/**
	 * Reference to the map that is currently vacuumed
	 */
	public GridMap newMap;
	/**
	 * Reference to the de-serialized GridMap object
	 */
	public GridMap serializedMap;
	/**
	 * Counter of cells that differ - it will be decremented until it reaches
	 * zero, which means the robot operates in a new environment
	 */
	public int differentCellsCounter = 20;
	/**
	 * Counter of cells that are the same - when it reaches a certain number,
	 * this will indicate the robot operates in a previously cleaned environment
	 */
	public int sameCellsCounter = 0;
	/**
	 * Flag for intentionally stoping the thread if required
	 */
	private boolean stopFlag;

	/**
	 * EnvironmentEvaluator constructor that takes the previous and the new
	 * vacuuming environment
	 * 
	 * @param slam2
	 *            Instance of the Slam2 class to be evaluated against the
	 *            de-serialized map
	 * @param serializedMap
	 *            Previous vacuumed de-serialized map that we will compare with
	 *            the current Slam2 map
	 */
	public EnvironmentEvaluator(Slam2 slam2, GridMap serializedMap) {
		this.newMap = slam2.getMap();
		this.serializedMap = serializedMap;
		this.slam2 = slam2;
		
		createMinMaxUpdater();
	}

	/**
	 * Thread that is run once when the program is started. The purpose of this thread is to keep on calculating the focus for the GUI map whilst the GUI is not yet loaded. 
	 * The data is then stored within fields in Slam2 and is then accessed by the GUI when it is finally loaded.
	 */
	private void createMinMaxUpdater() {
		Thread execute = new Thread(new Runnable() {
			
			public void run() {
				while (slam2.isEvaluatingEnvironment()) {

				int xCoordinate = slam2.getCurrentIndexX();
				int yCoordiante = slam2.getCurrentIndexY();
				if (slam2.getMap().getMaxPoint() == null) {
					slam2.getMap().setMaxPoint(new Point(slam2.getCurrentIndexX() + 1, slam2.getCurrentIndexY() + 1));
				}

				if (slam2.getMap().getMinPoint() == null) {
					slam2.getMap().setMinPoint(new Point(slam2.getCurrentIndexX(), slam2.getCurrentIndexY()));
				}
				
			

					if (yCoordiante - 1 < slam2.getMap().getMinPoint().getY()) {
						slam2.getMap().getMinPoint().setY(yCoordiante - 1);

					}
					if (xCoordinate - 1 < slam2.getMap().getMinPoint().getX()) {
						slam2.getMap().getMinPoint().setX(xCoordinate - 1);
					}

					if (yCoordiante + 1 > slam2.getMap().getMaxPoint().getY()) {

						slam2.getMap().getMaxPoint().setY(yCoordiante + 1);

						// System.out.println("doing it");

					}
					if (xCoordinate + 1 > slam2.getMap().getMaxPoint().getX()) {

						slam2.getMap().getMaxPoint().setX(xCoordinate + 1);
					}
					
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				
				
			}
		});
		execute.start();
		
	}

	/**
	 * EnvironmentEvlauator thread run method issued to concurrently asses
	 * whether the robot operates in an old or a new environment
	 */
	public void run() {

		// If different cells counter is bigger than 0 and flag not issued and
		// less than 5 same cells, continue
		while (differentCellsCounter > 1 && (!stopFlag) && sameCellsCounter < 5) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/**
			 * Fix the currentIndexX and currentIndexY of the old environment
			 * de-serialized map so that we can compare cells at the same
			 * indexes according to the coordinate system
			 */
			serializedMap = slam2.localize2(serializedMap);

			System.out
					.println("Serialized map details: currentIndexY: "
							+ serializedMap.getCurrentIndexY()
							+ " currentIndexX: "
							+ serializedMap.getCurrentIndexX()
							+ ", cord X: "
							+ serializedMap.getGrid()[serializedMap
									.getCurrentIndexY()][serializedMap
									.getCurrentIndexX()].getX()
							+ ", cord Y: "
							+ serializedMap.getGrid()[serializedMap
									.getCurrentIndexY()][serializedMap
									.getCurrentIndexX()].getY());

			System.out.println("New map details: currentIndexY: "
					+ slam2.getCurrentIndexY()
					+ " currentIndexX: "
					+ slam2.getCurrentIndexX()
					+ ", cord X: "
					+ newMap.getGrid()[slam2.getCurrentIndexY()][slam2
							.getCurrentIndexX()].getX()
					+ ", cord Y: "
					+ newMap.getGrid()[slam2.getCurrentIndexY()][slam2
							.getCurrentIndexX()].getY());

			// if cells at the slam2 current indexes and serialized maps current
			// indexes differ, then decrement the different cells counter
			if ((slam2.round3(slam2.getMap().getGrid()[slam2.getCurrentIndexY()][slam2.getCurrentIndexX()].getX()) != 
				slam2.round3(serializedMap.getGrid()[slam2.getCurrentIndexY()][slam2.getCurrentIndexX()].getX()))
				|| 
				(slam2.round3(slam2.getMap().getGrid()[slam2.getCurrentIndexY()][slam2.getCurrentIndexX()].getY()) != 
				slam2.round3(serializedMap.getGrid()[slam2.getCurrentIndexY()][slam2.getCurrentIndexX()].getY()))) {
				
				differentCellsCounter--;
				System.out
						.println("EnvironmentEvaluator: Cells differ: 1. "
								+ slam2.getMap().getGrid()[slam2
										.getCurrentIndexY()][slam2
										.getCurrentIndexX()].toString()
								+ " 2. "
								+ serializedMap.getGrid()[serializedMap
										.getCurrentIndexY()][serializedMap
										.getCurrentIndexX()].toString());
				// else if the cells are the same, then increment the same cells
				// counter
			} else if (
				(slam2.round3(slam2.getMap().getGrid()[slam2.getCurrentIndexY()][slam2.getCurrentIndexX()].getX()) == 
				slam2.round3(serializedMap.getGrid()[slam2.getCurrentIndexY()][slam2.getCurrentIndexX()].getX()))
				&& 
				(slam2.round3(slam2.getMap().getGrid()[slam2.getCurrentIndexY()][slam2.getCurrentIndexX()].getY()) == 
				slam2.round3(serializedMap.getGrid()[slam2.getCurrentIndexY()][slam2.getCurrentIndexX()].getY()))) {

				sameCellsCounter++;
				
				System.out
						.println("EnvironmentEvaluator: Same cells: 1. "
								+ slam2.getMap().getGrid()[slam2.getCurrentIndexY()][slam2.getCurrentIndexX()].toString()
								+ " 2. "
								+ serializedMap.getGrid()[slam2.getCurrentIndexY()][slam2.getCurrentIndexX()].toString());
				// if the comparison operation doesn't result in different/same
				// cells then there is something clearly wrong and print out to
				// console
			
			} else {
				System.out.println("What did just happen?");
				System.out
						.println("New map: "
								+ slam2.getMap().getGrid()[slam2
										.getCurrentIndexY()][slam2
										.getCurrentIndexX()].toString());
				System.out.println("Serialized map: "
						+ serializedMap.getGrid()[slam2
								.getCurrentIndexY()][slam2
								.getCurrentIndexX()].toString());

			}
		}

		// if the different cells counter is down to one, then this is a new
		// environment and just continue execution
		if (differentCellsCounter == 1) {

			System.out
					.println("EnvironmentEvaluator: A new map has been kept!");

			
			// if we reach 5 cells that are the same, then it is an old
			// environment and set the old map to be used for this vacuuming
			// session, as well as set its current indexes within that map
		} else if (sameCellsCounter == 10) {
			slam2.setStop(true);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			 * Perform any extra operations before setting the old map to the current environment.
			 */
			slam2.resetMapSelection();
			
			double x1 = serializedMap.getGrid()[0][0].getX() - 0.125;
			double y1 = serializedMap.getGrid()[0][0].getY() + 0.125;

			int xIndex = (int) Math.floor((slam2.getChargingStation().getX() - x1) / 0.25);
			int yIndex = (int) Math.floor((y1 - slam2.getChargingStation().getY()) / 0.25);
			
			
			slam2.setMap(serializedMap);
			
			slam2.localize(slam2.getMap());
			
			slam2.setChargingStation(slam2.getMap().getGrid()[yIndex][xIndex]);
			
			System.out
					.println("EnvironmentEvaluator: A serialized map will be used!");
			
			System.out.println("currentX of the robot: " + slam2.getMap().getCurrentIndexX() + "currentY of the robot: " + slam2.getMap().getCurrentIndexY());
			System.out.println("currentX of the robot: " + slam2.getCurrentIndexX() + "currentY of the robot: " + slam2.getCurrentIndexY());
			
			// stop the robot after swaping the maps and wait for users action
			slam2.pos2D.setSpeed(0,0);
			
			
			slam2.printMap();

		} else if (stopFlag) {
			System.out
					.println("EnvironmentEvlauator: A stop flag has been issued!");
			slam2.setStop(true);
		}
		
		slam2.setReturnToBase(true);
		// after one of the counters reaches its end and an operation is
		// performed, terminate the thread
		// and inform Slam2 that the robot no longer evaluates its environment
		slam2.setEvaluatingEnvironment(false);
		slam2.setAreaToCleanSelected(false);
	}

	/**
	 * To stop the thread without breaking it execution, set its flag to true
	 * 
	 * @param flag
	 *            boolean for stopping the thread
	 */
	public void setStopFlag(boolean flag) {
		this.stopFlag = flag;
	}
}
