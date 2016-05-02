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
import org.nextreamlabs.simplex.model.converter.CannotConvertModelException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a general linear model
 */
@SuppressWarnings("ClassWithTooManyMethods") // It's a facade, Demeter wants too many methods
public class GeneralLinearModel implements LinearModel {
  protected static final Logger logger;

  protected List<ImmutableLinearConstraint> constraints;
  protected ImmutableLinearObjfunc objfunc;
  protected Objective objective;
  protected List<ImmutableSolution> solutionHistory;

  static {
    logger = Logger.getLogger("org.nextreamlabs.simplex.GeneralLinearModel");
  }

  // { Constructors and Factories

  protected GeneralLinearModel(List<ImmutableLinearConstraint> constraints,
                               ImmutableLinearObjfunc objfunc, Objective objective,
                               List<ImmutableSolution> solutionHistory) {
    if (constraints == null || objfunc == null || objective == null || solutionHistory == null) {
      throw new IllegalArgumentException("No null arguments are allowed");
    }

    this.constraints = new ArrayList<ImmutableLinearConstraint>(constraints);
    this.objfunc = objfunc;
    this.objective = objective;
    this.solutionHistory = new ArrayList<ImmutableSolution>(solutionHistory);
  }

  public static LinearModel create(List<ImmutableLinearConstraint> constraints,
                                   ImmutableLinearObjfunc objfunc, Objective objective,
                                   List<ImmutableSolution> solutionHistory) {
    return new GeneralLinearModel(constraints, objfunc, objective, solutionHistory);
  }

  /**
   * Convert the linear model passed as argument into a general linear model
   * @param model The model to be converted
   * @return The new model as general linear model equivalent to the model argument
   */
  public static LinearModel create(LinearModel model) {
    return GeneralLinearModel.create(model.getConstraints(), model.getObjfunc(),
        model.getObjective(), model.getSolutionHistory());
  }

  // }

  @Override
  public LinearModel duplicate(List<ImmutableLinearConstraint> constraints,
                               ImmutableLinearObjfunc objfunc, Objective objective,
                               List<ImmutableSolution> solutionHistory) {
    return GeneralLinearModel.create(
        new ArrayList<ImmutableLinearConstraint>(
            (constraints == null) ? this.constraints : constraints),
        (objfunc == null) ? this.objfunc : objfunc,
        (objective == null) ? this.objective : objective,
        new ArrayList<ImmutableSolution>(
            (solutionHistory == null) ? this.solutionHistory : solutionHistory));
  }

  // { Getters and Setters

  @Override
  public List<Variable> getObjfuncVariables() { return this.objfunc.getVariables(); }
  @Override
  public ImmutableLinearObjfunc getObjfunc() { return this.objfunc; }
  @Override
  public void setObjfunc(ImmutableLinearObjfunc objfunc) { this.objfunc = objfunc; }

  @Override
  public Integer getConstraintsSize() { return this.constraints.size(); }
  @Override
  public List<ImmutableLinearConstraint> getConstraints() { return this.constraints; }
  @Override
  public ImmutableLinearConstraint getConstraint(Integer index) {
    return this.constraints.get(index);
  }
  @Override
  public void setConstraints(List<ImmutableLinearConstraint> constraints) {
    this.constraints = new ArrayList<ImmutableLinearConstraint>(constraints);
  }
  @Override
  public Boolean setConstraint(Integer index, ImmutableLinearConstraint constraint) {
    if (index >= this.constraints.size()) {
      return Boolean.FALSE;
    }
    this.constraints.set(index, constraint);
    return Boolean.TRUE;
  }

  @Override
  public Objective getObjective() { return this.objective; }
  @Override
  public void setObjective(Objective objective) { this.objective = objective; }

  @Override
  public Boolean hasSolution() { return this.getCurrentSolution() == null; }
  /**
   * @return The current solution or null if there isn't one
   */
  @Override
  public ImmutableSolution getCurrentSolution() {
    if (this.solutionHistory.size() > 0) {
      return this.solutionHistory.get(this.solutionHistory.size() - 1);
    } else {
      return null;
    }
  }
  @Override
  public List<ImmutableSolution> getSolutionHistory() { return solutionHistory; }
  @Override
  public void setSolutionHistory(List<ImmutableSolution> solutionHistory) {
    this.solutionHistory = new ArrayList<ImmutableSolution>(solutionHistory);
  }

  // }

  // { Model manipulations

  /**
   * Remove a variable from the model
   * @param id The id of the variable to be removed
   * @return Success or failure
   */
  @Override
  public Boolean removeVariable(Integer id) {
    // ==> Remove variable in the constraints
    for (Integer i = 0; i < this.constraints.size(); i++) {
      ImmutableLinearConstraint constraint = this.constraints.get(i).removeVariable(id);
      if (constraint == null) {
        return Boolean.FALSE;
      }
      this.constraints.set(i, constraint);
    }

    // ==> Remove variable in the objective-function
    ImmutableLinearObjfunc objfunc = this.objfunc.removeVariable(id);
    if (objfunc == null) {
      return Boolean.FALSE;
    }
    this.objfunc = objfunc;

    // ==> Remove variable in the solution history
    if (this.getCurrentSolution() != null) {
      this.solutionHistory.set(this.solutionHistory.size() - 1,
          this.getCurrentSolution().removeVariable(id, objfunc));
    }

    return Boolean.TRUE;
  }

  /**
   * Add a new constraint
   * @param constraintIndex Index for the constraint
   * @param vars Variables for the constraint
   * @param sign Sign for the constraint
   * @param constantTerm Constant-term for the constraint
   * @return Success or failure
   */
  @Override
  public Boolean addConstraint(Integer constraintIndex, List<Variable> vars, ConstraintSign sign,
                               BigDecimal constantTerm) {
    // { Preconditions
    assert (constraintIndex != null) : "The constraintIndex cannot be null";
    assert (vars != null) : "The vars cannot be null";
    assert (sign != null) : "The sign cannot be null";
    assert (constantTerm != null) : "The constantTerm cannot be null";
    // }

    try {
      this.constraints.add(constraintIndex,
          DefaultImmutableLinearConstraint.create(vars, sign, constantTerm));
    } catch (Exception exc) {
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

  /**
   * Remove the specified constraint
   * @param constraintIndex Index for the constraint to be removed
   * @return Success or failure
   */
  @Override
  public Boolean removeConstraint(Integer constraintIndex) {
    // { Preconditions
    assert (constraintIndex != null): "The constraintIndex cannot be null";
    // }

    try {
      this.constraints.remove(constraintIndex.intValue());
    } catch (Exception exc) {
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

  /**
   * Add a new Variable to the constraints and the objective-function
   * @param id The id for the new Variable
   * @param coefficients The first element is the coefficient for the new Variable in the
   *                     objective-function. The others are the coefficients for the constraints
   * @param augmented The new Variable should be augmented?
   * @return Success or failure
   */
  @Override
  public Boolean addVariable(Integer id, List<BigDecimal> coefficients, Boolean augmented) {
    // { Preconditions
    assert (id != null): "The id cannot be null";
    assert (coefficients != null): "The coefficients cannot be null";
    assert (augmented != null): "The augmented cannot be null";

    assert (this.constraints.size() == (coefficients.size() -1)):
        "Wrong number of elements in coefficients";
    // }

    this.objfunc = this.objfunc.addVariable(
        SmartVariable.create(id, coefficients.get(0), augmented));

    for (Integer i = 1; i < coefficients.size(); i++) {
      this.constraints.set(i-1, this.constraints.get(i-1).addVariable(
          SmartVariable.createFromExistingId(id, coefficients.get(i))));
    }

    return Boolean.TRUE;
  }

  /**
   * Add a new Variable to the constraints and the objective-function
   * @param vars The variables
   * @return Success or failure
   */
  @Override
  public Boolean addVariable(List<Variable> vars) {
    // { Preconditions
    assert (this.constraints.size() == (vars.size() -1)):
        "Wrong number of elements in coefficients";
    // }

    this.objfunc = this.objfunc.addVariable(vars.get(0));

    for (int i = 0; i < this.constraints.size(); i++) {
      this.constraints.set(i, this.constraints.get(i).addVariable(vars.get(i+1)));
    }

    return Boolean.TRUE;
  }

  /**
   * Set a variable for the objective-function
   * @param id The variable id to be changed
   * @param coefficient The new coefficient for the variable
   * @return Success or failure
   */
  @Override
  public Boolean setObjfuncVariable(Integer id, BigDecimal coefficient) {
    if (this.objfunc.hasVariableWithId(id)) {
      this.objfunc = this.objfunc.setVariable(id, coefficient);
    } else {
      this.objfunc = this.objfunc.addVariable(SmartVariable.create(id, coefficient, false));
    }

    return Boolean.TRUE;
  }

  /**
   * Set a variable for the specified constraint
   * @param constraintIndex The index of the constraint to be changed
   * @param id The variable id to be changed
   * @param coefficient The new coefficient for the variable
   * @return Success or failure
   */
  @Override
  public Boolean setConstraintVariable(Integer constraintIndex, Integer id,
                                       BigDecimal coefficient) {
    this.constraints.set(constraintIndex,
        this.constraints.get(constraintIndex).setVariable(id, coefficient));

    return Boolean.TRUE;
  }

  /**
   * Set the sign for the specified constraint
   * @param constraintIndex The index of the constraint to be changed
   * @param sign The new sign
   * @return Success or failure
   */
  @Override
  public Boolean setConstraintSign(Integer constraintIndex, ConstraintSign sign) {
    this.constraints.set(constraintIndex, this.constraints.get(constraintIndex).setSign(sign));

    return Boolean.TRUE;
  }

  /**
   * Set the constant-term for the specified constraint
   * @param constraintIndex The index of the constraint to be changed
   * @param constantTerm The new constant-term for the constraint
   * @return Success or failure
   */
  @Override
  public Boolean setConstraintConstantTerm(Integer constraintIndex, BigDecimal constantTerm) {
    this.constraints.set(constraintIndex,
        this.constraints.get(constraintIndex).setConstantTerm(constantTerm));

    return Boolean.TRUE;
  }

  /**
   * Clear the solution history
   * @return Success or failure
   */
  @Override
  public Boolean resetSolutionHistory() {
    this.solutionHistory = new ArrayList<ImmutableSolution>(this.solutionHistory.size());
    return Boolean.TRUE;
  }

  /**
   * Add a solution to the solution history
   * @return Success or failure
   */
  @Override
  public Boolean addSolution(ImmutableSolution solution) {
    this.solutionHistory.add(solution);
    return Boolean.TRUE;
  }
  // }

  /**
   * A general linear model is always valid
   * @return true
   */
  @Override
  public Boolean isValid() {
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("Objective: ").append(this.objective.toString()).append("\n");
    sb.append("Objective-Function: ").append(this.objfunc.toString()).append("\n");
    sb.append("Constraints: ").append("\n");
    for (ImmutableLinearConstraint constraint : this.constraints) {
      sb.append(constraint.toString()).append("\n");
    }
    ImmutableSolution solution = getCurrentSolution();
    sb.append("Solution: ").append((solution == null) ? "none" : solution.toString()).append("\n");

    return sb.toString();
  }

}

