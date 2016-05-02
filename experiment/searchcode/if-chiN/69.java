package com.blogspot.the3cube.beefree.ui;

import java.util.Scanner;

import com.blogspot.the3cube.beefree.logic.CommandHandler;
import com.blogspot.the3cube.beefree.util.Display;
import com.blogspot.the3cube.beefree.util.DisplayCLI;

/**
 * The command line interface for BeeFree.<br>
 * 
 * @author 	The3Cube
 * @since 	v0.1
 * @version v0.1
 * 
 */
public class BeeFreeCLI implements UserInterface {
	private final static String SPLASHSCREEN = 
			"Welcome to BeeFree!" + "\n"
			+ "Use BeeFree and be freed!";

	private CommandHandler handler_;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.blogspot.the3cube.beefree.UserInterface#run()
	 */
	@Override
	public void run() {
		splashScreen(); // welcome screen for the CLI

		Scanner stdin = new Scanner(System.in);
		handler_ = new CommandHandler();
		checkReminder();

		Display display = null;
		while (stdin.hasNextLine()) {
			String cmd = stdin.nextLine();
			cmd = cmd.replace(',', ' ');
			display = handler_.doCmd(cmd);
			display(display);
		}
	}

	/**
	 * this method check if there is a task for the next three day and remind
	 * the user.<br>
	 * 
	 * @programmer
	 * @start
	 * @finish
	 * 
	 * @reviewer
	 * @reviewdate
	 * 
	 * @since
	 * 
	 */
	private void checkReminder() {
		// TODO Auto-generated method stub

	}

	/**
	 * this method display the result after the command was run.<br>
	 * 
	 * @since			v0.1
	 * 
	 * @param display 	the display class with data formatted for the CLI.
	 */
	private void display(Display display) {
		if (display instanceof DisplayCLI) {
			DisplayCLI cli = (DisplayCLI) display;
			System.out.println(cli.getData_());
			System.out.println("-----------------------------------------");
		}
	}

	/**
	 * the method will display the splash screen for the CLI.<br>
	 * 
	 * @programmer 	Chin Gui Pei
	 * @start 		04 October 2011
	 * @finish 		04 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	04 October 2011
	 * 
	 * @since 		v0.1
	 */
	private void splashScreen() {
		Display splashScreen = new DisplayCLI(SPLASHSCREEN);
		display(splashScreen);
	}

}

