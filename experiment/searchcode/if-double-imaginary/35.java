package cc.creativecomputing.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.TraceGL2;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.GLUtessellator;

import cc.creativecomputing.graphics.CCGraphicsCore.CCShapeMode;
import cc.creativecomputing.graphics.font.CCFont;
import cc.creativecomputing.graphics.font.CCFontIO;
import cc.creativecomputing.graphics.font.CCGlutFont.CCGlutFontType;
import cc.creativecomputing.graphics.font.text.CCText;
import cc.creativecomputing.graphics.font.text.CCTextAlign;
import cc.creativecomputing.graphics.texture.CCPixelStorageModes;
import cc.creativecomputing.graphics.texture.CCTexture;
import cc.creativecomputing.graphics.texture.CCTexture.CCTextureTarget;
import cc.creativecomputing.graphics.util.CCClipSpaceFrustum;
import cc.creativecomputing.io.CCBufferUtil;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCMatrix32f;
import cc.creativecomputing.math.CCMatrix4f;
import cc.creativecomputing.math.CCQuaternion;
import cc.creativecomputing.math.CCVector2f;
import cc.creativecomputing.math.CCVector2i;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.CCVector4f;
import cc.creativecomputing.math.d.CCVector2d;
import cc.creativecomputing.math.d.CCVector3d;
import cc.creativecomputing.util.CCFormatUtil;
import cc.creativecomputing.util.logging.CCLog;


/**
 * This class represents the render module for creative computing
 * it contains all methods for drawing and is completely based on
 * OPENGL. OpenGL is strictly defined as "a software interface to 
 * graphics hardware." It is a 3D graphics and modeling library that 
 * is highly portable and very fast. Using OpenGL, you can create 
 * elegant and beautiful 3D graphics with nearly the visual quality 
 * of a ray tracer. Creative computing uses jogl as interface to 
 * OpenGL. It is aimed to simplify the access to OpengGL.
 * 
 * Every instance of CCApp has an instance of CCGraphics that
 * can be used for drawing.
 * @see CCApp 
 */
public class CCGraphics{
	
	public static CCGraphics instance;
	
	private static boolean debug = false;
	
	/**
	 * Changes the plain gl implementation with a composable pipeline which wraps 
	 * an underlying GL implementation, providing error checking after each OpenGL 
	 * method call. If an error occurs, causes a GLException to be thrown at exactly 
	 * the point of failure. 
	 */
	public static void debug() {
		debug = true;
	}
	
	/**
	 * Ends debugging.
	 * @see #debug()
	 */
	public void noDebug() {
		debug = false;
	}
	
	public static GL2 currentGL() {
		if(!debug)return GLU.getCurrentGL().getGL2();
		else return new DebugGL2(GLU.getCurrentGL().getGL2());
	}

	/**
	 * width of the parent application
	 */
	public int width;

	/**
	 * height of the parent application
	 */
	public int height;
	
	private final CCClipSpaceFrustum _myFrustum;

	private CCCamera _myCamera;

	/**
	 * Gives you the possibility to directly access OPENGL
	 */
	public GL2 gl;
	
	private GL2 _myPlainGL;
	
	private DebugGL2 _myDebugGL;
	
	private TraceGL2 _myTraceGL2;

	/**
	 * Gives you the possibility to directly access OPENGLs utility functions
	 */
	public GLU glu;

	GLUtessellator tobj;

	/**
	 * true if the host system is big endian (PowerPC, MIPS, SPARC),
	 * false if little endian (x86 Intel).
	 */
	static private boolean BIG_ENDIAN = System.getProperty("sun.cpu.endian").equals("big");

	/**
	 * Stores the number of lights supported by the OPENGL device
	 */
	private int MAX_LIGHTS;

	/**
	 * Quadratic object for drawing primitives and to define how 
	 * they have to be drawn
	 */
	private GLUquadric quadratic;
	

	private CCTexture[] _myTextures;

	public CCGraphics(final GL2 theGL){
		instance = this;
		gl = theGL;
		_myPlainGL = theGL;
		glu = new GLU();
		quadratic = glu.gluNewQuadric();
		MAX_LIGHTS = 8;
		
		_myFrustum = new CCClipSpaceFrustum(this);
		
		gl.glClearDepth(1.0f);										// Depth Buffer Setup
	    gl.glDepthFunc(GL.GL_LEQUAL);									// The Type Of Depth Testing (Less Or Equal)
	    gl.glEnable(GL.GL_DEPTH_TEST);									// Enable Depth Testing
	    gl.glShadeModel(GL2.GL_SMOOTH);									// Select Smooth Shading
	    gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);			// Set Perspective Calculations To Most Accurate
		

		// 
		frontFace(CCFace.COUNTER_CLOCK_WISE);

		// these are necessary for alpha (i.e. fonts) to work
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		//setup up default lighting
		lighting = false;
		gl.glDisable(GLLightingFunc.GL_LIGHTING);
		for (int i = 0; i < MAX_LIGHTS; i++){
			gl.glDisable(GLLightingFunc.GL_LIGHT0 + i);
		}
		gl.glEnable(GL2.GL_NORMALIZE);


		colorMaterial(AMBIENT_AND_DIFFUSE);
		
		lightModelTwoSide(true);
		
		int myTextureUnits = textureUnits();
		_myTextures = new CCTexture[myTextureUnits];
	}
	
	///////////////////////////////////////////////////
	//
	// OPENGL INFORMATIONS
	//
	//////////////////////////////////////////////////

	private int[] _myIntegerGet = new int[1];
	
	public int getInteger(int theGLIID){
		gl.glGetIntegerv(theGLIID, _myIntegerGet,0);
		return _myIntegerGet[0];
	}
	
	public IntBuffer getIntBuffer(int theGLID, int theNumberOfValues){
		final IntBuffer myResult = IntBuffer.allocate(theNumberOfValues);
		gl.glGetIntegerv(theGLID, myResult);
		myResult.rewind();
		return myResult;
	}
	
	public int[] getIntArray(int theGLID, int theNumberOfValues){
		int[] result = new int[theNumberOfValues];
		gl.glGetIntegerv(theGLID, result,0);
		return result;
	}
	
	private float[] _myFloatGet = new float[1];
	
	public float getFloat(int theGLIID){
		gl.glGetFloatv(theGLIID, _myFloatGet,0);
		return _myFloatGet[0];
	}
	
	public FloatBuffer getFloatBuffer(int theGLID, int theNumberOfValues){
		final FloatBuffer myResult = FloatBuffer.allocate(theNumberOfValues);
		gl.glGetFloatv(theGLID, myResult);
		myResult.rewind();
		return myResult;
	}
	
	public float[] getFloatArray(int theGLID, int theNumberOfValues){
		float[] result = new float[theNumberOfValues];
		gl.glGetFloatv(theGLID, result,0);
		return result;
	}
	
	public String getString(int theGLID){
		return gl.glGetString(theGLID);
	}
	
	/**
	 * Returns the name of the hardware vendor.
	 * @return the name of the hardware vendor
	 */
	public String vendor(){
		return getString(GL.GL_VENDOR);
	}
	
	/**
	 * Returns a brand name or the name of the vendor dependent on the
	 * OPENGL implementation.
	 * @return brand name or name of the vendor
	 */
	public String renderer(){
		return getString(GL.GL_RENDERER);
	}
	
	/**
	 * returns the version number followed by a space and any vendor-specific information. 
	 * @return the version number
	 */
	public String version(){
		return getString(GL.GL_VERSION);
	}
	
	/**
	 * Returns an array with all the extensions that are available on the current hardware setup.
	 * @return the available extensions
	 */
	public String[] extensions(){
		return getString(GL.GL_EXTENSIONS).split(" ");
	}
	
	/**
	 * Returns true if the given extension is available at the current hardware setup.
	 * @param theExtension extension to check
	 * @return true if the extension is available otherwise false
	 */
	public boolean isExtensionSupported(final String theExtension){
		for(String myExtension:extensions()){
			if(myExtension.equals(theExtension))return true;
		}
		return false;
	}
	
	/**
	 * true if you want to report that no error occurred
	 */
	private boolean _myReportNoError = false;
	private boolean _myReportErrors = true;
	
	/**
	 * Call this method to check for drawing errors. cc checks
	 * for drawing errors at the end of each frame automatically.
	 * However only the last error will be reported. You can call
	 * this method for debugging to find where errors occur. Error 
	 * codes are cleared when checked, and multiple error flags may 
	 * be currently active. To retrieve all errors, call this function 
	 * repeatedly until you get no error.
	 * @shortdesc Use this method to check for drawing errors.
	 */
	public void checkError(final String theString){
		switch(gl.glGetError()){
		case GL.GL_NO_ERROR:
			if(_myReportNoError)CCLog.error(theString + " # NO ERROR REPORTED");
			return;
		case GL.GL_INVALID_ENUM:
			CCLog.error(theString + " # INVALID ENUMERATION REPORTED. check for errors in OPENGL calls with constants.");
			return;
		case GL.GL_INVALID_VALUE:
			CCLog.error(theString + "# INVALID VALUE REPORTED. check for errors with passed values that are out of a defined range.");
			return;
		case GL.GL_INVALID_OPERATION:
			CCLog.error(theString + "# INVALID OPERATION REPORTED. check for function calls that are invalid in the current graphics state.");
			return;
		case GL2.GL_STACK_OVERFLOW:
			CCLog.error(theString + "# STACK OVERFLOW REPORTED. check for errors in matrix operations");
			return;
		case GL2.GL_STACK_UNDERFLOW:
			CCLog.error(theString + "# STACK UNDERFLOW REPORTED. check for errors  in matrix operations");
			return;
		case GL.GL_OUT_OF_MEMORY:
			CCLog.error(theString + "# OUT OF MEMORY. not enough memory to execute the commands");
			return;
		case GL2.GL_TABLE_TOO_LARGE:
			CCLog.error(theString + "# TABLE TOO LARGE.");
			return;
		}
	}
	
	public void checkError(){
		checkError("");
	}
	
	/**
	 * Use this method to tell cc if it should report no error
	 * @param theReportNoError
	 */
	public void reportNoError(final boolean theReportNoError){
		_myReportNoError = theReportNoError;
	}
	
	public void reportError(final boolean theReportError){
		_myReportErrors = theReportError;
	}

	protected boolean displayed = false;
	
	///////////////////////////////////////////////////
	//
	// GRAPHICS SETUP
	//
	//////////////////////////////////////////////////

	/**
	 * Called in response to a resize event, handles setting the
	 * new width and height internally.
	 * @invisible
	 */
	public void resize(final int theWidth, final int theHeight){ // ignore
		width = theWidth;
		height = theHeight;
		
		_myCamera = new CCCamera(this);
	}

	boolean firstFrame = true;

	/**
	 * @invisible
	 */
	public void beginDraw(){
		if(debug) {
			if(_myDebugGL == null)_myDebugGL = new DebugGL2(_myPlainGL);
			gl = _myDebugGL;
		}else {
			gl = _myPlainGL;
		}
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		//gl.glLoadIdentity();
		pushMatrix();
		if(_myCamera != null)_myCamera.draw(this);
	}

	/**
	 * @invisible
	 */
	public void endDraw(){
		popMatrix();
		
		if(_myReportErrors)checkError();
	}
	
	public static enum CCCompare{
		NEVER(GL.GL_NEVER),
		ALWAYS(GL.GL_ALWAYS),
		LESS(GL.GL_LESS),
		LESS_EQUAL(GL.GL_LEQUAL),
		GREATER(GL.GL_GREATER),
		GREATER_EQUAL(GL.GL_GEQUAL),
		EQUAL(GL.GL_EQUAL),
		NOT_EQUAL(GL.GL_NOTEQUAL);
		
		private final int glID;
		
		CCCompare(final int theGlID){
			glID = theGlID;
		}
	}

	/**
	 * Use this method to define a mask. You can use all available draw methods
	 * after it. After calling endMask everything drawn will be masked by the
	 * defined mask.
	 */
	public void beginMask(){
		colorMask(false, false, false, false);
		gl.glClearStencil(0x1);
		gl.glEnable(GL.GL_STENCIL_TEST);
        gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
        gl.glStencilFunc (GL.GL_ALWAYS, 0x1, 0x1);
        gl.glStencilOp (GL.GL_REPLACE, GL.GL_REPLACE, GL.GL_REPLACE);
        
	}

	/**
	 * Ends the mask
	 */
	public void endMask(){
		colorMask(true, true, true, true);
		gl.glStencilFunc (GL.GL_NOTEQUAL, 0x1, 0x1);
		gl.glStencilOp (GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
	}

	/**
	 * Disables a mask once you have defined one using beginMask and endMask
	 */
	public void noMask(){
		gl.glDisable(GL.GL_STENCIL_TEST);
	}
	
	public void scissor(final int theX, final int theY, final int theWidth, final int theHeight) {
		gl.glEnable(GL.GL_SCISSOR_TEST);
		gl.glScissor(theX, theY, theWidth, theHeight);
	}
	
	public void noScissor() {
		gl.glEnable(GL.GL_SCISSOR_TEST);
	}
	
	/**
	 * Specifies the depth comparison function.
	 * @author Riekoff
	 *
	 */
	public static enum CCDepthFunc{
		/**
		 * Never passes.
		 */
		NEVER(GL.GL_NEVER),
		/**
		 * Passes if the incoming depth value is less than the stored depth value.
		 */
		ALWAYS(GL.GL_ALWAYS),
		/**
		 * Passes if the incoming depth value is equal to the stored depth value.
		 */
		LESS(GL.GL_LESS),
		/**
		 * Passes if the incoming depth value is less than or equal to the stored depth value.
		 */
		LESS_EQUAL(GL.GL_LEQUAL),
		/**
		 * Passes if the incoming depth value is greater than the stored depth value.
		 */
		GREATER(GL.GL_GREATER),
		/**
		 * Passes if the incoming depth value is greater than or equal to the stored depth value.
		 */
		GREATER_EQUAL(GL.GL_GEQUAL),
		/**
		 * Passes if the incoming depth value is equal to the stored depth value.
		 */
		EQUAL(GL.GL_EQUAL),
		/**
		 * Passes if the incoming depth value is not equal to the stored depth value.
		 */
		NOT_EQUAL(GL.GL_NOTEQUAL);
		
		private final int glID;
		
		CCDepthFunc(final int theGlID){
			glID = theGlID;
		}
	}
	
	/**
	 * <p>
	 * Specifies the function used to compare each incoming pixel depth 
	 * value with the depth value present in the depth buffer. The comparison 
	 * is performed only if depth testing is enabled.
	 * </p>
	 * <p>
	 * The initial value of func is LESS_EQUAL. Initially, depth testing is disabled. 
	 * If depth testing is disabled or if no depth buffer exists, it is as if the depth test always passes.
	 * @param theCompare Specifies the depth comparison function.
	 */
	public void depthFunc(final CCDepthFunc theCompare){
		gl.glDepthFunc(theCompare.glID);
	}
	
	public void depthTest(){
		gl.glEnable(GL.GL_DEPTH_TEST);
	}
	
	public void noDepthTest(){
		gl.glDisable(GL.GL_DEPTH_TEST);
	}
	
	public void depthMask(){
		gl.glDepthMask(true);
	}

	public void noDepthMask(){
		gl.glDepthMask(false);
	}
	
	/**
	 * Clears the depth buffer
	 */
	public void clearDepthBuffer(){
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
	}
	
	public void clearDepth(final float theDefaultDepth){
		gl.glClearDepth(theDefaultDepth);
	}

	/**
	 * specifies the index used by clearStencil() to clear the stencil buffer. 
	 * s is masked with 2 m - 1 , where m is the number of bits in the stencil buffer.
	 * @param theS Specifies the index used when the stencil buffer is cleared. The initial value is 0.
	 */
	public void clearStencil(int theS){
		gl.glClearStencil(theS);
	}
	
	/**
	 * Clears the stencil buffer.
	 */
	public void clearStencilBuffer(){
		gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
	}
	
	/**
	 * If enabled, do stencil testing and update the stencil buffer. 
	 */
	public void stencilTest(){
		gl.glEnable(GL.GL_STENCIL_TEST);
	}

	/**
	 * If enabled, do stencil testing and update the stencil buffer. 
	 */
	public void noStencilTest(){
		gl.glDisable(GL.GL_STENCIL_TEST);
	}
	
	/**
	 * Specifies thestencil  test function.
	 * @author christianriekoff
	 *
	 */
	public static enum CCStencilFunction{
		/**
		 * Always fails.
		 */
		NEVER(GL.GL_NEVER),
		/**
		 * Passes if ( ref & mask ) < ( stencil & mask ).
		 */
		LESS(GL.GL_LESS),
		
		/**
		 * Passes if ( ref & mask ) <= ( stencil & mask ).
		 */
		LESS_EQUAL(GL.GL_LEQUAL),
		/**
		 * Passes if ( ref & mask ) > ( stencil & mask ).
		 */
		GREATER(GL.GL_GREATER),
		/**
		 * Passes if ( ref & mask ) >= ( stencil & mask ).
		 */
		GREATER_EQUAL(GL.GL_GEQUAL),
		/**
		 * Passes if ( ref & mask ) = ( stencil & mask ).
		 */
		EQUAL(GL.GL_EQUAL),
		/**
		 * Passes if ( ref & mask ) != ( stencil & mask ).
		 */
		NOT_EQUAL(GL.GL_NOTEQUAL),
		/**
		 * Always passes.
		 */
		ALWAYS(GL.GL_ALWAYS);
		
		
		private final int glID;
		
		CCStencilFunction(final int theGlID){
			glID = theGlID;
		}
	}
	
	/**
	 * <p>
	 * Stenciling, like depth-buffering, enables and disables drawing on a per-pixel basis. 
	 * Stencil planes are first drawn into using GL drawing primitives, then geometry and 
	 * images are rendered using the stencil planes to mask out portions of the screen. 
	 * Stenciling is typically used in multipass rendering algorithms to achieve special 
	 * effects, such as decals, outlining, and constructive solid geometry rendering.
	 * </p>
	 * <p>
	 * The stencil test conditionally eliminates a pixel based on the outcome of a comparison 
	 * between the reference value and the value in the stencil buffer. To enable and disable 
	 * the test, call {@linkplain #stencilTest()} and {@linkplain #noStencilTest()}. To specify 
	 * actions based on the outcome of the stencil test, call glStencilOp or glStencilOpSeparate.
	 * </p>
	 * <p>
	 * There can be two separate sets of func, ref, and mask parameters; one affects back-facing 
	 * polygons, and the other affects front-facing polygons as well as other non-polygon primitives. 
	 * glStencilFunc sets both front and back stencil state to the same values. Use glStencilFuncSeparate 
	 * to set front and back stencil state to different values.
	 * </p>
	 * <p>
	 * func is a symbolic constant that determines the stencil comparison function. It accepts 
	 * one of eight values, shown in the following list. ref is an integer reference value that 
	 * is used in the stencil comparison. It is clamped to the range 0 2 n - 1 , where n is the 
	 * number of bitplanes in the stencil buffer. mask is bitwise ANDed with both the reference 
	 * value and the stored stencil value, with the ANDed values participating in the comparison.
	 * </p>
	 * <p>
	 * If stencil represents the value stored in the corresponding stencil buffer location, the 
	 * following list shows the effect of each comparison function that can be specified by func. 
	 * Only if the comparison succeeds is the pixel passed through to the next stage in the 
	 * rasterization process (see glStencilOp). All tests treat stencil values as unsigned 
	 * integers in the range 0 2 n - 1 , where n is the number of bitplanes in the stencil buffer.
	 * </p>
	 * @param theFunc Specifies the test function. Eight symbolic constants are valid: 
	 * @param theRef 
	 * 		Specifies the reference value for the stencil test. ref is clamped to the range 
	 * 		0 2 n - 1 , where n is the number of bitplanes in the stencil buffer. The initial value is 0.
	 * @param theMask
	 * 		Specifies a mask that is ANDed with both the reference value and the stored stencil value when 
	 * 		the test is done. The initial value is all 1's.
	 */
	public void stencilFunc(CCStencilFunction theFunc, int theRef, int theMask){
		gl.glStencilFunc(theFunc.glID, theRef, theMask);
	}
	
	/**
	 * Specifies thestencil  test function.
	 * @author christianriekoff
	 *
	 */
	public static enum CCStencilOperation{
		/**
		 * Keeps the current value.
		 */
		KEEP(GL.GL_KEEP),
		/**
		 * Sets the stencil buffer value to 0.
		 */
		ZERO(GL.GL_ZERO),
		
		/**
		 * Sets the stencil buffer value to ref, as specified by stencilFunction.
		 */
		REPLACE(GL.GL_REPLACE),
		/**
		 * Increments the current stencil buffer value. Clamps to the maximum representable unsigned value.
		 */
		INCREMENT(GL.GL_INCR),
		/**
		 * Increments the current stencil buffer value. Wraps stencil buffer value to zero when incrementing the maximum representable unsigned value.
		 */
		INCREMENT_WRAP(GL.GL_INCR_WRAP),
		/**
		 * Decrements the current stencil buffer value. Clamps to 0.
		 */
		DECREMENT(GL.GL_DECR),
		/**
		 * Decrements the current stencil buffer value. Wraps stencil buffer value to the maximum representable unsigned value when decrementing a stencil buffer value of zero.
		 */
		DECREMENT_WRAP(GL.GL_DECR_WRAP),
		/**
		 * Bitwise inverts the current stencil buffer value.
		 */
		INVERT(GL.GL_INVERT);
		
		
		private final int glID;
		
		CCStencilOperation(final int theGlID){
			glID = theGlID;
		}
	}
	
	public void stencilOperation(
		CCStencilOperation theStencilTestFailOp,
		CCStencilOperation theDepthTestFailOp,
		CCStencilOperation ThePassOp
	){
		gl.glStencilOp(theStencilTestFailOp.glID, theDepthTestFailOp.glID, ThePassOp.glID);
	}
	
	public void stencilOperation(CCStencilOperation theOperation){
		stencilOperation(theOperation,theOperation,theOperation);
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	//
	//  COLOR HANDLING
	//
	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////

	/**
	 * Sets the clear color for OPENGL the clear color is the color the
	 * background is filled with after the call of clear. As long as you haven't
	 * defined a clear color it will be set to black. Normally you once define a
	 * clear color and than use clear to clear the screen
	 */
	public void clearColor(final float theRed, final float theGreen, final float theBlue, final float theAlpha) {
		gl.glClearColor(theRed, theGreen, theBlue, theAlpha);
	}
	
	public void clearColor(final float theRed, final float theGreen, final float theBlue) {
		gl.glClearColor(theRed, theGreen, theBlue, 1);
	}
	
	public void clearColor(final float theGray, final float theAlpha){
		gl.glClearColor(theGray,theGray,theGray,theAlpha);
	}
	
	public void clearColor(final float theGray){
		gl.glClearColor(theGray,theGray,theGray,1);
	}
	
	public void clearColor(final CCColor theColor){
		clearColor(theColor.r,theColor.g,theColor.b,theColor.a);
	}
	
	public void clearColor(final int theRGB) {
		if (((theRGB & 0xff000000) == 0) && (theRGB <= 255)) {
			clearColor(theRGB, theRGB, theRGB);
		} else {
			clearColor(
				(theRGB >> 16) & 0xff,
				(theRGB >> 8)  & 0xff,
				(theRGB)	   & 0xff,
				(theRGB >> 24) & 0xff
			);
		}
	}
	
	public void clearColor(final int theGray, final int theAlpha){
		clearColor(theGray,theGray,theGray,theAlpha);
	}
	
	public void clearColor(final int theRed, final int theGreen, final int theBlue){
		clearColor((float)theRed/255, (float)theGreen/255, (float)theBlue/255);
	}
	
	public void clearColor(final int theRed, final int theGreen, final int theBlue, final int theAlpha){
		clearColor((float)theRed/255, (float)theGreen/255, (float)theBlue/255, (float)theAlpha/255);
	}
	
	public void clearColor(){
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
	}
	
	/**
	 * Fills the background with the actual clear color, so that the screen is cleared.
	 * As long as you haven't defined clear color it will be set to black. 
	 * Normally you once define a clear color and than use clear to clear the screen
	 */
	public void clear() {
		gl.glClearStencil(0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
	}
	
	/**
	 * Use this method to set the drawing color, everything you draw
	 * after a call of color, will have the defined color, there are three
	 * ways to define a color, first is to use float values between 0 and 1,
	 * the second is to use integer values between 0 and 255 and the third way
	 * is to use the CCColor class.
	 * @param theRed
	 * @param theGreen
	 * @param theBlue
	 * @param theAlpha
	 */
	public void color(final float theRed, final float theGreen, final float theBlue, final float theAlpha){
		gl.glColor4f(theRed, theGreen, theBlue, theAlpha);
	}
	
	public void color(final double theRed, final double theGreen, final double theBlue, final double theAlpha){
		gl.glColor4d(theRed, theGreen, theBlue, theAlpha);
	}
	
	public void color(final float theRed, final float theGreen,final float theBlue){
		gl.glColor3f(theRed, theGreen, theBlue);
	}
	
	public void color(float theGray, final float theAlpha){
		gl.glColor4f(theGray,theGray,theGray,theAlpha);
	}
	
	public void color(double theGray, final double theAlpha) {
		gl.glColor4d(theGray,theGray,theGray,theAlpha);
	}
	
	public void color(float theGray){
		gl.glColor4f(theGray,theGray,theGray,1);
	}
	
	public void color(double theGray) {
		gl.glColor4d(theGray,theGray,theGray,1);
	}
	
	public void color(final CCColor color){
		color(color.r,color.g,color.b,color.a);
	}
	
	public void color(final CCColor color, final float theAlpha){
		color(color.r,color.g,color.b,theAlpha);
	}
	
	public void color(final int theRGB) {
		if (((theRGB & 0xff000000) == 0) && (theRGB <= 255)) {
			color(theRGB, theRGB, theRGB);
		} else {
			color(
				(theRGB >> 16) & 0xff,
				(theRGB >> 8)  & 0xff,
				(theRGB)	   & 0xff,
				(theRGB >> 24) & 0xff
			);
		}
	}
	
	public void color(final int theGray, final int theAlpha){
		color(theGray,theGray,theGray,theAlpha);
	}
	
	public void color(final int theRed, final int theGreen, final int theBlue){
		gl.glColor3ub((byte)theRed, (byte)theGreen, (byte)theBlue);
	}
	
	public void color(final int theRed, final int theGreen, final int theBlue, final int theAlpha){
		gl.glColor4ub((byte)theRed, (byte)theGreen, (byte)theBlue, (byte)theAlpha);
	}
	
	public CCColor color(){
		float[] myColor = new float[4];
		gl.glGetFloatv(GL2ES1.GL_CURRENT_COLOR,myColor,0);
		return new CCColor(myColor);
	}
	
	/**
	 * colorMask specifies whether the individual color components in the frame buffer 
	 * can or cannot be written. If theMaskRed is false, for example, no change is made to the red
	 * component of any pixel in any of the color buffers, regardless of the drawing operation attempted.
	 * @param theMaskRed
	 * @param theMaskGreen
	 * @param theMaskBlue
	 * @param theMaskAlpha
	 */
	public void colorMask(final boolean theMaskRed, final boolean theMaskGreen, final boolean theMaskBlue, final boolean theMaskAlpha) {
		gl.glColorMask(theMaskRed, theMaskGreen, theMaskBlue, theMaskAlpha);
	}
	
	/**
	 * Disables a previous color mask.
	 */
	public void noColorMask() {
		colorMask(true, true, true, true);
	}
	
	/**
	 * Use this method to set the drawing color, everything you draw
	 * after a call of color, will have the defined color, there are three
	 * ways to define a color, first is to use float values between 0 and 1,
	 * the second is to use integer values between 0 and 255 and the third way
	 * is to use the CCColor class.
	 * @param theRed
	 * @param theGreen
	 * @param theBlue
	 * @param theAlpha
	 */
	public void pcolor(final float theRed, final float theGreen, final float theBlue, final float theAlpha){
		gl.glColor4f(theRed, theGreen, theBlue, theAlpha);
	}
	
	public void pcolor(final float theRed, final float theGreen, final float theBlue){
		gl.glColor3f(theRed, theGreen, theBlue);
	}
	
	public void pcolor(final float theGray,final float theAlpha){
		gl.glColor4f(theGray,theGray,theGray,theAlpha);
	}
	
	public void pcolor(final float theGray){
		gl.glColor4f(theGray,theGray,theGray,1);
	}
	
	public void pcolor(final CCColor color){
		color(color.r,color.g,color.b,color.a);
	}
	
	public void pcolor(final CCColor color, final float theAlpha){
		color(color.r,color.g,color.b,theAlpha);
	}
	
	public void pcolor(final int theRGB) {
		if (((theRGB & 0xff000000) == 0) && (theRGB <= 255)) {
			color(theRGB, theRGB, theRGB);
		} else {
			color(
				(theRGB >> 16) & 0xff,
				(theRGB >> 8)  & 0xff,
				(theRGB)	   & 0xff,
				(theRGB >> 24) & 0xff
			);
		}
	}

	public void pcolor(final int theGray, final int theAlpha) {
		color(theGray, theGray, theGray, theAlpha);
	}

	public void pcolor(final int theRed, final int theGreen, final int theBlue) {
		gl.glColor3ub((byte) theRed, (byte) theGreen, (byte) theBlue);
	}
	
	public void pcolor(final int theRed, final int theGreen, final int theBlue, final int theAlpha) {
		gl.glColor4ub((byte) theRed, (byte) theGreen, (byte) theBlue, (byte) theAlpha);
	}
	
	public CCColor pcolor() {
		return color();
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	//
	//  MATRIX OPERATIONS
	//
	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	
	public static enum CCMatrixMode{
		/**
		 * Applies subsequent matrix operations to the modelview matrix stack.
		 */
		MODELVIEW(GLMatrixFunc.GL_MODELVIEW,GLMatrixFunc.GL_MODELVIEW_MATRIX),
		/**
		 * Applies subsequent matrix operations to the projection matrix stack.
		 */
		PROJECTION(GLMatrixFunc.GL_PROJECTION,GLMatrixFunc.GL_PROJECTION_MATRIX),
		/**
		 * Applies subsequent matrix operations to the texture matrix stack.
		 */
		TEXTURE(GL.GL_TEXTURE,GLMatrixFunc.GL_TEXTURE_MATRIX);
		
		int glID;
		int glMatrixID;
		
		private CCMatrixMode(final int theGlID, final int theGlMatrixID){
			glID = theGlID;
			glMatrixID = theGlMatrixID;
		}
	}
	
	/**
	 * Specifies whether the modelview, projection, or texture matrix will be modified, 
	 * using the argument MODELVIEW, PROJECTION, or TEXTURE for mode. Subsequent 
	 * transformation commands affect the specified matrix. Note that only one matrix 
	 * can be modified at a time. By default, the modelview matrix is the one that's 
	 * modifiable, and all three matrices contain the identity matrix.
	 * @param theMode int, Specifies which matrix stack is the target for subsequent matrix operations. 
	 * Three values are accepted: MODELVIEW, PROJECTION, and TEXTURE.
	 */
	public void matrixMode(final CCMatrixMode theMode){
		gl.glMatrixMode(theMode.glID);
	}
	
	public CCMatrixMode matrixMode() {
		switch(getInteger(GLMatrixFunc.GL_MATRIX_MODE)) {
		case GL.GL_TEXTURE:
			return CCMatrixMode.TEXTURE;
		case GLMatrixFunc.GL_PROJECTION:
			return CCMatrixMode.PROJECTION;
		default:
			return CCMatrixMode.MODELVIEW;
		}
		
		
	}
	
	/**
	 * Replaces the current matrix with the identity matrix. It is semantically 
	 * equivalent to calling glLoadMatrix with the identity matrix.
	 * Use the loadIdentity() command to clear the currently modifiable matrix 
	 * for future transformation commands, since these commands modify the current 
	 * matrix. Typically, you always call this command before specifying projection 
	 * or viewing transformations, but you might also call it before specifying 
	 * a modeling transformation.
	 */
	public void loadIdentity(){
		gl.glLoadIdentity();
		
		//gl.glTranslatef(0,0,-400.00001f);
	}
	
	/**
	 * Replaces the current matrix with the one specified in m. The current matrix 
	 * is the projection matrix, modelview matrix, or texture matrix, determined 
	 * by the current matrix mode.
	 * @param theMatrix Matrix4f, matrix the current matrix is set to
	 * @related matrixMode ( )
	 */
	public void loadMatrix(final CCMatrix4f theMatrix){
		gl.glLoadMatrixf(theMatrix.toFloatBuffer());
	}
	
	/**
	 * Applies the matrix specified by the sixteen values pointed to by m by the 
	 * current matrix and stores the result as the current matrix.
	 * @param theMatrix
	 */
	public void applyMatrix(final CCMatrix4f theMatrix){
		gl.glMultMatrixf(theMatrix.toFloatBuffer());
	}
	
	public void applyMatrix(final CCMatrix32f theMatrix) {
		applyMatrix(
			theMatrix.m00, theMatrix.m01, theMatrix.m02, 
			theMatrix.m10, theMatrix.m11, theMatrix.m12
		);
	}
	
	public void applyMatrix(final float[] theMatrix) {
		gl.glMultMatrixf(theMatrix, 0);
	}
	
	public void applyMatrix(
		final float n00, final float n01, final float n02, final float n03,
		final float n10, final float n11, final float n12, final float n13,
		final float n20, final float n21, final float n22, final float n23,
		final float n30, final float n31, final float n32, final float n33
	){
		final FloatBuffer myMatrixBuffer = FloatBuffer.allocate(16);
		
		myMatrixBuffer.put(n00); myMatrixBuffer.put(n10); myMatrixBuffer.put(n20); myMatrixBuffer.put(n30);
		myMatrixBuffer.put(n01); myMatrixBuffer.put(n11); myMatrixBuffer.put(n21); myMatrixBuffer.put(n31);
		myMatrixBuffer.put(n02); myMatrixBuffer.put(n12); myMatrixBuffer.put(n22); myMatrixBuffer.put(n32);
		myMatrixBuffer.put(n03); myMatrixBuffer.put(n13); myMatrixBuffer.put(n23); myMatrixBuffer.put(n33);

		myMatrixBuffer.rewind();
		gl.glMultMatrixf(myMatrixBuffer);
	}
	

	  /**
		 * Apply a 3x2 affine transformation matrix.
		 */
	public void applyMatrix(
		final float n00, final float n01, final float n02, 
		final float n10, final float n11, final float n12
	){
		final FloatBuffer myMatrixBuffer = FloatBuffer.allocate(16);
		
		myMatrixBuffer.put(n00); myMatrixBuffer.put(n10); myMatrixBuffer.put(0); myMatrixBuffer.put(0);
		myMatrixBuffer.put(n01); myMatrixBuffer.put(n11); myMatrixBuffer.put(0); myMatrixBuffer.put(0);
		myMatrixBuffer.put(0); myMatrixBuffer.put(0); myMatrixBuffer.put(1); myMatrixBuffer.put(0);
		myMatrixBuffer.put(n02); myMatrixBuffer.put(n12); myMatrixBuffer.put(0); myMatrixBuffer.put(1);

		myMatrixBuffer.rewind();
		gl.glMultMatrixf(myMatrixBuffer);
	}

	public void resetMatrix(){
		gl.glLoadIdentity();
	}
	
	/**
	 * Moves the coordinate system origin to the specified point.
	 * The point can be passed as vector or separate values and can be 2d or 3d.
	 * If the matrix mode is either MODELVIEW or PROJECTION, all objects drawn 
	 * after translate is called are translated. Use pushMatrix and popMatrix to 
	 * save and restore the untranslated coordinate system.
	 * @shortdesc Moves the coordinate system origin to the point defined point.
	 * @param theX float, x coord of the translation vector
	 * @param theY float, y coord of the translation vector
	 * @param theZ float, z coord of the translation vector
	 */
	public void translate(final float theX, final float theY, final float theZ){
		gl.glTranslatef(theX,theY,theZ);
	}
	
	public void translate(final double theX, final double theY, final double theZ){
		gl.glTranslated(theX,theY,theZ);
	}
	
	public void translate(final float theX,final float theY){
		translate(theX,theY,0);
	}
	
	public void translate(final double theX,final double theY){
		translate(theX,theY,0);
	}
	
	/**
	 * @param theVector Vector3f, the translation vector
	 */
	public void translate(final CCVector3f theVector){
		translate(theVector.x,theVector.y,theVector.z);
	}
	
	/**
	 * @param theVector Vector3f, the translation vector
	 */
	public void translate(final CCVector3d theVector){
		translate(theVector.x,theVector.y,theVector.z);
	}
	
	/**
	 * @param theVector Vector2f, the translation vector
	 */
	public void translate(final CCVector2f theVector){
		translate(theVector.x,theVector.y);
	}
	
	/**
	 * @param theVector Vector2f, the translation vector
	 */
	public void translate(final CCVector2i theVector){
		translate(theVector.x,theVector.y);
	}
	
	/**
	 * @param theVector Vector2d, the translation vector
	 */
	public void translate(final CCVector2d theVector){
		translate(theVector.x,theVector.y);
	}
	
	/**
	 * Multiplies the current matrix by a matrix that rotates an object 
	 * (or the local coordinate system) in a counterclockwise direction about 
	 * the ray from the origin through the point (x, y, z). The angle parameter 
	 * specifies the angle of rotation in degrees.<br>
	 * If the matrix mode is either MODELVIEW or PROJECTION, all objects drawn 
	 * after rotate is called are rotated. Use pushMatrix and popMatrix to save 
	 * and restore the unrotated coordinate system.
	 * @param theAngle float, the angle of rotation, in degrees.
	 * @param theX float, x coord of the vector
	 * @param theY float, y coord of the vector
	 * @param theZ float, z coord of the vector
	 */
	public void rotate(final float theAngle, final float theX, final float theY, final float theZ) {
		gl.glRotatef(theAngle, theX, theY, theZ);
	}
	
	/**
	 * @param theVector Vector3f, vector 
	 */
	public void rotate(final float theAngle, final CCVector3f theVector) {
		rotate(theAngle,theVector.x,theVector.y,theVector.z);
	}
	
	public void rotate(final CCQuaternion theQuaternion){
		final CCVector4f myRotation = theQuaternion.getVectorAndAngle();
		rotate(CCMath.degrees(myRotation.w()),myRotation.x,myRotation.y,myRotation.z);
	}
	
	/**
	 * Rotates an object around the X axis the amount specified by the angle parameter.
	 * Objects are always rotated around their relative position to the origin and positive 
	 * numbers rotate objects in a counterclockwise direction. Transformations apply to 
	 * everything that happens after and subsequent calls to the function accumulates the effect.
	 * @param theAngle float, the angle of rotation, in degrees.
	 */
	public void rotateX(final float theAngle){
		rotate(theAngle,1.0f,0.0f,0.0f);
	}
	
	/**
	 * Rotates an object around the Y axis the amount specified by the angle parameter.
	 * Objects are always rotated around their relative position to the origin and positive 
	 * numbers rotate objects in a counterclockwise direction. Transformations apply to 
	 * everything that happens after and subsequent calls to the function accumulates the effect.
	 * @param theAngle float, the angle of rotation, in degrees.
	 */
	public void rotateY(final float theAngle){
		rotate(theAngle,0.0f,1.0f,0.0f);
	}
	
	/**
	 * Rotates an object around the Z axis the amount specified by the angle parameter.
	 * Objects are always rotated around their relative position to the origin and positive 
	 * numbers rotate objects in a counterclockwise direction. Transformations apply to 
	 * everything that happens after and subsequent calls to the function accumulates the effect.
	 * @param theAngle float, the angle of rotation, in degrees.
	 */
	public void rotateZ(final float theAngle){
		rotate(theAngle,0.0f,0.0f,1.0f);
	}
	
	/**
	 * Rotates an object around the Z axis the amount specified by the angle parameter.
	 * Objects are always rotated around their relative position to the origin and positive 
	 * numbers rotate objects in a counterclockwise direction. Transformations apply to 
	 * everything that happens after and subsequent calls to the function accumulates the effect.
	 * @param theAngle float, the angle of rotation, in degrees.
	 */
	public void rotate(final float theAngle){
		rotate(theAngle,0.0f,0.0f,1.0f);
	}
	
	/**
	 * Produces a general scaling along the x, y, and z axes. The three arguments indicate 
	 * the desired scale factors along each of the three axes. If the matrix mode is either 
	 * MODELVIEW or PROJECTION, all objects drawn after scale is called are scaled. Use 
	 * pushMatrix and popMatrix to save and restore the unscaled coordinate system.<br>
	 * Scale() is the only one of the three modeling transformations that changes the apparent 
	 * size of an object: Scaling with values greater than 1.0 stretches an object, and using 
	 * values less than 1.0 shrinks it. Scaling with a -1.0 value reflects an object across an 
	 * axis. The identity values for scaling are (1.0, 1.0, 1.0). In general, you should limit 
	 * your use of scale() to those cases where it is necessary. Using scale() decreases the 
	 * performance of lighting calculations, because the normal vectors have to be renormalized 
	 * after transformation.<br>
	 * A scale value of zero collapses all object coordinates along that axis to zero. It's 
	 * usually not a good idea to do this, because such an operation cannot be undone. 
	 * Mathematically speaking, the matrix cannot be inverted, and inverse matrices are required 
	 * for certain lighting operations. Sometimes collapsing coordinates does make sense, however; 
	 * the calculation of shadows on a planar surface is a typical application. In general, if a 
	 * coordinate system is to be collapsed, the projection matrix should be used rather than 
	 * the modelview matrix. 
	 * @param theX float, scale factor along the x axis
	 * @param theY float, scale factor along the y axis
	 * @param theZ float, scale factor along the z axis
	 */
	public void scale(final float theX, final float theY, final float theZ) {
		gl.glScalef(theX, theY, theZ);
	}

	public void scale(final float theX, final float theY) {
		gl.glScalef(theX, theY, 1);
	}

	public void scale(final float theSize) {
		gl.glScalef(theSize, theSize, theSize);
	}

	public void scale(final double theSize) {
		gl.glScaled(theSize, theSize, theSize);
	}
	
	/**
	 * There is a stack of matrices for each of the matrix modes. In MODELVIEW mode, the stack depth 
	 * is at least 32. In the other two modes, PROJECTION and TEXTURE, the depth is at least 2. 
	 * The current matrix in any mode is the matrix on the top of the stack for that mode.<br>
	 * pushMatrix pushes the current matrix stack down by one, duplicating the current matrix. 
	 * That is, after a pushMatrix call, the matrix on the top of the stack is identical to the one below it.<br>
	 * popMatrix pops the current matrix stack, replacing the current matrix with the one below it on the stack.
	 * Initially, each of the stacks contains one matrix, an identity matrix. 
	 *
	 */
	public void pushMatrix(){
		gl.glPushMatrix();
	}
	
	/**
	 * There is a stack of matrices for each of the matrix modes. In MODELVIEW mode, the stack depth 
	 * is at least 32. In the other two modes, PROJECTION and TEXTURE, the depth is at least 2. 
	 * The current matrix in any mode is the matrix on the top of the stack for that mode.<br>
	 * pushMatrix pushes the current matrix stack down by one, duplicating the current matrix. 
	 * That is, after a pushMatrix call, the matrix on the top of the stack is identical to the one below it.<br>
	 * popMatrix pops the current matrix stack, replacing the current matrix with the one below it on the stack.
	 * Initially, each of the stacks contains one matrix, an identity matrix. 
	 */
	public void popMatrix(){
		gl.glPopMatrix();
	}
	
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	//
	// PRINTING OF MATRIZES
	//
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////

	private void printMatrixBuffer(final FloatBuffer theMatrix){
		int big = 0;

		for (int i = 0; i < 16; i++){
			big = Math.max(big, (int) Math.abs(theMatrix.get(i)));
		}

		// avoid infinite loop
		if (Float.isNaN(big) || Float.isInfinite(big)){
			big = 1000000; // set to something arbitrary
		}

		int d = 1;
		while ((big /= 10) != 0)
			d++; // cheap log()

		for (int i = 0; i < 16; i += 4){
			System.out.println(
				CCFormatUtil.nfs(theMatrix.get(i), d, 4) + " " + 
				CCFormatUtil.nfs(theMatrix.get(i + 1), d, 4) + " " + 
				CCFormatUtil.nfs(theMatrix.get(i + 2), d, 4) + " " + 
				CCFormatUtil.nfs(theMatrix.get(i + 3), d, 4));
		}
		System.out.println();
	}

	/**
	 * Prints out the given matrix in a nice format
	 */
	public void printGLMatrix(final CCMatrixMode theMatrixMode){
		printMatrixBuffer(getFloatBuffer(theMatrixMode.glMatrixID, 16));
	}

	/**
	 * Prints the current modelview matrix.
	 */
	public void printMatrix(){
		printGLMatrix(CCMatrixMode.MODELVIEW);
	}

	/**
	 * Prints the current projection matrix.
	 */
	public void printProjectionMatrix(){
		printGLMatrix(CCMatrixMode.PROJECTION);
	}
	
	/**
	 * Prints the current projection matrix.
	 */
	public void printTextureMatrix(){
		printGLMatrix(CCMatrixMode.PROJECTION);
	}
	
	public CCMatrix4f projectionMatrix(){
		return CCMatrix4f.createFromGLMatrix(getFloatBuffer(CCMatrixMode.PROJECTION.glMatrixID, 16));
	}
	
	public CCMatrix4f textureMatrix(){
		return CCMatrix4f.createFromGLMatrix(getFloatBuffer(CCMatrixMode.TEXTURE.glMatrixID, 16));
	}
	
	public CCMatrix4f modelviewMatrix(){
		return CCMatrix4f.createFromGLMatrix(getFloatBuffer(CCMatrixMode.MODELVIEW.glMatrixID, 16));
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// DRAWING
	//
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static enum CCAttributeMask{
		/**
		 * Accumulation buffer clear value
		 */
		ACCUM_BUFFER(GL2.GL_ACCUM_BUFFER_BIT),
		/**
		 * GL_ALPHA_TEST enable bit
		 * Alpha test function and reference value
		 * GL_BLEND enable bit
		 * Blending source and destination functions
		 * Constant blend color
		 * Blending equation
		 * GL_DITHER enable bit
		 * GL_DRAW_BUFFER setting
		 * GL_COLOR_LOGIC_OP enable bit
		 * GL_INDEX_LOGIC_OP enable bit
		 * Logic op function
		 * Color mode and index mode clear values
		 * Color mode and index mode writemasks
		 */
		COLOR_BUFFER(GL.GL_COLOR_BUFFER_BIT);
		
		int glID;
		
		private CCAttributeMask(final int theGlID){
			glID = theGlID;
		}
	}
	
	public static enum CCAttributeBit{
		
	}
	
	/**
	 * Saves the current attributes on the attribute stack so that they
	 * can be restored using popAttribute after made changes.
	 * @shortdesc push and pop the server attribute stack 
	 * @see #popAttribute()
	 */
	public void pushAttribute() {
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
	}
	
	/**
	 * popAttribute restores the values of the state variables saved with the last
	 * pushAttribute command. Those not saved are left unchanged. It is an error 
	 * to push attributes onto a full stack or to pop attributes off an empty stack.
	 * In either case, the error flag is set and no other change is made. Initially, 
	 * the attribute stack is empty.
	 * @shortdesc push and pop the server attribute stack 
	 * @see #pushMatrix()
	 */
	public void popAttribute() {
		gl.glPopAttrib();
	}
	
	/**
	 * Specifies whether front- or back-facing facets are candidates for culling.
	 */
	public static enum CCCullFace{
		FRONT(GL.GL_FRONT),
		BACK(GL.GL_BACK),
		FRONT_AND_BACK(GL.GL_FRONT_AND_BACK);
		
		int glID;
		
		private CCCullFace(final int theGlID){
			glID = theGlID;
		}
	}
	
	/**
	 * <p>
	 * specify whether front- or back-facing facets can be culled. cullFace enables culling and 
	 * specifies whether front- or back-facing facets are culled (as specified by mode). 
	 * Facet culling is initially disabled. Facets include triangles, quadrilaterals,
	 * polygons, and rectangles.
	 * </p>
	 * <p>
	 * {@link #frontFace(CCFace)} specifies which of the clockwise and counterclockwise facets
	 * are front-facing and back-facing.
	 * </p>
	 * @param theFace 
	 * 		Specifies whether front- or back-facing facets are candidates for culling.
	 * 		CCCullFace.FRONT, CCCullFace.BACK, and CCCullFace.FRONT_AND_BACK are accepted.
	 * 		The initial value is CCCullFace.BACK.
	 * @see #frontFace(CCFace)
	 * @see #noCullFace()
	 */
	public void cullFace(CCCullFace theFace){
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(theFace.glID);
	}
	
	public void noCullFace(){
		gl.glDisable(GL.GL_CULL_FACE);
	}
	
	public static enum CCFace{
		CLOCK_WISE(GL.GL_CW),
		COUNTER_CLOCK_WISE(GL.GL_CCW);
		
		int glID;
		
		private CCFace(final int theGlID){
			glID = theGlID;
		}
	}
	
	/**
	 * <p>
	 * define front- and back-facing polygons. In a scene composed entirely of opaque closed surfaces,
	 * back-facing polygons are never visible. Eliminating these invisible polygons has the obvious benefit
	 * of speeding up the rendering of the image. To enable and disable elimination of back-facing polygons, 
	 * call cullFace with the desired mode.
	 * </p>
	 * <p>
	 * The projection of a polygon to window coordinates is said to have clockwise winding if an imaginary 
	 * object following the path from its first vertex, its second vertex, and so on, to its last vertex,
	 * and finally back to its first vertex, moves in a clockwise direction about the interior of the polygon.
	 * The polygon's winding is said to be counterclockwise if the imaginary object following the same path moves 
	 * in a counterclockwise direction about the interior of the polygon.
	 * </p>
	 * <p>
	 * frontFace specifies whether polygons with clockwise winding in window coordinates, or counterclockwise 
	 * winding in window coordinates, are taken to be front-facing. Passing CCFace.COUNTER_CLOCK_WISE to mode selects 
	 * counterclockwise polygons as front-facing; CCFace.CLOCK_WISE selects clockwise polygons as front-facing.
	 * By default, counterclockwise polygons are taken to be front-facing.
	 * </p>
	 * @param theFace 
	 * 		specifies the orientation of front-facing polygons.
	 * 		CCFace.CLOCK_WISE and CCFace.COUNTER_CLOCK_WISE are accepted.
	 * 		The initial value is CCFace.COUNTER_CLOCK_WISE.
	 * 
	 * @see #cullFace(CCCullFace)
	 */
	public void frontFace(final CCFace theFace) {
		gl.glFrontFace(theFace.glID);
	}
	
	public static enum CCPolygonMode{
		POINT(GL2.GL_POINT),
		LINE(GL2.GL_LINE),
		FILL(GL2.GL_FILL);
		
		int glID;
		
		private CCPolygonMode(final int theGlID){
			glID = theGlID;
		}
	}
	
	/**
	 * This function allows you to change how polygons are rendered. By default, polygons are 
	 * filled or shaded with the current color or material properties. However, you may also 
	 * specify that only the outlines or only the vertices are drawn.
	 * @param thePolygonMode 
	 * Specifies the new drawing mode. 
	 * <ul>
	 * <li>FILL is the default, producing filled polygons. </li>
	 * <li>LINE produces polygon outlines, and </li>
	 * <li>POINT plots only the points of the vertices.</li>
	 * </ul>
	 */
	public void polygonMode(final CCPolygonMode thePolygonMode){
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, thePolygonMode.glID);
	}

	/**
	 * Treats each group of four vertices as an independent quadrilateral. 
	 * Vertices 4n-3, 4n-2, 4n-1, and 4n define quadrilateral n. N/4 quadrilaterals are drawn.
	 */
	static public final CCDrawMode QUADS = CCDrawMode.QUADS;

	/**
	 * Draws a connected group of quadrilaterals. One quadrilateral is defined for 
	 * each pair of vertices presented after the first pair. Vertices 2n-1, 2n, 2n+2, 
	 * and 2n+1 define quadrilateral n. N/2-1 quadrilaterals are drawn. 
	 * Note that the order in which vertices are used to construct a quadrilateral 
	 * from strip data is different from that used with independent data.
	 */
	static public final CCDrawMode QUAD_STRIP = CCDrawMode.QUAD_STRIP;

	/**
	 * Draws a single, convex polygon. Vertices 1 through N define this polygon.
	 */
	static public final CCDrawMode POLYGON = CCDrawMode.POLYGON;
	
	/**
	 * beginShape and endShape delimit the vertices that define a primitive or a group of like primitives. 
	 * beginShape accepts a single argument that specifies which of ten ways the vertices are interpreted. 
	 * @param theDrawMode
	 */
	public void beginShape(final CCDrawMode theDrawMode){
		gl.glBegin(theDrawMode.glID);
	}
	
	/**
	 * beginShape and endShape delimit the vertices that define a primitive or a group of like primitives. 
	 * beginShape accepts a single argument that specifies which of ten ways the vertices are interpreted.
	 */
	public void endShape(){
		gl.glEnd();
	}

	/**
	 * beginShape and endShape delimit the vertices that define a primitive or a group of like primitives. 
	 * beginShape accepts a single argument that specifies which of ten ways the vertices are interpreted.
	 */
	public void beginShape(){
		beginShape(POLYGON);
	}

	/**
	 * Each vertex of a polygon, separate triangle, or separate quadrilateral specified between a 
	 * beginShape/endShape pair is marked as the start of either a boundary or nonboundary edge. 
	 * If edge is activated when the vertex is specified, the vertex is marked as the start of a 
	 * boundary edge. Otherwise, the vertex is marked as the start of a nonboundary edge.
	 * <br>
	 * The vertices of connected triangles and connected quadrilaterals are always marked as boundary, 
	 * regardless if edges is activated or not. Boundary and nonboundary edges on vertices are significant 
	 * only if polygonMode is set to POINT or LINE. Initially, the edges is activated. 
	 *
	 */
	public void edges(){
		gl.glEdgeFlag(true);
	}
	
	public void noEdges(){
		gl.glEdgeFlag(false);
	}
	
	/**
	 * With OpenGL, all geometric objects are ultimately described as an ordered set of vertices.
	 * Vertex commands are used within beginShape/endShape pairs to specify point, line, and polygon vertices. 
	 * The current color, normal, and texture coordinates are associated with the vertex when vertex is called.
	 * @param theX
	 * @param theY
	 */
	public void vertex(final float theX, final float theY) {
		gl.glVertex2f(theX, theY);
	}
	
	/**
	 * With OpenGL, all geometric objects are ultimately described as an ordered set of vertices.
	 * Vertex commands are used within beginShape/endShape pairs to specify point, line, and polygon vertices. 
	 * The current color, normal, and texture coordinates are associated with the vertex when vertex is called.
	 * @param theX
	 * @param theY
	 */
	public void vertex(final double theX, final double theY) {
		gl.glVertex2d(theX, theY);
	}
	
	/**
	 * 
	 * @param theX
	 * @param theY
	 * @param theZ
	 */
	public void vertex(final float theX, final float theY, final float theZ){
		gl.glVertex3f(theX,theY,theZ);
	}
	
	/**
	 * 
	 * @param theX
	 * @param theY
	 * @param theZ
	 */
	public void vertex(final double theX, final double theY, final double theZ){
		gl.glVertex3d(theX,theY,theZ);
	}
	
	/**
	 * 
	 * @param theVector
	 */
	public void vertex(final CCVector2f theVector){
		gl.glVertex2f(theVector.x, theVector.y);
	}
	
	/**
	 * 
	 * @param theVector
	 */
	public void vertex(final CCVector2d theVector){
		gl.glVertex2d(theVector.x, theVector.y);
	}
	
	
	
	/**
	 * 
	 * @param theVector
	 */
	public void vertex(final CCVector3f theVector){
		gl.glVertex3f(theVector.x, theVector.y, theVector.z);
	}
	
	/**
	 * 
	 * @param theVector
	 */
	public void vertex(final CCVector3d theVector){
		gl.glVertex3d(theVector.x, theVector.y, theVector.z);
	}
	
	public void vertex(final float theX, final float theY, final float theU, final float theV){
		textureCoords(theU,theV);
		gl.glVertex2f(theX,theY);
	}
	
	/**
	 * 
	 * @param theX
	 * @param theY
	 * @param theZ
	 */
	public void vertex(final float theX, final float theY, final float theZ, final float theU, final float theV){
		textureCoords(theU,theV);
		gl.glVertex3f(theX,theY,theZ);
	}
	
	/**
	 * 
	 * @param theVertex
	 */
	public void vertex(final CCVector2f theVertex,final CCVector2f theTextureCoords){
		textureCoords(theTextureCoords);
		gl.glVertex2f(theVertex.x, theVertex.y);
	}
	
	
	
	/**
	 * 
	 * @param i_v
	 */
	public void vertex(final CCVector3f theVertex, final CCVector2f theTextureCoords){
		textureCoords(theTextureCoords);
		gl.glVertex3f(theVertex.x, theVertex.y, theVertex.z);
	}
	
	/**
	 * Sets the current normal vector as specified by the arguments. You use
	 * normal() to set the current normal to the value of the argument passed in.
	 * Subsequent calls to vertex() cause the specified vertices to be assigned
	 * the current normal. Often, each vertex has a different normal, which
	 * necessitates a series of alternating calls.<br>
	 * A normal vector (or normal, for short) is a vector that points in a
	 * direction that's perpendicular to a surface. For a flat surface, one
	 * perpendicular direction suffices for every point on the surface, but for a
	 * general curved surface, the normal direction might be different at each
	 * point. With OpenGL, you can specify a normal for each vertex. Vertices
	 * might share the same normal, but you can't assign normals anywhere other
	 * than at the vertices.<br>
	 * An object's normal vectors define the orientation of its surface in space -
	 * in particular, its orientation relative to light sources. These vectors
	 * are used by OpenGL to determine how much light the object receives at its
	 * vertices.
	 * 
	 * @param theX float, x part of the normal
	 * @param theY float, y part of the normal
	 * @param theZ float, z part of the normal
	 */
	public void normal(final float theX, final float theY, final float theZ){
		gl.glNormal3f(theX,theY,theZ);
	}
	
	/**
	 * 
	 * @param theNormal Vector3D, vector with the normal
	 */
	public void normal(final CCVector3f theNormal){
		gl.glNormal3f(theNormal.x, theNormal.y, theNormal.z);
	}
	
	/**
	 * 
	 * @param theNormal Vector3D, vector with the normal
	 */
	public void normal(final CCVector3d theNormal){
		gl.glNormal3d(theNormal.x, theNormal.y, theNormal.z);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//  DRAWING BEZIER CURVES
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private int bezierDetail = 31;
	
	/**
	 * Sets the number of divisions for a beziercurve
	 * @param bezierDetail
	 */
	public void bezierDetail(final int bezierDetail){
		this.bezierDetail = bezierDetail;
	}
	
	/**
	 * Returns the bezier detail used to draw bezier curves
	 * @return
	 */
	public int bezierDetail(){
		return bezierDetail;
	}
	
	/**
	 * Draws a bezier curve with the given points.
	 * @param thePointBuffer
	 */
	private void bezier(final FloatBuffer thePointBuffer, final int theNumberOfPoints){
		gl.glMap1f(GL2.GL_MAP1_VERTEX_3,0.0f,bezierDetail,3,theNumberOfPoints,thePointBuffer);
		gl.glEnable(GL2.GL_MAP1_VERTEX_3);
		gl.glMapGrid1d(bezierDetail,0,bezierDetail);
		gl.glEvalMesh1(GL2.GL_LINE,0,bezierDetail);
	}
	
	/**
	 * Draws a Bezier curve on the screen. These curves are defined by a series of anchor 
	 * and control points. The first two parameters specify the first anchor point and the 
	 * last two parameters specify the other anchor point. The middle parameters specify 
	 * the control points which define the shape of the curve. Bezier curves were developed 
	 * by French engineer Pierre Bezier. 
	 * @param x1,y1,z1 coordinates for the first anchor point
	 * @param x2,y2,z2 coordinates for the first control point
	 * @param x3,y3,z3 coordinates for the second control point
	 * @param x4,y4,z4 coordinates for the second anchor point
	 */
	public void bezier(
		final float x1, final float y1, final float z1,
		final float x2, final float y2, final float z2,
		final float x3, final float y3, final float z3,
		final float x4, final float y4, final float z4
	){
		final FloatBuffer myPoints = FloatBuffer.allocate(12);
		myPoints.put(x1); myPoints.put(y1); myPoints.put(z1);
		myPoints.put(x2); myPoints.put(y2); myPoints.put(z2);
		myPoints.put(x3); myPoints.put(y3); myPoints.put(z3);
		myPoints.put(x4); myPoints.put(y4); myPoints.put(z4);
		myPoints.rewind();
		bezier(myPoints,4);
	}
	
	public void bezier(
		final float x1, final float y1,
		final float x2, final float y2,
		final float x3, final float y3,
		final float x4, final float y4
	){
		bezier(
			x1,y1,0,
			x2,y2,0,
			x3,y3,0,
			x4,y4,0
		);
	}
	
	/**
	 * @param v1 CCVector2f: vector with the x, y coordinates of the first anchor point
	 * @param v2 CCVector2f: vector with the x, y coordinates of the first control point
	 * @param v3 CCVector2f: vector with the x, y coordinates of the second control point
	 * @param v4 CCVector2f: vector with the x, y coordinates of the second anchor point
	 */
	public void bezier(
		final CCVector2f v1,
		final CCVector2f v2,
		final CCVector2f v3,
		final CCVector2f v4
	){
		final FloatBuffer floatbuffer = FloatBuffer.allocate(12);
		floatbuffer.put(v1.x);
		floatbuffer.put(v1.y);
		floatbuffer.put(0);
		floatbuffer.put(v2.x);
		floatbuffer.put(v2.y);
		floatbuffer.put(0);
		floatbuffer.put(v3.x);
		floatbuffer.put(v3.y);
		floatbuffer.put(0);
		floatbuffer.put(v4.x);
		floatbuffer.put(v4.y);
		floatbuffer.put(0);
		floatbuffer.rewind();
		bezier(floatbuffer,4);
	}
	
	/**
	 * @param v1 CCVector3f: vector with the x, y coords of the first anchor point
	 * @param v2 CCVector3f: vector with the x, y coords of the first controll point
	 * @param v3 CCVector3f: vector with the x, y coords of the second controll point
	 * @param v4 CCVector3f: vector with the x, y coords of the second anchor point
	 */
	public void bezier(
		final CCVector3f v1,
		final CCVector3f v2,
		final CCVector3f v3,
		final CCVector3f v4
	){
		final FloatBuffer floatbuffer = FloatBuffer.allocate(12);
		floatbuffer.put(v1.x);
		floatbuffer.put(v1.y);
		floatbuffer.put(v1.z);
		
		floatbuffer.put(v2.x);
		floatbuffer.put(v2.y);
		floatbuffer.put(v2.z);
		
		floatbuffer.put(v3.x);
		floatbuffer.put(v3.y);
		floatbuffer.put(v3.z);
		
		floatbuffer.put(v4.x);
		floatbuffer.put(v4.y);
		floatbuffer.put(v4.z);
		floatbuffer.rewind();
		bezier(floatbuffer,4);
	}
	
	private float[] _myBezierCoords;
	private int _myNumberOfBezierCoords;
	
	private float _myLastBezierAnchorX;
	private float _myLastBezierAnchorY;
	private float _myLastBezierAnchorZ;
	
	private boolean _myIsAnchorDefined;
	
	public void beginBezier(){
		_myBezierCoords = new float[300];
		_myNumberOfBezierCoords = 0;
		_myIsAnchorDefined = false;
	}
	 
	public void endBezier(){
		gl.glMap1f(GL2.GL_MAP1_VERTEX_3,0.0f,bezierDetail,3,31,FloatBuffer.wrap(_myBezierCoords));
		gl.glEnable(GL2.GL_MAP1_VERTEX_3);
		gl.glMapGrid1d(bezierDetail,0,bezierDetail);
		gl.glEvalMesh1(GL2.GL_LINE,0,bezierDetail);
	}

	public void bezierVertex(
		final float x1, final float y1, final float z1, 
		final float x2, final float y2, final float z2, 
		final float x3, final float y3, final float z3, 
		final float x4, final float y4, final float z4
	){
		_myBezierCoords[_myNumberOfBezierCoords++] = x1;
		_myBezierCoords[_myNumberOfBezierCoords++] = y1;
		_myBezierCoords[_myNumberOfBezierCoords++] = z1;
		
		_myBezierCoords[_myNumberOfBezierCoords++] = x2;
		_myBezierCoords[_myNumberOfBezierCoords++] = y2;
		_myBezierCoords[_myNumberOfBezierCoords++] = z2;
		
		_myBezierCoords[_myNumberOfBezierCoords++] = x3;
		_myBezierCoords[_myNumberOfBezierCoords++] = y3;
		_myBezierCoords[_myNumberOfBezierCoords++] = z3;
		
		_myBezierCoords[_myNumberOfBezierCoords++] = x4;
		_myBezierCoords[_myNumberOfBezierCoords++] = y4;
		_myBezierCoords[_myNumberOfBezierCoords++] = z4;
		
		_myLastBezierAnchorX = x4;
		_myLastBezierAnchorY = y4;
		_myLastBezierAnchorZ = z4;
		
		_myIsAnchorDefined = true;
	}
	
	public void bezierVertex(
		final float x2, final float y2, final float z2, 
		final float x3, final float y3, final float z3, 
		final float x4, final float y4, final float z4
	){
		if(!_myIsAnchorDefined){
			throw new RuntimeException("You have to define a bezierVertex with two anchorpoints first!");
		}
		bezierVertex(
			_myLastBezierAnchorX,
			_myLastBezierAnchorY,
			_myLastBezierAnchorZ,
			x2, y2, z2, 
			x3, y3, z3, 
			x4, y4, z4
		);
	}
	
	public void bezierVertex(
		final float x1, final float y1, 
		final float x2, final float y2, 
		final float x3, final float y3, 
		final float x4, final float y4
	){
		bezierVertex(
			x1, y1, 0,
			x2, y2, 0, 
			x3, y3, 0, 
			x4, y4, 0
		);
	}
	
	public void bezierVertex(
		final float x2, final float y2, 
		final float x3, final float y3, 
		final float x4, final float y4
	){
		bezierVertex(
			x2, y2, 0, 
			x3, y3, 0, 
			x4, y4, 0
		);
	}


	public void bezierVertex(
		final CCVector3f v1, final CCVector3f v2, final CCVector3f v3, final CCVector3f v4
	){
		bezierVertex(
			v1.x, v1.y, v1.z, 
			v2.x, v2.y, v2.z, 
			v3.x, v3.y, v3.z,
			v4.x, v4.y, v4.z
		);
	}
	
	public void bezierVertex(
		final CCVector3f v1, final CCVector3f v2, final CCVector3f v3
	){
		bezierVertex(
			v1.x, v1.y, v1.z, 
			v2.x, v2.y, v2.z, 
			
