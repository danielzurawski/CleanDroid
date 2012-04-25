package com.cleandroid;

/**
 * DistanceThread class creates a thread that check for the state of the robot, if it is adjusting 
 * @author Team BLUE
 */
public class DistanceThread extends Thread{
private Slam2 slam;
	
	/**
	 * Constructor of the DistanceTread
	 * @param slam - Slam2 currently running SLAM
	 */
	public DistanceThread(Slam2 slam){
		this.slam = slam;
	}
	
	/**
	 * Thread that invokes the localize method if the map is not resizing or robot not stopped.
	 * Localize adjusts the currentIndexX and currentIndexY of the current Slam according to the pos2D coordinates
	 */
	public void run(){
		while (true) {
			if (!slam.getStop() || slam.getAreaToCleanSelected() || slam.getReturnToBase()) {
				if(!slam.isAdjusting() && !slam.isResizingMap()){
					
					slam.localize(slam.getMap());
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
}
