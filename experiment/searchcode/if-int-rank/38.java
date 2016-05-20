/*
 * The MIT License (MIT)

Copyright (c) 2014 Chess Position Manager

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.loloof64.android.chs_pos_mngr.core;

/**
 * Keeps track of the pieces of the position
 * Created by laurent-bernabe on 19/06/14.
 */
public class ChessPosition {

    /**
     *
     * @param type - char - FEN value (e.g 'N' for white knight).
     * @param file - int - 0 for file A, 1 for file B ...
     * @param rank - int - 0 for rank 1. 1 for rank 2 ...
     */
    public void setPieceAt(char type, int file, int rank){
        pieces[rank][file] = type;
    }


    /**
     *
     * @param file - int - 0 for file A, 1 for file B ...
     * @param rank - int - 0 for rank 1. 1 for rank 2 ...
     * @return the type - char - FEN value (e.g 'N' for white knight).
     */
    public char getPieceAt(int file, int rank){
        return pieces[rank][file];
    }


    /**
     *
     * @param fenValue - String - Forsyth-Edwards Notation of the position.
     * @throws FEN_Exception - if a character could not be recognized
     */
    public void setFromFEN(String fenValue) throws FEN_Exception{

        String boardPart = fenValue.split(" ")[0];
        String lines [] = boardPart.split("/");

        for (int screenRank = 0; screenRank < 8; screenRank++){
            int file = 0;
            String currentLine = lines[screenRank];
            for (int currLineIndex = 0; currLineIndex < currentLine.length(); currLineIndex++){
                if (file >= 8) break;
                char currentChar = currentLine.charAt(currLineIndex);
                boolean isDigit = Character.isDigit(currentChar);
                if (isDigit){
                    int digitValue = Character.digit(currentChar, 10);
                    for (int erasedIndex = 0; erasedIndex < digitValue; erasedIndex++) {
                        pieces[7 - screenRank][file + erasedIndex] = ' ';
                    }
                    file += digitValue;
                }
                else {
                    boolean recognizedChar = "pnbrqkPNBRQK".contains(String.valueOf(currentChar));
                    if (!recognizedChar){
                        throw new FEN_Exception(String.format("Unrecognized char %c in board part of fen value %s",
                                currentChar, fenValue));
                    }
                    pieces[7 - screenRank][file] = currentChar;
                    file++;
                }
            }
        }

    }

    /**
     * pieces [rank] [file]
     */
    private char pieces [][] = new char[8][8];

}

