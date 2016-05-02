/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.numerics.*;

/**
 * A panel that displays and controls functional curve fits to a Dataset.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DatasetCurveFitter extends JPanel {

  // instance fields
  Dataset dataset; // the Dataset to fit
  KnownFunction fit; // the Function to fit to the data
  HessianMinimize hessian = new HessianMinimize();
  LevenbergMarquardt levmar = new LevenbergMarquardt();
  FunctionDrawer drawer;
  Color color = Color.red;
  JCheckBox autofitCheckBox;
  String[] fitNames;
  JComboBox fitDropDown;
  JTextField equation;
  JLabel rmsLabel;
  NumberField rmsField;
  JToolBar toolbar = new JToolBar();
  JTable paramTable;
  TableCellRenderer cellRenderer;
  SpinCellEditor spinCellEditor; // uses number-crawler spinner
  Map namedFits = new HashMap();
  Map fitEditors = new HashMap();
  int fitNumber = 1;
  JButton editButton;
  int prevFit;

  /**
   * Constructs a DatasetCurveFitter for the specified Dataset.
   *
   * @param data the dataset
   */
  public DatasetCurveFitter(Dataset data) {
    dataset = data;
    createGUI();
    fit(fit);
  }

  /**
   * Gets the function drawer.
   *
   * @return the drawer
   */
  public Drawable getDrawer() {
    return drawer;
  }

  /**
   * Gets the data.
   *
   * @return the dataset
   */
  public Dataset getData() {
    return dataset;
  }

  /**
   * Sets the dataset.
   *
   * @param data the dataset
   */
  public void setData(Dataset data) {
    dataset = data;
    fit(fit);
  }

  /**
   * Sets the color.
   *
   * @param newColor the color
   */
  public void setColor(Color newColor) {
    color = newColor;
    if (drawer != null) {
      drawer.setColor(newColor);
    }
  }

  /**
   * Fits the current fit function to the current data.
   */
  public void fit(Function fit) {
    if (drawer == null) selectFit((String)fitDropDown.getSelectedItem());
    if (fit == null) return;
    double[] x = dataset.getValidXPoints();
    double[] y = dataset.getValidYPoints();
    double devSq = 0;
  	// autofit if checkbox is selected
    double[] prevParams = null;
    // get deviation before fitting
  	double prevDevSq = getDevSquared(fit, x, y);
    if (autofitCheckBox.isSelected()) {
      if (fit instanceof KnownPolynomial) {
        KnownPolynomial poly = (KnownPolynomial)fit;
        poly.fitData(x, y);
      }
      else if (fit instanceof UserFunction) {
      	// use HessianMinimize to autofit user function 
        UserFunction f = (UserFunction)fit;
        double[] params = new double[f.getParameterCount()];
        // can't autofit if no parameters
        if (params.length > 0) {
          MinimizeUserFunction minFunc = new MinimizeUserFunction(f, x, y);
          prevParams = new double[params.length];
          for (int i = 0; i < params.length; i++) {
          	params[i] = prevParams[i] = f.getParameterValue(i);
          }
          double tol = 1.0E-6;
          int iterations = 20;
        	hessian.minimize(minFunc, params, iterations, tol);
          // get deviation after minimizing
          devSq = getDevSquared(fit, x, y);
        	// restore previous parameters and try Levenberg-Marquardt if Hessian fit is worse
        	if (devSq > prevDevSq) {
            for (int i = 0; i < prevParams.length; i++) {
            	f.setParameterValue(i, prevParams[i]);
            }
          	levmar.minimize(minFunc, params, iterations, tol);
            // get deviation after minimizing
            devSq = getDevSquared(fit, x, y);
        	}
        	// restore previous parameters and deviation if new fit still worse
        	if (devSq > prevDevSq) {
            for (int i = 0; i < prevParams.length; i++) {
            	f.setParameterValue(i, prevParams[i]);
            }
            devSq = prevDevSq;
            autofitCheckBox.setSelected(false);
            Toolkit.getDefaultToolkit().beep();
        	}
        }
      }
      drawer.functionChanged = true;
      paramTable.repaint();
    }
    if (devSq == 0) devSq = getDevSquared(fit, x, y);
    rmsField.setValue(Math.sqrt(devSq/x.length));
    firePropertyChange("fit", null, null); //$NON-NLS-1$
  }

  // _______________________ protected & private methods __________________________

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    setLayout(new BorderLayout());
    // create autofit checkbox
    autofitCheckBox = new JCheckBox("", true); //$NON-NLS-1$
  	autofitCheckBox.setSelected(false);
    autofitCheckBox.setOpaque(false);
    autofitCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	spinCellEditor.stopCellEditing();
      	paramTable.clearSelection();
        fit(fit);
      }
    });
    fitNames = new String[] {
        ToolsRes.getString("Function.Poly1.Name"), //$NON-NLS-1$
        ToolsRes.getString("Function.Poly2.Name"), //$NON-NLS-1$
        ToolsRes.getString("Function.Poly3.Name"), //$NON-NLS-1$
    		ToolsRes.getString("Function.Custom"),}; //$NON-NLS-1$
    prevFit = 0;
    fitDropDown = new JComboBox(fitNames);
    fitDropDown.setMaximumSize(fitDropDown.getMinimumSize());
    fitDropDown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String name = (String)fitDropDown.getSelectedItem();
        if (name.equals(ToolsRes.getString("Function.Custom"))) //$NON-NLS-1$
        	createFit(ToolsRes.getString("Function.Custom.Name")+fitNumber); //$NON-NLS-1$
        else selectFit(name);
      }
    });
    // create equation field
    equation = new JTextField();
    equation.setEditable(false);
    equation.setEnabled(true);
    equation.setBackground(Color.white);
    // create rms field and label
    rmsField = new NumberField(5) {
    	public Dimension getMaximumSize() {
    		Dimension dim = super.getMaximumSize();
    		dim.width = 32;
    		return dim;
    	}
    };
    rmsField.setEditable(false);
    rmsField.setEnabled(true);
    rmsField.setBackground(Color.white);
    rmsLabel = new JLabel();
    rmsLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 2));
    // create toolbar
    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    add(toolbar, BorderLayout.NORTH);
    // create table
    cellRenderer = new ParamCellRenderer();
    spinCellEditor = new SpinCellEditor();
    paramTable = new ParamTable(new ParamTableModel());
    paramTable.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        // clear selection if pressed on the name column
        if (paramTable.getSelectedColumn() == 0) {
          paramTable.clearSelection();
        }
      }
    });
    add(new JScrollPane(paramTable), BorderLayout.CENTER);
    // create edit button
    editButton = new JButton();
    editButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        UserFunction f = (UserFunction)fit;
      	// refresh and display function editor
    		UserFunctionEditor editor = (UserFunctionEditor)fitEditors.get(f);
    		editor.refresh();
    		editor.setVisible(true);
    		if (editor.isDeleted()) {
    	  	namedFits.remove(f.getName());
    			fitDropDown.removeItem(f.getName());
    		}
      }
    });
    refreshGUI();
  }

  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
    autofitCheckBox.setText(ToolsRes.getString("Checkbox.Autofit.Label")); //$NON-NLS-1$
    rmsLabel.setText(ToolsRes.getString("DatasetCurveFitter.Label.RMSDeviation")); //$NON-NLS-1$
    editButton.setText(ToolsRes.getString("DatasetCurveFitter.Button.Edit")); //$NON-NLS-1$
		paramTable.tableChanged(null);
		int n = fitDropDown.getItemCount();
		Object[] list = new Object[n];
	  fitNames = new String[] {
	  				ToolsRes.getString("Function.Poly1.Name"), //$NON-NLS-1$
	  				ToolsRes.getString("Function.Poly2.Name"), //$NON-NLS-1$
	  				ToolsRes.getString("Function.Poly3.Name"), //$NON-NLS-1$
	  				ToolsRes.getString("Function.Custom"),}; //$NON-NLS-1$
	  int k = n-fitNames.length;
		for (int i = 0; i < n; i++) {
			if (i < k)
				list[i] = fitDropDown.getItemAt(i);
			else list[i] = fitNames[i-k];
		}
		DefaultComboBoxModel model = new DefaultComboBoxModel(list);
		int i = fitDropDown.getSelectedIndex();
    fitDropDown.setModel(model);
    fitDropDown.setSelectedIndex(i);
  }

  /**
   * Selects a named fit and drawer.
   */
  protected void selectFit(String name) {
    fit = (KnownFunction)namedFits.get(name);
    if (fit == null) {
    	if (name.equals(ToolsRes.getString("Function.Poly1.Name"))) //$NON-NLS-1$
        fit = new KnownPolynomial(new double[] {0, 0});
    	else if (name.equals(ToolsRes.getString("Function.Poly2.Name"))) //$NON-NLS-1$
        fit = new KnownPolynomial(new double[] {0, 0, 0});
    	else if (name.equals(ToolsRes.getString("Function.Poly3.Name"))) //$NON-NLS-1$
        fit = new KnownPolynomial(new double[] {0, 0, 0, 0});
	    if (fit != null) 
	    	namedFits.put(name, fit);
    }
    // assemble display
    toolbar.removeAll();
    toolbar.add(fitDropDown);
    toolbar.addSeparator();
  	if (fit instanceof UserFunction) {
  		toolbar.add(editButton);
      toolbar.addSeparator();
  	}
    toolbar.add(equation);
    if (fit != null) {
    	for (int i = 0; i < fitDropDown.getItemCount(); i++) {
    		if (fitDropDown.getItemAt(i).equals(name)) {
        	prevFit = i;
    			break;
    		}
    	}
      toolbar.add(rmsLabel);
      toolbar.add(rmsField);
  		toolbar.add(autofitCheckBox);
	    FunctionDrawer prev = drawer;
	    drawer = new FunctionDrawer(fit);
	    drawer.setColor(color);
	    paramTable.tableChanged(null);
	    // construct equation string
	    String depVar = GUIUtils.removeSubscripting(dataset.getColumnName(1));
	    String indepVar = GUIUtils.removeSubscripting(dataset.getColumnName(0));
	    equation.setText(depVar + " = " + fit.getExpression(indepVar)); //$NON-NLS-1$
	    firePropertyChange("drawer", prev, drawer); //$NON-NLS-1$
	    fit(fit);
    }
  }

  /**
   * Creates a custom fit.
   */
  protected void createFit(String name) {
  	int prev = prevFit;
  	autofitCheckBox.setSelected(false);
  	// create new user function and select it 
		UserFunction f = new UserFunction(name);
		String colName = GUIUtils.removeSubscripting(dataset.getColumnName(0));
		f.setExpression("0", colName); //$NON-NLS-1$		
  	namedFits.put(name, f);
		fitDropDown.insertItemAt(name, 0);
		fitDropDown.setSelectedItem(name);
		selectFit(name);
  	// create and display function editor
		UserFunctionEditor editor = new UserFunctionEditor(f, this);
		fitEditors.put(f, editor);
		editor.setVisible(true);
		if (editor.isDeleted()) {
	  	namedFits.remove(name);
			fitDropDown.removeItem(name);
			fitDropDown.setSelectedIndex(prev);
			selectFit((String)fitDropDown.getItemAt(prev));
		}
		else {
			fitNumber++;
		}
  }

  /**
   * Gets the total deviation squared between function and data 
   */
  private double getDevSquared(Function f, double[] x, double[] y) {
    double total = 0;
    for (int i = 0; i < x.length; i++) {
    	double next = f.evaluate(x[i]);
    	double dev = (next-y[i]);
    	total += dev*dev;
    }
    return total;
  }

		
// _______________________ inner classes __________________________

  /**
   * A table to display and edit parameters.
   */
  class ParamTable extends JTable {
    public ParamTable(ParamTableModel model) {
      super(model);
      setPreferredScrollableViewportSize(new Dimension(60, 50));
      setGridColor(Color.blue);
      JTableHeader header = getTableHeader();
      header.setForeground(Color.blue);
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
      return cellRenderer;
    }

    public TableCellEditor getCellEditor(int row, int column) {
      spinCellEditor.index = row;
      return spinCellEditor;
    }

  }

  /**
   * A class to provide model data for the parameters table.
   */
  class ParamTableModel extends AbstractTableModel {

    public String getColumnName(int col) {
      return col == 0? ToolsRes.getString("Table.Heading.Parameter"): //$NON-NLS-1$
          ToolsRes.getString("Table.Heading.Value"); //$NON-NLS-1$
    }

    public int getRowCount() {
      return fit == null? 0: fit.getParameterCount();
    }

    public int getColumnCount() {
      return 2;
    }

    public Object getValueAt(int row, int col) {
      if (col == 0) return fit.getParameterName(row);
      return new Double(fit.getParameterValue(row));
    }

    public boolean isCellEditable(int row, int col) {
      return col != 0;
    }

    public Class getColumnClass(int c) {
      return getValueAt(0, c).getClass();
    }

  }

  /**
   * A cell renderer for the parameter table.
   */
  class ParamCellRenderer extends JLabel implements TableCellRenderer {
    Color lightBlue = new Color(204, 204, 255);
    Color lightGray = javax.swing.UIManager.getColor("Panel.background"); //$NON-NLS-1$
    Font labelFont = getFont();
    Font fieldFont = new JTextField().getFont();

    // Constructor
    public ParamCellRenderer() {
      super();
      setOpaque(true); // make background visible
      setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 2));
    }

    // Returns a label for the specified cell.
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
      setHorizontalAlignment(col == 0? SwingConstants.RIGHT: SwingConstants.LEFT);
      if(value instanceof String) { // parameter name string
        setFont(labelFont);
        setBackground(lightGray);
        setForeground(Color.black);
        setText(value.toString());
      }
      else { // Double value
        setFont(fieldFont);
        setBackground(isSelected? lightBlue: Color.white);
        setForeground(isSelected? Color.red: Color.black);
        Format format = spinCellEditor.field.format;
        setText(format.format(value));
      }
      return this;
    }
  }

  /**
   * A cell editor that uses a JSpinner with a number crawler model.
   */
  class SpinCellEditor extends AbstractCellEditor implements TableCellEditor {
    JPanel panel = new JPanel(new BorderLayout());
    SpinnerNumberCrawlerModel crawlerModel = new SpinnerNumberCrawlerModel(1);
    JSpinner spinner;
    NumberField field;
    int index;
    JLabel stepSizeLabel = new JLabel("10%"); //$NON-NLS-1$

    // Constructor.
    SpinCellEditor() {
      panel.setOpaque(false);
      spinner = new JSpinner(crawlerModel);
      spinner.setToolTipText(ToolsRes.getString("Table.Spinner.ToolTip")); //$NON-NLS-1$
      spinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          autofitCheckBox.setSelected(false);
          double val = ((Double)spinner.getValue()).doubleValue();
          field.setValue(val);
          fit.setParameterValue(index, val);
          drawer.functionChanged = true;
          fit(fit);
        }
      });
      field = new NumberField(10);
      spinner.setBorder(BorderFactory.createEmptyBorder(0,1,1,0));
      spinner.setEditor(field);
      stepSizeLabel.addMouseListener(new MouseInputAdapter() {
        public void mousePressed(MouseEvent e) {
            JPopupMenu popup = new JPopupMenu();
            ActionListener listener = new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                // set the percent delta
                double percent = Double.parseDouble(e.getActionCommand());
                crawlerModel.setPercentDelta(percent);
                crawlerModel.refreshDelta();
                stepSizeLabel.setText(e.getActionCommand() + "%"); //$NON-NLS-1$
             }
            };
            for (int i = 0; i < 3; i++) {
              String val = i==0? "10": i==1? "1.0": "0.1"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              JMenuItem item = new JMenuItem(val + "%"); //$NON-NLS-1$
              item.setActionCommand(val);
              item.addActionListener(listener);
              popup.add(item);
            }
            // show the popup
            popup.show(stepSizeLabel, 0, stepSizeLabel.getHeight());
          }
      });
      field.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          JComponent comp = (JComponent) e.getSource();
          if(e.getKeyCode()==KeyEvent.VK_ENTER) {
            spinner.setValue(new Double(field.getValue()));
            comp.setBackground(Color.white);
            crawlerModel.refreshDelta();
          }
          else {
            comp.setBackground(Color.yellow);
          }
        }
      });
      panel.add(spinner, BorderLayout.CENTER);
      panel.add(stepSizeLabel, BorderLayout.EAST);
    }

    // Gets the component to be displayed while editing.
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      spinner.setValue(value);
      crawlerModel.refreshDelta();
      return panel;
    }

    // Determines when editing starts.
    public boolean isCellEditable(EventObject e) {
      if(e instanceof MouseEvent) {
      	return true;
      } else if(e instanceof ActionEvent) {
        return true;
      }
      return false;
    }

    // Called when editing is completed.
    public Object getCellEditorValue() {
      if (field.getBackground() == Color.yellow) {
        fit.setParameterValue(index, field.getValue());
        drawer.functionChanged = true;
        DatasetCurveFitter.this.firePropertyChange("fit", null, null); //$NON-NLS-1$
        field.setBackground(Color.white);
      }
      return null;
    }
  }

  /**
   * A number spinner model with a settable delta.
   */
  class SpinnerNumberCrawlerModel extends AbstractSpinnerModel {
    double val = 0;
    double delta;
    double percentDelta = 10;

    public SpinnerNumberCrawlerModel(double initialDelta) {
      delta = initialDelta;
    }

    public Object getValue() {
      return new Double(val);
    }

    public Object getNextValue() {
      return new Double(val + delta);
    }

    public Object getPreviousValue() {
      return new Double(val - delta);
    }

    public void setValue(Object value) {
      if (value != null) {
        val = ((Double)value).doubleValue();
        fireStateChanged();
      }
    }

    public void setPercentDelta(double percent) {
      percentDelta = percent;
    }

    public double getPercentDelta() {
      return percentDelta;
    }

    // refresh delta based on current value and percent
    public void refreshDelta() {
      if (val != 0) {
        delta = Math.abs(val * percentDelta / 100);
      }
    }

  }

  /**
   * A polynomial that implements KnownFunction.
   */
  class KnownPolynomial
      extends PolynomialLeastSquareFit implements KnownFunction {
    String[] names = {"a", "b", "c", "d", "e", "f"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    KnownPolynomial(double[] xdata, double[] ydata, int degree) {
      super(xdata, ydata, degree);
    }

    KnownPolynomial(double[] coeffs) {
      super(coeffs);
    }

    /**
     * Gets the parameter count.
     * @return the number of parameters
     */
    public int getParameterCount() {
      return coefficients.length;
    }

    /**
     * Gets a parameter name.
     *
     * @param i the parameter index
     * @return the name of the parameter
     */
    public String getParameterName(int i) {
      return names[i];
    }

    /**
     * Gets a parameter value.
     *
     * @param i the parameter index
     * @return the value of the parameter
     */
    public double getParameterValue(int i) {
      return coefficients[coefficients.length-i-1];
    }

    /**
     * Sets a parameter value.
     *
     * @param i the parameter index
     * @param value the value
     */
    public void setParameterValue(int i, double value) {
      coefficients[coefficients.length-i-1] = value;
    }

    /**
     * Gets the equation.
     *
     * @param indepVarName the name of the independent variable
     * @return the equation
     */
    public String getExpression(String indepVarName) {
      StringBuffer eqn = new StringBuffer();
      int end = coefficients.length-1;
      for (int i = 0; i <=end; i++) {
        eqn.append(getParameterName(i));
        if (end-i > 0) {
          eqn.append("*"); //$NON-NLS-1$
          eqn.append(indepVarName);
          if (end-i > 1) {
            eqn.append("^"); //$NON-NLS-1$
            eqn.append(end - i);
          }
          eqn.append(" + "); //$NON-NLS-1$
        }
      }
      return eqn.toString();
    }
  }

  /**
   * A function whose value is the total deviation squared 
   * between a multivariable function and a set of data points.
   * This is minimized by the HessianMinimize class.
   */
  public class MinimizeMultiVarFunction implements MultiVarFunction {
  	MultiVarFunction f;
    double[] x, y; // the data
    double[] vars = new double[5];
    
    // Constructor
    MinimizeMultiVarFunction(MultiVarFunction f, double[] x, double[] y) {
    	this.f = f;
    	this.x = x;
      this.y = y;
    }

    // Evaluates the function
    public double evaluate(double[] params) {
       System.arraycopy(params, 0, vars, 1, 4);
       double sum = 0.0;
       for(int i = 0, n = x.length; i<n; i++) {
          vars[0] = x[i];
          // evaluate the function and find deviation
          double dev = y[i] - f.evaluate(vars);
          // sum the squares of the deviations
          sum += dev*dev;
       }
       return sum;
    }
 }

  /**
   * A function whose value is the total deviation squared 
   * between a user function and a set of data points.
   * This function is minimized by the HessianMinimize class.
   */
  public class MinimizeUserFunction implements MultiVarFunction {
  	UserFunction f;
    double[] x, y; // the data
    
    // Constructor
    MinimizeUserFunction(UserFunction f, double[] x, double[] y) {
    	this.f = f;
    	this.x = x;
      this.y = y;
    }

    // Evaluates this function
    public double evaluate(double[] params) {
      // set the parameter values of the user function
      for (int i = 0; i < params.length; i++) {
      	f.setParameterValue(i, params[i]);
      }
      double sum = 0.0;
      for (int i = 0; i < x.length; i++) {
        // evaluate the user function and find deviation
        double dev = y[i] - f.evaluate(x[i]);
        // sum the squares of the deviations
        sum += dev*dev;
      }
      return sum;
    }
 }

  /**
   * A JTextField that accepts only numbers.
   */
  class NumberField extends JTextField {

    // instance fields
    protected NumberFormat format = NumberFormat.getInstance();
    protected double prevValue;

    public NumberField(int columns) {
      super(columns);
      if (format instanceof DecimalFormat) {
        ((DecimalFormat)format).applyPattern("0.000E0"); //$NON-NLS-1$
      }
      setForeground(Color.black);
    }

    public double getValue() {
      if (getText().equals(format.format(prevValue)))
        return prevValue;
      double retValue;
      try {
        retValue = format.parse(getText()).doubleValue();
      }
      catch (ParseException e) {
        Toolkit.getDefaultToolkit().beep();
        setValue(prevValue);
        return prevValue;
      }
      return retValue;
    }

    public void setValue(double value) {
      if (!isVisible())
        return;
      setText(format.format(value));
      prevValue = value;
    }
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */

