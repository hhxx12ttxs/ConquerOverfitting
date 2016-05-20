/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tnviet.proj.jna.report;

/**
 *
 * @author Administrator
 */
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif_lite.component.UIFSplitPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import org.apache.commons.mail.EmailException;
import tnviet.proj.jna.Program;
import tnviet.proj.jna.utilities.ResourceManager;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.SimpleEmail;
import org.jdesktop.swingx.JXTitledPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jvnet.substance.api.renderers.SubstanceDefaultTableCellRenderer;
import tnviet.proj.jna.utilities.TrafficRendererUtils;
import tnviet.proj.jna.utilities.Utilities;
public class ReportPane extends JDialog implements  ItemListener{
    ResourceManager resourceManager = new ResourceManager(this.getClass(), Program.locale);
    private AbstractButton todayButton, totalsButton, hourlyButton, weeklyButton, monthlyButton, trafficVolumeChartButton, dailyButton, emailButton;
    JFreeChart chart;
    ButtonGroup buttonGroup;
    JXTitledPanel categoryTitledPane, reportTitledPane;
    UIFSplitPane splitPane;
    JPanel cards;
    JTable table;
    JButton  closeButton, printButton;
    String title = "";

    JTextField smtpField;
    JTextField fromField;
    JTextField userField;
    JTextField toField;
    JTextField subjectField;
    JCheckBox chk;
    JPasswordField passField;

    public ReportPane() {
        
        cards = new JPanel(new CardLayout());
        JPanel tempPanel = this.buildLast24HoursPane();
        cards.add(tempPanel, "today");
        tempPanel = this.buildTotalsPane();
        cards.add(tempPanel, "totals");
        tempPanel = this.buildHourlyReportPane();
        cards.add(tempPanel, "hourly");
        tempPanel = this.buildDailyReportPane();
        cards.add(tempPanel, "daily");
        tempPanel = this.buildWeeklyReportPane();
        cards.add(tempPanel, "weekly");
        tempPanel = this.buildMonthlyReportPane();
        cards.add(tempPanel, "monthly");
        tempPanel = this.buildSendMailPane();
        cards.add(tempPanel, "email");
        JToolBar categoryBar = this.createCategoryBar();
        categoryTitledPane = new JXTitledPanel(resourceManager.getString("reportCategory"), categoryBar);
        reportTitledPane = new JXTitledPanel(resourceManager.getString("report"), cards);
        printButton = new JButton(resourceManager.createImageIcon("fileprint16.png", "Print"));
        printButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File f = new File(Program.JNProperty.getProperty("lastDir"));
                JFileChooser chooser = new JFileChooser(f);
                int ret = chooser.showSaveDialog(null);
                if(ret == JFileChooser.APPROVE_OPTION){
                    File file = chooser.getSelectedFile();
                    printPdfTable(file);
                }
            }
        });
        closeButton = new JButton(resourceManager.createImageIcon("fileclose16.png", "Close"));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        

        JPanel headerButtonBar = new JPanel();
        headerButtonBar.add(printButton);
        headerButtonBar.add(closeButton);

        reportTitledPane.setRightDecoration(headerButtonBar);
        splitPane = new UIFSplitPane(UIFSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(categoryTitledPane);
        splitPane.setBottomComponent(reportTitledPane);
        this.setLayout(new FormLayout("pref:grow", "pref"));
        CellConstraints cc = new CellConstraints();
        this.add(splitPane, cc.xy(1, 1));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    //private JToolBar tb;


    public JFreeChart createLast24HoursChart(Connection con){
        
        DefaultCategoryDataset datasetTraffic = new DefaultCategoryDataset();
        DefaultCategoryDataset datasetPackets = new DefaultCategoryDataset();
        CategoryAxis domainAxis = new CategoryAxis(resourceManager.getString("hour"));
        ValueAxis valueAxis1 = new NumberAxis(resourceManager.getString("traffic") + " " + "MB");
        ValueAxis valueAxis2 = new NumberAxis("Packets");
        StackedBarRenderer renderer1 = new StackedBarRenderer(false);
        StackedBarRenderer renderer2 = new StackedBarRenderer(false);
        CategoryPlot plot1 = new CategoryPlot(datasetTraffic, null, valueAxis1, renderer1);
        CategoryPlot plot2 = new CategoryPlot(datasetPackets, null, valueAxis2, renderer2);
        CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot(domainAxis);
        plot.add(plot1, 1);
        plot.add(plot2, 1);
        for(int i = 24; i>=0; i--){
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.HOUR, -i);
            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            ResultSet rs;
            try {
                rs = this.createHoursDiffResultSet(i, con);
                if(rs.next()){
                    double incoming = rs.getDouble(1)/8192;
                    double outgoing = rs.getDouble(2)/8192;
                    long inPackets = rs.getLong(3);
                    long outPackets = rs.getLong(4);
                    datasetTraffic.addValue(incoming, resourceManager.getString("incoming"),
                    String.valueOf(hourOfDay)
                    );
                    datasetTraffic.addValue(outgoing, resourceManager.getString("outgoing"),
                            String.valueOf(hourOfDay)
                            );
                    datasetPackets.addValue(inPackets, resourceManager.getString("inPackets"), String.valueOf(hourOfDay));
                    datasetPackets.addValue(outPackets, resourceManager.getString("outPackets"), String.valueOf(hourOfDay));
                }
            } catch (SQLException ex) {
            }
        }
        JFreeChart chart1 = new JFreeChart(resourceManager.getString("trafficLast24h"), plot);
        plot.setBackgroundPaint(Color.white);
        this.chart = chart1;
        this.table = null;

        return chart1;
    }
    public JPanel buildLast24HoursPane(){
        this.title = resourceManager.getString("trafficLast24h");
        return new ChartPanel(this.createLast24HoursChart(Program.connection));

    }

    public JPanel buildTotalsPane(){
        FormLayout layout = new FormLayout("fill:pref:grow", "pref, 2dlu, pref, 2dlu, pref, 2dlu, pref");
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        int row = 1;
        builder.add(this.createThisDayTotalPane(), cc.xy(1, row));
        row+=2;
        builder.add(this.createLast24HoursTotalsPane(), cc.xy(1, row));
        row+=2;
        builder.add(this.createThisWeekTotalsPane(), cc.xy(1, row));
        row+=2;
        builder.add(this.createThisMonthTotalsPane(), cc.xy(1, row));
        builder.setDefaultDialogBorder();
        this.chart = null;
        this.table = null;
        this.title = resourceManager.getString("totals");
        return builder.getPanel();
    }

    public JPanel buildHourlyReportPane(){
        FormLayout layout = new FormLayout("pref, 10dlu, pref, 10dlu, pref, pref:grow", "pref, 2dlu, pref, 5dlu, pref");
        int row = 1;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(7, 1, 365, 1);
        JSpinner spinner = new JSpinner(spinnerModel);
        final Vector data = new Vector();
        final HourlyReportTableModel tableModel = new HourlyReportTableModel(data);
        final JTable table = new JTable(tableModel);
        TableCellRenderer renderer;
        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSpinner sp = (JSpinner)e.getSource();
                SpinnerNumberModel model = (SpinnerNumberModel)sp.getModel();
                Integer day = (Integer)model.getValue();
                Date now = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.DATE, -day);
                Timestamp from  = new Timestamp(cal.getTime().getTime());
                try {
                    data.removeAllElements();
                    ResultSet rs = createHourlyResultSet(from, Program.connection);
                    while(rs.next()){
                        JNATraffic traffic = new JNATraffic(
                                rs.getTimestamp(1),
                                rs.getDouble(2),
                                rs.getDouble(3),
                                rs.getLong(4),
                                rs.getLong(5));
                        data.add(traffic);
                    }
                    tableModel.fireTableDataChanged();
                    }
                 catch (SQLException ex) {
                }
            }
        });
        Integer value = (Integer)spinnerModel.getValue();
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DATE, -value);
        Timestamp from  = new Timestamp(cal.getTime().getTime());
        ResultSet rs;
        try {
            rs = createHourlyResultSet(from, Program.connection);
            while(rs.next()){
            JNATraffic traffic = new JNATraffic(
                    rs.getTimestamp(1),
                    rs.getDouble(2),
                    rs.getDouble(3),
                    rs.getLong(4),
                        rs.getLong(5));
                data.add(traffic);
            }
            tableModel.fireTableDataChanged();
        } catch (SQLException ex) {
        }
        renderer = new DateCellRenderer("dd/MM/yyyy - HH':00'");
        tableModel.fireTableDataChanged();
        table.getColumnModel().getColumn(0).setCellRenderer(renderer);
        renderer = new TrafficCellRenderer();
        table.getColumnModel().getColumn(1).setCellRenderer(renderer);
        table.getColumnModel().getColumn(2).setCellRenderer(renderer);
        TableRowSorter sorter = new TableRowSorter(tableModel);
        table.setRowSorter(sorter);
        this.autoResizeColWidthByHeaderWidth(table);
        builder.addSeparator(resourceManager.getString("hourly"), cc.xyw(1, row, 6));
        row +=2;
        builder.addLabel(resourceManager.getString("timespan"), cc.xy(1, row));
        builder.add(spinner, cc.xy(3, row));
        builder.addLabel(resourceManager.getString("days"), cc.xy(5, row));
        row +=2;
        JScrollPane viewPort = new JScrollPane(table);
        builder.add(viewPort, cc.xyw(1, row, 6));
        builder.setDefaultDialogBorder();
        this.chart = null;
        this.table = table;
        this.title = resourceManager.getString("hourly");
        return builder.getPanel();
    }

    public JPanel buildWeeklyReportPane(){
        FormLayout layout = new FormLayout("pref, 10dlu, pref, 10dlu, pref, pref:grow", "pref, 2dlu, pref, 5dlu, pref");
        int row = 1;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(365, 1, 3650, 1);
        JSpinner spinner = new JSpinner(spinnerModel);
        final Vector data = new Vector();
        final HourlyReportTableModel tableModel = new HourlyReportTableModel(data);
        final JTable localTable = new JTable(tableModel);
        TableCellRenderer renderer;
        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSpinner sp = (JSpinner)e.getSource();
                SpinnerNumberModel model = (SpinnerNumberModel)sp.getModel();
                Integer value = (Integer)model.getValue();
                Calendar calNow = Calendar.getInstance();
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -value);
                data.removeAllElements();
                while (cal.before(calNow)) {
                Timestamp time  = new Timestamp(calNow.getTime().getTime());
                try {
                    ResultSet rs = createSameWeekResultSet(time, Program.connection);
                    if(rs.next()){
                    JNATraffic traffic = new JNATraffic(
                            time,
                            rs.getDouble(1),
                            rs.getDouble(2),
                            rs.getLong(3),
                            rs.getLong(4));
                        data.add(traffic);
                        }
                    } catch (SQLException ex) {
                    }
                    calNow.add(Calendar.WEEK_OF_YEAR, -1);
                }
                tableModel.fireTableDataChanged();
            }
        });
        Integer value = (Integer)spinnerModel.getValue();
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        Calendar nowCal = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -value);
        while (cal.before(nowCal)) {
            Timestamp time  = new Timestamp(nowCal.getTime().getTime());
            try {
                ResultSet rs = createSameWeekResultSet(time, Program.connection);
                if(rs.next()){
                    long sum_in_pk = rs.getLong(3);
                    long sum_out_pk = rs.getLong(4);
                    if(sum_in_pk!=0 || sum_out_pk!= 0){
                        JNATraffic traffic = new JNATraffic(
                            time,
                            rs.getDouble(1),
                            rs.getDouble(2),
                            sum_in_pk,
                            sum_out_pk);
                        data.add(traffic);
                    }
                }
            } catch (SQLException ex) {
            }
            nowCal.add(Calendar.WEEK_OF_YEAR, -1);
        }
        renderer = new WeekCellRenderer("dd/MM/yyyy");
        tableModel.fireTableDataChanged();
        localTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
        renderer = new TrafficCellRenderer();
        localTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
        localTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
        TableRowSorter sorter = new TableRowSorter(tableModel);
        localTable.setRowSorter(sorter);
        this.autoResizeColWidthByHeaderWidth(localTable);
        builder.addSeparator(resourceManager.getString("weekly"), cc.xyw(1, row, 6));
        row +=2;
        builder.addLabel(resourceManager.getString("timespan"), cc.xy(1, row));
        builder.add(spinner, cc.xy(3, row));
        builder.addLabel(resourceManager.getString("days"), cc.xy(5, row));
        row +=2;
        JScrollPane viewPort = new JScrollPane(localTable);
        builder.add(viewPort, cc.xyw(1, row, 6));
        builder.setDefaultDialogBorder();
        this.chart = null;
        this.table = localTable;
        this.title = resourceManager.getString("weekly");
        return builder.getPanel();
    }

    public JPanel buildMonthlyReportPane(){
        FormLayout layout = new FormLayout("pref, 10dlu, pref, 10dlu, pref, pref:grow", "pref, 2dlu, pref, 5dlu, pref");
        int row = 1;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(365, 1, 3650, 1);
        JSpinner spinner = new JSpinner(spinnerModel);
        final Vector data = new Vector();
        final HourlyReportTableModel tableModel = new HourlyReportTableModel(data);
        final JTable localTable = new JTable(tableModel);
        TableCellRenderer renderer;
        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSpinner sp = (JSpinner)e.getSource();
                SpinnerNumberModel model = (SpinnerNumberModel)sp.getModel();
                Integer value = (Integer)model.getValue();
                Calendar calNow = Calendar.getInstance();
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -value);
                data.removeAllElements();
                while (cal.before(calNow)) {
                Timestamp time  = new Timestamp(calNow.getTime().getTime());
                try {
                    ResultSet rs = createSameMonthResultSet(time, Program.connection);
                    if(rs.next()){
                    JNATraffic traffic = new JNATraffic(
                            time,
                            rs.getDouble(1),
                            rs.getDouble(2),
                            rs.getLong(3),
                            rs.getLong(4));
                        data.add(traffic);
                        }
                    } catch (SQLException ex) {
                    }
                    calNow.add(Calendar.MONTH, -1);
                }
                tableModel.fireTableDataChanged();
            }
        });
        Integer value = (Integer)spinnerModel.getValue();
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        Calendar nowCal = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -value);
        while (cal.before(nowCal)) {
            Timestamp time  = new Timestamp(nowCal.getTime().getTime());
            try {
                ResultSet rs = createSameMonthResultSet(time, Program.connection);
                if(rs.next()){
                    long sum_in_pk = rs.getLong(3);
                    long sum_out_pk = rs.getLong(4);
                    if(sum_in_pk!=0 || sum_out_pk!= 0){
                        JNATraffic traffic = new JNATraffic(
                            time,
                            rs.getDouble(1),
                            rs.getDouble(2),
                            sum_in_pk,
                            sum_out_pk);
                        data.add(traffic);
                    }
                }
            } catch (SQLException ex) {
            }
            nowCal.add(Calendar.MONTH, -1);
        }
        renderer = new MonthCellRenderer("M - yyyy");
        tableModel.fireTableDataChanged();
        localTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
        renderer = new TrafficCellRenderer();
        localTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
        localTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
        TableRowSorter sorter = new TableRowSorter(tableModel);
        localTable.setRowSorter(sorter);
        this.autoResizeColWidthByHeaderWidth(localTable);
        builder.addSeparator(resourceManager.getString("monthly"), cc.xyw(1, row, 6));
        row +=2;
        builder.addLabel(resourceManager.getString("timespan"), cc.xy(1, row));
        builder.add(spinner, cc.xy(3, row));
        builder.addLabel(resourceManager.getString("days"), cc.xy(5, row));
        row +=2;
        JScrollPane viewPort = new JScrollPane(localTable);
        builder.add(viewPort, cc.xyw(1, row, 6));
        builder.setDefaultDialogBorder();
        this.chart = null;
        this.table = localTable;
        this.title = resourceManager.getString("monthly");
        return builder.getPanel();
    }

    public JPanel buildDailyReportPane(){
        FormLayout layout = new FormLayout("pref, 10dlu, pref, 10dlu, pref, pref:grow", "pref, 2dlu, pref, 5dlu, pref");
        int row = 1;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(31, 1, 500, 1);
        JSpinner spinner = new JSpinner(spinnerModel);
        final Vector data = new Vector();
        final HourlyReportTableModel tableModel = new HourlyReportTableModel(data);
        final JTable localTable = new JTable(tableModel);
        TableCellRenderer renderer;
        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSpinner sp = (JSpinner)e.getSource();
                SpinnerNumberModel model = (SpinnerNumberModel)sp.getModel();
                Integer value = (Integer)model.getValue();
                Calendar calNow = Calendar.getInstance();
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -value);
                data.removeAllElements();
                while (cal.before(calNow)) {
                Timestamp time  = new Timestamp(calNow.getTime().getTime());
                try {
                    ResultSet rs = createSameDayResultSet(time, Program.connection);
                    if(rs.next()){
                    JNATraffic traffic = new JNATraffic(
                            time,
                            rs.getDouble(1),
                            rs.getDouble(2),
                            rs.getLong(3),
                            rs.getLong(4));
                        data.add(traffic);
                        }
                    } catch (SQLException ex) {
                    }
                    calNow.add(Calendar.DATE, -1);
                }
                tableModel.fireTableDataChanged();
            }
        });
        Integer value = (Integer)spinnerModel.getValue();
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        Calendar nowCal = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -value);
        while (cal.before(nowCal)) {
            Timestamp time  = new Timestamp(nowCal.getTime().getTime());
            try {
                ResultSet rs = createSameDayResultSet(time, Program.connection);
                if(rs.next()){
                    long sum_in_pk = rs.getLong(3);
                    long sum_out_pk = rs.getLong(4);
                    if(sum_in_pk!=0 || sum_out_pk!= 0){
                        JNATraffic traffic = new JNATraffic(
                            time,
                            rs.getDouble(1),
                            rs.getDouble(2),
                            sum_in_pk,
                            sum_out_pk);
                        data.add(traffic);
                    }
                }
            } catch (SQLException ex) {
            }
            nowCal.add(Calendar.DATE, -1);
        }
        renderer = new DateCellRenderer("dd/MM/yyyy");
        tableModel.fireTableDataChanged();
        localTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
        renderer = new TrafficCellRenderer();
        localTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
        localTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
        this.autoResizeColWidthByHeaderWidth(localTable);
        builder.addSeparator(resourceManager.getString("daily"), cc.xyw(1, row, 6));
        row +=2;
        builder.addLabel(resourceManager.getString("timespan"), cc.xy(1, row));
        builder.add(spinner, cc.xy(3, row));
        builder.addLabel(resourceManager.getString("days"), cc.xy(5, row));
        row +=2;
        JScrollPane viewPort = new JScrollPane(localTable);
        builder.add(viewPort, cc.xyw(1, row, 6));
        builder.setDefaultDialogBorder();
        this.chart = null;
        this.table = localTable;
        this.title = resourceManager.getString("daily");
        return builder.getPanel();
    }

    public JPanel buildSendMailPane(){
        FormLayout layout = new FormLayout("pref, 5dlu, pref:grow, 5dlu, pref, 5dlu, pref:grow",
                "pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref");
        int row = 1;
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();

        smtpField = new JTextField(Program.JNProperty.getProperty("email.smtp"));
        fromField = new JTextField(Program.JNProperty.getProperty("email.from"));
        userField = new JTextField(Program.JNProperty.getProperty("email.username"));
        toField = new JTextField(Program.JNProperty.getProperty("email.to"));
        subjectField = new JTextField(Program.JNProperty.getProperty("email.subject"));
        chk = new JCheckBox(resourceManager.getString("emailHourly"));
        chk.setSelected(Boolean.parseBoolean(Program.JNProperty.getProperty("emailHourly")));
        chk.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                int state = e.getStateChange();
                if(state == ItemEvent.SELECTED){
                    Program.JNProperty.setProperty("emailHourly", "true");
                } else {
                    Program.JNProperty.setProperty("emailHourly", "false");
                }
            }
        });
        passField = new JPasswordField(Program.JNProperty.getProperty("email.password"));
        JButton sendButton = new JButton(resourceManager.getString("send"));
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final String smtp = smtpField.getText();
                final String from = fromField.getText();
                final String[] to = Utilities.resolveEmailAddress(toField.getText());
                final String username = userField.getText();
                final String password = new String(passField.getPassword());
                final String subject = subjectField.getText();
                final String meg = " Jnet analyzer example";
                Runnable r = new Runnable() {
                    public void run() {
                        sendMail(smtp, from, to, username, password, subject, meg);
                    }
                };
                new Thread(r).start();
                
            }
        });

        builder.addLabel(resourceManager.getString("hostEmail"), cc.xy(1, row));
        builder.add(smtpField, cc.xyw(3, row, 5));
        row +=2;
        builder.addLabel(resourceManager.getString("emailFrom"), cc.xy(1, row));
        builder.add(fromField, cc.xyw(3, row, 5));
        row+=2;
        builder.addLabel(resourceManager.getString("username"), cc.xy(1, row));
        builder.add(userField, cc.xy(3, row));
        builder.addLabel(resourceManager.getString("password"), cc.xy(5, row));
        builder.add(passField, cc.xy(7, row));
        row+=2;
        builder.addLabel(resourceManager.getString("emailTo"), cc.xy(1, row));
        builder.add(toField, cc.xyw(3, row, 5));
        row+=2;
        builder.addLabel(resourceManager.getString("subject"), cc.xy(1, row));
        builder.add(subjectField, cc.xyw(3, row, 5));
        row+=2;
        builder.add(chk, cc.xy(1, row));
        builder.add(sendButton, cc.xy(3, row));

        builder.setDefaultDialogBorder();
        return builder.getPanel();


    }
//    public JPanel buildChartPane(){
//
//    }

    public void printPdf(File output){
        Document doc = new Document(PageSize.A4.rotate());
        try {
              PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream("jTable.pdf"));

              doc.open();
              PdfContentByte cb = writer.getDirectContent();

              cb.saveState();
              Graphics2D g2 = cb.createGraphicsShapes(500, 500);

              Shape oldClip = g2.getClip();
              g2.clipRect(0, 0, 500, 500);

              table.print(g2);
              g2.setClip(oldClip);

              g2.dispose();
              cb.restoreState();
            
        } catch (Exception e) {
        }
        doc.close();

    }
    public void printPdfTable(File output){
        PdfPTable pdfpTable = new PdfPTable(5);
        pdfpTable.setTotalWidth(300f);
        pdfpTable.setHeaderRows(2);
        if(table==null) return;
        int rowCount = table.getModel().getRowCount();
        int columnCount = table.getModel().getRowCount();
        for(int i=0; i< rowCount; i++){
            for(int j = 0; j< columnCount; j++){
                PdfPCell cell = new PdfPCell(new Phrase(table.getModel().getValueAt(i, j).toString()));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfpTable.addCell(cell);
            }
        }
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(output));
            doc.open();
            doc.add(pdfpTable);
            
        } catch (Exception e) {
        }
        doc.close();
    }

    public void sendMail(String smtp, String from, String[] to, String username, String password, String subject, String msg){
        try {
            SimpleEmail email = new SimpleEmail();
            email.setHostName(smtp);
            email.setAuthenticator(new DefaultAuthenticator(username, password));
            email.setDebug(true);
            email.setSmtpPort(465);
            email.getMailSession().getProperties().put("mail.smtps.auth", "true");
            email.getMailSession().getProperties().put("mail.debug", "true");
            email.getMailSession().getProperties().put("mail.smtps.port", "587");
            email.getMailSession().getProperties().put("mail.smtps.socketFactory.port", "587");
            email.getMailSession().getProperties().put("mail.smtps.socketFactory.class",   "javax.net.ssl.SSLSocketFactory");
            email.getMailSession().getProperties().put("mail.smtps.socketFactory.fallback", "false");
            email.getMailSession().getProperties().put("mail.smtp.starttls.enable", "true");
            for (int i = 0; i < to.length; i++) {
                email.addTo(to[i]);
                
            }
            email.setFrom(from, "JNetAnalyzer");
            email.setSubject(subject);
            email.setMsg(msg);
            email.setTLS(true);
            email.send();
        } catch (EmailException ex) {
            Logger.getLogger(ReportPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private ResultSet createHoursDiffResultSet(int hourdiff, Connection con) throws SQLException{
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(INCOMING), SUM(OUTGOING), SUM(IN_PACKETS), SUM(OUT_PACKETS) FROM PUBLIC.JNA_TRAFFIC ");
        sql.append("WHERE DATEDIFF('hh',TIME,NOW) = ");
        sql.append(hourdiff);
        sql.append(" AND TIME < NOW");
        Statement stm  = con.createStatement();
        ResultSet rs = stm.executeQuery(sql.toString());
        stm.close();
        return rs;
    }
    private ResultSet createDaysDiffResultSet(int daydiff, Connection con) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(INCOMING), SUM(OUTGOING) FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append(" TIMESTAMPDIFF(SQL_TSI_DAY,TIME, ? ) = 0");
        PreparedStatement pst = con.prepareCall(sql.toString());
        //pst.setTimestamp(1, day);
        ResultSet rs = pst.executeQuery();
        pst.close();
        return rs;
    }


    private  ResultSet createThisDayTotalsResultSet(Connection con) throws SQLException{
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(INCOMING), SUM(OUTGOING), SUM(IN_PACKETS), SUM(OUT_PACKETS) FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append("TIMESTAMPDIFF(SQL_TSI_DAY, TIME, NOW) = 0 ;");
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(sql.toString());
        stm.close();
        return rs;
    }
    private JPanel createThisDayTotalPane(){
        FormLayout layout = new FormLayout("left:pref, max(150dlu;pref):grow, right:pref,20dlu, right:pref", "pref, 4dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu");
        CellConstraints cc = new CellConstraints();
        int row = 1;
        PanelBuilder builder = new PanelBuilder(layout);
        double incomingTraffics = 0.0d, outgoingTraffics = 0.0d;
        long incomingPackets = 0, outgoingPackets = 0;

        ResultSet rs;
        try {
            rs = this.createThisDayTotalsResultSet(Program.connection);
            if(rs.next()){
            incomingTraffics = rs.getDouble(1);
            outgoingTraffics = rs.getDouble(2);
            incomingPackets = rs.getLong(3);
            outgoingPackets = rs.getLong(4);
        }
        } catch (SQLException ex) {
        }
        builder.addLabel(resourceManager.getString("traffic"), cc.xy(3, row));
        builder.addLabel(resourceManager.getString("packet"), cc.xy(5, row));
        builder.addSeparator("", cc.xyw(3, row+1, 3));

        row +=2;
        builder.addLabel(resourceManager.getString("incoming"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(incomingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(incomingPackets), cc.xy(5, row));

        row +=2;
        builder.addLabel(resourceManager.getString("outgoing"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(outgoingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(outgoingPackets), cc.xy(5, row));

        row +=2;
        builder.addLabel(resourceManager.getString("bothDirections"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(incomingTraffics + outgoingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(incomingPackets + outgoingPackets), cc.xy(5, row));

        builder.setBorder(BorderFactory.createTitledBorder(resourceManager.getString("trafficToday")));
        return builder.getPanel();
    }

    private  ResultSet createLast24HoursTotalsResultSet(Connection con) throws SQLException{
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(INCOMING), SUM(OUTGOING), SUM(IN_PACKETS), SUM(OUT_PACKETS) FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append("TIMESTAMPDIFF(SQL_TSI_HOUR, TIME, NOW) <25 ;");
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(sql.toString());
        stm.close();
        return rs;
    }

    private JPanel createLast24HoursTotalsPane(){
        FormLayout layout = new FormLayout("left:pref, max(150dlu;pref):grow, right:pref,20dlu, right:pref", "pref, 4dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu");
        CellConstraints cc = new CellConstraints();
        int row = 1;
        PanelBuilder builder = new PanelBuilder(layout);
        double incomingTraffics = 0.0d, outgoingTraffics = 0.0d;
        long incomingPackets = 0, outgoingPackets = 0;
        ResultSet rs;
        try {
            rs = this.createLast24HoursTotalsResultSet(Program.connection);
            if(rs.next()){
            incomingTraffics = rs.getDouble(1);
            outgoingTraffics = rs.getDouble(2);
            incomingPackets = rs.getLong(3);
            outgoingPackets = rs.getLong(4);
        }
        } catch (SQLException ex) {
        }
        builder.addLabel(resourceManager.getString("traffic"), cc.xy(3, row));
        builder.addLabel(resourceManager.getString("packet"), cc.xy(5, row));
        builder.addSeparator("", cc.xyw(3, row+1, 3));

        row +=2;
        builder.addLabel(resourceManager.getString("incoming"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(incomingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(incomingPackets), cc.xy(5, row));

        row +=2;
        builder.addLabel(resourceManager.getString("outgoing"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(outgoingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(outgoingPackets), cc.xy(5, row));

        row +=2;
        builder.addLabel(resourceManager.getString("bothDirections"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(incomingTraffics + outgoingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(incomingPackets + outgoingPackets), cc.xy(5, row));

        builder.setBorder(BorderFactory.createTitledBorder(resourceManager.getString("trafficLast24h")));
        return builder.getPanel();
    }
  

    private  ResultSet createThisWeekTotalsResultSet(Connection con) throws SQLException{
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(INCOMING), SUM(OUTGOING), SUM(IN_PACKETS), SUM(OUT_PACKETS) FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append("TIMESTAMPDIFF(SQL_TSI_WEEK, TIME, NOW) = 0 AND YEAR(TIME) = YEAR(NOW);");
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(sql.toString());
        stm.close();
        return rs;
    }

    private JPanel createThisWeekTotalsPane(){
        FormLayout layout = new FormLayout("left:pref, max(150dlu;pref):grow, right:pref,20dlu, right:pref", "pref, 4dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu");
        CellConstraints cc = new CellConstraints();
        int row = 1;
        PanelBuilder builder = new PanelBuilder(layout);
        double incomingTraffics = 0.0d, outgoingTraffics = 0.0d;
        long incomingPackets = 0, outgoingPackets = 0;
        ResultSet rs;
        try {
            rs = this.createThisWeekTotalsResultSet(Program.connection);
            if(rs.next()){
            incomingTraffics = rs.getDouble(1);
            outgoingTraffics = rs.getDouble(2);
            incomingPackets = rs.getLong(3);
            outgoingPackets = rs.getLong(4);
        }
        } catch (SQLException ex) {
        }
        builder.addLabel(resourceManager.getString("traffic"), cc.xy(3, row));
        builder.addLabel(resourceManager.getString("packet"), cc.xy(5, row));
        builder.addSeparator("", cc.xyw(3, row+1, 3));

        row +=2;
        builder.addLabel(resourceManager.getString("incoming"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(incomingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(incomingPackets), cc.xy(5, row));

        row +=2;
        builder.addLabel(resourceManager.getString("outgoing"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(outgoingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(outgoingPackets), cc.xy(5, row));

        row +=2;
        builder.addLabel(resourceManager.getString("bothDirections"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(incomingTraffics + outgoingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(incomingPackets + outgoingPackets), cc.xy(5, row));

        builder.setBorder(BorderFactory.createTitledBorder(resourceManager.getString("weeklyTotals")));
        return builder.getPanel();
    }

    private ResultSet createThisMonthTotalsResultSet(Connection con) throws SQLException{
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(INCOMING), SUM(OUTGOING), SUM(IN_PACKETS), SUM(OUT_PACKETS) FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append("TIMESTAMPDIFF(SQL_TSI_MONTH, TIME, NOW) = 0 AND YEAR(TIME) = YEAR(NOW)");
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(sql.toString());
        stm.close();
        return rs;
    }

    private JPanel createThisMonthTotalsPane(){
        FormLayout layout = new FormLayout("left:pref, max(150dlu;pref):grow, right:pref,20dlu, right:pref", "pref, 4dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu");
        CellConstraints cc = new CellConstraints();
        int row = 1;
        PanelBuilder builder = new PanelBuilder(layout);
        double incomingTraffics = 0.0d, outgoingTraffics = 0.0d;
        long incomingPackets = 0, outgoingPackets = 0;
        ResultSet rs;
        try {
            rs = this.createThisMonthTotalsResultSet(Program.connection);
            if(rs.next()){
            incomingTraffics = rs.getDouble(1);
            outgoingTraffics = rs.getDouble(2);
            incomingPackets = rs.getLong(3);
            outgoingPackets = rs.getLong(4);
        }
        } catch (SQLException ex) {
        }
        builder.addLabel(resourceManager.getString("traffic"), cc.xy(3, row));
        builder.addLabel(resourceManager.getString("packet"), cc.xy(5, row));
        builder.addSeparator("", cc.xyw(3, row+1, 3));

        row +=2;
        builder.addLabel(resourceManager.getString("incoming"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(incomingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(incomingPackets), cc.xy(5, row));

        row +=2;
        builder.addLabel(resourceManager.getString("outgoing"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(outgoingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(outgoingPackets), cc.xy(5, row));

        row +=2;
        builder.addLabel(resourceManager.getString("bothDirections"), cc.xy(1, row));
        builder.addLabel(TrafficRendererUtils.renderTraffic(incomingTraffics + outgoingTraffics), cc.xy(3, row));
        builder.addLabel(String.valueOf(incomingPackets + outgoingPackets), cc.xy(5, row));

        builder.setBorder(BorderFactory.createTitledBorder(resourceManager.getString("monthlyTotals")));
        return builder.getPanel();
    }

    private ResultSet createTrafficSinceResultSet(Timestamp timestamp, Connection con) throws SQLException{
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(INCOMING), SUM(OUTGOING), SUM(IN_PACKETS), SUM(OUT_PACKETS) FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append("TIME >= ?");
        PreparedStatement pst = con.prepareCall(sql.toString());
        pst.setTimestamp(1, timestamp);
        ResultSet rs = pst.executeQuery();
        pst.close();
        return rs;
    }

    private ResultSet createSameHourResultSet(Timestamp hour, Connection con) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(INCOMING), SUM(OUTGOING), SUM(IN_PACKETS), SUM(OUT_PACKETS) FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append(" TIMESTAMPDIFF(SQL_TSI_HOUR,TIME, ? ) = 0");
        PreparedStatement pst = con.prepareCall(sql.toString());
        pst.setTimestamp(1, hour);
        ResultSet rs = pst.executeQuery();
        pst.close();
        return rs;
    }

    private ResultSet createSameDayResultSet(Timestamp day, Connection con) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(INCOMING), SUM(OUTGOING), SUM(IN_PACKETS), SUM(OUT_PACKETS) FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append(" TIMESTAMPDIFF(SQL_TSI_DAY,TIME, ? ) = 0");
        PreparedStatement pst = con.prepareCall(sql.toString());
        pst.setTimestamp(1, day);
        ResultSet rs = pst.executeQuery();
        pst.close();
        return rs;
    }
    private ResultSet createSameWeekResultSet(Timestamp day, Connection con) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(INCOMING), SUM(OUTGOING), SUM(IN_PACKETS), SUM(OUT_PACKETS) FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append(" TIMESTAMPDIFF(SQL_TSI_WEEK,TIME, ? ) = 0");
        PreparedStatement pst = con.prepareCall(sql.toString());
        pst.setTimestamp(1, day);
        ResultSet rs = pst.executeQuery();
        pst.close();
        return rs;
    }

    private ResultSet createSameMonthResultSet(Timestamp month, Connection con) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(INCOMING), SUM(OUTGOING), SUM(IN_PACKETS), SUM(OUT_PACKETS) FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append(" TIMESTAMPDIFF(SQL_TSI_MONTH,TIME, ? ) = 0");
        PreparedStatement pst = con.prepareCall(sql.toString());
        pst.setTimestamp(1, month);
        ResultSet rs = pst.executeQuery();
        pst.close();
        return rs;
    }

    private ResultSet createHourlyResultSet(Timestamp from, Timestamp to, Connection con) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TIME, INCOMING, OUTGOING, IN_PACKETS, OUT_PACKETS FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append(" TIME BETWEEN ? AND ? ");
        PreparedStatement pst = con.prepareCall(sql.toString());
        pst.setTimestamp(1, from);
        pst.setTimestamp(2, to);
        ResultSet rs = pst.executeQuery();
        pst.close();
        return rs;
    }
    private ResultSet createHourlyResultSet(Timestamp from, Connection con) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TIME, INCOMING, OUTGOING, IN_PACKETS, OUT_PACKETS FROM PUBLIC.JNA_TRAFFIC WHERE ");
        sql.append(" TIME > ? ");
        PreparedStatement pst = con.prepareCall(sql.toString());
        pst.setTimestamp(1, from);
        ResultSet rs = pst.executeQuery();
        pst.close();
        return rs;
    }







    private JToolBar createCategoryBar(){
            JToolBar categoryBar = new JToolBar(resourceManager.getString("reportCategory"), JToolBar.VERTICAL);
            categoryBar.setFloatable(false);

            
            buttonGroup = new ButtonGroup();
            todayButton = new JToggleButton(resourceManager.getString("today"), resourceManager.createImageIcon("today.png", "Today"));
            totalsButton = new JToggleButton(resourceManager.getString("totals"), resourceManager.createImageIcon("totals.png", "Totals"));
            trafficVolumeChartButton = new JToggleButton(resourceManager.getString("trafficVolumeChart"), resourceManager.createImageIcon("Bar Graph.png", "Traffic volume chart"));
            hourlyButton = new JToggleButton(resourceManager.getString("hourly"), resourceManager.createImageIcon("hourly.png", "Hourly report"));
            dailyButton = new JToggleButton(resourceManager.getString("daily"), resourceManager.createImageIcon("1day.png", "Hourly report"));
            weeklyButton = new JToggleButton(resourceManager.getString("weekly"), resourceManager.createImageIcon("7days.png", "Weekly report"));
            monthlyButton = new JToggleButton(resourceManager.getString("monthly"), resourceManager.createImageIcon("month.png", "Monthly report"));
            emailButton = new JToggleButton(resourceManager.getString("email"), resourceManager.createImageIcon("mail_post_to32.png", "Email"));

            todayButton.addItemListener(this);
            todayButton.setVerticalTextPosition(AbstractButton.BOTTOM);
            todayButton.setHorizontalTextPosition(AbstractButton.CENTER);
            todayButton.setActionCommand("today");
            totalsButton.addItemListener(this);
            totalsButton.setActionCommand("totals");
            totalsButton.setVerticalTextPosition(AbstractButton.BOTTOM);
            totalsButton.setHorizontalTextPosition(AbstractButton.CENTER);
            trafficVolumeChartButton.addItemListener(this);
            trafficVolumeChartButton.setVerticalTextPosition(AbstractButton.BOTTOM);
            trafficVolumeChartButton.setHorizontalTextPosition(AbstractButton.CENTER);
            trafficVolumeChartButton.setActionCommand("chart");
            hourlyButton.addItemListener(this);
            hourlyButton.setVerticalTextPosition(AbstractButton.BOTTOM);
            hourlyButton.setHorizontalTextPosition(AbstractButton.CENTER);
            hourlyButton.setActionCommand("hourly");
            dailyButton.addItemListener(this);
            dailyButton.setVerticalTextPosition(AbstractButton.BOTTOM);
            dailyButton.setHorizontalTextPosition(AbstractButton.CENTER);
            dailyButton.setActionCommand("daily");
            weeklyButton.addItemListener(this);
            weeklyButton.setVerticalTextPosition(AbstractButton.BOTTOM);
            weeklyButton.setHorizontalTextPosition(AbstractButton.CENTER);
            weeklyButton.setActionCommand("weekly");
            monthlyButton.addItemListener(this);
            monthlyButton.setVerticalTextPosition(AbstractButton.BOTTOM);
            monthlyButton.setHorizontalTextPosition(AbstractButton.CENTER);
            monthlyButton.setActionCommand("monthly");
            emailButton.addItemListener(this);
            emailButton.setVerticalTextPosition(AbstractButton.BOTTOM);
            emailButton.setHorizontalTextPosition(AbstractButton.CENTER);
            emailButton.setActionCommand("email");

            buttonGroup.add(todayButton);
            buttonGroup.add(totalsButton);
            buttonGroup.add(hourlyButton);
            buttonGroup.add(dailyButton);
            buttonGroup.add(weeklyButton);
            buttonGroup.add(monthlyButton);
            buttonGroup.add(emailButton);
            //buttonGroup.add(trafficVolumeChartButton);


            com.jgoodies.forms.builder.ButtonStackBuilder tba= new ButtonStackBuilder();
            tba.addGridded(todayButton);
            tba.addGridded(totalsButton);
            tba.addGridded(hourlyButton);
            tba.addGridded(dailyButton);
            tba.addGridded(weeklyButton);
            tba.addGridded(monthlyButton);
            tba.addGridded(emailButton);
            //tba.addGridded(trafficVolumeChartButton);

            tba.getLayout().setRowGroups(new int[][] {{1,2,3,4,5,6,7}});
            categoryBar.add(tba.getPanel(), BorderLayout.CENTER);
            todayButton.setSelected(true);
            return categoryBar;
        }

    public void itemStateChanged(ItemEvent e) {
        int state = e.getStateChange();
        String cmd = ((JToggleButton)e.getSource()).getActionCommand();
        if(state == ItemEvent.SELECTED){
            CardLayout cl = (CardLayout)cards.getLayout();
            cl.show(cards, cmd);
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
            return comp;
        }
    }

    class WeekCellRenderer extends SubstanceDefaultTableCellRenderer {
        private DateFormat dateFormat;
        public WeekCellRenderer(int style){
            super();
            this.setDateFormat(style);
        }
        public WeekCellRenderer(String pattern){
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
                Calendar cal = Calendar.getInstance();
                cal.setTime((Date)value);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                Date temp = cal.getTime();
                val = this.dateFormat.format(temp);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                temp = cal.getTime();
                val = val + " - " + this.dateFormat.format(temp);
            } else val = value.toString();
            if(comp instanceof JTextComponent){
                ((JTextComponent)comp).setText(val);
            } else if(comp instanceof JLabel) {
                ((JLabel)comp).setText(val);
            }
            return comp;
        }
    }

    class MonthCellRenderer extends SubstanceDefaultTableCellRenderer {
        private DateFormat dateFormat;
        public MonthCellRenderer(int style){
            super();
            this.setDateFormat(style);
        }
        public MonthCellRenderer(String pattern){
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
            return comp;
        }
    }

    class TrafficCellRenderer extends SubstanceDefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = TrafficRendererUtils.renderTraffic((Double)value);
            ((JLabel)comp).setText(text);
            ((JLabel)comp).setHorizontalTextPosition(JLabel.RIGHT);
            ((JLabel)comp).setHorizontalAlignment(JLabel.RIGHT);
            return comp;
        }
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
            if(table.getRowCount()>0){
                renderer = table.getCellRenderer(0, i);
                comp = renderer.getTableCellRendererComponent(table, table.getValueAt(0, i), true, true, 0, i);
                int width2 = (int) comp.getPreferredSize().width + 2*margin;
                if(width < width2) width = width2;
            }
            col.setPreferredWidth(width);
        }
    }
}

