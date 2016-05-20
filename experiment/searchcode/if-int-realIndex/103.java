package uk.ac.ebi.pride.gui.component.table;

import org.jdesktop.swingx.search.SearchFactory;
import uk.ac.ebi.pride.gui.GUIUtilities;
import uk.ac.ebi.pride.gui.component.table.listener.EntryUpdateSelectionListener;
import uk.ac.ebi.pride.gui.component.table.model.ListTableModel;
import uk.ac.ebi.pride.gui.component.table.model.SpectrumTableModel;
import uk.ac.ebi.pride.gui.component.table.renderer.RowNumberRenderer;
import uk.ac.ebi.pride.gui.component.table.sorter.NumberTableRowSorter;
import uk.ac.ebi.pride.gui.desktop.Desktop;
import uk.ac.ebi.pride.gui.desktop.DesktopContext;
import uk.ac.ebi.pride.gui.utils.Constants;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;

/**
 * DefaultPrideTable set the default settings for all tables in pride inspector.
 * <p/>
 * User: rwang
 * Date: 11-Sep-2010
 * Time: 13:35:30
 */
public class DefaultPrideTable extends AlterRowColorTable implements ActionListener {
    /**
     * Action command for copy cell
     */
    private static final String COPY_CELL_ACTION = "Copy Cell";

    /**
     * Action command for copy row
     */
    private static final String COPY_ROW_ACTION = "Copy Row";
    /**
     * Action command for select all
     */
    private static final String SELECT_ALL_ACTION = "Select All";

    /**
     * Action command for deselect all
     */
    private static final String DESELECT_ALL_ACTION = "Deselect All";

    /**
     * Action command for search table
     */
    private static final String FIND = "find";

    /**
     * popup menu for copy and paste
     */
    private JPopupMenu popMenu;

    /**
     * System clipboard
     */
    private Clipboard clipboard;

    /**
     * row where mouse clicked
     */
    private int rowByMouse;

    /**
     * column where mouse clicked
     */
    private int columnByMouse;

    public DefaultPrideTable(TableModel dm) {
        this(dm, new DefaultTableColumnModel());
    }

    @SuppressWarnings("unchecked")
    public DefaultPrideTable(TableModel tableModel, TableColumnModel tableColumnModel) {
        super();

        if (tableColumnModel != null) {
            this.setColumnModel(tableColumnModel);
        }

        if (tableModel != null) {
            this.setModel(tableModel);
        }

        // selection mode
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // set column control visible
        setColumnControlVisible(true);

        // auto fill
        setFillsViewportHeight(true);

        // sorter
        setRowSorter(new NumberTableRowSorter(tableModel));

        // row height
        setRowHeight(20);

        // add entry selection listener, this set the default selection for the table
        tableModel.addTableModelListener(new EntryUpdateSelectionListener(this));

        // set row number
        TableColumn rowNumColumn = getColumn(SpectrumTableModel.TableHeader.ROW_NUMBER_COLUMN.getHeader());
        int rowColumnNum = rowNumColumn.getModelIndex();
        rowNumColumn.setCellRenderer(new RowNumberRenderer());
        getColumnModel().getColumn(rowColumnNum).setMaxWidth(40);
        setOmitColumn(rowColumnNum);

        // prevent dragging of column
        getTableHeader().setReorderingAllowed(false);

        // remvoe border
        setBorder(BorderFactory.createEmptyBorder());

        // clipboard
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // popup menu
        createPopupMenu();
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex =
                        columnModel.getColumn(index).getModelIndex();
                ListTableModel tableModel = (ListTableModel) DefaultPrideTable.this.getModel();
                return tableModel.getColumnTooltip(realIndex);
            }
        };
    }

    /**
     * create a popup menu for the table
     */
    private void createPopupMenu() {
        // pride inspector context
        DesktopContext context = Desktop.getInstance().getDesktopContext();

        // create a popup menu
        popMenu = new JPopupMenu();

        // search table
        JMenuItem findItem = new JMenuItem(context.getProperty("search.table.title"),
                GUIUtilities.loadIcon(context.getProperty("search.table.small.icon")));
        findItem.setActionCommand(FIND);
        findItem.addActionListener(this);
        popMenu.add(findItem);

        // separator
        popMenu.add(new JSeparator());

        // select all rows
        JMenuItem selectAllItem = new JMenuItem(context.getProperty("select.all.title"));
        selectAllItem.setActionCommand(SELECT_ALL_ACTION);
        selectAllItem.addActionListener(this);
        popMenu.add(selectAllItem);

        // deselect all rows
        JMenuItem deselectAllItem = new JMenuItem(context.getProperty("deselect.all.title"));
        deselectAllItem.setActionCommand(DESELECT_ALL_ACTION);
        deselectAllItem.addActionListener(this);
        popMenu.add(deselectAllItem);

        // add copy cell menu item
        JMenuItem copyCellItem = new JMenuItem(context.getProperty("copy.cell.title"),
                GUIUtilities.loadIcon(context.getProperty("copy.cell.small.icon")));
        copyCellItem.setActionCommand(COPY_CELL_ACTION);
        copyCellItem.addActionListener(this);
        popMenu.add(copyCellItem);

        // add copy row menu item
        JMenuItem copyRowItem = new JMenuItem(context.getProperty("copy.row.title"),
                GUIUtilities.loadIcon(context.getProperty("copy.row.small.icon")));
        copyRowItem.setActionCommand(COPY_ROW_ACTION);
        copyRowItem.addActionListener(this);
        popMenu.add(copyRowItem);

        // add popup menu listener
        PopupListener listener = new PopupListener();
        this.addMouseListener(listener);

        // listener to ctrl + c from keyboard
        registerKeyboardStroke();
    }

    /**
     * Register keyboard action, listens to ctrl+C to copy rows
     */
    private void registerKeyboardStroke() {
        // copy key stroke
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);

        // register key stroke
        this.registerKeyboardAction(this, COPY_ROW_ACTION, copy, JComponent.WHEN_FOCUSED);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String evtCmd = e.getActionCommand();

        if (COPY_CELL_ACTION.equals(evtCmd)) {
            copyCellToClipboard(rowByMouse, columnByMouse);
        } else if (COPY_ROW_ACTION.equals(evtCmd)) {
            copyRowsToClipboard(getSelectedRows(), getAllColumns());
        } else if (SELECT_ALL_ACTION.equals(evtCmd)) {
            selectAllRows();
        } else if (DESELECT_ALL_ACTION.equals(evtCmd)) {
            deselectAllRows();
        } else if (FIND.equals(evtCmd)) {
            SearchFactory.getInstance()
                        .showFindInput(this , getSearchable());
        }
    }

    private void selectAllRows() {
        int rowCnt = getRowCount();
        // todo: EDT ???
        this.getSelectionModel().setSelectionInterval(0, rowCnt);
    }

    private void deselectAllRows() {
        this.clearSelection();
    }


    /**
     * Copy cell value to clipboard
     *
     * @param row row number
     * @param col column number
     */
    private void copyCellToClipboard(int row, int col) {
        // get the value selected by mouse
        String str = getCellStringValue(row, col);

        // add to clipboard
        StringSelection strSelection = new StringSelection(str);
        clipboard.setContents(strSelection, strSelection);
    }

    /**
     * Copy specified rows to clipboard
     *
     * @param rows a array of row numbers
     * @param cols a array of column numbers
     */
    private void copyRowsToClipboard(int[] rows, int[] cols) {
        // string builder to store the values
        StringBuilder strBuilder = new StringBuilder();

        if (rows != null && cols != null) {
            // row count
            int rowCnt = rows.length;

            // column count
            int colCnt = cols.length;

            // iterate over all the selected rows, append all the values
            for (int i = 0; i < rowCnt; i++) {
                if (i < this.getRowCount()) {
                    for (int j = 0; j < colCnt; j++) {
                        if (j < this.getColumnCount()) {
                            // cell string value
                            String str = getCellStringValue(rows[i], cols[j]);

                            // append the value to string builder
                            strBuilder.append(str);
                            if (j < colCnt - 1) {
                                strBuilder.append(Constants.TAB);
                            }
                        }
                    }
                    strBuilder.append(Constants.LINE_SEPARATOR);
                }
            }
        }

        // add to clipboard
        StringSelection strSelection = new StringSelection(strBuilder.toString());
        clipboard.setContents(strSelection, strSelection);
    }

    /**
     * Get cell string value using row number and column number
     *
     * @param row row number
     * @param col column number
     * @return String   cell string value
     */
    private String getCellStringValue(int row, int col) {
        // get the value selected by mouse
        Object value = getValueAt(row, col);

        // get output string
        String str = "";
        if (value != null) {
            str = value.toString();
        }

        return str;
    }

    /**
     * Get a list of indexes for all columns
     *
     * @return int[]    a list of column indexes
     */
    private int[] getAllColumns() {
        int colCnt = getColumnCount();
        int[] cols = new int[colCnt];
        for (int i = 0; i < colCnt; i++) {
            cols[i] = i;
        }
        return cols;
    }

    /**
     * Listen to mouse click and show popup menu
     */
    private class PopupListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popMenu.show(e.getComponent(),
                        e.getX(), e.getY());
                Point clickPoint = new Point(e.getX(), e.getY());
                rowByMouse = rowAtPoint(clickPoint);
                columnByMouse = columnAtPoint(clickPoint);
            }
        }
    }

}

