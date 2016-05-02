package main;

import graphics.Sphere;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.List;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import sun.awt.GlobalCursorManager;
import sun.nio.cs.ext.TIS_620;
import util.Color;
import util.GLImage;
import util.Vector3D;

import bubble.BubblePair;
import bubble.SubdivisionSurface;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.FPSAnimator;
import com.sun.xml.internal.ws.util.ByteArrayBuffer;

/**
 * A minimal JOGL demo.
 * 
 * @author <a href="mailto:kain@land-of-kain.de">Kai Ruhl</a>
 * @since 26 Feb 2009
 */
public class SimpleRayTracer extends GLCanvas implements GLEventListener,
		MouseMotionListener, KeyListener {

	/** Serial version UID. */
	private static final long serialVersionUID = 1L;
	private GLU glu;
	private Point mousePoint;
	private BubbleRayTracer rayTracer;
	private GLImage glImage;
	private Vector3D eyePt;
	private int width;
	private int height;

	public SimpleRayTracer(GLCapabilities capabilities, int width, int height) {
		this.width = width;
		this.height = height;
		glImage = new GLImage(width, height);
		rayTracer = new BubbleRayTracer();
		eyePt = new Vector3D(0, 30, 100);

		addGLEventListener(this);
		addMouseMotionListener(this);
		
		addKeyListener(this);

	}

	/**
	 * @return Some standard GL capabilities (with alpha).
	 */
	private static GLCapabilities createGLCapabilities() {
		GLCapabilities capabilities = new GLCapabilities();
		capabilities.setRedBits(8);
		capabilities.setBlueBits(8);
		capabilities.setGreenBits(8);
		capabilities.setAlphaBits(8);
		return capabilities;
	}

	/**
	 * Sets up the screen.
	 * 
	 * @see javax.media.opengl.GLEventListener#init(javax.media.opengl.GLAutoDrawable)
	 */
	public void init(GLAutoDrawable drawable) {
		drawable.setGL(new DebugGL(drawable.getGL()));
		final GL gl = drawable.getGL();

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glShadeModel(GL.GL_FLAT);
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		glu = new GLU();
	}

	/**
	 * The only method that you should implement by yourself.
	 * 
	 * @see javax.media.opengl.GLEventListener#display(javax.media.opengl.GLAutoDrawable)
	 */
	public void display(GLAutoDrawable drawable) {

		double frameZ = 50;
		double random = Math.random();
		Move(rayTracer.scene);
		
		System.gc();
		long  startTime = System.currentTimeMillis();
		for (int j = 0; j < height; j++)
		for (int i = 0; i < width; i++) {
				double x = (eyePt.z - frameZ) * (i - width / 2) / eyePt.z / 2 ;
				double y = (eyePt.z - frameZ) * (-j + height / 2) / eyePt.z / 2;
				Vector3D framePt = new Vector3D(x, y, frameZ);
				Vector3D direction = Vector3D.Substract(framePt, eyePt);
				Color color = rayTracer.Trace(framePt, Vector3D.Normalize(direction), 0.1, 2000, random, 1);
				
				glImage.setPixel(i, j, color.ToColor255());
				
		}
		System.gc();
		long  endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		System.out.println("duration = " + duration);
		
		ByteBuffer colorValues = glImage.getBuffer();
		colorValues.rewind();

		GL gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glRasterPos2i(0, 0);
		gl.glDrawPixels(width, height, GL.GL_RGB, GL.GL_UNSIGNED_BYTE,
				colorValues);
		gl.glFlush();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL gl = drawable.getGL();

		gl.glViewport(0, 0, width, height);
		// height = h;
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluOrtho2D(0.0, (double) width, 0.0, (double) height);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();

	}

	
	private void Move(Scene scene){
		//hard coded
		List<Sphere> bubbleList = scene.getBubbleSphList();
		if(!bubbleList.get(0).getClass().toString().contains("BubblePair")){
			Sphere bubble1 = bubbleList.get(0);
			Sphere bubble2 = bubbleList.get(1);
			bubble1.getCenter().x += 2;
			bubble2.getCenter().x += 10;
			
			if(Vector3D.Substract(bubble1.getCenter(), bubble2.getCenter()).Length() < bubble1.getRadius() + bubble2.getRadius()){
				bubbleList.remove(0);
				bubbleList.remove(0);
				BubblePair bubblePair1 = new BubblePair(bubble1, bubble2);
				bubbleList.add(0, bubblePair1);
				System.out.println("integrated");
			}
		}
	}
	
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
		throw new UnsupportedOperationException(
				"Changing display is not supported.");
	}

	public void mouseMoved(MouseEvent mouse) {
		mousePoint = mouse.getPoint();
		display();
	}
	
	public void keyPressed(KeyEvent e){
		
		System.out.println();
		String key = KeyEvent.getKeyText(e.getKeyCode());
		if(key.equals("F")){
			eyePt.x += 10;
		}
		if(key.equals("A")){
			eyePt.x += 10;
		}
		if(key.equals("E")){
			eyePt.y += 10;
		}
		if(key.equals("D")){
			eyePt.y -= 10;
		}
		if(key.equals("J")){
			eyePt.z += 10;
		}
		if(key.equals("K")){
			eyePt.z -= 10;
		}
	}

	
	
	public static void main(String[] args) {
		int WIDTH = 800;
		int HEIGHT = 800;
		GLCapabilities capabilities = createGLCapabilities();
		SimpleRayTracer canvas = new SimpleRayTracer(capabilities, WIDTH,
				HEIGHT);
		JFrame frame = new JFrame("Mini JOGL Demo (breed)");
		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		frame.setSize(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		canvas.requestFocus();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
