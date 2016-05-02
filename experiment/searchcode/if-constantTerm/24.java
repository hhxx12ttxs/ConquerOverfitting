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

package org.nextreamlabs.simplex.solver.failing_solver;

import org.nextreamlabs.simplex.model.component.ConstraintSign;
import org.nextreamlabs.simplex.solver.Solver;
import org.nextreamlabs.simplex.solver.SolverEventListener;

import java.math.BigDecimal;
import java.util.List;

/**
 * A no-op solver that always fail
 */
public class FailingSolver implements Solver {
  @Override
  public Boolean removeVariable(Integer varId) {
    return false;
  }

  @Override
  public Boolean addConstraint(Integer constraintIndex, List<BigDecimal> vars, ConstraintSign sign,
                               BigDecimal constantTerm) {
    return false;
  }

  @Override
  public Boolean removeConstraint(Integer constraintIndex) {
    return false;
  }

  @Override
  public Boolean addVariable(Integer varId, List<BigDecimal> values) {
    return false;
  }

  @Override
  public Boolean setConstraintSign(Integer constraintIndex, ConstraintSign value) {
    return false;
  }

  @Override
  public Boolean setObjfuncVariable(Integer varId, BigDecimal value) {
    return false;
  }

  @Override
  public Boolean setConstraintConstantTerm(Integer constraintIndex, BigDecimal value) {
    return false;
  }

  @Override
  public Boolean setConstraintVariable(Integer constraintIndex, Integer varId, BigDecimal value) {
    return false;
  }

  @Override
  public Boolean doStep() {
    return false;
  }

  @Override
  public String getName() {
    return "FailingSolver";
  }

  @Override
  public void register(SolverEventListener listener) {}
}

