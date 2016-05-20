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
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import trussoptimizater.Gui.Actions.MyActionMap;
import trussoptimizater.Gui.Actions.SelectAction;
import trussoptimizater.Gui.Dialogs.SectionDialog;
import trussoptimizater.Gui.GUI;
import trussoptimizater.Gui.GUIModels.BarTableModel;
import trussoptimizater.Gui.GUIModels.SectionComboBoxModel;
import trussoptimizater.Truss.TrussModel;
import trussoptimizater.Truss.ElementModels.BarModel;
import trussoptimizater.Truss.Elements.Bar;
import trussoptimizater.Truss.Materials.Steel;

public class BarTable extends ElementTable<Bar> implements java.util.Observer {//extends DynamicTable

    private BarTableModel barTableModel;
    private SectionComboBoxModel sectionComboBoxModel;
    private final String[] columnToolTips = {
        "Bar Number",
        "Number of first Node bar is attached too",
        "Number of second Node bar is attached too",
        "Length of Bar in (m)",
        "Angle of Bar in degrees where 3 O'Clock is 0 degrees and clockwise is positive",
        "Bar Restraint",
        "Bar Section",
        "Area of Section",
        "Material"};

    public BarTable(BarModel model, TrussModel truss, GUI gui) {
        super(model, truss, gui);
        truss.getSectionModel().addObserver(this);
        //gui.getSectionDialog().addObserver(this);

    }

    protected void createGui() {
        sectionComboBoxModel = new SectionComboBoxModel(truss.getSectionModel());

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
                if (truss.getNodeModel().size() > 1) {
                    Bar bar = new Bar(model.size() + 1, truss.getNodeModel().get(0), truss.getNodeModel().get(1), truss.getMaterials().get(Steel.MATERIAL_NAME));

                    model.add(bar);
                    table.revalidate();
                    table.repaint();
                } else {
                    JOptionPane.showMessageDialog(gui.getFrame(), "Must be 2 Nodes before adding Bar", "Alert", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.add(removeButton);
        buttonPanel.add(addButton);


        barTableModel = new BarTableModel(truss);
        table = new JTable(barTableModel) {

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

            @Override
            public TableCellEditor getCellEditor(int row, int column) {

                if (column == BarTableModel.SECTION_COLUMN_INDEX) {
                    SectionComboBoxModel scbm = new SectionComboBoxModel(truss.getSectionModel());
                    return new MyComboBoxEditor(scbm.toNameList());
                } else {
                    return super.getCellEditor(row, column);
                }


            }
        };

        table.setGridColor(Color.black);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(20);
        table.getSelectionModel().addListSelectionListener(new ElementTableSelectionListener());

        TableColumn col = table.getColumnModel().getColumn(BarTableModel.BAR_NUMBER_COLOUMN_INDEX);
        col.setCellRenderer(new GeneralCellRenderer());
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(BarTableModel.NODE1_COLUMN_INDEX);
        col.setCellRenderer(new GeneralCellRenderer());
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(BarTableModel.NODE2_COLUMN_INDEX);
        col.setCellRenderer(new GeneralCellRenderer());
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(BarTableModel.LENGTH_COLUMN_INDEX);
        col.setCellRenderer(new GeneralCellRenderer());
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(BarTableModel.ANGLE_COLUMN_INDEX);
        col.setCellRenderer(new GeneralCellRenderer());
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(BarTableModel.SECTION_COLUMN_INDEX);
        //col.setCellRenderer(new MyComboBoxRenderer(gui.getSectionComboBoxModel()));
        col.setCellRenderer(new MyComboBoxRenderer(sectionComboBoxModel));
        //col.setCellEditor(new MyComboBoxEditor(sectionComboBoxModel.toArray()));
        col.setPreferredWidth(120);

        col = table.getColumnModel().getColumn(BarTableModel.AREA_COLUMN_INDEX);
        col.setCellRenderer(new GeneralCellRenderer());
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(BarTableModel.RESTRAINTS_COLUMN_INDEX);
        String[] restraints = {Bar.PINNED_PINNED_RESTRAINT, Bar.FIXED_FIXED_RESTRAINT};
        col.setCellEditor(new MyComboBoxEditor(restraints));
        col.setCellRenderer(new MyComboBoxRenderer(restraints));
        col.setPreferredWidth(120);

        col = table.getColumnModel().getColumn(BarTableModel.MATERIAL_COLUMN_INDEX);
        String[] materials = truss.getMaterialKeys();
        col.setCellEditor(new MyComboBoxEditor(materials));
        col.setCellRenderer(new MyComboBoxRenderer(materials));
        //col.setCellEditor(new MyComboBoxEditor(materials ));
        //col.setCellRenderer(new MyComboBoxRenderer(materials ));
        col.setPreferredWidth(120);

        //adjusting JTable header
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setVerticalAlignment(SwingConstants.TOP);
        JTableHeader header = table.getTableHeader();
        Dimension dim = header.getPreferredSize();
        dim.height *= 2;
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

    public BarTableModel getBarTableModel() {
        return barTableModel;
    }

    /*public void update(Observable o, Object arg) {



        if (gui.getTabbedPane().getSelectedIndex() == GUI.BARS_TAB_INDEX
        || truss.getOperation() != TrussModel.OPERATION_IDLE) {
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
            /*if (event.getValueIsAdjusting()
            || gui.getTabbedPane().getSelectedIndex() != GUI.BARS_TAB_INDEX) {//&& gui.getTabbedPane().getSelectedIndex() != GUI.BAR_OUTPUT_TAB_INDEX)
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


