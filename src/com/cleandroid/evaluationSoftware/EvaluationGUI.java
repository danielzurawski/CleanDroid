package com.cleandroid.evaluationSoftware;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.ui.RefineryUtilities;

import com.cleandroid.SaveLoad;
import com.cleandroid.Slam2;
import com.cleandroid.occupancyGridMap.Point;
/**
 * create the evaluation GUI for developers to evaluate the efficiency of the robot
 * @author Song
 *
 */
public class EvaluationGUI extends JFrame {

	// GraphicsCanvas a;

	// panel on which map is drawn
	EvaulationMapPanel gpMap;
	JScrollPane jspA;
	BarChart barChart;

	// zoom is to be used only if canvas is continued to work with
	// boolean zoom;
	// sensor determines if the sensor data is to be shown or not
	boolean sensor;
	boolean screenshot;
	boolean robotPos;
	boolean started;

	// panels to lay out the swing components
	JPanel jpSouth;
	JPanel jpWest;
	JPanel jpEast;
	JPanel jpScreenshotsControl;
	JPanel jpZoom;
	JPanel jpNorth;

	JPanel jpInfo;
	JPanel jpSensor;
	JPanel jpSensorInfo;
	JPanel jpRobotPos;
	JPanel jpDataReady;

	// the zoom buttons
	JButton jbIn;
	JButton jbOut;
	JButton jbReset;

	JButton jbScreenshotLocation;

	// button to start the process
	JCheckBox jbStart;

	// button to toggle if
	JCheckBox jbSensor;

	JButton jbRobotPos;
	JButton jbReports;

	Slam2 main;
	String dateNow;

	JSlider jsZoom;
	JSlider jsInterval;
	JTextField jtfLocation;

	Thread execute;
	Thread dataReady;

	Point panelSize;
	// Point currentPoint;

	long startTime;

	int screenshotCounter;

	String location;

	double area;
	double zoomValue;

	JLabel jlTimer;
	JLabel jlYaw;

	ArrayList<JLabel> jlSensor;
	JLabel jlArea;
	JLabel jlCoordinate;
	JLabel jlRobotPos;

	JRadioButton jlPos2D;

	JCheckBox jcbScreenshots;

	ImageIcon on;
	ImageIcon off;
	ImageIcon zoomIn;
	ImageIcon zoomOut;
	ImageIcon yellow;
	ImageIcon red;
	ImageIcon locationImage;

	Statistics statsGraphs;

	Screenshots screenshotShot;

	Thread screenshotThread;

	File directory = new File(".");

	/**
	 * constructor, no input parameters intialize JFrame
	 */
	public EvaluationGUI(Slam2 s) {

		super("Evaluation Tool");
		this.main = s;
		panelSize = new Point(950, 650);

		setSize((int) panelSize.getX() + 2, (int) panelSize.getY() + 29 + 46);
		screenshotShot = new Screenshots(this);

		sensor = false;
		screenshot = false;
		robotPos = true;

		off = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/evaluationSoftware/off.png");
		on = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/evaluationSoftware/on.png");

		zoomIn = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/evaluationSoftware/zoomIn.png");

		zoomOut = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/evaluationSoftware/zoomOut.png");

		yellow = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/evaluationSoftware/yellow.png");

		red = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/evaluationSoftware/red.png");

		locationImage = new ImageIcon(directory.getAbsolutePath()
				+ "/com/cleandroid/evaluationSoftware/open_folder.png");
		// a = new GraphicsCanvas(panelSize.getX(), panelSize.getY(), s);

		gpMap = new EvaulationMapPanel(panelSize.getX(), panelSize.getY(), this);

		jspA = new JScrollPane(gpMap, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		jcbScreenshots = new JCheckBox("Screenshots", off);
		jcbScreenshots.setHorizontalTextPosition(SwingConstants.LEFT);
		jcbScreenshots.setSelected(false);

		jbRobotPos = new JButton(on);

		jlYaw = new JLabel("Yaw ");
		jlArea = new JLabel("Area ");
		jlCoordinate = new JLabel("<html>X  " + "<br> Y </html>");
		jlRobotPos = new JLabel("<html> Robot <br> Position</html>");

		jlPos2D = new JRadioButton("<html> Pos2D <br> data</html> ", red);
		jlPos2D.setHorizontalTextPosition(SwingConstants.LEFT);

		jsInterval = new JSlider(5, 25, 15);
		jsInterval.setMajorTickSpacing(10);
		jsInterval.setMinorTickSpacing(1);
		jsInterval.setPaintTicks(true);
		jsInterval.setPaintLabels(true);

		jbScreenshotLocation = new JButton(locationImage);
		jbScreenshotLocation.setBounds(0, 0, 15, 15);

		jtfLocation = new JTextField();

		jbReports = new JButton("Reports");

		jbIn = new JButton();
		jbIn.setIcon(zoomIn);

		jbOut = new JButton();
		jbOut.setIcon(zoomOut);

		jbOut.setEnabled(false);

		jbStart = new JCheckBox("Robot");
		jbStart.setIcon(off);
		jbStart.setHorizontalTextPosition(SwingConstants.LEFT);

		final String st = "<html>" + "Sensor" + "<br>" + "Reading </html>";
		jbSensor = new JCheckBox(st, off);
		jbSensor.setHorizontalTextPosition(SwingConstants.LEFT);
		zoomValue = 1;

		jsZoom = new JSlider(JSlider.VERTICAL);
		jsZoom.setValue(0);

		jlTimer = new JLabel("Time " + 0);

		addActionListeners();
		jlSensor = new ArrayList<JLabel>();

		jpNorth = new JPanel(new BorderLayout());
		jpSouth = new JPanel(new BorderLayout());
		jpWest = new JPanel();
		jpWest.setLayout(null);
		jpWest.setPreferredSize(new Dimension((int) (this.getWidth() * 0.08),
				(int) (this.getHeight())));
		jpEast = new JPanel(new BorderLayout());
		jpEast.setPreferredSize(new Dimension(70, 70));

		jpScreenshotsControl = new JPanel(new FlowLayout());
		jpSensorInfo = new JPanel(new GridLayout(15, 1));
		jpInfo = new JPanel(new GridLayout(3, 1));
		jpSensor = new JPanel(new GridLayout(2, 1));
		jpRobotPos = new JPanel(new GridLayout(3, 1));
		jpZoom = new JPanel();
		jpZoom.setLayout(null);
		jpDataReady = new JPanel(new GridLayout(2, 1));

		jpSensorInfo.setBounds(0, 0, 150, 25);

		jpZoom.setBounds(55, 15, 23, 252);

		jbIn.setBounds(2, 2, 12, 12);
		jsZoom.setBounds(2, 16, 15, 222);
		jbOut.setBounds(3, 238, 12, 12);

		jlRobotPos.setBounds(10, 310, 50, 40);
		jlTimer.setBounds(10, 350, 50, 40);

		jpRobotPos.setBounds(7, 400, 62, 80);

		jpDataReady.setBounds(7, 500, 62, 50);

		jpRobotPos.setBackground(Color.WHITE);
		jpSensorInfo.setBackground(Color.WHITE);
		jpZoom.setBackground(Color.WHITE);

		location = directory.getAbsolutePath();
		jtfLocation.setText(location);

		jpZoom.add(jbIn);
		jpZoom.add(jsZoom);
		jpZoom.add(jbOut);

		jpDataReady.add(jlPos2D);

		jpScreenshotsControl.add(jtfLocation);
		jpScreenshotsControl.add(jbScreenshotLocation);
		jpScreenshotsControl.add(jsInterval);

		jpSensor.add(jbSensor);

		jpInfo.add(jpSensor);

		jpRobotPos.add(jlArea);
		jpRobotPos.add(jlCoordinate);
		jpRobotPos.add(jlYaw);

		// jpSouth.add(new JLabel(" "), BorderLayout.WEST);
		jpSouth.add(jcbScreenshots, BorderLayout.WEST);
		jpSouth.add(jpScreenshotsControl, BorderLayout.CENTER);
		jpSouth.add(jbStart, BorderLayout.EAST);

		jpSensor.add(new JLabel(st));

		jpNorth.add(jbSensor, BorderLayout.EAST);
		jpNorth.add(jbReports, BorderLayout.WEST);

		jpEast.add(new JLabel(" "), BorderLayout.NORTH);
		jpEast.add(jpSensorInfo, BorderLayout.CENTER);
		jpEast.add(new JLabel(" "), BorderLayout.SOUTH);

		jpWest.add(jpRobotPos);
		jpWest.add(jpDataReady);
		jpWest.add(jpZoom);
		jpWest.add(jlTimer);

		screenshotThread = new Thread(screenshotShot);

		// Thread screenshot = new Thread(threadScreenshot);
		// screenshot.start();

		for (int i = 0; i < 15; i++) {
			jlSensor.add(new JLabel("" + i));
			jpSensorInfo.add(jlSensor.get(jlSensor.size() - 1));
		}

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(jpNorth, BorderLayout.NORTH);
		getContentPane().add(jspA, BorderLayout.CENTER);
		getContentPane().add(jpSouth, BorderLayout.SOUTH);
		getContentPane().add(jpWest, BorderLayout.WEST);
		// getContentPane().add(jpEast, BorderLayout.EAST);
		getContentPane().add(jpSouth, BorderLayout.SOUTH);

		/**
		 * thread which updates the data on the screen starts when start button
		 * is pressed in UI
		 */

		execute = new Thread(new Runnable() {
			public void run() {

				while (true) {

					while (!getSlam2().getPos2D().isDataReady())
						;

					gpMap.repaint();

					// a.repaint();
					if (jbSensor.isSelected()) {
						// System.out.println("selected");
						getSensorData();
					}
					getTime();

					if (robotPos)
						getRobotPos();

					/*
					 * System.out.println("horizontal = " +
					 * jspA.getHorizontalScrollBar().getValue() /
					 * jspA.getHorizontalScrollBar().getMaximum());
					 * 
					 * System.out.println("vertical   = " +
					 * jspA.getVerticalScrollBar().getValue() /
					 * jspA.getVerticalScrollBar().getMaximum());
					 */

					try {
						Thread.sleep(1000);
					} catch (Exception e) {

					}
				}
			}
		});
		execute.start();

		dataReady = new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					if (getSlam2().getPos2D().isDataReady()) {
						jlPos2D.setIcon(yellow);
					} else
						jlPos2D.setIcon(red);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {

					}
				}
			}
		});

	}
	/**
	 *  return the map panel to evaluation tool
	 * @return
	 */
	public EvaulationMapPanel getMap() {
		return gpMap;
	}
	/**
	 * get the current time
	 * @return
	 */
	public long getTime() {

		long current = (System.currentTimeMillis() - startTime) / 1000;

		jlTimer.setText("<html>" + "Time " + "<br>" + current);
		return current;
	}
	/**
	 *  get the current time for statistics, if robot is not started, return 0.
	 * @return
	 */
	public long getStatisticsTime() {
		if (!started) {
			return 0;
		} else {
			return getTime();
		}
	}
	/**
	 * set the location folder for saving screen shot
	 * @param location : folder path
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	/**
	 *  get the location folder for saving screen shot
	 * @return : the fold path
	 */
	public String getLocationPath() {
		return location;
	}
	//TODO COMMENT
	/**
	 * 
	 */
	public void getRobotPos() {
		jlYaw.setText("Yaw "
				+ (double) Math.round(getSlam2().getPos2D().getYaw() * 100)
				/ 100);
		jlCoordinate.setText("<html> X = " + getSlam2().getCurrentIndexX()
				+ "<br>" + "Y = " + getSlam2().getCurrentIndexY() + "</html>");
	}
	//TODO COMMENT
	/**
	 * 
	 * @param zoom
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
	//TODO COMMENT
	/**
	 * 
	 */
	public void getSensorData() {

		double[] sonarArray = getSlam2().getSonar().getData().getRanges();
		for (int i = 0; i < sonarArray.length; i++) {
			jlSensor.get(i).setText(
					" " + i + " " + (double) Math.round(sonarArray[i] * 100)
							/ 100);
		}

	}
	/**
	 *  return slam2 object
	 * @return
	 */
	public Slam2 getSlam2() {
		return main;

	}
	//TODO COMMENT
	/**
	 * 
	 * @return
	 */
	public Point getPanelPoint() {
		return panelSize;
	}
	//TODO COMMENT
	/**
	 * 
	 * @return
	 */
	public boolean getScreenshot() {
		return jcbScreenshots.isSelected();
	}
	/**
	 * add action listeners to each buttons
	 */
	public void addActionListeners() {

		jbReports.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

				Calendar currentDate = Calendar.getInstance();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd");
				dateNow = formatter.format(currentDate.getTime());
				barChart = new BarChart("Statistices", getStatisticsTime(),
						gpMap.getCellsVisited(), dateNow);
				barChart.pack();
				RefineryUtilities.centerFrameOnScreen(barChart);
				barChart.setVisible(true);
			}
		});
		jbRobotPos.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (!robotPos) {
					robotPos = true;
					jpWest.add(jpRobotPos);
					jbRobotPos.setIcon(on);

				} else {
					robotPos = false;
					jpWest.remove(jpRobotPos);
					jbRobotPos.setIcon(off);

				}

				jpWest.repaint();
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

		jbReset = new JButton("Reset");
		jbReset.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setZoom(1);
			}

		});

		jbSensor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jbSensor.isSelected()) {
					jbSensor.setIcon(on);
					add(jpEast, BorderLayout.EAST);

				} else {

					jbSensor.setIcon(off);
					remove(jpEast);

				}
				// revalidate();
				repaint();
			}
		});

		jbStart.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				startTime = System.currentTimeMillis();

				if (jbStart.isSelected()) {
					if (!main.isAlive()) {
						main.start();
						main.setStarted(true);
						started = true;
					}

					jbStart.setIcon(on);
					main.setStop(false);

				} else {

					main.setStop(true);

					jbStart.setIcon(off);

					// System.out.println(a.check());
				}
			}

		});

		jsZoom.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub

				setZoom(((double) (jsZoom.getValue()) / 100) + 1);
			}

		});

		jcbScreenshots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jcbScreenshots.isSelected()) {
					jcbScreenshots.setIcon(on);
					if (!screenshotThread.isAlive()){
						screenshotThread = new Thread(screenshotShot);
						screenshotThread.start();
					
					}

					jpSouth.repaint();
				} else {
					jcbScreenshots.setIcon(off);

					jpSouth.repaint();
				}
				// revalidate();
			}
		});

		jbScreenshotLocation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new java.io.File("."));
				fileChooser.setDialogTitle("ScreenShot Location");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);

				if (fileChooser.showOpenDialog(rootPane) == JFileChooser.APPROVE_OPTION) {
					location = fileChooser.getSelectedFile().getAbsolutePath();
					jtfLocation.setText(location);
					System.out.print(location);
				} else {
					JOptionPane.showMessageDialog(rootPane,
							"No directories is selected.");
				}
			}
		});

	}

	public JScrollPane getJsp() {
		return jspA;
	}

}
