package cs567.particles;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.colorchooser.*;
import javax.swing.event.*;
import javax.vecmath.*;

import javax.media.opengl.*;
import com.sun.opengl.util.*;


/**
 * CS567: Assignment #1 "Particle Systems"
 * 
 * main() entry point class that initializes ParticleSystem, OpenGL
 * rendering, and GUI that manages GUI/mouse events.
 * 
 * Spacebar toggles simulation advance.
 * 
 * @author Doug James, January 2007
 */
public class ParticleSystemBuilder implements GLEventListener
{
    private ArrayList<Exporter>	exporters = new ArrayList<Exporter>();
	private boolean				do_export = false;
    
    private static int N_STEPS_PER_FRAME = 10;

    /** Default graphics time step size. */
    public static final double DT = 0.01;

    /** Main window frame. */
    JFrame frame = null;

    private int width, height;

    /** The single ParticleSystem reference. */
    ParticleSystem PS;

	 /** Object that handles all GUI and user interactions of building
     * Task objects, and simulation. */
    BuilderGUI     gui;

	// Frame-per-second
	private DurationTimer	timer = new DurationTimer();
	private boolean			firstRender = true;
	private int				frameCount;

	private int				WALL_DISPLAY_LIST1 = -1,
							WALL_DISPLAY_LIST2 = -1;

	/** Main constructor. Call start() to begin simulation. */
    ParticleSystemBuilder() throws Exception
    {
		PS = new ParticleSystem();
		exporters.add(new FrameExporter());
		exporters.add(new PovExporter(PS, this));
    }

    /**
     * Builds and shows windows/GUI, and starts simulator.
     */
    public void start()
    {
		if(frame != null) return;

		gui   = new BuilderGUI();/// MOVED HERE SINCE CALLED BY frame/animator

		frame = new JFrame("CS567 Particle System Builder");
		GLCanvas canvas = new GLCanvas();
		canvas.addGLEventListener(this);
		frame.add(canvas);

		final Animator animator = new Animator(canvas);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// Run this on another thread than the AWT event queue to
				// make sure the call to Animator.stop() completes before
				// exiting
				new Thread(new Runnable() {
					public void run() {
					animator.stop();
					System.exit(0);
					}
				}).start();
			}
			});

		frame.pack();
		frame.setSize(600,600);
		frame.setLocation(600, 0);
		frame.setVisible(true);
		animator.start();
    }



    private ViewportMap vp_map;

    /** Maps mouse event into computational cell using OrthoMap. */
    public Point3d getPoint3d(MouseEvent e) {
		return vp_map.getPoint3d(e);
    }

    /** GLEventListener implementation: Initializes JOGL renderer. */
    public void init(GLAutoDrawable drawable) 
    {
		// DEBUG PIPELINE (can use to provide GL error feedback... disable for speed)
		//drawable.setGL(new DebugGL(drawable.getGL()));

		GL	gl = drawable.getGL();
		System.err.println("INIT GL IS: " + gl.getClass().getName());

		gl.setSwapInterval(1);

		gl.glLineWidth(3);

		gl.glShadeModel(GL.GL_SMOOTH); // try setting this to GL_FLAT and see what happens.

		gl.glEnable(GL.GL_CULL_FACE);
		gl.glEnable(GL.GL_DEPTH_TEST);

		drawable.addMouseListener(gui);
		drawable.addMouseMotionListener(gui);

		drawable.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				gui.dispatchKey(e.getKeyChar(), e);
			}
			});

		// Wall display list
		int	displayListIndex = gl.glGenLists(1);
		gl.glNewList(displayListIndex, GL.GL_COMPILE);
		gl.glBegin(GL.GL_TRIANGLES);
		drawPlane(gl, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, -1, 0), new Vector3d(-1, 0, 0), 20);
		drawPlane(gl, new Point3d(-0.5, -0.5, -0.5), new Vector3d(0, 0, 1), new Vector3d(1, 0, 0), 20);
		drawPlane(gl, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, -1), new Vector3d(0, -1, 0), 20);
		gl.glEnd();
		gl.glEndList();
		System.out.println("MADE LIST "+displayListIndex+" : "+gl.glIsList(displayListIndex));
		WALL_DISPLAY_LIST1 = displayListIndex;

		displayListIndex = gl.glGenLists(1);
		gl.glNewList(displayListIndex, GL.GL_COMPILE);
		gl.glBegin(GL.GL_TRIANGLES);
		drawPlane(gl, new Point3d(-0.5, -0.5, -0.5), new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), 20);
		drawPlane(gl, new Point3d(-0.5, -0.5, -0.5), new Vector3d(0, 1, 0), new Vector3d(0, 0, 1), 20);
		drawPlane(gl, new Point3d(0.5, 0.5, 0.5), new Vector3d(-1, 0, 0), new Vector3d(0, 0, -1), 20);

		gl.glEnd();
		gl.glEndList();
		System.out.println("MADE LIST "+displayListIndex+" : "+gl.glIsList(displayListIndex));
		WALL_DISPLAY_LIST2 = displayListIndex;
    }

    /** GLEventListener implementation */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

    /** GLEventListener implementation */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) 
    {
	System.out.println("width="+width+", height="+height);
	height = Math.max(height, 1); // avoid height=0;
	
	this.width  = width;
	this.height = height;

 	GL gl = drawable.getGL();
	gl.glViewport(0,0,width,height);	

	/// SETUP ORTHOGRAPHIC PROJECTION AND MAPPING INTO UNIT CELL:
	gl.glMatrixMode(GL.GL_PROJECTION);	
	gl.glLoadIdentity();			
	vp_map = new OrthoMap(width, height);//Hide grungy details in OrthoMap
	//vp_map = new PerspectiveMap(width, height);
	vp_map.apply(gl);

	/// GET READY TO DRAW:
	gl.glMatrixMode(GL.GL_MODELVIEW);
	gl.glLoadIdentity();
    }

	private void drawPlane (GL gl, Point3d p, Vector3d v1, Vector3d v2, int number) {
		Vector3d	v11 = new Vector3d(),
					v12 = new Vector3d(),
					v21 = new Vector3d(),
					v22 = new Vector3d(),
					normal = new Vector3d();
		normal.cross(v1, v2);
		normal.normalize();
		for(int i=0; i<number; i++) {
			v11.scale((double)i/number, v1);
			v12.scale((double)(i+1)/number, v1);
			for(int j=0; j<number; j++) {
				v21.scale((double)j/number, v2);
				v22.scale((double)(j+1)/number, v2);

				gl.glNormal3d(normal.x, normal.y, normal.z);
				gl.glVertex3d(p.x+v11.x+v21.x, p.y+v11.y+v21.y, p.z+v11.z+v21.z);
				gl.glVertex3d(p.x+v12.x+v21.x, p.y+v12.y+v21.y, p.z+v12.z+v21.z);
				gl.glVertex3d(p.x+v12.x+v22.x, p.y+v12.y+v22.y, p.z+v12.z+v22.z);
				gl.glVertex3d(p.x+v11.x+v21.x, p.y+v11.y+v21.y, p.z+v11.z+v21.z);
				gl.glVertex3d(p.x+v12.x+v22.x, p.y+v12.y+v22.y, p.z+v12.z+v22.z);
				gl.glVertex3d(p.x+v11.x+v22.x, p.y+v11.y+v22.y, p.z+v11.z+v22.z);
			}
		}
	}

    /** 
     * Main event loop: OpenGL display + simulation
     * advance. GLEventListener implementation.
     */
    public void display(GLAutoDrawable drawable) 
    {
		// Calculate FPS
		if (!firstRender) {
			++frameCount;
			if (timer.getTime() > 2000) {
				timer.stop();
				System.err.println("Frames per second: " + (frameCount / timer.getDurationAsSeconds()) + ", Number of particles = " + PS.size());
				timer.reset();
				timer.start();
				frameCount = 0;
			}
		} else {
			firstRender = false;
			timer.start();
		}

		// Try to control FPS
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			throw new GLException(e);
		}

		GL gl = drawable.getGL();
		gl.glClearColor(0,0,0,0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// Show texts
		if(gui.simulate) {
			gl.glRasterPos2d(-0.7, 0.7);
			GLUT	glut = new GLUT();
			gl.glColor3f(1, 1, 1);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_10, "Simulating");
		}

		// Rotate camera
		gl.glPushMatrix();
		// This is a matrix stack, so the first matrix is multipled last
		gl.glRotated(-gui.getRotationZ(), 0.0, 0.0, 1.0);
		gl.glRotated(-gui.getRotationX(), 1.0, 0.0, 0.0);
		gl.glRotated(-gui.getRotationY(), 0.0, 1.0, 0.0);

		// Lights
		float	light_pos[] = { 0.2f, 0.25f, 0.3f, 1.0f },
				light_amb_color[] = { 0.0f, 0.0f, 0.0f, 1.0f },
				light_diffuse_color[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light_pos, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, light_amb_color, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, light_diffuse_color, 0);
        gl.glEnable(GL.GL_LIGHT0);

		/// DRAW COMPUTATIONAL CELL BOUNDARY:
		{
			double[][]	boundary_vertices = {
				{-0.5, 0.5, 0.5},
				{0.5, 0.5, 0.5},
				{-0.5, -0.5, 0.5},
				{0.5, -0.5, 0.5},
				{-0.5, -0.5, -0.5},
				{0.5, -0.5, -0.5},
				{-0.5, 0.5, -0.5},
				{0.5, 0.5, -0.5}
			};

			gl.glEnable(GL.GL_LIGHTING);
			float	gray[] = { 0.5f, 0.5f, 0.5f, 1.0f };
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, gray, 0);
			gl.glCallList(WALL_DISPLAY_LIST1);
			float	blue[] = { 0.45f, 0.45f, 0.6f, 1.0f };
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, blue, 0);
			gl.glCallList(WALL_DISPLAY_LIST2);
			gl.glDisable(GL.GL_LIGHTING);
		}

		/// SIMULATE/DISPLAY HERE (Handled by BuilderGUI):
		gui.simulateAndDisplayScene(gl);

		// Remember that every push needs a pop; this one is paired with
		// rotating the entire world
		gl.glPopMatrix();

		if(do_export)
			for(Exporter e : exporters)
				e.writeFrame();
    }

	public Matrix3d getInverseCameraRotationMatrix () {
		return gui.getInverseCameraRotationMatrix();
	}


    /** Interaction central: Handles windowing/mouse events, and building state. */
    class BuilderGUI implements MouseListener, MouseMotionListener//, KeyListener
    {
		boolean simulate = true;

		/** Current build task (or null) */
		Task task;

		JFrame  guiFrame, colorFrame;
		TaskSelector	taskSelector = new TaskSelector();
		ColorSelector	colorSelector = new ColorSelector();

		Color	current_color = null;

		// Viewpoint orientation
		private double	view_rotx = 0.0, view_roty = 0.0, view_rotz = 0.0;
		private int		prevMouseX, prevMouseY;
		private boolean	mouseRButtonDown = false;

		private ButtonGroup	radio_buttons = new ButtonGroup(),
							integrator_buttons = new ButtonGroup();

		public double getRotationX () { return view_rotx; }
		public double getRotationY () { return view_roty; }
		public double getRotationZ () { return view_rotz; }

		private void createButton (String name) {
			JButton button = new JButton (name);
			guiFrame.add(button);
			button.addActionListener(taskSelector);
		}

		private void createRadioButtons (String[] names, int default_id, ButtonGroup group) {
			for(int i=0; i<names.length; i++) {
				JToggleButton	button = new JRadioButton(names[i], (default_id == i));
				group.add(button);
				guiFrame.add(button);
				button.addActionListener(taskSelector);
			}
		}

		BuilderGUI()
		{
			guiFrame = new JFrame("Tasks");
			guiFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			//guiFrame.setLayout(new SpringLayout());
			guiFrame.setLayout(new GridLayout(20, 1));

			createButton("Reset");
			createButton("Clear");
			createButton("Add Fluid Force");

			// Action buttons
			String[] action_names = {"Create Particle", "Move Particle", "Create Spring",
				"Create Hair", "Pin Constraint", "Drag Particle", "Water Gun", "Cannon", "C-4", "Paint Brush"};
			createRadioButtons(action_names, 1, radio_buttons);

			// Configuration buttons
			createButton("Save Configuration");
			createButton("Load Configuration");

			// Integrator buttons
			String[] integrator_names = {"Forward Euler", "Symplectic Euler", "Midpoint", "Velocity Verlet"};
			createRadioButtons(integrator_names, 1, integrator_buttons);

			// Step size
			JSlider	number_of_steps = new JSlider(JSlider.HORIZONTAL, 1, 1000, N_STEPS_PER_FRAME);
			number_of_steps.setMajorTickSpacing(100);
			number_of_steps.setPaintTicks(true);
			number_of_steps.setPaintLabels(true);
			Hashtable	labelTable = new Hashtable();
			labelTable.put(new Integer(1), new JLabel("1") );
			labelTable.put(new Integer(500), new JLabel("500") );
			labelTable.put(new Integer(1000), new JLabel("1000") );
			number_of_steps.setLabelTable(labelTable);
			number_of_steps.addChangeListener(taskSelector);
			guiFrame.add(number_of_steps);

			guiFrame.setSize(200,200);
			guiFrame.pack();
			guiFrame.setVisible(true);

			colorFrame = new JFrame("Color");
			colorFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			colorFrame.setLayout(new BorderLayout());

			JColorChooser cc = new JColorChooser();
			cc.getSelectionModel().addChangeListener(colorSelector);
			current_color = cc.getColor();
			colorFrame.add(cc);
			colorFrame.setLocation(200, 0);
			colorFrame.pack();
			colorFrame.setVisible(true);

			task = new MoveParticleTask();
			//task = new EmitterTask(new WaterGun(new Point3d(0, 0, 0.5), new Point3d(0, 0, -1), new Vector3d(0, 0, -10), 5, PS));
			//task = new EmitterTask(new Cannon(new Point3d(0, 0, 0.5), new Point3d(0, 0, -1), new Vector3d(0, 0, -50), 50, PS));
			//task = new EmitterTask(new C4Explosive(new Point3d(0, 0, 0), 10, 1000, PS));
			//task = new EmitterTask(new PaintBrush(2, PS));
		}

		/** Simulate then display particle system and any builder
		 * adornments. */
		void simulateAndDisplayScene(GL gl)
		{
			/// TODO: OVERRIDE THIS INTEGRATOR (Doesn't use Force objects properly)
			if(simulate) {
			if(false) {//ONE EULER STEP
				PS.advanceTime(DT);
			}
			else {//MULTIPLE STEPS FOR STABILITY WITH FORWARD EULER (UGH!)
				int nSteps = N_STEPS_PER_FRAME;
				double dt  = DT/(double)nSteps;
				for(int k=0; k<nSteps; k++) {
				PS.advanceTime(dt);///
				}
			}

			/// TODO: PROCESS COLLISIONS HERE:

			}

			// Draw particles, springs, etc.
			PS.display(gl);

			// Display Task, e.g., currently drawn spring.
			if(task != null) task.display(gl);
		}

		private boolean toggleSimulate (boolean do_simulate) {
			boolean	prev = simulate;
			simulate = do_simulate;
			if(!simulate)
				PS.stop();
			return prev;
		}

		public Matrix3d getCameraRotationMatrix () {
			// Compute the rotation matrix (which transforms world coord. into eye coord.)
			Matrix3d	rotX = new Matrix3d(),
						rotY = new Matrix3d(),
						rotZ = new Matrix3d(),
						rot_mat = new Matrix3d();
			rotX.rotX(-gui.getRotationX() * Math.PI / 180);
			rotY.rotY(-gui.getRotationY() * Math.PI / 180);
			rotZ.rotZ(-gui.getRotationZ() * Math.PI / 180);
			rot_mat.setIdentity();
			rot_mat.mul(rotZ);
			rot_mat.mul(rotX);
			rot_mat.mul(rotY);

			return rot_mat;
		}

		public Matrix3d getInverseCameraRotationMatrix () {
			// Compute the inverse rotation matrix (which transforms eye coord. into world coord.)
			Matrix3d	rotX = new Matrix3d(),
						rotY = new Matrix3d(),
						rotZ = new Matrix3d(),
						rot_mat = new Matrix3d();
			rotX.rotX(gui.getRotationX() * Math.PI / 180);
			rotY.rotY(gui.getRotationY() * Math.PI / 180);
			rotZ.rotZ(gui.getRotationZ() * Math.PI / 180);
			rot_mat.setIdentity();
			rot_mat.mul(rotY);
			rot_mat.mul(rotX);
			rot_mat.mul(rotZ);

			return rot_mat;
		}

		class ColorSelector implements ChangeListener {

			public void stateChanged (ChangeEvent e) {
				ColorSelectionModel csm = (ColorSelectionModel)e.getSource();
				current_color = csm.getSelectedColor();
			}
		}

		/**
		 * ActionListener implementation to manage Task selection
		 * using (radio) buttons.
		 */
		class TaskSelector implements ActionListener, ChangeListener
		{
			/**
			 * Resets ParticleSystem to undeformed/material state,
			 * disables the simulation, and removes the active Task.
			 */
			void resetToRest() {
				PS.reset();//synchronized
				toggleSimulate(false);
				task = null;
			}

			/** Creates new Task objects to handle specified button action.  */
			public void actionPerformed(ActionEvent e)
			{
				String cmd = e.getActionCommand();
				System.out.println(cmd);

				if(cmd.equals("Reset")) {
					resetToRest();
				}
				else if(cmd.equals("Clear")){
					PS.clear();
				}
				// Action buttons
				else if(cmd.equals("Create Particle")){
					toggleSimulate(false);
					task = new CreateParticleTask();
				}
				else if(cmd.equals("Move Particle")){
					toggleSimulate(false);
					task = new MoveParticleTask();
				}
				else if(cmd.equals("Create Spring")){
					task = new CreateSpringTask();
				}
				else if(cmd.equals("Create Hair")){
					toggleSimulate(false);
					task = new CreateHairTask();
				}
				else if(cmd.equals("Pin Constraint")){
					toggleSimulate(false);
					task = new PinConstraintTask();
				}
		// 		else if(cmd.equals("Rigid Constraint")){
		// 		    task = new RigidConstraintTask();
		// 		}
				// Configuration buttons
				else if(cmd.equals("Save Configuration")){
					PS.saveConfiguration("conf.txt");
				}
				else if(cmd.equals("Load Configuration")){
					toggleSimulate(false);
					PS.loadConfiguration("conf.txt");
				}
				// Integrator buttons
				else if(cmd.equals("Forward Euler")){
					PS.changeIntegrator(new ForwardEulerIntegrator());
				}
				else if(cmd.equals("Symplectic Euler")){
					PS.changeIntegrator(new SymplecticEulerIntegrator());
				}
				else if(cmd.equals("Midpoint")){
					PS.changeIntegrator(new MidpointIntegrator());
				}
				else if(cmd.equals("Velocity Verlet")){
					PS.changeIntegrator(new VelocityVerletIntegrator());
				}
				else if(cmd.equals("Drag Particle")) {
					toggleSimulate(true);
					task = new DragParticleTask();
				}
				else if(cmd.equals("Water Gun")) {
					toggleSimulate(true);
					task = new EmitterTask(new WaterGun(new Point3d(0, 0, 0.5), new Point3d(0, 0, -1), new Vector3d(0, 0, -10), 1, PS));
				}
				else if(cmd.equals("Cannon")) {
					toggleSimulate(true);
					task = new EmitterTask(new Cannon(new Point3d(0, 0, 0.5), new Point3d(0, 0, -1), new Vector3d(0, 0, -10), 50, PS));
				}
				else if(cmd.equals("C-4")) {
					toggleSimulate(true);
					task = new EmitterTask(new C4Explosive(new Point3d(0, 0, 0), 10, 1000, PS));
				}
				else if(cmd.equals("Paint Brush")) {
					toggleSimulate(true);
					task = new EmitterTask(new PaintBrush(2, PS));
				}
				else if(cmd.equals("Add Fluid Force")) {
					toggleSimulate(true);
					PS.addForce(new FluidForce(PS));
				}
				else {
					System.out.println("UNHANDLED ActionEvent: "+e);
				}
			}

			public void stateChanged (ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if(!source.getValueIsAdjusting()) {
					// Change steps
					N_STEPS_PER_FRAME = Math.max(1, source.getValue());
					System.out.println("N_STEPS_PER_FRAME="+N_STEPS_PER_FRAME+";  dt="+(DT/(double)N_STEPS_PER_FRAME));
				}
			}
		}


		// Methods required for the implementation of MouseListener
		public void mouseEntered (MouseEvent e) { if(task!=null) task.mouseEntered(e);  }
		public void mouseExited  (MouseEvent e) { if(task!=null) task.mouseExited(e);   }
		public void mousePressed (MouseEvent e) {
			// Viewpoint Rotation
			if((e.getModifiers() & e.BUTTON3_MASK) != 0) {
				prevMouseX = e.getX();
				prevMouseY = e.getY();
				mouseRButtonDown = true;
			} else if(task!=null)
				task.mousePressed(e);
		}
		public void mouseReleased(MouseEvent e) {
			// Viewpoint Rotation
			if((e.getModifiers() & e.BUTTON3_MASK) != 0) {
				mouseRButtonDown = false;
			} else if(task!=null)
				task.mouseReleased(e);
		}
		public void mouseClicked (MouseEvent e) { if(task!=null) task.mouseClicked(e);  }

		// Methods required for the implementation of MouseMotionListener
		public void mouseDragged (MouseEvent e) {
			// Viewpoint Rotation
			if((e.getModifiers() & e.BUTTON3_MASK) != 0) {
				int x = e.getX();
				int y = e.getY();
				Dimension size = e.getComponent().getSize();

				float thetaY = 360.0f * ( (float)(x-prevMouseX)/(float)size.width);
				float thetaX = 360.0f * ( (float)(prevMouseY-y)/(float)size.height);

				prevMouseX = x;
				prevMouseY = y;

				/*if(mouseRButtonDown) {
					view_rotz += 0.5*thetaX;
				} else {*/
					view_rotx += 0.5*thetaX;
					view_roty -= 0.5*thetaY;
				//}
			} else if(task!=null)
				task.mouseDragged(e);
		}
		public void mouseMoved   (MouseEvent e) { if(task!=null) task.mouseMoved(e);    }

		/**
		 * Handles keyboard events, e.g., spacebar toggles
		 * simulation/pausing, and escape resets the current Task.
		 */
		public void dispatchKey(char key, KeyEvent e)
		{
			//System.out.println("CHAR="+key+", keyCode="+e.getKeyCode()+", e="+e);
			if(key == ' ') {//SPACEBAR --> TOGGLE SIMULATE
				toggleSimulate(!simulate);
				radio_buttons.clearSelection();
				task = null;
			}
			else if (e.toString().contains("Escape")) {//sloth
				System.out.println("ESCAPE");

				Task lastTask = task;
				taskSelector.resetToRest();//sets task=null;
				if(lastTask != null) {
					lastTask.reset();
					task = lastTask;
				}
			}
			else if (key == 'e') {//toggle exporter
				do_export = !do_export;
				System.out.println("'e' : do_export = "+do_export);
			}
			else if (key == '=') {//increase nsteps
				N_STEPS_PER_FRAME = Math.max((int)(1.05*N_STEPS_PER_FRAME), N_STEPS_PER_FRAME+1);
				System.out.println("N_STEPS_PER_FRAME="+N_STEPS_PER_FRAME+";  dt="+(DT/(double)N_STEPS_PER_FRAME));
			}
			else if (key == '-') {//decrease nsteps
				int n = Math.min((int)(0.95*N_STEPS_PER_FRAME), N_STEPS_PER_FRAME-1);
				N_STEPS_PER_FRAME = Math.max(1, n);
				System.out.println("N_STEPS_PER_FRAME="+N_STEPS_PER_FRAME+";  dt="+(DT/(double)N_STEPS_PER_FRAME));
			}
			else if (key == 'l') {
				PS.loadConfiguration("conf.txt");
			}
			else if (key == 's') {
				PS.saveConfiguration("conf.txt");
			}
		}

		/**
		 * "Task" command base-class extended to support
		 * building/interaction via mouse interface.  All objects
		 * extending Task are implemented here as inner classes for
		 * simplicity.
		 */
		abstract class Task implements MouseListener, MouseMotionListener
		{
			/** Displays any task-specific OpengGL information,
			 * e.g., highlights, etc. */
			public void display(GL gl) {}

			// Methods required for the implementation of MouseListener
			public void mouseEntered (MouseEvent e) {}
			public void mouseExited  (MouseEvent e) {}
			public void mousePressed (MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseClicked (MouseEvent e) {}

			// Methods required for the implementation of MouseMotionListener
			public void mouseDragged (MouseEvent e) {}
			public void mouseMoved   (MouseEvent e) {}

			/** Override to specify reset behavior during "escape" key
			 * events, etc. */
			abstract void reset();

		}
		/** Clicking task that creates particles. */
		class CreateParticleTask extends Task
		{
			//private Particle lastCreatedParticle = null;

			public void mousePressed (MouseEvent e) {
			Point3d x0 = getPoint3d(e);
			Particle lastCreatedParticle = PS.createParticle(x0);
			}
			void reset() {}
		}

		/** Task to move nearest particle. */
		class MoveParticleTask extends Task
		{
			private Particle moveParticle = null;

			/** Start moving nearest particle to mouse press. */
			public void mousePressed(MouseEvent e)
			{
			// FIND NEAREST PARTICLE:
			Point3d cursorP = getPoint3d(e);//cursor position
			moveParticle = PS.getNearestParticle(cursorP);

			/// START MOVING (+ HIGHLIGHT):
			updatePosition(cursorP);
			}
			/** Update moved particle state. */
			private void updatePosition(Point3d newX)
			{
				if(moveParticle==null) return;
				PS.removePassiveForce(moveParticle);
				moveParticle.setHighlight(true);
				// Currently, if there is spring on the particle, the rest length would not be updated
				moveParticle.x. set(newX);
				moveParticle.x0.set(newX);
			}
			/** Update particle. */
			public void mouseDragged(MouseEvent e)
			{
				Point3d cursorP = getPoint3d(e);//cursor position
				updatePosition(cursorP);
			}

			/** Invokes reset() */
			public void mouseReleased(MouseEvent e) {
				reset();
			}

			/** Disable highlight, and nullify moveParticle. */
			void reset() {
				if(moveParticle!=null) moveParticle.setHighlight(false);
				moveParticle = null;
			}

			public void display(GL gl) {}
		}


		/** Creates inter-particle springs. */
		class CreateSpringTask extends Task
		{
			private Particle p1 = null;
			private Particle p2 = null;
			private Point3d  cursorP = null;

			CreateSpringTask() {}

			/** Start making a spring from the nearest particle. */
			public void mousePressed(MouseEvent e)
			{
			// FIND NEAREST PARTICLE:
				cursorP = getPoint3d(e);//cursor position
				p1 = PS.getNearestParticle(cursorP); /// = constant (since at rest)
				p2 = null;
			}

			/** Update cursor location for display */
			public void mouseDragged(MouseEvent e)
			{
				cursorP = getPoint3d(e);//update cursor position
			}

			/** Find nearest particle, and create a
			 * SpringForce2Particle when mouse released, unless
			 * nearest particle, p2, is same as p1. */
			public void mouseReleased(MouseEvent e)
			{
				cursorP = getPoint3d(e);//cursor position
				p2      = PS.getNearestParticle(cursorP); /// = constant (since at rest)
				if(p1 != p2) {//make force object
					SpringForce2Particle newForce = new SpringForce2Particle(p1, p2, PS);//params
					PS.addForce(newForce);
				}
				/// RESET:
				p1 = p2 = null;
				cursorP = null;
			}

			/** Cancel any spring creation. */
			void reset()
			{
				p1 = p2 = null;
				cursorP = null;
			}

			/** Draw spring-in-progress.  NOTE: created springs are
			 * drawn by ParticleSystem. */
			public void display(GL gl)
			{
				if(cursorP==null || p1==null) return;

				/// DRAW A LINE:
				gl.glColor3f(1,1,1);
				gl.glBegin(GL.GL_LINES);
				gl.glVertex3d(cursorP.x, cursorP.y, cursorP.z);
				gl.glVertex3d(p1.x.x, p1.x.y, p1.x.z);
				gl.glEnd();
			}
		}


		/** Runtime dragging of nearest particle using a spring
		 * force. */
		class DragParticleTask extends Task
		{
			private Particle dragParticle = null;
			private Point3d  cursorP      = null;

			private SpringForce1Particle springForce = null;

			public void mousePressed(MouseEvent e)
			{
			// FIND NEAREST PARTICLE:
			cursorP = getPoint3d(e);//cursor position
			dragParticle = PS.getNearestParticle(cursorP);

			/// START APPLYING FORCE:
			springForce = new SpringForce1Particle(dragParticle, cursorP, PS);
			PS.addForce(springForce);//to be removed later
			}

			/** Cancel any particle dragging and forces. */
			void reset() {
			dragParticle = null;
			cursorP      = null;
			if(springForce != null)  PS.removeForce(springForce);
			}

			public void mouseDragged(MouseEvent e)
			{
			cursorP = getPoint3d(e);//cursor position

			/// UPDATE DRAG FORCE ANCHOR:
			springForce.updatePoint(cursorP);
			}

			public void mouseReleased(MouseEvent e)
			{
			cursorP = null;
			dragParticle = null;

			/// CANCEL/REMOVE FORCE:
			PS.removeForce(springForce);
			}

			public void display(GL gl) {}
		}


		/** Create hair task. */
		class CreateHairTask extends Task
		{
			ArrayList<Particle> hairParticles = new ArrayList<Particle>();

			/** Create new particle. */
			public void mousePressed(MouseEvent e)
			{
			Particle p2 = PS.createParticle(getPoint3d(e));
			if(hairParticles.size() > 0) {/// ADD STRETCH SPRING p1-p2:
				Particle p1 = hairParticles.get(hairParticles.size()-1);
				PS.addForce(new SpringForce2Particle(p1, p2, PS));

				if(hairParticles.size() > 1) {/// ADD BENDING SPRING TO p0-p1-p2
				Particle p0 = hairParticles.get(hairParticles.size()-2);
				PS.addForce(new SpringForceBending(p0, p1, p2, PS));
				}
			}
			hairParticles.add(p2);//finally add new particle to list
			}
			public void mouseDragged(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void display(GL gl) {}
			void reset() { hairParticles.clear(); }
		}

		/** Toggle pin constraints. */
		class PinConstraintTask extends Task
		{
			/** Toggle pin constraint on nearest particle. */
			public void mousePressed(MouseEvent e)
			{
			Point3d  cursorP = getPoint3d(e);
			Particle p1 = PS.getNearestParticle(cursorP); /// = constant (since at rest)
			if(p1 != null) {// TOGGLE PIN:
				p1.setPin( !p1.isPinned() );
			}
			}
			public void mouseDragged(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void display(GL gl) {}
			void reset() { }
		}

		/** Emitter task. */
		class EmitterTask extends Task
		{
			private Emitter		emitter;

			/** Calculate the intersection of a ray and a plane */
			private double calculateIntersection (Vector3d normal, Point3d x, Vector3d ray, Point3d p) {
				if(normal.dot(ray) < 0) {
					double	t = (normal.dot(new Vector3d(x)) - normal.dot(new Vector3d(p))) / normal.dot(ray);
					//if(t > 0)
						return t;
					/*else
						return Double.POSITIVE_INFINITY;*/
				} else
					return Double.POSITIVE_INFINITY;
			}

			/** Find the intersection in 3D space, and make emitter point to it */
			private void updateEmitterOrientation (MouseEvent e) {
				// Compute the ray and up vector in world coord.

				// Compute the inverse rotation matrix (which transforms eye coord. into world coord.)
				Matrix3d	rot_mat = new Matrix3d(getInverseCameraRotationMatrix());

				// Transform vectors
				Vector3d	ray = new Vector3d(0, 0, -1),
							up = new Vector3d(0, 1, 0),
							right = new Vector3d();
				Point3d		mouse_pt = getPoint3d(e);
				rot_mat.transform(ray);
				rot_mat.transform(up);
				rot_mat.transform(mouse_pt);
				right.cross(ray, up);

				// Find the intersection with the walls
				double		t = Double.POSITIVE_INFINITY, r;
				Vector3d[]	normals = {
					new Vector3d(1.0, 0.0, 0.0),
					new Vector3d(0.0, 1.0, 0.0),
					new Vector3d(0.0, 0.0, 1.0),
					new Vector3d(-1.0, 0.0, 0.0),
					new Vector3d(0.0, -1.0, 0.0),
					new Vector3d(0.0, 0.0, -1.0)
				};
				Point3d[]	points = {
					new Point3d(-0.5, -0.5, -0.5),
					new Point3d(-0.5, -0.5, -0.5),
					new Point3d(-0.5, -0.5, -0.5),
					new Point3d(0.5, 0.5, 0.5),
					new Point3d(0.5, 0.5, 0.5),
					new Point3d(0.5, 0.5, 0.5)
				};
				Vector3d	hit_normal = new Vector3d();
				for(int i=0; i<6; i++) {
					r = Math.min(t, calculateIntersection(normals[i], points[i], ray, mouse_pt));
					if(r < t) {
						t = r;
						hit_normal = normals[i];
					}
				}

				Point3d	hit_pt = new Point3d();
				hit_pt.scaleAdd(t, ray, mouse_pt);

				if(hit_pt.x <= 0.5 && hit_pt.x >= -0.5 && hit_pt.y <= 0.5 && hit_pt.y >= -0.5 && hit_pt.z <= 0.5 && hit_pt.z >= -0.5) {
					emitter.setDestination(hit_pt, hit_normal);
				}
			}

			public EmitterTask (Emitter emitter) {
				this.emitter = emitter;
			}

			public void mousePressed(MouseEvent e) {
				emitter.startShoot(new Color3f(current_color));
			}
			public void mouseDragged(MouseEvent e) {
				updateEmitterOrientation(e);
			}
			public void mouseReleased(MouseEvent e) {
				emitter.stopShoot();
			}
			public void mouseMoved   (MouseEvent e) {
				updateEmitterOrientation(e);
			}
			public void display(GL gl) {
				// Emitters
				emitter.display(gl);
				emitter.shoot();
			}
			void reset() {  }
		}


	// 	/** SKIP: Rigid body constraint. */
	// 	class RigidConstraintTask extends Task
	// 	{
	// 	    HashSet<Particle> rigidSet = new HashSet<Particle>();

	// 	    /** Toggle rigid constraints on nearest particle. */
	// 	    public void mousePressed(MouseEvent e)
	// 	    {
	// 		Point2d  cursorP = getPoint2d(e);
	// 		Particle p1 = PS.getNearestParticle(cursorP); /// = constant (since at rest)
	// 		if(p1 != null) {// TOGGLE SET:
	// 		    if(rigidSet.contains(p1)) {//REMOVE
	// 			rigidSet.remove(p1);
	// 			p1.setHighlight(false);
	// 		    }
	// 		    else {// ADD
	// 			rigidSet.add(p1);
	// 			p1.setHighlight(true);
	// 		    }
	// 		}
	// 	    }
	// 	    public void mouseDragged(MouseEvent e) {}
	// 	    public void mouseReleased(MouseEvent e) {}
	// 	    public void display(GL gl) {}
	// 	    void reset() { rigidSet.clear(); }
	// 	}
    }

    private static int exportId = -1;
    private class FrameExporter implements Exporter
    {
	private int nFrames  = 0;

	FrameExporter()  { 
	    exportId += 1;
	}

	public void writeFrame()
	{ 
	    long   timeNS   = -System.nanoTime();
	    String number   = Utils.getPaddedNumber(nFrames, 5, "0");
	    String filename = "frames/export"+exportId+"-"+number+".png";/// BUG: DIRECTORY MUST EXIST!

	    try{  
		java.io.File   file     = new java.io.File(filename);
		if(file.exists()) System.out.println("WARNING: OVERWRITING PREVIOUS FILE: "+filename);

		/// WRITE IMAGE: ( :P Screenshot asks for width/height --> cache in GLEventListener.reshape() impl)
		com.sun.opengl.util.Screenshot.writeToFile(file, width, height);

		System.out.println((timeNS/1000000)+"ms:  Wrote image: "+filename);
		
	    }catch(Exception e) { 
		e.printStackTrace();
		System.out.println("OOPS: "+e); 
	    } 

	    nFrames += 1;
	}
    }


    /**
     * ### Runs the ParticleSystemBuilder. ###
     */
    public static void main(String[] args) 
    {
	try{
	    ParticleSystemBuilder psb = new ParticleSystemBuilder();
	    psb.start();

	}catch(Exception e) {
	    e.printStackTrace();
	    System.out.println("OOPS: "+e);
	}
    }
}

