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

package org.nextreamlabs.simplex.solver.command;

import org.nextreamlabs.simplex.command.AbstractCommand;
import org.nextreamlabs.simplex.solver.Solver;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Command to set the constant-term for a constraint
 */
public class SetConstraintConstantTermCommand extends AbstractCommand<Solver> {
  private final Integer constraintIndex;
  private final BigDecimal constantTerm;

  public SetConstraintConstantTermCommand(Integer constraintIndex, BigDecimal constantTerm) {
    this.constraintIndex = constraintIndex;
    this.constantTerm = constantTerm;
  }

  @Override
  protected Boolean run(Solver target) {
    return target.setConstraintConstantTerm(this.constraintIndex, this.constantTerm);
  }

  @Override
  protected Map<String, String> getLoggingInfo() {
    Map<String, String> loggingInfo = new HashMap<String, String>();
    loggingInfo.put("constraint_idx", this.constraintIndex.toString());
    loggingInfo.put("constant-term", this.constantTerm.toString());
    return loggingInfo;
  }

  @Override
  public String getName() {
    return "Set Constraint Constant-Term";
  }

  @Override
  public Integer getPriority() {
    return 5;
  }
}

