package org.foobar.minesweeper.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import javax.swing.event.EventListenerList;

import org.foobar.minesweeper.events.BoardDataEvent;

/**
 * BoardModel is a data model of the playing field for Minesweeper.
 *
 * @author Evan Flynn
 */
public class BoardModel {

    /**
     *
     */
    public enum State {

        /** The game was reset. */
        START,
        /** The game is in progress. */
        PLAYING,
        /** The game has been lost. */
        LOST,
        /** The game has been won. */
        WON,
    }
    /** A random number generator. */
    private static Random generator = new Random();
    /** The number of rows on the field. */
    private int rows;
    /** The number of columns on the field. */
    private int columns;
    /** The number of mines on the field. */
    //private int mines;
    /** A two-dimensional array of the mines. */
    List<Integer> adj;
    /** The number of flags on the field. */
    private int flagCount;
    /** The current state of the game. */
    private State state;
    /** An event object. */
    private BoardDataEvent event;
    /** A list of event listeners for this model. */
    private EventListenerList listenerList;
    private BitSet exposed;
    private BitSet flags;
    private BitSet mined;
    private int rowLength;

    /**
     * Construct a BoardModel with dimensions of <code>rowCount</code> and
     * <code>columnCount</code>.  Mines will be randomly placed throughout the
     * board.
     *
     * @param rowCount the number of rows the board holds
     * @param columnCount the number of columns the board holds
     * @param mines the number of mines
     */
    public BoardModel(int rowCount, int columnCount, int mines) {
        this.rows = rowCount;
        this.columns = columnCount;
        rowLength = columnCount + 2;
        state = State.START;

        final int length = rowLength * (rowCount + 2);
        
        exposed = new BitSet(length);
        flags = new BitSet(length);
        mined = new BitSet(length);
        adj = new ArrayList<Integer>(Collections.nCopies(length, 0));

        exposed.set(0, rowLength);
        exposed.set(length - rowLength, length);

        for (int i = 1; i <= rowCount; i++) {
            exposed.set(i * rowLength);
            exposed.set(i * rowLength + columnCount + 1);
        }

        flagCount = 0;
        generator = new Random();
        listenerList = new EventListenerList();

        for (int i = 0; i < mines; i++) {
            setRandomMine();
        }

        adjacencies(false);
        debug();
    }

    /**
     * Adds a listener to the list that is notified each time a change to the
     * data model occurs.
     *
     * @param listener the BoardModelListener to be added
     */
    public void addBoardModelListener(BoardModelListener listener) {
        listenerList.add(BoardModelListener.class, listener);
    }

    private void adjacencies(boolean clear) {
        if (clear) {
            // clear adjacency table
            Collections.fill(adj, 0);
        }

        for (int i = mined.nextSetBit(0); i >= 0; i = mined.nextSetBit(i + 1)) {
            for (int r = i - 1; r <= i + 1; r++) {
                adj.set(r - rowLength, adj.get(r - rowLength) + 1);
                adj.set(r + rowLength, adj.get(r + rowLength) + 1);
            }

            adj.set(i - 1, adj.get(i - 1) + 1);
            adj.set(i + 1, adj.get(i + 1) + 1);
            adj.set(i, 0);
        }
    }

    private void debug() {
        for (int i = 0; i <= rows+ 1; i++) {
            for (int j = 0; j <= columns + 1; j++) {
                if (isExposed(i, j)) {
                    System.out.print('*');
                } else {
                    System.out.print('.');
                }
            }
            System.out.println();
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type.
     *
     * @param e the BoardDataEvent to deliver
     */
    protected void fireBoardChanged(BoardDataEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            // if (listeners[i] == BoardModelListener.class)
            ((BoardModelListener) listeners[i + 1]).boardChanged(e);
        }
    }

    private void fireCellChanged(int row, int column) {
        fireBoardChanged(new BoardDataEvent(this, row, column));
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type.
     */
    protected void fireWholeBoardChanged() {
        if (event == null) {
            event = new BoardDataEvent(this);
        }

        fireBoardChanged(event);
    }

    /**
     * Returns the number of columns in the model.
     *
     * @return number of columns on the board
     */
    public int getColumnCount() {
        return columns;
    }

    /**
     * Returns the number of flags on the board.
     *
     * @return number of flags on the board
     */
    public int getFlagCount() {
        return 0;
    }

    /**
     * Returns the number of mines adjacent to this cell at row and column.
     * Returns <code>MINE</code> if this cell is a mine.
     *
     * @param row the row being queried
     * @param column the column being queried
     * @return number of adjacent mines, or <code>MINE</code> if a mine.
     */
    public int getMineCount(int row, int column) {
        return adj.get(rowLength * row + column);
    }

    /**
     * Returns the number of rows in the model.
     *
     * @return number of rows on board
     */
    public int getRowCount() {
        return rows;
    }

    /**
     * Returns the game state.
     * <p>
     * Upon initialization, this will return START.  When the first cell is
     * revealed, the state will be PLAYING.  If a mine is revealed, the
     * state is LOST.  If the whole board has been swept without triggering
     * a mine, then the state is WON.
     * </p>
     *
     * @return the current game state
     */
    public State getState() {
        return state;
    }

    /**
     * Returns true if the cell at the specified position has been exposed.
     *
     * @param row the row being queried
     * @param column the column being queried
     * @return true if the cell has been exposed
     */
    public boolean isExposed(int row, int column) {
        return exposed.get(rowLength * row + column);
    }

    /**
     * Returns true if the cell at the specified position is flagged.
     *
     * @param row the row being queried
     * @param column the column being queried
     * @return true if the cell is flagged
     */
    public boolean isFlagged(int row, int column) {
        return flags.get(rowLength * row + column);
    }

    /**
     * Return true if the game is done.
     *
     * @return true if the game is lost or won
     */
    public boolean isGameOver() {
        return state == State.LOST || state == State.WON;
    }

    /**
     * Return true if the cell at the specified position is a mine.
     *
     * @param row the row being queried
     * @param column the column being queried
     * @return true if the cell is a mine
     */
    public boolean isMine(int row, int column) {
        return mined.get(rowLength * row + column);
    }

    /**
     * Removes a listener from the list that is notified each time a change to
     * the data model occurs.
     *
     * @param listener the <code>BoardModelListener</code> to remove
     * @see #addBoardModelListener(events.BoardModelListener)
     */
    public void removeBoardModelListener(BoardModelListener listener) {
        listenerList.remove(BoardModelListener.class, listener);
    }

    /**
     * Reset the game.
     */
    public void reset() {
    }

    /**
     * Resize the board and reset the game.
     *
     * @param rowCount the number of rows the board will have
     * @param columnCount the number of columns the board will have
     * @param mines the number of mines the board holds
     */
    public void resize(int rowCount, int columnCount, int mines) {
        return;
    }

    /**
     * Reveals a cell at row and column. If the cell is a mine, the game is
     * over. If the game is over or the cell has already been revealed,
     * or the cell is flagged, nothing happens.
     *
     * @param row the row whose cell will be revealed.
     * @param column the column whose cell will be revealed.
     */
    public void reveal(int row, int column) {
        if (isGameOver() || isExposed(row, column) || isFlagged(row, column)) {
            return;
        }

        if (state == State.START && isMine(row, column)) {
            setRandomMine();
            mined.clear(row * rowLength + column);
            // recalculate adjacencies
            adjacencies(true);
        }

        if (state == State.START) {
            state = State.PLAYING;
        }

        if (isMine(row, column)) {
            state = State.LOST;
            exposed.or(mined);

            fireWholeBoardChanged();
            return;
        } else {
            Deque<Integer> queue = new ArrayDeque<Integer>();

            queue.add(row * rowLength + column);

            while (!queue.isEmpty()) {
                visitPair(queue, queue.removeFirst());
            }
        }
    }

    /**
     * Set a flag on the board.
     *
     * @param row  the row
     * @param column the column
     * @param flag  flag
     */
    public void setFlag(int row, int column, boolean flag) {
        if (isFlagged(row, column) != flag && isExposed(row, column)) {
            flags.set(rowLength * rows + column, flag);

            if (flag) {
                flagCount++;
            } else {
                flagCount--;
            }

            fireCellChanged(row, column);
        }
    }

    private void setRandomMine() {        
        int r = generator.nextInt(rows) + 1;
        int c = generator.nextInt(columns) + 1;

        while(isMine(r, c)) {
            r = generator.nextInt(rows) + 1;
            c = generator.nextInt(columns) + 1;
        }
        
        // Set a mine
        mined.set(r * rowLength + c);
    }

    /**
     * Change the state of the flag located at <code>row</code> and
     * <code>column</code>.
     *
     * @param row the row whose flag will be toggled.
     * @param column the column whose flag will be toggled.
     */
    public void toggleFlag(int row, int column) {
        setFlag(row, column, !isFlagged(row, column));
    }

    /**
     * TODO: Some documentation here (9/22/10)
     */
    private void visitPair(Deque<Integer> queue, int index) {
        for (int r = index - rowLength; r <= index + rowLength; r += rowLength) {
            for (int c = -1; c <= 1; c++) {
                if (adj.get(r + c) == 0 && !exposed.get(r + c)) {
                    queue.add(r + c);
                }
            }
        }

        exposed.set(index);
    }
}

