package org.cdi.shikaku;

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 5/5/12
 * Time: 12:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class Placement {

    // top-left cell row
    int row;

    //top-left cell column
    int column;

    // number of rows
    int rows;

    // number of columns
    int columns;

    public Placement(int row, int column, int rows, int columns) {
        this.row = row;
        this.column = column;
        this.rows = rows;
        this.columns = columns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Placement placement = (Placement) o;

        if (column != placement.column) return false;
        if (columns != placement.columns) return false;
        if (row != placement.row) return false;
        if (rows != placement.rows) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        result = 31 * result + rows;
        result = 31 * result + columns;
        return result;
    }

    @Override
    public String toString() {
        return "[row=" + row + "; column=" + column + "; noRows=" + rows + "; noCols=" + columns + "]";
    }
}

