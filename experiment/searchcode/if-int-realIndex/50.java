package trussoptimizater.Gui.Tables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import trussoptimizater.Gui.*;
import trussoptimizater.Truss.JMatrix.Maths;
import trussoptimizater.Truss.TrussModel;

public class StressOutputTable extends ElementOutputTable{

    //private TrussModel truss;


    public static final int BAR_NUMBER_COLOUMN_INDEX = 0;
    public static final int NODE_COLUMN_INDEX = 1;
    public static final int STRESS_MAX_COLUMN_INDEX = 2;
    public static final int STRESS_MIN_COLUMN_INDEX = 3;
    public static final int STRESS_MAX2_COLUMN_INDEX = 4;
    public static final int STRESS_MIN2_COLUMN_INDEX = 5;
    public static final int STRESS_COLUMN_INDEX = 6;
    public static final int STRESS_RATIO_COLUMN_INDEX = 7;
    public static final String[] COLUMN_NAMES = {
        "Bar",
        "Node",
        "<HTML><CENTER>Smax<BR>(KN/mm^2)</CENTER></HTML>",
        "<HTML><CENTER>Smin<BR>(KN/mm^2)</CENTER></HTML>",
        "<HTML><CENTER>Smax (My)<BR>(KN/mm^2)</CENTER></HTML>",
        "<HTML><CENTER>Smin (My)<BR>(KN/mm^2)</CENTER></HTML>",
        "<HTML><CENTER>Stress<BR>(KN/mm^2)</CENTER></HTML>",
        "Stress Ratio"};
    public static final int COLUMN_COUNT = COLUMN_NAMES.length;
    private final String[] columnToolTips = {
        "Bar Number",
        "Node Number",
        "Max stress = F/A + My/I",
        "Min stress = F/A - My/I",
        "Max Stress = My/I",
        "Min Stress = -My/I",
        "Stress",
        "Stress Ratio = Yield Strength / Stress"};

    public StressOutputTable(TrussModel truss) {
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
        table.setGridColor(Color.darkGray);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1,1));
        table.setShowGrid(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultRenderer(Object.class, new GeneralCellRenderer());


        //adjusting JTable header
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setVerticalAlignment(SwingConstants.TOP);
        JTableHeader header = table.getTableHeader();
        Dimension dim = header.getPreferredSize();
        dim.height *=2;
        header.setPreferredSize(dim);

        setColoumnWidths();
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
    public void setColoumnWidths() {
        TableColumn col = table.getColumnModel().getColumn(StressOutputTable.BAR_NUMBER_COLOUMN_INDEX);
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(StressOutputTable.NODE_COLUMN_INDEX);
        col.setPreferredWidth(50);

        col = table.getColumnModel().getColumn(StressOutputTable.STRESS_MAX_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(StressOutputTable.STRESS_MIN_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(StressOutputTable.STRESS_MAX2_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(StressOutputTable.STRESS_MIN2_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(StressOutputTable.STRESS_COLUMN_INDEX);
        col.setPreferredWidth(100);

        col = table.getColumnModel().getColumn(StressOutputTable.STRESS_RATIO_COLUMN_INDEX);
        col.setPreferredWidth(100);
    }

    public Object[][] getData() {
        //filling up barOutput data
        //where a positive axial force means it is in compression
        //where a negative axial force means it is in tension

        Object[][] data = new Object[truss.getBarModel().size() * 2][COLUMN_COUNT];

        for (int i = 0; i < truss.getBarModel().size() * 2; i++) {

            int barIndex = i / 2;
            data[i][BAR_NUMBER_COLOUMN_INDEX] = truss.getBarModel().get(barIndex).getNumber();


            if (i % 2 == 0) {
                data[i][NODE_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getNode1().getNumber();
                data[i][STRESS_MAX_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getMaxStress()[0];
                data[i][STRESS_MIN_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getMinStress()[0];
                data[i][STRESS_MAX2_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getMaxMomentStress()[0];
                data[i][STRESS_MIN2_COLUMN_INDEX] = -truss.getBarModel().get(barIndex).getMaxMomentStress()[0];
                data[i][STRESS_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getStress();
                //data[i][STRESS_RATIO_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getStress() / truss.getBarModel().get(barIndex).getMaxStress()[0];
                data[i][STRESS_RATIO_COLUMN_INDEX] = Math.abs(truss.getBarModel().get(barIndex).getStress() /truss.getMaterials().get(truss.getMaterialKeys()[0]).getYieldStrength() );
                
            } else {
                data[i][NODE_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getNode2().getNumber();
                data[i][STRESS_MAX_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getMaxStress()[1];
                data[i][STRESS_MIN_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getMinStress()[1];
                data[i][STRESS_MAX2_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getMaxMomentStress()[1];
                data[i][STRESS_MIN2_COLUMN_INDEX] = -truss.getBarModel().get(barIndex).getMaxMomentStress()[1];
                data[i][STRESS_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getStress();
                //data[i][STRESS_RATIO_COLUMN_INDEX] = truss.getBarModel().get(barIndex).getStress() / truss.getBarModel().get(barIndex).getMaxStress()[1];
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
}

