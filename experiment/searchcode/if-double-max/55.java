<<<<<<< HEAD
package ncsa.d2k.modules.core.transform.binning;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ncsa.d2k.core.modules.PropertyDescription;
import ncsa.d2k.core.modules.HeadlessUIModule;
import ncsa.d2k.core.modules.UserView;
import ncsa.d2k.core.modules.ViewModule;
import ncsa.d2k.gui.JD2KFrame;
import ncsa.d2k.modules.core.datatype.table.ExampleTable;
import ncsa.d2k.modules.core.datatype.table.MutableTable;
import ncsa.d2k.modules.core.datatype.table.Table;
import ncsa.d2k.modules.core.datatype.table.transformations.BinTransform;
import ncsa.d2k.modules.core.vis.widgets.Histogram;
import ncsa.d2k.modules.core.vis.widgets.IntervalHistogram;
import ncsa.d2k.modules.core.vis.widgets.RangeHistogram;
import ncsa.d2k.modules.core.vis.widgets.TableMatrix;
import ncsa.d2k.modules.core.vis.widgets.UniformHistogram;
import ncsa.d2k.userviews.swing.JUserPane;
import ncsa.gui.Constrain;
import ncsa.gui.DisposeOnCloseListener;
import ncsa.gui.ErrorDialog;
import ncsa.gui.JOutlinePanel;
import ncsa.d2k.modules.core.datatype.table.util.*;

/**
 * put your documentation comment here
 */
public class BinAttributes extends HeadlessUIModule {
  private static final String EMPTY = "",
  COLON = " : ", COMMA = ",", DOTS = "...",
  OPEN_PAREN = "(", CLOSE_PAREN = ")", OPEN_BRACKET = "[", CLOSE_BRACKET = "]";

  private NumberFormat nf;

  /**
   * put your documentation comment here
   * @return
   */
  public String getModuleName () {
	return  "Bin Attributes";
  }

  /**
   * put your documentation comment here
   * @return
   */
  public String getModuleInfo () {
	StringBuffer sb = new StringBuffer( "<p>Overview: ");
	sb.append( "This module allows a user to interactively bin data from a table.");

	sb.append( "</p><p>Detailed Description: ");
	sb.append( "This module provides a powerful interface that allows the user to ");
	sb.append( "control how numeric (continuous/scalar) and nominal (categorical) data in the input ");
	sb.append( "Table should be divided into bins. ");

	sb.append( "</p><p> Numeric data can be binned in four ways:<br>" );
	sb.append( "<BR><U>Uniform range:</U><br>" );
	sb.append( "Enter a positive integer value for the number of bins. The module will divide the ");
	sb.append( "binning range evenly over these bins. <br> ");
	sb.append( "<BR><U>Specified range:</U><br> ");
	sb.append( "Enter a comma-separated sequence of integer or floating-point values for the endpoints of each bin. <br> ");
	sb.append( "<BR><U>Bin Interval:</u><br> ");
	sb.append( "Enter an integer or floating-point value for the width of each bin.<br> ");
	sb.append( "<BR><u>Uniform Weight:</U><br> ");
	sb.append( "Enter a positive integer value for even binning with that number in each bin. ");

	sb.append( "</P><P>The user may also bin nominal data. ");

	sb.append( "</P><P>For further information on how to use this module the user may click on the \"Help\" button ");
	sb.append( "during run time and get a detailed description of the functionality." );

	sb.append( "</p><P>Missing and Empty Values Handling: In scalar attributes, missing and empty values will be binned into " );
	sb.append( "\"UNKNOWN\". In nominal attributes, however, missing/empty values which are represented by " );
	sb.append( "'?' will be treated as a unique value in the attribute. In this case, if the user does not group " );
	sb.append( "the '?' and assign it a bin, then it would also be binned into \"UNKNOWN\".");

	sb.append( "</p><P>Data Handling:  This module does not change its input.  ");
	sb.append( "Rather, it outputs a Transformation that can later be applied to the original table data. ");
	sb.append( "Note that if its input is an example table, only input and output features will be eligible for binning.</P>" );

	return sb.toString();
  }

  /**
   * put your documentation comment here
   * @return
   */
  public String[] getInputTypes () {
	String[] types =  {
	  "ncsa.d2k.modules.core.datatype.table.Table"
	};
	return  types;
  }

  /**
   * put your documentation comment here
   * @param i
   * @return
   */
  public String getInputName (int i) {
	if (i == 0)
	  return  "Table";
	return  "no such input";
  }

  /**
   * put your documentation comment here
   * @param i
   * @return
   */
  public String getInputInfo (int i) {
	switch (i) {
	  case 0:
		return  "A Table with attributes to bin. If it is an Example Table, only input and output features will be eligible for binning.";
	  default:
		return  "No such input";
	}
  }

  /**
   * put your documentation comment here
   * @return
   */
  public String[] getOutputTypes () {
	String[] types =  {
	  "ncsa.d2k.modules.core.datatype.table.transformations.BinTransform"
	};
	return  types;
  }


  /**
   * put your documentation comment here
   * @param i
   * @return
   */
  public String getOutputName (int i) {
	switch (i) {
	  case 0:
		return "Binning Transformation";
	  default:
		return  "no such output!";
	}
  }

  /**
   * put your documentation comment here
   * @param i
   * @return
   */
  public String getOutputInfo (int i) {
	switch (i) {
	  case 0:
		String s = "A Binning Transformation, as defined by the user via this module.  " +
				   "This output is typically connected to a <i>Create Bin Tree</i> module " +
				   "or to an <i>Apply Transformation</i> module where it is applied to the input Table. ";
		return s;
	  default:
		return  "No such output";
	}
  }

  private BinDescriptor[] savedBins;

  public Object getSavedBins() { return savedBins; }
  public void setSavedBins(Object value) { savedBins = (BinDescriptor[])value; }

  /**
   * This method returns and array of property descriptions.  In this case,
   * there are no user-editable properties so we return an empty array.
   * If this method isn't defined here, then the user sees the windowName
   * property which they really should not be shown.
   **/
  public PropertyDescription[] getPropertiesDescriptions() {
	PropertyDescription[] pds = new PropertyDescription[2];
        pds[0] = this.supressDescription;
        pds[1] = new PropertyDescription("newColumn", "Create In New Column",
            "Set this property to true if you wish the binned columns to be created in new columns (applied only when 'Supress User Interface Display' is set to true)");
	return pds;
  }

  /**
   * put your documentation comment here
   * @return
   */
  protected UserView createUserView () {
	return  new BinColumnsView();
  }

  /**
   * put your documentation comment here
   * @return
   */
  public String[] getFieldNameMapping () {
	return  null;
  }

  private class BinColumnsView extends JUserPane {
	private boolean setup_complete;
	private BinDescriptor currentSelectedBin;
	private HashMap columnLookup;
	private HashSet[] uniqueColumnValues;
	private JList numericColumnLabels, textualColumnLabels, currentBins;
	private DefaultListModel binListModel;
	private MutableTable tbl;

		/* numeric text fields */
	private JTextField uRangeField, specRangeField, intervalField, weightField;

		/* textual lists */
	private JList textUniqueVals, textCurrentGroup;
	private DefaultListModel textUniqueModel, textCurrentModel;

		/* textual text field */
	private JTextField textBinName;

		/* current selection fields */
	private JTextField curSelName;
	private JList currentSelectionItems;
	private DefaultListModel currentSelectionModel;
	private JButton abort, done;
	private JCheckBox createInNewColumn;

	int uniqueTextualIndex = 0;

	/**
	 * put your documentation comment here
	 */
	private BinColumnsView () {
	  setup_complete = false;
	  nf = NumberFormat.getInstance();
	  nf.setMaximumFractionDigits(5);
	  nf.setMinimumFractionDigits(5);
	}

	/**
	 * put your documentation comment here
	 * @param o
	 * @param id
	 */
	public void setInput (Object o, int id) {
	  tbl = (MutableTable)o;
	  //tbl = (Table)o;

	  // set column labels on the table if necessary...
	 for (int i = 0; i < tbl.getNumColumns(); i++) {
		if (tbl.getColumnLabel(i) == null || tbl.getColumnLabel(i).length() == 0) {
		  tbl.setColumnLabel("column_" + i, i);
		}
	  }


	  // clear all text fields and lists...
	  curSelName.setText(EMPTY);
	  textBinName.setText(EMPTY);
	  uRangeField.setText(EMPTY);
	  specRangeField.setText(EMPTY);
	  intervalField.setText(EMPTY);
	  weightField.setText(EMPTY);
	  columnLookup = new HashMap();
	  uniqueColumnValues = new HashSet[tbl.getNumColumns()];
	  binListModel.removeAllElements();

	  /* This was causing it to put the same bins in multiple times.
      if (savedBins != null) {
		 for (int i = 0; i < savedBins.length; i++) {
			binListModel.addElement(savedBins[i]);
		 }
	  }*/

		DefaultListModel numModel = (DefaultListModel)numericColumnLabels.getModel(),
		txtModel = (DefaultListModel)textualColumnLabels.getModel();
		numModel.removeAllElements();
		txtModel.removeAllElements();

		textCurrentModel.removeAllElements();
		textUniqueModel.removeAllElements();
		uniqueTextualIndex = 0;

		// !: check inputs/outputs if example table
		ExampleTable et = null;
		HashMap etInputs = null;
		HashMap etOutputs = null;
		if (tbl instanceof ExampleTable) {
		   et = (ExampleTable)tbl;
		   int[] inputs = et.getInputFeatures();
		   int[] outputs = et.getOutputFeatures();
		   etInputs = new HashMap();
		   etOutputs = new HashMap();

		   for (int i = 0; i < inputs.length; i++) {
			  etInputs.put(new Integer(inputs[i]), null);
		   }
		   for (int i = 0; i < outputs.length; i++) {
			  etOutputs.put(new Integer(outputs[i]), null);
		   }
		}

		for (int i = 0; i < tbl.getNumColumns(); i++) {

		   if (et != null) {
			  if (!etInputs.containsKey(new Integer(i)) &&
				  !etOutputs.containsKey(new Integer(i))) {
				 continue;
			  }
		   }

		   columnLookup.put(tbl.getColumnLabel(i), new Integer(i));
		   //if (table.getColumn(i) instanceof NumericColumn)
		   if (tbl.isColumnScalar(i))
			  numModel.addElement(tbl.getColumnLabel(i));
		   else {          //if (table.getColumn(i) instanceof TextualColumn) {
			  txtModel.addElement(tbl.getColumnLabel(i));
			  uniqueColumnValues[i] = uniqueValues(i);
		   }

		}

		if (!validateBins(binListModel)) {
		   binListModel.removeAllElements();
		}

		// finished...
		setup_complete = true;
	}

	/**
	 * Create all of the components and add them to the view.
	 */
	public void initView (ViewModule m) {
	  currentBins = new JList();
	  binListModel = new DefaultListModel();
	  currentBins.setModel(binListModel);
	  currentBins.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	  currentBins.addListSelectionListener(new CurrentListener());
	  // set up the numeric tab
	  numericColumnLabels = new JList();
	  numericColumnLabels.setModel(new DefaultListModel());
	  // uniform range
	  JOutlinePanel urangepnl = new JOutlinePanel("Uniform Range");
	  uRangeField = new JTextField(5);
	  JButton addURange = new JButton("Add");
	  addURange.addActionListener(new AbstractAction() {

		public void actionPerformed (ActionEvent e) {
		  addUniRange();
		  uRangeField.setText(EMPTY);
		}
	  });
	  JButton showURange = new JButton("Show");
	  showURange.addActionListener(new AbstractAction() {

		public void actionPerformed (ActionEvent e) {
		  HashMap colLook = new HashMap();
		  for (int i = 0; i < tbl.getNumColumns(); i++) {
			if(tbl.isColumnNumeric(i)) {
			  colLook.put(tbl.getColumnLabel(i), new Integer(i));
			}
		  }

		  String txt = uRangeField.getText();
		  //vered: replacing if else with try catch
		  //if(txt != null && txt.length() != 0)
		  try
		  {
			int i = Integer.parseInt(txt);  //if this is successful then it is safe to go on with the preview
			if(i<=1)
			  throw new NumberFormatException();


			String selCol = (String)numericColumnLabels.getSelectedValue();
			if(selCol == null) throw new NullPointerException();

			final Histogram H = new UniformHistogram(new TableBinCounts(tbl),
				uRangeField.getText(), colLook, selCol);
			JD2KFrame frame = new JD2KFrame("Uniform Range");
			frame.getContentPane().setLayout(new GridBagLayout());
			Constrain.setConstraints(frame.getContentPane(), H, 0,
									 0, 3, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER,
									 1, 1);
			final JButton uniformAdd = new JButton("Add");
			Constrain.setConstraints(frame.getContentPane(), new JLabel(""),
									 0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST,
									 .33, 0);
			Constrain.setConstraints(frame.getContentPane(), uniformAdd,
									 1, 1, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH,
									 .34, 0);
			Constrain.setConstraints(frame.getContentPane(), new JLabel(""),
									 2, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.SOUTHEAST,
									 .33, 0);
			uniformAdd.addActionListener(new AbstractAction() {
			  final JSlider uniformSlider = H.getSlider();

			  public void actionPerformed (ActionEvent e) {
				uRangeField.setText(Integer.toString(uniformSlider.getValue()));
				numericColumnLabels.clearSelection();
				setSelectedNumericIndex(H.getSelection());
				addUniRange();
				uRangeField.setText(EMPTY);
			  }
			});
			frame.pack();
			frame.setVisible(true);
		  }//try
		  //else
		  catch(NumberFormatException nfe){
			// message dialog...must specify range
			ErrorDialog.showDialog("Must specify a valid range - an integer greater than 1.", "Error");
			uRangeField.setText(EMPTY);
		  }
		  catch(NullPointerException npe){
			ErrorDialog.showDialog("You must select an attribute to bin.", "Error");
		  }
					/*
					 final JSlider uniformSlider = H.getSlider();
					 uniformSlider.addChangeListener(new ChangeListener() {
					 public void stateChanged(ChangeEvent e) {
					 uRangeField.setText(Integer.toString(uniformSlider.getValue()));
					 }
					 });
					 */

		}
	  });
	  urangepnl.setLayout(new GridBagLayout());
	  Constrain.setConstraints(urangepnl, new JLabel("Number of Bins"),
							   0, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST,
							   1, 1);
	  Box b = new Box(BoxLayout.X_AXIS);
	  b.add(uRangeField);
	  b.add(addURange);
	  b.add(showURange);
	  Constrain.setConstraints(urangepnl, b, 1, 0, 1, 1, GridBagConstraints.NONE,
							   GridBagConstraints.EAST, 1, 1);
	  // specified range
	  JOutlinePanel specrangepnl = new JOutlinePanel("Specified Range");
	  specrangepnl.setLayout(new GridBagLayout());
	  Constrain.setConstraints(specrangepnl, new JLabel("Range"), 0,
							   0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST,
							   1, 1);
	  specRangeField = new JTextField(5);
	  JButton addSpecRange = new JButton("Add");
	  addSpecRange.addActionListener(new AbstractAction() {
		public void actionPerformed (ActionEvent e) {
		  addSpecifiedRange();
		  specRangeField.setText(EMPTY);
		}
	  });
	  JButton showSpecRange = new JButton("Show");
	  showSpecRange.addActionListener(new AbstractAction() {

		public void actionPerformed (ActionEvent e) {

		  //@todo: add exception handling for illegal input
		  String txt = specRangeField.getText();
		  //vered: wrapped it all with try catch.
		  try{
			if(txt == null || txt.length() == 0) {
			  // show message dialog
			  //vered
			  throw new IllegalArgumentException();

			  //ErrorDialog.showDialog("Must specify range.", "Error");
			  // return;
			}


			HashMap colLook = new HashMap();
			for (int i = 0; i < tbl.getNumColumns(); i++) {
			  if(tbl.isColumnNumeric(i)) {
				colLook.put(tbl.getColumnLabel(i), new Integer(i));
			  }
			}//for
			JD2KFrame frame = new JD2KFrame("Specified Range");
			String col = (String)numericColumnLabels.getSelectedValue();
			//vered:
			if (col == null) throw new NullPointerException();

			frame.getContentPane().add(new RangeHistogram(new TableBinCounts(tbl),
				/*Histogram.HISTOGRAM_RANGE,*/ specRangeField.getText(), colLook, col));
				 frame.pack();
				 frame.setVisible(true);
		  }//try
		  catch(NullPointerException npe){
			ErrorDialog.showDialog("You must select an attribute to bin.", "Error");
		  }
		  catch(IllegalArgumentException iae){
			ErrorDialog.showDialog("Please enter a comma-separated sequence of\ninteger or floating-point values for\nthe endpoints of each bin. ", "Error");
		  }
		}//action performed.
	  });
	  Box b1 = new Box(BoxLayout.X_AXIS);
	  b1.add(specRangeField);
	  b1.add(addSpecRange);
	  b1.add(showSpecRange);
	  Constrain.setConstraints(specrangepnl, b1, 1, 0, 1, 1, GridBagConstraints.NONE,
							   GridBagConstraints.EAST, 1, 1);
	  // interval
	  JOutlinePanel intervalpnl = new JOutlinePanel("Bin Interval");
	  intervalpnl.setLayout(new GridBagLayout());
	  intervalField = new JTextField(5);
	  JButton addInterval = new JButton("Add");
	  addInterval.addActionListener(new AbstractAction() {
		public void actionPerformed (ActionEvent e) {
		  addFromInterval();
		  intervalField.setText(EMPTY);
		}
	  });
	  JButton showInterval = new JButton("Show");
	  showInterval.addActionListener(new AbstractAction() {

		public void actionPerformed (ActionEvent e) {
		  HashMap colLook = new HashMap();
		  for (int i = 0; i < tbl.getNumColumns(); i++) {
			if(tbl.isColumnNumeric(i)) {
			  colLook.put(tbl.getColumnLabel(i), new Integer(i));
			}
		  }
		  String txt = intervalField.getText();

		  double intrval;
		  try {
			intrval = Double.parseDouble(txt);
		  }
		  catch(NumberFormatException ex) {
			ErrorDialog.showDialog("You must specify a valid, positive interval.", "Error");
			return;
		  }

		  if (intrval <= 0) {
			ErrorDialog.showDialog("You must specify a valid, positive interval.", "Error");
			return;
		  }

		  //vered: inserting a try catch to deal with inllegal argument exceptions
		  try{
			//if(txt != null && txt.length() != 0) {
			if(txt == null || txt.length() == 0) throw new IllegalArgumentException("Must specify an interval");

			String col = (String)numericColumnLabels.getSelectedValue();
			//vered: added this to prevent null pointer exception in init of histogram.
			if(col == null) throw new NullPointerException();

			final Histogram H = new IntervalHistogram(new TableBinCounts(tbl),
				/*Histogram.HISTOGRAM_INTERVAL,*/ intervalField.getText(), colLook, col);
				 JD2KFrame frame = new JD2KFrame("Bin Interval");
				 frame.getContentPane().setLayout(new GridBagLayout());
				 Constrain.setConstraints(frame.getContentPane(), H, 0,
					 0, 3, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER,
					 1, 1);
				 final JButton intervalAdd = new JButton("Add");
				 Constrain.setConstraints(frame.getContentPane(), new JLabel(""),
					 0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.SOUTHWEST,
					 .33, 0);
				 Constrain.setConstraints(frame.getContentPane(), intervalAdd,
					 1, 1, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH,
					 .34, 0);
				 Constrain.setConstraints(frame.getContentPane(), new JLabel(""),
					 2, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.SOUTHEAST,
					 .33, 0);
				 intervalAdd.addActionListener(new AbstractAction() {
				   final JSlider intervalSlider = H.getSlider();

				   /**
					* put your documentation comment here
					* @param e
					*/
				   public void actionPerformed (ActionEvent e) {
					 int sel = H.getSelection();
					 numericColumnLabels.clearSelection();
					 setSelectedNumericIndex(sel);
					 double interval = getInterval();
					 intervalField.setText(Double.toString(H.getPercentage()*interval));
					 addFromInterval();
					 intervalField.setText(EMPTY);
				   }
				 });
				 frame.pack();
				 frame.setVisible(true);
		  }//try
		  catch(IllegalArgumentException iae){
			String str = iae.getMessage();
			if(str == null || str.length() == 0) str = "You must specify a valid interval";
			ErrorDialog.showDialog(str, "Error");
		  }
		  catch(NullPointerException npe){
			ErrorDialog.showDialog("You must select an attribute to bin.", "Error");
		  }
					/*vered - the if else replaced by try catch
					else {
		  // message dialog...you must specify an interval
						ErrorDialog.showDialog("Must specify interval.", "Error");
					}*/
							 /*
					 final JSlider intervalSlider = H.getSlider();
					 intervalSlider.addChangeListener(new ChangeListener() {
					 public void stateChanged(ChangeEvent e) {
					 intervalField.setText(Integer.toString(intervalSlider.getValue()));
					 }
					 });
					 */

		}
	  });
	  Constrain.setConstraints(intervalpnl, new JLabel("Interval"), 0,
							   0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST,
							   1, 1);
	  Box b2 = new Box(BoxLayout.X_AXIS);
	  b2.add(intervalField);
	  b2.add(addInterval);
	  b2.add(showInterval);
	  Constrain.setConstraints(intervalpnl, b2, 1, 0, 1, 1, GridBagConstraints.NONE,
							   GridBagConstraints.EAST, 1, 1);
	  // uniform weight
	  JOutlinePanel weightpnl = new JOutlinePanel("Uniform Weight");
	  weightpnl.setLayout(new GridBagLayout());
	  weightField = new JTextField(5);
	  JButton addWeight = new JButton("Add");
	  addWeight.addActionListener(new AbstractAction() {

		/**
		 * put your documentation comment here
		 * @param e
		 */
		public void actionPerformed (ActionEvent e) {
		  addFromWeight();
		  weightField.setText(EMPTY);
		}
	  });
	  // JButton showWeight = new JButton("Show");
	  // showWeight.setEnabled(false);
	  Constrain.setConstraints(weightpnl, new JLabel("Number in each bin"),
							   0, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST,
							   1, 1);
	  Box b3 = new Box(BoxLayout.X_AXIS);
	  b3.add(weightField);
	  b3.add(addWeight);
	  // b3.add(showWeight);
	  Constrain.setConstraints(weightpnl, b3, 1, 0, 1, 1, GridBagConstraints.NONE,
							   GridBagConstraints.EAST, 1, 1);
	  // add all numeric components
	  JPanel numpnl = new JPanel();
	  numpnl.setLayout(new GridBagLayout());
	  JScrollPane jsp = new JScrollPane(numericColumnLabels);
	  //jsp.setColumnHeaderView(new JLabel("Numeric Columns"));
	  Constrain.setConstraints(numpnl, jsp, 0, 0, 4, 1, GridBagConstraints.BOTH,
							   GridBagConstraints.WEST, 1, 1);
	  Constrain.setConstraints(numpnl, urangepnl, 0, 1, 4, 1, GridBagConstraints.HORIZONTAL,
							   GridBagConstraints.WEST, 1, 1);
	  Constrain.setConstraints(numpnl, specrangepnl, 0, 2, 4, 1, GridBagConstraints.HORIZONTAL,
							   GridBagConstraints.WEST, 1, 1);
	  Constrain.setConstraints(numpnl, intervalpnl, 0, 3, 4, 1, GridBagConstraints.HORIZONTAL,
							   GridBagConstraints.WEST, 1, 1);
	  Constrain.setConstraints(numpnl, weightpnl, 0, 4, 4, 1, GridBagConstraints.HORIZONTAL,
							   GridBagConstraints.WEST, 1, 1);
	  // textual bins
	  JPanel txtpnl = new JPanel();
	  txtpnl.setLayout(new GridBagLayout());

	  textualColumnLabels = new JList();
	  textualColumnLabels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	  textualColumnLabels.addListSelectionListener(new TextualListener());
	  textualColumnLabels.setModel(new DefaultListModel());

	  // berkin
	  JButton autobin = new JButton("Auto Bin");
	  autobin.addActionListener(new AbstractAction() {

		public void actionPerformed(ActionEvent event) {
		  Object[] inputs = (Object[]) textualColumnLabels.getSelectedValues();

		  for (int input=0; input < inputs.length; input++) {
			Integer integer = (Integer) columnLookup.get((String) inputs[input]);
			// String[] values = TableUtilities.uniqueValues(tbl, integer.intValue());
         Object[] values = textUniqueModel.toArray();

			for (int value = 0; value < values.length; value++) {
			  String[] bin = {values[value].toString()};
			  BinDescriptor descriptor = BinDescriptorFactory.createTextualBin(integer.intValue(), values[value].toString(), bin, tbl);
			  addItemToBinList(descriptor);
			}

         Object val = inputs[input];
         int idx = ((Integer)columnLookup.get(val)).intValue();
         HashSet set = uniqueColumnValues[idx];
         textUniqueModel.removeAllElements();
         textCurrentModel.removeAllElements();
         set.clear();

		  }
		}
	  });

	  textUniqueVals = new JList();
	  textUniqueModel = new DefaultListModel();
	  textUniqueVals.setModel(textUniqueModel);
	  textUniqueVals.setFixedCellWidth(100);
	  textCurrentGroup = new JList();
	  textCurrentGroup.setFixedCellWidth(100);
	  textCurrentModel = new DefaultListModel();
	  textCurrentGroup.setModel(textCurrentModel);
	  JButton addTextToGroup = new JButton(">");
	  addTextToGroup.addActionListener(new AbstractAction() {

		/**
		 * put your documentation comment here
		 * @param e
		 */
		public void actionPerformed (ActionEvent e) {
		  if (!setup_complete)
			return;
		  Object[] sel = textUniqueVals.getSelectedValues();
		  for (int i = 0; i < sel.length; i++) {
			// textUniqueModel.removeElement(sel[i]);
			if (!textCurrentModel.contains(sel[i]))
			  textCurrentModel.addElement(sel[i]);
		  }
		}
	  });
	  JButton removeTextFromGroup = new JButton("<");
	  removeTextFromGroup.addActionListener(new AbstractAction() {

		/**
		 * put your documentation comment here
		 * @param e
		 */
		public void actionPerformed (ActionEvent e) {
		  if (!setup_complete)
			return;
		  Object[] sel = textCurrentGroup.getSelectedValues();
		  for (int i = 0; i < sel.length; i++) {
			textCurrentModel.removeElement(sel[i]);
			// textUniqueModel.addElement(sel[i]);
		  }
		}
	  });
	  JTabbedPane jtp = new JTabbedPane(JTabbedPane.TOP);
	  jtp.add(numpnl, "Scalar");
	  jtp.add(txtpnl, "Nominal");
	  Box bx = new Box(BoxLayout.Y_AXIS);
	  bx.add(Box.createGlue());
	  bx.add(addTextToGroup);
	  bx.add(removeTextFromGroup);
	  bx.add(Box.createGlue());
	  Box bx1 = new Box(BoxLayout.X_AXIS);
	  JScrollPane jp1 = new JScrollPane(textUniqueVals);
	  jp1.setColumnHeaderView(new JLabel("Unique Values"));
	  bx1.add(jp1);
	  bx1.add(Box.createGlue());
	  bx1.add(bx);
	  bx1.add(Box.createGlue());
	  JScrollPane jp2 = new JScrollPane(textCurrentGroup);
	  jp2.setColumnHeaderView(new JLabel("Current Group"));
	  bx1.add(jp2);
	  textBinName = new JTextField(10);
	  JButton addTextBin = new JButton("Add");
	  addTextBin.addActionListener(new AbstractAction() {

		/**
		 * put your documentation comment here
		 * @param e
		 */
		public void actionPerformed (ActionEvent e) {
		  Object[] sel = textCurrentModel.toArray();

		  if (sel.length == 0) {
			ErrorDialog.showDialog("You must select some nominal values to group.", "Error");
			return;
		  }

		  Object val = textualColumnLabels.getSelectedValue();
		  int idx = ((Integer)columnLookup.get(val)).intValue();

		  String textualBinName;

		  if (textBinName.getText().length() == 0)
			textualBinName = "bin" + uniqueTextualIndex++;
		  else
			textualBinName = textBinName.getText();
          if (!checkDuplicateBinNames(textualBinName)) {
            ErrorDialog.showDialog("The bin name must be unique, "+textualBinName+" already used.", "Error");
            return;
          }

		  BinDescriptor bd = createTextualBin(idx, textualBinName,
			  sel);

		  HashSet set = uniqueColumnValues[idx];
		  for (int i = 0; i < sel.length; i++) {
           textUniqueModel.removeElement(sel[i]);
			textCurrentModel.removeElement(sel[i]);
			set.remove(sel[i]);
		  }
		  addItemToBinList(bd);
		  textBinName.setText(EMPTY);
		}
	  });
	  JOutlinePanel jop = new JOutlinePanel("Group");
	  JPanel pp = new JPanel();
	  pp.add(new JLabel("Name"));
	  pp.add(textBinName);
	  pp.add(addTextBin);
	  jop.setLayout(new BoxLayout(jop, BoxLayout.Y_AXIS));
	  jop.add(bx1);
	  jop.add(pp);

	  JScrollPane jp3 = new JScrollPane(textualColumnLabels);
	  Constrain.setConstraints(txtpnl, jp3, 0, 0, 4, 1, GridBagConstraints.BOTH,
							   GridBagConstraints.NORTHWEST, 1, .5);
	  Constrain.setConstraints(txtpnl, autobin, 0, 1, 4, 1, GridBagConstraints.NONE,
							   GridBagConstraints.CENTER, 1, 0);
	  Constrain.setConstraints(txtpnl, jop, 0, 2, 4, 1, GridBagConstraints.BOTH,
							   GridBagConstraints.NORTHWEST, 1, .5);

	  /*
	  Constrain.setConstraints(txtpnl, jp3, 0, 0, 4, 1, GridBagConstraints.BOTH,
							   GridBagConstraints.WEST, 1, 1);
	  Constrain.setConstraints(txtpnl, jop, 0, 1, 4, 1, GridBagConstraints.HORIZONTAL,
							   GridBagConstraints.WEST, 1, 1);
	  */

	  // now add everything
	  JPanel pq = new JPanel();
	  pq.setLayout(new BorderLayout());
	  JScrollPane jp4 = new JScrollPane(currentBins);
	  jp4.setColumnHeaderView(new JLabel("Current Bins"));
	  pq.add(jp4, BorderLayout.CENTER);
	  JOutlinePanel jop5 = new JOutlinePanel("Current Selection");
	  currentSelectionItems = new JList();
	  currentSelectionItems.setVisibleRowCount(4);
	  currentSelectionItems.setEnabled(false);
	  currentSelectionModel = new DefaultListModel();
	  currentSelectionItems.setModel(currentSelectionModel);
	  JPanel pt = new JPanel();
	  curSelName = new JTextField(10);
	  pt.add(new JLabel("Name"));
	  pt.add(curSelName);
	  JButton updateCurrent = new JButton("Update");
	  updateCurrent.addActionListener(new AbstractAction() {

		/**
		 * put your documentation comment here
		 * @param e
		 */
		public void actionPerformed (ActionEvent e) {
		  if (!setup_complete)
			return;
		  if (currentSelectedBin != null) {
			currentSelectedBin.name = curSelName.getText();
			currentBins.repaint();
		  }
		}
	  });
	  JButton removeBin = new JButton("Remove Bin");
	  removeBin.addActionListener(new AbstractAction() {

		/**
		 * put your documentation comment here
		 * @param e
		 */
		public void actionPerformed (ActionEvent e) {
		  if (!setup_complete)
			return;

		  if (currentSelectedBin != null) {
			int col = currentSelectedBin.column_number;
			if (currentSelectedBin instanceof TextualBinDescriptor)
			  uniqueColumnValues[col].addAll(((TextualBinDescriptor)currentSelectedBin).vals);

			binListModel.removeElement(currentSelectedBin);
			currentSelectionModel.removeAllElements();
			curSelName.setText(EMPTY);

			// update the group
			Object lbl = textualColumnLabels.getSelectedValue();
			// gpape:
			if (lbl != null) {
			  int idx = ((Integer)columnLookup.get(lbl)).intValue();
			  HashSet unique = uniqueColumnValues[idx];
			  textUniqueModel.removeAllElements();
			  textCurrentModel.removeAllElements();
			  Iterator i = unique.iterator();
			  while (i.hasNext())
				textUniqueModel.addElement(i.next());
			}

		  }
		}
	  });
	  // gpape:
	  JButton removeAllBins = new JButton("Remove All");
	  removeAllBins.addActionListener(new AbstractAction() {

		/**
		 * put your documentation comment here
		 * @param e
		 */
		public void actionPerformed (ActionEvent e) {

		  if (!setup_complete)
			return;

        while (binListModel.getSize() > 0) {

           BinDescriptor bd = (BinDescriptor)binListModel.firstElement();

           if (bd instanceof TextualBinDescriptor) {
              uniqueColumnValues[bd.column_number].addAll(((TextualBinDescriptor)bd).vals);
           }
           binListModel.remove(0);

        }

		  // binListModel.removeAllElements();
		  currentSelectionModel.removeAllElements();
		  curSelName.setText(EMPTY);

        // update the group
        Object lbl = textualColumnLabels.getSelectedValue();
        // gpape:
        if (lbl != null) {
          int idx = ((Integer)columnLookup.get(lbl)).intValue();
          HashSet unique = uniqueColumnValues[idx];
          textUniqueModel.removeAllElements();
          textCurrentModel.removeAllElements();
          Iterator i = unique.iterator();
          while (i.hasNext())
           textUniqueModel.addElement(i.next());
			}

		}
	  });
	  // gpape:
	  createInNewColumn = new JCheckBox("Create in new column", false);
	  Box pg = new Box(BoxLayout.X_AXIS);
	  pg.add(updateCurrent);
	  //pg.add(removeItems);
	  pg.add(removeBin);
	  pg.add(removeAllBins);
	  // gpape:
	  Box pg2 = new Box(BoxLayout.X_AXIS);
	  pg2.add(createInNewColumn);
	  jop5.setLayout(new BoxLayout(jop5, BoxLayout.Y_AXIS));
	  jop5.add(pt);
	  JScrollPane pane = new JScrollPane(currentSelectionItems);
	  pane.setColumnHeaderView(new JLabel("Items"));
	  jop5.add(pane);
	  jop5.add(pg);
	  jop5.add(pg2);
	  JPanel bgpnl = new JPanel();
	  bgpnl.setLayout(new BorderLayout());
	  bgpnl.add(jp4, BorderLayout.CENTER);
	  bgpnl.add(jop5, BorderLayout.SOUTH);
	  // finally add everything to this
	  setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	  Box bxl = new Box(BoxLayout.X_AXIS);
	  bxl.add(jtp);
	  bxl.add(bgpnl);
	  JPanel buttonPanel = new JPanel();
	  abort = new JButton("Abort");
	  abort.addActionListener(new AbstractAction() {

		/**
		 * put your documentation comment here
		 * @param e
		 */
		public void actionPerformed (ActionEvent e) {
		  viewCancel();
		}
	  });
	  done = new JButton("Done");
	  done.addActionListener(new AbstractAction() {

		/**
		 * put your documentation comment here
		 * @param e
		 */
		public void actionPerformed (ActionEvent e) {
		  //binIt(createInNewColumn.isSelected());
		  // if (validateBins(binListModel)) {
			 Object[] tmp = binListModel.toArray();
			 BinDescriptor[] bins = new BinDescriptor[tmp.length];
			 for (int i = 0; i < bins.length; i++)
				bins[i] = (BinDescriptor)tmp[i];

			 savedBins = new BinDescriptor[bins.length];
			 for (int i = 0; i < bins.length; i++)
				savedBins[i] = bins[i];

		//ANCA
			//		 savedBins = BinningUtils.addMissingValueBins(tbl,savedBins);

			 BinTransform bt = new BinTransform(tbl, savedBins, createInNewColumn.isSelected());

			 pushOutput(bt, 0);
			 viewDone("Done");
		  // }
		}
	  });
	  JButton showTable = new JButton("Show Table");
	  showTable.addActionListener(new AbstractAction() {

		/**
		 * put your documentation comment here
		 * @param e
		 */
		public void actionPerformed (ActionEvent e) {
		  JD2KFrame frame = new JD2KFrame("Table");
		  frame.getContentPane().add(new TableMatrix(tbl));
		  frame.addWindowListener(new DisposeOnCloseListener(frame));
		  frame.pack();
		  frame.setVisible(true);
		}
	  });
	  JButton helpButton = new JButton("Help");
	  helpButton.addActionListener(new AbstractAction() {

		/**
		 * put your documentation comment here
		 * @param e
		 */
		public void actionPerformed (ActionEvent e) {
		  HelpWindow help = new HelpWindow();
		  help.setVisible(true);
		}
	  });
	  buttonPanel.add(abort);
	  buttonPanel.add(done);
	  buttonPanel.add(showTable);
	  buttonPanel.add(helpButton);
	  setLayout(new BorderLayout());
	  add(bxl, BorderLayout.CENTER);
	  add(buttonPanel, BorderLayout.SOUTH);
	}

    private boolean checkDuplicateBinNames(String newName) {
       for (int bdi = 0; bdi < binListModel.getSize(); bdi++) {
          BinDescriptor bd = (BinDescriptor)binListModel.elementAt(bdi);
          if (newName.equals(bd.name))
              return false;
       }
       return true;
    }

   private boolean checkDuplicateNumericBins(int[] newIndices) {

      for (int bdi = 0; bdi < binListModel.getSize(); bdi++) {

         BinDescriptor bd = (BinDescriptor)binListModel.elementAt(bdi);

         for (int i = 0; i < newIndices.length; i++) {

            if (newIndices[i] == bd.column_number) {

               JOptionPane.showMessageDialog(this, "You must remove all existing bins on attribute '" +
               tbl.getColumnLabel(newIndices[i]) + "' before creating new ones.", "Error", JOptionPane.ERROR_MESSAGE);

               return false;

            }

         }

      }

      return true;

   }

	private boolean validateBins(DefaultListModel newBins) {
	   boolean match = false;
	   for (int binIdx = 0; binIdx < newBins.size(); binIdx++) {
		  if (!(columnLookup.containsKey(((BinDescriptor)newBins.get(binIdx)).label))) {
			 // ErrorDialog.showDialog("Current bins contain non-selected attributes. Please remove them.", "Error");
			 // System.out.println("no good: " + ((BinDescriptor)newBins.get(binIdx)).label);
			 return false;
		  }
	   }
	   return true;
	}

	private HashSet uniqueValues (int col) {
	  // count the number of unique items in this column
	  HashSet set = new HashSet();
          for (int i = 0; i < tbl.getNumRows(); i++) {

            //vered - added thsi condition, only if the value is not missing
            //then the unique value will be added.
            if(!tbl.isValueMissing(i, col)){
              String s = tbl.getString(i, col);
              if (!set.contains(s))
                set.add(s);
            }//if value is not missing
	  }//for i
	  return  set;
	}

	/**
	 * Get the column indices of the selected numeric columns.
	 */
	private int[] getSelectedNumericIndices () {
	  Object[] setVals = numericColumnLabels.getSelectedValues();
	  int[] colIdx = new int[setVals.length];
	  for (int i = 0; i < colIdx.length; i++)
		colIdx[i] = ((Integer)columnLookup.get(setVals[i])).intValue();
	  return  colIdx;
	}

	/**
	 * put your documentation comment here
	 * @param index
	 */
	private void setSelectedNumericIndex (int index) {
	  numericColumnLabels.setSelectedIndex(index);
	}

	/**
	 * Get the range of the first selected column.
	 */
	private double getInterval () {
// !:
// int colIdx = numericColumnLabels.getSelectedIndex();
	  int colIdx = ((Integer)columnLookup.get(numericColumnLabels.getSelectedValue())).intValue();
	  double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
	  for (int i = 0; i < tbl.getNumRows(); i++) {
		double d = tbl.getDouble(i, colIdx);
		if (d < min)
		  min = d;
		if (d > max)
		  max = d;
	  }
	  return  max - min;
	}

	/**
	 * Add uniform range bins
	 */
	private void addUniRange () {
	  int[] colIdx = getSelectedNumericIndices();

	  //vered
	  if(colIdx.length == 0){
		ErrorDialog.showDialog("Must select an attribute to bin.", "Error");
		return;
	  }

     if (!checkDuplicateNumericBins(colIdx)) {
        return;
     }

	  // uniform range is the number of bins...
	  String txt = uRangeField.getText();
	  int num;
	  // ...get this number
	  try {
		num = Integer.parseInt(txt);
		//vered:
		if(num<=1) throw new NumberFormatException();
	  } catch (NumberFormatException e) {
		//vered:
		ErrorDialog.showDialog("Must specify an integer greater thatn 1.", "Error");
		return;
	  }
	  // find the maxes and mins
	  double[] maxes = new double[colIdx.length];
	  double[] mins = new double[colIdx.length];
	  for (int i = 0; i < colIdx.length; i++) {
		maxes[i] = Double.NEGATIVE_INFINITY;
		mins[i] = Double.POSITIVE_INFINITY;
	  }
	  for (int i = 0; i < colIdx.length; i++) {
		// find the max and min and make equally spaced bins
		//NumericColumn nc = (NumericColumn)table.getColumn(colIdx[i]);
/*
		for (int j = 0; j < tbl.getNumRows(); j++) {

		   double  d = tbl.getDouble(j, colIdx[i]);

		  if (d > maxes[i])
			maxes[i] = d;
		  if (d < mins[i])
			mins[i] = d;
		} */
//		ANCA added support for missing  values
		ScalarStatistics ss = TableUtilities.getScalarStatistics(tbl, colIdx[i]);
		 maxes[i] = ss.getMaximum();
		mins[i] = ss.getMinimum();

		double[] binMaxes = new double[num - 1];
		double interval = (maxes[i] - mins[i])/(double)num;
		// add the first bin manually
		binMaxes[0] = mins[i] + interval;
		//ANCA BinDescriptor nbd = createMinNumericBinDescriptor(colIdx[i],binMaxes[0]);
		BinDescriptor nbd = BinDescriptorFactory.createMinNumericBinDescriptor(colIdx[i],binMaxes[0],nf,tbl);
		addItemToBinList(nbd);
		for (int j = 1; j < binMaxes.length; j++) {
		  binMaxes[j] = binMaxes[j - 1] + interval;
		//  System.out.println("bin Maxes " + binMaxes[j-1]);
		  // now create the BinDescriptor and add it to the bin list
		  //ANCA nbd = createNumericBinDescriptor(colIdx[i], binMaxes[j - 1], binMaxes[j]);
		  nbd = BinDescriptorFactory.createNumericBinDescriptor(colIdx[i], binMaxes[j - 1], binMaxes[j],nf,tbl);
		  addItemToBinList(nbd);
		}
		//ANCA nbd = createMaxNumericBinDescriptor(colIdx[i], binMaxes[binMaxes.length- 1]);
		nbd = BinDescriptorFactory.createMaxNumericBinDescriptor(colIdx[i], binMaxes[binMaxes.length- 1],nf,tbl);
		addItemToBinList(nbd);
	  }
	}

	/**
	 * Add bins from a user-specified range
	 */
	private void addSpecifiedRange () {
	  int[] colIdx = getSelectedNumericIndices();
	  //vered:
	  if(colIdx.length == 0){
		ErrorDialog.showDialog("You must select an attribute to bin.", "Error");
		return;
	  }

     if (!checkDuplicateNumericBins(colIdx)) {
        return;
     }

	  // specified range is a comma-separated list of bin maxes
	  String txt = specRangeField.getText();

	  //vered:
	  if(txt == null || txt.length() == 0) {
		ErrorDialog.showDialog("Please enter a comma-separated sequence of\ninteger or floating-point values for\nthe endpoints of each bin. ", "Error");
		return;
	  }

	  ArrayList al = new ArrayList();
	  StringTokenizer strTok = new StringTokenizer(txt, COMMA);
	  double[] binMaxes = new double[strTok.countTokens()];
	  int idx = 0;
	  try {
		while (strTok.hasMoreElements()) {
		  String s = (String)strTok.nextElement();
		  binMaxes[idx++] = Double.parseDouble(s);
		}
	  } catch (NumberFormatException e) {
		//vered
		ErrorDialog.showDialog("Please enter a comma-separated sequence of\ninteger or floating-point values for\nthe endpoints of each bin. ", "Error");
		return;
	  }
	  // now create and add the bins
	  for (int i = 0; i < colIdx.length; i++) {
		//ANCA BinDescriptor nbd = createMinNumericBinDescriptor(colIdx[i],binMaxes[0]);
		BinDescriptor nbd = BinDescriptorFactory.createMinNumericBinDescriptor(colIdx[i],binMaxes[0],nf,tbl);
		addItemToBinList(nbd);
		for (int j = 1; j < binMaxes.length; j++) {
		  // now create the BinDescriptor and add it to the bin list

		  //ANCA nbd = createNumericBinDescriptor(colIdx[i], binMaxes[j -1], binMaxes[j]);
		  nbd = BinDescriptorFactory.createNumericBinDescriptor(colIdx[i], binMaxes[j -1], binMaxes[j],nf,tbl);
		  addItemToBinList(nbd);
		}
		//ANCA nbd = createMaxNumericBinDescriptor(colIdx[i], binMaxes[binMaxes.length- 1]);
		nbd = BinDescriptorFactory.createMaxNumericBinDescriptor(colIdx[i], binMaxes[binMaxes.length- 1],nf,tbl);
		addItemToBinList(nbd);
	  }
	}

	/**
	 * Add bins from an interval - the width of each bin
	 */
	private void addFromInterval () {
	  int[] colIdx = getSelectedNumericIndices();
	  //vered:
	  if(colIdx.length == 0){
		ErrorDialog.showDialog("You must select an attribute to bin.", "Error");
		return;
	  }

     if (!checkDuplicateNumericBins(colIdx)) {
        return;
     }

	  // the interval is the width
	  String txt = intervalField.getText();
	  double intrval;
	  try {
		intrval = Double.parseDouble(txt);
	  } catch (NumberFormatException e) {
		//vered:
		ErrorDialog.showDialog("Must specify a positive number", "Error");
		return;
	  }

	  if (intrval <= 0) {
		ErrorDialog.showDialog("Must specify a positive number", "Error");
		return;
	  }

	  // find the mins and maxes
	  double[] maxes = new double[colIdx.length];
	  double[] mins = new double[colIdx.length];
	  for (int i = 0; i < colIdx.length; i++) {
		maxes[i] = Double.NEGATIVE_INFINITY;
		mins[i] = Double.POSITIVE_INFINITY;
	  }

	   for (int i = 0; i < colIdx.length; i++) {
		// find the max and min
		//NumericColumn nc = (NumericColumn)table.getColumn(colIdx[i]);
	/*	for (int j = 0; j < tbl.getNumRows(); j++) {
		  double d = tbl.getDouble(j, colIdx[i]);
		  if (d > maxes[i])
			maxes[i] = d;
		  if (d < mins[i])
			mins[i] = d;
	    }
	*/
      //ANCA added support for missing  values
		ScalarStatistics ss = TableUtilities.getScalarStatistics(tbl, colIdx[i]);
		 maxes[i] = ss.getMaximum();
		mins[i] = ss.getMinimum();



                //vered: (01-16-04) added this code to create the desired number of bins.
                                //not too few and not too many.

                   double up;
                   int idx;
                   Vector bounds = new Vector();
                   for(idx=0,up=mins[i]; up<maxes[i]; idx++, up+=intrval){
                     bounds.add(idx, new Double(up));
                   }

                   //now bounds holds the upper bounds for all bins except the last one.
                  //end of vered's code.


//vered: (01-16-04) commented out this line, to create exactly the desired number of bins
		// the number of bins is (max - min) / (bin width)
//		int num = (int)Math.round((maxes[i] - mins[i])/intrval);




		//System.out.println("column " + i + " max " + maxes[i] + " min " + mins[i] + " num " + num+ " original " +( maxes[i]-mins[i])/intrval + " interval " + intrval);

                //vered: (01-16-04)replaced *num* with *bounds.size()*
		double[] binMaxes = new double[/*num*/bounds.size()];


//vered: (01-16-04)replaced *mins[i]* with *((Double)bounds.get(0)).doubleValue()*
		binMaxes[0] = /*mins[i]*/ ((Double)bounds.get(0)).doubleValue();


		// add the first bin manually
		//ANCA BinDescriptor nbd = createMinNumericBinDescriptor(colIdx[i],binMaxes[0]);
		BinDescriptor nbd = BinDescriptorFactory.createMinNumericBinDescriptor(colIdx[i],binMaxes[0],nf,tbl);
		addItemToBinList(nbd);
		for (int j = 1; j < binMaxes.length; j++) {

                  //vered: (01-16-04)replaced *binMaxes[k - 1] + intrval* with *((Double)bounds.get(k)).doubleValue()*
                    binMaxes[j] = /*binMaxes[k - 1] + intrval*/ ((Double)bounds.get(j)).doubleValue();

		  //System.out.println("binMax j " + binMaxes[j] + " " + j);
		  // now create the BinDescriptor and add it to the bin list
		  //ANCA nbd = createNumericBinDescriptor(colIdx[i], binMaxes[j - 1], binMaxes[j]);
		  nbd = BinDescriptorFactory.createNumericBinDescriptor(colIdx[i], binMaxes[j - 1], binMaxes[j],nf,tbl);
		  addItemToBinList(nbd);
		}
		//ANCA nbd = createMaxNumericBinDescriptor(colIdx[i], binMaxes[binMaxes.length- 1]);
		nbd = BinDescriptorFactory.createMaxNumericBinDescriptor(colIdx[i], binMaxes[binMaxes.length- 1],nf,tbl);
		addItemToBinList(nbd);
	  }
	}

	/**
	 * Add bins given a weight. This will attempt to make bins with an
	 * equal number of tallies in each.
	 */
	private void addFromWeight () {
	  int[] colIdx = getSelectedNumericIndices();

	  //vered:
	  if(colIdx.length == 0){
		ErrorDialog.showDialog("You must select an attribute to bin.", "Error");
		return;
	  }

     if (!checkDuplicateNumericBins(colIdx)) {
        return;
     }

	  // the weight is the number of items in each bin
	  String txt = weightField.getText();
	  int weight;
	  try {
		weight = Integer.parseInt(txt);
		//vered:
		if(weight <= 0) throw new NumberFormatException();

	  } catch (NumberFormatException e) {
		//vered:
		ErrorDialog.showDialog("Must specify a positive integer", "Error");
		return;
	  }

	    // we need to sort the data, but do not want to sort the
	  // actual column, so we get a copy of the data
	  for (int i = 0; i < colIdx.length; i++) {
		//NumericColumn nc = (NumericColumn)table.getColumn(colIdx[i]);
		//ANCA added support for eliminating missing values when setting interval limits
		int missing =0;
		 if (tbl.getColumn(colIdx[i]).hasMissingValues())
		 missing = tbl.getColumn(colIdx[i]).getNumMissingValues();


		double[] data =  new double[tbl.getNumRows()-missing];


                //vered: (01-20-04) commented out next code line and made k
                //the exclusive index of data and j of tbl so the loop won't
                //end before all non-missing values from tbl are copied into data.
		//int k=0;
                //k index into data, j index into tbl.
		for (int k=0,j = 0; k < data.length && j<tbl.getNumRows(); j++) {

		 if (missing > 0) {
		 		 	if(!tbl.isValueMissing(j,colIdx[i])) {
		 		 		data[k++] = tbl.getDouble(j,colIdx[i]);
			//System.out.println("data for k-1 = " + (k-1) + " is " + data[k-1]);
		 	}
		 }
		 else {
		 		  data[k++] = tbl.getDouble(j, colIdx[i]);
		 			//System.out.println("data for j = " + j + " is " + data[j]);
		 }
		}
		// sort it
		Arrays.sort(data);



		ArrayList list = new ArrayList();
		// now find the bin maxes...
		// loop through the sorted data.  the next max will lie at
		// data[curLoc+weight] items

         //vered - changed curIdx from 0 to -1. this way, first bin won't be too large.
		int curIdx = -1;
		while (curIdx < data.length - 1) {



		  curIdx += weight;



		  if (curIdx > data.length - 1)
			curIdx = data.length - 1;
		  // now loop until the next unique item is found
		  boolean done = false;
		  if (curIdx == data.length - 1)
			done = true;

                    //vered - debug
                   System.out.println("curIdx = " + curIdx + " and points to element " +
                                      data[curIdx]);
                   //end debug

		  while (!done) {
			if (data[curIdx] != data[curIdx + 1])
			  done = true;
			else
			  curIdx++;
			if (curIdx == data.length - 1)
			  done = true;
		  }
		  // now we have the current index
		  // add the value at this index to the list
		  Double dbl = new Double(data[curIdx]);




		  list.add(dbl);
		}
		//System.out.println("BEFORE");
		double[] binMaxes = new double[list.size()];
		for (int j = 0; j < binMaxes.length; j++) {

		  binMaxes[j] = ((Double)list.get(j)).doubleValue();
		  //System.out.println("binmaxes for j = " + j + " is " +  binMaxes[j]);
		}

		if (binMaxes.length < 2) {
			 BinDescriptor nbd = BinDescriptorFactory.createMinMaxBinDescriptor(colIdx[i],tbl);
				addItemToBinList(nbd);
	 } else { // binMaxes has more than one element

		// add the first bin manually
		//ANCA BinDescriptor nbd = createMinNumericBinDescriptor(colIdx[i],binMaxes[0]);
		BinDescriptor nbd = BinDescriptorFactory.createMinNumericBinDescriptor(colIdx[i],binMaxes[0],nf,tbl);
		addItemToBinList(nbd);
		for (int j = 1; j < binMaxes.length-1; j++) {
		  // now create the BinDescriptor and add it to the bin list
		  //ANCA nbd = createNumericBinDescriptor(colIdx[i], binMaxes[j - 1], binMaxes[j]);
		  nbd = BinDescriptorFactory.createNumericBinDescriptor(colIdx[i], binMaxes[j - 1], binMaxes[j],nf,tbl);
		  addItemToBinList(nbd);
		}
		//ANCA nbd = createMaxNumericBinDescriptor(colIdx[i], binMaxes[binMaxes.length- 2]);
		//if (binMaxes.length>2)
		 nbd = BinDescriptorFactory.createMaxNumericBinDescriptor(colIdx[i], binMaxes[binMaxes.length- 2],nf,tbl);
		//else
		  //nbd = BinDescriptorFactory.createMaxNumericBinDescriptor(colIdx[i], binMaxes[0],nf,tbl);
		addItemToBinList(nbd);
		}
	  }
	}

	/**
	 * put your documentation comment here
	 * @param idx
	 * @param name
	 * @param sel
	 * @return
	 */
	private BinDescriptor createTextualBin (int idx, String name, Object[] sel) {
	  String[] vals = new String[sel.length];
	  	  for (int i = 0; i < vals.length; i++)
		vals[i] = sel[i].toString();
	  return  new TextualBinDescriptor(idx, name, vals, tbl.getColumnLabel(idx));
	}
//
//	/**
//	 * Create a numeric bin that goes from min to max.
//	 */
//	private BinDescriptor createNumericBinDescriptor (int col, double min,
//		double max) {
//      System.out.println(" min " + min + " max " + max);
//	  StringBuffer nameBuffer = new StringBuffer();
//	  nameBuffer.append(OPEN_PAREN);
//	  //ANCA nameBuffer.append(nf.format(min));
//	  nameBuffer.append(min);
//	  nameBuffer.append(COLON);
//	  //ANCA nameBuffer.append(nf.format(max));
//	  nameBuffer.append(max);
//	  nameBuffer.append(CLOSE_BRACKET);
//	  BinDescriptor nb = new NumericBinDescriptor(col, nameBuffer.toString(),
//		  min, max, tbl.getColumnLabel(col));
//	  return  nb;
//	}
//
//	/**
//	 * Create a numeric bin that goes from Double.NEGATIVE_INFINITY to max
//	 */
//	private BinDescriptor createMinNumericBinDescriptor (int col, double max) {
//	  StringBuffer nameBuffer = new StringBuffer();
//	  nameBuffer.append(OPEN_BRACKET);
//	  nameBuffer.append(DOTS);
//	  nameBuffer.append(COLON);
//	  //ANCA nameBuffer.append(nf.format(max));
//	  nameBuffer.append(max);
//	  nameBuffer.append(CLOSE_BRACKET);
//	  BinDescriptor nb = new NumericBinDescriptor(col, nameBuffer.toString(),
//		  Double.NEGATIVE_INFINITY, max, tbl.getColumnLabel(col));
//	  return  nb;
//	}
//
//	/**
//	 * Create a numeric bin that goes from min to Double.POSITIVE_INFINITY
//	 */
//	private BinDescriptor createMaxNumericBinDescriptor (int col, double min) {
//	  StringBuffer nameBuffer = new StringBuffer();
//	  nameBuffer.append(OPEN_PAREN);
//	 //ANCA nameBuffer.append(nf.format(min));
//	 nameBuffer.append(min);
//	  nameBuffer.append(COLON);
//	  nameBuffer.append(DOTS);
//	  nameBuffer.append(CLOSE_BRACKET);
//	  BinDescriptor nb = new NumericBinDescriptor(col, nameBuffer.toString(),
//		  min, Double.POSITIVE_INFINITY, tbl.getColumnLabel(col));
//	  return  nb;
//	}

	/**
	 * put your documentation comment here
	 * @param bd
	 */
	private void addItemToBinList (BinDescriptor bd) {
	  binListModel.addElement(bd);
	}

	private class CurrentListener
		implements ListSelectionListener {

	  /**
	   * put your documentation comment here
	   * @param e
	   */
	  public void valueChanged (ListSelectionEvent e) {
		if (!setup_complete)
		  return;
		if (!e.getValueIsAdjusting()) {
		  currentSelectedBin = (BinDescriptor)currentBins.getSelectedValue();
		  if (currentSelectedBin == null) {
			currentSelectionModel.removeAllElements();
			curSelName.setText(EMPTY);
			return;
		  }
		  curSelName.setText(currentSelectedBin.name);
		  if (currentSelectedBin instanceof NumericBinDescriptor) {
			currentSelectionModel.removeAllElements();
			currentSelectionModel.addElement(currentSelectedBin.name);
		  }
		  else {
			currentSelectionModel.removeAllElements();
			HashSet hs = (HashSet)((TextualBinDescriptor)currentSelectedBin).vals;
			Iterator i = hs.iterator();
			while (i.hasNext())
			  currentSelectionModel.addElement(i.next());
		  }
		}
	  }
	}       // BinColumnsView$CurrentListener

	private class TextualListener
		implements ListSelectionListener {

	  /**
	   * put your documentation comment here
	   * @param e
	   */
	  public void valueChanged (ListSelectionEvent e) {
		if (!setup_complete)
		  return;
		if (!e.getValueIsAdjusting()) {
		  Object lbl = textualColumnLabels.getSelectedValue();
		  if (lbl != null) {
			int idx = ((Integer)columnLookup.get(lbl)).intValue();
			HashSet unique = uniqueColumnValues[idx];
			textUniqueModel.removeAllElements();
			textCurrentModel.removeAllElements();
			Iterator i = unique.iterator();
			while (i.hasNext())
			  textUniqueModel.addElement(i.next());
		  }
		}
	  }
	}       // BinColumnsView$TextualListener

	private class HelpWindow extends JD2KFrame {

	  /**
	   * put your documentation comment here
	   */
	  public HelpWindow () {
		super("About Bin Attributes");
		JEditorPane ep = new JEditorPane("text/html", getHelpString());
		ep.setCaretPosition(0);
		getContentPane().add(new JScrollPane(ep));
		setSize(400, 400);
	  }
	}

	/**
	 * put your documentation comment here
	 * @return
	 */
	private String getHelpString () {
	  StringBuffer sb = new StringBuffer();
	  sb.append("<html><body><h2>Bin Attributes</h2>");
	  sb.append("This module allows a user to interactively bin data from a table. ");
	  sb.append("Scalar data can be binned in four ways:<ul>");
	  sb.append("<li><b>Uniform range</b><br>");
	  sb.append("Enter a positive integer value for the number of bins. Bin Attributes will ");
	  sb.append("divide the binning range evenly over these bins.");
	  sb.append("<br><li><b>Specified range</b>:<br>");
	  sb.append("Enter a comma-separated sequence of integer or floating-point values ");
	  sb.append("for the endpoints of each bin.");
	  sb.append("<br><li><b>Bin Interval</b>:<br>");
	  sb.append("Enter an integer or floating-point value for the width of each bin.");
	  sb.append("<br><li><b>Uniform Weight</b>:<br>");
	  sb.append("Enter a positive integer value for even binning with that number in each bin.");
	  sb.append("</ul>");
	  sb.append("To bin scalar data, select attributes from the \"Scalar Attributes\" ");
	  sb.append("selection area (top left) and select a binning method by entering a value ");
	  sb.append("in the corresponding text field and clicking \"Add\". Data can ");
	  sb.append("alternately be previewed in histogram form by clicking the corresponding ");
	  sb.append("\"Show\" button (this accepts, but does not require, a value in the text field). ");
	  sb.append("Uniform weight binning has no histogram (it would always look the same).");
	  sb.append("<br><br>To bin nominal data, click the \"Nominal\" tab (top left) to bring ");
	  sb.append("up the \"Nominal Attributes\" selection area. Click on an attibute to show a list ");
	  sb.append("of unique nominal values in that attribute in the \"Unique Values\" area below. ");
	  sb.append("Select one or more of these values and click the right arrow button to group ");
	  sb.append("these values. They can then be assigned a collective name as before.");
	  sb.append("<br><br>To assign a name to a particular bin, select that bin in ");
	  sb.append("the \"Current Bins\" selection area (top right), enter a name in ");
	  sb.append("the \"Name\" field below, and click \"Update\". To bin the data and ");
	  sb.append("output the new table, click \"Done\".");
	  sb.append("</body></html>");
	  return  sb.toString();
	}
  }           // BinColumnsView


  //headless conversion support
  public void doit()throws Exception{
     Table table = (Table) pullInput(0);
    //BinningUtils.validateBins(table, savedBins, getAlias());
     pushOutput(new BinTransform(table, savedBins, newColumn), 0);

  }

  private boolean newColumn;
  public void setNewColumn(boolean val){newColumn = val;}
  public boolean getNewColumn(){return newColumn;}

  //headless conversion support
}//BinAttributes








class TableBinCounts implements BinCounts {

  private Table table;
  double[][] minMaxes;

  private static final int MIN = 0;
  private static final int MAX = 1;

  public TableBinCounts(Table t) {
	table = t;
	minMaxes = new double[table.getNumColumns()][];
	for(int i = 0; i < minMaxes.length; i++) {
	  if(table.isColumnNumeric(i)) {
		minMaxes[i] = new double[2];

		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;

		// get the max and min
		for (int j = 0; j < table.getNumRows(); j++) {
		  if (table.getDouble(j, i) < min)
			min = table.getDouble(j, i);
		  if (table.getDouble(j, i) > max)
			max = table.getDouble(j, i);
		}

		minMaxes[i][MIN] = min;
		minMaxes[i][MAX] = max;
	  }
	}
  }

  public int getNumRows() {
	return table.getNumRows();
  }

  public double getMin(int col) {
	return minMaxes[col][MIN];
  }

  public double getMax(int col) {
	return minMaxes[col][MAX];
  }

  public double getTotal(int col) {
	double tot = 0;
	for(int i = 0; i < table.getNumRows(); i++)
	  tot += table.getDouble(i, col);
	return tot;
  }

  public int[] getCounts(int col, double[] borders) {
	int[] counts = new int[borders.length+1];

	// some redundancy here
	boolean found;
	for (int i = 0; i < table.getNumRows(); i++) {
	  found = false;
	  for (int j = 0; j < borders.length; j++) {
		if (table.getDouble(i, col) <= borders[j] && !found) {
		  counts[j]++;
		  found = true;
		  break;
		}
	  }
	  if (!found)
		counts[borders.length]++;
	}

	return counts;
  }
}

//	QA comments:
// 2-27-03 vered started qa. added module description, exception handling.
// 2-27-03 commit back to core and back to greg - to reveiw bin nominal columns tab.
// 3-6-03 added to module info a note about missing values handling.
// 3-17 -3 anca: changed last bin in addFromWeigth
// 3-24-03 ruth: changed QA comment char so that they won't be seen by JavaDocs
//				 changed input type to Table; deleted output Table port; updated
//				 output port name; updated descriptions; added getPropertiesDescriptions
//				 so no properties a user can't edit are shown.
//

/**
 * 12-02-03 vered started qa.
 *          modules does not bin missing scalar values into "UNKNOWN" bin. [bug 140]
 *          modules allows overlapping binning of same column. [bug 142]
 *
 * 12-03-03 incorrect uniform binning [bug 153]
 *          missing values are binned as real values [bug 150]
 */

/** 12-04-03 anca added "unknown" bins for columns that have missing values
 *               changed  marked with ANCA
 * 			- fixed [bug 140], [bug 150]
*/

/** 12-05-03 - anca - fixed [bug 153]  - changes marked with ANCA
 *              The problem: display of double values using NumberFormat.
*               the margins of the interval for uniform binning are computed
* 				using (max-min)/ number of internals and thus what appears
*               as 3.6 interval margin is in fact 3.5999996. Without NumberFormat nf
* 				the 3.59999996 will be displayed and there will be no confusion.
*
* 12 -16-03 Anca moved creation of "unknown" bins to BinTransform
 *
*/

/**
 * 12-23-03: Vered - bug 142 was partially fixed. only regarding scalar columns.
 *           regarding nominal columns - via secodn run with gui, one may re-bin
 *           the nominal column which was binned in previous run. [bug 174]
 *
 *           bug 178 - with interval binning, creates one bin too many. that happens
 *           when the interval values devided (max-min) into an integer... (fixed)
 *
 *           bug 179 - array index out of bounds exception with weight binning.
 *
 * 01-08-04: vered.
  * user may create bins with identical names in same attribute - bug 207.


   * 01-16-04: vered.
   * changed the way bin maxes are computed in the addFromInterval method. now it is Math methods independant.
   * all code lines that were changed or added are preceeded by a comment line "//vered"
   * and description of change.
 *
 * 01-20-04: vered
 * changed indexing in method addFromWeight, so that data and tbl each has
 * its exclusive index. beforehand - j and k were serving as interchangeably
 * as index into data, which made the last items in data to be left as zeros
 * if the column has missing values.
 *
 * 01-21-04: vered
 * bug 207 is fixed.
 *
 * bug 228: binning and representation of missing values. missing values are binned
 * into the "UNKNOWN" bin but are still marked as missing if binning is done
 * in the same column. (fixed)
 *
 * bug 229 - when checking the "create in a new column" box, the module creates
   the new binned columns with identical labels as the original ones have. (fixed)


   bug 235 - module does not allow creation of bins with identical names even if
   bins belong to different attributes.

 *
*/
=======
package redis.clients.jedis;

import static redis.clients.jedis.Protocol.toByteArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.JedisByteHashMap;
import redis.clients.util.SafeEncoder;

public class BinaryJedis implements BinaryJedisCommands {
    protected Client client = null;

    public BinaryJedis(final String host) {
	client = new Client(host);
    }

    public BinaryJedis(final String host, final int port) {
	client = new Client(host, port);
    }

    public BinaryJedis(final String host, final int port, final int timeout) {
	client = new Client(host, port);
	client.setTimeout(timeout);
    }

    public BinaryJedis(final JedisShardInfo shardInfo) {
	client = new Client(shardInfo.getHost(), shardInfo.getPort());
	client.setTimeout(shardInfo.getTimeout());
	client.setPassword(shardInfo.getPassword());
    }

    public String ping() {
	checkIsInMulti();
	client.ping();
	return client.getStatusCodeReply();
    }

    /**
     * Set the string value as value of the key. The string can't be longer than
     * 1073741824 bytes (1 GB).
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Status code reply
     */
    public String set(final byte[] key, final byte[] value) {
	checkIsInMulti();
	client.set(key, value);
	return client.getStatusCodeReply();
    }

    /**
     * Get the value of the specified key. If the key does not exist the special
     * value 'nil' is returned. If the value stored at key is not a string an
     * error is returned because GET can only handle string values.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public byte[] get(final byte[] key) {
	checkIsInMulti();
	client.get(key);
	return client.getBinaryBulkReply();
    }

    /**
     * Ask the server to silently close the connection.
     */
    public String quit() {
	checkIsInMulti();
	client.quit();
	return client.getStatusCodeReply();
    }

    /**
     * Test if the specified key exists. The command returns "1" if the key
     * exists, otherwise "0" is returned. Note that even keys set with an empty
     * string as value will return "1".
     * 
     * Time complexity: O(1)
     * 
     * @param key
     * @return Integer reply, "1" if the key exists, otherwise "0"
     */
    public Boolean exists(final byte[] key) {
	checkIsInMulti();
	client.exists(key);
	return client.getIntegerReply() == 1;
    }

    /**
     * Remove the specified keys. If a given key does not exist no operation is
     * performed for this key. The command returns the number of keys removed.
     * 
     * Time complexity: O(1)
     * 
     * @param keys
     * @return Integer reply, specifically: an integer greater than 0 if one or
     *         more keys were removed 0 if none of the specified key existed
     */
    public Long del(final byte[]... keys) {
	checkIsInMulti();
	client.del(keys);
	return client.getIntegerReply();
    }

    /**
     * Return the type of the value stored at key in form of a string. The type
     * can be one of "none", "string", "list", "set". "none" is returned if the
     * key does not exist.
     * 
     * Time complexity: O(1)
     * 
     * @param key
     * @return Status code reply, specifically: "none" if the key does not exist
     *         "string" if the key contains a String value "list" if the key
     *         contains a List value "set" if the key contains a Set value
     *         "zset" if the key contains a Sorted Set value "hash" if the key
     *         contains a Hash value
     */
    public String type(final byte[] key) {
	checkIsInMulti();
	client.type(key);
	return client.getStatusCodeReply();
    }

    /**
     * Delete all the keys of the currently selected DB. This command never
     * fails.
     * 
     * @return Status code reply
     */
    public String flushDB() {
	checkIsInMulti();
	client.flushDB();
	return client.getStatusCodeReply();
    }

    /**
     * Returns all the keys matching the glob-style pattern as space separated
     * strings. For example if you have in the database the keys "foo" and
     * "foobar" the command "KEYS foo*" will return "foo foobar".
     * <p>
     * Note that while the time complexity for this operation is O(n) the
     * constant times are pretty low. For example Redis running on an entry
     * level laptop can scan a 1 million keys database in 40 milliseconds.
     * <b>Still it's better to consider this one of the slow commands that may
     * ruin the DB performance if not used with care.</b>
     * <p>
     * In other words this command is intended only for debugging and special
     * operations like creating a script to change the DB schema. Don't use it
     * in your normal code. Use Redis Sets in order to group together a subset
     * of objects.
     * <p>
     * Glob style patterns examples:
     * <ul>
     * <li>h?llo will match hello hallo hhllo
     * <li>h*llo will match hllo heeeello
     * <li>h[ae]llo will match hello and hallo, but not hillo
     * </ul>
     * <p>
     * Use \ to escape special chars if you want to match them verbatim.
     * <p>
     * Time complexity: O(n) (with n being the number of keys in the DB, and
     * assuming keys and pattern of limited length)
     * 
     * @param pattern
     * @return Multi bulk reply
     */
    public Set<byte[]> keys(final byte[] pattern) {
	checkIsInMulti();
	client.keys(pattern);
	final HashSet<byte[]> keySet = new HashSet<byte[]>(
		client.getBinaryMultiBulkReply());
	return keySet;
    }

    /**
     * Return a randomly selected key from the currently selected DB.
     * <p>
     * Time complexity: O(1)
     * 
     * @return Singe line reply, specifically the randomly selected key or an
     *         empty string is the database is empty
     */
    public byte[] randomBinaryKey() {
	checkIsInMulti();
	client.randomKey();
	return client.getBinaryBulkReply();
    }

    /**
     * Atomically renames the key oldkey to newkey. If the source and
     * destination name are the same an error is returned. If newkey already
     * exists it is overwritten.
     * <p>
     * Time complexity: O(1)
     * 
     * @param oldkey
     * @param newkey
     * @return Status code repy
     */
    public String rename(final byte[] oldkey, final byte[] newkey) {
	checkIsInMulti();
	client.rename(oldkey, newkey);
	return client.getStatusCodeReply();
    }

    /**
     * Rename oldkey into newkey but fails if the destination key newkey already
     * exists.
     * <p>
     * Time complexity: O(1)
     * 
     * @param oldkey
     * @param newkey
     * @return Integer reply, specifically: 1 if the key was renamed 0 if the
     *         target key already exist
     */
    public Long renamenx(final byte[] oldkey, final byte[] newkey) {
	checkIsInMulti();
	client.renamenx(oldkey, newkey);
	return client.getIntegerReply();
    }

    /**
     * Return the number of keys in the currently selected database.
     * 
     * @return Integer reply
     */
    public Long dbSize() {
	checkIsInMulti();
	client.dbSize();
	return client.getIntegerReply();
    }

    /**
     * Set a timeout on the specified key. After the timeout the key will be
     * automatically deleted by the server. A key with an associated timeout is
     * said to be volatile in Redis terminology.
     * <p>
     * Voltile keys are stored on disk like the other keys, the timeout is
     * persistent too like all the other aspects of the dataset. Saving a
     * dataset containing expires and stopping the server does not stop the flow
     * of time as Redis stores on disk the time when the key will no longer be
     * available as Unix time, and not the remaining seconds.
     * <p>
     * Since Redis 2.1.3 you can update the value of the timeout of a key
     * already having an expire set. It is also possible to undo the expire at
     * all turning the key into a normal key using the {@link #persist(byte[])
     * PERSIST} command.
     * <p>
     * Time complexity: O(1)
     * 
     * @see <ahref="http://code.google.com/p/redis/wiki/ExpireCommand">ExpireCommand</a>
     * 
     * @param key
     * @param seconds
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */
    public Long expire(final byte[] key, final int seconds) {
	checkIsInMulti();
	client.expire(key, seconds);
	return client.getIntegerReply();
    }

    /**
     * EXPIREAT works exctly like {@link #expire(byte[], int) EXPIRE} but
     * instead to get the number of seconds representing the Time To Live of the
     * key as a second argument (that is a relative way of specifing the TTL),
     * it takes an absolute one in the form of a UNIX timestamp (Number of
     * seconds elapsed since 1 Gen 1970).
     * <p>
     * EXPIREAT was introduced in order to implement the Append Only File
     * persistence mode so that EXPIRE commands are automatically translated
     * into EXPIREAT commands for the append only file. Of course EXPIREAT can
     * also used by programmers that need a way to simply specify that a given
     * key should expire at a given time in the future.
     * <p>
     * Since Redis 2.1.3 you can update the value of the timeout of a key
     * already having an expire set. It is also possible to undo the expire at
     * all turning the key into a normal key using the {@link #persist(byte[])
     * PERSIST} command.
     * <p>
     * Time complexity: O(1)
     * 
     * @see <ahref="http://code.google.com/p/redis/wiki/ExpireCommand">ExpireCommand</a>
     * 
     * @param key
     * @param unixTime
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */
    public Long expireAt(final byte[] key, final long unixTime) {
	checkIsInMulti();
	client.expireAt(key, unixTime);
	return client.getIntegerReply();
    }

    /**
     * The TTL command returns the remaining time to live in seconds of a key
     * that has an {@link #expire(byte[], int) EXPIRE} set. This introspection
     * capability allows a Redis client to check how many seconds a given key
     * will continue to be part of the dataset.
     * 
     * @param key
     * @return Integer reply, returns the remaining time to live in seconds of a
     *         key that has an EXPIRE. If the Key does not exists or does not
     *         have an associated expire, -1 is returned.
     */
    public Long ttl(final byte[] key) {
	checkIsInMulti();
	client.ttl(key);
	return client.getIntegerReply();
    }

    /**
     * Select the DB with having the specified zero-based numeric index. For
     * default every new client connection is automatically selected to DB 0.
     * 
     * @param index
     * @return Status code reply
     */
    public String select(final int index) {
	checkIsInMulti();
	client.select(index);
	return client.getStatusCodeReply();
    }

    /**
     * Move the specified key from the currently selected DB to the specified
     * destination DB. Note that this command returns 1 only if the key was
     * successfully moved, and 0 if the target key was already there or if the
     * source key was not found at all, so it is possible to use MOVE as a
     * locking primitive.
     * 
     * @param key
     * @param dbIndex
     * @return Integer reply, specifically: 1 if the key was moved 0 if the key
     *         was not moved because already present on the target DB or was not
     *         found in the current DB.
     */
    public Long move(final byte[] key, final int dbIndex) {
	checkIsInMulti();
	client.move(key, dbIndex);
	return client.getIntegerReply();
    }

    /**
     * Delete all the keys of all the existing databases, not just the currently
     * selected one. This command never fails.
     * 
     * @return Status code reply
     */
    public String flushAll() {
	checkIsInMulti();
	client.flushAll();
	return client.getStatusCodeReply();
    }

    /**
     * GETSET is an atomic set this value and return the old value command. Set
     * key to the string value and return the old value stored at key. The
     * string can't be longer than 1073741824 bytes (1 GB).
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Bulk reply
     */
    public byte[] getSet(final byte[] key, final byte[] value) {
	checkIsInMulti();
	client.getSet(key, value);
	return client.getBinaryBulkReply();
    }

    /**
     * Get the values of all the specified keys. If one or more keys dont exist
     * or is not of type String, a 'nil' value is returned instead of the value
     * of the specified key, but the operation never fails.
     * <p>
     * Time complexity: O(1) for every key
     * 
     * @param keys
     * @return Multi bulk reply
     */
    public List<byte[]> mget(final byte[]... keys) {
	checkIsInMulti();
	client.mget(keys);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * SETNX works exactly like {@link #set(byte[], byte[]) SET} with the only
     * difference that if the key already exists no operation is performed.
     * SETNX actually means "SET if Not eXists".
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Integer reply, specifically: 1 if the key was set 0 if the key
     *         was not set
     */
    public Long setnx(final byte[] key, final byte[] value) {
	checkIsInMulti();
	client.setnx(key, value);
	return client.getIntegerReply();
    }

    /**
     * The command is exactly equivalent to the following group of commands:
     * {@link #set(byte[], byte[]) SET} + {@link #expire(byte[], int) EXPIRE}.
     * The operation is atomic.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param seconds
     * @param value
     * @return Status code reply
     */
    public String setex(final byte[] key, final int seconds, final byte[] value) {
	checkIsInMulti();
	client.setex(key, seconds, value);
	return client.getStatusCodeReply();
    }

    /**
     * Set the the respective keys to the respective values. MSET will replace
     * old values with new values, while {@link #msetnx(String...) MSETNX} will
     * not perform any operation at all even if just a single key already
     * exists.
     * <p>
     * Because of this semantic MSETNX can be used in order to set different
     * keys representing different fields of an unique logic object in a way
     * that ensures that either all the fields or none at all are set.
     * <p>
     * Both MSET and MSETNX are atomic operations. This means that for instance
     * if the keys A and B are modified, another client talking to Redis can
     * either see the changes to both A and B at once, or no modification at
     * all.
     * 
     * @see #msetnx(String...)
     * 
     * @param keysvalues
     * @return Status code reply Basically +OK as MSET can't fail
     */
    public String mset(final byte[]... keysvalues) {
	checkIsInMulti();
	client.mset(keysvalues);
	return client.getStatusCodeReply();
    }

    /**
     * Set the the respective keys to the respective values.
     * {@link #mset(String...) MSET} will replace old values with new values,
     * while MSETNX will not perform any operation at all even if just a single
     * key already exists.
     * <p>
     * Because of this semantic MSETNX can be used in order to set different
     * keys representing different fields of an unique logic object in a way
     * that ensures that either all the fields or none at all are set.
     * <p>
     * Both MSET and MSETNX are atomic operations. This means that for instance
     * if the keys A and B are modified, another client talking to Redis can
     * either see the changes to both A and B at once, or no modification at
     * all.
     * 
     * @see #mset(String...)
     * 
     * @param keysvalues
     * @return Integer reply, specifically: 1 if the all the keys were set 0 if
     *         no key was set (at least one key already existed)
     */
    public Long msetnx(final byte[]... keysvalues) {
	checkIsInMulti();
	client.msetnx(keysvalues);
	return client.getIntegerReply();
    }

    /**
     * IDECRBY work just like {@link #decr(String) INCR} but instead to
     * decrement by 1 the decrement is integer.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(byte[])
     * @see #decr(byte[])
     * @see #incrBy(byte[], long)
     * 
     * @param key
     * @param integer
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long decrBy(final byte[] key, final long integer) {
	checkIsInMulti();
	client.decrBy(key, integer);
	return client.getIntegerReply();
    }

    /**
     * Decrement the number stored at key by one. If the key does not exist or
     * contains a value of a wrong type, set the key to the value of "0" before
     * to perform the decrement operation.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(byte[])
     * @see #incrBy(byte[], long)
     * @see #decrBy(byte[], long)
     * 
     * @param key
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long decr(final byte[] key) {
	checkIsInMulti();
	client.decr(key);
	return client.getIntegerReply();
    }

    /**
     * INCRBY work just like {@link #incr(byte[]) INCR} but instead to increment
     * by 1 the increment is integer.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(byte[])
     * @see #decr(byte[])
     * @see #decrBy(byte[], long)
     * 
     * @param key
     * @param integer
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long incrBy(final byte[] key, final long integer) {
	checkIsInMulti();
	client.incrBy(key, integer);
	return client.getIntegerReply();
    }

    /**
     * Increment the number stored at key by one. If the key does not exist or
     * contains a value of a wrong type, set the key to the value of "0" before
     * to perform the increment operation.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incrBy(byte[], long)
     * @see #decr(byte[])
     * @see #decrBy(byte[], long)
     * 
     * @param key
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long incr(final byte[] key) {
	checkIsInMulti();
	client.incr(key);
	return client.getIntegerReply();
    }

    /**
     * If the key already exists and is a string, this command appends the
     * provided value at the end of the string. If the key does not exist it is
     * created and set as an empty string, so APPEND will be very similar to SET
     * in this special case.
     * <p>
     * Time complexity: O(1). The amortized time complexity is O(1) assuming the
     * appended value is small and the already present value is of any size,
     * since the dynamic string library used by Redis will double the free space
     * available on every reallocation.
     * 
     * @param key
     * @param value
     * @return Integer reply, specifically the total length of the string after
     *         the append operation.
     */
    public Long append(final byte[] key, final byte[] value) {
	checkIsInMulti();
	client.append(key, value);
	return client.getIntegerReply();
    }

    /**
     * Return a subset of the string from offset start to offset end (both
     * offsets are inclusive). Negative offsets can be used in order to provide
     * an offset starting from the end of the string. So -1 means the last char,
     * -2 the penultimate and so forth.
     * <p>
     * The function handles out of range requests without raising an error, but
     * just limiting the resulting range to the actual length of the string.
     * <p>
     * Time complexity: O(start+n) (with start being the start index and n the
     * total length of the requested range). Note that the lookup part of this
     * command is O(1) so for small strings this is actually an O(1) command.
     * 
     * @param key
     * @param start
     * @param end
     * @return Bulk reply
     */
    public byte[] substr(final byte[] key, final int start, final int end) {
	checkIsInMulti();
	client.substr(key, start, end);
	return client.getBinaryBulkReply();
    }

    /**
     * 
     * Set the specified hash field to the specified value.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return If the field already exists, and the HSET just produced an update
     *         of the value, 0 is returned, otherwise if a new field is created
     *         1 is returned.
     */
    public Long hset(final byte[] key, final byte[] field, final byte[] value) {
	checkIsInMulti();
	client.hset(key, field, value);
	return client.getIntegerReply();
    }

    /**
     * If key holds a hash, retrieve the value associated to the specified
     * field.
     * <p>
     * If the field is not found or the key does not exist, a special 'nil'
     * value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @return Bulk reply
     */
    public byte[] hget(final byte[] key, final byte[] field) {
	checkIsInMulti();
	client.hget(key, field);
	return client.getBinaryBulkReply();
    }

    /**
     * 
     * Set the specified hash field to the specified value if the field not
     * exists. <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return If the field already exists, 0 is returned, otherwise if a new
     *         field is created 1 is returned.
     */
    public Long hsetnx(final byte[] key, final byte[] field, final byte[] value) {
	checkIsInMulti();
	client.hsetnx(key, field, value);
	return client.getIntegerReply();
    }

    /**
     * Set the respective fields to the respective values. HMSET replaces old
     * values with new values.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param key
     * @param hash
     * @return Always OK because HMSET can't fail
     */
    public String hmset(final byte[] key, final Map<byte[], byte[]> hash) {
	checkIsInMulti();
	client.hmset(key, hash);
	return client.getStatusCodeReply();
    }

    /**
     * Retrieve the values associated to the specified fields.
     * <p>
     * If some of the specified fields do not exist, nil values are returned.
     * Non existing keys are considered like empty hashes.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param key
     * @param fields
     * @return Multi Bulk Reply specifically a list of all the values associated
     *         with the specified fields, in the same order of the request.
     */
    public List<byte[]> hmget(final byte[] key, final byte[]... fields) {
	checkIsInMulti();
	client.hmget(key, fields);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * Increment the number stored at field in the hash at key by value. If key
     * does not exist, a new key holding a hash is created. If field does not
     * exist or holds a string, the value is set to 0 before applying the
     * operation. Since the value argument is signed you can use this command to
     * perform both increments and decrements.
     * <p>
     * The range of values supported by HINCRBY is limited to 64 bit signed
     * integers.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return Integer reply The new value at field after the increment
     *         operation.
     */
    public Long hincrBy(final byte[] key, final byte[] field, final long value) {
	checkIsInMulti();
	client.hincrBy(key, field, value);
	return client.getIntegerReply();
    }

    /**
     * Test for existence of a specified field in a hash.
     * 
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @return Return 1 if the hash stored at key contains the specified field.
     *         Return 0 if the key is not found or the field is not present.
     */
    public Boolean hexists(final byte[] key, final byte[] field) {
	checkIsInMulti();
	client.hexists(key, field);
	return client.getIntegerReply() == 1;
    }

    /**
     * Remove the specified field from an hash stored at key.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param fields
     * @return If the field was present in the hash it is deleted and 1 is
     *         returned, otherwise 0 is returned and no operation is performed.
     */
    public Long hdel(final byte[] key, final byte[]... fields) {
	checkIsInMulti();
	client.hdel(key, fields);
	return client.getIntegerReply();
    }

    /**
     * Return the number of items in a hash.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @return The number of entries (fields) contained in the hash stored at
     *         key. If the specified key does not exist, 0 is returned assuming
     *         an empty hash.
     */
    public Long hlen(final byte[] key) {
	checkIsInMulti();
	client.hlen(key);
	return client.getIntegerReply();
    }

    /**
     * Return all the fields in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields names contained into a hash.
     */
    public Set<byte[]> hkeys(final byte[] key) {
	checkIsInMulti();
	client.hkeys(key);
	final List<byte[]> lresult = client.getBinaryMultiBulkReply();
	return new HashSet<byte[]>(lresult);
    }

    /**
     * Return all the values in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields values contained into a hash.
     */
    public List<byte[]> hvals(final byte[] key) {
	checkIsInMulti();
	client.hvals(key);
	final List<byte[]> lresult = client.getBinaryMultiBulkReply();
	return lresult;
    }

    /**
     * Return all the fields and associated values in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields and values contained into a hash.
     */
    public Map<byte[], byte[]> hgetAll(final byte[] key) {
	checkIsInMulti();
	client.hgetAll(key);
	final List<byte[]> flatHash = client.getBinaryMultiBulkReply();
	final Map<byte[], byte[]> hash = new JedisByteHashMap();
	final Iterator<byte[]> iterator = flatHash.iterator();
	while (iterator.hasNext()) {
	    hash.put(iterator.next(), iterator.next());
	}

	return hash;
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
     * stored at key. If the key does not exist an empty list is created just
     * before the append operation. If the key exists but is not a List an error
     * is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @see BinaryJedis#rpush(byte[], byte[]...)
     * 
     * @param key
     * @param strings
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */
    public Long rpush(final byte[] key, final byte[]... strings) {
	checkIsInMulti();
	client.rpush(key, strings);
	return client.getIntegerReply();
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
     * stored at key. If the key does not exist an empty list is created just
     * before the append operation. If the key exists but is not a List an error
     * is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @see BinaryJedis#rpush(byte[], byte[]...)
     * 
     * @param key
     * @param strings
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */
    public Long lpush(final byte[] key, final byte[]... strings) {
	checkIsInMulti();
	client.lpush(key, strings);
	return client.getIntegerReply();
    }

    /**
     * Return the length of the list stored at the specified key. If the key
     * does not exist zero is returned (the same behaviour as for empty lists).
     * If the value stored at key is not a list an error is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return The length of the list.
     */
    public Long llen(final byte[] key) {
	checkIsInMulti();
	client.llen(key);
	return client.getIntegerReply();
    }

    /**
     * Return the specified elements of the list stored at the specified key.
     * Start and end are zero-based indexes. 0 is the first element of the list
     * (the list head), 1 the next element and so on.
     * <p>
     * For example LRANGE foobar 0 2 will return the first three elements of the
     * list.
     * <p>
     * start and end can also be negative numbers indicating offsets from the
     * end of the list. For example -1 is the last element of the list, -2 the
     * penultimate element and so on.
     * <p>
     * <b>Consistency with range functions in various programming languages</b>
     * <p>
     * Note that if you have a list of numbers from 0 to 100, LRANGE 0 10 will
     * return 11 elements, that is, rightmost item is included. This may or may
     * not be consistent with behavior of range-related functions in your
     * programming language of choice (think Ruby's Range.new, Array#slice or
     * Python's range() function).
     * <p>
     * LRANGE behavior is consistent with one of Tcl.
     * <p>
     * <b>Out-of-range indexes</b>
     * <p>
     * Indexes out of range will not produce an error: if start is over the end
     * of the list, or start > end, an empty list is returned. If end is over
     * the end of the list Redis will threat it just like the last element of
     * the list.
     * <p>
     * Time complexity: O(start+n) (with n being the length of the range and
     * start being the start offset)
     * 
     * @param key
     * @param start
     * @param end
     * @return Multi bulk reply, specifically a list of elements in the
     *         specified range.
     */
    public List<byte[]> lrange(final byte[] key, final int start, final int end) {
	checkIsInMulti();
	client.lrange(key, start, end);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * Trim an existing list so that it will contain only the specified range of
     * elements specified. Start and end are zero-based indexes. 0 is the first
     * element of the list (the list head), 1 the next element and so on.
     * <p>
     * For example LTRIM foobar 0 2 will modify the list stored at foobar key so
     * that only the first three elements of the list will remain.
     * <p>
     * start and end can also be negative numbers indicating offsets from the
     * end of the list. For example -1 is the last element of the list, -2 the
     * penultimate element and so on.
     * <p>
     * Indexes out of range will not produce an error: if start is over the end
     * of the list, or start > end, an empty list is left as value. If end over
     * the end of the list Redis will threat it just like the last element of
     * the list.
     * <p>
     * Hint: the obvious use of LTRIM is together with LPUSH/RPUSH. For example:
     * <p>
     * {@code lpush("mylist", "someelement"); ltrim("mylist", 0, 99); * }
     * <p>
     * The above two commands will push elements in the list taking care that
     * the list will not grow without limits. This is very useful when using
     * Redis to store logs for example. It is important to note that when used
     * in this way LTRIM is an O(1) operation because in the average case just
     * one element is removed from the tail of the list.
     * <p>
     * Time complexity: O(n) (with n being len of list - len of range)
     * 
     * @param key
     * @param start
     * @param end
     * @return Status code reply
     */
    public String ltrim(final byte[] key, final int start, final int end) {
	checkIsInMulti();
	client.ltrim(key, start, end);
	return client.getStatusCodeReply();
    }

    /**
     * Return the specified element of the list stored at the specified key. 0
     * is the first element, 1 the second and so on. Negative indexes are
     * supported, for example -1 is the last element, -2 the penultimate and so
     * on.
     * <p>
     * If the value stored at key is not of list type an error is returned. If
     * the index is out of range a 'nil' reply is returned.
     * <p>
     * Note that even if the average time complexity is O(n) asking for the
     * first or the last element of the list is O(1).
     * <p>
     * Time complexity: O(n) (with n being the length of the list)
     * 
     * @param key
     * @param index
     * @return Bulk reply, specifically the requested element
     */
    public byte[] lindex(final byte[] key, final int index) {
	checkIsInMulti();
	client.lindex(key, index);
	return client.getBinaryBulkReply();
    }

    /**
     * Set a new value as the element at index position of the List at key.
     * <p>
     * Out of range indexes will generate an error.
     * <p>
     * Similarly to other list commands accepting indexes, the index can be
     * negative to access elements starting from the end of the list. So -1 is
     * the last element, -2 is the penultimate, and so forth.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(N) (with N being the length of the list), setting the first or last
     * elements of the list is O(1).
     * 
     * @see #lindex(byte[], int)
     * 
     * @param key
     * @param index
     * @param value
     * @return Status code reply
     */
    public String lset(final byte[] key, final int index, final byte[] value) {
	checkIsInMulti();
	client.lset(key, index, value);
	return client.getStatusCodeReply();
    }

    /**
     * Remove the first count occurrences of the value element from the list. If
     * count is zero all the elements are removed. If count is negative elements
     * are removed from tail to head, instead to go from head to tail that is
     * the normal behaviour. So for example LREM with count -2 and hello as
     * value to remove against the list (a,b,c,hello,x,hello,hello) will have
     * the list (a,b,c,hello,x). The number of removed elements is returned as
     * an integer, see below for more information about the returned value. Note
     * that non existing keys are considered like empty lists by LREM, so LREM
     * against non existing keys will always return 0.
     * <p>
     * Time complexity: O(N) (with N being the length of the list)
     * 
     * @param key
     * @param count
     * @param value
     * @return Integer Reply, specifically: The number of removed elements if
     *         the operation succeeded
     */
    public Long lrem(final byte[] key, final int count, final byte[] value) {
	checkIsInMulti();
	client.lrem(key, count, value);
	return client.getIntegerReply();
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of
     * the list. For example if the list contains the elements "a","b","c" LPOP
     * will return "a" and the list will become "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned.
     * 
     * @see #rpop(byte[])
     * 
     * @param key
     * @return Bulk reply
     */
    public byte[] lpop(final byte[] key) {
	checkIsInMulti();
	client.lpop(key);
	return client.getBinaryBulkReply();
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of
     * the list. For example if the list contains the elements "a","b","c" LPOP
     * will return "a" and the list will become "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned.
     * 
     * @see #lpop(byte[])
     * 
     * @param key
     * @return Bulk reply
     */
    public byte[] rpop(final byte[] key) {
	checkIsInMulti();
	client.rpop(key);
	return client.getBinaryBulkReply();
    }

    /**
     * Atomically return and remove the last (tail) element of the srckey list,
     * and push the element as the first (head) element of the dstkey list. For
     * example if the source list contains the elements "a","b","c" and the
     * destination list contains the elements "foo","bar" after an RPOPLPUSH
     * command the content of the two lists will be "a","b" and "c","foo","bar".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned. If the srckey and dstkey are the same the operation is
     * equivalent to removing the last element from the list and pusing it as
     * first element of the list, so it's a "list rotation" command.
     * <p>
     * Time complexity: O(1)
     * 
     * @param srckey
     * @param dstkey
     * @return Bulk reply
     */
    public byte[] rpoplpush(final byte[] srckey, final byte[] dstkey) {
	checkIsInMulti();
	client.rpoplpush(srckey, dstkey);
	return client.getBinaryBulkReply();
    }

    /**
     * Add the specified member to the set value stored at key. If member is
     * already a member of the set no operation is performed. If key does not
     * exist a new set with the specified member as sole member is created. If
     * the key exists but does not hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param members
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the set
     */
    public Long sadd(final byte[] key, final byte[]... members) {
	checkIsInMulti();
	client.sadd(key, members);
	return client.getIntegerReply();
    }

    /**
     * Return all the members (elements) of the set value stored at key. This is
     * just syntax glue for {@link #sinter(String...) SINTER}.
     * <p>
     * Time complexity O(N)
     * 
     * @param key
     * @return Multi bulk reply
     */
    public Set<byte[]> smembers(final byte[] key) {
	checkIsInMulti();
	client.smembers(key);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new HashSet<byte[]>(members);
    }

    /**
     * Remove the specified member from the set value stored at key. If member
     * was not a member of the set no operation is performed. If key does not
     * hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */
    public Long srem(final byte[] key, final byte[]... member) {
	checkIsInMulti();
	client.srem(key, member);
	return client.getIntegerReply();
    }

    /**
     * Remove a random element from a Set returning it as return value. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The {@link #srandmember(byte[])} command does a similar work but the
     * returned element is not removed from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public byte[] spop(final byte[] key) {
	checkIsInMulti();
	client.spop(key);
	return client.getBinaryBulkReply();
    }

    /**
     * Move the specified member from the set at srckey to the set at dstkey.
     * This operation is atomic, in every given moment the element will appear
     * to be in the source or destination set for accessing clients.
     * <p>
     * If the source set does not exist or does not contain the specified
     * element no operation is performed and zero is returned, otherwise the
     * element is removed from the source set and added to the destination set.
     * On success one is returned, even if the element was already present in
     * the destination set.
     * <p>
     * An error is raised if the source or destination keys contain a non Set
     * value.
     * <p>
     * Time complexity O(1)
     * 
     * @param srckey
     * @param dstkey
     * @param member
     * @return Integer reply, specifically: 1 if the element was moved 0 if the
     *         element was not found on the first set and no operation was
     *         performed
     */
    public Long smove(final byte[] srckey, final byte[] dstkey,
	    final byte[] member) {
	checkIsInMulti();
	client.smove(srckey, dstkey, member);
	return client.getIntegerReply();
    }

    /**
     * Return the set cardinality (number of elements). If the key does not
     * exist 0 is returned, like for empty sets.
     * 
     * @param key
     * @return Integer reply, specifically: the cardinality (number of elements)
     *         of the set as an integer.
     */
    public Long scard(final byte[] key) {
	checkIsInMulti();
	client.scard(key);
	return client.getIntegerReply();
    }

    /**
     * Return 1 if member is a member of the set stored at key, otherwise 0 is
     * returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the element is a member of the
     *         set 0 if the element is not a member of the set OR if the key
     *         does not exist
     */
    public Boolean sismember(final byte[] key, final byte[] member) {
	checkIsInMulti();
	client.sismember(key, member);
	return client.getIntegerReply() == 1;
    }

    /**
     * Return the members of a set resulting from the intersection of all the
     * sets hold at the specified keys. Like in
     * {@link #lrange(byte[], int, int) LRANGE} the result is sent to the client
     * as a multi-bulk reply (see the protocol specification for more
     * information). If just a single key is specified, then this command
     * produces the same result as {@link #smembers(byte[]) SMEMBERS}. Actually
     * SMEMBERS is just syntax sugar for SINTER.
     * <p>
     * Non existing keys are considered like empty sets, so if one of the keys
     * is missing an empty set is returned (since the intersection with an empty
     * set always is an empty set).
     * <p>
     * Time complexity O(N*M) worst case where N is the cardinality of the
     * smallest set and M the number of sets
     * 
     * @param keys
     * @return Multi bulk reply, specifically the list of common elements.
     */
    public Set<byte[]> sinter(final byte[]... keys) {
	checkIsInMulti();
	client.sinter(keys);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new HashSet<byte[]>(members);
    }

    /**
     * This commnad works exactly like {@link #sinter(String...) SINTER} but
     * instead of being returned the resulting set is sotred as dstkey.
     * <p>
     * Time complexity O(N*M) worst case where N is the cardinality of the
     * smallest set and M the number of sets
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sinterstore(final byte[] dstkey, final byte[]... keys) {
	checkIsInMulti();
	client.sinterstore(dstkey, keys);
	return client.getIntegerReply();
    }

    /**
     * Return the members of a set resulting from the union of all the sets hold
     * at the specified keys. Like in {@link #lrange(byte[], int, int) LRANGE}
     * the result is sent to the client as a multi-bulk reply (see the protocol
     * specification for more information). If just a single key is specified,
     * then this command produces the same result as {@link #smembers(byte[])
     * SMEMBERS}.
     * <p>
     * Non existing keys are considered like empty sets.
     * <p>
     * Time complexity O(N) where N is the total number of elements in all the
     * provided sets
     * 
     * @param keys
     * @return Multi bulk reply, specifically the list of common elements.
     */
    public Set<byte[]> sunion(final byte[]... keys) {
	checkIsInMulti();
	client.sunion(keys);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new HashSet<byte[]>(members);
    }

    /**
     * This command works exactly like {@link #sunion(String...) SUNION} but
     * instead of being returned the resulting set is stored as dstkey. Any
     * existing value in dstkey will be over-written.
     * <p>
     * Time complexity O(N) where N is the total number of elements in all the
     * provided sets
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sunionstore(final byte[] dstkey, final byte[]... keys) {
	checkIsInMulti();
	client.sunionstore(dstkey, keys);
	return client.getIntegerReply();
    }

    /**
     * Return the difference between the Set stored at key1 and all the Sets
     * key2, ..., keyN
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * key1 = [x, a, b, c]
     * key2 = [c]
     * key3 = [a, d]
     * SDIFF key1,key2,key3 => [x, b]
     * </pre>
     * 
     * Non existing keys are considered like empty sets.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(N) with N being the total number of elements of all the sets
     * 
     * @param keys
     * @return Return the members of a set resulting from the difference between
     *         the first set provided and all the successive sets.
     */
    public Set<byte[]> sdiff(final byte[]... keys) {
	checkIsInMulti();
	client.sdiff(keys);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new HashSet<byte[]>(members);
    }

    /**
     * This command works exactly like {@link #sdiff(String...) SDIFF} but
     * instead of being returned the resulting set is stored in dstkey.
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sdiffstore(final byte[] dstkey, final byte[]... keys) {
	checkIsInMulti();
	client.sdiffstore(dstkey, keys);
	return client.getIntegerReply();
    }

    /**
     * Return a random element from a Set, without removing the element. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The SPOP command does a similar work but the returned element is popped
     * (removed) from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public byte[] srandmember(final byte[] key) {
	checkIsInMulti();
	client.srandmember(key);
	return client.getBinaryBulkReply();
    }

    /**
     * Add the specified member having the specifeid score to the sorted set
     * stored at key. If member is already a member of the sorted set the score
     * is updated, and the element reinserted in the right position to ensure
     * sorting. If key does not exist a new sorted set with the specified member
     * as sole member is crated. If the key exists but does not hold a sorted
     * set value an error is returned.
     * <p>
     * The score value can be the string representation of a double precision
     * floating point number.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * @param key
     * @param score
     * @param member
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the sorted set and the score
     *         was updated
     */
    public Long zadd(final byte[] key, final double score, final byte[] member) {
	checkIsInMulti();
	client.zadd(key, score, member);
	return client.getIntegerReply();
    }

    public Long zadd(final byte[] key, final Map<Double, byte[]> scoreMembers) {
	checkIsInMulti();
	client.zaddBinary(key, scoreMembers);
	return client.getIntegerReply();
    }

    public Set<byte[]> zrange(final byte[] key, final int start, final int end) {
	checkIsInMulti();
	client.zrange(key, start, end);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new LinkedHashSet<byte[]>(members);
    }

    /**
     * Remove the specified member from the sorted set value stored at key. If
     * member was not a member of the set no operation is performed. If key does
     * not not hold a set value an error is returned.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * 
     * 
     * @param key
     * @param members
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */
    public Long zrem(final byte[] key, final byte[]... members) {
	checkIsInMulti();
	client.zrem(key, members);
	return client.getIntegerReply();
    }

    /**
     * If member already exists in the sorted set adds the increment to its
     * score and updates the position of the element in the sorted set
     * accordingly. If member does not already exist in the sorted set it is
     * added with increment as score (that is, like if the previous score was
     * virtually zero). If key does not exist a new sorted set with the
     * specified member as sole member is crated. If the key exists but does not
     * hold a sorted set value an error is returned.
     * <p>
     * The score value can be the string representation of a double precision
     * floating point number. It's possible to provide a negative value to
     * perform a decrement.
     * <p>
     * For an introduction to sorted sets check the Introduction to Redis data
     * types page.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * @param key
     * @param score
     * @param member
     * @return The new score
     */
    public Double zincrby(final byte[] key, final double score,
	    final byte[] member) {
	checkIsInMulti();
	client.zincrby(key, score, member);
	String newscore = client.getBulkReply();
	return Double.valueOf(newscore);
    }

    /**
     * Return the rank (or index) or member in the sorted set at key, with
     * scores being ordered from low to high.
     * <p>
     * When the given member does not exist in the sorted set, the special value
     * 'nil' is returned. The returned rank (or index) of the member is 0-based
     * for both commands.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))
     * 
     * @see #zrevrank(byte[], byte[])
     * 
     * @param key
     * @param member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */
    public Long zrank(final byte[] key, final byte[] member) {
	checkIsInMulti();
	client.zrank(key, member);
	return client.getIntegerReply();
    }

    /**
     * Return the rank (or index) or member in the sorted set at key, with
     * scores being ordered from high to low.
     * <p>
     * When the given member does not exist in the sorted set, the special value
     * 'nil' is returned. The returned rank (or index) of the member is 0-based
     * for both commands.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))
     * 
     * @see #zrank(byte[], byte[])
     * 
     * @param key
     * @param member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */
    public Long zrevrank(final byte[] key, final byte[] member) {
	checkIsInMulti();
	client.zrevrank(key, member);
	return client.getIntegerReply();
    }

    public Set<byte[]> zrevrange(final byte[] key, final int start,
	    final int end) {
	checkIsInMulti();
	client.zrevrange(key, start, end);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new LinkedHashSet<byte[]>(members);
    }

    public Set<Tuple> zrangeWithScores(final byte[] key, final int start,
	    final int end) {
	checkIsInMulti();
	client.zrangeWithScores(key, start, end);
	Set<Tuple> set = getBinaryTupledSet();
	return set;
    }

    public Set<Tuple> zrevrangeWithScores(final byte[] key, final int start,
	    final int end) {
	checkIsInMulti();
	client.zrevrangeWithScores(key, start, end);
	Set<Tuple> set = getBinaryTupledSet();
	return set;
    }

    /**
     * Return the sorted set cardinality (number of elements). If the key does
     * not exist 0 is returned, like for empty sorted sets.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return the cardinality (number of elements) of the set as an integer.
     */
    public Long zcard(final byte[] key) {
	checkIsInMulti();
	client.zcard(key);
	return client.getIntegerReply();
    }

    /**
     * Return the score of the specified element of the sorted set at key. If
     * the specified element does not exist in the sorted set, or the key does
     * not exist at all, a special 'nil' value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param member
     * @return the score
     */
    public Double zscore(final byte[] key, final byte[] member) {
	checkIsInMulti();
	client.zscore(key, member);
	final String score = client.getBulkReply();
	return (score != null ? new Double(score) : null);
    }

    public Transaction multi() {
	client.multi();
	return new Transaction(client);
    }

    public List<Object> multi(final TransactionBlock jedisTransaction) {
	List<Object> results = null;
	jedisTransaction.setClient(client);
	try {
	    client.multi();
	    jedisTransaction.execute();
	    results = jedisTransaction.exec();
	} catch (Exception ex) {
	    jedisTransaction.discard();
	}
	return results;
    }

    protected void checkIsInMulti() {
	if (client.isInMulti()) {
	    throw new JedisDataException(
		    "Cannot use Jedis when in Multi. Please use JedisTransaction instead.");
	}
    }

    public void connect() {
	client.connect();
    }

    public void disconnect() {
	client.disconnect();
    }

    public String watch(final byte[]... keys) {
	client.watch(keys);
	return client.getStatusCodeReply();
    }

    public String unwatch() {
	client.unwatch();
	return client.getStatusCodeReply();
    }

    /**
     * Sort a Set or a List.
     * <p>
     * Sort the elements contained in the List, Set, or Sorted Set value at key.
     * By default sorting is numeric with elements being compared as double
     * precision floating point numbers. This is the simplest form of SORT.
     * 
     * @see #sort(byte[], byte[])
     * @see #sort(byte[], SortingParams)
     * @see #sort(byte[], SortingParams, byte[])
     * 
     * 
     * @param key
     * @return Assuming the Set/List at key contains a list of numbers, the
     *         return value will be the list of numbers ordered from the
     *         smallest to the biggest number.
     */
    public List<byte[]> sort(final byte[] key) {
	checkIsInMulti();
	client.sort(key);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * Sort a Set or a List accordingly to the specified parameters.
     * <p>
     * <b>examples:</b>
     * <p>
     * Given are the following sets and key/values:
     * 
     * <pre>
     * x = [1, 2, 3]
     * y = [a, b, c]
     * 
     * k1 = z
     * k2 = y
     * k3 = x
     * 
     * w1 = 9
     * w2 = 8
     * w3 = 7
     * </pre>
     * 
     * Sort Order:
     * 
     * <pre>
     * sort(x) or sort(x, sp.asc())
     * -> [1, 2, 3]
     * 
     * sort(x, sp.desc())
     * -> [3, 2, 1]
     * 
     * sort(y)
     * -> [c, a, b]
     * 
     * sort(y, sp.alpha())
     * -> [a, b, c]
     * 
     * sort(y, sp.alpha().desc())
     * -> [c, a, b]
     * </pre>
     * 
     * Limit (e.g. for Pagination):
     * 
     * <pre>
     * sort(x, sp.limit(0, 2))
     * -> [1, 2]
     * 
     * sort(y, sp.alpha().desc().limit(1, 2))
     * -> [b, a]
     * </pre>
     * 
     * Sorting by external keys:
     * 
     * <pre>
     * sort(x, sb.by(w*))
     * -> [3, 2, 1]
     * 
     * sort(x, sb.by(w*).desc())
     * -> [1, 2, 3]
     * </pre>
     * 
     * Getting external keys:
     * 
     * <pre>
     * sort(x, sp.by(w*).get(k*))
     * -> [x, y, z]
     * 
     * sort(x, sp.by(w*).get(#).get(k*))
     * -> [3, x, 2, y, 1, z]
     * </pre>
     * 
     * @see #sort(byte[])
     * @see #sort(byte[], SortingParams, byte[])
     * 
     * @param key
     * @param sortingParameters
     * @return a list of sorted elements.
     */
    public List<byte[]> sort(final byte[] key,
	    final SortingParams sortingParameters) {
	checkIsInMulti();
	client.sort(key, sortingParameters);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this
     * commands as blocking versions of LPOP and RPOP able to block if the
     * specified keys don't exist or contain empty lists.
     * <p>
     * The following is a description of the exact semantic. We describe BLPOP
     * but the two commands are identical, the only difference is that BLPOP
     * pops the element from the left (head) of the list, and BRPOP pops from
     * the right (tail).
     * <p>
     * <b>Non blocking behavior</b>
     * <p>
     * When BLPOP is called, if at least one of the specified keys contain a non
     * empty list, an element is popped from the head of the list and returned
     * to the caller together with the name of the key (BLPOP returns a two
     * elements array, the first element is the key, the second the popped
     * value).
     * <p>
     * Keys are scanned from left to right, so for instance if you issue BLPOP
     * list1 list2 list3 0 against a dataset where list1 does not exist but
     * list2 and list3 contain non empty lists, BLPOP guarantees to return an
     * element from the list stored at list2 (since it is the first non empty
     * list starting from the left).
     * <p>
     * <b>Blocking behavior</b>
     * <p>
     * If none of the specified keys exist or contain non empty lists, BLPOP
     * blocks until some other client performs a LPUSH or an RPUSH operation
     * against one of the lists.
     * <p>
     * Once new data is present on one of the lists, the client finally returns
     * with the name of the key unblocking it and the popped value.
     * <p>
     * When blocking, if a non-zero timeout is specified, the client will
     * unblock returning a nil special value if the specified amount of seconds
     * passed without a push operation against at least one of the specified
     * keys.
     * <p>
     * The timeout argument is interpreted as an integer value. A timeout of
     * zero means instead to block forever.
     * <p>
     * <b>Multiple clients blocking for the same keys</b>
     * <p>
     * Multiple clients can block for the same key. They are put into a queue,
     * so the first to be served will be the one that started to wait earlier,
     * in a first-blpopping first-served fashion.
     * <p>
     * <b>blocking POP inside a MULTI/EXEC transaction</b>
     * <p>
     * BLPOP and BRPOP can be used with pipelining (sending multiple commands
     * and reading the replies in batch), but it does not make sense to use
     * BLPOP or BRPOP inside a MULTI/EXEC block (a Redis transaction).
     * <p>
     * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to
     * return a multi-bulk nil reply, exactly what happens when the timeout is
     * reached. If you like science fiction, think at it like if inside
     * MULTI/EXEC the time will flow at infinite speed :)
     * <p>
     * Time complexity: O(1)
     * 
     * @see #brpop(int, String...)
     * 
     * @param timeout
     * @param keys
     * @return BLPOP returns a two-elements array via a multi bulk reply in
     *         order to return both the unblocking key and the popped value.
     *         <p>
     *         When a non-zero timeout is specified, and the BLPOP operation
     *         timed out, the return value is a nil multi bulk reply. Most
     *         client values will return false or nil accordingly to the
     *         programming language used.
     */
    public List<byte[]> blpop(final int timeout, final byte[]... keys) {
	checkIsInMulti();
	final List<byte[]> args = new ArrayList<byte[]>();
	for (final byte[] arg : keys) {
	    args.add(arg);
	}
	args.add(Protocol.toByteArray(timeout));

	client.blpop(args.toArray(new byte[args.size()][]));
	client.setTimeoutInfinite();
	final List<byte[]> multiBulkReply = client.getBinaryMultiBulkReply();
	client.rollbackTimeout();
	return multiBulkReply;
    }

    /**
     * Sort a Set or a List accordingly to the specified parameters and store
     * the result at dstkey.
     * 
     * @see #sort(byte[], SortingParams)
     * @see #sort(byte[])
     * @see #sort(byte[], byte[])
     * 
     * @param key
     * @param sortingParameters
     * @param dstkey
     * @return The number of elements of the list at dstkey.
     */
    public Long sort(final byte[] key, final SortingParams sortingParameters,
	    final byte[] dstkey) {
	checkIsInMulti();
	client.sort(key, sortingParameters, dstkey);
	return client.getIntegerReply();
    }

    /**
     * Sort a Set or a List and Store the Result at dstkey.
     * <p>
     * Sort the elements contained in the List, Set, or Sorted Set value at key
     * and store the result at dstkey. By default sorting is numeric with
     * elements being compared as double precision floating point numbers. This
     * is the simplest form of SORT.
     * 
     * @see #sort(byte[])
     * @see #sort(byte[], SortingParams)
     * @see #sort(byte[], SortingParams, byte[])
     * 
     * @param key
     * @param dstkey
     * @return The number of elements of the list at dstkey.
     */
    public Long sort(final byte[] key, final byte[] dstkey) {
	checkIsInMulti();
	client.sort(key, dstkey);
	return client.getIntegerReply();
    }

    /**
     * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this
     * commands as blocking versions of LPOP and RPOP able to block if the
     * specified keys don't exist or contain empty lists.
     * <p>
     * The following is a description of the exact semantic. We describe BLPOP
     * but the two commands are identical, the only difference is that BLPOP
     * pops the element from the left (head) of the list, and BRPOP pops from
     * the right (tail).
     * <p>
     * <b>Non blocking behavior</b>
     * <p>
     * When BLPOP is called, if at least one of the specified keys contain a non
     * empty list, an element is popped from the head of the list and returned
     * to the caller together with the name of the key (BLPOP returns a two
     * elements array, the first element is the key, the second the popped
     * value).
     * <p>
     * Keys are scanned from left to right, so for instance if you issue BLPOP
     * list1 list2 list3 0 against a dataset where list1 does not exist but
     * list2 and list3 contain non empty lists, BLPOP guarantees to return an
     * element from the list stored at list2 (since it is the first non empty
     * list starting from the left).
     * <p>
     * <b>Blocking behavior</b>
     * <p>
     * If none of the specified keys exist or contain non empty lists, BLPOP
     * blocks until some other client performs a LPUSH or an RPUSH operation
     * against one of the lists.
     * <p>
     * Once new data is present on one of the lists, the client finally returns
     * with the name of the key unblocking it and the popped value.
     * <p>
     * When blocking, if a non-zero timeout is specified, the client will
     * unblock returning a nil special value if the specified amount of seconds
     * passed without a push operation against at least one of the specified
     * keys.
     * <p>
     * The timeout argument is interpreted as an integer value. A timeout of
     * zero means instead to block forever.
     * <p>
     * <b>Multiple clients blocking for the same keys</b>
     * <p>
     * Multiple clients can block for the same key. They are put into a queue,
     * so the first to be served will be the one that started to wait earlier,
     * in a first-blpopping first-served fashion.
     * <p>
     * <b>blocking POP inside a MULTI/EXEC transaction</b>
     * <p>
     * BLPOP and BRPOP can be used with pipelining (sending multiple commands
     * and reading the replies in batch), but it does not make sense to use
     * BLPOP or BRPOP inside a MULTI/EXEC block (a Redis transaction).
     * <p>
     * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to
     * return a multi-bulk nil reply, exactly what happens when the timeout is
     * reached. If you like science fiction, think at it like if inside
     * MULTI/EXEC the time will flow at infinite speed :)
     * <p>
     * Time complexity: O(1)
     * 
     * @see #blpop(int, String...)
     * 
     * @param timeout
     * @param keys
     * @return BLPOP returns a two-elements array via a multi bulk reply in
     *         order to return both the unblocking key and the popped value.
     *         <p>
     *         When a non-zero timeout is specified, and the BLPOP operation
     *         timed out, the return value is a nil multi bulk reply. Most
     *         client values will return false or nil accordingly to the
     *         programming language used.
     */
    public List<byte[]> brpop(final int timeout, final byte[]... keys) {
	checkIsInMulti();
	final List<byte[]> args = new ArrayList<byte[]>();
	for (final byte[] arg : keys) {
	    args.add(arg);
	}
	args.add(Protocol.toByteArray(timeout));

	client.brpop(args.toArray(new byte[args.size()][]));
	client.setTimeoutInfinite();
	final List<byte[]> multiBulkReply = client.getBinaryMultiBulkReply();
	client.rollbackTimeout();

	return multiBulkReply;
    }

    /**
     * Request for authentication in a password protected Redis server. A Redis
     * server can be instructed to require a password before to allow clients to
     * issue commands. This is done using the requirepass directive in the Redis
     * configuration file. If the password given by the client is correct the
     * server replies with an OK status code reply and starts accepting commands
     * from the client. Otherwise an error is returned and the clients needs to
     * try a new password. Note that for the high performance nature of Redis it
     * is possible to try a lot of passwords in parallel in very short time, so
     * make sure to generate a strong and very long password so that this attack
     * is infeasible.
     * 
     * @param password
     * @return Status code reply
     */
    public String auth(final String password) {
	checkIsInMulti();
	client.auth(password);
	return client.getStatusCodeReply();
    }

    /**
     * Starts a pipeline, which is a very efficient way to send lots of command
     * and read all the responses when you finish sending them. Try to avoid
     * this version and use pipelined() when possible as it will give better
     * performance.
     * 
     * @param jedisPipeline
     * @return The results of the command in the same order you've run them.
     */
    public List<Object> pipelined(final PipelineBlock jedisPipeline) {
	jedisPipeline.setClient(client);
	jedisPipeline.execute();
	return jedisPipeline.syncAndReturnAll();
    }

    public Pipeline pipelined() {
	Pipeline pipeline = new Pipeline();
	pipeline.setClient(client);
	return pipeline;
    }

    public void subscribe(final JedisPubSub jedisPubSub,
	    final String... channels) {
	client.setTimeoutInfinite();
	jedisPubSub.proceed(client, channels);
	client.rollbackTimeout();
    }

    public Long publish(final String channel, final String message) {
	client.publish(channel, message);
	return client.getIntegerReply();
    }

    public void psubscribe(final JedisPubSub jedisPubSub,
	    final String... patterns) {
	client.setTimeoutInfinite();
	jedisPubSub.proceedWithPatterns(client, patterns);
	client.rollbackTimeout();
    }

    public Long zcount(final byte[] key, final double min, final double max) {
    	return zcount(key, toByteArray(min), toByteArray(max));
    }
    
    public Long zcount(final byte[] key, final byte[] min, final byte[] max) {
    	checkIsInMulti();
    	client.zcount(key, min, max);
    	return client.getIntegerReply();
    }
    
    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(byte[], double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(byte[], double, double)
     * @see #zrangeByScore(byte[], double, double, int, int)
     * @see #zrangeByScoreWithScores(byte[], double, double)
     * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
     * @see #zcount(byte[], double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<byte[]> zrangeByScore(final byte[] key, final double min,
	    final double max) {
	return zrangeByScore(key, toByteArray(min), toByteArray(max));
	}

    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min,
	    final byte[] max) {
	checkIsInMulti();
	client.zrangeByScore(key, min, max);
	return new LinkedHashSet<byte[]>(client.getBinaryMultiBulkReply());
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(byte[], double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(byte[], double, double)
     * @see #zrangeByScore(byte[], double, double, int, int)
     * @see #zrangeByScoreWithScores(byte[], double, double)
     * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
     * @see #zcount(byte[], double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<byte[]> zrangeByScore(final byte[] key, final double min,
	    final double max, final int offset, final int count) {
	return zrangeByScore(key, toByteArray(min),toByteArray(max),offset, count);
    }
    
    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min,
    	    final byte[] max, final int offset, final int count) {
    	checkIsInMulti();
    	client.zrangeByScore(key, min, max, offset, count);
    	return new LinkedHashSet<byte[]>(client.getBinaryMultiBulkReply());
        }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(byte[], double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(byte[], double, double)
     * @see #zrangeByScore(byte[], double, double, int, int)
     * @see #zrangeByScoreWithScores(byte[], double, double)
     * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
     * @see #zcount(byte[], double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key,
	    final double min, final double max) {
	return zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max));
    }
    
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key,
    	    final byte[] min, final byte[] max) {
    	checkIsInMulti();
    	client.zrangeByScoreWithScores(key, min, max);
    	Set<Tuple> set = getBinaryTupledSet();
    	return set;
        }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(byte[], double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(byte[], double, double)
     * @see #zrangeByScore(byte[], double, double, int, int)
     * @see #zrangeByScoreWithScores(byte[], double, double)
     * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
     * @see #zcount(byte[], double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key,
	    final double min, final double max, final int offset,
	    final int count) {
	return zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max), offset, count);
    }
    
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key,
    	    final byte[] min, final byte[] max, final int offset,
    	    final int count) {
    	checkIsInMulti();
    	client.zrangeByScoreWithScores(key, min, max, offset, count);
    	Set<Tuple> set = getBinaryTupledSet();
    	return set;
        }

    private Set<Tuple> getBinaryTupledSet() {
	checkIsInMulti();
	List<byte[]> membersWithScores = client.getBinaryMultiBulkReply();
	Set<Tuple> set = new LinkedHashSet<Tuple>();
	Iterator<byte[]> iterator = membersWithScores.iterator();
	while (iterator.hasNext()) {
	    set.add(new Tuple(iterator.next(), Double.valueOf(SafeEncoder
		    .encode(iterator.next()))));
	}
	return set;
    }

    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max,
	    final double min) {
	return zrevrangeByScore(key, toByteArray(max), toByteArray(min));
    }

    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max,
	    final byte[] min) {
	checkIsInMulti();
	client.zrevrangeByScore(key, max, min);
	return new LinkedHashSet<byte[]>(client.getBinaryMultiBulkReply());
    }

    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max,
	    final double min, final int offset, final int count) {
	return zrevrangeByScore(key, toByteArray(max), toByteArray(min), offset, count);
    }
    
    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max,
    	    final byte[] min, final int offset, final int count) {
    	checkIsInMulti();
    	client.zrevrangeByScore(key, max, min, offset, count);
    	return new LinkedHashSet<byte[]>(client.getBinaryMultiBulkReply());
        }

    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key,
	    final double max, final double min) {
	return zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min));
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key,
	    final double max, final double min, final int offset,
	    final int count) {
    	return zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min), offset, count);
    }
    
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key,
    	    final byte[] max, final byte[] min) {
	checkIsInMulti();
	client.zrevrangeByScoreWithScores(key, max, min);
	Set<Tuple> set = getBinaryTupledSet();
	return set;
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key,
	    final byte[] max, final byte[] min, final int offset,
	    final int count) {
	checkIsInMulti();
	client.zrevrangeByScoreWithScores(key, max, min, offset, count);
	Set<Tuple> set = getBinaryTupledSet();
	return set;
    }    

    /**
     * Remove all elements in the sorted set at key with rank between start and
     * end. Start and end are 0-based with rank 0 being the element with the
     * lowest score. Both start and end can be negative numbers, where they
     * indicate offsets starting at the element with the highest rank. For
     * example: -1 is the element with the highest score, -2 the element with
     * the second highest score and so forth.
     * <p>
     * <b>Time complexity:</b> O(log(N))+O(M) with N being the number of
     * elements in the sorted set and M the number of elements removed by the
     * operation
     * 
     */
    public Long zremrangeByRank(final byte[] key, final int start, final int end) {
	checkIsInMulti();
	client.zremrangeByRank(key, start, end);
	return client.getIntegerReply();
    }

    /**
     * Remove all the elements in the sorted set at key with a score between min
     * and max (including elements with score equal to min or max).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements removed by the operation
     * 
     * @param key
     * @param start
     * @param end
     * @return Integer reply, specifically the number of elements removed.
     */
    public Long zremrangeByScore(final byte[] key, final double start,
	    final double end) {
	return zremrangeByScore(key, toByteArray(start), toByteArray(end));
    }
    
    public Long zremrangeByScore(final byte[] key, final byte[] start,
    	    final byte[] end) {
    	checkIsInMulti();
    	client.zremrangeByScore(key, start, end);
    	return client.getIntegerReply();
        }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zunionstore(final byte[] dstkey, final byte[]... sets) {
	checkIsInMulti();
	client.zunionstore(dstkey, sets);
	return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @param params
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zunionstore(final byte[] dstkey, final ZParams params,
	    final byte[]... sets) {
	checkIsInMulti();
	client.zunionstore(dstkey, params, sets);
	return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zinterstore(final byte[] dstkey, final byte[]... sets) {
	checkIsInMulti();
	client.zinterstore(dstkey, sets);
	return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @param params
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zinterstore(final byte[] dstkey, final ZParams params,
	    final byte[]... sets) {
	checkIsInMulti();
	client.zinterstore(dstkey, params, sets);
	return client.getIntegerReply();
    }

    /**
     * Synchronously save the DB on disk.
     * <p>
     * Save the whole dataset on disk (this means that all the databases are
     * saved, as well as keys with an EXPIRE set (the expire is preserved). The
     * server hangs while the saving is not completed, no connection is served
     * in the meanwhile. An OK code is returned when the DB was fully stored in
     * disk.
     * <p>
     * The background variant of this command is {@link #bgsave() BGSAVE} that
     * is able to perform the saving in the background while the server
     * continues serving other clients.
     * <p>
     * 
     * @return Status code reply
     */
    public String save() {
	client.save();
	return client.getStatusCodeReply();
    }

    /**
     * Asynchronously save the DB on disk.
     * <p>
     * Save the DB in background. The OK code is immediately returned. Redis
     * forks, the parent continues to server the clients, the child saves the DB
     * on disk then exit. A client my be able to check if the operation
     * succeeded using the LASTSAVE command.
     * 
     * @return Status code reply
     */
    public String bgsave() {
	client.bgsave();
	return client.getStatusCodeReply();
    }

    /**
     * Rewrite the append only file in background when it gets too big. Please
     * for detailed information about the Redis Append Only File check the <a
     * href="http://code.google.com/p/redis/wiki/AppendOnlyFileHowto">Append
     * Only File Howto</a>.
     * <p>
     * BGREWRITEAOF rewrites the Append Only File in background when it gets too
     * big. The Redis Append Only File is a Journal, so every operation
     * modifying the dataset is logged in the Append Only File (and replayed at
     * startup). This means that the Append Only File always grows. In order to
     * rebuild its content the BGREWRITEAOF creates a new version of the append
     * only file starting directly form the dataset in memory in order to
     * guarantee the generation of the minimal number of commands needed to
     * rebuild the database.
     * <p>
     * 
     * @return Status code reply
     */
    public String bgrewriteaof() {
	client.bgrewriteaof();
	return client.getStatusCodeReply();
    }

    /**
     * Return the UNIX time stamp of the last successfully saving of the dataset
     * on disk.
     * <p>
     * Return the UNIX TIME of the last DB save executed with success. A client
     * may check if a {@link #bgsave() BGSAVE} command succeeded reading the
     * LASTSAVE value, then issuing a BGSAVE command and checking at regular
     * intervals every N seconds if LASTSAVE changed.
     * 
     * @return Integer reply, specifically an UNIX time stamp.
     */
    public Long lastsave() {
	client.lastsave();
	return client.getIntegerReply();
    }

    /**
     * Synchronously save the DB on disk, then shutdown the server.
     * <p>
     * Stop all the clients, save the DB, then quit the server. This commands
     * makes sure that the DB is switched off without the lost of any data. This
     * is not guaranteed if the client uses simply {@link #save() SAVE} and then
     * {@link #quit() QUIT} because other clients may alter the DB data between
     * the two commands.
     * 
     * @return Status code reply on error. On success nothing is returned since
     *         the server quits and the connection is closed.
     */
    public String shutdown() {
	client.shutdown();
	String status = null;
	try {
	    status = client.getStatusCodeReply();
	} catch (JedisException ex) {
	    status = null;
	}
	return status;
    }

    /**
     * Provide information and statistics about the server.
     * <p>
     * The info command returns different information and statistics about the
     * server in an format that's simple to parse by computers and easy to read
     * by humans.
     * <p>
     * <b>Format of the returned String:</b>
     * <p>
     * All the fields are in the form field:value
     * 
     * <pre>
     * edis_version:0.07
     * connected_clients:1
     * connected_slaves:0
     * used_memory:3187
     * changes_since_last_save:0
     * last_save_time:1237655729
     * total_connections_received:1
     * total_commands_processed:1
     * uptime_in_seconds:25
     * uptime_in_days:0
     * </pre>
     * 
     * <b>Notes</b>
     * <p>
     * used_memory is returned in bytes, and is the total number of bytes
     * allocated by the program using malloc.
     * <p>
     * uptime_in_days is redundant since the uptime in seconds contains already
     * the full uptime information, this field is only mainly present for
     * humans.
     * <p>
     * changes_since_last_save does not refer to the number of key changes, but
     * to the number of operations that produced some kind of change in the
     * dataset.
     * <p>
     * 
     * @return Bulk reply
     */
    public String info() {
	client.info();
	return client.getBulkReply();
    }

    /**
     * Dump all the received requests in real time.
     * <p>
     * MONITOR is a debugging command that outputs the whole sequence of
     * commands received by the Redis server. is very handy in order to
     * understand what is happening into the database. This command is used
     * directly via telnet.
     * 
     * @param jedisMonitor
     */
    public void monitor(final JedisMonitor jedisMonitor) {
	client.monitor();
	jedisMonitor.proceed(client);
    }

    /**
     * Change the replication settings.
     * <p>
     * The SLAVEOF command can change the replication settings of a slave on the
     * fly. If a Redis server is arleady acting as slave, the command SLAVEOF NO
     * ONE will turn off the replicaiton turning the Redis server into a MASTER.
     * In the proper form SLAVEOF hostname port will make the server a slave of
     * the specific server listening at the specified hostname and port.
     * <p>
     * If a server is already a slave of some master, SLAVEOF hostname port will
     * stop the replication against the old server and start the
     * synchrnonization against the new one discarding the old dataset.
     * <p>
     * The form SLAVEOF no one will stop replication turning the server into a
     * MASTER but will not discard the replication. So if the old master stop
     * working it is possible to turn the slave into a master and set the
     * application to use the new master in read/write. Later when the other
     * Redis server will be fixed it can be configured in order to work as
     * slave.
     * <p>
     * 
     * @param host
     * @param port
     * @return Status code reply
     */
    public String slaveof(final String host, final int port) {
	client.slaveof(host, port);
	return client.getStatusCodeReply();
    }

    public String slaveofNoOne() {
	client.slaveofNoOne();
	return client.getStatusCodeReply();
    }

    /**
     * Retrieve the configuration of a running Redis server. Not all the
     * configuration parameters are supported.
     * <p>
     * CONFIG GET returns the current configuration parameters. This sub command
     * only accepts a single argument, that is glob style pattern. All the
     * configuration parameters matching this parameter are reported as a list
     * of key-value pairs.
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * $ redis-cli config get '*'
     * 1. "dbfilename"
     * 2. "dump.rdb"
     * 3. "requirepass"
     * 4. (nil)
     * 5. "masterauth"
     * 6. (nil)
     * 7. "maxmemory"
     * 8. "0\n"
     * 9. "appendfsync"
     * 10. "everysec"
     * 11. "save"
     * 12. "3600 1 300 100 60 10000"
     * 
     * $ redis-cli config get 'm*'
     * 1. "masterauth"
     * 2. (nil)
     * 3. "maxmemory"
     * 4. "0\n"
     * </pre>
     * 
     * @param pattern
     * @return Bulk reply.
     */
    public List<byte[]> configGet(final byte[] pattern) {
	client.configGet(pattern);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * Reset the stats returned by INFO
     * 
     * @return
     */
    public String configResetStat() {
	client.configResetStat();
	return client.getStatusCodeReply();
    }

    /**
     * Alter the configuration of a running Redis server. Not all the
     * configuration parameters are supported.
     * <p>
     * The list of configuration parameters supported by CONFIG SET can be
     * obtained issuing a {@link #configGet(String) CONFIG GET *} command.
     * <p>
     * The configuration set using CONFIG SET is immediately loaded by the Redis
     * server that will start acting as specified starting from the next
     * command.
     * <p>
     * 
     * <b>Parameters value format</b>
     * <p>
     * The value of the configuration parameter is the same as the one of the
     * same parameter in the Redis configuration file, with the following
     * exceptions:
     * <p>
     * <ul>
     * <li>The save paramter is a list of space-separated integers. Every pair
     * of integers specify the time and number of changes limit to trigger a
     * save. For instance the command CONFIG SET save "3600 10 60 10000" will
     * configure the server to issue a background saving of the RDB file every
     * 3600 seconds if there are at least 10 changes in the dataset, and every
     * 60 seconds if there are at least 10000 changes. To completely disable
     * automatic snapshots just set the parameter as an empty string.
     * <li>All the integer parameters representing memory are returned and
     * accepted only using bytes as unit.
     * </ul>
     * 
     * @param parameter
     * @param value
     * @return Status code reply
     */
    public byte[] configSet(final byte[] parameter, final byte[] value) {
	client.configSet(parameter, value);
	return client.getBinaryBulkReply();
    }

    public boolean isConnected() {
	return client.isConnected();
    }

    public Long strlen(final byte[] key) {
	client.strlen(key);
	return client.getIntegerReply();
    }

    public void sync() {
	client.sync();
    }

    public Long lpushx(final byte[] key, final byte[] string) {
	client.lpushx(key, string);
	return client.getIntegerReply();
    }

    /**
     * Undo a {@link #expire(byte[], int) expire} at turning the expire key into
     * a normal key.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return Integer reply, specifically: 1: the key is now persist. 0: the
     *         key is not persist (only happens when key not set).
     */
    public Long persist(final byte[] key) {
	client.persist(key);
	return client.getIntegerReply();
    }

    public Long rpushx(final byte[] key, final byte[] string) {
	client.rpushx(key, string);
	return client.getIntegerReply();
    }

    public byte[] echo(final byte[] string) {
	client.echo(string);
	return client.getBinaryBulkReply();
    }

    public Long linsert(final byte[] key, final LIST_POSITION where,
	    final byte[] pivot, final byte[] value) {
	client.linsert(key, where, pivot, value);
	return client.getIntegerReply();
    }

    public String debug(final DebugParams params) {
	client.debug(params);
	return client.getStatusCodeReply();
    }

    public Client getClient() {
	return client;
    }

    /**
     * Pop a value from a list, push it to another list and return it; or block
     * until one is available
     * 
     * @param source
     * @param destination
     * @param timeout
     * @return the element
     */
    public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
	client.brpoplpush(source, destination, timeout);
	client.setTimeoutInfinite();
	byte[] reply = client.getBinaryBulkReply();
	client.rollbackTimeout();
	return reply;
    }

    /**
     * Sets or clears the bit at offset in the string value stored at key
     * 
     * @param key
     * @param offset
     * @param value
     * @return
     */
    public Boolean setbit(byte[] key, long offset, byte[] value) {
	client.setbit(key, offset, value);
	return client.getIntegerReply() == 1;
    }

    /**
     * Returns the bit value at offset in the string value stored at key
     * 
     * @param key
     * @param offset
     * @return
     */
    public Boolean getbit(byte[] key, long offset) {
	client.getbit(key, offset);
	return client.getIntegerReply() == 1;
    }

    public Long setrange(byte[] key, long offset, byte[] value) {
	client.setrange(key, offset, value);
	return client.getIntegerReply();
    }

    public String getrange(byte[] key, long startOffset, long endOffset) {
	client.getrange(key, startOffset, endOffset);
	return client.getBulkReply();
    }

    public Long publish(byte[] channel, byte[] message) {
	client.publish(channel, message);
	return client.getIntegerReply();
    }

    public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
	client.setTimeoutInfinite();
	jedisPubSub.proceed(client, channels);
	client.rollbackTimeout();
    }

    public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
	client.setTimeoutInfinite();
	jedisPubSub.proceedWithPatterns(client, patterns);
	client.rollbackTimeout();
    }

    public Long getDB() {
	return client.getDB();
    }

    /**
     * Evaluates scripts using the Lua interpreter built into Redis starting
     * from version 2.6.0.
     * <p>
     * 
     * @return Script result
     */
    public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
	client.setTimeoutInfinite();
	client.eval(script, toByteArray(keys.size()), getParams(keys, args));
	return client.getOne();
    }

    private byte[][] getParams(List<byte[]> keys, List<byte[]> args) {
	int keyCount = keys.size();
	byte[][] params = new byte[keyCount + args.size()][];

	for (int i = 0; i < keyCount; i++)
	    params[i] = keys.get(i);

	for (int i = 0; i < keys.size(); i++)
	    params[keyCount + i] = args.get(i);

	return params;
    }

    public Object eval(byte[] script, byte[] keyCount, byte[][] params) {
	client.setTimeoutInfinite();
	client.eval(script, keyCount, params);
	return client.getOne();
    }

    public byte[] scriptFlush() {
	client.scriptFlush();
	return client.getBinaryBulkReply();
    }

    public List<Long> scriptExists(byte[]... sha1) {
	client.scriptExists(sha1);
	return client.getIntegerMultiBulkReply();
    }

    public byte[] scriptLoad(byte[] script) {
	client.scriptLoad(script);
	return client.getBinaryBulkReply();
    }

    public byte[] scriptKill() {
	client.scriptKill();
	return client.getBinaryBulkReply();
    }

    public byte[] slowlogReset() {
	client.slowlogReset();
	return client.getBinaryBulkReply();
    }

    public long slowlogLen() {
	client.slowlogLen();
	return client.getIntegerReply();
    }

    public List<byte[]> slowlogGetBinary() {
	client.slowlogGet();
	return client.getBinaryMultiBulkReply();
    }

    public List<byte[]> slowlogGetBinary(long entries) {
	client.slowlogGet(entries);
	return client.getBinaryMultiBulkReply();
    }
    
    public Long objectRefcount(byte[] key) {
		client.objectRefcount(key);
		return client.getIntegerReply();
	}
	
	public byte[] objectEncoding(byte[] key) {
		client.objectEncoding(key);
		return client.getBinaryBulkReply();
	}

	public Long objectIdletime(byte[] key) {
		client.objectIdletime(key);
		return client.getIntegerReply();
	}
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
