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

package plugins.music.alias;

import com.kaear.common.*;
import com.kaear.interfaces.*;
import com.kaear.gui.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class displayAliasTableModel extends AbstractTableModel {
	
	    private boolean DEBUG = false;
		private static Vector data;
        private String[] columnNames;
		
		public displayAliasTableModel(dataList buildList)
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

            //data[row][col] = value;
			
			String[] myStrData = (String[])data.elementAt(row);
			
			for (int x=0; x < myStrData.length; x++)
			{
				if (x == col)
				{
					myStrData[x] = (String)value;
				}
			}
			
			data.setElementAt(myStrData,row);
			
			// Updates the actual view of the JTable
            fireTableCellUpdated(row, col);
			
			// Given a name, fetch the ID
			String newVal = formatUpdate((String)value,col);
			
			//String newVal = "-1";
			
			if (newVal.equals("-1")) {
				newVal = (String)value;
			}
			
			// Updates the database to match
			kerpowObjectManager.runDB.sqlRun("UPDATE alias SET " + getName(col) + " = " + newVal + " WHERE id = " + myStrData[0],"Table edit failed: ");

            if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }
		
		public void deleteRow(int row)
		{
			Vector args = new Vector();
			args.add(0,getValueAt(row,0));
			
			data.remove(row);
			fireTableRowsDeleted(row,row);
	
			new aliasCommands("deleteAlias",args);
		}


		private String formatUpdate(String value, int s)
		{
			/**
			 * Columns:
			 *
			 * 0 = ID
			 * 1 = Master (int)
			 * 2 = Alias (String)
			 */
			aliasCommands ac = new aliasCommands(null,null);
			
			if (s == 1) { return String.valueOf(ac.getMasterID(value)); }
			else 
			if (s == 2) { return "'" + value + "'";}
			else { return "-1"; }
		}
	
		private String getName(int s)
		{
			/**
			 * Columns:
			 *
			 * 0 = ID
			 * 1 = Master
			 * 2 = Alias
			 */

			if (s == 0) { return "id"; }
			else
			if (s == 1) { return "master"; }
			else
			if (s == 2) { return "alias"; }
			{ return ""; }
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

