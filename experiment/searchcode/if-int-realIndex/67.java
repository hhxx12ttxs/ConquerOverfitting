package trussoptimizater.Gui.Tables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Observable;
import javax.swing.*;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import trussoptimizater.Gui.Actions.MyActionMap;
import trussoptimizater.Gui.Actions.SelectAction;
import trussoptimizater.Gui.GUI;
import trussoptimizater.Gui.GUIModels.LoadTableModel;
import trussoptimizater.Truss.Elements.Load;
import trussoptimizater.Truss.TrussModel;
import trussoptimizater.Truss.ElementModels.LoadModel;

public class LoadTable extends ElementTable<Load> implements java.util.Observer {//extends DynamicTable


    private final String[] columnToolTips = {
        "Load Number",
        "Node that load is attached to",
        "Force on Node in KN",
        "Bar orientation"};

    public LoadTable(LoadModel model, TrussModel truss, GUI gui) {
        super(model, truss, gui);
    }

    protected void createGui() {

        JButton removeButton = new JButton("Remove Row(s)");
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                gui.getUndoManager().setCompoundUnadoableEdit(true);
                for (int i = 0; i < table.getSelectedRows().length; i++) {
                    model.remove(table.getSelectedRows()[i] - i);
                }
                gui.getUndoManager().setCompoundUnadoableEdit(false);
                table.revalidate();
                table.repaint();
            }
        });
        JButton addButton = new JButton("add Row ");
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (truss.getNodeModel().size() > 0) {
                    //String direction, double load, Node node, int loadType, int loadNumber) {
                    Load load = new Load(model.size() + 1, truss.getNodeModel().get(0), 0, Load.VERTICAL_LOAD, Load.USER_DEFINED);

                    model.add(load);
                    table.revalidate();
                    table.repaint();
                } else {
                    JOptionPane.showMessageDialog(gui.getFrame(), "Must be 1 Node to add Load", "Alert", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.add(removeButton);
        buttonPanel.add(addButton);

        table = new JTable(new LoadTableModel(truss)) {

            //Implement table header tool tips.
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {

                    @Override
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex =
                                columnModel.getColumn(index).getModelIndex();
                        return columnToolTips[realIndex];
                    }
                };
            }
        };
        table.setGridColor(Color.black);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1,1));
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(20);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getSelectionModel().addListSelectionListener(new ElementTableSelectionListener());

        TableColumn col = table.getColumnModel().getColumn(LoadTableModel.LOAD_NUMBER_COLOUMN_INDEX);
        col.setCellRenderer(new GeneralCellRenderer());
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(LoadTableModel.NODE_COLOUMN_INDEX);
        col.setCellRenderer(new GeneralCellRenderer());
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(LoadTableModel.LOAD_COLUMN_INDEX);
        col.setCellRenderer(new GeneralCellRenderer());
        col.setPreferredWidth(100);

        String[] directions = {Load.VERTICAL_LOAD, Load.HORIZOANTAL_LOAD};
        col = table.getColumnModel().getColumn(LoadTableModel.DIRECTION_COLUMN_INDEX);
        col.setCellEditor(new MyComboBoxEditor(directions));
        col.setCellRenderer(new MyComboBoxRenderer(directions));
        col.setPreferredWidth(100);

        //adjusting JTable header
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setVerticalAlignment(SwingConstants.TOP);
        JTableHeader header = table.getTableHeader();
        Dimension dim = header.getPreferredSize();
        dim.height *=2;
        header.setPreferredSize(dim);

        panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

    }



    public JPanel getPanel() {
        return panel;
    }

    public JTable getTable() {
        return table;
    }

    /*public void update(Observable o, Object arg) {

        if (gui.getTabbedPane().getSelectedIndex() == GUI.LOADS_TAB_INDEX || truss.getOperation() != TrussModel.OPERATION_IDLE) {
            return;
        }
        table.getSelectionModel().clearSelection();
        for (int i = 0; i < model.size(); i++) {
            if (model.get(i).isSelected()) {
                table.addRowSelectionInterval(i, i);
            }

        }
    }

    class BarSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent event) {

            //Only update truss model selections if user selects rows with mouse + keyboard
            if (event.getValueIsAdjusting() || gui.getTabbedPane().getSelectedIndex() != GUI.LOADS_TAB_INDEX) {
                return;
            }
            SelectAction selectAction = (SelectAction) MyActionMap.ACTION_MAP.get(MyActionMap.SELECT_ACTION_KEY);
            selectAction.selectAll(false);
            for (int i = 0; i < table.getSelectedRows().length; i++) {
                int selectedRow = table.getSelectedRows()[i];
                model.get(selectedRow).setSelected(true);
            }

        }
    }*/
}//end of class




