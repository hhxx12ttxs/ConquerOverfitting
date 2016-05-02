// ****************************************************************************
// Example Main Program for CS480 Programming Assignment 1
// ****************************************************************************
// Description:
//   
// This is a template program for the polygon tool. It supports drawing a
// polygon vertices, moving vertices, reporting whether the polygon is
// concave or convex, and testing points are inside/outside the current
// polygon.
//
// LEFTMOUSE: add polygon vertex
// RIGHTMOUSE: move closest vertex
// MIDDLEMOUSE: click to see if point is inside or outside polygon
//
// The following keys control the program:
//
// Q,q: quit
// ESC: reset
// T,t: cycle through test cases
// F,f: toggle polygon fill off/on
// 
// S,s: toggle applying inside outside test on all pixels
//
// ****************************************************************************
// History :
// 9 Jan 2013 - updated by Jianming Zhang
// 7 February 2011 - updated code to be more Java-y
// 9 Jan 2008 Created by Tai-Peng Tian (tiantp@gmail.com) based on the C
// code by Stan Sclaroff
//


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;//for new version of gl
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;//for new version of gl
import com.jogamp.opengl.util.gl2.GLUT;//for new version of gl

/**
 * The driver program which runs the polygon sketching tool.
 */
public class PA1 extends JFrame implements GLEventListener, KeyListener,
    MouseListener, MouseMotionListener {
  /** The default width of the lines used to draw polygons. */
  public static final float DEFAULT_LINE_WIDTH = 1.0f;
  /** The default height of the window. */
  public static final int DEFAULT_WINDOW_HEIGHT = 512;
  /** The default width of the window. */
  public static final int DEFAULT_WINDOW_WIDTH = 512;
  /** Randomly generated serial version UID. */
  private static final long serialVersionUID = 2585038734149359171L;
  
  /** Modes*/
  private static final int POLY_MODE=0;
  private static final int CIRC_MODE=1;
  private static final int INSI_MODE=2;
  /** Added in hole mode */
  private static final int HOLE_MODE=3;
  private static final int CIRCLE_HOLE_MODE=4;

  /**
   * Initializes the window and OpenGL components, then runs the animator.
   * 
   * @param args
   *          This parameter is ignored.
   */
  public static void main(String[] args) {
    final PA1 P = new PA1();
    P.run();
  }

  /** The animator used for animating the canvas. */
  FPSAnimator animator;
  /** The canvas used for drawing. */
  private GLCanvas canvas;
  /** The capabilities of this canvas. */
  private GLCapabilities capabilities = new GLCapabilities(null);
  /** Whether the mouse is currently dragging a selected vertex or 
   * the boundary of the circle*/
  private boolean dragging = false;
  /** Whether the mouse is currently dragging the center of the circle*/
  private boolean draggingCenter = false;
  /** Whether to fill the polygon using OpenGL. */
  private boolean fill = false;
  /** A GLU object. */
  private GLU glu = new GLU();
  /** A GLUT object. */
  private GLUT glut = new GLUT();
  /**
   * The message which displays whether a selected point is inside or outside
   * the polygon.
   */
  private String insideOutsideMessage = null;
  /** Whether to highlight pixels which are inside the polygon. */
  private boolean insideOutsideTest = false;
  
  /** Swith between polygon mode and circle mode*/
  private int mode = POLY_MODE;
  
  
  /** The polygon to draw on the canvas. */
  private Shape[] shapes = new Shape [2]; 
  private int selectedShapeIndex = 0;
  
 
  /** The polygon test cases. */
  private final TestCases testCases = new TestCases();

  /**
   * Initializes the OpenGL objects for GL capabilities, canvas, animators, etc.
   */
  private PA1() {
    // enables double buffering
    this.capabilities.setDoubleBuffered(true);
    this.capabilities.setStencilBits(8);
    
    this.canvas = new GLCanvas(this.capabilities);
    this.canvas.addGLEventListener(this);
    this.canvas.addMouseListener(this);
    this.canvas.addMouseMotionListener(this);
    this.canvas.addKeyListener(this);
    // auto swap buffer mode is true by default, but we are explicit here
    this.canvas.setAutoSwapBufferMode(true);
    this.getContentPane().add(this.canvas);

    // drive the display loop at 60 frames per second
    this.animator = new FPSAnimator(this.canvas, 60);

    this.setTitle("CS480/CS680 Skeleton Polygon Tool");
    this.setSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setVisible(true);
    
    canvas.setFocusable(true);
    canvas.requestFocusInWindow();
    
    // set up initial state of two polygons
    this.shapes[0] = new Polygon((float)1.0,(float)0.7,(float)0.5);
    this.shapes[1] = new Circle((float)0.5,(float)1.0,(float)0.2);
    this.selectedShapeIndex = 0;
    
  }

  /**
   * Draws the polygon, fills the polygon if necessary, and displays the
   * appropriate messages on the canvas.
   * 
   * @param drawable
   *          {@inheritDoc}
   */
  public void display(final GLAutoDrawable drawable) {
    final GL2 gl = (GL2) drawable.getGL();
    
    // check if we need to fill the polygon
    if (this.fill)
      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
    else
      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);

    // clear the display
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
    
    // check if we need to highlight pixels which are inside the polygon
    if (this.insideOutsideTest) {
      // push the current color
      gl.glPushAttrib(GL2.GL_CURRENT_BIT);
      gl.glPointSize(3);
      gl.glBegin(GL.GL_POINTS);   
      for (int y = 0; y < this.canvas.getHeight(); y++) {
        for (int x = 0; x < this.canvas.getWidth(); x++) {
          if (this.shapes[selectedShapeIndex].isInside(x, y))
            // the inside is red
            gl.glColor3f(1.0f, 0.0f, 0.0f);
          else
            // the outside is gray
            gl.glColor3f(0.0f, 0.0f, 0.0f);
          gl.glVertex2f(x, y);
        }
      }
      gl.glEnd();
      // pop the current color
      gl.glPopAttrib();
    }

    // render the polygon and the circle
    Drawer.draw(drawable, (Polygon)this.shapes[0], (Circle)this.shapes[1], false);   

    gl.glPopAttrib(); // pop attributes to restore
    
    //gl.glPushAttrib(GL2.GL_CURRENT_BIT); // push attributes to set scope of color changes
    if(((Polygon)this.shapes[0]).vertices().size()>2){
    	//color = this.shapes[0].getColor();
    	//gl.glColor3f(color.getRed(),color.getGreen(),color.getBlue());
    	
    	// display a message for concavity/convexity for each polygon
    	if (((Polygon)this.shapes[0]).isConcave()) 
    		drawString(drawable, 20, this.canvas.getHeight() - 60,
    				"The polygon is concave", GLUT.BITMAP_HELVETICA_18);
    	else
    		drawString(drawable, 20, this.canvas.getHeight() - 60,
    				"The polygon is convex", GLUT.BITMAP_HELVETICA_18);
    	
    	// display the result of inside/outside point test
        if (this.mode == INSI_MODE && this.insideOutsideMessage != null)
            drawString(drawable, 20, this.canvas.getHeight() - 20,
                this.insideOutsideMessage, GLUT.BITMAP_HELVETICA_18);
        // display the result of self-intersection test
        if (((Polygon)this.shapes[0]).hasSelfIntersection())
        	drawString(drawable, 20, this.canvas.getHeight() - 40,
    				"The polygon has self-intersection!!!!", GLUT.BITMAP_HELVETICA_18);
        else
        	drawString(drawable, 20, this.canvas.getHeight() - 40,
    				"The polygon does NOT have self-intersection", GLUT.BITMAP_HELVETICA_18);
        
        /* Display the result of Clockwise TEST */
        if (!((Polygon)this.shapes[0]).isCounterClockwise(((Polygon)this.shapes[0]).getVertices()))
        	drawString(drawable, this.canvas.getWidth()-60, this.canvas.getHeight() - 80,
    				"CW", GLUT.BITMAP_HELVETICA_18);
        else
        	drawString(drawable, this.canvas.getWidth()-60, this.canvas.getHeight() - 80,
    				"CCW", GLUT.BITMAP_HELVETICA_18);
    }
    
    
    // display the result for overlap detection
    if(((Polygon)this.shapes[0]).isOverlapping(((Circle)this.shapes[1])))
    	drawString(drawable, 20, this.canvas.getHeight() - 80,
    			"Shapes overlap!!!!!", GLUT.BITMAP_HELVETICA_18);
    else	
    	drawString(drawable, 20, this.canvas.getHeight() - 80,
    			"Shapes do not overlap", GLUT.BITMAP_HELVETICA_18);

    
    
  }

  /**
   * {@inheritDoc}
   * 
   * @param drawable
   *          {@inheritDoc}
   * @param modeChanged
   *          {@inheritDoc}
   * @param deviceChanged
   *          {@inheritDoc}
   */
  public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
      boolean deviceChanged) {
    // intentionally unimplemented
  }

  /**
   * Draws a String to the canvas.
   * 
   * @param drawable
   *          The drawable on which to draw the String.
   * @param x
   *          The x location to draw the String.
   * @param y
   *          The y location to draw the String.
   * @param s
   *          The String to draw.
   * @param font
   *          The font with which to draw the String, as chosen from GLUT
   *          constants.
   */
  private void drawString(final GLAutoDrawable drawable, final int x,
      final int y, final String s, final int font) {
    ((GL2) drawable.getGL()).glRasterPos2f(x, y);
    this.glut.glutBitmapString(font, s);
  }

  /**
   * {@inheritDoc}
   * 
   * @param drawable
   *          {@inheritDoc}
   */
  public void init(final GLAutoDrawable drawable) {
    final GL gl = drawable.getGL();
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    gl.glLineWidth(DEFAULT_LINE_WIDTH);
  }

  /**
   * {@inheritDoc}
   * 
   * @param key
   *          {@inheritDoc}
   */
  public void keyPressed(KeyEvent key) {
    switch (key.getKeyCode()) {
    case 'Q':
    case 'q':
      new Thread() {
        @Override
        public void run() {
          PA1.this.animator.stop();
        }
      }.start();
      System.exit(0);
      break;
    default:
      break;
    }
  }

  /**
   * This method is intentionally unimplemented.
   * 
   * @param key
   *          This parameter is ignored.
   */
  public void keyReleased(final KeyEvent key) {
    // intentionally unimplemented
  }

  /**
   * {@inheritDoc}
   * 
   * The following list summarizes the actions produced on the specified keys:
   * 
   * Q or q - quit the program T or t - cycle through the test cases F or f -
   * toggle OpenGL's polygon fill ESC - clear (i.e. reset to empty) the
   * current polygon S or s - highlight pixels which are inside the polygon
   * 
   * @param key
   *          {@inheritDoc}
   */
  public void keyTyped(final KeyEvent key) {
	  
	  /* If previously in hole mode, and a key was pressed, check to make sure the last whole
	   * had at least three points, otherwise, delete it 
	   */
	  if (this.mode == HOLE_MODE)
	  {
		  ((Polygon)this.shapes[0]).verifyHole();
	  }
	  
    switch (key.getKeyChar()) {
    
    case 'T':
    case 't':
    	((Polygon)this.shapes[0]).resetHole();
      this.shapes = this.testCases.next();
      break;

    case 'F':
    case 'f':
      this.fill = !this.fill;
      break;

    case 'R': 
    case 'r':
        // reset initial state of two polygons
      this.shapes[0] = new Polygon((float)1.0,(float)0.7,(float)0.5);
      this.shapes[1] = new Circle((float)0.5,(float)1.0,(float)0.2);
      this.selectedShapeIndex = 0;
      this.mode=POLY_MODE;
      break;

    case 'S':
    case 's':
      this.insideOutsideTest = !this.insideOutsideTest;
      break;
  
    case 'P':
    case 'p':
      this.selectedShapeIndex = 0;
      this.mode = POLY_MODE;
      break;
    	
    case 'C':
    case 'c':
      this.selectedShapeIndex = 1;
      this.mode = CIRC_MODE;
      break;
    	
    case 'I':
    case 'i':
      this.mode = INSI_MODE;
      
    case 'H':
    case 'h':
    	this.mode = HOLE_MODE;
    	((Polygon)this.shapes[0]).addHole();
    	break;
    case 'J':
    case 'j':
    	this.mode = CIRCLE_HOLE_MODE;  	
    	break;
    default:
      break;
    }
  }

  /**
   * This method is intentionally unimplemented.
   * 
   * @param mouse
   *          This parameter is ignored.
   */
  public void mouseClicked(final MouseEvent mouse) {
    // intentionally unimplemented
  }

  /**
   * If the right mouse button is being dragged, then move the selected vertex
   * to the point under the mouse event.
   * 
   * @param mouse
   *          {@inheritDoc}
   */
  public void mouseDragged(final MouseEvent mouse) {
	switch (mode) {
	case POLY_MODE:
		if (this.dragging)
			((Polygon)this.shapes[0]).moveVert(mouse.getX(), mouse.getY());
		break;
	case CIRC_MODE:
		if (this.dragging) // drag the boundary
			((Circle)this.shapes[1]).moveBoundary(mouse.getX(), mouse.getY());
		else if (this.draggingCenter) // drag the center
			((Circle)this.shapes[1]).setCenterPoint(mouse.getX(), mouse.getY());
		break;
	}    
  }

  /**
   * This method is intentionally unimplemented.
   * 
   * @param mouse
   *          This parameter is ignored.
   */
  public void mouseEntered(MouseEvent mouse) {
    // intentionally unimplemented
  }

  /**
   * This method is intentionally unimplemented.
   * 
   * @param mouse
   *          This parameter is ignored.
   */
  public void mouseExited(MouseEvent mouse) {
    // intentionally unimplemented
  }

  /**
   * This method is intentionally unimplemented.
   * 
   * @param mouse
   *          This parameter is ignored.
   */
  public void mouseMoved(final MouseEvent mouse) {
    // intentionally unimplemented
  }

  /**
   * Respond to mouse presses.
   * 
   * On left clicks, this method adds a vertex to the current polygon at the
   * location under the current mouse press.
   * 
   * On middle clicks, this method instructs the canvas to display a message
   * stating whether the point under the mouse is inside or outside the current
   * polygon.
   * 
   * On right clicks, this method selects the vertex of the current polygon
   * nearest the mouse press to initiate dragging.
   * 
   * @param mouse
   *          {@inheritDoc}
   */
  public void mousePressed(final MouseEvent mouse) {
    int button = mouse.getButton();
    switch (mode){
    case CIRC_MODE:
    	if (button == MouseEvent.BUTTON1){
        	((Circle)this.shapes[1]).setCenterPoint(mouse.getX(), mouse.getY());
        	this.draggingCenter = true;
    	}
    	else if (button == MouseEvent.BUTTON3) {
            int x = mouse.getX();
            int y = mouse.getY();
            ((Circle)this.shapes[1]).moveBoundary(x, y);
            this.dragging = true;
    	}
    	break;
    case POLY_MODE:
    	// on left mouse clicks, add a new vertex
        if (button == MouseEvent.BUTTON1)
        	((Polygon)this.shapes[0]).addVert(mouse.getX(), mouse.getY());
        // on right clicks, select the closest vertex
        else if (button == MouseEvent.BUTTON3) {
          int x = mouse.getX();
          int y = mouse.getY();
          ((Polygon)this.shapes[0]).selectVert(x, y);
          ((Polygon)this.shapes[0]).moveVert(x, y);
          this.dragging = true;
        }
        break;
    case INSI_MODE:
    	// display whether the point under the mouse is inside or
        // outside the polygon
        if (this.shapes[0].isInside(mouse.getX(), mouse.getY()))
        	this.insideOutsideMessage = "Point is INSIDE the polygon";
        else
            this.insideOutsideMessage = "Point is OUTSIDE the polygon";
    	break;
    case HOLE_MODE:
    	if (button == MouseEvent.BUTTON1)
    		((Polygon)this.shapes[0]).addHoleVert(mouse.getX(), mouse.getY());
    	break;
    	
    	/* For holes, no behavior for button3 */
    case CIRCLE_HOLE_MODE:
    	if (button == MouseEvent.BUTTON1)
    		((Polygon)this.shapes[0]).addCircleHole(mouse.getX(), mouse.getY());
    	else
    		((Polygon)this.shapes[0]).adjustCircleHoleRadius(mouse.getX(), mouse.getY());
    	break;
    	
    default:
    	break;
    }
    

  }

  /**
   * Remove any "inside/outside" message if the middle mouse button is released,
   * or terminate dragging a vertex if the right mouse button is released.
   * 
   * @param mouse
   *          {@inheritDoc}
   */
  public void mouseReleased(final MouseEvent mouse) {
    this.insideOutsideMessage = null;
    this.dragging = false;
    this.draggingCenter = false;
  }

  /**
   * {@inheritDoc}
   * 
   * This method is called when the window is resized.
   * 
   * @param drawable
   *          {@inheritDoc}
   * @param x
   *          {@inheritDoc}
   * @param y
   *          {@inheritDoc}
   * @param width
   *          {@inheritDoc}
   * @param height
   *          {@inheritDoc}
   */
  public void reshape(final GLAutoDrawable drawable, final int x, final int y,
      final int width, final int height) {
    // change viewport dimensions
    final GL2 gl = (GL2) drawable.getGL();
    gl.glViewport(0, 0, width, height);
    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();
    this.glu.gluOrtho2D(0, width, height, 0);
    gl.glMatrixMode(GL2.GL_MODELVIEW);
  }

  /** Runs the animator of the canvas. */
  public void run() {
    this.animator.start();
  }

public void dispose(GLAutoDrawable arg0) {
	// TODO Auto-generated method stub
	
}

}
