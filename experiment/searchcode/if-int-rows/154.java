package org.cdi.shikaku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 5/5/12
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class GridEntry {
    int number;
    int positionRow;
    int positionColumn;
    List<Placement> placements = new ArrayList<Placement>();

    public GridEntry(int number, int positionRow, int positionColumn) {
        this.number = number;
        this.positionColumn = positionColumn;
        this.positionRow = positionRow;
    }

    public void computeInitialPlacements(Grid grid) {
        Set<Placement> placementSet = new HashSet<Placement>();
        for (int rows = 1; rows <= number; rows++) {
            if (number % rows == 0) {
                int columns = number / rows;
                placementSet.addAll(createPlacements(positionRow, positionColumn, rows, columns));
            }
        }

        for (Placement placement : placementSet) {
            if (!isOutOfGrid(grid, placement)) {
                this.placements.add(placement);
            }
        }
    }

    private boolean isOutOfGrid(Grid grid, Placement placement) {
        return placement.row <= 0 ||
                placement.column <= 0 ||
                placement.row + placement.rows - 1 > grid.rows ||
                placement.column + placement.columns - 1 > grid.columns;
    }

    private List<Placement> createPlacements(int row, int column, int rows, int columns) {
        List<Placement> result = new ArrayList<Placement>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.add(new Placement(row - i, column - j, rows, columns));
            }
        }
        return result;
    }

    public void eliminateInvalidPlacements(Grid grid) {
        List<Placement> placementsToRemove = new ArrayList<Placement>();

        for (Placement placement : placements) {
            boolean shouldRemove = false;
            for (int i = 0; i < placement.rows; i++) {
                if (shouldRemove) {
                    break;
                }
                for (int j = 0; j < placement.columns; j++) {
                    int row = placement.row + i;
                    int column = placement.column + j;
                    if ((row != positionRow || column != positionColumn) && grid.isTaken(row, column)) {
                        shouldRemove = true;
                        break;
                    }
                }
            }

            if (shouldRemove) {
                placementsToRemove.add(placement);
            }
        }

        placements.removeAll(placementsToRemove);
    }

    public List<Placement> getPlacements() {
        return placements;
    }

    @Override
    public String toString() {
        return "[" + positionRow + ";" + positionColumn + ";" + number + "; {" + placements + "}]";
    }
}

