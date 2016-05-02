package lpa.model;

import java.util.*;

/**
 * @author Ivanov Sergey
 * Date: 07.12.2012 21:30
 */
/**
 * Class that hold all GridElements
 */
public class Grid implements Iterable<GridElement> {

    /*
     * matr is a grid of GridElements
     */
    public List<List<GridElement>> matr;
    /*
     * nRows is a number of rows
     */
    private int nRows;
    /*
     * nColumns is a number of columns
     */
    private int nColumns;

    /**
     * Constructs a grid from a scanner.
     *
     * @pre {@code scanner != null}
     */
    public Grid(Scanner scanner) throws IllegalArgumentException {
        matr = new ArrayList<>();
        int columns = 0; // number of columns
        int j = - 1;

        while (scanner.hasNextLine()) {
            List<GridElement> row;
            row = new ArrayList<>();
            String stroka = scanner.nextLine();
            char[] myChar = stroka.toCharArray();
            j++;
            for (int i = 0; i < myChar.length; i++) {
                if (myChar[i] == '0' || myChar[i] == '1' || myChar[i] == '2' || myChar[i] == '3') {
                    String strokadel;
                    strokadel = "" + myChar[i];
                    row.add(new Cell(j, i, Integer.parseInt(strokadel)));
                }
                if (myChar[i] == '|' || myChar[i] == '-') {
                    row.add(new Edge(j, i, "PRESENT"));
                }
                if (myChar[i] == '.') {
                    row.add(new Edge(j, i, "UNDERTERMINED"));
                }
                if (myChar[i] == '+') {
                    row.add(new Vertex(j, i));
                }
                if (myChar[i] == ' ') {
                    if (i % 2 == 0) {
                        row.add(new Edge(j, i, "ABSENT"));
                    } else {
                        if (j % 2 == 0) {
                            row.add(new Edge(j, i, "ABSENT"));
                        } else {
                            row.add(new Cell(j, i, 4));
                        }
                    }
                }
                if (myChar[i] != ' ' && myChar[i] != '+' && myChar[i] != '.' &&
                    myChar[i] != '-' && myChar[i] != '|' && myChar[i] != '0' &&
                    myChar[i] != '1' && myChar[i] != '2' && myChar[i] != '3') {
                    throw new IllegalArgumentException("Unknown character");
                }
            }
            matr.add(row);
            if (matr.size() == 1) {
                columns = row.size();
            } else {
                if (columns != row.size()) {
                    throw new IllegalArgumentException(
                            "Grid(Scanner): precondition violated"
                            + ", row " + matr.size() + " has deviating length");
                }
            }
        }
        nRows = matr.size();
        nColumns = columns;
    }

    /**
     * Converts this grid to a string.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (List<GridElement> row : matr) {
            for (GridElement i : row) {
                result.append(i.ToString());
            }
            result.append("\n");
        }
        return result.toString();
    }

    /**
     * Get grid element, given its coordinates param x coordinate of GridElement
     *
     * @param y is a coordinate of GridElement
     * @param x is a coordinate of GridElement
     * @pre {@code x >= 0 && y >= 0}
     * @return GridElement
     * @throws NullPointerException if matr == null
     */
    public GridElement getGridElement(int x, int y) throws NullPointerException {
        return matr.get(x).get(y);
    }

    /**
     * Check grid for correctness
     *
     * @pre {@code Grid is not empty}
     * @throws NullPointerException if matr == null
     * @post {@code otvet is a result for correct grid}
     */
    public boolean Check() throws NullPointerException {
        boolean flag = true;
        for (int x = 0; x < nRows; x++) {
            for (int y = 0; y < nColumns; y++) {
                if (matr.get(x).get(y) instanceof Cell) {
                    Cell cell = (Cell) matr.get(x).get(y);
                    Cell mycell = new Cell(cell.getX(), cell.getY());
                    mycell.addEdge((Edge) matr.get(x + 1).get(y));
                    mycell.addEdge((Edge) matr.get(x - 1).get(y));
                    mycell.addEdge((Edge) matr.get(x).get(y + 1));
                    mycell.addEdge((Edge) matr.get(x).get(y - 1));
                    Histrogram histrogram1 = mycell.elementGroup.kolvoEdges();
                    int tekcount1 = histrogram1.get(EdgeState.PRESENT);
                    int tekcount2 = histrogram1.get(EdgeState.ABSENT);
                    int count1 = cell.getCellState();
                    if (count1 != 4) {
                        if (tekcount1 > count1) {
                            flag = false;
                        }
                    }
                    if (count1 != 4) {
                        if (4 - count1 < tekcount2) {
                            flag = false;
                        }
                    }
                }
                if (matr.get(x).get(y) instanceof Vertex) {
                    Vertex myVertex = new Vertex(matr.get(x).get(y).getX(), matr.get(x).get(y).getY());
                    if (x + 1 < nRows) {
                        myVertex.addEdge((Edge) matr.get(x + 1).get(y));
                    }
                    if (x - 1 >= 0) {
                        myVertex.addEdge((Edge) matr.get(x - 1).get(y));
                    }
                    if (y + 1 < nColumns) {
                        myVertex.addEdge((Edge) matr.get(x).get(y + 1));
                    }
                    if (y - 1 >= 0) {
                        myVertex.addEdge((Edge) matr.get(x).get(y - 1));
                    }
                    Histrogram histrogram1 = myVertex.elementGroup.kolvoEdges();
                    int tekcount1 = histrogram1.get(EdgeState.PRESENT);
                    int tekcount2 = histrogram1.get(EdgeState.ABSENT);
                    int tekcount3 = histrogram1.get(EdgeState.UNDERTERMINED);
                    int all = tekcount1 + tekcount2 + tekcount3;
                    if (tekcount1 > 2) {
                        flag = false;
                    }
                    if (tekcount1 == 1 && tekcount2 == all - 1) {
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    @Override
    public Iterator<GridElement> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class GridIterator implements Iterator<GridElement> {

        private int rowIndex;
        private int columnIndex;

        public GridIterator() {
            this.columnIndex = -1;
            this.rowIndex = 0;
        }

        /**
         * Returns whether the iteration has more cells.
         */
        @Override
        public boolean hasNext() {
            return rowIndex < nRows;
        }

        /**
         * Returns the next cell in the iteration.
         */
        @Override
        public GridElement next() {
            if (hasNext()) {
                GridElement result = matr.get(rowIndex).get(columnIndex);
                ++columnIndex;
                if (columnIndex == nColumns) {
                    ++rowIndex;
                    columnIndex = 0;
                }
                return result;
            } else {
                throw new NoSuchElementException("ColumnIterator.next()");
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * @return a number of rows
     */
    public int getRow() {
        return nRows;
    }

    public void setRow(int a) {
        nRows = a;
    }

    public void setRowPlusOne() {
        nRows++;
    }

    public void setRowMinusOne() {
        nRows--;
    }

    public void setColumn(int a) {
        nColumns = a;
    }

    public void setColumnPlusOne() {
        nColumns++;
    }

    public void setColumnMinusOne() {
        nColumns--;
    }

    /**
     * @return a number of columns
     */
    public int getColumn() {
        return nColumns;
    }

    /**
     * Add new row to grid
     *
     * @throws NullPointerException if grid == null
     */
    public void addRow() throws NullPointerException {
        setRowPlusOne();
        setRowPlusOne();
        List<GridElement> row = new ArrayList<>();
        for (int i = 0; i < nColumns; i++) {
            if (i % 2 == 0) {
                if (matr.get(i) == null) {
                    throw new IllegalArgumentException("Row is null");
                }
                row.add(new Edge(nRows - 2, i));
            } else {
                if (matr.get(i) == null) {
                    throw new IllegalArgumentException("Row is null");
                }
                row.add(new Cell(nRows - 2, i, 4));
            }
        }
        matr.add(row);
        row = new ArrayList<>();
        for (int i = 0; i < nColumns; i++) {
            if (i % 2 == 0) {
                if (matr.get(i) == null) {
                    throw new IllegalArgumentException("Row is null");
                }
                row.add(new Vertex(nRows - 1, i));
            } else {
                if (matr.get(i) == null) {
                    throw new IllegalArgumentException("Row is null");
                }
                row.add(new Edge(nRows - 1, i));
            }
        }
        matr.add(row);
    }

    /**
     * Delete row from grid
     */
    public void deleteRow() throws NullPointerException {
        if (nRows <= 1) {
            throw new IllegalArgumentException("It is impossible to remove a row");
        }
        matr.remove(nRows - 1);
        matr.remove(nRows - 2);
        setRowMinusOne();
        setRowMinusOne();
    }

    /**
     * Add new column to grid
     *
     * @throws NullPointerException if grid == null
     */
    public void addColumn() throws NullPointerException {
        setColumnPlusOne();
        setColumnPlusOne();
        for (int i = 0; i < nRows; i++) {
            if (i % 2 == 0) {
                if (matr.get(i) == null) {
                    throw new IllegalArgumentException("Row is null");
                }
                matr.get(i).add(new Edge(i, nColumns - 2));
                matr.get(i).add(new Vertex(i, nColumns - 1));
            } else {
                matr.get(i).add(new Cell(i, nColumns - 2, 4));
                matr.get(i).add(new Edge(i, nColumns - 1));
            }
        }
    }

    /**
     * Delete column from grid
     *
     * @throws NullPointerException if grid == null
     */
    public void deleteColumn() throws NullPointerException {
        if (nColumns <= 1) {
            throw new IllegalArgumentException("It is impossible to remove a column");
        }

        for (int i = 0; i < nRows; i++) {
            if (matr.get(i) == null) {
                throw new IllegalArgumentException("Row is null");
            }
            matr.get(i).remove(nColumns - 1);
            matr.get(i).remove(nColumns - 2);
        }
        setColumnMinusOne();
        setColumnMinusOne();
    }
}
