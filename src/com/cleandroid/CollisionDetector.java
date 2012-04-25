package com.cleandroid;

/**
 * CollisionDetector class creates a background thread that check whether the robot is in its stalled state. 
 * If the stall occurs, then this thread will invoke a recover method from the Slam class, 
 * that will break Slams current execution and will try to recover the robot.
 * @author Team BLUE
 *
 */
public class CollisionDetector extends Thread{
	private Slam2 slam;
	
	/**
	 * Construct a new instance of the CollisionDetector
	 * @param slam - the currently running Slam2
	 */
	public CollisionDetector(Slam2 slam){
		this.slam = slam;
	}
	
	/**
	 * Run a background thread that checks for the robots stall state. 
	 * If the robot stalls, then call the recover method from the Slam2 class.
	 */
	public void run(){
		while (true) {
			if (slam.getStall() == 1) {
				// Call to the recover 
				slam.recover();
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
