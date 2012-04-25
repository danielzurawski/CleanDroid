package com.cleandroid.consumerSoftware;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import com.cleandroid.Slam2;
import com.cleandroid.occupancyGridMap.GridCell;
import com.cleandroid.occupancyGridMap.GridMap;
import com.cleandroid.occupancyGridMap.Point;

/**
 * This class build the map panel in consumer GUI tool to display map
 * 
 * @author Team BLUE
 * 
 */
public class ConsumerMapPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private Rectangle currentRect = null;
	private Rectangle rectToDraw = null;
	private Rectangle previousRectDrawn = new Rectangle();

	Rectangle totalRepaint;

	// used to set the percentage; allows the map to be zoomed
	double percentageZoom;

	// the current location of the currentcell
	Point currentCell;

	// the point representing the top left corner of selection
	Point firstSelectedPoint;
	// the point representing bottom right of the selection
	Point secondSelectedPoint;

	int refreshInterval = (5 * 60 * 1000);
	/**
	 * The number of indices which need to be moved for the panel to display the
	 * map in the center
	 */
	int moveX;
	int moveY;

	int xCoordinate;
	int yCoordiante;

	int x;
	int y;
	// to be used to draw the selected cells as blue
	boolean drawSelected;

	boolean released;

	Slam2 main;

	ConsumerMapPanel panel;

	ConsumerGUI frame;

	long currentTime;

	int cellType;

	GridCell cell;

	Color timeColor;

	GridMap map;

	private long reportInterval;

	LinkedList<GridCell> selectedCellsList;

	boolean drawHover;

	private double cellSize;

	private Point one;
	private Point two;

	/**
	 * 
	 * @param a
	 *            the width of the panel
	 * @param b
	 *            the height of the panel
	 * @param s
	 *            the slam object to be used
	 */

	public ConsumerMapPanel(double a, double b, ConsumerGUI consumerUI) {

		setSize((int) a, (int) b);
		percentageZoom = 1;

		frame = consumerUI;

		frame.panelSize.setX(50 * calculateCellSize());
		frame.panelSize.setY(50 * calculateCellSize());

		main = frame.getSlam2();

		map = main.getMap();

		currentCell = new Point(getSlam2().getCurrentIndexX(), getSlam2()
				.getCurrentIndexX());

		if (map.getMaxPoint() == null) {
			map.setMaxPoint(new Point(currentCell.getX() + 1, currentCell
					.getY() + 1));
		}

		if (map.getMinPoint() == null) {
			map.setMinPoint(new Point(currentCell.getX() - 1, currentCell
					.getY() - 1));
		}

		if (map.getMapPoint() == null) {
			map.setMapPoint(new Point(this.getWidth() / calculateCellSize(),
					this.getHeight() / calculateCellSize()));
		}

		totalRepaint = null;

		MyListener myListener = new MyListener();
		addMouseListener(myListener);
		addMouseMotionListener(myListener);
		setAutoscrolls(true);
		mouseListener myMouseWheel = new mouseListener();
		addMouseWheelListener(myMouseWheel);
		drawSelected = false;

		setAutoscrolls(true);
		panel = this;
		released = false;
		setToolTipText("Drag and Select for selective cleaning");
		reportInterval = 10;

		selectedCellsList = new LinkedList<GridCell>();

		drawHover = false;

		moveX = 0;
		moveY = 0;
	}

	/**
	 * Method to draw the map
	 **/
	public void paint(Graphics g) {

		super.paint(g);

		super.setBackground(new Color(172, 158, 158));

		moveX = (int) ((int) (map.getMapPoint().getX() - (map.getMaxPoint()
				.getX() - map.getMinPoint().getX())) / 2);
		moveY = (int) ((int) (map.getMapPoint().getY() - (map.getMaxPoint()
				.getY() - map.getMinPoint().getY())) / 2);

		resetSize();
		getCurrentCell();
		refreshMaxMin();
		gridDraw(g);
		if (drawSelected)
			drawSelection(g);

		if (drawHover) {
			drawHoverSelection(g);
		}

	}

	/**
	 * Method which retains the size of the panel
	 */

	public void resetSize() {

		if (this.getWidth() < frame.panelSize.getX())
			xCoordinate = (int) frame.panelSize.getX();
		else
			xCoordinate = this.getWidth();

		if (this.getHeight() < frame.panelSize.getY())
			yCoordiante = (int) frame.panelSize.getY();
		else

			yCoordiante = this.getHeight();

		Dimension resize = new Dimension(xCoordinate, yCoordiante);

		setPreferredSize(resize);
		repaint();

	}

	/**
	 * 
	 * @param g
	 *            the graphics object initialised in paint method which is used
	 *            to draw on the panel
	 * 
	 * 
	 *            loops from the minimum cell (map.getMinPoint()) explored of
	 *            the environment to the maximum cell (map.getMaxPoint())
	 */
	public void gridDraw(Graphics g) {

		x = 0;
		y = 0;

		cellSize = calculateCellSize();

		for (int i = (int) map.getMinPoint().getX(); i <= getSlam2().getMap()
				.getMaxPoint().getX(); i++) {
			x++;
			y = 0;
			for (int j = (int) map.getMinPoint().getY(); j <= getSlam2()
					.getMap().getMaxPoint().getY(); j++) {
				y++;
				cellDraw(i, j, g, x, y, 0);
			}

		}
		try {
			Thread.sleep(25);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * draws a cell according to its type within the map
	 * 
	 * @param j
	 *            the horizontal position of the cell in the grid
	 * @param i
	 *            the vertical position of the cell in the grid
	 * @param g
	 *            the graphics object --- initialised by paint method
	 * 
	 * 
	 */
	public void cellDraw(int j, int i, Graphics g, int m, int n, int value) {
		try {
			cell = map.getGrid()[i][j];
			cellType = cell.getType();
		} catch (Exception e) {

		}

		// only draw if celltype has a value other than 3
		if (cellType != 3) {

			// if cell is a visited draw it accroding to the last time cleaned
			if (cellType == 1) {

				if (cell.getLastCleaned() == null) {
					Date date = new Date();
					cell.setLastCleaned(date);
				}

				currentTime = (System.currentTimeMillis() - cell
						.getLastCleaned().getTime()) / (5 * 60 * 60);

				if (currentTime / reportInterval > 6) {
					timeColor = new Color(255, 255, 0);
				} else if (currentTime / reportInterval > 5) {
					timeColor = new Color(255, 255, 51);
				} else if (currentTime / reportInterval > 4) {
					timeColor = new Color(255, 255, 102);
				} else if (currentTime / reportInterval > 3) {
					timeColor = new Color(255, 255, 153);
				} else if (currentTime / reportInterval > 2) {
					timeColor = new Color(255, 255, 204);
				} else if (currentTime / reportInterval >= 0) {
					timeColor = new Color(255, 255, 255);
				}

				g.setColor(timeColor);

			}

			// if cell is marked as an obstacle draw it as black

			else if (cellType == -1) {

				g.setColor(Color.BLACK);

			}

			// if cell marks the location of the robot draw it as red

			else if (cellType == 2) {

				g.setColor(Color.RED);

			}

			// if cell is marked as an unvisited cell it draws it as yellow

			else if (cellType == 0) {
				timeColor = Color.YELLOW;
				
				/*
				 *changes colour if the cell has been visited before according to the interval set
				 */
				if (cell.getLastCleaned() != null) {
					currentTime = (System.currentTimeMillis() - cell
							.getLastCleaned().getTime()) / (5 * 60 * 60);

					if (currentTime / reportInterval > 6) {
						timeColor = new Color(255, 255, 0);
					} else if (currentTime / reportInterval > 5) {
						timeColor = new Color(255, 255, 51);
					} else if (currentTime / reportInterval > 4) {
						timeColor = new Color(255, 255, 102);
					} else if (currentTime / reportInterval > 3) {
						timeColor = new Color(255, 255, 153);
					} else if (currentTime / reportInterval > 2) {
						timeColor = new Color(255, 255, 204);
					} else if (currentTime / reportInterval >= 0) {
						timeColor = new Color(255, 255, 255);
					}
				}

				g.setColor(timeColor);

			}

			// if cell is marked as a charging station it draws it with cyan
			else if (cellType == 7) {

				g.setColor(Color.CYAN);
			}

			// if cell is selected draw it as blue
			if (cell.isSelected()) {
				g.setColor(Color.BLUE);

			}

			if (value > 0) {
				g.setColor(Color.BLUE);
			}

			g.fillRect((((moveX + m) * (int) cellSize)),
					(((moveY + n) * (int) cellSize)), (int) cellSize,
					(int) cellSize);

			g.setColor(new Color(172, 158, 158));
			g.drawRect((((moveX + m) * (int) cellSize)),
					(((moveY + n) * (int) cellSize)), (int) cellSize,
					(int) cellSize);
		}

	}

	public void drawSelection(Graphics g) {

		g.setColor(Color.BLACK);

		if (rectToDraw != null) {
			// g.clearRect(x, y, width, height)
			g.drawRect(rectToDraw.x, rectToDraw.y, rectToDraw.width - 1,
					rectToDraw.height - 1);

		}

		if (totalRepaint != null) {
			// System.out.println("not null");
			g.drawRect(totalRepaint.x, totalRepaint.y, totalRepaint.width - 1,
					totalRepaint.height - 1);

		}

	}

	public void drawHoverSelection(Graphics g) {

		int minX = (int) map.getMinPoint().getX();
		int minY = (int) map.getMinPoint().getY();
		if (one != null && two != null)
			for (int i = (int) one.getX(); i < two.getX(); i++) {
				int a = i - minX;

				for (int j = (int) one.getY(); j < two.getY(); j++) {
					int b = j - minY;
					cellDraw(i, j, g, b, a, 1);

				}
			}
	}

	public double round1(double shrink) {
		double result = (double) (Math.round(shrink * 1000)) / 1000;

		return result;
	}

	public void setHoverSelection(LinkedList<GridCell> selection) {
		drawHover = true;
		this.selectedCellsList = selection;
	}

	public LinkedList<GridCell> getHoverSelection() {
		return selectedCellsList;
	}

	/**
	 * 
	 * @return the calculated width of each cell
	 */

	public double calculateCellSize() {

		return Math.round(14 * percentageZoom);

	}

	/**
	 * Returns map refresh interval value.
	 * 
	 * @return the value of the refresh interval in milliseconds.
	 */
	public int getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * Sets the map refresh interval value.
	 * 
	 * @param interval
	 *            specifies the map refresh interval value in milliseconds.
	 */
	public void setRefreshInterval(int interval) {
		this.refreshInterval = interval;
	}
	

	/**
	 * 	
	 * @param px :- the pixels determined from the selection
	 * @return :- the x index of the selected cell
	 */

	public int getSelectedCellX(int px) {
		
		int cellX = (int) (map.getMinPoint().getX()
				+ (int) Math.floor((px / calculateCellSize())) - moveX);
		System.out.println("the selected x is " + cellX);
		return cellX - 1;

	}

	/**
	 * 
	 * @param px :- the pixels determined from the selection
	 * @return :- the y index of the selected cell
	 */
	public int getSelectedCellY(int px) {
		
		int cellY = (int) (map.getMinPoint().getY()
				+ (int) Math.floor((px / calculateCellSize())) - moveY);
		return cellY - 1;

	}

	/**
	 * 
	 * @param shrink
	 *            the percentage by which to zoom the map
	 */
	public void setShrink(double shrink) {

		if (shrink == 1)
			percentageZoom = 1;

		else
			percentageZoom = round1(shrink) * percentageZoom;

		resizePanel(round1(shrink));

		this.repaint();
	}

	/**
	 * 
	 * @return released :- boolean set to true if mouse has been released from selection
	 */
	
	public boolean isReleased() {
		return released;
	}
	/**
	 * 
	 * @param zoomPercentage percentage by which panel should be resized
	 */

	public void resizePanel(double shrink) {

		int x;
		int y;
		System.out.println(shrink);
		if (shrink != 1) {
			x = (int) (50 * calculateCellSize());
			y = (int) (50 * calculateCellSize());
		} else {
			x = (int) frame.panelSize.getX();
			y = (int) frame.panelSize.getY();
		}

		Dimension resize = new Dimension(x, y);

		setPreferredSize(resize);
		revalidate();
	}

	public Slam2 getSlam2() {
		return main;
	}

	public void getCurrentCell() {
		currentCell = new Point(getSlam2().getCurrentIndexX(), getSlam2()
				.getCurrentIndexY());

	}

	/**
	 * 
	 * @return : return the first selected point : get the index of the top left selected cell
	 */
	public Point getFirstSelectedPoint() {
		return firstSelectedPoint;
	}

	/**
	 * 
	 * @return: get the second selected point: get index of the bottom right selected cell
	 */
	public Point getSecondSelectedPoint() {
		return secondSelectedPoint;
	}

	/**
	 * 
	 * @return
	 */
	public ConsumerGUI getFrame() {
		return frame;
	}

	/**
	 * 
	 * @param frame
	 */
	public void setFrame(ConsumerGUI frame) {
		this.frame = frame;
	}
	
	/**
	 * refreshed the max and min points for the panel to draw the cells from 
	 */

	public void refreshMaxMin() {
		xCoordinate = (int) currentCell.getX();
		yCoordiante = (int) currentCell.getY();

		if (map.getMapPoint().getY() > map.getMaxPoint().getY()
				- map.getMinPoint().getY()
				&& map.getMapPoint().getX() > getSlam2().getMap().getMaxPoint()
						.getX()
						- map.getMinPoint().getX()) {

			if (yCoordiante - 1 < map.getMinPoint().getY()) {
				map.getMinPoint().setY(yCoordiante - 1);

			}
			if (xCoordinate - 1 < map.getMinPoint().getX()) {
				map.getMinPoint().setX(xCoordinate - 1);
			}

			if (yCoordiante + 1 > map.getMaxPoint().getY()) {

				map.getMaxPoint().setY(yCoordiante + 1);

				// System.out.println("doing it");

			}
			if (xCoordinate + 1 > map.getMaxPoint().getX()) {

				map.getMaxPoint().setX(xCoordinate + 1);
			}

		}

		else {

			if (map.getMapPoint().getX() < map.getGrid()[0].length) {

				setPreferredSize(new Dimension((int) (20 * calculateCellSize())
						+ this.getWidth(), this.getHeight()));

				this.repaint();

				map.getMapPoint().setX(map.getMapPoint().getX() + 20);

				frame.panelSize.setX(this.getWidth()
						+ (int) (20 * calculateCellSize()));

			}

			if (map.getMapPoint().getY() < map.getGrid().length) {

				frame.panelSize.setY(this.getHeight()
						+ (int) (20 * calculateCellSize()));
				setPreferredSize(new Dimension(this.getWidth(),
						this.getHeight() + (int) (20 * calculateCellSize())));

				map.getMapPoint().setY(map.getMapPoint().getY() + 10);
				this.repaint();

			}

		}

	}

	private class mouseListener implements MouseWheelListener {

		/**
		 * 
		 */
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			int notches = e.getWheelRotation();

			getFrame()
					.getJspMap()
					.getVerticalScrollBar()
					.setValue(
							getFrame().getJspMap().getVerticalScrollBar()
									.getValue()
									+ (notches * 10));

		}

	}

	/**
	 * map selection, drag mouse to select a rectangle area on the map.
	 * 
	 * 
	 */
	private class MyListener extends MouseInputAdapter {
		/**
		 * get the mouse position on the map then update the rectangle width and
		 * height when mouse pressed
		 */
		public void mousePressed(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			currentRect = new Rectangle(x, y, 0, 0);
			updateDrawableRect(getWidth(), getHeight());
			repaint();
		}

		/**
		 * update the rectangle size when mouse dragged
		 */
		public void mouseDragged(MouseEvent e) {
			updateSize(e);
			released = true;

		}

		/**
		 * update the rectangle size when mouse released
		 */
		public void mouseReleased(MouseEvent e) {
			updateSize(e);
			drawSelected = false;
			firstSelectedPoint = new Point(getSelectedCellX(rectToDraw.x),
					getSelectedCellY(rectToDraw.y));
			secondSelectedPoint = new Point(
					getSelectedCellX((int) (rectToDraw.x + rectToDraw.width)),
					getSelectedCellY((int) (rectToDraw.y + rectToDraw.height)));

			if (released & !getSlam2().getAreaToCleanSelected()) {

				getFrame().getSlam2().setSelection(
						(int) getFirstSelectedPoint().getX(),
						(int) getFirstSelectedPoint().getY(),
						(int) getSecondSelectedPoint().getX(),
						(int) getSecondSelectedPoint().getY());

				one = new Point((int) getFirstSelectedPoint().getX(),
						(int) getFirstSelectedPoint().getY());
				two = new Point((int) getSecondSelectedPoint().getX(),
						(int) getSecondSelectedPoint().getY());

			}

			released = false;

		}

		// updateSize method to merge the current rectangle with the previous
		// rectangle
		void updateSize(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			currentRect.setSize(x - currentRect.x, y - currentRect.y);
			updateDrawableRect(getWidth(), getHeight());
			totalRepaint = rectToDraw.union(previousRectDrawn);
			drawSelected = true;
			repaint();
		}
	}

	/**
	 * draw the final selected rectangle
	 * 
	 * @param compWidth
	 * @param compHeight
	 */
	private void updateDrawableRect(int compWidth, int compHeight) {
		int x = currentRect.x;
		int y = currentRect.y;
		int width = currentRect.width;
		int height = currentRect.height;

		// get the right rectangle independent of dragging from left to right or
		// top to bottom
		if (width < 0) {
			width = 0 - width;
			x = x - width + 1;
			if (x < 0) {
				width += x;
				x = 0;
			}
		}
		if (height < 0) {
			height = 0 - height;
			y = y - height + 1;
			if (y < 0) {
				height += y;
				y = 0;
			}
		}

		if ((x + width) > compWidth) {
			width = compWidth - x;
		}
		if ((y + height) > compHeight) {
			height = compHeight - y;
		}

		
		if (rectToDraw != null) {
			previousRectDrawn.setBounds(rectToDraw.x, rectToDraw.y,
					rectToDraw.width, rectToDraw.height);
			rectToDraw.setBounds(x, y, width, height);
		} else {
			rectToDraw = new Rectangle(x, y, width, height);
		}
	}

	public long getReportInterval() {
		return reportInterval;
	}

	public void setReportInterval(long reportInterval) {

		this.reportInterval = reportInterval;
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
