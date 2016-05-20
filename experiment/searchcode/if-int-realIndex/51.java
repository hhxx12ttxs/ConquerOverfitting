package trussoptimizater.Gui.Tables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import trussoptimizater.Truss.TrussModel;

public class NodeOutputTable extends ElementOutputTable{

    //private TrussModel truss;
    public static final int NODE_COLUMN_INDEX = 0;
    public static final int X_DISPLACEMENT_COLUMN_INDEX = 1;
    public static final int Z_DISPLACEMENT_COLUMN_INDEX = 2;
    public static final int ROTATION_COLUMN_INDEX = 3;

    public static final String[] COLUMN_NAMES = {
        "Node",
        "<HTML><CENTER>X Displacment<BR>(mm)</CENTER></HTML>",
        "<HTML><CENTER>Z Displacment<BR>(mm)</CENTER></HTML>",
        "<HTML><CENTER>Ry Displacment<BR>(rads)</CENTER></HTML>"};
    public static final int COLUMN_COUNT = COLUMN_NAMES.length;
    private final String[] columnToolTips = {
        "Node Number",
        "Hoizontal Displacement in (m)",
        "Vertical Displacement in (m)",
        "Rotational Displacement in (rads)"};

    public NodeOutputTable(TrussModel truss, ListSelectionModel listModel) {
        super(truss);
        DefaultTableModel model = new DefaultTableModel(getData(), COLUMN_NAMES) {

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
                        int realIndex = columnModel.getColumn(index).getModelIndex();
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

        if(listModel!=null){
            table.setSelectionModel(listModel);
        }
        

        //adjusting JTable header
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setVerticalAlignment(SwingConstants.TOP);
        JTableHeader header = table.getTableHeader();
        Dimension dim = header.getPreferredSize();
        dim.height *=2;
        header.setPreferredSize(dim);
        setColoumnWidths();
    }

    public void setColoumnWidths() {
        TableColumn col = table.getColumnModel().getColumn(NodeOutputTable.NODE_COLUMN_INDEX);
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(NodeOutputTable.X_DISPLACEMENT_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(NodeOutputTable.Z_DISPLACEMENT_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(NodeOutputTable.ROTATION_COLUMN_INDEX);
        col.setPreferredWidth(100);
    }

    public Object[][] getData() {
        Object[][] data = new Object[truss.getNodeModel().size()][COLUMN_COUNT];
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            data[i][NODE_COLUMN_INDEX] = truss.getNodeModel().get(i).getNumber();
            data[i][X_DISPLACEMENT_COLUMN_INDEX] = truss.getNodeModel().get(i).getXDisplacement();
            data[i][Z_DISPLACEMENT_COLUMN_INDEX] = truss.getNodeModel().get(i).getZDisplacement();
            data[i][ROTATION_COLUMN_INDEX] = truss.getNodeModel().get(i).getRotDisplacement();
        }
        return data;
    }

    public JTable getTable() {
        return table;
    }

    public JScrollPane getScrollTable() {
        return new JScrollPane(table);
    }
}

