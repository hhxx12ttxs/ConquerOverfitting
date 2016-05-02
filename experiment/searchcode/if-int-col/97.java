package hongkongviewer;

import hongkong.RelationsNWrapper;
import hongkong.TuplesNWrapper;
import hongkongviewer.observer.Observer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

public class SwingRelationContent extends JPanel {
    
    private static final JFileChooser chooser = new JFileChooser();
    private static final int FILTER_ROW = 0;
    public final Observer<Notif> observer = Observer.newInstance(Notif.class);
    private final JTable table = new JTable();
    private final Model model = new Model();
    private final CardLayout card = new CardLayout();
    private final SwingRelationContentCell filterEditor = new SwingRelationContentCell();
    private TableRowSorter<Model> sorter;
    private final Filter filter = new Filter();
    private Object currentSelectedInstance = null;
    private final JButton btnSave = new JButton();

    private final Comparator<Object> filterAwareComparator = new Comparator<Object>() {
        
        private final Collator collator = Collator.getInstance();
        
        public int compare(Object o1, Object o2) {
            if(o1 instanceof FilterContent) {
                return isAscending() ? -1 : 1;
            }
            if(o2 instanceof FilterContent) {
                return isAscending() ? 1 : -1;
            }
            return collator.compare(o1.toString(), o2.toString());
        }

        private boolean isAscending() {
            SortOrder order = sorter.getSortKeys().get(0).getSortOrder();
            return (order == SortOrder.ASCENDING);
        }
    };

    
    private static class Filter extends RowFilter<Model, Integer> {

        private static class FilterDesc {
            public String filterText;
            public boolean fromStart;
        }
        
        private final Map<Integer, FilterDesc> filters = new HashMap<Integer, FilterDesc>();
        
        public void resetFilter() {
            filters.clear();
        }
        
        public void setColFilter(int col, String filter) {
            if(filter.isEmpty()) {
                filters.remove(col);
            } else {
                FilterDesc desc = new FilterDesc();
                if(filter.startsWith("*")) {
                    desc.filterText = filter.substring(1, filter.length());
                    desc.fromStart = false;
                } else {
                    desc.filterText = filter;
                    desc.fromStart = true;
                }
                filters.put(col, desc);                
            }
        }
        
        @Override
        public boolean include(Entry<? extends Model, ? extends Integer> entry) {
            if(entry.getIdentifier() == FILTER_ROW) {
                return true;
            }
            for(Map.Entry<Integer, FilterDesc> e : filters.entrySet()) {
                FilterDesc desc = e.getValue();
                String str = entry.getStringValue(e.getKey());
                if(desc.fromStart) {
                    if(! str.startsWith(desc.filterText)) {
                        return false;
                    }                                        
                } else {
                    if(! str.contains(desc.filterText)) {
                        return false;
                    }                    
                }
            }
            return true;
        }
        
    }
    
    public static interface Notif {
        void instanceSelected(Class<?> instanceClass, Object instance);
        void instanceDeselected();
    }
    
    public SwingRelationContent() {
        setLayout(card);
        JPanel tableAndMenu = new JPanel(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        tableAndMenu.add(sp, BorderLayout.CENTER);
        JToolBar bar = new JToolBar(SwingConstants.HORIZONTAL);
        bar.setBorderPainted(false);
        bar.setFloatable(false);
        bar.add(btnSave);
        tableAndMenu.add(bar, BorderLayout.PAGE_END);
        add(tableAndMenu, "table");
        btnSave.setIcon(new ImageIcon(SwingRelationContent.class.getResource("/hongkongviewer/save_button.gif")));
        JLabel lblNoRelation = new JLabel("no relation selected");
        lblNoRelation.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblNoRelation, "no_selection");
        card.show(this, "no_selection");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(true);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        ListSelectionListener listener = new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) {
                    int col = table.getSelectedColumn();
                    int row = table.getSelectedRow();
                    if(col == -1 || row == -1) {
                        observer.notif.instanceDeselected();
                    } else {
                        if(table.getSelectedRow() != FILTER_ROW) {
                            Object instance = model.getValueAt(table.convertRowIndexToModel(row), table.convertColumnIndexToModel(col));
                            Class colClass = model.getColumnClass(table.convertColumnIndexToModel(col));
                            if(instance != currentSelectedInstance) {
                                observer.notif.instanceSelected(colClass, instance);
                                currentSelectedInstance = instance;
                            }
                        }
                    }
                }
            }
        };
        table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
        table.getSelectionModel().addListSelectionListener(listener);
        table.setDefaultRenderer(Object.class, new SwingRelationContentCell());
        table.setDefaultEditor(Object.class, filterEditor);
        filterEditor.observer.addListener(new SwingRelationContentCell.Notif() {

            public void insert(int col, int offset, String strInserted, String strResult) {
                filter.setColFilter(col, strResult);
                sorter.sort();
            }

            public void changeOrRemove(int col, String strResult) {
                filter.setColFilter(col, strResult);
                sorter.sort();
            }
        });
        btnSave.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
    }

    private void save() {
        StringBuilder sb = new StringBuilder();
        int rowCount = table.getRowCount();
        int colCount = table.getColumnCount();
        for(int r = 1; r < rowCount; r++) {
            for(int c = 0; c < colCount; c++) {
                Object content = table.getValueAt(r, c);
                sb.append('"').append(content.toString()).append('"');
                if(c != colCount - 1) {
                    sb.append(',');
                }
            }
            sb.append('\n');
        }
        int resp = chooser.showSaveDialog(this);
        if(resp == JFileChooser.APPROVE_OPTION) {
            File saveFile = chooser.getSelectedFile();
            final String EXTENSION = ".csv";
            String name = saveFile.getName();
            if(! name.toLowerCase().endsWith(EXTENSION)) {
                saveFile = new File(saveFile.getParentFile(), name + EXTENSION);
            }
            write(saveFile, sb.toString());
        }
    }
    
    private void write(File saveFile, String content) {
        OutputStream o1 = null;
        Writer o2 = null;
        Writer o3 = null;
        try {
            o1 = new FileOutputStream(saveFile);
            o2 = new OutputStreamWriter(o1, Charset.forName("UTF-8"));
            o3 = new BufferedWriter(o2);
            o3.write(content);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Unable to write to file " + saveFile);
        } finally {
            Util.close(o3);
            Util.close(o2);
            Util.close(o1);            
        }
    }

    public void setRelation(RelationsNWrapper relation, List<Class<?>> colNames) {
        model.setRelation(relation, colNames);
        if(table.getModel() != model) {
            sorter = new TableRowSorter<Model>(model);
            sorter.setRowFilter(filter);
            table.setRowSorter(sorter);
            table.setModel(model);
        }
        for(int i = 0; i < model.getColumnCount(); i++) {
            sorter.setComparator(i, filterAwareComparator);
        }
        filter.resetFilter();
        sorter.sort();
        card.show(this, "table");
    }
    
    public void setNoRelation() {
        card.show(this, "no_selection");
    }
    
    private static class FilterContent implements Comparable {
        
        public static final FilterContent EMPTY = new FilterContent("");
        public final String str;

        public FilterContent(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str.toString();
        }

        public int compareTo(Object o) {
            return str.compareTo(o.toString());
        }
    }
    
    private static class Model extends AbstractTableModel {

        private RelationsNWrapper relation;
        private List<TuplesNWrapper> list;
        private List<Class<?>> colClasses;
        private List<FilterContent> filters = new ArrayList<FilterContent>();
        
        public void setRelation(RelationsNWrapper relation, List<Class<?>> colClasses) {
            if(relation.nbCols() != colClasses.size()) {
                throw new IllegalArgumentException("incompatible arguments");
            }
            this.relation = relation;
            this.list = new ArrayList<TuplesNWrapper>(relation.toSet());
            this.colClasses = colClasses;
            
            filters.clear();
            filters.addAll(Collections.nCopies(colClasses.size(), FilterContent.EMPTY));
            fireTableStructureChanged();
        }
        
        public int getRowCount() {
            return list.size()+1;
        }

        public int getColumnCount() {
            return relation.nbCols();
        }

        @Override
        public String getColumnName(int col) {
            return colClasses.get(col).getSimpleName();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return rowIndex == FILTER_ROW;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if(rowIndex == FILTER_ROW) {
                filters.set(columnIndex, new FilterContent(aValue.toString()));
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return colClasses.get(columnIndex);
        }

        public Object getValueAt(int row, int col) {
            if(row == FILTER_ROW) {
                return filters.get(col);
            }
            return list.get(row-1).get(col);
        }

    }
}

