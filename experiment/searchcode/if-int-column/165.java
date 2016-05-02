/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RepositoryUIMainPanelView.java
 *
 * Created on 17-jun-2011, 11:02:31
 */
package chamiloda.gui.implementations.repository;

import chamiloda.gui.implementations.RepositoryUI;
import chamiloda.domain.*;
import chamiloda.domain.utilities.*;
import chamiloda.domain.contentobjects.*;
import chamiloda.gui.toolset.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 *
 * @author mmeu164
 */
public class GeneralViewBottom extends javax.swing.JPanel implements TableButtonActionListener, MouseMotionListener
{

    private final String [] HEADERS = { "", "title", "description"};
    private final String [] EDIT_BUTTONS = 
        {
            RepositoryUI.ACTION_DELETE
        };
    private String[] headers = null;
    private Boolean[] editables = null;
    
    private RepositoryUI parentUI;
    private ContentObjectImplementation viewedObject = null;
    private ComplexContentObjectImplementation viewedComplexObject = null;
    private int rowHeight;
    private int noRowsHeight;
            
    public GeneralViewBottom()
    {
        initComponents();
        // makes sure you can scroll through the verticalJComponentContainer
        // with the mouse wheel. The scrol pane doesn't need it anyway; this
        // view component adapts to its size.
        for (MouseWheelListener m : jScrollPane2.getMouseWheelListeners())
            jScrollPane2.removeMouseWheelListener(m);
        for (MouseWheelListener m : jScrollPane3.getMouseWheelListeners())
            jScrollPane3.removeMouseWheelListener(m);

        tableChildren.addMouseMotionListener(this);
        tableParents.addMouseMotionListener(this);
        // code to ensure the component takes optimal minimum height
        // (minimum without causing scroll bars in the tables)
        rowHeight = Math.max(this.tableChildren.getRowHeight(), this.tableParents.getRowHeight());
        // original GUI builder code has 4 rows
        noRowsHeight = super.getPreferredSize().height - rowHeight*4;
    }
    
    @Override
    public Dimension getPreferredSize()
    {
        // code to ensure the component takes optimal minimum height
        // (minimum without causing scroll bars in the tables)
        Dimension dim = super.getPreferredSize();
        // The tables are in a single tabbed pane, so they're 'parallel'.
        // Use the maximum needed.
        int rows = Math.max(tableChildren.getRowCount(), tableParents.getRowCount());
        rows = Math.max(rows, 1);
        int height = noRowsHeight + rows*rowHeight;
        Dimension newPrefSize = new Dimension((int)dim.width, height);
        Dimension newSize = new Dimension(this.getWidth(), height);
        this.setSize(newSize);
        return newPrefSize;
    }
    
    public RepositoryUI getParentUI()
    {
        return parentUI;
    }

    public void setParentUI(RepositoryUI parentUI)
    {
        this.parentUI = parentUI;
    }

    private void setHeaders()
    {
        // same for children and parents table
        LanguageController lc = LanguageController.getInstance();
        headers = new String[HEADERS.length + EDIT_BUTTONS.length];
        editables = new Boolean[HEADERS.length + EDIT_BUTTONS.length];
        Arrays.fill(editables, true);
        // translated headers for the information. Not editable.
        for (int i = 0; i < HEADERS.length; i++)
        {
            headers[i] = lc.getString(HEADERS[i]);
            editables[i] = false;
        }
        // empty headers for the editing buttons. Buttons need to be editable
        for (int i = HEADERS.length; i < HEADERS.length + EDIT_BUTTONS.length; i++)
        {
            headers[i] = "";
            editables[i] = true;
        }
    }
    
    private void fillChildrenTable()
    {
        List<ComplexContentObjectImplementation> children;
        if (viewedObject == null || viewedComplexObject != null)
            children = new ArrayList<ComplexContentObjectImplementation>();
        else
            children=viewedObject.getChildren();
        Object[][] data = new Object[children.size()][];
        for (int i = 0; i < children.size(); i++)
        {
            ComplexContentObjectImplementation ccoi = children.get(i);
            data[i] = new Object[HEADERS.length + EDIT_BUTTONS.length];
            data[i][0]= ccoi.getEncapsulatedObject().getType();
            data[i][1]= ccoi.getEncapsulatedObject().getTitle();
            data[i][2]= ccoi.getEncapsulatedObject().getDescription();
            // insert commands
            System.arraycopy(EDIT_BUTTONS, 0, data[i], HEADERS.length, EDIT_BUTTONS.length);
        }
        editables[1]=true;
        tableChildren.setModel(new CustomTableModel(data, headers, editables));

        // type icon
        makeIconColumn(tableChildren, this, 0);
        for (int i = 1; i < HEADERS.length; i++)
        {
            this.makeClickableColumn(tableChildren, this, i);
        }
        this.makeClickableColumn(tableChildren, this, 1);
        // icon column
        for (int i = HEADERS.length; i < HEADERS.length + EDIT_BUTTONS.length; i++)
        {
            this.makeIconColumn(tableChildren , this, i);
        }
    }

    
    private void fillParentsTable()
    {
        List<ContentObjectImplementation> parents;
        
        if (viewedComplexObject != null)
        {
            parents = new ArrayList<ContentObjectImplementation> ();
            ContentObjectImplementation par = 
                    DomainController.getInstance().getMapper().getParent(viewedComplexObject);
            if (par!=null)
                parents.add(par);
        }
        else if (viewedObject == null)
            parents = new ArrayList<ContentObjectImplementation>();
        else
            parents = DomainController.getInstance().getMapper().getParents(this.viewedObject);
        Object[][] data = new Object[parents.size()][];
        for (int i = 0; i < parents.size(); i++)
        {
            ContentObjectImplementation coi = parents.get(i);
            data[i] = new Object[HEADERS.length + EDIT_BUTTONS.length];
            data[i][0]= coi.getType();
            data[i][1]= coi.getTitle();
            data[i][2]= coi.getDescription();
            // insert commands
            System.arraycopy(EDIT_BUTTONS, 0, data[i], HEADERS.length, EDIT_BUTTONS.length);
        }
        editables[1]=true;
        tableParents.setModel(new CustomTableModel(data, headers, editables));

        // type icon
        makeIconColumn(tableParents, this, 0);
        for (int i = 1; i < HEADERS.length; i++)
        {
            this.makeClickableColumn(tableParents, this, i);
        }
        this.makeClickableColumn(tableParents, this, 1);
        // icon column
        for (int i = HEADERS.length; i < HEADERS.length + EDIT_BUTTONS.length; i++)
        {
            this.makeIconColumn(tableParents , this, i);
        }
    }    

    private void makeClickableColumn(JTable table, TableButtonActionListener parent, int columnNumber)
    {
        TableColumn column = table.getColumnModel().getColumn(columnNumber);
        column.setCellRenderer(new TableButtonRenderer(parent, false, true, false, false, WIDTH, WIDTH));
        column.setCellEditor(new TableButtonEditor(parent, false, true, false, false, WIDTH, WIDTH));
    }

    private void makeIconColumn(JTable table, TableButtonActionListener parent,int columnNumber)
    {
        TableColumn column = table.getColumnModel().getColumn(columnNumber);
        column.setCellRenderer(new TableButtonRenderer(parent, true, false, true, true, 15, 15));
        column.setCellEditor(new TableButtonEditor(parent, true, false, true, true, 15, 15));
        column.setMinWidth(20);
        column.setMaxWidth(20);
        column.setPreferredWidth(20); 
    }    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPaneLinks = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableParents = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableChildren = new javax.swing.JTable();

        tabbedPaneLinks.setName(""); // NOI18N

        jScrollPane2.setName("[parents]"); // NOI18N

        tableParents.setModel(new javax.swing.table.DefaultTableModel(
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
        tableParents.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(tableParents);

        tabbedPaneLinks.addTab("parents", jScrollPane2);

        jScrollPane3.setName("[children]"); // NOI18N

        tableChildren.setModel(new javax.swing.table.DefaultTableModel(
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
        tableChildren.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(tableChildren);

        tabbedPaneLinks.addTab("children", jScrollPane3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPaneLinks, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPaneLinks, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane tabbedPaneLinks;
    private javax.swing.JTable tableChildren;
    private javax.swing.JTable tableParents;
    // End of variables declaration//GEN-END:variables

    public void update()
    {
        StringsResetter.resetStrings(this);
        Object obj = parentUI.getSelectedObject();
        if (obj instanceof ContentObjectImplementation)
        {
            this.viewedObject = (ContentObjectImplementation)obj;
            this.viewedComplexObject = null;
        }
        else if (obj instanceof ComplexContentObjectImplementation)
        {
            this.viewedComplexObject = (ComplexContentObjectImplementation)obj;
            this.viewedObject = viewedComplexObject.getEncapsulatedObject();
        }
        else
            this.viewedObject = null;

        this.fillInfo();
    }

    private void fillInfo()
    {
        this.setHeaders();
        fillChildrenTable();
        fillParentsTable();
        selectPreferredTab();
    }

    @Override
    public void processCommand(int row, int column, JTable sender)
    {
        if (this.viewedObject == null)
            return;
        
        if (sender.equals(this.tableChildren))
        {
            processClickChildTable(row, column);
        }
        else if (sender.equals(this.tableParents))
        {
            processClickParentTable(row, column);
        }
    }

    @Override
    public boolean checkIfEnabled(int row, int column, JTable sender)
    {
        // unlink button is always enabled: things that can't be unlinked
        // wouldn't be in the table in the first place.
        return true;
    }
    
    private void processClickChildTable(int row, int column)
    {
        List<ComplexContentObjectImplementation> children
                = this.viewedObject.getChildren();
        if (row >= children.size())
            return;
        ComplexContentObjectImplementation ccoi = children.get(row);
        String action = null;
        if (column < HEADERS.length)
        {
            parentUI.setSelectedObject(ccoi.getEncapsulatedObject(), null);
            return;
        }
        else
        {
            action = EDIT_BUTTONS[column-HEADERS.length];
        }
        if (action !=null && action.equals(RepositoryUI.ACTION_DELETE))
            parentUI.getMenuActionExecutor().executeAction(viewedObject, RepositoryUI.ACTION_UNLINK_CHILD, ccoi);
    }
    
    private void processClickParentTable(int row, int column)
    {
        DomainController dc = DomainController.getInstance();
        ContentObjectImplementation parent;
        if (viewedComplexObject == null)
        {
            
            List<ContentObjectImplementation> parents = 
                    dc.getMapper().getParents(this.viewedObject);
            if (row >= parents.size())
                return;
            parent = parents.get(row);
        }
        else
        {
            parent = dc.getMapper().getParent(this.viewedComplexObject);
        }

        String action = null;
        if (column < HEADERS.length)
        {
            parentUI.setSelectedObject(parent, null);
            return;
        }
        else
        {
            action = EDIT_BUTTONS[column-HEADERS.length];
        }
        if (action != null && action.equals(RepositoryUI.ACTION_DELETE))
                parentUI.getMenuActionExecutor().executeAction(viewedObject, RepositoryUI.ACTION_UNLINK_FROM_PARENT, parent);
    }
    
    private void selectPreferredTab()
    {
        if (viewedObject == null)
        {
            tabbedPaneLinks.setSelectedIndex(0);
            return;
        }
        int parentsIndex=0;
        int childrenIndex=0;
        for (int i = 0; i < tabbedPaneLinks.getComponentCount(); i++)
        {
            Component tabcomp = tabbedPaneLinks.getComponentAt(i);
            if (tabcomp instanceof Container)
            {
                if (GuiUtilities.containsComponent((Container)tabcomp, tableParents))
                    parentsIndex = i;
                else if (GuiUtilities.containsComponent((Container)tabcomp, tableChildren))
                    childrenIndex = i;
            }
        }
        boolean hasChildren = viewedObject.getChildren().size() > 0;
        boolean hasParents = DomainController.getInstance().getMapper().isLinkedChild(viewedObject);
        
        if (hasChildren && !hasParents)
            tabbedPaneLinks.setSelectedIndex(childrenIndex);
        else if (hasParents && !hasChildren)
            tabbedPaneLinks.setSelectedIndex(parentsIndex);
        else // no preference
            tabbedPaneLinks.setSelectedIndex(0);
    }

    @Override
    public void mouseDragged(MouseEvent e) { }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        // both tables use this same function. See which tab is selected.
        Component tabcomp = tabbedPaneLinks.getSelectedComponent();
        JTable currentTable=null;
        if (!(tabcomp instanceof Container))
            return;
        if (GuiUtilities.containsComponent((Container)tabcomp, tableParents))
            currentTable = tableParents;
        if (GuiUtilities.containsComponent((Container)tabcomp, tableChildren))
            currentTable = tableChildren;
        
        // Tabbed pane could be made to contain more tabs,
        // so check if a specific table was found.
        if (currentTable != null)
        {
            int col = currentTable.columnAtPoint(e.getPoint());
            int row = currentTable.rowAtPoint(e.getPoint());
            if (col >= 0 && editables[col] && checkIfEnabled(row, col, currentTable))
            {
                currentTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            else
                currentTable.setCursor(null);
        }
    }
    
    public JTabbedPane getTabbedPaneLinks()
    {
        return tabbedPaneLinks;
    }

}

