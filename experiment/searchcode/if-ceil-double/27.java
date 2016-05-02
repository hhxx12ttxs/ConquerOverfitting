// Near Infinity - An Infinity Engine Browser and Editor
// Copyright (C) 2001 - 2005 Jon Olav Hauglid
// See LICENSE.txt for license information

package infinity.gui;

import infinity.NearInfinity;
import infinity.datatype.Datatype;
import infinity.datatype.Editable;
import infinity.datatype.EffectType;
import infinity.datatype.HexNumber;
import infinity.datatype.InlineEditable;
import infinity.datatype.ResourceRef;
import infinity.datatype.SectionCount;
import infinity.datatype.TextString;
import infinity.datatype.Unknown;
import infinity.datatype.UnknownBinary;
import infinity.datatype.UnknownDecimal;
import infinity.icon.Icons;
import infinity.resource.AbstractStruct;
import infinity.resource.AddRemovable;
import infinity.resource.HasAddRemovable;
import infinity.resource.HasDetailViewer;
import infinity.resource.Resource;
import infinity.resource.ResourceFactory;
import infinity.resource.StructEntry;
import infinity.resource.Viewable;
import infinity.resource.dlg.AbstractCode;
import infinity.resource.dlg.DlgResource;
import infinity.resource.dlg.State;
import infinity.resource.dlg.Transition;
import infinity.search.AttributeSearcher;
import infinity.search.DialogItemRefSearcher;
import infinity.search.DialogStateReferenceSearcher;
import infinity.search.ReferenceSearcher;
import infinity.util.StructClipboard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;

public final class StructViewer extends JPanel implements ListSelectionListener, ActionListener,
                                                          ItemListener, ChangeListener, TableModelListener
{
  public static final String CMD_VIEW           = "View";
  public static final String CMD_EXPORT         = "Export";
  public static final String CMD_PRINT          = "Print";
  public static final String CMD_REMOVE         = "Remove";
  public static final String CMD_SAVE           = "Save";
  public static final String CMD_ADD            = "Add";
  public static final String CMD_FIND           = "Find";
  public static final String CMD_COPYVALUE      = "VCopy";
  public static final String CMD_PASTEVALUE     = "VPaste";
  public static final String CMD_CUT            = "Cut";
  public static final String CMD_COPY           = "Copy";
  public static final String CMD_PASTE          = "Paste";
  public static final String CMD_TOHEX          = "ToHex";
  public static final String CMD_TOSTRING       = "ToStr";
  public static final String CMD_TOBIN          = "ToBin";
  public static final String CMD_TODEC          = "ToDec";
  public static final String CMD_SHOWVIEWER     = "ShowView";
  public static final String CMD_SHOWNEWVIEWER  = "ShowNewView";
  public static final String UPDATE_VALUE       = "UpdateValue";

  private static Class<? extends StructEntry> lastNameStruct, lastIndexStruct;
  private static String lastName;
  private static int lastIndex;
  private final AbstractStruct struct;
  private final ButtonPopupMenu bfind;
  private final CardLayout cards = new CardLayout();
  private final JButton bview = createButton(CMD_VIEW, "View/Edit", Icons.getIcon("Zoom16.gif"), this);
  private final JButton bexport = createButton(CMD_EXPORT, "Export...", Icons.getIcon("Export16.gif"), this);
  private final JButton bprint = createButton(CMD_PRINT, null, Icons.getIcon("Print16.gif"), this);
  private final JButton bremove = createButton(CMD_REMOVE, "Remove", Icons.getIcon("Remove16.gif"), this);
  private final JButton bsave = createButton(CMD_SAVE, "Save", Icons.getIcon("Save16.gif"), this);
  private final JMenuItem ifindattribute = new JMenuItem("selected attribute");
  private final JMenuItem ifindreferences = new JMenuItem("references to this file");
  private final JMenuItem ifindstatereferences = new JMenuItem("references to this state");
  private final JMenuItem ifindreftoitem = new JMenuItem("references to selected item in this file");
  private final JMenuItem miCopyValue = createMenuItem(CMD_COPYVALUE, "Copy value", Icons.getIcon("Copy16.gif"), this);
  private final JMenuItem miPasteValue = createMenuItem(CMD_PASTEVALUE, "Replace value", Icons.getIcon("Paste16.gif"), this);
  private final JMenuItem miCut = createMenuItem(CMD_CUT, "Cut", Icons.getIcon("Cut16.gif"), this);
  private final JMenuItem miCopy = createMenuItem(CMD_COPY, "Copy", Icons.getIcon("Copy16.gif"), this);
  private final JMenuItem miPaste = createMenuItem(CMD_PASTE, "Paste", Icons.getIcon("Paste16.gif"), this);
  private final JMenuItem miToHex = createMenuItem(CMD_TOHEX, "Edit as hex", Icons.getIcon("Refresh16.gif"), this);
  private final JMenuItem miToString = createMenuItem(CMD_TOSTRING, "Edit as string", Icons.getIcon("Refresh16.gif"), this);
  private final JMenuItem miToBin = createMenuItem(CMD_TOBIN, "Edit as binary", Icons.getIcon("Refresh16.gif"), this);
  private final JMenuItem miToDec = createMenuItem(CMD_TODEC, "Edit as decimal", Icons.getIcon("Refresh16.gif"), this);
  private final JMenuItem miShowViewer = createMenuItem(CMD_SHOWVIEWER, "Show in viewer", null, this);
  private final JMenuItem miShowNewViewer = createMenuItem(CMD_COPYVALUE, "Show in new viewer", null, this);
  private final JPanel lowerpanel = new JPanel(cards);
  private final JPanel editpanel = new JPanel();
  private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
  private final JPopupMenu popupmenu = new JPopupMenu();
  private final JTextArea tatext = new JTextArea();
  private final StructTable table = new StructTable();
  private AddRemovable emptyTypes[];
  private ButtonPopupMenu badd;
  private Editable editable;
  private JTabbedPane tabbedPane;

  private static JButton createButton(String cmd, String text, Icon icon, ActionListener l)
  {
    JButton b = new JButton(text, icon);
    b.setActionCommand(cmd);
    if (l != null)
      b.addActionListener(l);
    return b;
  }

  private static JMenuItem createMenuItem(String cmd, String text, Icon icon, ActionListener l)
  {
    JMenuItem m = new JMenuItem(text, icon);
    m.setActionCommand(cmd);
    if (l != null)
      m.addActionListener(l);
    return m;
  }


  public StructViewer(AbstractStruct struct)
  {
    this(struct, null);
  }

  public StructViewer(AbstractStruct struct, Collection<Component> extraComponents)
  {
    this.struct = struct;
    struct.addTableModelListener(this);
    table.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    table.getSelectionModel().addListSelectionListener(this);
    table.setFont(BrowserMenuBar.getInstance().getScriptFont());
    table.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() == 2 && table.getSelectedRowCount() == 1) {
          Object selected = table.getModel().getValueAt(table.getSelectedRow(), 1);
          if (selected instanceof Viewable)
            new ViewFrame(table.getTopLevelAncestor(), (Viewable)selected);
        }
      }
    });
    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
    {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus, int row,
                                                     int column)
      {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (column == 2)
          setHorizontalAlignment(JLabel.TRAILING);
        else
          setHorizontalAlignment(JLabel.LEADING);
        return this;
      }
    });
    popupmenu.add(miCopyValue);
    popupmenu.add(miPasteValue);
    popupmenu.add(miCut);
    popupmenu.add(miCopy);
    popupmenu.add(miPaste);
    popupmenu.add(miToHex);
    popupmenu.add(miToBin);
    popupmenu.add(miToDec);
    popupmenu.add(miToString);
    if (struct instanceof DlgResource) {
      popupmenu.add(miShowViewer);
      popupmenu.add(miShowNewViewer);
    }
    table.addMouseListener(new PopupListener());
    miCopyValue.setEnabled(false);
    miPasteValue.setEnabled(false);
    miCut.setEnabled(false);
    miCopy.setEnabled(false);
    miPaste.setEnabled(
            StructClipboard.getInstance().getContentType(struct) == StructClipboard.CLIPBOARD_ENTRIES);
    miToHex.setEnabled(false);
    miToBin.setEnabled(false);
    miToDec.setEnabled(false);
    miToString.setEnabled(false);
    miShowViewer.setEnabled(false);
    miShowNewViewer.setEnabled(false);

    tatext.setEditable(false);
    tatext.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    tatext.setFont(BrowserMenuBar.getInstance().getScriptFont());
    bview.setMnemonic('v');
    bview.setEnabled(false);
    bremove.setMnemonic('r');
    bremove.setEnabled(false);
    bprint.setMargin(new Insets(bprint.getMargin().top, 3, bprint.getMargin().bottom, 3));
    bprint.setToolTipText("Print");
    table.setModel(struct);
    table.getColumnModel().getColumn(0).setPreferredWidth(10);
    table.getColumnModel().getColumn(1).setPreferredWidth(400);
    if (table.getColumnCount() == 3)
      table.getColumnModel().getColumn(2).setPreferredWidth(6);

    lowerpanel.add(new JScrollPane(tatext), "Text");
    lowerpanel.add(editpanel, "Edit");
    lowerpanel.add(new JPanel(), "Empty");
    cards.show(lowerpanel, "Empty");

    if (struct instanceof HasAddRemovable && struct.getRowCount() > 0) {
      try {
        emptyTypes = ((HasAddRemovable)struct).getAddRemovables();
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (emptyTypes == null)
        emptyTypes = new AddRemovable[0];
      JMenuItem menuItems[] = new JMenuItem[emptyTypes.length];
      for (int i = 0; i < menuItems.length; i++)
        menuItems[i] = new JMenuItem(emptyTypes[i].getName());
      badd = new ButtonPopupMenu("Add...", menuItems);
      badd.setIcon(Icons.getIcon("Add16.gif"));
      badd.addItemListener(this);
      badd.setActionCommand(CMD_ADD);
      if (emptyTypes.length > 0) {
        buttonPanel.add(badd);
        buttonPanel.add(bremove);
      } else
        badd.setEnabled(false);
    }
    ifindattribute.setEnabled(false);
    ifindreferences.setEnabled(struct instanceof Resource && struct.getSuperStruct() == null);
    ifindstatereferences.setEnabled(false);
    ifindreftoitem.setEnabled(false);
    if (struct instanceof DlgResource)
      bfind = new ButtonPopupMenu("Find...", new JMenuItem[]{ifindattribute, ifindreferences,
                                                             ifindstatereferences, ifindreftoitem});
    else
      bfind = new ButtonPopupMenu("Find...", new JMenuItem[]{ifindattribute, ifindreferences});
    bfind.setIcon(Icons.getIcon("Find16.gif"));
    bfind.addItemListener(this);
    bfind.setActionCommand(CMD_FIND);
    buttonPanel.add(bfind);
    buttonPanel.add(bview);
    buttonPanel.add(bprint);
    if (struct instanceof Resource && struct.getRowCount() > 0 && struct.getSuperStruct() == null) {
      bexport.setToolTipText("NB! Will export last *saved* version");
      bexport.setMnemonic('e');
      bsave.setMnemonic('a');
      buttonPanel.add(bexport);
      buttonPanel.add(bsave);
    }
    if (extraComponents != null) {
      for (final Component c: extraComponents) {
        buttonPanel.add(c);
      }
    }

    JScrollPane scrollTable = new JScrollPane(table);
    scrollTable.getViewport().setBackground(table.getBackground());
    scrollTable.setBorder(BorderFactory.createEmptyBorder());
    JSplitPane splitv = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTable, lowerpanel);
    splitv.setDividerLocation(2 * (NearInfinity.getInstance().getHeight() - 100) / 3);

    setLayout(new BorderLayout());
    if (struct instanceof HasDetailViewer) {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(splitv, BorderLayout.CENTER);
      panel.add(buttonPanel, BorderLayout.SOUTH);
      tabbedPane = new JTabbedPane();
      tabbedPane.addTab("View", ((HasDetailViewer)struct).getDetailViewer());
      tabbedPane.addTab("Edit", panel);
      add(tabbedPane, BorderLayout.CENTER);
      if (struct.getSuperStruct() != null && struct.getSuperStruct() instanceof HasDetailViewer) {
        StructViewer sViewer = struct.getSuperStruct().getViewer();
        if (sViewer == null)
          sViewer = struct.getSuperStruct().getSuperStruct().getViewer();
        tabbedPane.setSelectedIndex(sViewer.tabbedPane.getSelectedIndex());
      }
      else if (lastIndexStruct == struct.getClass())
        tabbedPane.setSelectedIndex(lastIndex);
      else if (BrowserMenuBar.getInstance().getDefaultStructView() == BrowserMenuBar.DEFAULT_EDIT)
        tabbedPane.setSelectedIndex(1);
      if (tabbedPane.getSelectedIndex() == 1) {
        if (lastNameStruct == struct.getClass())
          selectEntry(lastName);
        else
          table.getSelectionModel().setSelectionInterval(0, 0);
      }
    }
    else {
      add(splitv, BorderLayout.CENTER);
      add(buttonPanel, BorderLayout.SOUTH);
      if (lastNameStruct == struct.getClass())
        selectEntry(lastName);
      else
        table.getSelectionModel().setSelectionInterval(0, 0);
    }

    StructClipboard.getInstance().addChangeListener(this);
    table.repaint();
  }

// --------------------- Begin Interface ActionListener ---------------------

  @Override
  public void actionPerformed(ActionEvent event)
  {
    if (event.getActionCommand().equals(CMD_VIEW)) {
      Viewable selected = (Viewable)table.getModel().getValueAt(table.getSelectedRow(), 1);
      new ViewFrame(getTopLevelAncestor(), selected);
    }
    else if (event.getActionCommand().equals(UPDATE_VALUE)) {
      if (editable.updateValue(struct)) {
        struct.setStructChanged(true);
        struct.fireTableRowsUpdated(struct.getIndexOf(editable), struct.getIndexOf(editable));
        if (editable instanceof EffectType) // Updates multiple lines - could be done better?
          struct.fireTableDataChanged();
      }
      else
        JOptionPane.showMessageDialog(this, "Error updating value", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (event.getActionCommand().equals(CMD_REMOVE)) {
      int row = table.getSelectedRow();
      AddRemovable selected = (AddRemovable)table.getModel().getValueAt(row, 1);
      struct.removeDatatype(selected, true);
    }
    else if (event.getActionCommand().equals(CMD_SAVE)) {
      if (ResourceFactory.getInstance().saveResource((Resource)struct, getTopLevelAncestor()))
        struct.setStructChanged(false);
    }
    else if (event.getActionCommand().equals(CMD_EXPORT))
      ResourceFactory.getInstance().exportResource(struct.getResourceEntry(), getTopLevelAncestor());
    else if (event.getActionCommand().equals(CMD_PRINT)) {
      PrinterJob pj = PrinterJob.getPrinterJob();
      pj.setPrintable(table);
      if (pj.printDialog()) {
        try {
          pj.print();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    else if (event.getActionCommand().equals(CMD_COPYVALUE))
      StructClipboard.getInstance().copyValue(struct, table.getSelectionModel().getMinSelectionIndex(),
                                              table.getSelectionModel().getMaxSelectionIndex());
    else if (event.getActionCommand().equals(CMD_PASTEVALUE)) {
      int changed = StructClipboard.getInstance().pasteValue(struct,
                                                             table.getSelectionModel().getMinSelectionIndex());
      if (changed == 0)
        JOptionPane.showMessageDialog(this, "Attributes doesn't match!", "Error", JOptionPane.ERROR_MESSAGE);
      else {
        struct.fireTableRowsUpdated(table.getSelectionModel().getMinSelectionIndex(),
                                    table.getSelectionModel().getMinSelectionIndex() + changed);
        struct.setStructChanged(true);
      }
    }
    else if (event.getActionCommand().equals(CMD_CUT)) {
      ListSelectionModel lsm = table.getSelectionModel();
      int min = lsm.getMinSelectionIndex();
      int max = lsm.getMaxSelectionIndex();
      lsm.removeIndexInterval(min, max);
      table.clearSelection();
      StructClipboard.getInstance().cut(struct, min, max);
    }
    else if (event.getActionCommand().equals(CMD_COPY))
      StructClipboard.getInstance().copy(struct, table.getSelectionModel().getMinSelectionIndex(),
                                         table.getSelectionModel().getMaxSelectionIndex());
    else if (event.getActionCommand().equals(CMD_PASTE)) {
      table.clearSelection();
      table.scrollRectToVisible(table.getCellRect(StructClipboard.getInstance().paste(struct), 1, true));
    }
    else if (event.getActionCommand().equals(CMD_TOHEX))
      convertAttribute(table.getSelectedRow(), miToHex);
    else if (event.getActionCommand().equals(CMD_TOBIN))
      convertAttribute(table.getSelectedRow(), miToBin);
    else if (event.getActionCommand().equals(CMD_TODEC))
      convertAttribute(table.getSelectedRow(), miToDec);
    else if (event.getActionCommand().equals(CMD_TOSTRING))
      convertAttribute(table.getSelectedRow(), miToString);
    else if (event.getActionCommand().equals(CMD_SHOWVIEWER)) {
      // this should only be available for DlgResources
      DlgResource dlgRes = (DlgResource) struct;
      dlgRes.showStateWithStructEntry((StructEntry)table.getValueAt(table.getSelectedRow(), 1));
      JComponent detailViewer = dlgRes.getDetailViewer();
      JTabbedPane parent = (JTabbedPane) detailViewer.getParent();
      parent.getModel().setSelectedIndex(parent.indexOfComponent(detailViewer));

    }
    else if (event.getActionCommand().equals(CMD_SHOWNEWVIEWER)) {
      // get a copy of the resource first
      DlgResource dlgRes = (DlgResource) ResourceFactory.getResource(struct.getResourceEntry());
      new ViewFrame(getTopLevelAncestor(), dlgRes);
      dlgRes.showStateWithStructEntry((StructEntry)table.getValueAt(table.getSelectedRow(), 1));

    }
  }

// --------------------- End Interface ActionListener ---------------------


// --------------------- Begin Interface ChangeListener ---------------------

  @Override
  public void stateChanged(ChangeEvent event)
  {
    considerMenuEnabled();
  }

// --------------------- End Interface ChangeListener ---------------------


// --------------------- Begin Interface ItemListener ---------------------

  @Override
  public void itemStateChanged(ItemEvent event)
  {
    if (event.getSource() == badd) {
      //      JMenuItem item = (JMenuItem)event.getItem();  // Should have worked!
      JMenuItem item = badd.getSelectedItem();
      AddRemovable toadd = null;
      for (final AddRemovable emtryType : emptyTypes)
        if (emtryType != null && emtryType.getName().equals(item.getText())) {
          toadd = emtryType;
          break;
        }
      try {
        toadd = (AddRemovable)toadd.clone();
      } catch (CloneNotSupportedException e) {
        e.printStackTrace();
        return;
      }
      int index = struct.addDatatype(toadd);
      table.getSelectionModel().setSelectionInterval(index, index);
      table.scrollRectToVisible(table.getCellRect(index, 1, true));
    }
    else if (event.getSource() == bfind) {
      //      JMenuItem item = (JMenuItem)event.getItem();  // Should have worked!
      JMenuItem item = bfind.getSelectedItem();
      if (item == ifindattribute)
        new AttributeSearcher(struct, (StructEntry)table.getValueAt(table.getSelectedRow(), 1),
                              getTopLevelAncestor());
      else if (item == ifindreferences)
        new ReferenceSearcher(struct.getResourceEntry(), getTopLevelAncestor());
      else if (item == ifindstatereferences) {
        State state = (State)table.getValueAt(table.getSelectedRow(), 1);
        new DialogStateReferenceSearcher(struct.getResourceEntry(), state.getNumber(), getTopLevelAncestor());
      }
      else if (item == ifindreftoitem) {
        new DialogItemRefSearcher((DlgResource) struct, table.getValueAt(table.getSelectedRow(), 1),
                                  getTopLevelAncestor());
      }
    }
  }

// --------------------- End Interface ItemListener ---------------------


// --------------------- Begin Interface ListSelectionListener ---------------------

  @Override
  public void valueChanged(ListSelectionEvent event)
  {
    if (event.getValueIsAdjusting())
      return;
    considerMenuEnabled();
    ListSelectionModel lsm = (ListSelectionModel)event.getSource();
    if (lsm.isSelectionEmpty() || lsm.getMaxSelectionIndex() != lsm.getMinSelectionIndex()) {
      tatext.setText("");
      bremove.setEnabled(false);
      bview.setEnabled(false);
      cards.show(lowerpanel, "Empty");
      miToHex.setEnabled(false);
      miToBin.setEnabled(false);
      miToDec.setEnabled(false);
      miToString.setEnabled(false);
      miShowViewer.setEnabled(false);
      miShowNewViewer.setEnabled(false);
      ifindattribute.setEnabled(false);
      ifindstatereferences.setEnabled(false);
      ifindreftoitem.setEnabled(false);
    }
    else {
      table.scrollRectToVisible(table.getCellRect(lsm.getMinSelectionIndex(), 0, true));
      Object selected = table.getModel().getValueAt(lsm.getMinSelectionIndex(), 1);
      miPaste.setEnabled(
              StructClipboard.getInstance().getContentType(struct) == StructClipboard.CLIPBOARD_ENTRIES);
      bremove.setEnabled(selected instanceof AddRemovable && ((AddRemovable)selected).canRemove());
      bview.setEnabled(selected instanceof Viewable);
      ifindattribute.setEnabled(!(selected instanceof AbstractStruct));
      ifindstatereferences.setEnabled(selected instanceof State);
      miToHex.setEnabled(selected instanceof Datatype && !(selected instanceof HexNumber ||
                                                           selected instanceof Unknown ||
                                                           selected instanceof SectionCount));
      if (selected instanceof UnknownBinary || selected instanceof UnknownDecimal)
        miToHex.setEnabled(true);
      miToBin.setEnabled(selected instanceof Datatype && !(selected instanceof UnknownBinary ||
                                                           selected instanceof SectionCount));
      miToDec.setEnabled(selected instanceof Datatype && !(selected instanceof UnknownDecimal ||
                                                           selected instanceof SectionCount));
      miToString.setEnabled(selected instanceof Datatype && (selected instanceof Unknown ||
                                                             selected instanceof ResourceRef));
      boolean isSpecialDlgStruct = (selected instanceof State
                                 || selected instanceof Transition
                                 || selected instanceof AbstractCode);

      ifindreftoitem.setEnabled(isSpecialDlgStruct);
      miShowViewer.setEnabled(isSpecialDlgStruct);
      miShowNewViewer.setEnabled(isSpecialDlgStruct);

      if (selected instanceof Editable) {
        editable = (Editable)selected;
        editpanel.removeAll();

        JComponent editor = editable.edit(this);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        editpanel.setLayout(gbl);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbl.setConstraints(editor, gbc);
        editpanel.add(editor);

        editpanel.revalidate();
        editpanel.repaint();
        cards.show(lowerpanel, "Edit");
        editable.select();
      }
      else if (selected instanceof InlineEditable) {
        tatext.setText("");
        cards.show(lowerpanel, "Empty");
      }
      else {
        editable = null;
        if (selected instanceof AbstractStruct)
          tatext.setText(((AbstractStruct)selected).toMultiLineString());
        else
          tatext.setText(selected.toString());
        tatext.setCaretPosition(0);
        cards.show(lowerpanel, "Text");
      }
    }
  }

// --------------------- End Interface ListSelectionListener ---------------------


// --------------------- Begin Interface TableModelListener ---------------------

  @Override
  public void tableChanged(TableModelEvent event)
  {
    if (event.getType() == TableModelEvent.UPDATE) {
      StructEntry structEntry = struct.getStructEntryAt(event.getFirstRow());
      if (structEntry instanceof Editable && (editable == null || (structEntry.getOffset() == editable.getOffset() &&
                                                                   structEntry != editable))) {
        editable = (Editable)structEntry;
        editpanel.removeAll();

        JComponent editor = editable.edit(this);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        editpanel.setLayout(gbl);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbl.setConstraints(editor, gbc);
        editpanel.add(editor);

        editpanel.revalidate();
        editpanel.repaint();
        cards.show(lowerpanel, "Edit");
        editable.select();
      }
    }
  }

// --------------------- End Interface TableModelListener ---------------------

  public JPanel getButtonPanel()
  {
    return buttonPanel;
  }

  public void close()
  {
    StructClipboard.getInstance().removeChangeListener(this);
    if (struct instanceof Resource) {
      if (struct instanceof HasDetailViewer) {
        lastIndex = tabbedPane.getSelectedIndex();
        lastIndexStruct = struct.getClass();
      }
      else
        lastIndexStruct = null;
      if (table.getSelectionModel().getMinSelectionIndex() != -1) {
        lastName =
        ((StructEntry)table.getModel().getValueAt(table.getSelectionModel().getMinSelectionIndex(), 1)).getName();
        lastNameStruct = struct.getClass();
      }
      else {
        lastName = null;
        lastNameStruct = null;
      }
    }
  }

  public StructEntry getSelectedEntry()
  {
    if (table.getSelectedRow() == -1)
      return null;
    return (StructEntry)table.getModel().getValueAt(table.getSelectedRow(), 1);
  }

  public int getSelectedRow()
  {
    return table.getSelectedRow();
  }

  public void selectEntry(String name)
  {
    for (int i = 0; i < struct.getRowCount(); i++) {
      StructEntry entry = struct.getStructEntryAt(i);
      if (entry.getName().equals(name)) {
        selectEntry(entry);
        return;
      }
    }
  }

  public void selectEntry(int offset)
  {
    for (int i = 0; i < struct.getRowCount(); i++) {
      StructEntry entry = struct.getStructEntryAt(i);
      if (entry instanceof AbstractStruct)
        selectEntry((AbstractStruct)entry, offset);
      else if (entry.getOffset() == offset)
        selectEntry(entry);
    }
  }

  public boolean hasViewTab()
  {
    return (tabbedPane != null && tabbedPane.getTabCount() > 0);
  }

  public boolean isViewTabSelected()
  {
    if (tabbedPane != null) {
      return tabbedPane.getSelectedIndex() == 0;
    }
    return false;
  }

  public void selectViewTab()
  {
    if (tabbedPane != null && tabbedPane.getSelectedIndex() != 0) {
      tabbedPane.setSelectedIndex(0);
    }
  }

  public boolean isEditTabSelected()
  {
    if (tabbedPane != null) {
      return tabbedPane.getSelectedIndex() == 1;
    }
    return true;
  }

  public void selectEditTab()
  {
    if (tabbedPane != null && tabbedPane.getTabCount() > 1 && tabbedPane.getSelectedIndex() != 1) {
      tabbedPane.setSelectedIndex(1);
    }
  }

  private void considerMenuEnabled()
  {
    ListSelectionModel lsm = table.getSelectionModel();
    if (lsm.isSelectionEmpty()) {
      miCopyValue.setEnabled(false);
      miPasteValue.setEnabled(false);
      miCut.setEnabled(false);
      miCopy.setEnabled(false);
      miPaste.setEnabled(
              StructClipboard.getInstance().getContentType(struct) == StructClipboard.CLIPBOARD_ENTRIES);
    }
    else if (lsm.getMaxSelectionIndex() != lsm.getMinSelectionIndex()) {
      boolean isRemovable = true;
      boolean isValue = true;
      for (int i = lsm.getMinSelectionIndex(); i <= lsm.getMaxSelectionIndex(); i++) {
        Object o = table.getModel().getValueAt(i, 1);
        if (!(o instanceof AddRemovable))
          isRemovable = false;
        else
          isRemovable = ((AddRemovable)o).canRemove();
        if (o instanceof AbstractStruct)
          isValue = false;
      }
      miCopyValue.setEnabled(isValue);
      miPasteValue.setEnabled(false);
      miCut.setEnabled(isRemovable);
      miCopy.setEnabled(isRemovable);
      miPaste.setEnabled(
              StructClipboard.getInstance().getContentType(struct) == StructClipboard.CLIPBOARD_ENTRIES);
    }
    else {
      Object selected = table.getModel().getValueAt(lsm.getMinSelectionIndex(), 1);
      miPaste.setEnabled(
              StructClipboard.getInstance().getContentType(struct) == StructClipboard.CLIPBOARD_ENTRIES);
      if (selected instanceof AddRemovable) {
        miCopyValue.setEnabled(false);
        miPasteValue.setEnabled(false);
        miCut.setEnabled(((AddRemovable)selected).canRemove());
        miCopy.setEnabled(((AddRemovable)selected).canRemove());
      }
      else {
        miCopyValue.setEnabled(!(selected instanceof AbstractStruct));
        miPasteValue.setEnabled(miCopyValue.isEnabled() &&
                                StructClipboard.getInstance().getContentType(struct) ==
                                StructClipboard.CLIPBOARD_VALUES);
        miCut.setEnabled(false);
        miCopy.setEnabled(false);
      }
    }
  }

  private void convertAttribute(int index, JMenuItem menuitem)
  {
    StructEntry entry = struct.getStructEntryAt(index);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      entry.write(baos);
      StructEntry newentry;
      if (menuitem == miToHex)
        newentry = new Unknown(baos.toByteArray(), 0, entry.getSize(), entry.getName());
      else if (menuitem == miToBin)
        newentry = new UnknownBinary(baos.toByteArray(), 0, entry.getSize(), entry.getName());
      else if (menuitem == miToDec)
        newentry = new UnknownDecimal(baos.toByteArray(), 0, entry.getSize(), entry.getName());
      else if (menuitem == miToString)
        newentry = new TextString(baos.toByteArray(), 0, entry.getSize(), entry.getName());
      else
        throw new NullPointerException();
      newentry.setOffset(entry.getOffset());
      struct.setListEntry(index, newentry);
      table.getSelectionModel().removeSelectionInterval(index, index);
      table.getSelectionModel().addSelectionInterval(index, index);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void selectEntry(StructEntry structEntry)
  {
    for (int i = 0; i < struct.getRowCount(); i++) {
      StructEntry o = struct.getStructEntryAt(i);
      if (structEntry == o) {
        table.getSelectionModel().setSelectionInterval(i, i);
        if (tabbedPane != null)
          tabbedPane.setSelectedIndex(1);
        return;
      }
      else if (o instanceof AbstractStruct)
        selectSubEntry((AbstractStruct)o, structEntry);
    }
  }

  private void selectEntry(AbstractStruct subStruct, int offset)
  {
    for (int i = 0; i < subStruct.getRowCount(); i++) {
      StructEntry entry = subStruct.getStructEntryAt(i);
      if (entry instanceof AbstractStruct)
        selectEntry((AbstractStruct)entry, offset);
      else if (entry.getOffset() == offset)
        selectSubEntry(subStruct, entry);
    }
  }

  private void selectSubEntry(AbstractStruct subStruct, StructEntry structEntry)
  {
    for (int i = 0; i < subStruct.getRowCount(); i++) {
      StructEntry o = subStruct.getStructEntryAt(i);
      if (structEntry == o) {
        new ViewFrame(getTopLevelAncestor(), subStruct);
        StructViewer viewer = subStruct.getViewer();
        viewer.table.getSelectionModel().setSelectionInterval(i, i);
        table.scrollRectToVisible(table.getCellRect(i, 0, true));
        if (viewer.tabbedPane != null)
          viewer.tabbedPane.setSelectedIndex(1);
        return;
      }
      else if (o instanceof AbstractStruct)
        selectSubEntry((AbstractStruct)o, structEntry);
    }
  }

// -------------------------- INNER CLASSES --------------------------

  private final class PopupListener extends MouseAdapter
  {
    @Override
    public void mousePressed(MouseEvent e)
    {
      maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e)
    {
      if (e.isPopupTrigger())
        popupmenu.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  private final class StructTable extends JTable implements Printable
  {
    private StructTable()
    {
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException
    {
      Graphics2D g2 = (Graphics2D)graphics;
      g2.setColor(Color.black);
      int fontHeight = g2.getFontMetrics().getHeight();
      int fontDesent = g2.getFontMetrics().getDescent();

      //leave room for page number
      double pageHeight = pageFormat.getImageableHeight() - (double)fontHeight;
      double pageWidth = pageFormat.getImageableWidth();
      double tableWidth = (double)getColumnModel().getTotalColumnWidth();
      double scale = (double)1;
      if (tableWidth >= pageWidth)
        scale = pageWidth / tableWidth;

      double headerHeightOnPage = (double)getTableHeader().getHeight() * scale;
      double tableWidthOnPage = tableWidth * scale;

      double oneRowHeight = (double)getRowHeight() * scale;
      int numRowsOnAPage = (int)((pageHeight - headerHeightOnPage) / oneRowHeight);
      double pageHeightForTable = oneRowHeight * (double)numRowsOnAPage;
      int totalNumPages = (int)Math.ceil((double)getRowCount() / (double)numRowsOnAPage);
      if (pageIndex >= totalNumPages)
        return NO_SUCH_PAGE;

      g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
      //bottom center
      g2.drawString(struct.getName() + " - Page " + (pageIndex + 1) + '/' + totalNumPages,
                    ((int)pageWidth >> 1) - 35, (int)(pageHeight + (double)fontHeight - (double)fontDesent));

      g2.translate((double)0.0f, headerHeightOnPage - (double)pageIndex * pageHeightForTable);

      //If this piece of the table is smaller than the size available, clip to the appropriate bounds.
      if (pageIndex + 1 == totalNumPages) {
        int lastRowPrinted = numRowsOnAPage * pageIndex;
        int numRowsLeft = getRowCount() - lastRowPrinted;
        g2.setClip(0, (int)(pageHeightForTable * (double)pageIndex), (int)Math.ceil(tableWidthOnPage),
                   (int)Math.ceil(oneRowHeight * (double)numRowsLeft));
      }
      //else clip to the entire area available.
      else
        g2.setClip(0, (int)(pageHeightForTable * (double)pageIndex), (int)Math.ceil(tableWidthOnPage),
                   (int)Math.ceil(pageHeightForTable));

      g2.scale(scale, scale);
      paint(g2);
      g2.scale((double)1 / scale, (double)1 / scale);
      g2.translate((double)0.0f, (double)pageIndex * pageHeightForTable - headerHeightOnPage);
      g2.setClip(0, 0, (int)Math.ceil(tableWidthOnPage), (int)Math.ceil(headerHeightOnPage));
      g2.scale(scale, scale);
      getTableHeader().paint(g2);
      //paint header at top

      return Printable.PAGE_EXISTS;
    }
  }
}


