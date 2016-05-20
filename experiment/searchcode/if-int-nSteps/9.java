package tankds;

/**
 * This applet will demonstrate projectile motion
 * by displaying a cannon firing.
 * by Drew Dolgert for Dr. Michael Fowler of UVa, mf1i@virginia.edu
 * Copyright (C) Michael Fowler 1998 under Gnu Public License
 * http://www.gnu.org/copyleft/gpl.html
 **/

import java.applet.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

// The applet is the nerve center for the panels it displays.  It creates an object to
// calculate projectile locations, then connects inputs to it and an output.
// When the applet says it implements a Listener, it promises to include routines
// other objects can call when an event happens.
//
// The applet itself doesn't care about redrawing or sizes.  Each contained component will
// redraw itself.
public class ProjectileApplet extends Applet implements
				ActionListener, BuddyBar.BuddyListener, ItemListener {
	Projectile bullet; // This calculates the current position.
	PositionGraph arcs; // This is a Panel that displays the current position.
	BuddyBar velocityBuddy; // A buddy bar is a home grown component with label, textField, and Scrollbar.
	BuddyBar angleBuddy;
	BuddyBar massBuddy;
	Checkbox airCheckBox;
	Checkbox trailCheckBox;

	// init is called by the browser to initialize the applet.
	public void init() {
		ResourceBundle rb;
        String sResString, sUnitString;
        
		String sUseRB = this.getParameter("international");
		rb = null;
		if ((sUseRB == null) || !sUseRB.equalsIgnoreCase("no")) {
			String sLang = this.getParameter("language");
			String sCountry = this.getParameter("country");
			try {
				try {
					if (sLang == null) {
						rb = ResourceBundle.getBundle("ProjectileStrings",Locale.getDefault());
					} else {
						Locale thisloc;
						if (sCountry == null)
							thisloc = new Locale(sLang,"");
						else
							thisloc = new Locale(sLang,sCountry);
						rb = ResourceBundle.getBundle("ProjectileStrings",thisloc);
					}
				} catch (java.lang.ClassFormatError e) {
					System.out.println("Cannot load the ResourceBundle class.  Must be Netscape.");
					System.out.println("Turning off Internationalization.  Sorry.");
					rb = null;
				}
			} catch (MissingResourceException e) {
				System.out.println(e.getMessage());
				rb = null;
			}
		}
		// A border layout allows you to add panels to the north, south, center, etc.
		this.setLayout(new BorderLayout(5,5));
		this.setBackground(Color.white);

		// Create the position calculator.
		bullet = new Projectile();
		// Create a display panel that reads info from
		// the bullet.  Because a PositionGraph is a Panel,
		// it can be added to the Applet.
		arcs = new PositionGraph(bullet, rb);
		// add()ing a Panel places it visually in the applet window.
		// Center is just that, the center of the window.
		this.add("Center",arcs);

		// The p1 panel will contain the button and checkboxes.
		// It gets its own layout.  Then the whole panel will be added
		// to the applet.
		Panel p1 = new Panel(new FlowLayout(FlowLayout.RIGHT));
        if (rb != null) sResString = rb.getString("fire");
        else sResString = "Fire";
		Button b = new Button(sResString);
		b.setActionCommand("fire");
		// Here is the first Listener command.  "this" is a pointer to the applet itself.
		// By asking button b to add us as an ActionListener, we ask it to call us when
		// something happens.  It does that by calling the method that we, as ActionListeners,
		// promise to have, the actionPerformed method you will see below.  All the listener
		// interfaces work this way.
		b.addActionListener(this);

        if (rb != null) sResString = rb.getString("air");
        else sResString = "air resistance";
		airCheckBox = new Checkbox(sResString,false);
        if (rb != null) sResString = rb.getString("trails");
        else sResString = "show trails";
		trailCheckBox = new Checkbox(sResString,true);
		airCheckBox.addItemListener(this);
		trailCheckBox.addItemListener(this);
		p1.add(airCheckBox);
		p1.add(trailCheckBox);

		// This will hold four objects pushed to the bottom of the applet window.
		// Here is who contains whom.
		// applet - PositionGraph
		//        - panel3 (gridBag)  - panel1 (flowlayout) - checkbox
		//                                                  - checkbox
		//                            - button
		//                            - 3 velocityBuddy pieces
		//                            - 3 angleBuddy pieces
		//                            - 3 massBuddy pieces

		// Arguments are the min and max double values on the scrollbar,
		// the min and max double values you are allowed to enter by hand
		// in the textBox, and lastly the initial setting for both.
        if (rb != null) sResString = rb.getString("velocity");
        else sResString = "Velocity [m/s]";
		velocityBuddy = new BuddyBar(sResString,1,100,0,1000,50);

		// This listener isn't the same as the others.  I rolled my own b/c I
		// made a new buddyBar object, and it doesn't need to be fancy.  It works
		// pretty much the same way as the other listeners, though.
		velocityBuddy.addBuddyListener(this);
		
        if (rb != null) sResString = rb.getString("angle");
        else sResString = "Angle [degrees]";
		angleBuddy = new BuddyBar(sResString,0,90,0,90,70);
		angleBuddy.addBuddyListener(this);

        if (rb != null) sResString = rb.getString("mass");
        else sResString = "Mass [kg]";
		massBuddy = new BuddyBar(sResString,1,20,1,1000,10);
		massBuddy.addBuddyListener(this);

		Panel p3 = new Panel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4,4,4,4);
		c.gridx = 0; c.gridy = 0; c.gridwidth = 3; c.gridheight = 1;
		c.weighty = 0; c.weightx = 0;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;
		p3.add(p1,c);
		c.gridwidth = 1; c.gridx = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		p3.add(b,c);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.gridx = 0; c.gridy = 1;
		p3.add(velocityBuddy.getLabel(),c);
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 1;
		p3.add(velocityBuddy.getTextField(),c);
		c.anchor = GridBagConstraints.EAST;
		c.gridx=0; c.gridy = 2;
		p3.add(angleBuddy.getLabel(),c);
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 1;
		p3.add(angleBuddy.getTextField(),c);
		c.anchor = GridBagConstraints.EAST;
		c.gridx=0; c.gridy = 3;
		p3.add(massBuddy.getLabel(),c);
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 1;
		p3.add(massBuddy.getTextField(),c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth=2;
		c.gridx = 2; c.gridy = 1; c.weightx = 1;
		p3.add(velocityBuddy.getScrollbar(),c);
		c.gridy = 2;
		p3.add(angleBuddy.getScrollbar(),c);
		c.gridy = 3;
		p3.add(massBuddy.getScrollbar(),c);

		this.add("South",p3);

        String sParam = this.getParameter("threadsleep");
        if (sParam != null) {
            try {
                int iSleep = Integer.parseInt(sParam);
                bullet.setSleepTime(iSleep);
			} catch (NumberFormatException e) {
			    System.out.println("threadsleep should be an integer number of milliseconds.");
			}
        }
        sParam = this.getParameter("numsteps");
        if (sParam != null) {
            try {
                int nSteps = Integer.parseInt(sParam);
                bullet.setNumSteps(nSteps);
			} catch (NumberFormatException e) {
			    System.out.println("numsteps should be an integer number of timesteps.");
			}
        }

        sParam = this.getParameter("markfrequency");
        if (sParam != null) {
            try {
                int nSteps = Integer.parseInt(sParam);
                bullet.setMarkFrequency(nSteps);
			} catch (NumberFormatException e) {
			    System.out.println("markfrequency should be an integer like 5 or 10.");
			}
        }
	}

	public String getAppletInfo() {
		return "ProjectileApplet v. 1.0.  Written by Drew Dolgert for Dr. Michael Fowler.\n"+
		"Copyright (C) Michael Fowler mf1i@virginia.edu 1998\n"+
		"Under Gnu Public License http://www.gnu.org/copyleft/gpl.html";
	}
	static final String[][] parameterInfo = {
			{"international","yes or no [yes]", "Setting this to \"no\" turns off internationalization "+
					"in case your browser cannot handle it."},
			{"language","two letter language code", "EN for English, FR for French, etc."},
			{"country","two letter country code", "CN for China, IT for Italy.  Not required."},
			{"threadsleep","integer [20]","Number of milliseconds the gas thread should sleep."},
			{"markfrequency","integer [8]","How many timesteps per trail mark."},
			{"numsteps","integer [125]","Number of timesteps for the ball to travel the "+
			        "a full arc."}
		};
		
	public String[][] getParameterInfo() {
		return parameterInfo;
	}

	// This is called when someone clicks on a button.  We listen to the only button
	// we have, which is the fire button.  All info gets passed on to the projectile.
	public void actionPerformed(ActionEvent e) {
		bullet.setVelocity(velocityBuddy.getValue());
		bullet.setAngle(angleBuddy.getValue()*Math.PI/180);
		bullet.setMass(massBuddy.getValue());
		bullet.fire();
	}

	// This is my imitation of a listener method.  When the buddy bar changed, this
	// gets called with the current value of that bar.  Note we need to search through
	// our buddy bars to see which one it is.  When we get the info, we pass it on to
	// the Projectile calculator.
	public void buddyValueChanged(BuddyBar changer, double dVal)
	{
		if (changer==velocityBuddy) {
			bullet.setVelocity(dVal);
		} else if (changer == angleBuddy) {
			bullet.setAngle(dVal*Math.PI/180);
		} else if (changer == massBuddy) {
			bullet.setMass(dVal);
		}
	}

	// This is a standard listener for checkboxes.  Info here is passed on to the projectile.
	// Note I could make a slider for the projectile resistance if I wanted.
	public void itemStateChanged(ItemEvent evt) {
		if (airCheckBox == evt.getSource()) {
			if (airCheckBox.getState())
				bullet.setResistance(0.5);
			else
				bullet.setResistance(0.0);
		} else {
			if (trailCheckBox.getState())
				arcs.setTrails(true);
			else
				arcs.setTrails(false);
		}
	}

}


