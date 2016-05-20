package game;

import helpers.SoundPlayer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

import utils.ImageUtils;

import actors.*;

/**
 * The Class SpaceInvaders. The main game class that contains most of the logic and determines the gameplay. 
 */
@SuppressWarnings("serial")
public class SpaceInvaders extends JPanel implements ActionListener
{
	/** The width of the screen; a constant. */
	public static final int WIDTH = 506;
	
	/** The height of the screen; a constant. */
	public static final int HEIGHT = 526;
	
	/** The dark red colour used throughout the game. */
	public static final Color COLOR_DARK_RED = new Color(246, 0, 3);
	
	/** The white colour used throughout the game. */
	public static final Color COLOR_WHITE = Color.white;
	
	/** The background image for the game. */
	private BufferedImage background = ImageUtils.loadImage("images/background2.png");
	
	/** The background x coordinate offset. */
	private int backgroundX = 0;
	
	/** The background y coordinate offset. */
	private int backgroundY = HEIGHT-background.getHeight()-26;
	
	/** The player healthbar image. */
	private BufferedImage healthbar = ImageUtils.loadImage("images/healthbar.png");
	
	/** The boss healthbar image. */
	private BufferedImage bossHealthbar = ImageUtils.loadImage("images/bossHealthbar.png");
	
	/** The upgrade screen image. */
	private BufferedImage upgradeScreen = ImageUtils.loadImage("images/upgrades.png");
	
	/** The delay constant for the game timer. */
	public static final int DELAY = 30;
	
	/** The current focus of the game that reacts to key input; usually the player. */
	private Object focus;
    
    /** The player of the game. */
    private Player player = new Player(350, 350, this);
    
    /** The timer for the game; checks for collisions, causes scrolling, and repaints the screen. */
    private javax.swing.Timer gameTimer;
    
    /** The actors array; contains every actor that is in the game. */
    private ArrayList<Actor> actors = new ArrayList<Actor>();
    
    /** Whether or not the game and all fo its actors have been paused. */
    private boolean paused = false;
    
    /** A variable used to keep track of the frequency of things like background scrolling. */
    private int time = 0;
    
    /** The "scene" of the game the user is currently viewing. Can be "menu", "game", "gameOver", etc... */
    private String scene = "menu";
    
    /** The list of possible player ship parts that can be upgraded. */
    private String[] upgrades = {"Guns", "Wings", "Ship", "Cockpit"};
    
    /** The "glow" images for these upgradeable parts that are shown during the "upgrade" scene. */
    private BufferedImage[] upgradeGlows = {ImageUtils.loadImage("images/upgradeGlowGuns.png"), ImageUtils.loadImage("images/upgradeGlowWings.png"), ImageUtils.loadImage("images/upgradeGlowShip.png"), ImageUtils.loadImage("images/upgradeGlowCockpit.png")};
    
    /** The index of the current upgradeable part that is selected in the "upgrade" scene. */
    private int upgradeIndex = 0;
    
    /** The current "glow" image for the upgradeable part in the "upgrades" scene. */
    private BufferedImage upgradeGlow = upgradeGlows[upgradeIndex];
    
    /** The menu choices on the main menu. */
    private String[] menuChoices = {"Campaign", "Onslaught", "Instructions", "Options"};
    
    /** The currently selected menu choice. */
    private int menuChoice = 0;
    
    /** Whether or not the current game is the onslaught mode or not (otherwise it is the campaign). */
    private boolean onslaught;
    
    /** The alpha (transparency) value for the opening credits; drops to 0 at the start of the game. */
    private float creditsAlpha = 1;
    
    /** The current player score for the onslaught mode. */
    private int score = 0;
    
    /** Whether or not the score for the onslaught mode is displayed on the screen. */
    private boolean scoreVisible = true;
    
    /** The highscore for the onslaught mode. */
    private int highscore = 0;
    
    /** Whether or not the campaign has been completed; if it has, a special "crest" symbol is displayed on the menu. */
    private boolean campaignCompleted = false;
    
    /** The sound player that plays all music and sound effects in the game. */
    private SoundPlayer sound = new SoundPlayer();
    
    /** The current volume level of the game. */
    private double volume = 5; 
    
    /** The options and option choices that can be selected for each; used in the "options" scene. */
    private String[][] options = { {"Campaign Background: ", "1", "2", "3", "4"},
    							   {"Music: ", "ON", "OFF"},
    							   {"Sound Effects: ", "ON", "OFF"},
    							   {"Volume: "},
    							   {"Clear History", "Press SPACE To Confirm", "History Cleared"},
    							   {" "},
    							   {"Back"} };
    
    /** The currently selected choices for the options double array. */
    private int[] optionChoices = {1, 1, 1, 0, 0, 0, 0}; 
    
    /** The current option that can be changed; it is displayed as white instead of red in the "options" scene. */
    private int currentOption = 0; 
    
    //the next block of variables is used for drawing variable text onto the board. They are used in the Options and GameOver scenes.
   
    /** The message string that determines what message will be written onto the screen. */
    private String msg;
    
    /** The metrics for the current font; used for things like centering messages of various fonts. */
    private FontMetrics metrics;
    
    //group of fonts used in program:
    /** The Arial Black, size 18 font used in the game. */
    public static final Font FONT_AB18 = new Font("Arial Black", Font.PLAIN, 18);
   
    /** The Arial Black, size 24 font used in the game. */  
    private static final Font FONT_AB24 = new Font("Arial Black", Font.PLAIN, 24);
    
    /** The Copperplate Gothic Bold, size 56 font used in the game. */
    private static final Font FONT_CGB56 = new Font("Copperplate Gothic Bold", Font.PLAIN, 56); 
    
	/**
	 * The main method for the program; instantiates a JFrame with special properties and adds a new SpaceInvaders panel to it.
	 *
	 * @param args the arguments array
	 */
	public static void main(String[] args) 
	{
		JFrame frame = new JFrame("*Valkyr* Space Invaders - By Sasha Kitaygorodsky");
		frame.setBounds(100, 100, WIDTH, HEIGHT); //setting window location and size
		frame.add(new SpaceInvaders());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //exit the application when the frame gets closed
        frame.setResizable(false); //frame will be a non-resizeable
        frame.setVisible(true); //show the frame with all components
	}
    
    /**The constructor for the game; adds and instantiates the key listener, background, and gameTimer, as well as loading the game state and sounds.*/
    public SpaceInvaders() 
    {    
        super(); //calls the constructor for the thing Board extends: JPanel. 
        addKeyListener(new KeyTracker());
        setFocusable(true);
		setBackground(Color.black);
      
		loadState();
		
        gameTimer = new javax.swing.Timer(DELAY, this); //creates the game timer that is responsible for moving the game forward and for repainting the board.
        gameTimer.start(); //starts the timer.
        
        scene = "menu";
        focus = this;
        
        loadSounds();
        changeMusic("introSound");
    }
    
    /**
     * Initializes a new game; destroys every actor left in the actors array and adds new ones.
     * Also re-instantiates game variables and properties. 
     */
    public void initialize()
    {
    	while(actors.size()>0)
    	{
			try
			{
				Actor a = actors.get(0);
				a.die();
				actors.remove(0);
				if (a instanceof Enemy)
					((Enemy)a).killAIThread();
			} 
			catch(NullPointerException ex) { }
			catch(IndexOutOfBoundsException ex) { }
    	}
    	
    	actors = new ArrayList<Actor>();
    	player = new Player(350, 350, this);
        actors.add(player);
        focus = player;
        loadState(); //loads game and player state from state.txt file.
        player.refresh(); //ensures proper speed, HP regen, health, and damage from player ship parts
        
        if (onslaught)
        {
        	background = ImageUtils.loadImage("images/background0.png");
        	addRandomEnemy(1);
        }
        else
        {
        	background = ImageUtils.loadImage("images/background"+optionChoices[0]+".png"); 
        	actors.addAll(buildLevel("campaign"));
        }
        score = 0;
        backgroundX = 0;
        backgroundY = HEIGHT-background.getHeight()-26;
        healthbar = ImageUtils.loadImage("images/healthbar.png");
    	bossHealthbar = ImageUtils.loadImage("images/bossHealthbar.png");
    }
    
    /**
     * Initializes a new game; destroys every actor left in the actors array and adds new ones.
     * Also re-instantiates game variables and properties. 
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics page)
    {
        super.paintComponent(page); //lets one do custom painting.
        Graphics2D g = (Graphics2D) page;
        
        if (scene.equals("menu"))
        {	
        	g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1)); 
        	g.drawImage(ImageUtils.loadImage("images/menu/menu"+menuChoices[menuChoice]+".png"), 0, 0, this);
        	if (campaignCompleted)
        		g.drawImage(ImageUtils.loadImage("images/campaignCompleteCrest.png"), 445, 450, this); 
        	g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, creditsAlpha));   
        	g.drawImage(ImageUtils.loadImage("images/credits.png"), 0, 0, this);
        	g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1)); 
        }
        else if (scene.equals("instructions"))
        {
        	g.drawImage(ImageUtils.loadImage("images/menu/instructions.png"), 0, 0, this);
        }
        else if (scene.equals("options"))
        {
        	g.drawImage(ImageUtils.loadImage("images/menu/options.png"), 0, 0, this);    	
       	 	g.setFont(FONT_AB24);
       	 	for (int i=0; i<options.length; i++) 
       	 	{
       	 		if (i<3) //background, music, and sound effect options
       	 			msg = options[i][0]+options[i][optionChoices[i]]; 
       	 		else if (i<4)//volume option
       	 			msg = options[i][0]+(new DecimalFormat("##.#").format(volume));
       	 		else //clear history and back options
       	 			msg = options[i][optionChoices[i]];
       	 		
       	 		g.setColor((i==currentOption)?COLOR_WHITE:COLOR_DARK_RED);
       	 		g.drawString(msg, 50, 150+i*50);
       	 	}
        }
        else if (scene.equals("game"))
        {
	        g.drawImage(background, backgroundX, backgroundY, this);
	        
	        if(healthbar!=null)
	        	g.drawImage(healthbar, 0, HEIGHT-healthbar.getHeight()-26, this);
	        if(player.getBoss()!=null && bossHealthbar!=null)
	        	g.drawImage(bossHealthbar, WIDTH-bossHealthbar.getWidth()-7, HEIGHT-bossHealthbar.getHeight()-26, this);
	        
	        if (onslaught && scoreVisible)
	        {
	        	g.setColor(COLOR_WHITE);
	        	g.setFont(FONT_AB18);
	        	metrics = this.getFontMetrics(FONT_AB18);
	        	msg = "Score: "+score;
	        	g.drawString(msg, WIDTH-metrics.stringWidth(msg)-10, HEIGHT-35);
	        }
	        
	        for (int i=actors.size()-1; i>=0; i--)
	        {
	        	try
	        	{
	        		Actor actor = actors.get(i);
	        		if (actor.isVisible())
	        			g.drawImage(actor.getImage(), actor.getX(), actor.getY(), this);
	        	}
	        	catch(Exception ex) {}//catches potential IndexOutOfBounds exceptions caused by multiple threads working at the same time and changing actors. 
	        }
	        
	        if (paused) //game paused; write "Paused"
	        {
	        	g.setColor(COLOR_WHITE);
	        	g.setFont(FONT_CGB56);
	        	msg = "Paused";
	        	metrics = this.getFontMetrics(FONT_CGB56);
	        	g.drawString(msg, (WIDTH - metrics.stringWidth(msg)) / 2, HEIGHT / 3 + 50);
	        }
        }
        else if (scene.equals("upgrades")) //upgrade scene: put glows at 127, 137
        {
        	 g.drawImage(upgradeScreen, 0, 0, this);
        	 g.drawImage(upgradeGlow, 137, 127, this);
        	 
        	 g.setColor(COLOR_DARK_RED);
        	 g.setFont(FONT_AB18);
        	 metrics = this.getFontMetrics(FONT_AB18);
        	 String part = upgrades[upgradeIndex].toLowerCase();
        	 
        	 if (upgradeIndex == 0) //guns
        		 msg = "GUNS: Improve bullet power.";
        	 else if (upgradeIndex == 1) //wings
        		 msg = "WINGS: Improve movement speed.";
        	 else if (upgradeIndex == 2) //ship
        		 msg = "SHIP: Improves maximum health.";
        	 else //cockpit
        		 msg = "COCKPIT: Improves health regeneration.";
             g.drawString(msg, 70, 350);
             msg = "Current Level: "+player.getPartLevel(part);
             g.drawString(msg, 70, 370);
             int cost = player.getUpgradeCost(part);
             msg = "Crystals to Upgrade: "+ ((cost>0) ? cost : "MAXED");
             g.drawString(msg, 70, 390);
             msg = "My Crystals: "+player.getMoney();
             g.drawString(msg, 70, 430);	 
        }
        else if (scene.equals("gameOver"))
        {
        	g.drawImage(background, backgroundX, backgroundY, this);
        	g.setColor(COLOR_WHITE);
        	
        	g.setFont(FONT_CGB56);
        	msg = "Game Over";
        	metrics = this.getFontMetrics(FONT_CGB56);
        	g.drawString(msg, (WIDTH - metrics.stringWidth(msg)) / 2, HEIGHT / 3);
        	
        	g.setFont(FONT_AB18);
        	metrics = this.getFontMetrics(FONT_AB18);
        	if (onslaught)
        	{
        		if (score>highscore)
        		{
        			msg = "NEW HIGHSCORE: "+score;
        		}
        		else
        		{
        			msg = "Score: "+score+"    Highscore: "+highscore;
        		}
        	}
        	else
        	{
        		msg = "Unfortunately you have been killed in battle...";
        	}
        	g.drawString(msg, (WIDTH - metrics.stringWidth(msg)) / 2, HEIGHT / 2);
        	msg = "< Press SPACE to return to the Menu >";
        	g.drawString(msg, (WIDTH - metrics.stringWidth(msg)) / 2, HEIGHT / 2 + 35);
        }
        else if (scene.equals("gameWon"))
        {
        	g.drawImage(background, backgroundX, backgroundY, this);
        	g.setColor(COLOR_WHITE);
        	
        	g.setFont(FONT_CGB56);
        	metrics = this.getFontMetrics(FONT_CGB56);
        	msg = "Campaign";
        	g.drawString(msg, (WIDTH - metrics.stringWidth(msg)) / 2, HEIGHT / 3 - 55);
        	msg = "Complete";
        	g.drawString(msg, (WIDTH - metrics.stringWidth(msg)) / 2, HEIGHT / 3 +20);
        	
        	g.setFont(FONT_AB18);
        	metrics = this.getFontMetrics(FONT_AB18);
        	msg = "You have stopped the alien invasion!";
        	g.drawString(msg, (WIDTH - metrics.stringWidth(msg)) / 2, HEIGHT / 2 +20);
        	msg = "And the Valkyr? YOU are the Valkyr...";
        	g.drawString(msg, (WIDTH - metrics.stringWidth(msg)) / 2, HEIGHT / 2 + 55);
        	msg = "Thank you for rescuing mankind!!!";
        	g.drawString(msg, (WIDTH - metrics.stringWidth(msg)) / 2, HEIGHT / 2 + 90);
        	msg = "< Press SPACE to return to the Menu >";
        	g.drawString(msg, (WIDTH - metrics.stringWidth(msg)) / 2, HEIGHT / 2 + 125);
        }
        
    
    }
    
    
    /**
     * Checks for collisions, controls scrolling, repaints the screen, and adds new enemies during the "onslaught" mode. 
     * It is called every gameTimer interval (DELAY). 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) 
    {
    	if (time<60000)
    		time++; //time is increased
    	else
    		time = 0;
    	
    	if (scene.equals("menu") && creditsAlpha>0 && time>80) //credit fade out
    	{
    		creditsAlpha-=0.04;
    		if (creditsAlpha<0)
    		{
    			creditsAlpha = 0;
    			changeMusic("SandOcean");
    		}
    	}
    	if (scene.equals("game") && !paused)
    	{	
	    	//checks for collisions
	    	int index1 = 0, index2 = 0;
	    	Actor a1, a2;
	    	while(index1<actors.size())
	    	{
	    		a1 = actors.get(index1);
    			for(index2=0; index2<actors.size(); index2++)
    			{
    				try
    				{
	    				a2 = actors.get(index2);
	    				if (a1!=a2 && a1.isAlive() && a2.isAlive() && !(a1.getTeam().equals(a2.getTeam())) && a1.collision(a2))
	    				{
	    					if ((a1 instanceof Bullet || a1 instanceof Kamikaze) && !(a2.getTeam().equals("item") || a2 instanceof Bullet)) //bullet hits non-item and non-bullet and non-inactive boss; (hits enemy or player or active boss)
	    					{
	    						int damage = (a1 instanceof Bullet) ? ((Bullet)a1).getDamage() : ((Kamikaze)a1).getDamage();
	    						if (a1 instanceof Explodable)
	    							a1.explode();
	    						else
	    							a1.die();
	    						
	    						if (!(a2 instanceof Boss) || ((Boss)a2).isActive()) //if a2 is enemy, player, or active boss, lower health
	    						{
		    						a2.lowerHealth(damage);
		    							
		    						if (a2 instanceof Boss) //if second actor is boss, lower boss health bar. 
		    						{
		    							if (a2.getHealth()>0 && (500*((double)player.getHealth()/(double)player.getMaxHealth())>10))
		    								bossHealthbar = ImageUtils.resize(healthbar, healthbar.getWidth(), (int)(HEIGHT*((double)a2.getHealth()/(double)a2.getMaxHealth())));
		    							else
		    							{
		    								bossHealthbar = null;
		    								player.setBoss(null);
		    							}
		    						}
	    						}
	    					
	    						break;
	    					}
	    					else if (a1 instanceof Enemy && a2 instanceof Player) //player hits an enemy; enemy explodes and damages player
	    					{
	    						if (a1 instanceof Boss)
	    							a2.explode();
	    						else
	    						{
	    							a2.lowerHealth(a1.getHealth()*3);
	    							a1.explode();
	    						}
	    					}
	    					else if (a1.getTeam().equals("item") && a2 instanceof Player) //player hits an item; pick it up or add to score
	        				{
	    						if (onslaught)
	    							score+=10*((Crystal)a1).getWorth();
	        					((Player)a2).pickUp(a1);
	        				}
	    				}
    				} catch(NullPointerException ex) {}
	    		}
	    		index1++;
	    	}
	    	
	    	if (onslaught && time%50==0) //add more enemies if in onslaught mode. 
	    	{
	    		Random randomGen = new Random();
	    		int number = randomGen.nextInt(4);
	    		int difficulty = 1;
	    		
	    		if (score>1000)
	    			difficulty = 3;
	    		else if (score>500)
	    			difficulty = 2;
	    			
	    		for (int i=0; i<number; i++)
	    		addRandomEnemy(difficulty);
	    	}
	    		
	    	try //downwards screen scrolling
			{
	    		for(Actor a : actors)
	    		{
	    			if(!(a instanceof Player || a instanceof Bullet || (a instanceof Boss && ((Boss)a).isActive())))
	    				a.setY(a.getY()+1);
	    		}
	    	}
	    	catch(ConcurrentModificationException ex) {}
	    	catch(NoSuchElementException ex) {}
    	
	    	if (time%2==0 && backgroundY<0) //downwards background scrolling
	    	{
		    	try 
				{
		    		backgroundY++;
		    	}
		    	catch(ConcurrentModificationException ex) {}
		    	if (backgroundY == 0 && onslaught)
		    		backgroundY = HEIGHT-background.getHeight()-26;
	    	}
	    	
	    	if (time%15==0 && player.isAlive() && player.getHealth()<player.getMaxHealth()) //player health regeneration
	    	{
	    		player.setHealth(player.getHealth()+player.getHealthRegen());
	    		refreshPlayerHealthBar();
	    	}
    	}
    	
    	
        repaint(); 
    }
    
    /**
     * Refreshes the player's health bar.
     */
    public void refreshPlayerHealthBar()
    {
    	if (player.getHealth()>0 && (500*((double)player.getHealth()/(double)player.getMaxHealth())>10) && healthbar!=null)
			healthbar = ImageUtils.resize(healthbar, healthbar.getWidth(), (int)(HEIGHT*((double)player.getHealth()/(double)player.getMaxHealth())));
		else
			healthbar = null;
    }
    
    /**
     * Sets the current scene.
     *
     * @param scene the new scene
     */
    public void setScene(String scene)
    {
    	this.scene = scene;
    }
    
    /**
     * Play a sound effect if the sound effects option is on. 
     *
     * @param name the name of the sound to be played. 
     */
    public void playSound(String name)
    {
    	if (optionChoices[2] == 1)//sound effects are on.
    		sound.play(name);
    }
    
    /**
     * Pauses or unpauses the current music.
     *
     * @param pause whether the music should be paused or not.
     */
    public void pauseMusic(boolean pause)
    {
    	sound.switchPause(pause);
    }
    
    /**
     * Changes the currently playing music.
     *
     * @param name the name of the music to be played.
     */
    public void changeMusic(String name)
    {
    	if (optionChoices[1] == 1) //music option is on
    	{
    		sound.stop();
    		sound.play(name);
    	}
    }
    
    /**
     * Stops the currently playing music.
     */
    public void stopMusic()
    {
    	sound.stop();
    }
    
    /**
     * Increases the volume level of the game by a specified number of steps. 
     *
     * @param steps the number of steps to increase the volume.
     * @return the new volume level as a double.
     */
    public double increaseVolume(int steps)
    {
    	return volume = sound.increaseVolume(steps)*10;
    }
    
    /**
     * Builds a level for the "campaign" game mode by adding actors read in from a file.
     *
     * @param level the level to be built
     * @return the array list of Actors to be added.
     */
    public ArrayList<Actor> buildLevel(String level)
    {
    	ArrayList<Actor> enemies = new ArrayList<Actor>();
    	try 
    	{
			Scanner scan = new Scanner(new File("files/"+level+".txt"));
			while(scan.hasNext())
			{
				String line = scan.nextLine(); 
				String[] params = line.trim().split(" "); //enemy parameters
				if (params[0].equals("BasicEnemy"))
				{
					enemies.add(new BasicEnemy(Integer.parseInt(params[1]), Integer.parseInt(params[2]), this));
				}
				if (params[0].equals("Sniper"))
				{
					enemies.add(new Sniper(Integer.parseInt(params[1]), Integer.parseInt(params[2]), this));
				}
				else if (params[0].equals("Stormer"))
				{
					enemies.add(new Stormer(Integer.parseInt(params[1]), Integer.parseInt(params[2]), this));
				}
				else if (params[0].equals("Saucer"))
				{
					enemies.add(new Saucer(Integer.parseInt(params[1]), Integer.parseInt(params[2]), this));
				}
				else if (params[0].equals("Mine"))
				{
					enemies.add(new Mine(Integer.parseInt(params[1]), Integer.parseInt(params[2]), this));
				}
				else if (params[0].equals("Devilship"))
				{
					enemies.add(new Devilship(Integer.parseInt(params[1]), Integer.parseInt(params[2]), Boolean.parseBoolean(params[3]), this));
				}	
				else if (params[0].equals("SaucerBoss"))
				{
					enemies.add(new SaucerBoss(Integer.parseInt(params[1]), Integer.parseInt(params[2]), this));
				}
			}
		} 
    	catch (FileNotFoundException e){ System.out.println("Level file not found. "); }
		
    	return enemies;
    }
    
    /**
     * Adds a random enemy to the actors ArrayList during the "onslaught" game mode depending on the difficulty level.
     *
     * @param difficulty the difficulty level
     */
    public void addRandomEnemy(int difficulty) //difficulty goes from 1 (easy) to 3 (hard) and helps randomly select appropriate enemy.
    {
    	Random randomGen = new Random();  
    	int enemyType = randomGen.nextInt(100);
    	int x = randomGen.nextInt(WIDTH-100)+30;
    	int y = -randomGen.nextInt(75)-100;
    		
		if (enemyType>94 && difficulty>2)
			actors.add(new Saucer(x, y, this));
		else if (enemyType>70 && difficulty>2)
		{
			boolean leftSide = randomGen.nextBoolean();
			actors.add(new Devilship(((leftSide)?-200:520), y, leftSide, this));
		}
		else if (enemyType>50 && difficulty>1)
			actors.add(new Stormer(x, y, this));
		else if (enemyType>30 && difficulty>1)
			actors.add(new Sniper(x, y, this));
		else if (enemyType>80)
			actors.add(new Stormer(x, y, this));
		else if (enemyType>20)
			actors.add(new BasicEnemy(x, y, this));
		else if (enemyType>5)
		{
			actors.add(new Mine(x, y, this));
			actors.add(new Mine(WIDTH-50-x, y, this));
		}
    }
    
    /**
     * Loads the game's sounds from the sounds.txt file, including individual relative volume level.
     */
    public void loadSounds()
    {
    	try 
    	{
			Scanner scan = new Scanner(new File("files/sounds.txt"));
			while(scan.hasNext())
			{
				String line = scan.nextLine(); 
				String[] params = line.trim().split(" "); //sound parameters
				sound.loadSound(params[0], Double.parseDouble(params[1]), Boolean.parseBoolean(params[2]));
			}
		} 
    	catch (FileNotFoundException e){ System.out.println("Sound file not found."); }
    }
    
    /**
     * Saves the current state of the game.
     */
    public void saveState()
    {
    	try 
    	{
			BufferedWriter writer = new BufferedWriter(new FileWriter("files/state.txt"));
			String[] data = new String[7]; //data that needs to be written.
			data[0] = campaignCompleted ? "1" : "0"; //campaign complete
			data[1] = Integer.toString(highscore);
			data[2] = Integer.toString(player.getMoney()); //player's money (crystals)
			data[3] = Integer.toString(player.getPartLevel("guns")); //gun level
			data[4] = Integer.toString(player.getPartLevel("wings")); //wings level
			data[5] = Integer.toString(player.getPartLevel("ship")); //ship level
			data[6] = Integer.toString(player.getPartLevel("cockpit")); //cockpit level
			
			for (int i=0; i<data.length; i++)
			{
				writer.write(data[i]);
				writer.write(" ");
			}
			
			writer.close();
			
		} catch (IOException e) { System.out.println("State file could not be written to."); }
    }
    
    /**
     * Loads the current state of the game.
     */
    public void loadState()
    {
    	try 
    	{
			Scanner scan = new Scanner(new File("files/state.txt"));
			campaignCompleted = (scan.nextInt() == 1);
			highscore = scan.nextInt();
			player.setMoney(scan.nextInt());
			player.setPartLevel("guns", scan.nextInt());
			player.setPartLevel("wings", scan.nextInt());
			player.setPartLevel("ship", scan.nextInt());
			player.setPartLevel("cockpit", scan.nextInt());
			
		} 
    	catch (FileNotFoundException e) { System.out.println("State file could not be found."); }
    	catch (NoSuchElementException e) { System.out.println("State file is broken."); }	
    }
    
    /**
     * Clears the current state of the game, thus clearing all game history.
     */
    public void clearState()
    {
    	try 
    	{
			BufferedWriter writer = new BufferedWriter(new FileWriter("files/state.txt"));
			writer.write("0 0 0 1 1 1 1"); 
			writer.close();
			
		} catch (IOException e) { System.out.println("State file could not be written to."); }
    }
    
    /**
     * Switches whether or not the game is paused.
     */
    public void switchPause()
    {
    	paused = !paused;
    }
    
    /**
     * Makes the campaign complete and causes a "crest" to be drawn on the menu.
     */
    public void completeCampaign()
    {
    	campaignCompleted = true;
    }
    
    /**
     * Gets the actors ArrayList, allowing other classes access to every actor in the game.
     *
     * @return the actors ArrayList
     */
    public ArrayList<Actor> getActorList()
    {
    	return actors;
    }
    
    /**
     * Gets the player.
     *
     * @return the player ship of the game
     */
    public Player getPlayer()
    {
    	if (actors.size()>0 && actors.get(0) instanceof Player)
    		return (Player)actors.get(0);
    	else
    		return null;
    }
    
    /**
     * Gets the current scene of the game.
     *
     * @return the current scene of the game.
     */
    public String getScene()
    {
    	return scene;
    }
    
    /**
     * Gets the current score for the "onslaught" mode.
     *
     * @return the current score of the "onslaught" mode.
     */
    public int getScore()
    {
    	return score;
    }
    
    /**
     * Gets the highscore for the "onslaught" mode.
     *
     * @return the highscore for the "onslaught" mode.
     */
    public int getHighscore()
    {
    	return highscore;
    }
    
    /**
     * Checks if the current game mode is on "onslaught".
     *
     * @return true, if it is the "onslaught" mode
     */
    public boolean isOnslaughtMode()
    {
    	return onslaught;
    }
    
    /**
     * Gets the current focus of the game (the thing that responds to key input; usually the player). 
     *
     * @return the game's focus
     */
    public Object getFocus()
    {
    	return focus;
    }
    
    /**
     * Sets this game's focus (the thing that responds to key input; usually the player).
     *
     * @param focus the new focus for the game.
     */
    public void setFocus(Object focus)
    {
    	this.focus = focus;
    }
    
    /**
     * Checks if the game is paused.
     *
     * @return true, if it is paused
     */
    public boolean isPaused()
    {
    	return paused;
    }
    
    /**
     * The Nested Class KeyTracker.
     * Reads player key input and responds accordingly; allows the user to interact with the game.
     */
    private class KeyTracker extends KeyAdapter
    {	    	
        
        /* (non-Javadoc)
         * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
         */
        public void keyPressed(KeyEvent e)
        {
            int key = e.getKeyCode(); //a local variable that records the key code of whatever key was pressed.
            
            if (key == KeyEvent.VK_EQUALS) // "+" key
        	{
            	playSound("beep");
        		increaseVolume(1);
        	}
        	else if (key == KeyEvent.VK_MINUS)// "-" key
        	{
        		playSound("beep");
        		increaseVolume(-1);
        	}
            
            if (scene.equals("menu"))
            {
            	if (key == KeyEvent.VK_Q)
            		System.exit(0);
            	else if (creditsAlpha>0)
            	{
            		creditsAlpha = 0; 
            		changeMusic("SandOcean");
            	}
            	else if (key == KeyEvent.VK_UP)
            	{
            		menuChoice--;
            		if (menuChoice<0)
            			menuChoice = menuChoices.length-1;
            		
            		playSound("switch");
            	}
            	else if (key == KeyEvent.VK_DOWN)
            	{
            		menuChoice++;
            		if (menuChoice>=menuChoices.length)
            			menuChoice = 0;	
            		
            		playSound("switch");
            	}
            	else if (key == KeyEvent.VK_X || key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ENTER || key == KeyEvent.VK_RIGHT) //select
            	{
            		playSound("beep");
            		
            		if (menuChoice == 0) //campaign
            		{
            			onslaught = false;
            			initialize();
            			changeMusic("BigBlue");
            			scene = "game";	
            		}
            		else if (menuChoice == 1) //onslaught
            		{
            			onslaught = true;
            			initialize();
            			changeMusic("silence");
            			scene = "game";	
            		}
            		else if (menuChoice == 2)
            		{
            			scene = "instructions";
            		}
            		else if (menuChoice == 3)
            		{
            			optionChoices[4] = 0; //clear history option is reset. 
            			scene = "options";
            		}
            	}
            }
            else if (scene.equals("instructions"))
            {
            	scene = "menu";
            }
            else if (scene.equals("options"))
            {
            	if (key == KeyEvent.VK_BACK_SPACE || key == KeyEvent.VK_C)
            	{
            		scene = "menu";
            	}
            	else if (key == KeyEvent.VK_DOWN)
            	{
            		playSound("switch");
            		if (currentOption == 4) //clear history
            			optionChoices[4] = 0; //clear history option is reset.
            		currentOption++; 
            		if (currentOption>options.length-1)
            			currentOption = 0;
            		else if (currentOption == 5) //empty space
            			currentOption++;
            	}
            	else if (key == KeyEvent.VK_UP)
            	{
            		playSound("switch");
            		if (currentOption == 4) //clear history
            			optionChoices[4] = 0; //clear history option is reset.
            		currentOption--; 
            		if (currentOption<0)
            			currentOption = options.length-1;
            		else if (currentOption == 5) //empty space
            			currentOption--; 
            	}
            	else if (currentOption == 4) //clear history option
        		{
        			if (optionChoices[currentOption] == 0 && (key == KeyEvent.VK_X || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ENTER)) //next asks for clear history confirmation
        				optionChoices[currentOption]++;
        			else if (optionChoices[currentOption] == 1 && key == KeyEvent.VK_SPACE) //confirms history clearing
        			{
        				clearState();
        				loadState(); 
        				optionChoices[currentOption]++;
        			}
        			else //pressing any other key resets clearing history confirmation
        				optionChoices[currentOption] = 0;
        		}
            	else if (key == KeyEvent.VK_X || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ENTER)
            	{
            		playSound("beep");
            		if (currentOption<3)//background, music, & sound effects options
            		{
            			optionChoices[currentOption]++; 
            			if (optionChoices[currentOption]>options[currentOption].length-1)
            				optionChoices[currentOption] = 1;
            			
            			if (currentOption == 1) //music toggling
            			{
            				if (optionChoices[1] == 2) //music option is off
                        	{
                        		stopMusic(); 
                        	}
                        	else
                        		changeMusic("SandOcean");
            			}
            		}
            		else if (currentOption == 3) //volume option
            		{
            			increaseVolume(1); 
            		}
            		else if (currentOption == 6) //back option
            		{
            			scene = "menu"; 
            		}
              	}
            	else if (key == KeyEvent.VK_LEFT)
            	{
            		playSound("beep");
            		if (currentOption<3)//background, music, & sound effects options
            		{
            			optionChoices[currentOption]--; 
            			if (optionChoices[currentOption]<1)
            				optionChoices[currentOption] = options[currentOption].length-1;
            			
            			if (currentOption == 1) //music toggling
            			{
            				if (optionChoices[1] == 2) //music option is off
                        	{
                        		stopMusic(); 
                        	}
                        	else
                        		changeMusic("SandOcean");
            			}
            		}
            		else if (currentOption == 3) //volume option
            		{
            			increaseVolume(-1); 
            		}
            	}
            	
            	
            }
            else if (scene.equals("gameOver") || scene.equals("gameWon"))
            {
            	if (key == KeyEvent.VK_SPACE)
            	{
            		scene = "menu";
            		changeMusic("SandOcean");
            		
            		if (onslaught && score>highscore)
            		{
            			highscore = score;
            			saveState();
            		}
            	}
            }
            else if (scene.equals("game"))
            {
	            if (key == KeyEvent.VK_P)
	        	{
	            	playSound("beep");
	        		paused = !paused;
	        		pauseMusic(paused);
	        	}
	            else if (key == KeyEvent.VK_Q || key == KeyEvent.VK_BACK_SPACE)
	            {
	            	scene = "menu";
            		changeMusic("SandOcean");
	            }
	            else if (key == KeyEvent.VK_SPACE)
	        	{
	            	if (onslaught)
	            	{
	            		playSound("beep");
	            		scoreVisible = !scoreVisible;
	            	}
	            	else
	            	{
	            		scene = "upgrades";
	            		paused = true;
	            	}
	        	}
	            else if (focus == player && !paused)
	            {
	            	if (key == KeyEvent.VK_RIGHT)
	            		player.moveX(player.getXSpeed());
	            	else if (key == KeyEvent.VK_LEFT)
	            		player.moveX(-player.getXSpeed());
	            	else if (key == KeyEvent.VK_UP)
	            		player.moveY(-player.getYSpeed());
	            	else if (key == KeyEvent.VK_DOWN)
	            		player.moveY(player.getYSpeed());
	            	else if (key == KeyEvent.VK_X && player.canFire())
	            	{
	            		player.fire();
	            		player.switchCanFire();
	            	}
	            	else if (key == KeyEvent.VK_C)
	        		{
	        			playSound("switch");
	        			player.switchWeapon(1);
	        		}
	        		else if (key == KeyEvent.VK_Z)
	        		{
	        			playSound("switch");
	        			player.switchWeapon(-1);
	        		}
	            }
            }
            else if (scene.equals("upgrades"))
            {
            	if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_BACK_SPACE || key == KeyEvent.VK_G || key == KeyEvent.VK_C)
	        	{
	        		scene = "game";
	        		paused = false;
	        	}
            	else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_DOWN)
	        	{
            		playSound("switch");
	        		upgradeIndex++;
	        		if (upgradeIndex>=upgrades.length)
	        			upgradeIndex=0;
	        		upgradeGlow = upgradeGlows[upgradeIndex];
	        	}
            	else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_UP)
	        	{
            		playSound("switch");
	        		upgradeIndex--;
	        		if (upgradeIndex<0)
	        			upgradeIndex=upgrades.length-1;
	        		upgradeGlow = upgradeGlows[upgradeIndex];
	        	}
            	else if (key == KeyEvent.VK_X || key == KeyEvent.VK_ENTER || key == KeyEvent.VK_RIGHT)
	        	{
	        		player.upgradePart(upgrades[upgradeIndex].toLowerCase());
	        	}
            }
                  	
    	}
        
        /* (non-Javadoc)
         * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
         */
        public void keyReleased(KeyEvent e)
        {
            int key = e.getKeyCode(); //a local variable that records the key code of whatever key was pressed.
            
            if (focus == player)
            {
            	if (key == KeyEvent.VK_RIGHT && player.getdX()>0)
            		player.moveX(0);
            	else if (key == KeyEvent.VK_LEFT && player.getdX()<0)
            		player.moveX(0);
            	else if (key == KeyEvent.VK_UP && player.getdY()<0)
            		player.moveY(0);
            	else if (key == KeyEvent.VK_DOWN && player.getdY()>0)
            		player.moveY(0);
            	else if (key == KeyEvent.VK_X)
            		player.switchCanFire();
            }
        }
    }
    
}



