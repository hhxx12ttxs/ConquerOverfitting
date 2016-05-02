/***********************************************************************
 *Author: Chris Rees and Wilfredo Velasquez
 *Date: 12/2/08
 *File Name: CharSheet.java
 *Purpose: Show character's current stats and allow incrementing of stats
 *with stat points after each level.
***********************************************************************/

package com.serneum.rpg.character;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class CharSheet extends JFrame
{
    JLabel name, race, classChoice, blank1, level, exp, nextLvl;
    JLabel blank2, str, wis, endur, intel, dex;
    JLabel blank3, endMod, strMod, wisMod, dexMod, intMod, blank4;
    JLabel strB, endB, wisB, dexB, intB;
    JLabel blank5, blank6, blank7, blank8, blank9, blank10, blank11, points;

    JButton inc1, inc2, inc3, inc4, inc5;
    JButton dec1, dec2, dec3, dec4, dec5;

    JButton accept, cancel;

    JTextArea strF, endurF, dexF, intelF, wisF;

    private static String numHold = "";
    private static int numStore = 0;
    private static int count = 0;
    private static int ok = 0;

    public static boolean charOpen = false;

    public CharSheet()
    {
        //Import images to use for buttons
        URL decUrl = CharSheet.class.getResource("/images/dec.gif");
        URL incUrl = CharSheet.class.getResource("/images/inc.gif");
        ImageIcon dec = new ImageIcon(decUrl);
        ImageIcon inc = new ImageIcon(incUrl);

        //Run basic stat printing and generation for use in the JLabel initialization
        //(Makes sure all stats are up to date)
        try
        {
            Stats.printStats();
            Stats.modGen();
            Stats.getMin();
        }
        catch(FileNotFoundException FNFE)
        {
        }

        //Creates Accept and Cancel buttons
        accept = new JButton("Accept");
        cancel = new JButton("Cancel");

        //Create the basic character info  labels and a space
        name = new JLabel("Character: " + Stats.charName.toString());
        race = new JLabel("Race: " + Stats.race.toString());
        classChoice = new JLabel("Class: " + Stats.classChoice.toString());
        blank1 = new JLabel("");

        //Create level info labels and a space
        level = new JLabel("Level: " + Stats.level);
        exp = new JLabel("EXP: " + Stats.EXP);
        nextLvl = new JLabel("Next Level: " + Stats.nextLevel);
        blank2 = new JLabel("");

        //Create stat info labels and a space
        str = new JLabel("Strength: " + Stats.str);
        endur = new JLabel("Endurance: " + Stats.endur);
        dex = new JLabel("Dexterity: " + Stats.dex);
        intel = new JLabel("Intelligence: " + Stats.intel);
        wis = new JLabel("Wisdom: " + Stats.wis);
        blank3 = new JLabel("");

        //Create stat mod labels and a space
        strMod = new JLabel("Strength Mod: " + Stats.strMod);
        endMod = new JLabel("Endurance Mod: " + Stats.endMod);
        dexMod = new JLabel("Dexterity Mod: " + Stats.dexMod);
        intMod = new JLabel("Intelligence Mod: " + Stats.intMod);
        wisMod = new JLabel("Wisdom Mod: " + Stats.wisMod);
        blank4 = new JLabel("");

        //Create small labels to show stat in the upgrade section
        strB = new JLabel("Strength:");
        endB = new JLabel("Endurance:");
        dexB = new JLabel("Dexterity:");
        intB = new JLabel("Intelligence:");
        wisB = new JLabel("Wisdom:");

        //Create extra blank labels for space
        blank5 = new JLabel("");
        blank6 = new JLabel("");
        blank7 = new JLabel("");
        blank8 = new JLabel("");
        blank9 = new JLabel("");
        blank10 = new JLabel("");
        blank11 = new JLabel("");

        //Create label to show points left to spend
        points = new JLabel("Stat Points: " + Stats.statPoints);

        //Create the 5 text fields for the stats
        strF = new JTextArea(1,10);
        endurF = new JTextArea(1,10);
        dexF = new JTextArea(1,10);
        intelF = new JTextArea(1,10);
        wisF = new JTextArea(1,10);

        strF.append("" + Stats.str);
        endurF.append("" + Stats.endur);
        dexF.append("" + Stats.dex);
        intelF.append("" + Stats.intel);
        wisF.append("" + Stats.wis);

        //Set the 5 text fields to uneditable
        strF.setEditable(false);
        endurF.setEditable(false);
        dexF.setEditable(false);
        intelF.setEditable(false);
        wisF.setEditable(false);

        //Create the 5 increment buttons
        inc1 = new JButton(inc);
        inc2 = new JButton(inc);
        inc3 = new JButton(inc);
        inc4 = new JButton(inc);
        inc5 = new JButton(inc);

        //create the 5 decrement buttons
        dec1 = new JButton(dec);
        dec2 = new JButton(dec);
        dec3 = new JButton(dec);
        dec4 = new JButton(dec);
        dec5 = new JButton(dec);

        //Create button handler
        buttonHandler bHandler;
        bHandler = new buttonHandler();

        //Add increment buttons to listener
        inc1.addActionListener(bHandler);
        inc2.addActionListener(bHandler);
        inc3.addActionListener(bHandler);
        inc4.addActionListener(bHandler);
        inc5.addActionListener(bHandler);

        //Add decrement buttons to listener
        dec1.addActionListener(bHandler);
        dec2.addActionListener(bHandler);
        dec3.addActionListener(bHandler);
        dec4.addActionListener(bHandler);
        dec5.addActionListener(bHandler);

        //Add accept and continue buttons to listener
        accept.addActionListener(bHandler);
        cancel.addActionListener(bHandler);

        //Create the content pane to show buttons and labels
        Container pane = getContentPane();
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        //Set constraints and locations of each individual element.
        //If some constraints are to be the same, do not repeat their
        //value inputs.
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;

        //Add all the data fields to the pane
        c.gridx = 1;
        c.gridy = 0;
        pane.add(name, c);
        c.gridy = 1;
        pane.add(race, c);
        c.gridy = 2;
        pane.add(classChoice, c);
        c.gridy = 3;
        pane.add(blank1, c);
        c.gridy = 4;
        pane.add(blank2, c);
        c.gridy = 5;
        pane.add(level, c);
        c.gridy = 6;
        pane.add(exp, c);
        c.gridy = 7;
        pane.add(nextLvl, c);
        c.gridy = 8;
        pane.add(blank3, c);
        c.gridy = 9;
        pane.add(blank4, c);
        c.gridy = 10;
        pane.add(str, c);
        c.gridy = 11;
        pane.add(endur, c);
        c.gridy = 12;
        pane.add(dex, c);
        c.gridy = 13;
        pane.add(intel, c);
        c.gridy = 14;
        pane.add(wis, c);
        c.gridy = 15;
        pane.add(blank5, c);
        c.gridy = 16;
        pane.add(blank6, c);
        c.gridy = 17;
        pane.add(strMod, c);
        c.gridy = 18;
        pane.add(endMod, c);
        c.gridy = 19;
        pane.add(dexMod, c);
        c.gridy = 20;
        pane.add(intMod, c);
        c.gridy = 21;
        pane.add(wisMod, c);
        c.gridy = 22;
        pane.add(blank7, c);
        c.gridy = 23;
        pane.add(blank8, c);
        c.gridy = 24;
        pane.add(points, c);
        c.gridy = 25;
        pane.add(blank9);
        c.gridy = 26;
        pane.add(blank10);

        //Add the  decrement buttons
        c.gridx = 0;
        c.gridy = 28;
        c.anchor = GridBagConstraints.CENTER;
        pane.add(dec1, c);
        c.gridy = 30;
        pane.add(dec2, c);
        c.gridy = 32;
        pane.add(dec3, c);
        c.gridy = 34;
        pane.add(dec4, c);
        c.gridy = 36;
        pane.add(dec5, c);

        //Add the stat fields
        c.gridx = 1;
        c.gridy = 28;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        pane.add(strF, c);
        c.gridy = 30;
        pane.add(endurF, c);
        c.gridy = 32;
        pane.add(dexF, c);
        c.gridy = 34;
        pane.add(intelF, c);
        c.gridy = 36;
        pane.add(wisF, c);

        //Add the increment buttons
        c.gridx = 2;
        c.gridy = 28;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        pane.add(inc1, c);
        c.gridy = 30;
        pane.add(inc2, c);
        c.gridy = 32;
        pane.add(inc3, c);
        c.gridy = 34;
        pane.add(inc4, c);
        c.gridy = 36;
        pane.add(inc5, c);

        //Add the stat name labels
        c.gridy = 27;
        c.gridx = 1;
        c.anchor = GridBagConstraints.CENTER;
        pane.add(strB, c);
        c.gridy = 29;
        pane.add(endB, c);
        c.gridy = 31;
        pane.add(dexB, c);
        c.gridy = 33;
        pane.add(intB, c);
        c.gridy = 35;
        pane.add(wisB, c);

        //Add accept and cancel buttons
        c.gridx = 0;
        c.gridy = 37;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        pane.add(blank11);
        c.gridy = 40;
        c.gridwidth = 1;
        pane.add(cancel, c);
        c.gridx = 2;
        pane.add(accept, c);

        setSize(350,650);
        setTitle("Character Sheet");
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private class buttonHandler implements ActionListener
      {
          @Override
        public void actionPerformed(ActionEvent e)
          {
              //If increment or decrement buttons are pressed,
              //adjust the stat points, and the stored number
              //so that the line can show the change, and be increased
              //or decreased again up until Accept is clicked and the
              //error check is confirmed
              if(e.getSource() == dec1)
              {
                  numHold = strF.getText();
                  numStore = Integer.parseInt(numHold);

                  if(numStore > Stats.minStr && Stats.statPoints >= 0)
                  {
                      count--;
                      numStore--;
                      Stats.statPoints++;
                      numHold = "" + numStore;
                      points.setText("Stat Points: " + Stats.statPoints);
                  }

                  strF.setText(numHold);
              }

              if(e.getSource() == dec2)
              {
                  numHold = endurF.getText();
                  numStore = Integer.parseInt(numHold);

                  if(numStore > Stats.minEnd && Stats.statPoints >= 0)
                  {
                      count--;
                      numStore--;
                      Stats.statPoints++;
                      numHold = "" + numStore;
                      points.setText("Stat Points: " + Stats.statPoints);
                  }

                  endurF.setText(numHold);
              }

              if(e.getSource() == dec3)
              {
                  numHold = dexF.getText();
                  numStore = Integer.parseInt(numHold);

                  if(numStore > Stats.minDex && Stats.statPoints >= 0)
                  {
                      count--;
                      numStore--;
                      Stats.statPoints++;
                      numHold = "" + numStore;
                      points.setText("Stat Points: " + Stats.statPoints);
                  }

                  dexF.setText(numHold);
              }

              if(e.getSource() == dec4)
              {
                  numHold = intelF.getText();
                  numStore = Integer.parseInt(numHold);

                  if(numStore > Stats.minInt && Stats.statPoints >= 0)
                  {
                      count--;
                      numStore--;
                      Stats.statPoints++;
                      numHold = "" + numStore;
                      points.setText("Stat Points: " + Stats.statPoints);
                  }

                  intelF.setText(numHold);
              }

              if(e.getSource() == dec5)
              {
                  numHold = wisF.getText();
                  numStore = Integer.parseInt(numHold);

                  if(numStore > Stats.minWis && Stats.statPoints >= 0)
                  {
                      count--;
                      numStore--;
                      Stats.statPoints++;
                      numHold = "" + numStore;
                      points.setText("Stat Points: " + Stats.statPoints);
                  }

                  wisF.setText(numHold);
              }

              if(e.getSource() == inc1)
              {
                  numHold = strF.getText();
                  numStore = Integer.parseInt(numHold);

                  if(Stats.statPoints > 0)
                  {
                      count++;
                      numStore++;
                      Stats.statPoints--;
                      numHold = "" + numStore;
                      points.setText("Stat Points: " + Stats.statPoints);
                  }

                  strF.setText(numHold);
              }

              if(e.getSource() == inc2)
              {
                  numHold = endurF.getText();
                  numStore = Integer.parseInt(numHold);

                  if(Stats.statPoints > 0)
                  {
                      count++;
                      numStore++;
                      Stats.statPoints--;
                      numHold = "" + numStore;
                      points.setText("Stat Points: " + Stats.statPoints);
                  }

                  endurF.setText(numHold);
              }

              if(e.getSource() == inc3)
              {
                  numHold = dexF.getText();
                  numStore = Integer.parseInt(numHold);

                  if(Stats.statPoints > 0)
                  {
                      count++;
                      numStore++;
                      Stats.statPoints--;
                      numHold = "" + numStore;
                      points.setText("Stat Points: " + Stats.statPoints);
                  }

                  dexF.setText(numHold);
              }

              if(e.getSource() == inc4)
              {
                  numHold = intelF.getText();
                  numStore = Integer.parseInt(numHold);

                  if(Stats.statPoints > 0)
                  {
                      count++;
                      numStore++;
                      Stats.statPoints--;
                      numHold = "" + numStore;
                      points.setText("Stat Points: " + Stats.statPoints);
                  }

                  intelF.setText(numHold);
              }

              if(e.getSource() == inc5)
              {
                  numHold = wisF.getText();
                  numStore = Integer.parseInt(numHold);

                  if(Stats.statPoints > 0)
                  {
                      count++;
                      numStore++;
                      Stats.statPoints--;
                      numHold = "" + numStore;
                      points.setText("Stat Points: " + Stats.statPoints);
                  }

                  wisF.setText(numHold);
              }

              if(e.getSource() == cancel)
              {
                  charOpen = false;
                  dispose();
              }

              if(e.getSource() == accept)
              {
                  try
                  {
                      //Save all stats in variables.
                      numHold = strF.getText();
                      Stats.str = Integer.parseInt(numHold);
                      numHold = endurF.getText();
                      Stats.endur = Integer.parseInt(numHold);
                      numHold = dexF.getText();
                      Stats.dex = Integer.parseInt(numHold);
                      numHold = intelF.getText();
                      Stats.intel = Integer.parseInt(numHold);
                      numHold = wisF.getText();
                      Stats.wis = Integer.parseInt(numHold);

                      if(count > 0)
                      {
                          //Placeholder frame...serves no purpose
                          Frame frame = new Frame();

                          //Define button text
                          Object[] options = {"Yes, keep them", "I'm not ready yet"};

                          //Present user with option to save stats or not
                          ok = JOptionPane.showOptionDialog(frame, "Are you sure you like these stats?\n(This will make you unable to lower them anymore)",
                              "Confirm Stats", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                      }

                      //If user clicks ok, then print stats and set new minimum values.
                      if(ok == JOptionPane.YES_OPTION)
                      {
                          count = 0;
                          Stats.printStats();
                          Stats.modGen();
                          Stats.minStatsPrint();
                          charOpen = false;
                          dispose();
                      }

                      if(ok == JOptionPane.NO_OPTION)
                      {
                          //Temporary stat variables
                          int str, end, dex, intel, wis;
                          //Temporary differnce variables (D stands for difference)
                          int sD, eD, dD, iD, wD;
                          //A temporary variable to figure out how many points to give back
                          int returnPoints;

                          //Store integers of the text fields
                          str = Integer.parseInt(strF.getText());
                          end = Integer.parseInt(endurF.getText());
                          dex = Integer.parseInt(dexF.getText());
                          intel = Integer.parseInt(intelF.getText());
                          wis = Integer.parseInt(wisF.getText());

                          //Determine difference between whats in the text field and the minimum
                          sD = str - Stats.minStr;
                          eD = end - Stats.minEnd;
                          dD = dex - Stats.minDex;
                          iD = intel - Stats.minInt;
                          wD = wis - Stats.minWis;

                          //Reset the text fields to the minimums
                          strF.setText("" + Stats.minStr);
                          endurF.setText("" + Stats.minEnd);
                          dexF.setText("" + Stats.minDex);
                          intelF.setText("" + Stats.minInt);
                          wisF.setText("" + Stats.minWis);

                          //Determine the amount of points to return
                          returnPoints = sD + eD + dD + iD + wD;

                          //Reload the data in the label
                          Stats.statPoints += returnPoints;
                          points.setText("Stat Points: " + Stats.statPoints);

                          Stats.printStats();
                      }

                  }
                  catch(FileNotFoundException FNFE)
                  {

                  }
              }
          }
      }
 /*
    public static void main(String[] args)
    {
        CharSheet sheet = new CharSheet();
    }
*/
}
