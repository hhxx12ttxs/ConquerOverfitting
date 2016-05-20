/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ResultsDialog.java
 *
 * Created on 15/12/2010, 01:17:13 PM
 */

package com.sm4rt.ui;

import com.sm4rt.pc.db.model.ROCData;
import com.sm4rt.pc.db.model.SimpleExperiment;
import com.sm4rt.pc.kpi.KPIData;
import java.awt.Color;
import java.io.DataOutputStream;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jdesktop.application.Action;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Alfonso Kim
 */
public class ResultsDialog extends javax.swing.JDialog {

    private List<KPIData> kpiData;
    private List<ROCData> rocData;
    private SimpleExperiment experiment;
    private final static Log LOG = LogFactory.getLog(ResultsDialog.class);
    private String[] titles;
    private Object[][] data;

    /** Creates new form ResultsDialog */
    public ResultsDialog(java.awt.Frame parent, 
            List<KPIData> kpiData,
            List<ROCData>rocData,
            SimpleExperiment experiment,
            boolean modal) {
        super(parent, modal);
        this.kpiData = kpiData;
        this.rocData = rocData;
        this.experiment = experiment;
        initComponents();
        getRootPane().setDefaultButton(btnCerrar);
        scrollKPI.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        buildKPITable();
        buildAFPRChart();
        buildRocCurve();
       
    }

    private void buildKPITable(){
        DecimalFormat doubleFormat = new DecimalFormat("0.##");
        titles = new String[]{
            "Epsilon", "SumaTP", "Ahorros",
            "Tx TP", "Tx FP", "Tx TN", "Tx FN",
            "Tarjetas TP", "Tarjetas FP", "Tarjetas TN", "Tarjetas FN",
            "Monto TP", "Monto FP", "Monto TN", "Monto FN",
            "Alarmas", "ADR",
            "ADT", "AFPR", "VDR", "FPR", "TPR"
        };
        data = new Object[kpiData.size()][titles.length];
        for (int row = 0; row < kpiData.size(); row++){
            KPIData kData = kpiData.get(row);
           // LOG.info("Nuevos valores: STP:" + kData.getSumaTP() + " -- stpr:" + kData.getStpr());
            data[row] = new Object[]{
                doubleFormat.format(kData.getEpsilon()),
                new Integer(kData.getSumaTP()),
                doubleFormat.format(kData.getSavings()),
                new Integer(kData.getTransCountTP()),
                new Integer(kData.getTransCountFP()),
                new Integer(kData.getTransCountTN()),
                new Integer(kData.getTransCountFN()),
                new Integer(kData.getCardCountTP()),
                new Integer(kData.getCardCountFP()),
                new Integer(kData.getCardCountTN()),
                new Integer(kData.getCardCountFN()),
                doubleFormat.format(kData.getAmountTP()),
                doubleFormat.format(kData.getAmountFP()),
                doubleFormat.format(kData.getAmountTN()),
                doubleFormat.format(kData.getAmountFN()),
                // Aqui iba el Savings
                doubleFormat.format(kData.getAlarms()),
                doubleFormat.format(kData.getAdr()),
                doubleFormat.format(kData.getAdt()),
                doubleFormat.format(kData.getAfpr()),
                doubleFormat.format(kData.getVdr()),
                doubleFormat.format(kData.getFpr()),
                doubleFormat.format(kData.getTpr())
                // Iba aqui el STPR
            };
        }
        tblKPI.setModel(new DefaultTableModel(data, titles));
        tblKPI.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for(int colCount = 0; colCount < tblKPI.getColumnCount(); colCount++){
            tblKPI.getColumnModel().getColumn(colCount).setPreferredWidth(70);
        }
    }

    private void buildAFPRChart(){
        XYSeries vdrSeries = new XYSeries("VDR", false);
        XYSeries adrSeries = new XYSeries("ADR", false);
        for (int i = 0; i < kpiData.size(); i++){
            KPIData kData = kpiData.get(i);
            vdrSeries.add(kData.getAfpr(), kData.getVdr());
            adrSeries.add(kData.getAfpr(), kData.getAdr());
        }
        XYSeriesCollection dataSet = new XYSeriesCollection();
        dataSet.addSeries(vdrSeries);
        dataSet.addSeries(adrSeries);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                    "ADR-VDR",          // chart title
                    "AFPR",               // domain axis label
                    "ADR-VDR",                  // range axis label
                    dataSet,                  // data
                    PlotOrientation.VERTICAL,
                    true,                     // include legend
                    true,
                    false
                );
        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("ADR-VDR");
        //final NumberAxis rangeAxis = new LogarithmicAxis("Log(y)");
        final NumberAxis rangeAxis = new NumberAxis("AFPR");
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        chart.setBackgroundPaint(pnlAdrChart.getBackground());
        plot.setOutlinePaint(Color.BLACK);
        ChartPanel chartPanel = new ChartPanel(chart);
        //chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        //super.setContentPane(chartPanel);
        chartPanel.setSize(pnlAdrChart.getSize());
        chartPanel.setBackground(pnlAdrChart.getBackground());
        pnlAdrChart.add(chartPanel);
    }

    private void buildRocCurve2(){
        int totalTrue = experiment.getTotalFraud();
        int totalFalse = experiment.getTotalNoFraud();
        LOG.info("TotalFraude:" + totalTrue + " -  Total No Fraude:" + totalFalse);
        boolean canDoIt = totalTrue > 0 && totalFalse > 0;
        XYSeries rocSeries = new XYSeries("ROC", false);
        for (int rocC = 0; rocC < rocData.size() && canDoIt; rocC++){
            ROCData oneRoc = rocData.get(rocC);
            double x = oneRoc.getX();
            double y = oneRoc.getY();
            rocSeries.add(x, y);
            LOG.debug("Para e=" + oneRoc.getEpsilon() + " x=" + y + ",Y=" + y);
        }
        XYSeriesCollection dataSet = new XYSeriesCollection();
        dataSet.addSeries(rocSeries);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                    "ROC",          // chart title
                    "Falsos Positivos",               // domain axis label
                    "Verdaderos Positivos",                  // range axis label
                    dataSet,                  // data
                    PlotOrientation.VERTICAL,
                    true,                     // include legend
                    true,
                    false
                );
        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("Falsos Positivos");
        //final NumberAxis rangeAxis = new LogarithmicAxis("Log(y)");
        final NumberAxis rangeAxis = new NumberAxis("Verdaderos Positivos");
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.setNoDataMessage("Imposible generar la curva");
        chart.setBackgroundPaint(pnlRocChart.getBackground());
        plot.setOutlinePaint(Color.BLACK);
        ChartPanel chartPanel = new ChartPanel(chart);
        //chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        //super.setContentPane(chartPanel);
        chartPanel.setBackground(pnlRocChart.getBackground());
        chartPanel.setSize(pnlRocChart.getSize());
        pnlRocChart.add(chartPanel);
    }


    private void buildRocCurve(){
        int totalTrue = experiment.getTotalFraud();
        int totalFalse = experiment.getTotalNoFraud();
        LOG.info("TotalFraude:" + totalTrue + " -  Total No Fraude:" + totalFalse);
        boolean canDoIt = totalTrue > 0 && totalFalse > 0;
        XYSeries rocSeries = new XYSeries("ROC", false);
        for (int kpiC = 0; kpiC < kpiData.size() && canDoIt; kpiC++){
            KPIData oneKpi = kpiData.get(kpiC);
            double x = new Double(oneKpi.getTransCountFP()).doubleValue() /
                       new Double(totalFalse).doubleValue();
            double y = new Double(oneKpi.getTransCountTP()).doubleValue() /
                       new Double(totalTrue).doubleValue();
            rocSeries.add(x, y);
            LOG.debug("Para e=" + oneKpi.getEpsilon() + " x=" + y + ",Y=" + y);
        }
        XYSeriesCollection dataSet = new XYSeriesCollection();
        dataSet.addSeries(rocSeries);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                    "ROC",          // chart title
                    "Falsos Positivos",               // domain axis label
                    "Verdaderos Positivos",                  // range axis label
                    dataSet,                  // data
                    PlotOrientation.VERTICAL,
                    true,                     // include legend
                    true,
                    false
                );
        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("Falsos Positivos");
        //final NumberAxis rangeAxis = new LogarithmicAxis("Log(y)");
        final NumberAxis rangeAxis = new NumberAxis("Verdaderos Positivos");
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.setNoDataMessage("Imposible generar la curva");
        chart.setBackgroundPaint(pnlRocChart.getBackground());
        plot.setOutlinePaint(Color.BLACK);
        ChartPanel chartPanel = new ChartPanel(chart);
        //chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        //super.setContentPane(chartPanel);
        chartPanel.setBackground(pnlRocChart.getBackground());
        chartPanel.setSize(pnlRocChart.getSize());
        pnlRocChart.add(chartPanel);
    }

    @Action
    public void excelAction() {
        LOG.info("Exportando a Excel");
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar archivo KPI");
        fc.setFileFilter(new FileFilter(){
            @Override public boolean accept(File f) {
                return (f.isDirectory() && f.exists()) ||
                       (f.isFile() &&
                       (f.getName().toLowerCase().endsWith(".xls")) ||
                       (f.getName().toLowerCase().endsWith(".csv")));
            }
            @Override public String getDescription() {
                return "Archivos de MS Excel (xls) o Valores separados por Coma (csv)";
            }
        });
        if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            try {
                File targetFile = fc.getSelectedFile();
                FileOutputStream fileOut = new FileOutputStream(targetFile.getAbsolutePath());
                DataOutputStream dos = new DataOutputStream(fileOut);
                LOG.info("Guardando archivo " + targetFile.getAbsolutePath());
                boolean excelMode = targetFile.getName().toLowerCase().endsWith(".xls");
                Workbook wb = new HSSFWorkbook();
                Sheet sheet = wb.createSheet("KPI");
                //CreationHelper createHelper = wb.getCreationHelper();
                // == Titulos ==
                Row row = sheet.createRow((short)0);
                for(int rowC = 0; rowC < titles.length; rowC++){
                    if(excelMode){
                        row.createCell(rowC).setCellValue(titles[rowC]);
                    } else {
                        dos.writeBytes(titles[rowC]);
                        if(rowC < titles.length-1){
                            dos.writeChars(",");
                        }
                    }
                }
                if(!excelMode){
                    dos.writeChars("\n");
                }
                // == Datos ==
                for(int rowC = 0; rowC < data.length; rowC++){
                    Object[] rowData = data[rowC];
                    row = sheet.createRow(rowC+1);
                    for(int dataC = 0; dataC < rowData.length; dataC++){
                        if(excelMode){
                            if(rowData[dataC] instanceof Double){
                                row.createCell(dataC).setCellValue((Double)rowData[dataC]);
                            } else if(rowData[dataC] instanceof Integer){
                                row.createCell(dataC).setCellValue((Integer)rowData[dataC]);
                            } else {
                                row.createCell(dataC).setCellValue(rowData[dataC].toString());
                            }
                        } else {
                            dos.writeChars(rowData[dataC].toString());
                            if(dataC < rowData.length-1){
                                dos.writeChars(",");
                            }
                        }
                    }
                    if(!excelMode){
                        dos.writeChars("\n");
                    }
                }
                // Escribir el archivo
                if(excelMode){
                    wb.write(fileOut);
                    fileOut.close();
                } else {
                    dos.flush();
                    dos.close();
                }
                JOptionPane.showMessageDialog(
                        this,
                        "Exportado correctamente",
                        "Informacion",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e){
                LOG.error("Error al escribir archivo de Excel", e);
                JOptionPane.showMessageDialog(
                        this,
                        "Error al exportar, verifique que el archivo no est?? siendo usado.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlKPI = new javax.swing.JPanel();
        btnExport = new javax.swing.JButton();
        scrollKPI = new javax.swing.JScrollPane();
        tblKPI = new javax.swing.JTable();
        pnlAFPR = new javax.swing.JPanel();
        pnlAdrChart = new javax.swing.JPanel();
        pnlROC = new javax.swing.JPanel();
        pnlRocChart = new javax.swing.JPanel();
        btnCerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.sm4rt.ui.UI.class).getContext().getResourceMap(ResultsDialog.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        pnlKPI.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlKPI.border.title"))); // NOI18N
        pnlKPI.setName("pnlKPI"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.sm4rt.ui.UI.class).getContext().getActionMap(ResultsDialog.class, this);
        btnExport.setAction(actionMap.get("excelAction")); // NOI18N
        btnExport.setText(resourceMap.getString("btnExport.text")); // NOI18N
        btnExport.setName("btnExport"); // NOI18N

        scrollKPI.setAutoscrolls(true);
        scrollKPI.setName("scrollKPI"); // NOI18N

        tblKPI.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblKPI.setName("tblKPI"); // NOI18N
        scrollKPI.setViewportView(tblKPI);

        javax.swing.GroupLayout pnlKPILayout = new javax.swing.GroupLayout(pnlKPI);
        pnlKPI.setLayout(pnlKPILayout);
        pnlKPILayout.setHorizontalGroup(
            pnlKPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlKPILayout.createSequentialGroup()
                .addContainerGap(424, Short.MAX_VALUE)
                .addComponent(btnExport))
            .addComponent(scrollKPI, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
        );
        pnlKPILayout.setVerticalGroup(
            pnlKPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlKPILayout.createSequentialGroup()
                .addComponent(scrollKPI, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExport))
        );

        pnlAFPR.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlAFPR.border.title"))); // NOI18N
        pnlAFPR.setName("pnlAFPR"); // NOI18N

        pnlAdrChart.setName("pnlAdrChart"); // NOI18N

        javax.swing.GroupLayout pnlAdrChartLayout = new javax.swing.GroupLayout(pnlAdrChart);
        pnlAdrChart.setLayout(pnlAdrChartLayout);
        pnlAdrChartLayout.setHorizontalGroup(
            pnlAdrChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 364, Short.MAX_VALUE)
        );
        pnlAdrChartLayout.setVerticalGroup(
            pnlAdrChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 250, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlAFPRLayout = new javax.swing.GroupLayout(pnlAFPR);
        pnlAFPR.setLayout(pnlAFPRLayout);
        pnlAFPRLayout.setHorizontalGroup(
            pnlAFPRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlAdrChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlAFPRLayout.setVerticalGroup(
            pnlAFPRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlAdrChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pnlROC.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlROC.border.title"))); // NOI18N
        pnlROC.setName("pnlROC"); // NOI18N

        pnlRocChart.setName("pnlRocChart"); // NOI18N

        javax.swing.GroupLayout pnlRocChartLayout = new javax.swing.GroupLayout(pnlRocChart);
        pnlRocChart.setLayout(pnlRocChartLayout);
        pnlRocChartLayout.setHorizontalGroup(
            pnlRocChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 364, Short.MAX_VALUE)
        );
        pnlRocChartLayout.setVerticalGroup(
            pnlRocChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 267, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlROCLayout = new javax.swing.GroupLayout(pnlROC);
        pnlROC.setLayout(pnlROCLayout);
        pnlROCLayout.setHorizontalGroup(
            pnlROCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlRocChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlROCLayout.setVerticalGroup(
            pnlROCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlRocChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        btnCerrar.setAction(actionMap.get("closeAction")); // NOI18N
        btnCerrar.setText(resourceMap.getString("btnCerrar.text")); // NOI18N
        btnCerrar.setName("btnCerrar"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(pnlKPI, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlAFPR, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlROC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(btnCerrar, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlKPI, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(pnlAFPR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlROC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCerrar)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void closeAction() {
        setVisible(false);
    }

    /**
    * @param args the command line arguments
    */
    /*
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ResultsDialog dialog = new ResultsDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnExport;
    private javax.swing.JPanel pnlAFPR;
    private javax.swing.JPanel pnlAdrChart;
    private javax.swing.JPanel pnlKPI;
    private javax.swing.JPanel pnlROC;
    private javax.swing.JPanel pnlRocChart;
    private javax.swing.JScrollPane scrollKPI;
    private javax.swing.JTable tblKPI;
    // End of variables declaration//GEN-END:variables

}

