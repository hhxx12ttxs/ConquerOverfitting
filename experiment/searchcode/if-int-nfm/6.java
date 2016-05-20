package umlMain;

import diagram.InterfaceDiagram;
import diagram.ExternalDependency;
import diagram.PMath;
import diagram.UMLInterface;
import guiParts.AssociationAddDialog;
import guiParts.DeleteDialog;
import guiParts.NewUMLClassDialog;
import guiParts.ErrorDialog;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import mouse.MouseState;
import mouse.MouseState.CursorState;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UMLClass extends JPanel implements Serializable{

	//Trying something, storing inheritance lines
	//TODO
	public ArrayList<Line2D.Double> lines = new ArrayList<Line2D.Double>();

	private Point nextLollyPos = new Point(1, 0);

	//A handle for this class
	private UMLClass handle = this;

	//A boolean to tell if a dependency is currently being drawn
	private boolean drawingDep = false;
	private DependencyModel dm = null;


	//stuff for draggable ablility
	private boolean draggable = true;

	protected Point anchorPoint;

	protected Cursor draggingCursor = 
			Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

	protected boolean overbearing = false;

	// Size of the class
	private static final int classBorderWidth = 16;
	private static final int classBorderHeight = 0;

	//count of number of classes
	private static int classCount = 0;

	//unique id number set by constructor
	private int classId;


	protected UMLClassManager classMgr;
	private UMLClass InParent;
	private List<ExternalDependency> umlExtDeps;
	// For interfaces that are drawn as lollipops
	private List<UMLInterface> umlInterfaces;
	// For interfaces that are drawn as classes
	private List<UMLClass> umlClassInterfaces;
	//A collection of dependencies and associations that use this class. To be checked when class is deleted
	private ArrayList<AssociationModel> assModList = new ArrayList<AssociationModel>();
	private ArrayList<DependencyModel> depModList = new ArrayList<DependencyModel>();

	private String className;
	private Boolean isClassAbstract;
	private JLabel nameLabel;
	private String stereoName;
	private JLabel stereoLabel;

	private JPopupMenu popupMenu;
	private JMenuItem editRenameClass;
	private JMenuItem editDeleteClass;
	private JMenuItem editAddInterface;
	private JMenuItem editAddAssociation;

	//Co-ordinates of the top left corner of the class
	private Point position;
	private boolean dragged = false;
	private int coox;
	private int cooy;
	private String strID;

	private boolean associationfromtb = false;
	private boolean associationFromRightClickEdit = false;

	//Stereotypes labels
	ArrayList<JLabel> stereoTypeLabels = new ArrayList<JLabel>();

	/**
	 * Construct a UMLClass which is represented by the draggable box in
	 * the user interface.
	 * @param mgr
	 */
	public UMLClass(UMLClassManager mgr, String stereo){
		classId = classCount;
		classCount++;

		classMgr = mgr;
		className = "";
		stereoName = stereo;
		isClassAbstract = false;

		position = new Point(0, 0);
		InParent = null;
		umlInterfaces = new LinkedList<UMLInterface>();
		umlExtDeps = new LinkedList<ExternalDependency>();

		setBorder(BorderFactory.createLineBorder(Color.black));

		addClassFields();

		init();
	}

	/*
	 * Copy constructor for UMLClass.
	 */
	public UMLClass(UMLClass umlClass){
		classId = umlClass.getClassId();

		classMgr = umlClass.classMgr;
		className = umlClass.getClassName();
		stereoName = umlClass.stereoName;
		isClassAbstract = umlClass.isClassAbstract;
		position = umlClass.position;
		InParent = umlClass.InParent;
		umlInterfaces = new LinkedList<UMLInterface>(umlClass.umlInterfaces);
		umlExtDeps = new LinkedList<ExternalDependency>(umlClass.umlExtDeps);

		setBorder(BorderFactory.createLineBorder(Color.BLACK));

		addClassFields();
		init();
	}

	@Override
	public  String toString()
	{
		return className;
	}

	/*
	 * Returns true if the class is an interface, otherwise returns false.
	 */
	public boolean isAnInterface(){
		if(stereoName.contains("Interface") ||
				stereoName.contains("interface")){
			return true;
		}
		return false;
	}

	/**
	 * Initialise pop up menu and action listeners
	 */
	private void init(){
		popupMenu = new JPopupMenu();
		for (MouseListener m : getMouseListeners()) {
			removeMouseListener(m);
		}
		addMouseListener(new PopupTriggerListener());

		editRenameClass = new JMenuItem("Edit Class");
		editDeleteClass = new JMenuItem("Delete Class");
		editAddInterface = new JMenuItem("Add Interface");
		editAddAssociation = new JMenuItem("Add Association");


		final UMLClass classHandle = this;

		editRenameClass.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				renameClass();
				validate();
			}
		});

		// Add action listener for the delete class pop up option
		editDeleteClass.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				DeleteDialog dia = new DeleteDialog(classMgr, classHandle);
				dia.setVisible(true);;
				classMgr.setgreatestXY();
				classMgr.repaint();
			}
		});

		editAddInterface.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				interfaces();
				classMgr.repaint();
			}
		});

		//fran added this dog, yo yo dog
		editAddAssociation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				associations();
				classMgr.repaint();
			}
		});


		// Add rename and delete options to the pop up menu
		popupMenu.add(editRenameClass);
		popupMenu.add(editDeleteClass);
		popupMenu.add(editAddInterface);
		popupMenu.add(editAddAssociation);
		addDragListeners();
	}

	/**
	 * adds the class fields such as classname.
	 */

	private void addClassFields(){

		removeAll();
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.CENTER;

		String [] stereotypes = stereoName.split(",");


		// Started motivated, now were here
		for(String std: stereotypes){
			stereoLabel = new JLabel("<<"+ std + ">>");
			stereoLabel.setBorder(BorderFactory.createEmptyBorder((classBorderHeight),
					classBorderWidth, classBorderHeight, classBorderWidth));
			stereoLabel.setOpaque(true);
			stereoLabel.setFont(new Font("Calibri", Font.PLAIN,14));
			stereoTypeLabels.add(stereoLabel);
			//setStereoType(stereoName);

			if (!(std.equals(""))) {
				this.add(stereoLabel, gbc);
				gbc.gridy += 1;
			}
		}
		nameLabel = new JLabel();
		nameLabel.setText(this.className);
		nameLabel.setBorder(BorderFactory.createEmptyBorder(classBorderHeight,
				classBorderWidth, classBorderHeight, classBorderWidth));
		nameLabel.setOpaque(true);
		this.add(nameLabel, gbc);

	}

	public void addStereotype(){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.CENTER;

		stereoLabel = new JLabel();
		stereoLabel.setBorder(BorderFactory.createEmptyBorder(classBorderHeight,
				classBorderWidth, classBorderHeight, classBorderWidth));
		stereoLabel.setOpaque(true);
		setStereoType(stereoName);

		if (!(stereoName.equals(""))) {
			this.add(stereoLabel, gbc);
		}
	}

	public void addInterface(UMLInterface itf) {
		umlInterfaces.add(itf);
		itf.attachToClass(this);
	}

	public void addExtDep(ExternalDependency ExD) {
		umlExtDeps.add(ExD);
	}

	public List<UMLInterface> getInterfaces() {
		return umlInterfaces;
	}

	public List<ExternalDependency> getExtDeps() {
		return umlExtDeps;
	}

	//sets the interface list to the input list
	public void setInterfaces(List<UMLInterface> Inlist){
		umlInterfaces = Inlist;
		for(UMLInterface itf : umlInterfaces){
			itf.attachToClass(this);
		}
	}


	public void setExtDeps(List<ExternalDependency> Inlist){
		umlExtDeps = Inlist;
	}

	public void redrawInterfaces(Point moveDiff){
		for (UMLInterface itf : umlInterfaces){
			itf.setPosition(PMath.add(moveDiff, itf.getPosition()));
			itf.getViewInterface().validate();
		}
		classMgr.validate();
	}

	//deletes an interface link from the class (NOTE: doesn't actually delete the interface from the display)
	public void deleteInterface(UMLInterface Inter){
		// WTF?! this function gets passed the interface to be deleted
		// why would we need to look through the interface list looking for
		// an interface we already have?
		//		UMLInterface toremove = null;
		//		for (UMLInterface umlint: umlInterfaces){
		//			if (umlint.getInterfaceName().equalsIgnoreCase(Inter.getInterfaceName())){
		//				toremove = Inter;
		//			}
		//		}
		umlInterfaces.remove(Inter);
		// Now we do delete from class manager
		classMgr.removeInterfaceViewsById(
				Inter.getViewInterface().getInterfaceID());
	}

	public void deleteExtDep(ExternalDependency Inter){
		ExternalDependency toremove = null;
		for (ExternalDependency umlint: umlExtDeps){
			if (umlint.getExDFullName().equalsIgnoreCase(Inter.getExDFullName())){
				toremove = Inter;
			}
		}
		umlExtDeps.remove(toremove);
	}

	public String getInterfacesAsString(){
		String result = "";
		for (UMLInterface itf : umlInterfaces){
			result += itf.getInterfaceName() + ", ";
		}
		return result;
	}
	public String getExtDepsAsString(){
		String result = "";
		for (ExternalDependency itf : umlExtDeps){
			result += itf.getExDName() + ", ";
		}
		return result;
	}

	public void addInterfacesToMgr() {
		for (UMLInterface itf : umlInterfaces){
			itf.attachToClass(this);
			itf.setPosition(PMath.add(nextLollyPos, position));

			nextLollyPos = getNextLollyPos();

			InterfaceDiagram itfView = new InterfaceDiagram(itf,this);
			classMgr.add(itfView, new Integer(0), 0);
			itfView.setup();
			itfView.validate();
		}
		classMgr.validate();
	}

	private Point getNextLollyPos() {
		Point result = new Point(0, 0);

		if (nextLollyPos.x == getBounds().x + getBounds().width){
			if (nextLollyPos.y == getBounds().y + getBounds().height){
				result = PMath.add(nextLollyPos, new Point(-60, 0));
			}
			else{
				result = PMath.add(nextLollyPos, new Point(0, 60));
			}
		}
		else{
			result = PMath.add(nextLollyPos, new Point(10, 0));
		}

		if (result.x > getBounds().x + getBounds().width){
			result.x = getBounds().x;
		}
		if (result.y > getBounds().y + getBounds().height){
			result.y = getBounds().y;
		}

		return result;
	}

	/*
	 * This is a mysterious and powerful black box. Its takes the mouse state so it can change it back to
	 * the normal cursor after the function has been called 
	 */

	private void blackBoxFunction(MouseState mouse) {
		//TODO: reset the drawing of dependencies if the user selects another
		//      tool such as inheritance, associations etc
		if (!classMgr.getDependencyManager().getDrawing()) {

			classMgr.getDependencyManager().setDrawing(true);
			dm = new DependencyModel(getHandle());

			classMgr.getDependencyManager().setLastUsed(dm);
			classMgr.getDependencyManager().addDependency(dm);
			//This is what updates that first bug shitty thing...
			classMgr.refresh();

		}

		else{
			dm = classMgr.getDependencyManager().getLastUsed();

			if (classMgr.getDependencyManager().checkExists(dm, getHandle())) {
				//Removes if it exists
				classMgr.getDependencyManager().removeDependency(dm);

			} 
			else if (dm.getBase().getClassName() == getHandle().getClassName()) {
				//This is the check for the class depending on itself - which it shouldnt, so its deleted
				classMgr.getDependencyManager().removeDependency(dm);
			}

			else {
				dm.setDependentClass(getHandle());

				//The next two lines notify the class so that when it is deleted it knows which dependencies it has.							
				UMLClass baseClass = dm.getBase();
				UMLClass dependentClass = getHandle();
				dm.setDependencyLine(classMgr);

				//Add this dependency to classes
				addDependency(dm);
				dm.getBase().addDependency(dm);
				classMgr.createUndoRedoState(); //TODO: test




			}
			mouse.setState(CursorState.NORMAL);
			// Changes it back to the normal cursor
			classMgr.getUmlGui().setCursor(mouse.getCursorImage());
			classMgr.getDependencyManager().setDrawing(false);
		}

	}
	private class PopupTriggerListener extends MouseAdapter {

		MouseState mouse = MouseState.getMouseStateInstance();
		private int xCoord, yCoord;

		@Override
		public void mousePressed(MouseEvent ev) {
			// This is to allow double click for editing a class
			if (ev.getClickCount() == 2 && !ev.isConsumed()) {
				//handle double click event.
				ev.consume();
				renameClass();

			}

			classMgr.getUmlGui().setSelectedclass(getClassName());
			classMgr.moveToFront(classMgr.getUmlGui().getSelectedClass());
			coox = ev.getX();
			cooy = ev.getY();


			if (ev.isPopupTrigger()&& mouse.getState().equals(CursorState.NORMAL)) {
				removeDragListeners();
				xCoord = ev.getX();
				yCoord = ev.getY();
				popupMenu.show(ev.getComponent(), xCoord, yCoord);
			}

			else {

				///////////////////////////////////////////////////////////////////////////////////
				////////This is where the magic happens ... be careful changing shit cos it usually
				//////// breaks more shit than you fix :(
				//////////////////////////////////////////////////////////////////////////////////

				if (mouse.getState().equals(CursorState.DRAW_DEP)||(mouse.getState().equals(CursorState.DRAW_DEP_ACTIVE))) { 
					mouse.setState(CursorState.DRAW_DEP_ACTIVE);
					classMgr.getUmlGui().setCursor(mouse.getCursorImage());
					blackBoxFunction(mouse);
				}

				else if (MouseState.getState().equals(CursorState.DELETE)) {
					//This is strictly the delete function for when a class is selected
					deleteThis();
					classMgr.getUmlGui().setSelectedClassToNull();
					mouse.setState(CursorState.NORMAL);
					classMgr.getUmlGui().setCursor(mouse.getCursorImage());
				}
				else if (MouseState.getState().equals(CursorState.DRAW_INHE)||MouseState.getState().equals(CursorState.DRAW_INHE_ACTIVE)) {
					mouse.setState(CursorState.DRAW_INHE_ACTIVE);

					classMgr.getUmlGui().setCursor(mouse.getCursorImage());
					if (classMgr.itemp.isEmpty()) {
						setFirst(); // set the temporary class for first
					}
					else {
						linkInheritance();
						mouse.setState(CursorState.NORMAL);
						classMgr.getUmlGui().setCursor(mouse.getCursorImage());
					}
				}
				else if (MouseState.getState().equals(CursorState.DRAW_ASSO)||MouseState.getState().equals(CursorState.DRAW_ASSO_ACTIVE)) {
					mouse.setState(CursorState.DRAW_ASSO_ACTIVE);
					classMgr.getUmlGui().setCursor(mouse.getCursorImage());

					if (classMgr.atemp.isEmpty()) {
						setFirst();
					}
					else {
						linkAssociation();
						// after action, set back to normal
						mouse.setState(CursorState.NORMAL);
						classMgr.getUmlGui().setCursor(mouse.getCursorImage());
					}

				}

				addDragListeners();
				ev.consume();
			}
			// Sets class that was clicked to selected class


		}

		@Override
		public void mouseReleased(MouseEvent ev) {
			if (ev.isPopupTrigger()) {
				xCoord = ev.getX();
				yCoord = ev.getY();

				popupMenu.show(ev.getComponent(), xCoord, yCoord);

			}
			if (dragged) {


				classMgr.getUmlGui().setChanges(true);
				classMgr.createUndoRedoState();
				classMgr.moveToFront(classMgr.getUmlGui().getSelectedClass());
				dragged = false;
				classMgr.validate();
			}
			else {
				//makeChanges(true); //dont think this is neccessary.
			}
			ev.consume();
		}
	}

	/**
	 * comes up with the delete dialog for when a class wants to be
	 * deleted via the toolbar item
	 */
	private void deleteThis() {

		DeleteDialog d = new DeleteDialog(classMgr, this);
		d.setVisible(true);
		classMgr.setgreatestXY();
		classMgr.repaint();
	}


	/**
	 * sets the class to its correct temporary ArrayList (inheritance or association)
	 * from the toolbar selection
	 */
	private void setFirst() {
		if (MouseState.getState().equals(CursorState.DRAW_INHE_ACTIVE)){
			classMgr.itemp.add(this);
		}
		else if (MouseState.getState().equals(CursorState.DRAW_ASSO_ACTIVE)) {
			classMgr.atemp.add(this);
		}
	}

	/**
	 * links the inheritance between 2 classes from toolbar selection
	 */
	private void linkInheritance() {
		UMLClass n = classMgr.itemp.get(0);
		if ((n == this) || (n.classIsChildOf(this))) {
			ErrorDialog e = new ErrorDialog("Invalid parent selected.");
			e.setVisible(true);
		}
		else {
			n.setInParent(this);
			classMgr.getUmlGui().setChanges(true);
			classMgr.createUndoRedoState();
			classMgr.itemp.clear();
			classMgr.refresh();
		}
	}
	/**
	 * links the association between 2 classes from toolbar selection
	 */
	private void linkAssociation() {
		associationfromtb = true;
		associations();
		classMgr.atemp.clear();
		associationfromtb = false;
	}

	public void makeChanges(boolean val){
		classMgr.getUmlGui().setChanges(val);
	}

	private void addDragListeners(){

		final UMLClass handle = this;
		addMouseMotionListener(new MouseAdapter(){

			@Override
			public void mouseMoved(MouseEvent e){
				//This occurs when the mouse enters a class and moves over it.
				anchorPoint = e.getPoint();
				if (getCursor().getType() == 13) {

					repaint();

				}
				else if(getCursor().getType() != 13) {
					// setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); //Commented out this line so I could get dependencies working all good - it creates a fucking annoying bug
				}

			}

			@Override
			public void mouseDragged(MouseEvent e){

				if (getCursor().getType() == 13)  {
					//Draw line as long as dragged

				}

				else {
					dragged = true;
					int anchorX = anchorPoint.x;

					int anchorY = anchorPoint.y;

					Point parentOnScreen = getParent().getLocationOnScreen();
					Point mouseOnScreen = e.getLocationOnScreen();
					Point position = new Point(mouseOnScreen.x - parentOnScreen.x -
							anchorX, mouseOnScreen.y - parentOnScreen.y - anchorY);
					setLocation(position);
					if (position.x < 0){
						position.x = 0;
					}
					if (position.y < 0){
						position.y = 0;
					}

					Point posDiff = PMath.minus(position,
							handle.getPosition());

					handle.setPosition(position);
					handle.redrawInterfaces(posDiff);

					handle.classMgr.getUmlGui().repaint();

					handle.classMgr.setgreatestXY();


					if (overbearing){
						getParent().setComponentZOrder(handle, 0);
						repaint();
					}
					// Add horizontal scroll bars if class is placed below 0
					if(getX() <= 0){

					}

				}
			}

		});

	}

	private void removeDragListeners(){
		for (MouseMotionListener listener : this.getMouseMotionListeners()){
			removeMouseMotionListener(listener);
		}
		//TODO: not sure how this affects the cursor
		// after action, set back to normal
		MouseState m = MouseState.getMouseStateInstance();
		m.setState(CursorState.NORMAL);
		setCursor(m.getCursorImage());
		//		setCursor(Cursor.getDefaultCursor());

	}

	@Override
	public void validate(){
		super.validate();
		setLocation(position);
		for (UMLInterface itf : umlInterfaces){
			if (itf.getViewInterface() != null) {
				itf.getViewInterface().validate();
			}
		}
	}

	/**
	 * Sets the position of the class for use in positioning the UMLClass
	 * in the panel
	 * @param x
	 * @param y
	 */
	public void setPosition(int x, int y){
		position = new Point(x, y);
	}

	/**
	 * Sets the position of the class for use in positioning the UMLClass
	 * in the panel
	 * @param p
	 */
	public void setPosition(Point p){
		position = p;
	}

	/**
	 * Gets the stored position of the class.
	 * @return      position
	 */
	public Point getPosition(){
		return position;
	}


	/**
	 * Gets whether the class is abstract or not.
	 * Returns true if class is abstract, false otherwise.
	 * @return      Boolean value.//	public void setStereoType(String Name){
//		if (!(Name.equals(""))) {
//			stereoName = Name;
//			stereoLabel.setFont(new Font("Calibri", Font.PLAIN,14));
//			stereoLabel.setText("<<" + stereoName + "\n>>");
//		}
//		else{
//			stereoName = "";
//			remove(stereoLabel);
//		}
//
//	}
	 */
	public Boolean getIsAbstract(){
		return isClassAbstract;
	}

	/**
	 * Sets the abstract class property.
	 * True to set class as abstract, false forrenameClass() normal class.
	 * @param abstr     Boolean value to set as abstract or not/.
	 */
	public void setIsAbstract(Boolean abstr){
		isClassAbstract = abstr;
	}

	//Getter and setter for parent property
	public UMLClass getInParent(){
		return InParent;
	}
	public void setInParent(UMLClass P){
		InParent = P;
	}

	/**
	 * Setter and getter for the UMLClass name.
	 */
	//gets classname
	public String getClassName(){
		return className;
	}

	//sets classname
	public void setClassName(String name){
		className = name;
		//
		if (this.getIsAbstract() == true){
			nameLabel.setFont(new Font("Calibri", Font.ITALIC,14));

		} else {
			nameLabel.setFont(new Font("Calibri", Font.BOLD,14));
		}

		nameLabel.setText(className);
	}
	//TODO update stereo types
	//sets stereotype
	public void setStereoType(String Name){
		if (!(Name.equals(""))) {
			stereoName = Name;
			stereoLabel.setFont(new Font("Calibri", Font.PLAIN,14));
		}
		else{
			stereoName = "";
			remove(stereoLabel);
		}
		addClassFields();
	}

	// gets stereotype
	public String getStereoType(){
		return stereoName;
	}

	/**
	 * Gets the class ID, unique for each instance of UMLClass.
	 * @return classId
	 */
	public int getClassId(){
		return classId;
	}

	/**
	 * sets the class ID
	 * @param i
	 */
	public void setClassId(int i) {
		this.classId = i;
	}

	public String getStrId() {
		return strID;
	}

	public void setStrId(String s) {
		strID = s;
	}

	/**
	 * Refreshes the class, automatically called by UMLClassManager after
	 * a state has been loaded. This sets up the action listeners again which
	 * allows popup to continue working.
	 */
	public void refresh() {

		init();
		//              addClassFields();
		//              validate();

		for (UMLInterface itf : umlInterfaces){
			itf.resetView();
		}
	}


	// makes class highlightededitclass.getPosition()
	public void highlightLabel(){

		this.setBackground(Color.yellow);
		nameLabel.setBackground(Color.yellow);
		for(JLabel jl: stereoTypeLabels)
			jl.setBackground(Color.yellow);

	}

	// makes class unhighlighted
	public void unHighlightLabel(){
		this.setBackground(null);
		nameLabel.setBackground(null);
		for(JLabel jl: stereoTypeLabels)
			jl.setBackground(null);
	}

	// rename function
	public boolean renameClass(){
		NewUMLClassDialog dia = new NewUMLClassDialog(classMgr, this, 1,this.getLocation().x ,this.getLocation().y);
		dia.setVisible(true);
		if (dia.getChanges()) {
			classMgr.getUmlGui().setChanges(true);
		}
		//Dimension umlDimen = ClasssizebyLabel();
		//setBounds( getPosition().x, getPosition().y,  umlDimen.width, umlDimen.height);
		classMgr.validate();
		classMgr.setgreatestXY();
		classMgr.getUmlGui().getSelectedClass().highlightLabel();   
		return dia.getChanges();
	}

	// Start in interfaces tab
	public boolean interfaces(){
		NewUMLClassDialog dia = new NewUMLClassDialog(classMgr, this, 2,this.getLocation().x ,this.getLocation().y);
		dia.setVisible(true);
		if (dia.getChanges()) {
			classMgr.getUmlGui().setChanges(true);
		}
		//Dimension umlDimen = ClasssizebyLabel();
		//setBounds( getPosition().x, getPosition().y,  umlDimen.width, umlDimen.height);
		classMgr.validate();
		classMgr.setgreatestXY();
		classMgr.getUmlGui().getSelectedClass().highlightLabel();   
		return dia.getChanges();
	}

	// Start in association tab
	public boolean associations(){
		//NewUMLClassDialog dia;
		AssociationAddDialog dia = null;

		if (associationfromtb) {
			//dia = new NewUMLClassDialog(classMgr, classMgr.atemp.get(0), this, 3,this.getLocation().x ,this.getLocation().y);
			dia = new AssociationAddDialog(classMgr, classMgr.atemp.get(0), this);
		}
		else {
			//dia = new NewUMLClassDialog(classMgr, this, 3,this.getLocation().x ,this.getLocation().y);
			dia = new AssociationAddDialog(classMgr, this);
		}
		dia.setVisible(true);
		if (!dia.isCancel()) {
			classMgr.getUmlGui().setChanges(true);
		}
		//Dimension umlDimen = ClasssizebyLabel();
		//setBounds( getPosition().x, getPosition().y,  umlDimen.width, umlDimen.height);
		classMgr.validate();
		classMgr.setgreatestXY();
		classMgr.getUmlGui().getSelectedClass().highlightLabel();   
		return dia.getChanges();
	}

	//checks to see if input class is a child of the class calling this method. returns boolean
	public Boolean classIsChildOf(UMLClass PosChild){
		if (PosChild.getInParent() == this){
			return true;
		}
		else if (PosChild.getInParent() == null){
			return false;
		}
		else{
			return this.classIsChildOf(PosChild.getInParent());
		}
	}

	//Reads class name and stereotypes to find dimensions needed for class
	public Dimension ClasssizebyLabel(){
		Dimension Lengthheight = new Dimension();
		int Dheight = 10;
		int Dwidth = 50;
		FontMetrics NFM = nameLabel.getFontMetrics(nameLabel.getFont());
		FontMetrics SFM = stereoLabel.getFontMetrics(stereoLabel.getFont());
		int Nwidth = SwingUtilities.computeStringWidth(NFM, nameLabel.getText());
		int Swidth = SwingUtilities.computeStringWidth(SFM, stereoLabel.getText());
		int NHeight = NFM.getHeight();
		int SHeight = SFM.getHeight();

		if (Nwidth >= Swidth){
			Dwidth += Nwidth;
		}

		else if (Swidth > Nwidth){
			Dwidth += Swidth;
		}

		if (!(this.className.isEmpty())){
			Dheight += 18 + NHeight;
		}
		if (!(this.className.isEmpty())&&(!(this.stereoName.isEmpty()))){
			Dheight += 9 + NHeight + SHeight;
		}
		else if (!(this.stereoName.isEmpty())){
			Dheight += 34 + SHeight;
		}
		Lengthheight.height = Dheight;
		Lengthheight.width = Dwidth;
		return Lengthheight;
	}

	//gets class manager
	public UMLClassManager getClassManager(){
		return classMgr;
	}

	public void addDependency(DependencyModel dm) {
		//A dependency is added to this class
		depModList.add(dm);
	}

	public ArrayList<DependencyModel> getDependencies() {
		return depModList;
	}


	//Given one class this will return a list of all the associations with it
	public List<AssociationModel> displayAllAssociationsWithMe(){
		List<AssociationModel> listOfAssociationsBetween = new ArrayList<AssociationModel>();
		for (AssociationModel m : this.getClassManager().getAssociationManager().getAssociationList()){
			if (m.getAssociationStart() == this || m.getAssociationEnd() == this){
				// if this class is the start or end class of m
				listOfAssociationsBetween.add(m);
			}
		}
		return listOfAssociationsBetween;
	}

	//Given one class this will return a list of all the associations with it
	public List<AssociationModel> displayAllAssociationsWithMyself(){
		List<AssociationModel> listOfAssociationswithMyself = new ArrayList<AssociationModel>();
		for (AssociationModel m : this.getClassManager().getAssociationManager().getAssociationList()){
			if (m.getAssociationStart() == this && m.getAssociationEnd() == this){
				// if this class is the start or end class of m
				listOfAssociationswithMyself.add(m);
			}
		}
		return listOfAssociationswithMyself;
	}

	public JLabel getNameLabel(){
		return nameLabel;
	}

	public void setAbstract(boolean bool){
		isClassAbstract = bool;
	}

	public UMLClass getHandle() {
		return handle;
	}

	public void setHandle(UMLClass handle) {
		this.handle = handle;
	}

	public Line2D getLine() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addInheritanceLine(Line2D.Double line){
		this.lines.add(line);
	}


	public void clearLines(){
		//I know you want it, I know you want it
		lines.clear();
	}


}

