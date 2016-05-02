package fr.istic.evc.presentation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.picking.behaviors.PickRotateBehavior;
import com.sun.j3d.utils.picking.behaviors.PickTranslateBehavior;
import com.sun.j3d.utils.picking.behaviors.PickZoomBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;

import fr.istic.evc.controller.CFactory;
import fr.istic.evc.controller.CVirtualReality;

public class PVirtualReality extends JFrame {
	
	private static final long serialVersionUID = -7459376830120915468L;
	
	private static final String FRAME_TITLE = "EVC - VirtualReality";

	private CVirtualReality controller;
	
	private SimpleUniverse universe;
	
	private Canvas3D canvas3d;
	
	// Buttons
	private JButton rotateXPositiveBtn;
	private JButton rotateXNegativeBtn;
	private JButton rotateYPositiveBtn;
	private JButton rotateYNegativeBtn;
	private JButton rotateZPositiveBtn;
	private JButton rotateZNegativeBtn;
	private JButton moveForwardBtn;
	private JButton moveBackwardBtn;
	private JButton moveUpBtn;
	private JButton moveDownBtn;
	private JButton moveLeftBtn;
	private JButton moveRightBtn;
	
	private JTextField absPosX;
	private JTextField absPosY;
	private JTextField absPosZ;

	private JTextField absRotX;
	private JTextField absRotY;
	private JTextField absRotZ;
	
	private JList listObjects;
	private HashMap<String, PVirtualObject> objects;
	private PVirtualObject[] selectedObjects;

	public PVirtualReality(CVirtualReality ctrl) {
		super(FRAME_TITLE);
		
		this.controller = ctrl;
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1024, 768);
		setLocationRelativeTo(null);
		
		this.initLayout();
		this.initListeners();
	}
	
	/**
	 * Initialize the graphic components
	 */
	private void initLayout() {
		GraphicsConfiguration config = SimpleUniverse
				.getPreferredConfiguration();
		
		canvas3d = new Canvas3D(config);
		
		BranchGroup scene = createSceneGraph();
		
		this.universe = new SimpleUniverse(canvas3d);
		this.universe.getViewingPlatform().setNominalViewingTransform();
		this.universe.addBranchGraph(scene);

		JPanel leftPanel = new JPanel(new GridLayout(0, 1));
		moveForwardBtn = new JButton("Move Forward");
		moveBackwardBtn = new JButton("Move Backward");
		moveUpBtn = new JButton("Move Up");
		moveDownBtn = new JButton("Move Down");
		moveLeftBtn = new JButton("Move Left");
		moveRightBtn = new JButton("Move Right");
		leftPanel.add(moveForwardBtn);
		leftPanel.add(moveBackwardBtn);
		leftPanel.add(moveUpBtn);
		leftPanel.add(moveDownBtn);
		leftPanel.add(moveLeftBtn);
		leftPanel.add(moveRightBtn);
		
		JPanel absPosPanel = new JPanel(new FlowLayout());
		this.absPosX = new JTextField();
		this.absPosY = new JTextField();
		this.absPosZ = new JTextField();
		this.absPosX.setToolTipText("X");
		this.absPosY.setToolTipText("Y");
		this.absPosZ.setToolTipText("Z");
		this.absPosX.setColumns(3);
		this.absPosY.setColumns(3);
		this.absPosZ.setColumns(3);
		
		JButton absPosBtn = new JButton("Go");
		absPosBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.p2cMoveAbsolute(Float.valueOf(absPosX.getText()), 
										   Float.valueOf(absPosY.getText()), 
										   Float.valueOf(absPosZ.getText()));
			}
		});
		
		absPosX.setText("0");
		absPosY.setText("0");
		absPosZ.setText("0");
		
		absPosPanel.add(absPosX);
		absPosPanel.add(absPosY);
		absPosPanel.add(absPosZ);
		absPosPanel.add(absPosBtn);
		
		JPanel absRotPanel = new JPanel(new FlowLayout());
		this.absRotX = new JTextField();
		this.absRotY = new JTextField();
		this.absRotZ = new JTextField();
		this.absRotX.setToolTipText("X");
		this.absRotY.setToolTipText("Y");
		this.absRotZ.setToolTipText("Z");
		this.absRotX.setColumns(3);
		this.absRotY.setColumns(3);
		this.absRotZ.setColumns(3);

		JButton absRotBtn = new JButton("Go");
		absRotBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.p2cRotateAbsolute(Float.valueOf(absRotX.getText()), 
											 Float.valueOf(absRotY.getText()), 
											 Float.valueOf(absRotZ.getText()));
			}
		});
		
		absRotX.setText("0");
		absRotY.setText("0");
		absRotZ.setText("0");
		
		absRotPanel.add(absRotX);
		absRotPanel.add(absRotY);
		absRotPanel.add(absRotZ);
		absRotPanel.add(absRotBtn);
		
		JPanel absPanel = new JPanel(new GridLayout(2, 1));
		absPanel.add(absPosPanel);
		absPanel.add(absRotPanel);
		
		leftPanel.add(absPanel);
		
		JPanel bottomPanel = new JPanel(new GridLayout(4, 1));
		rotateXNegativeBtn = new JButton("Rotate Upward");
		rotateXPositiveBtn = new JButton("Rotate Downward");
		rotateYNegativeBtn = new JButton("Rotate Right");
		rotateYPositiveBtn = new JButton("Rotate Left");
		rotateZNegativeBtn = new JButton("Roll Right");
		rotateZPositiveBtn = new JButton("Roll Left");
		bottomPanel.add(rotateXPositiveBtn);
		bottomPanel.add(rotateXNegativeBtn);
		bottomPanel.add(rotateYPositiveBtn);
		bottomPanel.add(rotateYNegativeBtn);
		bottomPanel.add(rotateZPositiveBtn);
		bottomPanel.add(rotateZNegativeBtn);

		JPanel rightPanel = new JPanel();
		listObjects = new JList(objects.keySet().toArray());
		listObjects.setSelectionModel(new DefaultListSelectionModel() {
		    private static final long serialVersionUID = 1L;

		    boolean gestureStarted = false;

		    @Override
		    public void setSelectionInterval(int index0, int index1) {
		        if(!gestureStarted){
		            if (isSelectedIndex(index0)) {
		                super.removeSelectionInterval(index0, index1);
		            } else {
		                super.addSelectionInterval(index0, index1);
		            }
		        }
		        gestureStarted = true;
		    }

		    @Override
		    public void setValueIsAdjusting(boolean isAdjusting) {
		        if (isAdjusting == false) {
		            gestureStarted = false;
		        }
		    }

		});
		listObjects.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					selectedObjects = (PVirtualObject[]) listObjects.getSelectedValues();
				}
			}
		});
		rightPanel.add(listObjects);
		
		this.setLayout(new BorderLayout());
		this.add(leftPanel, BorderLayout.WEST);
		this.add(canvas3d, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
		this.add(rightPanel, BorderLayout.EAST);
		
	}
	
	/**
	 * Initialize the listeners
	 */
	private void initListeners() {
		moveForwardBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cMove(0, 0, -0.1f);
			}
		});
		moveBackwardBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cMove(0, 0, 0.1f);
			}
		});
		moveUpBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cMove(0, 0.1f, 0);
			}
		});
		moveDownBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cMove(0, -0.1f, 0);
			}
		});
		moveLeftBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cMove(-0.1f, 0, 0);
			}
		});
		moveRightBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cMove(0.1f, 0, 0);
			}
		});

		rotateXPositiveBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cRotate(0.1f, 0, 0);
			}
		});
		rotateXNegativeBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cRotate(-0.1f, 0, 0);
			}
		});
		rotateYPositiveBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cRotate(0, 0.1f, 0);
			}
		});
		rotateYNegativeBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cRotate(0, -0.1f, 0);
			}
		});
		rotateZPositiveBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cRotate(0, 0, 0.1f);
			}
		});
		rotateZNegativeBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.p2cRotate(0, 0, -0.1f);
			}
		});
	}
	
	/**
	 * Create the 3D elements
	 * @return the branch group created
	 */
	public BranchGroup createSceneGraph() {
		BranchGroup objRoot = new BranchGroup();
		this.objects = new HashMap<String, PVirtualObject>();
		
		Appearance a = new Appearance();
		a.setColoringAttributes(new ColoringAttributes(0.2f, 0.1f, 0.1f, ColoringAttributes.FASTEST));
		
		// Cube 1
		PVirtualObject cube = CFactory.newVirtualCube(0.2f, a).getPresentation();
		cube.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		cube.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
		objRoot.addChild(cube);
		objects.put("Cube rouge", cube);
		
		a = new Appearance();
		a.setColoringAttributes(new ColoringAttributes(0.1f, 0.2f, 0.1f, ColoringAttributes.FASTEST));
		
		// Sphere 1
		PVirtualObject sphere1 = CFactory.newVirtualSphere(0.1f, a).getPresentation();
		sphere1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		sphere1.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
		objRoot.addChild(sphere1);
		objects.put("Sphere verte", sphere1);
		
		a = new Appearance();
		a.setColoringAttributes(new ColoringAttributes(0.1f, 0.1f, 0.2f, ColoringAttributes.FASTEST));
		
		// Cube 2
		PVirtualObject cube2 = CFactory.newVirtualCube(0.15f, a).getPresentation();
		cube2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		cube2.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
		objRoot.addChild(cube2);
		objects.put("Cube bleu", cube2);
		
		
		// Behaviour
		objRoot.addChild(new PickRotateBehavior(objRoot, canvas3d, 
				new BoundingSphere(new Point3d(0, 0, 0), 0.2)));
		objRoot.addChild(new PickTranslateBehavior(objRoot, canvas3d, 
				new BoundingSphere(new Point3d(0, 0, 0), 0.2)));
		objRoot.addChild(new PickZoomBehavior(objRoot, canvas3d, 
				new BoundingSphere(new Point3d(0, 0, 0), 0.2)));
		
		// Compile
		objRoot.compile();
		
		return objRoot;
	}
	
	/**
	 * Move relatively to the current point of view
	 * @param dx
	 * @param dy
	 * @param dz
	 */
	public void c2pMoveRelative(float dx, float dy, float dz) {
		TransformGroup vpTrans = universe.getViewingPlatform().getViewPlatformTransform();
		
		Transform3D oldT3D = new Transform3D();
		vpTrans.getTransform(oldT3D);

		Vector3d translate = new Vector3d();
		translate.set(dx, dy, dz);
		
		Transform3D localT3D = new Transform3D();
		localT3D.setTranslation(translate);
		
		Transform3D newT3D = new Transform3D();
		newT3D.mul(oldT3D, localT3D);
		
		vpTrans.setTransform(newT3D);
	}
	
	/**
	 * Move on an absolute position
	 * @param dx
	 * @param dy
	 * @param dz
	 */
	public void c2pMoveAbsolute(float dx, float dy, float dz) {
		TransformGroup vpTrans = universe.getViewingPlatform().getViewPlatformTransform();
		
		Vector3d translate = new Vector3d();
		translate.set(dx, dy, dz);
		
		Transform3D t3d = new Transform3D();
		vpTrans.getTransform(t3d);
		t3d.setTranslation(translate);
		vpTrans.setTransform(t3d);
	}
	
	/**
	 * Orientate relatively to the current point of view
	 * @param h
	 * @param p
	 * @param r
	 */
	public void c2pOrientRelative(float h, float p, float r) {
		TransformGroup vpTrans = universe.getViewingPlatform().getViewPlatformTransform();
		
		Transform3D oldT3D = new Transform3D();
		vpTrans.getTransform(oldT3D);
		
		Vector3d rotate = new Vector3d();
		rotate.set(h, p, r);
		
		Transform3D localT3D = new Transform3D();
		localT3D.setEuler(rotate);
		
		Transform3D newT3D = new Transform3D();
		newT3D.mul(oldT3D, localT3D);
		
		vpTrans.setTransform(newT3D);
	}
	
	/**
	 * Orientate on an absolute angle
	 * @param h
	 * @param p
	 * @param r
	 */
	public void c2pOrientAbsolute(float h, float p, float r) {
		TransformGroup vpTrans = universe.getViewingPlatform().getViewPlatformTransform();
		
		Vector3d rotate = new Vector3d();
		rotate.set(h, p, r);
		
		Transform3D t3d = new Transform3D();
		vpTrans.getTransform(t3d);
		
		Vector3d translate = new Vector3d();
		t3d.get(translate);
		t3d.setEuler(rotate);
		t3d.setTranslation(translate);
		
		vpTrans.setTransform(t3d);
	}
}

