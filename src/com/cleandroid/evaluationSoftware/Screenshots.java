package com.cleandroid.evaluationSoftware;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


/**
 * take a screen shot of the evalutaion GUI and save it it a folder
 * @author yu song & jawad rehman
 *
 */
public class Screenshots implements Runnable {
	EvaluationGUI main;
	int screenshotCounter;
	String location;
	/**
	 * 
	 * @param main
	 */
	public Screenshots(EvaluationGUI main) {
		this.main = main;
		
		screenshotCounter = 0;

	}
	/**
	 * take a screent shot of evaluation GUI and save as jpg image to a selected folder
	 */
	public void run() {
		
		while (true) {
			
			if (main.getScreenshot()) {
				location = main.getLocationPath();
				System.out.println("done at " + location);
				JFrame win = (JFrame) SwingUtilities.getWindowAncestor(main
						.getMap());
				Dimension size = win.getSize();
				BufferedImage image = (BufferedImage) win.createImage(
						size.width, size.height);
				Graphics g = image.getGraphics();
				win.paint(g);
				g.dispose();
				

				try {
					ImageIO.write(image, "jpg", new File(location + "/image"
							+ screenshotCounter + ".jpg"));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				screenshotCounter++;

				try {

					Thread.sleep(main.jsInterval.getValue() * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
