/*
 * Created by:  Pieter Sartain
 *
 * Licensed under the GPL:
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 *
 */

package plugins.video.series;

import plugins.video.*;
import com.kaear.common.*;
import com.kaear.interfaces.*;
import com.kaear.gui.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.sql.ResultSet;

public class displaySeriesTableModel extends AbstractTableModel {
	
	    private boolean DEBUG = false;
		private static Vector data;
        private String[] columnNames;
		
		public displaySeriesTableModel(dataList buildList)
		{
			//Pull in the information for whatever module is required.
			columnNames = buildList.getColumnHeaders();
			data = buildList.makeList();
		}
		
		public void setData(dataList buildList)
		{
			data = buildList.makeList();
			fireTableDataChanged();
		}
		
	    public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

		
		public Object getValueAt(int row, int col) {
			return ((String[])data.elementAt(row))[col];
        }
		

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
			if (col == 0) {
                return false;
            } else {
                return true;
            }
        }
		
		/*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of "
                                   + value.getClass() + ")");
            }

 			String[] myStrData = (String[])data.elementAt(row);
			myStrData[col] = (String)value;
			
			data.setElementAt(myStrData,row);

			// Updates the actual view of the JTable
            fireTableCellUpdated(row, col);
			
			// Given a name, fetch the ID
			String newVal = getAnID((String)value,col);
			
			String[] columnNames = new videoSeriesList("").getColumnHeaders();
			String colName = columnNames[col];
			
			if (col == 0 || col > 3) {
				// This is part of the video table, not series
				if (newVal.equals("-1"))
				{
					try { int x = Integer.parseInt((String)value,10);
							newVal = String.valueOf(x); }
					catch (Throwable e) { newVal = "'" + (String)value + "'"; }
				}
				kerpowObjectManager.runDB.sqlRun("UPDATE video SET " + colName + " = " + newVal + " WHERE id = " + myStrData[0],"Table edit failed: ");
			} else {
				if (newVal.equals("-1"))
				{
					try { int x = Integer.parseInt((String)value,10);
							newVal = String.valueOf(x); }
					catch (Throwable e) { newVal = "'" + (String)value + "'"; }
				}
				videoCommands mc = new videoCommands(null,null);
				//System.out.println(myStrData[1] + " : " + Integer.parseInt(myStrData[2]) + " : " + Integer.parseInt(myStrData[3]));
				
				int result = -1;
				try {
					ResultSet rs = kerpowObjectManager.runDB.sqlExe("SELECT type_id FROM video WHERE type = " + mc.getID("series","type","name") + " AND id = " + myStrData[0],"type_id reclamation failed: ");
					rs.next();
					result = Integer.parseInt(rs.getString(1));
				} 
				catch (Throwable e) { new exhandle("type_ID failed: ", e); }
				
				kerpowObjectManager.runDB.sqlRun("UPDATE series SET " + colName + " = " + newVal + " WHERE id = " + result,"Table edit failed: ");
				int id = mc.getSeriesID(myStrData[1],Integer.parseInt(myStrData[2]),Integer.parseInt(myStrData[3]));
				kerpowObjectManager.runDB.sqlRun("UPDATE video SET type_id = " + id + " WHERE id = " + myStrData[0],"Table edit failed: ");
			}


            if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }
		
		public void deleteRow(int row)
		{
			Vector args = new Vector();
			args.add(0,getValueAt(row,0));
			args.add(1,"series");
			
			data.remove(row);
			fireTableRowsDeleted(row,row);
	
			new videoCommands("deleteRecord",args);
		}
		
		private String getAnID(String value, int s)
		{
			/**
			 * Columns:
			 *
			 * 0 = ID
			 * 1 = Name
			 * 2 = Season
			 * 3 = Episodes
			 * 4 = Disks
			 * 5 = Format
			 * 6 = Quality
			 * 7 = Location
			 * 8 = Classification
			 */
			videoCommands mc = new videoCommands(null,null);
			
			if (s == 5) { return String.valueOf(mc.getID(value,"videoformat","name")); }
			else
			if (s == 6) { return String.valueOf(mc.getID(value,"quality","name")); }
			else
			if (s == 8) { return String.valueOf(mc.getID(value,"classification","name")); }
			else
			{ return "-1"; }
		}

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + data.elementAt(i));
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
	
}

