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

package org.nextreamlabs.simplex.model.component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contract for a linear constraint
 */
public interface ImmutableLinearConstraint {
  public ConstraintSign getSign();
  public BigDecimal getConstantTerm();
  public Integer getSize();
  public Variable getVariable(Integer id);
  public List<Variable> getVariables();

  public ImmutableLinearConstraint duplicate(List<Variable> vars, ConstraintSign sign,
                                    BigDecimal constantTerm);

  public Variable findVariableById(Integer id);

  // { Manipulation operations

  public ImmutableLinearConstraint multiply(BigDecimal scalar);
  public ImmutableLinearConstraint sum(ImmutableLinearConstraint constraint);

  public ImmutableLinearConstraint addVariable(Variable var);
  public ImmutableLinearConstraint removeVariable(Integer id);
  public ImmutableLinearConstraint setVariable(Integer id, BigDecimal coefficient);

  public ImmutableLinearConstraint setSign(ConstraintSign sign);

  public ImmutableLinearConstraint setConstantTerm(BigDecimal constantTerm);

  // }
}

