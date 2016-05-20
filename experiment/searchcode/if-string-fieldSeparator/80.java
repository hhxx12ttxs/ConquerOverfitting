/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package org.gvsig.symbology.gui.layerproperties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import org.gvsig.gui.beans.AcceptCancelPanel;
import org.gvsig.gui.beans.swing.GridBagLayoutPanel;
import org.gvsig.gui.beans.swing.JBlank;
import org.gvsig.gui.beans.swing.JButton;
import org.gvsig.symbology.fmap.labeling.parse.LabelExpressionParser;
import org.gvsig.symbology.fmap.labeling.parse.ParseException;
import org.gvsig.symbology.fmap.rendering.filter.operations.Expression;
import org.gvsig.symbology.fmap.rendering.filter.operations.ExpressionException;
import org.gvsig.symbology.fmap.styles.SimpleLabelStyle;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.styles.ILabelStyle;
import com.iver.cit.gvsig.fmap.core.styles.IStyle;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ITextSymbol;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelClass;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelingFactory;
import com.iver.cit.gvsig.gui.styling.AbstractStyleSelectorFilter;
import com.iver.cit.gvsig.gui.styling.StylePreviewer;
import com.iver.cit.gvsig.gui.styling.StyleSelector;

/**
 *
 * LabelClassProperties.java
 *
 *
 * @author jaume dominguez faus - jaume.dominguez@iver.es May 27, 2008
 *
 */
public class LabelClassProperties extends JPanel implements IWindow,
ActionListener {
	private static final String TEST_SQL = "TEST_SQL";
	private static final String DELETE_FIELD = "DELETE_FIELD";
	private static final long serialVersionUID = 6528513396536811057L;
	private static final String ADD_FIELD = "ADD_FIELD";
	private static final String SELECT_STYLE = "SELECT_STYLE";
	private static final String CLEAR_STYLE = "CLEAR_STYLE";
	private static final String VERIFY = "VERIFY";
	private JTextField txtName;
	private LabelClass lc, clonedClass;
	private JTable tblExpressions;
	private JButton btnRemoveField;
	private JButton btnAddField;
	private JButton btnVerify;
	private JComponent styPreviewer;
	private JButton btnSelStyle;
	private JRadioButton rdBtnFilteredFeatures;
	private JRadioButton rdBtnAllFeatures;
	private JTextField txtSQL;
	private JCheckBox chkLabelFeatures;
	private LabelTextStylePanel textStyle;
	private JPanel sqlPnl;
	private LabelClassPreview labelPreview;
	private String[] fieldNames;
	private int[] fieldTypes;
	private JButton btnDontUseStyle;
	private boolean accepted = true;



	/**
	 * <p>
	 * Creates a new instance of the dialog that configures all the properties
	 * of a LabelClass.
	 * </p>
	 * @param strategy
	 * @param asWindow
	 */
	public LabelClassProperties(String[] fieldNames, int[] fieldTypes) {
		if (fieldNames==null)  {
			throw new IllegalArgumentException("fieldNames "+
					PluginServices.getText(this, "cannot_be_null"));
		}

		if (fieldTypes==null) {
			throw new IllegalArgumentException("fieldTypes "+
					PluginServices.getText(this, "cannot_be_null"));

		}

		if (fieldNames.length != fieldTypes.length) {
			throw new IllegalArgumentException(
					PluginServices.getText(this, "names_and_types_count_are_disctint"));
		}
		this.fieldNames = fieldNames;
		this.fieldTypes = fieldTypes;
		initialize();
	}

	private void initialize() {
		setPreferredSize(new Dimension(810, 580));
		setLayout(new BorderLayout(1, 1));

		txtName = new JTextField(40);
		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 2));
		northPanel.add(new JLabel(PluginServices.getText(this, "name") + ":"));
		northPanel.add(txtName);
		add(northPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
		textStyle = new LabelTextStylePanel();
		textStyle.addActionListener(this);
		centerPanel.add(textStyle, BorderLayout.NORTH);

		JPanel labelExpressionsPanel = new JPanel(new BorderLayout(1, 1));

		labelExpressionsPanel.add(new JBlank(10, 30), BorderLayout.WEST);
		labelExpressionsPanel.add(new JScrollPane(getTableFields()),
				BorderLayout.CENTER);

		JPanel a = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		a.add(new JLabel("<html><b>"+PluginServices
				.getText(this, "text_fields")+"</b></html>"));
		centerPanel.add(labelExpressionsPanel, BorderLayout.CENTER);
		GridBagLayoutPanel aux = new GridBagLayoutPanel();// (new
		// FlowLayout(FlowLayout.CENTER));
		aux.setPreferredSize(new Dimension(120, 100));
		aux.addComponent(getBtnVerify());
		aux.addComponent(getBtnAddField());
		aux.addComponent(getBtnRemoveField());
		centerPanel.add(aux, BorderLayout.EAST);


		aux = new GridBagLayoutPanel();
//		aux.setBorder(BorderFactory.createTitledBorder(PluginServices.getText(
//		this, "features")));
		rdBtnAllFeatures = new JRadioButton(PluginServices.getText(this,
		"all_features"));
		rdBtnFilteredFeatures = new JRadioButton(PluginServices.getText(this,
		"filtered_features")+" (SQL GDBMS)");

		rdBtnAllFeatures.addActionListener(this);
		rdBtnFilteredFeatures.addActionListener(this);

		ButtonGroup g = new ButtonGroup();
		g.add(rdBtnAllFeatures);
		g.add(rdBtnFilteredFeatures);
		aux.addComponent(chkLabelFeatures = new JCheckBox(PluginServices
				.getText(this, "label_features_in_this_class")));
		aux.addComponent("", rdBtnAllFeatures);
		aux.addComponent("", rdBtnFilteredFeatures);
		sqlPnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sqlPnl.add(new JLabel("   SQL: select * from "));
		sqlPnl.add(new JLabel("%" + PluginServices.getText(this, "layer_name")
				+ "%"));
		sqlPnl.add(new JLabel(" where  "));
		sqlPnl.add(txtSQL = new JTextField(40));
		sqlPnl.add(new JLabel(";"));
		aux.addComponent("", sqlPnl);


		JPanel auxPanel = new JPanel(new BorderLayout(1,1));

		JPanel aux2 = new JPanel();
		auxPanel.setBorder(BorderFactory.createTitledBorder(PluginServices.getText(
				this, "background_style")));

		aux2.add(getStylePreviewer());



		GridBagLayoutPanel aux3 = new GridBagLayoutPanel();
		aux3.setPreferredSize(new Dimension(120, 100));
		aux3.addComponent(getBtnSelectStyle());
		aux3.addComponent(getBtnDontUseStyle());

		auxPanel.add(aux2,BorderLayout.CENTER);
		auxPanel.add(aux3,BorderLayout.EAST);

		JPanel aux4 = new JPanel(new GridLayout(1,2));
		aux4.add(auxPanel);
		aux2 = new JPanel(new BorderLayout());
		aux2.setBorder(BorderFactory.createTitledBorder(PluginServices.getText(
				this, "preview")));
		aux2.add(labelPreview = new LabelClassPreview(), BorderLayout.CENTER);
		aux4.add(aux2);

		GridBagLayoutPanel aux5 = new GridBagLayoutPanel();
		aux5.addComponent("", aux);
		aux5.addComponent(new JBlank(3, 5));
		aux5.addComponent("", aux4);
		centerPanel.add(aux5, BorderLayout.SOUTH);
		add(centerPanel, BorderLayout.CENTER);

		add(new AcceptCancelPanel(this, this), BorderLayout.SOUTH);

		chkLabelFeatures.addActionListener(this);
		txtName.addActionListener(this);

	}

	private JComponent getStylePreviewer() {

		if (styPreviewer == null) {
			styPreviewer = new StylePreviewer();
			styPreviewer.setPreferredSize(new java.awt.Dimension(90,90));
			styPreviewer.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
			((StylePreviewer) styPreviewer).setShowOutline(true);
		}
		return styPreviewer;

	}

	private JButton getBtnSelectStyle() {
		if (btnSelStyle == null) {
			btnSelStyle = new JButton(PluginServices.getText(this, "select"));
			btnSelStyle.setActionCommand(SELECT_STYLE);
			btnSelStyle.addActionListener(this);
		}

		return btnSelStyle;
	}


	private JButton getBtnRemoveField() {
		if (btnRemoveField == null) {
			btnRemoveField = new JButton(PluginServices.getText(this, "remove"));
			btnRemoveField.setActionCommand(DELETE_FIELD);
			btnRemoveField.addActionListener(this);
		}

		return btnRemoveField;
	}

	private JButton getBtnAddField() {
		if (btnAddField == null) {
			btnAddField = new JButton(PluginServices.getText(this, "add"));
			btnAddField.setActionCommand(ADD_FIELD);
			btnAddField.addActionListener(this);
		}

		return btnAddField;
	}

	private JButton getBtnVerify() {
		if(btnVerify == null) {
			btnVerify = new JButton(PluginServices.getText(this, "verify"));
			btnVerify.setActionCommand(VERIFY);
			btnVerify.addActionListener(this);
		}
		return btnVerify;
	}

	private JButton getBtnDontUseStyle() {
		if (btnDontUseStyle == null) {
			btnDontUseStyle = new JButton(PluginServices.getText(this, "no_style"));
			btnDontUseStyle.setActionCommand(CLEAR_STYLE);
			btnDontUseStyle.addActionListener(this);
		}

		return btnDontUseStyle;
	}

	private JTable getTableFields() {
		if (tblExpressions == null) {
			tblExpressions = new JTable();
			tblExpressions.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		}

		return tblExpressions;
	}


	private void setComponentEnabled(Component c, boolean b) {
		if (c instanceof JComponent) {
			JComponent c1 = (JComponent) c;
			for (int i = 0; i < c1.getComponentCount(); i++) {
				setComponentEnabled(c1.getComponent(i), b);
			}
		}
		c.setEnabled(b);
	}



	public void setLabelClass(LabelClass labelClass) {
		if (labelClass == null)
			return;

		lc = labelClass;
		clonedClass = LabelingFactory
		.createLabelClassFromXML(lc.getXMLEntity());

		labelPreview.setLabelClass(clonedClass);
		if (!clonedClass.isUseSqlQuery()) {
			rdBtnAllFeatures.setSelected(true);
			txtSQL.setText("");
		} else {
			rdBtnFilteredFeatures.setSelected(true);
			txtSQL.setText(clonedClass.getSQLQuery());
		}

		chkLabelFeatures.setSelected(clonedClass.isVisible());
		rdBtnFilteredFeatures.setSelected(clonedClass.isUseSqlQuery());
		rdBtnAllFeatures.setSelected(!clonedClass.isUseSqlQuery());
		((StylePreviewer) styPreviewer).setStyle(clonedClass.getLabelStyle());
		txtName.setText(clonedClass.getName());
		String expr = lc.getStringLabelExpression();


		String EOExpr = LabelExpressionParser.tokenFor(LabelExpressionParser.EOEXPR);

		if (expr == null)
			expr = EOExpr;


		expr = expr.trim();
		if (!expr.endsWith(EOExpr)) {
			throw new Error("Invalid expression. Missing EOExpr token ("+EOExpr+").\n"+expr);
		}
		expr = expr.substring(0, expr.length()-1);

		expr = expr.trim();

		ITextSymbol lcTextSymbol = lc.getTextSymbol();
		ITextSymbol clonedSymbol = (ITextSymbol) SymbologyFactory.createSymbolFromXML(
				((ISymbol)lcTextSymbol).getXMLEntity(), lcTextSymbol.getDescription());

		textStyle.setModel(
				clonedSymbol, //lc.getTextSymbol(),
				lc.getUnit(),
				lc.getReferenceSystem());

		getTableFields().setRowHeight(22);
		getTableFields().setModel(new FieldTableExpressions(lc.getLabelExpressions()));
		TableColumnModel colMod = getTableFields().getColumnModel();
		colMod.getColumn(0).setPreferredWidth(100);
		colMod.getColumn(0).setWidth(100);
		colMod.getColumn(1).setResizable(true);
		colMod.getColumn(1).setPreferredWidth(513);
		getTableFields().setDefaultEditor(Object.class, new DefaultEditor());
		repaint();

		actionPerformed(new ActionEvent(this, 0, null));
	}

	private class DefaultEditor extends AbstractCellEditor implements
			TableCellEditor, ActionListener {

		JPanel editor;
		LabelExpressionEditorPanel dialog;
		private JTextField text;
		private JButton button;
		protected static final String EDIT = "edit";

		public DefaultEditor() {
			editor = new JPanel();
			editor.setLayout(new GridBagLayout());
			GridBagConstraints cons = new GridBagConstraints();
//			cons.anchor = GridBagConstraints.FIRST_LINE_START;
			cons.fill = cons.BOTH;
			cons.weightx=1.0;
			cons.weighty =1.0;
			text = new JTextField();
			editor.add(text, cons);

			GridBagConstraints cons1 = new GridBagConstraints();
//			cons.anchor = GridBagConstraints.FIRST_LINE_END;
			cons1.fill = cons1.VERTICAL;
			cons1.weighty =1.0;
//			cons.gridheight = GridBagConstraints.REMAINDER;
			button = new JButton("...");
			button.setActionCommand(DefaultEditor.EDIT);
			button.addActionListener(this);
			editor.add(button,cons1);

			editor.updateUI();

			// Set up the dialog that the button brings up.
			dialog = new LabelExpressionEditorPanel(fieldNames,fieldTypes);
		}

		public void actionPerformed(ActionEvent e) {
			if (EDIT.equals(e.getActionCommand())) {

				dialog.setValue(text.getText());

				PluginServices.getMDIManager().addWindow(dialog);


				//fireEditingStopped(); // Make the renderer reappear.

				text.setText(dialog.getValue());

			}
		}

		// Implement the one CellEditor method that AbstractCellEditor doesn't.
		public Object getCellEditorValue() {
			return text.getText();
		}

		// Implement the one method defined by TableCellEditor.
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			text.setText((String) value);
			return editor;
		}
	}




	public WindowInfo getWindowInfo() {
		WindowInfo wi = new WindowInfo(WindowInfo.MODALDIALOG
				| WindowInfo.RESIZABLE);
		wi.setTitle(PluginServices.getText(this, "label_class_properties"));
		wi.setWidth(getPreferredSize().width + 8);
		wi.setHeight(getPreferredSize().height);
		return wi;
	}

	public Object getWindowProfile() {
		return WindowInfo.DIALOG_PROFILE;
	}


	/*
	 * Return a hashtable with example values.
	 * Only for verification purposes.
	 */
	private Hashtable<String, Value> getValuesTable(String[] fNames, int[] fTypes) {
		Hashtable<String, Value> parser_symbol_table = new Hashtable<String, Value>();

		Random rand = new Random();
		byte b = 0;
    	short s = (short)rand.nextInt(Short.MAX_VALUE);
    	int i = rand.nextInt();
		long l = rand.nextLong();
		boolean bool = rand.nextBoolean();
		float f = rand.nextFloat();
		double d = rand.nextDouble();

		for (int j = 0; j < fNames.length; j++) {
			Value value=null;

			int type = fTypes[j];
            switch (type) {
            case Types.BIGINT:
                value = ValueFactory.createValue(l);
                break;
            case Types.BIT:
            case Types.BOOLEAN:
                value = ValueFactory.createValue(bool);
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                value = ValueFactory.createValue("");
                break;
            case Types.DATE:
                Date auxDate = new Date();
            	if (auxDate != null){
                    value = ValueFactory.createValue(auxDate);
            	}
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.FLOAT:
            case Types.DOUBLE:
                value = ValueFactory.createValue(d);
                break;
            case Types.INTEGER:
                value = ValueFactory.createValue(i);
                break;
            case Types.REAL:
                value = ValueFactory.createValue(f);
                break;
            case Types.SMALLINT:
                value = ValueFactory.createValue(s);
                break;
            case Types.TINYINT:
                value = ValueFactory.createValue(b);
                break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                byte[] auxByteArray = {b}; //new byte[1];
                value = ValueFactory.createValue(auxByteArray);
                break;
            case Types.TIMESTAMP:
           		value = ValueFactory.createValue(new Timestamp(l));
                break;
            case Types.TIME:
            	value = ValueFactory.createValue(new Time(l));
                break;
            default:
            	value = ValueFactory.createValue("");
            	break;
        }
			parser_symbol_table.put(fNames[j], value);
		}
		return parser_symbol_table;
	}

	private boolean verifyExpresion(String expresion, Hashtable<String, Value> symbols){
		LabelExpressionParser parser;
		try {
			parser = new LabelExpressionParser(new StringReader(expresion),symbols);
			parser.LabelExpression();
			Expression expr = ((Expression)parser.getStack().pop());
			expr.evaluate();
		} catch (ExpressionException e2) {
			return false;
		} catch (ParseException e) {
			return false;
		} catch (Exception e3) {
			return false;
		}
		return true;
	}

	private boolean verifyExpressions(){
		Hashtable<String, Value> symbols = getValuesTable(this.fieldNames, this.fieldTypes);
		String[] expressions = ((FieldTableExpressions) getTableFields().getModel()).getExpression();
		if(expressions.length == 1 && expressions[0].equals("")){
			return true;
		}
		boolean result = true;
		String message = "";
		for (int i = 0; i < expressions.length; i++) {
			String expresion = expressions[i];
			if(!verifyExpresion(expresion, symbols)){
				result = false;
				if(message.compareTo("")!=0){
					message = message +	" , " + (i+1);
				} else {
					message = " "+PluginServices.getText(this, "at_fields")+" "+(i+1);
				}
			}
		}
		message = message +".";
		if(!result){
			JOptionPane.showMessageDialog((Component)PluginServices.getMainFrame(),
					PluginServices.getText(this, "malformed_or_empty_expression")+
					message);
		}
		return result;
	}

	public boolean isAccepted(){
		return accepted;
	}

	public void actionPerformed(ActionEvent e) {
		setComponentEnabled(sqlPnl, rdBtnFilteredFeatures.isSelected());
		applySettings();
		if ("OK".equals(e.getActionCommand())) {
			clonedClass.setUnit(textStyle.getUnit());
			clonedClass.setReferenceSystem(textStyle.getReferenceSystem());
			clonedClass.setTextSymbol(textStyle.getTextSymbol());
			lc = clonedClass;
			accepted = true;
			try {
				PluginServices.getMDIManager().closeWindow(this);
			} catch (Exception ex) {
				// this only happens when running this as stand-alone app
				// from main method;
				ex.printStackTrace();
			}
		} else if ("CANCEL".equals(e.getActionCommand())) {
			accepted = false;
			setLabelClass(lc);
			try {
				PluginServices.getMDIManager().closeWindow(this);
			} catch (Exception ex) {
				// this only happens when running this as stand-alone app
				// from main method;
				ex.printStackTrace();
			}
		} else if (CLEAR_STYLE.equals(e.getActionCommand())) {
			clonedClass.setLabelStyle(null);
//			styPreviewer.setStyle(null);
			((StylePreviewer) styPreviewer).setStyle(null);
		} else if (TEST_SQL.equals(e.getActionCommand())) {
			System.out.println(TEST_SQL);
		} else if (DELETE_FIELD.equals(e.getActionCommand())) {
			int[] rowInd = getTableFields().getSelectedRows();
			for (int i = rowInd.length-1; i >= 0 ; i--) {
				delField(rowInd[i]);
			}
			clonedClass.setLabelExpressions(((FieldTableExpressions) getTableFields().getModel()).getExpression());
			setLabelClass(clonedClass);
		} else if (VERIFY.equals(e.getActionCommand())) {
			verifyExpressions();
		} else if (ADD_FIELD.equals(e.getActionCommand())) {
			addField();
		} else if (SELECT_STYLE.equals(e.getActionCommand())) {

			IStyle myStyle = ((StylePreviewer)styPreviewer).getStyle();
			StyleSelector stySel = new StyleSelector(
					myStyle,
					FShape.TEXT,  new AbstractStyleSelectorFilter(new SimpleLabelStyle()) {
					});

			stySel.setTitle(PluginServices.getText(this, "style_selector"));


			PluginServices.getMDIManager().addWindow(stySel);
			ILabelStyle sty = (ILabelStyle) stySel.getSelectedObject();

			if (sty != null) {
				// gather the style and apply to the class
				clonedClass.setLabelStyle(sty);
				clonedClass.setUnit(stySel.getUnit());
				clonedClass.setReferenceSystem(stySel.getReferenceSystem());
				setLabelClass(clonedClass);
			}

//			styPreviewer.setStyle(sty);
			((StylePreviewer) styPreviewer).setStyle(sty);

		}else if (e.getSource().equals(rdBtnAllFeatures) || e.getSource().equals(rdBtnFilteredFeatures)){
			clonedClass.setUseSqlQuery(rdBtnFilteredFeatures.isSelected());
		}

		repaint();
	}

	private void applySettings() {
		clonedClass.setVisible(chkLabelFeatures.isSelected());
		clonedClass.setName(txtName.getText());
		clonedClass.setSQLQuery(txtSQL.getText());
		JTable tableFields=getTableFields();
		TableCellEditor cellEditor=tableFields.getCellEditor();
		if(cellEditor != null){
			int row = tableFields.getEditingRow();
			int column = tableFields.getEditingColumn();
			cellEditor.stopCellEditing();
			Object value = cellEditor.getCellEditorValue();

			if (value != null) {
				((FieldTableExpressions) tableFields.
						getModel()).setValueAt(value, row, column);
			}
		}
		clonedClass.setLabelExpressions(
				((FieldTableExpressions) tableFields.
						getModel()).getExpression());
		clonedClass.setTextSymbol(textStyle.getTextSymbol());
	}

	private void addField() {
		addField("");
	}

	private void addField(String fieldExpr) {
		FieldTableExpressions m = ((FieldTableExpressions) getTableFields().getModel());
		m.addRow(new Object[] { m.getRowCount()+1, fieldExpr });
	}

	private void delField(int fieldIndex) {
		try {
			((FieldTableExpressions) getTableFields().getModel()).removeRow(fieldIndex);
		} catch (ArrayIndexOutOfBoundsException ex) {}
	}


	public LabelClass getLabelClass() {
		return lc;
	}



	private static final Object[] TABLE_HEADERS = new String[] {
		PluginServices.getText(FieldTableExpressions.class, "field_number"),
		PluginServices.getText(FieldTableExpressions.class, "label_expression")
		+" ("+ PluginServices.getText(FieldTableExpressions.class, "SLD_grammar") + ")",

	};
	private static String fieldSeparator = LabelExpressionParser.tokenImage[LabelExpressionParser.EOFIELD].replaceAll("\"", "");
	private class FieldTableExpressions extends DefaultTableModel {
		private static final long serialVersionUID = 2002427714889477770L;
		public FieldTableExpressions(String[] expressions) {
			super();

			if(expressions != null && expressions.length > 0){
				Integer[] aux = new Integer[expressions.length];
				for (int i = 0; i < aux.length; i++) {
					aux[i] = i+1;
				}

				Object[][] values = new Object[aux.length][2];
				for (int i = 0; i < values.length; i++) {
					values[i][0] = aux[i];
					values[i][1] = expressions[i];
				}
				setDataVector(values, TABLE_HEADERS);
			}
			else{
				Object[][] values = new Object[1][2];
				values[0][0] = 1;
				values[0][1] = "";
				setDataVector(values,TABLE_HEADERS);
			}

		}


		@Override
		public boolean isCellEditable(int row, int column) {
//			System.out.println("FieldTableExpressions.isCellEditable() "+(column == 1));
			return column == 1;
		}

		public String[] getExpression() {

			String[] expressions = new String[getRowCount()];

			for (int i = 0; i < getRowCount(); i++) {
				expressions[i] = ""+(String) getValueAt(i,1);
			}
			return expressions;
		}

	}

//	public static void main(String[] args) {
//		JFrame f = new JFrame("Test LabelClassProperties panel");
//
//		String xmlString =
//			"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
//			"<xml-tag xmlns=\"http://www.gvsig.gva.es\">\n" +
//			"    <property key=\"className\" value=\"com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelClass\"/>\n" +
//			"    <property key=\"isVisible\" value=\"false\"/>\n" +
//			"    <property key=\"name\" value=\"Default\"/>\n" +
//			"    <property key=\"labelExpression\" value=\"[TIPO] : lao39805502232 : Substring([MOTO], 2,2);\"/>\n" +
//			"    <property key=\"unit\" value=\"-1\"/>\n" +
//			"    <property key=\"referenceSystem\" value=\"0\"/>\n" +
//			"    <property key=\"sqlQuery\" value=\"TIPO = 'C'\"/>\n" +
//			"    <property key=\"priority\" value=\"0\"/>\n" +
//			"    <xml-tag>\n" +
//			"        <property key=\"className\" value=\"org.gvsig.symbology.fmap.styles.SimpleLabelStyle\"/>\n" +
//			"        <property key=\"desc\" value=\"Placa Carrer VLC.style\"/>\n" +
//			"        <property key=\"markerPointX\" value=\"0.0\"/>\n" +
//			"        <property key=\"markerPointY\" value=\"0.0\"/>\n" +
//			"        <property key=\"minXArray\" value=\"0.35 ,0.25\"/>\n" +
//			"        <property key=\"minYArray\" value=\"0.15 ,0.5\"/>\n" +
//			"        <property key=\"widthArray\" value=\"0.5 ,0.6\"/>\n" +
//			"        <property key=\"heightArray\" value=\"0.27 ,0.37\"/>\n" +
//			"        <property key=\"id\" value=\"labelStyle\"/>\n" +
//			"        <xml-tag>\n" +
//			"            <property key=\"className\" value=\"org.gvsig.symbology.fmap.styles.RemoteFileStyle\"/>\n" +
//			"            <property key=\"source\" value=\"http://www.boomlapop.com/boomlapop.jpg\"/>\n" +
//			"            <property key=\"desc\"/>\n" +
//			"            <property key=\"id\" value=\"LabelStyle\"/>\n" +
//			"        </xml-tag>\n" +
//			"    </xml-tag>\n" +
//			"    <xml-tag>\n" +
//			"        <property key=\"className\" value=\"com.iver.cit.gvsig.fmap.core.symbols.SimpleTextSymbol\"/>\n" +
//			"        <property key=\"desc\"/>\n" +
//			"        <property key=\"isShapeVisible\" value=\"true\"/>\n" +
//			"        <property key=\"font\" value=\"Arial\"/>\n" +
//			"        <property key=\"fontStyle\" value=\"1\"/>\n" +
//			"        <property key=\"size\" value=\"12\"/>\n" +
//			"        <property key=\"text\" value=\"GeneralLabeling.sample_text\"/>\n" +
//			"        <property key=\"textColor\" value=\"255,255,0,255\"/>\n" +
//			"        <property key=\"unit\" value=\"-1\"/>\n" +
//			"        <property key=\"referenceSystem\" value=\"0\"/>\n" +
//			"        <property key=\"id\" value=\"TextSymbol\"/>\n" +
//			"    </xml-tag>\n" +
//			"</xml-tag>\n" +
//			"";
//
//		LabelClass lc = null;
//		try {
//			XMLEntity xml = new XMLEntity((XmlTag) XmlTag.unmarshal(
//					XMLEncodingUtils.getReader(new ByteArrayInputStream(xmlString.getBytes()))));
//			lc = new LabelClass();
//			lc.setXMLEntity(xml);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		String[] names = new String[] { "Field1", "Field2", "Field3"	};
//		int[] types = new int[] { Types.VARCHAR, Types.INTEGER, Types.DOUBLE };
//		final LabelClassProperties lcp = new LabelClassProperties(
//				names, types ) {
//			private static final long serialVersionUID = -1843509415649666459L;
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				super.actionPerformed(e);
//				if ("OK".equals(e.getActionCommand())) {
//					System.out.println(getLabelClass().getXMLEntity());
//				}
//			}
//		};
//		lcp.setLabelClass(lc);
//		f.setContentPane(lcp);
//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		f.pack();
//		f.setVisible(true);
//
//	}

}

