package com.cleandroid.consumerSoftware;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;

import com.cleandroid.SaveLoad;
import com.cleandroid.Slam2;
import com.cleandroid.occupancyGridMap.GridCell;
import com.cleandroid.occupancyGridMap.Point;

public class ConsumerGUI extends JFrame {

	boolean load;
	double zoomValue;

	boolean repaint;

	String selectionError;

	String dayError;

	boolean complete;
	// to display time
	private JLabel jlTime;

	// to display the status
	private JLabel returnToBaseStatus;
	private JLabel jlMapDetection;

	// shows laoding animation
	private JLabel jlLoadingAnimation;

	// to set the color interval as selected by user
	JButton jbSetColorInterval;

	// the zoom buttons
	JButton jbIn;
	JButton jbOut;

	// JPanels to arrange the GUI items according to BorderLayout
	JPanel jpSouth;
	// panel to show zoom controls
	JPanel jpWest;
	// panel to show schedules toggle
	JPanel jpNorth;
	// panel to show and add schedules
	JPanel jpEast;

	// panel to set time for schedules
	JPanel jpTime;

	// panel containing zoom controls
	JPanel jpZoom;

	// panel containing objects to add schedule
	JPanel jpAddSchedule;
	// panel contianting days jcheckboxes
	JPanel jpDaysList;
	// panel containing
	JPanel jpScheduleArea;
	// panel showing all saved schedules
	JPanel jpViewSchedules;

	// panel containing objects for immediate cleaning
	JPanel jpImmediateCleaning;

	// panel to set values for color coding of the map
	JPanel jpColorReports;

	JTabbedPane jpToggle;

	// to show cleaning options
	JTabbedPane jtpCleaning;

	// /scrollpane for the days
	JScrollPane jspDays;
	// scroll pane for the map
	JScrollPane jspMap;
	// scroll pane for the days list
	JScrollPane jspDaysList;
	// scrollpane for schedule table
	JScrollPane jspScheduleTable;

	// the map panel
	ConsumerMapPanel gpMap;

	// the slam object
	Slam2 main;

	// button to start the robot and bring it back to base
	JCheckBox jbStart;

	// adds schedule to list
	JButton jbAddSchedule;
	// resets the selection on the map
	JButton jbResetSelection;

	// toggle button to display schedules
	JCheckBox jcbControls;

	// controls the zoom
	JSlider jsZoom;

	// used to add selection to a schedule
	LinkedList<GridCell> selection;

	// table showing list of schedules
	JTable jtSchedules;

	// to store the schedules and display them accordingly
	ScheduleTableModel schedulesModel;
	/*
	 * combo box boxes to specify schedules
	 */
	JComboBox jcbHour;
	JComboBox jcbMinute;
	JComboBox jcbPeriod;
	JComboBox jcbSelectionCleaning;
	JComboBox jcbSelectionSchedule;

	// combox boxes to determine values to be used for the intervals of color
	// coding
	JComboBox jcbColorDay;
	JComboBox jcbColorHr;
	JComboBox jcbColorMin;
	JComboBox jcbColorSeconds;

	// tabbed pane to add and display schedules
	JTabbedPane jtpSchedule;

	// lists for the combo boxes
	Object[] hourList = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
			"21", "22", "23"};

	Object[] minuteList;

	Object[] hourColorList;

	Object[] dayColorList;

	Object[] dcbSelection;

	// the checkboxes to specify days for the schedules
	JCheckBox jcbMonday;
	JCheckBox jcbTuesday;
	JCheckBox jcbWednesday;
	JCheckBox jcbThursday;
	JCheckBox jcbFriday;
	JCheckBox jcbSaturday;
	JCheckBox jcbSunday;

	// to store the panel size
	Point panelSize;

	File directory = new File(".");

	ImageIcon on;
	ImageIcon off;
	ImageIcon powerOn;
	ImageIcon powerOff;
	ImageIcon toggleDown;
	ImageIcon toggleUp;
	ImageIcon loadingAnimation;
	ImageIcon reset;

	private ArrayList<Schedule> schedule;

	ConsumerGUI frame;

	SaveLoad save;

	private DateFormat dateFormat;

	protected int start;

	int counter;

	String[] periodList = { "am", "pm" };

	Thread execute;

	JComboBox dayBox;

	DefaultCellEditor dceEdit;

	ConsumerMapPanel schedulePanel;

	JButton jbDeleteSchedule;
	private JLabel jlFirstInterval;
	private JLabel jlSecondInterval;
	private JLabel jlThirdInterval;
	private JLabel jlFourthInterval;
	private JLabel jlSixthInterval;
	private JLabel jlFifthInterval;
	/**
	 * This class build consumer GUI tool
	 * 
	 * @param a: a object of slam2
	 */
	
	public ConsumerGUI(Slam2 a) {
		// initalize declared variables and objects
		super("Consumer Tool");
		panelSize = new Point(700, 670);
		setSize((int) panelSize.getX() + 254, (int) panelSize.getY() + 52);

		save = new SaveLoad();

		zoomValue = 1;

		main = a;

		dcbSelection = new Object[2];

		dcbSelection[0] = "complete";
		dcbSelection[1] = "selection";

		schedulesModel = new ScheduleTableModel(getSlam2());

		// Initialise the table and add tool tip to it
		jtSchedules = new JTable(schedulesModel) {

			// Implement table cell tool tips.
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);
				int realColumnIndex = convertColumnIndexToModel(colIndex);

				boolean selected = schedulesModel.getList().get(rowIndex)
						.getOne() != null;
				if (selected) { 
					tip = "this is for: " + "selection";
				} else {
					tip = "this is for complete";
				}

				return tip;
			}
		};

	

		minuteList = new Object[60];

		hourColorList = new Object[24];

		dayColorList = new Object[7];

		updateModels();

		execute = new Thread(new Runnable() {
			public void run() {
				boolean load = false;

				while (true) {
					if (repaint) {
						gpMap.repaint();
					}

					if (getSlam2().isEvaluatingEnvironment()) {
						if (!load)
							jpSouth.add(jlLoadingAnimation);
						jpSouth.add(jlMapDetection);
						jpSouth.revalidate();
						load = true;
					}

					else {
						jpSouth.remove(jlMapDetection);
						jpSouth.revalidate();
					}

					if (getSlam2().getReturnToBase()) {

						if (!load)
							jpSouth.add(jlLoadingAnimation);

						jpSouth.add(returnToBaseStatus);
						jpSouth.revalidate();
					}

					else {
						jpSouth.remove(returnToBaseStatus);
						jpSouth.revalidate();
					}

					if (!getSlam2().getReturnToBase()
							&& !getSlam2().isEvaluatingEnvironment()) {
						jpSouth.remove(jlLoadingAnimation);
						load = false;
					}

					jlTime.setText("Time "
							+ (dateFormat.format(System.currentTimeMillis())));

					try {
						Thread.sleep(1000);
					} catch (Exception e) {

					}
				}
			}
		});

	}

	/**
	 * starts the execute thread
	 * 
	 * @param repaint
	 *            :- boolean to indicate if the thread should repaint the map or
	 *            not; set to true if libraries are not loaded
	 */

	public void execute(boolean repaint) {

		this.repaint = repaint;
		execute.start();
	}

	/**
	 * @return: return the scroll bar
	 */
	public JScrollPane getJspMap() {
		return jspMap;
	}

	/**
	 * 
	 * @return: return the map panel
	 */

	public ConsumerMapPanel getGpMap() {
		return gpMap;
	}

	/**
	 * @return : return arraylist of schedules
	 */

	public ArrayList<Schedule> getSchedule() {
		return schedule;
	}

	/**
	 * update the data in ComboBoxes
	 */

	public void updateModels() {

		for (int i = 0; i < 10; i++) {
			minuteList[i] = "0" + Integer.toString(i);
		}

		for (int i = 10; i < 60; i++) {
			minuteList[i] = i;
		}

		for (int i = 0; i < dayColorList.length; i++) {
			dayColorList[i] = i;
		}

		for (int i = 0; i < 24; i++) {
			hourColorList[i] = i;
		}
	}

	/**
	 * to check if jpToggle has to be shown or hidden
	 * 
	 * @param args
	 */
	public void settingsDisplay() {

		if (jcbControls.isSelected()) {
			jpEast.add(jtpCleaning, BorderLayout.SOUTH);
			jpEast.add(jpToggle, BorderLayout.CENTER);
			jcbControls.setIcon(toggleUp);

		} else {

			jpEast.remove(jpToggle);
			jpEast.add(jtpCleaning, BorderLayout.NORTH);
			jcbControls.setIcon(toggleDown);

		}

		revalidate();
		repaint();
	}

	/**
	 * 
	 * @param zoom
	 *            the percentage by which the map has to be increased
	 */
	public void setZoom(double zoom) {

		if (zoom == 1) {
			gpMap.setShrink(1);
		} else {
			double setZoom = 1 - (zoomValue - zoom);

			if (setZoom != 1)
				gpMap.setShrink(setZoom);

		}
		zoomValue = zoom;
		if (zoomValue > 1) {

			jbOut.setEnabled(true);
		} else {
			jbOut.setEnabled(false);

		}

	}

	/**
	 * Add action listeners for all buttons initialised in constructor
	 */
	public void addActionListner() {

		jbDeleteSchedule.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int row = jtSchedules.getSelectedRow();
				System.out.println("the selected row is " + row);

				schedulesModel.removeSchedule(row);
			}
		});

		dayListener listner = new dayListener();
		jcbMonday.addActionListener(listner);
		jcbTuesday.addActionListener(listner);
		jcbWednesday.addActionListener(listner);
		jcbThursday.addActionListener(listner);
		jcbFriday.addActionListener(listner);
		jcbSaturday.addActionListener(listner);
		jcbSunday.addActionListener(listner);

		jbStart.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (jbStart.isSelected()) {
					if (!main.isAlive()) {
						main.start();
						main.setStarted(true);
						jbStart.setToolTipText("Return to charging station");
					}

					if (jcbSelectionCleaning.getSelectedItem().equals(
							"selection")) {

						if (getSlam2().getSelectedCells().size() == 0) {
							JOptionPane.showMessageDialog(frame,
									"select an area first");
							jbStart.setSelected(false);

						} else {
							main.setStop(true);
							main.setAreaToCleanSelected(true);
							jbStart.setIcon(powerOn);
						}

					} else {

						main.setStop(false);
						main.setAreaToCleanSelected(false);
						jbStart.setIcon(powerOn);
					}

				} else {

					main.setStop(true);
					jbStart.setIcon(powerOff);
					main.setReturnToBase(true);
					jbStart.setToolTipText("Start Cleaning");
				}
			}

		});

		jbIn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				jsZoom.setValue(jsZoom.getValue() + 10);

			}

		});

		jbOut.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				jsZoom.setValue(jsZoom.getValue() - 10);

			}

		});

		jsZoom.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				setZoom(((double) (jsZoom.getValue()) / 100) + 1);
			}

		});

		jcbControls.addActionListener(new ActionListener()

		{

			public void actionPerformed(ActionEvent e) {

				settingsDisplay();
			}
		});

		jbAddSchedule.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				int hourofDay = jcbHour.getSelectedIndex();
				int minute = jcbMinute.getSelectedIndex();

				int period;

				counter = 0;

				Point one;
				Point two = null;
				if (hourofDay > 12) {
					period = 1;
					hourofDay = hourofDay - 12;

				} else {

					period = 0;
				}

				complete = jcbSelectionSchedule.getSelectedItem() == "complete";

				if (!complete) {

					one = gpMap.getOne();
					two = gpMap.getTwo();
				} else {
					one = null;
				}
				if (jcbMonday.isSelected()) {
					schedulesModel.addToRow(new Schedule(2, hourofDay, minute,
							period, one, two));
					schedulesModel.addEntry(new Schedule(2, hourofDay, minute,
							period, one, two));
					jcbMonday.setSelected(false);
					setDay(jcbMonday);

					counter++;

				}
				if (jcbTuesday.isSelected()) {
					schedulesModel.addToRow(new Schedule(3, hourofDay, minute,
							period, one, two));
					schedulesModel.addEntry(new Schedule(3, hourofDay, minute,
							period, one, two));
					jcbTuesday.setSelected(false);
					setDay(jcbTuesday);

					counter++;
				}
				if (jcbWednesday.isSelected()) {
					schedulesModel.addToRow(new Schedule(4, hourofDay, minute,
							period, one, two));
					schedulesModel.addEntry(new Schedule(4, hourofDay, minute,
							period, one, two));
					jcbWednesday.setSelected(false);
					setDay(jcbWednesday);
					counter++;
				}
				if (jcbThursday.isSelected()) {
					schedulesModel.addToRow(new Schedule(5, hourofDay, minute,
							period, one, two));
					schedulesModel.addEntry(new Schedule(5, hourofDay, minute,
							period, one, two));
					jcbThursday.setSelected(false);
					setDay(jcbThursday);
					counter++;
				}
				if (jcbFriday.isSelected()) {
					schedulesModel.addToRow(new Schedule(6, hourofDay, minute,
							period, one, two));

					schedulesModel.addEntry(new Schedule(6, hourofDay, minute,
							period, one, two));
					jcbFriday.setSelected(false);
					counter++;

					setDay(jcbFriday);
				}
				if (jcbSaturday.isSelected()) {
					schedulesModel.addToRow(new Schedule(0, hourofDay, minute,
							period, one, two));
					schedulesModel.addEntry(new Schedule(0, hourofDay, minute,
							period, one, two));
					jcbSaturday.setSelected(false);
					System.out.println("Lets print out the schedules mode: ");
					System.out.printf(schedulesModel.toString());
					setDay(jcbSaturday);
					counter++;
				}

				if (jcbSunday.isSelected()) {
					schedulesModel.addToRow(new Schedule(1, hourofDay, minute,
							period, one, two));
					schedulesModel.addEntry(new Schedule(1, hourofDay, minute,
							period, one, two));
					jcbSunday.setSelected(false);

					setDay(jcbSunday);
					counter++;
				}

				if (!complete) {
					JOptionPane
							.showMessageDialog(frame, "Select an area first");
				}

				if (counter == 0) {
					if (counter == 0)

						JOptionPane.showMessageDialog(frame, "Select a day");

				}

			}
		});

		jbSetColorInterval.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

				SimpleDateFormat sdf = new SimpleDateFormat("dd:HH:mm:ss");
				getGpMap()
						.setReportInterval(
								((jcbColorDay.getSelectedIndex() * 60 * 60 * 24) + (jcbColorHr
										.getSelectedIndex()) * 60 * 60)
										+ ((jcbColorMin.getSelectedIndex()) * 60));

				int day = jcbColorDay.getSelectedIndex();
				int hr = jcbColorHr.getSelectedIndex();
				int min = jcbColorMin.getSelectedIndex();

				jlFirstInterval.setText(day + " day " + hr + "hr " + min
						+ " min ");
				jlSecondInterval.setText((2 * day) + " day " + (2 * hr) + "hr "
						+ (2 * min) + " min ");
				jlThirdInterval.setText((3 * day) + " day " + (3 * hr) + "hr "
						+ (3 * min) + " min ");
				jlFourthInterval.setText((4 * day) + " day " + (4 * hr) + "hr "
						+ (4 * min) + " min ");
				jlFifthInterval.setText((5 * day) + " day " + (5 * hr) + "hr "
						+ (5 * min) + " min ");
				jlFifthInterval.setText((6 * day) + " day " + (6 * hr) + "hr "
						+ (6 * min) + " min ");
				
				

			}

		});

		jcbSelectionCleaning.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				Object[] options = { "No", "Yes" };
				if (jcbSelectionCleaning.getSelectedItem() == "selection"
						&& jbStart.isSelected()) {

					// System.out.println("should show dialog box");

					int n = JOptionPane
							.showOptionDialog(
									frame,
									"<html>"
											+ "Cleaning already in process."
											+ "<br>"
											+ "Would you like to clean the selected area instead? </html>",
									"Confirmation",
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.QUESTION_MESSAGE, null,
									options, options[1]);
					if (n == 1) {
						getSlam2().setStop(true);
						getSlam2().setAreaToCleanSelected(true);
					}
				}

				else if (jcbSelectionCleaning.getSelectedItem() == "complete"
						&& jbStart.isSelected()) {
					int n = JOptionPane
							.showOptionDialog(
									frame,
									"<html>"
											+ "Cleaning already in process."
											+ "<br>"
											+ "Would you like to clean the complete area instead? </html>",
									"A Silly Question",
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.QUESTION_MESSAGE, null,
									options, options[1]);
					if (n == 1) {
						getSlam2().setAreaToCleanSelected(false);
						getSlam2().setStop(false);

					}
				}
			}
		});

		jbResetSelection.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

				getSlam2().resetMapSelection();
			}
		});

	}

	/**
	 * set day checkbox to true or false accordingly
	 * 
	 * @param day
	 */

	public void setDay(JCheckBox day) {
		if (day.isSelected()) {
			day.setIcon(on);

			System.out.println("true");

		} else {
			day.setIcon(off);

			System.out.println("off");
		}
		jspDaysList.revalidate();
		// day.setSelected(true);
		jpEast.revalidate();

	}

	/**
	 * 
	 * @return: return slam2
	 */
	public Slam2 getSlam2() {
		return main;
	}

	/**
	 * add a listener to the day checkbox
	 * 
	 */

	private class dayListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			// TODO Auto-generated method stub

			setDay((JCheckBox) e.getSource());

		}

	}

	/**
	 * create consumer GUI, initialise and add all components to the frame
	 */

	public void createGUI() {

		returnToBaseStatus = new JLabel("Returning to charging station");

		jlTime = new JLabel("Time "
				+ (System.currentTimeMillis() / (1000 * 60 * 60)) + " : "
				+ (System.currentTimeMillis() / (1000 * 60)));

		jlMapDetection = new JLabel("Detecting environment");

		gpMap = new ConsumerMapPanel(panelSize.getX(), panelSize.getY(), this);

		jspMap = new JScrollPane(gpMap,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		off = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/consumerSoftware/off.png");
		on = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/consumerSoftware/on.png");
		powerOn = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/consumerSoftware/powerOn.png");
		powerOff = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/consumerSoftware/powerOff.png");
		toggleDown = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/consumerSoftware/toggleDown.png");
		toggleUp = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/consumerSoftware/toggleUp.png");
		loadingAnimation = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/consumerSoftware/loading.gif");
		reset = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/consumerSoftware/reset.png");

		jlLoadingAnimation = new JLabel(loadingAnimation);

		jbSetColorInterval = new JButton("Set");

		jbStart = new JCheckBox();
		jbStart.setIcon(powerOff);
		jbStart.setToolTipText("Start Cleaning");

		jcbMonday = new JCheckBox("Monday", off);
		jcbMonday.setForeground(Color.YELLOW);

		jcbTuesday = new JCheckBox("Tuesday", off);
		jcbTuesday.setForeground(Color.YELLOW);

		jcbWednesday = new JCheckBox("Wednesday", off);
		jcbWednesday.setForeground(Color.YELLOW);

		jcbThursday = new JCheckBox("Thursday", off);
		jcbThursday.setForeground(Color.YELLOW);

		jcbFriday = new JCheckBox("Friday", off);
		jcbFriday.setForeground(Color.YELLOW);

		jcbSaturday = new JCheckBox("Saturday", off);
		jcbSaturday.setForeground(Color.YELLOW);

		jcbSunday = new JCheckBox("Sunday", off);
		jcbSunday.setForeground(Color.YELLOW);

		jbAddSchedule = new JButton("Add");

		jbResetSelection = new JButton(reset);
		jbResetSelection.setBounds(57, 280, 18, 21);

		jcbHour = new JComboBox(hourList);
		jcbMinute = new JComboBox(minuteList);

		jcbSelectionCleaning = new JComboBox(dcbSelection);
		jcbSelectionSchedule = new JComboBox(dcbSelection);

		jcbColorDay = new JComboBox(dayColorList);
		jcbColorHr = new JComboBox(hourColorList);
		jcbColorMin = new JComboBox(minuteList);

		dateFormat = new SimpleDateFormat("HH:mm:ss");

		jpColorReports = new JPanel(new BorderLayout());
		// jpColorReports = new JPanel(new GridBagLayout());

		jpTime = new JPanel(new GridLayout(2, 3));

		jsZoom = new JSlider(JSlider.VERTICAL);

		jbIn = new JButton();
		// jbIn.setIcon(zoomIn);

		jbOut = new JButton();

		jtpSchedule = new JTabbedPane();

		jpViewSchedules = new JPanel(new BorderLayout());

		TableColumn dayColumn = jtSchedules.getColumnModel().getColumn(0);
		dayBox = new JComboBox();

		jcbControls = new JCheckBox("Control", toggleDown);
		jcbControls.setSelected(false);

		DefaultCellEditor dceEdit = new DefaultCellEditor(dayBox);

		jpZoom = new JPanel();

		jpToggle = new JTabbedPane();

		jpToggle.add("Settings", jpColorReports);
		jpToggle.add("Schedules", jtpSchedule);

		// jpToggle.add(jcbSchedules);

		jpSouth = new JPanel();
		jpWest = new JPanel();
		jpNorth = new JPanel(new BorderLayout());
		jpEast = new JPanel(new BorderLayout());

		jsZoom.setValue(0);
		jsZoom.addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				// TODO Auto-generated method stub
				int notches = -e.getWheelRotation();
				jsZoom.setValue(jsZoom.getValue() + +(notches * 10));
			}
		});

		jbOut.setEnabled(false);

		jbIn.setBounds(2, 2, 12, 12);
		jsZoom.setBounds(2, 16, 15, 222);
		jbOut.setBounds(3, 238, 12, 12);

		jbDeleteSchedule = new JButton("Delete Schedule(s)");

		addActionListner();

		jcbControls.setBounds(0, 0, 23, 22);

		jpSouth.setBackground(Color.WHITE);

		jpWest.setLayout(null);
		jpWest.setPreferredSize(new Dimension((int) (this.getWidth() * 0.08),
				(int) (this.getHeight())));

		jpEast.setPreferredSize(new Dimension(210, 500));

		jpZoom.setLayout(null);
		jpZoom.setBounds(55, 15, 23, 252);
		jpZoom.setBackground(Color.WHITE);

		dayBox.addItem("Sat");
		dayBox.addItem("Sun");
		dayBox.addItem("Mon");
		dayBox.addItem("Tue");
		dayBox.addItem("Wed");
		dayBox.addItem("Thu");
		dayBox.addItem("Fri");

		dceEdit.setClickCountToStart(2);
		dayColumn.setCellEditor(dceEdit);

		jspScheduleTable = new JScrollPane(jtSchedules);

		jpViewSchedules.add(jspScheduleTable, BorderLayout.CENTER);
		jpViewSchedules.add(jbDeleteSchedule, BorderLayout.SOUTH);

		jpAddSchedule = new JPanel(new BorderLayout());

		jpScheduleArea = new JPanel();

		jpImmediateCleaning = new JPanel(new GridBagLayout());

		jpDaysList = new JPanel(new GridLayout(9, 1));

		jspDaysList = new JScrollPane(jpDaysList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.frame = this;
		getContentPane().setLayout(new BorderLayout());

		jpZoom.add(jbIn);
		jpZoom.add(jsZoom);
		jpZoom.add(jbOut);

		jpWest.add(jpZoom);
		jpWest.add(jbResetSelection);

		jpScheduleArea.add(new JLabel("for"));
		jpScheduleArea.add(jcbSelectionSchedule);

		jpDaysList.setBorder(BorderFactory.createEtchedBorder(Color.black,
				Color.gray));

		jpDaysList.add(jpTime);
		// jpDaysList.add(new JLabel("for"));
		jpDaysList.add(jpScheduleArea);
		jpDaysList.add(jcbMonday);
		jpDaysList.add(jcbTuesday);
		jpDaysList.add(jcbWednesday);
		jpDaysList.add(jcbThursday);
		jpDaysList.add(jcbFriday);
		jpDaysList.add(jcbSaturday);
		jpDaysList.add(jcbSunday);

		jpDaysList.setBackground(new Color(174, 174, 166));

		jpSouth.add(new JLabel(""));

		jpNorth.add(jlTime, BorderLayout.WEST);

		jpNorth.add(jcbControls, BorderLayout.EAST);

		GridBagConstraints c = new GridBagConstraints();
		// c.anchor = GridBagConstraints.WEST;
		c.gridy = 0;
		c.gridx = GridBagConstraints.RELATIVE;
		c.insets = new Insets(10, 5, 10, 10);
		jpImmediateCleaning.add(jcbSelectionCleaning, c);

		jpImmediateCleaning.add(new JLabel("area"));

		c.gridy = 1;

		jpImmediateCleaning.add(jbStart, c);

		jtpSchedule.addTab("Add", jpDaysList);
		jtpSchedule.addTab("View", jpViewSchedules);

		jtpSchedule.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		jtpSchedule.setMnemonicAt(0, KeyEvent.VK_1);

		JPanel jpLegend = new JPanel(new GridLayout(8, 2));
		final Border border = LineBorder.createGrayLineBorder();

		jlFirstInterval = new JLabel("5 min");
		jlSecondInterval = new JLabel("10 min");
		jlThirdInterval = new JLabel("15 min");
		jlFourthInterval = new JLabel("20 min");
		jlFifthInterval = new JLabel("25 min");
		jlSixthInterval = new JLabel("30 min");

		class mapShades extends JLabel {

			mapShades(String label, int rValue, int gValue, int bValue) {
				this.setText(label);
				this.setOpaque(true);
				Color bgColour = new Color(rValue, gValue, bValue);
				this.setBackground(bgColour);
				this.setPreferredSize(new Dimension(15, 15));
				this.setBorder(border);
			}
		}

		mapShades jlrobotPosition = new mapShades("", 255, 0, 0);
		mapShades jlObstacle = new mapShades("", 0, 0, 0);
		mapShades jlFirstShade = new mapShades("", 255, 255, 255);
		mapShades jlSecondShade = new mapShades("", 255, 255, 204);
		mapShades jlThirdShade = new mapShades("", 255, 255, 153);
		mapShades jlFourthShade = new mapShades("", 255, 255, 102);
		mapShades jlFifthShade = new mapShades("", 255, 255, 51);
		mapShades jlSixthShade = new mapShades("", 255, 255, 0);

		JPanel jpRobotPositionLegend = new JPanel(new FlowLayout());
		jpRobotPositionLegend.add(jlrobotPosition);

		JPanel jpObstacleLegend = new JPanel(new FlowLayout());
		jpObstacleLegend.add(jlObstacle);

		JPanel jpFirstLayout = new JPanel(new FlowLayout());
		jpFirstLayout.add(jlFirstShade);

		JPanel jpSecondLayout = new JPanel(new FlowLayout());
		jpSecondLayout.add(jlSecondShade);

		JPanel jpThirdLayout = new JPanel(new FlowLayout());
		jpThirdLayout.add(jlThirdShade);

		JPanel jpFourthLayout = new JPanel(new FlowLayout());
		jpFourthLayout.add(jlFourthShade);

		JPanel jpFifthLayout = new JPanel(new FlowLayout());
		jpFifthLayout.add(jlFifthShade);

		JPanel jpSixthLayout = new JPanel(new FlowLayout());
		jpSixthLayout.add(jlSixthShade);

		jpLegend.add(jpRobotPositionLegend);
		jpLegend.add(new JLabel("Robot"));

		jpLegend.add(jpObstacleLegend);
		jpLegend.add(new JLabel("Obstacle"));

		jpLegend.add(jpFirstLayout);
		jpLegend.add(jlFirstInterval);
		jpLegend.add(jpSecondLayout);
		jpLegend.add(jlSecondInterval);
		jpLegend.add(jpThirdLayout);
		jpLegend.add(jlThirdInterval);
		jpLegend.add(jpFourthLayout);
		jpLegend.add(jlFourthInterval);
		jpLegend.add(jpFifthLayout);
		jpLegend.add(jlFifthInterval);
		jpLegend.add(jpSixthLayout);
		jpLegend.add(jlSixthInterval);

		jpColorReports.add(jpLegend, BorderLayout.SOUTH);
		JPanel jpFilter = new JPanel(new GridLayout(3, 3));
		jpColorReports.add(jpFilter, BorderLayout.NORTH);
		jpFilter.add(jcbColorDay);
		jpFilter.add(jcbColorHr);
		jpFilter.add(jcbColorMin);

		jpFilter.add(new JLabel("Day"));
		jpFilter.add(new JLabel("Hr"));
		jpFilter.add(new JLabel("Min"));

		jpFilter.add(new JLabel(""));
		jpFilter.add(jbSetColorInterval);

		jpTime.add(new JLabel("Time"));
		jpTime.add(jcbHour);
		jpTime.add(jcbMinute);
		jpTime.add(new JLabel(""));
		jpTime.add(new JLabel(""));
		jpTime.add(jbAddSchedule);
		jtpCleaning = new JTabbedPane();
		jtpCleaning.add(jpImmediateCleaning, "Immediate Cleaning");
		jpEast.add(jtpCleaning, BorderLayout.SOUTH);

		add(jpSouth, BorderLayout.SOUTH);
		add(jpNorth, BorderLayout.NORTH);
		add(jspMap, BorderLayout.CENTER);
		add(jpWest, BorderLayout.WEST);
		add(jpEast, BorderLayout.EAST);

	}
}
