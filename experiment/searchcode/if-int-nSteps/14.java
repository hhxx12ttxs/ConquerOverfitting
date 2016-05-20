package cs5643.finalproj;

import cs5643.finalproj.knitted.*;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.colorchooser.*;
import javax.swing.event.*;
import javax.vecmath.*;

import javax.media.opengl.*;
import com.sun.opengl.util.*;
import com.sun.opengl.util.j2d.*;

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
public class ParticleSystemBuilder implements GLEventListener {

	private ArrayList<Exporter> exporters = new ArrayList<Exporter>();
	private boolean do_export = false;
	private static int N_STEPS_PER_FRAME = 50;
	/** Default graphics time step size. */
	public static final double DT = 0.01;
	/** Main window frame. */
	JFrame frame = null;
	private int width, height;
	/** The single ParticleSystem reference. */
	ParticleSystem PS;
	/** Object that handles all GUI and user interactions of building
	 * Task objects, and simulation. */
	BuilderGUI gui;
	/** Render text */
	private TextRenderer textRenderer = new TextRenderer(new Font("Monospaced", Font.BOLD, 16), true, true);

	/** Most recent number of maximum iterations of collision test and constraints */
	private int				max_collision_iterations, max_constraint_iterations;
	/** Most recent number of iterations of collision test and constraints (average over all steps) */
	private double			avg_collision_iterations, avg_constraint_iterations;

	// Frame-per-second
	private DurationTimer timer = new DurationTimer();
	private boolean firstRender = true;
	private int frameCount;
	private double frame_per_sec;
	private int WALL_DISPLAY_LIST1 = -1,
			WALL_DISPLAY_LIST2 = -1;

	/** Main constructor. Call start() to begin simulation. */
	ParticleSystemBuilder() throws Exception {
		PS = new ParticleSystem();
		exporters.add(new FrameExporter());
	}

	/**
	 * Builds and shows windows/GUI, and starts simulator.
	 */
	public void start() {
		if (frame != null) {
			return;
		}

		gui = new BuilderGUI();/// MOVED HERE SINCE CALLED BY frame/animator

		frame = new JFrame("CS5643 Final");
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
		frame.setSize(600, 600);
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
	public void init(GLAutoDrawable drawable) {
		// DEBUG PIPELINE (can use to provide GL error feedback... disable for speed)
		//drawable.setGL(new DebugGL(drawable.getGL()));

		GL gl = drawable.getGL();
		System.err.println("INIT GL IS: " + gl.getClass().getName());

		gl.setSwapInterval(1);

		gl.glLineWidth(3);

		gl.glShadeModel(GL.GL_SMOOTH); // try setting this to GL_FLAT and see what happens.

		// TODO: Enable this
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glEnable(GL.GL_DEPTH_TEST);

		drawable.addMouseListener(gui);
		drawable.addMouseMotionListener(gui);
		drawable.addMouseWheelListener(gui);

		drawable.addKeyListener(new KeyAdapter() {

			public void keyTyped(KeyEvent e) {
				gui.dispatchKey(e.getKeyChar(), e);
			}
		});

		// Wall display list
		int displayListIndex = gl.glGenLists(1);
		gl.glNewList(displayListIndex, GL.GL_COMPILE);
		gl.glBegin(GL.GL_TRIANGLES);
		drawPlane(gl, new Point3d(-5.0, 0.0, -5.0), new Vector3d(0, 0, 1), new Vector3d(1, 0, 0), 10.0, 20);
		gl.glEnd();
		gl.glEndList();
		System.out.println("MADE LIST " + displayListIndex + " : " + gl.glIsList(displayListIndex));
		WALL_DISPLAY_LIST1 = displayListIndex;

		// TODO: Remove
		/*displayListIndex = gl.glGenLists(1);
		gl.glNewList(displayListIndex, GL.GL_COMPILE);
		gl.glBegin(GL.GL_TRIANGLES);
		drawPlane(gl, new Point3d(-0.5, -0.5, -0.5), new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), 20);
		drawPlane(gl, new Point3d(-0.5, -0.5, -0.5), new Vector3d(0, 1, 0), new Vector3d(0, 0, 1), 20);
		drawPlane(gl, new Point3d(0.5, 0.5, 0.5), new Vector3d(-1, 0, 0), new Vector3d(0, 0, -1), 20);

		gl.glEnd();
		gl.glEndList();
		System.out.println("MADE LIST "+displayListIndex+" : "+gl.glIsList(displayListIndex));
		WALL_DISPLAY_LIST2 = displayListIndex;*/
	}

	/** GLEventListener implementation */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
	}

	/** GLEventListener implementation */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

		System.out.println("width=" + width + ", height=" + height);
		height = Math.max(height, 1); // avoid height=0;

		this.width = width;
		this.height = height;

		GL gl = drawable.getGL();
		gl.glViewport(0, 0, width, height);

		/// SETUP ORTHOGRAPHIC PROJECTION AND MAPPING INTO UNIT CELL:
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		vp_map = new OrthoMap(width, height, gui.getViewHeight());//Hide grungy details in OrthoMap
		//vp_map = new PerspectiveMap(width, height);
		vp_map.apply(gl);

		/// GET READY TO DRAW:
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	private void drawPlane(GL gl, Point3d p, Vector3d v1, Vector3d v2, double size, int number) {
		Vector3d v11 = new Vector3d(),
				v12 = new Vector3d(),
				v21 = new Vector3d(),
				v22 = new Vector3d(),
				normal = new Vector3d();
		normal.cross(v1, v2);
		normal.normalize();
		for (int i = 0; i < number; i++) {
			v11.scale((double) i * size / number, v1);
			v12.scale((double) (i + 1) * size / number, v1);
			for (int j = 0; j < number; j++) {
				v21.scale((double) j * size / number, v2);
				v22.scale((double) (j + 1) * size / number, v2);

				gl.glNormal3d(normal.x, normal.y, normal.z);
				gl.glVertex3d(p.x + v11.x + v21.x, p.y + v11.y + v21.y, p.z + v11.z + v21.z);
				gl.glVertex3d(p.x + v12.x + v21.x, p.y + v12.y + v21.y, p.z + v12.z + v21.z);
				gl.glVertex3d(p.x + v12.x + v22.x, p.y + v12.y + v22.y, p.z + v12.z + v22.z);
				gl.glVertex3d(p.x + v11.x + v21.x, p.y + v11.y + v21.y, p.z + v11.z + v21.z);
				gl.glVertex3d(p.x + v12.x + v22.x, p.y + v12.y + v22.y, p.z + v12.z + v22.z);
				gl.glVertex3d(p.x + v11.x + v22.x, p.y + v11.y + v22.y, p.z + v11.z + v22.z);
			}
		}
	}

	/**
	 * Main event loop: OpenGL display + simulation
	 * advance. GLEventListener implementation.
	 */
	public void display(GLAutoDrawable drawable) {
		// Calculate FPS
		if (!firstRender) {
			++frameCount;
			if (timer.getTime() > 1000) {
				timer.stop();
				frame_per_sec = (double) frameCount / timer.getDurationAsSeconds();
				timer.reset();
				timer.start();
				frameCount = 0;
			}
		} else {
			firstRender = false;
			timer.start();
		}

		// Try to control FPS
		/*try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			throw new GLException(e);
		}*/

		GL gl = drawable.getGL();
		gl.glClearColor(0, 0, 0, 0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		vp_map = new OrthoMap(width, height, gui.getViewHeight());
		vp_map.apply(gl);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();

		// Rotate camera
		gl.glPushMatrix();
		// This is a matrix stack, so the first matrix is multipled last
		// TODO: Remove
		// Move the world back to the original place (center is (0.5, 0.5))
		//gl.glTranslated(0.5, 0.5, 0.0);
		// TODO: For PerspectiveMap. The position is near+1(the dist. to the nearer wall)+0.5(the dist. of the nearer wall to the center)
		//gl.glTranslated(0.0, 0.0, -2.698);
		gl.glRotated(-gui.getRotationZ(), 0.0, 0.0, 1.0);
		gl.glRotated(-gui.getRotationX(), 1.0, 0.0, 0.0);
		gl.glRotated(-gui.getRotationY(), 0.0, 1.0, 0.0);
		// Move the world to the world origin (move the center (0.5, 0.5) to (0, 0)
		//gl.glTranslated(-0.5, -0.5, 0.0);

		// Lights
		//float	light_pos[] = { 0.2f, 0.25f, 0.3f, 1.0f },
		float	light_pos[] = { 1.0f, 1.0f, 0.5f, 0.0f },
				light_amb_color[] = { 0.2f, 0.2f, 0.2f, 1.0f },
				light_diffuse_color[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light_pos, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, light_amb_color, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, light_diffuse_color, 0);
		gl.glEnable(GL.GL_LIGHT0);

		/// DRAW COMPUTATIONAL CELL BOUNDARY:
		{
			gl.glEnable(GL.GL_LIGHTING);
			float blue[] = {0.45f, 0.45f, 0.6f, 1.0f};
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, blue, 0);
			gl.glCallList(WALL_DISPLAY_LIST1);
			gl.glDisable(GL.GL_LIGHTING);
		}

		/// SIMULATE/DISPLAY HERE (Handled by BuilderGUI):
		gui.simulateAndDisplayScene(gl);

		// Remember that every push needs a pop; this one is paired with
		// rotating the entire world
		gl.glPopMatrix();

		// Show FPS
		textRenderer.beginRendering(width, height);
		textRenderer.setColor(0.9f, 0.9f, 0.9f, 1f);
		textRenderer.draw(String.format("%.2f fps (constraint: %.2f/%d, collision: %.2f/%d)",
					frame_per_sec,
					avg_constraint_iterations, max_constraint_iterations,
					avg_collision_iterations, max_collision_iterations),
				20, height - 16);
		if(gui.simulate)
			textRenderer.draw("simulating", 20, height - 32);
		textRenderer.endRendering();

		if (do_export) {
			for (Exporter e : exporters) {
				e.writeFrame();
			}
		}
	}

	public Matrix3d getInverseCameraRotationMatrix() {
		return gui.getInverseCameraRotationMatrix();
	}

	/** Interaction central: Handles windowing/mouse events, and building state. */
	class BuilderGUI implements MouseListener, MouseMotionListener, MouseWheelListener//, KeyListener
	{

		boolean simulate = false;
		/** Current build task (or null) */
		Task task;
		JFrame guiFrame, colorFrame;
		TaskSelector taskSelector = new TaskSelector();
		ColorSelector colorSelector = new ColorSelector();
		Color current_color = null;
		// Viewpoint orientation
		//private double view_rotx = -30.0, view_roty = 45.0, view_rotz = 0.0;
		private double view_rotx = -30.0, view_roty = 0.0, view_rotz = 0.0, view_height = 10.0;
		private int prevMouseX, prevMouseY;
		private boolean mouseRButtonDown = false;
		private ButtonGroup radio_buttons = new ButtonGroup(),
				integrator_buttons = new ButtonGroup();

		public double getRotationX() {
			return view_rotx;
		}

		public double getRotationY() {
			return view_roty;
		}

		public double getRotationZ() {
			return view_rotz;
		}

		public double getViewHeight () {
			return view_height;
		}

		private void createButton(String name) {
			JButton button = new JButton(name);
			guiFrame.add(button);
			button.addActionListener(taskSelector);
		}

		private void createRadioButtons(String[] names, int default_id, ButtonGroup group) {
			for (int i = 0; i < names.length; i++) {
				JToggleButton button = new JRadioButton(names[i], (default_id == i));
				group.add(button);
				guiFrame.add(button);
				button.addActionListener(taskSelector);
			}
		}

		BuilderGUI() {
			guiFrame = new JFrame("Tasks");
			guiFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			//guiFrame.setLayout(new SpringLayout());
			guiFrame.setLayout(new GridLayout(5, 1));

			createButton("Reset");
			createButton("Clear");

			// Action buttons
			/*String[] action_names = {"Create Particle", "Move Particle", "Create Spring",
				"Create Hair", "Pin Constraint", "Drag Particle", "Water Gun", "Cannon", "C-4", "Paint Brush"};
			createRadioButtons(action_names, 1, radio_buttons);*/

			// Configuration buttons
			createButton("Save Configuration");
			createButton("Load Configuration");

			// Integrator buttons
			/*String[] integrator_names = {"Forward Euler", "Symplectic Euler", "Midpoint", "Velocity Verlet"};
			createRadioButtons(integrator_names, 1, integrator_buttons);*/

			// Step size
			JSlider number_of_steps = new JSlider(JSlider.HORIZONTAL, 1, 1000, N_STEPS_PER_FRAME);
			number_of_steps.setMajorTickSpacing(100);
			number_of_steps.setPaintTicks(true);
			number_of_steps.setPaintLabels(true);
			Hashtable labelTable = new Hashtable();
			labelTable.put(new Integer(1), new JLabel("1"));
			labelTable.put(new Integer(500), new JLabel("500"));
			labelTable.put(new Integer(1000), new JLabel("1000"));
			number_of_steps.setLabelTable(labelTable);
			number_of_steps.addChangeListener(taskSelector);
			guiFrame.add(number_of_steps);

			guiFrame.setSize(200, 200);
			guiFrame.pack();
			// TODO: Debug
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
			// TODO: Debug
			//colorFrame.setVisible(true);

			task = new DragParticleTask();
			//task = new EmitterTask(new WaterGun(new Point3d(0, 0, 0.5), new Point3d(0, 0, -1), new Vector3d(0, 0, -10), 5, PS));
			//task = new EmitterTask(new Cannon(new Point3d(0, 0, 0.5), new Point3d(0, 0, -1), new Vector3d(0, 0, -50), 50, PS));
			//task = new EmitterTask(new C4Explosive(new Point3d(0, 0, 0), 10, 1000, PS));
			//task = new EmitterTask(new PaintBrush(2, PS));
		}

		/** Simulate then display particle system and any builder
		 * adornments. */
		void simulateAndDisplayScene(GL gl) {
			/// TODO: OVERRIDE THIS INTEGRATOR (Doesn't use Force objects properly)
			if (simulate) {
				if (false) {//ONE EULER STEP
					PS.advanceTime(DT);
				} else {//MULTIPLE STEPS FOR STABILITY WITH FORWARD EULER (UGH!)
					int nSteps = N_STEPS_PER_FRAME;
					double dt = DT / (double) nSteps;

					avg_constraint_iterations = 0;
					max_constraint_iterations = 0;
					avg_collision_iterations = 0;
					max_collision_iterations = 0;
					for(int k = 0; k < nSteps; k++) {
						PS.advanceTime(dt);

						IterationInfo itinfo = PS.getIterationInfo();
						avg_collision_iterations += itinfo.collision_iterations;
						max_collision_iterations = Math.max(max_collision_iterations, itinfo.collision_iterations);
						avg_constraint_iterations += itinfo.constraint_iterations;
						max_constraint_iterations = Math.max(max_constraint_iterations, itinfo.constraint_iterations);
					}
					avg_collision_iterations /= nSteps;
					avg_constraint_iterations /= nSteps;
				}

				/// TODO: PROCESS COLLISIONS HERE:

			}

			// Draw particles, springs, etc.
			PS.display(gl);

			// Display Task, e.g., currently drawn spring.
			if (task != null) {
				task.display(gl);
			}
		}

		private boolean toggleSimulate(boolean do_simulate) {
			boolean prev = simulate;
			simulate = do_simulate;
			// TODO: Is this needed?
			if (!simulate) {
				PS.stop();
			}
			return prev;
		}

		public Matrix3d getCameraRotationMatrix() {
			// Compute the rotation matrix (which transforms world coord. into eye coord.)
			Matrix3d rotX = new Matrix3d(),
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

		public Matrix3d getInverseCameraRotationMatrix() {
			// Compute the inverse rotation matrix (which transforms eye coord. into world coord.)
			Matrix3d rotX = new Matrix3d(),
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

			public void stateChanged(ChangeEvent e) {
				ColorSelectionModel csm = (ColorSelectionModel) e.getSource();
				current_color = csm.getSelectedColor();
			}
		}

		/**
		 * ActionListener implementation to manage Task selection
		 * using (radio) buttons.
		 */
		class TaskSelector implements ActionListener, ChangeListener {

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
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				System.out.println(cmd);

				if (cmd.equals("Reset")) {
					resetToRest();
				} else if (cmd.equals("Clear")) {
					PS.clear();
				}
				// 		else if(cmd.equals("Rigid Constraint")){
				// 		    task = new RigidConstraintTask();
				// 		}
				// Configuration buttons
				else if (cmd.equals("Save Configuration")) {
					PS.saveConfiguration("tmp.knit");
				}
				/*else if (cmd.equals("Load Configuration")) {
					PS.loadConfiguration("conf.txt");*/
				// Integrator buttons
				else if (cmd.equals("Drag Particle")) {
					toggleSimulate(true);
					task = new DragParticleTask();
				} else {
					System.out.println("UNHANDLED ActionEvent: " + e);
				}
			}

			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					// Change steps
					N_STEPS_PER_FRAME = Math.max(1, source.getValue());
					System.out.println("N_STEPS_PER_FRAME=" + N_STEPS_PER_FRAME + ";  dt=" + (DT / (double) N_STEPS_PER_FRAME));
				}
			}
		}

		// Methods required for the implementation of MouseListener
		public void mouseEntered(MouseEvent e) {
			if (task != null) {
				task.mouseEntered(e);
			}
		}

		public void mouseExited(MouseEvent e) {
			if (task != null) {
				task.mouseExited(e);
			}
		}

		public void mousePressed(MouseEvent e) {
			// Viewpoint Rotation
			if ((e.getModifiers() & e.BUTTON3_MASK) != 0) {
				prevMouseX = e.getX();
				prevMouseY = e.getY();
				mouseRButtonDown = true;
			} else if (task != null) {
				task.mousePressed(e);
			}
		}

		public void mouseReleased(MouseEvent e) {
			// Viewpoint Rotation
			if ((e.getModifiers() & e.BUTTON3_MASK) != 0) {
				mouseRButtonDown = false;
			} else if (task != null) {
				task.mouseReleased(e);
			}
		}

		public void mouseClicked(MouseEvent e) {
			if (task != null) {
				task.mouseClicked(e);
			}
		}

		// Methods required for the implementation of MouseMotionListener
		public void mouseDragged(MouseEvent e) {
			// Viewpoint Rotation
			if ((e.getModifiers() & e.BUTTON3_MASK) != 0) {
				int x = e.getX();
				int y = e.getY();
				Dimension size = e.getComponent().getSize();

				float thetaY = 360.0f * ((float) (x - prevMouseX) / (float) size.width);
				float thetaX = 360.0f * ((float) (prevMouseY - y) / (float) size.height);

				prevMouseX = x;
				prevMouseY = y;

				/*if(mouseRButtonDown) {
				view_rotz += 0.5*thetaX;
				} else {*/
				view_rotx += 0.5 * thetaX;
				view_roty -= 0.5 * thetaY;
				//}
			} else if (task != null) {
				task.mouseDragged(e);
			}
		}

		public void mouseMoved(MouseEvent e) {
			if (task != null) {
				task.mouseMoved(e);
			}
		}

		public void mouseWheelMoved (MouseWheelEvent e) {
			view_height += view_height*e.getWheelRotation()*0.1;
		}

		/**
		 * Handles keyboard events, e.g., spacebar toggles
		 * simulation/pausing, and escape resets the current Task.
		 */
		public void dispatchKey(char key, KeyEvent e) {
			//System.out.println("CHAR="+key+", keyCode="+e.getKeyCode()+", e="+e);
			if (key == ' ') {//SPACEBAR --> TOGGLE SIMULATE
				toggleSimulate(!simulate);
				radio_buttons.clearSelection();
				task = new DragParticleTask();
			} else if (e.toString().contains("Escape")) {//sloth
				System.out.println("ESCAPE");

				Task lastTask = task;
				taskSelector.resetToRest();//sets task=null;
				if (lastTask != null) {
					lastTask.reset();
					task = lastTask;
				}
			} else if (key == 'e') {//toggle exporter
				do_export = !do_export;
				System.out.println("'e' : do_export = " + do_export);
			} else if (key == '=') {//increase nsteps
				N_STEPS_PER_FRAME = Math.max((int) (1.05 * N_STEPS_PER_FRAME), N_STEPS_PER_FRAME + 1);
				System.out.println("N_STEPS_PER_FRAME=" + N_STEPS_PER_FRAME + ";  dt=" + (DT / (double) N_STEPS_PER_FRAME));
			} else if (key == '-') {//decrease nsteps
				int n = Math.min((int) (0.95 * N_STEPS_PER_FRAME), N_STEPS_PER_FRAME - 1);
				N_STEPS_PER_FRAME = Math.max(1, n);
				System.out.println("N_STEPS_PER_FRAME=" + N_STEPS_PER_FRAME + ";  dt=" + (DT / (double) N_STEPS_PER_FRAME));
			/*} else if (key == 'l') {
				PS.loadConfiguration("conf.txt");*/
			} else if (key == 's') {
				PS.saveConfiguration("tmp.knit");
			} else if (key == 'r') {
				PS.reset();
				toggleSimulate(false);
				task = null;
			}
		}

		/**
		 * "Task" command base-class extended to support
		 * building/interaction via mouse interface.  All objects
		 * extending Task are implemented here as inner classes for
		 * simplicity.
		 */
		abstract class Task implements MouseListener, MouseMotionListener {

			/** Displays any task-specific OpengGL information,
			 * e.g., highlights, etc. */
			public void display(GL gl) {
			}

			// Methods required for the implementation of MouseListener
			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}

			// Methods required for the implementation of MouseMotionListener
			public void mouseDragged(MouseEvent e) {
			}

			public void mouseMoved(MouseEvent e) {
			}

			/** Override to specify reset behavior during "escape" key
			 * events, etc. */
			abstract void reset();
		}

		/** Runtime dragging of nearest particle using a spring
		 * force. */
		class DragParticleTask extends Task {

			private ClothPick				dragCloth = null;
			private Point3d					cursorP = null,
											initial_point = null;
			private SpringForce1Particle	springForce = null;

			/** Project current cursor (along the ray x,n) to a plane containing p and perpendicular to n
			 * Note that n should be UNIT vector */
			private Point3d projectCursor (Point3d p, Point3d x, Vector3d n) {
				Vector3d	px = new Vector3d();
				px.sub(p, x);
				double	r = px.dot(n);
				Point3d	cursor = new Point3d();
				cursor.scaleAdd(r, n, x);
				return cursor;
			}

			/** Compute the cursor and ray based on MouseEvent and current transform */
			private void computeRay (MouseEvent e, Vector3d ray, Point3d cursor) {
				
				// Compute the ray and up vector in world coord.
				// Compute the inverse rotation matrix (which transforms eye coord. into world coord.)
				Matrix3d rot_mat = new Matrix3d(getInverseCameraRotationMatrix());

				// Transform vectors
				ray.set(0, 0, -1);
				cursor.set(getPoint3d(e));
				// TODO: Remove
				/*Vector3d	up = new Vector3d(0, 1, 0),
							right = new Vector3d();*/
				rot_mat.transform(ray);
				//rot_mat.transform(up);
				rot_mat.transform(cursor);
				//right.cross(ray, up);

				// TODO: Test
				/*System.err.println(String.format("ray = (%.2f, %.2f, %.2f)", ray.x, ray.y, ray.z));
				System.err.println(String.format("up = (%.2f, %.2f, %.2f)", up.x, up.y, up.z));
				System.err.println(String.format("right = (%.2f, %.2f, %.2f)", right.x, right.y, right.z));
				System.err.println(String.format("mouse_pt = (%.2f, %.2f, %.2f)", mouse_pt.x, mouse_pt.y, mouse_pt.z));*/
			}

			public void mousePressed(MouseEvent e) {

				Point3d		cursor = new Point3d();
				Vector3d	ray = new Vector3d();
				computeRay(e, ray, cursor);

				dragCloth = PS.getNearestParticle(cursor, ray);
				if(dragCloth != null) {
					// TODO: Debug
					//System.err.println(String.format("ClothPick = (%d, %.2f)", cp.i, cp.d));

					// Project cursor to the plane perpendicular to ray and contains the picked particle
					initial_point = new Point3d(dragCloth.cloth.getPosition(dragCloth.i));
					cursorP = projectCursor(initial_point, cursor, ray);
					springForce = new SpringForce1Particle(dragCloth, cursorP, PS);
					PS.addForce(springForce);
				}
			}

			/** Cancel any particle dragging and forces. */
			void reset() {
				dragCloth = null;
				cursorP = null;
				initial_point = null;
				if (springForce != null)
					PS.removeForce(springForce);
			}

			public void mouseDragged(MouseEvent e) {
				if(dragCloth != null) {
					Point3d		cursor = new Point3d();
					Vector3d	ray = new Vector3d();
					computeRay(e, ray, cursor);
					
					// Project cursor to the plane perpendicular to ray and contains the picked particle
					cursorP = projectCursor(initial_point, cursor, ray);
					springForce.updatePoint(cursorP);
				}
			}

			public void mouseReleased(MouseEvent e) {
				cursorP = null;
				dragCloth = null;
				initial_point = null;

				if(springForce != null) {
					/// CANCEL/REMOVE FORCE:
					PS.removeForce(springForce);
					springForce = null;
				}
			}

			public void display(GL gl) {
			}
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

	private class FrameExporter implements Exporter {

		private int nFrames = 0;

		FrameExporter() {
			exportId += 1;
		}

		public void writeFrame() {
			long timeNS = -System.nanoTime();
			String number = Utils.getPaddedNumber(nFrames, 5, "0");
			String filename = "frames/export" + exportId + "-" + number + ".png";/// BUG: DIRECTORY MUST EXIST!

			try {
				java.io.File file = new java.io.File(filename);
				if (file.exists()) {
					System.out.println("WARNING: OVERWRITING PREVIOUS FILE: " + filename);
				}

				/// WRITE IMAGE: ( :P Screenshot asks for width/height --> cache in GLEventListener.reshape() impl)
				com.sun.opengl.util.Screenshot.writeToFile(file, width, height);

				System.out.println((timeNS / 1000000) + "ms:  Wrote image: " + filename);

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("OOPS: " + e);
			}

			nFrames += 1;
		}
	}

	/**
	 * ### Runs the ParticleSystemBuilder. ###
	 */
	public static void main(String[] args) {

		try {
			ParticleSystemBuilder psb = new ParticleSystemBuilder();
			psb.start();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("OOPS: " + e);
		}
	}
}

