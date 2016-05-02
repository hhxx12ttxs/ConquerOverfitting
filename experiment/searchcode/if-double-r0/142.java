package artoo;

import com.sun.opengl.util.Animator;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.event.*;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLJPanel;
import com.sun.opengl.util.texture.*;
import java.io.*;
import java.util.* ;
import Jama.*;

import java.io.File;

/**
 * ModelViewer.java <BR>
 * author: Brian Paul (converted to Java by Ron Cemer and Sven Goethel) <P>
 *
 * This version is equal to Brian Paul's version 1.2 1999/10/21
 */
public class ModelViewer implements GLEventListener, MouseListener, MouseWheelListener, MouseMotionListener {

	public static void main(String[] args) {
		File dir1 = new File (".");
		try {
			System.out.println ("Current dir : " + dir1.getCanonicalPath());
		} catch (Exception e) {
		}

		Frame frame = new Frame("Model Viewer");
		GLCanvas canvas = new GLCanvas();

		canvas.addGLEventListener(new ModelViewer());
		frame.add(canvas);
		frame.setSize(640, 480);
		final Animator animator = new Animator(canvas);
		frame.addWindowListener(new WindowAdapter() {

			@Override
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
		// Center frame
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		animator.start();
	}

	private float	view_rotx = 0.0f, view_roty = 0.0f, view_rotz = 0.0f;
	private float	view_zpos = 20.0f;
	private int		gear1, axis;
	private float	angle = 0.0f;

	// Frame-per-second
	private DurationTimer	timer = new DurationTimer();
	private boolean			firstRender = true;
	private int				frameCount;

	// Cameras
	private Vector<Camera>	cameras;
	private	Matrix			average_point;

	// Mouse-dragging
	private int		prevMouseX, prevMouseY;
	private boolean	mouseRButtonDown = false;

	public void init(GLAutoDrawable drawable) {
		// Use debug pipeline
		// drawable.setGL(new DebugGL(drawable.getGL()));

		GL gl = drawable.getGL();
		System.err.println("INIT GL IS: " + gl.getClass().getName());
		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

		// Enable VSync
		gl.setSwapInterval(1);

		// Setup the drawing area and shading mode
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glShadeModel(GL.GL_SMOOTH); // try setting this to GL_FLAT and see what happens.

		// Some data
		float light_pos[] = { 5.0f, 5.0f, 10.0f, 0.0f };
		float red[] = { 0.8f, 0.1f, 0.0f, 1.0f };
		float green[] = { 0.0f, 0.8f, 0.2f, 1.0f };
		float blue[] = { 0.2f, 0.2f, 1.0f, 1.0f };

		// Add light
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light_pos, 0);
		gl.glEnable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_LIGHT0);
		gl.glEnable(GL.GL_DEPTH_TEST);

		// Build cameras
		/*double	rotation_mat[][] = {
			{9.3091418937e-01, -2.9456222286e-01, -2.1594413374e-01},
			{3.1844298092e-01, 3.6504081392e-01, 8.7483671166e-01},
			{-1.7886542409e-01, -8.8316380193e-01, 4.3362294569e-01},
		};
		double	translation[] = {-1.3706584569e+00, 5.5183406351e+00, -3.7536523238e+00};
		cameras.add(new Camera(gl, rotation_mat, translation, 677.7, 640, 480));*/
		try {
			BundleFileInputStream	bundle_file = new BundleFileInputStream(gl, "bundle/bundle.out", "list_tmp.txt");
			cameras = bundle_file.getCameras();
			average_point = bundle_file.getAveragePoint();
		} catch (IOException e) {
			System.err.printf("Exception: %s\n", e.getMessage());
		}

		// TODO: Remove this
		gear1 = gl.glGenLists(1);
		gl.glNewList(gear1, GL.GL_COMPILE);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, red, 0);
		gear(gl, 1.3f, 2.0f, 0.5f, 10, 0.7f);
		gl.glEndList();

		// Axis
		axis = gl.glGenLists(1);
		gl.glNewList(axis, GL.GL_COMPILE);
		gl.glBegin(GL.GL_LINES);
		gl.glColor3f(0.8f, 0.1f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(1.0f, 0.0f, 0.0f);
		gl.glColor3f(0.0f, 0.8f, 0.2f);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 1.0f, 0.0f);
		gl.glColor3f(0.2f, 0.2f, 1.0f);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, 1.0f);
		gl.glEnd();
		gl.glEndList();

		// TODO: Not sure this is needed
		gl.glEnable(GL.GL_NORMALIZE);

		// Alpha blending
		//gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		drawable.addMouseListener(this);
		drawable.addMouseMotionListener(this);
		drawable.addMouseWheelListener(this);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL gl = drawable.getGL();

		double ratio = (double)height / (double)width;
		System.out.printf("reshape(): width = %d, height = %d, h = %.2f\n", width, height, ratio);

		gl.glMatrixMode(GL.GL_PROJECTION);

		System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
		gl.glLoadIdentity();
		// Currently I assume a 35mm lens on 135 film (36x24mm), xfov = 39.6deg
		//double	view_focal_len = 76, film_width = 36.0;
		double	view_focal_len = 1400, film_width = 615;
		System.err.printf("Focal length = %.2fmm (135 film), xfov = %.2fdeg\n", view_focal_len/film_width*36, Math.atan(film_width/2/view_focal_len)*180/Math.PI*2);
		gl.glFrustum(-film_width/2/view_focal_len,
				film_width/2/view_focal_len,
				-film_width/2/view_focal_len*ratio,
				film_width/2/view_focal_len*ratio,
				1.0, 1600.0);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public void display(GLAutoDrawable drawable) {
		if (!firstRender) {
			if (++frameCount == 30) {
				timer.stop();
				//System.err.println("Frames per second: " + (30.0f / timer.getDurationAsSeconds()));
				timer.reset();
				timer.start();
				frameCount = 0;
			}
		} else {
			firstRender = false;
			timer.start();
		}

		// Used to control speed ...
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			throw new GLException(e);
		}

		GL gl = drawable.getGL();

		// Special handling for the case where the GLJPanel is translucent
		// and wants to be composited with other Java 2D content
		// TODO: Not sure this is needed
		if ((drawable instanceof GLJPanel) &&
				!((GLJPanel) drawable).isOpaque() &&
				((GLJPanel) drawable).shouldPreserveColorBufferIfTranslucent()) {
			gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		} else {
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		}

		// Rotate the entire world based on how the user
		// dragged the mouse around
		// Note: everything is reversed since we are moving the world instead of the camera itself
		// See getCameraDir()
		gl.glPushMatrix();
		// This is a matrix stack, so the first matrix is multipled last
		// See getCameraDir()
		gl.glTranslatef(0.0f, 0.0f, -view_zpos);
		gl.glRotatef(-view_rotz, 0.0f, 0.0f, 1.0f);
		gl.glRotatef(-view_rotx, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(-view_roty, 0.0f, 1.0f, 0.0f);
		// Following translation move the rotation center of the camera to the average point
		// Note: we in fact move the world instead of moving the camera, so everything is reversed
		gl.glTranslated(-average_point.getArray()[0][0], -average_point.getArray()[1][0], -average_point.getArray()[2][0]);

		// Place the third gear and call its display list
		/*gl.glEnable(GL.GL_LIGHTING);
		gl.glPushMatrix();
		gl.glCallList(gear1);
		gl.glPopMatrix();
		gl.glDisable(GL.GL_LIGHTING);*/

		// Place the image plane
		// TODO: Make update a function called by any function that change the camera
		Matrix	view_cam_dir = getCameraDir();
		double	max_similarity = Double.NEGATIVE_INFINITY;
		int		max_id = -1;

		for(int i=0; i<cameras.size(); i++) {
			Camera	camera = cameras.elementAt(i);

			Matrix	cam_dir = camera.getCameraDir(average_point);
			double	similarity = cam_dir.dotProduct(view_cam_dir);

			//System.out.printf("%d: %.2f\n", i+1, similarity);
			if(similarity > max_similarity) {
				max_similarity = similarity;
				max_id = i;
			}

			/*double	alpha = Math.max((similarity - 1.99) * 100, 0.0);
			cameras.elementAt(i).draw(gl, alpha);*/
		}
		//System.out.printf("Camera %d\n", max_id+1);
		cameras.elementAt(max_id).draw(gl, 1.0);
		for(int i=0; i<cameras.size(); i++) {
			cameras.elementAt(i).drawCamera(gl, max_id == i);
		}
		/*for(Enumeration e = cameras.elements(); e.hasMoreElements();) {
			Camera	camera = (Camera)e.nextElement();
			camera.draw(gl);
		}*/

		// Axis
		gl.glPushMatrix();
		gl.glCallList(axis);
		gl.glPopMatrix();

		// Remember that every push needs a pop; this one is paired with
		// rotating the entire world
		gl.glPopMatrix();

		// Flush all drawing operations to the graphics card
		// TODO: Not sure this is needed
		gl.glFlush();
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
	}

	// Methods required for the implementation of MouseListener
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
		prevMouseX = e.getX();
		prevMouseY = e.getY();
		if ((e.getModifiers() & e.BUTTON3_MASK) != 0) {
			mouseRButtonDown = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if ((e.getModifiers() & e.BUTTON3_MASK) != 0) {
			mouseRButtonDown = false;
		}
	}

	public void mouseClicked(MouseEvent e) {}

	// Methods required for the implementation of MouseMotionListener
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		Dimension size = e.getComponent().getSize();

		float thetaY = 360.0f * ( (float)(x-prevMouseX)/(float)size.width);
		float thetaX = 360.0f * ( (float)(prevMouseY-y)/(float)size.height);

		prevMouseX = x;
		prevMouseY = y;

		if(mouseRButtonDown) {
			view_rotz += 0.5*thetaX;
		} else {
			view_rotx += 0.5*thetaX;
			view_roty -= 0.5*thetaY;
		}
		// TODO: Remove
		//System.out.printf("rotx = %.2f\n", view_rotx);
	}

	public void mouseMoved(MouseEvent e) {}

	// Methods required for the implementation of MouseWheelListener
	public void mouseWheelMoved (MouseWheelEvent e) {
		view_zpos += e.getWheelRotation();
	}

	// TODO: Remove
	public static void gear(GL gl,
                          float inner_radius,
                          float outer_radius,
                          float width,
                          int teeth,
                          float tooth_depth)
	{
		int i;
		float r0, r1, r2;
		float angle, da;
		float u, v, len;

		r0 = inner_radius;
		r1 = outer_radius - tooth_depth / 2.0f;
		r2 = outer_radius + tooth_depth / 2.0f;

		da = 2.0f * (float) Math.PI / teeth / 4.0f;

		gl.glShadeModel(GL.GL_FLAT);

		gl.glNormal3f(0.0f, 0.0f, 1.0f);

		/* draw front face */
		gl.glBegin(GL.GL_QUAD_STRIP);
		for (i = 0; i <= teeth; i++)
		  {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), width * 0.5f);
			gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), width * 0.5f);
			if(i < teeth)
			  {
				gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), width * 0.5f);
				gl.glVertex3f(r1 * (float)Math.cos(angle + 3.0f * da), r1 * (float)Math.sin(angle + 3.0f * da), width * 0.5f);
			  }
		  }
		gl.glEnd();

		/* draw front sides of teeth */
		gl.glBegin(GL.GL_QUADS);
		for (i = 0; i < teeth; i++)
		  {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), width * 0.5f);
			gl.glVertex3f(r2 * (float)Math.cos(angle + da), r2 * (float)Math.sin(angle + da), width * 0.5f);
			gl.glVertex3f(r2 * (float)Math.cos(angle + 2.0f * da), r2 * (float)Math.sin(angle + 2.0f * da), width * 0.5f);
			gl.glVertex3f(r1 * (float)Math.cos(angle + 3.0f * da), r1 * (float)Math.sin(angle + 3.0f * da), width * 0.5f);
		  }
		gl.glEnd();

		/* draw back face */
		gl.glBegin(GL.GL_QUAD_STRIP);
		for (i = 0; i <= teeth; i++)
		  {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), -width * 0.5f);
			gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), -width * 0.5f);
			gl.glVertex3f(r1 * (float)Math.cos(angle + 3 * da), r1 * (float)Math.sin(angle + 3 * da), -width * 0.5f);
			gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), -width * 0.5f);
		  }
		gl.glEnd();

		/* draw back sides of teeth */
		gl.glBegin(GL.GL_QUADS);
		for (i = 0; i < teeth; i++)
		  {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glVertex3f(r1 * (float)Math.cos(angle + 3 * da), r1 * (float)Math.sin(angle + 3 * da), -width * 0.5f);
			gl.glVertex3f(r2 * (float)Math.cos(angle + 2 * da), r2 * (float)Math.sin(angle + 2 * da), -width * 0.5f);
			gl.glVertex3f(r2 * (float)Math.cos(angle + da), r2 * (float)Math.sin(angle + da), -width * 0.5f);
			gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), -width * 0.5f);
		  }
		gl.glEnd();

		/* draw outward faces of teeth */
		gl.glBegin(GL.GL_QUAD_STRIP);
		for (i = 0; i < teeth; i++)
		  {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), width * 0.5f);
			gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), -width * 0.5f);
			u = r2 * (float)Math.cos(angle + da) - r1 * (float)Math.cos(angle);
			v = r2 * (float)Math.sin(angle + da) - r1 * (float)Math.sin(angle);
			len = (float)Math.sqrt(u * u + v * v);
			u /= len;
			v /= len;
			gl.glNormal3f(v, -u, 0.0f);
			gl.glVertex3f(r2 * (float)Math.cos(angle + da), r2 * (float)Math.sin(angle + da), width * 0.5f);
			gl.glVertex3f(r2 * (float)Math.cos(angle + da), r2 * (float)Math.sin(angle + da), -width * 0.5f);
			gl.glNormal3f((float)Math.cos(angle), (float)Math.sin(angle), 0.0f);
			gl.glVertex3f(r2 * (float)Math.cos(angle + 2 * da), r2 * (float)Math.sin(angle + 2 * da), width * 0.5f);
			gl.glVertex3f(r2 * (float)Math.cos(angle + 2 * da), r2 * (float)Math.sin(angle + 2 * da), -width * 0.5f);
			u = r1 * (float)Math.cos(angle + 3 * da) - r2 * (float)Math.cos(angle + 2 * da);
			v = r1 * (float)Math.sin(angle + 3 * da) - r2 * (float)Math.sin(angle + 2 * da);
			gl.glNormal3f(v, -u, 0.0f);
			gl.glVertex3f(r1 * (float)Math.cos(angle + 3 * da), r1 * (float)Math.sin(angle + 3 * da), width * 0.5f);
			gl.glVertex3f(r1 * (float)Math.cos(angle + 3 * da), r1 * (float)Math.sin(angle + 3 * da), -width * 0.5f);
			gl.glNormal3f((float)Math.cos(angle), (float)Math.sin(angle), 0.0f);
		  }
		gl.glVertex3f(r1 * (float)Math.cos(0), r1 * (float)Math.sin(0), width * 0.5f);
		gl.glVertex3f(r1 * (float)Math.cos(0), r1 * (float)Math.sin(0), -width * 0.5f);
		gl.glEnd();

		gl.glShadeModel(GL.GL_SMOOTH);

		/* draw inside radius cylinder */
		gl.glBegin(GL.GL_QUAD_STRIP);
		for (i = 0; i <= teeth; i++)
		  {
			angle = i * 2.0f * (float) Math.PI / teeth;
			gl.glNormal3f(-(float)Math.cos(angle), -(float)Math.sin(angle), 0.0f);
			gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), -width * 0.5f);
			gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), width * 0.5f);
		  }
		gl.glEnd();
	}

	// Get the effective camera direction (the vector from average point center
	// to the camera) in the world coord.
	private Matrix getCameraDir () {
		// We move the camera by following transformations:
		// cam_pos = (Tavg * Ry * Rx * Rz * Tc) * [0; 0; 0]
		// Which means: move the camera to Tc (which should be a positive position on z-axis),
		// rotate it along z-axis (roll), then x-axis (pitch), then y-axis (yaw),
		// and finally we move it to Tavg (the face the average center of the object).
		// However, in implementation we "move" the camera by transform the whole world,
		// so everything is reversed
		// Here, I want to calculate the "imaginary" camera direction, so we use the transformation,
		// not reversed one. Note that I only need the direction (from average point center to the camera)
		// so no translation is needed

		// TODO: Test
		// Note: the matrix here is in transposed form since Matrix constructor use column-packed array
		double	roty_mat_data[] = {
					Math.cos(view_roty/180*Math.PI), 0,	-Math.sin(view_roty/180*Math.PI), 0,
					0, 1, 0, 0,
					Math.sin(view_roty/180*Math.PI), 0, Math.cos(view_roty/180*Math.PI), 0,
					0, 0, 0, 1 },
				rotx_mat_data[] = {
					1, 0, 0, 0,
					0, Math.cos(view_rotx/180*Math.PI), Math.sin(view_rotx/180*Math.PI), 0,
					0, -Math.sin(view_rotx/180*Math.PI), Math.cos(view_rotx/180*Math.PI), 0,
					0, 0, 0, 1 };
		double	cam_dir_data[] = {0, 0, 1, 1};
		Matrix	roty_mat = new Matrix(roty_mat_data, 4),
				rotx_mat = new Matrix(rotx_mat_data, 4),
				cam_dir = new Matrix(cam_dir_data, 4),
				cur_dir = roty_mat.times(rotx_mat.times(cam_dir));	// Multiply Rx first, then Ry
		// TODO: Remove
		/*gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(0.0, 0.0, 0.0);
		gl.glVertex3dv(cur_dir.getColumnPackedCopy(), 0);
		gl.glEnd();*/
		//System.out.printf("%.1f, %.1f, %.1f\n", cur_dir.get(0, 0), cur_dir.get(1, 0), cur_dir.get(2, 0));

		return cur_dir;
	}
}


