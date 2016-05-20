package com.objectwave.uiWidget;

import com.objectwave.viewUtility.WidgetFunctions;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;

/**
 *  A replacement for the jclass.fields.JCDateTextField.  This little guy
 *  requires less setup, but is less configurable than the former.  If all
 *  that is required is a date-parsing text field, this is the guy to use.
 *  Also, this JTextField allows null values to be legal: the user can
 *  supply a string to be used in such cases, the default being "".
 *  Additionally, the user can define a string to be used whenever an invalid
 *  date has been entered.
 */
public class SimpleDateTextField extends JTextField
{
	private String nullText = "";
	private String invalidText = "invalid";

	private static SimpleDateTextField main_tf; // for testing.
	private static JTextField main_tf2; // for testing.
	public SimpleDateTextField() { super(); init(); }
	public SimpleDateTextField(String invalidText, String nullText)
	{
		super();
		this.nullText = nullText;
		this.invalidText = invalidText;
		init();
	}
	public void bletch(Date date)
	{
		setValue(date);
	}
	public String getInvalidtext() { return invalidText; }
	public String getNullText() { return nullText; }
	public Date getValue()
	{
		return WidgetFunctions.getDateFrom(this);
	}
	protected void init()
	{
		java.awt.Dimension dim = getSize();
		if (dim.width == 0)
		{
			dim.width = 50;
			setSize(dim);
		}

		this.addFocusListener(
		    new java.awt.event.FocusAdapter()
		    {
		        public void focusLost(java.awt.event.FocusEvent e)
		        {
		            setValue(WidgetFunctions.getDateFrom(SimpleDateTextField.this));
		        }
		    } );
		this.addKeyListener(
		    new java.awt.event.KeyAdapter()
		    {
		        public void keyTyped(java.awt.event.KeyEvent e)
		        {
		            if (e.getKeyChar() == '\r')
			        {
			            setValue(WidgetFunctions.getDateFrom(SimpleDateTextField.this));
			        }
		        }
		    } );
	}
	public static void main(String args[])
	{
		main_tf = new SimpleDateTextField("null", "invalid");
		JPanel panel = new JPanel();
		panel.setLayout(new java.awt.FlowLayout());
		panel.add(main_tf);
		java.awt.Dimension dim = main_tf.getSize();
		if (dim.width == 0)
		{
			dim.width = 50;
			main_tf.setSize(dim);
		}

		JButton button;
		button = new JButton(">> get value >>");
		panel.add(button);
		main_tf2 = new JTextField();
		button.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent e)
				{
					main_tf2.setText("" + main_tf.getValue());
				}
			} );
		panel.add(main_tf2);
		com.objectwave.uiWidget.SimpleOkCancelDialog dialog;
		dialog = new com.objectwave.uiWidget.SimpleOkCancelDialog(null, "Test date field", panel);
		dialog.setBounds(100, 100, 300, 100);
		dialog.setVisible(true);
		System.exit(0);
	}
	public void setInvalidText(String s) { invalidText = s ; }
	public void setNullText(String s) { nullText = s; }
	public void setValue(Date date)
	{
		if (date == null)
		{
			this.setText(nullText);
			return;
		}
		WidgetFunctions.setDateFor(this, date, invalidText);
	}
}
