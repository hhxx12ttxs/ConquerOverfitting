/*
<<<<<<< HEAD
 * Copyright (c) 2008, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * $Id: AreaViewer.java,v 1.5 2009/02/18 12:08:10 fros4943 Exp $
 */

package se.sics.mrm;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ProgressMonitor;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.jdom.Element;

import se.sics.cooja.ClassDescription;
import se.sics.cooja.GUI;
import se.sics.cooja.PluginType;
import se.sics.cooja.Simulation;
import se.sics.cooja.SupportedArguments;
import se.sics.cooja.VisPlugin;
import se.sics.cooja.interfaces.DirectionalAntennaRadio;
import se.sics.cooja.interfaces.Position;
import se.sics.cooja.interfaces.Radio;
import se.sics.mrm.ChannelModel.TxPair;

/**
 * The class AreaViewer belongs to the MRM package.
 *
 * It is used to visualize available radios, traffic between them as well
 * as the current radio propagation area of single radios.
 * Users may also add background images (such as maps) and color-analyze them
 * in order to add simulated obstacles in the radio medium.
 *
 * For more information about MRM see MRM.java
 *
 * @see MRM
 * @author Fredrik Osterlind
 */
@ClassDescription("MRM Radio environment")
@PluginType(PluginType.SIM_PLUGIN)
@SupportedArguments(radioMediums = {MRM.class})
public class AreaViewer extends VisPlugin {
  private static final long serialVersionUID = 1L;
  private static Logger logger = Logger.getLogger(AreaViewer.class);

  private final JPanel canvas;

  ChannelModel.TransmissionData dataTypeToVisualize = ChannelModel.TransmissionData.SIGNAL_STRENGTH;
  ButtonGroup visTypeSelectionGroup;

  // General drawing parameters
  private Point lastHandledPosition = new Point(0,0);
  private double zoomCenterX = 0.0;
  private double zoomCenterY = 0.0;
  private Point zoomCenterPoint = new Point();
  private double currentZoomX = 1.0f;
  private double currentZoomY = 1.0f;
  private double currentPanX = 0.0f;
  private double currentPanY = 0.0f;

  private boolean drawBackgroundImage = true;
  private boolean drawCalculatedObstacles = true;
  private boolean drawChannelProbabilities = true;
  private boolean drawRadios = true;
  //private boolean drawRadioActivity = true;
  private boolean drawScaleArrow = true;

  // Background drawing parameters (meters)
  private double backgroundStartX = 0.0;
  private double backgroundStartY = 0.0;
  private double backgroundWidth = 0.0;
  private double backgroundHeight = 0.0;
  private Image backgroundImage = null;
  private File backgroundImageFile = null;

  // Obstacle drawing parameters (same scale as background)
  private boolean needToRepaintObstacleImage = false;
  private double obstacleStartX = 0.0;
  private double obstacleStartY = 0.0;
  private double obstacleWidth = 0.0;
  private double obstacleHeight = 0.0;
  private Image obstacleImage = null;

  // Channel probabilities drawing parameters (meters)
  private double channelStartX = 0.0;
  private double channelStartY = 0.0;
  private double channelWidth = 0.0;
  private double channelHeight = 0.0;
  private Image channelImage = null;

  private JSlider resolutionSlider;
  private Box controlPanel;
  private JScrollPane scrollControlPanel;

  private Simulation currentSimulation;
  private MRM currentRadioMedium;
  private ChannelModel currentChannelModel;

  private final String antennaImageFilename = "antenna.png";
  private final Image antennaImage;

  private Radio selectedRadio = null;
  private boolean inSelectMode = true;
  private boolean inTrackMode = false;

  private ChannelModel.TrackedSignalComponents trackedComponents = null;

  // Coloring variables
  private JPanel coloringIntervalPanel = null;
  private double coloringHighest = 0;
  private double coloringLowest = 0;
  private boolean coloringIsFixed = true;

  private Thread attenuatorThread = null;

  private JCheckBox showSettingsBox;
  private JCheckBox backgroundCheckBox;
  private JCheckBox obstaclesCheckBox;
  private JCheckBox channelCheckBox;
  private JCheckBox radiosCheckBox;
//  private JCheckBox radioActivityCheckBox;
  private JCheckBox arrowCheckBox;

  private JRadioButton noneButton = null;

  private JRadioButton selectModeButton;
  private JRadioButton panModeButton;
  private JRadioButton zoomModeButton;
  private JRadioButton trackModeButton;

  private Action paintEnvironmentAction;

  /**
   * Initializes an AreaViewer.
   *
   * @param simulationToVisualize Simulation using MRM
   */
  public AreaViewer(Simulation simulationToVisualize, GUI gui) {
    super("MRM Radio environment", gui);

    currentSimulation = simulationToVisualize;
    currentRadioMedium = (MRM) currentSimulation.getRadioMedium();
    currentChannelModel = currentRadioMedium.getChannelModel();

    // We want to listen to changes both in the channel model as well as in the radio medium
    currentChannelModel.addSettingsObserver(channelModelSettingsObserver);
    currentRadioMedium.addSettingsObserver(radioMediumSettingsObserver);
    currentRadioMedium.addRadioMediumObserver(radioMediumActivityObserver);

    // Set initial size etc.
    setSize(500, 500);
    setVisible(true);

    // Canvas mode radio buttons + show settings checkbox
    showSettingsBox = new JCheckBox ("settings", true);
    showSettingsBox.setAlignmentY(Component.TOP_ALIGNMENT);
    showSettingsBox.setContentAreaFilled(false);
    showSettingsBox.setActionCommand("toggle show settings");
    showSettingsBox.addActionListener(canvasModeHandler);

    selectModeButton = new JRadioButton ("select");
    selectModeButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    selectModeButton.setContentAreaFilled(false);
    selectModeButton.setActionCommand("set select mode");
    selectModeButton.addActionListener(canvasModeHandler);
    selectModeButton.setSelected(true);

    panModeButton = new JRadioButton ("pan");
    panModeButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    panModeButton.setContentAreaFilled(false);
    panModeButton.setActionCommand("set pan mode");
    panModeButton.addActionListener(canvasModeHandler);

    zoomModeButton = new JRadioButton ("zoom");
    zoomModeButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    zoomModeButton.setContentAreaFilled(false);
    zoomModeButton.setActionCommand("set zoom mode");
    zoomModeButton.addActionListener(canvasModeHandler);

    trackModeButton = new JRadioButton ("track rays");
    trackModeButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    trackModeButton.setContentAreaFilled(false);
    trackModeButton.setActionCommand("set track rays mode");
    trackModeButton.addActionListener(canvasModeHandler);
    trackModeButton.setEnabled(false);

    ButtonGroup group = new ButtonGroup();
    group.add(selectModeButton);
    group.add(panModeButton);
    group.add(zoomModeButton);
    group.add(trackModeButton);

    // Create canvas
    canvas = new JPanel() {
      private static final long serialVersionUID = 1L;
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        repaintCanvas((Graphics2D) g);
      }
    };
    canvas.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    canvas.setBackground(Color.WHITE);
    canvas.setLayout(new BorderLayout());
    canvas.addMouseListener(canvasMouseHandler);

    // Create canvas mode panel
    JPanel canvasModePanel = new JPanel();
    canvasModePanel.setOpaque(false);
    canvasModePanel.setLayout(new BoxLayout(canvasModePanel, BoxLayout.Y_AXIS));
    canvasModePanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    canvasModePanel.add(showSettingsBox);
    canvasModePanel.add(Box.createVerticalGlue());
    canvasModePanel.add(selectModeButton);
    canvasModePanel.add(panModeButton);
    canvasModePanel.add(zoomModeButton);
    canvasModePanel.add(trackModeButton);
    canvas.add(BorderLayout.EAST, canvasModePanel);

    // Create control graphics panel
    JPanel graphicsComponentsPanel = new JPanel();
    graphicsComponentsPanel.setLayout(new BoxLayout(graphicsComponentsPanel, BoxLayout.Y_AXIS));
    graphicsComponentsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    graphicsComponentsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    graphicsComponentsPanel.add(new JLabel("Show/Hide:"));

    backgroundCheckBox = new JCheckBox("Background image", true);
    backgroundCheckBox.setActionCommand("toggle background");
    backgroundCheckBox.addActionListener(selectGraphicsHandler);
    graphicsComponentsPanel.add(backgroundCheckBox);

    obstaclesCheckBox = new JCheckBox("Obstacles", true);
    obstaclesCheckBox.setActionCommand("toggle obstacles");
    obstaclesCheckBox.addActionListener(selectGraphicsHandler);
    graphicsComponentsPanel.add(obstaclesCheckBox);

    channelCheckBox = new JCheckBox("Channel", true);
    channelCheckBox.setActionCommand("toggle channel");
    channelCheckBox.addActionListener(selectGraphicsHandler);
    graphicsComponentsPanel.add(channelCheckBox);

    radiosCheckBox = new JCheckBox("Radios", true);
    radiosCheckBox.setActionCommand("toggle radios");
    radiosCheckBox.addActionListener(selectGraphicsHandler);
    graphicsComponentsPanel.add(radiosCheckBox);

//    radioActivityCheckBox = new JCheckBox("Radio Activity", true);
//    radioActivityCheckBox.setActionCommand("toggle radio activity");
//    radioActivityCheckBox.addActionListener(selectGraphicsHandler);
//    graphicsComponentsPanel.add(radioActivityCheckBox);

    arrowCheckBox = new JCheckBox("Scale arrow", true);
    arrowCheckBox.setActionCommand("toggle arrow");
    arrowCheckBox.addActionListener(selectGraphicsHandler);
    graphicsComponentsPanel.add(arrowCheckBox);

    graphicsComponentsPanel.add(Box.createRigidArea(new Dimension(0,20)));
    graphicsComponentsPanel.add(new JLabel("Obstacle configuration:"));

    noneButton = new JRadioButton("No obstacles");
    noneButton.setActionCommand("set no obstacles");
    noneButton.addActionListener(obstacleHandler);
    noneButton.setSelected(true);

    JRadioButton pre1Button = new JRadioButton("Predefined 1");
    pre1Button.setActionCommand("set predefined 1");
    pre1Button.addActionListener(obstacleHandler);

    JRadioButton pre2Button = new JRadioButton("Predefined 2");
    pre2Button.setActionCommand("set predefined 2");
    pre2Button.addActionListener(obstacleHandler);

    JRadioButton customButton = new JRadioButton("From bitmap");
    customButton.setActionCommand("set custom bitmap");
    customButton.addActionListener(obstacleHandler);
    if (GUI.isVisualizedInApplet()) {
      customButton.setEnabled(false);
    }

    group = new ButtonGroup();
    group.add(noneButton);
    group.add(pre1Button);
    group.add(pre2Button);
    group.add(customButton);
    graphicsComponentsPanel.add(noneButton);
    graphicsComponentsPanel.add(pre1Button);
    graphicsComponentsPanel.add(pre2Button);
    graphicsComponentsPanel.add(customButton);

    // Create visualize channel output panel
    Box visualizeChannelPanel = Box.createVerticalBox();
    visualizeChannelPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    visualizeChannelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    // Channel coloring intervals
    visualizeChannelPanel.add(new JLabel("Channel coloring:"));

    JPanel fixedVsRelative = new JPanel(new GridLayout(1, 2));
    JRadioButton fixedColoringButton = new JRadioButton("Fixed");
    fixedColoringButton.setSelected(true);
    fixedColoringButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        coloringIsFixed = true;
      }
    });
    fixedVsRelative.add(fixedColoringButton);

    JRadioButton relativeColoringButton = new JRadioButton("Relative");
    relativeColoringButton.setSelected(true);
    relativeColoringButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        coloringIsFixed = false;
      }
    });
    fixedVsRelative.add(relativeColoringButton);
    ButtonGroup coloringGroup = new ButtonGroup();
    coloringGroup.add(fixedColoringButton);
    coloringGroup.add(relativeColoringButton);

    fixedVsRelative.setAlignmentX(Component.LEFT_ALIGNMENT);
    visualizeChannelPanel.add(fixedVsRelative);

    coloringIntervalPanel = new JPanel() {
      private static final long serialVersionUID = 8247374386307237940L;

      public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        double diff = coloringHighest - coloringLowest;
        int textHeight = g.getFontMetrics().getHeight();

        // If computing
        if (attenuatorThread != null && attenuatorThread.isAlive()) {
          g.setColor(Color.WHITE);
          g.fillRect(0, 0, width, height);
          g.setColor(Color.BLACK);
          String stringToDraw = "[calculating]";
          g.drawString(stringToDraw, width/2 - g.getFontMetrics().stringWidth(stringToDraw)/2, height/2 + textHeight/2);
          return;
        }

        // Check for infinite values
        if (Double.isInfinite(coloringHighest) || Double.isInfinite(coloringLowest)) {
          g.setColor(Color.WHITE);
          g.fillRect(0, 0, width, height);
          g.setColor(Color.BLACK);
          String stringToDraw = "INFINITE VALUES EXIST";
          g.drawString(stringToDraw, width/2 - g.getFontMetrics().stringWidth(stringToDraw)/2, height/2 + textHeight/2);
          return;
        }

        // Check if values are constant
        if (diff == 0) {
          g.setColor(Color.WHITE);
          g.fillRect(0, 0, width, height);
          g.setColor(Color.BLACK);
          NumberFormat formatter = DecimalFormat.getNumberInstance();
          String stringToDraw = "CONSTANT VALUES (" + formatter.format(coloringHighest) + ")";
          g.drawString(stringToDraw, width/2 - g.getFontMetrics().stringWidth(stringToDraw)/2, height/2 + textHeight/2);
          return;
        }

        for (int i=0; i < width; i++) {
          double paintValue = coloringLowest + (double) i / (double) width * diff;
          g.setColor(
              new Color(
                  getColorOfSignalStrength(paintValue, coloringLowest, coloringHighest)));

          g.drawLine(i, 0, i, height);
        }

        if (dataTypeToVisualize == ChannelModel.TransmissionData.PROB_OF_RECEPTION) {
          NumberFormat formatter = DecimalFormat.getPercentInstance();
          g.setColor(Color.BLACK);
          g.drawString(formatter.format(coloringLowest), 3, textHeight);
          String stringToDraw = formatter.format(coloringHighest);
          g.drawString(stringToDraw, width - g.getFontMetrics().stringWidth(stringToDraw) - 3, textHeight);
        } else if (dataTypeToVisualize == ChannelModel.TransmissionData.SIGNAL_STRENGTH ||
            dataTypeToVisualize == ChannelModel.TransmissionData.SIGNAL_STRENGTH_VAR ) {
          NumberFormat formatter = DecimalFormat.getNumberInstance();
          g.setColor(Color.BLACK);
          g.drawString(formatter.format(coloringLowest) + "dBm", 3, textHeight);
          String stringToDraw = formatter.format(coloringHighest) + "dBm";
          g.drawString(stringToDraw, width - g.getFontMetrics().stringWidth(stringToDraw) - 3, textHeight);
        } else if (dataTypeToVisualize == ChannelModel.TransmissionData.SNR ||
            dataTypeToVisualize == ChannelModel.TransmissionData.SNR_VAR) {
          NumberFormat formatter = DecimalFormat.getNumberInstance();
          g.setColor(Color.BLACK);
          g.drawString(formatter.format(coloringLowest) + "dB", 3, textHeight);
          String stringToDraw = formatter.format(coloringHighest) + "dB";
          g.drawString(stringToDraw, width - g.getFontMetrics().stringWidth(stringToDraw) - 3, textHeight);
        } else if (dataTypeToVisualize == ChannelModel.TransmissionData.DELAY_SPREAD_RMS) {
          NumberFormat formatter = DecimalFormat.getNumberInstance();
          g.setColor(Color.BLACK);
          g.drawString(formatter.format(coloringLowest) + "us", 3, textHeight);
          String stringToDraw = formatter.format(coloringHighest) + "us";
          g.drawString(stringToDraw, width - g.getFontMetrics().stringWidth(stringToDraw) - 3, textHeight);
        }

      }
    };
    coloringIntervalPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    Dimension colorPanelSize = new Dimension(200, 20);
    coloringIntervalPanel.setPreferredSize(colorPanelSize);
    coloringIntervalPanel.setMinimumSize(colorPanelSize);
    coloringIntervalPanel.setMaximumSize(colorPanelSize);
    coloringIntervalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    visualizeChannelPanel.add(coloringIntervalPanel);

    // Choose channel output to visualize
    visualizeChannelPanel.add(Box.createRigidArea(new Dimension(0,20)));
    visualizeChannelPanel.add(new JLabel("Visualize:"));

    JRadioButton signalStrengthButton = new JRadioButton("Signal strength");
    signalStrengthButton.setActionCommand("signalStrengthButton");
    signalStrengthButton.setSelected(true);
    signalStrengthButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dataTypeToVisualize = ChannelModel.TransmissionData.SIGNAL_STRENGTH;
      }
    });
    visualizeChannelPanel.add(signalStrengthButton);

    JRadioButton signalStrengthVarButton = new JRadioButton("Signal strength variance");
    signalStrengthVarButton.setActionCommand("signalStrengthVarButton");
    signalStrengthVarButton.setSelected(false);
    signalStrengthVarButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dataTypeToVisualize = ChannelModel.TransmissionData.SIGNAL_STRENGTH_VAR;
      }
    });
    visualizeChannelPanel.add(signalStrengthVarButton);

    JRadioButton SNRButton = new JRadioButton("Signal to Noise ratio");
    SNRButton.setActionCommand("SNRButton");
    SNRButton.setSelected(false);
    SNRButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dataTypeToVisualize = ChannelModel.TransmissionData.SNR;
      }
    });
    visualizeChannelPanel.add(SNRButton);

    JRadioButton SNRVarButton = new JRadioButton("Signal to Noise variance");
    SNRVarButton.setActionCommand("SNRVarButton");
    SNRVarButton.setSelected(false);
    SNRVarButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dataTypeToVisualize = ChannelModel.TransmissionData.SNR_VAR;
      }
    });
    visualizeChannelPanel.add(SNRVarButton);

    JRadioButton probabilityButton = new JRadioButton("Probability of reception");
    probabilityButton.setActionCommand("probabilityButton");
    probabilityButton.setSelected(false);
    probabilityButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dataTypeToVisualize = ChannelModel.TransmissionData.PROB_OF_RECEPTION;
      }
    });
    visualizeChannelPanel.add(probabilityButton);

    JRadioButton rmsDelaySpreadButton = new JRadioButton("RMS delay spread");
    rmsDelaySpreadButton.setActionCommand("rmsDelaySpreadButton");
    rmsDelaySpreadButton.setSelected(false);
    rmsDelaySpreadButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dataTypeToVisualize = ChannelModel.TransmissionData.DELAY_SPREAD_RMS;
      }
    });
    visualizeChannelPanel.add(rmsDelaySpreadButton);

    visTypeSelectionGroup = new ButtonGroup();
    visTypeSelectionGroup.add(signalStrengthButton);
    visTypeSelectionGroup.add(signalStrengthVarButton);
    visTypeSelectionGroup.add(SNRButton);
    visTypeSelectionGroup.add(SNRVarButton);
    visTypeSelectionGroup.add(probabilityButton);
    visTypeSelectionGroup.add(rmsDelaySpreadButton);

    visualizeChannelPanel.add(Box.createRigidArea(new Dimension(0,20)));

    visualizeChannelPanel.add(new JLabel("Resolution:"));

    resolutionSlider = new JSlider(JSlider.HORIZONTAL, 30, 600, 100);
    resolutionSlider.setMajorTickSpacing(100);
    resolutionSlider.setPaintTicks(true);
    resolutionSlider.setPaintLabels(true);
    resolutionSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    visualizeChannelPanel.add(resolutionSlider);

    visualizeChannelPanel.add(Box.createRigidArea(new Dimension(0,20)));

    JButton recalculateVisibleButton = new JButton("Paint radio channel");
    paintEnvironmentAction = new AbstractAction("Paint radio channel") {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        repaintRadioEnvironment();
      }
    };
    paintEnvironmentAction.setEnabled(false);
    recalculateVisibleButton.setAction(paintEnvironmentAction);
    visualizeChannelPanel.add(recalculateVisibleButton);

    // Create control panel
    controlPanel = Box.createVerticalBox();
    graphicsComponentsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    controlPanel.add(graphicsComponentsPanel);
    controlPanel.add(new JSeparator());
    controlPanel.add(visualizeChannelPanel);
    controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    scrollControlPanel = new JScrollPane(
        controlPanel,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollControlPanel.setPreferredSize(new Dimension(250,0));

    // Add everything
    this.setLayout(new BorderLayout());
    this.add(BorderLayout.CENTER, canvas); // Add canvas
    this.add(BorderLayout.EAST, scrollControlPanel);

    // Load external images (antenna)
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    URL imageURL = this.getClass().getClassLoader().getResource(antennaImageFilename);
    antennaImage = toolkit.getImage(imageURL);

    MediaTracker tracker = new MediaTracker(canvas);
    tracker.addImage(antennaImage, 1);

    try {
      tracker.waitForAll();
    } catch (InterruptedException ex) {
      logger.fatal("Interrupted during image loading, aborting");
      return;
    }


    // Try to select current plugin
    try {
      setSelected(true);
    } catch (java.beans.PropertyVetoException e) {
      // Could not select
    }
  }

  /**
   * Listens to mouse event on canvas
   */
  private MouseAdapter canvasMouseHandler = new MouseAdapter() {
    private Popup popUpToolTip = null;
    private boolean temporaryZoom = false;
    private boolean temporaryPan = false;
    private boolean trackedPreviously = false;

    public void mouseReleased(MouseEvent e) {
      if (temporaryZoom) {
        temporaryZoom = false;
        if (trackedPreviously) {
          trackModeButton.doClick();
        } else {
          selectModeButton.doClick();
        }
      }
      if (temporaryPan) {
        temporaryPan = false;
        if (trackedPreviously) {
          trackModeButton.doClick();
        } else {
          selectModeButton.doClick();
        }
      }

      if (popUpToolTip != null) {
        popUpToolTip.hide();
        popUpToolTip = null;
      }
    }

    public void mousePressed(final MouseEvent e) {
      if (e.isControlDown()) {
        temporaryZoom = true;
        trackedPreviously = inTrackMode;
        zoomModeButton.doClick();
      }
      if (e.isAltDown()) {
        temporaryPan = true;
        trackedPreviously = inTrackMode;
        panModeButton.doClick();
        //canvasModeHandler.actionPerformed(new ActionEvent(e, 0, "set zoom mode"));
      }

      if (popUpToolTip != null) {
        popUpToolTip.hide();
        popUpToolTip = null;
      }

      /* Zoom & Pan */
      lastHandledPosition = new Point(e.getX(), e.getY());
      zoomCenterX = e.getX() / currentZoomX - currentPanX;
      zoomCenterY = e.getY() / currentZoomY - currentPanY;
      zoomCenterPoint = e.getPoint();
      if (temporaryZoom || temporaryPan) {
        e.consume();
        return;
      }

      /* Select */
      if (inSelectMode) {
        ArrayList<Radio> hitRadios = trackClickedRadio(e.getPoint());
        if (hitRadios == null || hitRadios.size() == 0) {
          if (e.getButton() != MouseEvent.BUTTON1) {
            selectedRadio = null;
            channelImage = null;
            trackModeButton.setEnabled(false);
            paintEnvironmentAction.setEnabled(false);
            canvas.repaint();
          }
          return;
        }

        if (hitRadios.size() == 1 && hitRadios.get(0) == selectedRadio) {
          return;
        }

        if (selectedRadio == null || !hitRadios.contains(selectedRadio)) {
          selectedRadio = hitRadios.get(0);
          trackModeButton.setEnabled(true);
          paintEnvironmentAction.setEnabled(true);
        } else {
          selectedRadio = hitRadios.get(
              (hitRadios.indexOf(selectedRadio)+1) % hitRadios.size()
          );

          trackModeButton.setEnabled(true);
          paintEnvironmentAction.setEnabled(true);
        }

        channelImage = null;
        canvas.repaint();
        return;
      }

      /* Track */
      if (inTrackMode && selectedRadio != null) {
        TxPair txPair = new TxPair() {
          public double getFromX() { return selectedRadio.getPosition().getXCoordinate(); }
          public double getFromY() { return selectedRadio.getPosition().getYCoordinate(); }
          public double getToX() { return e.getX() / currentZoomX - currentPanX; }
          public double getToY() { return e.getY() / currentZoomY - currentPanY; }
          public double getTxPower() { return selectedRadio.getCurrentOutputPower(); }
          public double getTxGain() {
            if (!(selectedRadio instanceof DirectionalAntennaRadio)) {
              return 0;
            }
            DirectionalAntennaRadio r = (DirectionalAntennaRadio)selectedRadio;
            double txGain = r.getRelativeGain(r.getDirection() + getAngle(), getDistance());
            //logger.debug("tx gain: " + txGain + " (angle " + String.format("%1.1f", Math.toDegrees(r.getDirection() + getAngle())) + ")");
            return txGain;
          }
          public double getRxGain() {
            return 0;
          }
        };
        trackedComponents = currentChannelModel.getRaysOfTransmission(txPair);
        canvas.repaint();

        /* Show popup */
        JToolTip t = AreaViewer.this.createToolTip();

        String logHtml =
                "<html>" +
                trackedComponents.log.replace("\n", "<br>").replace(" pi", " &pi;") +
                "</html>";
        t.setTipText(logHtml);

        if (t.getTipText() == null || t.getTipText().equals("")) {
          return;
        }
        popUpToolTip = PopupFactory.getSharedInstance().getPopup(
                        AreaViewer.this, t, e.getXOnScreen(), e.getYOnScreen());
        popUpToolTip.show();
      }
    }
  };

  /**
   * Listens to mouse movements when in pan mode
   */
  private MouseMotionListener canvasPanModeHandler = new MouseMotionListener() {
    public void mouseMoved(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {
      if (lastHandledPosition == null) {
        lastHandledPosition = e.getPoint();
        return;
      }

      // Pan relative to mouse movement and current zoom
      // This way the mouse "lock" to the canvas
      currentPanX += ((e.getX() - lastHandledPosition.x)) / currentZoomX;
      currentPanY += ((e.getY() - lastHandledPosition.y)) / currentZoomY;
      lastHandledPosition = e.getPoint();

      canvas.repaint();
    }
  };

  /**
   * Listens to mouse movements when in zoom mode
   */
  private MouseMotionListener canvasZoomModeHandler = new MouseMotionListener() {
    public void mouseMoved(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {
      if (lastHandledPosition == null) {
        lastHandledPosition = e.getPoint();
        return;
      }

      // Zoom relative to mouse movement (keep XY-proportions)
      currentZoomY += 0.005 * currentZoomY * ((lastHandledPosition.y - e.getY()));
      currentZoomY = Math.max(0.05, currentZoomY);
      currentZoomY = Math.min(1500, currentZoomY);
      currentZoomX = currentZoomY;

      // We also need to update the current pan in order to zoom towards the mouse
      currentPanX =  zoomCenterPoint.x/currentZoomX - zoomCenterX;
      currentPanY =  zoomCenterPoint.y/currentZoomY - zoomCenterY;

      lastHandledPosition = e.getPoint();
      canvas.repaint();
    }
  };

  /**
   * Selects which mouse mode the canvas should be in (select/pan/zoom)
   */
  private ActionListener canvasModeHandler = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals("set select mode")) {
        // Select mode, no mouse motion listener needed
        for (MouseMotionListener reggedListener: canvas.getMouseMotionListeners()) {
          canvas.removeMouseMotionListener(reggedListener);
        }

        inTrackMode = false;
        inSelectMode = true;
      } else if (e.getActionCommand().equals("set pan mode")) {
        // Remove all other mouse motion listeners
        for (MouseMotionListener reggedListener: canvas.getMouseMotionListeners()) {
          canvas.removeMouseMotionListener(reggedListener);
        }
        inSelectMode = false;
        inTrackMode = false;

        // Add the special pan mouse motion listener
        canvas.addMouseMotionListener(canvasPanModeHandler);
      } else if (e.getActionCommand().equals("set zoom mode")) {
        // Remove all other mouse motion listeners
        for (MouseMotionListener reggedListener: canvas.getMouseMotionListeners()) {
          canvas.removeMouseMotionListener(reggedListener);
        }
        inSelectMode = false;
        inTrackMode = false;

        // Add the special zoom mouse motion listener
        canvas.addMouseMotionListener(canvasZoomModeHandler);
      } else if (e.getActionCommand().equals("set track rays mode")) {
        // Remove all other mouse motion listeners
        for (MouseMotionListener reggedListener: canvas.getMouseMotionListeners()) {
          canvas.removeMouseMotionListener(reggedListener);
        }
        inSelectMode = false;
        inTrackMode = true;

      } else if (e.getActionCommand().equals("toggle show settings")) {
        if (((JCheckBox) e.getSource()).isSelected()) {
          scrollControlPanel.setVisible(true);
        } else {
          scrollControlPanel.setVisible(false);
        }
        AreaViewer.this.invalidate();
        AreaViewer.this.revalidate();
      }
    }
  };

  /**
   * Selects which graphical parts should be painted
   */
  private ActionListener selectGraphicsHandler = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals("toggle background")) {
        drawBackgroundImage = ((JCheckBox) e.getSource()).isSelected();
      } else if (e.getActionCommand().equals("toggle obstacles")) {
        drawCalculatedObstacles = ((JCheckBox) e.getSource()).isSelected();
      } else if (e.getActionCommand().equals("toggle channel")) {
        drawChannelProbabilities = ((JCheckBox) e.getSource()).isSelected();
      } else if (e.getActionCommand().equals("toggle radios")) {
        drawRadios = ((JCheckBox) e.getSource()).isSelected();
//      } else if (e.getActionCommand().equals("toggle radio activity")) {
//        drawRadioActivity = ((JCheckBox) e.getSource()).isSelected();
      } else if (e.getActionCommand().equals("toggle arrow")) {
        drawScaleArrow = ((JCheckBox) e.getSource()).isSelected();
      }

      canvas.repaint();
    }
  };

  /**
   * Helps user set a background image which can be analysed for obstacles/freespace.
   */
  private ActionListener obstacleHandler = new ActionListener() {

    /**
     * Choosable file filter that supports tif, gif, jpg, png, bmp.
     */
    class ImageFilter extends FileFilter {
      public boolean accept(File f) {
        if (f.isDirectory()) {
          return true;
        }

        String filename = f.getName();
        if (filename == null) {
          return false;
        }

        if (filename.endsWith(".gif") || filename.endsWith(".GIF") ||
            filename.endsWith(".jpg") || filename.endsWith(".JPG") ||
            filename.endsWith(".jpeg") || filename.endsWith(".JPEG") ||
            filename.endsWith(".png") || filename.endsWith(".PNG")){
          return true;
        }

        return false;
      }

      public String getDescription() {
        return "All supported images";
      }
    }

    class ImageSettingsDialog extends JDialog {
      private static final long serialVersionUID = 3026474554976919518L;

      private double
      virtualStartX = 0.0,
      virtualStartY = 0.0,
      virtualWidth = 0.0,
      virtualHeight = 0.0;

      private JFormattedTextField
      virtualStartXField,
      virtualStartYField,
      virtualWidthField,
      virtualHeightField;

      private boolean terminatedOK = false;

      private NumberFormat doubleFormat = NumberFormat.getNumberInstance();

      /**
       * Creates a new dialog for settings background parameters
       */
      protected ImageSettingsDialog(File imageFile, Image image, Frame frame) {
        super(frame, "Image settings");
        setupDialog();
      }
      protected ImageSettingsDialog(File imageFile, Image image, Dialog dialog) {
        super(dialog, "Image settings");
        setupDialog();
      }
      protected ImageSettingsDialog(File imageFile, Image image, Window window) {
        super(window, "Image settings");
        setupDialog();
      }

      private void setupDialog() {
        JPanel mainPanel, tempPanel;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        doubleFormat.setMinimumIntegerDigits(1);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Set layout and add components
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        tempPanel = new JPanel(new GridLayout(1, 2));
        tempPanel.add(new JLabel("Start X (m)     "));
        virtualStartXField = new JFormattedTextField(doubleFormat);
        virtualStartXField.setValue(new Double(0.0));
        tempPanel.add(virtualStartXField);
        mainPanel.add(tempPanel);

        tempPanel = new JPanel(new GridLayout(1, 2));
        tempPanel.add(new JLabel("Start Y (m)"));
        virtualStartYField = new JFormattedTextField(doubleFormat);
        virtualStartYField.setValue(new Double(0.0));
        tempPanel.add(virtualStartYField);
        mainPanel.add(tempPanel);

        tempPanel = new JPanel(new GridLayout(1, 2));
        tempPanel.add(new JLabel("Width (m)"));
        virtualWidthField = new JFormattedTextField(doubleFormat);
        virtualWidthField.setValue(new Double(100.0));
        tempPanel.add(virtualWidthField);
        mainPanel.add(tempPanel);

        tempPanel = new JPanel(new GridLayout(1, 2));
        tempPanel.add(new JLabel("Height (m)"));
        virtualHeightField = new JFormattedTextField(doubleFormat);
        virtualHeightField.setValue(new Double(100.0));
        tempPanel.add(virtualHeightField);
        mainPanel.add(tempPanel);

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(Box.createVerticalStrut(10));

        tempPanel = new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
        tempPanel.add(Box.createHorizontalGlue());

        final JButton okButton = new JButton("OK");
        this.getRootPane().setDefaultButton(okButton);
        final JButton cancelButton = new JButton("Cancel");
        okButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            virtualStartX = ((Number) virtualStartXField.getValue()).doubleValue();
            virtualStartY = ((Number) virtualStartYField.getValue()).doubleValue();
            virtualWidth = ((Number) virtualWidthField.getValue()).doubleValue();
            virtualHeight = ((Number) virtualHeightField.getValue()).doubleValue();

            terminatedOK = true;
            dispose();
}
        });
        cancelButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            terminatedOK = false;
            dispose();
          }
        });

        tempPanel.add(okButton);
        tempPanel.add(cancelButton);
        mainPanel.add(tempPanel);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel);

        // Show dialog
        setModal(true);
        pack();
        setLocationRelativeTo(this.getParent());
        setVisible(true);
      }

      public boolean terminatedOK() {
        return terminatedOK;
      }
      public double getVirtualStartX() {
        return virtualStartX;
      }
      public double getVirtualStartY() {
        return virtualStartY;
      }
      public double getVirtualWidth() {
        return virtualWidth;
      }
      public double getVirtualHeight() {
        return virtualHeight;
      }

    }

    public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals("set custom bitmap")) {
        if (!setCustomBitmap() || !analyzeBitmapForObstacles()) {
          backgroundImage = null;
          currentChannelModel.removeAllObstacles();

          noneButton.setSelected(true);
          repaint();
        }
      } else if (e.getActionCommand().equals("set predefined 1")) {
        currentChannelModel.removeAllObstacles();
        currentChannelModel.addRectObstacle(0, 0, 50, 5, false);
        currentChannelModel.addRectObstacle(0, 5, 5, 50, false);

        currentChannelModel.addRectObstacle(70, 0, 20, 5, false);
        currentChannelModel.addRectObstacle(0, 70, 5, 20, false);

        currentChannelModel.notifySettingsChanged();
        repaint();
      } else if (e.getActionCommand().equals("set predefined 2")) {
        currentChannelModel.removeAllObstacles();
        currentChannelModel.addRectObstacle(0, 0, 10, 10, false);
        currentChannelModel.addRectObstacle(30, 0, 10, 10, false);
        currentChannelModel.addRectObstacle(60, 0, 10, 10, false);
        currentChannelModel.addRectObstacle(90, 0, 10, 10, false);

        currentChannelModel.addRectObstacle(5, 90, 10, 10, false);
        currentChannelModel.addRectObstacle(25, 90, 10, 10, false);
        currentChannelModel.addRectObstacle(45, 90, 10, 10, false);
        currentChannelModel.addRectObstacle(65, 90, 10, 10, false);
        currentChannelModel.addRectObstacle(85, 90, 10, 10, false);
        currentChannelModel.notifySettingsChanged();
        repaint();
      } else if (e.getActionCommand().equals("set no obstacles")) {
        backgroundImage = null;
        currentChannelModel.removeAllObstacles();
        repaint();
      } else {
        logger.fatal("Unhandled action command: " + e.getActionCommand());
      }
    }

    private boolean setCustomBitmap() {

      /* Select image file */
      JFileChooser fileChooser = new JFileChooser();
      ImageFilter filter = new ImageFilter();
      fileChooser.addChoosableFileFilter(filter);

      int returnVal = fileChooser.showOpenDialog(canvas);
      if (returnVal != JFileChooser.APPROVE_OPTION) {
        return false;
      }

      File file = fileChooser.getSelectedFile();
      if (!filter.accept(file)) {
        logger.fatal("Non-supported file type, aborting");
        return false;
      }

      logger.info("Opening '" + file.getAbsolutePath() + "'");

      /* Load image data */
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Image image = toolkit.getImage(file.getAbsolutePath());

      MediaTracker tracker = new MediaTracker(canvas);
      tracker.addImage(image, 1);

      try {
        tracker.waitForAll();
        if (tracker.isErrorAny() || image == null) {
          logger.info("Error when loading '" + file.getAbsolutePath() + "'");
          image = null;
          return false;
        }
      } catch (InterruptedException ex) {
        logger.fatal("Interrupted during image loading, aborting");
        return false;
      }

      /* Set virtual size of image */
      Container topParent = GUI.getTopParentContainer();
      ImageSettingsDialog dialog;
      if (topParent instanceof Frame) {
        dialog = new ImageSettingsDialog(file, image, (Frame) topParent);
      } else if (topParent instanceof Dialog) {
        dialog = new ImageSettingsDialog(file, image, (Dialog) topParent);
      } else if (topParent instanceof Window) {
        dialog = new ImageSettingsDialog(file, image, (Window) topParent);
      } else {
        logger.fatal("Unknown parent container, aborting");
        return false;
      }

      if (!dialog.terminatedOK()) {
        logger.fatal("User canceled, aborting");
        image = null;
        return false;
      }

      /* Show loaded image */
      backgroundStartX = dialog.getVirtualStartX();
      backgroundStartY = dialog.getVirtualStartY();
      backgroundWidth = dialog.getVirtualWidth();
      backgroundHeight = dialog.getVirtualHeight();

      backgroundImage = image;
      backgroundImageFile = file;

      return true;
    }

    private boolean analyzeBitmapForObstacles() {
      if (backgroundImage == null) {
        return false;
      }

      /* Show obstacle finder dialog */
      ObstacleFinderDialog obstacleFinderDialog;
      Container parentContainer = GUI.getTopParentContainer();
      if (parentContainer instanceof Window) {
        obstacleFinderDialog = new ObstacleFinderDialog(
            backgroundImage, currentChannelModel, (Window) GUI.getTopParentContainer()
        );
      } else if (parentContainer instanceof Frame) {
        obstacleFinderDialog = new ObstacleFinderDialog(
            backgroundImage, currentChannelModel, (Frame) GUI.getTopParentContainer()
        );
      } else if (parentContainer instanceof Dialog) {
        obstacleFinderDialog = new ObstacleFinderDialog(
            backgroundImage, currentChannelModel, (Dialog) GUI.getTopParentContainer()
        );
      } else {
        logger.fatal("Unknown parent container");
        return false;
      }

      if (!obstacleFinderDialog.exitedOK) {
        return false;
      }

      /* Register obstacles */
      final boolean[][] obstacleArray = obstacleFinderDialog.obstacleArray;
      final int boxSize = obstacleFinderDialog.sizeSlider.getValue();

      // Create progress monitor
      final ProgressMonitor pm = new ProgressMonitor(
          GUI.getTopParentContainer(),
          "Registering obstacles",
          null,
          0,
          obstacleArray.length - 1
      );

      // Thread that will perform the work
      final Runnable runnable = new Runnable() {
        public void run() {
          try {
            /* Remove already existing obstacles */
            currentChannelModel.removeAllObstacles();

            int foundObstacles = 0;
            for (int x=0; x < obstacleArray.length; x++) {
              for (int y=0; y < (obstacleArray[0]).length; y++) {

                if (obstacleArray[x][y]) {
                  /* Register obstacle */
                  double realWidth = (boxSize * backgroundWidth) / backgroundImage.getWidth(null);
                  double realHeight = (boxSize * backgroundHeight) / backgroundImage.getHeight(null);
                  double realStartX = backgroundStartX + x * realWidth;
                  double realStartY = backgroundStartY + y * realHeight;

                  foundObstacles++;

                  if (realStartX + realWidth > backgroundStartX + backgroundWidth) {
                    realWidth = backgroundStartX + backgroundWidth - realStartX;
                  }
                  if (realStartY + realHeight > backgroundStartY + backgroundHeight) {
                    realHeight = backgroundStartY + backgroundHeight - realStartY;
                  }

                  currentChannelModel.addRectObstacle(
                      realStartX, realStartY,
                      realWidth, realHeight,
                      false
                  );
                }
              }

              /* Check if user has aborted */
              if (pm.isCanceled()) {
                return;
              }

              /* Update progress monitor */
              pm.setProgress(x);
              pm.setNote("After/Before merging: " +
                  currentChannelModel.getNumberOfObstacles() + "/" + foundObstacles);
            }

            currentChannelModel.notifySettingsChanged();
            AreaViewer.this.repaint();

          } catch (Exception ex) {
            if (pm.isCanceled()) {
              return;
            }
            logger.fatal("Obstacle adding exception: " + ex.getMessage());
            ex.printStackTrace();
            pm.close();
            return;
          }
          pm.close();
        }
      };

      /* Start thread */
      Thread thread = new Thread(runnable);
      thread.start();

      return true;
    }

  };

  class ObstacleFinderDialog extends JDialog {
    private static final long serialVersionUID = -8963997923536967296L;

    private NumberFormat intFormat = NumberFormat.getIntegerInstance();
    private BufferedImage imageToAnalyze = null;
    private BufferedImage obstacleImage = null;
    private JPanel canvasPanel = null;
    private boolean[][] obstacleArray = null;
    private boolean exitedOK = false;

    private JSlider redSlider, greenSlider, blueSlider, toleranceSlider, sizeSlider;

    /**
     * Listens to preview mouse motion event (when picking color)
     */
    private MouseMotionListener myMouseMotionListener = new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {

      }
      public void mouseMoved(MouseEvent e) {
        // Convert from mouse to image pixel position
        Point pixelPoint = new Point(
            (int) (e.getX() * (((double) imageToAnalyze.getWidth()) / ((double) canvasPanel.getWidth()))),
            (int) (e.getY() * (((double) imageToAnalyze.getHeight()) / ((double) canvasPanel.getHeight())))
        );

        // Fetch color
        int color = imageToAnalyze.getRGB(pixelPoint.x, pixelPoint.y);
        int red = (color & 0x00ff0000) >> 16;
        int green = (color & 0x0000ff00) >> 8;
        int blue = color & 0x000000ff;

        // Update sliders
        redSlider.setValue(red);
        redSlider.repaint();
        greenSlider.setValue(green);
        greenSlider.repaint();
        blueSlider.setValue(blue);
        blueSlider.repaint();
      }
    };

    /**
     * Listens to preview mouse event (when picking color)
     */
    private MouseListener myMouseListener = new MouseListener() {
      public void mouseClicked(MouseEvent e) {

      }
      public void mouseReleased(MouseEvent e) {

      }
      public void mouseEntered(MouseEvent e) {

      }
      public void mouseExited(MouseEvent e) {

      }
      public void mousePressed(MouseEvent e) {
        // Stop picking color again; remove mouse listeners and reset mouse cursor
        MouseListener[] allMouseListeners = canvasPanel.getMouseListeners();
        for (MouseListener mouseListener: allMouseListeners) {
          canvasPanel.removeMouseListener(mouseListener);
        }

        MouseMotionListener[] allMouseMotionListeners = canvasPanel.getMouseMotionListeners();
        for (MouseMotionListener mouseMotionListener: allMouseMotionListeners) {
          canvasPanel.removeMouseMotionListener(mouseMotionListener);
        }

        canvasPanel.setCursor(Cursor.getDefaultCursor());
      }
    };

      /**
       * Creates a new dialog for settings background parameters
       */
      protected ObstacleFinderDialog(Image currentImage, ChannelModel currentChannelModel, Frame frame) {
        super(frame, "Analyze for obstacles");
        setupDialog(currentImage, currentChannelModel);
      }
      protected ObstacleFinderDialog(Image currentImage, ChannelModel currentChannelModel, Window window) {
        super(window, "Analyze for obstacles");
        setupDialog(currentImage, currentChannelModel);
      }
      protected ObstacleFinderDialog(Image currentImage, ChannelModel currentChannelModel, Dialog dialog) {
        super(dialog, "Analyze for obstacles");
        setupDialog(currentImage, currentChannelModel);
      }

      private void setupDialog(Image currentImage, ChannelModel currentChannelModel) {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel tempPanel;
        JLabel tempLabel;
        JSlider tempSlider;
        JButton tempButton;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Dimension labelDimension = new Dimension(100, 20);

        // Convert Image to BufferedImage
        imageToAnalyze = new BufferedImage(
            currentImage.getWidth(this),
            currentImage.getHeight(this),
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = imageToAnalyze.createGraphics();
        g.drawImage(currentImage, 0, 0, null);

        // Prepare initial obstacle image
        obstacleImage = new BufferedImage(
            currentImage.getWidth(this),
            currentImage.getHeight(this),
            BufferedImage.TYPE_INT_ARGB
        );

        // Set layout and add components
        intFormat.setMinimumIntegerDigits(1);
        intFormat.setMaximumIntegerDigits(3);
        intFormat.setParseIntegerOnly(true);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Obstacle color
        tempPanel = new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
        tempPanel.add(tempLabel = new JLabel("Obstacle"));
        tempLabel.setPreferredSize(labelDimension);
        mainPanel.add(tempPanel);

        tempPanel = new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
        tempPanel.add(tempLabel = new JLabel("Red"));
        tempLabel.setPreferredSize(labelDimension);
        tempLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        tempPanel.add(tempSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 0));
        tempSlider.setMajorTickSpacing(50);
        tempSlider.setPaintTicks(true);
        tempSlider.setPaintLabels(true);
        mainPanel.add(tempPanel);
        redSlider = tempSlider;

        tempPanel = new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
        tempPanel.add(tempLabel = new JLabel("Green"));
        tempLabel.setPreferredSize(labelDimension);
        tempLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        tempPanel.add(tempSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 0));
        tempSlider.setMajorTickSpacing(50);
        tempSlider.setPaintTicks(true);
        tempSlider.setPaintLabels(true);
        mainPanel.add(tempPanel);
        greenSlider = tempSlider;

        tempPanel = new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
        tempPanel.add(tempLabel = new JLabel("Blue"));
        tempLabel.setPreferredSize(labelDimension);
        tempLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        tempPanel.add(tempSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 0));
        tempSlider.setMajorTickSpacing(50);
        tempSlider.setPaintTicks(true);
        tempSlider.setPaintLabels(true);
        mainPanel.add(tempPanel);
        blueSlider = tempSlider;

        // Tolerance
        tempPanel = new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
        tempPanel.add(tempLabel = new JLabel("Tolerance"));
        tempLabel.setPreferredSize(labelDimension);
        tempLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        tempPanel.add(tempSlider = new JSlider(JSlider.HORIZONTAL, 0, 128, 0));
        tempSlider.setMajorTickSpacing(25);
        tempSlider.setPaintTicks(true);
        tempSlider.setPaintLabels(true);
        mainPanel.add(tempPanel);
        toleranceSlider = tempSlider;

        // Obstacle size
        tempPanel = new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
        tempPanel.add(tempLabel = new JLabel("Obstacle size"));
        tempLabel.setPreferredSize(labelDimension);
        tempLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        tempPanel.add(tempSlider = new JSlider(JSlider.HORIZONTAL, 1, 40, 40));
        tempSlider.setInverted(true);
        tempSlider.setMajorTickSpacing(5);
        tempSlider.setPaintTicks(true);
        tempSlider.setPaintLabels(true);
        mainPanel.add(tempPanel);
        sizeSlider = tempSlider;

        // Buttons: Pick color, Preview obstacles etc.
        tempPanel = new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
        tempPanel.add(Box.createHorizontalGlue());
        tempPanel.add(tempButton = new JButton("Pick color"));
        tempButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // Set to color picker mode (if not already there)
            if (canvasPanel.getMouseMotionListeners().length == 0) {
              canvasPanel.addMouseListener(myMouseListener);
              canvasPanel.addMouseMotionListener(myMouseMotionListener);
              canvasPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }
          }
        });
        tempPanel.add(Box.createHorizontalStrut(5));
        tempPanel.add(tempButton = new JButton("Preview obstacles"));
        tempButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            obstacleImage = createObstacleImage();
            canvasPanel.repaint();
          }
        });
        mainPanel.add(tempPanel);

        mainPanel.add(Box.createVerticalStrut(10));

        // Preview image
        tempPanel = new JPanel() {
          private static final long serialVersionUID = 1L;
          public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(imageToAnalyze, 0, 0, getWidth(), getHeight(), this);
            g.drawImage(obstacleImage, 0, 0, getWidth(), getHeight(), this);
          }
        };
        tempPanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK), "Preview"));
        tempPanel.setPreferredSize(new Dimension(400, 400));
        tempPanel.setBackground(Color.CYAN);
        mainPanel.add(tempPanel);
        canvasPanel = tempPanel; // Saved in canvasPanel

        // Buttons: Cancel, OK
        tempPanel = new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
        tempPanel.add(Box.createHorizontalGlue());
        tempPanel.add(tempButton = new JButton("Cancel"));
        tempButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dispose();
          }
        });
        tempPanel.add(Box.createHorizontalStrut(5));
        tempPanel.add(tempButton = new JButton("OK"));
        tempButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            obstacleImage = createObstacleImage();
            exitedOK = true;
            dispose();
          }
        });
        mainPanel.add(tempPanel);

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(Box.createVerticalStrut(10));

        add(mainPanel);

        // Show dialog
        setModal(true);
        pack();
        setLocationRelativeTo(this.getParent());

        /* Make sure dialog is not too big */
        Rectangle maxSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        if (maxSize != null &&
            (getSize().getWidth() > maxSize.getWidth() ||
                getSize().getHeight() > maxSize.getHeight())) {
          Dimension newSize = new Dimension();
          newSize.height = Math.min((int) maxSize.getHeight(),
              (int) getSize().getHeight());
          newSize.width = Math.min((int) maxSize.getWidth(),
              (int) getSize().getWidth());
          setSize(newSize);
        }
        setVisible(true);
      }

      /**
       * Create obstacle image by analyzing current background image
       * and using the current obstacle color, size and tolerance.
       * This method also creates the boolean array obstacleArray.
       *
       * @return New obstacle image
       */
      private BufferedImage createObstacleImage() {
        int nrObstacles = 0;

        // Create new obstacle image all transparent (no obstacles)
        BufferedImage newObstacleImage = new BufferedImage(
            imageToAnalyze.getWidth(),
            imageToAnalyze.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        for (int x=0; x < imageToAnalyze.getWidth(); x++) {
          for (int y=0; y < imageToAnalyze.getHeight(); y++) {
            newObstacleImage.setRGB(x, y, 0x00000000);
          }
        }

        // Get target color to match against
        int targetRed = redSlider.getValue();
        int targetGreen = greenSlider.getValue();
        int targetBlue = blueSlider.getValue();

        // Get obstacle resolution and size
        int boxSize = sizeSlider.getValue();
        int tolerance = toleranceSlider.getValue();

        // Divide image into boxes and check each box for obstacles
        int arrayWidth = (int) Math.ceil((double) imageToAnalyze.getWidth() / (double) boxSize);
        int arrayHeight = (int) Math.ceil((double) imageToAnalyze.getHeight() / (double) boxSize);

        obstacleArray = new boolean[arrayWidth][arrayHeight];
        for (int x=0; x < imageToAnalyze.getWidth(); x+=boxSize) {
          for (int y=0; y < imageToAnalyze.getHeight(); y+=boxSize) {
            boolean boxIsObstacle = false;

            // Check all pixels in box for obstacles
            for (int xx=x; xx < x + boxSize && xx < imageToAnalyze.getWidth(); xx++) {
              for (int yy=y; yy < y + boxSize && yy < imageToAnalyze.getHeight(); yy++) {

                // Get current pixel color
                int color = imageToAnalyze.getRGB(xx, yy);
                int red = (color & 0x00ff0000) >> 16;
                int green = (color & 0x0000ff00) >> 8;
                int blue = color & 0x000000ff;

                // Calculate difference from target color
                int difference =
                  Math.abs(red - targetRed) +
                  Math.abs(green - targetGreen) +
                  Math.abs(blue - targetBlue);

                // If difference is small enough make this box an obstacle
                if (difference <= tolerance) {
                  boxIsObstacle = true;
                  break;
                }
              }
              if (boxIsObstacle) {
                break;
              }
            }

            // If box is obstacle, colorize it
            if (boxIsObstacle) {
              obstacleArray[x/boxSize][y/boxSize] = true;
              nrObstacles++;

              // Colorize all pixels in the box
              for (int xx=x; xx < x + boxSize && xx < imageToAnalyze.getWidth(); xx++) {
                for (int yy=y; yy < y + boxSize && yy < imageToAnalyze.getHeight(); yy++) {
                  newObstacleImage.setRGB(xx, yy, 0x9922ff22);
                }
              }
            } else {
              obstacleArray[x/boxSize][y/boxSize] = false;
            }


          }
        } // End of "divide into boxes" for-loop

        return newObstacleImage;
      }

    }

  /**
   * Listens to settings changes in the radio medium.
   */
  private Observer radioMediumSettingsObserver = new Observer() {
    public void update(Observable obs, Object obj) {
      // Clear selected radio (if any selected) and radio medium coverage
      selectedRadio = null;
      channelImage = null;
      trackModeButton.setEnabled(false);
      paintEnvironmentAction.setEnabled(false);
      canvas.repaint();
    }
  };

  /**
   * Listens to settings changes in the radio medium.
   */
  private Observer radioMediumActivityObserver = new Observer() {
    public void update(Observable obs, Object obj) {
      // Just remove any selected radio (it may have been removed)
      canvas.repaint();
    }
  };

  /**
   * Listens to settings changes in the channel model.
   */
  private Observer channelModelSettingsObserver = new Observer() {
    public void update(Observable obs, Object obj) {
      needToRepaintObstacleImage = true;
      canvas.repaint();
    }
  };

  /**
   * Returns a color corresponding to given value where higher values are more green, and lower values are more red.
   *
   * @param value Signal strength of received signal (dB)
   * @param lowBorder
   * @param highBorder
   * @return Integer containing standard ARGB color.
   */
  private int getColorOfSignalStrength(double value, double lowBorder, double highBorder) {
    double upperLimit = highBorder; // Best signal adds green
    double lowerLimit = lowBorder; // Bad signal adds red
    double intervalSize = (upperLimit - lowerLimit) / 2;
    double middleValue = lowerLimit + (upperLimit - lowerLimit) / 2;

    if (value > highBorder) {
      return 0xCC00FF00;
    }

    if (value < lowerLimit) {
      return 0xCCFF0000;
    }

    int red = 0, green = 0, blue = 0, alpha = 0xCC;

    // Upper limit (green)
    if (value > upperLimit - intervalSize) {
      green = (int) (255 - 255*(upperLimit - value)/intervalSize);
    }

    // Medium signal adds blue
    if (value > middleValue - intervalSize && value < middleValue + intervalSize) {
      blue = (int) (255 - 255*Math.abs(middleValue - value)/intervalSize);
    }

    // Bad signal adds red
    if (value < lowerLimit + intervalSize) {
      red = (int) (255 - 255*(value - lowerLimit)/intervalSize);
    }

    return (alpha << 24) | (red << 16) | (green << 8) | blue;
  }

  private void repaintRadioEnvironment() {
        // Get resolution of new image
        final Dimension resolution = new Dimension(
            resolutionSlider.getValue(),
            resolutionSlider.getValue()
        );

        // Abort if no radio selected
        if (selectedRadio == null) {
          channelImage = null;
          canvas.repaint();
          return;
        }

        // Get new location/size of area to attenuate
        final double startX = -currentPanX;
        final double startY = -currentPanY;
        final double width = canvas.getWidth() / currentZoomX;
        final double height = canvas.getHeight() / currentZoomY;

        // Get sending radio position
        Position radioPosition = selectedRadio.getPosition();
        final double radioX = radioPosition.getXCoordinate();
        final double radioY = radioPosition.getYCoordinate();

        // Create temporary image
        final BufferedImage tempChannelImage = new BufferedImage(resolution.width, resolution.height, BufferedImage.TYPE_INT_ARGB);

        // Save time for later analysis
        final long timeBeforeCalculating = System.currentTimeMillis();

        // Create progress monitor
        final ProgressMonitor pm = new ProgressMonitor(
            GUI.getTopParentContainer(),
            "Calculating channel attenuation",
            null,
            0,
            resolution.width - 1
        );

        // Thread that will perform the work
        final Runnable runnable = new Runnable() {
          public void run() {
            try {

              // Available signal strength intervals
              double lowestImageValue = Double.MAX_VALUE;
              double highestImageValue = -Double.MAX_VALUE;

              // Create image values (calculate each pixel)
              double[][] imageValues = new double[resolution.width][resolution.height];
              for (int x=0; x < resolution.width; x++) {
                for (int y=0; y < resolution.height; y++) {
                  final double xx = x;
                  final double yy = y;
                  TxPair txPair = new TxPair() {
                    public double getDistance() {
                      double w = getFromX() - getToX();
                      double h = getFromY() - getToY();
                      return Math.sqrt(w*w+h*h);
                    }
                    public double getFromX() { return radioX; }
                    public double getFromY() { return radioY; }
                    public double getToX() { return startX + width * xx/resolution.width; }
                    public double getToY() { return startY + height * yy/resolution.height; }
                    public double getTxPower() { return selectedRadio.getCurrentOutputPower(); }
                    public double getTxGain() {
                      if (!(selectedRadio instanceof DirectionalAntennaRadio)) {
                        return 0;
                      }
                      DirectionalAntennaRadio r = (DirectionalAntennaRadio)selectedRadio;
                      double txGain = r.getRelativeGain(r.getDirection() + getAngle(), getDistance());
                      //logger.debug("tx gain: " + txGain + " (angle " + String.format("%1.1f", Math.toDegrees(r.getDirection() + getAngle())) + ")");
                      return txGain;
                    }
                    public double getRxGain() {
                      return 0;
                    }
                  };

                  if (dataTypeToVisualize == ChannelModel.TransmissionData.SIGNAL_STRENGTH) {
                    // Attenuate
                    double[] signalStrength = currentChannelModel.getReceivedSignalStrength(txPair);

                    // Collecting signal strengths
                    if (signalStrength[0] < lowestImageValue) {
                      lowestImageValue = signalStrength[0];
                    }
                    if (signalStrength[0] > highestImageValue) {
                      highestImageValue = signalStrength[0];
                    }

                    imageValues[x][y] = signalStrength[0];

                  } else if (dataTypeToVisualize == ChannelModel.TransmissionData.SIGNAL_STRENGTH_VAR) {
                    // Attenuate
                    double[] signalStrength = currentChannelModel.getReceivedSignalStrength(txPair);

                    // Collecting variances
                    if (signalStrength[1] < lowestImageValue) {
                      lowestImageValue = signalStrength[1];
                    }
                    if (signalStrength[1] > highestImageValue) {
                      highestImageValue = signalStrength[1];
                    }

                    imageValues[x][y] = signalStrength[1];

                  } else if (dataTypeToVisualize == ChannelModel.TransmissionData.SNR) {
                    // Get signal to noise ratio
                    double[] snr = currentChannelModel.getSINR(
                        txPair,
                        -Double.MAX_VALUE
                    );

                    // Collecting signal to noise ratio
                    if (snr[0] < lowestImageValue) {
                      lowestImageValue = snr[0];
                    }
                    if (snr[0] > highestImageValue) {
                      highestImageValue = snr[0];
                    }

                    imageValues[x][y] = snr[0];

                  } else if (dataTypeToVisualize == ChannelModel.TransmissionData.SNR_VAR) {
                    // Get signal to noise ratio
                    double[] snr = currentChannelModel.getSINR(
                        txPair,
                        -Double.MAX_VALUE
                    );

                    // Collecting variances
                    if (snr[1] < lowestImageValue) {
                      lowestImageValue = snr[1];
                    }
                    if (snr[1] > highestImageValue) {
                      highestImageValue = snr[1];
                    }

                    imageValues[x][y] = snr[1];
                  } else if (dataTypeToVisualize == ChannelModel.TransmissionData.PROB_OF_RECEPTION) {
                    // Get probability of receiving a packet TODO What size? Does it matter?
                    double probability = currentChannelModel.getProbability(
                        txPair, -Double.MAX_VALUE
                    )[0];

                    // Collecting variances
                    if (probability < lowestImageValue) {
                      lowestImageValue = probability;
                    }
                    if (probability > highestImageValue) {
                      highestImageValue = probability;
                    }

                    imageValues[x][y] = probability;
                  } else if (dataTypeToVisualize == ChannelModel.TransmissionData.DELAY_SPREAD_RMS) {
                    // Get RMS delay spread of receiving a packet
                    double rmsDelaySpread = currentChannelModel.getRMSDelaySpread(
                        txPair
                    );

                    // Collecting variances
                    if (rmsDelaySpread < lowestImageValue) {
                      lowestImageValue = rmsDelaySpread;
                    }
                    if (rmsDelaySpread > highestImageValue) {
                      highestImageValue = rmsDelaySpread;
                    }

                    imageValues[x][y] = rmsDelaySpread;
                  }

                  // Check if the dialog has been canceled
                  if (pm.isCanceled()) {
                    return;
                  }

                  // Update progress
                  pm.setProgress(x);

                }
              }

              // Adjust coloring signal strength limit
              if (coloringIsFixed) {
                if (dataTypeToVisualize == ChannelModel.TransmissionData.SIGNAL_STRENGTH) {
                  lowestImageValue = -100;
                  highestImageValue = 0;
                } else if (dataTypeToVisualize == ChannelModel.TransmissionData.SIGNAL_STRENGTH_VAR) {
                  lowestImageValue = 0;
                  highestImageValue = 20;
                } else if (dataTypeToVisualize == ChannelModel.TransmissionData.SNR) {
                  lowestImageValue = -10;
                  highestImageValue = 30;
                } else if (dataTypeToVisualize == ChannelModel.TransmissionData.SNR_VAR) {
                  lowestImageValue = 0;
                  highestImageValue = 20;
                } else if (dataTypeToVisualize == ChannelModel.TransmissionData.PROB_OF_RECEPTION) {
                  lowestImageValue = 0;
                  highestImageValue = 1;
                } else if (dataTypeToVisualize == ChannelModel.TransmissionData.DELAY_SPREAD_RMS) {
                  lowestImageValue = 0;
                  highestImageValue = 5;
                }
              }

              // Save coloring high-low interval
              coloringHighest = highestImageValue;
              coloringLowest = lowestImageValue;

              // Create image
              for (int x=0; x < resolution.width; x++) {
                for (int y=0; y < resolution.height; y++) {

                  tempChannelImage.setRGB(
                      x,
                      y,
                      getColorOfSignalStrength(imageValues[x][y], lowestImageValue, highestImageValue)
                  );
                }
              }
              logger.info("Attenuating area done, time=" + (System.currentTimeMillis() - timeBeforeCalculating));

              // Repaint to show the new channel propagation
              channelStartX = startX;
              channelStartY = startY;
              channelWidth = width;
              channelHeight = height;
              channelImage = tempChannelImage;

              AreaViewer.this.repaint();
              coloringIntervalPanel.repaint();

            } catch (Exception ex) {
              if (pm.isCanceled()) {
                return;
              }
              logger.fatal("Attenuation aborted: " + ex);
              ex.printStackTrace();
              pm.close();
            }
            pm.close();
          }
        };

        // Start thread
        attenuatorThread = new Thread(runnable);
        attenuatorThread.start();
  }

  /**
   * Repaint the canvas
   * @param g2d Current graphics to paint on
   */
  protected void repaintCanvas(Graphics2D g2d) {
    AffineTransform originalTransform = g2d.getTransform();

    // Create "real-world" transformation (scaled 100 times to reduce double->int rounding errors)
    g2d.scale(currentZoomX, currentZoomY);
    g2d.translate(currentPanX, currentPanY);
    AffineTransform realWorldTransform = g2d.getTransform();
    g2d.scale(0.01, 0.01);
    AffineTransform realWorldTransformScaled = g2d.getTransform();

    // -- Draw background image if any --
    if (drawBackgroundImage && backgroundImage != null) {
      g2d.setTransform(realWorldTransformScaled);

      g2d.drawImage(backgroundImage,
          (int) (backgroundStartX * 100.0),
          (int) (backgroundStartY * 100.0),
          (int) (backgroundWidth * 100.0),
          (int) (backgroundHeight * 100.0),
          this);
    }

    // -- Draw calculated obstacles --
    if (drawCalculatedObstacles) {

      // (Re)create obstacle image if needed
      if (obstacleImage == null || needToRepaintObstacleImage) {

        // Abort if no obstacles exist
        if (currentChannelModel.getNumberOfObstacles() > 0) {

          // Get bounds of obstacles
          obstacleStartX = currentChannelModel.getObstacle(0).getMinX();
          obstacleStartY = currentChannelModel.getObstacle(0).getMinY();
          obstacleWidth = currentChannelModel.getObstacle(0).getMaxX();
          obstacleHeight = currentChannelModel.getObstacle(0).getMaxY();

          double tempVal = 0;
          for (int i=0; i < currentChannelModel.getNumberOfObstacles(); i++) {
            if ((tempVal = currentChannelModel.getObstacle(i).getMinX()) < obstacleStartX) {
              obstacleStartX = tempVal;
            }
            if ((tempVal = currentChannelModel.getObstacle(i).getMinY()) < obstacleStartY) {
              obstacleStartY = tempVal;
            }
            if ((tempVal = currentChannelModel.getObstacle(i).getMaxX()) > obstacleWidth) {
              obstacleWidth = tempVal;
            }
            if ((tempVal = currentChannelModel.getObstacle(i).getMaxY()) > obstacleHeight) {
              obstacleHeight = tempVal;
            }
          }
          obstacleWidth -= obstacleStartX;
          obstacleHeight -= obstacleStartY;

          // Create new obstacle image
          BufferedImage tempObstacleImage;
          if (backgroundImage != null) {
            tempObstacleImage = new BufferedImage(
                Math.max(600, backgroundImage.getWidth(null)),
                Math.max(600, backgroundImage.getHeight(null)),
                BufferedImage.TYPE_INT_ARGB
            );
          } else {
            tempObstacleImage = new BufferedImage(
                600,
                600,
                BufferedImage.TYPE_INT_ARGB
            );
          }

          Graphics2D obstacleGraphics = (Graphics2D) tempObstacleImage.getGraphics();

          // Set real world transform
          obstacleGraphics.scale(
              tempObstacleImage.getWidth()/obstacleWidth,
              tempObstacleImage.getHeight()/obstacleHeight
          );
          obstacleGraphics.translate(-obstacleStartX, -obstacleStartY);


          // Paint all obstacles
          obstacleGraphics.setColor(new Color(0, 0, 0, 128));

          // DEBUG: Use random obstacle color to distinguish different obstacles
          //Random random = new Random();

          for (int i=0; i < currentChannelModel.getNumberOfObstacles(); i++) {
            //obstacleGraphics.setColor((new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), 128)));
            obstacleGraphics.fill(currentChannelModel.getObstacle(i));
          }
          obstacleImage = tempObstacleImage;

        } else {

          // No obstacles exist - create dummy obstacle image
          obstacleStartX = 0;
          obstacleStartY = 0;
          obstacleWidth = 1;
          obstacleHeight = 1;
          obstacleImage = new BufferedImage(
              1,
              1,
              BufferedImage.TYPE_INT_ARGB
          );
        }

        needToRepaintObstacleImage = false;
      }

      // Painting in real world coordinates
      g2d.setTransform(realWorldTransformScaled);

      g2d.drawImage(obstacleImage,
          (int) (obstacleStartX * 100.0),
          (int) (obstacleStartY * 100.0),
          (int) (obstacleWidth * 100.0),
          (int) (obstacleHeight * 100.0),
          this);
    }

    // -- Draw channel probabilities if calculated --
    if (drawChannelProbabilities && channelImage != null) {
      g2d.setTransform(realWorldTransformScaled);

      g2d.drawImage(channelImage,
          (int) (channelStartX * 100.0),
          (int) (channelStartY * 100.0),
          (int) (channelWidth * 100.0),
          (int) (channelHeight * 100.0),
          this);
    }

    // -- Draw radios --
    if (drawRadios) {
      for (int i=0; i < currentRadioMedium.getRegisteredRadioCount(); i++) {
        g2d.setStroke(new BasicStroke((float) 0.0));
        g2d.setTransform(realWorldTransform);

        // Translate to real world radio position
        Radio radio = currentRadioMedium.getRegisteredRadio(i);
        Position radioPosition = radio.getPosition();
        g2d.translate(
            radioPosition.getXCoordinate(),
            radioPosition.getYCoordinate()
        );

        // Fetch current translation
        double xPos = g2d.getTransform().getTranslateX();
        double yPos = g2d.getTransform().getTranslateY();

        // Jump to identity transform and paint without scaling
        g2d.setTransform(new AffineTransform());

        if (selectedRadio == radio) {
          g2d.setColor(new Color(255, 0, 0, 100));
          g2d.fillRect(
              (int) xPos - antennaImage.getWidth(this)/2,
              (int) yPos - antennaImage.getHeight(this)/2,
              antennaImage.getWidth(this),
              antennaImage.getHeight(this)
          );
          g2d.setColor(Color.BLUE);
          g2d.drawRect(
              (int) xPos - antennaImage.getWidth(this)/2,
              (int) yPos - antennaImage.getHeight(this)/2,
              antennaImage.getWidth(this),
              antennaImage.getHeight(this)
          );
        }

        g2d.drawImage(antennaImage, (int) xPos - antennaImage.getWidth(this)/2, (int) yPos - antennaImage.getHeight(this)/2, this);

      }
    }

    // -- Draw radio activity --
//    if (drawRadioActivity) {
//      for (RadioConnection connection: currentRadioMedium.getActiveConnections()) {
//        Position sourcePosition = connection.getSource().getPosition();
//
//        // Paint scaled (otherwise bad rounding to integers may occur)
//        g2d.setTransform(realWorldTransformScaled);
//        g2d.setStroke(new BasicStroke((float) 0.0));
//
//        for (Radio receivingRadio: connection.getDestinations()) {
//          g2d.setColor(Color.GREEN);
//
//          // Get source and destination coordinates
//          Position destinationPosition = receivingRadio.getPosition();
//
//          g2d.draw(new Line2D.Double(
//              sourcePosition.getXCoordinate()*100.0,
//              sourcePosition.getYCoordinate()*100.0,
//              destinationPosition.getXCoordinate()*100.0,
//              destinationPosition.getYCoordinate()*100.0
//          ));
//        }
//
//        for (Radio interferedRadio: connection.getInterfered()) {
//          g2d.setColor(Color.RED);
//
//          // Get source and destination coordinates
//          Position destinationPosition = interferedRadio.getPosition();
//
//          g2d.draw(new Line2D.Double(
//              sourcePosition.getXCoordinate()*100.0,
//              sourcePosition.getYCoordinate()*100.0,
//              destinationPosition.getXCoordinate()*100.0,
//              destinationPosition.getYCoordinate()*100.0
//          ));
//        }
//
//        g2d.setColor(Color.BLUE);
//        g2d.setTransform(realWorldTransform);
//
//        g2d.translate(
//            sourcePosition.getXCoordinate(),
//            sourcePosition.getYCoordinate()
//        );
//
//        // Fetch current translation
//        double xPos = g2d.getTransform().getTranslateX();
//        double yPos = g2d.getTransform().getTranslateY();
//
//        // Jump to identity transform and paint without scaling
//        g2d.setTransform(new AffineTransform());
//
//        g2d.fillOval(
//            (int) xPos,
//            (int) yPos,
//            5,
//            5
//        );
//
//      }
//    }

    // -- Draw scale arrow --
    if (drawScaleArrow) {
      g2d.setStroke(new BasicStroke((float) .0));

      g2d.setColor(Color.BLACK);

      // Decide on scale comparator
      double currentArrowDistance = 0.1; // in meters
      if (currentZoomX < canvas.getWidth() / 2) {
        currentArrowDistance = 0.1; // 0.1m
      }
      if (currentZoomX < canvas.getWidth() / 2) {
        currentArrowDistance = 1; // 1m
      }
      if (10 * currentZoomX < canvas.getWidth() / 2) {
        currentArrowDistance = 10; // 10m
      }
      if (100 * currentZoomX < canvas.getWidth() / 2) {
        currentArrowDistance = 100; // 100m
      }
      if (1000 * currentZoomX < canvas.getWidth() / 2) {
        currentArrowDistance = 1000; // 100m
      }

      // "Arrow" points
      int pixelArrowLength = (int) (currentArrowDistance * currentZoomX);
      int xPoints[] = new int[] { -pixelArrowLength, -pixelArrowLength, -pixelArrowLength, 0, 0, 0 };
      int yPoints[] = new int[] { -5, 5, 0, 0, -5, 5 };

      // Paint arrow and text
      g2d.setTransform(originalTransform);
      g2d.translate(canvas.getWidth() - 120, canvas.getHeight() - 20);
      g2d.drawString(currentArrowDistance + "m", -30, -10);
      g2d.drawPolyline(xPoints, yPoints, xPoints.length);
    }

    // -- Draw tracked components (if any) --
    if (!currentSimulation.isRunning() && inTrackMode && trackedComponents != null) {
      g2d.setTransform(realWorldTransformScaled);
      g2d.setStroke(new BasicStroke((float) 0.0));

      Random random = new Random(); /* Do not use main random generator */
      for (Line2D l: trackedComponents.components) {
        g2d.setColor(new Color(255, random.nextInt(255), random.nextInt(255), 255));
        Line2D newLine = new Line2D.Double(
            l.getX1()*100.0,
            l.getY1()*100.0,
            l.getX2()*100.0,
            l.getY2()*100.0
        );
        g2d.draw(newLine);
      }
    }

    g2d.setTransform(originalTransform);
  }

  /**
   * Tracks an on-screen position and returns all hit radios.
   * May for example be used by a mouse listener to determine
   * if user clicked on a radio.
   *
   * @param clickedPoint On-screen position
   * @return All hit radios
   */
  protected ArrayList<Radio> trackClickedRadio(Point clickedPoint) {
    ArrayList<Radio> hitRadios = new ArrayList<Radio>();
    if (currentRadioMedium.getRegisteredRadioCount() == 0) {
      return null;
    }

    double realIconHalfWidth = antennaImage.getWidth(this) / (currentZoomX*2.0);
    double realIconHalfHeight = antennaImage.getHeight(this) / (currentZoomY*2.0);
    double realClickedX = clickedPoint.x / currentZoomX - currentPanX;
    double realClickedY = clickedPoint.y / currentZoomY - currentPanY;

    for (int i=0; i < currentRadioMedium.getRegisteredRadioCount(); i++) {
      Radio testRadio = currentRadioMedium.getRegisteredRadio(i);
      Position testPosition = testRadio.getPosition();

      if (realClickedX > testPosition.getXCoordinate() - realIconHalfWidth &&
          realClickedX < testPosition.getXCoordinate() + realIconHalfWidth &&
          realClickedY > testPosition.getYCoordinate() - realIconHalfHeight &&
          realClickedY < testPosition.getYCoordinate() + realIconHalfHeight) {
        hitRadios.add(testRadio);
      }
    }

    if (hitRadios.size() == 0) {
      return null;
    }
    return hitRadios;
  }

  public void closePlugin() {
    // Remove all our observers

    if (currentChannelModel != null && channelModelSettingsObserver != null) {
      currentChannelModel.deleteSettingsObserver(channelModelSettingsObserver);
    } else {
      logger.fatal("Could not remove observer: " + channelModelSettingsObserver);
    }

    if (currentRadioMedium != null && radioMediumSettingsObserver != null) {
      currentRadioMedium.deleteSettingsObserver(radioMediumSettingsObserver);
    } else {
      logger.fatal("Could not remove observer: " + radioMediumSettingsObserver);
    }

    if (currentRadioMedium != null && radioMediumActivityObserver != null) {
      currentRadioMedium.deleteRadioMediumObserver(radioMediumActivityObserver);
    } else {
      logger.fatal("Could not remove observer: " + radioMediumActivityObserver);
    }
  }


  /**
   * Returns XML elements representing the current configuration.
   *
   * @see #setConfigXML(Collection)
   * @return XML element collection
   */
  public Collection<Element> getConfigXML() {
    ArrayList<Element> config = new ArrayList<Element>();
    Element element;

    /* Selected mote */
    if (selectedRadio != null) {
      element = new Element("selected");
      element.setAttribute("mote", "" + selectedRadio.getMote().getID());
      config.add(element);
    }

    // Controls visible
    element = new Element("controls_visible");
    element.setText(Boolean.toString(showSettingsBox.isSelected()));
    config.add(element);

    // Viewport
    element = new Element("zoom_x");
    element.setText(Double.toString(currentZoomX));
    config.add(element);
    element = new Element("zoom_y");
    element.setText(Double.toString(currentZoomY));
    config.add(element);
    element = new Element("pan_x");
    element.setText(Double.toString(currentPanX));
    config.add(element);
    element = new Element("pan_y");
    element.setText(Double.toString(currentPanY));
    config.add(element);

    // Components shown
    element = new Element("show_background");
    element.setText(Boolean.toString(drawBackgroundImage));
    config.add(element);
    element = new Element("show_obstacles");
    element.setText(Boolean.toString(drawCalculatedObstacles));
    config.add(element);
    element = new Element("show_channel");
    element.setText(Boolean.toString(drawChannelProbabilities));
    config.add(element);
    element = new Element("show_radios");
    element.setText(Boolean.toString(drawRadios));
    config.add(element);
//    element = new Element("show_activity");
//    element.setText(Boolean.toString(drawRadioActivity));
//    config.add(element);
    element = new Element("show_arrow");
    element.setText(Boolean.toString(drawScaleArrow));
    config.add(element);

    // Visualization type
    element = new Element("vis_type");
    element.setText(visTypeSelectionGroup.getSelection().getActionCommand());
    config.add(element);

    // Background image
    if (backgroundImageFile != null) {
      element = new Element("background_image");
      element.setText(backgroundImageFile.getPath());
      config.add(element);

      element = new Element("back_start_x");
      element.setText(Double.toString(backgroundStartX));
      config.add(element);
      element = new Element("back_start_y");
      element.setText(Double.toString(backgroundStartY));
      config.add(element);
      element = new Element("back_width");
      element.setText(Double.toString(backgroundWidth));
      config.add(element);
      element = new Element("back_height");
      element.setText(Double.toString(backgroundHeight));
      config.add(element);
    }

    // Resolution
    element = new Element("resolution");
    element.setText(Integer.toString(resolutionSlider.getValue()));
    config.add(element);

    return config;
  }

  /**
   * Sets the configuration depending on the given XML elements.
   *
   * @see #getConfigXML()
   * @param configXML
   *          Config XML elements
   * @return True if config was set successfully, false otherwise
   */
  public boolean setConfigXML(Collection<Element> configXML, boolean visAvailable) {
    for (Element element : configXML) {
      if (element.getName().equals("selected")) {
        int id = Integer.parseInt(element.getAttributeValue("mote"));
        selectedRadio =  currentSimulation.getMoteWithID(id).getInterfaces().getRadio();
        trackModeButton.setEnabled(true);
        paintEnvironmentAction.setEnabled(true);
      } else if (element.getName().equals("controls_visible")) {
        showSettingsBox.setSelected(Boolean.parseBoolean(element.getText()));
        canvasModeHandler.actionPerformed(new ActionEvent(showSettingsBox,
            ActionEvent.ACTION_PERFORMED, showSettingsBox.getActionCommand()));
      } else if (element.getName().equals("zoom_x")) {
        currentZoomX = Double.parseDouble(element.getText());
      } else if (element.getName().equals("zoom_y")) {
        currentZoomY = Double.parseDouble(element.getText());
      } else if (element.getName().equals("pan_x")) {
        currentPanX = Double.parseDouble(element.getText());
      } else if (element.getName().equals("pan_y")) {
        currentPanY = Double.parseDouble(element.getText());
      } else if (element.getName().equals("show_background")) {
        backgroundCheckBox.setSelected(Boolean.parseBoolean(element.getText()));
        selectGraphicsHandler.actionPerformed(new ActionEvent(backgroundCheckBox,
            ActionEvent.ACTION_PERFORMED, backgroundCheckBox.getActionCommand()));
      } else if (element.getName().equals("show_obstacles")) {
        obstaclesCheckBox.setSelected(Boolean.parseBoolean(element.getText()));
        selectGraphicsHandler.actionPerformed(new ActionEvent(obstaclesCheckBox,
            ActionEvent.ACTION_PERFORMED, obstaclesCheckBox.getActionCommand()));
      } else if (element.getName().equals("show_channel")) {
        channelCheckBox.setSelected(Boolean.parseBoolean(element.getText()));
        selectGraphicsHandler.actionPerformed(new ActionEvent(channelCheckBox,
            ActionEvent.ACTION_PERFORMED, channelCheckBox.getActionCommand()));
      } else if (element.getName().equals("show_radios")) {
        radiosCheckBox.setSelected(Boolean.parseBoolean(element.getText()));
        selectGraphicsHandler.actionPerformed(new ActionEvent(radiosCheckBox,
            ActionEvent.ACTION_PERFORMED, radiosCheckBox.getActionCommand()));
      } else if (element.getName().equals("show_activity")) {
//        radioActivityCheckBox.setSelected(Boolean.parseBoolean(element.getText()));
//        selectGraphicsHandler.actionPerformed(new ActionEvent(radioActivityCheckBox,
//            ActionEvent.ACTION_PERFORMED, radioActivityCheckBox.getActionCommand()));
      } else if (element.getName().equals("show_arrow")) {
        arrowCheckBox.setSelected(Boolean.parseBoolean(element.getText()));
        selectGraphicsHandler.actionPerformed(new ActionEvent(arrowCheckBox,
            ActionEvent.ACTION_PERFORMED, arrowCheckBox.getActionCommand()));
      } else if (element.getName().equals("vis_type")) {
        String visTypeIdentifier = element.getText();
        Enumeration<AbstractButton> buttonEnum = visTypeSelectionGroup.getElements();
        while (buttonEnum.hasMoreElements()) {
          AbstractButton button = buttonEnum.nextElement();
          if (button.getActionCommand().equals(visTypeIdentifier)) {
            visTypeSelectionGroup.setSelected(button.getModel(), true);
            button.getActionListeners()[0]
                .actionPerformed(new ActionEvent(button,
                    ActionEvent.ACTION_PERFORMED, button.getActionCommand()));
          }
        }
      } else if (element.getName().equals("background_image")) {
        backgroundImageFile = new File(element.getText());
        if (backgroundImageFile.exists()) {
          Toolkit toolkit = Toolkit.getDefaultToolkit();
          backgroundImage = toolkit.getImage(backgroundImageFile.getAbsolutePath());

          MediaTracker tracker = new MediaTracker(canvas);
          tracker.addImage(backgroundImage, 1);

          try {
            tracker.waitForAll();
          } catch (InterruptedException ex) {
            logger.fatal("Interrupted during image loading, aborting");
            backgroundImage = null;
          }

        }
      } else if (element.getName().equals("back_start_x")) {
        backgroundStartX = Double.parseDouble(element.getText());
      } else if (element.getName().equals("back_start_y")) {
        backgroundStartY = Double.parseDouble(element.getText());
      } else if (element.getName().equals("back_width")) {
        backgroundWidth = Double.parseDouble(element.getText());
      } else if (element.getName().equals("back_height")) {
        backgroundHeight = Double.parseDouble(element.getText());
      } else if (element.getName().equals("resolution")) {
        resolutionSlider.setValue(Integer.parseInt(element.getText()));
      } else {
        logger.fatal("Unknown configuration value: " + element.getName());
      }
    }

    canvas.repaint();
    return true;
  }


=======
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package eu.udig.style.jgrass.legend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.refractions.udig.ui.graphics.SLDs;

import org.eclipse.swt.widgets.Display;
import org.geotools.data.DataUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.LiteShape;
import org.geotools.renderer.style.GraphicStyle2D;
import org.geotools.renderer.style.MarkStyle2D;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.Style2D;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.util.NumberRange;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.expression.Expression;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Drawing utility package - make your own previews and glyphs with java 2d.
 */
public final class Java2dDrawing {
    private GeometryFactory gf = new GeometryFactory();

    private Java2dDrawing() {
        // prevent subclassing
    }

    /**
     * Retrieve the default Drawing implementation.
     * 
     * @return Drawing ready for use
     */
    public static Java2dDrawing create() {
        return new Java2dDrawing();
    }

    // THIS
    public void drawDirect( BufferedImage bI, Display display, SimpleFeature feature, Rule rule ) {
        AffineTransform worldToScreenTransform = new AffineTransform();

        drawFeature(bI, feature, worldToScreenTransform, false, getSymbolizers(rule), null);
    }

    public void drawFeature( BufferedImage bI, SimpleFeature feature,
            AffineTransform worldToScreenTransform, boolean drawVertices, MathTransform mt ) {
        if (feature == null)
            return;
        drawFeature(bI, feature, worldToScreenTransform, drawVertices, getSymbolizers(feature), mt);
    }

    public void drawFeature( BufferedImage bI, SimpleFeature feature,
            AffineTransform worldToScreenTransform ) {
        if (feature == null)
            return;
        drawFeature(bI, feature, worldToScreenTransform, false, getSymbolizers(feature), null);
    }

    public void drawFeature( BufferedImage bI, SimpleFeature feature,
            AffineTransform worldToScreenTransform, Style style ) {
        if (feature == null)
            return;
        drawFeature(bI, feature, worldToScreenTransform, false, getSymbolizers(style), null);
    }

    public void drawFeature( BufferedImage bI, SimpleFeature feature, Style style,
            AffineTransform worldToScreenTransform ) {
        if (feature == null)
            return;

        drawFeature(bI, feature, worldToScreenTransform, false, getSymbolizers(style), null);
    }

    @SuppressWarnings("deprecation")
    Symbolizer[] getSymbolizers( Style style ) {
        List<Symbolizer> symbs = new ArrayList<Symbolizer>();
        FeatureTypeStyle[] styles = style.getFeatureTypeStyles();
        for( int i = 0; i < styles.length; i++ ) {
            FeatureTypeStyle fstyle = styles[i];
            Rule[] rules = fstyle.getRules();
            for( int j = 0; j < rules.length; j++ ) {
                Rule rule = rules[j];
                symbs.addAll(Arrays.asList(rule.getSymbolizers()));
            }
        }
        return symbs.toArray(new Symbolizer[symbs.size()]);
    }

    Symbolizer[] getSymbolizers( Rule rule ) {
        List<Symbolizer> symbs = new ArrayList<Symbolizer>();
        symbs.addAll(Arrays.asList(rule.getSymbolizers()));
        return symbs.toArray(new Symbolizer[symbs.size()]);
    }

    public void drawFeature( BufferedImage bI, SimpleFeature feature,
            AffineTransform worldToScreenTransform, boolean drawVertices, Symbolizer[] symbs,
            MathTransform mt ) {

        LiteShape shape = new LiteShape(null, worldToScreenTransform, false);
        if (symbs == null)
            return;
        for( int m = 0; m < symbs.length; m++ ) {
            drawFeature(bI, feature, worldToScreenTransform, drawVertices, symbs[m], mt, shape);
        }
    }

    public void drawFeature( BufferedImage bI, SimpleFeature feature,
            AffineTransform worldToScreenTransform, boolean drawVertices, Symbolizer symbolizer,
            MathTransform mathTransform, LiteShape shape ) {
        Graphics2D g2d = (Graphics2D) bI.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (symbolizer instanceof RasterSymbolizer) {
            // TODO
        } else {
            Geometry g = findGeometry(feature, symbolizer);
            if (g == null)
                return;
            if (mathTransform != null) {
                try {
                    g = JTS.transform(g, mathTransform);
                } catch (Exception e) {
                    // do nothing
                }
            }
            shape.setGeometry(g);

            paint(bI, feature, shape, symbolizer);
            if (drawVertices) {
                double averageDistance = 0;
                Coordinate[] coords = g.getCoordinates();
                java.awt.Point oldP = worldToPixel(coords[0], worldToScreenTransform);
                for( int i = 1; i < coords.length; i++ ) {
                    Coordinate coord = coords[i];
                    java.awt.Point p = worldToPixel(coord, worldToScreenTransform);
                    averageDistance += p.distance(oldP) / i;
                    oldP = p;
                }
                int pixels = 1;
                if (averageDistance > 20)
                    pixels = 3;
                if (averageDistance > 60)
                    pixels = 5;
                if (pixels > 1) {
                    g2d.setColor(Color.RED);
                    for( int i = 0; i < coords.length; i++ ) {
                        Coordinate coord = coords[i];
                        java.awt.Point p = worldToPixel(coord, worldToScreenTransform);
                        g2d
                                .fillRect(p.x - (pixels - 1) / 2, p.y - (pixels - 1) / 2, pixels,
                                        pixels);
                    }
                }
            }
        }
    }

    public java.awt.Point worldToPixel( Coordinate coord, AffineTransform worldToScreenTransform ) {
        Point2D w = new Point2D.Double(coord.x, coord.y);
        AffineTransform at = worldToScreenTransform;
        Point2D p = at.transform(w, new Point2D.Double());
        return new java.awt.Point((int) p.getX(), (int) p.getY());
    }

    /** Unsure if this is the paint for the border, or the fill? */
    private void paint( BufferedImage bI, SimpleFeature feature, LiteShape shape, Symbolizer symb ) {

        Graphics2D g2d = (Graphics2D) bI.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (symb instanceof PolygonSymbolizer) {
            PolygonSymbolizer polySymb = (PolygonSymbolizer) symb;
            Color stroke = SLDs.polyColor(polySymb);
            double opacity = SLDs.polyFillOpacity(polySymb);
            Color fill = SLDs.polyFill(polySymb);

            int width = SLDs.width(SLDs.stroke(polySymb));
            if (width == SLDs.NOTFOUND)
                width = 1;

            if (Double.isNaN(opacity))
                opacity = 1.0;
            if (fill != null) {
                fill = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(),
                        (int) (255 * opacity));
                g2d.setColor(fill);
                g2d.fill(shape);
            }
            if (stroke != null) {
                g2d.setColor(stroke);
                g2d.setStroke(new BasicStroke(width));
                g2d.draw(shape);
            }
        }
        if (symb instanceof LineSymbolizer) {
            LineSymbolizer lineSymbolizer = (LineSymbolizer) symb;
            Color c = SLDs.color(lineSymbolizer);
            int w = SLDs.width(lineSymbolizer);
            if (c != null && w > 0) {
                g2d.setColor(c);
                g2d.setStroke(new BasicStroke(w));
                g2d.draw(shape);
            }
        }
        if (symb instanceof PointSymbolizer) {
            int imgH = bI.getHeight();
            int imgW = bI.getWidth();
            PointSymbolizer pointSymbolizer = (PointSymbolizer) symb;

            Color c = SLDs.pointColor(pointSymbolizer);
            Color fill = SLDs.pointFill(pointSymbolizer);
            int width = SLDs.width(SLDs.stroke(pointSymbolizer));
            if (width < 1)
                width = 1;
            float[] point = new float[6];
            shape.getPathIterator(null).currentSegment(point);
            SLDStyleFactory styleFactory = new SLDStyleFactory();
            Style2D tmp = styleFactory.createStyle(feature, pointSymbolizer, new NumberRange(0,
                    imgH));

            if (tmp instanceof MarkStyle2D) {

                MarkStyle2D style = (MarkStyle2D) tmp;
                style.setSize(imgH / 2);
                Shape shape2 = style.getTransformedShape(imgH / 2f, imgH / 2f);

                if (c == null && fill == null) {
                    g2d.setColor(Color.GRAY);
                    g2d.fill(shape2);
                }

                if (fill != null) {
                    g2d.setColor(fill);
                    g2d.fill(shape2);
                } else {
                    g2d.setColor(Color.GRAY);
                    g2d.fill(shape2);
                }
                if (c != null) {
                    g2d.setStroke(new BasicStroke(width));
                    g2d.setColor(c);
                    g2d.draw(shape2);
                } else {
                    g2d.setStroke(new BasicStroke(width));
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.draw(shape2);
                }
            } else if (tmp instanceof GraphicStyle2D) {
                GraphicStyle2D style = (GraphicStyle2D) tmp;

                float rotation = style.getRotation();

                g2d.setTransform(AffineTransform.getRotateInstance(rotation));

                BufferedImage image = style.getImage();
                g2d.drawImage(image, (int) (point[0] - ((double) image.getWidth()) / (double) 2),
                        (int) (point[1] - ((double) image.getHeight()) / (double) 2), null);
            }
        }
    }
    public static Symbolizer[] getSymbolizers( SimpleFeature feature ) {
        return getSymbolizers((Class< ? extends Geometry>) feature.getDefaultGeometry().getClass(),
                Color.RED);
    }

    public static Symbolizer[] getSymbolizers( Class< ? extends Geometry> type, Color baseColor ) {
        return getSymbolizers(type, baseColor, true);
    }
    public static Symbolizer[] getSymbolizers( Class< ? extends Geometry> type, Color baseColor,
            boolean useTransparency ) {

        StyleBuilder builder = new StyleBuilder();
        Symbolizer[] syms = new Symbolizer[1];
        if (LineString.class.isAssignableFrom(type) || MultiLineString.class.isAssignableFrom(type))
            syms[0] = builder.createLineSymbolizer(baseColor, 2);
        if (Point.class.isAssignableFrom(type) || MultiPoint.class.isAssignableFrom(type)) {
            PointSymbolizer point = builder.createPointSymbolizer(builder.createGraphic());
            point.getGraphic().getMarks()[0].setSize((Expression) CommonFactoryFinder
                    .getFilterFactory(GeoTools.getDefaultHints()).literal(10));
            point.getGraphic().getMarks()[0].setFill(builder.createFill(baseColor));
            syms[0] = point;
        }
        if (Polygon.class.isAssignableFrom(type) || MultiPolygon.class.isAssignableFrom(type)) {
            syms[0] = builder.createPolygonSymbolizer(builder.createStroke(baseColor, 2), builder
                    .createFill(baseColor, useTransparency ? .6 : 1.0));
        }
        return syms;
    }

    /**
     * Finds the geometric attribute requested by the symbolizer.
     * 
     * @param feature The victim
     * @param symbolizer The symbolizer
     * @param style the resolved style for the specified victim
     * @return The geometry requested in the symbolizer, or the default geometry if none is
     *         specified
     */
    private com.vividsolutions.jts.geom.Geometry findGeometry( SimpleFeature feature,
            Symbolizer symbolizer ) {
        String geomName = getGeometryPropertyName(symbolizer);
        // get the geometry
        com.vividsolutions.jts.geom.Geometry geometry;
        if (geomName == null || feature.getType().getDescriptor(geomName) == null) {
            geometry = (Geometry) feature.getDefaultGeometry();
        } else {
            geometry = (com.vividsolutions.jts.geom.Geometry) feature.getAttribute(geomName);
        }
        if (geometry == null) {
            return null; // nothing to see here
        }
        // if the symbolizer is a point or text symbolizer generate a suitable
        // location to place the
        // point in order to avoid recomputing that location at each rendering
        // step

        if ((symbolizer instanceof PointSymbolizer || symbolizer instanceof TextSymbolizer)
                && !(geometry instanceof Point)) {
            if (geometry instanceof LineString && !(geometry instanceof LinearRing)) {
                // use the mid point to represent the point/text symbolizer
                // anchor
                Coordinate[] coordinates = geometry.getCoordinates();
                Coordinate start = coordinates[0];
                Coordinate end = coordinates[1];
                Coordinate mid = new Coordinate((start.x + end.x) / 2, (start.y + end.y) / 2);
                geometry = geometry.getFactory().createPoint(mid);
            } else {
                // otherwise use the centroid of the polygon
                geometry = geometry.getCentroid();
            }
        }
        return geometry;
    }
    private String getGeometryPropertyName( Symbolizer s ) {
        String geomName = null;
        // TODO: fix the styles, the getGeometryPropertyName should probably be
        // moved into an interface...
        if (s instanceof PolygonSymbolizer) {
            geomName = ((PolygonSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof PointSymbolizer) {
            geomName = ((PointSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof LineSymbolizer) {
            geomName = ((LineSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof TextSymbolizer) {
            geomName = ((TextSymbolizer) s).getGeometryPropertyName();
        }
        return geomName;
    }
    /**
     * TODO summary sentence for worldToScreenTransform ...
     * 
     * @param bounds
     * @param rectangle
     * @return
     */
    public static AffineTransform worldToScreenTransform( BoundingBox mapExtent,
            Rectangle screenSize ) {
        double scaleX = screenSize.getWidth() / mapExtent.getWidth();
        double scaleY = screenSize.getHeight() / mapExtent.getHeight();

        double tx = -mapExtent.getMinX() * scaleX;
        double ty = (mapExtent.getMinY() * scaleY) + screenSize.getHeight();

        AffineTransform at = new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY, tx, ty);

        return at;
    }
    /**
     * Create a SimpleFeatureType schema using a type short hand.
     * <p>
     * Code Example:<pre><code>
     * new Drawing().schema("namespace.typename", "id:0,*geom:LineString,name:String,*centroid:Point");
     * </code></pre>
     * <ul>
     * <li>SimpleFeatureType with identifier "namespace.typename"
     * <li>Default Geometry "geom" of type LineStirng indicated with a "*"
     * <li>Three attributes: id of type Integer, name of type String and centroid of type Point
     * </ul>
     * </p>
     * @param name namespace.name
     * @param spec
     * @return Generated SimpleFeatureType
     */
    public SimpleFeatureType schema( String name, String spec ) {
        try {
            return DataUtilities.createType(name, spec);
        } catch (SchemaException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static SimpleFeatureType pointSchema;
    static SimpleFeatureType lineSchema;
    static SimpleFeatureType polygonSchema;
    static SimpleFeatureType multipointSchema;
    static SimpleFeatureType multilineSchema;
    static SimpleFeatureType multipolygonSchema;
    static {
        try {
            pointSchema = DataUtilities.createType("generated:point", "*point:Point"); //$NON-NLS-1$ //$NON-NLS-2$
            lineSchema = DataUtilities.createType("generated:linestring", "*linestring:LineString"); //$NON-NLS-1$ //$NON-NLS-2$
            polygonSchema = DataUtilities.createType("generated:polygon", "*polygon:Polygon"); //$NON-NLS-1$ //$NON-NLS-2$
            multipointSchema = DataUtilities.createType(
                    "generated:multipoint", "*multipoint:MultiPoint"); //$NON-NLS-1$ //$NON-NLS-2$
            multilineSchema = DataUtilities.createType(
                    "generated:multilinestring", "*multilinestring:MultiLineString"); //$NON-NLS-1$ //$NON-NLS-2$
            multipolygonSchema = DataUtilities.createType(
                    "generated:multipolygon", "*multipolygon:MultiPolygon"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (SchemaException unExpected) {
            System.err.println(unExpected);
        }
    }

    /**
     * Just a convinient method to create feature from geometry.
     * 
     * @param geom the geometry to create feature from
     * @return feature instance
     */
    public SimpleFeature feature( Geometry geom ) {
        if (geom instanceof Polygon) {
            return feature((Polygon) geom);
        } else if (geom instanceof MultiPolygon) {
            return feature((MultiPolygon) geom);
        } else if (geom instanceof Point) {
            return feature((Point) geom);
        } else if (geom instanceof LineString) {
            return feature((LineString) geom);
        } else if (geom instanceof MultiPoint) {
            return feature((MultiPoint) geom);
        } else if (geom instanceof MultiLineString) {
            return feature((MultiLineString) geom);
        } else {
            throw new IllegalArgumentException("Geometry is not supported to create feature"); //$NON-NLS-1$
        }
    }

    /**
     * Simple feature with one attribute called "point".
     * @param point 
     * @return SimpleFeature with a default geometry and no attribtues 
     */
    // THIS
    public SimpleFeature feature( Point point ) {
        if (point == null)
            throw new NullPointerException("Point required"); //$NON-NLS-1$
        try {
            return SimpleFeatureBuilder.build(pointSchema, new Object[]{point}, null);
        } catch (IllegalAttributeException e) {
            // this should not happen because we *know* the parameter matches schame
            throw new RuntimeException("Could not generate feature for point " + point); //$NON-NLS-1$
        }
    }
    /**
     * Simple SimpleFeature with a default geometry and no attribtues.
     * @param line 
     * @return SimpleFeature with a default geometry and no attribtues 
     */
    public SimpleFeature feature( LineString line ) {
        if (line == null)
            throw new NullPointerException("line required"); //$NON-NLS-1$
        try {
            return SimpleFeatureBuilder.build(lineSchema, new Object[]{line}, null);
        } catch (IllegalAttributeException e) {
            // this should not happen because we *know* the parameter matches schame
            throw new RuntimeException("Could not generate feature for point " + line); //$NON-NLS-1$
        }
    }

    /**
     * Simple SimpleFeature with a default geometry and no attribtues.
     * @param polygon 
     * @return SimpleFeature with a default geometry and no attribtues 
     */
    public SimpleFeature feature( Polygon polygon ) {
        if (polygon == null)
            throw new NullPointerException("polygon required"); //$NON-NLS-1$
        try {
            return SimpleFeatureBuilder.build(polygonSchema, new Object[]{polygon}, null);
        } catch (IllegalAttributeException e) {
            // this should not happen because we *know* the parameter matches schame
            throw new RuntimeException("Could not generate feature for point " + polygon); //$NON-NLS-1$
        }
    }

    /**
     * Simple SimpleFeature with a default geometry and no attribtues.
     * @param multipoint 
     * @return SimpleFeature with a default geometry and no attribtues 
     */
    public SimpleFeature feature( MultiPoint multipoint ) {
        if (multipoint == null)
            throw new NullPointerException("multipoint required"); //$NON-NLS-1$
        try {
            return SimpleFeatureBuilder.build(multipointSchema, new Object[]{multipoint}, null);
        } catch (IllegalAttributeException e) {
            // this should not happen because we *know* the parameter matches schame
            throw new RuntimeException("Could not generate feature for point " + multipoint); //$NON-NLS-1$
        }
    }
    /**
     * Simple SimpleFeature with a default geometry and no attribtues.
     * @param multilinestring
     * @return SimpleFeature with a default geometry and no attribtues 
     */
    public SimpleFeature feature( MultiLineString multilinestring ) {
        if (multilinestring == null)
            throw new NullPointerException("multilinestring required"); //$NON-NLS-1$
        try {
            return SimpleFeatureBuilder.build(multilineSchema, new Object[]{multilinestring}, null);
        } catch (IllegalAttributeException e) {
            // this should not happen because we *know* the parameter matches schame
            throw new RuntimeException("Could not generate feature for point " + multilinestring); //$NON-NLS-1$
        }
    }
    /**
     * Simple SimpleFeature with a default geometry and no attribtues.
     * @param multipolygon 
     * @return SimpleFeature with a default geometry and no attribtues 
     */
    public SimpleFeature feature( MultiPolygon multipolygon ) {
        if (multipolygon == null)
            throw new NullPointerException("multipolygon required"); //$NON-NLS-1$
        try {
            return SimpleFeatureBuilder.build(multipolygonSchema, new Object[]{multipolygon}, null);
        } catch (IllegalAttributeException e) {
            // this should not happen because we *know* the parameter matches schame
            throw new RuntimeException("Could not generate feature for point " + multipolygon); //$NON-NLS-1$
        }
    }

    /**
     * Generate Point from two dimensional ordinates
     * 
     * @param x
     * @param y
     * @return Point
     */
    // THIS
    public Point point( int x, int y ) {
        return gf.createPoint(new Coordinate(x, y));
    }
    /**
     * Generate LineStrings from two dimensional ordinates
     * 
     * @param xy
     * @return LineStirng
     */
    // THIS
    public LineString line( int[] xy ) {
        Coordinate[] coords = new Coordinate[xy.length / 2];

        for( int i = 0; i < xy.length; i += 2 ) {
            coords[i / 2] = new Coordinate(xy[i], xy[i + 1]);
        }

        return gf.createLineString(coords);
    }

    /**
     * Generate a MultiLineString from two dimensional ordinates
     * 
     * @param xy
     * @return MultiLineStirng
     */
    public MultiLineString lines( int[][] xy ) {
        LineString[] lines = new LineString[xy.length];

        for( int i = 0; i < xy.length; i++ ) {
            lines[i] = line(xy[i]);
        }

        return gf.createMultiLineString(lines);
    }
    /**
     * Convience constructor for GeometryFactory.createPolygon.
     * <p>
     * The provided xy ordinates are turned into a linear rings.
     * </p>
     * @param xy Two dimensional ordiantes.
     * @return Polygon
     */
    // THIS
    public Polygon polygon( int[] xy ) {
        LinearRing shell = ring(xy);
        return gf.createPolygon(shell, null);
    }

    /**
     * Convience constructor for GeometryFactory.createPolygon.
     * <p>
     * The provided xy and holes are turned into linear rings.
     * </p>
     * @param xy Two dimensional ordiantes.
     * @param holes Holes in polygon or null.
     * 
     * @return Polygon 
     */
    public Polygon polygon( int[] xy, int[] holes[] ) {
        if (holes == null || holes.length == 0) {
            return polygon(xy);
        }
        LinearRing shell = ring(xy);

        LinearRing[] rings = new LinearRing[holes.length];

        for( int i = 0; i < xy.length; i++ ) {
            rings[i] = ring(holes[i]);
        }
        return gf.createPolygon(shell, rings);
    }

    /**
     * Convience constructor for GeometryFactory.createLinearRing.
     * 
     * @param xy Two dimensional ordiantes.
     * @return LinearRing for use with polygon
     */
    public LinearRing ring( int[] xy ) {
        int length = xy.length / 2;
        if (xy[0] != xy[xy.length - 2] || xy[1] != xy[xy.length - 1]) {
            length++;
        }
        Coordinate[] coords = new Coordinate[length];

        for( int i = 0; i < xy.length; i += 2 ) {
            coords[i / 2] = new Coordinate(xy[i], xy[i + 1]);
        }
        if (xy[0] != xy[xy.length - 2] || xy[1] != xy[xy.length - 1]) {
            coords[length - 1] = coords[0];
        }
        return gf.createLinearRing(coords);
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

