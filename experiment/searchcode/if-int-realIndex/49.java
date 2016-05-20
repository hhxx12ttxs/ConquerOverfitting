package trussoptimizater.Gui.Tables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import trussoptimizater.Truss.TrussModel;

public class SupportOutputTable extends ElementOutputTable{

    
    public static final int SUPPORT_NUMBER_COLOUMN_INDEX = 0;
    public static final int NODE_COLUMN_INDEX = 1;
    public static final int UX_COLUMN_INDEX = 2;
    public static final int UZ_COLUMN_INDEX = 3;
    public static final int RY_COLUMN_INDEX = 4;
    
    public static final String[] COLUMN_NAMES = {
        "Support",
        "Node",
        "<HTML><CENTER>X Reaction<BR>(KN)</CENTER></HTML>",
        "<HTML><CENTER>Z Reaction<BR>(KN)</CENTER></HTML>",
        "<HTML><CENTER>Ry Reaction<BR>(KNm)</CENTER></HTML>"};
    public static int COLUMN_COUNT = COLUMN_NAMES.length;
    private final String[] columnToolTips = {
        "Support Number",
        "Node that support is attached too",
        "Horizontal reaction in (KN)",
        "Vertical reaction in (KN)",
        "Rotational reaction in (KNm)"};

    public SupportOutputTable(TrussModel truss, ListSelectionModel listModel) {
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
                        int realIndex =
                                columnModel.getColumn(index).getModelIndex();
                        return columnToolTips[realIndex];
                    }
                };
            }
        };
        table.setAutoCreateRowSorter(true);
        table.setGridColor(Color.black);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1,1));
        table.setShowGrid(true);
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
        TableColumn col = table.getColumnModel().getColumn(SupportOutputTable.SUPPORT_NUMBER_COLOUMN_INDEX);
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(SupportOutputTable.NODE_COLUMN_INDEX);
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(SupportOutputTable.UX_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(SupportOutputTable.UZ_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(SupportOutputTable.RY_COLUMN_INDEX);
        col.setPreferredWidth(100);

        //setHeaderRenderer();

    }

    /*public void setHeaderRenderer(){
    MultiLineHeaderRenderer mlhr = new MultiLineHeaderRenderer();
    Enumeration e = table.getColumnModel().getColumns();
    while (e.hasMoreElements()) {
    TableColumn col = ((TableColumn) e.nextElement());
    col.setHeaderRenderer(mlhr);
    }
    }*/
    public Object[][] getData() {
        Object[][] data = new Object[truss.getSupportModel().size()][COLUMN_COUNT];
        for (int i = 0; i < truss.getSupportModel().size(); i++) {
            data[i][SUPPORT_NUMBER_COLOUMN_INDEX] = truss.getSupportModel().get(i).getNumber();
            data[i][NODE_COLUMN_INDEX] = truss.getSupportModel().get(i).getNode().getNumber();
            data[i][UX_COLUMN_INDEX] = truss.getSupportModel().get(i).getReactionX();
            data[i][UZ_COLUMN_INDEX] = truss.getSupportModel().get(i).getReactionZ();
            data[i][RY_COLUMN_INDEX] = truss.getSupportModel().get(i).getRotationReaction();
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

