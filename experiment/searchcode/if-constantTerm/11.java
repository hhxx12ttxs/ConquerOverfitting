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
import org.nextreamlabs.simplex.model.component.ConstraintSign;
import org.nextreamlabs.simplex.solver.Solver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command to add a constraint
 */
public class AddConstraintCommand extends AbstractCommand<Solver> {
  private final Integer constraintIndex;
  private final List<BigDecimal> coefficients;
  private final ConstraintSign sign;
  private final BigDecimal constantTerm;

  public AddConstraintCommand(Integer constraintIndex, List<BigDecimal> coefficients,
                              ConstraintSign sign, BigDecimal constantTerm) {
    this.constraintIndex = constraintIndex;
    this.coefficients = new ArrayList<BigDecimal>(coefficients);
    this.sign = sign;
    this.constantTerm = constantTerm;
  }

  @Override
  protected Boolean run(Solver target) {
    return target.addConstraint(this.constraintIndex, this.coefficients, this.sign,
        this.constantTerm);
  }

  @Override
  protected Map<String, String> getLoggingInfo() {
    Map<String, String> loggingInfo = new HashMap<String, String>();
    loggingInfo.put("constraint_idx", this.constraintIndex.toString());
    loggingInfo.put("coefficients", this.coefficients.toString());
    loggingInfo.put("sign", "'" + this.sign.toString() + "'");
    loggingInfo.put("constant_term", this.constantTerm.toString());
    return loggingInfo;
  }

  @Override
  public String getName() {
    return "Add Constraint";
  }

  @Override
  public Integer getPriority() {
    return 0; // Maximum priority
  }
}

