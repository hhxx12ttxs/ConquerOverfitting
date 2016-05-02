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

package org.nextreamlabs.simplex.solver.pivot_solver;

import org.apache.commons.lang3.mutable.MutableInt;
import org.nextreamlabs.simplex.model.LinearModel;
import org.nextreamlabs.simplex.model.StandardLinearModel;
import org.nextreamlabs.simplex.model.component.*;
import org.nextreamlabs.simplex.solver.Solver;
import org.nextreamlabs.simplex.solver.SolverEventListener;
import org.nextreamlabs.simplex.solver.SolverEventSource;
import org.nextreamlabs.simplex.ui.command.SetStatusText;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Solver implementation using the pivot algorithm
 * It's a facade for the problem model
 * It also exposes the methods to optimize the model
 */
public final class PivotSolver implements Solver {
  private static final Logger logger;

  private PivotSolverStepHelper solverStepHelper;
  private final LinearModel originalModel;
  private LinearModel steppingModel;

  private final Set<SolverEventListener> listeners;
  private final MutableInt stepNumber;

  static {
    logger = Logger.getLogger("org.nextreamlabs.PivotSolver");
  }

  // { Constructors and Factories

  private PivotSolver() {
    this.originalModel = StandardLinearModel.create(new ArrayList<ImmutableLinearConstraint>(),
        DefaultImmutableLinearObjfunc.create(), Objective.MIN,
        new ArrayList<ImmutableSolution>());
    this.steppingModel = this.originalModel.duplicate(null, null, null, null);
    this.solverStepHelper = DefaultPivotSolverStepHelper.create(this.steppingModel);
    this.stepNumber = new MutableInt(0);

    this.listeners = new HashSet<SolverEventListener>();
  }

  public static Solver create() {
    return new PivotSolver();
  }

  // }

  // { Solver implementation

  /**
   * Request a variable removal
   * As side-effect it clears the solutions history
   * @param id The id of the variable to be removed
   * @return Success or failure
   */
  @Override
  public Boolean removeVariable(Integer id) {
    this.originalModel.resetSolutionHistory();
    Boolean status = this.originalModel.removeVariable(id);
    this.reinitializeStepping();

    String msg = "Model Remove [var_id=" + id + "]";
    this.notifyStatus((status ? "[SUCCESS] " : "[FAIL] ") + msg);
    return status;
  }

  /**
   * Add the constraint into the model
   * As side-effect it clears the solutions history
   * @param constraintIndex The constraint index
   * @param vars Non-augmented variables for the constraint.
   *             The order in the vars list, represents the creation order and the ids order
   * @param sign The constraint sign
   * @param constantTerm The constraint constant-term
   * @return The constraint has been added into the model or some errors are occurred
   */
  @Override
  public Boolean addConstraint(Integer constraintIndex, List<BigDecimal> vars,
                               ConstraintSign sign, BigDecimal constantTerm) {
    // { Convert the vars into a form comprehensible by the model
    List<Variable> varsForModel = new ArrayList<Variable>(vars.size());
    for (Integer i = 0; i < vars.size(); i++) {
      varsForModel.add(SmartVariable.create(i, vars.get(i), false));
    }
    // }
    this.originalModel.resetSolutionHistory();
    Boolean status = this.originalModel.addConstraint(constraintIndex, varsForModel, sign,
        constantTerm);
    this.reinitializeStepping();

    String msg = "Model Add [constraint_idx=" + constraintIndex + "]";
    this.notifyStatus((status ? "[SUCCESS] " : "[FAIL] ") + msg);
    return status;
  }

  /**
   * Remove a constraint from the model
   * As side-effect it clears the solutions history
   * @param constraintIndex The index for the constraint to be removed from the model
   * @return Success or failure
   */
  @Override
  public Boolean removeConstraint(Integer constraintIndex) {
    this.originalModel.resetSolutionHistory();
    Boolean status = this.originalModel.removeConstraint(constraintIndex);
    this.reinitializeStepping();

    String msg = "Model Remove [constraint_idx=" + constraintIndex + "]";
    this.notifyStatus((status ? "[SUCCESS] " : "[FAIL] ") + msg);
    return status;
  }

  /**
   * Add the specified variable into the model
   * As side-effect it clears the solutions history
   * @param id
   * @param coefficients
   * @return Success or failure
   */
  @Override
  public Boolean addVariable(Integer id, List<BigDecimal> coefficients) {
    this.originalModel.resetSolutionHistory();
    Boolean status = this.originalModel.addVariable(id, coefficients, false);
    this.reinitializeStepping();

    String msg = "Model Add [var_id=" + id + "]";
    this.notifyStatus((status ? "[SUCCESS] " : "[FAIL] ") + msg);
    return status;
  }

  /**
   * Set a variable for the objective-function
   * As side-effect it clears the solutions history
   * @param id The variable id to be changed
   * @param coefficient The new coefficient for the variable
   * @return Success or failure
   */
  @Override
  public Boolean setObjfuncVariable(Integer id, BigDecimal coefficient) {
    this.originalModel.resetSolutionHistory();
    Boolean status = this.originalModel.setObjfuncVariable(id, coefficient);
    this.reinitializeStepping();

    String msg = "Model Change [objective-function][var_id=" + id + "][coeff=" + coefficient + "]";
    this.notifyStatus((status ? "[SUCCESS] " : "[FAIL] ") + msg);
    return status;
  }

  /**
   * Set a variable for the specified constraint
   * As side-effect it clears the solutions history
   * @param constraintIndex The index of the constraint to be changed
   * @param id The variable id to be changed
   * @param coefficient The new coefficient for the variable
   * @return Success or failure
   */
  @Override
  public Boolean setConstraintVariable(Integer constraintIndex, Integer id,
                                       BigDecimal coefficient) {
    this.originalModel.resetSolutionHistory();
    Boolean status = this.originalModel.setConstraintVariable(constraintIndex, id, coefficient);
    this.reinitializeStepping();

    String msg = "Model Change [constraint_idx=" + constraintIndex + "][var_id=" + id + "]" +
        "[coeff=" + coefficient + "]";
    this.notifyStatus((status ? "[SUCCESS] " : "[FAIL] ") + msg);
    return status;
  }

  /**
   * Set the sign for the specified constraint
   * As side-effect it clears the solutions history
   * @param constraintIndex The index of the constraint to be changed
   * @param sign The new sign
   * @return Success or failure
   */
  @Override
  public Boolean setConstraintSign(Integer constraintIndex, ConstraintSign sign) {
    this.originalModel.resetSolutionHistory();
    Boolean status = this.originalModel.setConstraintSign(constraintIndex, sign);
    this.reinitializeStepping();

    String msg = "Model Change [constraint_idx=" + constraintIndex + "][sign=" + sign + "]";
    this.notifyStatus((status ? "[SUCCESS] " : "[FAIL] ") + msg);
    return status;
  }

  /**
   * Set the constant-term for the specified constraint
   * As side-effect it clears the solutions history
   * @param constraintIndex The index of the constraint to be changed
   * @param constantTerm The new constant-term for the constraint
   * @return Success or failure
   */
  @Override
  public Boolean setConstraintConstantTerm(Integer constraintIndex, BigDecimal constantTerm) {
    this.originalModel.resetSolutionHistory();
    Boolean status = this.originalModel.setConstraintConstantTerm(constraintIndex, constantTerm);
    this.reinitializeStepping();

    String msg = "Model Change [constraint_idx=" + constraintIndex + "][constant-term="
        + constantTerm + "]";
    this.notifyStatus((status ? "[SUCCESS] " : "[FAIL] ") + msg);
    return status;
  }

  /**
   * Perform a step
   * @return True if the step run successfully, otherwise false
   */
  @Override
  public Boolean doStep() {
    this.incrementStepNumber();
    this.modelLogging("The step " + this.stepNumber + " is starting.\n" +
        "The model is:\n", this.steppingModel);
    this.notifyStatus("Step " + this.stepNumber + " started");

    // ==> Convert the model into a standard linear model if it isn't valid
    if (!this.steppingModel.isValid() && !this.solverStepHelper.fixModel()) {
      this.notifyStatusAndLog("Cannot fix the input model");
      return Boolean.FALSE;
    }

    // ==> If the solution is already optimal don't do nothing
    if (this.solverStepHelper.isSolutionOptimal()) {
      this.notifyStatusAndLog("The found solution is optimal. The solution is: " +
          this.steppingModel.getCurrentSolution().getNonAugmentedSolution());
      return Boolean.TRUE;
    }

    if (this.solverStepHelper.checkInitialSolution()) {
      // ==> There is an initial solution => compute the current step for the current model
      try {
        Integer[] pivot = this.solverStepHelper.findPivot();
        this.solverStepHelper.doPivot(pivot[0], pivot[1]);
        logger.info("Completed the pivot step.\nThe model is:\n" + this.steppingModel.toString());

        if (this.solverStepHelper.isSolutionOptimal()) {
          // ==> The step computation returned an optimal solution
          logger.info("The found solution is optimal");
          this.notifyStatus("Completed the pivot step. The found solution is optimal: " +
              this.steppingModel.getCurrentSolution().getNonAugmentedSolution());
        } else {
          // ==> The step computation returned a non-optimal solution
          logger.info("The found solution isn't optimal. More steps are needed.");
          this.notifyStatus("Completed the pivot step. The found solution isn't optimal. " +
              "More steps are needed.");
        }
      } catch (UnlimitedProblemException exc) {
        this.notifyStatusAndLog("The problem is unlimited");
      }
    } else {
      // ==> There isn't a initial solution => create and compute a new simplex to get the initial
      //     solution
      try {
        this.solverStepHelper.findInitialSolution();
        this.notifyStatusAndLog("Found the initial solution:\n" +
            this.steppingModel.getCurrentSolution().toString());
      } catch (UnlimitedProblemException exc) {
        this.notifyStatusAndLog("The problem is unlimited");
      }
    }

    return Boolean.TRUE;
  }

  @Override
  public String getName() {
    return "PivotSolver";
  }

  // }

  // { Listeners management

  @Override
  public void register(SolverEventListener listener) {
    this.listeners.add(listener);
    ((SolverEventSource) this.solverStepHelper).register(listener);
  }

  // }

  // { Listeners notifiers

  private void notifyStatus(String status) {
    for (SolverEventListener listener : this.listeners) {
      listener.commandRequestedForUi(new SetStatusText(status));
    }
  }

  // }

  // { Utilities for DRY

  private void reinitializeStepping() {
    this.steppingModel = this.originalModel.duplicate(null, null, null, null);
    this.solverStepHelper = DefaultPivotSolverStepHelper.create(this.steppingModel);
    for (SolverEventListener listener : this.listeners) {
      ((SolverEventSource) this.solverStepHelper).register(listener);
    }
    this.resetStepNumber();
  }

  private void modelLogging(String string, LinearModel model) {
    logger.info(string + model.toString());
  }

  private void resetStepNumber() { this.stepNumber.setValue(0); }
  private void incrementStepNumber() { this.stepNumber.increment(); }

  private void notifyStatusAndLog(String status) {
    logger.info(status);
    this.notifyStatus(status);
  }

  // }
}

