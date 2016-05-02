package com.beadingschememaker.model;

import com.beadingschememaker.model.Bead.BeadType;
import com.beadingschememaker.schemepainter.SchemePainter;
import com.beadingschememaker.schemepainter.SchemeType;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author Aloren
 */
public class BeadMatrix implements Iterable<Bead>, Serializable {

    private static final long serialVersionUID = 1L;
    private SchemeType type;
    private Bead[][] beads;
    private int rows;
    private int cols;

    public BeadMatrix(int rows, int cols, SchemeType type) {
        this.type = type;
        this.rows = rows;
        this.cols = cols;
        this.beads = new Bead[rows][cols];
    }

    @Override
    public Iterator<Bead> iterator() {
        return new BeadIterator<>();
    }

    public void setBeadType(BeadType beadType) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Bead bead = beads[i][j];
                if (bead != null) {
                    beads[i][j] = BeadFactory.createBead(beadType, bead.x, bead.y);
                }
            }
        }
    }

    private class BeadIterator<Bead> implements Iterator<Bead> {

        int cursor;
        int size = rows * cols;

        @Override
        public boolean hasNext() {
            return cursor != size && !(cursor == size - 1 && isNull());
        }

        @Override
        public Bead next() {
//            checkForComodification();
            int i = cursor;
            if (i >= size) {
                throw new NoSuchElementException();
            }
            cursor = i + 1;
            int col = i % cols;
            int row = (int) Math.floor((double) i / cols);
            Bead bead = (Bead) beads[row][col];
            if (bead == null && hasNext()) {
                return next();
            }
            return bead;
        }

        @Override
        public void remove() {
//            if (lastRet < 0)
//                throw new IllegalStateException();
//            checkForComodification();
//
//            try {
//                ArrayList.this.remove(lastRet);
//                cursor = lastRet;
//                lastRet = -1;
//                expectedModCount = modCount;
//            } catch (IndexOutOfBoundsException ex) {
//                throw new ConcurrentModificationException();
//            }
        }

        private boolean isNull() {
            int i = cursor;
            int col = i % cols;
            int row = (int) Math.floor((double) i / cols);
            return (Bead) beads[row][col] == null;
        }
    }

    public void addBead(int row, int col, Bead bead) {
        rangeCheck(row, col);
        beads[row][col] = bead;
    }

    public void addRows(int number, Bead.BeadType beadType) {
        Bead[][] buffer = new Bead[rows + number][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(beads[i], 0, buffer[i], 0, cols);
        }
        int x = beads[rows - 1][0].getX();
        int y = beads[rows - 1][0].getY();
        Bead[][] added = SchemePainter.getPainter(type, beadType).createRowBeads(rows, number, cols, x, y).beads;
        for (int i = rows, ii = 0; i < rows + number; i++, ii++) {
            System.arraycopy(added[ii], 0, buffer[i], 0, cols);
        }
        beads = buffer;
        rows += number;
    }

    public void addCols(int number, Bead.BeadType beadType) {
        Bead[][] buffer = new Bead[rows][cols + number];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(beads[i], 0, buffer[i], 0, cols);
        }
        int x = beads[0][cols - 1].getX();
        int y = beads[0][cols - 1].getY();
        BeadMatrix matrix = SchemePainter.getPainter(type, beadType).createColBeads(rows, number, x, y);
//        matrix.outputMatrix();
//        System.out.println("");
        Bead[][] added = matrix.beads;
        for (int i = 1; i < rows + 1; i++) {
            boolean even = (type == SchemeType.PEYOTE) ? (i % 2 == 0) : false;
            for (int j = cols, jj = 0; j < cols + number; j++, jj++) {
                buffer[i - 1][even ? j - 1 : j] = added[i - 1][jj];
            }
        }
        beads = buffer;
        cols += number;
//        outputMatrix();
    }

    private void rangeCheck(int row, int col) {
        if (row > beads.length || row < 0 || col > cols || col < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    public SchemeType getType() {
        return type;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}

