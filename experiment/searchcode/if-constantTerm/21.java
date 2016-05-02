/**************************************************************************************************
 * Copyright (C) 2012  nextreamlabs                                                               *
 * This program is free software: you can redistribute it and/or modify                           *
 * it under the terms of the GNU General Public License as published by                           *
 * the Free Software Foundation, either version 3 of the License, or                              *
 * (at your option) any later version.                                                            *
 * This program is distributed in the hope that it will be useful,                                *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
 * GNU General Public License for more details.                                                   *
 * You should have received a copy of the GNU General Public License                              *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.                          *
 **************************************************************************************************/

package org.nextreamlabs.simplex.ui.file;

import org.nextreamlabs.simplex.model.component.ConstraintSign;
import org.nextreamlabs.simplex.ui.Ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * Represent a kind a user interaction through files
 */
public class FileUi implements Ui {
  private static final Logger logger;

  private final File file;

  static {
    logger = Logger.getLogger("org.nextreamlabs.simplex.FileUi");
  }

  // { Constructors and Factories

  private FileUi(String path) throws InvalidInputFileException {
    File file = new File(path);
    if (!file.canWrite()) {
      throw new InvalidInputFileException("The file " + path + " cannot be written");
    }
    this.file = file;
  }

  public static Ui create(String path) throws InvalidInputFileException {
    return new FileUi(path);
  }

  // }

  // { UI implementation

  @Override
  public Boolean start() {
    return Boolean.TRUE; // No initialization is needed
  }

  @Override
  public Boolean setConstraintSign(Integer constraintIndex, ConstraintSign sign) {
    return this.writeLine("Set constraint sign: [constraint_idx=" + constraintIndex + "]" +
        "[sign='" + sign + "']");
  }

  @Override
  public Boolean setConstraintVariable(Integer constraintIndex, Integer id, BigDecimal coefficient) {
    return this.writeLine("Set constraint variable: [constraint_idx=" + constraintIndex + "]" +
        "[var_id=" + id + "][coefficient=" + coefficient + "]");
  }

  @Override
  public Boolean setConstraintConstantTerm(Integer constraintIndex, BigDecimal constantTerm) {
    return this.writeLine("Set constraint constant-term: [constraint_idx=" + constraintIndex + "]" +
        "[constant_term=" + constantTerm + "]");
  }

  @Override
  public Boolean setObjfuncVariable(Integer id, BigDecimal coefficient) {
    return this.writeLine("Set objective-function variable: [var_id=" + id + "]" +
        "[coefficient='" + coefficient + "']");
  }

  @Override
  public Boolean setStatusText(String text) {
    return Boolean.TRUE; // NO-OP
  }

  // { Utilities for DRY

  /**
   * Open the file in write mode
   * @return The BufferedWriter for the file
   */
  private BufferedWriter getWriter() {
    BufferedWriter writer;
    try {
      writer = new BufferedWriter(new FileWriter(this.file, true));
    } catch (IOException exc) {
      logger.severe("Can't open the output file: " + this.file.getAbsolutePath() +
          " in write mode");
      return null;
    }
    return writer;
  }

  private Boolean writeLine(String line) {
    Boolean status = Boolean.TRUE;
    BufferedWriter writer = this.getWriter();
    if (writer == null) {
      logger.warning("Cannot create the writer");
      status = Boolean.FALSE;
    } else {
      try {
        writer.write(line + "\n");
      } catch (IOException e) {
        logger.severe("Cannot write to the file " + this.file.getAbsolutePath());
        status = Boolean.FALSE;
      }

      try {
        writer.close();
      } catch (IOException ignored) {}
    }
    return status;
  }

  // }
}

