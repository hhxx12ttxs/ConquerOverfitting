/*
 *
 *  Copyright (C) 201 Andreas Reichel <andreas@manticore-projects.com>
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or (at
 *  your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package com.manticore.swingui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class MTableCellRenderer implements TableCellRenderer {
	public final static DateFormat dateFormat=DateFormat.getDateInstance(DateFormat.SHORT);
    public final static DateFormat dateTimeFormat=DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	public final static DecimalFormat decimalFormat=(DecimalFormat) DecimalFormat.getInstance();
	public final static DecimalFormat integerFormat=(DecimalFormat) DecimalFormat.getIntegerInstance();

	static {
		decimalFormat.setGroupingUsed(true);
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setMaximumFractionDigits(5);

		integerFormat.setGroupingUsed(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
												   boolean hasFocus, int row, int column) {
		JLabel label = new JLabel();
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
		label.setForeground(SwingUI.MANTICORE_DARK_BLUE);
		label.setBorder(new EmptyBorder(2,6,2,2));

		if (value instanceof java.sql.Timestamp) {
			label.setHorizontalAlignment(JLabel.TRAILING);
			label.setText(dateTimeFormat.format((java.sql.Timestamp) value));
		} else if (value instanceof Date) {
            label.setHorizontalAlignment(JLabel.TRAILING);
            GregorianCalendar cal=(GregorianCalendar) GregorianCalendar.getInstance();
            cal.setTime((Date) value);
            if ( cal.get(GregorianCalendar.HOUR_OF_DAY)!=0
                 || cal.get(GregorianCalendar.MINUTE)!=0
                 || cal.get(GregorianCalendar.SECOND)!=0
                 || cal.get(GregorianCalendar.MILLISECOND)!=0
                )
                label.setText(dateTimeFormat.format((Date) value));
            else
                label.setText(dateFormat.format((Date) value));
		}  else if (value instanceof Long) {
			label.setHorizontalAlignment(JLabel.TRAILING);
			label.setText(integerFormat.format((Long) value));

			if (((Long) value) < 0)
				label.setForeground(SwingUI.MANTICORE_ORANGE);

		} else if (value instanceof Integer) {
			label.setHorizontalAlignment(JLabel.TRAILING);
			label.setText(integerFormat.format((Integer) value));

			if (((Integer) value) < 0)
				label.setForeground(SwingUI.MANTICORE_ORANGE);

		} else if (value instanceof Short) {
			label.setHorizontalAlignment(JLabel.TRAILING);
			label.setText(integerFormat.format((Short) value));

			if (((Short) value) < 0)
				label.setForeground(SwingUI.MANTICORE_ORANGE);

		} else if (value instanceof Double) {
			label.setHorizontalAlignment(JLabel.TRAILING);
			label.setText(decimalFormat.format((Double) value));

			if (((Double) value) < 0)
				label.setForeground(SwingUI.MANTICORE_ORANGE);

		} else if (value instanceof Float) {
			label.setHorizontalAlignment(JLabel.TRAILING);
			label.setText(decimalFormat.format((Float) value));

			if (((Float) value) < 0)
				label.setForeground(SwingUI.MANTICORE_ORANGE);

		} else if (value instanceof BigDecimal) {
			label.setHorizontalAlignment(JLabel.TRAILING);
			label.setText(decimalFormat.format((BigDecimal) value));
            
			if (((BigDecimal) value).doubleValue() < 0)
				label.setForeground(SwingUI.MANTICORE_ORANGE);

		} else if (value instanceof Number) {
			label.setHorizontalAlignment(JLabel.TRAILING);
			label.setText(decimalFormat.format((BigDecimal) value));

			if (((Number) value).doubleValue() < 0d)
				label.setForeground(SwingUI.MANTICORE_ORANGE);

		} else
			label.setText(value != null
						  ? value.toString()
						  : "");


		//label.setFont(table.getFont());
		label.setOpaque(true);
		label.setToolTipText(label.getText());
		if (isSelected) {
			label.setBackground(table.getSelectionBackground());
			label.setForeground(table.getSelectionForeground());
		} else if (row % 2 == 0)
			label.setBackground(SwingUI.MANTICORE_LIGHT_GREY);
		else
			label.setBackground(Color.WHITE);

		return label;
	}

}

