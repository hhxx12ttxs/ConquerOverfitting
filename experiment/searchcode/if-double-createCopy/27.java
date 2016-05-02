package uk.ac.lkl.migen.system.expresser.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.ac.lkl.common.ui.jft.Stencil;
import uk.ac.lkl.common.util.ID;
import uk.ac.lkl.common.util.IDFactory;
import uk.ac.lkl.common.util.IDObject;
import uk.ac.lkl.common.util.ObjectWithID;
import uk.ac.lkl.common.util.config.MiGenConfiguration;
import uk.ac.lkl.common.util.event.SourcedUpdateSupport;
import uk.ac.lkl.common.util.event.UpdateEvent;
import uk.ac.lkl.common.util.event.UpdateListener;
import uk.ac.lkl.common.util.expression.Expression;
import uk.ac.lkl.common.util.expression.ExpressionList;
import uk.ac.lkl.common.util.expression.LocatedExpression;
import uk.ac.lkl.common.util.expression.ModifiableOperation;
import uk.ac.lkl.common.util.expression.Operator;
import uk.ac.lkl.common.util.expression.operator.NumberAdditionOperator;
import uk.ac.lkl.common.util.expression.operator.NumberMultiplicationOperator;
import uk.ac.lkl.common.util.expression.operator.NumberSubtractionOperator;
import uk.ac.lkl.common.util.value.Number;
import uk.ac.lkl.common.util.value.Value;
import uk.ac.lkl.migen.system.ExpresserLauncher;
import uk.ac.lkl.migen.system.ai.feedback.ui.callout.ModalPaperNoteCallOut;
import uk.ac.lkl.migen.system.expresser.model.AttributeHandle;
import uk.ac.lkl.migen.system.expresser.model.ExpresserModel;
import uk.ac.lkl.migen.system.expresser.model.StandaloneWalker;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BlockShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.GroupShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.PatternShape;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.TiedNumberExpression;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.UnspecifiedTiedNumberExpression;
import uk.ac.lkl.migen.system.expresser.ui.ecollaborator.ActivityDocument;
import uk.ac.lkl.migen.system.expresser.ui.uievent.TiedNumberColorEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.TiedNumberDragStartEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.TiedNumberMenuItemEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.TiedNumberMenuPopupEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.TiedNumberSlaveValueChangedEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.TiedNumberValueEditedEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.UIEventManager;
import uk.ac.lkl.migen.system.util.MiGenUtilities;

public class TiedNumberPanel<V extends Value<V>> extends JPanel 
       implements Transferable, DragGestureListener, DragSourceListener, IDObject, ObjectWithID {

    public static final String NAME_IF_NO_NAME_GIVEN = TiedNumberExpression.getUnnamedName();

    // FIXME: @Hack(who = "MM", why = "WOZ", issues = 0)
    // used for WOZ to fully lockdown expresser (i.e. do not allow further
    // unlocking)
    public static boolean expresserFullyLockedDown = false;

    private UpdateListener<Expression<Number>> tiedNumberUpdateListener =
	new UpdateListener<Expression<Number>>() {

	@Override
	public void objectUpdated(UpdateEvent<Expression<Number>> e) {
	    String currentTextValue = valueTextField.getText();
	    String tiedNumberTextValue = tiedNumber.evaluate().toString();
	    // following causes
	    // Exception in thread "AWT-EventQueue-0"
	    // java.lang.IllegalStateException: Attempt to mutate in
	    // notification
	    // if the value was edited but is the same integer (e.g.
	    // 1 and 01).
	    if (!tiedNumberTextValue.equals(currentTextValue)) {
		try {
		    valueTextField.setText(tiedNumberTextValue);
		    if (readOnly &&
			TiedNumberPanel.this.isDisplayable()) {
			// TODO: figure out why some non-displayable
			// tied number panels listen to this
			UIEventManager.processEvent(
				new TiedNumberSlaveValueChangedEvent(getIdentityForLogMessage()));
//			logMessage("Slave tied number value changed.");
		    }
//		    invalidate();
		} catch (java.lang.IllegalStateException ignore) {
		    // don't care if editing, for example, 03 changes it to 3
		}
	    }
	    updateSize();
	    // following attempt to fix the exception made it worse
	    // int currentValue = tiedNumber.evaluate().getInt();
	    // try {
	    // int newValue = Integer.parseInt(currentTextValue);
	    // if (newValue == currentValue) {
	    // return; // change already noted
	    // } else {
	    // valueTextField.setText(Integer.toString(newValue));
	    // }
	    // } catch (NumberFormatException numberFormatException) {
	    // return;
	    // }
	}

    };

    private UpdateListener<TiedNumberExpression<Number>> lockStatusUpdateListener =
	new UpdateListener<TiedNumberExpression<Number>>() {

	@Override
	public void objectUpdated(
		UpdateEvent<TiedNumberExpression<Number>> e) {
	    reflectChangableStatus();
	    if (!tiedNumber.isLocked()) {
		// was shared earlier
		MasterSlaveUniverseMicroworldPanel masterSlaveUniverseMicroworldPanel =
		    getMasterSlaveUniverseMicroworldPanel();
		if (masterSlaveUniverseMicroworldPanel != null) {
//		    BlockShapeCanvasPanel slavePanel =
//			masterSlaveUniverseMicroworldPanel.getSlavePanel();
//		    if (slavePanel.isAncestorOf(TiedNumberPanel.this)) {
//			ExpresserModel slaveModel = slavePanel.getModel();
//			if (slaveModel != null) {
//			    final ExpresserModelSlaveCopy expresserModelSlaveCopy =
//				(ExpresserModelSlaveCopy) slaveModel;
//			    TiedNumberExpression<?> slaveCopy =
//				expresserModelSlaveCopy.getSlaveCopy(tiedNumber);
//			    if (slaveCopy != tiedNumber) {
//				setTiedNumber((TiedNumberExpression<IntegerValue>) slaveCopy);
//				// use a fresh slave copy if the master changes
//				final TiedNumberExpression<IntegerValue> originalMaster =
//				    tiedNumber;
//				UpdateListener<Expression<IntegerValue>> updateListener =
//				    new UpdateListener<Expression<IntegerValue>>() {
//
//				    public void objectUpdated(
//					    UpdateEvent<Expression<IntegerValue>> e) {
//					TiedNumberExpression<?> slaveCopy =
//					    expresserModelSlaveCopy.getSlaveCopy(originalMaster);
//					setTiedNumber((TiedNumberExpression<IntegerValue>) slaveCopy);
//				    }
//
//				};
//				tiedNumber.addUpdateListener(updateListener);
//				slaveModel.setDirtyModel(true);
//			    }
//			}
//		    } else {
			masterSlaveUniverseMicroworldPanel.getModel().setDirtyModel(true);
//		    }
		}
	    }
	}

    };

    // Some tied number panels are not to be dragged (e.g. those in the slave
    // universe)
    private boolean dragEnabled = false;

    // the tied number this panel represents
    protected TiedNumberExpression<Number> tiedNumber;

    // text field for the value of the number
    protected JTextField valueTextField = new JTextField();

    // TODO: determine if this is now obsolete
    // and only valueTextField is needed
    protected JPanel valueTextFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    // this is where the name or description of the tied number
    // is displayed and can be edited
    // only used with the old horizontal layout
    private JTextField nameField = null;
    
    // vertical layout doesn't support implicit editing
    // of names but must be explicitly requested
    // via a menu item
    private JLabel nameLabel = null;
    
    // when editing the name the following
    // replaces the nameLabel
    private JTextField nameEditField = null;

    private JPanel nameFieldPanel;
    
    private BorderLayout borderLayout;

    private static Color defaultUnlockedFieldBackgroundColor =
	new Color(255, 255, 200); // light yellow

    private static Color defaultLockedFieldBackgroundColor =
	new Color(220, 220, 220); // lighter gray than Color.LIGHT_GRAY

    /**
     * If this is replaced by an expression replacement is set
     */
    private Expression<Number> replacement = null;

    /**
     * ExpressionPanel will want to know if this is replaced
     */
    private SourcedUpdateSupport<TiedNumberPanel<V>> updateSupport =
	new SourcedUpdateSupport<TiedNumberPanel<V>>(this);

    // automates changing the value of this tied number
    private JButton playButton = null;

    // for drag and drop need a flavor for TiedNumberPanels
    static public DataFlavor tiedNumberPanelFlavor = createTiedNumberPanelDataFlavor();

    private static DataFlavor[] dataFlavors = { tiedNumberPanelFlavor };

    // by default copy the panel when dragging but 
    // becomes move when left on the canvas
    private int transferMode = TransferHandler.COPY;

    private DragGestureRecognizer dragGestureRecognizer = null;

    private boolean dropEnabled;

    protected DocumentListener valueEditedListener = new DocumentListener() {

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
	    acceptEditedValue();
	    if (!readOnly) {
		UIEventManager.processEvent(
			new TiedNumberValueEditedEvent(getIdentityForLogMessage(), true));
	    }
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	    acceptEditedValue();
	    if (!readOnly) {
		UIEventManager.processEvent(
			new TiedNumberValueEditedEvent(getIdentityForLogMessage(), false));
	    }
	}

    };

    private boolean readOnly;
    
    private Boolean containedInMasterPanel = null;

    // used to maintain the location that the slave of this was moved to
    private Point slaveLocation;

    // each panel needs an id for logging since the same number can be inside
    // multiple panels
    private ID id;
    
    private ExpressionPanel<Number> dragee = null;

    private DragSource dragSource = null;

    private DisplayDragSourceDragSourceMotionListener dragSourceMotionListener =
	null;

    private UpdateListener<TiedNumberExpression<Number>> nameFieldUpdateListener =
	null;

    private UpdateListener<TiedNumberExpression<Number>> displayModeUpdateListener =
	null;

    private TiedNumberPanel<V> numberPanelStartingDrag = null;

    public static Font defaultFont = new Font("Courier", Font.PLAIN, 14);

    public static final Border DEFAULT_BORDER_HORIZONTAL = 
	BorderFactory.createEtchedBorder();
    
    public static final Border DEFAULT_BORDER_VERTICAL = 
	BorderFactory.createEtchedBorder(Color.BLUE, Color.LIGHT_GRAY);
      
    // defining the border size
    private static final int VERTICAL_GAP = 3;
    private static final int HORIZONTAL_GAP = 3;

    // font for value
    public static Font VALUE_FONT = null;

    // this needs to be a fixed width font to avoid lots of white space
    // appearing to the right of the name
    public static Font NAME_FONT_HORIZONTAL = new Font("Courier", Font.BOLD, 20);
    
    // following is font used by Windows for title bars
    // and available on Macs 
    public static Font NAME_FONT_VERTICAL = null;

    protected static final Color NAME_IN_USE_COLOR = Color.MAGENTA;
    protected static final Color NAME_IMPROPER_COLOR = Color.RED;
    protected static final Color NAME_NOT_GIVEN_COLOR = Color.PINK;
    // text colour when vertical looks more like a typical title bar
    protected static final Color NAME_OK_COLOR_VERTICAL = Color.WHITE;
    protected static final Color NAME_OK_COLOR_HORIZONTAL = Color.BLACK;
    
    // use title bar default colour in Windows XP
    public static final Color NAME_BACKGROUND_COLOR_VERTICAL = new Color(2, 105, 254);

    public TiedNumberPanel(final TiedNumberExpression<Number> tiedNumber) {
	this(tiedNumber, false);
    }

    public TiedNumberPanel(final TiedNumberExpression<Number> tiedNumber, boolean readOnly) {
	this.readOnly = readOnly;
	id = IDFactory.newID(this);
	nameFieldPanel = new JPanel(new BorderLayout());
	borderLayout = new BorderLayout();
	setLayout(borderLayout);
	setTiedNumber(tiedNumber);
	displayModeUpdated();
//	if (!readOnly) {
//	    addHierarchyListener();
//	}
	// needed even if readOnly but many menu items will be disabled
	// change in policy with Issue 946 that menu pops up only if not read only
	if (!readOnly) {
	    addPopupMenuOnClick();
	}
    }
    
    @Override
    public String getIdName() {
	return "TiedNumberPanel";
    }

//    protected void addHierarchyListener() {
//	addHierarchyListener(new HierarchyListener() {
//
//	    public void hierarchyChanged(HierarchyEvent e) {
//		displayModeUpdated();
//		setDropEnabled(isOnACanvas());
//	    }
//	}
//	);
//    }

    public void setTiedNumber(final TiedNumberExpression<Number> tiedNumber) {
	if (this.tiedNumber == tiedNumber) {
	    return;
	}
	removeAll();
	this.tiedNumber = tiedNumber;
	String name = tiedNumber.getName();
	if (!editingName()) {
	    if (nameLabel != null) {
		nameFieldPanel.remove(nameLabel);
	    }
	    // put spaces on both sides to look nicer
	    nameLabel = new JLabel(" " + name + " " ); // + authorInfo()
	    nameLabel.setBackground(NAME_BACKGROUND_COLOR_VERTICAL);
	    nameLabel.setForeground(NAME_OK_COLOR_VERTICAL);
	    nameLabel.setFont(nameFont());
	    nameFieldPanel.add(nameLabel, BorderLayout.CENTER);
	}
	tiedNumber.addLockStatusUpdateListener(lockStatusUpdateListener);
	addTiedNumberValueUpdateListener();
	addDisplayModeUpdateListener();
	addNameFieldUpdateListener();
	valueTextField.setBackground(defaultUnlockedFieldBackgroundColor);
	String newTextValue = tiedNumber.getValue().toString();
	String oldTextValue = valueTextField.getText();
	if (!newTextValue.equals(oldTextValue)) {
	    valueTextField.setText(newTextValue);
	}
	addDocumentListener();
	reflectChangableStatus();
	valueTextFieldPanel.removeAll();
	valueTextFieldPanel.add(valueTextField, BorderLayout.CENTER);
	// was !readOnly but see
	// http://code.google.com/p/migen/source/detail?r=2813
	setDragEnabled(true);
	displayModeUpdated();
    }

//    private String authorInfo() {
//	if (MiGenConfiguration.isShowAuthorOfVariables() && 
//	    tiedNumber.displayAuthorInfo()) {
//	    return MiGenUtilities.getLocalisedMessage("AuthorStart") + 
//	           tiedNumber.getAuthor() + 
//	           MiGenUtilities.getLocalisedMessage("AuthorEnd");
//	} else {
//	    return "";
//	}
//    }

    protected void reflectChangableStatus() {
	boolean changeable = !tiedNumber.isLocked();
	setEditable(changeable);
	updateValueTextFieldBackground();
	setBorderAndFontForValueOnly();
	displayModeUpdated();
    }

    protected void setEditable(boolean changeable) {
	valueTextField.setEditable(changeable && okToEditValue());
    }

    private void updateValueTextFieldBackground() {
	// TODO: determine if the names of colors below should be swapped for clarity
	if (tiedNumber.isLocked()) {
	    valueTextField.setBackground(defaultUnlockedFieldBackgroundColor);    
	} else {    
	    valueTextField.setBackground(defaultLockedFieldBackgroundColor);
	}
    }

    protected void addDocumentListener() {
	valueTextField.getDocument().addDocumentListener(valueEditedListener);
    }

    protected void setBorderAndFontForValueOnly() {
	valueTextField.setFont(getValueFont());
	valueTextField.setBorder(defaultBorder());
    }

    private void setBorderAndFontForNameAndValue() {
	valueTextField.setFont(getValueFont()); 
	valueTextField.setBorder(null);
	if (nameField != null) {
	    nameField.setBorder(null);
	}
    }

    protected void addPopupMenuOnClick() {
	addMouseListener(new MouseListener() {

	    @Override
	    public void mouseClicked(MouseEvent e) {
	    }

	    @Override
	    public void mouseEntered(MouseEvent e) {
		TiedNumberPanel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		TiedNumberPanel.this.setCursor(Cursor.getDefaultCursor());
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
		addPopupMenu();
	    }

	});
    }

    public JPopupMenu addPopupMenu() {
	UIEventManager.processEvent(new TiedNumberMenuPopupEvent(getIdentityForLogMessage(), "Main"));
//	logMenuPoppedUp("Main");
	JPopupMenu menu = new JPopupMenu();
	menu.add(lockUnlockMenuItem());
	menu.add(showNameAndValueMenuItem());
	menu.add(showNameOnlyMenuItem());
	menu.add(showValueOnlyMenuItem());
	menu.add(editNameMenuItem());
	menu.add(removeThisMenuItem());
	menu.add(copyThisMenuItem());
	JMenuItem menuItem = showContainersMenuItem(false, false);
	if (menuItem != null) {
	    menu.add(menuItem);
	}
	if (MiGenConfiguration.isAddMenuItemToShowAnythingUsingTiedNumber()) {
	    menuItem = showContainersMenuItem(true, false);
	    if (menuItem != null) {
		menu.add(menuItem);
	    }
	}
	menu.show(this, 0, 0);
	return menu;
    }
    
    protected JMenuItem lockUnlockMenuItem() {
	final ExpresserModel model = getModel();
	JMenuItem item;
	if (!getTiedNumberExpression().isLocked()) {
	    final String label = MiGenUtilities.getLocalisedMessage("Lock");
	    item = new JMenuItem(label);
	    item.addActionListener(new ActionListener() {

		
		@Override
		public void actionPerformed(ActionEvent e) {
		    UIEventManager.processEvent(
			    new TiedNumberMenuItemEvent(getIdentityForLogMessage(), label));
//		    logActionPerformed(label);
		    valueTextField.setEditable(false);
		    // remove if it is there
		    model.unlockedTiedNumberRemoved(getTiedNumberExpression()); 
		    displayModeUpdated();
		}
	    });
	} else {
	    final String label =
		MiGenUtilities.getLocalisedMessage("Unlock");
	    item = new JMenuItem(label);
	    if (getTiedNumberExpression().isKeyAvailable()) {
		item.addActionListener(new ActionListener() {

		    
		    @Override
		    public void actionPerformed(ActionEvent e) {
			UIEventManager.processEvent(
				new TiedNumberMenuItemEvent(getIdentityForLogMessage(), label));
//			logActionPerformed(label);
			// following will also set the flag
			model.unlockedTiedNumberAdded(getTiedNumberExpression());
			valueTextField.setEditable(okToEditValue());
			displayModeUpdated();
		    }
		});
	    } else {
		item.setEnabled(false);
	    }
	}
	if (isReadOnly() || expresserFullyLockedDown || model == null) {
	    // model can be null if canvas is part of an activity document
	    item.setEnabled(false);
	}
	return item;
    }
    
    protected JMenuItem showNameAndValueMenuItem() {
	final ExpresserModel model = getModel();
	final int displayMode = getTiedNumberExpression().getDisplayMode();
	final boolean unnamed = !tiedNumber.isNamed();
	final String showOrGiveName =
	      unnamed ? MiGenUtilities.getLocalisedMessage("GiveName")
		      : MiGenUtilities.getLocalisedMessage("ShowNameValue");
	JMenuItem item = new JMenuItem(showOrGiveName);
	if (displayMode == TiedNumberExpression.DISPLAY_NAME_AND_VALUE) {
	    item.setEnabled(false);
	} else {
	    item.addActionListener(new ActionListener() {

		
		@Override
		public void actionPerformed(ActionEvent e) {
		    UIEventManager.processEvent(
			    new TiedNumberMenuItemEvent(getIdentityForLogMessage(), showOrGiveName));
//		    logActionPerformed(showOrGiveName);
		    tiedNumber.setDisplayMode(TiedNumberExpression.DISPLAY_NAME_AND_VALUE);
		    if (unnamed) {
			editName();
		    }
		    ExpresserLauncher.repaint();
		}
	    });
	}
	if (isReadOnly() || model == null) {
	    item.setEnabled(false);
	}
	return item;
    }
    
    protected JMenuItem showNameOnlyMenuItem() {
	final String showNameLabel =
	    MiGenUtilities.getLocalisedMessage("ShowNameOnly");
	final ExpresserModel model = getModel();
	final int displayMode = getTiedNumberExpression().getDisplayMode();
	final boolean unnamed = !tiedNumber.isNamed();
	JMenuItem item = new JMenuItem(showNameLabel);
	if (displayMode == TiedNumberExpression.DISPLAY_ONLY_NAME || unnamed) {
	    item.setEnabled(false);
	} else {
	    item.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    UIEventManager.processEvent(
			    new TiedNumberMenuItemEvent(getIdentityForLogMessage(), showNameLabel));
//		    logActionPerformed(showNameLabel);
		    tiedNumber.setDisplayMode(TiedNumberExpression.DISPLAY_ONLY_NAME);    
		    // so property mirrors are updated
		    ExpresserLauncher.setCurrentPanelDirty(); 
		    ExpresserLauncher.repaint();
		}
	    });
	}
	if (isReadOnly() || model == null) {
	    item.setEnabled(false);
	}
	return item;
    }
    
    protected JMenuItem showValueOnlyMenuItem() {
	final String showValueOnlyLabel =
	    MiGenUtilities.getLocalisedMessage("ShowValueOnly");
	final ExpresserModel model = getModel();
	final int displayMode = getTiedNumberExpression().getDisplayMode();
	JMenuItem item = new JMenuItem(showValueOnlyLabel);
	if (displayMode == TiedNumberExpression.DISPLAY_ONLY_VALUE) {
	    item.setEnabled(false);
	} else {
	    item.addActionListener(new ActionListener() {

		
		@Override
		public void actionPerformed(ActionEvent e) {
		    UIEventManager.processEvent(
			    new TiedNumberMenuItemEvent(getIdentityForLogMessage(), showValueOnlyLabel));
//		    logActionPerformed(showValueOnlyLabel);
		    tiedNumber.setDisplayMode(TiedNumberExpression.DISPLAY_ONLY_VALUE);
		    // so property mirrors are updated
		    ExpresserLauncher.setCurrentPanelDirty(); 
		    // the following works around the problem that if this tied
		    // number is also in the task variables panel it needs
		    // to be repainted
		    // TODO: determine if the following is no longer needed
		    ExpresserLauncher.repaint();
		}
	    });
	}
	if (isReadOnly() || model == null) {
	    item.setEnabled(false);
	}
	return item;
    }
    
    protected JMenuItem editNameMenuItem() {
	final int displayMode = getTiedNumberExpression().getDisplayMode();
	final String editNameLabel =
	    MiGenUtilities.getLocalisedMessage("EditName");
	JMenuItem item = new JMenuItem(editNameLabel);
	if (displayMode == TiedNumberExpression.DISPLAY_NAME_AND_VALUE || displayMode == TiedNumberExpression.DISPLAY_ONLY_NAME) {
	    item.addActionListener(new ActionListener() {

		
		@Override
		public void actionPerformed(ActionEvent e) {
		    UIEventManager.processEvent(
			    new TiedNumberMenuItemEvent(getIdentityForLogMessage(), editNameLabel));
//		    logActionPerformed(editNameLabel);
		    editName();
		}
	    });
	} else {
	    item.setEnabled(false);
	}
	return item;
    }
    
    protected JMenuItem removeThisMenuItem() {
	final String removeExpressionLabel =
	    MiGenUtilities.getLocalisedMessage("RemoveExpressionFromCanvas");
	JMenuItem item = new JMenuItem(removeExpressionLabel);
	final Container parent = TiedNumberPanel.this.getParent();
	if (!(parent instanceof ExpressionPanel<?>)) {
	    MiGenUtilities.printError("Expected this tied number panel to be part of an expression panel.");
	    return item;
	}
	final ExpressionPanel<?> expressionPanelParent = (ExpressionPanel<?>) parent;
	item.addActionListener(new ActionListener() {

	    @Override
	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    public void actionPerformed(ActionEvent e) {
		UIEventManager.processEvent(
			new TiedNumberMenuItemEvent(getIdentityForLogMessage(), removeExpressionLabel));
		Container grandParent = expressionPanelParent.getParent();
		if (expressionPanelParent.isOnACanvas() && expressionPanelParent.okToRemoveExpression()) {
		    grandParent.remove(expressionPanelParent);
		    grandParent.repaint();
		} else {    
		    if (grandParent instanceof ExpressionPanel &&
			    ((ExpressionPanel<?>) grandParent).okToRemoveExpression()) {
		        ExpressionPanel.removeSubExpression(
		    	    (ExpressionPanel<Number>) parent, 
		    	    (ExpressionPanel<Number>) expressionPanelParent.getParent());
		    } else {
		        expressionPanelParent.setExpression(new UnspecifiedTiedNumberExpression());
		        grandParent.validate();
		        grandParent.repaint();
		    }
		}
		ExpresserLauncher.setCurrentModelDirty();
	    }
	});
	return item;
    }
    
    protected JMenuItem copyThisMenuItem() {
	final String copyExpressionLabel =
	    MiGenUtilities.getLocalisedMessage("CopyExpression");
	JMenuItem item = new JMenuItem(copyExpressionLabel);
	Container parent = TiedNumberPanel.this.getParent();
	if (!(parent instanceof ExpressionPanel<?>)) {
	    MiGenUtilities.printError("Expected this tied number panel to be part of an expression panel.");
	    return item;
	}
	final ExpressionPanel<?> expressionPanelParent = (ExpressionPanel<?>) parent;
	if (isOnACanvas()) {
	    item.addActionListener(new ActionListener() {

		
		@Override
		public void actionPerformed(ActionEvent e) {
		    UIEventManager.processEvent(
			    new TiedNumberMenuItemEvent(getIdentityForLogMessage(), copyExpressionLabel));
		    ExpressionPanel<?> copy = expressionPanelParent.createCopy();
		    getCanvas().add(copy);
		    copy.setLocation(expressionPanelParent.getX() + expressionPanelParent.getWidth() / 2, 
			             expressionPanelParent.getY() + expressionPanelParent.getHeight() / 2);
		    copy.setDragCreatesCopy(false);
		}
	    });
	} else {
	    item.setEnabled(false);
	}
	return item;
    }
    
    protected JMenuItem showContainersMenuItem(
	    final boolean considerEverything,
	    final boolean searchActivityDocument) {
	final ObjectSetCanvas canvas = findCanvas();
	if (canvas == null) {
	    return null;
	}
	final List<ExpresserModel> modelsOfHighlightedProxies = canvas.getModelsOfHighlightedProxies();
	final List<java.util.Timer> timersOfHighlightedTiedNumberPanels = canvas.getTimersOfHighlightedTiedNumberPanels();
	final ExpresserModel model = canvas.getModel();
	boolean alreadyHighlighting = 
	    (model != null && !model.getTemporaryHighlightedShapes().isEmpty()) ||
	    !modelsOfHighlightedProxies.isEmpty() ||
	    !timersOfHighlightedTiedNumberPanels.isEmpty();
	if (alreadyHighlighting && considerEverything) {
	    // don't want to copies of the remove highlighting menu item
	    return null;
	}
	final boolean constructionExpressionTiedNumber = 
	    getTiedNumberExpression().isConstructionExpressionCoefficient();
	String messageName;
	if (alreadyHighlighting) {
	    messageName = "RemoveHighlighting"; 
	} else if (considerEverything) {
	    messageName = "ShowTiedNumberContainers";
	} else {
	    if (constructionExpressionTiedNumber) {
		messageName = "showBBWithThisInConstructionExpression";
	    } else {
		messageName = "ShowPatternsRepeatingWithTiedNumber";
	    }	    
	}
	final String label = MiGenUtilities.getLocalisedMessage(messageName);
	JMenuItem item = new JMenuItem(label);
	if (model == null) { // used to include isReadOnly() || 
	    item.setEnabled(false);
	} else {
	    ActionListener processEventAction = 
		new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
		    UIEventManager.processEvent(
			    new TiedNumberMenuItemEvent(getIdentityForLogMessage(), label));
		}
	    };
	    item.addActionListener(processEventAction);
	    ActionListener action; 
	    if (alreadyHighlighting) {
		action =
		    new ActionListener() {

		    @Override
		    public void actionPerformed(ActionEvent e) {
			canvas.clearHighlighting();
			canvas.repaint();
		    }
		};	
	    } else {
		action =
		    new ActionListener() {

		    @Override
		    public void actionPerformed(ActionEvent e) {
			StandaloneWalker walker = new StandaloneWalker() {

			    @Override
			    public boolean tiedNumberFound(
				    TiedNumberExpression<?> encounteredTiedNumber,
				    BlockShape shape,
				    AttributeHandle<Number> handle,
				    ExpresserModel containingModel) {
				if (containingModel == null) {
				    return true;
				}
				if (shape == null) {
				    return true;
				}
				if (tiedNumber.getOriginal() == encounteredTiedNumber.getOriginal()) {
				    if (constructionExpressionTiedNumber) {
					BlockShape superShape = shape.getSuperShape();
					if (superShape instanceof GroupShape) {
					    GroupShape superGroupShape = (GroupShape) superShape;
					    if (!superGroupShape.isEntireModel()) {
						List<BlockShape> shapes = superGroupShape.getShapes();
						for (BlockShape subShape : shapes) {
						    if (subShape.equivalent(shape)) {
							// equivalent sibling so highlight it
							containingModel.addTemporaryHighlightedShape(subShape);
						    }
						}
					    }
					} else {
					    containingModel.addTemporaryHighlightedShape(shape);
					}
				    } else if (considerEverything || handle == PatternShape.ITERATIONS) {
					BlockShape shapeToHighlight;
					if (shape instanceof PatternShape && !considerEverything) {
					    PatternShape patternShape = (PatternShape) shape;
					    // first shape is adjusted for negative deltas
					    // unlike getShape()
					    shapeToHighlight = patternShape.getShape(0);
					} else {
					    shapeToHighlight = shape;
					}
					if (searchActivityDocument && containingModel != null) {
					    containingModel.addTemporaryHighlightedShape(shapeToHighlight);
					    if (!modelsOfHighlightedProxies.contains(containingModel)) {
						modelsOfHighlightedProxies.add(containingModel);
					    }
					} else {
					    containingModel.addTemporaryHighlightedShape(shapeToHighlight);
					}
				    }
				}
				return true;
			    }
			    
			    @Override
			    public boolean tiedNumberPanelFound(String expressionPanelOfTiedNumberId) {
				ExpressionPanel<Number> expressionPanelOfTiedNumber = 
				    ExpressionPanel.fetchExpressionPanelByIdString(expressionPanelOfTiedNumberId);		
				if (considerEverything &&
					expressionPanelOfTiedNumber.getTiedNumberExpression().getOriginal() == 
					    tiedNumber.getOriginal()) {
				    Color originalBackground = expressionPanelOfTiedNumber.getBackground();	
				    java.util.Timer timer = expressionPanelOfTiedNumber.createHighlightTimer(originalBackground);
				    timersOfHighlightedTiedNumberPanels.add(timer);
				}
				return true;		    
			    }

			};
			ActivityDocument activityDocument = null;
			if (searchActivityDocument) {
			    activityDocument = canvas.getActivityDocument();
			}
			if (activityDocument == null) {
			    if (canvas.walkToTiedNumbers(walker, true)) {
				BlockShapeCanvasPanel masterPanel = ExpresserLauncher.getSelectedMasterPanel();
				ArrayList<ExpressionPanel<Number>> globalAllocationExpressionPanels = 
				    masterPanel.getGlobalAllocationExpressionPanels();
				if (globalAllocationExpressionPanels != null) {
				    for (ExpressionPanel<Number> expressionPanel : globalAllocationExpressionPanels) {
					expressionPanel.walkToTiedNumbers(walker);
				    }
				}
//				if (slaveCanvas != null) {
//				    slaveCanvas.walkToTiedNumbers(walker, true);
//				}
			    }
			} else {
			    activityDocument.walkToTiedNumbers(walker);
			    ExpresserLauncher.setCurrentPanelDirty();
			}
			if (modelsOfHighlightedProxies.isEmpty() &&
				timersOfHighlightedTiedNumberPanels.isEmpty() &&
				model.getTemporaryHighlightedShapes().isEmpty()) {
			    JOptionPane.showMessageDialog(
				    SwingUtilities.getWindowAncestor(TiedNumberPanel.this),
				    MiGenUtilities.getLocalisedMessage("NothingFoundToSelect"));
			}
		    }
		};
	    }
	    item.addActionListener(action);
	}
	return item;
    }

    protected void editName() {
	String name = tiedNumber.getName();
	nameEditField = new JTextField(name);
	nameEditField.setFont(nameFont());
	nameEditField.setColumns(Math.max(8, name.length()));
	KeyListener listenForEnter = new KeyListener() {

	    @Override
	    public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_ENTER) {
		    editCompleted();
		}
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
	    }

	    @Override
	    public void keyTyped(KeyEvent e) {
	    }
	    
	};
	nameEditField.addKeyListener(listenForEnter);
	addNameFocusListener(nameEditField);
	nameFieldPanel.remove(nameLabel);
	nameFieldPanel.add(nameEditField);
	nameEditField.setSize(nameEditField.getPreferredSize());
	nameFieldPanel.setSize(nameFieldPanel.getPreferredSize());
	nameEditField.requestFocus();
	nameEditField.setSelectionStart(0);
	nameEditField.setSelectionEnd(name.length());
//	nameFieldPanel.setComponentZOrder(nameEditField, 0);
	setSize(getPreferredSize());
	updateSizeOfAncestorExpressionPanels();
    }

    protected void updateSizeOfAncestorExpressionPanels() {
	Container parent = getParent();
	if (parent instanceof ExpressionPanel<?>) {
	    ((ExpressionPanel<?>) parent).updateSizeOfAncestorExpressionPanels();
	}
    }
    
    protected void editCompleted() {
	if (!editingName()) {
	    return;
	}
	String newName = nameEditField.getText();
	if (nameLabel.getText().equals(newName)) {
	    return; // no change
	}
	nameFieldPanel.remove(nameEditField);
	nameFieldPanel.add(nameLabel);
	nameEditField = null;
	tiedNumber.setName(newName);
	if (newName.equals(NAME_IF_NO_NAME_GIVEN)) {
//	    createDialog("NoNameGiven");
	    tiedNumber.setDisplayMode(TiedNumberExpression.DISPLAY_ONLY_VALUE);
	    tiedNumber.setNamed(false);
	} else {
	    nameLabel.setText(tiedNumber.getName()); // + authorInfo());
	    setSize(getPreferredSize());
	    ExpresserLauncher.autoSaveCurrentModel();
	    TiedNumberExpression<?> otherWithName = isNameInUse(tiedNumber);
	    if (otherWithName != null) {
		if (isNameInUse(tiedNumber) != null) {
		    createDialog("NameInUse");
		    editName();
		} else {
		    tiedNumber.setDisplayAuthorInfo(true);
		    otherWithName.setDisplayAuthorInfo(true);
		}
	    } else if (newName.length() == 0 ||
		       !Character.isLetter(newName.charAt(0))) {
		createDialog("NameImproper");
		editName();
	    }
	}
	updateSizeOfAncestorExpressionPanels();
    }
    
    public TiedNumberExpression<?> isNameInUse(TiedNumberExpression<?> other) {
	ObjectSetCanvas canvas = getCanvas();
	if (canvas == null) {
	    return null;
	}
	return canvas.isNameInUse(other);
    }

    public boolean editingName() {
	return nameEditField != null;
    }

    protected boolean okToEditValue() {
	return !tiedNumber.isReadOnly() && 
	       (!readOnly || MiGenConfiguration.isAllowEditingOfSlaveUnlockedNumbers());
    }

    protected ExpresserModel getModel() {
	Container ancestor = getParent();
	while (ancestor != null) {
	    if (ancestor instanceof ExpresserModelPanel) {
		return ((ExpresserModelPanel) ancestor).getModel();
	    } else if (ancestor instanceof DocumentCanvas) {
		return ((DocumentCanvas) ancestor).getModel();
	    }
	    ancestor = ancestor.getParent();
	}
	return null;
    }
    
    public TiedNumberPanel<V> createCopy() {	
	return createTiedNumberPanelCopy();
    }

    public TiedNumberPanel<V> createTiedNumberPanelCopy() {
	// if readOnly not specified use its value for this
	return createTiedNumberPanelCopy(this.readOnly);
    }
    
    public TiedNumberPanel<V> createTiedNumberPanelCopy(boolean readOnly) {
	// number itself is shared among all copies
	// that is the "magic" of tied numbers
	return new TiedNumberPanel<V>(tiedNumber.createCopy(), readOnly);
    }

    private void addNameFieldUpdateListener() {
	nameFieldUpdateListener =
	    new UpdateListener<TiedNumberExpression<Number>>() {

	    @Override
	    public void objectUpdated(
		    UpdateEvent<TiedNumberExpression<Number>> e) {
		processNameChange();
	    }
	};
	tiedNumber.addNameFieldUpdateListener(nameFieldUpdateListener);
    }

    private void addNameFocusListener(final JTextField editField) {
	if (editField == null) {
	    // see issue 544 -- probably unspecified -- e.g. a ? value
	    // could also be vertical layout
	    return; 
	}
	FocusListener focusListener = new FocusListener() {

	    @Override
	    public void focusGained(FocusEvent e) {
	    }

	    @Override
	    public void focusLost(FocusEvent e) {
		Color color = editField.getForeground();
		if (color.equals(NAME_IN_USE_COLOR)) {
		    createDialog("NameInUse");
		} else if (color.equals(NAME_IMPROPER_COLOR)) {
		    createDialog("NameImproper");
		} else if (color.equals(NAME_NOT_GIVEN_COLOR)) {
		    createDialog("NoNameGiven");
		}
		// see Issue 1012
		editCompleted();
	    }

	};
	editField.addFocusListener(focusListener);
    }

    public void createDialog(String messageName) {
	String message = MiGenUtilities.getLocalisedMessage(messageName);
	Stencil stencil = MiGenUtilities.getStencil(this);
	ModalPaperNoteCallOut intervention = new ModalPaperNoteCallOut(stencil, message);
	intervention.showMessage();
    }

    protected void addDisplayModeUpdateListener() {
	displayModeUpdateListener =
	    new UpdateListener<TiedNumberExpression<Number>>() {

	    @Override
	    public void objectUpdated(
		    UpdateEvent<TiedNumberExpression<Number>> e) {
		displayModeUpdated();
	    }
	};
	tiedNumber.addDisplayModeUpdateListener(displayModeUpdateListener);
    }

    protected void addTiedNumberValueUpdateListener() {
	tiedNumber.addUpdateListener(tiedNumberUpdateListener);
    }

    // private void removeTiedNumberValueUpdateListener() {
    // tiedNumber.removeUpdateListener(tiedNumberUpdateListener);
    // }

    protected MasterSlaveUniverseMicroworldPanel getMasterSlaveUniverseMicroworldPanel() {
	Container parent = getParent();
	while (parent != null
		&& !(parent instanceof MasterSlaveUniverseMicroworldPanel)) {
	    parent = parent.getParent();
	}
	return (MasterSlaveUniverseMicroworldPanel) parent;
    }

    public TiedNumberExpression<Number> getTiedNumberExpression() {
	return tiedNumber;
    }

    public String getTiedNumberName() {
	// better to get the name from the tiedNumber??
	return nameLabel.getText();
    }

    public void setPlayButtonEnabled(boolean enabled) {
	playButton.setEnabled(enabled);
    }

    protected void displayModeUpdated() {
	if (editingName()) {
	    return;
	}
	int displayMode = tiedNumber.getDisplayMode();
	updateBackgroundAppearance();
	Dimension valueDimension = valueTextFieldPanel.getPreferredSize();
	valueTextField.setEditable(!tiedNumber.isLocked() && okToEditValue());
	updateValueTextFieldBackground();
	switch (displayMode) {
	case TiedNumberExpression.DISPLAY_ONLY_VALUE:
	    add(valueTextFieldPanel);
	    remove(nameFieldPanel);
	    setSize(valueDimension.width + HORIZONTAL_GAP,
		    valueDimension.height + VERTICAL_GAP);
	    setBorderAndFontForValueOnly();
	    invalidate();
	    break;
	case TiedNumberExpression.DISPLAY_ONLY_NAME:
	    remove(valueTextFieldPanel);
	    add(nameFieldPanel, BorderLayout.NORTH);
	    nameFieldPanel.setSize(nameFieldPanel.getPreferredSize());
	    setSize(getPreferredSize());
	    break;
	case TiedNumberExpression.DISPLAY_NAME_AND_VALUE:
	    setBorderAndFontForNameAndValue(); 
	    add(nameFieldPanel, BorderLayout.NORTH);
	    nameFieldPanel.setSize(nameFieldPanel.getPreferredSize());
	    add(valueTextFieldPanel, BorderLayout.SOUTH);
	    valueTextFieldPanel.setSize(valueTextFieldPanel.getPreferredSize());
	    setSize(getPreferredSize());
	    break;
	}
	updateSize();
	Container parent = getParent();
	if (parent != null) {
	    Container grandparent = parent.getParent();
	    if (grandparent != null && grandparent instanceof AttributeManifest) {
		Container canvas = grandparent.getParent();
		if (canvas != null && canvas instanceof ObjectSetCanvas) {
		    ((ObjectSetCanvas) canvas).setDirtyManifests(true);
		}
	    }
	}
	updateSizeOfAncestorExpressionPanels();
    }

    protected boolean isUntiedNumberPanel() {
	// Overridden by UntiedNumberPanel
	return false;
    }

    public void updateBackgroundAppearance() {
	Color currentBackground = getBackground();
	Color newBackground;
	if (getTiedNumberExpression().isLocked()) {
	    newBackground = Color.LIGHT_GRAY;
	} else {
	    newBackground = Color.PINK;
	}
	if (!currentBackground.equals(newBackground)) {
	    changeBackgroundColor(newBackground);
	}
	setBorder(defaultBorder());
    }
    
//    public void displayDropFeedback(boolean on, boolean topLevel) {
//	if (on) {
//	    String dropFeedbackColor = MiGenConfiguration.getDropFeedbackColor();
//	    try {
//		changeBackgroundColor(Color.decode(dropFeedbackColor));
//	    } catch (NumberFormatException e) {
//		MiGenUtilities.printError("Unable to parse the color " + dropFeedbackColor);
//		e.printStackTrace();
//	    }
//	} else {
//	    updateBackgroundAppearance();
//	}
//    }

    public void changeBackgroundColor(Color newBackgroundColor) {
	if (editingName()) {
	    return;
	}
	valueTextFieldPanel.setBackground(newBackgroundColor);
	// what would have changed it?
	// TODO: determine if this does anything
	nameFieldPanel.setBackground(NAME_BACKGROUND_COLOR_VERTICAL);
	nameFieldPanel.setBorder(defaultBorder());
	setBackground(newBackgroundColor);
    }

    // following needed to support drag and drop:

    private void enableDragAndDrop() {
	final TransferHandler transferHandler = new TransferHandler() {

	    @Override
	    public boolean canImport(JComponent comp, DataFlavor[] drageeDataFlavors) {
		if (TiedNumberPanel.this.canImport()) {
		    for (int i = 0; i < drageeDataFlavors.length; i++) {
			if (drageeDataFlavors[i].equals(tiedNumberPanelFlavor)) {
			    return true;
			}
		    }
		}
		return false;
	    }

	    @Override
	    public boolean importData(JComponent comp, Transferable t) {
		return TiedNumberPanel.this.canImport();
	    }

	    @Override
	    public int getSourceActions(JComponent c) {
		return transferMode;
	    }

	    @Override
	    public Transferable createTransferable(JComponent c) {
		try {
		    return TiedNumberPanel.this.getTransferData(tiedNumberPanelFlavor);
		} catch (Exception e) {
		    e.printStackTrace();
		    return null;
		}
	    }
	};
	// initialise transfer handler so the data (i.e. the tied number) can be
	// transfered to the drop object
	setTransferHandler(transferHandler);
	// set up listener to drag events
	dragSource = DragSource.getDefaultDragSource();
	dragGestureRecognizer =
	    dragSource.createDefaultDragGestureRecognizer(this, transferMode, this);
	// following needed to see tied number while being dragged
	dragSourceMotionListener =
	    new DisplayDragSourceDragSourceMotionListener(tiedNumberPanelFlavor);
	dragSource.addDragSourceMotionListener(dragSourceMotionListener);
    }

    protected boolean canImport() {
	return true;
    }
    
    private void disableDragAndDrop() {
	setTransferHandler(null);
	if (dragGestureRecognizer != null) {
	    dragGestureRecognizer.removeDragGestureListener(this);
	    dragGestureRecognizer = null;
	}
    }

    protected Transferable createTransferable() {
	try {
	    return getTransferData(tiedNumberPanelFlavor);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    static public DataFlavor createTiedNumberPanelDataFlavor() {
	try {
	    return new DataFlavor(
		    DataFlavor.javaJVMLocalObjectMimeType
			    + ";class=uk.ac.lkl.migen.system.expresser.ui.TiedNumberPanel");
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    @Override
    public TiedNumberPanel<?> getTransferData(DataFlavor flavor)
	    throws UnsupportedFlavorException, IOException {
	return this;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
	return dataFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
	return flavor.equals(tiedNumberPanelFlavor);
    }

    @Override
    public void dragGestureRecognized(final DragGestureEvent event) {
	if (ExpressionPanel.isDragStarted()) {
	    return;
	}
	ExpressionPanel.setDragStarted(true);
	ObjectSetCanvas canvas = findCanvas();
	dragee = createDragee();
	dragee.setPanelStartingDrag(this);
	dragee.setDragEnabled(false); // until dropped
	ExpresserLauncher.add(dragee);
	Point location = getLocation();
	location = SwingUtilities.convertPoint(getParent(), location, SwingUtilities.getWindowAncestor(this));
	dragee.setLocation(location);
	if (getId() != null && dragee.getId() != null && canvas != null && canvas.getId() != null) {
	    // see Issue 1379
	    UIEventManager.processEvent(new TiedNumberDragStartEvent(getId().toString(), canvas.getId().toString(), dragee.getId().toString()));
	}
//	dragee.logMessage("Drag started");
	try {
	    event.startDrag(DragSource.DefaultCopyDrop, dragee.createTransferable(), dragee);
//	} catch (InvalidDnDOperationException e) {
//	    System.out.println("Ignoring drag error.");
//	    e.printStackTrace();
	} catch (Exception e) {
	    System.out.println("Ignoring drag error.");
	    e.printStackTrace();
	    ExpressionPanel.setDragStarted(false);
	}
	if (!dragCreatesCopy() && canvas != null) { 
	    // added copy; so remove the original to behave like a move
	    // postpone this until clear if cross-canvas drag 
	    // which copies or within-canvas drag which just moves
	    dragee.setPanelStartingDrag(this);
//	    canvas.remove(this);
	}
    }

    @SuppressWarnings("unchecked")
    protected ExpressionPanel<Number> createDragee() {
	Container parent = getParent();
	if (parent == null || !(parent instanceof ExpressionPanel)) {
	    return new ExpressionPanel<Number>(getTiedNumberExpression().createCopy());
	} else {
	    // tied number panels are typically inside an expression panel and that is what
	    // should be dragged not the tied number panel
	    // See Issue 1415.
	    return ((ExpressionPanel<Number>) parent).createCopy();
	}
    }
    
    public static boolean isMacOS() {
	String osName = System.getProperty("os.name").toLowerCase();
	return osName.startsWith("mac os x");
    }

    private ObjectSetCanvas findCanvas() {
	// a tied number can be on a canvas directly
	// or in a container that "knows" the canvas
	// Ideally the dragging should happen on a layer above the entire panel
	// but attempts to do some had multiple problems including
	// the tied number panel looking like just a pink box.
	Container ancestor = getParent();
	while (ancestor != null) {
	    if (ancestor instanceof ObjectSetCanvas) {
		return (ObjectSetCanvas) ancestor;
	    } else if (ancestor instanceof GlobalColorAllocationPanel) {
		GlobalColorAllocationPanel globalColorAllocationPanel =
			(GlobalColorAllocationPanel) ancestor;
		return globalColorAllocationPanel.getCanvas();
	    } else if (ancestor instanceof MasterSlaveUniverseMicroworldPanel) {
		MasterSlaveUniverseMicroworldPanel masterSlaveUniverseMicroworldPanel =
			(MasterSlaveUniverseMicroworldPanel) ancestor;
		return masterSlaveUniverseMicroworldPanel.getMasterPanel().getCanvas();
	    }
	    ancestor = ancestor.getParent();
	}
	return null;
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
//	logMessage("Drag ended");
	 ExpressionPanel.setDragStarted(false);
	// if it was not dropped on something willing to receive it
	// e.g. the slave panel
	ExpresserLauncher.remove(this); 
	// if left on the canvas should not create a copy when dragged
//	boolean onACanvas = onACanvas();
//	setDragCreatesCopy(!onACanvas);
//	if (onACanvas) {
//	    setDragEnabled(true);
//	}
//	Container topLevelAncestor = getTopLevelAncestor();
//	if (topLevelAncestor != null) {
//	    // see Issue 582
//	    topLevelAncestor.repaint();
//	}
	ExpresserLauncher.setCurrentPanelDirty();
    }

    protected ObjectSetCanvas getCanvas() {
	Container ancestor = getParent();
	while (ancestor != null) {
	    if (ancestor instanceof ObjectSetCanvas) {
		return (ObjectSetCanvas) ancestor;
	    }
	    ancestor = ancestor.getParent();
	}
	return null;
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public static DataFlavor getFlavor() {
	return tiedNumberPanelFlavor;
    }

    public void setDragCreatesCopy(boolean flag) {
	if (flag) {
	    transferMode = TransferHandler.COPY;
	} else {
	    transferMode = TransferHandler.MOVE;
	}
    }

    public boolean dragCreatesCopy() {
	return transferMode == TransferHandler.COPY;
    }

    public void setDragEnabled(boolean dragEnabled) {
	this.dragEnabled = dragEnabled;
	if (dragEnabled) {
	    enableDragAndDrop();
	} else {
	    disableDragAndDrop();
	}
    }

    public boolean isDragEnabled() {
	return dragEnabled;
    }

    public void setDropEnabled(boolean dropEnabled) {
	this.dropEnabled = dropEnabled;
	if (dropEnabled) {
	    DropTargetListener dropTargetListener = new DropTargetListener() {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
		    Transferable transferable = dtde.getTransferable();
		    if (transferable.isDataFlavorSupported(TiedNumberPanel.getFlavor()) || 
			transferable.isDataFlavorSupported(ExpressionPanel.getFlavor())) {
			// expression panel is generating drop feedback
			// this caused Issue 902
//			displayDropFeedback(true, true);
			dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		    }
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
//		    TiedNumberPanel.this.displayDropFeedback(false, true);
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
		}

		@Override
		@SuppressWarnings("unchecked")
		// this warning is a serious problem if we ever had tied numbers
		// of more than one kind of Value
		// since the drag doesn't preserve the generic information -- no
		// problem if we stick with IntegerValue
		public void drop(DropTargetDropEvent dtde) {
		    ExpressionPanel.setDragStarted(false);
		    TiedNumberPanel.this.updateBackgroundAppearance();
		    Transferable transferable = dtde.getTransferable();
		    try {
			Component transferData =
			    (Component) transferable.getTransferData(TiedNumberPanel.getFlavor());
			double dropX = dtde.getLocation().getX();
			boolean droppedOnLeftHalf =
			    dropX < TiedNumberPanel.this.getWidth() / 2;
			ExpressionPanel.acquireFocusForAncestorAttributeManifest(
				TiedNumberPanel.this);
			if (transferData instanceof TiedNumberPanelProxy) {
			    TiedNumberPanelProxy<Number> tiedNumberPanelProxy =
				    (TiedNumberPanelProxy<Number>) transferData;
			    TiedNumberExpression<Number> droppedExpression =
				tiedNumberPanelProxy.getTiedNumberExpression().getOriginal();
			    handleDropOfExpressionOnNumber(
				    droppedOnLeftHalf, droppedExpression, tiedNumberPanelProxy);
			} else if (transferData instanceof TiedNumberPanel) {
			    TiedNumberPanel<Number> tiedNumberPanel =
				    (TiedNumberPanel<Number>) transferData;
			    if (!tiedNumberPanel.isReadOnly() || 
				TiedNumberPanel.this.isPartOfGlobalColorAllocationPanel()) {
				tiedNumberPanel.setDragCreatesCopy(true);
				TiedNumberExpression<Number> droppedExpression =
				    tiedNumberPanel.getTiedNumberExpression();
				handleDropOfExpressionOnNumber(
					droppedOnLeftHalf, droppedExpression,
					tiedNumberPanel);
			    }
			} else if (transferData instanceof ExpressionPanel) {
			    ExpressionPanel<Number> expressionDropTarget =
				    (ExpressionPanel<Number>) transferData;
			    if (!expressionDropTarget.isReadOnly() || 
				TiedNumberPanel.this.isPartOfGlobalColorAllocationPanel()) {
				expressionDropTarget.setDragCreatesCopy(true);
				Expression<Number> droppedExpression =
				    expressionDropTarget.getExpression();
				handleDropOfExpressionOnNumber(
					droppedOnLeftHalf, droppedExpression,
					expressionDropTarget);
			    }
			}
			dtde.dropComplete(true);
		    } catch (Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		    }
		    ExpresserLauncher.repaint();
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

	    };
	    addDropTargetListener(dropTargetListener);
	} else {
	    setDropTarget(null);
	}
    }

    public void addDropTargetListener(DropTargetListener dropTargetListener) {
	new DropTarget(this, dropTargetListener);
	DropTargetListener forwardingListener =
	    forwardToParentDropTargetListener(dropTargetListener);
	new DropTarget(valueTextField, forwardingListener);
	new DropTarget(getNameComponent(), forwardingListener);
    }
    
    public static DropTargetListener forwardToParentDropTargetListener(
	    final DropTargetListener parentDropTargetListener) {
	return new DropTargetListener() {

	    @Override
	    public void dragEnter(DropTargetDragEvent dtde) {
		parentDropTargetListener.dragEnter(dtde);
	    }

	    @Override
	    public void dragExit(DropTargetEvent dte) {
		parentDropTargetListener.dragExit(dte);
	    }

	    @Override
	    public void dragOver(DropTargetDragEvent dtde) {
		parentDropTargetListener.dragOver(dtde);
	    }

	    @Override
	    public void drop(DropTargetDropEvent dtde) {
		parentDropTargetListener.drop(dtde);
	    }

	    @Override
	    public void dropActionChanged(DropTargetDragEvent dtde) {
		parentDropTargetListener.dropActionChanged(dtde);
	    }

	};
    }
 
    protected void handleDropOfExpressionOnNumber(
	    final boolean droppedOnLeftHalf,
	    final Expression<Number> droppedExpression,
	    final JComponent droppedPanel) {
	// I believe this is now obsolete -- see Issue 653
	UIEventManager.processEvent(new TiedNumberMenuPopupEvent(getIdentityForLogMessage(), "Drop"));
	JPopupMenu menu = new JPopupMenu();
	JMenuItem item;
	final String replaceLabel =
	    MiGenUtilities.getLocalisedMessage("ReplaceTiedNumberLabel");
	item = new JMenuItem(replaceLabel);
	item.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		UIEventManager.processEvent(
			new TiedNumberMenuItemEvent(getIdentityForLogMessage(), replaceLabel));
//		logActionPerformed(replaceLabel);
		TiedNumberExpression<Number> tiedNumber = getTiedNumberExpression();
		tiedNumber.setValue(droppedExpression.evaluate());
		ExpressionPanel.removeComponent(droppedPanel);
	    }

	});
	menu.add(item);
	final String addLabel =
	    MiGenUtilities.getLocalisedMessage("AddToTiedNumberLabel");
	item = new JMenuItem(addLabel);
	item.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		UIEventManager.processEvent(
			new TiedNumberMenuItemEvent(getIdentityForLogMessage(), 
				                    addLabel + " " + (droppedOnLeftHalf ? "(left)" : "(right)")));
//		logActionPerformed(addLabel + " "
//			           + (droppedOnLeftHalf ? "(left)" : "(right)"));
		replaceWithOperation(droppedOnLeftHalf, droppedExpression,
			             new NumberAdditionOperator(), droppedPanel);
		// IntegerValue value =
		// tiedNumberPanel.getTiedNumberExpression().getValue();
		// IntegerValue newValue =
		// getTiedNumberExpression().getValue().add(value);
		// getTiedNumberExpression().setValue(newValue);
		// ExpressionEditorDropTarget.removeDroppedComponent(
		// tiedNumberPanel);
	    }

	});
	menu.add(item);
	final String subtractLabel =
	    droppedOnLeftHalf ?
	    MiGenUtilities.getLocalisedMessage("SubtractFromTiedNumberLabelLeft") :
		MiGenUtilities.getLocalisedMessage("SubtractFromTiedNumberLabelRight");
	item = new JMenuItem(subtractLabel);
	item.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		UIEventManager.processEvent(
			new TiedNumberMenuItemEvent(getIdentityForLogMessage(), 
				                    subtractLabel + " " + (droppedOnLeftHalf ? "(left)" : "(right)")));
//		logActionPerformed(subtractLabel + " "
//			+ (droppedOnLeftHalf ? "(left)" : "(right)"));
		replaceWithOperation(droppedOnLeftHalf, droppedExpression,
			new NumberSubtractionOperator(), droppedPanel);
		// TiedNumberExpression<IntegerValue> otherTiedNumber =
		// tiedNumberPanel.getTiedNumberExpression();
		// IntegerValue myValue = getTiedNumberExpression().getValue();
		// IntegerValue otherValue = otherTiedNumber.getValue();
		// IntegerValue newValue;
		// if (droppedOnLeftHalf) {
		// newValue = otherValue.subtract(myValue);
		// } else {
		// newValue = myValue.subtract(otherValue);
		// }
		// getTiedNumberExpression().setValue(newValue);
		// ExpressionEditorDropTarget.removeDroppedComponent(
		// tiedNumberPanel);
	    }

	});
	menu.add(item);
	final String multiplyLabel =
	    MiGenUtilities.getLocalisedMessage("MultiplyTiedNumberLabel");
	item = new JMenuItem(multiplyLabel);
	item.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		UIEventManager.processEvent(
			new TiedNumberMenuItemEvent(getIdentityForLogMessage(), 
				                    multiplyLabel + " " + (droppedOnLeftHalf ? "(left)" : "(right)")));
//		logActionPerformed(multiplyLabel + " "
//			+ (droppedOnLeftHalf ? "(left)" : "(right)"));
		replaceWithOperation(droppedOnLeftHalf, droppedExpression,
			new NumberMultiplicationOperator(), droppedPanel);
		// IntegerValue value =
		// droppedTiedNumberPanel.getTiedNumberExpression().getValue();
		// IntegerValue newValue =
		// getTiedNumberExpression().getValue().multiply(value);
		// getTiedNumberExpression().setValue(newValue);
		// ExpressionEditorDropTarget.removeDroppedComponent(
		// droppedTiedNumberPanel);
	    }

	});
	menu.add(item);
	menu.show(this, 0, getHeight());
    }

    private void replaceWithOperation(boolean droppedOnLeftHalf,
	    Expression<Number> droppedExpression,
	    Operator<Number, Number> operator,
	    JComponent droppedPanel) {
	ExpressionList<Number> operands;
	if (droppedOnLeftHalf) {
	    operands =
		new ExpressionList<Number>(droppedExpression,
			                         getTiedNumberExpression());
	} else {
	    operands =
		new ExpressionList<Number>(getTiedNumberExpression(),
			                         droppedExpression);
	}
	ModifiableOperation<Number, Number> operation =
	    new ModifiableOperation<Number, Number>(operator, operands);
	setReplacement(operation);
	Container parent = getParent();
	Point location = this.getLocation();
	ExpressionPanel<Number> expressionPanel =
	    new ExpressionPanel<Number>(operation);
	expressionPanel.setDragCreatesCopy(false);
	expressionPanel.setLocation(location);
//	if (parent instanceof ObjectSetCanvas) {
//	    ObjectSetCanvas canvas = (ObjectSetCanvas) parent;
//	    ExpresserModel model = canvas.getModel();
//	    model.addLocatedExpression(expressionPanel.getLocatedExpression());
//	    model.removeLocatedExpression(getLocatedExpression());
//	}
	parent.add(expressionPanel);
	parent.remove(this);
	ExpressionPanel.removeComponent(droppedPanel);
	parent.repaint();
    }

    public boolean isDropEnabled() {
	return dropEnabled;
    }

    public Expression<Number> getReplacement() {
	return replacement;
    }

    public void setReplacement(Expression<Number> replacement) {
	this.replacement = replacement;
	fireObjectUpdated();
    }

    public void fireObjectUpdated() {
	updateSupport.fireObjectUpdated();
    }

    public void addUpdateListener(UpdateListener<TiedNumberPanel<V>> listener) {
	updateSupport.addListener(listener);
    }

    public void removeUpdateListener(UpdateListener<TiedNumberPanel<V>> listener) {
	updateSupport.removeListener(listener);
    }

    public boolean canAcceptEditedValue() {
	try {
	    String text = valueTextField.getText();
	    Integer.parseInt(text);
	    return true;
	} catch (NumberFormatException event) {
	    return false;
	}
    }

    public boolean acceptEditedValue() {
	try {
	    String text = valueTextField.getText();
	    int value = Integer.parseInt(text);
	    getTiedNumberExpression().setValue(new Number(value));
	    valueTextField.setForeground(Color.BLACK);
	    if (!readOnly) {
		updateSize();
	    }
	    return true;
	} catch (NumberFormatException event) {
	    // just leave things alone since might be typing a negative number
	    // or the like
	    valueTextField.setForeground(Color.RED);
	    return false;
	}
    }

    private void processNameChange() {
	String newName = tiedNumber.getName();
	if (nameLabel != null) {
	    nameLabel.setText(newName);
	}
	if (nameFieldPanel == null) {
	    return; // can happen with unspecified tied number panels
	}
	if (newName.equals(NAME_IF_NO_NAME_GIVEN)) {
	    if (!nameFieldPanel.getForeground().equals(NAME_NOT_GIVEN_COLOR)) {
		UIEventManager.processEvent(
			new TiedNumberColorEvent(getIdentityForLogMessage(),  "Name color changed to 'name not given color'"));
//		logMessage("Name color changed to 'name not given color'");
		nameFieldPanel.setForeground(NAME_NOT_GIVEN_COLOR);
	    }
//	    nameFieldPanel.requestFocus();
//	    nameFieldPanel.setSelectionStart(0);
//	    nameFieldPanel.setSelectionEnd(4);
	} else if (newName.length() > 0
		   && Character.isLetter(newName.charAt(0))) {
	    ObjectSetCanvas canvas = getCanvas();
	    if (canvas != null && canvas.isNameInUse(tiedNumber) != null) {
		if (!nameFieldPanel.getForeground().equals(NAME_IN_USE_COLOR)) {
			UIEventManager.processEvent(
				new TiedNumberColorEvent(getIdentityForLogMessage(), "Name color changed to 'name in use color'"));
//		    logMessage("Name color changed to 'name in use color'");
		    nameFieldPanel.setForeground(NAME_IN_USE_COLOR);
		}
	    } else {
		Color nameOkColor = nameOkColor();
		if (!nameFieldPanel.getForeground().equals(nameOkColor)) {
		    UIEventManager.processEvent(
			    new TiedNumberColorEvent(getIdentityForLogMessage(), "Name color changed to 'name OK color'"));
//		    logMessage("Name color changed to 'name OK color'");
		    nameFieldPanel.setForeground(nameOkColor);
		}
	    }
	} else {
	    if (!nameFieldPanel.getForeground().equals(NAME_IMPROPER_COLOR)) {
		UIEventManager.processEvent(
			new TiedNumberColorEvent(getIdentityForLogMessage(), "Name color changed to 'improper color'"));
//		logMessage("Name color changed to 'improper color'");
		nameFieldPanel.setForeground(NAME_IMPROPER_COLOR);
	    }
	}
//	nameFieldPanel.setText(newName);
	if (!readOnly) {
	    updateSize();
	}
	ExpresserLauncher.autoSaveCurrentModel();
    }

    protected Color nameOkColor() {
	return NAME_OK_COLOR_VERTICAL;
    }
    
    protected Font nameFont() {
	return getNameFontVertical();
    }
    
    protected Font getNameFontVertical() {
	if (NAME_FONT_VERTICAL == null) {
	    NAME_FONT_VERTICAL = 
		new Font(MiGenConfiguration.getNumberNameFontFamily(),
			 MiGenConfiguration.isNumberNameFontBold() ? Font.BOLD: Font.PLAIN,
			 MiGenConfiguration.getNumberNameFontSize());
	}
	return NAME_FONT_VERTICAL;
    }
    
    protected Font getValueFont() {
	if (VALUE_FONT == null) {
	    VALUE_FONT = 
		new Font(MiGenConfiguration.getNumberValueFontFamily(),
			 MiGenConfiguration.isNumberValueFontBold() ? Font.BOLD: Font.PLAIN,
			 MiGenConfiguration.getNumberValueFontSize());
	}
	return VALUE_FONT;
    }
    
    protected Border defaultBorder() {
	return DEFAULT_BORDER_VERTICAL;
    }

    public void updateSize() {
	// following ensures that the size of the panel and its components
	// will be maintained to the same values that fresh copies have
	Runnable delayedUpdateSize = new Runnable() {

	    @Override
	    public void run() {
		Component nameComponent = getNameComponent();
		Dimension namePreferredSize = nameComponent.getPreferredSize();
		nameComponent.setSize(namePreferredSize);
		Dimension valuePreferredSize = valueTextField.getPreferredSize();
		valueTextField.setSize(valuePreferredSize);
		valueTextFieldPanel.setSize(valueTextFieldPanel.getPreferredSize());
		setSize(getPreferredSize());
		updateSizeOfAncestorExpressionPanels();
		invalidate();
		ExpresserLauncher.repaint();
	    }
	    
	};
	SwingUtilities.invokeLater(delayedUpdateSize);
    }

    public boolean isReadOnly() {
	return readOnly;
    }

    protected boolean isPartOfGlobalColorAllocationPanel() {
	Container parent = getParent();
	Container ancestor = parent;
	while (ancestor != null) {
	    if (ancestor instanceof GlobalColorAllocationPanel) {
		return true;
	    }
	    ancestor = ancestor.getParent();
	}
	return false;
    }

    public Point getSlaveLocation() {
	if (slaveLocation == null) {
	    return getLocation();
	} else {
	    return slaveLocation;
	}
    }

    public void setSlaveLocation(Point slaveLocation) {
	this.slaveLocation = slaveLocation;
    }

//    protected void logMenuPoppedUp(String kind) {
//	logAction(kind + " menu popped up");
//    }

//    void logActionPerformed(String menuLabel) {
//	logAction("Clicked '" + menuLabel + "'");
//    }

    public String getIdentityForLogMessage() {
	if (containedInMasterPanel == null) {
	    ExpresserModelPanel selectedModelPanel = 
		ExpresserLauncher.getModelCopyTabbedPanel().getSelectedModelPanel();
	    if (selectedModelPanel instanceof MasterSlaveUniverseMicroworldPanel) {
		BlockShapeCanvasPanel masterPanel = 
		    ((MasterSlaveUniverseMicroworldPanel) selectedModelPanel).getMasterPanel();
		containedInMasterPanel = masterPanel.isAncestorOf(this);
	    } else {
		containedInMasterPanel = false;
	    }
	}
	if (containedInMasterPanel) {
	    return logIdentity();
	}
	return getId().toString();
    }

    @Override
    public void removeNotify() {
	super.removeNotify();
	if (ExpresserLauncher.isRemovalFinal(getParent())) {
//	    removeListeners();
	    disableDragAndDrop();
	}
    }

    public void removeListeners() {
	if (dragSource != null) {
	    dragSource.removeDragSourceMotionListener(dragSourceMotionListener);
	}
	tiedNumber.removeNameFieldUpdateListener(nameFieldUpdateListener);
	tiedNumber.removeUpdateListener(tiedNumberUpdateListener);
	tiedNumber.removeDisplayModeUpdateListener(displayModeUpdateListener);
	tiedNumber.removeLockStatusUpdateListener(lockStatusUpdateListener);
    }

    public String logIdentity() {
	return " (" + getId() + " TiedNumber" + tiedNumber.getIdString()
		+ " value: " + tiedNumber.toString() + " name: "
		+ tiedNumber.getName() + ")";
    }

    @Override
    public ID getId() {
	return id;
    }

    public JTextField getValueTextField() {
	return valueTextField;
    }

    public ExpressionPanel<Number> getDragee() {
        return dragee;
    }
    
    public Component getNameComponent() {
	return nameLabel;
    }

    public TiedNumberPanel<V> getNumberPanelStartingDrag() {
        return numberPanelStartingDrag;
    }

    public void setNumberPanelStartingDrag(TiedNumberPanel<V> numberPanelStartingDrag) {
        this.numberPanelStartingDrag = numberPanelStartingDrag;
    }
    
    public boolean isOnACanvas() {
	Container parent = getParent();
	return parent != null && 
	       parent instanceof ExpressionPanel<?> && 
	       ((ExpressionPanel<?>) parent).isOnACanvas();
    }
    
    public LocatedExpression<Number> getLocatedExpression() {
	return new LocatedExpression<Number>(tiedNumber, getX(), getY());
//	if (locatedExpression == null) {
//	    locatedExpression = new LocatedExpression<IntegerValue>(tiedNumber, getX(), getY());
//	}
//	return locatedExpression;
    }

    public static String hexFormat(Color color) {
        return "#" + Integer.toHexString(color.getRGB() & 0x00ffffff);
    }
    
//    @Override
//    public void setLocation(int x, int y) {
//	super.setLocation(x, y);
//	getLocatedExpression().setX(x);
//	getLocatedExpression().setY(y);
//    }

}

