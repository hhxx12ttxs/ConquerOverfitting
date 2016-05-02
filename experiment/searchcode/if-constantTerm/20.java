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

import org.nextreamlabs.simplex.model.converter.CannotConvertModelException;
import org.nextreamlabs.simplex.model.component.ConstraintSign;
import org.nextreamlabs.simplex.solver.MetaSolverCreator;
import org.nextreamlabs.simplex.solver.command.StepRequestCommand;
import org.nextreamlabs.simplex.ui.Ui;
import org.nextreamlabs.simplex.ui.UiEventListener;
import org.nextreamlabs.simplex.ui.UiEventSource;
import org.nextreamlabs.simplex.ui.file.FileUi;
import org.nextreamlabs.simplex.ui.file.InvalidInputFileException;
import org.nextreamlabs.simplex.ui.file.TextFileFilter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * The main frame for the GUI
 */
public class MainFrame extends JFrame implements UiEventSource, Ui {
  private static final Logger logger;

  // { Default values
  private static final String defBtnInfoText;
  private static final String defBtnDoStepText;
  private static final String defBtnResizeText;
  private static final String defBtnAddFileUiText;
  private static final String defLblVarCountText;
  private static final String defComboboxStatusText;
  private static final String defLblSelectSolversText;
  private static final Integer defVarCount;
  private static final Integer defConsCount;

  private static final Integer minWidth = 360;
  private static final Integer minHeight = 160;
  // }


  // { Components
  private final JPanel contentPane;

  private final JPanel westPanel;
  private final JLabel lblSelectSolvers;
  private final JComboBox selectSolvers;

  private final JPanel southPanel;
  private final JComboBox comboboxStatus;
  private final JButton btnDoStep;

  private final JPanel northPanel;
  private final JLabel lblVarCount;
  private final JButton btnInfo, btnResize, btnAddFileUi;
  private final JSpinner spVarCount;

  private final JScrollPane modelPanelScrollPane;
  private final ModelPanel panelModel;
  // }

  private final Collection<UiEventListener> listeners;

  static {
    defLblSelectSolversText = "Select a solver:";
    defBtnInfoText = "Info..";
    defBtnAddFileUiText = "Add FileUI";
    defLblVarCountText = "Num Vars:";
    defComboboxStatusText = "";
    defBtnDoStepText = "Do Step";
    defBtnResizeText = "Resize";
    defVarCount = 3;
    defConsCount = 2;
  }

  static {
    logger = Logger.getLogger("org.nextreamlabs.simplex.FileUi");
  }

  // { Constructors and Factories

  private MainFrame(String title) {
    super(title); // Initialize the JFrame

    this.listeners = new ArrayList<UiEventListener>(); // Initialize the listeners collection

    // { Create the 'Components' for the GUI
    this.contentPane = new JPanel();
    this.southPanel = new JPanel();
    this.comboboxStatus = new JComboBox(new String[]{defComboboxStatusText});
    this.btnDoStep = new JButton(defBtnDoStepText);
    this.modelPanelScrollPane = new JScrollPane();
    this.panelModel = ModelPanel.create(this);
    this.northPanel = new JPanel();
    this.btnInfo = new JButton(defBtnInfoText);
    this.btnAddFileUi = new JButton(defBtnAddFileUiText);
    this.lblVarCount = new JLabel(defLblVarCountText);
    this.spVarCount = new JSpinner();
    this.btnResize = new JButton(defBtnResizeText);
    this.westPanel = new JPanel();
    this.lblSelectSolvers = new JLabel(defLblSelectSolversText);
    this.selectSolvers = new JComboBox(MetaSolverCreator.values());
    // }
  }

  /**
   * Create a new MainFrame
   *
   * @param title The GUI title
   * @return The new MainFrame object representing the GUI
   */
  public static MainFrame create(String title) {
    return new MainFrame(title);
  }

  // }

  /**
   * Initialize the 'Components' for the GUI
   */
  public void initialize() {
    // ==> contentPane initial setup
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);

    // ==> BorderLayout.SOUTH setup
    southPanel.setLayout(new GridBagLayout());

    GuiUtil.addComponent(southPanel, new JLabel("Status:"), 0, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH);

    comboboxStatus.setBorder(new EmptyBorder(0, 10, 0, 10));
    comboboxStatus.setToolTipText("Status History");
    GuiUtil.addComponent(southPanel, comboboxStatus, 1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
        GridBagConstraints.BOTH);

    btnDoStep.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        btnStepActionPerformed(e);
      }
    });
    GuiUtil.addComponent(southPanel, btnDoStep, 2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
        GridBagConstraints.BOTH);

    contentPane.add(southPanel, BorderLayout.SOUTH);

    // ==> BorderLayout.CENTER setup
    modelPanelScrollPane.setViewportView(panelModel);
    contentPane.add(modelPanelScrollPane, BorderLayout.CENTER);

    // ==> BorderLayout.NORTH setup
    northPanel.setLayout(new GridBagLayout());

    btnInfo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        btnInfoActionPerformed(e);
      }
    });
    GuiUtil.addComponent(northPanel, btnInfo, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
        GridBagConstraints.BOTH);

    final JFileChooser chooserFileUi = new JFileChooser();
    chooserFileUi.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooserFileUi.setAcceptAllFileFilterUsed(Boolean.FALSE);
    chooserFileUi.addChoosableFileFilter(new TextFileFilter());
    btnAddFileUi.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAddFileUi) {
          Integer status = chooserFileUi.showOpenDialog(MainFrame.this);

          if (status == JFileChooser.APPROVE_OPTION) {
            try {
              notifyUiAdded(FileUi.create(chooserFileUi.getSelectedFile().getAbsolutePath()));
            } catch (InvalidInputFileException exc) {
              MainFrame.logger.warning("The selected file is invalid");
            }
          }
        }
      }
    });
    GuiUtil.addComponent(northPanel, btnAddFileUi, 1, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
        GridBagConstraints.BOTH);

    lblVarCount.setBorder(new EmptyBorder(2, 30, 2, 6));
    GuiUtil.addComponent(northPanel, lblVarCount, 2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
        GridBagConstraints.BOTH);

    spVarCount.setModel(new SpinnerNumberModel(defVarCount, 1, null, 1));
    spVarCount.setBorder(new EmptyBorder(2, 0, 2, 8));
    spVarCount.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        spVarCountStatedChanged(e);
      }
    });
    GuiUtil.addComponent(northPanel, spVarCount, 3, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH);

    btnResize.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        btnResizeActionPerformed(e);
      }
    });
    GuiUtil.addComponent(northPanel, btnResize, 4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
        GridBagConstraints.BOTH);

    GuiUtil.addComponent(northPanel, Box.createHorizontalStrut(0), 1, 0, 1, 1, 1.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH);

    contentPane.add(northPanel, BorderLayout.NORTH);

    // ==> BorderLayout.WEST setup
    westPanel.setLayout(new GridBagLayout());

    lblSelectSolvers.setBorder(new EmptyBorder(0, 0, 2, 4));
    selectSolvers.setBorder(new EmptyBorder(0, 0, 0, 4));
    final Component parent = this;
    selectSolvers.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        MetaSolverCreator metaSolverCreator = (MetaSolverCreator) selectSolvers.getSelectedItem();
        try {
          notifySolverSelected(metaSolverCreator);
        } catch (CannotConvertModelException exc) {
          StringBuilder sb = new StringBuilder();
          sb.append("Error when creating the solver.");

          JOptionPane.showMessageDialog(parent, sb.toString());
        }
      }
    });

    GuiUtil.addComponent(westPanel, lblSelectSolvers, 0, 0, 1, 1, 1.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH);
    GuiUtil.addComponent(westPanel, selectSolvers, 0, 1, 1, 1, 1.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH);

    contentPane.add(westPanel, BorderLayout.WEST);

    // ==> contentPane final setup
    this.setContentPane(contentPane);

    // ==> Cascade calls for initialization
    this.panelModel.initialize(defConsCount, defVarCount);

    // ==> Additional GUI configurations
    // Don't allow the GUI to be smaller than (minWidth, minHeight)
    this.setMinimumSize(new Dimension(minWidth, minHeight));
    // Terminate the VM on close. Wait for other displayable windows to be disposed
    // before terminating the VM.
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // Set the size of this JFrame to fit its internal components
    this.pack();
    // Center the JFrame in the middle of the current screen
    this.locate();
  }

  /**
   * Position the GUI in the center of the screen and resize it to avoid overflows
   */
  private void locate() {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Point location = new Point((screenSize.width - this.getSize().width) / 2,
        (screenSize.height - this.getSize().height) / 2);
    this.setLocation(location);
  }

  // { GUI events handlers behaviour

  /**
   * Triggered when an action is performed on spVarCount, a.k.a. an entity wants to change the
   * number of variables
   *
   * @param e The ActionEvent of an action performed on spVarCount
   */
  private void spVarCountStatedChanged(ChangeEvent e) {
    JSpinner spinner = (JSpinner) e.getSource();
    this.panelModel.setVariablesCount((Integer) spinner.getValue());
    this.panelModel.updateGui();
    this.setComboboxStatusText("Variables count changed to: " + spinner.getValue());
  }

  /**
   * Triggered when an action is performed on btnInfo, a.k.a. an entity requested more informations
   * about the program
   *
   * @param ignored The ActionEvent of an action performed on btnInfo
   */
  private void btnInfoActionPerformed(ActionEvent ignored) {
    StringBuilder sb = new StringBuilder();
    sb.append("Linear problems solver using the simplex method.\n\n");
    sb.append("Instructions:\n");
    sb.append("Insert or modify the problem model;\n");
    sb.append("play with utils functions in the right side of the form or just press ");
    sb.append("the smart DoStep button.\n\n");
    sb.append("Made by Alessandro Molari for the FINF-B exam of ing2.unibo.it");

    JOptionPane.showMessageDialog(this, sb.toString());
  }

  /**
   * Triggered when an action is performed on btnResize, a.k.a. an entity requested a resize on
   * the current MainFrame
   *
   * @param ignored The ActionEvent of an action performed on btnResize
   */
  private void btnResizeActionPerformed(ActionEvent ignored) {
    this.pack();
    this.locate();
  }

  /**
   * Triggered when a step has been requested
   * @param ignored The ActionEvent of an action performed on btnStep
   */
  private void btnStepActionPerformed(ActionEvent ignored) {
    this.notifyStepRequested();
  }

  // }

  // { Listeners management

  /**
   * A listener for the MainFrame is also a listener for the ModelPanel, because some events can be
   * thrown by the MainFrame and model-specific events are thrown by the ModelPanel
   *
   * @param listener The listener to be added to the current MainFrame and panelModel listeners
   */
  @Override
  public void register(UiEventListener listener) {
    this.listeners.add(listener);
    this.panelModel.register(listener);
  }

  // }

  // { Listeners notifiers

  private void notifySolverSelected(MetaSolverCreator metaSolverCreator)
      throws CannotConvertModelException {
    for (UiEventListener listener : this.listeners) {
      listener.solverSelected(metaSolverCreator);
    }
  }

  private void notifyStepRequested() {
    for (UiEventListener listener : this.listeners) {
      listener.commandRequestedForSolver(new StepRequestCommand());
    }
  }

  private void notifyUiAdded(Ui ui) {
    for (UiEventListener listener : this.listeners) {
      listener.uiAdded(ui);
    }
  }

  // }

  // { Utilities to DRY

  private void setComboboxStatusText(String text) {
    this.comboboxStatus.insertItemAt(text, 0);
    this.comboboxStatus.setSelectedIndex(0);
    this.comboboxStatus.setMinimumSize(this.comboboxStatus.getPreferredSize());
    this.pack();
    this.locate();
  }

  // }

  // { Ui implementation

  @Override
  public Boolean start() {
    SwingUtilities.invokeLater(
      new Runnable() {
        @Override
        public void run() {
          setVisible(true);
        }
      }
    );
    return Boolean.TRUE;
  }

  @Override
  public Boolean setConstraintSign(final Integer constraintIndex, final ConstraintSign sign) {
    SwingUtilities.invokeLater(
      new Runnable() {
        @Override
        public void run() {
          panelModel.setConstraintSign(constraintIndex, sign);
        }
      }
    );
    return Boolean.TRUE;
  }

  @Override
  public Boolean setConstraintVariable(final Integer constraintIndex, final Integer id, final BigDecimal coefficient) {
    SwingUtilities.invokeLater(
      new Runnable() {
        @Override
        public void run() {
          panelModel.setConstraintVariable(constraintIndex, id, coefficient);
        }
      }
    );
    return Boolean.TRUE;
  }

  @Override
  public Boolean setConstraintConstantTerm(final Integer constraintIndex, final BigDecimal constantTerm) {
    SwingUtilities.invokeLater(
      new Runnable() {
        @Override
        public void run() {
          panelModel.setConstraintConstantTerm(constraintIndex, constantTerm);
        }
      }
    );
    return Boolean.TRUE;
  }

  @Override
  public Boolean setObjfuncVariable(final Integer id, final BigDecimal coefficient) {
    SwingUtilities.invokeLater(
      new Runnable() {
        @Override
        public void run() {
          panelModel.setObjfuncVariable(id, coefficient);
        }
      }
    );
    return Boolean.TRUE;
  }

  @Override
  public Boolean setStatusText(final String text) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        setComboboxStatusText((text == null) ? "" : text);
        locate();
      }
    };
    SwingUtilities.invokeLater(runnable);

    return Boolean.TRUE;
  }

  // }

}

