/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cleandroid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import com.cleandroid.consumerSoftware.ConsumerGUI;
import com.cleandroid.evaluationSoftware.EvaluationGUI;
import com.cleandroid.occupancyGridMap.GridCell;

/**
 * 
 * @author danielzurawski
 */
public class CleanDroid extends JFrame {

	/**
	 * An instance of Consumer GUI.
	 */
	static ConsumerGUI cGUI;
	
	/**
	 * An instance of the SLAM Slam2 class
	 */
	static Slam2 slamClass;
	
	/**
	 * An instance of the SaveLoad class that provies functionality for serializing and de-serializing files.
	 */
	static SaveLoad saveLoad;
	
	/**
	 * An instance of environment evaluator class for evaluating if the robot is placed in an old or new environment.
	 */
	static EnvironmentEvaluator environmentEvaluator;

	/**
	 * The default look and feel to be used when the program is launched
	 */
	static String LOOKANDFEEL = "SubstanceDustLookAndFeel";

	File directory = new File(".");
	
	static boolean repaint;

	/**
	 * CleanDroid constructor which invokes createGUI() when instantiated. 
	 * It uses SwingUtilities invokeLater for performance reasons. 
	 */
	public CleanDroid() {
		slamClass = new Slam2();
		setLookAndFeel();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {

					createGUI();
					
				} catch (Exception ex) {

				}
			}
		});

	}

	/**
	 * Build the software selection GUI.
	 */
	public void createGUI() {

		File directory = new File(".");
		JButton jbConsumer = new JButton("Consumer");
		JButton jbEvaluation = new JButton("Evaluation");
		final ImageIcon logo = new ImageIcon(directory.getAbsolutePath() + "/com/cleandroid/consumerSoftware/CleanDroid.png");
		JPanel jpSouth = new JPanel(new FlowLayout());getContentPane().setLayout(new BorderLayout());
		JPanel jpCenter = new JPanel(new FlowLayout());
		jpSouth.add(jbConsumer);
		jpSouth.add(jbEvaluation);
		final JLabel jlLoading = new JLabel(logo);
		jpSouth.add(jlLoading);
		add(jpSouth, BorderLayout.SOUTH);
		add(jlLoading,BorderLayout.CENTER);
		
		jbConsumer.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				
				add(jlLoading, BorderLayout.CENTER);
				startEvaulator();
				showConsumer();

			}

		});
		
		jbEvaluation.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				showEvaluation();
			}
		});
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
	    Dimension wielkosc = toolkit.getScreenSize();
	    int screenHeight = wielkosc.height;
	    int screenWidth = wielkosc.width;
	    setSize(screenWidth / 2, screenHeight / 2);
	    setLocation(screenWidth / 4, screenHeight / 4);
		setVisible(true);
		setSize(300, 300);

	}

	/**
	 * Try to set the look and feel using the Substance LAF libararies. 
	 * If they are not available, then try various default Swing LAFs and start the program.
	 */
	private static void setLookAndFeel() {
		String lookAndFeel = null;
		repaint = true;
		if (LOOKANDFEEL != null) {
			if (LOOKANDFEEL.equals("Metal")) {
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();

			} else if (LOOKANDFEEL.equals("System")) {
				lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			} else if (LOOKANDFEEL.equals("Motif")) {
				lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
			} else if (LOOKANDFEEL.equals("GTK")) {
				lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
				repaint = false;
			} else if (LOOKANDFEEL.equals("SubstanceDustLookAndFeel")) {
				lookAndFeel = "org.pushingpixels.substance.api.skin.SubstanceDustLookAndFeel";

			} else {
				System.err
						.println("Unexpected value of LOOKANDFEEL specified: "
								+ LOOKANDFEEL);
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
			}

			try {

				UIManager.setLookAndFeel(lookAndFeel);

				// If the look and feel is set to Metal, then set the theme to
				// Ocean
				if (LOOKANDFEEL.equals("Metal")) {
					MetalLookAndFeel.setCurrentTheme(new OceanTheme());
					UIManager.setLookAndFeel(new MetalLookAndFeel());
				}
			} catch (ClassNotFoundException e) {
				System.err
						.println("Couldn't find class for specified look and feel:"
								+ lookAndFeel);
				System.err
						.println("Did you include the L&F library in the class path?");
				System.err.println("Using the default look and feel.");
				repaint = true;

			} catch (UnsupportedLookAndFeelException e) {
				System.err.println("Can't use the specified look and feel ("
						+ lookAndFeel + ") on this platform.");
				System.err.println("Using the default look and feel.");

				repaint = true;

			} catch (Exception e) {
				System.err.println("Couldn't get specified look and feel ("
						+ lookAndFeel + "), for some reason.");
				System.err.println("Using the default look and feel.");
				repaint = true;
				e.printStackTrace();

			}
		}
	}

	/**
	 * Start the environment evaluator responsible for determining whether the robot is placed in a new environment or not.
	 * Try to deserialize the map, if the map is deserialized then evaluate it and if the robot operates in the old environment, then load the full previous map. 
	 * If it doesn't deserialize or it deserializes, but the environment is new, then it will just carry on with the new environment.  
	 */
	public static void startEvaulator() {
		slamClass.setEvaluatingEnvironment(true);
		saveLoad = new SaveLoad(slamClass);

		if (saveLoad.deserializeMap() == true) {

			slamClass.localize2(saveLoad.getGridMap());

			double tempX = saveLoad.getGridMap().getGrid()[saveLoad
					.getGridMap().getCurrentIndexY()][saveLoad.getGridMap()
					.getCurrentIndexX()].getX() - 125 * 0.25;

			double tempY = saveLoad.getGridMap().getGrid()[saveLoad
					.getGridMap().getCurrentIndexY()][saveLoad.getGridMap()
					.getCurrentIndexX()].getY() + 125 * 0.25;

			for (int i = 0; i < 251; i++) {
				for (int j = 0; j < 251; j++) {
					slamClass.getMap().getGrid()[i][j] = new GridCell(3);
					slamClass.getMap().getGrid()[i][j].setyIndex(i);
					slamClass.getMap().getGrid()[i][j].setxIndex(j);

					slamClass.getMap().getGrid()[i][j].setY(tempY - 0.25 * i);
					slamClass.getMap().getGrid()[i][j].setX(tempX + 0.25 * j);
				}
			}

			slamClass.adjust();

			slamClass.getMap().getGrid()[slamClass.getCurrentIndexY()][slamClass
					.getCurrentIndexX()].setType(7);

			slamClass.setChargingStation(slamClass.getMap().getGrid()[slamClass
					.getCurrentIndexY()][slamClass.getCurrentIndexX()]);

			slamClass.heading = slamClass
					.determineYaw(slamClass.pos2D.getYaw());

			slamClass.markSurrounding();

			slamClass.getMap().getGrid()[slamClass.getCurrentIndexY()][slamClass
					.getCurrentIndexX()].setX(slamClass.pos2D.getX());
			slamClass.getMap().getGrid()[slamClass.getCurrentIndexY()][slamClass
					.getCurrentIndexX()].setY(slamClass.pos2D.getY());

			slamClass.printMap();

			environmentEvaluator = new EnvironmentEvaluator(slamClass,
					saveLoad.getGridMap());

			slamClass.start();

			environmentEvaluator.start();
		} else {
			slamClass.setEvaluatingEnvironment(false);
			System.out.println("Map did no de-serialize");

		}
	}

	/**
	 * This is the showConsumer() method that is run when user chooses to run the Consumer GUI. 
	 * It start the environment evaluator responsible for determining whether the robot is placed in a new environment or not.
	 * After the environment evaluator finishes evaluating the environment and decies which map should be used, 
	 * it launches consumer GUI.   
	 */
	public static void showConsumer() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					
					while (slamClass.isEvaluatingEnvironment()) {
						try {
							Thread.sleep(100);
						} catch (Exception e) {

						}
					}
					cGUI = new ConsumerGUI(slamClass);

					cGUI.createGUI();
					cGUI.execute(repaint);
					cGUI.setVisible(true);
					System.out
							.println("called from main:- the value of repaint is"
									+ repaint);

				} catch (Exception ex) {

				}
			}
		});
	}

	/**
	 * Method instantiates the evaluation GUI. When the user decides to use the Evaluation GUI, this method is invoked.
	 */
	public void showEvaluation() {
		EvaluationGUI evaluationUI = new EvaluationGUI(slamClass);
		evaluationUI.setVisible(true);
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		new CleanDroid();

	}
}
