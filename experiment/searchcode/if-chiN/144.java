package tests;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.squery.ComponentList;
import com.squery.SQuery;

/*
 * CheckBoxDemo.java requires 16 image files in the images/geek
 * directory: 
 * geek-----.gif, geek-c---.gif, geek--g--.gif, geek---h-.gif, geek----t.gif,
 * geek-cg--.gif, ..., geek-cght.gif.
 */
@SuppressWarnings("serial")
public class CheckBoxDemo extends JPanel
                          implements ItemListener {

    SQuery squery;
    StringBuffer choices;

    public CheckBoxDemo() {
        super(new BorderLayout());
        squery = new SQuery(this);


        ComponentList clist;
        
        clist = squery.get(new JCheckBox()).set("text", "Chin").set("mnemonic", KeyEvent.VK_C);
        clist.data("index", 0).data("flag", 'c').group("choice");
        
        clist = squery.get(new JCheckBox()).set("text", "Glasses").set("mnemonic", KeyEvent.VK_G);
        clist.data("index", 1).data("flag", 'g').group("choice");

        clist = squery.get(new JCheckBox()).set("text", "Hair").set("mnemonic", KeyEvent.VK_H);
        clist.data("index", 2).data("flag", 'h').group("choice");

        clist = squery.get(new JCheckBox()).set("text", "Teeth").set("mnemonic", KeyEvent.VK_T);
        clist.data("index", 3).data("flag", 't').group("choice");


        //Indicates what's on the geek.
        choices = new StringBuffer("cght");


        //Put the check boxes in a column in a panel
        JPanel checkPanel = new JPanel(new GridLayout(0, 1));
        
        // add all components in the choice group to the checkpanel
        squery.get(checkPanel).add(".choice");
        
        // set all components in the choice group to be selected
        squery.get(".choice").set("selected", true);
        

        add(checkPanel, BorderLayout.LINE_START);
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        
        // make all the checkboxes added to this frame use this object as its item listener
        squery.get("JCheckBox").listen("item", this);
      
        squery.preprocess(CheckBoxDemo.class);
    }

    /** Listens to the check boxes. */
    public void itemStateChanged(ItemEvent e) {
        int index = 0;
        char c = '-';
        Object source = e.getItemSelectable();
        
        index = (Integer) squery.data((Component) source, "index");
        c = (Character) squery.data((Component) source, "flag");


        //Now that we know which button was pushed, find out
        //whether it was selected or deselected.
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            c = '-';
        }

        //Apply the change to the string.
        choices.setCharAt(index, c);

        printStatus();
    }

    protected void printStatus() {
        System.out.println(choices.toString());        
    }

    public static void main(String[] args) {
        SQuery.run("CheckBoxDemo", new CheckBoxDemo());
    }
}

