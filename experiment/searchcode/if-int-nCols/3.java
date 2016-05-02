/*
 * TableOutputDialog.java
 *
 * Created on March 14, 2006, 8:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package kuhnlab.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author drjrkuhn
 */
public class TableOutputDialog extends JDialog {

    public JPanel buttonPanel;
    public JButton okButton;
    public JButton copyAllButton;
    public JButton copyAllTransposeButton;
    public JButton copySelectedButton;
    public JButton copySelectedTransposeButton;
    public JTable table;
    public JScrollPane tableScrollPane;
    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public TableOutputDialog(JFrame parent, String title, boolean isModal, Object[][] data) {
        this(parent, title, isModal);
        setData(data, null, true);
    }

    public TableOutputDialog(JFrame parent, String title, boolean isModal, Object[][] data, Object[] header) {
        this(parent, title, isModal);
        setData(data, header, true);
    }

    public TableOutputDialog(JFrame parent, String title, boolean isModal, String tabDelimTable, boolean hasHeader) {
        this(parent, title, isModal);
        try {
            BufferedReader reader = new BufferedReader(new StringReader(tabDelimTable));
            String line = reader.readLine().trim();
            String[] header = null;
            boolean parseHeaderRow = hasHeader;
            List<String[]> rowList = new ArrayList<String[]>();
            line = reader.readLine().trim();
            while (line != null && !line.equals("")) {
                String[] split = line.split("\t");
                if (parseHeaderRow) {
                    header = split;
                    parseHeaderRow = false;
                } else {
                    rowList.add(split);
                }
                line = reader.readLine().trim();
            }
            reader.close();
            setData((String[][]) rowList.toArray(), header, true);
        } catch (IOException ex) {
            initComponents(new DefaultTableModel(), true);
        }
    }

    public TableOutputDialog(JFrame parent, String title, boolean isModal, List<List<Object>> data) {
        this(parent, title, isModal);
        // convert list of lists to array of arrays
        int nRows = data.size();
        Object[][] rows = new Object[nRows][];
        for (int i = 0; i < nRows; i++) {
            rows[i] = data.get(i).toArray();
        }
        setData(rows, null, true);
    }

    public TableOutputDialog(JFrame parent, String title, boolean isModal, List<List<Object>> data, List<Object> header) {
        this(parent, title, isModal);
        // convert list of lists to array of arrays
        int nRows = data.size();
        Object[][] rows = new Object[nRows][];
        for (int i = 0; i < nRows; i++) {
            rows[i] = data.get(i).toArray();
        }
        setData(rows, header.toArray(), true);
    }

    /**
     * Common constructor.
     */
    protected TableOutputDialog(JFrame parent, String title, boolean isModal) {
        super(parent, isModal);
        setTitle(title);
        setLocation(new Point(parent.getWidth(), 0));
    }

    /**
     * Data initialization. Used by constructors.
     */
    protected void setData(Object[][] data, Object[] header, boolean boldFirstRow) {
        if (header == null) {
            int nCols = data[0].length;
            header = new String[nCols];
            Arrays.fill(header, " ");
        }
        TableModel model = new DefaultTableModel(data, header);
        initComponents(model, boldFirstRow);
    }

    protected void initComponents(TableModel tableModel, boolean boldFirstRow) {
        tableScrollPane = new JScrollPane();
        table = new JTable();
        buttonPanel = new JPanel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        table.setModel(tableModel);
        table.setColumnSelectionAllowed(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        final int COLWIDTH = 100;
        int ROWHEIGHT = table.getRowHeight();
        int maxShowColumns = Math.max(3, tableModel.getColumnCount());
        int width = COLWIDTH * maxShowColumns;
        int height = ROWHEIGHT * table.getRowCount();
        Dimension dim = new Dimension(width, height);
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(COLWIDTH);
        }
        //tableScrollPane.setPreferredSize(dim);
        //tableScrollPane.setSize(dim);
        table.setPreferredScrollableViewportSize(dim);
        //table.setSize(dim);
        if (boldFirstRow) {
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                Color normBack, selBack, normFore, selFore;

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (normBack == null && !isSelected) {
                        normBack = c.getBackground();
                        normFore = c.getForeground();
                    } else if (selBack == null && isSelected) {
                        selBack = c.getBackground();
                        selFore = c.getForeground();
                    }
                    if (row == 0) {
                        c.setBackground(normFore);
                        c.setForeground(normBack);
                    } else {
                        c.setBackground(isSelected ? selBack : normBack);
                        c.setForeground(isSelected ? selFore : normFore);
                    }
                    return c;
                }
            });
        }
        tableScrollPane.setViewportView(table);
        tableScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(SystemColor.controlDkShadow)));

        getContentPane().add(tableScrollPane, java.awt.BorderLayout.CENTER);

        copyAllButton = new JButton("Copy All");
        copyAllButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableModel data = table.getModel();
                StringBuffer tabData = new StringBuffer();
                int nCols = data.getColumnCount();
                int nRows = data.getRowCount();
                for (int r = 0; r < nRows; r++) {
                    for (int c = 0; c < nCols; c++) {
                        tabData.append(data.getValueAt(r, c).toString());
                        if (c < nCols - 1) {
                            tabData.append('\t');
                        }
                    }
                    tabData.append('\n');
                    StringSelection contents = new StringSelection(tabData.toString());
                    clipboard.setContents(contents, contents);
                }
            }
        });
        buttonPanel.add(copyAllButton);

        copyAllTransposeButton = new JButton("Copy All Pivot");
        copyAllTransposeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableModel data = table.getModel();
                StringBuffer tabData = new StringBuffer();
                int nCols = data.getColumnCount();
                int nRows = data.getRowCount();
                for (int c = 0; c < nCols; c++) {
                    for (int r = 0; r < nRows; r++) {
                        tabData.append(data.getValueAt(r, c).toString());
                        if (r < nRows - 1) {
                            tabData.append('\t');
                        }
                    }
                    tabData.append('\n');
                    StringSelection contents = new StringSelection(tabData.toString());
                    clipboard.setContents(contents, contents);
                }
            }
        });
        buttonPanel.add(copyAllTransposeButton);

        copySelectedButton = new JButton("Copy Selected");
        copySelectedButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableModel data = table.getModel();
                StringBuffer tabData = new StringBuffer();
                int nCols = data.getColumnCount();
                int nRows = data.getRowCount();
                int[] sel = table.getSelectedRows();
                if (sel.length < 1)
                    return;
                int[] copyRows = sel;
                if (sel[0] != 0) {
                    // add the header row if not selected
                    copyRows = Arrays.copyOf(sel, sel.length + 1);
                    copyRows[sel.length] = 0;
                }
                Arrays.sort(copyRows);
                for (int i = 0; i < copyRows.length; i++) {
                    int r = copyRows[i];
                    for (int c = 0; c < nCols; c++) {
                        tabData.append(data.getValueAt(r, c).toString());
                        if (c < nCols - 1) {
                            tabData.append('\t');
                        }
                    }
                    tabData.append('\n');
                    StringSelection contents = new StringSelection(tabData.toString());
                    clipboard.setContents(contents, contents);
                }
            }
        });
        buttonPanel.add(copySelectedButton);

        copySelectedTransposeButton = new JButton("Copy Selected Pivot");
        copySelectedTransposeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableModel data = table.getModel();
                StringBuffer tabData = new StringBuffer();
                int nCols = data.getColumnCount();
                int nRows = data.getRowCount();
                int[] sel = table.getSelectedRows();
                if (sel.length < 1)
                    return;
                int[] copyRows = sel;
                if (sel[0] != 0) {
                    // add the header row if not selected
                    copyRows = Arrays.copyOf(sel, sel.length + 1);
                    copyRows[sel.length] = 0;
                }
                Arrays.sort(copyRows);
                for (int c = 0; c < nCols; c++) {
                    for (int i = 0; i < copyRows.length; i++) {
                        int r = copyRows[i];
                        tabData.append(data.getValueAt(r, c).toString());
                        if (r < nRows - 1) {
                            tabData.append('\t');
                        }
                    }
                    tabData.append('\n');
                    StringSelection contents = new StringSelection(tabData.toString());
                    clipboard.setContents(contents, contents);
                }
            }
        });
        buttonPanel.add(copySelectedTransposeButton);
        
        okButton = new JButton("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });
        buttonPanel.add(okButton);


        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);
        pack();
        //Dimension dimBig = this.getSize();
        //this.setSize(width + 10, dimBig.height);
    }

    // Allow the ESCAPE key to close
    @Override
    protected JRootPane createRootPane() {
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        };
        JRootPane rootPane = new JRootPane();
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rootPane;
    }
}

