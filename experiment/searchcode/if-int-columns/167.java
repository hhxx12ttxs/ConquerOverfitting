/*
 * GenericInputDialog.java
 *
 * Created on March 15, 2006, 12:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package kuhnlab.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

/**
 *
 * @author drjrkuhn
 */
public class GenericOptionsPanel extends JPanel {
    
    protected JPanel itemsPanel;
    protected List<Item> items;
    
    protected static final int GAP_RELATED = 3;
    protected static final int GAP_CONTAINER = 6;
    protected static final Insets FIELD_INSETS = new Insets(0, GAP_RELATED, GAP_RELATED, GAP_RELATED);
    protected static final Insets LABEL_INSETS = new Insets(0, GAP_RELATED, GAP_RELATED-1, 0);
    
    /** Creates a new instance of GenericOptionsPanel */
    public GenericOptionsPanel(String name, String message) {
        super();
        this.setName(name);
        this.setBorder(BorderFactory.createEmptyBorder(GAP_CONTAINER, GAP_CONTAINER, GAP_CONTAINER, GAP_CONTAINER));
        this.setLayout(new BorderLayout());
        
        items = new ArrayList<Item>();
        itemsPanel = new JPanel(new GridBagLayout());
        if (message != null) {
            itemsPanel.setBorder(BorderFactory.createTitledBorder(message));
        }
        this.add(itemsPanel, BorderLayout.CENTER);
    }
    
    //=======================================================================
    // Methods to setup and show GenericInputDialog
    //=======================================================================
    
    public void addString(String label, Object[] items, int index, int columns) {
        if (!(items[index] instanceof String)) {
            throw new IllegalArgumentException("Item at index "+index+" is not of class String");
        }
        JTextField text = new JTextField((String)items[index], columns);
        text.addFocusListener(new SelectionFocusListener());
        addLabeledComponent(label, items, index, text, new Parser() {
            public Object parse(JComponent component) {
                JTextField field = (JTextField)component;
                return field.getText();
            }
        });
    }
    
    public void addInteger(String label, Object[] items, int index, int columns) {
        if (!(items[index] instanceof Integer)) {
            throw new IllegalArgumentException("Item at index "+index+" is not of class Integer");
        }
        JTextField text = new JTextField(Integer.toString((Integer)items[index]), columns);
        text.addFocusListener(new SelectionFocusListener());
        addLabeledComponent(label, items, index, text, new Parser() {
            public Object parse(JComponent component) {
                JTextField field = (JTextField)component;
                return new Integer(field.getText());
            }
        });
    }
    
    public void addDouble(String label, Object[] items, int index, int columns) {
        if (!(items[index] instanceof Double)) {
            throw new IllegalArgumentException("Item at index "+index+" is not of class Double");
        }
        JTextField text = new JTextField(Double.toString((Double)items[index]), columns);
        text.addFocusListener(new SelectionFocusListener());
        addLabeledComponent(label, items, index, text, new Parser() {
            public Object parse(JComponent component) {
                JTextField field = (JTextField)component;
                return new Double(field.getText());
            }
        });
    }
    
    public void addBoolean(String label, Object[] items, int index, boolean labelFirst) {
        if (!(items[index] instanceof Boolean)) {
            throw new IllegalArgumentException("Item at index "+index+" is not of class Boolean");
        }
        JCheckBox check = new JCheckBox(labelFirst ? "" : label, (Boolean)items[index]);
        check.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        addLabeledComponent(labelFirst ? label : "", items, index, check, new Parser() {
            public Object parse(JComponent component) {
                JCheckBox check = (JCheckBox)component;
                return new Boolean(check.isSelected());
            }
        });
    }
    
    public void addSeparator(String title) {
        int row = items.size();

        Box boxsep, boxline = new Box(BoxLayout.X_AXIS);

        TitledBorder systemTitledBorder = BorderFactory.createTitledBorder("");
        
        JLabel label = new JLabel(title);
        label.setFont(systemTitledBorder.getTitleFont());
        label.setForeground(systemTitledBorder.getTitleColor());
        label.setBorder(BorderFactory.createEmptyBorder(0,GAP_RELATED,0,GAP_RELATED));
        JSeparator sepL = new JSeparator(JSeparator.HORIZONTAL);
        // Calculate a gap to lower the separator to the center of the label
        int lh = (int)((label.getPreferredSize().height - sepL.getPreferredSize().height)/2);
        //sepL.setForeground(systemTitledBorder.getTitleColor());
        
        boxsep = new Box(BoxLayout.Y_AXIS);
        boxsep.add(Box.createVerticalStrut(lh));
        boxsep.add(sepL);
        boxline.add(boxsep);
        
        boxline.add(label);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, GAP_RELATED, 0);
        gbc.anchor = GridBagConstraints.WEST;
        itemsPanel.add(boxline, gbc);
        
        items.add(new Item(null, 0, boxline, null));
    }
    
    //-----------------------------------------------------------------------
    // helper functions
    //-----------------------------------------------------------------------
    
    public void retrieveItems() {
        for (Item it : items) {
            if (it.values != null && it.parser != null) {
                it.values[it.index] = it.parser.parse(it.field);
            }
        }
    }
    
    //-----------------------------------------------------------------------
    // Utilities
    //-----------------------------------------------------------------------
    
    protected void addLabeledComponent(String labelName, Object[] values, int index, JComponent component, Parser parser) {
        int row = items.size();
        
        JLabel label = new JLabel(labelName);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.EAST;
        itemsPanel.add(label, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.WEST;
        itemsPanel.add(component, gbc);
        
        items.add(new Item(values, index, component, parser));
    }
    
    /** Interface used to convert a JComponent back into an object. */
    protected interface Parser {
        public Object parse(JComponent component);
    }
    
    /** Used to hold list of items. */
    protected class Item {
        public Object[] values;
        public int index;
        public JComponent field;
        public Parser parser;
        public Item(Object[] values, int index, JComponent field, Parser parser) {
            this.values = values;
            this.index = index;
            this.field = field;
            this.parser = parser;
        }
    }

    /** Used to correctly set focus of text boxes */
    protected class SelectionFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            if (!(e.getComponent() instanceof JTextComponent)) return;
            ((JTextComponent)e.getComponent()).selectAll();
        }
        public void focusLost(FocusEvent e) { }
        
    }
    
}

