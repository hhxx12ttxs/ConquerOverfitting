package cs5643.particles;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.vecmath.*;

import javax.media.opengl.*;
import com.sun.opengl.util.*;
import com.sun.opengl.util.j2d.*;


/**
 * CS 5643: Assignment #2 "Robust Collision Processing" 
 * (a.k.a. "The Spaghetti Factory")
 * <pre>
 * main() entry point class that initializes ParticleSystem, OpenGL
 * rendering, and GUI that manages GUI/mouse events. Spacebar toggles simulation advance.
 * </pre>
 * 
 * @author Doug James, January 2007 (revised Feb 2009)
 */
public class ParticleSystemBuilder implements GLEventListener
{
    private FrameExporter frameExporter;
    
    private static int N_STEPS_PER_FRAME = 30;

    /** Default graphics time step size. */
    public static final double DT = 0.01/N_STEPS_PER_FRAME;

    Color3f bgColor = new Color3f(0,0,0);

    /** Main window frame. */
    JFrame frame = null;

    private int width, height;

	/** For FPS */
	private DurationTimer	timer = new DurationTimer();
	/** For FPS */
	private boolean			firstRender = true;
	/** For FPS */
	private int				frameCount;
	/** Most recent frame per second */
	private float			frame_per_sec;
	/** Most recent number of maximum iterations of collision test */
	private int				max_number_of_iterations;
	/** Most recent number of iterations of collision test (average over all steps) */
	private double			avg_number_of_iterations;

	/** Render text */
	private TextRenderer textRenderer = new TextRenderer(new Font("Monospaced", Font.BOLD, 16), true, true);;

    /** The single ParticleSystem reference. */
    ParticleSystem PS;

    SpaghettiFactory spaghettiFactory = null;

    /** Object that handles all GUI and user interactions of building
     * Task objects, and simulation. */
    BuilderGUI     gui;




    /** Main constructor. Call start() to begin simulation. */
    ParticleSystemBuilder() 
    {
	PS  = new ParticleSystem();
	gui = new BuilderGUI();
    }

    /**
     * Builds and shows windows/GUI, and starts simulator.
     */
    public void start()
    {
	if(frame != null) return;

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
	frame.setLocation(240, 0);
	frame.setVisible(true);
	animator.start();
    }



    private OrthoMap orthoMap;

    /** Maps mouse event into computational cell using OrthoMap. */
    public Point2d getPoint2d(MouseEvent e) {
	return orthoMap.getPoint2d(e);
    }

    /** GLEventListener implementation: Initializes JOGL renderer. */
    public void init(GLAutoDrawable drawable) 
    {
	// DEBUG PIPELINE (can use to provide GL error feedback... disable for speed)
	//drawable.setGL(new DebugGL(drawable.getGL()));

	GL gl = drawable.getGL();
	System.err.println("INIT GL IS: " + gl.getClass().getName());

	gl.setSwapInterval(1);

	/// SETUP ANTI-ALIASED POINTS AND LINES:
	gl.glLineWidth(2);  /// YOU MAY WANT TO ADJUST THIS WIDTH
	gl.glPointSize(3f); /// YOU MAY WANT TO ADJUST THIS SIZE
	gl.glEnable(gl.GL_POINT_SMOOTH);
	gl.glHint  (gl.GL_POINT_SMOOTH_HINT, gl.GL_NICEST);
	gl.glEnable(gl.GL_LINE_SMOOTH);
	gl.glHint  (gl.GL_LINE_SMOOTH_HINT, gl.GL_NICEST);
	gl.glEnable(gl.GL_BLEND);
	gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                
	drawable.addMouseListener(gui);
	drawable.addMouseMotionListener(gui);

	drawable.addKeyListener(new KeyAdapter() {
		public void keyTyped(KeyEvent e) {
		    gui.dispatchKey(e.getKeyChar(), e);
		}
	    });
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
	orthoMap = new OrthoMap(width, height);//Hide grungy details in OrthoMap
	orthoMap.apply_glOrtho(gl);

	/// GET READY TO DRAW:
	gl.glMatrixMode(GL.GL_MODELVIEW);
	gl.glLoadIdentity();
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
				//System.err.println("Frames per second: " + (frameCount / timer.getDurationAsSeconds()) + ", Number of particles = " + PS.size());
				frame_per_sec = frameCount / timer.getDurationAsSeconds();
				timer.reset();
				timer.start();
				frameCount = 0;
			}
		} else {
			firstRender = false;
			timer.start();
		}

		// For debugging
		// Try to control FPS
		/*try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			throw new GLException(e);
		}*/

		GL gl = drawable.getGL();
		//gl.glClearColor(0,0,0,0);
		gl.glClearColor(bgColor.x, bgColor.y, bgColor.z, 0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		/// DRAW COMPUTATIONAL CELL BOUNDARY:
		{
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glColor3f(0.1f, 0.1f, 0.1f);
			gl.glVertex2d(0,0);	gl.glVertex2d(1,0);	gl.glVertex2d(1,1);	gl.glVertex2d(0,1);	gl.glVertex2d(0,0);
			gl.glEnd();
		}

		/// SIMULATE/DISPLAY HERE (Handled by BuilderGUI):
		gui.simulateAndDisplayScene(gl);

		// Show FPS
		textRenderer.beginRendering(width, height);
		{
			// optionally set the color
			textRenderer.setColor(0.9f, 0.9f, 0.9f, 1f);
			textRenderer.draw(String.format("%.2f fps (%.2f iterations, max %d) (%d particles)", frame_per_sec, avg_number_of_iterations, max_number_of_iterations, PS.size()), 20, height-16);
		}
		textRenderer.endRendering();

		if(frameExporter != null) {
			frameExporter.writeFrame();
		}
    }

    /** Interaction central: Handles windowing/mouse events, and building state. */
    class BuilderGUI implements MouseListener, MouseMotionListener//, KeyListener
    {
	boolean simulate = false;

	/** Current build task (or null) */
	Task task;

	JFrame  guiFrame;
	TaskSelector taskSelector = new TaskSelector();

	JToggleButton[] buttons;

	private void createButton (String name) {
		JButton button = new JButton (name);
		guiFrame.add(button);
		button.addActionListener(taskSelector);
	}

	private JToggleButton[] createRadioButtons (String[] names, int default_id, ButtonGroup group) {
		JToggleButton[]	buttons = new JToggleButton[names.length];
		for(int i=0; i<names.length; i++) {
			buttons[i] = new JRadioButton(names[i], (default_id == i));
			group.add(buttons[i]);
			guiFrame.add(buttons[i]);
			buttons[i].addActionListener(taskSelector);
		}
		return buttons;
	}

	BuilderGUI() 
	{
	    ButtonGroup     buttonGroup  = new ButtonGroup();
		String[]		action_names = {
								"Reset",
								"Create Particle",
								"Delete Particle",
								"Move Particle",
								"Drag Particle",
								"Create Spring",
								"Create Hair",
								"Draw a Hair",
								"Pin Constraint",
								"Start Spaghetti Factory",
								"Load Configuration",
								"Save Configuration",
								"Create Bubble",
								"Add Fluid",
						};
	    guiFrame = new JFrame("Tasks");
	    guiFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    //guiFrame.setLayout(new SpringLayout());
	    guiFrame.setLayout(new GridLayout(action_names.length,1));

	    this.buttons = createRadioButtons(action_names, 1, buttonGroup);

	    guiFrame.setSize(200,200);
	    guiFrame.pack();
	    guiFrame.setVisible(true);

	    task = new CreateParticleTask();
	}

	/** Simulate then display particle system and any builder
	 * adornments. */
	void simulateAndDisplayScene(GL gl)
	{
	    /// TODO: OVERRIDE THIS INTEGRATOR (Doesn't use Force objects properly)
	    if(simulate) {

		/// MULTIPLE STEPS OF SIZE DT (different from Assignment#1)
		int    nSteps = N_STEPS_PER_FRAME;
		double dt     = DT;
		avg_number_of_iterations = 0;
		max_number_of_iterations = 0;
		for(int k=0; k<nSteps; k++) {
		    if(spaghettiFactory!=null) {/// CREATE SPAGHETTI PARTICLES/FORCES, ETC.
			spaghettiFactory.advanceTime(DT);
		    }
		    PS.advanceTime(dt);///

			int	number_of_iterations = PS.getNumberOfIterations();
			avg_number_of_iterations += number_of_iterations;
			if(number_of_iterations > max_number_of_iterations)
				max_number_of_iterations = number_of_iterations;
		}
		avg_number_of_iterations /= nSteps;

		/// EDGE-EDGE VALIDITY CHECK:
		if(PS.hasOverlappingSprings()) {
		    System.out.println("\n ############### OVERLAP DETECTED!!!!!!!!! \n");
		    simulate = false;
		    bgColor.set(0.3f,0,0);
		}

	    }

	    // Draw particles, springs, etc.
	    PS.display(gl);

	    if(spaghettiFactory!=null) spaghettiFactory.display(gl, width, height);

	    // Display Task, e.g., currently drawn spring.
	    if(task != null) task.display(gl);
	}

	/**
	 * ActionListener implementation to manage Task selection
	 * using (radio) buttons.
	 */
	class TaskSelector implements ActionListener
	{
	    /** 
	     * Resets ParticleSystem to undeformed/material state,
	     * disables the simulation, and removes the active Task.
	     */
	    void resetToRest() {
		if(task != null)  task.reset();
		task = null;

		PS.reset();//synchronized
		if(spaghettiFactory!=null) spaghettiFactory.reset();

		simulate = false;
		bgColor.set(0,0,0);
	    }

	    /** Creates new Task objects to handle specified button action.  */
	    public void actionPerformed(ActionEvent e)
	    {
		String cmd = e.getActionCommand();
		System.out.println(cmd);

		if(cmd.equals("Reset")) {
			resetToRest();
		}
		else if(cmd.equals("Create Particle")){
		    task = new CreateParticleTask();
		}
		else if(cmd.equals("Delete Particle")){
		    task = new DeleteParticleTask();
		}
		else if(cmd.equals("Move Particle")){
			resetToRest();
		    task = new MoveParticleTask();
		}
		else if(cmd.equals("Create Spring")){
		    task = new CreateSpringTask();
		}
		else if(cmd.equals("Create Hair")){
		    task = new CreateHairTask();
		}
		else if(cmd.equals("Draw a Hair")){
		    task = new DrawHairTask();
		}
		else if(cmd.equals("Pin Constraint")){
		    task = new PinConstraintTask();
		}
		else if(cmd.equals("Start Spaghetti Factory")) {
		    task = null;
		    resetToRest();
		    PS = new ParticleSystem();
		    spaghettiFactory = new SpaghettiFactory(PS);/// Mmmm...
		    simulate = true;
		}
		else if(cmd.equals("Load Configuration")) {
			resetToRest();
			PS.loadConfiguration("conf.txt");
		}
		else if(cmd.equals("Save Configuration")) {
			PS.saveConfiguration("conf.txt");
		}
		else if(cmd.equals("Create Bubble")) {
			task = new CreateBubbleTask(true);
		}
		else if(cmd.equals("Add Fluid")) {
			task = new AddFluidTask();
		}
		else if(cmd.equals("Drag Particle")) {
			task = new DragParticleTask();
		}
// 		else if(cmd.equals("Rigid Constraint")){
// 		    task = new RigidConstraintTask();
// 		}
		else {
		    System.out.println("UNHANDLED ActionEvent: "+e);
		}
	    }

	    
	}


	// Methods required for the implementation of MouseListener
	public void mouseEntered (MouseEvent e) { if(task!=null) task.mouseEntered(e);  }
	public void mouseExited  (MouseEvent e) { if(task!=null) task.mouseExited(e);   }
	public void mousePressed (MouseEvent e) { if(task!=null) task.mousePressed(e);  }
	public void mouseReleased(MouseEvent e) { if(task!=null) task.mouseReleased(e); }
    	public void mouseClicked (MouseEvent e) { if(task!=null) task.mouseClicked(e);  }

 	// Methods required for the implementation of MouseMotionListener
 	public void mouseDragged (MouseEvent e) { if(task!=null) task.mouseDragged(e);  }
 	public void mouseMoved   (MouseEvent e) { if(task!=null) task.mouseMoved(e);    }

	/**
	 * Handles keyboard events, e.g., spacebar toggles
	 * simulation/pausing, and escape resets the current Task.
	 */
	public void dispatchKey(char key, KeyEvent e)
	{
	    //System.out.println("CHAR="+key+", keyCode="+e.getKeyCode()+", e="+e);
	    if(key == ' ') {//SPACEBAR --> TOGGLE SIMULATE
		simulate = !simulate;
		if(simulate) {
		    task = new DragParticleTask();
		}
		else {
		    task = null;
		}
	    }
	    else if (key == 'r') {
		//System.out.println("ESCAPE");

		if(task != null)  task.reset();

		//Task lastTask = task;
		taskSelector.resetToRest();//sets task=null;
		buttons[0].doClick();
	    }
	    else if (key == 'e') {//toggle exporter
		frameExporter = ((frameExporter==null) ? (new FrameExporter()) : null);
		System.out.println("'e' : frameExporter = "+frameExporter);
	    }
	    else if (key == '=') {//increase nsteps
		N_STEPS_PER_FRAME = Math.max((int)(1.05*N_STEPS_PER_FRAME), N_STEPS_PER_FRAME+1);
		System.out.println("N_STEPS_PER_FRAME="+N_STEPS_PER_FRAME+
				   ";  dt="+(float)DT+";  dtFrame="+(float)(DT*N_STEPS_PER_FRAME));
	    }
	    else if (key == '-') {//decrease nsteps
		int n = Math.min((int)(0.95*N_STEPS_PER_FRAME), N_STEPS_PER_FRAME-1);
		N_STEPS_PER_FRAME = Math.max(1, n);
		System.out.println("N_STEPS_PER_FRAME="+N_STEPS_PER_FRAME+
				   ";  dt="+(float)DT+";  dtFrame="+(float)(DT*N_STEPS_PER_FRAME));
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
		Point2d x0 = getPoint2d(e);
		Particle lastCreatedParticle = PS.createParticle(x0, false);
	    }
	    void reset() {}
	}

	/** Clicking task that deletes particles. */
	class DeleteParticleTask extends Task  
	{
	    public void mousePressed (MouseEvent e) 
	    {
		Particle deletedParticle = PS.getNearestParticle(getPoint2d(e));
		if(deletedParticle==null) return;
		PS.removeParticle(deletedParticle);
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
		Point2d cursorP = getPoint2d(e);//cursor position
		moveParticle = PS.getNearestParticle(cursorP);

		/// START MOVING (+ HIGHLIGHT):
		updatePosition(cursorP);
	    }
	    /** Update moved particle state. */
	    private void updatePosition(Point2d newX)
	    {
		if(moveParticle==null) return;
		moveParticle.setHighlight(true);
		moveParticle.x. set(newX);
		moveParticle.x0.set(newX);
	    }
	    /** Update particle. */
	    public void mouseDragged(MouseEvent e)
	    {
		Point2d cursorP = getPoint2d(e);//cursor position 
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
	    private Point2d  cursorP = null;

	    CreateSpringTask() {}

	    /** Start making a spring from the nearest particle. */
	    public void mousePressed(MouseEvent e) 
	    {
		// FIND NEAREST PARTICLE:
		cursorP = getPoint2d(e);//cursor position
		p1 = PS.getNearestParticle(cursorP); /// = constant (since at rest)
		p2 = null;
	    }

	    /** Update cursor location for display */
	    public void mouseDragged(MouseEvent e)
	    {
		cursorP = getPoint2d(e);//update cursor position
	    }

	    /** Find nearest particle, and create a
	     * SpringForce2Particle when mouse released, unless
	     * nearest particle, p2, is same as p1. */
	    public void mouseReleased(MouseEvent e) 
	    {
		cursorP = getPoint2d(e);//cursor position
		p2      = PS.getNearestParticle(cursorP); /// = constant (since at rest)
		if(p1 != p2) {//make force object
		    SpringForce2Particle newForce = new SpringForce2Particle(p1, p2, Constants.STIFFNESS_STRETCH, PS);//params
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
		gl.glVertex2d(cursorP.x, cursorP.y);
		gl.glVertex2d(p1.x.x, p1.x.y);
		gl.glEnd();
	    }
	}


	/** Runtime dragging of nearest particle using a spring
	 * force. */
	class DragParticleTask extends Task  
	{
	    private Particle dragParticle = null;
	    private Point2d  cursorP      = null;

	    private SpringForce1Particle springForce = null;

	    public void mousePressed(MouseEvent e) 
	    {
		// FIND NEAREST PARTICLE:
		cursorP = getPoint2d(e);//cursor position
		//dragParticle = PS.getNearestParticle(cursorP);
		dragParticle = PS.getNearestPinnedParticle(cursorP, false);//unpinned

		if(dragParticle != null) {/// START APPLYING FORCE:
		    springForce = new SpringForce1Particle(dragParticle, cursorP, PS);
		    PS.addForce(springForce);//to be removed later
		}
	    }

	    /** Cancel any particle dragging and forces. */
	    void reset() {
		dragParticle = null;
		cursorP      = null;
		if(springForce != null)  PS.removeForce(springForce);
	    }

	    public void mouseDragged(MouseEvent e)
	    {
		if(springForce != null) {
		    cursorP = getPoint2d(e);//cursor position 

		    /// UPDATE DRAG FORCE ANCHOR:
		    springForce.updatePoint(cursorP);
		}
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
		Particle p2 = PS.createParticle(getPoint2d(e), false);
		if(hairParticles.size() > 0) {/// ADD STRETCH SPRING p1-p2:
		    Particle p1 = hairParticles.get(hairParticles.size()-1);
		    PS.addForce(new SpringForce2Particle(p1, p2, Constants.STIFFNESS_STRETCH, PS));

		    if(hairParticles.size() > 1) {/// ADD BENDING SPRING TO p0-p1-p2
			Particle p0 = hairParticles.get(hairParticles.size()-2);
			PS.addForce(new SpringForceBending(p0, p1, p2, Constants.STIFFNESS_BEND, PS));
		    }
		}
		hairParticles.add(p2);//finally add new particle to list
	    }
	    public void mouseDragged(MouseEvent e) {}
	    public void mouseReleased(MouseEvent e) {}
	    public void display(GL gl) {}
	    void reset() { hairParticles.clear(); }
	}

	/** Draw hair task. */
	class DrawHairTask extends Task  
	{
	    ArrayList<Particle> hairParticles = new ArrayList<Particle>();

	    /** Create new particle. */
	    public void mousePressed(MouseEvent e) 
	    {
			Particle p2 = PS.createParticle(getPoint2d(e), false, new Vector2d(0, 0), Constants.PARTICLE_MASS);
			hairParticles.add(p2);//finally add new particle to list
	    }
	    public void mouseDragged(MouseEvent e) 
	    {
		if(hairParticles.size()==0) throw new RuntimeException("hairParticles.size() was zero... HOW?!?");

		Particle p1 = hairParticles.get(hairParticles.size()-1);

		Point2d x = getPoint2d(e);
		if(x.distance(p1.x0) < 0.04)  
		    return;

		/// OTHERWISE ADD A PARTICLE
		Particle p2 = PS.createParticle(x, false, new Vector2d(0, 0), Constants.PARTICLE_MASS);
		
		/// ADD STRETCH SPRING p1-p2:
		PS.addForce(new SpringForce2Particle(p1, p2, Constants.STIFFNESS_STRETCH, PS));

		if(hairParticles.size() > 1) {/// ADD BENDING SPRING TO p0-p1-p2
		    Particle p0 = hairParticles.get(hairParticles.size()-2);
		    PS.addForce(new SpringForceBending(p0, p1, p2, Constants.STIFFNESS_BEND, PS));
		}

		hairParticles.add(p2);//finally add new particle to list
	    }
	    public void mouseReleased(MouseEvent e) { hairParticles.clear(); }
	    public void display(GL gl) {}
	    void reset() { hairParticles.clear(); }
	}

	/** Toggle pin constraints. */
	class PinConstraintTask extends Task  
	{
	    /** Toggle pin constraint on nearest particle. */
	    public void mousePressed(MouseEvent e) 
	    {
		Point2d  cursorP = getPoint2d(e);
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

	/** Creates bubbles. */
	class CreateBubbleTask extends Task {
		
		final private double	bubble_size = 0.1;
		/** Number of edges(vertices) in a bubble. At least 3 */
		final private int		bubble_edges = 15;

		private Point2d			cursor_p = null;
		private boolean			with_fluid;

		public CreateBubbleTask (boolean with_fluid) {
			this.with_fluid = with_fluid;
		}

	    /** Not used */
	    public void mousePressed(MouseEvent e) {
			cursor_p = getPoint2d(e);
		}

	    /** Not used */
	    public void mouseDragged(MouseEvent e) {
			cursor_p = getPoint2d(e);
		}

	    /** Create a bubble if there is no collision */
	    public void mouseReleased(MouseEvent e) {
			final double	mass = 5.0,
							fluid_mass = 3.0;
			Point2d	vertices[] = calculateVertices(cursor_p);
			if(checkValidity(vertices)) {
				Particle	p0 = PS.createParticle(vertices[0], false, new Vector2d(0, 0), mass),
							p1 = PS.createParticle(vertices[1], false, new Vector2d(0, 0), mass);
				PS.addForce(new SpringForce2Particle(p0, p1, Constants.BUBBLE_STIFFNESS_STRETCH, PS));
				Particle	a = p0, b = p1;
				for(int i=2; i<bubble_edges; i++) {
					Particle c = PS.createParticle(vertices[i], false, new Vector2d(0, 0), mass);
					PS.addForce(new SpringForce2Particle(b, c, Constants.BUBBLE_STIFFNESS_STRETCH, PS));
					PS.addForce(new SpringForceBending(a, b, c, Constants.BUBBLE_STIFFNESS_BEND, PS));
					a = b;
					b = c;
				}
				PS.addForce(new SpringForce2Particle(b, p0, Constants.BUBBLE_STIFFNESS_STRETCH, PS));
				PS.addForce(new SpringForceBending(a, b, p0, Constants.BUBBLE_STIFFNESS_BEND, PS));
				PS.addForce(new SpringForceBending(b, p0, p1, Constants.BUBBLE_STIFFNESS_BEND, PS));

				// Add fluid
				if(with_fluid) {
					final int		number_of_fluid_particles = 20;
					final double	radius = bubble_size / 2;
					final double	d = 0.015;
					int	count = 0;

					for(double y = cursor_p.y-radius+d; y < cursor_p.y+radius-d; y += d) {
						for(double x = cursor_p.x-radius+d; x < cursor_p.x+radius-d; x += d) {
							if(count++ < number_of_fluid_particles &&
									(x-cursor_p.x)*(x-cursor_p.x) + (y-cursor_p.y)*(y-cursor_p.y) < radius*radius*0.9)
								PS.createParticle(new Point2d(x, y), true, new Vector2d(0, 0), fluid_mass);
						}
					}

					/*for(int i=0; i<number_of_fluid_particles; i++) {
						double	r = Math.random()*radius*0.9, theta = Math.random()*Math.PI*2;
						PS.createParticle(new Point2d(cursor_p.x+r*Math.sin(theta), cursor_p.y+r*Math.cos(theta)), true, new Vector2d(0, 0), fluid_mass);
					}*/
				}
			}
			cursor_p = null;
		}

	    /** Cancel */
	    public void reset() {
			cursor_p = null;
	    }

		/** Compute the positions of bubble vertices */
		private Point2d[] calculateVertices (Point2d center) {
			Point2d	vertices[] = new Point2d[bubble_edges];
			for(int i=0; i<bubble_edges; i++) {
				final double	theta = 2 * Math.PI * i / bubble_edges,
								radius = bubble_size / 2;
				vertices[i] = new Point2d(center.x+radius*Math.sin(theta), center.y+radius*Math.cos(theta));
			}
			return vertices;
		}

		/** Check if the bubble-to-be overlaps any edge */
		private boolean checkValidity (Point2d vertices[]) {
			for(int i=0; i<bubble_edges-1; i++) {
				if(PS.isOverlappingSprings(vertices[i], vertices[i+1]))
					return false;
			}
			if(PS.isOverlappingSprings(vertices[bubble_edges-1], vertices[0]))
				return false;
			return true;
		}

	    /** Draw the bubble-to-be */
	    public void display(GL gl) {
			if(cursor_p == null)
				return;
			Point2d	vertices[] = calculateVertices(cursor_p);
			gl.glBegin(GL.GL_LINE_STRIP);
			if(checkValidity(vertices))
				gl.glColor3f(0.0f, 1.0f, 0.0f);
			else
				gl.glColor3f(1.0f, 0.0f, 0.0f);
			for(int i=0; i<bubble_edges; i++)
				gl.glVertex2d(vertices[i].x, vertices[i].y);
			gl.glVertex2d(vertices[0].x, vertices[0].y);
			gl.glEnd();
	    }
	}

	/** Add fluid particles to the system. */
	class AddFluidTask extends Task {

		private Point2d		cursor_p = null;
		private double		tick = 0;

		public AddFluidTask () {}

	    /** Start adding particles */
	    public void mousePressed(MouseEvent e) {
			cursor_p = getPoint2d(e);
		}

	    /** Start adding particles (to different position) */
	    public void mouseDragged(MouseEvent e) {
			cursor_p = getPoint2d(e);
		}

	    /** Stop adding particles */
	    public void mouseReleased(MouseEvent e) {
			cursor_p = null;
		}

	    /** Cancel */
	    public void reset() {
			cursor_p = null;
	    }

		/** I use this function to add particles since it's called every frame */
	    public void display(GL gl) {
			if(cursor_p == null)
				return;
			final double	margin = 0.05,
							rate = 2,
							cur_tick = tick;
			tick += rate;
			for(int i=0; i<(int)(tick-cur_tick); i++) {
				PS.createParticle(new Point2d(cursor_p.x+(Math.random()*2-1)*margin, cursor_p.y+(Math.random()*2-1)*margin), true);
			}
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
    private class FrameExporter
    {
	private int nFrames  = 0;

	FrameExporter()  { 
	    exportId += 1;
	}

	void writeFrame()
	{ 
		long   timeNS   = -System.nanoTime();
	    String number   = Utils.getPaddedNumber(nFrames, 5, "0");
	    String filename = "frames/export"+exportId+"-"+number+".png";/// BUG: DIRECTORY MUST EXIST!

	    try{  
		java.io.File   file     = new java.io.File(filename);
		if(file.exists()) System.out.println("WARNING: OVERWRITING PREVIOUS FILE: "+filename);

		/// WRITE IMAGE: ( :P Screenshot asks for width/height --> cache in GLEventListener.reshape() impl)
		com.sun.opengl.util.Screenshot.writeToFile(file, width, height);

		timeNS += System.nanoTime();
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

