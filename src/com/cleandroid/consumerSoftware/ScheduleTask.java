package com.cleandroid.consumerSoftware;

import java.util.LinkedList;
import java.util.TimerTask;

import com.cleandroid.Slam2;
import com.cleandroid.occupancyGridMap.GridCell;
import com.cleandroid.occupancyGridMap.Point;
/**
 * run the scheduled tasks
 * @author Song
 *
 */
public class ScheduleTask extends TimerTask {
	Slam2 main;

	private Point one;
	private Point two;
	
	/**
	 * set the schedule task according to the selection
	 * @param main : slam2 object
	 * @param selection : the list of selected cells
	 */
	public ScheduleTask(Slam2 main, Point one, Point two) {
		this.main = main;
		this.one = one;
		this.two = two;
		
	}
	/**
	 * run the scheduled task
	 */
	public void run() {
		// TODO Auto-generated method stub

		System.out.println("the thread is alive :" + main.isAlive());
		if (!main.isAlive()) {

			main.start();
			main.setStarted(true);
		}

		while (!main.getStop() || main.getAreaToCleanSelected()
				|| main.isEvaluatingEnvironment() || main.getReturnToBase()) {

			// TODO PRINT OUT STATUS ON WAITING FOR SCHEDULE
		}
		if (one == null) {
			System.out.println("selection is empty");
			main.setStop(false);

		} else {
			main.setAreaToCleanSelected(true);
			// TODO pop up to confirm overriding the users selection
			// to make sure the robot only cleans the areas specified by the
			// users schedule

			
			main.setSelection((int)one.getX(), (int)one.getY(), (int)two.getX(),(int) two.getY());
			main.setAreaToCleanSelected(true);

		}

		/*
		 * else{ main.setStop(true); main.cleanSelectedArea(selectedCells);
		 * main.setAreaToCleanSelected(true); }
		 */

	}

}
