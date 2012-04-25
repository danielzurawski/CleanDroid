package com.cleandroid.evaluationSoftware;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import com.cleandroid.Slam2;
import com.cleandroid.consumerSoftware.ConsumerGUI;
import com.cleandroid.occupancyGridMap.GridCell;
import com.cleandroid.occupancyGridMap.GridMap;
import com.cleandroid.occupancyGridMap.Point;

public class EvaulationMapPanel extends JPanel {
	/**
	 * second version of graphics panel implemented methods getMaxMin() and
	 * getCurrentCell() to determine where to loop from and to what point.
	 * 
	 * @author Jawad Rehman
	 */
	private static final long serialVersionUID = 1L;

	// used to set the percentage; allows the map to be zoomed
	double percentageZoom;

	// the current location of the currentcell
	Point currentCell;

	// the point representing the top left corner of selection
	Point firstSelectedPoint;
	// the point representing bottom right of the selection
	Point secondSelectedPoint;

	Point mapSize;
	/*
	 * the number of indices which need to be moved for the panel to display the
	 * map in the center
	 */
	int moveX;
	int moveY;

	int x;
	int y;

	private int cellsVisited;

	private int totalCells;
	// to be used to draw the selected cells as blue

	Slam2 main;

	EvaulationMapPanel panel;

	EvaluationGUI frame;

	long currentTime;

	int cellType;

	GridCell cell;

	private double cellSize;



	private Point minPoint;

	private Point mapPoint;

	private int xCoordinate;

	private int yCoordiante;

	private GridMap map;

	/**
	 * 
	 * @param a
	 *            the width of the panel
	 * @param b
	 *            the height of the panel
	 * @param s
	 *            the slam object to be used
	 */

	public EvaulationMapPanel(double a, double b, EvaluationGUI frameUI) {

		setSize((int) a, (int) b);
		percentageZoom = 1;
		// System.out.println(getHeight());

		map = frameUI.getSlam2().getMap();
		
		frame = frameUI;

		main = frame.getSlam2();

		mapSize = new Point(getSlam2().getMap().getGrid()[0].length, getSlam2()
				.getMap().getGrid().length);

		currentCell = new Point(getSlam2().getCurrentIndexX(), getSlam2()
				.getCurrentIndexY());

		totalCells = 0;

		cellsVisited = 0;
		
		
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

		setAutoscrolls(true);

		mouseListener mouse = new mouseListener();
		addMouseWheelListener(mouse);
	}

	/**
	 * method to draw the map
	 **/
	public void paint(Graphics g) {

		super.paintComponent(g);

		this.setBackground(new Color(172, 158, 158));
		moveX = (int) ((int) (map.getMapPoint().getX() - (map.getMaxPoint()
				.getX() - map.getMinPoint().getX())) / 2);
		moveY = (int) ((int) (map.getMapPoint().getY() - (map.getMaxPoint()
				.getY() - map.getMinPoint().getY())) / 2);
		resetSize();
		getCurrentCell();
		refreshMaxMin();
		gridDraw(g);

	}
	/**
	 * reset the size of the map panel
	 */
	public void resetSize() {

		if (this.getWidth() < frame.panelSize.getX())
			x = (int) frame.panelSize.getX();
		else
			x = this.getWidth();

		if (this.getHeight() < frame.panelSize.getY())
			y = (int) frame.panelSize.getY();
		else

			y = this.getHeight();

		Dimension resize = new Dimension(x, y);

		setPreferredSize(resize);
		revalidate();

		revalidate();
	}

	/**
	 * 
	 * @param g
	 *            the graphics object intialized in paint method which is used
	 *            to draw on the panel
	 * 
	 * 
	 *            loops from the minimum cell
	 *            (getSlam2().getMap().getmap.getMinPoint()()) explored of the
	 *            environment to the maximum cell
	 *            (map.getMaxPoint())
	 */

	public void gridDraw(Graphics g) {

		x = 0;
		y = 0;

		cellSize = calculateCellSize();

		for (int i = (int) map.getMinPoint().getX(); i <= map.getMaxPoint().getX(); i++) {
			x++;
			y = 0;
			for (int j = (int) map.getMinPoint().getY(); j <= 
					map.getMaxPoint().getY(); j++) {
				y++;
				cellDraw(i, j, g);
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
	 * draws a cell according to its type within the map if the cell has value
	 * of 1 its color is set to white if the cell has value of -1 its color is
	 * set to black if the cell has value of 2 its red if the value is 0 its set
	 * to yellow
	 * 
	 * 
	 * @param j
	 *            the horizontal position of the cell in the grid
	 * @param i
	 *            the vertical position of the vell in the grid
	 * @param g
	 *            the graphics object --- intialized by paint method
	 * 
	 * 
	 */
	public void cellDraw(int j, int i, Graphics g) {
		try {
			cell = getSlam2().getMap().getGrid()[i][j];
			cellType = cell.getType();
		} catch (Exception e) {
			System.err.println("error at cell draw :-");
			e.printStackTrace();
		}

		if (cellType != 3) {

			if (cellType == 1) {

				currentTime = (System.currentTimeMillis() - cell
						.getLastCleaned().getTime()) / (1000);

				g.setColor(Color.WHITE);
				
				totalCells++;
				cellsVisited++;
			}

			else if (cellType == -1) {

				g.setColor(Color.BLACK);

			}

			// if cell marks the location of the robot draw it as red

			else if (cellType == 2) {

				g.setColor(Color.RED);

			}

			// if cell is marked as an unvisited cell it draws it as yellow

			else if (cellType == 0) {

				g.setColor(Color.YELLOW);

			}

			// if cell is marked as a charging station it draws it with cyan
			else if (cellType == 7) {

				g.setColor(Color.CYAN);
			}

			// if cell is selected draw it as blue
			if (cell.isSelected()) {
				g.setColor(Color.BLUE);

			}

			g.fillRect((((moveX + x) * (int) cellSize)),
					(((moveY + y) * (int) cellSize)), (int) cellSize,
					(int) cellSize);

			g.setColor(new Color(172, 158, 158));
			g.drawRect((((moveX + x) * (int) cellSize)),
					(((moveY + y) * (int) cellSize)), (int) cellSize,
					(int) cellSize);
		}

	}
	//TODO COMMENT
	/**
	 * 
	 * @param g
	 */
	public void drawSelection(Graphics g) {

		g.setColor(Color.BLACK);

	}

	/**
	 * 
	 * @return the calculated size of each cell
	 */

	public double calculateCellSize() {

		return Math.round(14 * percentageZoom);

	}

	/**
	 * round the double number to 1 decimal point
	 * @param shrink: for zoom in and zoom out
	 * @return : return the rounded number
	 */
	public double round1(double shrink){
		double result =(double)( Math.round(shrink *1000)) /1000;
		
		return result;
	}

	/**
	 * 
	 * @param px
	 * @return the calculated width of each cell
	 */
	public int getSelectedCellX(int px) {
		return 0;

	}
	/**
	 * 
	 * @param px
	 * @return the calculated height of each cell
	 */

	public int getSelectedCellY(int px) {
		return (int) (px / calculateCellSize()) - moveY;

	}

	/**
	 * 
	 * @param shrink
	 *            the percentage by which to zoom the
	 *            getSlam2().getMap().getGrid()
	 */
	public void setShrink(double shrink) {

		if (shrink == 1)
			percentageZoom = 1;

		else
			percentageZoom = shrink * percentageZoom;

		resizePanel(shrink);

		this.repaint();
	}
	/**
	 * round the double number to 1 decimal point
	 * @param shrink: for zoom in and zoom out
	 * @return : return the rounded number
	 */
	public void resizePanel(double shrink) {

		int x;
		int y;
		System.out.println(shrink);
		if (shrink != 1) {
			x = (int) (50*calculateCellSize());
			y = (int) (50*calculateCellSize());
		} else {
			x = (int) frame.panelSize.getX();
			y = (int) frame.panelSize.getY();
		}

		Dimension resize = new Dimension(x, y);

		setPreferredSize(resize);
		revalidate();

	}

	/**
	 * 
	 * @return slam2
	 */
	public Slam2 getSlam2() {
		return main;
	}
	/**
	 * get current cell
	 */
	public void getCurrentCell() {
		currentCell = new Point(getSlam2().getCurrentIndexX(), getSlam2()
				.getCurrentIndexY());

	}
	/**
	 *  
	 * @return totalCells :- the total number of cells which have been detected
	 */
	public int getTotalCells() {
		return totalCells;
	}
	/**
	 * 
	 * @return firstSelectedPoint: the index of top left cell of the selection
	 */
	public Point getFirstSelectedPoint() {
		return firstSelectedPoint;
	}
	/**
	 * 
	 * @return secondSelectedPoint: the index of bottom right cell of the selection
	 */
	public Point getSecondSelectedPoint() {
		return secondSelectedPoint;
	}
	/**
	 * 
	 * @return frame; evaluation GUI frame
	 */
	public EvaluationGUI getFrame() {
		return frame;
	}
	/**
	 * 
	 * @param frame : evaluation GUI frame
	 */
	public void setFrame(EvaluationGUI frame) {
		this.frame = frame;
	}
	//TODO COMMENT
	/**
	 * 
	 */
	public void refreshMaxMin() {
		xCoordinate = (int) currentCell.getX();
		yCoordiante = (int) currentCell.getY();

		if (map.getMapPoint().getY() > map.getMaxPoint().getY()
				- map.getMinPoint().getY()
				&& map.getMapPoint().getX() > map.getMaxPoint()
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
	/**
	 * 
	 * @return cellsVisited: get the total cells which have been visited
	 */

	public int getCellsVisited() {
		return cellsVisited;
	}
	/**
	 * 
	 * @param cellsVisited
	 */
	public void setCellsVisited(int cellsVisited) {
		this.cellsVisited = cellsVisited;
	}
	//TODO COMMENT
	/**
	 * 
	 * @author Song
	 *
	 */
	private class mouseListener implements MouseWheelListener {

		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			int notches = e.getWheelRotation();

			getFrame()
					.getJsp()
					.getVerticalScrollBar()
					.setValue(
							getFrame().getJsp().getVerticalScrollBar()
									.getValue()
									+ (notches * 10));

		}

	}

}
