package com.objectwave.viewUtility;

import java.util.*;
import java.awt.*;
//import jclass.field.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import com.objectwave.uiWidget.StateCheckBox;

/**
 * A collection of handy static methods which can be used with AWT/Swing
 * GUI components.
 * @version 1.4
 */
public class WidgetFunctions
{

	static final private String [] states = { "Y", "N", "U" };

	/**
	 *  A very common operation when building GUI panels is to do a
	 *  <label> <component> addition.  This method simplifies the operation.
	 */
	public static void addComponent(Font captionFont, String caption, Container container, Component component, boolean lastInRow)
	{
		JLabel label = null;
		if (caption != null)
		{
			label = initLabel(caption);
			label.setFont(captionFont);
		}
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(2, 2, 2, 2);
		if (caption != null)
			((GridBagLayout)container.getLayout()).setConstraints(label, gbc);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		if (lastInRow)
			gbc.gridwidth = GridBagConstraints.REMAINDER;
		else if (caption == null)
			gbc.gridwidth = 2;
		if (GridBagLayout.class.isInstance(container.getLayout()))
			((GridBagLayout)container.getLayout()).setConstraints(component, gbc);
		if (caption != null)
			container.add(label);
		container.add(component);
	}
	/**
	 * Display any component (probably most commonly a JPanel) in it's very
	 * own frame.  The return value indicates whether or not "ok" was clicked.
	 */
	public static boolean displayComponent(JComponent component, Rectangle bounds)
	{
		com.objectwave.uiWidget.SimpleOkCancelDialog okCancel;
		okCancel = new com.objectwave.uiWidget.SimpleOkCancelDialog(null, "Test Component", component);
		if (bounds != null)
			okCancel.setBounds(bounds);
		okCancel.setVisible(true);
		return !okCancel.isCancelled();
	}
	/**
	* A handy method to find the first ancestor of "component" which is
	* a Frame.
	*/
	public static Frame findParentFrame(Component component)
	{
		Frame f = null;
		if (Frame.class.isInstance(component))
			f = (Frame)component;
		else
		{
			Container c = component.getParent();
			for (;;)
			{
				if (c == null || Frame.class.isInstance(c))
				{
					f = (Frame)c;
					break;
				}
				c = c.getParent();
			}
		}
		return f;
	}
	/**
	 *  Use a simple JTextField to get/set date values.  This is useable
	 *  wherever a JCDateTextField is inconvenient.  If there is no text or
	 *  if the text cannot be parsed to a date, return null.
	 */
	public static Date getDateFrom(JTextField tf)
	{
	    String text = tf.getText();
	    return com.objectwave.utility.DateHelper.getDateFrom(tf.getText());
	}
	// Remove the following. Only valid if JClass is installed.
	/**
	 *  Create & return a new JCIntTextField, formatted properly.
	 *
	public static JCCurrencyTextField initCurrency()
	{
		JCCurrencyTextField tf = new JCCurrencyTextField();
		tf.setInsets(new java.awt.Insets(2,2,2,2));
		tf.setInvalidForeground(Color.red);
		tf.setDefaultValue(new Double(0));
		tf.setBackground(Color.white);
		tf.setEditable(true);
		return tf;
	}
	/**
	 *  Create & return a new JCDateTextField, formatted properly.
	public static JCDateTextField initDate()
	{
		JCDateTextField tf = new JCDateTextField();
		tf.setInvalidForeground(Color.red);
		tf.setEditFormats(jclass.util.JCUtilConverter.toStringList("EEEE MMMM d yyyy|MMMM d yyyy|dd-MMM-yy|dd-MMM-yyyy|M/d/yyyy|M/d/yyyy|dd-MMMM-yyyy"));
		tf.setValue(new java.util.GregorianCalendar(1998,3,23,4,23,9));
		tf.setInsets(new java.awt.Insets(2,2,2,2));
		tf.setBackground(Color.white);
		tf.setFormat("dd-MMM-yyyy");
		tf.setDefaultValue(new java.util.GregorianCalendar(1998,3,6,3,59,41));
		return tf;
	}
	/**
	 *  Create & return a new JCDoubletextField, formatted properly.
	public static JCDoubleTextField initDouble()
	{
		JCDoubleTextField tf = new JCDoubleTextField();
		tf.setInsets(new java.awt.Insets(2,2,2,2));
		tf.setInvalidForeground(Color.red);
		tf.setDefaultValue(new Double(0));
		tf.setBackground(Color.white);
		return tf;
	}
	/**
	 *  Create & return a new JCIntTextField, formatted properly.
	public static JCIntTextField initInt()
	{
		JCIntTextField tf = new JCIntTextField();
		tf.setInsets(new java.awt.Insets(2,2,2,2));
		tf.setInvalidForeground(Color.red);
		tf.setDefaultValue(new Integer(0));
		tf.setBackground(Color.white);
		return tf;
	}
	*/
	/**
	 *  Create & return a new JLabel, given caption.
	 */
	public static JLabel initLabel(String caption)
	{
		JLabel label = new JLabel(caption);
		label.setForeground(Color.black);
		label.setOpaque(false);
		return label;
	}
	public static StateCheckBox initStateCheckBoxYNU(String caption)
	{
		StateCheckBox cb = new com.objectwave.uiWidget.StateCheckBox(caption);
		cb.setForeground(Color.black);
		cb.setOpaque(false);
		cb.setStates(states);
		cb.setValue(states[2]);
		return cb;
	}
	/**
	 *  Create & return a new JCDoubletextField, formatted properly.
	 */
	public static JTextField initTextField()
	{
		JTextField tf = new JTextField();
		tf.setBackground(Color.white);
		return tf;
	}
	/**
	 * A utility method that will limit the amount of characters allowed in
	 * a text component.
	 */
	public static void insertUpdate(DocumentEvent e, final JTextComponent ivjTextArea1, int limit)
	{
	    String text = ivjTextArea1.getText();
	    if(text.length() > limit)
	    {
	        final String text2 = text.substring(0,limit);
	        final int position = e.getOffset() + e.getLength();
	        Runnable r = new Runnable(){
			                    public void run() {
									    ivjTextArea1.setText(text2);
									    java.awt.Toolkit.getDefaultToolkit().beep();
									    if(position < 250)
									        ivjTextArea1.setCaretPosition(position);
								}
							};
			SwingUtilities.invokeLater(r);
		    ivjTextArea1.revalidate();
		    return;
		}
	    ivjTextArea1.revalidate();
	}
	/**
	 *  Use a simple JTextField to get/set date values.  This is useable
	 *  wherever a JCDateTextField is inconvenient.
	 *
	 * @param tf The textfield to set the text of.
	 * @param date The date to set into the tf.  Null is legal if nulltext!=null.
	 * @param nullText The text to use if date==null.  Also used if date == 1/1/1801.
	 */
	public static void setDateFor(JTextField tf, Date date, String nullText)
	{
	    if (date == null)
	        tf.setText(nullText);
	    else
	    {
		    GregorianCalendar cal = new GregorianCalendar();
		    cal.setTime(date);
		    if (cal.get(Calendar.YEAR) == 1801 &&
		        cal.get(Calendar.MONTH) == Calendar.JANUARY &&
		        cal.get(Calendar.DAY_OF_MONTH) == 1)
		    {
		        tf.setText(nullText);
		        return;
		    }
		}

	    java.text.SimpleDateFormat formatter;
	    formatter = new java.text.SimpleDateFormat("dd-MMM-yyyy");
	    tf.setText(formatter.format(date));
	}
}
