/**
 * 
 */
package uk.ac.lkl.migen.system.expresser.ui.ecollaborator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.tomtessier.scrollabledesktop.JScrollableDesktopPane;

import uk.ac.lkl.client.URLParameters;
import uk.ac.lkl.common.util.ID;
import uk.ac.lkl.common.util.IDFactory;
import uk.ac.lkl.common.util.IDObject;
import uk.ac.lkl.common.util.LoggingUtilities;
import uk.ac.lkl.common.util.ObjectWithID;
import uk.ac.lkl.common.util.config.MiGenConfiguration;
import uk.ac.lkl.common.util.value.Number;
import uk.ac.lkl.migen.system.ExpresserLauncher;
import uk.ac.lkl.migen.system.expresser.ExternalInterface;
import uk.ac.lkl.migen.system.expresser.model.ExpresserModel;
import uk.ac.lkl.migen.system.expresser.model.ExpresserModelImpl;
import uk.ac.lkl.migen.system.expresser.model.ModelColor;
import uk.ac.lkl.migen.system.expresser.model.ModelCopier;
import uk.ac.lkl.migen.system.expresser.model.Walker;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.TiedNumberExpression;
import uk.ac.lkl.migen.system.expresser.ui.ActivityMaker;
import uk.ac.lkl.migen.system.expresser.ui.BlockShapeCanvasPanel;
import uk.ac.lkl.migen.system.expresser.ui.DocumentCanvas;
import uk.ac.lkl.migen.system.expresser.ui.ExpresserModelPanel;
import uk.ac.lkl.migen.system.expresser.ui.ExpressionPanel;
import uk.ac.lkl.migen.system.expresser.ui.GlobalColorAllocationPanel;
import uk.ac.lkl.migen.system.expresser.ui.MultiUniverseTabbedPanel;
import uk.ac.lkl.migen.system.expresser.ui.ObjectCanvasFrame;
import uk.ac.lkl.migen.system.expresser.ui.ObjectSetCanvas;
import uk.ac.lkl.migen.system.expresser.ui.RulesPanel;
import uk.ac.lkl.migen.system.expresser.ui.action.ZoomInAction;
import uk.ac.lkl.migen.system.expresser.ui.action.ZoomOutAction;
import uk.ac.lkl.migen.system.expresser.ui.icon.IconVariableFactory;
import uk.ac.lkl.migen.system.expresser.ui.menu.ActivityDocumentSaveAsTabMenuItem;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ActivityDocumentClearedEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ActivityDocumentCompletedEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ActivityDocumentTextAreaDissolvedEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ActivityDocumentTextAreaUndissolvedEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ElementAddedToActivityDocumentEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.UIEventManager;
import uk.ac.lkl.migen.system.server.MiGenServerCommunicator;
import uk.ac.lkl.migen.system.server.UserSet;
import uk.ac.lkl.migen.system.task.TaskIdentifier;
import uk.ac.lkl.migen.system.util.MiGenUtilities;

/**
 * Implements the Structured Activity Documents as originally described in
 * https://docs.google.com/Doc?id=ddz62t26_62d6qbbzfq&hl=en
 * 
 * The structure of these documents (used when converting to and from XML)
 * can be found at 
 * http://code.google.com/p/migen/wiki/StructureOfActivityDocuments
 * 
 * @author Ken Kahn
 *
 */
public class ActivityDocument extends JPanel implements IDObject, ObjectWithID {

    private static final Color TEXT_BACKGROUND_COLOR = Color.WHITE;

    private JSplitPane splitPane;

    private JScrollableDesktopPane contentsPanel;

    private JPanel buttonPanel;

    private ID id;

    private String documentName;

    // used to communicate with the server
    private ActivityDocumentXMLString activityDocumentXMLString = null;
    
    private int xMax;
    private int yMax;
    
    // don't want to add duplicates of a listener to tied numbers so keep a table
    // but don't want it to interfere with garbage collection of tied numbers hence the use of WeakHashMap
    // need to rethink this since this prevented the total allocation label from being updated when animating
//    private WeakHashMap<TiedNumberExpression<IntegerValue>, JLabel> tiedNumberListeners =
//	new WeakHashMap<TiedNumberExpression<IntegerValue>, JLabel>();    

    // to maintain identities across canvas boundaries need
    // maps between those on the outside and those inside
    // not that canvases have this as well but if they are part of an activity document
    // then they use this one
    protected HashMap<TiedNumberExpression<Number>, TiedNumberExpression<Number>> tiedNumberImportTable = 
	new HashMap<TiedNumberExpression<Number>, TiedNumberExpression<Number>>();

    private ArrayList<ActivityDocumentComponent> activityDocumentComponents =
	new ArrayList<ActivityDocumentComponent>();

    // component disabled waiting for a done button press
    private ArrayList<Component> disabledComponents = new ArrayList<Component>();

    private int extraEffortCounter = 1;

    private boolean activityCompletedAndSaved = false;

    public static int initialWidth = 300;
    
    public static final String ACTIVITY_DOCUMENT_SAVED_BECAUSE_SHUTDOWN = "S";
    public static final String ACTIVITY_DOCUMENT_SAVED_BECAUSE_DONE_BUTTON = "A";
    public static final String ACTIVITY_DOCUMENT_SAVED_FOR_UNDO = "U";
    public static final String ACTIVITY_DOCUMENT_SAVED_BECAUSE_INDICATOR = "I";
    public static final String ACTIVITY_DOCUMENT_SAVED_BECAUSE_MENU_ITEM = "M";
    public static final String ACTIVITY_DOCUMENT_CREATED_BY_PAIRING_TOOL = "P";
    // following is used to connect indicators with a model
    public static final String ACTIVITY_DOCUMENT_STARTUP = "B";
    
    public static final String RULE_CANVAS = "rule canvas";
    public static final String CONSTRUCTION_PARTS = "construction parts";

    protected ArrayList<ExpresserModel> models = new ArrayList<ExpresserModel>();

    // the task identifier of the activity
    protected TaskIdentifier taskIdentifier = null;

    private FinishedButton finishedButton;

    private DocumentCanvasContainer generalModelCanvas;

    public ActivityDocument(JSplitPane splitPane) {
	super();
	this.splitPane = splitPane;
	setLayout(new BorderLayout());
	if (splitPane != null) {
	    splitPane.setOneTouchExpandable(true);
	    setPreferredSize(new Dimension(initialWidth, splitPane.getHeight()));
	}
	if (MiGenConfiguration.isAddActivityDocumentButtons()) {
	    addActivityDocumentButtons(true, true);
	}
	// JScrollableDesktopPane obtained from 
	// http://www.javaworld.com/javaworld/jw-11-2001/jw-1130-jscroll.html?page=1
	contentsPanel = new JScrollableDesktopPane();
	contentsPanel.setBackground(Color.LIGHT_GRAY);
	add(contentsPanel, BorderLayout.CENTER);
	// ok to cover this up
	setMinimumSize(new Dimension(0, 0));
	id = IDFactory.newID(this);
	ExpresserModel currentModel = ExpresserLauncher.getModelOfSelectedTab();
	if (currentModel != null) {
	    addModel(currentModel);
	}
	addFinishedButton();
    }

    public ActivityDocument() {
	// for testing
	this(null);
    }

    /**
     * Creates a dummy activity document containing a specified model
     * for the purposes of saving it to the server.
     * 
     * @param model
     * @param name
     */
    public ActivityDocument(ExpresserModel model, String name) {
	this.documentName = name;
	if (model != null) {
	    addModel(model);
	}
    }

    /**
     * Adds buttons for adding text areas and canvases to 
     * this activity document
     */
    public void addActivityDocumentButtons(boolean textButton, boolean canvasButton) {
	buttonPanel = new JPanel(new FlowLayout());
	if (textButton) {
	    JButton addTextButton = createTextEditorButton();
	    buttonPanel.add(addTextButton);
	}
	if (canvasButton) {
	    JButton addCanvasButton = createEmptyCanvasButton();
	    buttonPanel.add(addCanvasButton);
	    //	    JButton animatingCanvasButton = createAnimatingCanvasButton();
	    //	    buttonPanel.add(animatingCanvasButton);
	}
	add(buttonPanel, BorderLayout.NORTH);
    }

    public void setSelectedFrame(JInternalFrame frame) {
	contentsPanel.setSelectedFrame(frame);
    }

    public JViewport getViewport() {
	return contentsPanel.getViewport();
    }

    private JButton createEmptyCanvasButton() {
	JButton button = new JButton(MiGenUtilities.getLocalisedMessage("AddCanvas"));
	ActionListener actionListener = new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		String title = createTitle();
		//TODO: decide if we want canvas without grids
		createCanvas(title, null, true, false, true, false, true, false); 
	    }

	};
	button.addActionListener(actionListener);
	return button;
    }

    public static String createTitle() {
	return "By " + LoggingUtilities.getUserName() + " created " + new Date().toString();
    }

    private JButton createTextEditorButton() {
	JButton button = new JButton(MiGenUtilities.getLocalisedMessage("AddText"));
	ActionListener actionListener = new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		String title = createTitle();
		createTextArea(ActivityDocument.this, title, null, "", "text/html", true, true, true, false);
	    }

	};
	button.addActionListener(actionListener);
	return button;
    }

    public void addElement(
	    final Component component, 
	    final ActivityDocumentComponent activityDocumentComponent, 
	    Dimension size,
	    boolean computeLocation) {
	JInternalFrame selectedFrame = contentsPanel.getSelectedFrame();
	Point location;
	if (computeLocation) {
	    if (selectedFrame == null) {
		location = new Point(0, 0);
	    } else {
		location = selectedFrame.getLocation();
		location.translate(0, selectedFrame.getHeight());
	    }
	} else {
	    // save location since clobbered by add below
	    location = activityDocumentComponent.getLocation(); 
	}
	activityDocumentComponent.add(component);
	//	contentsPanel.add(activityDocumentComponent);
	//	activityDocumentComponent.setLocation(location);
	if (selectedFrame == null) {
	    Dimension preferredSize = size != null ? size : new Dimension(300, 300);
	    activityDocumentComponent.setPreferredSize(preferredSize);
	    component.setPreferredSize(preferredSize);
	} else {
	    Dimension previousSize = size != null ? size : selectedFrame.getSize();
	    activityDocumentComponent.setPreferredSize(previousSize);
	    component.setPreferredSize(previousSize);
	}
	activityDocumentComponent.setSize(activityDocumentComponent.getPreferredSize());
	component.setSize(component.getPreferredSize());
	final int extraWidth = 20;
	int minimumWidth = activityDocumentComponent.getWidth()+extraWidth;
	if (splitPane != null && splitPane.getDividerLocation() < minimumWidth) {
	    splitPane.setDividerLocation(minimumWidth);
	}
	//	internalFrames.add(activityDocumentComponent);
	//	activityDocumentComponent.setActivityDocument(this);
	addFrame(activityDocumentComponent, location.x, location.y);
	UIEventManager.processEvent(
		new ElementAddedToActivityDocumentEvent(getId().toString(), activityDocumentComponent.getId().toString()));
    }

    public void addFrame(ActivityDocumentComponent activityDocumentComponent) {
	addFrame(activityDocumentComponent, -1, -1);
    }

    public void addFrame(ActivityDocumentComponent activityDocumentComponent, int x, int y) {
	activityDocumentComponent.setActivityDocument(this);
	contentsPanel.add(activityDocumentComponent, x, y);
	activityDocumentComponents.add(activityDocumentComponent);
	addDoneButtonListener(activityDocumentComponent);
	activityDocumentComponent.show();
	if (x > xMax) {
	    xMax = x;
	}
	if (y > yMax) {
	    yMax = y;
	}
    }

    public void removeFrame(ActivityDocumentComponent activityDocumentComponent) {
	contentsPanel.remove(activityDocumentComponent);
	removeInternalActivityDocumentComponent(activityDocumentComponent);    
    }

    public void removeInternalActivityDocumentComponent(ActivityDocumentComponent activityDocumentComponent) {
	activityDocumentComponents.remove(activityDocumentComponent);
    }

    public void addInternalActivityDocumentComponent(ActivityDocumentComponent activityDocumentComponent) {
	activityDocumentComponents.add(activityDocumentComponent);
	recursivelySetEnabled(activityDocumentComponent.isEnabled(), activityDocumentComponent);
    }
    
    public ActivityDocumentGoalArea getActivityDocumentGoalArea() {
	// really gets the first it can find
	for (ActivityDocumentComponent activityDocumentComponent : activityDocumentComponents) {
	    if (activityDocumentComponent instanceof ActivityDocumentGoalArea)  {
		ActivityDocumentGoalArea activityDocumentGoalArea = (ActivityDocumentGoalArea) activityDocumentComponent;
		return activityDocumentGoalArea;
	    }
	}
	return null;
    }

    @SuppressWarnings("unchecked")
    public void removeContents() {
	// clone the list to avoid ConcurrentModificationException
	ArrayList<ActivityDocumentComponent> internalFramesCloned = 
	    (ArrayList<ActivityDocumentComponent>) activityDocumentComponents.clone();
	for (ActivityDocumentComponent internalFrame : internalFramesCloned) {
	    contentsPanel.remove(internalFrame);
	}
	activityDocumentComponents.clear();
	if (buttonPanel != null) {
	    remove(buttonPanel);
	}
	disabledComponents.clear();
	tiedNumberImportTable.clear();
//	tiedNumberListeners.clear();
	UIEventManager.processEvent(new ActivityDocumentClearedEvent(getId().toString()));
    }
    
    public DocumentCanvasContainer addGeneralModel(Integer x, Integer y, Integer width, Integer height) {
	if (generalModelCanvas != null) {
	    return generalModelCanvas; // already created
	}
	String label = MiGenUtilities.getLocalisedMessage("SlavePanelLabel");
	ExpresserModel masterModel = getFirstModel();
	if (width == null) {
	    width = MiGenConfiguration.getGeneralModelCanvasWidth();
	}
	if (height == null) {
	    height = MiGenConfiguration.getGeneralModelCanvasHeight();
	}
	Dimension size = new Dimension(width, height);
	generalModelCanvas = createCanvas(this, masterModel, true, label, size, true, true, false, true, false, false, false, true);
	if (x != null && y != null) {
	    generalModelCanvas.setLocation(x, y);
	}
	DocumentCanvas documentCanvas = generalModelCanvas.getDocumentCanvas();
	documentCanvas.setGridSize(MiGenConfiguration.getGeneralModelCanvasGridSize());
	return generalModelCanvas;
    }

    /**
     * @param title
     * @param size -- size is based on previous element if null
     * @param initialText 
     * @param containsDoneButton 
     * @return a text area with the title
     * and added it to the activity document
     */
    public static ActivityDocumentTextArea createTextArea(
	    final ActivityDocument activityDocument,
	    String title, 
	    Dimension size, 
	    String initialText, 
	    String type,
	    boolean dissolvable,
	    boolean closable,
	    boolean resizeable,
	    boolean containsDoneButton) {
	// TODO: add a rich text editor
	// tried to add an option to setOpaque(false) but didn't work
	final ActivityDocumentEditorPanel textComponent = 
	    new ActivityDocumentEditorPanel(new JEditorPane(type, initialText), containsDoneButton);
	textComponent.setBackground(TEXT_BACKGROUND_COLOR);
	final ActivityDocumentTextArea activityDocumentTextArea = 
	    new ActivityDocumentTextArea(title, closable, resizeable, dissolvable, containsDoneButton);
	if (activityDocument != null) {
	    activityDocument.addElement(textComponent, activityDocumentTextArea, size, true);
	} else {
	    activityDocumentTextArea.add(textComponent);
	}
	activityDocumentTextArea.setContents(textComponent);
	textComponent.setActivityDocumentTextArea(activityDocumentTextArea);
	if (dissolvable) {
	    JMenuBar menuBar = new JMenuBar();
	    JMenu menu = new JMenu(MiGenUtilities.getLocalisedMessage("DissolveTextIntoCanvas"));
	    MenuListener menuListener = new MenuListener() {

		public void menuCanceled(MenuEvent e) {
		}

		public void menuDeselected(MenuEvent e) {
		}
		public void menuSelected(MenuEvent e) {
		    ActivityDocument currentActivityDocument = 
			activityDocumentTextArea.getActivityDocument();
		    currentActivityDocument.dissolve(activityDocumentTextArea, textComponent.getEditorPane());
		}

	    };
	    menu.addMenuListener(menuListener);
	    menuBar.add(menu);
	    activityDocumentTextArea.setJMenuBar(menuBar);
	}
	if (containsDoneButton && activityDocument != null) {
	    ActionListener doneButtonActionListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    activityDocument.disableComponentsBelowNextGoalAndEnableThoseAbove(activityDocumentTextArea);
		}

	    };
	    activityDocumentTextArea.getGoalDoneButton().addActionListener(doneButtonActionListener);
	}
	return activityDocumentTextArea;
    }

    private void dissolve(
	    final ActivityDocumentTextArea textArea, 
	    JEditorPane textComponent) {
	// removes documentComponent and adds the contents of textComponent to an intersecting canvas
	if (!textArea.isVisible()) {
	    // maybe double clicked on the button
	    return;
	}
	Rectangle testBounds = textArea.getBounds();
	for (final ActivityDocumentComponent activityDocumentComponent : activityDocumentComponents) {
	    final DocumentCanvas documentCanvas = activityDocumentComponent.getDocumentCanvas();
	    if (documentCanvas != null) {
		final DocumentCanvasContainer activityDocumentCanvas = 
		    (DocumentCanvasContainer) activityDocumentComponent;
		Rectangle canvasBounds = activityDocumentComponent.getBounds();
		if (testBounds.intersects(canvasBounds)) {
		    Point location = textArea.getLocation();
		    // following does work -- probably because these are using
		    // JInternalFrame and JDesktopPane where location seems to be
		    // implemented differently
		    //		    Point newLocation = 
		    //			SwingUtilities.convertPoint(documentComponent, location, documentCanvas);
		    Point canvasLocation = activityDocumentComponent.getLocation();
		    Rectangle contentBounds = activityDocumentComponent.getContentPane().getBounds();
		    int titleBarHeight = canvasBounds.height - contentBounds.height;
		    // without using titleBarHeight as an offset
		    // the dissolved text is located too high
		    // Not sure why subtracting 10 works best -- bottom border could explain it
		    // TODO: the difference in x is 5 pixels -- 
		    // determine if split evenly on both sides
		    location.translate(-canvasLocation.x, -canvasLocation.y + (titleBarHeight-10));
		    removeFrame(textArea);
		    String text = textComponent.getText();
		    final DissolvedText replacement = 
			new DissolvedText(text, activityDocumentCanvas, location);
		    // the replacement should go on top of existing elements
		    documentCanvas.add(replacement, 0);
		    UIEventManager.processEvent(
			    new ActivityDocumentTextAreaDissolvedEvent(
				    getId().toString(), 
				    textArea.getId().toString(), 
				    activityDocumentComponent.getId().toString(), 
				    new uk.ac.lkl.common.util.Location(location.x, location.y)));
		    return;
		}
	    }
	}
	// TODO: communicate this somehow
	MiGenUtilities.printError("Communicate back that not over a canvas");
    }

    protected void undissolve(
	    DissolvedText replacement, 
	    DocumentCanvasContainer activityDocumentCanvas) {
	// creates a new text area to replace the original that had been dissolved
	Point labelLocation = replacement.getLocation();
	ActivityDocumentTextArea newTextComponenent = 
	    createTextArea(
		    this,
		    activityDocumentCanvas.getTitle(), 
		    activityDocumentCanvas.getSize(),
		    replacement.getText(),
		    "text/html",
		    true,
		    true,
		    true,
		    false);
	SwingUtilities.convertPoint(activityDocumentCanvas, labelLocation, contentsPanel);
	newTextComponenent.setLocation(labelLocation);
	replacement.getParent().remove(replacement);
	UIEventManager.processEvent(
		new ActivityDocumentTextAreaUndissolvedEvent(
			getId().toString(), newTextComponenent.getId().toString(), activityDocumentCanvas.getId().toString()));
    }

    /**
     * @param initialText
     * @param type -- text/plain, text/html, or text/rtf
     * @param title
     * @param x
     * @param y
     * @param width
     * @param height
     * @param closable 
     * @param b 
     * @return a text area with the title
     * located at x and y
     * and added it to the activity document
     */
    public ActivityDocumentComponent createTextArea(String initialText, String type, 
	    String title, 
	    int x, int y, int width, int height,
	    boolean closable, 
	    boolean resizable,
	    boolean dissolvable,
	    boolean containsDoneButton) {
	ActivityDocumentComponent textArea = 
	    createTextArea(this, title, new Dimension(width, height), initialText, type, dissolvable, true, true, containsDoneButton);
	textArea.setLocation(x, y);
	textArea.setClosable(closable);
	textArea.setResizable(resizable);
	return textArea;
    }

    public static DocumentCanvasContainer createCanvas(
	    final ActivityDocument activityDocument,
	    ExpresserModel model, 
	    boolean addPlayMenu, 
	    String title, 
	    Dimension size, 
	    boolean gridShowing,
	    boolean addZoomButtons,
	    boolean patternsDraggable,
	    boolean resizeable,
	    boolean closable,
	    boolean containsDoneButton,
	    boolean replaceableWithGeneralWorld,
	    boolean mirrorChangesInMyWorld) {
	final DocumentCanvas canvas = new DocumentCanvas(model);
	canvas.enableDrop();
	canvas.setGridShowing(gridShowing);
	canvas.setAddZoomButtons(addZoomButtons);
	canvas.setMirrorChangesInMyWorld(mirrorChangesInMyWorld);
	if (patternsDraggable) {
	    canvas.removePatternsAddProxies();
	}
	RulesPanel computersModelRulesDisplay = null;
	if (mirrorChangesInMyWorld) {
	    if (activityDocument != null) {
		computersModelRulesDisplay = new RulesPanel(model);
	    }
	}
	final DocumentCanvasContainer activityDocumentCanvas = 
	    new DocumentCanvasContainer(
		    title, resizeable, closable, addPlayMenu, 
		    patternsDraggable, replaceableWithGeneralWorld, containsDoneButton);
	JComponent element;
	// following may depend upon the size of the canvas so do the above first
	if (addPlayMenu) {
	    PanelWithPlayToolBar panelWithPlayToolBar = 
		new PanelWithPlayToolBar(canvas, patternsDraggable, !mirrorChangesInMyWorld, mirrorChangesInMyWorld);
	    JToolBar toolBar = panelWithPlayToolBar.getToolBar();
	    if (addZoomButtons) {
		toolBar.add(new ZoomInAction(canvas));
		toolBar.add(new ZoomOutAction(canvas));
	    }
	    element = panelWithPlayToolBar;
	    if (computersModelRulesDisplay != null && activityDocument != null) {
		TiedNumberValuesDisplay tiedNumberValuesDisplay = new TiedNumberValuesDisplay(model);
		activityDocument.insertComputersModelRulesDisplay(
			computersModelRulesDisplay, tiedNumberValuesDisplay, panelWithPlayToolBar);
		canvas.setTiedNumberValuesDisplayToUpdate(tiedNumberValuesDisplay);
		canvas.setRulesPanelToUpdate(computersModelRulesDisplay);
		canvas.getModelCopier().copyModelProcess();
	    } else {
		canvas.setPanelWithPlayToolBar(panelWithPlayToolBar);
	    }
	} else {
	    element = canvas;
	}
	if (replaceableWithGeneralWorld) {
	    addReplaceModelInterface(canvas, activityDocumentCanvas);
	}
	if (containsDoneButton) {
	    final PanelWithDoneButton panelWithDoneButton = 
		new PanelWithDoneButton(element, true);
	    DocumentCanvasListener documentCanvasListener = 
		new DocumentCanvasListener() {

		public void canvasChanged() {
		    panelWithDoneButton.getGoalDoneButton().setEnabled(true);
		}

	    };
	    ActionListener doneButtonActionListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    canvas.getActivityDocument().disableComponentsBelowNextGoalAndEnableThoseAbove(
			    activityDocumentCanvas);
		}

	    };
	    panelWithDoneButton.getGoalDoneButton().addActionListener(doneButtonActionListener);
	    canvas.addDocumentCanvasListeners(documentCanvasListener);
	    element = panelWithDoneButton;     
	}
	if (activityDocument != null) {
	    activityDocument.addElement(element, activityDocumentCanvas, size, true);
	} else {
	    activityDocumentCanvas.add(element);
	}
	activityDocumentCanvas.setContents(element);
	activityDocumentCanvas.setMirrorChangesInMyWorld(mirrorChangesInMyWorld);
	return activityDocumentCanvas;
    }

    public void insertComputersModelRulesDisplay(RulesPanel rulesDisplay, TiedNumberValuesDisplay tiedNumberValuesDisplay, PanelWithPlayToolBar panelWithPlayToolBar) {
	// add label that mirrors the total allocation rule
	// and the bindings of named or unlocked tied numbers
	JPanel panelWithPlayToolbarAndRule = new JPanel(new GridBagLayout());
	GridBagConstraints gridBagConstraints = getComputersModelPanelGridBagConstraints();
	JToolBar toolBar = panelWithPlayToolBar.getToolBar();
	panelWithPlayToolBar.add(toolBar, BorderLayout.NORTH);
//	panelWithPlayToolbarAndRule.add(toolBar, gridBagConstraints);
	//first add the panel that displays the unlocked numbers 
	panelWithPlayToolbarAndRule.add(tiedNumberValuesDisplay, gridBagConstraints);
	//then add the panel that displays the various allocations
	panelWithPlayToolbarAndRule.add(rulesDisplay, gridBagConstraints);
	panelWithPlayToolBar.add(panelWithPlayToolbarAndRule, BorderLayout.SOUTH);
	toolBar.setSize(toolBar.getPreferredSize());
	panelWithPlayToolBar.setSize(panelWithPlayToolBar.getPreferredSize());
	panelWithPlayToolbarAndRule.setSize(panelWithPlayToolbarAndRule.getPreferredSize());
    }
    
    public static GridBagConstraints getComputersModelPanelGridBagConstraints() {
	GridBagConstraints gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
	gridBagConstraints.gridheight = 2;
	gridBagConstraints.anchor = GridBagConstraints.LINE_START;
	// the following doesn't seem to fix the layout problem when the expression is too long
	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
	return gridBagConstraints;
    }

    protected static void addReplaceModelInterface(
	    final DocumentCanvas canvas,
	    final DocumentCanvasContainer activityDocumentCanvas) {
	JMenuBar menuBar = new JMenuBar();
	JMenu menu = new JMenu(MiGenUtilities.getLocalisedMessage("ReplaceWithMyWorld"));
	MenuListener menuListener = new MenuListener() {

	public void menuCanceled(MenuEvent e) {
	}

	public void menuDeselected(MenuEvent e) {
	}

	public void menuSelected(MenuEvent e) {
	    ExpresserModel model = ExpresserLauncher.getModelOfSelectedTab();
	    Dimension newCanvasSize = canvas.replaceModel(model);
	    if (newCanvasSize == null)
		return;    
	    int newCanvasWidth = newCanvasSize.width;
	    int newCanvasHeight = newCanvasSize.height;
	    int canvasWidthDelta = newCanvasWidth - canvas.getWidth();
	    int canvasHeightDelta = newCanvasHeight - canvas.getHeight();
	    if (!activityDocumentCanvas.isAddPlayMenu()) {
		// doesn't have play buttons so add them
		PanelWithPlayToolBar panelWithPlayToolBar = 
		    new PanelWithPlayToolBar(canvas, true, true, false);
		JComponent contents = activityDocumentCanvas.getContents();
		if (contents instanceof PanelWithDoneButton) {
		    PanelWithDoneButton panelWithDoneButton = (PanelWithDoneButton) contents;
		    panelWithDoneButton.setContents(panelWithPlayToolBar);
		} else {
		    activityDocumentCanvas.add(panelWithPlayToolBar, BorderLayout.CENTER);
		    activityDocumentCanvas.setContents(panelWithPlayToolBar);
		}
		int newWidth = Math.max(panelWithPlayToolBar.getWidth(), newCanvasWidth);
		int newHeight = newCanvasHeight;		
		panelWithPlayToolBar.setSize(newWidth, newHeight);
		panelWithPlayToolBar.setPreferredSize(panelWithPlayToolBar.getSize());
		activityDocumentCanvas.setAddPlayMenu(true);
	    }
	    canvas.removePatternsAddProxies();
	    int currentWidth = activityDocumentCanvas.getWidth();
	    int newActivityDocumentCanvasWidth = 
		canvasWidthDelta > 0 ? currentWidth + canvasWidthDelta : currentWidth;
		int newActivityDocumentCanvasHeight = 
		    activityDocumentCanvas.getHeight() + canvasHeightDelta;
		if (MiGenConfiguration.isAddRulePanelWhenReplacingWithGeneralWorld()) {
		    // temporary object to think with
		    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		    int tabIndex = ExternalInterface.getTabbedPanelSelectedIndex();
		    GlobalColorAllocationPanel globalAllocationPanel = 
			ExternalInterface.getGlobalAllocationPanel(tabIndex, 0);
		    ExpressionPanel<Number> expressionPanel = 
			globalAllocationPanel.getExpressionPanel().createCopy();
		    ModelColor color = globalAllocationPanel.getSelectedColor();
		    if (color == null) {
			List<ModelColor> colorsWithoutRules = 
			    canvas.getModel().getColorsWithoutRules();
			ImageIcon manyColorsAllocationIcon = 
			    IconVariableFactory.createManyColorsAllocationIcon(
				    colorsWithoutRules);
			panel.add(new JLabel(manyColorsAllocationIcon));
		    } else {
			ImageIcon icon = 
			    IconVariableFactory.createColorAllocationIcon(color.getRGB(), color.isNegative());
			panel.add(new JLabel(icon));
		    }
		    panel.add(expressionPanel);
		    canvas.add(panel);
		    Dimension preferredSize = panel.getPreferredSize();
		    panel.setSize(preferredSize);
		    newActivityDocumentCanvasWidth += preferredSize.width;
		    panel.setLocation(0, newActivityDocumentCanvasHeight-preferredSize.height);
		    newActivityDocumentCanvasHeight += preferredSize.height;
		}
		activityDocumentCanvas.setSize(
			newActivityDocumentCanvasWidth, 
			newActivityDocumentCanvasHeight);
		activityDocumentCanvas.validate();
	}

	};
	menu.addMenuListener(menuListener);
	menuBar.add(menu);
	activityDocumentCanvas.setJMenuBar(menuBar);
    }
    
//    protected void updateColorSpecificLabels(HashMap<ColorResourceAttributeHandle, GlobalRuleLabel> handleLabelMap, 
//	                                     ExpressedObject<BlockShape> modelAsAGroup) {
//	Set<Entry<ColorResourceAttributeHandle, GlobalRuleLabel>> entrySet = handleLabelMap.entrySet();
//	for (Entry<ColorResourceAttributeHandle, GlobalRuleLabel> entry : entrySet) {
//	    ColorResourceAttributeHandle handle = entry.getKey();
//	    Expression<IntegerValue> expression = modelAsAGroup.getAttributeExpression(handle);
//	    GlobalRuleLabel label = entry.getValue();
//	    if (expression.isSpecified()) {
//		label.updateText();
//	    }
//	    label.setSize(label.getPreferredSize());
//	}
//    }


    /**
     * @param miGenServerCommunicator
     * @param userSet
     * @param model
     */
    public void saveActivityDocumentOnServer(
	    MiGenServerCommunicator miGenServerCommunicator, 
	    UserSet userSet,
	    ExpresserModel model) {
	nameAnyUnnamedModels();
	//	if (activityDocumentXMLString == null) {
	// need to recompute it since typically has changed
	activityDocumentXMLString = new ActivityDocumentXMLString(this);
	// See Issue 940 for why this is commented out
	//	    miGenServerCommunicator.activityDocumentXMLStringAdded(activityDocumentXMLString, userSet);
	//	}
	miGenServerCommunicator.setActivityDocumentXMLString(userSet, activityDocumentXMLString, model);
    }

    private void nameAnyUnnamedModels() {
	int counter = 1;
	for (ExpresserModel model : getModels()) {
	    String name = model.getName();
	    if (name == null || name.isEmpty()) {
		UserSet userSet = ExpresserLauncher.getUserSet();
		if (userSet != null) {
		    String userSetName = userSet.getNamesAsString(" & ");
		    if (counter > 1) {
			model.setName(userSetName + " #" + counter);
		    } else {
			model.setName(userSetName);
		    }
		}
	    }
	    counter++;
	}	
    }

    /**
     * This method saves the activity document using the given
     * MiGenServerCommunicator, for the given UserSet, and using 
     * the given type.
     * 
     * A ExpresserModel-handle is automatically created.
     * 
     * @param miGenServerCommunicator
     * @param userSet
     * @param model
     */
    public void saveActivityDocumentOnServer(
	    MiGenServerCommunicator miGenServerCommunicator, 
	    UserSet userSet,
	    String saveType) {	
	activityDocumentXMLString = new ActivityDocumentXMLString(this);
	ExpresserModel handle = new ExpresserModelImpl();
	handle.setName(generateModelName(saveType, null));
	miGenServerCommunicator.addModel(userSet, handle);
	miGenServerCommunicator.setActivityDocumentXMLString(userSet, activityDocumentXMLString, handle);
    }

    protected DocumentCanvasContainer createCanvas(
	    String title, 
	    Dimension size, 
	    boolean gridShowing,
	    boolean addZoomButtons,
	    boolean patternsDraggable,
	    boolean containsDoneButton,
	    boolean replaceableWithGeneralWorld,
	    boolean mirrorChangesInMyWorld) {
	// model used in loading model file into a canvas
	return createCanvas(
		this, 
		new ExpresserModelImpl(), 
		false, 
		title, 
		size, 
		gridShowing,
		addZoomButtons,
		patternsDraggable, 
		true, 
		true, 
		containsDoneButton, 
		replaceableWithGeneralWorld,
		mirrorChangesInMyWorld);
    }

    /**
     * @param title
     * @param x
     * @param y
     * @param width
     * @param height
     * @param showGrid
     * @return a canvas with the title and dimensions
     * located at x and y
     */
    public DocumentCanvasContainer createCanvas(
	    String title, 
	    int x, int y, 
	    int width, int height,
	    boolean closable, 
	    boolean resizable, 
	    boolean showGrid,
	    boolean addZoomButtons,
	    boolean patternsDraggable,
	    boolean containsDoneButton,
	    boolean replaceableWithGeneralWorld,
	    boolean mirrorChangesInMyWorld) {
	DocumentCanvasContainer canvasFrame = 
	    createCanvas(
		    title, new Dimension(width, height), showGrid, addZoomButtons, patternsDraggable, containsDoneButton, replaceableWithGeneralWorld, mirrorChangesInMyWorld);
	canvasFrame.setLocation(x, y);
	canvasFrame.setClosable(closable);
	canvasFrame.setResizable(resizable);
	return canvasFrame;
    }

    public JPanel getButtonPanel() {
	return buttonPanel;
    }

    /**
     * @param title
     * @param goalDescriptions 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param closable 
     * @param resizable
     * @param introductoryText -- HTML string above the goal check boxes, can be null
     * @return a goal area with the title
     * located at x and y
     * and added it to the activity document
     */
    public ActivityDocumentGoalArea createGoalArea(
	    String title, 
	    String[] goalDescriptions,
	    int x, int y, int width, int height,
	    boolean closable,
	    boolean resizable,
	    String introductoryText) {
	NonStaticGoalArea goalArea = 
	    new NonStaticGoalArea(GoalArea.createGoalList(goalDescriptions), introductoryText);
	return createGoalArea(title, goalArea, x, y, width, height, closable, resizable);
    }

    public ActivityDocumentGoalArea createGoalArea(
	    String title,
	    GoalArea goalArea, 
	    int x, int y, int width, int height,
	    boolean closable,
	    boolean resizable) {
	return createGoalArea(this, title, goalArea, x, y, width, height, closable, resizable);
    }

    /**
     * @param title
     * @param goals 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param closable 
     * @param resizable
     * @param introductoryText -- HTML string above the goal check boxes, can be null
     * @return a goal area with the title
     * located at x and y
     * and added it to the activity document
     */
    public ActivityDocumentGoalArea createGoalArea(
	    String title, 
	    List<NonStaticGoal> goals,
	    int x, int y, 
	    int width, int height,
	    boolean closable, 
	    boolean resizable,
	    String introductoryText) {
	NonStaticGoalArea goalArea = new NonStaticGoalArea(goals, introductoryText);
	return createGoalArea(this, title, goalArea, x, y, width, height, closable, resizable);
    }

    public static ActivityDocumentGoalArea createGoalArea(
	    final ActivityDocument activityDocument, 
	    String title, 
	    GoalArea goalArea,
	    int x, int y, 
	    int width, int height,
	    boolean closable, 
	    boolean resizable) {
	// a panel for the check boxes
	//	Dimension preferredSize = goalArea.getPreferredSize();
	//	preferredSize.width = width;
	// create and add element
	final ActivityDocumentGoalArea activityDocumentGoalArea = 
	    new ActivityDocumentGoalArea(title, resizable, closable, width, height, goalArea);
	if (activityDocument != null) {
	    activityDocument.addElement(goalArea, activityDocumentGoalArea, new Dimension(width, height), true);
	    //	    activityDocument.addDoneButtonListener(activityDocumentGoalArea);
	} else {
	    activityDocumentGoalArea.add(goalArea);
	}
	activityDocumentGoalArea.setContents(goalArea);
	activityDocumentGoalArea.setLocation(x, y);
	return activityDocumentGoalArea;
    }

    public void addDoneButtonListener(final ActivityDocumentComponent activityDocumentComponent) {
	GoalDoneButton goalDoneButton = activityDocumentComponent.getGoalDoneButton();
	if (goalDoneButton == null) {
	    return;
	}
	ActionListener doneButtonActionListener = new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		disableComponentsBelowNextGoalAndEnableThoseAbove(activityDocumentComponent);
	    }

	};

	goalDoneButton.addActionListener(doneButtonActionListener);
    }

    public String getIdName() {
	return "ActivityDocument";
    }

    public ID getId() {
	return id;
    }

    public String getDocumentName() {
	return documentName;
    }

    public void setDocumentName(String documentName) {
	this.documentName = documentName;
    }

    public Collection<ActivityDocumentComponent> getActivityDocumentComponents() {
	return activityDocumentComponents;
    }

    public void disableComponentsBelowNextGoalAndEnableThoseAbove(
	    final ActivityDocumentComponent currentGoal) {
	if (taskIdentifier == null) {
	    // no point saving or disabling if this isn't a known task
	    return;
	}
	if (URLParameters.isThumbnail()) {
	    return;
	}
	MiGenServerCommunicator migenServerCommunicator = ActivityMaker.getMigenServerCommunicator();
	if (currentGoal != null && migenServerCommunicator != null && migenServerCommunicator.isEnabled()) {  
	    saveToServer(migenServerCommunicator, ACTIVITY_DOCUMENT_SAVED_BECAUSE_DONE_BUTTON, null);
	}
	ActivityDocumentComponent nextGoal = nextGoal(currentGoal);
	if (nextGoal == null) {
	    if (currentGoal != null) {
		ActionListener actionListener = new ActionListener() {

		    @Override
		    public void actionPerformed(ActionEvent e) {
			if (!activityCompletedAndSaved) {
			    MiGenServerCommunicator migenServerCommunicator = 
				ActivityMaker.getMigenServerCommunicator();
			    if (!migenServerCommunicator.isEnabled()) {
				// save to local file system
				ActivityDocumentSaveAsTabMenuItem.saveActivityDocument(null);
			    }	    
			    activityCompletedAndSaved = true;
			    UIEventManager.processEvent(new ActivityDocumentCompletedEvent(getId().toString()));
			}
			//check if they want to build another 
			//only if check-to-build-another is true
			if (MiGenConfiguration.isCheckToBuildAnother() && userWantsToBuildAnother()) {
			    int currentGoalHeight = currentGoal.getHeight();
//			    int y = currentGoal.getY()+currentGoalHeight;
//			    int x = currentGoal.getX();
			    int x = contentsPanel.getX();
			    int y = yMax+currentGoalHeight;
			    yMax = y;
			    DocumentCanvasContainer extraEffortCanvas =
				createCanvas(
					"Put your new construction here", 
					x, 
					y, 
					currentGoal.getWidth(), 
					currentGoalHeight, 
					currentGoal.isClosable(), 
					currentGoal.isResizable(), 
					true, false, true, true, true, false);
			    extraEffortCanvas.setName("extra effort canvas #" + extraEffortCounter++);
			    // what about creating an area for the rule? 
			    // depends upon which activity is active?
			    extraEffortCanvas.getGoalDoneButton().setEnabled(true);
			    JViewport viewport = getViewport();
			    viewport.setViewPosition(new Point(x, y+currentGoalHeight));
			    MultiUniverseTabbedPanel modelCopyTabbedPanel = 
				ExpresserLauncher.getModelCopyTabbedPanel();
			    modelCopyTabbedPanel.removeTabAt(0);
			    modelCopyTabbedPanel.addPage("");
			    BlockShapeCanvasPanel modelPanel = ExpresserLauncher.getSelectedMasterPanel();
			    ObjectSetCanvas canvas = modelPanel.getCanvas();
			    ExpresserModel model = canvas.getModel();
			    canvas.loadXMLDocument(ActivityMaker.getTaskFile());
//			    model.currentShapesAreUndeletable();
			    addModel(model);
			} else if (MiGenConfiguration.isSingleActivityOnly()) {  
			    if (closingExpresserMessage()) {
				ExpresserLauncher.closeExpresserInterface();
			    }
			}			
		    }
		    
		};
		finishedButton.addActionListener(actionListener);
		finishedButton.setEnabled(true);
	    }
	    return;
	}
	int nextGoalY = enableAllAbove(nextGoal);
	disableAllBelow(nextGoalY, disabledComponents);
    }

    public void saveToServer(MiGenServerCommunicator migenServerCommunicator, String saveType, String name) {
	ExpresserModel model = new ExpresserModelImpl();
	// set the name to aid retrieval 
	String modelName = generateModelName(saveType, name);
	model.setName(modelName);
	if (ACTIVITY_DOCUMENT_SAVED_BECAUSE_SHUTDOWN.equals(saveType)) {
	    model.setAsPollable(false);	    
	}
	UserSet userSet = ExpresserLauncher.getUserSet();
	migenServerCommunicator.addModel(userSet, model);
	saveActivityDocumentOnServer(
		migenServerCommunicator, 
		userSet, 
		model);
    }

    /**
     * @param typeOfSave Currently one of 
     *   ACTIVITY_DOCUMENT_SAVED_BECAUSE_SHUTDOWN, 
     *   ACTIVITY_DOCUMENT_SAVED_BECAUSE_DONE_BUTTON,
     *   ACTIVITY_DOCUMENT_SAVED_FOR_UNDO 
     *   ACTIVITY_DOCUMENT_SAVED_BECAUSE_INDICATOR
     *   ACTIVITY_DOCUMENT_SAVED_BECAUSE_MENU_ITEM
     * @return a string containing enough information to flexibly retrieve models and activity documents
     */
    public String generateModelName(String typeOfSave, String name) {
	int taskId = taskIdentifier == null ? 0 : taskIdentifier.getId();
	return generateModelName(taskId, typeOfSave, name);
    }

    public static String generateModelName(int taskId, String typeOfSave, String name) {
	String generatedName = taskId + "_" + Long.toString(System.currentTimeMillis(), 16) + "_" + typeOfSave;
	if (name != null) {
	    generatedName += "_" + name;
	}
	return generatedName;
    }

    public boolean userWantsToBuildAnother() {
	int n = JOptionPane.showOptionDialog(
		ExpresserLauncher.getFrame(),
		MiGenUtilities.getLocalisedMessage("WantToBuildAnother"),
		MiGenUtilities.getLocalisedMessage("WantToBuildAnotherDialogTitle"),
		JOptionPane.YES_NO_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,
		null,
		null);
	return n == 0; // yes
    }

    //TODO: configure messages
    public boolean closingExpresserMessage() {
	int n = JOptionPane.showOptionDialog(
		ExpresserLauncher.getFrame(),
		"eXpresser will close now. Cancel to continue with this activity.",
		"Closing eXpresser",
		JOptionPane.CANCEL_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,
		null,
		null);
	return n == 0; // yes
    }
    @SuppressWarnings("unchecked")
    private int enableAllAbove(ActivityDocumentComponent nextGoal) {
	recursivelySetEnabled(true, nextGoal);
	int nextGoalY = nextGoal.getY();
	if (!disabledComponents.isEmpty()) {
	    ArrayList<Component> disabledComponentsClone = 
		(ArrayList<Component>) disabledComponents.clone();
	    for (Component component : disabledComponentsClone) {
		if (component.getY() <= nextGoalY) {
		    recursivelySetEnabled(true, component);
		}
	    }
	}
	return nextGoalY;
    }

    protected void disableAllBelow(int goalY, ArrayList<Component> disabledComponents) {
	for (ActivityDocumentComponent component : getActivityDocumentComponents()) {
	    if (component.getY() > goalY) {
		recursivelySetEnabled(false, component);
	    }
	}	
    }

    public void recursivelySetEnabled(boolean enable, Component component) {
	component.setEnabled(enable);
	if (enable) {
	    disabledComponents.remove(component);
	} else if (!disabledComponents.contains(component)) {
	    disabledComponents.add(component);
	}
	if (component instanceof Container) {
	    for (Component subComponent : ((Container) component).getComponents()) {
		recursivelySetEnabled(enable, subComponent);
	    }
	}
    }

    protected ActivityDocumentComponent nextGoal(ActivityDocumentComponent currentGoal) {
	// finds the next goal area after the goalArea
	// if goalArea is null finds the first one
	boolean foundFirstGoal = (currentGoal == null);
	for (ActivityDocumentComponent component : getActivityDocumentComponents()) {
	    GoalDoneButton goalDoneButton = component.getGoalDoneButton();
	    if (goalDoneButton != null) {
		if (foundFirstGoal) {
		    if (component.isEnabled() && goalDoneButton.isEnabled()) {
			// if the next goal is already enabled return null
			return null;
		    }
		    return component;
		} else if (component == currentGoal) {
		    foundFirstGoal = true;
		}
	    }
	}
	return null;
    }

    public JSplitPane getSplitPane() {
	return splitPane;
    }

    public ActivityDocumentComponent getComponentWithName(String name) {
	for (ActivityDocumentComponent activityDocumentComponent : activityDocumentComponents) {
	    if (activityDocumentComponent.getName().equals(name)) {
		return activityDocumentComponent;
	    }    
	}
	return null;
    }

    public ActivityDocumentComponent getLowestComponent() {
	int lowestY = Integer.MIN_VALUE;
	ActivityDocumentComponent bestSoFar = null;
	for (ActivityDocumentComponent activityDocumentComponent : activityDocumentComponents) {
	    int y = activityDocumentComponent.getY();
	    if (y > lowestY) {
		lowestY = y;
		bestSoFar = activityDocumentComponent;
	    }
	}
	return bestSoFar;	
    }

    public void moveFrameToBottom(ActivityDocumentComponent componentWithName) {
	ActivityDocumentComponent lowestComponent = getLowestComponent();
	if (lowestComponent != null) {
	    Point location = lowestComponent.getLocation();
	    location.translate(0, lowestComponent.getHeight());
	    componentWithName.setLocation(location);
	}	
    }

    public void setActivityDocumentComponents(
	    ArrayList<ActivityDocumentComponent> activityDocumentComponents) {
	this.activityDocumentComponents = activityDocumentComponents;
    }

    public boolean walkToTiedNumbers(Walker walker) {
	for (ActivityDocumentComponent activityDocumentComponent : activityDocumentComponents) {
	    if (!activityDocumentComponent.walkToTiedNumbers(walker)) {
		return false;
	    }
	}
	return true;
    }

    public HashMap<TiedNumberExpression<Number>, TiedNumberExpression<Number>> getTiedNumberImportTable() {
	return tiedNumberImportTable;
    }

    /**
     * @return a list of models that always contains at least one element
     * The first element is the original model, the others are added if the user selects to
     * try other solutions.
     */
    public List<ExpresserModel> getModels() {
	return models;
    }

    /**
     * @return The original model created when the activity started.
     */
    public ExpresserModel getFirstModel() {
	return models.get(0);
    }
    
    /**
     * @param model -- model to add to the associated models
     */
    public void addModel(ExpresserModel model) {
	models.add(model);
	model.setTaskIdentifier(getTaskIdentifier());
    }

    public void setModels(List<ExpresserModel> initialModels) {
	models.clear();
	for (ExpresserModel model : initialModels) {
	    addModel(model);
	}
    }

    /**
     * @deprecated in favour of ActivityMaker.mergeActivityDocuments
     */
    @Deprecated 
    public static Point mergeActivityDocuments(
	    ActivityDocument baseDocument, 
	    ActivityDocument student1, 
	    ActivityDocument student2, 
	    ActivityDocument student3,
	    int y) {
	Point point = new Point(0, y);
	if (student1 != null) {
	    point = ActivityMaker.addComponentsFromAnotherActivityDocument(
		    1, baseDocument, student1, point); 
	}
	if (student2 != null) {
	    point = ActivityMaker.addComponentsFromAnotherActivityDocument(
		    2, baseDocument, student2, point);
	}
	if (student3 != null) {
	    point = ActivityMaker.addComponentsFromAnotherActivityDocument(
		    3, baseDocument, student3, point); 
	}
	return point;    
    }

    /**
     * Replaces the current activity document with this.
     * Optionally loads the associated models of the activity document (if it has any).
     */
    public void installActivityDocument(boolean installModels, ObjectCanvasFrame<?> frame) {
	Collection<ActivityDocumentComponent> activityDocumentComponents = getActivityDocumentComponents();
	if (frame == null) {
	    frame = ExpresserLauncher.getFrame();
	}
	ActivityDocument activityDocument = frame.getActivityDocument();
	activityDocument.removeContents();
	activityDocument.disabledComponents = new ArrayList<Component>(disabledComponents);
	for (ActivityDocumentComponent activityDocumentComponent : activityDocumentComponents) {
	    activityDocument.addFrame(
		    activityDocumentComponent,
		    activityDocumentComponent.getX(),
		    activityDocumentComponent.getY());
	    if (activityDocumentComponent.isMirrorChangesInMyWorld()) {
		generalModelCanvas = (DocumentCanvasContainer) activityDocumentComponent;
		DocumentCanvas documentCanvas = generalModelCanvas.getDocumentCanvas();
		ExpresserModel masterModel = ExternalInterface.getSelectedExpresserModelPanel().getModel();
		ModelCopier newModelCopier = new ModelCopier(masterModel, documentCanvas);
		newModelCopier.copyModelProcess();
		documentCanvas.setModelCopier(newModelCopier);
		generalModelCanvas.addTotalAllocationLabel(activityDocument);
	    }
	}
//	activityDocument.disableComponentsBelowNextGoalAndEnableThoseAbove(null);
	activityDocument.setSize(activityDocument.getPreferredSize());
	activityDocument.setVisible(true);
	ActivityMaker.createTypeEmptyActivityDocument("", activityDocument, "", null, false, generalModelCanvas == null);
	if (installModels) {
	    openTabsForModels();
	    activityDocument.setModels(models);
	}
	JSplitPane currentSplitPane = activityDocument.getSplitPane();
	if (currentSplitPane != null) {
	    currentSplitPane.setDividerLocation(
		    Math.max(MiGenConfiguration.getDividerLocation(), currentSplitPane.getDividerLocation()));
	}
	if (taskIdentifier != null) {
	    activityDocument.setTaskIdentifier(taskIdentifier);
	}
	if (generalModelCanvas != null) {
	    activityDocument.setGeneralModelCanvas(generalModelCanvas);
	}
    }

    /**
     * Opens a tab for each model associated with this activity document.
     */
    protected void openTabsForModels() {
	// TODO: determine if this could be simplified so the first branch of the conditional
	// is when the index is 0 and no need for selectedExpresserModelPanel
	ExpresserModelPanel selectedExpresserModelPanel = 
	    ExternalInterface.getSelectedExpresserModelPanel();
	for (int i = 0; i < models.size(); i++) {
	    ExpresserModel model = models.get(i);
	    MultiUniverseTabbedPanel tabbedPanel = ExpresserLauncher.getModelCopyTabbedPanel();
	    String name = model.getName();
	    if (selectedExpresserModelPanel != null) {
		// first model replaces the current model
		ExpresserModel existingModel = selectedExpresserModelPanel.getModel();
		selectedExpresserModelPanel.getCanvas().replaceModelWith(existingModel, model);
		int selectedIndex = tabbedPanel.getSelectedIndex();
		tabbedPanel.setTitleAt(selectedIndex, name);
		models.set(i, existingModel);
//		if (generalModelCanvas != null) {
//		    removeFrame(generalModelCanvas);
//		    addGeneralWorld();
//		}
		// only do this for the first one
		selectedExpresserModelPanel = null;
	    } else {
		tabbedPanel.addPage(name);
		int tabIndex = tabbedPanel.indexOfTab(name);
		ExpresserModel masterModel = tabbedPanel.getModelAt(tabIndex);
		BlockShapeCanvasPanel modelPanel = ExpresserLauncher.getSelectedMasterPanel();
		if (modelPanel != null) {
		    ObjectSetCanvas canvas = modelPanel.getCanvas();
		    canvas.replaceModelWith(masterModel, model);
		}
		models.set(i, masterModel);
	    }
	}
    }

    public TaskIdentifier getTaskIdentifier() {
	return taskIdentifier;
    }

    public void setTaskIdentifier(TaskIdentifier taskIdentifier) {
	this.taskIdentifier = taskIdentifier;
	for (ExpresserModel model : models) {
	    model.setTaskIdentifier(taskIdentifier);
	}
    }

    protected void addFinishedButton() {
	finishedButton = new FinishedButton();
	JPanel finishedButtonPanel = new JPanel(new GridBagLayout());
	GridBagConstraints gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.weightx = 0.0f;
	gridBagConstraints.anchor = GridBagConstraints.SOUTH;
	finishedButtonPanel.add(finishedButton, gridBagConstraints);
	add(finishedButtonPanel, BorderLayout.SOUTH);
    }

    public int getXMax() {
        return xMax;
    }

    public int getYMax() {
        return yMax;
    }

    public DocumentCanvasContainer getGeneralModelCanvas() {
        return generalModelCanvas;
    }

    public void setGeneralModelCanvas(DocumentCanvasContainer generalModelCanvas) {
        this.generalModelCanvas = generalModelCanvas;
    }

}

