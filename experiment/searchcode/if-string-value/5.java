/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tnviet.proj.jna.bandwidth;

import com.l2fprod.common.swing.plaf.windows.WindowsOutlookBarUI.ThinScrollBarUI;
import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Renderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jvnet.substance.api.renderers.SubstanceDefaultTableCellRenderer;
import tnviet.proj.jna.Program;
import tnviet.proj.jna.utilities.ResourceManager;

/**
 *
 * @author Administrator
 */
public class FilterTableFrame extends JFrame implements ActionListener{
    JTable table;
    Vector filters = new Vector();
    FilterTableModel model = new FilterTableModel(filters);
    ResourceManager resourceManager = new ResourceManager(this.getClass(), Program.locale);
    JPopupMenu popoup = new JPopupMenu();
    JMenuItem insertMenu, removeMenu, activateOrDeactivateMenu;

    private FilterTableFrame getInstance(){
        return this;
    }


    public FilterTableFrame() {
        //DefaultTableRenderer renderer = new DefaultTableRenderer();
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableViewPort = new JScrollPane(table);
        insertMenu = new JMenuItem(resourceManager.getString("insertFilter"));
        insertMenu.setActionCommand("Insert");
        insertMenu.addActionListener(this);
        removeMenu = new JMenuItem(resourceManager.getString("removeFilter"));
        removeMenu.setActionCommand("Remove");
        removeMenu.addActionListener(this);
        activateOrDeactivateMenu = new JMenuItem(resourceManager.getString("activate/deactivate"));
        activateOrDeactivateMenu.setActionCommand("ActivateOrDeactivate");
        activateOrDeactivateMenu.addActionListener(this);
        popoup.add(insertMenu);
        popoup.add(removeMenu);
        popoup.add(activateOrDeactivateMenu);
        PopupListener popupListener = new PopupListener();
        table.addMouseListener(popupListener);
        table.removeColumn(table.getColumnModel().getColumn(10));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JXTitledPanel titledPane = new JXTitledPanel(resourceManager.getString("filters.title"), tableViewPort);
        titledPane.setPreferredSize(new Dimension(600, 400));
        //titledPane.setPreferredSize(table.getPreferredSize());
        this.getContentPane().add(titledPane, BorderLayout.CENTER);
        table.setFillsViewportHeight(true);
        this.setTableRenderer();
        table.setColumnSelectionAllowed(false);
        this.autoResizeColWidthByHeaderWidth(table);
        TableRowSorter sorter = new TableRowSorter<FilterTableModel>(model);
        table.setRowSorter(sorter);
        this.setTitle(resourceManager.getString("filters.title"));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
    }
    protected void autoResizeColWidthByHeaderWidth(JTable table){
        int margin = 5;
        for(int i=0; i<table.getColumnCount();i++){
            TableColumn col = table.getColumnModel().getColumn(i);
            int width = 0;
            TableCellRenderer renderer = col.getHeaderRenderer();
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
            Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
            width = (int) comp.getPreferredSize().width + 2*margin;
            col.setPreferredWidth(width);
        }
    }

    private void setTableRenderer(){
        TableCellRenderer renderer = null;
        renderer = new IndexRenderer();
        table.setDefaultRenderer(Long.class, renderer);
        renderer = new DateCellRenderer("dd/MM - HH:mm:ss");
        table.setDefaultRenderer(Date.class, renderer);
        renderer = new PortCellRenderer();
        table.setDefaultRenderer(Integer.class, renderer);
        renderer = new AddressCellRenderer();
        table.getColumnModel().getColumn(2).setCellRenderer(renderer);
        table.getColumnModel().getColumn(4).setCellRenderer(renderer);
        renderer = new SubnetMaskCellRenderer();
        table.getColumnModel().getColumn(3).setCellRenderer(renderer);
        table.getColumnModel().getColumn(5).setCellRenderer(renderer);
        renderer = new ProtocolCellRenderer();
        table.getColumnModel().getColumn(6).setCellRenderer(renderer);
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if(cmd.equals("Insert")){
            BlockFilterPane blockPane = new BlockFilterPane(getInstance(), true);
            blockPane.setVisible(true);
            if(blockPane.getOption() == JOptionPane.OK_OPTION){
                FilterInfo filter = blockPane.getFilter();
                filter.InsertFilter();
                filters.addElement(filter);
                model.fireTableRowsInserted(filters.size()-1, filters.size()-1);
            }
            blockPane.dispose();
        }
        if(cmd.equals("Remove")){
            int index = table.convertRowIndexToModel(table.getSelectedRow());
            if (index < 0) return;
            FilterInfo filter = (FilterInfo)filters.elementAt(index);
            filter.RemoveFilter();
            filters.removeElementAt(index);
            model.fireTableRowsDeleted(index, index);
        } else if(cmd.equals("ActivateOrDeactivate")){
            int index = table.convertRowIndexToModel(table.getSelectedRow());
            if (index < 0) return;
            FilterInfo filter = (FilterInfo)filters.elementAt(index);
            filter.ActivateOrDeactivate();
            model.fireTableRowsUpdated(index, index);
        }

    }




    class PopupListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            this.showPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            this.showPopup(e);
        }


        private void showPopup(MouseEvent e){
            if(e.isPopupTrigger()){
                Point p = e.getPoint();
                int rowNumber = table.rowAtPoint(p);
                ListSelectionModel model = table.getSelectionModel();
                model.setSelectionInterval(rowNumber, rowNumber);
                popoup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

    }

    class DateCellRenderer extends SubstanceDefaultTableCellRenderer {
        private DateFormat dateFormat;
        public DateCellRenderer(int style){
            super();
            this.setDateFormat(style);
        }
        public DateCellRenderer(String pattern){
            super();
            this.setDateFormat(pattern);
        }

        public void setDateFormat(int style){
            this.dateFormat = DateFormat.getDateInstance(style);
        }
        public void setDateFormat(String pattern){
            this.dateFormat = new SimpleDateFormat(pattern);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp =  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String val = "";
            if(value instanceof Date){
                val = this.dateFormat.format(value);
            } else val = value.toString();
            if(comp instanceof JTextComponent){
                ((JTextComponent)comp).setText(val);
            } else if(comp instanceof JLabel) {
                ((JLabel)comp).setText(val);
            }
            this.setToolTipText((String) table.getModel().getValueAt(row, 10));
            return comp;
        }
    }
    class AddressCellRenderer extends SubstanceDefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(((String)value).equals("*")){
                ((JLabel)comp).setText(resourceManager.getString("anyAddress"));
            } else if(((String)value).equals("0")){
                ((JLabel)comp).setText(resourceManager.getString("myAddress"));
            }
            this.setToolTipText((String) table.getModel().getValueAt(row, 10));
            return comp;
        }
    }
    class SubnetMaskCellRenderer extends SubstanceDefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(((String)value).equals("0")){
                ((JLabel)comp).setText(resourceManager.getString("notSubnet"));
            }
            this.setToolTipText((String) table.getModel().getValueAt(row, 10));
            return comp;
        }
    }
    class PortCellRenderer extends SubstanceDefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(((Integer)value) == 0){
                ((JLabel)comp).setText(resourceManager.getString("anyPort"));
            }
            this.setToolTipText((String) table.getModel().getValueAt(row, 10));
            return comp;
        }
    }
    class IndexRenderer extends SubstanceDefaultTableCellRenderer{
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            this.setToolTipText((String) table.getModel().getValueAt(row, 10));
            return comp;
        }
    }
    class ProtocolCellRenderer extends SubstanceDefaultTableCellRenderer{
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(((String)value).equals("0")){
                ((JLabel)comp).setText(resourceManager.getString("anyProtocol"));
            }
            this.setToolTipText((String) table.getModel().getValueAt(row, 10));
            return comp;
        }
    }

}


