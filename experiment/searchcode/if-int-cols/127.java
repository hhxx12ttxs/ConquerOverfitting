package prominesweeper;

import java.util.ArrayList;
import processing.core.PApplet;
import controlP5.*;


public class ProMinesweeper extends PApplet {

	private ControlP5 controlP5;
	public final static  int ROWS = 10;
	public final static int COLS = 10;
	public final static int BUTTON_SIZE = 30;
	private ArrayList <Button> bombs;
	private Button [][] buttons;
	//your added declarations here
	public void setup() 
	{
		size(380, 460); 
		bombs = new ArrayList<Button>();
		buttonSetup();

		//your added initializations here 
		setBombs();
	}
	public void draw() 
	{
		//may leave empty
	}
	public void controlEvent (ControlEvent theEvent) 
	{
		Button shot = (Button)theEvent.controller();
		if(mousePressed == true && mouseButton == LEFT)
		{
			
			
			int mX = mouseX / BUTTON_SIZE + 40;
			int mY = mouseY / BUTTON_SIZE + 40;
 			
			changeBackground(mX, mY);
		}
		else if(mousePressed == true && mouseButton == RIGHT)
		{
			shot.setColorBackground(color(0,255, 0));
		}
	}
	public void buttonSetup()
	{
		controlP5 = new ControlP5(this);
		buttons = new Button[ROWS][COLS];
		for(int r = 0; r < ROWS; r++)
		{
			for(int c = 0; c < COLS; c++)
			{
				int x = c * BUTTON_SIZE + 40; //cols at x=40,70,100,130,150,190,220,250,280,310
				int y = r * BUTTON_SIZE + 60; //rows at y=60,90,120,150,180,210,240,270,300,330
				buttons[r][c] = controlP5.addButton("("+r+","+c+")",0,x,y,BUTTON_SIZE-1, BUTTON_SIZE-1);
				buttons[r][c].setLabel(""); //remove this to label the buttons with the row and column
			}
		}
	}
	public void setBombs() //randomly places the bombs
	{
		for(int i = 0; i < ROWS; i++)
		{
			for(int j = 0; j < COLS; j++)
			{
				if(Math.random() < 1)
				{
					bombs.add(buttons[i][j]);
				}
			}
		}
	}
	public boolean isValid(int row, int col) //returns true if (row,col) is a valid location on the grid
	{
		if(row >= 0 && col >= 0)
		{
			if(row < ROWS && col < COLS)
			{
				return true;
			}
		}
		return false;
	}
	public int countBombs(int row, int col) //counts the bombs in the 8 neighbors--(remember to check to see if the neighboring button is valid before checking to see if it's a mine)
	{
		int count = 0;
		for(int r = row - 1; r < row + 1; r++)
		{
			for(int c = col - 1; c < col + 1; c++)
			{
				if(r != row && c != col)
				{
					for(Button temp : bombs)
					{
						if(temp == buttons[r][c])
						{
							count++;
						}
					}
				}
			}
		}
		return count;
	}
	public void changeBackground(int row, int col) //Changes the background of a button that has been clicked normally and uses setLabel to label the button with the number of neighboring mines
	{
		if(buttons[col-1][row].getColor().getBackground() != color(0,54,82))
			return;
		buttons[col][row].setColorBackground(color(175, 175, 175));
		if(countBombs(col, row) != 0) {
			buttons[col][row].setLabel(String.valueOf(countBombs(col,row)));
		}
	}
	public boolean isWon() //determines if the player has won the game
	{
		return true;
	}
	public void displayLosingMessage() //displays the positions of all the bombs and displays a losing message
	{
		for(int col = 0; col < COLS; col++)
		{
			for(int row = 0; row < ROWS; row++)
			{
				if(bombs.contains((buttons[col][row])))
				{
					buttons[col][row].setColorBackground(color(255, 0, 0));
				}
			}
		}
	}
}

