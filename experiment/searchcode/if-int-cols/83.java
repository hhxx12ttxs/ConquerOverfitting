/**
 * WumpusWorld.java
 * 2 player game
 * 
 */
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class WumpusWorld extends JFrame {
	// Named-constants for the game board
	public static final int ROWS = 6;  // ROWS by COLS cells
	public static final int COLS = 6;

	// Named-constants of the various dimensions used for graphics drawing
	public static final int CELL_SIZE = 100; // cell width and height (square)
	public static final int CANVAS_WIDTH = CELL_SIZE * COLS;  // the drawing canvas
	public static final int CANVAS_HEIGHT = CELL_SIZE * ROWS;
	public static final int GRID_WIDTH = 2;                   // Grid-line's width
	public static final int GRID_WIDHT_HALF = GRID_WIDTH / 2; // Grid-line's half-width

	// Use an enumeration (inner class) to represent the various states of the game
	public enum GameState {
		PLAYING, DRAW, H1_WON, H2_WON
	}
	private GameState currentState;  // the current game state

	private Player currentPlayer;  // the current player
	protected Player h1;    // hunter 1
	protected AI h2;    // hunter 2

	private Board board   ; // Game board of ROWS-by-COLS cells
	private DrawCanvas canvas; // Drawing canvas (JPanel) for the game board
	private JLabel statusBar;  // Status Bar
	private JLabel titleBar;  // Title Bar
	private JTextArea instructions; // instructions on rules
	private DrawRoom[][] squares = new DrawRoom[ROWS][COLS]; //rooms
	private ImageIcon imageH1[] = new ImageIcon[4];//4 images for hunter 1
	private ImageIcon imageH2[] = new ImageIcon[4];//4 images for hunter 2
	private int currentImageH1; //image hunter 1 currently using
	private int currentImageH2; //image hunter 2 currently using

	/** Constructor to setup the game and the GUI components */
	public WumpusWorld() {
		board = new Board();
		Room startRoom = board.getRoom(0,0);
		startRoom.setHints();
		h1 = new Player(startRoom);
		h2 = new AI(startRoom);

		canvas = new DrawCanvas();  // Construct a drawing canvas (a JPanel) 
		canvas.setFocusable(true);
		canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		canvas.setBorder(BorderFactory.createEmptyBorder(2, 5, 4, 5));

		//draw rooms
		canvas.setLayout(new GridLayout(ROWS,COLS));
		for (int i = 0; i < ROWS; i++)
			for (int j = 0; j < COLS; j++){
				squares[i][j] = new DrawRoom();
				squares[i][j].setBackground(Color.WHITE);
				squares[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
				canvas.add(squares[i][j]);
			}

		// Setup the title bar (JLabel)    
		titleBar = new JLabel(new ImageIcon("title.jpg"));
		titleBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 4, 5));


		// Setup the instruction area (JTextArea)                      
		instructions = new JTextArea(
				"Goal of the game: Grab the gold.\n\n" +
						"Player moves:\n" +
						"Move (F)orward, Turn (L)eft, Turn (R)ight,\n"+
						"(G)rab the Gold, (S)hoot the Arrow, \n" + 
						"(Q)uit the game, Play (A)gain.\n\n" +
						"Sensors: In the square \n"+
						"1. containing the wumpus and in the directly \n(not diagonally) adjacent squares, there is a (S)tench.\n"+
						"2. directly adjacent to a pit, there is a (B)reeze.\n"+
						"3. where the gold is, there is a (G)litter.\n" +
						"4. When the wumpus is killed, it (R)oars.\n\n"+                             
						"Environment:\n"+                               
						"1. one randomly located piece of gold,\n"+
						"2. two randomly located wumpus (not moving),\n" +
						"3. 20% of the randomly selected squares may have pits.\n\n"+
						"Equipment:\n" +                             
						"The Hunter has 2 arrows.\n\n" +
						"Notes:\n"+
						"1. The Hunter dies if he is in the square with pit.\n"+
						"2. The Hunter dies if he is in the square with a live wumpus.\n"+
						"3. A shot arrow will kill one wumpus in the direction\n" +
						"the Hunter is facing.\n"                         
				);
		instructions.setFont(new Font("Serif", Font.ITALIC, 16));
		instructions.setFocusable(false);
		instructions.setWrapStyleWord(true);
		instructions.setBorder(BorderFactory.createEmptyBorder(2, 5, 4, 5));

		// Setup the status bar (JLabel) to display status message
		statusBar = new JLabel("  ");
		statusBar.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 15));
		statusBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 4, 5));


		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.setBackground(Color.WHITE);
		cp.add(titleBar, BorderLayout.PAGE_START); // same as NORTH
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(instructions, BorderLayout.LINE_END);
		cp.add(statusBar, BorderLayout.PAGE_END); // same as SOUTH


		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();  // pack all the components in this JFrame
		setTitle("Wumpus World");
		setVisible(true);  // show this JFrame

		currentState = GameState.PLAYING; // ready to play
		currentPlayer = h1;       // h1 plays first
		currentImageH1 = 0;
		currentImageH2 = 0;
	}

	/** Initialize the game-board contents and the status */
	public void initGame() {
		board = new Board();
		Room startRoom = board.getRoom(0,0);
		startRoom.setHints();
		h1 = new Player(startRoom);
		h2 = new AI(startRoom);
		currentState = GameState.PLAYING; // ready to play
		currentPlayer = h1;       // h1 plays first
		currentImageH1 = 0;
		currentImageH2 = 0;

		for (int i = 0; i < ROWS; i++)  //hide pictures in all rooms
			for (int j = 0; j < COLS; j++){
				squares[i][j].hidePics();
			}

		repaint();
	}


	/** Update the currentState after the current player press a key */
	public void updateGame(Player currentPlayer, char command) {
		int currentPlayerLocation = currentPlayer.getCurrentRoom().getLocation(); //get current player location

		switch(Character.toUpperCase(command)){
		case 'F': 
			if(currentPlayer == h1) //hide the hunter's current location
				squares[currentPlayerLocation/ROWS][currentPlayerLocation%COLS].pics[2][0].setVisible(false);
			else
				squares[currentPlayerLocation/ROWS][currentPlayerLocation%COLS].pics[2][2].setVisible(false);
			currentPlayer.forward(); 
			break;
		case 'L': 
			currentPlayer.turnLeft(); 
			if(currentPlayer == h1){ 
				if (currentImageH1 == 0) //rotate picture
					currentImageH1 = 3;
				else
					currentImageH1 --;
			}
			else{
				if (currentImageH2 == 0) //rotate picture
					currentImageH2 = 3;
				else
					currentImageH2 --;
			}
			break; 
		case 'R': 
			currentPlayer.turnRight(); 
			if(currentPlayer == h1){ 
				if (currentImageH1 == 3) //rotate picture
					currentImageH1 = 0;
				else
					currentImageH1 ++;
			}
			else{
				if (currentImageH2 == 3) //rotate picture
					currentImageH2 = 0;
				else
					currentImageH2 ++;
			}
			break;
		case 'G': 
			if(currentPlayer.grabGold()){
				squares[currentPlayerLocation/ROWS][currentPlayerLocation%COLS].pics[1][2].setVisible(true); //show gold picture
			}
			break;
		case 'S': 
			currentPlayer.shoot(); 
			if(currentPlayer.isDragonKiller()){
				squares[currentPlayerLocation/ROWS][currentPlayerLocation%COLS].pics[1][1].setVisible(true); //show roar picture
				currentPlayer.resetDragonKiller();
			}
			break;
		case 'Q': 
			currentPlayer.quit(); 
			break;
		}

		if (hasWon(currentPlayer)) {  // check for win
			currentState = (currentPlayer == h1) ? GameState.H1_WON : GameState.H2_WON;
		} else if (isDraw()) {  // check for draw
			currentState = GameState.DRAW;
		}
		// Otherwise, no change to current state (still GameState.PLAYING).
	}

	/** Return true if it is a draw (i.e., both players died) */
	public boolean isDraw() {
		if (!h1.isAlive() && !h2.isAlive())
			return true;
		return false;
	}

	/** Return true if the player has gold */
	public boolean hasWon(Player currentPlayer) {
		return currentPlayer.hasGold();
	}

	class DrawRoom extends JPanel { //place images in a room
		public JLabel[][] pics = new JLabel[3][3]; //picture array

		public DrawRoom() {

			imageH1[0] = changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/hunter10.jpg");
			imageH1[1] = changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/hunter11.jpg");
			imageH1[2] = changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/hunter12.jpg");
			imageH1[3] = changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/hunter13.jpg");
			imageH2[0] = changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/hunter20.jpg");
			imageH2[1] = changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/hunter21.jpg");
			imageH2[2] = changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/hunter22.jpg");
			imageH2[3] = changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/hunter23.jpg");

			setLayout(new GridLayout(3,3));

			pics[0][0] = new JLabel(changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/breeze.jpg"));
			pics[0][1] = new JLabel(changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/stench.jpg"));
			pics[0][2] = new JLabel(changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/glitter.jpg"));
			pics[1][0] = new JLabel(changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/pitc.gif"));
			pics[1][1] = new JLabel(changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/roar.jpg"));
			pics[1][2] = new JLabel(changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/gold.jpg"));
			pics[2][0] = new JLabel(imageH1[currentImageH1]);
			pics[2][1] = new JLabel(changeImageSize("/Users/bwood1/Documents/workspace/wumpus-world/src/wumpusc.gif"));
			pics[2][2] = new JLabel(imageH2[currentImageH2]);

			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++){
					pics[i][j].setVisible(false);
					add(pics[i][j]);

				}
		}

		public void showPics(String s){ //show the pictures in the room
			String[] symbols = s.split("[ ]");
			for (int i = 0; i < symbols.length; i++){
				//System.out.println(symbols[i]);
				switch (symbols[i]){
				case "B": pics[0][0].setVisible(true); break;
				case "S": pics[0][1].setVisible(true); break;
				case "G": pics[0][2].setVisible(true); break;
				case "P": pics[1][0].setVisible(true); break;
				case "R": pics[1][1].setVisible(true); break;
				case "D": pics[1][2].setVisible(true); break;
				case "H1": pics[2][0].setIcon(imageH1[currentImageH1]);
				pics[2][0].setVisible(true); break;
				case "W": pics[2][1].setVisible(true); break;
				case "H2": pics[2][2].setIcon(imageH2[currentImageH2]);
				pics[2][2].setVisible(true); break;
				}
			}
		}

		public void hidePics(){ //hide all the pictures in a room
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					pics[i][j].setVisible(false);
		}
	}


	public ImageIcon changeImageSize(String fileName){ //change the image size to be fitted for the room
		ImageIcon myIcon = new ImageIcon(fileName);
		Image img = myIcon.getImage();
		Image newImg = img.getScaledInstance(CELL_SIZE/3, CELL_SIZE/3, java.awt.Image.SCALE_SMOOTH);
		ImageIcon newIcon = new ImageIcon(newImg);
		return newIcon;
	}

	/**
	 *  Inner class DrawCanvas (extends JPanel) used for custom graphics drawing.
	 */
	class DrawCanvas extends JPanel {
		public DrawCanvas(){
			//get key input
			addKeyListener(new KeyAdapter(){
				@Override 
				public void keyTyped(KeyEvent e){
					char command = e.getKeyChar();
					if (currentState == GameState.PLAYING) {
						updateGame(currentPlayer, command); // update state
						// Switch player
						Player nextPlayer = (currentPlayer == h1 ? h2 : h1);
						if (nextPlayer.isAlive()) {
							currentPlayer = nextPlayer;
						}
					} else if (Character.toUpperCase(command) == 'A') {       // game over
						initGame();// restart the game
					}
					// Refresh the drawing canvas
					repaint();  // Call-back paintComponent().
				}
			});
		}

		@Override
		public void paintComponent(Graphics g) {  // invoke via repaint()
			super.paintComponent(g);    // fill background
			setBackground(Color.WHITE); // set its background color

			int pl1 = h1.getCurrentRoom().getLocation(); //player 1 location
			int pl2 = h2.getCurrentRoom().getLocation(); //player 2 location

			// Loop through all of the rooms and find out where to draw the hunters
			for(int i = 0; i < ROWS; i++) // for each row
				for (int j = 0; j < COLS; j++){ // for each column
					if (board.rooms[i][j].isShown()){
						ArrayList<Character> perceptions = board.rooms[i][j].perceptions();
						String status = new String();
						for (Character ch: perceptions) {
							status += ch + " ";
						}
						if ((i == (pl1/ROWS)) && (j == (pl1%COLS)))
							status += "H1 ";
						if ((i == (pl2/ROWS)) && (j == (pl2%COLS)))
							status += "H2 ";
						squares[i][j].showPics(status);
					}
				}

			// Print status-bar message
			if (currentState == GameState.PLAYING) {
				statusBar.setForeground(Color.BLACK);
				if (currentPlayer == h1) {
					statusBar.setText("Hunter Green's Turn");
				} else {
					statusBar.setText("Hunter Black's Turn");
					h2.analyzeRoom();
					updateGame(h2, h2.decideMove());
					// Switch player
					Player nextPlayer = (currentPlayer == h1 ? h2 : h1);
					if (nextPlayer.isAlive()) {
						currentPlayer = nextPlayer;
					}
					repaint();
				}
			} else if (currentState == GameState.DRAW) {
				statusBar.setForeground(Color.RED);
				statusBar.setText("It's a Draw!");
			} else if (currentState == GameState.H1_WON) {
				statusBar.setForeground(Color.RED);
				statusBar.setText("Hunter Green Won!");
			} else if (currentState == GameState.H2_WON) {
				statusBar.setForeground(Color.RED);
				statusBar.setText("Hunter Black Won!");
			}
		}
	}

	/** The entry main() method */
	public static void main(String[] args) {
		// Run GUI codes in the Event-Dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new WumpusWorld(); // Let the constructor do the job
			}
		});
	}
}
