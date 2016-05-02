/**
 Java implementation of a computer engine to play the 
 Hasami Shogi game. See: http://en.wikipedia.org/wiki/Hasami_shogi

 Copyright (C) 2010 Flavio Regis de Arruda <https://bitbucket.org/flavio_regis>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package hasami;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {

	public enum Color {BLACK, WHITE};

	private Color[][] board;
	private Color currentPlayer;
	private final int boardSize;
	
	//-- Hasami variables --//
	private int plyCounter;
	private Set<Position> blackPieces;
	private Set<Position> whitePieces;
	private HashMap<Color, Set<Position>> mapPieces;
	private HashMap<Integer, List<Position>> mapCaptures;
	
	public Board(int size){
		this.boardSize = size;
		board = new Color[size][size];
		currentPlayer = Color.WHITE;
		blackPieces = new HashSet<Position>();
		whitePieces = new HashSet<Position>();
		mapPieces = new HashMap<Color, Set<Position>> ();
		mapCaptures = new HashMap<Integer, List<Position>> ();
		mapPieces.put(Color.WHITE, whitePieces);
		mapPieces.put(Color.BLACK, blackPieces);
		plyCounter = 0;
		setupBoard();
	}//end constructor().

	
	public int getBoardSize() {
		return boardSize;
	}//end getSize().


	public boolean isGameOver(){
		if(whitePieces.size() < 2 || blackPieces.size() < 2)
			return true;
		return false;
	}//end makeMove().

	public Color get(int lin, int col){
		return board[lin][col];
	}//end makeMove().
	
	public List<HasamiMove> getMoves(){
		List<HasamiMove> response = new ArrayList<HasamiMove>();
		Set<Position> piecesPos = mapPieces.get(this.currentPlayer());
		
		for(Position pos : piecesPos){
			Collections.addAll(response, getMoves(pos));
		}
		
		return response;
	}//end makeMove().
	
	
	public void makeMove(HasamiMove move){
		board[move.endLine][move.endCol] = board[move.startLine][move.startCol];
		board[move.startLine][move.startCol] = null;
		//Testa e executa captura.
		verifyAndExecuteCapture(move.endLine, move.endCol);
		//Before turn player, update picesPos.
		Set<Position> piecesPos = mapPieces.get(this.currentPlayer());
		piecesPos.remove(new Position(move.startLine, move.startCol));
		piecesPos.add(new Position(move.endLine, move.endCol));
		turnPlayer();
		plyCounter++;
	}//end makeMove().
	
	public void undoMove(HasamiMove move){
		plyCounter--;
		
		turnPlayer();
		//Update picesPos.
		Set<Position> piecesPos = mapPieces.get(this.currentPlayer());
		piecesPos.remove(new Position(move.endLine, move.endCol));
		piecesPos.add(new Position(move.startLine, move.startCol));
		
		verifyAndUndoCapture();
		
		board[move.startLine][move.startCol] = board[move.endLine][move.endCol];
		board[move.endLine][move.endCol] = null;
		
		
		
	}//end undoMove
	

	private void verifyAndUndoCapture() {
		List<Position> captureList = mapCaptures.get(plyCounter);
		if(captureList==null)
			return;
		for(Position pos : captureList){
			board[pos.lin][pos.col]=getOpponentColor();
			mapPieces.get(getOpponentColor()).add(pos);
		}
	}

	public double evaluate(Color player, int depth){
		double eval=0;
		int fator=1;
		
		if(currentPlayer==Color.BLACK)//Importante: tem que ser o currentPlayer!!!! (năo é o player).
			fator=-1;
		
		if(hasWinner()){
				return -100+depth;
		}
		
		for(Position p : mapPieces.get(Color.WHITE)){
			double plus = evaluationBonus(Color.WHITE, p);
			eval++;
			eval += plus;
		}
	
		for(Position p : mapPieces.get(Color.BLACK)){
			double plus = -evaluationBonus(Color.BLACK, p);  //positional bonus.
			eval--;
			eval += plus;
		}
	
		return fator*eval + (Math.random()*0.1);
	}//end evaluate().

	
	private double evaluationBonus(Color posColor, Position p) {
		double eval=0;
		Position[] crux = new Position[]{new Position(p.lin+1, p.col),
										 new Position(p.lin-1, p.col),
										 new Position(p.lin, p.col+1),
										 new Position(p.lin, p.col-1)};
		
		Position[] x = new Position[]{new Position(p.lin+1, p.col+1),
				 					  new Position(p.lin-1, p.col-1),
				 					  new Position(p.lin-1, p.col+1),
				 					  new Position(p.lin+1, p.col-1)};
		
		//Try to capture more.
		for(Position pos : crux){
			if(mapPieces.get(opponentColor(posColor)).contains(pos))
				eval+=0.15;
		}
		//Try to defend more.
		for(Position pos : x){
			if(mapPieces.get(posColor).contains(pos))
				eval+=0.05;
		}
		// Corner squares are good!
		if((p.lin==0 && p.col==0) || 
		   (p.lin==0 && p.col==boardSize-1) ||
		   (p.lin==boardSize-1 && p.col==0) ||
		   (p.lin==boardSize-1 && p.col==boardSize-1)){
			eval += 0.09;
		}
		//Try to advance more.
		if(posColor==Color.WHITE){
			eval += p.lin/2d * 0.05;
		}else{
			eval += (boardSize-1-p.lin)/2d * 0.05;
		}
		
		return eval;
	}


	public double evaluateFast(Color player, int depth){
		double eval=0;
		int fator=1;
		
		if(currentPlayer==Color.BLACK)//Importante: tem que ser o currentPlayer!!!! (năo é o player).
			fator=-1;
		
		if(hasWinner()){
				return -100+depth;
		}
		for(Position p : mapPieces.get(Color.WHITE)){
			double plus = p.lin/2d * 0.1; //positional bonus.
			eval++;
			eval += plus;
		}
		for(Position p : mapPieces.get(Color.BLACK)){
			double plus = -(boardSize-1-p.lin)/2d * 0.1;  //positional bonus.
			eval--;
			eval += plus;
		}
		
		return fator*eval + (Math.random()*0.1);
	}//end evaluate().
	
	public Color currentPlayer(){
		return currentPlayer;
	}//end currentPlayer().
	

	public boolean hasWinner() {
		if(whitePieces.size() < 2 || blackPieces.size() < 2)
			return true;
		
		return false;
	}//end hasWinner().
	
	//----------------- private ---------------------
	
	private void setupBoard() {
		for(int col=0; col < boardSize; col++){
			board[0][col]=Color.WHITE;
			board[boardSize-1][col]=Color.BLACK;
			whitePieces.add(new Position(0, col));
			blackPieces.add(new Position(boardSize-1, col));
		}
		for(int line=1; line < boardSize-1; line++){
			for(int col=0; col < boardSize; col++){
				board[line][col]=null;
			}
		}
	}//end of setupBoard().

	private void verifyAndExecuteCapture(int pieceLine, int pieceCol) {
		List<Position> captures = new ArrayList<Position>();
		List<Position> buffer = new ArrayList<Position>();
		Color currentPlayerColor = board[pieceLine][pieceCol];
		Color opponentColor = getOpponentColor();
		
		//Inclui casas vazias na mesma coluna, linhas decrescentes.
		int newLin = pieceLine - 1;
		while(newLin >= 0 && board[newLin][pieceCol]!=null){
			if(board[newLin][pieceCol] == opponentColor){
				buffer.add(new Position(newLin, pieceCol));
			}else if(buffer.size()>0 && board[newLin][pieceCol]==currentPlayerColor){
				collectionsCopy(captures, buffer);
				buffer.clear();
				break; //captures from this side are finished!
			}
			newLin--;
		}

		//Inclui casas vazias na mesma coluna, linhas crescentes.
		buffer.clear();
		newLin = pieceLine+1;
		while(newLin < board.length && board[newLin][pieceCol]!=null){
			if(board[newLin][pieceCol] == opponentColor){
				buffer.add(new Position(newLin, pieceCol));
			}else if(buffer.size()>0 && board[newLin][pieceCol]==currentPlayerColor){
				collectionsCopy(captures, buffer);
				buffer.clear();
				break; //captures from this side are finished!
			}	
			newLin++;
		}
		//Inclui casas vazias na mesma linha, colunas decrescentes.
		buffer.clear();
		int newCol = pieceCol-1;
		while(newCol >= 0 && board[pieceLine][newCol]!=null){
			if(board[pieceLine][newCol] == opponentColor){
				buffer.add(new Position(pieceLine, newCol));
			}else if(buffer.size()>0 && board[pieceLine][newCol]==currentPlayerColor){
				collectionsCopy(captures, buffer);
				buffer.clear();
				break; //captures from this side are finished!
			}	
			newCol--;
		}
		//Inclui casas vazias na mesma linha, colunas crescentes.
		buffer.clear();
		newCol = pieceCol+1;
		while(newCol < board.length && board[pieceLine][newCol]!=null){
			if(board[pieceLine][newCol] == opponentColor){
				buffer.add(new Position(pieceLine, newCol));
			}else if(buffer.size()>0 && board[pieceLine][newCol] == currentPlayerColor){
				collectionsCopy(captures, buffer);
				buffer.clear();
				break; //captures from this side are finished!
			}	
			newCol++;
		}
		
		executeCaptures(captures);

	}//and of verifyAndGetCaptures.

	private <T> void collectionsCopy(List<T> dst, List<T> src) {
		for(T elem : src){
			dst.add(elem);
		}
		
	}


	private void executeCaptures(List<Position> captureList) {
		//Save it in case of latter undo.
		mapCaptures.put(plyCounter, captureList);
		
		for(Position pos : captureList){
			board[pos.lin][pos.col]=null;
			mapPieces.get(getOpponentColor()).remove(pos);
		}
		
	}//end of executeCaptures.

	private HasamiMove[] getMoves(Position pos) {
		List<HasamiMove> moves = new ArrayList<HasamiMove>();
		//Inclui casas vazias na mesma coluna, linhas decrescentes.
		int newLin = pos.lin-1;
		while(newLin >= 0 && board[newLin][pos.col]==null){
			moves.add(new HasamiMove(pos.lin, pos.col, newLin, pos.col));
			newLin--;
		}
		//Inclui casas vazias na mesma coluna, linhas crescentes.
		newLin = pos.lin+1;
		while(newLin < board.length && board[newLin][pos.col]==null){
			moves.add(new HasamiMove(pos.lin, pos.col, newLin, pos.col));
			newLin++;
		}
		//Inclui casas vazias na mesma linha, colunas decrescentes.
		int newCol = pos.col-1;
		while(newCol >= 0 && board[pos.lin][newCol]==null){
			moves.add(new HasamiMove(pos.lin, pos.col, pos.lin, newCol));
			newCol--;
		}
		//Inclui casas vazias na mesma linha, colunas crescentes.
		newCol = pos.col+1;
		while(newCol < board.length && board[pos.lin][newCol]==null){
			moves.add(new HasamiMove(pos.lin, pos.col, pos.lin, newCol));
			newCol++;
		}
		return moves.toArray(new HasamiMove[]{});
	}// end of getMoves.

	private void turnPlayer(){
		//turn player.
		if(currentPlayer==Color.BLACK)
			currentPlayer=Color.WHITE;
		else
			currentPlayer=Color.BLACK;
	}
	
	public Color getOpponentColor(){
		if(currentPlayer==Color.BLACK)
			return Color.WHITE;
		else
			return Color.BLACK;
	}
	
	public Color opponentColor(Color color){
		if(color==Color.BLACK)
			return Color.WHITE;
		else
			return Color.BLACK;
	}
	
	//------ Inner class Position -----------
	private static class Position{
		public final int lin;
		public final int col;
		public Position(int lin, int col){
			this.lin=lin;
			this.col=col;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (! (obj instanceof Position)) {
				return false;
			}
			Position pos = (Position) obj;
			return pos.hashCode()==this.hashCode();
		}
		
		@Override
		public int hashCode() {
			return 1000*lin+col;
		}	
	}//end of class Position.
	
}//end of class Board.
