import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.JButton;


public class Main {

	private JFrame frame;
	
	private static int fieldWidth=9;
	private static int fieldHeight=9;
	private static int minesCount=10;
	
	/* values in field:
	 * 0-8 - how many mines in area
	 * -1 - mine
	 * -2 - known mine
	 * -3 - wrong guess 
	 */
	private int[][] field; // represents mine field with values
	private boolean isWrong=false;
	private int minesFound=0; // number of found mines
	private MyButton[][] fieldButtons; // buttons representing field 
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// checks if args are in format: WIDTH HEIGHT MINES. If not, gives default values: 9, 9, 10
		// values are in bounds as in Windows 7's Minesweeper. Max number of mines is approximately (WIDTH-1)*(HEIGHT-1)
		// it's the closest value to what Minesweeper calculates and I don't know it's exact formula
		if (args.length==3){
			int argWidth = Integer.parseInt(args[0]);
			int argHeight = Integer.parseInt(args[1]);
			int argMines = Integer.parseInt(args[2]);
			
			if (argWidth>8 && argWidth<25)
				fieldWidth=argWidth;
			
			if (argHeight>8 && argHeight<31)
				fieldHeight=argHeight;
			
			int maxMines = (fieldWidth-1)*(fieldHeight-1);
			if (argMines>9 && argMines<=maxMines)
				minesCount=argMines;
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}
	
	/**
	 * Returns random int between 0 (inclusive) and given max value (exclusive) 
	 * @param max
	 * @return
	 */
	private int random(int max){
		Random rand = new Random();
		return rand.nextInt(max);
	}
	
	
	/**
	 * Returns whether mine can be put in this place or not
	 * @param x
	 * @param y
	 * @return true/false
	 */
	private boolean canPutTheMine(int x, int y){
		// check if there is a mine
		if (field[x][y]==-1)
			return false;
		// check if field is surrounded by mines
		if (!minesCheck(x,y))
			return false;
		// check for surrounding fields, if we won't make them surrounded by mines
		for (int i=x-1; i<=x+1; i++)
			if (i>=0 && i<fieldWidth)
				for (int j=y-1; j<=y+1; j++)
					if (j>=0 && j<fieldHeight)
						if (i!=x || j!=y){
							field[x][y]=-1; // temporarily set the mine
							if (!minesCheck(i,j)){
								field[x][y]=0; // set the value back
								return false;
							}
							field[x][y]=0;
						}
		// for every other situation, we return true
		return true;
	}
	
	/**
	 * Checks if field isn't surrounded by mines
	 * @param x
	 * @param y
	 * @return true/false
	 */
	private boolean minesCheck(int x, int y){
		// situation 1: cell surrounded by mines
		if (countMines(x,y)==8)
			return false;
		// situation 2: cell in a corner surrounded by mines
		if ((x==0 || x==fieldWidth-1) && (y==0 || y==fieldHeight-1))
			if (countMines(x,y)==3)
				return false;
		// situation 3: cell is by the border and surrounded by mines
		if (x==0 || x==fieldWidth-1 || y==0 || y==fieldHeight-1)
			if (countMines(x,y)==5)
				return false;
		return true;
	}
	
	/**
	 * Counts how many mines surrounds given field
	 * @param x
	 * @param y
	 * @return number of mines
	 */
	private int countMines(int x, int y){
		int mines=0;
		for (int i=x-1; i<=x+1; i++)
			if (i>=0 && i<fieldWidth) // there is no sense running into another loop if i is lower than 0 or higher than fieldWidth
				for (int j=y-1; j<=y+1; j++)
					if (j>=0 && j<fieldHeight) // checking if our point is on field
						if (i!=x || j!=y) // checking if we aren't in place for which we are counting mines
							if (field[i][j]==-1 || field[i][j]==-2)
								mines++; // incrementing number of mines if there's mine or known mine
		return mines;
	}
	
	/**
	 * Generates mine field
	 */
	private void generateField(){
		// 1. create tables
		field = new int[fieldWidth][fieldHeight];	
		// 2. place mines
		int i=minesCount;
		while (i>0){
			int x=random(fieldWidth);
			int y=random(fieldHeight);
			if (canPutTheMine(x,y)){
				field[x][y]=-1;
				i--; // decrementing counter only when mine was placed
			}
		}		
		// 3. count surrounding mines
		for (i=0; i<fieldWidth; i++)
			for (int j=0; j<fieldHeight; j++)
				if (field[i][j]!=-1)
					field[i][j]=countMines(i,j);
	}
	
	/**
	 * Recursive method for showing all zeros surrounding given field.
	 * @param x
	 * @param y
	 */
	private void showZeros(int x, int y){
		// 1. check if we're on "zero"
		if (field[x][y]==0)
			// 2. checking surrounding fields, loops and conditions similar to those in counting mines
			for (int i=x-1; i<=x+1; i++)
				if (i>=0 && i<fieldWidth)
					for (int j=y-1; j<=y+1; j++)
						if (j>=0 && j<fieldHeight)
							if (i!=x || j!=y){
								// 3. display the field
								fieldButtons[i][j].setText(Integer.toString(field[i][j]));
								// 4. if button wasn't disabled earlier, we need to do disable it and do a recursive call
								if (fieldButtons[i][j].isEnabled()){
									fieldButtons[i][j].setEnabled(false);
									showZeros(i,j);
								}
							}
	}
	
	/**
	 * Shows field under given button. 
	 * 
	 * @param button
	 */
	private void showField(MyButton button){
		int x = button.getFieldX();
		int y = button.getFieldY();
		if (field[x][y]!=-2 && field[x][y]!=-3){
			if (field[x][y]==-1){
				// mine found
				button.setText("M");
				JOptionPane.showMessageDialog(frame, "Game over!");
				frame.dispose();
			}
			else{
				button.setEnabled(false);
				button.setText(Integer.toString(field[x][y]));
				if (field[x][y]==0)
					showZeros(x,y); // show all surrounding zeros if we clicked on 0
			}
		}
	}
	
	/**
	 * Sets field under given button as mine.
	 * 
	 * @param button
	 */
	private void setGuess(MyButton button){
		int x = button.getFieldX();
		int y = button.getFieldY();
		if (button.isEnabled()){
			if (field[x][y]==-1){
				// actions when under button there is a mine
				field[x][y]=-2; // change into known mine
				button.setText("!");
				minesFound++; // inc counter of found mines
				if (minesFound==minesCount && !isWrong){
					// every mine is found
					JOptionPane.showMessageDialog(frame, "You've won!");
					frame.dispose();
				}
			}
			else if (field[x][y]==-2){
				// actions when we change known mine into empty button
				field[x][y]=-1;
				button.setText("");
				minesFound--;
			}
			else if (field[x][y]==-3){
				// actions when we change wrong guess into empty button
				field[x][y]=countMines(x,y); // restoring mine count
				isWrong=false;
				button.setText("");
			}
			else {
				// actions when under button there is a number
				field[x][y]=-3;
				button.setText("!");
				isWrong=true;
			}
		}
	}
	
	/**
	 * Shows what's under every button
	 */
	private void cheat(){
		for (int i=0; i<fieldWidth; i++)
			for (int j=0; j<fieldHeight; j++){
				String text = Integer.toString(field[i][j]);
				if (text.equals("-1") || text.equals("-2")){
					text="M";
					fieldButtons[i][j].setBackground(Color.RED);
				}
				else if (text.equals("-3"))
					text=Integer.toString(countMines(i,j));
				fieldButtons[i][j].setText(text);
				//fieldButtons[i][j].setText(fieldButtons[i][j].getFieldX()+","+fieldButtons[i][j].getFieldY());
			}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, fieldWidth*50, fieldHeight*50);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(fieldHeight, fieldWidth, 0, 0)); // for convenience we set GridLayout with rows equals height, and columns equals width
		
		generateField();
		fieldButtons=new MyButton[fieldWidth][fieldHeight];
		for (int j=0; j<fieldHeight; j++)
			for (int i=0; i<fieldWidth; i++){
				fieldButtons[i][j] = new MyButton(i,j);
				fieldButtons[i][j].addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent e) {
						int button = e.getButton();
						if (button==MouseEvent.BUTTON1)
							// action for left click
							showField((MyButton)e.getSource());
						else if(button==MouseEvent.BUTTON3)
							// action for right click
							setGuess((MyButton)e.getSource());		
						else if(button==MouseEvent.BUTTON2)
							// action for middle click
							cheat();
					}

					@Override
					public void mouseEntered(MouseEvent e) {}

					@Override
					public void mouseExited(MouseEvent e) {}

					@Override
					public void mousePressed(MouseEvent e) {}

					@Override
					public void mouseReleased(MouseEvent e) {}
				});
				frame.getContentPane().add(fieldButtons[i][j]);
			}
		
	}

}

