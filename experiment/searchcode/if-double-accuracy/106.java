/*
	Isabella Moreira
	July 24, 2013
	COP 3330: Professor Eisler
	
	Description
	===========
	Classic Asteroids game with GUI interface
	
	Includes:
		- Sound effects
		- Scoring mechanism
		- Multiple levels with easy additional configurations
*/

//package imports
package comets;
import comets.*;

//standard Java imports
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;

import java.applet.*;
import java.net.URL;

import java.util.*;
import java.io.*;

// This class is primarily responsible for organizing the game of Comets
public class CometsMain implements KeyListener, ActionListener
{
	// About the game
	private JButton about;
	private JButton help;
	
	// Used for playing sounds
	private URL soundPath;
	public AudioClip theSound;
	public JCheckBox soundEnabled;
	
	// GUI Data
	private JFrame frame; // The window itself
	private Canvas playArea;  // The area where the game takes place
	private JPanel options;
	private JPanel col1, col2, row1;
	private JButton resetStats, resetLevel, resetGame;
	
	
	// Scoreboard stats
	public JLabel scoreboard;
	public int deaths = 0;
	public int shotsFired = 0;
	public int cometsHit = 0;
	public double accuracy = 0.0;
	public double kdr = 0.0; //kill divided by death
	public int currentGame = 0;
	public int totalScore = 0;
	
	// List of levels
	public String [] listCfgs = {"level1.cfg", "level2.cfg", "level3.cfg", "level4.cfg"};
	public int totalLevels = listCfgs.length;
	public int maxLevelPlayed = 1;
	public boolean gameCurrentlyRunning = false;
	public JLabel lvlListLabel;
	
	//Level navigation
	private JButton [] lvlButtons = new JButton[totalLevels];
	
	private final int playWidth = 500; // The width of the play area (in pixels)
	private final int playHeight = 500; // The height of the play area (in pixels)
	
	// Game Data
	private Ship ship; // The ship in play
	private Vector<Shot> shots; // The shots fired by the player
	private Vector<Comet> comets; // The comets floating around
	
	private boolean shipDead; // Whether or not the ship has been blown up
	private long shipTimeOfDeath; // The time at which the ship blew up
	
	// Keyboard data
	// Indicates whether the player is currently holding the accelerate, turn
	// left, or turn right buttons, respectively
	private boolean accelerateHeld = false;
	private boolean turnLeftHeld = false;
	private boolean turnRightHeld = false;
	private boolean decelerateHeld = false;
	
	// Indicates whether the player struck the fire key
	private boolean firing = false;
	
	// Set up the game and play!
	public CometsMain(String filename) throws IOException
	{
		// Get everything set up
		configureGUI();
		configureGameData(filename);
		
		// Display the window so play can begin
		frame.setVisible(true);
		
		// Warn the user before playing the first level (so they have time to prepare themselves)
		if(currentGame == 0){
			JOptionPane.showMessageDialog(frame, "Click okay to begin level", "Get ready...", JOptionPane.WARNING_MESSAGE);
		}
		
		// Start the gameplay
		playGame();
	}
	
	public void setButtons() {
		
		for(int i = 0; i < totalLevels; i++){
			lvlButtons[i] = new JButton("" + (i+1));
			lvlButtons[i].addActionListener(this);
			
			lvlButtons[i].setEnabled(false);
			
			lvlButtons[i].setPreferredSize(new Dimension(20, 20));
			lvlButtons[i].setFocusable(false);
			
			col2.add(lvlButtons[i]);
		}
	}
	
	public void toggleButtons(){
		for(int i = 0; i < totalLevels; i++){
			if(i+1 <= maxLevelPlayed) lvlButtons[i].setEnabled(true);
			else lvlButtons[i].setEnabled(false);
		}
	}
	
	//navigate to this level
	public void actionPerformed(ActionEvent e)
	{
		//loop through the buttons to determine which was clicked
		for (int i = 0; i < totalLevels; i++) 
		{
			//buttons[i] was clicked
			if(e.getSource()==lvlButtons[i])
			{
				currentGame = i;
				totalScore -= (currentGame+1)*100;
				nextLevel(i);
			}
		}
	}
	
	// Set up the initial positions of all space objects
	private void configureGameData(String fileName)
	{
		// Configure the play area size
		SpaceObject.playfieldWidth = playWidth;
		SpaceObject.playfieldHeight = playHeight;
		
		// Create the ship
		ship = new Ship(playWidth/2, playHeight/2, 0, 0);
		
		// Create the shot vector (initially, there shouldn't be any shots on the screen)
		shots = new Vector<Shot>();
		
		// Read the comets from comets.cfg
		comets = new Vector<Comet>();
		
		try
		{
			Scanner fin = new Scanner(new File(fileName));
			
			// Loop through each line of the file to read a comet
			while(fin.hasNext())
			{
				String cometType = fin.next();
				double xpos = fin.nextDouble();
				double ypos = fin.nextDouble();
				double xvel = fin.nextDouble();
				double yvel = fin.nextDouble();
				
				if(cometType.equals("Large")){
					comets.add(new LargeComet(xpos, ypos, xvel, yvel));
				}
				else if(cometType.equals("Medium")){
					comets.add(new MediumComet(xpos, ypos, xvel, yvel));
				}
				else{
					comets.add(new SmallComet(xpos, ypos, xvel, yvel));
				}
			}
		}
		// If the file could not be read correctly for whatever reason, abort
		// the program
		catch(FileNotFoundException e)
		{
			System.err.println("Unable to locate comets.cfg");
			System.exit(0);
		}
		catch(Exception e)
		{
			System.err.println("comets.cfg is not in a proper format");
			System.exit(0);
		}
	}
	
	// Set up the game window
	private void configureGUI() throws IOException
	{
		// Create the window object
		frame = new JFrame("Comets");
		//frame.setBackground(BLACK);
			
		frame.setSize(playWidth+100, playHeight+300);
		frame.setResizable(false);
		
		// The program should end when the window is closed
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Set the window's layout manager
		frame.setLayout(new FlowLayout());
		
		// Create the play area
		playArea = new Canvas(){ 
			//with background image
			  private Image backgroundImage = ImageIO.read(new File("black.jpg"));
			  public void paint( Graphics g ) { 
			    super.paint(g);
			    g.drawImage(backgroundImage, 0, 0, null);
			  }
			};
		playArea.setSize(playWidth, playHeight);
		//playArea.setBackground(Color.BLACK);
		playArea.setFocusable(false);
		frame.add(playArea);	
		
		// add scoreboard
		options = new JPanel();
		col1 = new JPanel();
		col2 = new JPanel();
		row1 = new JPanel();
		
		options.setLayout(new BoxLayout(options, BoxLayout.X_AXIS));
		row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
		col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
		col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
		
		//game buttons on col1
		//help and about on col2
		
		scoreboard = new JLabel();
		String scoreboardFormat = 
			    "<html><body width='150px'>" +
			    "Playing level: " + (currentGame+1) + "<br/><br/>Scoreboard<br/>===========<br/>Deaths: <br/>Shots fired: <br/>Comets hit: <br/>Accuracy: <br/>Kill/Death ratio: <br/>" +
			    "<br/><br/>Total score: " + totalScore + "</body></html>";
		scoreboard.setText(scoreboardFormat);
		
		frame.add(scoreboard);		

		//button to reset current stats
		resetStats = new JButton("Reset stats");
		resetStats.setFocusable(false);
		resetStats.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e)
	        {
	            resetGameStats();
	        }
	    }); 
		resetStats.setPreferredSize(new Dimension(200,30));
		col1.add(resetStats);
		
		//how to play
		help = new JButton(" Help ");
		help.setFocusable(false);
		help.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e)
	        {
	        	String label = "<html><b>Moving</b><br/>Up / down arrows: accelerate / decelerate <br/>Left / right arrows: rotate <br/><br/> <b>Scoring</b><br/>+10 per comet<br/> +100*level per level advance<br/>-50 per ship death</html>";
	        	JOptionPane.showMessageDialog(frame, label, "How to play", JOptionPane.WARNING_MESSAGE);
	        }
	    }); 
		col2.add(help);
		
		//add about game
		about = new JButton("About");
		about.setFocusable(false);
		about.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e)
	        {
	        	String label = "<html><b>Game</b><br/>Rendition of classic Asteroid game. <br/><br/> <b>Author</b><br/>Isabella Moreira<br/><br/><b>Contact</b><br/>Twitter: @ScriptEvolution<br/>www.scriptevolution.com<br/<br/>Copyright &copy; 2013<br/></html>";
	        	JOptionPane.showMessageDialog(frame, label, "How to play", JOptionPane.WARNING_MESSAGE);
	        }
	    }); 
		col2.add(about);
		
		//button to reset current level
		resetLevel = new JButton("Reset level");
		resetLevel.setFocusable(false);
		resetLevel.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e)
	        {
	        	totalScore -= (currentGame+1)*100;
	        	nextLevel(currentGame);
	        }
	    }); 
		resetLevel.setPreferredSize(new Dimension(200,30));
		col1.add(resetLevel);
		
		//button to reset entire game
		resetGame = new JButton("New game");
		resetGame.setFocusable(false);
		resetGame.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e)
	        {
	        	currentGame = 0;
	        	resetGameStats();
				JOptionPane.showMessageDialog(frame, "Click okay to begin level", "Get ready...", JOptionPane.WARNING_MESSAGE);
	        	nextLevel(0);
	        }
	    }); 
		resetGame.setPreferredSize(new Dimension(200,30));
		col1.add(resetGame);
		
		
		//read in saved highscores (format: AAA ####)
		
		
		//enable or disable sound effects
		soundEnabled = new JCheckBox("Enable Sound Effects");
		soundEnabled.setSelected(false);
		soundEnabled.setFocusable(false);
		col1.add(soundEnabled);				
		
		
		//add level navigation
		lvlListLabel = new JLabel("<html><br/>Level Select</html>");
		col2.add(lvlListLabel);
		setButtons();
		
		
		//add cols to options panel
		options.add(col1);
		options.add(col2);
		
		//add everything to the options menu
		frame.add(options);
		
		// Make the frame listen to keystrokes
		frame.addKeyListener(this);
	}
	
	//reset the stats
	public void resetGameStats(){
		deaths = 0;
		shotsFired = 0;
		cometsHit = 0;
		accuracy = 0.0;
		kdr = 0.0;
		totalScore = 0;
	}
	
	// Use checkbox to enable/disable sound
	private void playSound(String type){
		if(type.equals("comet")){
			soundPath = CometsMain.class.getResource("explosion.wav");
			theSound = Applet.newAudioClip(soundPath);
			theSound.play();
		}
		else return;
	}
	
	public void nextLevel(int nL){
		//load the next level
		totalScore += (currentGame+1)*100; //level bonus
		
		if(currentGame == totalLevels){
			return;
		}
		
		//start the next level
		if(nL < 0) nL = 0;
		configureGameData(listCfgs[nL]);
		
		//redraw the background to cover up old graphics
		try {
			Image bgImg = ImageIO.read(new File("black.jpg"));
			playArea.getGraphics().drawImage(bgImg, 0, 0, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//enables buttons up to highest level played
		toggleButtons();
	}
	
	// The main game loop. This method coordinates everything that happens in
	// the game
	private void playGame()
	{	
		gameCurrentlyRunning = true;
		//infinite game!!
		while(true)
		{
			if(currentGame == totalLevels){
				JOptionPane.showMessageDialog( null, "Thanks for playing Asteroids! Congrats on battling the asteroid belt.\nRestart game..." );
				//gameCurrentlyRunning = false;
				currentGame = -1;
				break;
			}
			
			// Measure the current time in an effort to keep up a consistent
			// frame rate
			long time = System.currentTimeMillis();
			
			// If the ship has been dead for more than 3 seconds, revive it
			if(shipDead && shipTimeOfDeath + 3000 < time)
			{
				shipDead = false;
				ship = new Ship(playWidth/2, playHeight/2, 0, 0);
			}
			
			// Process game events, move all the objects floating around,
			// and update the display
			if(!shipDead)
				handleKeyEntries();
			handleCollisions();
			moveSpaceObjects();
			
			if(comets.isEmpty()){
				//still have more levels to play
				if(currentGame+1 != totalLevels);
				else{
					JOptionPane.showMessageDialog( null, "Thanks for playing Asteroids! Congrats on battling the asteroid belt.\nRestart game..." );
					//gameCurrentlyRunning = false;
					//break;
					currentGame = -1;
				}
				
				if(currentGame+1 >= maxLevelPlayed) maxLevelPlayed = currentGame+2;
				
				String label = "Level " + (currentGame+2) + " ready. Click okay to continue.";
				JOptionPane.showMessageDialog( null, label);
				currentGame++;
				nextLevel(currentGame);
			}
			
			// Sleep until it's time to draw the next frame 
			// (i.e. 32 ms after this frame started processing)
			try
			{
				long delay = Math.max(0, 32-(System.currentTimeMillis()-time));
				
				Thread.sleep(delay);
			}
			catch(InterruptedException e)
			{
			}
			
		}
	}

	// Deal with objects hitting each other
	private void handleCollisions()
	{
		// Anything that is destroyed should be erased, so get ready
		// to erase stuff
		Graphics g = playArea.getGraphics();
		g.setColor(Color.BLACK);
		
		// Deal with shots blowing up comets
		for(int i = 0; i < shots.size(); i++)
		{
			Shot s = shots.elementAt(i);
			for(int j = 0; j < comets.size(); j++)
			{
				Comet c = comets.elementAt(j);
				
				// If a shot has hit a comet, destroy both the shot and comet
				if(s.overlapping(c))
				{
					//update score
					cometsHit++;
					totalScore += 10; //10 points for hitting a comet
					kdr = (double)cometsHit / deaths;
					accuracy = (double)cometsHit / shotsFired;
					
					//play explosion sound if sound effects enabled
					if(soundEnabled.isSelected()) playSound("comet"); //arg is the SpaceObject we just hit
					
					// Erase the bullet
					shots.remove(i);
					i--;
					this.drawSpaceObject(g, s);
					
					// If the comet was actually destroyed, replace the comet
					// with the new comets it spawned (if any)
					Vector<Comet> newComets = c.explode();
					if(newComets != null)
					{
						this.drawSpaceObject(g, c);
						comets.remove(j);
						j--;
						comets.addAll(newComets);		
					}
					break;
				}
			}
		}
		
		// Deal with comets blowing up the ship
		if(!shipDead)
		{
			for(Comet c : comets)
			{
				// If the ship hit a comet, kill the ship and mark down the time 
				if(c.overlapping(ship))
				{
					//update score
					deaths++;
					kdr = (double)cometsHit / deaths;
					
					totalScore -= 50; //lose 50 points when user destroy the ship (possible to have negative score)
					
					shipTimeOfDeath = System.currentTimeMillis();
					shipDead = true;
					drawShip(g, ship);
				}
			}
		}
	}
	
	// Check which keys have been pressed and respond accordingly
	private void handleKeyEntries()
	{
		// Ship movement keys
		if(accelerateHeld){
			ship.accelerate();
		}
		
		if(decelerateHeld){
			ship.decelerate();
		}
	
		// Shooting the cannon
		if(firing)
		{
			firing = false;			//comment this for an awesome explosion of fire!!!
			shots.add(ship.fire());
		}
	}
	
	// Deal with moving all the objects that are floating around
	private void moveSpaceObjects()
	{
		Graphics g = playArea.getGraphics();

		// Handle the movements of all objects in the field
		if(!shipDead)
			updateShip(g);
		updateShots(g);
		updateComets(g);
		
		String scoreboardFormat = 
			    "<html><body style='width: 150px;'>" +
			    "Playing level: " + (currentGame+1) + "<br/><br/>Scoreboard<br/>===========<br/>Deaths: " + deaths + "<br/>Shots fired: " + shotsFired + "<br/>Comets hit: " + cometsHit + 
			    "<br/>Accuracy: " + (accuracy*100) + "<br/>Kill/Death ratio: " + kdr + "<br/>" +
			    "<br/><br/>Total score: " + totalScore + "</body></html>";
		scoreboard.setText(scoreboardFormat);
		//scoreboard.setText("Scoreboard\n===========\nDeaths: " + deaths + "\nShots fired: " + shotsFired + "\nComets hit: " + cometsHit + "\nAccuracy: " + (accuracy*100) + "%\nKill/Death ratio: " + kdr);
	}
	
	// Move all comets and draw them to the screen
	private void updateComets(Graphics g)
	{
		for(Comet c : comets)
		{
			// Erase the comet at its old position
			g.setColor(Color.BLACK);
			drawSpaceObject(g, c);
			
			// Move the comet to its new position
			c.move();
			
			// Draw it at its new position
			g.setColor(Color.CYAN);
			drawSpaceObject(g, c);
			
		}
	}
	
	// Move all shots and draw them to the screen
	private void updateShots(Graphics g)
	{
		
		for(int i = 0; i < shots.size(); i++)
		{
			Shot s = shots.elementAt(i);
			
			// Erase the shot at its old position
			g.setColor(Color.BLACK);
			drawSpaceObject(g, s);
			
			// Move the shot to its new position
			s.move();
			
			// Remove the shot if it's too old
			if(s.getAge() > 180)
			{
				shots.remove(i);
				i--;
			}
			// Otherwise, draw it at its new position
			else
			{
				g.setColor(Color.RED);
				drawSpaceObject(g, s);
			}		
		}
	}
	
	// Draws the space object s to the the specified graphics context
	private void drawSpaceObject(Graphics g, SpaceObject s)
	{
		// Figure out where the object should be drawn
		int radius = (int)s.getRadius();
		int xCenter = (int)s.getXPosition();
		int yCenter = (int)s.getYPosition();
		
		// Draw the object
		g.drawOval(xCenter - radius, yCenter - radius, radius*2, radius*2);
	}
	
	// Moves the ship and draws it at its new position
	private void updateShip(Graphics g)
	{
		// Erase the ship at its old position
		g.setColor(Color.BLACK);
		drawShip(g, ship);

		// Ship rotation must be handled between erasing the ship at its old position
		// and drawing it at its new position so that artifacts aren't left on the screen
		if(turnLeftHeld)
			ship.rotateLeft();
		if(turnRightHeld)
			ship.rotateRight();
		
		ship.move();
		
		// Draw the ship at its new position
		g.setColor(Color.WHITE);
		drawShip(g, ship);
	}
	
	// Draws this ship s to the specified graphics context
	private void drawShip(Graphics g, Ship s)
	{
		// Figure out where the ship should be drawn
		int radius = (int)s.getRadius();
		int xCenter = (int)s.getXPosition();
		int yCenter = (int)s.getYPosition();
		
		// Draw the ship body
		//g.setColor(Color.magenta);
		g.fillOval(xCenter - radius, yCenter - radius, radius*2, radius*2);
		
		// Draw the gun turret
		int guntipXoffset = (int)(radius * 1.5 * Math.sin(s.getAngle()));
		int guntipYoffset = (int)(radius * 1.5 * Math.cos(s.getAngle()));
		//g.setColor(Color.BLUE);
		g.drawLine(xCenter, yCenter, xCenter + guntipXoffset, yCenter + guntipYoffset);
	}
		
	// Deals with keyboard keys being pressed
	public void keyPressed(KeyEvent key)
	{
		// Mark down which important keys have been pressed
		if(key.getKeyCode() == KeyEvent.VK_UP)
			this.accelerateHeld = true;
		if(key.getKeyCode() == KeyEvent.VK_DOWN){
			this.decelerateHeld = true;
		}
		if(key.getKeyCode() == KeyEvent.VK_LEFT)
			this.turnLeftHeld = true;
		if(key.getKeyCode() == KeyEvent.VK_RIGHT)
			this.turnRightHeld = true;
		if(key.getKeyCode() == KeyEvent.VK_SPACE){
			this.firing = true;
			shotsFired++;
			accuracy = (double)cometsHit / shotsFired;
		}
	}

	// Deals with keyboard keys being released
	public void keyReleased(KeyEvent key)
	{
		// Mark down which important keys are no longer being pressed
		if(key.getKeyCode() == KeyEvent.VK_UP)
			this.accelerateHeld = false;
		if(key.getKeyCode() == KeyEvent.VK_DOWN){
			this.decelerateHeld = false;
		}
		if(key.getKeyCode() == KeyEvent.VK_LEFT)
			this.turnLeftHeld = false;
		if(key.getKeyCode() == KeyEvent.VK_RIGHT)
			this.turnRightHeld = false;
		if(key.getKeyCode() == KeyEvent.VK_SPACE)
			this.firing = false;
	}

	// This method is not actually used, but is required by the KeyListener interface
	public void keyTyped(KeyEvent arg0)
	{
	}
	
	
	public static void main(String[] args) throws IOException
	{
		// A GUI program begins by creating an instance of the GUI
		// object. The program is event driven from that point on.
		//always start on the first level
		new CometsMain("level1.cfg");
	}

}
