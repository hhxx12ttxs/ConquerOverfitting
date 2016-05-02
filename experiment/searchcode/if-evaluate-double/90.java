package uk.co.micaherne.noughts3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board {
	
	public enum Player {
		O,
		X
	}
	
	public enum State {
		EMPTY,
		O,
		X
	}
	
	public State[][] squares;
	public Set<Line> lines;
	
	public Map<String, Double> evaluationCache = new HashMap<String, Double>();
	public Integer evaluationCacheUsage = 0;
	
	public boolean noughtsToMove = true;
	private boolean useCache = true;
		
	public void init() {
		squares = new State[3][9];
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				setSquareState(i, j, State.EMPTY);
			}
		}
		generateLines();
		noughtsToMove = true;
	}
	
	public void generateLines() {
		lines = new HashSet<Line>();
		// Lines on planes
		for(Integer i = 0; i < 3; i++) {
			for (Integer j = 0; j < 7; j += 3) {
				Line line = new Line();
				line.squares.add(new Square(i, j));
				line.squares.add(new Square(i, j + 1));
				line.squares.add(new Square(i, j + 2));
				lines.add(line);
			}
			for (Integer j = 0; j < 3; j++) {
				Line line = new Line();
				line.squares.add(new Square(i, j));
				line.squares.add(new Square(i, j + 3));
				line.squares.add(new Square(i, j + 6));
				lines.add(line);
			}
		}
		
		// Columns
		for(Integer j = 0; j < 9; j++) {
			Line line = new Line();
			line.squares.add(new Square(0, j));
			line.squares.add(new Square(1, j));
			line.squares.add(new Square(2, j));
			lines.add(line);
		}
		
		// Diagonals on planes
		for(Integer i = 0; i < 3; i++) {
			Line line = new Line();
			line.squares.add(new Square(i, 0));
			line.squares.add(new Square(i, 4));
			line.squares.add(new Square(i, 8));
			lines.add(line);
			
			line = new Line();
			line.squares.add(new Square(i, 2));
			line.squares.add(new Square(i, 4));
			line.squares.add(new Square(i, 6));
			lines.add(line);
		}
		
		// Diagonals on columns - front to back
		for(Integer j = 0; j < 3; j++) {
			Line line = new Line();
			line.squares.add(new Square(0, j));
			line.squares.add(new Square(1, j + 3));
			line.squares.add(new Square(2, j + 6));
			lines.add(line);
			
			line = new Line();
			line.squares.add(new Square(0, j + 6));
			line.squares.add(new Square(1, j + 3));
			line.squares.add(new Square(2, j));
			lines.add(line);
		}
		// left to right
		for(Integer j = 0; j < 7; j += 3) {
			Line line = new Line();
			line.squares.add(new Square(0, j));
			line.squares.add(new Square(1, j + 1));
			line.squares.add(new Square(2, j + 2));
			lines.add(line);
			
			line = new Line();
			line.squares.add(new Square(0, j + 2));
			line.squares.add(new Square(1, j + 1));
			line.squares.add(new Square(2, j));
			lines.add(line);
		}
		
		// Diagonal diagonals
		Line line = new Line();
		line.squares.add(new Square(0, 0));
		line.squares.add(new Square(1, 4));
		line.squares.add(new Square(2, 8));
		lines.add(line);
		
		line = new Line();
		line.squares.add(new Square(0, 8));
		line.squares.add(new Square(1, 4));
		line.squares.add(new Square(2, 0));
		lines.add(line);
		
		line = new Line();
		line.squares.add(new Square(0, 2));
		line.squares.add(new Square(1, 4));
		line.squares.add(new Square(2, 6));
		lines.add(line);
		
		line = new Line();
		line.squares.add(new Square(0, 6));
		line.squares.add(new Square(1, 4));
		line.squares.add(new Square(2, 2));
		lines.add(line);

	}

	public State getSquareState(int i, int j) {
		return squares[i][j];
	}

	public void move(int j) throws IllegalMoveException {
		State newState = noughtsToMove ? State.O : State.X;
		for(int i = 0; i < 3; i++) {
			if (getSquareState(i, j) == State.EMPTY) {
				setSquareState(i, j, newState);
				noughtsToMove = !noughtsToMove;
				return;
			}
		}
		
		throw new IllegalMoveException();
	}
	
	private void undoMove(int j) throws IllegalMoveException {
		for(int i = 2; i >= 0; i--) {
			if (getSquareState(i, j) != State.EMPTY) {
				setSquareState(i, j, State.EMPTY);
				noughtsToMove = !noughtsToMove;
				return;
			}
		}
		throw new IllegalMoveException();
	}

	public void setSquareState(int i, int j, State newState) {
		squares[i][j] = newState;
	}

	public List<Integer> moveGen() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(int j = 0; j < 9; j++) {
			if (getSquareState(2, j) == State.EMPTY) {
				result.add(j);
			}
		}
		Collections.shuffle(result);
		return result;
	}

	public Map<State, Integer> checkLine(Line l) {
		Map<State, Integer> result = new HashMap<State, Integer>();
		result.put(State.EMPTY, 0);
		result.put(State.O, 0);
		result.put(State.X, 0);
		for (Square sq : l.squares) {
			Integer existingValue = result.get(getSquareState(sq.i, sq.j));
			existingValue++;
			result.put(getSquareState(sq.i, sq.j), existingValue);
		}
		return result;
	}

	public Player getWinner() {
		for(Line l : lines) {
			Map<State, Integer> check = checkLine(l);
			if (check.get(State.O) == 3) {
				return Player.O;
			} else if (check.get(State.X) == 3) {
				return Player.X;
			}
		}
		return null;
	}
	
	public double evaluate() {
		double maxEvaluation = 1;
		
		String evaluationCacheKey = null;
		
		if (useCache ) {
			evaluationCacheKey = evaluationCacheKey();
			if(evaluationCache.containsKey(evaluationCacheKey)) {
				evaluationCacheUsage++;
				return evaluationCache.get(evaluationCacheKey);
			}
		}
		
		double result;
		// first check if someone has won
		Player winner = getWinner();
		if (winner != null) {
			result = -maxEvaluation;
		} else {
			result = 0;
		}
		
		if (useCache) {
			evaluationCache.put(evaluationCacheKey, result);
		}
			
		return result;
	}
	
	public String evaluationCacheKey() {
		StringBuffer result = new StringBuffer();
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				result.append(squares[i][j]);
			}
		}
		return result.toString();
	}
	
	public int bestMove(int depth) throws NoValidMovesException, IllegalMoveException {
		
		List<Integer> legalMoves = moveGen();

		if (legalMoves.size() == 0) {
			throw new NoValidMovesException();
		}
		
		if (gameOver()) {
			throw new NoValidMovesException();
		}
		
		double max = Double.NEGATIVE_INFINITY;
		int result = -1;
		// Check the value of each possible move
		
		for(Integer move : legalMoves) {
			System.out.println("Analysing " + move);
			move(move);
			double score = -negamaxAB(this, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			// System.out.println("Move score: " + move + " = " + score);
			undoMove(move);
			if (score == 1) {
				System.out.println("wins");
			} else if (score == -1) {
				System.out.println("loses");
			} else {
				System.out.println("unclear");
			}
			if (score > max) {
				result = move;
				// System.out.println("Best move so far: " + result);
				max = score;
			}
		}
		
		return result;
	}
	
	public boolean gameOver() {
		Player winner = getWinner();
		return (winner != null);
	}
	
	public double negamaxAB(Board board, int depth, double alpha, double beta) throws IllegalMoveException {
		List<Integer> legalMoves = board.moveGen();
		
		if (board.gameOver() || legalMoves.size() == 0 || depth == 0) {
			return board.evaluate();
		}
		
		for(Integer move : legalMoves) {
			board.move(move);
			
			double score = -negamaxAB(board, depth - 1, -beta, -alpha);
			board.undoMove(move);
			if (score >= beta) {
				return beta;
			}
			if (score > alpha) {
				alpha = score;
			}
		}
		
		return alpha;
	}
	
	@Override
	public String toString() {
		String result = " 1   2   3\n--- --- ---\n";
		for(int a = 6; a >= 0; a -= 3) { // row
			for(int b = 0; b < 3; b++) { // layer
				for(int c = 0; c < 3; c++) { // col
					Integer i = b;
					Integer j = a + c;
					switch (getSquareState(i, j)) {
					
						case O:
							result += 'O';
							break;
						case X:
							result += 'X';
							break;
						case EMPTY:
							result += '+';
							break;
					}
				}
				result += " ";
			}
			result += "\n";
		}
		
		return result;
	}

	public void importPosition(String position) throws InvalidPositionException {
		String[] parts = position.trim().split(" ");
		if (parts.length != 9) {
			throw new InvalidPositionException();
		}
		
		int noughts = 0;
		int crosses = 0;
		
		for(int a = 0; a < 9; a++) {
			int i = a % 3;
			for(int b = 0; b < 3; b++) {
				int j = 6 - ((a / 3) * 3) + b;
				switch(parts[a].charAt(b)) {
				case 'O':
					setSquareState(i, j, State.O);
					noughts++;
					break;
				case 'X':
					setSquareState(i, j, State.X);
					crosses++;
					break;
				case '+':
					setSquareState(i, j, State.EMPTY);
					break;
					
				}
			}
			
		}
		noughtsToMove = (noughts == crosses);
	}
	
}

