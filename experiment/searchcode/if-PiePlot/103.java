/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tnviet.proj.jna.stat;

import com.jgoodies.uif_lite.component.UIFSplitPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import jpcap.packet.Packet;
import org.jdesktop.swingx.JXTitledPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import tnviet.proj.jna.JNCaptor;
import tnviet.proj.jna.Program;
import tnviet.proj.jna.utilities.ResourceManager;

/**
 *
 * @author Administrator
 */
public class JNCumulativeStatDialog extends JNStatDialog implements ListSelectionListener{
    JTable table;
    CumulativeStatTableModel model = null;
    JNStatisticsController controller;
    int statType = 0;
    ResourceManager resourceManager = new ResourceManager(this.getClass(), Program.locale);
    DefaultPieDataset pieDataset;
    JFreeChart chart;
    PiePlot plot;

    public static JNCumulativeStatDialog createDialog(Frame owner, Vector packets, JNStatisticsController controller){
        final JNCumulativeStatDialog dialog = new JNCumulativeStatDialog(owner,packets, controller);
        final JNCaptor captor = JNCaptor.getInstance();
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                captor.removeStatDialog(dialog);
            }
        });
        return dialog;
    }

    public JNCumulativeStatDialog(Frame owner, Vector packets, JNStatisticsController controller) {
        super(owner, controller.getName(), false);
        this.controller = controller;
        controller.analyze(packets);
        model=new CumulativeStatTableModel();
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScroll = new JScrollPane(table);
        JXTitledPanel tablePane = new JXTitledPanel(resourceManager.getString("table.title"), tableScroll);
        tablePane.setPreferredSize(tableScroll.getPreferredSize());
        if(controller.getLabels().length>1){
            pieDataset = new DefaultPieDataset();
            UIFSplitPane splitPane = new UIFSplitPane(UIFSplitPane.HORIZONTAL_SPLIT);
            Object obj;
            for(int i=1; i<table.getColumnCount();i++){
                obj = table.getValueAt(0, i);
                long tempValue = 0;
                if(obj != null){
                    tempValue = Integer.parseInt(obj.toString());
                }
                pieDataset.setValue(table.getColumnName(i),tempValue);
            }
            chart = ChartFactory.createRingChart((String)model.getValueAt(0, 0),
                    pieDataset,
                    true, true, true);
            plot = (PiePlot)chart.getPlot();
            plot.setBackgroundPaint(Color.white);
            ChartPanel chartPanel = new ChartPanel(chart);
            JXTitledPanel chartPane = new JXTitledPanel(resourceManager.getString("chart.title"), chartPanel);
            splitPane.setTopComponent(tablePane);
            splitPane.setBottomComponent(chartPane);
            this.getContentPane().add(splitPane, BorderLayout.CENTER);
        } else {
            this.getContentPane().add(tablePane, BorderLayout.CENTER);
        }

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    @Override
    void fireUpdate() {
        int selectRow = table.getSelectedRow();
        if(model!=null){
            model.update();
        }
        if(selectRow>=0){
            table.setRowSelectionInterval(selectRow, selectRow);
            updateChart(selectRow);
        } else {
            updateChart(0);
        }
    }

    @Override
    public void addPacket(Packet p) {
        controller.addPacket(p);
    }

    @Override
    public void clear() {
        controller.clear();
        if(model!= null) model.update();
        updateChart(0);
        repaint();
    }

    public void valueChanged(ListSelectionEvent e) {
        if(e.getValueIsAdjusting()) return;
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if(lsm.isSelectionEmpty()) statType = 0;
        else statType = lsm.getMinSelectionIndex();
        System.out.println(statType);
        updateChart(statType);
        repaint();
    }
    public void updateChart(int type){
        if(chart != null && pieDataset!= null){
            for(int i=1; i<table.getColumnCount();i++){
                pieDataset.setValue(model.getColumnName(i),(Long)model.getValueAt(type, i));
            }
            chart.setTitle((String)model.getValueAt(type, 0));
        }
    }

    class CumulativeStatTableModel extends AbstractTableModel{
        String[] labels;
        Object[][] values;

        public CumulativeStatTableModel() {
            labels = new String[controller.getLabels().length + 1];
            labels[0] = new String();
            System.arraycopy(controller.getLabels(), 0, labels, 1, controller.getLabels().length);
            String[] types = controller.getStatTypes();
            values = new Object[types.length][controller.getLabels().length + 1];
            this.update();
        }


        public int getRowCount() {
            return values.length;
        }

        public int getColumnCount() {
            return labels.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return values[rowIndex][columnIndex];
        }

        @Override
        public String getColumnName(int column) {
            return labels[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return this.getValueAt(0, columnIndex).getClass();
        }


         void update(){
            String[] types = controller.getStatTypes();
            values = new Object[types.length][controller.getLabels().length + 1];
            for(int i = 0; i < values.length; i++){
                values[i][0] = types[i];
                long[] ret = controller.getValues(i);
                for(int j = 0; j<ret.length; j++){
                    values[i][j+1] = new Long(ret[j]);
                }
            }
            fireTableDataChanged();
        }


    }

}

