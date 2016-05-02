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

package org.nextreamlabs.simplex.solver;

import org.nextreamlabs.simplex.model.component.ConstraintSign;

import java.math.BigDecimal;
import java.util.List;

/**
 * The contract for a problem solver
 */
public interface Solver extends SolverEventSource {

  // Remove the variable from the model (from the objective-function and the constraints)
  public Boolean removeVariable(Integer id);

  // Add a new constraint into the model
  public Boolean addConstraint(Integer constraintIndex, List<BigDecimal> vars, ConstraintSign sign,
                               BigDecimal constantTerm);

  // Remove the constraint from the model
  public Boolean removeConstraint(Integer constraintIndex);

  // Add the variable into the model (in the objective-function and the constraints)
  public Boolean addVariable(Integer id, List<BigDecimal> coefficients);

  // Change the sign of the specified constraint
  public Boolean setConstraintSign(Integer constraintIndex, ConstraintSign sign);

  // Change the specified variable of the objective function
  public Boolean setObjfuncVariable(Integer id, BigDecimal value);

  // Change the constant term of the specified constraint
  public Boolean setConstraintConstantTerm(Integer constraintIndex, BigDecimal constantTerm);

  // Change the value of the specified variable in the specified constraint
  public Boolean setConstraintVariable(Integer constraintIndex, Integer id, BigDecimal coefficient);

  // Perform the successive step
  public Boolean doStep();

  // Return the solver name
  public String getName();

}

