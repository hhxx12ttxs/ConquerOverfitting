/*************************************************************************
 Copyright 2005 Webstersmalley

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 *************************************************************************/
/*
 * Created on 23-Aug-2005
 */
package com.webstersmalley.chessweb.model;

/**
 * Simple coordinate system for the position of a piece on the board.
 * 
 * @author Matthew Smalley
 */
public final class Position implements Cloneable {
    /** Position along the board. */
    private String file;

    /** Position up the board. */
    private int rank;

    /**
     * @return Returns the file.
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file
     *            The file to set.
     */
    public void setFile(final String file) {
        this.file = file;
    }

    /**
     * @return Returns the rank.
     */
    public int getRank() {
        return rank;
    }

    /**
     * @param rank
     *            The rank to set.
     */
    public void setRank(final int rank) {
        this.rank = rank;
    }

    /**
     * Default constructor.
     */
    public Position() {

    }

    /**
     * Convenience constructor.
     * 
     * @param file
     *            the file coordinate
     * @param rank
     *            the rank coordinate
     */
    public Position(final String file, final int rank) {
        this.file = file;
        this.rank = rank;
    }

    /**
     * Convenience constructor.
     * 
     * @param algebraicNotation
     *            the algebraicNotation
     */
    public Position(final String algebraicNotation) {
        setAlgebraicNotation(algebraicNotation);
    }

    /**
     * Returns a String representation.
     * 
     * @return String the string
     */
    public String toString() {
        return "" + file + rank;
    }

    /**
     * Return the modulus of the square number. Useful for working out
     * blank/white of the square.
     * 
     * @return the modulus
     */
    public int getModulus() {
        return (getFileNumber() + rank) % 2;
    }

    /**
     * Return the file number.
     * 
     * @return the file number
     */
    public int getFileNumber() {
        int i = 0;
        while (!FILE_NAMES[i].equals(file)) {
            i++;
        }
        return i + 1;
    }

    /** Helper array for index Constructor. * */
    private static final String[] FILE_NAMES = { 
        "a", "b", "c", "d", "e", "f",
            "g", "h" };

    /**
     * Create a Position based on file and rank (0-index) coordinates.
     * 
     * @param file
     *            the file
     * @param rank
     *            the rank
     */
    public Position(final int file, final int rank) {
        this(FILE_NAMES[file], rank + 1);
    }

    /**
     * Create a clone of this position.
     * 
     * @return the clone
     * @throws CloneNotSupportedException
     *             (This should not happen!)
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Whether two objects are equal.
     * 
     * @param obj
     *            the comparator
     * @return whether they are equal
     * @override java.lang.Object.equals
     */
    public boolean equals(final Object obj) {
        if (obj == null || (!(obj instanceof Position))) {
            return false;
        } else {
            Position pos = (Position) obj;
            return (pos.getFile().equals(getFile())
                    && (pos.getRank() == getRank()));
        }
    }

    /**
     * Get the hash code of the Object.
     * 
     * @return the hashCode
     */
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Adds a vector onto the position.
     * @param fileIncrement the change in file
     * @param rankIncrement the change in rank
     */
    public void add(final int fileIncrement, final int rankIncrement) {
        rank += rankIncrement;
        file = FILE_NAMES[getFileNumber() + fileIncrement - 1];
    }
    
    /**
     * Returns the algebraic notation of this position.
     * @return the algebraic notation.
     */
    public String getAlgebraicNotation() {
        return file + rank;
    }
    
    /**
     * Resets the position based on the algebraic notation.
     * @param algebraicNotation the position
     */
    public void setAlgebraicNotation(final String algebraicNotation) {
        file = algebraicNotation.substring(0, 1);
        rank = Integer.valueOf(algebraicNotation.substring(1));
    }
}

