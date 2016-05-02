// AForceMenu.java
// Jim Sproch
// Created: April 29, 2006
// Modified: March 30, 2008
// Part of the Aforce Port
// Mac < Windows < Linux

/**
	AForceMenu is the top menu bar at the top of the AForce window!
	@author Jim Sproch
	@version 0.1a beta
*/


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import javax.swing.text.*;
import javax.swing.event.*;
import java.text.BreakIterator;

import java.lang.reflect.Method;	//Needed for opening web browser

public class AForceMenu implements ActionListener
{
	private final int ITEM_PLAIN = 0;	// Item types
	private final int ITEM_CHECK = 1;
	private final int ITEM_RADIO = 2;

	private static AForce aforce;
	JPanel topPanel;
	JMenuBar menuBar;

	JMenu menuGame;
		JMenu menuGameNewGame;
			JMenuItem menuGameNewGameSingle;
			JMenuItem menuGameNewGameMulti;
			JMenuItem menuGameNewGameCustom;
		JMenuItem menuGameNew;
		JMenuItem menuGamePauseResume;
		JMenuItem menuGameScores;
		JMenuItem menuGameOpen;
		JMenuItem menuGameScreenShot;
		JMenuItem menuGameSave;
		JMenuItem menuGameSaveAs;
		JMenuItem menuGameExit;

	JMenu menuOptions;
		JMenuItem menuOptionsSingle;
		JMenuItem menuOptionsMulti;

	JMenu menuHelp;
		JMenuItem menuHelpHandbook;
		JMenuItem menuHelpTipoftheDay;
		JMenuItem menuHelpBugReport;
		JMenuItem menuHelpDonate;
		JMenuItem menuHelpAbout;




	public AForceMenu(Display frame, final AForce owner)
	{
		aforce = owner;

		// Create the menu bar
		menuBar = new JMenuBar();

		// Set this instance as the application's menu bar
		frame.setJMenuBar(menuBar);
		
		// Build the property sub-menu
		menuGameNewGame = new JMenu("New");
		menuGameNewGame.setMnemonic('N');

		// Create property items
		menuGameNewGameSingle = CreateMenuItem( menuGameNewGame, ITEM_PLAIN, "Single-Player Game", null, 'S', null, AForceMenu.Identification.NewSinglePlayerGame);
		menuGameNewGameMulti = CreateMenuItem( menuGameNewGame, ITEM_PLAIN, "Multi-Player Game", null, 'M', null, AForceMenu.Identification.NewMultiPlayerGame);
		menuGameNewGameCustom = CreateMenuItem( menuGameNewGame, ITEM_PLAIN, "Custom (Requires Root)", null, 'C', null, AForceMenu.Identification.NewCustomGame);
		
		// Create the file menu
		menuGame = new JMenu("Game");
		menuGame.setMnemonic('G');
		menuBar.add(menuGame);

		menuGame.add(menuGameNewGame);	


		menuGamePauseResume = CreateMenuItem(menuGame, ITEM_PLAIN, "Pause/Resume", null, 'r', "Pause/Resume Game", AForceMenu.Identification.PauseResume);

		menuGamePauseResume = CreateMenuItem(menuGame, ITEM_PLAIN, "High Scores", null, 'r', "High Score List", AForceMenu.Identification.HighScores);

		menuGameScreenShot = CreateMenuItem(menuGame, ITEM_PLAIN, "Capture ScreenShot", null, 'c', "Capture ScreenShot", AForceMenu.Identification.ScreenShot);

		menuGameSave = CreateMenuItem(menuGame, ITEM_PLAIN, "Save", null, 's', "Save", AForceMenu.Identification.Save);

		menuGameSaveAs = CreateMenuItem(menuGame, ITEM_PLAIN, "Save As", null, 'a', "Save As", AForceMenu.Identification.SaveAs);

		menuGameExit = CreateMenuItem(menuGame, ITEM_PLAIN, "Exit", null, 'x', "Exit the program", AForceMenu.Identification.EXIT);



		// Create the Options menu
		menuOptions = new JMenu("Options");
		menuOptions.setMnemonic('O');
		menuBar.add(menuOptions);

		// Create edit menu options
		menuOptionsSingle = CreateMenuItem(menuOptions, ITEM_PLAIN, "Single-Player Options", null, 'S', "Options for single-player games", AForceMenu.Identification.SinglePlayerOptions);
		menuOptionsMulti = CreateMenuItem(menuOptions, ITEM_PLAIN, "Multi-Player Options", null, 'M', "Options for multi-player lan games", AForceMenu.Identification.MultiPlayerOptions);


		// Create the Help menu
		menuHelp = new JMenu("Help");
		menuHelp.setMnemonic('H');
		menuBar.add(menuHelp);


		// Create edit menu options
		menuHelpHandbook = CreateMenuItem( menuHelp, ITEM_PLAIN, "AForce Handbook", null, 'H', "AForce Handbook", AForceMenu.Identification.HelpHandbook);
		menuHelpTipoftheDay = CreateMenuItem( menuHelp, ITEM_PLAIN, "Tip of the Day", null, 'T', "Tip of the Day", AForceMenu.Identification.HelpTipOfTheDay);
		menuHelpBugReport = CreateMenuItem( menuHelp, ITEM_PLAIN, "Report a Bug", null, 'B', "Bug reporting", AForceMenu.Identification.HelpBugReporting);
		menuHelpDonate = CreateMenuItem( menuHelp, ITEM_PLAIN, "Donate :)", null, 'B', "Please Donate!", AForceMenu.Identification.HelpDonate);
		menuHelpAbout = CreateMenuItem( menuHelp, ITEM_PLAIN, "About", null, 'A', "About AForce", AForceMenu.Identification.HelpAboutAForce);

		MouseListener mouselistener = new MouseListener()
		{
			// The menus gain keyboard focus when they are opened, in which case
			//  we should pause the game!  We will resume it when the menus are closed.
				public void mouseClicked(MouseEvent e){}
				public void mouseEntered(MouseEvent e){}
				public void mouseExited(MouseEvent e){}
				public void mousePressed(MouseEvent e){if(AForce.clicker.isRunning()) AForce.pause();}
				public void mouseReleased(MouseEvent e){}
		};

		menuGame.addMouseListener(mouselistener);
		menuOptions.addMouseListener(mouselistener);
		menuHelp.addMouseListener(mouselistener);
	}




	static class KeepUp implements WindowFocusListener, WindowListener
	{
	
		boolean done = false;

		public KeepUp(){} // Constructor
	
		public void windowLostFocus(WindowEvent e)
		{
			if(!done) ((JFrame)e.getSource()).toFront();
		}
	
		public void windowGainedFocus(WindowEvent e){}
		public void windowActivated(WindowEvent e){}
		public void windowClosed(WindowEvent e){done=true;}
		public void windowClosing(WindowEvent e){done=true;}
		public void windowDeactivated(WindowEvent e){}
		public void windowDeiconified(WindowEvent e){}
		public void windowIconified(WindowEvent e){}
		public void windowOpened(WindowEvent e){}
	
	}


	public JMenuItem CreateMenuItem(JMenu menu, int iType, String sText, ImageIcon image, int acceleratorKey, String sToolTip, String idkey)
	{
		// Create the item
		JMenuItem menuItem;

		switch(iType)
		{
			case ITEM_RADIO:
				menuItem = new JRadioButtonMenuItem();
				break;

			case ITEM_CHECK:
				menuItem = new JCheckBoxMenuItem();
				break;

			default:
				menuItem = new JMenuItem();
				break;
		}

		// Add the item test
		menuItem.setText(sText);

		// Add the optional icon
		if(image != null) menuItem.setIcon(image);

		// Add the accelerator key
		if(acceleratorKey > 0) menuItem.setMnemonic( acceleratorKey );

		// Add the optional tool tip text
		if(sToolTip != null) menuItem.setToolTipText(sToolTip);

		// adds the identification key (so we can see what was clicked
		menuItem.setActionCommand(idkey);

		// Add an action handler to this menu item
		menuItem.addActionListener(this);

		menu.add(menuItem);

		return menuItem;
	}

	public void setowner(AForce owner)
	{
		aforce = owner;
	}

	public void actionPerformed(ActionEvent event)
	{
		if(event.getActionCommand() == Identification.NewSinglePlayerGame)
		{
			aforce.newGame();
			return;
		}

		if(event.getActionCommand() == Identification.NewMultiPlayerGame)
		{
			new Login();
			return;
		}

		if(event.getActionCommand() == Identification.NewCustomGame)
		{
			new Login();
			return;
		}

		if(event.getActionCommand() == Identification.EXIT)
		{
			aforce.destroy();
			return;
		}

		if(event.getActionCommand() == Identification.SinglePlayerOptions)
		{
			new Login();
			return;
		}

		if(event.getActionCommand() == Identification.MultiPlayerOptions)
		{
			new Login();
			return;
		}

		if(event.getActionCommand() == Identification.HelpHandbook)
		{
			new WebBrowser("http://www.aforce2.com/help.php");
			return;
		}

		if(event.getActionCommand() == Identification.HelpTipOfTheDay)
		{
			new TipOfTheDay();
			return;
		}

		if(event.getActionCommand() == Identification.HelpBugReporting)
		{
			new WebBrowser("http://www.aforce2.com/bugreport.php");
			return;
		}

		if(event.getActionCommand() == Identification.HelpDonate)
		{
			new WebBrowser("http://www.aforce2.com/donate.php");
			return;
		}

		if(event.getActionCommand() == Identification.HelpAboutAForce)
		{
			new About();
			return;
		}

		if(event.getActionCommand() == Identification.PauseResume)
		{
			AForce.pause();
			return;
		}

		if(event.getActionCommand() == Identification.HighScores)
		{
			aforce.getScoreBoard().buildGUI(aforce);
			return;
		}

		if(event.getActionCommand() == Identification.Save)
		{
			new Login();
			return;
		}

		if(event.getActionCommand() == Identification.SaveAs)
		{
			new Login();
			return;
		}

		if(event.getActionCommand() == Identification.ScreenShot)
		{
			AForce.getFrame().saveSnapShot();
			return;
		}

		Printer.err.println("ERROR 101: Event Not Identified... #AForceMenu.actionPerformed()");
	}

	public static void main(String args[])
	{
		Printer.noexecute();
	}


	public static void promptplayagain(AForce owner)
	{
		AForce.getClicker().stop();
		JOptionPane.showMessageDialog(null, "Game Over!\n  Sorry, but you didn't make the high score list.\n   Better luck next time!", "Game Over!", JOptionPane.ERROR_MESSAGE);
		owner.newGame();
	}


	public static void madeHighScoreList(JFrame owner, boolean madelist)
	{
		AForce.getClicker().stop();
		if(madelist) JOptionPane.showMessageDialog(owner, "Congratulations,\n  You made the high score list!", "Congratulations!", JOptionPane.ERROR_MESSAGE);
		else JOptionPane.showMessageDialog(owner, "Game Over!\n  Sorry, but you didn't make the high score list.\n   Better luck next time!", "Game Over!", JOptionPane.ERROR_MESSAGE);
		aforce.newGame();
	}


	static class Identification
	{
		public static final String NewSinglePlayerGame = "NewSinglePlayerGame";
		public static final String NewMultiPlayerGame = "NewMultiPlayerGame";
		public static final String NewCustomGame = "NewCustomGame";
		public static final String EXIT = "EXIT";
		public static final String SinglePlayerOptions = "SinglePlayerOptions";
		public static final String MultiPlayerOptions = "MultiPlayerOptions";
		public static final String HelpHandbook = "HelpHandbook";
		public static final String HelpTipOfTheDay = "HelpTipOfTheDay";
		public static final String HelpBugReporting = "HelpBugReporting";
		public static final String HelpDonate = "HelpDonate";
		public static final String HelpAboutAForce = "HelpAboutAForce";
		public static final String OK = "OK";
		public static final String PauseResume = "PauseResume";
		public static final String HighScores = "HighScores";
		public static final String Save = "Save";
		public static final String SaveAs = "SaveAs";
		public static final String ScreenShot = "ScreenShot";
	}



	public static void wrapLabelText(JLabel label, String text)
	{
		FontMetrics fm = label.getFontMetrics(label.getFont());
		int containerWidth = label.getWidth();
	
		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(text);
	
		StringBuffer trial = new StringBuffer();
		StringBuffer real = new StringBuffer("<html>");
	
		int start = boundary.first();
		for (int end = boundary.next(); end != BreakIterator.DONE;
			start = end, end = boundary.next()) {
			String word = text.substring(start,end);
			trial.append(word);
			int trialWidth = SwingUtilities.computeStringWidth(fm,
				trial.toString());
			if (trialWidth > containerWidth) {
				trial = new StringBuffer(word);
				real.append("<br>  ");
			}
			real.append(word);
		}
	
		real.append("</html>");
	
		label.setText(real.toString());
	}

	// Special Thanks to Dem for massive help with this WebBrowser class
	//   http://www.centerkey.com/java/browser/
	class WebBrowser
	{
		WebBrowser(String url)
		{
			String osName = System.getProperty("os.name");
			url += "?os="+osName;
			try
			{
				if (osName.startsWith("Mac OS"))
				{
					Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
					Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
					openURL.invoke(null, new Object[] {url});
				}
				else if (osName.startsWith("Windows"))
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
				else {
					//assume Unix or Linux
					String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
					String browser = null;
					for (int count = 0; count < browsers.length && browser == null;count++)
						if (Runtime.getRuntime().exec( new String[] {"which", browsers[count]}).waitFor() == 0)
							browser = browsers[count];
					if (browser == null) Printer.err.println("ERROR 521: Web Browser Not Identified #AForceMenu.WebBrowser.WebBrowser()");
					else Runtime.getRuntime().exec(new String[] {browser, url});
				}
			}
			catch (Exception e)
			{
				Printer.err.println("ERROR 522: Could not launch Web Browser! #AForceMenu.WebBrowser.WebBrowser");
			}
		}
	}


	class Login implements DocumentListener, ActionListener
	{
		private JFrame frame;
		private JTextField field1;
		private JPasswordField field2;
		private JButton button1;
		private JLabel label1;
		private JLabel label2;
	
		public Login()
		{
			// NOTE:  In order to create the desired output, this example
			// uses a NULL layout manager and hard-codes the sizes and
			// positions of components.  This is NOT something you want
			// to do in production code.

			frame = new JFrame();

			frame.setTitle("Please Login!");
			frame.setSize( 300, 190 );
			frame.setLocation(new Point(AForce.getFrame().getX()+frame.getX()+(int)frame.getSize().getWidth()/2, AForce.getFrame().getY()+frame.getY()+(int)frame.getSize().getHeight()));
			frame.getContentPane().setBackground(Color.gray);
			frame.setResizable(false);
			frame.addWindowListener(new AForceMenu.KeepUp());
			frame.addWindowFocusListener((WindowFocusListener)frame.getWindowListeners()[0]);
	
			JPanel topPanel = new JPanel();
			topPanel.setLayout(null);
			frame.getContentPane().add(topPanel);
	
			// Create a field and label
			field1 = new JTextField();
			field1.setBounds( 20, 40, 260, 25 );
			field1.setFocusAccelerator( 'v' );
			topPanel.add(field1);
	
			label1 = new JLabel("Username:");
			label1.setBounds(20, 15, 260, 20);
			label1.setLabelFor(field1);
			label1.setDisplayedMnemonic('V');
			topPanel.add(label1);
	
			// Create a second label and text field
			field2 = new JPasswordField();
			field2.setBounds(20, 90, 260, 25);
			field2.setFocusAccelerator('a');
			topPanel.add(field2);
	
			label2 = new JLabel("Password:");
			label2.setDisplayedMnemonic('a');
			label2.setBounds(20, 65, 260, 20);
			label2.setLabelFor(field2);
			topPanel.add(label2);
	
	
			// Create a button and add it to the panel
			button1 = new JButton("OK");
			button1.setBounds(100, 130, 100, 25);
			button1.setEnabled(false);
			button1.setActionCommand(Identification.OK);
			button1.addActionListener(this);
			topPanel.add(button1);
	
			// Add a document listener
			Document document;
			document = field1.getDocument();
			document.addDocumentListener(this);
			document = field2.getDocument();
			document.addDocumentListener(this);
			
			frame.setVisible(true);
		}
	
		// Handle keyboard accelerators
		public void actionPerformed( ActionEvent event )
		{
			if(event.getActionCommand() == Identification.OK)
			{
				JOptionPane.showMessageDialog(frame, "ERROR: Access Denied!\n  Invalid username or password", "ERROR: Access Denied!", JOptionPane.ERROR_MESSAGE);
				frame.dispose();
			}
		}
	
		// Handle insertions into the text field
		public void insertUpdate( DocumentEvent event )
		{
			String username = "";
			String password = "";

			if(field1 != null && field1.getText() != null)
				username = field1.getText();
			if(field2 != null && field2.getPassword() != null)
				password = new String(field2.getPassword());

			if(username.length() >= 3 && password.length() >= 3) button1.setEnabled(true);
			else button1.setEnabled(false);
		}
	
		// Handle deletions	from the text field
		public void removeUpdate( DocumentEvent event )
		{
			insertUpdate(event);
		}
	
		// Handle changes to the text field
		public void changedUpdate( DocumentEvent event )
		{
			// Nothing to do here
		}
	}









	class TipOfTheDay implements DocumentListener, ActionListener
	{
		private JFrame frame;
		private JTextField field1;
		private JButton button1;
		private JLabel label1;
		private String[] tips = new String[9];

		public TipOfTheDay()
		{
			TipOfTheDayHelper(((int)(Math.random()*8454761)) % tips.length);
		}
	
		public TipOfTheDay(int tipnumber)
		{
			TipOfTheDayHelper(tipnumber);
		}

		private void TipOfTheDayHelper(int tipnumber)
		{
			// Tips!
			tips[0] = "You can: press 's' to stop your ship in mid-flight :)";
			tips[1] = "If you press 'a', your ship will fly on autopilot";
			tips[2] = "If you type 'java AForce Tux', you can play another map";
			tips[3] = "Free updates can be found at www.aforce2.com";
			tips[4] = "You can press 'p' to pause the game!";
			tips[5] = "You can write tips too, email them to tipoftheday@aforce2.com";
			tips[6] = "You can fire a laser by waiting 25 seconds and then pressing 'L'";
			tips[7] = "You can jump ahead extra fast by pressing 'w' (warp drive)";
			tips[8] = "You can place a mine by pressing 'm'";


			// NOTE:  In order to create the desired output, this example
			// uses a NULL layout manager and hard-codes the sizes and
			// positions of components.  This is NOT something you want
			// to do in production code.

			frame = new JFrame();

			frame.setTitle("Tip Number: "+tipnumber+"  ");
			frame.setSize( 300, 190 );
			frame.setLocation(new Point(AForce.getFrame().getX()+frame.getX()+(int)frame.getSize().getWidth()/2, AForce.getFrame().getY()+frame.getY()+(int)frame.getSize().getHeight()));
			frame.getContentPane().setBackground(Color.gray);
			frame.setResizable(false);
	
			JPanel topPanel = new JPanel();
			topPanel.setLayout(null);
			frame.getContentPane().add(topPanel);



			label1 = new JLabel();
			label1.setBounds(20, 15, 260, 100);
			wrapLabelText(label1, "Did you know...<BR><BR>   "+tips[tipnumber]);
			label1.setLabelFor(field1);
			label1.setDisplayedMnemonic('V');
			topPanel.add(label1);

	
			// Create a button and add it to the panel
			button1 = new JButton("OK");
			button1.setBounds(100, 130, 100, 25);
			button1.setActionCommand(Identification.OK);
			button1.addActionListener(this);
			topPanel.add(button1);
	
			frame.setVisible(true);
		}
	
		// Handle keyboard accelerators
		public void actionPerformed( ActionEvent event )
		{
			if(event.getActionCommand() == Identification.OK)
			{
				frame.dispose();
			}
		}
	
		// Handle insertions into the text field
		public void insertUpdate( DocumentEvent event )
		{
			// Nothing to do here
		}
	
		// Handle deletions from the text field
		public void removeUpdate( DocumentEvent event )
		{
			// Nothing to do here
		}
	
		// Handle changes to the text field
		public void changedUpdate( DocumentEvent event )
		{
			// Nothing to do here
		}
	}









	class About implements DocumentListener, ActionListener
	{
		private JFrame frame;
		private JTextField field1;
		private JButton button1;
		private JLabel label1;

		public About()
		{

			frame = new JFrame();

			frame.setTitle("About AForce! ");
			frame.setSize( 300, 220 );
			frame.setLocation(new Point(AForce.getFrame().getX()+frame.getX()+(int)frame.getSize().getWidth()/2, AForce.getFrame().getY()+frame.getY()+(int)frame.getSize().getHeight()));
			frame.getContentPane().setBackground(Color.gray);
			frame.setResizable(false);
	
			JPanel topPanel = new JPanel();
			topPanel.setLayout(null);
			frame.getContentPane().add(topPanel);



			label1 = new JLabel();
			label1.setBounds(10, 10, 260, 130);
			wrapLabelText(label1, "Alien Airforce (Original Version by Robert Epps), was rewritten by Jim Sproch in 2006.  Many improvements have been made to the game such as the ability to load maps, multiplayer, ability to save games, and an improved score board.  This game is copyright 2006 Jim Sproch, all rights reserved.");
			label1.setLabelFor(field1);
			label1.setDisplayedMnemonic('V');
			topPanel.add(label1);

	
			// Create a button and add it to the panel
			button1 = new JButton("OK");
			button1.setBounds(100, 150, 100, 25);
			button1.setActionCommand(Identification.OK);
			button1.addActionListener(this);
			topPanel.add(button1);
	
			frame.setVisible(true);
		}
	

		// Handle keyboard accelerators
		public void actionPerformed( ActionEvent event )
		{
			if(event.getActionCommand() == Identification.OK)
			{
				frame.dispose();
			}
		}
	
		// Handle insertions into the text field
		public void insertUpdate( DocumentEvent event )
		{
			// Nothing to do here
		}
	
		// Handle deletions from the text field
		public void removeUpdate( DocumentEvent event )
		{
			// Nothing to do here
		}
	
		// Handle changes to the text field
		public void changedUpdate( DocumentEvent event )
		{
			// Nothing to do here
		}
	}



}

