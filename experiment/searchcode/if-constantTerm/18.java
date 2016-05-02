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

package org.nextreamlabs.simplex.model;

import org.nextreamlabs.simplex.model.component.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * A linear model
 * @link http://en.wikipedia.org/wiki/Linear_model
 */
public interface LinearModel {

  // { Getters and Setters

  public List<Variable> getObjfuncVariables();
  public ImmutableLinearObjfunc getObjfunc();
  public void setObjfunc(ImmutableLinearObjfunc objfunc);

  public Integer getConstraintsSize();
  public List<ImmutableLinearConstraint> getConstraints();
  public ImmutableLinearConstraint getConstraint(Integer index);
  public void setConstraints(List<ImmutableLinearConstraint> constraints);
  public Boolean setConstraint(Integer index, ImmutableLinearConstraint constraint);

  public Objective getObjective();
  public void setObjective(Objective objective);

  public Boolean hasSolution();
  public ImmutableSolution getCurrentSolution();
  public List<ImmutableSolution> getSolutionHistory();
  public void setSolutionHistory(List<ImmutableSolution> solutionHistory);

  // }

  public Boolean addSolution(ImmutableSolution solution);

  public Boolean isValid();

  // { Operations to manipulate the model
  public Boolean removeVariable(Integer id);
  public Boolean addConstraint(Integer constraintIndex, List<Variable> vars, ConstraintSign sign,
                               BigDecimal constantTerm);
  public Boolean removeConstraint(Integer constraintIndex);
  public Boolean addVariable(Integer id, List<BigDecimal> coefficients, Boolean augmented);
  public Boolean addVariable(List<Variable> vars);
  public Boolean setConstraintSign(Integer constraintIndex, ConstraintSign sign);
  public Boolean setObjfuncVariable(Integer id, BigDecimal value);
  public Boolean setConstraintConstantTerm(Integer constraintIndex, BigDecimal constantTerm);
  public Boolean setConstraintVariable(Integer constraintIndex, Integer id, BigDecimal coefficient);

  public Boolean resetSolutionHistory();

  // }

  public LinearModel duplicate(List<ImmutableLinearConstraint> constraints,
                               ImmutableLinearObjfunc objfunc, Objective objective,
                               List<ImmutableSolution> solutionHistory);
}

