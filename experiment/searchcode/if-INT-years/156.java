import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Vector;


public class Main_Form {
    private ArrayList<String> dims_list;
    private Connection connection = null;
    private int transactions = 0;

    private JComboBox first_dim;
    private JComboBox second_dim;
    private JButton olap_btn;
    private JTable olap_table;
    private JPanel main_panel;
    private JScrollPane olap_scroll;
    private JTabbedPane tabs;
    private JPanel olap_panel;
    private JPanel dm_panel;
    private JScrollPane dm_scroll;
    private JTable dm_table;
    private JLabel support_lbl;
    private JTextField support_percents_fld;
    private JButton dm_btn;
    private JButton dm_how_to_btn;
    private JButton olap_how_to_btn;
    private JLabel mnth_lbl;
    private JLabel year_lbl;
    private JList olap_mnths_list;
    private JList olap_years_list;
    private JTextField units_field;
    private JLabel field;


    private int new_dim_id(int current) {
        int id = current + 1;
        if (id == dims_list.size())
            id -= 2;
        return id;
    }



    private class FirstDimActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            if (first_dim.getSelectedIndex() == second_dim.getSelectedIndex())
                second_dim.setSelectedIndex(new_dim_id(first_dim.getSelectedIndex()));
            second_dim.repaint();
        }
    }



    private class SecondDimActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            if (first_dim.getSelectedIndex() == second_dim.getSelectedIndex())
               first_dim.setSelectedIndex(new_dim_id(second_dim.getSelectedIndex()));
            first_dim.repaint();
        }
    }



    private void rebuild_olap_table(ArrayList<ArrayList<String>> query_table) {
        DefaultTableModel model = new DefaultTableModel();
        olap_table.setModel(model);

        model.addColumn(first_dim.getSelectedItem());
        model.addColumn(second_dim.getSelectedItem());
        model.addColumn("Total Cost, $");

        DecimalFormat df = new DecimalFormat("###,###,###,###,###,###,###,###.##");
        for (ArrayList<String> row : query_table) {
            model.addRow(new Object[] {row.get(0), row.get(1), df.format(new Double(row.get(2)))} );
        }

        olap_table.repaint();
    }


    
    private String generate_WHERE() {
        String WHERE = "";
        int[] years,
              mnths;

        if (olap_mnths_list.getSelectedIndex() == 0)
            mnths = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        else
            mnths = olap_mnths_list.getSelectedIndices();

        if (olap_years_list.getSelectedIndex() == 0)
            years = new int[] {2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012};
        else {
            years = olap_years_list.getSelectedIndices();
            for (int i = 0; i < years.length; i++)
                years[i] += 1999;
        }

        for (int year : years) {
            for (int mnth : mnths) {
                WHERE += "Year=" + year + " and Month=" + mnth + " or ";
            }
        }
        
        return WHERE.substring(0, WHERE.length() - 3);
    }



    private class RollupActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            String query =
                "SELECT " +
                        "`" + first_dim.getSelectedItem() + "`, " +
                        "`" + second_dim.getSelectedItem() + "`, " +
                        "sum(`Total Cost`) AS `Total Cost` " +
                "FROM `warehouse`.`warehouse` " +
                "WHERE " +
                        generate_WHERE() +
                "GROUP BY " +
                        "`" + first_dim.getSelectedItem() + "`, " +
                        "`" + second_dim.getSelectedItem() + "` " +
                "WITH ROLLUP;";
            try {
                if (connection == null)
                    connection = DriverManager.getConnection("jdbc:mysql://localhost/warehouse", "expert", "expert");

                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                ArrayList<ArrayList<String>> query_table = new ArrayList<ArrayList<String>>();
                while (rs.next()) {
                    ArrayList<String> row = new ArrayList<String>();
                    row.add(rs.getString(1));
                    row .add(rs.getString(2));
                    row.add(rs.getString(3));
                    query_table.add(row);
                }

                rebuild_olap_table(query_table);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private class MonthsActionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent listSelectionEvent) {
            int[] selected = olap_mnths_list.getSelectedIndices();
            for (int item : selected)
                if (item == 0) olap_mnths_list.setSelectedIndex(0);

            if (selected.length == 12) olap_mnths_list.setSelectedIndex(0);
        }
    }



    private class YearsActionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent listSelectionEvent) {
            int[] selected = olap_years_list.getSelectedIndices();
            for (int item : selected)
                if (item == 0) olap_years_list.setSelectedIndex(0);

            if (selected.length == 13) olap_years_list.setSelectedIndex(0);
        }
    }



    private class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

        public MultiLineCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setFont(table.getFont());
            if (hasFocus) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                if (table.isCellEditable(row, column)) {
                    setForeground(UIManager.getColor("Table.focusCellForeground"));
                    setBackground(UIManager.getColor("Table.focusCellBackground"));
                }
            } else {
                setBorder(new EmptyBorder(1, 2, 1, 2));
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class DoubleComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Double dbl1 = Double.parseDouble((java.lang.String) o1);
            Double dbl2 = Double.parseDouble((java.lang.String) o2);
            return dbl1.compareTo(dbl2);
        }
    }


    private void rebuild_dm_table(Vector<String> products_sets, Vector<Double> supports_percents, Vector<Integer> supports_units) {
        DefaultTableModel model = new DefaultTableModel(){
            public Class getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        dm_table.setModel(model);
        model.addColumn("?");
        model.addColumn("Frequent Items");
        model.addColumn("Support, %");
        model.addColumn("Support, Units");

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
        dm_table.getColumnModel().getColumn(2).setCellRenderer( rightRenderer );
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment( JLabel.RIGHT );
        dm_table.getColumnModel().getColumn(3).setCellRenderer( leftRenderer );

        DecimalFormat df = new DecimalFormat("#.##");

        for (int i = 0; i < products_sets.size(); i++) {
            model.addRow(new Object[]{
                    Integer.toString(i + 1),
                    products_sets.get(i),
                    Double.valueOf(df.format(supports_percents.get(i))).toString(),
                    supports_units.get(i).toString()
            });
        }

        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<DefaultTableModel>(model);
        trs.setSortable(0, true);
        trs.setComparator(0, new DoubleComparator());
        trs.setSortable(1, false);
        trs.setSortable(2, true);
        trs.setComparator(2, new DoubleComparator());
        trs.setSortable(3, true);
        trs.setComparator(3, new DoubleComparator());
        dm_table.setRowSorter(trs);
        dm_table.repaint();
    }



    private class DMActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            double min_support = Double.parseDouble(support_percents_fld.getText());
            try {
                if (connection == null)
                    connection = DriverManager.getConnection("jdbc:mysql://localhost/warehouse", "expert", "expert");
                Statement stmt = connection.createStatement();
                Apriori.start(2, min_support, stmt);
                rebuild_dm_table(Apriori.get_candidates(), Apriori.get_supports_percents(), Apriori.get_supports_units());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class OlapHowToActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            JOptionPane.showMessageDialog(olap_panel,
                    "This part of program is provides the opportunity to build and execute an OLAP queries." +
                    "\nTo start working with it:" +
                    "\n   1) choose first and second dimensions;" +
                    "\n   2) choose years and months [Use Ctrl+LeftMouseButton to select more than one elements];" +
                    "\n   3) press button \"Analyse\" to execute query."
            );
        }
    }


    class DMHowToActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            JOptionPane.showMessageDialog(dm_panel,
                    "This part of program is provides the opportunity to analyse transactions to get a frequently product sets." +
                    "\nTo start working with it:" +
                    "\n   1) input support in percents [Input Format: X.XXXX];" +
                    "\n   2) press button \"Analyse\" to execute query;" +
                    "\n   3) click on header of column \"Support, %\" to sort results."
            );
        }
    }


    private int get_transactions(Statement stmt) {
        try {
            String query = "SELECT COUNT(id) FROM bills";
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            transactions = (int) (rs.getInt(1) / 100.0 * Double.parseDouble(support_percents_fld.getText()));
            return transactions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private class SupportActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent)  {
            try{
                get_transactions(connection.createStatement());
                units_field.setText(String.valueOf(transactions));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void createUIComponents() {
        try {
            if (connection == null)
                connection = DriverManager.getConnection("jdbc:mysql://localhost/warehouse", "expert", "expert");
        } catch(Exception e) {
            e.printStackTrace();
        }

        dims_list = new ArrayList<String>();
        dims_list.add("Product Name");
        dims_list.add("Product Type");
        dims_list.add("Product Color");
        dims_list.add("Product Consumer");
        dims_list.add("Market");

        this.first_dim = new JComboBox(dims_list.toArray());
        this.first_dim.setSelectedIndex(0);
        this.first_dim.addActionListener(new FirstDimActionListener());

        this.second_dim = new JComboBox(dims_list.toArray());
        this.second_dim.setSelectedIndex(1);
        this.second_dim.addActionListener(new SecondDimActionListener());

        this.olap_btn = new JButton();
        this.olap_btn.addActionListener(new RollupActionListener());

        DefaultTableModel olap_model = new DefaultTableModel();
        olap_model.addColumn("First Dimension");
        
        olap_model.addColumn("Second Dimension");
        olap_model.addColumn("Total Cost, $");
        olap_model.addRow(new Object[]{"N/A", "N/A", "N/A"});
        this.olap_table = new JTable(olap_model);

        this.olap_scroll = new JScrollPane();
        this.olap_scroll.add(this.olap_table);
        
        this.olap_mnths_list = new JList(new String[] {"All", "Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"});
        this.olap_mnths_list.setSelectedIndex(0);
        this.olap_mnths_list.addListSelectionListener(new MonthsActionListener());

        this.olap_years_list = new JList(new String[] {"All", "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012"});
        this.olap_years_list.setSelectedIndex(0);
        this.olap_years_list.addListSelectionListener(new YearsActionListener());
        
        this.olap_how_to_btn = new JButton();
        this.olap_how_to_btn.addActionListener(new OlapHowToActionListener());



        DefaultTableModel dm_model = new DefaultTableModel() {
            public Class getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        dm_model.addColumn("?");
        dm_model.addColumn("Frequent Items");
        dm_model.addColumn("Support, %");
        dm_model.addRow(new Object[] {"N/A", "N/A\nN/A", "N/A"});
        this.dm_table = new JTable(dm_model);
        this.dm_table.setRowHeight(this.dm_table.getRowHeight()*2);
        this.dm_table.setDefaultRenderer(String.class, new MultiLineCellRenderer());
        this.dm_table.setAutoCreateRowSorter(true);


        this.support_percents_fld = new JTextField();
        this.support_percents_fld.setText("1");
        this.support_percents_fld.addActionListener(new SupportActionListener());
        this.units_field = new JTextField("10");
        this.units_field.setEnabled(false);
        this.dm_btn = new JButton();
        this.dm_btn.addActionListener(new DMActionListener());

        this.dm_how_to_btn = new JButton();
        this.dm_how_to_btn.addActionListener(new DMHowToActionListener());
    }


    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Basic frame options
        JFrame frame = new JFrame("Decision Support Systems");
        frame.setPreferredSize(new Dimension(700, 520));
        frame.setContentPane(new Main_Form().main_panel);
        frame.setMinimumSize(new Dimension(700, 520));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);

        try {
            String driver = "com.mysql.jdbc.Driver";
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

