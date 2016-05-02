/**
 * Tic-Tac-Toe game implementation
 *
 * @author Jonathan Walsh (jwalsh8484@gmail.com)
 */

package egs.games;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class TicTacToeGame extends Game {

	/*Winning coordinates*/
	private final static int[][][] WIN_COORDS = {
		{{0,0},{0,1},{0,2}},
		{{1,0},{1,1},{1,2}},
		{{2,0},{2,1},{2,2}},
		{{0,0},{1,0},{2,0}},
		{{0,1},{1,1},{2,1}},
		{{0,2},{1,2},{2,2}},
		{{0,0},{1,1},{2,2}},
		{{0,2},{1,1},{2,0}}
	};
	
    /*Valid board size constants*/
    public final short GAME_WIDTH = 3;
    public final short GAME_HEIGHT = 3;

    /*Player piece identification*/
    public final static int EMPTY = 0;
    public final static int X_PIECE = 1;
    public final static int O_PIECE = 2;
    
    /*End Game state codes*/
    public final static int CONTINUE_GAME = 0;
    public final static int X_WIN = 1;
    public final static int O_WIN = 2;
    public final static int TIE = 3;
    
    private int player = -1;
    private int oppPlayer = -1;


    /**
     * Creates an empty uninitialized game board
     */
    public TicTacToeGame() {
        currentBoard = new int[GAME_HEIGHT][GAME_WIDTH];
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
                currentBoard[h][w] = EMPTY;
            }
        }

        width = GAME_WIDTH;
        height = GAME_HEIGHT;

        game_version = 1;
    }
    
    /** 
     * Creates a board with some initial state
     */
    public TicTacToeGame( int[][] board ) throws TicTacToeGameException {

        super( board );

        /*check height*/
        if ( height != GAME_HEIGHT || width != GAME_WIDTH ) {
            throw new TicTacToeGameException( "Invalid sized board receieved" );
        }

        game_version = 1;
    }

    
    public TicTacToeGame( int[][] board, int playerNumber ) throws TicTacToeGameException {
    	
        super( board );

        /*check height*/
        if ( height != GAME_HEIGHT || width != GAME_WIDTH ) {
            throw new TicTacToeGameException( "Invalid sized board receieved" );
        }

        game_version = 1;
        
        player = playerNumber;
        if (player == 1)
        	oppPlayer = 2;
        else if (player == 2)
        	oppPlayer = 1;
    }


    public int verifyMove( int[][] board, int player ) {
    	boolean foundMove = false;
    	int currentPiece, newPiece;
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
            	currentPiece = currentBoard[h][w];
                newPiece = board[h][w];
                switch(currentPiece) {
                case EMPTY:
                	if (newPiece != currentPiece)
                		if (foundMove)
                			return INVALID_MOVE; // more than one move
                		else if (newPiece != player)
                			return INVALID_MOVE; // move made by out-of-turn player
                		else
                			foundMove = true; // we found the move and it is valid
                	break;
                case X_PIECE:
                	if (newPiece != currentPiece)
                		return INVALID_MOVE; // invalid board change
                	break;
                case O_PIECE:
                	if (newPiece != currentPiece)
                		return INVALID_MOVE; // invalid board change
                	break;
                default:
                	return INVALID_MOVE; // unknown piece!
                }
            }
        }
        
        if (foundMove)
        	return VALID;
        else
        	return INVALID_MOVE;
    }

    public int updateState( int[][] board ) {

        if ( height != (short)board.length || width != (short)board[0].length ) {
            return INVALID_BOARD;
        }

        //currentBoard = board;
        for ( int h = 0; h < height; h++ )
            for ( int w = 0; w < width; w++ )
            	currentBoard[h][w] = board[h][w];
        
        return VALID;
    }

    public int makeMove( int[][] board, int player ) {
        if ( verifyMove( board, player ) == VALID) return updateState( board );        
        return INVALID_MOVE;
    }
    
    private int getWinner(int[][] coords) {
    	int lastPiece = EMPTY;
    	int piece;
    	for (int[] coord : coords) {
    		int h = coord[0];
    		int w = coord[1];
    		piece = currentBoard[h][w];
    		if (piece == EMPTY)
    			return TIE; // empty piece; neither player wins
    		else if (lastPiece == EMPTY)
    			lastPiece = piece;
    		else if (lastPiece != piece)
    			return TIE; // two different piece types in row; neither player wins
    	}
    	if (lastPiece == X_PIECE)
    		return X_WIN;
    	else if (lastPiece == O_PIECE)
    		return O_WIN;
    	else
    		return TIE;
    }

    public int getGameOver() {
    	boolean boardFull = true;
    	boolean xWin = false;
    	boolean oWin = false;
    	
    	// check for X and O wins
    	for (int[][] coords : WIN_COORDS) {
    		int win = getWinner(coords);
    		switch(win) {
    		case TIE:
    			break;
    		case X_WIN:
    			xWin = true;
    			break;
    		case O_WIN:
    			oWin = true;
    			break;
    		default:
    			break;
    		}
    	}
    	
    	// check for full board
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
            	if (currentBoard[h][w] == EMPTY) {
            		boardFull = false;
            		break;
            	}
            }
        }
        
    	// return result
    	if (xWin && oWin)
    		return TIE;
    	else if (xWin)
            return X_WIN;
    	else if (oWin)
            return O_WIN;
    	else if (boardFull)
    		return TIE;
    	else
    		return CONTINUE_GAME;
    }

    public String toString() {
        return super.toString();
    }

    public static void main( String argv[] ) {
    	/*Simple test to make sure is working*/
    	Game boardGetter = new TicTacToeGame();
    	int board[][] = boardGetter.getRawBoard();
    	Game game = new TicTacToeGame();
    	
    	board[0][0] = TicTacToeGame.X_PIECE;
        if (game.makeMove(board, 1) != VALID)
        	System.out.println("Error making move ");
        else
        	System.out.println("Game board:\n" + game);
        System.out.println("Game over: " + game.getGameOver());
        
    	board[0][1] = TicTacToeGame.O_PIECE;
        if (game.makeMove(board, 2) != VALID)
        	System.out.println("Error making move ");
        else
        	System.out.println("Game board:\n" + game);
        System.out.println("Game over: " + game.getGameOver());
        
    	board[1][0] = TicTacToeGame.X_PIECE;
        if (game.makeMove(board, 1) != VALID)
        	System.out.println("Error making move ");
        else
        	System.out.println("Game board:\n" + game);
        System.out.println("Game over: " + game.getGameOver());
        
    	board[2][2] = TicTacToeGame.O_PIECE;
        if (game.makeMove(board, 2) != VALID)
        	System.out.println("Error making move ");
        else
        	System.out.println("Game board:\n" + game);
        System.out.println("Game over: " + game.getGameOver());
        
    	board[1][2] = TicTacToeGame.X_PIECE;
        if (game.makeMove(board, 1) != VALID)
        	System.out.println("Error making move ");
        else
        	System.out.println("Game board:\n" + game);
        System.out.println("Game over: " + game.getGameOver());
        
    	board[2][0] = TicTacToeGame.O_PIECE;
        if (game.makeMove(board, 2) != VALID)
        	System.out.println("Error making move ");
        else
        	System.out.println("Game board:\n" + game);
        System.out.println("Game over: " + game.getGameOver());
        
    	board[0][2] = TicTacToeGame.X_PIECE;
        if (game.makeMove(board, 1) != VALID)
        	System.out.println("Error making move ");
        else
        	System.out.println("Game board:\n" + game);
        System.out.println("Game over: " + game.getGameOver());
        
    	board[1][1] = TicTacToeGame.O_PIECE;
        if (game.makeMove(board, 2) != VALID)
        	System.out.println("Error making move ");
        else
        	System.out.println("Game board:\n" + game);
        System.out.println("Game over: " + game.getGameOver());
        
    	board[2][1] = TicTacToeGame.O_PIECE;
        if (game.makeMove(board, 1) != VALID)
        	System.out.println("Error making move ");
        else
        	System.out.println("Game board:\n" + game);
        System.out.println("Game over: " + game.getGameOver());
        
    	board[0][0] = TicTacToeGame.O_PIECE;
        if (game.makeMove(board, 2) != VALID)
        	System.out.println("Error making move ");
        else
        	System.out.println("Game board:\n" + game);
        System.out.println("Game over: " + game.getGameOver());
        
    }

	public int acceptMove(int[][] board, int playerNum) {
		int rc = verifyMove(board, oppPlayer);
		if (rc != 0)
			return rc;
		return updateState(board); 
	}

	public String getMove() throws IOException, InterruptedException {
		System.out.println("Player " + player  + ", please enter move (row col):");
		String input = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		input = reader.readLine();
		return input;
	}

	private boolean isMoveInBounds(short row, short col) 
	{
		if (row < 0 || col < 0 || row >= GAME_HEIGHT || col >= GAME_WIDTH) {
			System.err.println("Move is out of bounds.");
			return false;
		}
		return true;
	}
	
	public boolean executeMove(String move) {
		String[] moveFields = move.split(" ");
		if (moveFields.length != 2)
		{
			System.err.println("Incorrect move format, must be 2 integers");
			return false;
		}
		
		short row = Short.parseShort(moveFields[0]);
		short col = Short.parseShort(moveFields[1]);
		boolean inBounds = isMoveInBounds(row, col);
		
		if (inBounds) {
			int[][] board = new int[height][width];
	        for ( int h = 0; h < height; h++ )
	            for ( int w = 0; w < width; w++ )
	            	board[h][w] = currentBoard[h][w];
	        
	        if (player == TicTacToeGame.X_PIECE) {
	        	board[row][col] = TicTacToeGame.X_PIECE;
	        } else if (player == TicTacToeGame.O_PIECE) {
	        	board[row][col] = TicTacToeGame.O_PIECE;
	        }
			
			updateState(board);
		}
		
		return inBounds;
	}

	public void updateBoardDisplay() {
		System.out.println("   0 1 2");
		System.out.println("  -------");
        for ( int h = 0; h < height; h++ ) {
            String line = h + " |";
            for ( int w = 0; w < width; w++ ) {
            	switch(currentBoard[h][w]) {
            	case X_PIECE:
            		line = line + "x" + "|";
            		break;
            	case O_PIECE:
            		line = line + "o" + "|";
            		break;
            	case EMPTY:
            		line = line + " " + "|";
            		break;
            	default:
            		line = line + " " + "|";
            		break;
            	}
            }
            System.out.println(line);
            System.out.println("  -------");
        }
	}

}
