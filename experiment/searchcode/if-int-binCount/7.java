package org.omelogic.tome.histogram;

/* Adapted for HocusLocus by Ajish George <ajishg@gmail.com>
 * from code by
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @author <a href="http://webfoot.com/ducky.home.html">Kaitlin Duck Sherwood</a>
 *
 * See HistogramFrame.java for details 
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.DecimalFormat;

import java.util.*;

import prefuse.data.Table;
import prefuse.data.io.DataIOException;
import prefuse.data.io.DelimitedTextTableReader;
import prefuse.util.display.ExportDisplayAction;



/**
 * A simple histogram visualization that allows different columns
 * in a data table to be histogramized and displayed on the fly.
 * extended by Kaitlin Duck Sherwood to show histograms.
 * 
 * Kaitlin Duck Sherwood's modifications are granted as is for any
 * commercial or non-commercial use, with or without attribution.
 * The only conditions are that you can't pretend that you wrote them,
 * and that you leave these notices about her authorship in the source.
 * 
 * Possible limitations: I'm not completely sure if the histogram 
 * graph gets cleaned up properly.  I *think* that when I do the 
 * getContentPane().remove() of the histoGraph and toolbar
 * in getToolbar, that that frees them for garbage
 * collection.  However, I have not verified that. 
 * 
 * Known bug: If you use the default data (fisher.iris.txt),
 * and display the "Species Name" (a String column) and then display
 * PetalWidth (a double column), then the first two axis labels use 
 * names from Species Name.  I've looked at this carefully and suspect
 * it is a Prefuse bug.  I could be wrong.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @author <a href="http://webfoot.com/ducky.home.html">Kaitlin Duck Sherwood</a>
 */
public class HistogramPanel extends JPanel {
	Table m_dataTable;
	String name;
	HistogramTable m_histoTable;
	JToolBar m_toolbar;
	HashMap<String, Double> statistics;
	final JPanel thisHistogramPanel = this; 

	/**
	 * @param dataFileName - the name of a file in CSV format that holds the 
	 * data to be histogrammized.
	 * @param defaultFieldName - the name of the field (column) of the data table 
	 * whose histogram is to be shown in the histogram graph.
	 * @param aBinCount - the number of bins that the histogram should sort data values into
	 */
	public HistogramPanel(String argName, HashMap<String, Double> stats, Table ctrls, int aBinCount) {
		super(new BorderLayout());
		this.setPreferredSize(new Dimension(600,400));
		this.setMaximumSize(new Dimension(600,400));
		m_dataTable = ctrls;
		name = argName;
		statistics = stats;
		int statRow = m_dataTable.addRow();
		Iterator<String> statIter = stats.keySet().iterator();
		String defaultFieldName = null;
		while (statIter.hasNext()){
			String curStat = (String)statIter.next();
			Double curStatVal = stats.get(curStat);

			m_dataTable.set(statRow, curStat, curStatVal);
			if (defaultFieldName == null)
			{
				defaultFieldName = curStat;
			}
		}
		
		int binCount = (aBinCount > 0) ? aBinCount : 50;
		m_histoTable = new HistogramTable(m_dataTable, binCount);
		// histoTable.printWholeTable();  // debug

		HistogramGraph m_histoGraph = new HistogramGraph(m_histoTable, defaultFieldName);
		m_toolbar = getToolbar(m_histoGraph, defaultFieldName, binCount);
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel(name));
		this.add(namePanel, BorderLayout.NORTH);
		this.add(m_histoGraph, BorderLayout.CENTER);
		this.add(m_toolbar, BorderLayout.SOUTH);

	}



	/**
	 * @param histoGraph - a JComponent showing a graph of the histogram data
	 * @param fieldName - the name of the field (column) of the data table whose 
	 * histogram is to be shown in the histogram graph.
	 * @param binCount - the number of bins that the histogram should sort data values into
	 * @return toolbar - a JToolbar that controls the field to display and the number of bins
	 */
	private JToolBar getToolbar(final HistogramGraph histoGraph, 
			final String fieldName, final int binCount)
	{
		int spacing = 10;

		String[] fieldNames = HistogramTable.getFieldNames(m_dataTable);

		// create toolbar that allows displaying different histograms
		JToolBar toolbar = new JToolBar();
		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
		toolbar.add(Box.createHorizontalStrut(spacing));
		final DecimalFormat fieldFormat = new DecimalFormat(": ##.###");
		final JComboBox fieldsBox = new JComboBox(fieldNames);
		final JLabel fieldValue = new JLabel("0.000");
		if(fieldName == null) 
		{
			fieldsBox.setSelectedItem(fieldNames[0]);
		} else {
			fieldsBox.setSelectedItem(fieldName);
		}
		fieldValue.setText(fieldFormat.format(statistics.get(fieldName)));

		fieldsBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String dataField = (String)fieldsBox.getSelectedItem();
				fieldValue.setText(fieldFormat.format(statistics.get(dataField)));
				histoGraph.updateAxes(dataField); 
			}
		});

		toolbar.add(fieldsBox);
		toolbar.add(fieldValue);
		toolbar.add(Box.createHorizontalStrut(2*spacing));

		ExportDisplayAction exporter = new ExportDisplayAction(histoGraph);
		JButton export = new JButton("save");
		export.addActionListener(exporter);
		toolbar.add(export);
		toolbar.add(Box.createHorizontalStrut(2*spacing));

		return toolbar;
	}

}

