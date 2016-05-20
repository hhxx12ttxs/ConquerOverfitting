package org.cdi.shikaku;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 5/5/12
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Grid {
    int rows;
    int columns;
    List<GridEntry> gridEntries = new ArrayList<GridEntry>();
    boolean[][] state;

    public Grid(int rows, int columns) {
        this(rows, columns, Collections.<GridEntry>emptyList());
    }

    public Grid(int rows, int columns, Collection<GridEntry> gridEntries) {
        this.rows = rows;
        this.columns = columns;
        this.state = new boolean[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                state[i][j] = false;
            }
        }

        for (GridEntry entry : gridEntries) {
            addGridEntry(entry);
        }
    }

    public void addGridEntry(GridEntry gridEntry) {
        this.gridEntries.add(gridEntry);
        markGridEntryPosition(gridEntry);
        gridEntry.computeInitialPlacements(this);
    }

    private void markGridEntryPosition(GridEntry entry) {
        state[entry.positionRow - 1][entry.positionColumn - 1] = true;
    }

    public boolean solve() {
        List<GridEntry> remainingEntries = new ArrayList<GridEntry>(gridEntries);
        boolean noMoreUpdates = false;

        while (!noMoreUpdates) {
            for (GridEntry entry : remainingEntries) {
                entry.eliminateInvalidPlacements(this);
                System.out.println(entry);
            }

            List<GridEntry> entriesToSettle = new ArrayList<GridEntry>();
            for (GridEntry entry : remainingEntries) {
                List<Placement> placements = entry.getPlacements();
                if (placements.size() == 1) {
                    entriesToSettle.add(entry);
                    markPlacement(placements.get(0));
                }
            }
            remainingEntries.removeAll(entriesToSettle);
            noMoreUpdates = entriesToSettle.isEmpty();
        }

        return remainingEntries.isEmpty();
    }

    private void markPlacement(Placement placement) {
        for (int i = 0; i < placement.rows; i++) {
            for (int j = 0; j < placement.columns; j++) {
                state[placement.row + i - 1][placement.column + j - 1] = true;
            }
        }
    }

    public boolean isTaken(int row, int column) {
        return state[row - 1][column - 1];
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (GridEntry entry : gridEntries) {
            builder.append('\t').append(entry).append('\n');
        }
        return builder.toString();
    }
}

