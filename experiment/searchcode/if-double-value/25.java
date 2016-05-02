<<<<<<< HEAD
/* CPSTable.java - created: Jan 30, 2008
 * Copyright (C) 2008 Clayton Carter
 * 
 * This file is part of the project "Crop Planning Software".  For more
 * information:
 *    website: http://cropplanning.googlecode.com
 *    email:   cropplanning@gmail.com 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package CPS.UI.Swing;

import CPS.Data.CPSDateValidator;
import CPS.Data.CPSRecord;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;

public class CPSTable extends JTable {
   
    private CPSDateValidator dateValidator;
    private static final Color ROW_SHADE_GRAY_5 = new Color( 242, 242, 242 );
    private static final Color ROW_SHADE_GRAY_10 = new Color( 229, 229, 229 );
    private static final Color ROW_SHADE_GRAY = ROW_SHADE_GRAY_10;
    
    public CPSTable() {
        super();
        init();
    }

    public CPSTable( TableModel tm ) {
        super( tm );
        init();
    }

    private void init() {
        /* setup selection parameters */
       // enable row selection, disable column selection (default)
       this.setColumnSelectionAllowed( false );
       this.setRowSelectionAllowed( true );
       // allow multiple rows to be selected
       this.getTableHeader().setReorderingAllowed(false);

        dateValidator = new CPSDateValidator();
        dateValidator.addFormat( CPSDateValidator.DATE_FORMAT_SQL );
    
        setAutoResizeMode(AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        
        /* reset column widths for certain column types */
        for ( int colIndex = 0; colIndex < getColumnCount(); colIndex++ ) {
           Class c = getColumnClass(colIndex);

           // Boolean
           if ( c.getName().equals( Boolean.class.getName() ) ) {
              getColumnModel().getColumn( colIndex ).setMaxWidth( 20 );
              getColumnModel().getColumn( colIndex ).setPreferredWidth( 20 );
           }
           else if ( c.getName().equals( Date.class.getName() ))
              getColumnModel().getColumn( colIndex ).setPreferredWidth( 50 );
           else if ( c.getName().equals( Integer.class.getName() ))
              getColumnModel().getColumn( colIndex ).setPreferredWidth( 40 );
           else if ( c.getName().equals( Double.class.getName() ) )
              getColumnModel().getColumn( colIndex ).setPreferredWidth( 50 );
           else if ( c.getName().equals( Float.class.getName() ) )
              getColumnModel().getColumn( colIndex ).setPreferredWidth( 50 );
        }
        
         // install custom table renderes and editors
       for ( int i = 0 ; i < getColumnModel().getColumnCount() ; i++ ) {

          // install date renderers and editors on all Date columns
          if ( getColumnClass( i ).equals( new Date().getClass() ) ) {
             getColumnModel().getColumn( i ).setCellRenderer( new DateCellRenderer() );
             getColumnModel().getColumn( i ).setCellEditor( new DateCellEditor() );
          }
          // floating point columns
          else if ( getColumnClass( i ).equals( Float.class )  ||
                    getColumnClass( i ).equals( Double.class ) ||
                    getColumnClass( i ).equals( Integer.class ) ) {
             getColumnModel().getColumn( i ).setCellRenderer( new NumberCellRenderer() );
          }
       }
        
    }

    @Override
    public void setRowSelectionInterval( int arg0, int arg1 ) {
        super.setRowSelectionInterval( arg0, arg1 );
        ensureRowIsVisible(arg0);
    }
    
    // Assumes table is contained in a JScrollPane. Scrolls the
    // cell (rowIndex, vColIndex) so that it is visible within the viewport.
    public void ensureRowIsVisible( int rowIndex ) {
        if ( !( this.getParent() instanceof JViewport ) ) {
            return;
        }
        JViewport viewport = (JViewport) this.getParent();
    
        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = this.getCellRect( rowIndex, 1, true );
    
        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();
    
        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation( rect.x - pt.x, rect.y - pt.y );
    
        // Scroll the area into view
        viewport.scrollRectToVisible( rect );
    }
    
    
    public void setColumnNamesAndToolTips( List<String[]> prettyNames ) {
        ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
        int COLNAME = 0;
        int PRETTYNAME = 1;
        int EDITABLE = 2;
        
        // Assign a tooltip for each of the columns
        for ( int c = 0; c < getColumnCount(); c++ ) {
            String colName = getColumnModel().getColumn( c ).getHeaderValue().toString().toLowerCase();
            String s = null;
            Boolean editable = Boolean.TRUE; // default to true
            
            for ( int l = 0; l < prettyNames.size(); l++ )
                if ( colName.equals( prettyNames.get(l)[COLNAME]) ) {
                    s = prettyNames.get(l)[PRETTYNAME];
                    editable = new Boolean( prettyNames.get(l)[EDITABLE] );
                    break;
                }
            
            // Change the name to the "pretty name"
            if ( s != null )
                getColumnModel().getColumn( c ).setHeaderValue( s );
            // set the tool tip to the "pretty name"
            tips.setToolTip( getColumnModel().getColumn( c ), s );
            
            // HACK!
            // if the column is labeled as uneditable, then install an "uneditable" cell editor
            if ( ! editable.booleanValue() ) {
               getColumnModel().getColumn( c ).setCellEditor( new UneditableCellEditor() );
            }
            
        }
        getTableHeader().addMouseMotionListener( tips );
    }

    @Override
    public Component prepareRenderer( TableCellRenderer renderer, int rowIndex, int vColIndex ) {
     
        Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
        shadeComponentInRow( c, rowIndex );
        return c;
    }
    
    private boolean isRowShaded( int rowIndex ) {
        return ( (int) rowIndex / 2 ) % 2 == 0;
    }
    
    private Component shadeComponentInRow( Component c, int rowIndex ) {
      
        if ( isRowSelected( rowIndex )) 
            c.setBackground( getSelectionBackground() );
        else if ( isRowShaded( rowIndex ) )
            c.setBackground( ROW_SHADE_GRAY );
        else
            // If not shaded, match the table's background
            c.setBackground( getBackground() );

        return c;
    }
    
    
    private class InsetRenderer extends JLabel implements TableCellRenderer {
        
        public InsetRenderer() {
            super();
            setOpaque(true);
            setBorder( BorderFactory.createEmptyBorder( 1, 5, 1, 5 ));
        }
        
        public Component getTableCellRendererComponent( JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus, 
                                                        int rowIndex, int vColIndex ) {
           setText( (String) value );
           return this;
        }
    }
    
    private class NumberCellRenderer extends InsetRenderer implements TableCellRenderer {
        
        public NumberCellRenderer() {
            super();
            setHorizontalAlignment( JLabel.RIGHT );
        }
        
        // This method is called each time a cell in a column
        // using this renderer needs to be rendered.
        public Component getTableCellRendererComponent( JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus, 
                                                        int rowIndex, int vColIndex ) {
            // 'value' is value contained in the cell located at (rowIndex, vColIndex)
    
            // Configure the component with the specified value
            // in case, we display a formated string
          String s;

           if ( value instanceof Float ) {
            if ( ((Float) value).floatValue() == 0f )
               s = "";
             else
              s = CPSRecord.formatFloat( ((Float) value).floatValue(), 3 );
           }
           else if ( value instanceof Integer ) {
             if ( ((Integer) value).intValue() == 0 )
               s = "";
             else
               s = CPSRecord.formatInt( (Integer) value );
           }
           else if ( value instanceof Double ) {
             if ( ((Double) value).floatValue() == 0f )
               s = "";
             else
               s = CPSRecord.formatFloat( ((Double) value).floatValue(), 3 );
           }
           else if ( value == null )
              s = "";
           else
              s = value.toString();

           setText(s);

            return this;
        }
    }
    
    private class DateCellRenderer extends InsetRenderer implements TableCellRenderer {
        
        public DateCellRenderer() {
            super();
            setOpaque(true);
        }
        
        // This method is called each time a cell in a column
        // using this renderer needs to be rendered.
        public Component getTableCellRendererComponent( JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus, 
                                                        int rowIndex, int vColIndex ) {
            // 'value' is value contained in the cell located at (rowIndex, vColIndex)
    
            // Configure the component with the specified value
            // in case, we display a formated string
            setText( CPSDateValidator.format( (Date) value, CPSDateValidator.DATE_FORMAT_SHORT ));
            setToolTipText( CPSDateValidator.format( (Date) value, CPSDateValidator.DATE_FORMAT_FULLYEAR_DAY_OF_WEEK ) );

            return this;
        }
        
        // The following methods override the defaults for performance reasons
        public void validate() {}
        public void revalidate() {}
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    }
    
    public static class CPSComboBoxCellEditor extends ComboBoxCellEditor implements TableCellEditor {
        
        public CPSComboBoxCellEditor(final JComboBox comboBox) {
           super( comboBox );
        }
    
        public boolean isCellEditable(EventObject evt) {
            if (evt instanceof MouseEvent) {
                // For double-click activation
                return ((MouseEvent)evt).getClickCount() >= 2;
            }
            return true;
        }
    }
       
    private class DateCellEditor extends AbstractCellEditor implements TableCellEditor {
        // This is the component that will handle the editing of the cell value
        JComponent component = new JTextField();

        // This method is called when a cell value is edited by the user.
        // 'value' is value contained in the cell located at (rowIndex, vColIndex)
        public Component getTableCellEditorComponent( JTable table, Object value,
                                                      boolean isSelected, int rowIndex, int vColIndex ) {
            // Configure the component with the specified value
            // In ths case, we accept a Date and fill the text field with a formated String.
            ( (JTextField) component ).setText( dateValidator.format( (Date) value ));

            return component;
        }

        // This method is called when editing is completed.
        // It must return the new value to be stored in the cell.
        // In this case, we return a Date
        public Object getCellEditorValue() {
            if ( ((JTextField) component ).getText().equals( "" ))
                return null;
            
            String dateText = ( (JTextField) component ).getText();
            
            return dateValidator.parse( dateText.trim() );
        }
        
        public boolean isCellEditable(EventObject evt) {
            if (evt instanceof MouseEvent) {
                // For double-click activation
                return ((MouseEvent)evt).getClickCount() >= 2;
            }
            return true;
        }
    }
       
    private class UneditableCellEditor extends AbstractCellEditor implements TableCellEditor {
        // This is the component that will handle the editing of the cell value
        JComponent component = new InsetRenderer();
        
        // This method is called when a cell value is edited by the user.
        // 'value' is value contained in the cell located at (rowIndex, vColIndex)
        public Component getTableCellEditorComponent( JTable table, Object value,
                                                      boolean isSelected, int rowIndex, int vColIndex ) {
            // Configure the component with the specified value
            // In ths case, we accept a Date and fill the text field with a formated String.
            ( (JLabel) component ).setText( CPSDateValidator.format( (Date) value ));

            return component;
        }

        // This method is called when editing is completed.
        // It must return the new value to be stored in the cell.
        // In this case, we return a Date
        public Object getCellEditorValue() {
            if ( ((JLabel) component ).getText().equals( "" ))
                return null;
            
            String dateText = ( (JLabel) component ).getText();
            
            return dateValidator.parse( dateText.trim() );
        }
        
       @Override
       public boolean isCellEditable( EventObject evt ) {
          return false;
       }
    }
       
    private class ColumnHeaderToolTips extends MouseMotionAdapter {
        // Current column whose tooltip is being displayed.
        // This variable is used to minimize the calls to setToolTipText().
        TableColumn curCol;
    
        // Maps TableColumn objects to tooltips
        Map tips = new HashMap();
    
        // If tooltip is null, removes any tooltip text.
        public void setToolTip(TableColumn col, String tooltip) {
            if (tooltip == null) {
                tips.remove(col);
            } else {
                tips.put(col, tooltip);
            }
        }
    
        public void mouseMoved(MouseEvent evt) {
            TableColumn col = null;
            JTableHeader header = (JTableHeader)evt.getSource();
            JTable table = header.getTable();
            TableColumnModel colModel = table.getColumnModel();
            int vColIndex = colModel.getColumnIndexAtX(evt.getX());
    
            // Return if not clicked on any column header
            if (vColIndex >= 0) {
                col = colModel.getColumn(vColIndex);
            }
    
            if (col != curCol) {
                header.setToolTipText((String)tips.get(col));
                curCol = col;
            }
        }
   }

=======
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package memoria.utils.coordinate;

import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author diego
 */
public class CoordinateConversion {

    public CoordinateConversion()
  {

  }

  public static double[] utm2LatLon(String UTM)
  {
    UTM2LatLon c = new UTM2LatLon();
    return c.convertUTMToLatLong(UTM);
  }

  public static String latLon2UTM(double latitude, double longitude)
  {
    LatLon2UTM c = new LatLon2UTM();
    return c.convertLatLonToUTM(latitude, longitude);

  }

  public static void validate(double latitude, double longitude)
  {
    if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0
        || longitude >= 180.0)
    {
      throw new IllegalArgumentException(
          "Legal ranges: latitude [-90,90], longitude [-180,180).");
    }

  }

  public static String latLon2MGRUTM(double latitude, double longitude)
  {
    LatLon2MGRUTM c = new LatLon2MGRUTM();
    return c.convertLatLonToMGRUTM(latitude, longitude);

  }

  public static double[] mgrutm2LatLon(String MGRUTM)
  {
    MGRUTM2LatLon c = new MGRUTM2LatLon();
    return c.convertMGRUTMToLatLong(MGRUTM);
  }

  public static double degreeToRadian(double degree)
  {
    return degree * Math.PI / 180;
  }

  public static double radianToDegree(double radian)
  {
    return radian * 180 / Math.PI;
  }

  public static double POW(double a, double b)
  {
    return Math.pow(a, b);
  }

  public static double SIN(double value)
  {
    return Math.sin(value);
  }

  public static double COS(double value)
  {
    return Math.cos(value);
  }

  public static double TAN(double value)
  {
    return Math.tan(value);
  }

  
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

