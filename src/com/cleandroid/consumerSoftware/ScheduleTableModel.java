package com.cleandroid.consumerSoftware;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import com.cleandroid.SaveLoad;
import com.cleandroid.Slam2;

/**
 * An implementation of the AbstractTableModel, which allows to enter, update and delete schedule from the table.
 * Contains two columns representing the day and time of the schedule
 * 
 * @author TeamBlue
 * 
 */
public class ScheduleTableModel extends AbstractTableModel {

	/**
	 *Contains the names of the columns
	 */
	private String[] columnNames = { "Day", "Time" };
	
	/**
	 * List of all schedules added
	 */
	private ArrayList<Schedule> row;

	/**
	 * To validate data editing 
	 */
	private Pattern pattern;
	/**
	 * Object which should match for data editing
	 */
	private Matcher matcher;
	
	/**
	 * Regular Expression as String for editing time in 24 hour time format
	 */
	private static final String patternString24hours = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
	/**
	 * Saves the schedules as a serialised file
	 */
	SaveLoad save;

	/**
	 * Determines the day chosen from the table when editing
	 */
	int day;

	/**
	 * Service which launches the scheudles on time
	 */
	ScheduledExecutorService scheduler;

	/**
	 * Used with {@link ScheduledExecutorService} to determine time to start the schedule 
	 */
	long startTime;

	/**
	 * Used with {@link ScheduledExecutorService} to compare the time in the schedules.
	 * Avoids launching of schedules if time has passed by when the schedules are loaded
	 */
	Date current;

	/**
	 * Slam object on which schedule is to be performed
	 */
	Slam2 main;

	/**
	 * save all schedules in to a table and display it
	 * 
	 * @param main
	 *            : slam2 object
	 */
	public ScheduleTableModel(Slam2 main) {

		this.main = main;
		scheduler = Executors.newScheduledThreadPool(1);
		save = new SaveLoad();
		current = new Date();
		if (save.deserializeSchedules()) {
			row = save.getSchedules();
			for (int i = 0; i < row.size(); i++) {
				addEntry(row.get(i));
			}

		} else {
			row = new ArrayList<Schedule>();
		}

		pattern = Pattern.compile(patternString24hours);

	}

	/**
	 * 
	 * @return * the list of schedules
	 */
	public ArrayList<Schedule> getList() {
		return row;
	}

	/**
	 * 
	 * @param time
	 *            :- the time which has been edited
	 * @return boolean which is true if the time is valid according to 24 hour
	 *         model
	 */

	public boolean validate(final String time) {

		matcher = pattern.matcher(time);
		return matcher.matches();

	}

	
	public int getRowCount() {
		return row.size();
	}


	public String getColumnName(int col) {
		return columnNames[col];
	}

	
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Add a schedule task to the the service
	 * Serialize the schedules once added
	 * 
	 * @param obj1
	 */
	public void addEntry(Schedule obj1) {

		addScheduleToService(row.get(row.size() - 1));
		save.serializeSchedules(row);
		
	}
	/**
	 * Updates the table after a schedule is added
	 * 
	 * @param obj :- added to the the list of Schedules
	 */

	public void addToRow(Schedule obj) {
		row.add(obj);
		fireTableDataChanged();
	}

	/**
	 * Allows the data to be edited
	 */
	public void setValueAt(Object obj, int rowNumber, int col) {

		if (col == 0) {
			if (obj == "Mon")
				day = 2;
			else if (obj == "Tue")
				day = 3;
			else if (obj == "Wed") {
				day = 4;
			} else if (obj == "Thu")
				day = 5;
			else if (obj == "Fri")
				day = 6;
			else if (obj == "Sat")
				day = 0;
			else if (obj == "Sun")
				day = 1;

			row.get(rowNumber).set(col, day);
		}

		else {
			if (!validate(obj.toString()))
				JOptionPane.showMessageDialog(null, "enter a valid time");
			else
				row.get(rowNumber).set(col, obj);

		}
		fireTableDataChanged();
		resetService();

	}

	/**
	 * Removes all schedules from the service.
	 * Adds all schedules in the list to the service
	 */
	public void resetService() {
		scheduler.shutdownNow();

		scheduler = Executors.newScheduledThreadPool(1);

		for (int i = 0; i < row.size(); i++) {
			addScheduleToService(row.get(i));
		}
		save.serializeSchedules(row);
	}

	/**
	 * add schedule to service of scheduler
	 * 
	 * @param i
	 *            : schedule object
	 */
	public void addScheduleToService(Schedule i) {

		startTime = i.getDate().getTimeInMillis() - System.currentTimeMillis();
		if (startTime > 0) {

		} else
			startTime = startTime * -1;
		if (i.getDate().before(current)) {
			i.getDate().add(Calendar.DAY_OF_MONTH, 7);

			scheduler.scheduleAtFixedRate(
					new ScheduleTask(main, i.getOne(), i.getTwo()), startTime,
					1000 * 60 * 60 * 24 * 7, TimeUnit.MILLISECONDS);

		} else {
			scheduler.scheduleAtFixedRate(
					new ScheduleTask(main, i.getOne(), i.getTwo()), startTime,
					1000 * 60 * 60 * 24 * 7, TimeUnit.MILLISECONDS);
		}

	}

	/**
	 * Removes schedule from the selected row
	 * 
	 * @param i :- the index of the schedule to be removed
	 */
	public void removeSchedule(int i) {
		row.remove(i);
		fireTableDataChanged();

	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return row.get(rowIndex).get(columnIndex);
	}


	public boolean isCellEditable(int row, int col) {
		return true;
	}

}
