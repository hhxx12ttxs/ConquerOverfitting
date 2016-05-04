package egs.games;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Stack;

public class CheckersGame extends Game
{
	public final static int EMPTY = 0;
	public final static int P1_PIECE = 1;
	public final static int P1_KING = 2;
	public final static int P2_PIECE = 3;
	public final static int P2_KING = 4;
	private final static String[] pieceNames = {"  ", "p1", "K1", "p2", "K2"};
	
	public final static int CONTINUE_GAME = 0;
	public final static int P1_WIN = 1;
	public final static int P2_WIN = 2;
	public final static int TIE = 3;
	
	private static final short UP_DIR = -1;
	private static final short DOWN_DIR = 1;
	
	public static final int PLAYER_NUM_OUT_OF_RANGE = 128;
	public static final int SQUARE_NOT_EMPTY = 129;
	public static final int MULT_PIECES_MOVED = 130;
	public static final int WRONG_PLAYER_MOVED = 131;
	public static final int UNKNOWN_MOVE = 132;
	public static final int REQ_JUMPS = 133;
	public static final int INVALID_NON_JUMP = 134;
	public static final int MUST_KING_ME = 135;
	public static final int INVALID_JUMP = 136;
	
	private short p1HomeRow;
	private short p2HomeRow;
		
	public CheckersGame(int[][] board)
	{
		super(board);
		game_version = 1;
		setHomeRows();
	}
	
	@Override
	public int makeMove(int[][] board, int player) 
	{
		int ret = verifyMove(board, player);
		
		if(ret != 0)
			return ret;
		
		return updateState(board); 
	}
	
	@Override
	public int updateState(int[][] boardUpdate) 
	{
		if (boardUpdate.length != height || boardUpdate[0].length != width)
			return INVALID_BOARD;
		
		currentBoard = copyBoard(boardUpdate);
		return VALID;
	}
	
	@Override
	public int verifyMove(int[][] postMoveBoard, int player) 
	{
		if (currentBoard == null || postMoveBoard.length != currentBoard.length || postMoveBoard[0].length != currentBoard[0].length)
			return INVALID_BOARD;
		
		if (player != 1 && player != 2)
			return PLAYER_NUM_OUT_OF_RANGE;
		
		Move moveResult = new Move();
		int result = getMoveResult(postMoveBoard, player, moveResult);
		
		return verifyMoveResult(player, moveResult, postMoveBoard);
	}
	
	@Override
	public int getGameOver() 
	{
		int numP1Pieces = 0;
		int numP2Pieces = 0;
		for (short row = 0; row < height; row++)
		{
			for (short col = 0; col < width; col++)
			{
				int piece = currentBoard[row][col];
				if (piece == P1_PIECE || piece == P1_KING)
					numP1Pieces++;
				
				if (piece == P2_PIECE || piece == P2_KING)
					numP2Pieces++;
			}
		}
		
		if (numP1Pieces == 0)
			return P2_WIN;
		
		if (numP2Pieces == 0)
			return P1_WIN;
		
		return CONTINUE_GAME;
	}

	private void setHomeRows() 
	{
		for (int col = 0; col < width; col++)
		{
			if (currentBoard[0][col] == P1_PIECE)
			{
				p1HomeRow = 0;
				p2HomeRow = (short) (height - 1);
				break;
			}
			else if(currentBoard[height - 1][col] == P1_PIECE)
			{
				p1HomeRow = (short) (height - 1);
				p2HomeRow = 0;
				break;
			}
		}
	}

	

	private int verifyMoveResult(int player, Move move, int[][] postMoveBoard) 
	{
		if (move.getMoveStart() == null || move.getMoveEnd() == null)
		{
			System.err.println("Move start or move end could not be determined.");
			return UNKNOWN_MOVE;
		}
		
		int moverPiece = player == 1 ? P1_PIECE : P2_PIECE;
		int moverKing = player == 1 ? P1_KING : P2_KING;
		int moverKingMeRow = player == 1 ? p2HomeRow : p1HomeRow;
		
		int nonMoverPiece = player == 1 ? P2_PIECE : P1_PIECE;
		int nonMoverKing = player == 1 ? P2_KING : P1_KING;
		
		SquareState moveStart = move.getMoveStart();
		SquareState moveEnd = move.getMoveEnd();
		ArrayList<SquareState> capdPieces = move.getCapturedPieces();
		
		// Check if the move required jumps and if jumps occurred
		ArrayList<Move> startingJumps = getAllJumps(player);
		if (startingJumps.size() != 0 && capdPieces.size() == 0)
		{
			System.err.println("Must take jumps available.");
			return REQ_JUMPS;
		}
		
		// Check if it was a simple move 
		if (capdPieces.size() == 0)
		{
			if (!isValid1SquareMove(move))
				return INVALID_NON_JUMP;
			
			if (moveEnd.row() == moverKingMeRow && moveEnd.piece() != moverKing)
			{
				System.err.println("Piece moved to end row, should be kinged but was not.  Square moved to: " + moveEnd.row() + ":" + moveEnd.col());
				return MUST_KING_ME;
			}
			
			return VALID;
		}
		// Check correctness of jump
		else
		{
			for (Move jump : startingJumps)
			{
				if (CheckJump(jump, move.getMoveEnd(), capdPieces))
					return VALID;
			}
			
			System.err.println("Pieces missing and placement of moved pieces do not correspond to any valid jump sequence");
			return INVALID_JUMP;
		}
	}
	
	/***
	 * Checks if this jump or any jumps that could be made thereafter in the same turn will
	 * capture all the pieces in the list
	 * @param jump
	 * @param capdPieces
	 * @return
	 */
	private boolean CheckJump(Move jump, SquareState moveEnd, ArrayList<SquareState> capdPieces)
	{
		int numMatchingCapdPieces = 0;
		for(SquareState capd : capdPieces)
		{
			if (!jump.getCapturedPieces().contains(capd))
				numMatchingCapdPieces ++;
		}
		
		// If the jump lands where the move ends, and each captured piece is captured by the move (and no more are captured), then this jump is a match for the move
		if (jump.getMoveEnd().equals(moveEnd) && capdPieces.size() == jump.getCapturedPieces().size() && numMatchingCapdPieces == capdPieces.size())
		{
			return true;
		}
		
		ArrayList<Move> nextJumps = getNextJumps(jump.getMoveEnd(), jump.getCapturedPieces());
		
		for (Move nextJump : nextJumps)
		{
			if (CheckJump(nextJump, moveEnd, capdPieces))
				return true;
		}
		
		return false;
	}

	private ArrayList<Move> getAllJumps(int player) 
	{
		ArrayList<Move> jumps = new ArrayList<Move>();
		for (short row = 0; row < height; row++)
		{
			for (short col = 0; col < width; col++)
			{
				int piece = currentBoard[row][col];
				boolean getJumpsForPiece = false;
				if (player == 1 && (piece == P1_PIECE || piece == P1_KING))
					getJumpsForPiece = true;
				
				if (player == 2 && (piece == P2_PIECE || piece == P2_KING))
					getJumpsForPiece = true;
				
				if (getJumpsForPiece)
				{
					// Get the next jumps for every square on the board.
					jumps.addAll(getNextJumps(new SquareState(row, col, currentBoard[row][col]), 
							new ArrayList<SquareState>()));
				}
			}
		}
		
		return jumps;
	}
	
	public ArrayList<Move> getNextJumps(SquareState fromSquare, ArrayList<SquareState> alreadyCapdPieces) 
	{
		ArrayList<Move> nextJumps = new ArrayList<Move>();
		if (isRegPiece(fromSquare.piece()))
		{
			short moveDir = getMoveDir(fromSquare.piece());
			nextJumps.addAll(getJumpsForDir(fromSquare, alreadyCapdPieces, nextJumps, moveDir));
		}
		else if (isKingPiece(fromSquare.piece()))
		{
			nextJumps.addAll(getJumpsForDir(fromSquare, alreadyCapdPieces, nextJumps, UP_DIR));
			nextJumps.addAll(getJumpsForDir(fromSquare, alreadyCapdPieces, nextJumps, DOWN_DIR));
		}
		
		return nextJumps;
	}

	private ArrayList<Move> getJumpsForDir(SquareState fromSquare, ArrayList<SquareState> alreadyCapdPieces,
			ArrayList<Move> nextJumps, short moveDir) 
	{
		ArrayList<Move> jumps = new ArrayList<Move>();
		
		short jumpOverRow = (short) (fromSquare.row() + moveDir);
		short jumpToRow = (short) (fromSquare.row() + (moveDir * 2));
		short jumpOverColRight = (short) (fromSquare.col() + 1);
		short jumpOverColLeft = (short) (fromSquare.col() - 1);
		short jumpToColRight = (short) (fromSquare.col() + 2);
		short jumpToColLeft = (short) (fromSquare.col() - 2);

		// Check the rows
		if (jumpOverRow < 0 || jumpToRow < 0 || jumpOverRow > height - 1 || jumpToRow > height - 1)
			return jumps;
		
		// Check jump to the right
		if (jumpOverColRight < width && jumpToColRight < width)
		{
			// If the piece to be jumped is the opposing player, and the square to jump to is empty
			if (areDiffPlayers(fromSquare.piece(), currentBoard[jumpOverRow][jumpOverColRight]) &&
				currentBoard[jumpToRow][jumpToColRight] == EMPTY)
			{
				jumps.add(createJump(fromSquare, alreadyCapdPieces, jumpOverRow, jumpOverColRight, jumpToRow, jumpToColRight));
			}
		}
		if (jumpOverColLeft >= 0 && jumpToColLeft >= 0)
		{
			// If the piece to be jumped is the opposing player, and the square to jump to is empty
			if (areDiffPlayers(fromSquare.piece(), currentBoard[jumpOverRow][jumpOverColLeft]) &&
				currentBoard[jumpToRow][jumpToColLeft] == EMPTY)
			{
				jumps.add(createJump(fromSquare, alreadyCapdPieces, jumpOverRow, jumpOverColLeft, jumpToRow, jumpToColLeft));
			}
		}
		
		return jumps;
	}

	private Move createJump(SquareState fromSquare, ArrayList<SquareState> alreadyCapdPieces,
			short jumpOverRow, short jumpOverCol, short jumpToRow, short jumpToCol) 
	{
		Move newJump = new Move();
		newJump.setMoveStart(fromSquare);
		
		int endPiece = fromSquare.piece();
		if (needsKingMe(jumpToRow, fromSquare.piece()))
			endPiece = kingMe(fromSquare.piece());
		
		newJump.setMoveEnd(new SquareState(jumpToRow, jumpToCol, endPiece));
		newJump.getCapturedPieces().addAll(alreadyCapdPieces);
		 
		newJump.getCapturedPieces().add(new SquareState(jumpOverRow, jumpOverCol, currentBoard[jumpOverRow][jumpOverCol]));
		
		return newJump;
	}

	private boolean areDiffPlayers(int piece, int otherPiece) 
	{
		if ((piece == P1_PIECE || piece == P1_KING) && (otherPiece == P2_PIECE || otherPiece == P2_KING))
			return true;
		
		if ((piece == P2_PIECE || piece == P2_KING) && (otherPiece == P1_PIECE || otherPiece == P1_KING))
			return true;
		
		return false;
	}

	private short getMoveDir(int piece) 
	{
		if (piece == P1_PIECE || piece == P1_KING)
		{
			if (p1HomeRow == 0)
				return DOWN_DIR;
			else
				return UP_DIR;
		}
		else if (piece == P2_PIECE || piece == P2_KING)
		{
			if (p2HomeRow == 0)
				return DOWN_DIR;
			else
				return UP_DIR;
		}
		
		return 0;
	}

	private boolean isKingPiece(int piece) 
	{
		return piece == P1_KING || piece == P2_KING;
	}

	private boolean isRegPiece(int piece) 
	{
		return piece == P1_PIECE || piece == P2_PIECE;
	}
	
	public boolean needsKingMe(short row, int piece) 
	{
		if (isRegPiece(piece))
		{
			if (piece == P1_PIECE && row == p2HomeRow)
				return true;
			
			if (piece == P2_PIECE && row == p1HomeRow)
				return true;
		}
		
		return false;
	}

	public int kingMe(int piece) 
	{
		if (piece == P1_PIECE || piece == P1_KING)
			return P1_KING;
		
		if (piece == P2_PIECE || piece == P2_KING)
			return P2_KING;
		
		return EMPTY;
	}
	
	private boolean isValid1SquareMove(Move move) 
	{
		boolean ret = false;
		
		int movedPiece = move.getMoveStart().piece();
		if (isRegPiece(movedPiece))
		{
			short moveDir = getMoveDir(movedPiece);
			ret = isValid1SquareMove(move, moveDir);
		}
		else if (isKingPiece(movedPiece))
		{
			boolean upValid = isValid1SquareMove(move, UP_DIR);
			ret = upValid || isValid1SquareMove(move, DOWN_DIR);
		}
		else
		{
			// Shouldn't get here
			System.err.println("Invalid move, no piece in starting square.");
			ret = false;
		}
		
		if (!ret)
			System.err.println("Invalid move, piece can not move to that location.  Starting position: " + move.getMoveStart().row() + ":" + move.getMoveStart().col() + ".  Ending position: " + move.getMoveEnd().row() + ":" + move.getMoveEnd().col());
		
		return ret;
	}

	private boolean isValid1SquareMove(Move move, short moveDir) 
	{
		boolean ret = 
			   move.moveEnd.row() == move.moveStart.row() + moveDir && 
			  (move.moveEnd.col() == move.moveStart.col() + 1 || 
			   move.moveEnd.col() == move.moveStart.col() - 1);
		
		return ret;
	}

	private int getMoveResult(int[][] postMoveBoard, int player, Move moveResult) 
	{		
		int moverPiece = player == 1 ? P1_PIECE : P2_PIECE;
		int moverKing = player == 1 ? P1_KING : P2_KING;
		
		int nonMoverPiece = player == 1  ? P2_PIECE : P1_PIECE;
		int nonMoverKing = player == 1  ? P2_KING : P1_KING;
		
		for (short row = 0; row < height; row++)
		{
			for (short col = 0; col < width; col++)
			{
				// If the state of a square changed
				if (currentBoard[row][col] != postMoveBoard[row][col])
				{
					int preMovePiece = currentBoard[row][col];
					int postMovePiece = postMoveBoard[row][col];
					
					// If changed piece was a mover piece
					if (preMovePiece == moverPiece || preMovePiece == moverKing)
					{
						// The only acceptable result is an empty square
						if (postMovePiece != EMPTY)
						{
							System.err.println("Square should be empty: " + row + ":" + col);
							return SQUARE_NOT_EMPTY;
						}
							
						// If at least one other mover piece changed, the move is invalid
						if (moveResult.getMoveStart() == null)
							moveResult.setMoveStart(new SquareState(row, col, preMovePiece));
						else
						{
							System.err.println("More than one piece moved.");
							return MULT_PIECES_MOVED;
						}
					}
					
					// If the changed piece was empty
					if (preMovePiece == EMPTY)
					{
						// If the square was empty, it now must have a mover piece or king
						if (postMovePiece != moverPiece && postMovePiece != moverKing)
						{
							System.err.println("Wrong player moved to square.  Square: " + row + ":" + col + "; Should have been player: " + player);
							return WRONG_PLAYER_MOVED;
						}
						
						if (moveResult.getMoveEnd() != null)
						{
							System.err.println("More than piece has moved or appeared.");
							return MULT_PIECES_MOVED;
						}
						
						moveResult.setMoveEnd(new SquareState(row, col, postMovePiece));
					}
					
					// If the changed piece was a non-mover piece
					if (preMovePiece == nonMoverPiece || preMovePiece == nonMoverKing)
					{
						// If the square is not now empty, indicating a captured piece, the move was invalid
						if (postMovePiece != EMPTY)
						{
							System.err.println("Non-moving player piece changed. Square: " + row + ":" + col);
							return WRONG_PLAYER_MOVED;
						}
						
						moveResult.getCapturedPieces().add(new SquareState(row, col, preMovePiece));
					}
				}
			}
		}
		
		return VALID;
	}
	
	public int[][] getBoardState() 
	{
		return copyBoard(currentBoard);
	}
	
	public static int[][] copyBoard(int[][] boardToCopy) 
	{
		int[][] ret = new int[boardToCopy.length][boardToCopy[0].length];
		for (short row = 0; row < boardToCopy.length; row++)
		{
			for (short col = 0; col < boardToCopy[0].length; col++)
			{
				ret[row][col] = boardToCopy[row][col];
			}
		}
		
		return ret;
	}
	public static String printBoardState(int[][] state)
	{
		String ret = "   ";
		String boardLine = "";
		for (short col = 0; col < state[0].length; col++)
		{
			ret += " " + col + " ";
			boardLine += "---";
		}
		ret+= "\n";
		boardLine = "   " + boardLine + "-\n";
		
		for (short row = 0; row < state.length; row++)
		{
			ret += boardLine;
			ret += row + "  ";
			for (short col = 0; col < state[row].length; col++)
			{
				ret += "|" + pieceNames[state[row][col]];	
			}
			
			ret += "|\n";
		}
		ret += boardLine;
		
		return ret;
	}
	

	public static int[][] getInitGameStateP2Top()
	{
		return new int[][] {
				{ EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE },
				{ P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY },
				{ EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE },
				{ EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY },
				{ EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY },
				{ P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY},
				{ EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE},
				{ P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY}
		};
	}
	
	public static int[][] getInitGameStateP1Top()
	{
		return new int[][] {
				{ EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE},
				{ P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY},
				{ EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE, EMPTY, P1_PIECE},
				{ EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY },
				{ EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY },
				{ P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY },
				{ EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE },
				{ P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY, P2_PIECE, EMPTY },
		};
	}
}
