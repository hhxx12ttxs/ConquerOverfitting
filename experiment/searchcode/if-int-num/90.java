package sudoku;

public class SudokuSolveElimination {
	private boolean[][][] Possible; 
	private int[][] theSolved; 
	private int[][] NumPossible; 
	private boolean[][] NewlySolved;
	public SudokuSolveElimination(boolean[][][] Possible, int[][] theSolved, int[][] NumPossible, boolean[][] NewlySolved){
		this.Possible = Possible; this.theSolved = theSolved; 
		this.NumPossible = NumPossible; this.NewlySolved = NewlySolved;
	}
	public boolean[][][] getPossible(){
		return Possible;
	}
	public boolean[][] getNewlySolved(){
		return NewlySolved;
	}
	public int[][] getNumPossible(){
		return NumPossible;
	}
	public int[][] getTheSolved(){
		return theSolved;
	}
	public void EliminatePossible(){
		for(int j =1; j <= 9; j++){
			for(int k = 1; k <= 9; k++)
				if(NumPossible[j][k] == 1 && NewlySolved[j][k] == true && theSolved[j][k] != 0){
					Eliminate(j,k,theSolved[j][k]);
					NewlySolved[j][k] = false;
				}
		}
	}
	public void CheckSolved(){
		int num = 1;
		for(int j =1; j <= 9; j++){
			for(int k = 1; k <= 9; k++)
				if(NumPossible[j][k] == 1 && theSolved[j][k] == 0){
					for(int m = 1; m <= 9; m++){
						if(Possible[j][k][m]){
							num = m;
						}
					}
					theSolved[j][k] = num;
					NewlySolved[j][k] = true;
					num = 1;
				}
		}
	}
	private void Eliminate(int j, int k, int num){
		EliminateFromRow(j,k,num);
		EliminateFromColumn(j,k,num);
		EliminateFromBox(j,k,num);
	}
	private void EliminateFromRow(int row, int column, int num){
		for(int j = 1; j <= 9; j++){
			if(j != column){
				if(Possible[row][j][num]){
					NumPossible[row][j]--;
				}
				Possible[row][j][num] = false;
			}
		}
	}
	private void EliminateFromColumn(int row, int column, int num){
		for(int j = 1; j <= 9; j++){
			if(j != row){
				if(Possible[j][column][num]){
					NumPossible[j][column]--;
				}
				Possible[j][column][num] = false;
			}
		}
	}
	private void EliminateFromBox(int row, int column, int num){
		int rstart = ((row-1)/3)*3 + 1;
		int cstart = ((column-1)/3)*3 + 1;
		for(int j = rstart; j <= rstart + 2; j++){
			for(int k = cstart; k <= cstart + 2; k++){
				if((j != row) || (k != column)){
					if(Possible[j][k][num]){
						NumPossible[j][k]--;
					}
					Possible[j][k][num] = false;
				}
			}
		}
	}
}

