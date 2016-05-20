package org.sudoku.util;

/**
 * Utility class
 * @author stallman wang
 */
public class Utils {

    /**
     * Output the 2 dimensional array
     * @param title The title will be displayed at the top of the matrix
     * @param cells The value of this array will be displayed as matrix
     */
    public static void writeMatrix(String title, int[][] cells) {
        System.out.println(title);
        for (int rowIndex = Const.INDEX_START_FROM; rowIndex < Const.DIMENSION; ++rowIndex) {
            //Print '---' every 3 rows
            if (rowIndex % Const.DIMENSION_OF_BOX == Const.ZERO) {
                System.out.println(" -----------------------");
            }
            for (int colIndex = Const.INDEX_START_FROM; colIndex < Const.DIMENSION; ++colIndex) {
                //Print '|' every 3 columns
                if (colIndex % Const.DIMENSION_OF_BOX == Const.ZERO) {
                    System.out.print("| ");
                }
                //Print the value of the cells, print space while the cell value is zero ( default value of the array element)
                System.out.print(cells[rowIndex][colIndex] == Const.DFL_CELL_VALUE ? " " : Integer.toString(cells[rowIndex][colIndex]));
                System.out.print(' ');
            }
            System.out.println("|");
        }
        System.out.println(" -----------------------");
    }

    /**
     * Check whether the fixed cells are legal in the sudoku puzzles
     * Will throw exception while program check the illegal puzzle value
     * @param puzzles
     */
    public static void checkPuzzles(int[][] puzzles) throws IllegalArgumentException {
        int puzzleValue;
        if (puzzles == null) {
            throw new java.lang.IllegalArgumentException(" The puzzle set is empty! ");
        }
        if (puzzles.length != Const.DIMENSION) {
            throw new java.lang.IllegalArgumentException("Incorrect size of the puzzles, the actual length is " + puzzles.length + " , while the expected value is " + Const.DIMENSION);
        }
        for (int rowIndex = Const.INDEX_START_FROM; rowIndex < Const.DIMENSION; rowIndex++) {
            for (int colIndex = Const.INDEX_START_FROM; colIndex < Const.DIMENSION; colIndex++) {
                puzzleValue = puzzles[rowIndex][colIndex];
                if (puzzleValue != Const.DFL_CELL_VALUE && !isLegalPuzzleValue(puzzleValue, puzzles, rowIndex, colIndex)) {
                    //Check an illegal puzzle value
                    throw new IllegalArgumentException("The puzzle value \'" + puzzleValue + "\' in row:"
                            + rowIndex + ",col:" + colIndex + " is not legal for sudoku,please check.");
                }
            }
        }
    }

    /**
     * Check whether the fixed cells are legal in the sudoku puzzles
     * @param puzzles
     * @return true for legal, false for illegal
     */
    public static boolean isLegalPuzzles(int[][] puzzles) {        
        int puzzleValue;
        for (int rowIndex = Const.INDEX_START_FROM; rowIndex < Const.DIMENSION; rowIndex++) {
            for (int colIndex = Const.INDEX_START_FROM; colIndex < Const.DIMENSION; colIndex++) {
                puzzleValue = puzzles[rowIndex][colIndex];
                if (puzzleValue != Const.DFL_CELL_VALUE && !isLegalPuzzleValue(puzzleValue, puzzles, rowIndex, colIndex)) {                    
                    //Check an illegal puzzle value                    
                    return false;
                }
            }
        }

        return true; // The fixed cells are legal puzzles
    }

    /**
     * Indicate if the value is legal/suitable to be filled in current cell.
     * After detect the other cell  value in same row,same column and same box
     * @param puzzleValue puzzle value expect to be filled in the cell
     * @param puzzles whole puzzle values
     * @param currRow The row of the cell
     * @param currCol the column of the cell
     * @return  true for legal, false for illegal
     */
    public static boolean isLegalPuzzleValue(int puzzleValue, int[][] puzzles, int currRow, int currCol) {
        //The boolean value indicate if the value is legal/suitable to be filled in current cell
        boolean isLegal = true;

        // check in same row
        for (int rowIndex = Const.INDEX_START_FROM; rowIndex < Const.DIMENSION; ++rowIndex) {
            //Find the same value and it's not the same cell
            if (puzzleValue == puzzles[rowIndex][currCol] && rowIndex != currRow) {
                isLegal = false;

            }
        }

        //check in same column
        for (int colIndex = Const.INDEX_START_FROM; colIndex < Const.DIMENSION; ++colIndex) {
            //Find the same value and it's not the same cell
            if (puzzleValue == puzzles[currRow][colIndex] && colIndex != currCol) {
                isLegal = false;
            }
        }
        //Check the offset of current box
        int boxRowOffset = (currRow / Const.DIMENSION_OF_BOX) * Const.DIMENSION_OF_BOX;
        int boxColOffset = (currCol / Const.DIMENSION_OF_BOX) * Const.DIMENSION_OF_BOX;
        int boxRowStart = Const.INDEX_START_FROM + boxRowOffset;
        int boxRowEnd = boxRowStart + Const.DIMENSION_OF_BOX;
        int boxColStart = Const.INDEX_START_FROM + boxColOffset;
        int boxColEnd = boxColStart + Const.DIMENSION_OF_BOX;

        //Check in the box
        for (int rowIndex = boxRowStart; rowIndex < boxRowEnd; ++rowIndex) {
            for (int colIndex = boxColStart; colIndex < boxColEnd; ++colIndex) {
                //Find the same value and it's not the same cell
                if (puzzleValue == puzzles[rowIndex][colIndex] && rowIndex != currRow && colIndex != currCol) {
                    isLegal = false;
                }
            }
        }

        return isLegal;
    }
}

