package topcoderSemiCD;

import java.util.ArrayList;
import java.util.Collections;

public class Checkers {

    private String[][] board = new String[8][8];
    ArrayList<String> listPieces = null;
    ArrayList<Integer> listNumberOfMoves = new ArrayList<Integer>();
    ArrayList<String> listJumpPossibilities = new ArrayList<String>();
    //
    private int rowIndex = 0;
    private int colIndex = 0;
    //
    private final int COL_POS = 0;
    private final int ROW_POS = 1;
    //
    private final String BLACK_PIECE = "B";
    private final String RED_PIECE = "R";
    private final String EMPTY_SPACE = "-";
    //
    private final int MOVE_LEFT = -1;
    private final int MOVE_RIGHT = +1;

    public int compute(String startPos, String[] pieces) {
        listPieces = arrayToArrayList(pieces);

        return countBestMove(startPos, startPos, 0);
    }

    private int countBestMove(String startPos, String pieces, int numberMoves) {
        String previewMove;
        boolean haveMoved = false;
        String stackPos = startPos;

        boolean movedRight = false;

        if (endOfBoard(startPos)) {
            listNumberOfMoves.add(numberMoves);
            return numberMoves;
        }

        previewMove = getMove(startPos, MOVE_RIGHT);
        if (canMove(previewMove)) {
            stackPos = getMove(startPos, MOVE_RIGHT);
            numberMoves++;
            haveMoved = true;
            movedRight = true;
            countBestMove(stackPos, pieces, numberMoves);
        }

        previewMove = getMove(startPos, MOVE_LEFT);
        if (canMove(previewMove)) {
            stackPos = getMove(startPos, MOVE_LEFT);
            if (!movedRight) {
                numberMoves++;
            }
            haveMoved = true;
            countBestMove(stackPos, pieces, numberMoves);
        }

        if (!haveMoved) {
            listNumberOfMoves.add(-1);
        }

        return Collections.min(listNumberOfMoves);
    }

    private boolean canMove(String move) {
        boolean canMove = false;
        if (move.equals("")) {
            return false;
        }
        String[] rowCol = move.split(",");
        int row = Integer.parseInt(rowCol[ROW_POS]);
        int col = Integer.parseInt(rowCol[COL_POS]);

        if (row <= 7 && col <= 7 && col >= 0) {
            canMove = true;
        }

        return canMove;
    }

    private boolean endOfBoard(String pos) {
        String[] rowCol = pos.split(",");
        int row = Integer.parseInt(rowCol[ROW_POS]);
        int col = Integer.parseInt(rowCol[COL_POS]);

        return row == 7;
    }

    private String buildPos(int row, int col) {
        return col + "," + row;
    }

    private ArrayList arrayToArrayList(String[] array) {
        ArrayList pieces = new ArrayList();
        for (int i = 0; i < array.length; i++) {
            pieces.add(array[i]);
        }
        return pieces;
    }

    private boolean hasPiece(String pos) {
        return listPieces.indexOf(pos) > -1;
    }

    private String getMove(String pos, int rightLeft) {
        String[] rowCol = pos.split(",");
        int row = Integer.parseInt(rowCol[ROW_POS]);
        int col = Integer.parseInt(rowCol[COL_POS]);

        row += 1;
        col += rightLeft;
        String newPos = buildPos(row, col);
        if (hasPiece(newPos)) {
            row += 1;
            col += rightLeft;
            newPos = buildPos(row, col);
            if (!hasPiece(newPos)) {
                return processJump(newPos);
            } else {
                return "";
            }
        } else {
            return newPos;
        }
    }

    private String processJump(String pos) {
        String jumpPos = "";
        String bestJump = pos;
        String newPos = "";

        String[] rowCol = pos.split(",");
        int row = Integer.parseInt(rowCol[ROW_POS]);
        int col = Integer.parseInt(rowCol[COL_POS]);

        //first to the right
        if (havePieceToJump(pos, MOVE_RIGHT)) {
            row += 2;
            col += 2;
            newPos = buildPos(row, col);
            if (canMove(newPos)) {
                if (!hasPiece(newPos)) {
                    jumpPos = processJump(newPos);
                } else {
                    jumpPos = "";
                }
            } else {
                jumpPos = "";
            }

            if (getRowValue(jumpPos) > getRowValue(bestJump)) {
                bestJump = jumpPos;
            }
        }

        //now to the left
        if (havePieceToJump(pos, MOVE_LEFT)) {
            row += 2;
            col -= 2;
            newPos = buildPos(row, col);
            if (canMove(newPos)) {
                if (!hasPiece(newPos)) {
                    jumpPos = processJump(newPos);
                } else {
                    jumpPos = "";
                }
            } else {
                jumpPos = "";
            }

            if (getRowValue(jumpPos) > getRowValue(bestJump)) {
                bestJump = jumpPos;
            }
        }

        return bestJump;
    }

    private int getRowValue(String pos) {
        if (pos.equals("")) {
            return 0;
        } else {
            String[] rowCol = pos.split(",");
            return Integer.parseInt(rowCol[ROW_POS]);
        }
    }

    private boolean havePieceToJump(String pos, int rigthLeft) {
        String[] rowCol = pos.split(",");
        int row = Integer.parseInt(rowCol[ROW_POS]);
        int col = Integer.parseInt(rowCol[COL_POS]);
        row += 1;
        col += rigthLeft;
        String newPos = buildPos(row, col);
        return hasPiece(newPos);
    }
}

