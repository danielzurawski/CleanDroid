package com.cleandroid.occupancyGridMap;

import java.io.Serializable;

/**
 * This generic class is used to store coordinates of different points on the map.
 * @author Team Blue
 *
 */
public class Point implements Serializable{
	
	/**
	 * Horizontal coordinate.
	 */
	private double x;
	
	/**
	 * Vertical coordinate.
	 */
	private double y;
	
	/**
	 * Point constructor.
	 * @param x - int x coordinate
	 * @param y - int y coordinate
	 */
	public Point(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public void setX(double x){
		this.x = x;
	}
	
	public void setY(double y){
		this.y = y;
	}
	
	public String toString(){
		return "x = "+ x+ " y  = " + y;
	}
}
