package com.cleandroid.evaluationSoftware;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

import com.cleandroid.SaveLoad;

/**
 * JFreeChart Bar chart class which provides functionality for creating and serializing 
 * and de-serializing the statistics object and building a BarChart graph using the open source JFreeChart library
 * @author Team BLUE
 *
 */
public class BarChart extends JFrame {

	private static final long serialVersionUID = 1L;
	
	/**
	 * An instance of the SaveLoad tool class.
	 */
	private SaveLoad saveLoad;
	
	/**
	 * Statistics object which encapsulates the data that we want to provide to the JFreeChart for it to construct the chart.
	 */
	private Statistics statistics;

	{
		ChartFactory
				.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
	}

	/**
	 * 
	 * @param title - tite of the window when we instantiate the BarChart
	 * @param timeTaken - long: the time taken for the cleaning process since the ON button has been pressed
	 * @param cellsCovered - int count of the cells that have been covered so far
	 * @param time - the current system time
	 */
	public BarChart(String title, long timeTaken,
			int cellsCovered, String time) {
		super(title);

		saveLoad = new SaveLoad();
		if (saveLoad.deserializeStatistics()) {
			System.out.println("Statistics de-serialized.");
			statistics = saveLoad.getStatistics();
			if (timeTaken != 0){
				statistics.addCycle(timeTaken, cellsCovered, time);
			}
			saveLoad.serializeStatistics(statistics);
		} else {
			System.out.println("Statistics did not de-serialize.");
			statistics = new Statistics();
			if (timeTaken != 0){
				statistics.addCycle(timeTaken, cellsCovered, time);
			}
			saveLoad.serializeStatistics(statistics);
		}

		if(statistics.getListOfCycles().size()>0){
			System.out.println("BarChart - condition for displayin the chart passed!");
			CategoryDataset dataset = createDataset();
			JFreeChart chart = createChart(dataset);
			ChartPanel chartPanel = new ChartPanel(chart);
			chartPanel.setFillZoomRectangle(true);
			chartPanel.setMouseWheelEnabled(true);
			chartPanel.setPreferredSize(new Dimension(500, 270));
			setContentPane(chartPanel);
		}else{
			JOptionPane.showMessageDialog(this, "No Statistics data. Please run the robot first.");
		}
	}

	/**
	 * This method iterates through the list of cycles that are stored as an ArrayList in the de-serialized Statistics object.
	 * @return object of type CategoryDataset.
	 */
	private CategoryDataset createDataset() {

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		
		for (int i = 0; i < statistics.getListOfCycles().size(); i++) {
			
			dataset.addValue(statistics.getListOfCycles().get(i).getTimeTaken(), 
					Integer.toString(statistics.getListOfCycles().get(i).getCellsCovered()),
					statistics.getListOfCycles().get(i).getTime());
		}
		
		return dataset;

	}

	/**
	 * Method returns a constructed, fully customised chart. 
	 * @param dataset - dataset from the de-serialized Statistics object
	 * @return JFreeChart chart
	 */
	private static JFreeChart createChart(CategoryDataset dataset) {

		// create the chart...
		JFreeChart chart = ChartFactory.createBarChart("Time taken/Cells covered Performance", // Title of the chart
																			
				"", // X Axis label
				"Time taken", // Y axis label
				dataset, // Data used for the chart
				PlotOrientation.VERTICAL, // Orientation of the chart to be drawn
				false, // Legend
				true, // Tooltips
				false // URL
				);
		
		// Display an additional label undeneath the bar chart to tell the user to hover over the bar chart to see more deatils
		TextTitle legendText = new TextTitle("* Hover over the bar to see the cells covered." );
		legendText.setPosition(RectangleEdge.BOTTOM);
		chart.addSubtitle(legendText);

		// Background for the chart
		chart.setBackgroundPaint(Color.white);

		// Store reference to the plot to customise it
		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		// Y axis should display integers only (so that the time taken is given in second integers)
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		// Set the colours for the graphs
		GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue, 0.0f,
				0.0f, new Color(0, 0, 64));
		GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.green, 0.0f,
				0.0f, new Color(0, 64, 0));
		GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.red, 0.0f,
				0.0f, new Color(64, 0, 0));
		
		renderer.setSeriesPaint(0, gp0);
		renderer.setSeriesPaint(1, gp1);
		renderer.setSeriesPaint(2, gp2);

		// Set up the X axis so that the title appears diagonal
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions
				.createUpRotationLabelPositions(Math.PI / 6.0));
		
		return chart;
	}
}
