package othelloAI;

import java.util.ArrayList;
import java.awt.Point;

public class Board{
	
	public static enum Piece{
		BLACK,
		WHITE,
		EMPTY;
		public String toString(){return this.name().toLowerCase();}}
	
	private final byte dimension;	
	private final Piece[] layout;
	private Piece currentPlayer;
		
	private static final int[][] directions = 
	{
		{-1,1},{0,1},{1,1},
		{-1,0},		 {1,0},
		{-1,-1},{0,-1},{1,-1}
	};

	public Board(int dimension, Piece startingPlayer){
		this.dimension = (byte) dimension;
		currentPlayer = startingPlayer;
		layout = new Piece[dimension * dimension];
		//Initial Position
		java.util.Arrays.fill(layout, Piece.EMPTY);		
		setPosition(dimension/2 -1, dimension/2 -1, Piece.WHITE);
		setPosition(dimension/2, dimension/2, Piece.WHITE);
		setPosition(dimension/2 -1, dimension/2, Piece.BLACK);
		setPosition(dimension/2, dimension/2 -1, Piece.BLACK);
	}
	
	public Board(Board board){
		dimension = board.dimension;
		currentPlayer = board.currentPlayer;
		layout = java.util.Arrays.copyOf(board.layout, dimension * dimension);
		//for(int i=0; i<dimension*dimension; i++){
		//	layout[i] = new Point(board.layout[i]);
		//}
	}
	

	public Piece[] getBoard() {return layout;}

	public int getDimension() {return dimension;}
	
	public Piece getPosition(int x, int y)/* throws Exception*/{
		//		if( x<0 | x>dimension | y<0 | y>dimension){
		//			throw new Exception("Out of bounds.");
		//			}
		return getPosition(y * dimension + x);
	}
	public Piece getPosition(int i){
		//System.out.println(i);
		return layout[i];
	}


	public Piece getPosition(Point pos) {
		return getPosition(pos.x, pos.y);
	}
	
	
	public boolean isGameOver(){
		for(Piece p: layout){
			if (p == Piece.EMPTY){
				return false;
			}
		}
		return true;
	}
	public ArrayList<Point> availablePositions(){
		ArrayList<Point> pointList = new ArrayList<Point>();
		for(int i=0; i<dimension * dimension; i++){
			if (layout[i]==Piece.EMPTY && valid(i)){
				pointList.add(toPoint(i));
			}
		}
		return pointList;
	}
	private ArrayList<Point> flippablePieces(int x,int y){
		ArrayList<Point> flippable = new ArrayList<Point>();
		for(int i=0; i<8; i++){			
			flippable.addAll(listFlippableOneDirection(new Point(x,y), directions[i]));
		}
		return flippable;
	}

	private boolean valid(int i) {
		return !flippablePieces(i%dimension,i/dimension).isEmpty();
	}

	private Point toPoint(int i) {
		return new Point(i%dimension,i/dimension);
	}

	private boolean isValidPosition(int x, int y){
		//System.out.print(x);System.out.println(y);
		if( x<0 || x>=dimension || y<0 || y>=dimension){
			return false;
		}
		return true;
	}
	private boolean isValidPosition(Point p){
		return isValidPosition(p.x, p.y);
	}
	private ArrayList<Point> listFlippableOneDirection(Point pos, int[] dir) {
		pos.translate(dir[0], dir[1]);
		ArrayList<Point> flip = new ArrayList<Point>();
		while (isValidPosition(pos)) {
			Board.Piece p = getPosition(pos);
			if (p == opponent(currentPlayer)) { //e.g. WBBB..
				flip.add(new Point(pos));
				pos.translate(dir[0], dir[1]);
			} else if (p == currentPlayer && !flip.isEmpty()) { //Sandwich!
				return flip;
			} else { // Empty. Return empty array.
				return new ArrayList<Point>();
			}
		}
		return new ArrayList<Point>();
	}

	static Piece opponent(Piece p){
		if(p == Piece.WHITE){return Piece.BLACK;}
		else if(p == Piece.BLACK){return Piece.WHITE;}
		else return p;
	}

	public boolean move(int x, int y){
		ArrayList<Point> fp = flippablePieces(x, y);
		if (getPosition(x, y)==Piece.EMPTY && !fp.isEmpty()){
			putPiece(new Point(x,y));
			for(Point pos: fp){
				putPiece(pos);
			}
			return true;
		}
		return false;
	}
	
	public boolean move(Point point) {
		return move(point.x, point.y);
	}
	public void setPosition(int x, int y, Piece p){
		layout[y * dimension + x] = p;
	}
	public void setPosition(Point pos, Piece p) {
		setPosition(pos.x, pos.y, p);		
	}
	public void putPiece(Point pos){
		setPosition(pos, currentPlayer);
	}


	public String toString(){
		StringBuilder sb = new StringBuilder("-------------------------\n");
		for(int y = 0; y < dimension; y++){
			sb.append("|");
			for(int x = 0; x < dimension; x++){
				if(getPosition(y,x) != null){
					sb.append(getPosition(x,y).toString() + " |");}
				else{sb.append("  |");}
			}sb.append("\n-------------------------\n");
		}
		return sb.toString();
	}

	public int heuristicWinning(){
		int count = 0;
		for(Piece p:layout){
			if(p==currentPlayer)count++;
			if(p==opponent(currentPlayer))count--;
		}
		return count;
	}

	public Piece winner() {
		int count = heuristicWinning();
		if(count>0)return currentPlayer;
		if(count<0)return opponent(currentPlayer);
		else return Piece.EMPTY;
	}

	public Piece getCurrentPlayer() {
		return currentPlayer;
	}

	public  void rotatePlayer() {
		currentPlayer = opponent(currentPlayer);
		
	}
}
