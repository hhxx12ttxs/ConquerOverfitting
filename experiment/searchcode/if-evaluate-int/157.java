package com.kudzu.android.babttt;
/*
*	Kate Kinnear
*	3165595
*	CS4725
*	TicTacToe Alpha Beta Pruning Program
*  Game board program
*
*	November 19, 2006
*/


import java.awt.*;
import java.util.*;


/*
*	Class to remember the best move during alpha beta pruning
*/
class MoveInfo {

   public static final int NEGATIVE_INFINITY = -500000;

	private int moveX;
	private int moveY;
	private int score;

	//Constructor
	public MoveInfo()
	{
		moveX = -1;
		moveY = -1;
		score = NEGATIVE_INFINITY;
	}
	
	//Get X value of best move
	public int getMoveX()
	{
		return moveX;
	}
	
	//Set X value of best move
	public void setMoveX(int x)
	{
		moveX = x;
	}
	
	//Get Y value of best move
	public int getMoveY()
	{
		return moveY;
	}
	
	//Set Y value of best move
	public void setMoveY(int y)
	{
		moveY = y;
	}
	
	//Get value of the score associated with this move
	public int getScore()
	{
		return score;
	}
	
	//Set value of the score associated with this move
	public void setScore(int s)
	{
		score = s;
	}


}// end MoveInfo


/*
*	This class represents the current state of the game
*/
class GameBoard {

    //constants
	 public static final int POSITIVE_INFINITY = 500000;
	 public static final int NEGATIVE_INFINITY = -500000;
    public static final char EMPTY = 'E';
    public static final char PLAYERX = 'X';
    public static final char PLAYERO = 'O';
	 public static final int NOWIN = 0;
	 public static final int XWIN = 1;
	 public static final int OWIN = 2;
    public static final int TIE = 3;


    // board fields
    private char[][] board;
	 private int emptySquares;
	 private int size;
	 private int won;
    private int moveX;
    private int moveY;
	 private int winLength;


	//Constructor
	//Creates empty board of size N x N
    public GameBoard(int N, int M){
	 	  size = N;
		  winLength = M;
		  emptySquares = N*N;
		  won = NOWIN;
        board = new char[N][N];
        for(int i = 0; i < N ;i++)
		  {
            for(int j = 0; j < N; j++)
				{
                board[i][j] = 'E';
				} 
		  }
		  moveX = -1;
		  moveY = -1;
    }

	//Copy constructor for creating children
    public GameBoard(GameBoard b, int N, int M, int e){
	     size = N;
		  winLength = M;
		  emptySquares = e;
        board = new char[N][N];
		  moveX = -1;
		  moveY = -1;
        for(int i=0;i<N;i++)
		  {
            for(int j=0;j<N;j++)
				{
                board[i][j] = b.board[i][j];
				}
		  }
    }
	 
	 //Set length needed to win
	 public void setWinLength(int m)
	 {
	 	winLength = m;
	 }
	 
	 //Access length needed to win
	 public int getWinLength()
	 {
	 	return winLength;
	 }
	 
	 
	 //Reset best move to null
	 public void resetMove()
	 {
	   moveX = -1;
		moveY = -1;
	 }
	 
	 //Access size of board
	 public int getSize()
	 {
	 	return size;
	 }
	 
	 //Access game winner
	 public int getWinner()
	 {
	 	return won;
	 }
	 
	 //Get X value of the best move
	 public int getMoveX()
	 {
	 	return moveX;
	 }
	 
	 //Get Y value of the best move
	 public int getMoveY()
	 {
	 	return moveY;
	 }
	 
	 //Set X value of the best move
	 public void setMoveX(int x)
	 {
	 	moveX = x;
	 }
	 
	 //Set Y value of the best move
	 public void setMoveY(int y)
	 {
	 	moveY = y;
	 }
	 
	 //Get number of empty game tiles
	 public int getEmptySquares()
	 {
	 	return emptySquares;
	 }
	 
	 //Set number of empty game tiles
	 public void setEmptySquares(int e)
	 {
	 	emptySquares = e;
	 }
	 
	 //A game tile has been filled, decrement empty squares
	 public void fillSquare()
	 {
	 	emptySquares--;
	 }
	 
	 //Set game winner
	 //Return true if integer passed is a valid winner code
	 //Otherwise, return false
	 public boolean setWinner(int w)
	 {
	 	//Make sure the value is a valid code
	 	if(w >= NOWIN && w <= TIE)
		{
			won = w;
			return true;
		}
		else
		{
			return false;
		}
	 }
	 
	 //Set size of board
	 public void setSize(int s)
	 {
	 	size = s;
	 }
	 
	 //Set a tile on the board
    public void setTile(int x,int y,char val){
        board[x][y] = val;
    }

	//Get the value of a tile on the board
    public char getTile(int x,int y){
        return board[x][y];
    }
	 
	 //Print Game Board
    public void printBoard()
	 {
	 	  System.out.println("Current Board:");
        for(int i = 0; i < size; i++)
		  {
            for(int j = 0; j < size; j++)
				{
                System.out.print(board[i][j] + " ");
				}
				System.out.print("\n");
        }
    }
	 
	 //Updated to account for winning length and all diagonals
	 //When a new move is made, check to see if it is a winning move
	 //Set the board winner if there is one
	 //Return true if there is a winner or a tie, otherwise false
	 public boolean checkWinner(int x, int y, char C)
	 {
	 		int check = 0;
			int i;
	 		//Check row
			for(i = 0; i < size; i++)
			{
				if(board[x][i] == C)
				{
					check++;
					if(check == winLength)
					{
						if(C == PLAYERX)
						{
							setWinner(XWIN);
						}
						else if(C == PLAYERO)
						{
							setWinner(OWIN);
						}
						return true;
					}
				}
				else
				{
					check = 0;
				}
			}//end for
			if(check == winLength)
			{
				if(C == PLAYERX)
				{
					setWinner(XWIN);
				}
				else if(C == PLAYERO)
				{
					setWinner(OWIN);
				}
				
				return true;
			}//end if
			
			//Check column
			check = 0;
			for(i = 0; i < size; i++)
			{
				if(board[i][y] == C)
				{
					check++;
					if(check == winLength)
					{
						if(C == PLAYERX)
						{
							setWinner(XWIN);
						}
						else if(C == PLAYERO)
						{
							setWinner(OWIN);
						}
						return true;
					}
				}
				else
				{
					check = 0;
				}
			} 
			if(check == winLength)
			{
				if(C == PLAYERX)
				{
					setWinner(XWIN);
				}
				else if(C == PLAYERO)
				{
					setWinner(OWIN);
				}
				
				return true;
			}
			
			//Need to always check diagonals
			//Check diagonal with negative slope
			int tempx = x;
			int tempy = y;
			while(tempx > 0 && tempy > 0)
			{
				tempx--;
				tempy--;
			}
			//System.out.println("X:" + tempx + " Y:" + tempy);
			check = 0;
			while(tempx < size && tempy < size)
			{
				if(board[tempx][tempy] == C)
				{
					check++;
					//System.out.println("There is a " + C + " at space " + tempx + " " + tempy);
					if(check == winLength)
					{
						//System.out.println("Winner with Negative slope diagonal!");
						if(C == PLAYERX)
						{
							setWinner(XWIN);
						}
						else if(C == PLAYERO)
						{
							setWinner(OWIN);
						}
						return true;
					}//end if
					
				}//end if
				else
				{
					check = 0;
				}
				tempx++;
				tempy++;
			}//end while
			
			if(check == winLength)
			{
				//System.out.println("Winner with Negative slope diagonal!");
				if(C == PLAYERX)
				{
					setWinner(XWIN);
				}
				else if(C == PLAYERO)
				{
					setWinner(OWIN);
				}
				
				return true;
			}//end if
			
	 		//Check diagonal with positive slope
			//System.out.println("Check positive slope diagonal");
			check = 0;
			tempx = x;
			tempy = y;
			while(tempx > 0 && tempy < size - 1)
			{	
				tempx--;
				tempy++;
			}
			
			while(tempx < size && tempy >= 0)
			{
				if(board[tempx][tempy] == C)
				{
					check++;
					//System.out.println("There is a " + C + " at space " + tempx + " " + tempy);
					if(check == winLength)
					{
						//System.out.println("Winner with Positive slope diagonal!");
						if(C == PLAYERX)
						{
							setWinner(XWIN);
						}
						else if(C == PLAYERO)
						{
							setWinner(OWIN);
						}
						return true;
					}//end if
					
				}//end if
				else
				{
					check = 0;
				}
				tempx++;
				tempy--;
				
			}//end while
			
				if(check == winLength)
				{
					//System.out.println("Winner with Positive slope diagonal!");
					if(C == PLAYERX)
					{
						setWinner(XWIN);
					}
					else if(C == PLAYERO)
					{
						setWinner(OWIN);
					}
				
					return true;
				}//end if
			
		//Check for tie
		if(emptySquares == 0)
		{
			setWinner(TIE);
			return true;
		}

		//No winner found
		return false;

    }

	 //Evaluate board for a player
	 //Function awards N points to a player for every row/column/diagonal
	 //they have with exactly N of their tiles filled and none of the opponent's
	 //tiles
	 //This is calculated for both players, and the final value of the board
	 //is calculated by subtracting the points of the opponent from the points
	 //of the current player
	 //Should award more points if 
	 public int evaluationSimple(char player)
	 {
	 	char other;
		int utility = 0;
		int i,j,k;
		int countRowP = 0,countRowO = 0,countColP = 0,countColO = 0;
		int countDiagP = 0,countDiagO = 0;
				
		//Don't use 0 index,  not needed for the function
		int arrayPlayer[] = new int[size+1];
		int arrayOther[] = new int[size+1];
	 
	 	//Figure out what player you are evaluating for
		//and who the opponent is
	 	if(player == PLAYERX)
			other = PLAYERO;
		else
			other = PLAYERX;
			
		//Count X's and O's for each column, row
		for(i = 0; i < size; i++)
		{
			countRowP = 0;
			countRowO = 0;
			countColP = 0;
			countColO = 0;
		
			for(j = 0; j< size; j++)
			{
				//Add rows and columns
				if(board[i][j] == player)
				{
					//System.out.println("AddORow " + i + " " + j);
					countRowP++;
				}
				if(board[i][j] == other)
				{
					//System.out.println("AddXRow " + i + " " + j);
					countRowO++;
				}
				//Add columns
				if(board[j][i] == player)
				{
					//System.out.println("AddOCol " + j + " " + i);
					countColP++;
				}
				if(board[j][i] == other)
				{
					//System.out.println("AddXCol " + j + " " + i);
					countColO++;
				}
			}
						
			if(countRowP == 0 && countRowO != 0)
			{
				arrayOther[countRowO]++;
			}
			if(countRowO == 0 && countRowP != 0)
			{
				arrayPlayer[countRowP]++;
			}
			if(countColP == 0 && countColO != 0)
			{
				arrayOther[countColO]++;
			}
			if(countColO == 0 && countColP != 0)
			{
				arrayPlayer[countColP]++;
			}
			
		} 
		
		//Count diagonals
		//Do Positive Slope Diagonals
		int startRow, startCol;
		for(k = 0; k < size*2; k++)
		{
			//Only check if diagonal is big enough
			if(k >= winLength-1 && k < (size*2 - winLength))
			{
			
			if(k > size-1)
			{
				startRow = size-1;
			}
			else
			{
				startRow = k;
			}
			
			if(k <= size-1)
			{
				startCol = 0;
			}
			else
			{
				startCol = k%(size-1);
			}
			
			while(startRow > 0 && startCol < size-1)
			{
				if(board[startRow][startCol] == player)
				{
					//System.out.println("AddOCol " + j + " " + i);
					countDiagP++;
				}
				if(board[startRow][startCol] == other)
				{
					//System.out.println("AddXCol " + j + " " + i);
					countDiagO++;
				}
			
				startRow--;
				startCol++;
			}
			
			if(countDiagP == 0 && countDiagO != 0)
			{
				arrayOther[countDiagO]++;
			}
			if(countDiagO == 0 && countDiagP != 0)
			{
				arrayPlayer[countDiagP]++;
			}
			countDiagP = 0;
			countDiagO = 0;
			
			}//end if
		}//end for
		
		countDiagP = 0;
		countDiagO = 0;
		//Do Negative Slope Diagonals
		for(k = 0; k < size*2; k++)
		{
			//Only check if diagonal is big enough
			if(k >= winLength-1 && k < (size*2 - winLength))
			{
			
			if(k > size-1)
			{
				startRow = 0;
				startCol = k%(size-1);
			}
			else
			{
				startCol = 0;
				startRow = (size-1) - k;
			}
			
			while(startRow < size-1 && startCol < size-1)
			{
				if(board[startRow][startCol] == player)
				{
					//System.out.println("AddOCol " + j + " " + i);
					countDiagP++;
				}
				if(board[startRow][startCol] == other)
				{
					//System.out.println("AddXCol " + j + " " + i);
					countDiagO++;
				}
			
				startRow++;
				startCol++;
			}
			
			if(countDiagP == 0 && countDiagO != 0)
			{
				arrayOther[countDiagO]++;
			}
			if(countDiagO == 0 && countDiagP != 0)
			{
				arrayPlayer[countDiagP]++;
			}
			countDiagP = 0;
			countDiagO = 0;
			
			}//end if
		}//end for
		
		
		int tempPlayer;
		int tempOther;
		
		//Updated evaluation function for M
		for(i = 1; i <= size; i++)
		{
			if(i >= winLength-1)
			{
				tempPlayer = 2*i*arrayPlayer[i];
				tempOther = 2*i*arrayOther[i];
			}
			else
			{
				tempPlayer = i*arrayPlayer[i];
				tempOther = i*arrayOther[i];
			}
			utility = utility + tempPlayer - tempOther;
		}
		
		return utility;
	 
	 }
	 
	 
	 
	 //Evaluate board for a player
	 //This is calculated for both players, and the final value of the board
	 //is calculated by subtracting the points of the opponent from the points
	 //of the current player
	 //Need to award points even if there are X's and O's in the same row/col/diag
	 public int evaluation(char player)
	 {
	 	char opponent;
		int possibleWin = 0;
		int utilityPlayer = 0, utilityOpponent = 0;
		int i,j,k,m;
		int lengthPlayer = 0, lengthOpponent = 0;
		int countPlayer = 0, countOpponent = 0;
		int startRow, startCol;
	 
	 	//Use the simple evaluation if possible
	 	if(winLength == size || winLength == size-1)
		{
			return evaluationSimple(player);
		}
		
		//Figure out what player you are evaluating for
		//and who the opponent is
	 	if(player == PLAYERX)
			opponent = PLAYERO;
		else
			opponent = PLAYERX;
			
		
		//System.out.println("Check Rows");
		//Do each row
		for(i = 0; i < size; i++)
		{
			//Reset lengths for each new row
			lengthPlayer = 0;
			lengthOpponent = 0;
			
			for(j = 0; j < size; j++)
			{
			
				if(board[i][j] == EMPTY)
				{
					lengthPlayer++;
					lengthOpponent++;
				}
				else if(board[i][j] == player)
				{
					lengthPlayer++;
					
					//Check opponent's section
					//Only give points if big enough to win in
					if(lengthOpponent >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthOpponent/winLength;
					
						//Start checking at square before this one
						//i stays the same, same row
						//k is the end of the section
						k = j-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthOpponent >= winLength + 1)
						{
							utilityOpponent = utilityOpponent + guaranteedWinRow(i,k,lengthOpponent);
						}
						
						countOpponent = 0;
						while(lengthOpponent > 0)
						{
							//Count number of total squares
							if(board[i][k] == opponent)
							{
								countOpponent++;
							}
							
							k--;
							lengthOpponent--;
						}//end while
						
						utilityOpponent = utilityOpponent + possibleWin*countOpponent;
						
					}//end if
					
					//Reset opponent's section
					lengthOpponent = 0;
				}
				else if(board[i][j] == player)
				{
					lengthOpponent++;
					
					if(lengthPlayer >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthPlayer/winLength;
					
						//Start checking at square before this one
						//i stays the same, same row
						//k is the end of the section
						k = j-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthPlayer >= winLength + 1)
						{
							utilityPlayer = utilityPlayer + guaranteedWinRow(i,k,lengthPlayer);
						}
						
						countPlayer = 0;
						while(lengthPlayer > 0)
						{
							//Count number of total squares
							if(board[i][k] == player)
							{
								countPlayer++;
							}
							
							k--;
							lengthPlayer--;
						}//end while
						
						utilityPlayer = utilityPlayer + possibleWin*countPlayer;
						
					}//end if
					
					lengthPlayer = 0;
				}//end else if

			}//end for j row
			if(lengthPlayer > lengthOpponent)
			{
				if(lengthPlayer >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthPlayer/winLength;
					
						//Start checking at square before this one
						//i stays the same, same row
						//k is the end of the section
						k = j-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthPlayer >= winLength + 1)
						{
							utilityPlayer = utilityPlayer + guaranteedWinRow(i,k,lengthPlayer);
						}
						
						countPlayer = 0;
						while(lengthPlayer > 0)
						{
							//Count number of total squares
							if(board[i][k] == player)
							{
								countPlayer++;
							}
							
							k--;
							lengthPlayer--;
						}//end while
						
						utilityPlayer = utilityPlayer + possibleWin*countPlayer;
						
					}//end if
					
					lengthPlayer = 0;
			}
			else
			{
				if(lengthOpponent >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthOpponent/winLength;
					
						//Start checking at square before this one
						//i stays the same, same row
						//k is the end of the section
						k = j-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthOpponent >= winLength + 1)
						{
							utilityOpponent = utilityOpponent + guaranteedWinRow(i,k,lengthOpponent);
						}
						
						countOpponent = 0;
						while(lengthOpponent > 0)
						{
							//Count number of total squares
							if(board[i][k] == opponent)
							{
								countOpponent++;
							}
							
							k--;
							lengthOpponent--;
						}//end while
						
						utilityOpponent = utilityOpponent + possibleWin*countOpponent;
						
					}//end if
					
					//Reset opponent's section
					lengthOpponent = 0;
			}
		}//end for i row
		


		//System.out.println("Check Columns");
		//Do each column
		for(i = 0; i < size; i++)
		{
			//Reset lengths for each new column
			lengthPlayer = 0;
			lengthOpponent = 0;
			
			for(j = 0; j < size; j++)
			{
				if(board[j][i] == EMPTY)
				{
					lengthPlayer++;
					lengthOpponent++;
				}
				else if(board[j][i] == player)
				{
					lengthPlayer++;
					
					//Check opponent's section
					//Only give points if big enough to win in
					if(lengthOpponent >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthOpponent/winLength;
					
						//Start checking at square before this one
						//i stays the same, same column
						//k is the end of the section
						k = j-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthOpponent >= winLength + 1)
						{
							utilityOpponent = utilityOpponent + guaranteedWinCol(i,k,lengthOpponent);
						}
						
						countOpponent = 0;
						while(lengthOpponent > 0)
						{
							//Count number of total squares
							if(board[k][i] == opponent)
							{
								countOpponent++;
							}
							
							k--;
							lengthOpponent--;
						}//end while
						
						utilityOpponent = utilityOpponent + possibleWin*countOpponent;
						
					}//end if
					
					//Reset opponent's section
					lengthOpponent = 0;
				}
				else
				{
					lengthOpponent++;
					
					if(lengthPlayer >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthPlayer/winLength;
					
						//Start checking at square before this one
						//i stays the same, same row
						//k is the end of the section
						k = j-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthPlayer >= winLength + 1)
						{
							utilityPlayer = utilityPlayer + guaranteedWinCol(i,k,lengthPlayer);
						}
						
						countPlayer = 0;
						while(lengthPlayer > 0)
						{
							//Count number of total squares
							if(board[k][i] == player)
							{
								countPlayer++;
							}
							
							k--;
							lengthPlayer--;
						}//end while
						
						utilityPlayer = utilityPlayer + possibleWin*countPlayer;
						
					}//end if
					
					lengthPlayer = 0;
				}//end else
			}//end for j col
			
			if(lengthPlayer > lengthOpponent)
			{
				if(lengthPlayer >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthPlayer/winLength;
					
						//Start checking at square before this one
						//i stays the same, same row
						//k is the end of the section
						k = j-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthPlayer >= winLength + 1)
						{
							utilityPlayer = utilityPlayer + guaranteedWinCol(i,k,lengthPlayer);
						}
						
						countPlayer = 0;
						while(lengthPlayer > 0)
						{
							//Count number of total squares
							if(board[k][i] == player)
							{
								countPlayer++;
							}
							
							k--;
							lengthPlayer--;
						}//end while
						
						utilityPlayer = utilityPlayer + possibleWin*countPlayer;
						
					}//end if
					
					lengthPlayer = 0;
			}
			else
			{
				if(lengthOpponent >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthOpponent/winLength;
					
						//Start checking at square before this one
						//i stays the same, same column
						//k is the end of the section
						k = j-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthOpponent >= winLength + 1)
						{
							utilityOpponent = utilityOpponent + guaranteedWinCol(i,k,lengthOpponent);
						}
						
						countOpponent = 0;
						while(lengthOpponent > 0)
						{
							//Count number of total squares
							if(board[k][i] == opponent)
							{
								countOpponent++;
							}
							
							k--;
							lengthOpponent--;
						}//end while
						
						utilityOpponent = utilityOpponent + possibleWin*countOpponent;
						
					}//end if
					
					//Reset opponent's section
					lengthOpponent = 0;

			}//end else
			
		}//end for i col
		
		
		//System.out.println("Check Positive Diagonals");
		//Do Positive slope diagonals************************************************************
		for(i = 0; i < size*2; i++)
		{
			lengthPlayer = 0;
			lengthOpponent = 0;
			//Only check if diagonal is big enough
			if(i >= winLength-1 && i < (size*2 - winLength))
			{
			
			if(i > size-1)
			{
				startRow = size-1;
				startCol = i%(size-1);
			}
			else
			{
				startRow = i;
				startCol = 0;
			}
			
			//System.out.println("Current Positive Diagonal Start:" + startRow + " " + startCol);
			while(startRow > 0 && startCol < size-1)
			{
				if(board[startRow][startCol] == EMPTY)
				{
					lengthPlayer++;
					lengthOpponent++;
				}
				else if(board[startRow][startCol] == player)
				{
					lengthPlayer++;
					
					//Check opponent's section
					//Only give points if big enough to win in
					if(lengthOpponent >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthOpponent/winLength;
					
						//Start checking at square before this one on the diagonal
						k = startRow+1;
						m = startCol-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthOpponent >= winLength + 1)
						{
							utilityOpponent = utilityOpponent + guaranteedWinDiagPos(k,m,lengthOpponent);
						}
						
						countOpponent = 0;
						while(lengthOpponent > 0)
						{
							//Count number of total squares
							if(board[k][m] == opponent)
							{
								countOpponent++;
							}
							
							k++;
							m--;
							lengthOpponent--;
						}//end while
						
						utilityOpponent = utilityOpponent + possibleWin*countOpponent;
						
					}//end if
					
					//Reset opponent's section
					lengthOpponent = 0;
				}
				else
				{
					lengthOpponent++;
					
					if(lengthPlayer >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthPlayer/winLength;
					
						//Start checking at square before this one
						//i stays the same, same row
						//k is the end of the section
						k = startRow+1;
						m = startCol-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthPlayer >= winLength + 1)
						{
							utilityPlayer = utilityPlayer + guaranteedWinDiagPos(k,m,lengthPlayer);
						}
						
						countPlayer = 0;
						while(lengthPlayer > 0)
						{
							//Count number of total squares
							if(board[k][m] == player)
							{
								countPlayer++;
							}
							
							k++;
							m--;
							lengthPlayer--;
						}//end while
						
						utilityPlayer = utilityPlayer + possibleWin*countPlayer;
						
					}//end if
					
					lengthPlayer = 0;
				}//end else
			
			
				startRow--;
				startCol++;
			}
			
			if(lengthPlayer > lengthOpponent)
			{
				if(lengthPlayer >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthPlayer/winLength;
					
						//Start checking at square before this one
						//i stays the same, same row
						//k is the end of the section
						k = startRow+1;
						m = startCol-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthPlayer >= winLength + 1)
						{
							utilityPlayer = utilityPlayer + guaranteedWinDiagPos(k,m,lengthPlayer);
						}
						
						countPlayer = 0;
						while(lengthPlayer > 0)
						{
							//Count number of total squares
							if(board[k][m] == player)
							{
								countPlayer++;
							}
							
							k++;
							m--;
							lengthPlayer--;
						}//end while
						
						utilityPlayer = utilityPlayer + possibleWin*countPlayer;
						
					}//end if
					
					lengthPlayer = 0;
			}
			else
			{
				if(lengthOpponent >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthOpponent/winLength;
					
						//Start checking at square before this one on the diagonal
						k = startRow+1;
						m = startCol-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthOpponent >= winLength + 1)
						{
							utilityOpponent = utilityOpponent + guaranteedWinDiagPos(k,m,lengthOpponent);
						}
						
						countOpponent = 0;
						while(lengthOpponent > 0)
						{
							//Count number of total squares
							if(board[k][m] == opponent)
							{
								countOpponent++;
							}
							
							k++;
							m--;
							lengthOpponent--;
						}//end while
						
						utilityOpponent = utilityOpponent + possibleWin*countOpponent;
						
					}//end if
					
					//Reset opponent's section
					lengthOpponent = 0;
			}
			
			
			}//end if
		}//end for
		
		
		//System.out.println("Check Negative Diagonals");
		//Do Negative Slope Diagonals *****************************************************
		for(i = 0; i < size*2; i++)
		{
			lengthPlayer = 0;
			lengthOpponent = 0;
			int num;
			//Only check if diagonal is big enough
			if(i >= winLength-1 && i < (size*2 - winLength))
			{
			
			if(i > size-1)
			{
				startRow = 0;
				startCol = i%(size-1);
			}
			else
			{
				startCol = 0;
				startRow = (size-1) - i;
			}

			
			while(startRow < size-1 && startCol < size-1)
			{
				if(board[startRow][startCol] == EMPTY)
				{
					lengthPlayer++;
					lengthOpponent++;
				}
				else if(board[startRow][startCol] == player)
				{
					lengthPlayer++;
					
					//Check opponent's section
					//Only give points if big enough to win in
					if(lengthOpponent >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthOpponent/winLength;
					
						//Start checking at square before this one on the diagonal
						k = startRow-1;
						m = startCol-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthOpponent >= winLength + 1)
						{
							utilityOpponent = utilityOpponent + guaranteedWinDiagNeg(k,m,lengthOpponent);
						}
						
						countOpponent = 0;
						while(lengthOpponent > 0)
						{
							//Count number of total squares
							if(board[k][m] == opponent)
							{
								countOpponent++;
							}
							
							k--;
							m--;
							lengthOpponent--;
						}//end while
						
						utilityOpponent = utilityOpponent + possibleWin*countOpponent;
						
					}//end if
					
					//Reset opponent's section
					lengthOpponent = 0;
				}
				else
				{
					lengthOpponent++;
					
					if(lengthPlayer >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthPlayer/winLength;
					
						//Start checking at square before this one
						//i stays the same, same row
						//k is the end of the section
						k = startRow-1;
						m = startCol-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthPlayer >= winLength + 1)
						{
							utilityPlayer = utilityPlayer + guaranteedWinDiagNeg(k,m,lengthPlayer);
						}
						
						countPlayer = 0;
						while(lengthPlayer > 0)
						{
							//Count number of total squares
							if(board[k][m] == player)
							{
								countPlayer++;
							}
							
							k--;
							m--;
							lengthPlayer--;
						}//end while
						
						utilityPlayer = utilityPlayer + possibleWin*countPlayer;
						
					}//end if
					
					lengthPlayer = 0;
				}//end else
			
			
				startRow++;
				startCol++;
			}//end while
			
			if(lengthPlayer > lengthOpponent)
			{
				if(lengthPlayer >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthPlayer/winLength;
					
						//Start checking at square before this one
						//i stays the same, same row
						//k is the end of the section
						k = startRow-1;
						m = startCol-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthPlayer >= winLength + 1)
						{
							utilityPlayer = utilityPlayer + guaranteedWinDiagNeg(k,m,lengthPlayer);
						}
						
						countPlayer = 0;
						while(lengthPlayer > 0)
						{
							//Count number of total squares
							if(board[k][m] == player)
							{
								countPlayer++;
							}
							
							k--;
							m--;
							lengthPlayer--;
						}//end while
						
						utilityPlayer = utilityPlayer + possibleWin*countPlayer;
						
					}//end if
					
					lengthPlayer = 0;
			}
			else
			{
				if(lengthOpponent >= winLength)
					{
						//Determine how many times you can win in this section, weighting factor
						//Integer divide, want to truncate
						possibleWin = lengthOpponent/winLength;
					
						//Start checking at square before this one on the diagonal
						k = startRow-1;
						m = startCol-1;
						//Check for guaranteed win either this turn or next
						//Can only have one if section is length winLength+1 at least
						//Don't bother otherwise
						if(lengthOpponent >= winLength + 1)
						{
							utilityOpponent = utilityOpponent + guaranteedWinDiagNeg(k,m,lengthOpponent);
						}
						
						countOpponent = 0;
						while(lengthOpponent > 0)
						{
							//Count number of total squares
							if(board[k][m] == opponent)
							{
								countOpponent++;
							}
							
							k--;
							m--;
							lengthOpponent--;
						}//end while
						
						utilityOpponent = utilityOpponent + possibleWin*countOpponent;
						
					}//end if
					
					//Reset opponent's section
					lengthOpponent = 0;
			}//end else
			
			}//end if
		}//end for

		//System.out.println("Return from evaluation function");
		return utilityPlayer - utilityOpponent;
	 }//end evaluation function
	 
	 
	 
	 
	 //Pass method the row number
	 //endCol is the column number of the last element of the section
	 //length is the length of the section
	 //This method checks to see if this section has a guaranteed win in it
	 //or if this section can be set up to have a guaranteed win in one move
	 //Returns a utility value based on this
	 //Don't need to know what player it is because section only contains
	 //tiles of the current player, if it's not empty it's the right player
	 public int guaranteedWinRow(int row, int endCol, int length)
	 {
	 	//System.out.println("Check guaranteed win row");
		int counter = 0;
		int index = length;
		int col = endCol;
		//Check for guaranteed win this turn
		while(index > 0)
		{
	 		if(board[row][col] == EMPTY)
			{
				counter = 0;
			}
			else
			{
				counter++;
				if(counter == winLength-1)
				{
					//Make sure these sqaures are valid on the board
					if(col-1 >= 0 && col-1 < size && col+counter >= 0 && col+counter < size)
					{
						if(board[row][col-1] == EMPTY && board[row][col+counter] == EMPTY)
						{
							//System.out.println("Return from guaranteed win row");
							return 10000;
						}
					}
				}
			}
			
			col--;
	 		index--;
	 	}
		
		counter = 0;
		index = length;
		col = endCol;
		//Check for one move away from guaranteed win, possibility one
		while(index > 0)
		{
	 		if(board[row][col] == EMPTY)
			{
				counter = 0;
			}
			else
			{
				counter++;
				if(counter == winLength-2)
				{
					//Only check if these sqaures are on the board (>=0 && <size)
					if(col-1 >= 0 && col-1 < size && col+counter >= 0 && col+counter < size)
					{
						if(board[row][col-1] == EMPTY && board[row][col+counter] == EMPTY)
						{
							//Check separately and only if squares are valid
							if(col-2 >=0 && col-2 < size && board[row][col-2] == EMPTY)
							{
								//System.out.println("Return from guaranteed win row");
								return 500;
							}
							if(col+counter+1 < size && col+counter+1 >=0 && board[row][col+counter+1] == EMPTY)
							{
								//System.out.println("Return from guaranteed win row");
								return 500;
							}
						}
					}
				}
			}//end else
			
			col--;
	 		index--;
	 	}
		
		counter = 0;
		index = length;
		col = endCol;
		int numEmpty = 0;
		//Check for one move away from guaranteed win, possibility two
		while(index > 0)
		{
	 		if(board[row][col] == EMPTY)
			{
				if(counter > 0 && numEmpty == 0)
				{
					counter++;
					numEmpty = 1;
				}
				else
				{
					counter = 0;
					numEmpty = 0;
				}
			}
			else
			{
				counter++;
				if(counter == winLength-1)
				{
					//Make sure these sqaures are valid on the board
					if(col-1 >= 0 && col-1 < size && col+counter >= 0 && col+counter < size)
					{
						//Check squares on either side of the sequence
						if(board[row][col-1] == EMPTY && board[row][col+counter] == EMPTY)
						{
							//System.out.println("Return from guaranteed win row");
							return 500;
						}
					}
				}
			}//end else
			
			col--;
	 		index--;
	 	}
	 	//if no guaranteed wins, return nothing
		//System.out.println("Return from guaranteed win row");
	 	return 0;
	 }//end guaranteedWinRow
	 
	 
	 //Pass method the row number
	 //endCol is the column number of the last element of the section
	 //length is the length of the section
	 //This method checks to see if this section has a guaranteed win in it
	 //or if this section can be set up to have a guaranteed win in one move
	 //Returns a utility value based on this
	 //Don't need to know what player it is because section only contains
	 //tiles of the current player, if it's not empty it's the right player
	 public int guaranteedWinCol(int col, int endRow, int length)
	 {
	 	//System.out.println("Check guaranteed win col");
		int counter = 0;
		int index = length;
		int row = endRow;
		//Check for guaranteed win this turn
		while(index > 0)
		{
	 		if(board[row][col] == EMPTY)
			{
				counter = 0;
			}
			else
			{
				counter++;
				if(counter == winLength-1)
				{
					//Make sure these sqaures are valid on the board
					if(row-1 >= 0 && row-1 < size && row+counter >= 0 && row+counter < size)
					{
						if(board[row-1][col] == EMPTY && board[row+counter][col] == EMPTY)
						{
							//System.out.println("Return from guaranteed win col");
							return 10000;
						}
					}
				}
			}
			
			row--;
	 		index--;
	 	}
		
		counter = 0;
		index = length;
		row = endRow;
		//Check for one move away from guaranteed win, possibility one
		while(index > 0)
		{
	 		if(board[row][col] == EMPTY)
			{
				counter = 0;
			}
			else
			{
				counter++;
				if(counter == winLength-2)
				{
					//Only check if these sqaures are on the board (>=0 && <size)
					if(row-1 >= 0 && row-1 < size && row+counter >= 0 && row+counter < size)
					{
						if(board[row-1][col] == EMPTY && board[row+counter][col] == EMPTY)
						{
							//Check separately and only if squares are valid
							if(row-2 >=0 && row -2 < size && board[row-2][col] == EMPTY)
							{
								//System.out.println("Return from guaranteed win col");
								return 500;
							}
							if(row+counter+1 < size && row+counter+1 >=0 && board[row+counter+1][col] == EMPTY)
							{
								//System.out.println("Return from guaranteed win col");
								return 500;
							}
						}
					}
				}
			}//end else
			
			row--;
	 		index--;
	 	}
		
		counter = 0;
		index = length;
		row = endRow;
		int numEmpty = 0;
		//Check for one move away from guaranteed win, possibility two
		while(index > 0)
		{
	 		if(board[row][col] == EMPTY)
			{
				if(counter > 0 && numEmpty == 0)
				{
					counter++;
					numEmpty = 1;
				}
				else
				{
					counter = 0;
					numEmpty = 0;
				}
			}
			else
			{
				counter++;
				if(counter == winLength-1)
				{
					//Make sure these sqaures are valid on the board
					if(row-1 >= 0 && row-1 < size && row+counter >= 0 && row+counter < size)
					{
						//Check squares on either side of the sequence
						if(board[row-1][col] == EMPTY && board[row+counter][col] == EMPTY)
						{
							//System.out.println("Return from guaranteed win col");
							return 500;
						}
					}
				}
			}//end else
			
			row--;
	 		index--;
	 	}
	 	//if no guaranteed wins, return nothing
		//System.out.println("Return from guaranteed win col");
	 	return 0;
	 }//end guaranteedWinCol
	 



	 //Pass method the row number
	 //endCol is the column number of the last element of the section
	 //length is the length of the section
	 //This method checks to see if this section has a guaranteed win in it
	 //or if this section can be set up to have a guaranteed win in one move
	 //Returns a utility value based on this
	 //Don't need to know what player it is because section only contains
	 //tiles of the current player, if it's not empty it's the right player
	 public int guaranteedWinDiagPos(int endRow, int endCol, int length)
	 {
	 	//System.out.println("Check guaranteed win pos diag");
		int counter = 0;
		int index = length;
		int row = endRow;
		int col = endCol;
		//Check for guaranteed win this turn
		while(index > 0)
		{
			//ARRAY OUT OF BOUNDS EXCEPTION: 7 when limit is 0-6
	 		if(board[row][col] == EMPTY)
			{
				counter = 0;
			}
			else
			{
				counter++;
				if(counter == winLength-1)
				{
					//Make sure these sqaures are valid on the board
					if(row+1 >= 0 && row+1 < size && row-counter >= 0 && row-counter < size && col-1 >= 0 && col-1 < size && col+counter >= 0 && col+counter < size)
					{
						if(board[row+1][col-1] == EMPTY && board[row-counter][col+counter] == EMPTY)
						{
							//System.out.println("Return from guaranteed win pos diag, 1000");
							return 10000;
						}
					}
				}
			}
			
			row++;
			col--;
	 		index--;
	 	}
		
		counter = 0;
		index = length;
		row = endRow;
		col = endCol;
		//Check for one move away from guaranteed win, possibility one
		while(index > 0)
		{
	 		if(board[row][col] == EMPTY)
			{
				counter = 0;
			}
			else
			{
				counter++;
				if(counter == winLength-2)
				{
					//Only check if these sqaures are on the board (>=0 && <size)
					if(row+1 >= 0 && row+1 < size && row-counter >= 0 && row-counter < size && col-1 >= 0 && col-1 < size && col+counter >= 0 && col+counter < size)
					{
						if(board[row+1][col-1] == EMPTY && board[row-counter][col+counter] == EMPTY)
						{
							//Check separately and only if squares are valid
							if(row+2 >=0 && row+2 < size && col-2 >= 0 && col-2 < size && board[row+2][col-2] == EMPTY)
							{
								//System.out.println("Return from guaranteed win pos diag, 500");
								return 500;
							}
							if(row-counter-1 < size && row-counter-1 >=0 && col+counter+1 >= 0 && col+counter+1 < size && board[row-counter-1][col+counter+1] == EMPTY)
							{
								//System.out.println("Return from guaranteed win pos diag, 500");
								return 500;
							}
						}
					}
				}
			}//end else
			
			row++;
			col--;
	 		index--;
	 	}
		
		counter = 0;
		index = length;
		row = endRow;
		col = endCol;
		int numEmpty = 0;
		//Check for one move away from guaranteed win, possibility two
		while(index > 0)
		{
	 		if(board[row][col] == EMPTY)
			{
				if(counter > 0 && numEmpty == 0)
				{
					counter++;
					numEmpty = 1;
				}
				else
				{
					counter = 0;
					numEmpty = 0;
				}
			}
			else
			{
				counter++;
				if(counter == winLength-1)
				{
					//Make sure these sqaures are valid on the board
					if(row+1 >= 0 && row+1 < size && row-counter >= 0 && row-counter < size && col-1 >= 0 && col-1 < size && col+counter >= 0 && col+counter < size)
					{
						if(board[row+1][col-1] == EMPTY && board[row-counter][col+counter] == EMPTY)
						{
							//System.out.println("Return from guaranteed win pos diag, 500");
							return 500;
						}
					}
				}
			}//end else
			
			row++;
			col--;
	 		index--;
	 	}
		
	 	//if no guaranteed wins, return nothing
		//System.out.println("Return from guaranteed win pos diag, 0");
	 	return 0;
	 }//end guaranteedWinDiagPos
	 
	 
	 


	 //Pass method the row number
	 //endCol is the column number of the last element of the section
	 //length is the length of the section
	 //This method checks to see if this section has a guaranteed win in it
	 //or if this section can be set up to have a guaranteed win in one move
	 //Returns a utility value based on this
	 //Don't need to know what player it is because section only contains
	 //tiles of the current player, if it's not empty it's the right player
	 public int guaranteedWinDiagNeg(int endRow, int endCol, int length)
	 {
	 	//System.out.println("Check guaranteed win neg diag");
		int counter = 0;
		int index = length;
		int row = endRow;
		int col = endCol;
		//Check for guaranteed win this turn
		while(index > 0)
		{
	 		if(board[row][col] == EMPTY)
			{
				counter = 0;
			}
			else
			{
				counter++;
				if(counter == winLength-1)
				{
					//Make sure these sqaures are valid on the board
					if(row-1 >= 0 && row-1 < size && row+counter >= 0 && row+counter < size && col-1 >= 0 && col-1 < size && col+counter >= 0 && col+counter < size)
					{
						if(board[row-1][col-1] == EMPTY && board[row+counter][col+counter] == EMPTY)
						{
							//System.out.println("Return from guaranteed win neg diag, 1000");
							return 10000;
						}
					}
				}
			}
			
			row--;
			col--;
	 		index--;
	 	}
		
		counter = 0;
		index = length;
		row = endRow;
		col = endCol;
		//Check for one move away from guaranteed win, possibility one
		while(index > 0)
		{
	 		if(board[row][col] == EMPTY)
			{
				counter = 0;
			}
			else
			{
				counter++;
				if(counter == winLength-2)
				{
					//Only check if these sqaures are on the board (>=0 && <size)
					if(row-1 >= 0 && row-1 < size && row+counter >= 0 && row+counter < size && col-1 >= 0 && col-1 < size && col+counter >= 0 && col+counter < size)
					{
						if(board[row-1][col-1] == EMPTY && board[row+counter][col+counter] == EMPTY)
						{
							//Check separately and only if squares are valid
							if(row-2 >=0 && row-2 < size && col-2 >= 0 && col-2 < size && board[row-2][col-2] == EMPTY)
							{
								//System.out.println("Return from guaranteed win neg diag, 500");
								return 500;
							}
							if(row+counter+1 < size && row+counter+1 >=0 && col+counter+1 >= 0 && col+counter+1 < size && board[row+counter+1][col+counter+1] == EMPTY)
							{
								//System.out.println("Return from guaranteed win neg diag, 500");
								return 500;
							}
						}
					}
				}
			}//end else
			
			row--;
			col--;
	 		index--;
	 	}
		
		counter = 0;
		index = length;
		row = endRow;
		col = endCol;
		int numEmpty = 0;
		//Check for one move away from guaranteed win, possibility two
		while(index > 0)
		{
	 		if(board[row][col] == EMPTY)
			{
				if(counter > 0 && numEmpty == 0)
				{
					counter++;
					numEmpty = 1;
				}
				else
				{
					counter = 0;
					numEmpty = 0;
				}
			}
			else
			{
				counter++;
				if(counter == winLength-1)
				{
					//Make sure these sqaures are valid on the board
					if(row-1 >= 0 && row-1 < size && row+counter >= 0 && row+counter < size && col-1 >= 0 && col-1 < size && col+counter >= 0 && col+counter < size)
					{
						if(board[row-1][col-1] == EMPTY && board[row+counter][col+counter] == EMPTY)
						{
							//System.out.println("Return from guaranteed win neg diag, 500");
							return 500;
						}
					}
				}
			}//end else
			
			row--;
			col--;
	 		index--;
	 	}
		
	 	//if no guaranteed wins, return nothing
		//System.out.println("Return from guaranteed win neg diag, 0");
	 	return 0;
	 }//end guaranteedWinDiagNeg




	 
	 
	//AI makes move
    public int moveOpponent(char c)
	 {	  
		  //Object to keep track of the best move and its value
		  //in the alpha-beta function
		  MoveInfo finalMove = new MoveInfo();
		  
		  //Call alpha-beta function
	     int val = alphaBeta('O',4,this, finalMove);
		  
		  //Get best move
		  moveX = finalMove.getMoveX();
		  moveY = finalMove.getMoveY();
		  
		  //System.out.println("Value of Final move: " + finalMove.getScore() + " (" + moveX + "," + moveY + ")");
		  
		  //Set the move on the board
        setTile(moveX,moveY,c);
		  fillSquare();
		 
		  
		  //System.out.println("*******FINAL MOVE*******");
		  //printBoard();
		 
		  //Check for a winner based on this move
		  checkWinner(moveX,moveY,c);
        return getWinner();
    }
	 
	 
	
//Alpha-beta pruning function 
public static int alphaBeta(char c, int depth, GameBoard board, MoveInfo lastMove)
{
	int value = maxValue(c, depth, board, lastMove, NEGATIVE_INFINITY, POSITIVE_INFINITY);
	return value;
}

//Minimax function for max nodes
public static int maxValue(char c, int depth, GameBoard board, MoveInfo finalMove, int alpha, int beta)
{
		//Determine who our opponent is
		char next;
		if (c == PLAYERO)
			next = PLAYERX;
		else
			next = PLAYERO;	

		//IF GAME IS OVER in current board position, return board value
	 	if(board.getWinner() == OWIN)
		{
			return POSITIVE_INFINITY;
		}
		else if(board.getWinner() == XWIN)
		{
			return NEGATIVE_INFINITY;
		}
		else if(board.getWinner() == TIE)
		{
			return 0;
		}
		else if(depth <=0)
		{
			return board.evaluation(c);
		}
		
		int v = NEGATIVE_INFINITY;
		
			//For all children (all legal moves for the player from the current board)
			for(int i = 0; i < board.getSize(); i++)
			{
				for(int j = 0; j < board.getSize(); j++)
				{
					//Make sure this is a legal move, square is not occupied
					if(board.getTile(i,j) == EMPTY)
					{
						//Create and set child node
						GameBoard child = new GameBoard(board, board.getSize(), board.getWinLength(), board.getEmptySquares());
						child.setTile(i,j,c);
						child.fillSquare();
						child.checkWinner(i,j,c);
						
						//For each board, print the board, overall score, alpha and beta
						//System.out.println("\n----");
						//System.out.println("Player O's Move");
						//child.printBoard();
						
						//Value of this node
						int score = minValue(next,depth-1,child,finalMove,alpha,beta);
						
						//System.out.println("Score:" + score + " Alpha:" + alpha + " Beta:" + beta);
						//System.out.println("----\n");
						
						if(score > v)
						{
							v = score;
						}
						
						//Found a better move?
						if(v > alpha)
						{
							alpha = v;
							finalMove.setMoveX(i);
							finalMove.setMoveY(j);
							finalMove.setScore(v);
							//System.out.println("\n--");
							//System.out.println("Found Better Move");
							//child.printBoard();
							//System.out.println("MAXVALUEScore: " + score + " Alpha:" + alpha + " Beta:" + beta);
							//System.out.println("New Best move:" + finalMove.getMoveX() + " " + finalMove.getMoveY());
							//System.out.println("----\n");
						}
						
						
						//Found a better move??
            		if(v >= beta)
						{
							return v;
						}
						
					}//end large if
				}//end for j
			}//end for i
        return v;
	
}//end maxvalue


//Minimax function for min nodes
public static int minValue(char c, int depth, GameBoard board,MoveInfo finalMove, int alpha, int beta)
{
		//Determine who our opponent is
		char next;
		if (c == PLAYERO)
				next = PLAYERX;
		else
				next = PLAYERO;
		
		//IF GAME IS OVER in current board position, return board value
	 	if(board.getWinner() == XWIN)
		{
			return NEGATIVE_INFINITY;
		}
		else if(board.getWinner() == OWIN)
		{
			return POSITIVE_INFINITY;
		}
		else if(board.getWinner() == TIE)
		{
			return 0;
		}
		else if(depth <=0)
		{
			return board.evaluation(next);
		}
		
		int v = POSITIVE_INFINITY;
		
			//For all children (all legal moves for the player from the current board)
			for(int i = 0; i < board.getSize(); i++)
			{
				for(int j = 0; j < board.getSize(); j++)
				{
					//Make sure this is a legal move, square is not occupied
					if(board.getTile(i,j) == EMPTY)
					{
						//Create and set child node
						GameBoard child = new GameBoard(board, board.getSize(), board.getWinLength(), board.getEmptySquares());
						child.setTile(i,j,c);
						child.fillSquare();
						child.checkWinner(i,j,c);
						
						//For each board, print the board, overall score, alpha and beta
						//System.out.println("\n----");
						//System.out.println("Player X's Move");
						//child.printBoard();
						
						//Value of this node
						int score = maxValue(next,depth-1,child,finalMove,alpha,beta);
						
						//System.out.println("Score:" + score + " Alpha:" + alpha + " Beta:" + beta);
						//System.out.println("----\n");
						
						if(score < v)
						{
							v = score;
						}
						
						if(v < beta)
						{
							beta = v;
						}
						
            		if(v <= alpha)
						{
							//Best move for X
							//child.printBoard();
							//System.out.println("MINVALUEScore: " + score + " Alpha:" + alpha);
							//System.out.println("New Best move:" + i + " " + j);
							return v;
						}
					}//end large if
				}//end for j
			}//end for i
			
        return v;
	
}//end minvalue

}//end GameBoard
