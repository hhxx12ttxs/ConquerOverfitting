package chessminion.gameinfo;

import java.awt.Point;
import java.io.Serializable;
import java.util.*;

import chessminion.gameinfo.ChessPiece.*;
import javax.persistence.Embeddable;


@Embeddable
public class ChessBoard implements Serializable{


	enum MoveType {
		PLAIN, CAPTURE, CASTLE, EN_PASSANT, PAWN_2MOVE, CHECKMATE;

		public boolean isCapture() {
			switch(this){
			case CAPTURE:
			case EN_PASSANT:
			case CHECKMATE:
				return true;
			default:
				return false;
			}
		}
	};

	private ChessPiece[][] board = new ChessPiece[8][8];
	protected Point passing_pawn_loc = null;

	protected boolean white_in_check = false;
	protected boolean black_in_check = false;

	/**
	 * We can use this to store all captured pieces in one array--if we
	 * need just one or the other, we can always just filter out by color.
	 */
	protected ArrayList<ChessPiece> captured = new ArrayList<ChessPiece>();

	/*board init code */
	{
		board[0][0] = ChessPiece.getPiece(PieceColor.WHITE, PieceType.ROOK);
		board[7][0] = ChessPiece.getPiece(PieceColor.WHITE, PieceType.ROOK);
		board[1][0] = ChessPiece.getPiece(PieceColor.WHITE, PieceType.KNIGHT);
		board[6][0] = ChessPiece.getPiece(PieceColor.WHITE, PieceType.KNIGHT);
		board[2][0] = ChessPiece.getPiece(PieceColor.WHITE, PieceType.BISHOP);
		board[5][0] = ChessPiece.getPiece(PieceColor.WHITE, PieceType.BISHOP);
		board[3][0] = ChessPiece.getPiece(PieceColor.WHITE, PieceType.QUEEN);
		board[4][0] = ChessPiece.getPiece(PieceColor.WHITE, PieceType.KING);


		board[0][7] = ChessPiece.getPiece(PieceColor.BLACK, PieceType.ROOK);
		board[7][7] = ChessPiece.getPiece(PieceColor.BLACK, PieceType.ROOK);
		board[1][7] = ChessPiece.getPiece(PieceColor.BLACK, PieceType.KNIGHT);
		board[6][7] = ChessPiece.getPiece(PieceColor.BLACK, PieceType.KNIGHT);
		board[2][7] = ChessPiece.getPiece(PieceColor.BLACK, PieceType.BISHOP);
		board[5][7] = ChessPiece.getPiece(PieceColor.BLACK, PieceType.BISHOP);
		board[3][7] = ChessPiece.getPiece(PieceColor.BLACK, PieceType.QUEEN);
		board[4][7] = ChessPiece.getPiece(PieceColor.BLACK, PieceType.KING);

		for (int i = 0; i < 8; i++) {
			board[i][1] = ChessPiece.getPiece(PieceColor.WHITE, PieceType.PAWN);
			board[i][6] = ChessPiece.getPiece(PieceColor.BLACK, PieceType.PAWN);
		}
	}

	/**
	 * Dump the current state of the board into a human-readable string.
	 * @return a string containing an ASCII depiction of the board, and
	 * information on which player (if any) is in check.
	 */
	private String dumpString() {
		String eol = System.getProperty("line.separator");
		StringBuffer b = new StringBuffer();
		for (int row = 7; row >= 0; --row) {
			for (int col = 0; col < 8; col++) {
				ChessPiece p = this.board[col][row];
				char out;
				if (null == p) {
					out = '-';
				} else {
					out = p.getType().pieceCode();
					if (PieceColor.WHITE == p.getColor()) {
						out = Character.toUpperCase(out);
					}
				}

				b.append(out);
			}
			b.append(eol);
		}
		if (white_in_check) {
			b.append("White in check");
			b.append(eol);
		}
		if (black_in_check) {
			b.append("Black in check");
			b.append(eol);
		}
		return b.toString();
	}

	/**
	 * Commit a move, without checking legality (invalid moves will be rejected, but you
	 * can still put yourself in check--better have called checkMove first.)
	 * @param from the origin space of the move
	 * @param to the destination space of the move
	 */
	public void commitMove(Point from, Point to) {
		commitMove(from, to, null);
	}

	public void commitMove(Point from, Point to, PieceType promoteTo) {
		ChessPiece moving = at(from);
		Point victimPoint = to;
		ArrayList<HalfMove> fromMoves = getMoves(from);
		Move chosen = null;
		ChessPiece victim = null;
		boolean mustPromote = false;
		for (HalfMove m : fromMoves) {
			if (m.dest.equals(to)) {
				chosen = new Move(from,to,m.type);
				break;
			}
		}


		if (null == chosen) {
			throw new IllegalArgumentException("Move is not legal");
		} else if (MoveType.EN_PASSANT == chosen.type) {
			victimPoint = new Point(to.x, from.y);
		} else if (PieceType.PAWN == moving.getType()) {
			if ( 0 == to.y || 7 == to.y) {
				mustPromote = true;
				if (null == promoteTo) {
					throw new UnPromotedPawnException("moving "
							+ moving + " to final row requires promotion");
				}
			}
		}

		victim = at(victimPoint);
		if (null != victim) {
			if (victim.color_ == moving.color_) {
				throw new IllegalArgumentException("can't capture allies");
			}
		}

		if (mustPromote) { // may throw IllegalArgumentException--that's OK
			Pawn p = (Pawn) moving;
			p.promoteTo(promoteTo);
		}

		/* transaction start ... */
		clear(from);
		clear(victimPoint);
		set(to, moving);
		moving.firstmove = false;
		if (null != victim) {
			this.captured.add(victim);
		}
		if (MoveType.PAWN_2MOVE == chosen.type) {
			passing_pawn_loc = to;
		} else {
			passing_pawn_loc = null;
			if (MoveType.CASTLE == chosen.type) {
				int rook_start, rook_end;
				if (6 == to.x) {
					rook_start = 7;
					rook_end = 5;
				} else {
					rook_start = 0;
					rook_end = 3;
				}
				Point rookfrom = new Point(rook_start,from.y);
				Point rookto = new Point(rook_end,from.y);
				ChessPiece rook = at(rookfrom);
				set(rookto,rook);
				clear(rookfrom);
			}
		}

		// oh dear, this gets time-consuming (better to pre-check)
		black_in_check = hasCheck(PieceColor.WHITE);
		white_in_check = hasCheck(PieceColor.BLACK);
		/* transaction end */
	}


	/**
	 * Check if the give move is legal in all senses: the move can be made,
	 * and taking it would not place or leave the player in check.
	 * @param from the origin space of the move
	 * @param to the destination space of the move
	 * @return true if the move is legal, false otherwise.
	 */
	public boolean checkMove(Point from, Point to) {
		HalfMove foundMove = null;
		for (HalfMove m : getMoves(from) ) {
			if (m.dest.equals(to)) {
				foundMove = m;
				break;
			}
		}
		if (null != foundMove) {
			boolean moveLegal = true;
			ChessPiece oldOccupant = at(to); // may be null
			ChessPiece moving = at(from);
			board[to.x][to.y] = moving;
			board[from.x][from.y] = null;
			if (hasCheck(moving.getColor().reverse())) {
				moveLegal = false;
			}
			// rollback:
			board[from.x][from.y] = moving;
			board[to.x][to.y] = oldOccupant;
			return moveLegal;
		} else {
			return false;
		}
	}


        public boolean isPawnPromotion(Point from, Point to) {
            ChessPiece cp = at(from);
            if(cp == null)
                throw new IllegalArgumentException("No piece at this point");

            if (PieceType.PAWN == cp.getType()) {
                if ( 0 == to.y || 7 == to.y) {
                    return true;
                }
            }
            return false;
        }
	/**
	 * Retrieve all the moves that a piece at this location could make.  Does not
	 * take into account questions such as placing yourself in check.
	 * @param from a point on the board
	 * @return all moves that the piece at the given point could make,
	 * assuming no restrictions other than movement and capturing rules.
	 */
	private ArrayList<HalfMove> getMoves(Point from) {
		return getMoves(from, false);
	}

	/**
	 * Retrieve all the moves that a piece at this location could make.  Does not
	 * take into account questions such as placing yourself in check.
	 * @param from the point at which the piece to be moved is currently located
	 * @param threatsonly if true, return only moves which would capture an opposing piece.
	 * @return a list of Moves, including either all moves that a piece at this location
	 * could make or all captures that a piece at this location could make.
	 */
	private ArrayList<HalfMove> getMoves(Point from, boolean threatsonly) {
		ArrayList<HalfMove> moves = new ArrayList<HalfMove>();
		ChessPiece moving = at(from);
		if (null == moving) {
			throw new IllegalArgumentException("No piece to move at " + from);
		}

		PieceColor myColor = moving.getColor();
		PieceType t = moving.getType();
		switch (t) {
		case PAWN:
			/* very special cases:
			 * first move
			 * capture
			 * capture en-passant
			 */
			int ydir = PieceColor.WHITE == moving.getColor() ? 1 : -1;
			int simple_y = ydir + from.y;
			if ( 7 < simple_y  || 0 > simple_y) {
				// throw new UnPromotedPawnException(moving.getColor() + " pawn in last rank should have been promoted!");
				System.err.println(moving.getColor() + " pawn in last rank should have been promoted!");
			}
			// capture...
			int column;
			if (from.x > 0) {
				column = from.x - 1;
				Point maybe = new Point(column, simple_y);
				ChessPiece other = at(maybe);
				if (null != other) {
					if(!other.isColor(myColor)) {
						MoveType mt = other.getType().equals(PieceType.KING) ? MoveType.CHECKMATE : MoveType.CAPTURE;
						moves.add(new HalfMove(maybe,mt));
					}
				} else if (null != passing_pawn_loc) { // passing pawn can't be there if there's a regular capture
					Point passpoint = new Point(column, from.y);
					if (passing_pawn_loc.equals(passpoint)) {
						moves.add(new HalfMove(maybe,MoveType.EN_PASSANT));
					}
				}
			}
			if (from.x < 7) {
				column = from.x + 1;
				Point maybe = new Point(column, simple_y);
				ChessPiece other = at(maybe);
				if (null != other) {
					if (!other.isColor(myColor)) {
						MoveType mt = other.getType().equals(PieceType.KING) ? MoveType.CHECKMATE : MoveType.CAPTURE;
						moves.add(new HalfMove(maybe,mt));
					}
				} else if (null != passing_pawn_loc) {
					Point passpoint = new Point(column,from.y);
					if (passing_pawn_loc.equals(passpoint)) {
						moves.add(new HalfMove(maybe,MoveType.EN_PASSANT));
					}
				}
			}
			// if we only want captures, stop here
			if (threatsonly) break;

			// standard
			Point simplemove = new Point(from.x,simple_y);
			if (null == at(simplemove)) {
				moves.add(new HalfMove( simplemove ));
				// first move
				Point twomove = new Point(from.x,from.y + 2 * ydir);
				if (moving.firstmove && null == at(twomove)) {
					moves.add(new HalfMove(twomove,MoveType.PAWN_2MOVE));
				}
			}
			break;
		case KING:
			/* special case:
			 * castling (only if we care about non-capturing moves)
			 * other cases handled by standard-move code below
			 */

			if (moving.firstmove && !threatsonly && !inCheck(myColor)) {
				// castling possible
				int rank = from.y;
				boolean qcastle_ok = true;
				boolean kcastle_ok = true;
				/*
				 * for each side
				 * 	find the rook and
				 *  	check if it has moved (just check the two squares)
				 *  check if the path between is clear
				 *  check that we are not in check
				 *  	and there's no threat to the intermediate square
				 *  	(that one's a pain)
				 */
				ChessPiece qRook = board[0][rank];
				ChessPiece kRook = board[7][rank];
				if (null == qRook || !qRook.firstmove) {
					qcastle_ok = false;
				} else {
					for (int i = 1; i < 4; i++) {
						if (null != board[i][rank]) {
							qcastle_ok = false;
							break;
						}
					}
				}
				if (null == kRook || !kRook.firstmove) {
					kcastle_ok = false;
				} else {
					if (null != board[5][rank] || null != board[6][rank]) {
						kcastle_ok = false;
					}
				}
				// granted, we know now that from.x == 4, but shush
				if (qcastle_ok) {
					Point halfway = new Point(from.x - 1, rank);
					if ( !threatenedBy(halfway, myColor.reverse()) ) {
						moves.add(new HalfMove(new Point(from.x - 2,rank),MoveType.CASTLE));
					}
				}
				if (kcastle_ok) {
					Point halfway = new Point(from.x + 1, rank);
					if ( !threatenedBy(halfway, myColor.reverse()) ) {
						moves.add(new HalfMove(new Point(from.x + 2,rank),MoveType.CASTLE));
					}
				}
			}
		case KNIGHT:
		case ROOK:
		case BISHOP:
		case QUEEN:
			Point[][] maybies = ChessPiece.simpleMoveOptions(t,from);
			for (Point[] path : maybies) {
				moves.addAll(filterMoves(path, moving.getColor()));
			}
		}
		return moves;
	}

	public boolean hasMoves(PieceColor whose) {
		for (int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				Point here = new Point(x,y);
				ChessPiece thisPiece = at(here);
				if (null != thisPiece && thisPiece.isColor(whose)) {
					for (HalfMove m : getMoves(here)) {
						if (this.checkMove(here, m.dest)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * According to the internal data structures (which hopefully have been updated recently), is this player in check?
	 * @param who the player who may be in check
	 * @return true if the player is in check, false otherwise
	 */
	public boolean inCheck(PieceColor who) {
		if(who == null)
			throw new IllegalArgumentException("Can't check if null pointer is in check");
		switch(who) {
		case WHITE: return white_in_check;
		case BLACK: return black_in_check;
		default: throw new IllegalArgumentException("Can't check if null pointer is in check");
		}
	}
	/**
	 * Does this player have his opponent in check, given the current
	 * configuration of the board?
	 * @param checking the color of the side on offense
	 * @return true if the checking color has the other player in check, false otherwise
	 */
	private boolean hasCheck(PieceColor checking) {
		for (int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				Point here = new Point(x,y);
				ChessPiece thisPiece = at(here);
				if (null != thisPiece && thisPiece.isColor(checking)) {
					for (HalfMove m : getMoves(here,true)) { // should be (here, true), no?
						if (MoveType.CHECKMATE == m.type) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Is this space on the board threatened by a piece of this color?
	 * @param boardspace a point on the board
	 * @param byWhom the color of pieces which might threaten.
	 * @return true if there is a piece of the designated color that threatens the designated space.
	 */
	private boolean threatenedBy(Point boardspace, PieceColor byWhom) {
		for (int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				Point here = new Point(x,y);
				ChessPiece thisPiece = at(here);
				if (null != thisPiece && thisPiece.isColor(byWhom)) {
					List<HalfMove> moves = getMoves(here,true);
					for (HalfMove m : moves) {
						if(boardspace.equals(m.dest)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Convenience method: what piece is currently in this square?
	 * @param where a point on the board
	 * @return the piece at this position on the board,
	 * or null if the square is unoccupied.
	 * @throws IllegalArgument exception if the point given does not lie on the board.
	 */
	protected ChessPiece at(Point where) {
		if (!inRange(where)) {
			throw new IllegalArgumentException("point " + where + " is not on the board");
		}
		return board[where.x][where.y];
	}

	/**
	 * @param where
	 * @param moving
	 */
	private void set(Point where, ChessPiece moving) {
		if (!inRange(where)) {
			throw new IllegalArgumentException("point " + where + " is not on the board");
		}
		board[where.x][where.y] = moving;
	}


	/**
	 * @param where
	 */
	protected void clear(Point where) {
		if (!inRange(where)) {
			throw new IllegalArgumentException("point " + where + " is not on the board");
		}
		board[where.x][where.y] = null;
	}


	/**
	 * Filter a list of potentially possible moves to remove actually impossible ones.
	 * This method takes an array of points with the following characteristic: each
	 * point on the list is one to which a specific piece could move <b>if and only if</b>
	 * it is possible for it to move through each earlier point on the list.
	 *
	 * The method iterates through the list, building a list of HalfMove objects with each
	 * successive point as destinations, and stopping if it encounters a square through
	 * which a piece cannot move.  If this final square is occupied by a piece of the
	 * opposing color, a move to it is also returned (as a HalfMove with the CAPTURE
	 * or CHECKMATE type); if not, only the previously built list (which may be empty)
	 * is returned.
	 * @param options
	 * @param movingColor the color of the piece making the move
	 * @return a list of Moves representig the set of points in this list which are legal moves
	 * for a piece of this color.
	 */
	protected ArrayList<HalfMove> filterMoves(Point[] options, PieceColor movingColor) {
		ArrayList<HalfMove> moves_ok = new ArrayList<HalfMove>();
		for (Point dest : options) {
			ChessPiece destPiece = at(dest);
			if (null != destPiece) {
				/* add this as a capture if opposite color */
				if (destPiece.getColor() != movingColor) {
					MoveType t = MoveType.CAPTURE;
					if (PieceType.KING == destPiece.getType()) {
						t = MoveType.CHECKMATE;
					}
					moves_ok.add(new HalfMove(dest,t));
				}
				break;
			}
			moves_ok.add(new HalfMove(dest));
		}
		return moves_ok;
	}
	/**
	 * Check if a given move is valid for the player with the given color,
	 * assuming no blocking pieces:
	 * there is a piece at the origin point which belongs to the player whose turn it is,
	 * that piece is capable of moving from the origin to the destination,
	 * and if there is a piece at the destination, it belongs to the other player.
	 * @param fromSquare the square on the board where the move starts.
	 * @param toSquare the square on the board where the move ends.
	 * @param moving the color of the player who is attempting to move.
	 * @return true if the conditions outlined above hold, false otherwise.
	 */
	public boolean moveValid(Point fromSquare, Point toSquare, PieceColor moving) {
		boolean isValid = false;
		ChessPiece toMove = at(fromSquare);
		ChessPiece toCapture = at(toSquare);
		if (null != toMove) {
			isValid = toMove.isColor(moving);
			boolean isCapture = false;
			if (null != toCapture) {
				if (toCapture.isColor(moving)) {
					isValid = false;
				} else {
					isCapture = true;
				}
			}

			if (isValid) {
				isValid = toMove.canMove(fromSquare, toSquare, isCapture);
			}
		}

		return isValid;
	}
	/**
	 * Convenience method: is this point on the board?
	 * @param q a Point
	 * @return true if the point lies on the standard chessboard coordinates
	 * (0 to 7, inclusive, on both x and y axes).
	 */
	private boolean inRange(Point q) {
		if (0 > q.x || 7 < q.x || 0 > q.y || 7 < q.y) return false;
		else return true;
	}

	public ChessPiece[][] getBoard(){
		return this.board;
	}

	/**
	 * An inner class that encapsulates partial move information.
	 * @author ben
	 *
	 */
	private class HalfMove {
		Point dest;
		MoveType type;
		public HalfMove(Point d, MoveType t) {
			dest = d;
			type = t;
		}
		public HalfMove(Point d) {
			this(d,MoveType.PLAIN);
		}

	}

	private class Move extends HalfMove {
		Point origin;
		public Move(Point from, Point to, MoveType t) {
			super(to, t);
			origin = from;
		}

	}


	@SuppressWarnings("serial")
	private class UnPromotedPawnException extends IllegalArgumentException {
		public UnPromotedPawnException(String s) {
			super(s);
		}
	}
	/**
	 * Demo main function--play a random chess game.
	 * @param args
	 */
	public static void main(String[] args) {
		ChessBoard b = new ChessBoard();
		System.out.print(b.dumpString());
		PieceColor current = PieceColor.WHITE;
		Random rand = new Random();

		for (int movenumber = 0; movenumber < 200; movenumber++) {
			int validseen = 0;
			int captures_seen = 0;
			Point moveFrom = null;
			HalfMove moveChosen = null;
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					Point here = new Point(j,i);
					ChessPiece thisOne = b.at(here);
					try {
						List<HalfMove> fromHere = b.getMoves(here);
						boolean ourturn = b.at(here).getColor() == current;
						for (HalfMove m : fromHere) {
							if (!b.checkMove(here, m.dest)) {
								System.out.println("Can NOT move " + thisOne + " from " + here +
										" to " + m.dest + "(" + m.type + ")");
								continue;
							}
							System.out.println("Can move " + thisOne + " from " + here +
									" to " + m.dest + "(" + m.type + ")");
							if (ourturn) {
								boolean choosemove = false;
								if (m.type.isCapture()) {
									if (0 == rand.nextInt(++captures_seen)) {
										choosemove = true;
									}
								} else if (0 == captures_seen && 0==rand.nextInt(++validseen)) {
									choosemove = true;
								}
								if (choosemove) {
									moveFrom = here;
									moveChosen = m;
								}
							}
						}
					} catch (IllegalArgumentException e) {
						// meh
					}
				}
			}

			try {
				if (null == moveChosen) {
					System.out.println("No moves left for " + current + ": terminating");
					break;
				}
				System.out.println(
						String.format("Moving %s from %s to %s (%s)",
								new Object[] {b.at(moveFrom),moveFrom,moveChosen.dest,moveChosen.type})
				);
				try {
					b.commitMove(moveFrom, moveChosen.dest);
				} catch (UnPromotedPawnException e) {
					System.err.println("Promoting pawn at " + moveChosen.dest);
					b.commitMove(moveFrom, moveChosen.dest, PieceType.QUEEN);
				}
			} catch (IllegalArgumentException e) {
				System.err.println(e.getMessage());
				e.printStackTrace(System.err);
			}


			System.out.print(b.dumpString());
			current = current.reverse();
		}
	}

}

