package com.cleandroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.cleandroid.consumerSoftware.Schedule;
import com.cleandroid.evaluationSoftware.Statistics;
import com.cleandroid.occupancyGridMap.GridMap;


/**
 * SaveLoad class that provides easy to use methods for serializing, as well as
 * de-serializing specific components of the program - the map, schedules and
 * statistics (and growing).
 * 
 * @author Team BLUE
 * 
 */
public class SaveLoad {

	/**
	 * Reference to the Slam2 class
	 */
	private Slam2 slam2;
	/**
	 * A list of schedules, after they have been deserialized
	 */
	private ArrayList<Schedule> schedules;
	/**
	 * Statistics object that contains a list of Cycles, after it it de-serialized
	 */
	private Statistics statistics;
	/**
	 * GridMap object that contains the previous map, after it is de-serialized
	 */
	private GridMap gridMap;
	private String mapPath;
	private String schedulesPath;
	private String statisticsPath;
	private File directory = new File("");

	/**
	 * Construct a new SaveLoad for map serialisation.
	 * @param slam2 Slam2 object - currently run environment
	 */
	public SaveLoad(Slam2 slam2) {
		this.slam2 = slam2;
		mapPath = directory.getAbsolutePath() + "/com/cleandroid/consumerSoftware/map.ser";
		

	}

	/**
	 * Construct a new SaveLoad for schedules and statistics only (no Slam2 needed).
	 */
	public SaveLoad() {
		schedulesPath = directory.getAbsolutePath()
				+ "/com/cleandroid/consumerSoftware/schedules.ser";
		statisticsPath = directory.getAbsolutePath()
				+ "/com/cleandroid/evaluationSoftware/stats.ser";
	}

	/**
	 * Serialize the current map from the Slam2 object into a file specified
	 */
	public void serializeMap() {
		try {

			FileOutputStream fosMap;
			fosMap = new FileOutputStream(mapPath);
			ObjectOutputStream outMap;
			outMap = new ObjectOutputStream(fosMap);

			outMap.writeObject(this.getSlam2().getMap());

			outMap.close();
			fosMap.close();
			System.out.println("SaveLoad: Map has been serialized!");

		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	/**
	 * Map for de-serializing the map and loading it into the gridMap field of
	 * this class, so that it can be easily accessed whenever required.
	 * 
	 * @return true if the file exists, false otherwise
	 */
	public boolean deserializeMap() {
		FileInputStream fisMap;
		ObjectInputStream inMap;

		boolean exists = (new File(mapPath)).exists();

		if (exists) {
			try {

				fisMap = new FileInputStream(mapPath);
				inMap = new ObjectInputStream(fisMap);

				try {

					gridMap = (GridMap) inMap.readObject();

					for (int i = 0; i < gridMap.getGrid().length; i++) {
						for (int j = 0; j < gridMap.getGrid()[0].length; j++) {
							if (gridMap.getGrid()[i][j].getType() == 0) {
								
								slam2.addToUnvisitedCells(gridMap.getGrid()[i][j]);
							}
						}
					}

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				inMap.close();
				fisMap.close();
				System.out.println("SaveLoad: Map has been deserialized!");

			} catch (IOException io) {
				io.printStackTrace();
			}
		}
		return exists;
	}

	/**
	 * This method provides functionality for serializing a list (ArrayList) of
	 * type Schedule.
	 * 
	 * @param schedules
	 *            A list of schedules to be serialized
	 */
	public void serializeSchedules(ArrayList<Schedule> schedules) {
		try {

			FileOutputStream fosSchedules;
			ObjectOutputStream outSchedules;
			fosSchedules = new FileOutputStream(schedulesPath);
			outSchedules = new ObjectOutputStream(fosSchedules);

			outSchedules.writeObject(schedules);

			outSchedules.close();
			fosSchedules.close();
			System.out.println("SaveLoad: Schedules have been serialized!");

		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	/**
	 * Method for de-serializing schedules stored on the disk and then assigning
	 * them to the schedules field of this class, so that it can be easily
	 * accessed later on.
	 * 
	 * @return true if file exists, false otherwise
	 */
	public boolean deserializeSchedules() {
		FileInputStream fisSchedules;
		ObjectInputStream inSchedules;

		boolean exists = (new File(schedulesPath)).exists();

		if (exists) {
			try {

				fisSchedules = new FileInputStream(schedulesPath);
				inSchedules = new ObjectInputStream(fisSchedules);

				try {
					schedules = new ArrayList<Schedule>();
					setSchedules((ArrayList<Schedule>) inSchedules.readObject());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				inSchedules.close();
				fisSchedules.close();
				System.out
						.println("SaveLoad: Schedules have been deserialized!");

				return true;

			} catch (IOException io) {
				io.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Method for de-serializing the statistics object which contains a list of
	 * Cycles, each containing data specific to a history cleaning cycle
	 * 
	 * @return true if file exists, false otherwise
	 */
	public boolean deserializeStatistics() {
		FileInputStream fisStatistics;
		ObjectInputStream inStatistics;

		boolean exists = (new File(statisticsPath)).exists();
		
		if (exists) {
			try {

				fisStatistics = new FileInputStream(statisticsPath);
				inStatistics = new ObjectInputStream(fisStatistics);

				try {
					setStatistics((Statistics) inStatistics.readObject());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				inStatistics.close();
				fisStatistics.close();
				System.out
						.println("SaveLoad: Statistics have been deserialized!");

				return true;

			} catch (IOException io) {
				io.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Method for serializing the statistics with regards to each cleaning cycle.
	 * @param statistics Statistics object that contains a list of Cycles
	 */
	public void serializeStatistics(Statistics statistics) {
		try {

			FileOutputStream fosStatistics;
			ObjectOutputStream outStatistics;
			fosStatistics = new FileOutputStream(statisticsPath);
			outStatistics = new ObjectOutputStream(fosStatistics);

			outStatistics.writeObject(statistics);

			outStatistics.close();
			fosStatistics.close();
			System.out.println("SaveLoad: Statistics have been serialized!");

		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public Slam2 getSlam2() {
		return this.slam2;
	}

	public void setSlam2(Slam2 slam2) {
		this.slam2 = slam2;
	}

	public GridMap getGridMap() {
		return gridMap;
	}

	public void setGridMap(GridMap gridMap) {
		this.gridMap = gridMap;
	}

	/**
	 * Getter for the schedules. This can be used after deserializeSchedules has been used.
	 * @return ArrayList of de-sersialized schedules 
	 */
	public ArrayList<Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(ArrayList<Schedule> schedules) {
		this.schedules = schedules;
	}

	/**
	 * Getter for the statistics. This can be used after deserializeStatisticss has been used.
	 * @return instance of Statistics object.
	 */
	public Statistics getStatistics() {
		return statistics;
	}

	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}

}
