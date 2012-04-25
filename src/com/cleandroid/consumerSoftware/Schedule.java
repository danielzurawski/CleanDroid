package com.cleandroid.consumerSoftware;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import com.cleandroid.occupancyGridMap.GridCell;
import com.cleandroid.occupancyGridMap.Point;
/**
 * this class create and save the schedules made by consumers to schedule the robot to 
 * work in the future automatically
 * @author Song
 *
 */
public class Schedule implements Serializable {

	/**
	 * Integer representing the day of the week
	 */
	int dayofWeek;
	
	/**
	 * Integer representing the hour of the day
	 */
	int hourOfDay;
	
	/**
	 * Integer representing the minute of the day
	 */
	int minute;
	
	/**
	 * Integer representing the duration
	 */
	int ampm;

	/**
	 * Used to return the day from the date entered in the schedule
	 */
	DateFormat dfDay;
	
	/**
	 * Used to return the time of the date entered 
	 */
	DateFormat dfTime;

	/**
	 * Used to return the hours of date entered
	 */
	DateFormat dfHours;
	
	/**
	 * Used to return the minutes ot the date entered
	 */
	DateFormat dfMin;

	/**
	 * Used to add the date to each schedule
	 */
	private Calendar date;
	

	private Point one;
	private Point two;
	/**
	 * create a schedule by setting the time and selected area
	 * @param dayofWeek : the day of the week( Mon,Tue....Sun)
	 * @param hourofDay : hour of the day ( in 12 hour format)
	 * @param minute : minute of the hour
	 * @param ampm : set the if it is am or pm of the day
	 * @param selectedCells: selected cells by the consumer to be cleaned according
	 * 							to schedule
	 */
	public Schedule(int dayofWeek, int hourofDay, int minute, int ampm,
			Point one, Point two) {
		
		this.setOne(one);
		this.setTwo(two);
		this.dayofWeek = dayofWeek;
		this.hourOfDay = hourofDay;
		this.minute = minute;
		this.ampm = ampm;
		date = Calendar.getInstance();
		
		
		if(hourOfDay == 12){
			if(ampm == 1)
				ampm = 0;
			else
				ampm = 1;
		}
		date.set(Calendar.AM_PM, ampm);
		date.set(Calendar.DAY_OF_WEEK, dayofWeek);
		date.set(Calendar.HOUR, hourofDay);
		date.set(Calendar.MINUTE, minute);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		dfTime = new SimpleDateFormat("HH:mm");
		dfHours = new SimpleDateFormat("HH");
		dfMin = new SimpleDateFormat("mm");
		dfDay = new SimpleDateFormat("EEE");

	}
	
	//TODO COMMENT
	/**
	 * 
	 * @param col
	 * @return
	 */
	public String get(int col) {
		if (col == 0)
			return dfDay.format(getDate().getTime());
		else if (col == 1)
			return dfTime.format(getDate().getTime());
		else
			return null;
	}

	public Calendar getDate() {
		return date;
	}
	
	public void setDate(Calendar date) {
		this.date = date;
	}
	
	
	public void set(int column, Object object) {

		if (column == 0) {

			date.set(Calendar.DAY_OF_WEEK, (Integer) object);
		}
		int hr;
		int min;

		if (column == 1) {

			hr = Integer.parseInt((object).toString().substring(0, 2));

			if (hr > 12) {
				hr = hr - 12;
				date.set(Calendar.AM_PM, Calendar.PM);
			} else if (hr == 0 || hr == 24) {
				hr = 12;
				date.set(Calendar.AM_PM, Calendar.AM);

			}

			else {

				date.set(Calendar.AM_PM, Calendar.AM);

			}

			min = Integer.parseInt((object).toString().substring(3));
			date.set(Calendar.HOUR, hr);
			date.set(Calendar.MINUTE, min);
		}
	}
	
	public String toString() {
		return "the schedule is " + hourOfDay + " " + minute + " " + ampm;
	}

	public Point getOne() {
		return one;
	}

	public void setOne(Point one) {
		this.one = one;
	}

	public Point getTwo() {
		return two;
	}

	public void setTwo(Point two) {
		this.two = two;
	}

}
