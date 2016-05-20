import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * This class the main game panel
 * @author Vinh Doan and Yuta Arai
 *
 */
public class GamePanel extends JPanel implements KeyListener, Runnable {

	/**
	 * Width of panel
	 */
	public static final int DRAWING_WIDTH = Circ.DRAWING_WIDTH;
	/**
	 * Height of panels
	 */
	public static final int DRAWING_HEIGHT = Circ.DRAWING_HEIGHT;
	
	Circ circ;
	private final static int squareSize = 40;

	private static boolean gameOver;
	private static int winnerIndici;
	

	private static Ball[] balls;
	private static ArrayList<Rectangle2D.Double> obstacles;
	private static ArrayList<Rectangle> graySqaures;
	private static ArrayList<Point> spawnPoints;
	
	private static ArrayList<Rectangle2D.Double> zone1;
	private static ArrayList<Rectangle2D.Double> zone2;
	
	private static ArrayList<Powerup> powerUps;
	private static ArrayList<Powerup> stillPowerUps;
	
	private GameUI gameUI;
	
	private Rectangle[] gridLines;
	private GameMode gameMode;
	
	//private boolean rightKey, leftKey, upKey, downKey, aKey, sKey, wKey, dKey;
	
	//game info
	private static int[] playerTypes;
	private static String[] playerNames;
	private static int gameType;
	private static int AILevel;
	private static int gameTime;
	
	
	private static int width;
	private static int height;

	private static int extraAI;
	
	private MiniGamePanel[] mgp;
	private JPanel fourSplitPanels;
	
	/**
	 * Creates game panel
	 * @param circ - the main Circ
	 */
	public GamePanel (Circ circ) {
		super();
		this.circ = circ;
		obstacles = new ArrayList<Rectangle2D.Double>();
		graySqaures = new ArrayList<Rectangle>();
		zone1 = new ArrayList<Rectangle2D.Double>();
		zone2 = new ArrayList<Rectangle2D.Double>();
		spawnPoints =  new ArrayList<Point>(); 
		powerUps = new ArrayList<Powerup>(); 
		stillPowerUps = new ArrayList<Powerup>(); 
		gameUI = new GameUI();
		gameOver = false;
		this.setBackground(Color.DARK_GRAY);
		this.setLayout(new BorderLayout());
		fourSplitPanels = new JPanel();
		fourSplitPanels.setBackground(Color.BLACK);
		fourSplitPanels.setLayout(new GridLayout(2,2, 5, 5));
		mgp = new MiniGamePanel[4];
		mgp[0] = new MiniGamePanel(0);
		mgp[1] = new MiniGamePanel(1);
		mgp[2] = new MiniGamePanel(2);
		mgp[3] = new MiniGamePanel(3);

		this.setVisible(false);
		
	}
	
	public static void setGameTime(int gameTime) {
		GamePanel.gameTime = gameTime;
	}
	
	public static void setAILevel(int aILevel) {
		AILevel = aILevel;
	}

	public static void setExtraAI(int extraAI) {
		GamePanel.extraAI = extraAI;
	}
	
	public static void setWinnerIndici(int winnerIndici) {
		GamePanel.winnerIndici = winnerIndici;
	}
	
	public static void setGameOver(boolean gameOver) {
		GamePanel.gameOver = gameOver;
	}
	
	public static void setPlayerTypes(int[] playerTypes) {
		GamePanel.playerTypes = playerTypes;
	}

	public static void setPlayerNames(String[] playerNames) {
		GamePanel.playerNames = playerNames;
	}

	public static void setGameType(int gameType) {
		GamePanel.gameType = gameType;
	}
	
	public static int getGameType() {
		return gameType;
	}

	/**
	 * Ends the game. Resets all settings
	 */
	public void endGame()
	{
		/*
		for (Ball b: balls)
		{
			b = null;
		}
		*/
		zone1.clear();
		zone2.clear();
		obstacles.clear();
		graySqaures.clear();
		spawnPoints.clear();
		powerUps.clear();
		stillPowerUps.clear();
		for (int i = 0; i < playerTypes.length; i++)
		{
			if (playerTypes[i] == 0)
			{
				fourSplitPanels.remove(mgp[i]);
				mgp[i].off();
			}
		}
		this.remove(fourSplitPanels);
		for (int i = obstacles.size() - 1; i >= 0; i--)
		{
			obstacles.remove(i);
		}
		gameOver = false;
		winnerIndici = 0;
		GameUI.setWinnerDetermined("");
	}

	/**
	 * Begins the game, creates the stage, draw the grid, and set the game mode.
	 */
	public void startGame()
	{
		int chosenAmount = getPlayers(playerTypes);
		balls = new Ball[chosenAmount + extraAI];
		int[][] stage;
		if (getGameType() == 1) // TO CHANGE TO CHOSEN TYPE
			stage = Stages.getStage("LastStage" + (int)(Math.random() * 5));
		else
			stage = Stages.getStage("SharkStage" + (int)(Math.random() * 4));
		arrToStage(stage);
		createGrid(stage[0].length, stage.length);
		int ballsPut = 0;
		int humanBalls = 0;
		for (int i = 0; i < playerTypes.length; i++)
		{
			// 0 = player, 1 = cpu, 2 = none
			if (playerTypes[i] == 0)
			{
				if (i == 0)
					balls[ballsPut] =  new Ball(false,0,0,"WASD");
				else if (i == 1)
					balls[ballsPut] =  new Ball(false,0,0,"IJKL");
				else if (i == 2)
					balls[ballsPut] =  new Ball(false,0,0,"TFGH");
				else
					balls[ballsPut] =  new Ball(false,0,0,"ARROWS");
				balls[ballsPut].setName(playerNames[i]);
				ballsPut++;
				humanBalls++;
			}
			else if (playerTypes[i] == 1)
			{
				balls[ballsPut] =  new Ball(false,0,0,AILevel, balls);
				balls[ballsPut].setName(playerNames[i]);
				ballsPut++;
			}
		}
		setOtherSizes(humanBalls);
		ArrayList<String> names = getPossibleNames();
		for (int i = chosenAmount; i < chosenAmount + extraAI; i++)
		{
			balls[i] = new Ball(false,0,0,AILevel, balls);
			int randNum = (int)(Math.random() * names.size());
			balls[i].setName(names.get(randNum));
			names.remove(randNum);
		}
		ballsPut = 0;
		for (int i = 0; i < playerTypes.length; i++)
		{
			if (playerTypes[i] == 0)
			{
				mgp[i].setThings(balls, obstacles, gridLines, graySqaures, zone1, zone2, powerUps, stillPowerUps);
				mgp[i].setBall(balls[ballsPut]);
				ballsPut++;
			}
			else if (playerTypes[i] == 1)
			{
				ballsPut++;
			}
		}
		
		if (getGameType() == 1) // TO CHANGE TO CHOSEN TYPE
		{
			gameMode = new LastManMode(gameTime, balls);
		}
		else
			gameMode = new SharkMode(gameTime, balls, zone1, zone2);
		
		gameMode.startGameMode();
		this.setVisible(true);
		new Thread(this).start();	
		
	}
	
	private ArrayList<String> getPossibleNames()
	{
		ArrayList<String> names = new ArrayList<String>();
		names.add("VinhDoan");
		names.add("YutaArai");
		names.add("yz");
		names.add("iQuit");
		names.add("SomeBall");
		names.add("MrShelby");
		names.add("Koala");
		names.add("TagPro");
		names.add("I<3Circ");
		names.add("LameName");
		return names;
	}
	
	/**
	 * Creates a ball within a specified radius
	 * @param b - Ball to spawn
	 * @param rad - Radius
	 */
	public static void spawnBall(Ball b, int rad)
	{
		int sNum = spawnPoints.size();
		Point p = spawnPoints.get((int)(Math.random() * sNum));
		int pX = (int) p.getX();
		int pY = (int) p.getY();
		int xPos = pX + (rad - (int) (Math.random() * (rad + 1))) * squareSize;
		int yPos = pY + (rad - (int) (Math.random() * (rad + 1))) * squareSize;
		while(!isInOther(xPos, yPos))
		{
			p = spawnPoints.get((int)(Math.random() * sNum));
			pX = (int) p.getX();
			pY = (int) p.getY();
			xPos = pX + (rad - (int) (Math.random() * (rad + 1))) * squareSize;
			yPos = pY + (rad - (int) (Math.random() * (rad + 1))) * squareSize;
		}
		b.setX(xPos);
		b.setY(yPos);
	}
	
	/**
	 * Spawns ball at a place in the zone
	 * @param b - Ball to spawn
	 */
	public static void zoneSpawn(Ball b)
	{
		int zNum = zone1.size();
		Rectangle2D.Double rect = zone1.get((int)(zNum * Math.random()));
		int xPos = (int) rect.getX();
		int yPos = (int) rect.getY();
		while(!isInOther(xPos, yPos))
		{
			rect = zone1.get((int)(zNum * Math.random()));
			xPos = (int) rect.getX();
			yPos = (int) rect.getY();
		}
		b.setX(xPos);
		b.setY(yPos);
	}
	
	/**
	 * Spawns a power up in a random location
	 */
	public static void spawnPowerUp()
	{
		int rNum = graySqaures.size();
		Rectangle rect = graySqaures.get((int)(rNum * Math.random()));
		int xPos = (int) rect.getX();
		int yPos = (int) rect.getY();
		while(!isInOther(xPos, yPos))
		{
			rect = graySqaures.get((int)(rNum * Math.random()));
			xPos = (int) rect.getX();
			yPos = (int) rect.getY();
		}
		
		if ((int)(Math.random() * 2) == 0)
		{
			powerUps.add(new Immunity(xPos,yPos));
		}
		else
		{
			powerUps.add(new JukeJuice(xPos,yPos));
		}
	}
	
	/**
	 * Wipes board of power ups
	 */
	public static void clearPowerUps()
	{
		powerUps.clear();
	}
	
	private static boolean isInOther(int x, int y)
	{
		if(x <= 1 || y <= 1 || x >= width - 20 || y >= height - 20)
		{
			return false;
		}
		for (Ball b:balls)
		{
			if (b != null && Math.abs(b.getX() - x) < .00001 && Math.abs(b.getY() - y) < .000001)
				return false;
		}
		for (Rectangle2D.Double w:obstacles)
		{
			if (Math.abs(w.getX() - x) < .00001 && Math.abs(w.getY() - y)< .00001)
				return false;
		}
		for (Powerup w:powerUps)
		{
			if (Math.abs(w.getX() - x) < .00001 && Math.abs(w.getY() - y)< .00001)
				return false;
		}
		for (Powerup w:stillPowerUps)
		{
			if (Math.abs(w.getX() - x) < .00001 && Math.abs(w.getY() - y)< .00001)
				return false;
		}
		return true;
		
	}
	
	private void setOtherSizes(int n)
	{
		add(fourSplitPanels);
		for (MiniGamePanel m: mgp)
		{
			m.setOtherSizes(n);
		}
		
		if (n == 1)
		{
			gameUI.setOnlyOne(true);
			fourSplitPanels.setLayout(new GridLayout(1,1, 0, 0));
			
			for (int i = 0; i < playerTypes.length; i++)
			{
				if (playerTypes[i] == 0)
				{
					fourSplitPanels.add(mgp[i]);
				}
			}
		}
		else if (n == 2)
		{
			gameUI.setOnlyOne(false);
			fourSplitPanels.setLayout(new GridLayout(1,2, 5, 5));
			
			for (int i = 0; i < playerTypes.length; i++)
			{
				if (playerTypes[i] == 0)
				{
					fourSplitPanels.add(mgp[i]);
				}
			}
		}
		else // n = 3, 4
		{
			gameUI.setOnlyOne(false);
			fourSplitPanels.setLayout(new GridLayout(2,2, 5, 5));
			
			fourSplitPanels.add(mgp[0]);
			fourSplitPanels.add(mgp[1]);
			fourSplitPanels.add(mgp[2]);
			fourSplitPanels.add(mgp[3]);
		}
	//	add(fourSplitPanels);
	}
	
	private int getPlayers(int[] playerTypes)
	{
		int initSize = playerTypes.length;
		for (int i:playerTypes)
		{
			if (i == 2)
				initSize--;
		}
		return initSize;
	}
	
	private void createGrid(int v, int h)
	{
		int vertLines = v;
		int horLines = h;
		gridLines = new Rectangle[horLines + vertLines];
		for (int i = 0; i < horLines; i++)
		{
			gridLines[i] = new Rectangle(0, i * squareSize,  vertLines * 40, 1);
		}
		
		for (int i = 0; i < vertLines; i++)
		{
			gridLines[i + horLines] = new Rectangle(i * squareSize, 0, 1, horLines * 40);
		}
	}
	
	private void arrToStage(int[][] stage)
	{
		int rows = stage.length;
		int cols = stage[0].length;
		width = cols * 40;
		height = rows * 40;
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				int x = j * squareSize;
				int y = i * squareSize;
				if (stage[i][j] == 1)
					obstacles.add(new Wall(x,y));
				else if (stage[i][j] == 2)
				{
					spawnPoints.add(new Point(x,y));
				}
				else if 
				(stage[i][j] == 3)
				{
					zone1.add(new Rectangle2D.Double(x,y, 40, 40));
				}
				else if (stage[i][j] == 4)
				{
					zone2.add(new Rectangle2D.Double(x,y, 40, 40));
				}
				else if (stage[i][j] == 5) // bomb
				{
					stillPowerUps.add(new Bomb(x,y));
				}
				else if (stage[i][j] == 6) // boostpad
				{
					stillPowerUps.add(new BoostPad(x,y));
				}

				if (stage[i][j] == 0 || stage[i][j] == 2 || stage[i][j] == 3 || stage[i][j] == 4 )
					graySqaures.add(new Rectangle(x, y, squareSize , squareSize));
			}
		}
	}

	/**
	 * Draws game panel
	 * @param g Graphics
	 */
    protected void paintChildren(Graphics g) 
	{
    	super.paintChildren(g);
		int width = getWidth();
		int height = getHeight();
		gameUI.draw(g, width / 2, height);
	}


	public void keyPressed(KeyEvent e) {
		for(Ball b:balls)
		{
			b.keyPressed(e);
		}
	}

	public void keyReleased(KeyEvent e) {
		for(Ball b:balls)
		{
			b.keyReleased(e);
		}
	}

	public void keyTyped(KeyEvent e) {

	}

	
	
	/**
	 * Run program.
	 */
	public void run() {
		while (!gameOver) { // Modify this to allow quitting
		
//			for(Ball b:balls)
//			{
//				b.run(obstacles);
//			}
			if(gameType == 1)
			{
				for(Ball ball: balls)
				{
					ball.run(obstacles, powerUps, stillPowerUps, balls);
				}
			}
			else
			{
				for(Ball ball: balls)
				{
					if(ball.isMarked())
						ball.run(obstacles, zone1, zone2, powerUps, stillPowerUps, balls);
					else
						ball.run(obstacles,powerUps, stillPowerUps, balls);
				}
			}
			
			

			repaint();

			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {}
		}
		
		if (gameOver)
		{
			String winner;
			if (winnerIndici == -2)
				winner = "THE SHARKS WIN";
			else
				winner = playerNames[winnerIndici] + " WINS";
				
			GameUI.setWinnerDetermined(winner);
			this.addMouseListener(new PauseMouseHandler(this));
			repaint();
		}
		
	}
	
	class PauseMouseHandler implements MouseListener
	{
		GamePanel g;
		public PauseMouseHandler(GamePanel g)
		{
			this.g = g;
		}
		
		@Override
		public void mouseClicked(MouseEvent arg0) {
			int aX = arg0.getX();
			int aY = arg0.getY();
			
			//System.out.println(aX  + "  " + aY);
			int h = getHeight();
			int w = getWidth();
			if (aX < w / 2 && aY > h/2) //MENU
			{
				g.removeMouseListener(this);
				endGame();
				circ.changePanel(2);
			}
			else if (aX > w / 2 && aY > h/2) //RESTART
			{
				g.removeMouseListener(this);
				endGame();
				startGame();
			}
				
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

	}

}

