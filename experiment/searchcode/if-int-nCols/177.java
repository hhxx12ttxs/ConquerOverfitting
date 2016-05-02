/**
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original  Software is Simple Shell. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc.
 *
 * Matt Tucker, Sun Microsystems
 *
 */
package com.sun.datastorage.shell.command;

import com.sun.datastorage.shell.CmdLineParser;
import com.sun.datastorage.shell.Command;
import com.sun.datastorage.shell.Commander;
import java.io.File;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *
 * @author 
 */
public class LsCmd extends Command {

    public LsCmd(Commander cmdr) {
        super(cmdr, "ls", "com.sun.datastorage.shell.command.ls",
                "nbdocs://com.sun.datastorage.shell/com/sun/datastorage/shell/docs/shell-command-ls.html");
    }

    @Override
    public void printUsage(String[] args) {
        cmdr.getDoc().getOutWriter().println("Usage: ls ");
    }

    @Override
    public void execute(String[] args) throws Exception {

        super.execute(args);


        String[] fileNames = null;
        File[] files = null;
        if (otherArgs == null || otherArgs.length == 0) {
            fileNames = cmdr.getDoc().getPathManager().getCwd().list();
            files = cmdr.getDoc().getPathManager().getCwd().listFiles();
        } else {
            try {
                fileNames = cmdr.getDoc().getPathManager().getFile(otherArgs[1]).list();
                files = cmdr.getDoc().getPathManager().getFile(otherArgs[1]).listFiles();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        java.util.Arrays.sort(fileNames); // Sort it
        java.util.Arrays.sort(files); // Sort it
        int nFiles = fileNames.length;

        //compute number of rows, number of columns, and column size
        int nRows;
        int nCols = 1;
        int[] columnSize = new int[1];
        int totalRowSize;

        // brute force algorthim to determine the number of rows needed.
        for (nRows = 1; nRows <= nFiles; nRows++) {
            nCols = (int) Math.ceil(nFiles / nRows);
            columnSize = new int[nCols];
            totalRowSize = 0;
            for (int iCol = 0; iCol < nCols; iCol++) {
                columnSize[iCol] = 0;
                for (int j = iCol * nRows; j < (iCol + 1) * nRows && j < nFiles; j++) {
                    if (fileNames[j].length() > columnSize[iCol]) {
                        columnSize[iCol] = fileNames[j].length();
                    }
                }
                columnSize[iCol] += 3; // add in for postfix
                totalRowSize += columnSize[iCol];

                //TODO  should adjust to the panel size.
                if (totalRowSize > 80) {
                    break;
                }
            }

            if (totalRowSize < 80) {
                break;
            }
        }
        int idx;
        String postfix;

        if (nRows * nCols < nFiles) {
            nRows++;
        }

        for (int iRow = 0; iRow < nRows; iRow++) {
            for (int jCol = 0; jCol < nCols; jCol++) {
                idx = jCol * nRows + iRow;
                if (idx >= nFiles) {
                    break;
                }
                if (files[idx].isDirectory()) {
                    postfix = "/  ";
                } else {
                    postfix = "  ";
                }
                cmdr.getDoc().getOutWriter().printf("%-" + columnSize[jCol] + "s", fileNames[idx] + postfix);
            }
            cmdr.getDoc().getOutWriter().println();
        }


    }
}


