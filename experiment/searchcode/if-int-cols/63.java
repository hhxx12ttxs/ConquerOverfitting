package solo;

import java.util.ArrayList;
import java.util.List;

public class Board {

	private final int rows, cols;
	private final Token[] board;
	private final List<Token> tokens;
	
	public Board(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.board = new Token[rows * cols];
		this.tokens = new ArrayList<Token>();
	}
	
	public int getRows() {
		return rows;
	}
	public int getCols() {
		return cols;
	}
	public List<Token> getTokens() {
		return tokens;
	}
	
	public int getTokensRemaining() {
		int remaining = 0;
		for (Token t: tokens) {
			if (!t.isRemoved()) remaining++;
		}
		return remaining;
	}
	
	public int getPos(int row, int col) {
		return row * this.cols + col;
	}
	public int getRow(int pos) {
		return pos / this.cols;
	}
	public int getCol(int pos) {
		return pos % this.cols;
	}
	
	public boolean isValidPos(int pos) {
		return pos >= 0 && pos < rows * cols;
	}
	public boolean isValidPos(int row, int col) {
		return row >= 0 && row < rows && col >= 0 && col < cols;
	}
	
	public Token getTokenAt(int pos) {
		return board[pos];
	}
	public Token getTokenAt(int row, int col) {
		return board[getPos(row, col)];
	}
	
	private Token setToken(Token token) {
		board[token.getPos()] = token;
		token.setRemoved(false);
		return token;
	}
	private Token unsetToken(Token token) {
		board[token.getPos()] = null;
		token.setRemoved(true);
		return token;
	}
	private void moveTokenTo(Token token, int target) {
		board[token.getPos()] = null;
		token.setPos(target);
		board[target] = token;
	}
	
	public Token addToken(int pos) {
		if (getTokenAt(pos) == null) {
			Token t = new Token(pos);
			tokens.add(t);
			return setToken(t);
		} else {
			return null;
		}
	}
	public Token addToken(int row, int col) {
		return addToken(getPos(row, col));
	}
	
	public boolean removeToken(Token token) {
		if (getTokenAt(token.getPos()) == token) {
			unsetToken(token);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isMoveValidWithinRange(int start, int end) {
		int delta = end - start;
		if (Math.abs(delta) == 2) {
			int startcol = getCol(start);
			if ((delta > 0 && startcol < cols - 2) ||
				(delta < 0 && startcol >= 2)) {
				return true;
			}
		} else if (Math.abs(delta) == cols * 2) {
			int startrow = getRow(start);
			if ((delta > 0 && startrow < rows - 2) ||
				(delta < 0 && startrow >= 2)) {
				return true;
			}
		}
		return false;
	}
	public Token isValidMove(int start, int end) {
		if (isMoveValidWithinRange(start, end) && getTokenAt(end) == null) {
			return getTokenAt((start + end) / 2);
		} else {
			return null;
		}
	}
	public Token tryMoveToken(Token token, int target) {
		Token removed = isValidMove(token.getPos(), target);
		if (removed != null) {
			moveTokenTo(token, target);
			removeToken(removed);
		}
		return removed;
	}
	
	public boolean undoMove(Token token, Token removed) {
		if (token.isRemoved() || !removed.isRemoved()) {
			return false;
		}
		int delta = removed.getPos() - token.getPos();
		int target = token.getPos() + delta * 2;
		moveTokenTo(token, target);
		setToken(removed);
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(rows * (cols + 1));
		for (int i = 0; i < rows * cols; i++) {
			if (i > 0 && i % cols == 0) {
				sb.append('\n');
			}
			sb.append(board[i] != null ? 'O' : '路');
		}
		return sb.toString();
	}
	
	public int rotatePosClockwise(int pos) {
		return getPos(getCol(pos), cols - getRow(pos) - 1);
	}
	public int mirrorPosHorizontal(int pos) {
		return getPos(getRow(pos), cols - getCol(pos) - 1);
	}
	public int mirrorPosVertical(int pos) {
		return getPos(rows - getRow(pos) - 1, getCol(pos));
	}
}


