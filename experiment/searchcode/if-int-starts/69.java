// AForce.java
// Jim Sproch
// Created: April 29, 2006
// Modified: March 30, 2008
// Part of the Aforce Port
// Mac < Windows < Linux

/**
	Aforce loads the envronment and initilizes the ships!
	@author Jim Sproch
	@version 0.1a beta
*/

import java.applet.*;
import java.util.*;


// This application can be started in two ways:
//	1.  Call from main (normal launch)
//	2.  Call from browser (applet mode)




public class AForce extends Applet implements Runnable
{

	public AForce()
	{
		AForce.game = this;
	}

	// This method is called if and only if the game starts in applet mode
	public void init()
	{
		useAppletMode = true;
	}
	

	public static boolean isAppletMode()
	{
		return useAppletMode;
	}
	
	public static AForceEnv field;
	public static Dashboard dashboard;
	public static Clicker clicker;
	int mystatus;
	String lastmap;

	// If it starts up as an applet, this will set its self to true
	private static boolean useAppletMode = false;

	private ScoreBoard scoreboard = ScoreBoard.getInstance();
	public static Display frame;

	public static AForce game = null;

	public static ArrayList<Thread> threads = new ArrayList<Thread>();


	// Because this object is Serializable because it extends JComponent
	private static final long serialVersionUID = 7526471155622776147L;

	public static ArrayList<Thread> getThreads()
	{
		return threads;
	}



	public static void main(String[] args)
	{
		Printer.printIntro();

		Printer.debug.println("Debugging Mode is ON!");

		Printer.info.println("INFO: Starting Application...");
		if(game == null) game = new AForce();
		
		// Initializes the game by setting up display and environment and stuff
		game.setup();

		Printer.info.println("INFO: Loading Images...");
		Images.loadimages();

		Printer.info.println("INFO: Setting Map based on executation time arguments...");
		String mapname;
		if(args.length > 0) mapname = args[0];
		else mapname = "Standard";

		game.newlevel(mapname);
	}

	public static AForce getGame()
	{
		return game;
	}

	public void run()
	{
		main(new String[1]);
		while(true) {repaint(); Thread.yield(); try{Thread.sleep(1);}catch(Exception e){}}
	}

	public static void pause()
	{
		if(!clicker.isRunning())
		{
			clicker.start();
			frame.start();
		}
		else
		{
			clicker.stop();
		}
	}

	// Only called if we enter applet mode (called instead of main)
	public void start()
	{
		AForce.main(new String[1]);
	}

	public void stop()
	{
		Printer.debug.println("ERROR 339: Stop has been sub-deprecated #AForce.stop()");
	}


	public void setup()
	{
		clicker = new Clicker();

		// remember, the env should have space for the paths on the right and bottom
		//  if ships start driving off the map, check these numbers again!
		Printer.info.println("INFO: Creating the environment...");
		field = new AForceEnv(new Size(525,525));
		Printer.info.println("INFO: Starting the heads-up-display (Dashboard)...");
		dashboard = new Dashboard(new Size(745-525, 525, 525, 0));
		field.setAForce(this);

		Printer.info.println("INFO: Starting the display...");
		frame = new Display();

		Printer.info.println("INFO: Setting status to -1 (waiting for user start)...");
		mystatus = -1;
	}

	public void loadmap(String mapname)
	{
		XMLParser xmlParser = new XMLParser("Maps/"+mapname+"/Obstacles.xml");

		while(true)
		{
			DisplayableObject obstacle = xmlParser.getDisplayableObject();
			if(obstacle == null) break;
			field.addobject(obstacle);
		}
	}

	public void nextlevel()
	{
		field.AForceEnvhelper();
		field.nextLevel();
		Printer.note.println("NOTE 218: Loading Level: "+AForceEnv.getLevel());
		newlevel(lastmap);
	}

	public void newlevel(String map)
	{
		mystatus = -1;
		// remember, some stuff was created in the AForce constructor in static main()
		if(!Printer.getState("railgunmode")) for(int x = 0; x < frame.getKeyListeners().length; x++)
			frame.removeKeyListener(frame.getKeyListeners()[x]);

		for(int x = 0; x < frame.getMouseListeners().length; x++) 
			frame.removeMouseListener(frame.getMouseListeners()[x]);

		for(int x = 0; x < frame.getFocusListeners().length; x++)
			frame.removeFocusListener(frame.getFocusListeners()[x]);

		lastmap = map;
		mystatus = ObjectType.AwaitingStart;
		clicker.resetLevelTimes();
		field.AForceEnvhelper();
		if(AForce.class.getResourceAsStream("Maps/"+map+"/Obstacles.xml") == null || AForce.class.getResourceAsStream("Maps/"+map+"/Units-Single.xml") == null)
			map = "Standard";
		loadmap(map);
		loadunits(map);
		startLevel();
		dashboard.update();
		Thread.yield();
		try{ Thread.sleep(1); } catch(Exception e){}
	}


	public void startLevel()
	{
		if(field.numobjects() == 0) System.err.println("ERROR 217: Game can not be started without a map");
		else
		{
			Printer.note.println("NOTE 512: Field Map has been created");
			Printer.note.println("NOTE 824: The Game has been Started!");
			Printer.note.println("NOTE 112: Awaiting User Start!");
			if(Ship.userShip.getAILevel() > 0) clicker.start();
		}
	}

	public void newGame()
	{
		(new Exception()).printStackTrace();
		Printer.info.println("INFO 358: Preparing New Game");
		Ship.setUserShip(null);
		Printer.info.println("INFO 843: Stopping the Clicker");
		clicker.stop();
		Printer.info.println("INFO 844: Resetting Game Time");
		clicker.resetGameTimes();
		Printer.info.println("INFO 838: Resetting Scores");
		field.resetScores();
		Printer.info.println("INFO 472: Resetting Level");
		field.resetLevel();
		Printer.info.println("INFO 844: Loading Map");
		newlevel(lastmap);
		Printer.info.println("INFO 645: Done Loading New Game");
	}



	public static Dashboard getDashboard()
	{
		return dashboard;
	}

	public static AForceEnv getField()
	{
		return field;
	}


	public void loadunits(String mapname)
	{

		// Add bad ships from map :)
		XMLParser xmlParser= new XMLParser("Maps/"+mapname+"/Units-Single.xml");
		while(true)
		{
			DisplayableObject unit = xmlParser.getDisplayableObject();
			if(unit == null) break;
			if(unit instanceof Ship)
			{
				field.addobject((Ship)unit);

				if(unit == Ship.userShip)
				{
					// Add the user ship (single player)
					Status usershipstatus = null;
					int userAILevel = 0;
					if(Ship.oldUserShip != null)
					{
						usershipstatus = Ship.oldUserShip.getStatus();
						userAILevel = Ship.oldUserShip.getAILevel();
					}
					if(usershipstatus != null && usershipstatus.getHealth() != 0)
						Ship.userShip.setstatus(usershipstatus);
					Ship.userShip.setAILevel(userAILevel);
					addUserIO(Ship.userShip);

				}
			}
			else Printer.err.println("ERROR 125: Unknown Unit Type #AForce.loadunits();");
			}
	}


	public void destroy()
	{
		//frame.dispose();  // TODO: dispose of frame in threadsafe way
		Printer.note.println("NOTE 342: The Game has been #AForce.destroy()");
		Printer.stderr.print("\n"); // so the console won't appear on AForceConsole line
	//	try{System.exit(1);}catch(Exception e){/* Not much we can do, happens in applet mode*/}
	}




	public void addUserIO(Ship usership)
	{
		UserIO userio = new UserIO();
		frame.addKeyListener(userio);
		frame.addMouseListener(userio);
		frame.addFocusListener(userio);
		userio.addship(usership);
	}


	public static Display getFrame()
	{
		return frame;
	}

	public ScoreBoard getScoreBoard()
	{
		return scoreboard;
	}

	public static Clicker getClicker()
	{
		return clicker;
	}


}
  
