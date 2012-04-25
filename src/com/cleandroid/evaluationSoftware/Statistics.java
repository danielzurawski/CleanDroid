package com.cleandroid.evaluationSoftware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Statistics class which holds a list of cycles. 
 * Cycle inner class which encapsulates details about each individual clealing cycle.
 * @author daniel
 *
 */
public class Statistics implements Serializable {

	/**
	 * List of all history cycles and their data. 
	 */
	private ArrayList<Cycle> listOfCycles;
	
	public Statistics() {
		listOfCycles = new ArrayList<Cycle>();
	}
	
	/**
	 * Method for adding a cleaning cycle upon completion to the listOfCycles list.
	 * @param timeTaken - time taken for cleaning all of the environment in this cycle
	 * @param cellsTotal - the total amount of cells discovered
	 * @param cellsCovered - the amount of covered cells
	
	 */
	public void addCycle(long timeTaken, int cellsCovered, String time) {
		Cycle cycle = new Cycle(timeTaken, cellsCovered, time);
		
		listOfCycles.add(cycle);
	}

	/**
	 * An inner Cycle class for encapsulating data about each cleaning cycle.
	 * @author daniel
	 *
	 */
	class Cycle implements Serializable {
		
		private long timeTaken;
		private String time;
		private int cellsCovered;
		
		public Cycle(long timeTaken, int cellsCovered, String time) {
			this.timeTaken = timeTaken;
			this.cellsCovered = cellsCovered;
			this.time = time;
		}
		
		public long getTimeTaken() {
			return timeTaken;
		}
		public void setTimeTaken(long timeTaken) {
			this.timeTaken = timeTaken;
		}
		public String getTime() {
			return time;
		}
		public void setTime(String time) {
			this.time = time;
		}
		public int getCellsCovered() {
			return cellsCovered;
		}
		public void setCellsCovered(int cellsCovered) {
			this.cellsCovered = cellsCovered;
		}
		
	}
	/**
	 * 
	 * @return listOfCycles: a array list of statistics data
	 */
	public ArrayList<Cycle> getListOfCycles() {
		return listOfCycles;
	}
	/**
	 * set statistics data in the array list
	 */
	public void setListOfCycles(ArrayList<Cycle> listOfCycles) {
		this.listOfCycles = listOfCycles;
	}
	
}
