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

import org.nextreamlabs.simplex.model.BigDecimalUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents an immutable linear constraint
 */
public class DefaultImmutableLinearConstraint implements ImmutableLinearConstraint {
  private final List<Variable> vars;
  private final ConstraintSign sign;
  private final BigDecimal constantTerm;

  // { Constructors and Factories

  private DefaultImmutableLinearConstraint(Collection<Variable> vars, ConstraintSign sign,
                                           BigDecimal constantTerm) {
    // { Preconditions
    assert (vars != null) : "The vars cannot be null";
    assert (sign != null) : "The sign cannot be null";
    assert (constantTerm != null) : "The constant-term cannot be null";
    // }

    this.sign = sign;
    this.constantTerm = constantTerm;
    this.vars = new ArrayList<Variable>(vars.size());

    for (Variable var : vars) {
      this.vars.add(var);
    }
  }

  /**
   * Create a new ImmutableLinearConstraint
   * @param vars The variables
   * @param sign The constraint sign
   * @param constantTerm The constant-term
   * @return The created ImmutableLinearConstraint
   */
  public static ImmutableLinearConstraint create(Collection<Variable> vars, ConstraintSign sign,
                                         BigDecimal constantTerm) {
    return new DefaultImmutableLinearConstraint(vars, sign, constantTerm);
  }

  // }

  // { Getters and Setters

  @Override
  public ConstraintSign getSign() { return this.sign; }
  @Override
  public Integer getSize() { return this.vars.size(); }
  @Override
  public BigDecimal getConstantTerm() { return this.constantTerm; }
  @Override
  public Variable getVariable(Integer id) { return this.vars.get(id); }
  @Override
  public List<Variable> getVariables() { return new ArrayList<Variable>(this.vars); }

  // }

  /**
   * Duplicate the current object
   * @param vars If not null, the vars for the new ImmutableLinearConstraint
   * @param sign If not null, the sign for the new ImmutableLinearConstraint
   * @param constantTerm If not null, the constantTerm for the new ImmutableLinearConstraint
   * @return The created DefaultImmutableLinearConstraint
   */
  @Override
  public ImmutableLinearConstraint duplicate(List<Variable> vars, ConstraintSign sign,
                                    BigDecimal constantTerm) {
    return DefaultImmutableLinearConstraint.create(
        ((vars == null) ? this.vars : vars),
        ((sign == null) ? this.sign : sign),
        ((constantTerm == null) ? this.constantTerm : constantTerm));
  }

  /**
   * Find and return the variable with the id passed as argument
   * @param id The id to be found
   * @return The found variable or null if no variables have the id
   */
  @Override
  public Variable findVariableById(Integer id) {
    for (Variable var : this.vars) {
      if (var.getId().equals(id)) {
        return var;
      }
    }
    return null;
  }

  /**
   * Multiply the current constraint variables coefficients and the constant-term by the provided
   * scalar
   * @param scalar Value to be multiplied
   * @return The new ImmutableLinearConstraint as the result of the multiplication
   */
  @Override
  public ImmutableLinearConstraint multiply(BigDecimal scalar) {
    // { Preconditions
    assert (scalar != null) : "The scalar cannot be null";
    // }

    List<Variable> resultVars = new ArrayList<Variable>();
    for (Variable var : this.vars) {
      resultVars.add(
          SmartVariable.createFromExistingId(var.getId(), var.getCoefficient().multiply(scalar)));
    }

    return DefaultImmutableLinearConstraint.create(resultVars,
        (scalar.compareTo(BigDecimal.ZERO) == -1) ? this.sign.invert() : this.sign,
        this.constantTerm.multiply(scalar));
  }

  /**
   * Sum the current constraint with the provided one
   * @param constraint Constraint to be summed with the current one
   * @return The result of the sum
   */
  @Override
  public ImmutableLinearConstraint sum(ImmutableLinearConstraint constraint) {
    // { Preconditions
    assert constraint.getSize().equals(this.getSize()) : "Invalid constraint size. It must be " +
        "the same to the receiver size";
    for (Integer i = 0; i < this.getSize(); i++) { // Both constraints must have the "same" variables
      Variable curVarByIdx = this.vars.get(i);
      Variable tgtVarById = constraint.findVariableById(curVarByIdx.getId());
      Variable tgtVarByIdx = constraint.getVariable(i);
      Variable curVarById = this.findVariableById(tgtVarByIdx.getId());
      assert tgtVarById != null : "Variable " + curVarByIdx.getId() +
          " isn't available in the constraint passed as argument";
      assert curVarById != null : "Variable " + tgtVarByIdx.getId() +
          " isn't available in the current constraint";
      assert curVarByIdx.isAugmented().equals(tgtVarById.isAugmented()) :
          "Inconsistent variables between constraints";
    }
    // }

    // { Compute the sum between the current constraint and the one passed as argument
    List<Variable> vars = new ArrayList<Variable>(this.vars.size());
    for (Variable var : this.vars) {
      BigDecimal coefficient = var.getCoefficient();
      vars.add(SmartVariable.createFromExistingId(var.getId(),
          coefficient.add(constraint.findVariableById(var.getId()).getCoefficient())));
    }
    // }

    return DefaultImmutableLinearConstraint.create(vars, this.sign,
        this.constantTerm.add(constraint.getConstantTerm()));
  }

  /**
   * Add the provided variable in the current constraint
   * @param var Variable to be added
   * @return The result of the addition of the provided variable into the current constraint
   */
  @Override
  public ImmutableLinearConstraint addVariable(Variable var) {
    List<Variable> vars = new ArrayList<Variable>(this.vars);
    vars.add(var);
    return DefaultImmutableLinearConstraint.create(vars, this.sign, this.constantTerm);
  }

  /**
   * Remove a variable from the current constraint
   * @param id The id of the variable to be removed
   * @return The result of the removal
   */
  @Override
  public ImmutableLinearConstraint removeVariable(Integer id) {
    Variable var = this.findVariableById(id);
    List<Variable> vars = new ArrayList<Variable>(this.vars);
    if (var != null) {
      vars.remove(var);
    }
    return DefaultImmutableLinearConstraint.create(vars, this.sign, this.constantTerm);
  }

  /**
   * Change the coefficient of a variable
   * @param id The id of the variable to be changed
   * @param coefficient The new coefficient for the variable
   * @return The result of the changement
   */
  @Override
  public ImmutableLinearConstraint setVariable(Integer id, BigDecimal coefficient) {
    // { Preconditions
    Boolean found = Boolean.FALSE;
    for (Variable var : this.vars) {
      if (var.getId().equals(id)) {
        found = Boolean.TRUE;
      }
    }
    assert found : "Wrong id argument";
    // }

    List<Variable> vars = new ArrayList<Variable>(this.vars);
    for (Integer i = 0; i < vars.size(); i++) {
      if (vars.get(i).getId().equals(id)) {
        vars.set(i, SmartVariable.createFromExistingId(id, coefficient));
      }
    }
    return DefaultImmutableLinearConstraint.create(vars, this.sign, this.constantTerm);
  }

  /**
   * Change the sign of the current constraint
   * @param sign The new sign for the current constraint
   * @return The result of the changement
   */
  @Override
  public ImmutableLinearConstraint setSign(ConstraintSign sign) {
    return DefaultImmutableLinearConstraint.create(new ArrayList<Variable>(this.vars), sign,
        this.constantTerm);
  }

  /**
   * Change the constant term of the current constraint
   * @param constantTerm The new constant-term for the current constraint
   * @return The result of the changement
   */
  @Override
  public ImmutableLinearConstraint setConstantTerm(BigDecimal constantTerm) {
    return DefaultImmutableLinearConstraint.create(new ArrayList<Variable>(this.vars), this.sign,
        constantTerm);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (Integer i = 0; i < this.vars.size(); i++) {
      sb.append(this.vars.get(i).toString());
      if (!i.equals(this.vars.size() - 1)) {
        sb.append(" + ");
      }
    }
    sb.append(" ").append(this.sign.toString()).append(" ");
    sb.append(BigDecimalUtil.toPrettyString(this.constantTerm));

    return sb.toString();
  }

}

