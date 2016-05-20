package trussoptimizater.Gui.Tables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import trussoptimizater.Gui.*;
import trussoptimizater.Truss.TrussModel;

public class BarOutputTable extends ElementOutputTable{

    public static final int BAR_NUMBER_COLOUMN_INDEX = 0;
    public static final int NODE_COLUMN_INDEX = 1;
    public static final int AXIAL_COLUMN_INDEX = 2;
    public static final int SHEAR_COLUMN_INDEX = 3;
    public static final int MOMENT_COLUMN_INDEX = 4;
    public static final int MAX_FORCE_COLUMN_INDEX = 5;
    public static final int FORCE_RATIO_COLUMN_INDEX = 6;
    public static final String[] COLUMN_NAMES = {
        "Bar",
        "Node",
        "<HTML><CENTER>Axial<BR>(KN)</CENTER></HTML>",
        "<HTML><CENTER>Shear<BR>(KN)</CENTER></HTML>",
        "<HTML><CENTER>Moment<BR>(KNm)</CENTER></HTML>",
        "<HTML><CENTER>Max Compression Force<BR>(KN)</CENTER></HTML>",
        "Force Ratio"};
    public static final int COLUMN_COUNT = COLUMN_NAMES.length;
    private final String[] columnToolTips = {
        "Bar Number",
        null,
        "Axial Force in bar in (KN), where positive axial force denotes compression force",
        "Shear Force in bar in (KN)",
        "Moment at node in (KNm)",
        "Axial Force in bar in (KN), where positive axial force denotes compression force",
        "Max Compression force allowed in bar, found from following EC",
        null};

    public BarOutputTable(TrussModel truss, GUI gui) { //, ListSelectionModel listModel
        super(truss);

        DefaultTableModel model = new DefaultTableModel(getData(), COLUMN_NAMES){

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        };
        table = new JTable(model) {
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



        table.setAutoCreateRowSorter(true);
        table.setGridColor(Color.black);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1,1));

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultRenderer(Object.class, new GeneralCellRenderer());
        //table.setSelectionModel(listModel);

        //adjusting JTable header
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setVerticalAlignment(SwingConstants.TOP);
        JTableHeader header = table.getTableHeader();
        Dimension dim = header.getPreferredSize();
        dim.height *=2;
        header.setPreferredSize(dim);

        setColoumnWidths();
    }


    protected void setColoumnWidths() {
        TableColumn col = table.getColumnModel().getColumn(BarOutputTable.BAR_NUMBER_COLOUMN_INDEX);
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(BarOutputTable.NODE_COLUMN_INDEX);
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(BarOutputTable.AXIAL_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(BarOutputTable.SHEAR_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(BarOutputTable.MOMENT_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(BarOutputTable.MAX_FORCE_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(BarOutputTable.FORCE_RATIO_COLUMN_INDEX);
        col.setPreferredWidth(100);
    }

    public Object[][] getData() {
        //filling up barOutput data
        //where a positive axial force means it is in compression
        //where a negative axial force means it is in tension

        //Object[][] data = new Object[truss.getBarModel().size()][COLUMN_COUNT];
        Object[][] data = new Object[truss.getBarModel().size() * 2][COLUMN_COUNT];

        for (int i = 0; i < truss.getBarModel().size() * 2; i++) {

            int barIndex = i / 2;
            if (i % 2 == 0) {
                data[i][BAR_NUMBER_COLOUMN_INDEX] = truss.getBarModel().get(barIndex).getNumber();
                data[i][NODE_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getNode1().getNumber();
                data[i][AXIAL_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getAxialForce();
                data[i][SHEAR_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getShearForce()[0];
                data[i][MOMENT_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getMomentForce()[0];
            } else {
                data[i][BAR_NUMBER_COLOUMN_INDEX] = truss.getBarModel().get(barIndex).getNumber();
                data[i][NODE_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getNode2().getNumber();
                data[i][AXIAL_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getAxialForce();
                data[i][SHEAR_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getShearForce()[1];
                data[i][MOMENT_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getMomentForce()[1];
            }


            if (!truss.getBarModel().get(barIndex).isInTension()) {
                double forceRatio = truss.getBarModel().get(barIndex).getAxialForce() / truss.getBarModel().get(barIndex).getMaxCompressionAxialForce();
                data[i][MAX_FORCE_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getMaxCompressionAxialForce();
                data[i][FORCE_RATIO_COLUMN_INDEX] = forceRatio;
            }
        }
        return data;
    }

    public JTable getTable() {
        return table;
    }

    public JScrollPane getScrollTable() {
        return new JScrollPane(table);
    }

    /*class BarOutputSelectionListener implements ListSelectionListener {

    public void valueChanged(ListSelectionEvent event) {


    //Only update truss model selections if user selects rows with mouse + keyboard
    if (event.getValueIsAdjusting() || gui.getTabbedPane().getSelectedIndex() != GUI.BAR_OUTPUT_TAB_INDEX
    && gui.getTabbedPane().getSelectedIndex() != GUI.BARS_TAB_INDEX  ) {
    return;
    }
    truss.unHighlightAll();
    for (int i = 0; i < table.getSelectedRows().length; i++) {
    int selectedRow = table.getSelectedRows()[i];
    truss.getBarModel().get(selectedRow).setSelected(true);
    }

    }
    }*/
}

