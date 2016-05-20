package fr.inria.eventloud.logs_analyzer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

/**
 * 
 * @author lpellegr
 */
public class LogsAnalyzerGUI {

    private JFrame frame;

    private final Action quitAction = new QuitAction();

    private JXTable jXTable;

    /**
     * Create the application.
     */
    public LogsAnalyzerGUI() {
        this.init();
    }

    /**
     * Initialize the contents of the frame.
     * 
     * @param entries
     */
    public void init() {
        this.frame = new JFrame();
        this.frame.setTitle("Social Filter Logs Analyzer");
        this.frame.setBounds(100, 100, 590, 330);
        this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        this.frame.getContentPane().add(menuBar, BorderLayout.NORTH);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmQuit = new JMenuItem("Quit");
        mntmQuit.setAction(this.quitAction);
        mnFile.add(mntmQuit);

        this.jXTable = this.createJXTable();
        this.configureJXTable(this.jXTable);

        // Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(this.jXTable);
        JComponent content = new JPanel(new BorderLayout());

        // Add the scroll pane to this panel.
        content.add(scrollPane, BorderLayout.CENTER);

        this.frame.getContentPane().add(content, BorderLayout.CENTER);
    }

    public void show() {
        this.frame.setMinimumSize(new Dimension(900, 500));
        this.frame.pack();
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
    }

    private JXTable createJXTable() {
        JXTable jtable = new JXTable();
        jtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtable.setRowHeight(25);
        jtable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable jtable = (JTable) e.getSource();
                    int rowIndex = jtable.getSelectedRow();

                    SocialFilterResult entry =
                            ((SocialFilterResultsTableModel) jtable.getModel()).getEntry(rowIndex);

                    JOptionPane.showMessageDialog(
                            frame,
                            "Graph=" + entry.getGraph() + "\nSubject="
                                    + entry.getSubject() + "\nPredicate="
                                    + entry.getPredicate() + "\nObject="
                                    + entry.getObject(),
                            "Last quadruple triggering the social filter verification",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        jtable.setModel(new SocialFilterResultsTableModel());

        return jtable;
    }

    public void update(List<SocialFilterResult> entries) {
        SocialFilterResultsTableModel tableModel =
                (SocialFilterResultsTableModel) this.jXTable.getModel();

        tableModel.clear();
        tableModel.loadData(entries);
        tableModel.fireTableStructureChanged();

        this.jXTable.packAll();
        this.frame.pack();
    }

    private void configureJXTable(JXTable jxTable) {
        // This shows the column control on the right-hand of the header.
        // All there is to it--users can now select which columns to view
        jxTable.setColumnControlVisible(true);

        // We'll add a highlighter to offset different row numbers
        // Note the setHighlighters() takes an array parameter; you can chain
        // these together.
        jxTable.setHighlighters(HighlighterFactory.createSimpleStriping());
        jxTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jxTable.setEditable(false);
        jxTable.packTable(20);
    }

    private static class SocialFilterResultsTableModel extends
            AbstractTableModel {
        private static final long serialVersionUID = 1L;

        private List<SocialFilterResult> entries;

        public void loadData(List<SocialFilterResult> entries) {
            Collections.sort(entries);
            this.entries = entries;
        }

        public void clear() {
            if (this.entries != null) {
                this.entries.clear();
            }
        }

        public SocialFilterResult getEntry(int rowIndex) {
            return this.entries.get(rowIndex);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Icon.class;
                case 1:
                    return Date.class;
                case 4:
                case 5:
                    return Double.class;
                default:
                    return super.getColumnClass(columnIndex);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "";
                case 1:
                    return "Date";
                case 2:
                    return "Source";
                case 3:
                    return "Destination";
                case 4:
                    return "Threshold";
                case 5:
                    return "Relationship Strengh";
                default:
                    throw new IllegalStateException("Unknown column index: "
                            + column);
            }
        }

        public int getColumnCount() {
            return 6;
        }

        public int getRowCount() {
            if (this.entries != null) {
                return this.entries.size();
            }

            return 0;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            SocialFilterResult entry = this.entries.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    String iconName = "accept";
                    if (entry.getRelationshipStrengh() < entry.getThreshold()) {
                        iconName = "exclamation";
                    }

                    return new ImageIcon(this.getClass().getResource(
                            "/" + iconName + ".png"));
                case 1:
                    return entry.getTimeOfOccurence();
                case 2:
                    return entry.getSource();
                case 3:
                    return entry.getDestination();
                case 4:
                    return entry.getThreshold();
                case 5:
                    return entry.getRelationshipStrengh();
                default:
                    throw new IllegalStateException("Unknown column index: "
                            + columnIndex);
            }
        }
    }

    private class DateCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (value instanceof Date) {
                String strDate =
                        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format((Date) value);
                this.setText(strDate);
            }

            return this;
        }
    }

    private class QuitAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public QuitAction() {
            this.putValue(NAME, "Quit");
            this.putValue(SHORT_DESCRIPTION, "Quit the application");
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

}

