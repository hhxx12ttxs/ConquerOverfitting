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

package org.nextreamlabs.simplex.ui.gui;

import org.nextreamlabs.simplex.model.component.ConstraintSign;
import org.nextreamlabs.simplex.solver.command.*;
import org.nextreamlabs.simplex.ui.UiEventListener;
import org.nextreamlabs.simplex.ui.UiEventSource;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a collection of constraints with an objective function associated to them
 */
public class ModelPanel extends JPanel implements UiEventSource {

  // { default values
  private static final BigDecimal defVarValue;
  private static final BigDecimal defConstantTermValue;
  private static final ConstraintSign defSign;
  // }

  // { components
  private final List<List<Component>> constraintComponents;
  private final List<JPanel> constraintPanels;
  private final List<BigDecimalTextField> objfuncComponents;
  private final JPanel objfuncPanel;
  // }

  // listeners
  private final Collection<UiEventListener> listeners;

  private final JFrame parent;

  static {
    defVarValue = BigDecimal.ZERO;
    defConstantTermValue = BigDecimal.ZERO;
    defSign = ConstraintSign.Equal;
  }

  // { Constructors and Factories

  /**
   * Create a new ModelPanel
   */
  private ModelPanel(JFrame parent) {
    super();

    // Initialize the 'components'
    this.constraintComponents = new ArrayList<List<Component>>();
    this.objfuncComponents = new ArrayList<BigDecimalTextField>();
    // }

    // { Initialize the 'panels'
    this.constraintPanels = new ArrayList<JPanel>();
    this.objfuncPanel = new JPanel();
    // }

    this.listeners = new ArrayList<UiEventListener>();
    this.parent = parent;
  }

  /**
   * Create a new ModelPanel
   *
   * @return The new ModelPane object created
   */
  public static ModelPanel create(JFrame parent) {
    return new ModelPanel(parent);
  }

  // }

  /**
   * Initialize the current ModelPanel
   *
   * @param constraintCount The number of constraints
   * @param varCount The number of variables for each constraint
   */
  public void initialize(Integer constraintCount, Integer varCount) {
    // { Initialization for default fields values
    List<List<BigDecimalTextField>> constraintsVars = new ArrayList<List<BigDecimalTextField>>();
    List<BigDecimalTextField> constraintsConstantTerm = new ArrayList<BigDecimalTextField>();
    List<BigDecimalTextField> objfuncVars = new ArrayList<BigDecimalTextField>();

    for (Integer i = 0; i < varCount; i++) {
      objfuncVars.add(new BigDecimalTextField(defVarValue));
    }

    for (Integer i = 0; i < constraintCount; i++) {
      constraintsVars.add(new ArrayList<BigDecimalTextField>());
      for (int j = 0; j < varCount; j++) {
        constraintsVars.get(i).add(new BigDecimalTextField(defVarValue));
      }
      constraintsConstantTerm.add(new BigDecimalTextField(defConstantTermValue));
    }
    // }

    // INF: Before anything, setup BigDecimalTextFields
    for (Integer i = 0; i < constraintsVars.size(); i++) {

      for (Integer j = 0; j < constraintsVars.get(i).size(); j++) {
        // { Notify listeners for: constraint var changed
        // Insert into the current variable's document the variable as the document owner,
        // because the document owner is needed by the DocumentListener's methods
        constraintsVars.get(i).get(j).getDocument()
            .putProperty("owner", constraintsVars.get(i).get(j));
        final int k = i, w = j; // declared as final because used into the inner class
        constraintsVars.get(i).get(j).getDocument().addDocumentListener(new DocumentListener() {
          @Override
          public void changedUpdate(DocumentEvent e) {}

          @Override
          public void removeUpdate(DocumentEvent e) {
            BigDecimalTextField owner = (BigDecimalTextField) e.getDocument().getProperty("owner");
            try {
              notifyConstraintVarChange(k, w, new BigDecimal(owner.getText()));
            } catch (NumberFormatException ignored) {}
            pack();
          }

          @Override
          public void insertUpdate(DocumentEvent e) {
            BigDecimalTextField owner = (BigDecimalTextField) e.getDocument().getProperty("owner");
            try {
              notifyConstraintVarChange(k, w, new BigDecimal(owner.getText()));
            } catch (NumberFormatException ignored) {}
            pack();
          }
        });
        // }
      }

      // { Notify listeners for: constraint constant term changed
      // Insert into the current constant-term's document the constant-term as the document owner,
      // because the document owner is needed by the DocumentListener's methods
      constraintsConstantTerm.get(i).getDocument()
          .putProperty("owner", constraintsConstantTerm.get(i));
      final int k = i; // declared as final because used into the inner class
      constraintsConstantTerm.get(i).getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void changedUpdate(DocumentEvent e) {}

        @Override
        public void removeUpdate(DocumentEvent e) {
          BigDecimalTextField owner = (BigDecimalTextField) e.getDocument().getProperty("owner");
          try {
            notifyConstraintConstantTermChange(k, new BigDecimal(owner.getText()));
          } catch (NumberFormatException ignored) {}
          pack();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
          BigDecimalTextField owner = (BigDecimalTextField) e.getDocument().getProperty("owner");
          try {
            notifyConstraintConstantTermChange(k, new BigDecimal(owner.getText()));
          } catch (NumberFormatException ignored) {}
          pack();
        }
      });
      // }
    }

    for (int i = 0; i < objfuncVars.size(); i++) {
      // { Notify listeners for: constraint var changed
      // Insert into the current objective-function's document the objective-function as the
      // document owner, because the document owner is needed by the DocumentListener's methods
      objfuncVars.get(i).getDocument().putProperty("owner", objfuncVars.get(i));
      final int k = i; // declared as final because used into the inner class
      objfuncVars.get(i).getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void changedUpdate(DocumentEvent e) {}

        @Override
        public void removeUpdate(DocumentEvent e) {
          BigDecimalTextField owner = (BigDecimalTextField) e.getDocument().getProperty("owner");
          try {
            notifyObjfuncVarChange(k, new BigDecimal(owner.getText()));
          } catch (NumberFormatException ignored) {}
          pack();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
          BigDecimalTextField owner = (BigDecimalTextField) e.getDocument().getProperty("owner");
          try {
            notifyObjfuncVarChange(k, new BigDecimal(owner.getText()));
          } catch (NumberFormatException ignored) {}
          pack();
        }
      });
      // }
    }

    this.setLayout(new GridBagLayout());

    this.addObjfunc(objfuncVars);
    // { Notify listeners for: objective-function added
    for (Integer i = 0; i < objfuncVars.size(); i++) {
      this.notifyObjfuncVarChange(i, objfuncVars.get(i).getValue());
    }
    // }

    for (int i = 0; i < constraintsVars.size(); i++) {
      // Add a panel for the current constraint
      this.addConstraintPanel();

      this.addConstraint(constraintsVars.get(i), defSign, constraintsConstantTerm.get(i));

      // { Notify listeners for: constraint added
      List<BigDecimal> constraintCoefficients = new ArrayList<BigDecimal>();
      for (BigDecimalTextField f : constraintsVars.get(i)) {
        constraintCoefficients.add(new BigDecimal(f.getText()));
      }
      this.notifyConstraintAdd(i, constraintCoefficients,
          new BigDecimal(constraintsConstantTerm.get(i).getText()), defSign);
      // }
    }

    this.updateGui();

  }

  // { Visualization-related operations

  /**
   * Adjust the current model-panel
   */
  private void pack() {
    this.setSize(this.getPreferredSize());
    this.parent.pack();
  }

  /**
   * Request a refresh for the current ModelPanel
   */
  public void updateGui() {
    // { Remove all components from the ModelPanel
    for (JPanel constraintPanel : this.constraintPanels) {
      constraintPanel.removeAll();
    }
    this.objfuncPanel.removeAll();
    this.removeAll();
    // }

    // { GUI part for the objective-function
    GuiUtil.addComponent(this, objfuncPanel, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
        GridBagConstraints.BOTH);
    GuiUtil.addComponent(this.objfuncPanel, new JLabel("f(x) = "), 0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH);
    for (int i = 0, j = 1; i < objfuncComponents.size(); i++, j += 2) {
      GuiUtil.addComponent(this.objfuncPanel, objfuncComponents.get(i), j, 0, 1, 1, 1.0, 1.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH);
      String lblText = "* X" + i + (i == objfuncComponents.size() - 1 ? "" : " + ");
      GuiUtil.addComponent(this.objfuncPanel, new JLabel(lblText), j + 1, 0, 1, 1, 1.0, 1.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH);
    }
    // }

    // { GUI part for the constraints
    // Add constraint panels to the ModelPanel
    for (int i = 0; i < constraintPanels.size(); i++) {
      GuiUtil.addComponent(this, constraintPanels.get(i), 0, i + 1, 1, 1, 1.0, 1.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH);
    }

    // Add the components of each constraint panel
    for (int i = 0; i < constraintComponents.size(); i++) {
      for (int j = 0; j < constraintComponents.get(i).size(); j++) {
        GuiUtil.addComponent(this.constraintPanels.get(i),
            this.constraintComponents.get(i).get(j), j, 0, 1, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH);
      }
    }
    // }

    this.pack();

    updateUI(); // Resets the UI property with a value from the current look and feel
  }

  // }

  // { Utilities for DRY

  /**
   * Add the objective-function into the current ModelPanel
   * @param objfuncVars
   */
  private void addObjfunc(Iterable<BigDecimalTextField> objfuncVars) {
    for (BigDecimalTextField objfuncVar : objfuncVars) {
      this.objfuncComponents.add(objfuncVar);
    }
  }

  /**
   * Add a constraint into the current ModelPanel
   * @param vars
   * @param signEnum
   * @param constantTerm
   */
  private void addConstraint(Iterable<BigDecimalTextField> vars, ConstraintSign signEnum,
                            BigDecimalTextField constantTerm) {
    this.addConstraint(this.constraintComponents.size(), vars, signEnum, constantTerm);
  }

  /**
   * Add the constraint into the current ModelPanel
   * @param index
   * @param vars
   * @param signEnum
   * @param constantTerm
   */
  private void addConstraint(int index, Iterable<BigDecimalTextField> vars, ConstraintSign signEnum,
                             BigDecimalTextField constantTerm) {
    List<Component> component = new ArrayList<Component>();

    // { Add vars
    for (BigDecimalTextField var : vars) {
      component.add(var);
    }
    // }

    // { Add sign
    final JComboBox sign = new JComboBox(ConstraintSign.values());
    sign.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < constraintComponents.size(); i++) {
          for (int j = 0; j < constraintComponents.get(i).size(); j++) {
            if (constraintComponents.get(i).get(j).equals(sign)) {
              notifySignChange(i, (ConstraintSign) sign.getSelectedItem());
              pack();
            }
          }
        }
      }
    });
    sign.setSelectedItem(signEnum);
    component.add(sign);
    // }

    // { Add constant term
    component.add(constantTerm);
    // }

    // { Add add/rem buttons
    JButton btnAdd = new JButton("Add");
    JButton btnRem = new JButton("Rem");
    btnAdd.setToolTipText("Add a new constraint");
    btnAdd.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        btnAddMouseReleased(e);
        pack();
        updateGui();
      }
    });
    btnRem.setToolTipText("Remove this constraint");
    btnRem.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        btnRemMouseReleased(e);
        pack();
        updateGui();
      }
    });
    component.add(btnAdd);
    component.add(btnRem);
    // }

    this.constraintComponents.add(index, component);
  }

  /**
   * Remove the constraint from the current ModelPanel
   * @param index Constraint index to be removed
   */
  private void removeConstraint(Integer index) {
    this.constraintComponents.remove(index.intValue());
  }

  /**
   * Create a new panel for a constraint and put it into the bottom of the constraints
   */
  private void addConstraintPanel() {
    this.addConstraintPanel(this.constraintPanels.size());
  }

  /**
   * Create a new panel for a constraint and put it into the specified position of the constraints
   * @param index Position where the new constraint panel should be placed into
   */
  private void addConstraintPanel(Integer index) {
    JPanel p = new JPanel();
    p.setLayout(new GridBagLayout());
    this.constraintPanels.add(index, p);
  }

  /**
   * Remove the specified constraint panel
   * @param index Index of the constraint panel to be removed
   */
  private void removeConstraintPanel(Integer index) {
    this.constraintPanels.remove(index.intValue());
  }

  /**
   * Add the specified variable
   * @param coefficient Coefficient for the variable
   * @param index The variable index
   */
  private void addVariable(BigDecimal coefficient, Integer index) {
    for (List<Component> constraintComponent : this.constraintComponents) {
      constraintComponent.add(index, new BigDecimalTextField(coefficient));
    }
    this.objfuncComponents.add(index, new BigDecimalTextField(coefficient));
  }

  /**
   * Remove the specified variable
   * @param index Index of the variable to be removed
   */
  private void removeVariable(int index) {
    for (List<Component> constraintComponent : this.constraintComponents) {
      constraintComponent.remove(index);
    }
    this.objfuncComponents.remove(index);
  }

  /**
   * Set the variables count
   * @param varCount The new variables count
   */
  public void setVariablesCount(int varCount) {
    for (Integer i = this.constraintComponents.get(0).size() - 4;
         varCount > this.constraintComponents.get(0).size() - 4; i++) {
      this.addVariable(defVarValue, i);
      // { Notify listeners for: var added
      List<BigDecimal> varValues = new ArrayList<BigDecimal>();
      for (Integer j = 0; j < this.constraintComponents.size(); j++) {
        varValues.add(defVarValue);
      }
      varValues.add(defVarValue);
      this.notifyVarAdd(i, varValues);
      // }
    }
    for (Integer i = this.constraintComponents.get(0).size() - 5;
         this.constraintComponents.get(0).size() - 4 > varCount; i--) {
      this.removeVariable(i);
      // Notify listeners for: var removed
      this.notifyVarRem(i);
    }
  }

  // }

  // { GUI events handlers behaviour (Extracted outside the inner-classes for more clarity)

  private void btnAddMouseReleased(MouseEvent e) {
    Integer index = -1;
    for (Integer i = 0; i < this.constraintComponents.size(); i++) {
      for (Integer j = 0; j < this.constraintComponents.get(i).size(); j++) {
        if (this.constraintComponents.get(i).get(j) == e.getComponent()) {
          index = i + 1;
        }
      }
    }

    this.addConstraintPanel(index);

    Collection<BigDecimalTextField> vars = new ArrayList<BigDecimalTextField>();
    for (int i = 0; i < this.constraintComponents.get(0).size() - 4; i++) {
      vars.add(new BigDecimalTextField(defVarValue));
    }
    this.addConstraint(index, vars, defSign, new BigDecimalTextField(defConstantTermValue));
    // { Notify listeners for: constraint added
    List<BigDecimal> varsValue = new ArrayList<BigDecimal>();
    for (BigDecimalTextField f : vars) {
      varsValue.add(new BigDecimal(f.getText()));
    }
    this.notifyConstraintAdd(index, varsValue, defConstantTermValue, defSign);
    // }
  }

  private void btnRemMouseReleased(MouseEvent e) {
    if (this.constraintComponents.size() > 1) {
      Integer index = -1;
      for (Integer i = 0; i < this.constraintComponents.size(); i++) {
        for (Integer j = 0; j < this.constraintComponents.get(i).size(); j++) {
          if (this.constraintComponents.get(i).get(j) == e.getComponent()) {
            index = i;
          }
        }
      }
      this.removeConstraintPanel(index);
      this.removeConstraint(index);
      // Notify listeners for: constraint removed
      this.notifyConstraintRemove(index);
    }
  }

  // }

  // { Listeners management

  /**
   * Adds the listener to the current ModelPanel listeners
   * @param listener The listener to be added to the current ModelPanel listeners
   */
  @Override
  public void register(UiEventListener listener) {
    this.listeners.add(listener);
  }

  // }

  // { Listeners Notifiers

  private void notifyConstraintRemove(Integer constraintIndex) {
    for (UiEventListener listener : this.listeners) {
      listener.commandRequestedForSolver(new RemoveConstraintCommand(constraintIndex));
    }
  }

  private void notifyConstraintAdd(Integer constraintIndex, List<BigDecimal> vars,
                                  BigDecimal constantTerm, ConstraintSign sign) {
    for (UiEventListener listener : this.listeners) {
      listener.commandRequestedForSolver(new AddConstraintCommand(constraintIndex, vars, sign, constantTerm));
    }
  }

  private void notifyVarAdd(Integer varIndex, List<BigDecimal> values) {
    for (UiEventListener listener : this.listeners) {
      listener.commandRequestedForSolver(new AddVariableCommand(varIndex, values));
    }
  }

  private void notifyVarRem(int varIndex) {
    for (UiEventListener listener : this.listeners) {
      listener.commandRequestedForSolver(new RemoveVariableCommand(varIndex));
    }
  }

  private void notifyConstraintVarChange(Integer constraintIndex, Integer varIndex,
                                         BigDecimal value) {
    for (UiEventListener listener : this.listeners) {
      listener.commandRequestedForSolver(new SetConstraintVariableCommand(constraintIndex, varIndex, value));
    }
  }

  private void notifyConstraintConstantTermChange(Integer constraintIndex, BigDecimal value) {
    for (UiEventListener listener : this.listeners) {
      listener.commandRequestedForSolver(new SetConstraintConstantTermCommand(constraintIndex, value));
    }
  }

  private void notifyObjfuncVarChange(Integer varIndex, BigDecimal value) {
    for (UiEventListener listener : this.listeners) {
      listener.commandRequestedForSolver(new SetObjfuncVariableCommand(varIndex, value));
    }
  }

  private void notifySignChange(Integer constraintIndex, ConstraintSign value) {
    for (UiEventListener listener : this.listeners) {
      listener.commandRequestedForSolver(new SetConstraintSignCommand(constraintIndex, value));
    }
  }

  // }

  // { methods to allow modifications from outside (e.g. a container of this)

  /**
   * Call this method from awt thread (e.g. <code>SwingUtilities.invokeLater()</code>).
   */
  public void setConstraintSign(Integer constraintIndex, ConstraintSign sign) {

    /* { TODO: something like:
    Integer index = this.constraintComponents.size - 2;
    JComboBox<ConstraintSign> cb = (JComboBox<ConstraintSign>) this.constraintComponents.get(constraintIndex).get(index);
    // TODO: select <sign> for <cb>
    } */
  }

  /**
   * Call this method from awt thread (e.g. <code>SwingUtilities.invokeLater()</code>).
   */
  public void setConstraintVariable(Integer constraintIndex, Integer id, BigDecimal coefficient) {
    /* { TODO: something like:
    BigDecimalTextField tf = (BigDecimalTextField) this.constraintComponents.get(constraintIndex).get(id);
    tf.setText(coefficient);
    } */
  }

  /**
   * Call this method from awt thread (e.g. <code>SwingUtilities.invokeLater()</code>).
   */
  public void setConstraintConstantTerm(Integer constraintIndex, BigDecimal constantTerm) {
    /* { TODO: something like:
    Integer index = this.constraintComponents.size - 1;
    BigDecimalTextField tf = (BigDecimalTextField) this.constraintComponents.get(constraintIndex).get(index);
    tf.setText(constantTerm);
    } */
  }

  /**
   * Call this method from awt thread (e.g. <code>SwingUtilities.invokeLater()</code>).
   */
  public void setObjfuncVariable(Integer id, BigDecimal coefficient) {
    /* { TODO: something like:
    this.objfuncComponents.get(id).setText(coefficient);
    } */
  }

  // }

}

