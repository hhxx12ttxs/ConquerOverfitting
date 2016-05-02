package GUI_helpers;

import Database.DbConnect;
import java.awt.BorderLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class CustomJTable extends JDialog implements ActionListener, ListSelectionListener {
    public Object result;
    JPanel North = new JPanel(), Center = new JPanel(), South = new JPanel();
    JButton selecteer = new JButton("Selecteer rij");
    
    /** The <code>TableModel</code> of the table. */
    protected TableModel dataModel;

    /** The <code>ListSelectionModel</code> of the table, used to keep track of row selections. */
    protected ListSelectionModel selectionModel;
    
    
    //  Initialiseren van CustomJTable
    JTable jTable;
    //  De ID van de geselecteerde rij.
    private Object selectedID;
    //  De database connectie
    DbConnect dbc = new DbConnect();
    
    /**
     * Haal een CustomJTable op gevuld met de data die je mee geeft.
     * @param columnnames De namen van de kolommen.
     * @param columnsizes De grote van de kolommen. (evenveel opgeven als namen)
     * @param data De data uit de database.
     */
    public CustomJTable(final String[] columnnames, final int[] columnsizes, final Object[][] data) { 
        super();
        dataModel = new AbstractTableModel() {
            // Met de volgende functies wordt het tablemodel gemaakt, niets wijzigen
            /**
             * Haalt het aantal kolommen op
             */
            @Override
            public int getColumnCount() { return columnnames.length; }
            /**
             * Haalt het aantal rijen op
             */
            @Override
            public int getRowCount() { return data.length; }
            /**
             * Haalt een bepaalde waarde op van een rij en een kolom (beide als intergers opgeven)
             * @param row Interger van de rij
             * @param column Interger van de kolom
             */
            @Override
            public Object getValueAt(int row, int column) { return data[row][column]; }
            /**
             * Haal de kolomnaam op
             * @param column Interger van de kolom
             */
            @Override
            public String getColumnName(int column) { return columnnames[column]; }
            /**
             * Zet een waarde op de plaats van een rij en een kolom (beide als intergers opgeven)
             * @param value Waarde die geplaatst moet worden
             * @param row Interger van de rij van de plaats
             * @param colum Interger van de kolom van de plaats
             */
            @Override
            public void setValueAt(Object value, int row, int column) { data[row][column] = value; }
        };
        this.setSize(800, 600);
        this.setModal(true);
        this.setLayout(new BorderLayout());
        
        // Vult de tabel aan
        jTable = new javax.swing.JTable(dataModel);
        jTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        // Implementeer de grote van de kolommen
        for (int i = 0; i <= (dataModel.getColumnCount() - 1); i++) {
            jTable.getColumnModel().getColumn(i).setPreferredWidth(columnsizes[i]);
        }
        
        // Voegt de tabel toe aan het panel in een scrollpane
        this.add(new JScrollPane(jTable));
        
        ListSelectionModel listMod;
        // Dit is de list selectioner, die kijkt of je iets selecteert
        listMod = jTable.getSelectionModel();
        // Hierdoor kan je maar 1 regel selecteren
        listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Voegt de listener toe aan het frame
        listMod.addListSelectionListener(this);

        Center.add(new JScrollPane(jTable));
        South.add(selecteer);
        selecteer.addActionListener(this);
        
        this.add(North, BorderLayout.NORTH);
        this.add(Center, BorderLayout.CENTER);
        this.add(South, BorderLayout.SOUTH);
        this.setVisible(true);
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        // Pakt de tablemodel van jTable
        TableModel tm = jTable.getModel();
        // Bepaalt de geselecteerde rij en vult een array met alle waardes
        int[] selRows = jTable.getSelectedRows();
        // Dit vult selectedID
        this.result =  tm.getValueAt(selRows[0],0);
    }
    
    public TableModel getModel() {
        return dataModel;
    }
    
    
    /**
     * Returns the indices of all selected rows.
     *
     * @return an array of integers containing the indices of all selected rows,
     *         or an empty array if no row is selected
     * @see #getSelectedRow
     */
    public int[] getSelectedRows() {
        int iMin = selectionModel.getMinSelectionIndex();
        int iMax = selectionModel.getMaxSelectionIndex();

        if ((iMin == -1) || (iMax == -1)) {
            return new int[0];
        }

        int[] rvTmp = new int[1+ (iMax - iMin)];
        int n = 0;
        for(int i = iMin; i <= iMax; i++) {
            if (selectionModel.isSelectedIndex(i)) {
                rvTmp[n++] = i;
            }
        }
        int[] rv = new int[n];
        System.arraycopy(rvTmp, 0, rv, 0, n);
        return rv;
    }
    
    public ListSelectionModel getSelectionModel() {
        return selectionModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selecteer) {
            this.setVisible(false);
            dispose();
        }
    }
}

