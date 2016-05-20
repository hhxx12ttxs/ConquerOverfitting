package tictactoe;

import java.awt.Dimension;
import java.util.ArrayList;
import static tictactoe.Cell.Figure.FREE;
import static tictactoe.Data.Strategy.*;

/**
 *
 * @author Kapellan
 */
public class Data {

    Cell[][] cells = new Cell[3][3];
    final int CELL_SIZE = 50;
    final int PADDING = 30;
    final Dimension PREFFERED_SIZE = new Dimension(70 + PADDING * 3 + CELL_SIZE * 3, PADDING * 4 + CELL_SIZE * 3);
    private boolean gameOverUserWon;//HA-HA)))
    private boolean gameOverCompWon;
    private boolean gameOverNobodypWon;
    private boolean thisIsFirstClick;
    private Strategy currentStrategy;
    private ArrayList<Cell> variant;
    private Cell clickedCell;

    enum Strategy {

        CENTER, CORNER, SIDE;
    }

    void clickOccurred(int X, int Y) {
        try {
            clickedCell = getCellByXY(X, Y);
            if (clickedCell.isFree()) {
                if (checkForGameOver()) {
                    return;
                }
                clickedCell.setTic();
                variant = new ArrayList<Cell>();
                checkAllTriplets();
            } else {
                return;
            }
            if (thisIsFirstClick) {
                setStrategy(X, Y);
            }
            if (currentStrategy == CENTER) {
                centerStrategy().setTac();
            }
            if (currentStrategy == CORNER) {
                cornerStrategy().setTac();
            }
            if (currentStrategy == SIDE) {
                sideStrtegy().setTac();
            }
            if (thisIsFirstClick) {
                thisIsFirstClick = false;
            }
        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println(e.toString());
        } finally {
            checkForGameOver();
        }
    }

    private boolean checkForGameOver() {
        checkAllTriplets();
        if (gameOverUserWon) {
//            System.out.println("user won");
        }
        if (gameOverCompWon) {
//            System.out.println("comp won");
        }
        if (!isFreeCell()) {
            gameOverNobodypWon = true;
//            System.out.println("nobody won");
        }
        return gameOverUserWon || gameOverCompWon || gameOverNobodypWon;
    }

    /**
     * If enemy took center cell
     */
    private Cell centerStrategy() throws Exception {
        // First step must be to corner
        if (thisIsFirstClick) {
            return getCornerCell();
        }

        // If we can finish battle - do it
        for (int i = 0; i < variant.size(); i++) {
            if (variant.get(i).isWinner()) {
                return variant.get(i);
            }
        }
        // If not - we should not permit our enemy finish
        // But take cornel cells, if have
        for (int i = 0; i < variant.size(); i++) {
            if (variant.get(i).isBarrier() || variant.get(i).isCorner()) {
                return variant.get(i);
            }
        }
        // If have not
        for (int i = 0; i < variant.size(); i++) {
            if (variant.get(i).isBarrier()) {
                return variant.get(i);
            }
        }
        // On other situations we must go to the corner
        Cell cornerCell = getCornerCell();
        if (cornerCell.isExist()) {
            return cornerCell;
        }
        // Or get free cell
        Cell freeCell = getFreeCell();
        if (freeCell.isExist()) {
            return freeCell;
        }
        throw new Exception("Can't provide center startegy");
    }

    private Cell cornerStrategy() throws Exception {
        // First step must be to center
        if (thisIsFirstClick) {
            Cell centerCell = getCenterCell();
            if (centerCell.isFree()) {
                return centerCell;
            }
        }
        // If we can finish battle - do it
        for (int i = 0; i < variant.size(); i++) {
            if (variant.get(i).isWinner()) {
                return variant.get(i);
            }
        }
        // If not - we should not permit our enemy finish
        // But take cornel cells, if have
        for (int i = 0; i < variant.size(); i++) {
            if (variant.get(i).isBarrier() || variant.get(i).isCorner()) {
                return variant.get(i);
            }
        }
        for (int i = 0; i < variant.size(); i++) {
            if (variant.get(i).isBarrier()) {
                return variant.get(i);
            }
        }
        // On was click on opponent corner - take side cell
        if (clickedCell.isCorner()) {
            Cell sideCell = getSideCell();
            if (sideCell.isExist()) {
                return sideCell;
            }
        }
        Cell cornerCell = getCornerCell();
        if (cornerCell.isFree()) {
            return cornerCell;
        }
        Cell freeCell = getFreeCell();
        if (freeCell.isExist()) {
            return freeCell;
        }
        throw new Exception("Can't provide corner startegy");
    }

    private Cell sideStrtegy() throws Exception {
        // First step must be to center
        if (thisIsFirstClick) {
            return getCenterCell();
        }
        // If we can finish battle - do it
        for (int i = 0; i < variant.size(); i++) {
            if (variant.get(i).isWinner()) {
                return variant.get(i);
            }
        }
        // If not - we should not permit our enemy finish
        for (int i = 0; i < variant.size(); i++) {
            if (variant.get(i).isBarrier()) {
                return variant.get(i);
            }
        }
        // On other situations we must go to side
        if (variant.isEmpty()) {
            if (cells[0][1].isTic()) {
                if (cells[0][0].isFree()) {
                    return cells[0][0];
                }
                if (cells[0][2].isFree()) {
                    return cells[0][2];
                }
            }
            if (cells[1][2].isTic()) {
                if (cells[0][2].isFree()) {
                    return cells[0][2];
                }
                if (cells[2][2].isFree()) {
                    return cells[2][2];
                }
            }
            if (cells[2][1].isTic()) {
                if (cells[2][2].isFree()) {
                    return cells[2][2];
                }
                if (cells[2][0].isFree()) {
                    return cells[2][0];
                }
            }
            if (cells[1][0].isTic()) {
                if (cells[2][0].isFree()) {
                    return cells[2][0];
                }
                if (cells[0][0].isFree()) {
                    return cells[0][0];
                }
            }
        }
        Cell cornerCell = getCornerCell();
        if (cornerCell.isFree()) {
            return cornerCell;
        }
        Cell freeCell = getFreeCell();
        if (freeCell.isExist()) {
            return freeCell;
        }
        throw new Exception("Can't provide side startegy");
    }

    private void setStrategy(int X, int Y) throws Exception {
        if (getCellByXY(X, Y).isCenter()) {
            currentStrategy = CENTER;
        }
        if (getCellByXY(X, Y).isCorner()) {
            currentStrategy = CORNER;
        }
        if (getCellByXY(X, Y).isSide()) {
            currentStrategy = SIDE;
        }
    }

    private Cell getCellByXY(int X, int Y) throws Exception {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (X >= cells[r][c].X && X < cells[r][c].X + CELL_SIZE) {
                    if (Y >= cells[r][c].Y && Y < cells[r][c].Y + CELL_SIZE) {
                        return cells[r][c];
                    }
                }
            }
        }
        throw new Exception("Can't get XY - not a cell");
    }

    private void checkTriplet(Cell triplet[]) {
        int numTic = 0;
        int numTac = 0;
//        System.out.println("begin  ");
        for (int i = 0; i < triplet.length; i++) {
//            System.out.println("triplet  " + triplet[i].index);
            if (triplet[i].isTic()) {
                numTic++;
                if (numTic == 2) {
                    for (int a = 0; a < triplet.length; a++) {
                        if (triplet[a].isFree()) {
                            triplet[a].setBarrier();
                            variant.add(triplet[a]);
//                            System.out.println("barrier " + triplet[a].index);
                        }
                    }
                }
                if (numTic == 3) {
                    for (int a = 0; a < triplet.length; a++) {
                        triplet[a].setWinnerFigure();
                    }
                    gameOverUserWon = true;
//                    System.out.println("user won!!!");
                }
            }
            if (triplet[i].isTac()) {
                numTac++;
                if (numTac == 2) {
                    for (int b = 0; b < triplet.length; b++) {
                        if (triplet[b].isFree()) {
                            triplet[b].setWinner();
                            variant.add(triplet[b]);
//                            System.out.println("winner " + triplet[b].index);
                        }
                    }
                }
                if (numTac == 3) {
                    for (int a = 0; a < triplet.length; a++) {
                        triplet[a].setWinnerFigure();
                    }
                    gameOverCompWon = true;
//                    System.out.println("comp won!!!");
                }
            }

        }
    }

    private void checkRow() {
        Cell triplet[] = new Cell[3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                triplet[c] = cells[r][c];
            }
            checkTriplet(triplet);
        }
    }

    private void checkCol() {
        Cell triplet[] = new Cell[3];
        for (int s = 0; s < 3; s++) {
            for (int r = 0; r < 3; r++) {
                triplet[r] = cells[r][s];
            }
            checkTriplet(triplet);
        }
    }

    private void checkLeftDiag() {
        Cell triplet[] = new Cell[3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (r == c) {
                    triplet[r] = cells[r][c];
                }
            }
        }
        checkTriplet(triplet);
    }

    private void checkRightDiag() {
        Cell triplet[] = new Cell[3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (r + c == 2) {
                    triplet[r] = cells[r][c];
                }
            }
        }
        checkTriplet(triplet);
    }

    private Cell getCornerCell() {
        if (cells[0][0].isFree()) {
            return cells[0][0];
        }
        if (cells[0][2].isFree()) {
            return cells[0][2];
        }
        if (cells[2][2].isFree()) {
            return cells[2][2];
        }
        if (cells[2][0].isFree()) {
            return cells[2][0];
        }
        return new Cell();
    }

    private Cell getCenterCell() {
        if (cells[1][1].isFree()) {
            return cells[1][1];
        }
        return new Cell();
    }

    private Cell getSideCell() throws Exception {
        if (cells[0][1].isFree()) {
            return cells[0][1];
        }
        if (cells[1][2].isFree()) {
            return cells[1][2];
        }
        if (cells[2][1].isFree()) {
            return cells[2][1];
        }
        if (cells[1][0].isFree()) {
            return cells[1][0];
        }
        return new Cell();
    }

    boolean isFreeCell() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (cells[r][c].isFree()) {
                    return true;
                }
            }
        }
        return false;
    }

    private Cell getFreeCell() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (cells[r][c].isFree()) {
                    return cells[r][c];
                }
            }
        }
        return new Cell();
    }

    private void checkAllTriplets() {
        checkRow();
        checkCol();
        checkLeftDiag();
        checkRightDiag();
    }

    void setDefault() {
        gameOverUserWon = false;//HA-HA)))
        gameOverCompWon = false;
        gameOverNobodypWon = false;
        thisIsFirstClick = true;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cells[r][c] = new Cell(PADDING + c * CELL_SIZE, PADDING + r * CELL_SIZE, FREE, (Integer.toString(r) + Integer.toString(c)));
            }
        }
    }

    public Data() {
        setDefault();
    }
}

