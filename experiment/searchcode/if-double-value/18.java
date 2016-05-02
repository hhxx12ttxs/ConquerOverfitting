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

import com.creamtec.ajaxswing.AjaxSwingManager;
import com.creamtec.ajaxswing.core.AgentInitData;
import com.creamtec.ajaxswing.core.ClientAgent;
import static com.manticore.swingui.MTableCellRenderer.dateFormat;
import static com.manticore.swingui.MTableCellRenderer.dateTimeFormat;
import static com.manticore.swingui.MTableCellRenderer.decimalFormat;
import static com.manticore.swingui.MTableCellRenderer.integerFormat;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class MTable extends JTable {
	public final static Dimension viewPortSize = new Dimension(480, 120);
	public final TableColumnAdjuster tca;

	JMenuItem menuItem1=new JMenuItem("Copy All");
	JMenuItem menuItem2=new JMenuItem("Copy Row");
	JMenuItem menuItem3=new JMenuItem("Copy Column");

	public ActionListener actionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent ae) {
			Object source=ae.getSource();
			if (source.equals(menuItem1))
				copyToClipBoard(-1, -1);
			else if (source.equals(menuItem2))
				copyToClipBoard(getSelectedRow(), -1);
			else if (source.equals(menuItem3))
				copyToClipBoard(-1, getSelectedColumn());
			popupMenu.setVisible(false);
		}
	};
	public JPopupMenu popupMenu = new JPopupMenu("Table Actions");
	{
		popupMenu.add(menuItem1);
		popupMenu.add(menuItem2);
		popupMenu.add(menuItem3);
		menuItem1.addActionListener(actionListener);
		menuItem2.addActionListener(actionListener);
		menuItem3.addActionListener(actionListener);
	}
    
//    public class MyMouseAdapter extends MouseAdapter {
//		@Override
//		public void mouseClicked(MouseEvent e) {
//			if (e.getButton()==MouseEvent.BUTTON3 && !e.isPopupTrigger()) popupMenu.show(MTable.this, e.getX(), e.getY());
//		}
//	};
//    public MouseAdapter mouseAdapter=new MyMouseAdapter();

	public MTable() {
		super();
		setDefaultRenderer(Object.class, new MTableCellRenderer());
		setDefaultRenderer(Integer.class, new MTableCellRenderer());
		setDefaultRenderer(Long.class, new MTableCellRenderer());
		setDefaultRenderer(Float.class, new MTableCellRenderer());
		setDefaultRenderer(Double.class, new MTableCellRenderer());
		setDefaultRenderer(BigDecimal.class, new MTableCellRenderer());
		setDefaultRenderer(Number.class, new MTableCellRenderer());
		setDefaultRenderer(Short.class, new MTableCellRenderer());
		setDefaultRenderer(String.class, new MTableCellRenderer());

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setColumnSelectionAllowed(false);
		setPreferredScrollableViewportSize(viewPortSize);
		setAutoCreateRowSorter(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		tca = new TableColumnAdjuster(this);
		tca.adjustColumns();

		//addMouseListener(mouseAdapter);
        //putClientProperty(AjaxSwingProperties.C COMPONENT_MOUSE_RIGHT_EVENT_LISTENER, AjaxSwingConstants.MOUSE_ON_CONTEXT_MENU);
        setComponentPopupMenu(popupMenu);
		setInheritsPopupMenu(true);
	}

	public void adjustColumns() {
		tca.adjustColumns();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public void setData(String[] columnNames, ArrayList<Object[]> data) {
		DefaultTableModel model = new DefaultTableModel(data.toArray(new Object[0][]), columnNames);
		setModel(model);
		adjustColumns();
	}

	public void setData(String[] columnNames, Object[][] data) {
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		setModel(model);
		adjustColumns();
	}

	public void setData(Object[][] data) {
		Object[] columnNames = data[0];
		Object[][] d = (Object[][]) data[1];

		DefaultTableModel model = new DefaultTableModel(d, columnNames);
		setModel(model);
		adjustColumns();
	}

	public void copyToClipBoard(int row, int col) {
		StringBuilder stringBuilder = new StringBuilder("<html>");
		stringBuilder.append("<table>").append("<thead><tr>");
		for (int c = 0; c < getColumnCount(); c++) if (col<0 || col==c) {
			stringBuilder.append("<th alignt='center' bgcolor=").append(SwingUI.getHexColor(SwingUI.MANTICORE_DARK_BLUE)).append("'><font color='WHITE'>").append(getColumnName(c)).append("</font></th>");
		}
		stringBuilder.append("</tr></thead>");
        stringBuilder.append("<tbody>");
		for (int r = 0; r < getRowCount(); r++) if (row<0 || row==r) {
			stringBuilder.append("<tr>");
			for (int c = 0; c < getColumnCount(); c++) if (col<0 || col==c) {
                Object value=getValueAt(r, c);
                String s="";
                String align="left";
                Color fgColor=SwingUI.MANTICORE_DARK_BLUE;
                
                if (value==null) {
                    s="";
                } else if (value instanceof java.sql.Timestamp) {
                    align="right";
                    s=dateTimeFormat.format((java.sql.Timestamp) value);
                } else if (value instanceof Date) {
                    align="right";
                    GregorianCalendar cal=(GregorianCalendar) GregorianCalendar.getInstance();
                    cal.setTime((Date) value);
                    if ( cal.get(GregorianCalendar.HOUR_OF_DAY)!=0
                        || cal.get(GregorianCalendar.MINUTE)!=0
                        || cal.get(GregorianCalendar.SECOND)!=0
                        || cal.get(GregorianCalendar.MILLISECOND)!=0
                     )
                        s=dateTimeFormat.format((Date) value);
                    else
                        s=dateFormat.format((Date) value);
                }  else if (value instanceof Long) {
                    align="right";
                    s=integerFormat.format((Long) value);

                    if (((Long) value) < 0)
                        fgColor=SwingUI.MANTICORE_ORANGE;

                } else if (value instanceof Integer) {
                    align="right";
                    s=integerFormat.format((Integer) value);

                    if (((Integer) value) < 0)
                        fgColor=SwingUI.MANTICORE_ORANGE;

                } else if (value instanceof Short) {
                    align="right";
                    s=integerFormat.format((Short) value);

                    if (((Short) value) < 0)
                        fgColor=SwingUI.MANTICORE_ORANGE;

                } else if (value instanceof Double) {
                    align="right";
                    s=decimalFormat.format((Double) value);

                    if (((Double) value) < 0)
                        fgColor=SwingUI.MANTICORE_ORANGE;

                } else if (value instanceof Float) {
                    align="right";
                    s=decimalFormat.format((Float) value);

                    if (((Float) value) < 0)
                       fgColor=SwingUI.MANTICORE_ORANGE;

                } else if (value instanceof BigDecimal) {
                    align="right";
                    s=decimalFormat.format((BigDecimal) value);

                    if (((BigDecimal) value).doubleValue() < 0)
                        fgColor=SwingUI.MANTICORE_ORANGE;

                } else if (value instanceof Number) {
                    align="right";
                    s=decimalFormat.format((Number) value);

                    if (((Number) value).doubleValue() < 0d)
                        fgColor=SwingUI.MANTICORE_ORANGE;

                } else
                    s= value.toString();
                
				stringBuilder.append("<td align='").append(align).append("' bgcolor='").append(r%2==0 ? "WHITE" : SwingUI.getHexColor(SwingUI.MANTICORE_LIGHT_GREY)).append("'><font color='").append(SwingUI.getHexColor(fgColor)).append("'>").append(s).append("</font></td>");
			}
			stringBuilder.append("</tr>");
		}
		stringBuilder.append("</tbody></table>");
		stringBuilder.append("</html>");
        
        if (!AjaxSwingManager.isAjaxSwingRunning()) {
            Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = new BasicTransferable("", stringBuilder.toString());
            systemClipboard.setContents(transferable, null);
        } else {
            FileWriter writer=null;
            try {
                ClientAgent.getCurrentInstance().clearAllowedDownloads();
                AgentInitData initData = ClientAgent.getCurrentInstance().getInitData();
                // http://localhost:8040/app/fsoper/fxfprod/AjaxSwing4.1.1/tomcat/webapps/ajaxswing/temp/test.txt
                // http://localhost:8040/ajaxswing/temp/test.txt
                File p = new File(initData.tempDirPath + "/" + initData.clientId);
                if (!p.exists()) {
                    p.mkdirs();
                }
                
                File f = new File(p, "table_"+ System.nanoTime() + ".htm");
                writer = new FileWriter(f);
                writer.write(stringBuilder.toString());
                writer.flush();
                writer.close();
                AjaxSwingManager.openInNewTab("/ajaxswing/temp/" + initData.clientId + "/" + f.getName());
            } catch (IOException ex) {
                Logger.getLogger(MTable.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
	}
}

