package com.cleandroid;

import javaclient3.PlayerClient;
import javaclient3.PlayerException;
import javaclient3.Position2DInterface;
import javaclient3.RangerInterface;
import javaclient3.structures.PlayerConstants;

/**
 * Robot class provides the fields for the Robot so that other classes can extend it, as well as it proxies the player with the stage.
 * It also provides a reference to the pos2D interface, as well as the ranger interface.
 */
public class Robot extends Thread {
    
	
    /**
     * The robot's current heading.
     */
    int heading;
    
    /**
     * X coordinate of the robot.
     */
    double x;
    
    /**
     * Y coordinate of the robot.
     */
    double y;
    
    /**
     * Yaw of the robot.
     */
    double yaw;
    
    /**
     * Stall state of the robot. Is the robot stalled?
     */
    int stall;
    
    /**
     * The robot's front sensor reading when it has crashed.
     */
    double frontForCrash = 0;
   
    /**
	 * Accuracy for the robots yaw calculations used in SLAM.
	 */
    protected final static int ACCURACY = 2500;
    
    /**
     * Robots turn rate value, the lower the quicker the robot turns.
     */
    protected double turnRate = 1.35;
    
    /**
     * Constant integer 3 of the robots North direction in the form of a variable.
     */
    public final static int NORTH=3;
    
    /**
     * Constant integer 0 of the robots East direction in the form of a variable.
     */
    public final static int EAST=0;
    
    /**
     * Constant integer 2 of the robots West direction in the form of a variable.
     */
    public final static int WEST=2;
    
    /**
     * Constant integer 3 of the robots South direction in the form of a variable.
     */
    public final static int SOUTH=1;

    /**
     The PlayerClient robots Proxy object.
     */
    PlayerClient robot=null;
    
    /**
     The robots position2d interface (actuators).
     */
    Position2DInterface pos2D=null;
    
    /**
     The robots RangerInterface (sonars).
     */
    RangerInterface sonar=null;
    
    /**
     * The robot's front sensor reading.
     */
    double front = 0;
    
    /**
     * The robot's back sensor reading.
     */
    double back = 0;
    
    /**
     * The robot's left sensor reading.
     */
    double left = 0;
    
    /**
     * The robot's right sensor reading.
     */
    double right = 0;

    /**
     Constructor for the robot, which sets up the proxy objects and a thread to
     keep the sensor fields up to date.
     */
    public Robot() {

        // Set up service proxies
        try {
            robot = new PlayerClient("localhost", 6665);
            pos2D = robot.requestInterfacePosition2D(0,PlayerConstants.PLAYER_OPEN_MODE);
            sonar = robot.requestInterfaceRanger(0,PlayerConstants.PLAYER_OPEN_MODE);
        } catch (PlayerException e) {
            System.err.println("BaseRobot: Error connecting to Player!\n>>>" + e.toString());
            System.exit(1);
        }
        
        /**
         * Run the robot in a threaded mode. 
         */
        robot.runThreaded(-1,-1);

        // Instantiate the values for the sonars
        while(!sonar.isDataReady());
        double[] sonarValues = sonar.getData().getRanges();
        front = min(sonarValues[12] - 0.2, sonarValues[3] - 0.4,
				sonarValues[4] - 0.4, sonarValues[10]*Math.cos(Math.toRadians(5)) - 0.1, sonarValues[11]*Math.cos(Math.toRadians(5)) - 0.1);
		left = min(sonarValues[13] - 0.2, sonarValues[5] - 0.1,
				sonarValues[6] - 0.1, 1000, 1000);
		back = min(sonarValues[0], sonarValues[1], sonarValues[2], 1000, 1000);
		right = min(sonarValues[14] - 0.2, sonarValues[7] - 0.1,
				sonarValues[8] - 0.1, 1000, 1000);
    }
    
    /**
     * Method returning the minimum double out of the given list of double values.
     * @param d1 - double value 1
     * @param d2 - double value 2
     * @param d3 - double value 3
     * @param d4 - double value 4
     * @param d5 - double value 5
     * @return min - double minimum value out of the given doubles
     */
    public double min(double d1, double d2, double d3, double d4, double d5){
    	double min = d1;
    	if(d2 < min){
    		min = d2;
    	}
    	if(d3 < min){
    		min = d3;
    	}
    	if(d4 < min){
    		min = d4;
    	}
    	if(d5 < min){
    		min = d5;
    	}
    	return min;
    }
    
    /**
     * Getter for robots sonar.
     * @return RangerInterface sonar.
     */
    public RangerInterface getSonar(){
    	return sonar;
    }

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getYaw() {
		return yaw;
	}

	public int getStall() {
		return stall;
	}

	public void setYaw(double yaw) {
		this.yaw = yaw;
	}

	public void setStall(int stall) {
		this.stall = stall;
	}
	
	
    
    
}