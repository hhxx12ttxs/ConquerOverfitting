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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import org.jdesktop.application.SingleFrameApplication;

/**
 *
 * @author drjrkuhn
 */
public class GenericInputDialog extends JDialog {
    
    protected JButton okButton;
    protected JButton cancelButton;
    protected JPanel itemsPanel;
    protected List<Item> items;
    
    protected boolean canceled;
    protected static final int GAP_RELATED = 3;
    protected static final int GAP_CONTAINER = 6;
    protected static final Insets FIELD_INSETS = new Insets(0, GAP_RELATED, GAP_RELATED, GAP_RELATED);
    protected static final Insets LABEL_INSETS = new Insets(0, GAP_RELATED, GAP_RELATED-1, 0);
    
    /** Creates a new instance of GenericInputDialog */
    public GenericInputDialog(SingleFrameApplication app, String message, String title) {
        this(app.getMainFrame(), message, title);
    }
    
    /** Creates a new instance of GenericInputDialog */
    public GenericInputDialog(java.awt.Frame parent, String message, String title) {
        super(parent, true);
        setLocationRelativeTo(parent);
        canceled = true;
        items = new ArrayList<Item>();
        
        setTitle(title);
        JPanel outerPanel = new JPanel();
        outerPanel.setBorder(BorderFactory.createEmptyBorder(GAP_CONTAINER, GAP_CONTAINER, GAP_CONTAINER, GAP_CONTAINER));
        outerPanel.setLayout(new BorderLayout());
        this.add(outerPanel);
        
        itemsPanel = new JPanel(new GridBagLayout());
        if (message != null) {
            itemsPanel.setBorder(BorderFactory.createTitledBorder(message));
        }
        outerPanel.add(itemsPanel, BorderLayout.CENTER);
        
        okButton = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        
        cancelButton = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP_RELATED, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(GAP_CONTAINER, 0, 0, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        outerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(okButton);
        pack();
    }
    
    //-----------------------------------------------------------------------
    // Actions
    //-----------------------------------------------------------------------
    
    protected void onOK() {
        canceled = false;
        for (Item it : items) {
            if (it.value != null && it.parser != null) {
                it.value[0] = it.parser.parse(it.field);
            }
        }
        dispose();
    }
    
    protected void onCancel() {
        canceled = true;
        dispose();
    }
    
    //-----------------------------------------------------------------------
    // Utilities
    //-----------------------------------------------------------------------
    
    protected void addLabeledComponent(String labelName, Object[] value, JComponent component, Parser parser) {
        int row = items.size();
        
        JLabel label = new JLabel(labelName);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = gbc.EAST;
        itemsPanel.add(label, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = gbc.WEST;
        itemsPanel.add(component, gbc);
        
        items.add(new Item(value, component, parser));
        pack();
    }
    
    /** Interface used to convert a JComponent back into an object. */
    protected interface Parser {
        public Object parse(JComponent component);
    }
    
    /** Used to hold list of items. */
    protected class Item {
        public Object[] value;
        public JComponent field;
        public Parser parser;
        public Item(Object[] value, JComponent field, Parser parser) {
            this.value = value;
            this.field = field;
            this.parser = parser;
        }
    }
    
    protected class SelectionFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            if (!(e.getComponent() instanceof JTextComponent)) return;
            ((JTextComponent)e.getComponent()).selectAll();
        }
        public void focusLost(FocusEvent e) { }
        
    }
    
    // Allow the ESCAPE key to close
    protected JRootPane createRootPane() {
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                onCancel();
            }
        };
        JRootPane rootPane = new JRootPane();
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rootPane;
    }
    
    //=======================================================================
    // Methods to setup and show GenericInputDialog
    //=======================================================================
    
    public void addString(String label, String[] item, int columns) {
        JTextField text = new JTextField(item[0], columns);
        text.addFocusListener(new SelectionFocusListener());
        addLabeledComponent(label, item, text, new Parser() {
            public Object parse(JComponent component) {
                JTextField field = (JTextField)component;
                return field.getText();
            }
        });
    }
    
    public void addInteger(String label, Integer[] item, int columns) {
        JTextField text = new JTextField(Integer.toString(item[0]), columns);
        text.addFocusListener(new SelectionFocusListener());
        addLabeledComponent(label, item, text, new Parser() {
            public Object parse(JComponent component) {
                JTextField field = (JTextField)component;
                return new Integer(field.getText());
            }
        });
    }
    
    public void addDouble(String label, Double[] item, int columns) {
        JTextField text = new JTextField(Double.toString(item[0]), columns);
        text.addFocusListener(new SelectionFocusListener());
        addLabeledComponent(label, item, text, new Parser() {
            public Object parse(JComponent component) {
                JTextField field = (JTextField)component;
                return new Double(field.getText());
            }
        });
    }
    
    public void addBoolean(String label, Boolean[] item, boolean labelFirst) {
        JCheckBox check = new JCheckBox(labelFirst ? "" : label, item[0]);
        check.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        addLabeledComponent(labelFirst ? label : "", item, check, new Parser() {
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
        gbc.fill = gbc.HORIZONTAL;
        gbc.insets = new Insets(0, 0, GAP_RELATED, 0);
        gbc.anchor = gbc.WEST;
        itemsPanel.add(boxline, gbc);
        
        items.add(new Item(null, boxline, null));
        pack();
    }
    
    public boolean showDialog() {
        setVisible(true);
        // wait for modal exit;
        return !wasCanceled();
    }
    
    public boolean wasCanceled() {
        return canceled;
    }
    
    //=======================================================================
    // TEST
    //=======================================================================
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String message = "Enter Test Values";
                //String message = null;
                GenericInputDialog gd = new GenericInputDialog(new javax.swing.JFrame(), message, "GenericInputDialog");
                String[] stringVal = {"Hello, World!"};
                Integer[] intVal = {new Integer(100)};
                Boolean[] boolVal = {new Boolean(true)};
                Double[] doubleVal = {new Double(200)};
                Boolean[] boolOpt1 = {new Boolean(false)};
                Boolean[] boolOpt2 = {new Boolean(true)};
                Boolean[] boolOpt3 = {new Boolean(false)};
                gd.addString("String:", stringVal, 12);
                gd.addSeparator("Separator");
                gd.addInteger("Integer:", intVal, 4);
                gd.addBoolean("Boolean:", boolVal, true);
                gd.addDouble("Double:", doubleVal, 6);
                gd.addBoolean("Option 1", boolOpt1, false);
                gd.addBoolean("Option 2", boolOpt2, false);
                gd.addBoolean("Option 3", boolOpt3, false);
                if (gd.showDialog()) {
                    System.out.println("Dialog input was approved");
                } else {
                    System.out.println("Dialog input was canceled");
                }
                System.out.println("  String = " + stringVal[0]);
                System.out.println("  Integer = " + intVal[0]);
                System.out.println("  Boolean = " + boolVal[0]);
                System.out.println("  Double = " + doubleVal[0]);
                System.out.println("  Option 1 = " + boolOpt1[0]);
                System.out.println("  Option 2 = " + boolOpt2[0]);
                System.out.println("  Option 3 = " + boolOpt3[0]);
                System.exit(0);
            }
        });
    }
    
}

