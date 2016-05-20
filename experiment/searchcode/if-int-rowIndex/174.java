package org.sudoku;

import org.sudoku.util.Const;
import org.sudoku.solver.RecusiveSolver;
import org.sudoku.solver.Solver;
import org.sudoku.util.Utils;

/**
 * Sudoku is a number puzzle that is popular in some of the world's
 * press. The rules of the game are simple: You simply fill in all the
 * squares in the grid so that each rowIndex, column and each of the 3x3
 * squares contains all the digits from 1 to 9 without repeats or
 * omissions.
 *
 * <p>The following is an example of a Sudoku problem:
 *
 * <pre>
 * -----------------------
 *|     6 | 2 9   | 3 4   |
 *|       |   8 6 |   9 2 |
 *|       |   7   |     1 |
 * -----------------------
 *| 1     |     8 |   7   |
 *|     8 |   2   | 1     |
 *|   9   | 1     |     8 |
 * -----------------------
 *| 7     |   3   |       |
 *| 5 1   | 8 4   |       |
 *|   6 4 |       |       |
 * -----------------------
 * </pre>
 *
 *
 *
 * <p>The full command-line invocation for the above puzzle is:
 *
 * <pre>
 * The solver can be run
 *
 * > mvn package
 * > java -cp target/sudoku-1.0-SNAPSHOT.jar org.sudoku.Sudoku 006 290 340 000 086 092 000 070 001 100 008 070 008 020 100 090 100 008 700 030 000 510 840 000 064
 * </pre>
 *
 * The empty value can be presented as <code>0</code>, every 3 value as one entry of the array string.
 * Iterate the value from left to right, then top  to down.
 *
 * The string array follow by the class should equals or less than 27 parameters (rowIndex.e. total 81 value)
 * If the array passing the program less than 27 parameters , program will treat the rest as default value/missing value.
 * e.g. There are 25 parameters passing in, then the program will treat the value of 26th and 27th parameter as default value - zero.
 *
 * The goal is to fill in the missing numbers so that
 * every rowIndex, column and box contains each of the numbers
 * <code>1-9</code>.  Giving is the result to the
 * problem above:
 *
 *
 * <pre>
 * -----------------------
 *| 1 4 6 | 2 9 4 | 3 4 8 |
 *| 2 5 8 | 1 8 6 | 5 9 2 |
 *| 3 7 9 | 3 7 5 | 6 7 1 |
 * -----------------------
 *| 1 4 6 | 3 5 8 | 2 7 6 |
 *| 2 5 8 | 4 2 7 | 1 4 9 |
 *| 3 9 7 | 1 6 9 | 3 5 8 |
 * -----------------------
 *| 7 3 8 | 1 3 6 | 1 4 7 |
 *| 5 1 9 | 8 4 7 | 2 5 8 |
 *| 2 6 4 | 2 5 9 | 3 6 9 |
 * -----------------------
 * </pre>
 * 
 *
 * tips:
 * The program currently is using RecusiveSolver to solve the Sudoku puzzles,
 * for further development, programmer can implement new Sudoku Solver and inject into
 * the main program to solve the puzzle , by implement <code>org.sudoku.solver.Solver</code>
 *
 * e.g.
 *
 * <code>
 * ...
 *
 * Sudoku sudoku=new Sudoku();
 * Solver solver=new FreeCellSolver();
 * int[][] puzzles = sudoku.parsePuzzles(args);
 * Utils.checkPuzzles(puzzles);
 * sudoku.setSolver(solver);
 * sudoku.solvePuzzles(puzzles);
 *
 * ..
 * </code>
 *
 * or
 *
 * <code>
 * ...
 *
 * Sudoku sudoku=new Sudoku();
 * Solver solver=new RecusiveSolver();
 * sudoku.execute(args,solver)
 *
 * ..
 * </code>
 *
 *
 * @author stallman wang
 */
public class Sudoku {

    public static void main(String[] args) {

        Sudoku sudoku = new Sudoku();
        sudoku.execute(args);
    }
    private Solver solver;

    /**
     * The solver implementation to solve the puzzle
     * @return
     */
    public Solver getSolver() {
        return solver;
    }

    /**
     * Set the solver implementation to solve the puzzle
     * @param solver
     */
    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    public Sudoku() {
        //Default solver is Recusive Solver
        solver = new RecusiveSolver();
    }

    /**
     * 
     * @param solver the solver implementation to solve the puzzle
     */
    public Sudoku(Solver solver) {
        this.solver = solver;
    }

    /**
     * Entry method
     * @param parameters   The parameters contain the value of puzzles
     * @param solver the solver implementation to solve the puzzles
     * @throws NumberFormatException
     */
    public void execute(String[] parameters, Solver solver) throws NumberFormatException {
        this.setSolver(solver);
        this.execute(parameters);
    }

    /**
     * Entry method
     * @param parameters  The parameters contain the value of puzzles, each parameter value contain 3 digits.
     *                    if the parameter value is empty string, then it will be treated as 000
     * @throws NumberFormatException
     */
    public void execute(String[] parameters) throws NumberFormatException {
        int expectArgLength = Const.DIMENSION * Const.DIMENSION / Const.DIMENSION_OF_BOX;
        if (parameters == null) {
            throw new IllegalArgumentException(" The parameter is empty! ");
        }
        if (parameters.length > expectArgLength) {
            throw new IllegalArgumentException(" Unexpected length of parameters, actual length is "
                    + parameters.length + ", and program expect equals to or less than: " + expectArgLength);
        }

        //Parse the problem paramters into puzzles array
        int[][] puzzles = parsePuzzles(parameters);

        Utils.checkPuzzles(puzzles);

        Utils.writeMatrix("Orginal Puzzles: ", puzzles);
        if (solver.solve(puzzles)) {
            Utils.writeMatrix("Puzzles been solved by solver - \'" + solver.getSolverName() + "\': ", puzzles);
        } else {
            System.out.println("NO SOLUTION from solver - \'" + solver.getSolverName() + "\'");
        }
    }

    /**
     * Parse the parameters contain the value of puzzle,
     * then fill in and return the 2 dimensional puzzle arrays
     * @param parameters The parameters contain the value of puzzles
     * @return
     * @throws NumberFormatException
     */
    public int[][] parsePuzzles(String[] parameters) throws NumberFormatException {
        int[][] puzzles = new int[Const.DIMENSION][Const.DIMENSION]; // board
        int paraSize = parameters.length;
        String paraValue = null;
        int rowIndex = 0, colIndex = 0;

        for (int index = 0; index < paraSize; ++index) {
            rowIndex = index / Const.DIMENSION_OF_BOX;
            colIndex = index % Const.DIMENSION_OF_BOX * Const.DIMENSION_OF_BOX;
            paraValue = parameters[index];
            parseOneParameter(paraValue, puzzles, rowIndex, colIndex);
        }

        return puzzles;
    }

    /**
     * Solve the puzzle by default solver
     * @param puzzles The puzzle arrays with expected dimension
     * @return
     */
    public boolean solvePuzzles(int[][] puzzles) {
        return solver.solve(puzzles);
    }

    /**
     * Parse one parameter and fill in the puzzles
     * @param paraValue
     * @param puzzles
     * @param rowIndex
     * @param colIndex
     * @throws NumberFormatException
     */
    private void parseOneParameter(String paraValue, int[][] puzzles, int rowIndex, int colIndex) throws NumberFormatException {
        int left, center, right;
        if (paraValue == null) {
            throw new IllegalArgumentException("Empty parameter, in row : " + rowIndex + " , from column :" + colIndex);
        }

        if (paraValue.length() == 0) {
            //empty parameter value, set to default value
            puzzles[rowIndex][colIndex] = Const.DFL_CELL_VALUE;
            puzzles[rowIndex][colIndex + 1] = Const.DFL_CELL_VALUE;
            puzzles[rowIndex][colIndex + 2] = Const.DFL_CELL_VALUE;
        } else {
            if (paraValue.length() != Const.DIMENSION_OF_BOX) {
                throw new IllegalArgumentException(" Unexpected length of this parameter: \'" + paraValue
                        + "\', in row : " + rowIndex + " , from column :" + colIndex + "program expect the length of one parameter should be : " + Const.DIMENSION_OF_BOX);
            }

            try {
                left = Integer.parseInt(paraValue.substring(0, 1));
                center = Integer.parseInt(paraValue.substring(1, 2));
                right = Integer.parseInt(paraValue.substring(2, 3));
            } catch (NumberFormatException e) {
                throw new NumberFormatException(" Invalid number while parsing parameter \'"
                        + paraValue + "\', in row : " + rowIndex + " , from column :" + colIndex);
            }
            puzzles[rowIndex][colIndex] = left;
            puzzles[rowIndex][colIndex + 1] = center;
            puzzles[rowIndex][colIndex + 2] = right;
        }
    }
}

