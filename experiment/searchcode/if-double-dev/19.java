/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-10 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation, version 2.1.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

package processing.core;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import processing.core.PShape;


/**
 * Base class for all sketches that use processing.core.
 * <p/>
 * Note that you should not use AWT or Swing components inside a Processing
 * applet. The surface is made to automatically update itself, and will cause
 * problems with redraw of components drawn above it. If you'd like to
 * integrate other Java components, see below.
 * <p/>
 * As of release 0145, Processing uses active mode rendering in all cases.
 * All animation tasks happen on the "Processing Animation Thread". The
 * setup() and draw() methods are handled by that thread, and events (like
 * mouse movement and key presses, which are fired by the event dispatch
 * thread or EDT) are queued to be (safely) handled at the end of draw().
 * For code that needs to run on the EDT, use SwingUtilities.invokeLater().
 * When doing so, be careful to synchronize between that code (since
 * invokeLater() will make your code run from the EDT) and the Processing
 * animation thread. Use of a callback function or the registerXxx() methods
 * in PApplet can help ensure that your code doesn't do something naughty.
 * <p/>
 * As of release 0136 of Processing, we have discontinued support for versions
 * of Java prior to 1.5. We don't have enough people to support it, and for a
 * project of our size, we should be focusing on the future, rather than
 * working around legacy Java code. In addition, Java 1.5 gives us access to
 * better timing facilities which will improve the steadiness of animation.
 * <p/>
 * This class extends Applet instead of JApplet because 1) historically,
 * we supported Java 1.1, which does not include Swing (without an
 * additional, sizable, download), and 2) Swing is a bloated piece of crap.
 * A Processing applet is a heavyweight AWT component, and can be used the
 * same as any other AWT component, with or without Swing.
 * <p/>
 * Similarly, Processing runs in a Frame and not a JFrame. However, there's
 * nothing to prevent you from embedding a PApplet into a JFrame, it's just
 * that the base version uses a regular AWT frame because there's simply
 * no need for swing in that context. If people want to use Swing, they can
 * embed themselves as they wish.
 * <p/>
 * It is possible to use PApplet, along with core.jar in other projects.
 * In addition to enabling you to use Java 1.5+ features with your sketch,
 * this also allows you to embed a Processing drawing area into another Java
 * application. This means you can use standard GUI controls with a Processing
 * sketch. Because AWT and Swing GUI components cannot be used on top of a
 * PApplet, you can instead embed the PApplet inside another GUI the way you
 * would any other Component.
 * <p/>
 * It is also possible to resize the Processing window by including
 * <tt>frame.setResizable(true)</tt> inside your <tt>setup()</tt> method.
 * Note that the Java method <tt>frame.setSize()</tt> will not work unless
 * you first set the frame to be resizable.
 * <p/>
 * Because the default animation thread will run at 60 frames per second,
 * an embedded PApplet can make the parent sluggish. You can use frameRate()
 * to make it update less often, or you can use noLoop() and loop() to disable
 * and then re-enable looping. If you want to only update the sketch
 * intermittently, use noLoop() inside setup(), and redraw() whenever
 * the screen needs to be updated once (or loop() to re-enable the animation
 * thread). The following example embeds a sketch and also uses the noLoop()
 * and redraw() methods. You need not use noLoop() and redraw() when embedding
 * if you want your application to animate continuously.
 * <PRE>
 * public class ExampleFrame extends Frame {
 *
 *     public ExampleFrame() {
 *         super("Embedded PApplet");
 *
 *         setLayout(new BorderLayout());
 *         PApplet embed = new Embedded();
 *         add(embed, BorderLayout.CENTER);
 *
 *         // important to call this whenever embedding a PApplet.
 *         // It ensures that the animation thread is started and
 *         // that other internal variables are properly set.
 *         embed.init();
 *     }
 * }
 *
 * public class Embedded extends PApplet {
 *
 *     public void setup() {
 *         // original setup code here ...
 *         size(400, 400);
 *
 *         // prevent thread from starving everything else
 *         noLoop();
 *     }
 *
 *     public void draw() {
 *         // drawing code goes here
 *     }
 *
 *     public void mousePressed() {
 *         // do something based on mouse movement
 *
 *         // update the screen (run draw once)
 *         redraw();
 *     }
 * }
 * </PRE>
 *
 * <H2>Processing on multiple displays</H2>
 * <P>I was asked about Processing with multiple displays, and for lack of a
 * better place to document it, things will go here.</P>
 * <P>You can address both screens by making a window the width of both,
 * and the height of the maximum of both screens. In this case, do not use
 * present mode, because that's exclusive to one screen. Basically it'll
 * give you a PApplet that spans both screens. If using one half to control
 * and the other half for graphics, you'd just have to put the 'live' stuff
 * on one half of the canvas, the control stuff on the other. This works
 * better in windows because on the mac we can't get rid of the menu bar
 * unless it's running in present mode.</P>
 * <P>For more control, you need to write straight java code that uses p5.
 * You can create two windows, that are shown on two separate screens,
 * that have their own PApplet. this is just one of the tradeoffs of one of
 * the things that we don't support in p5 from within the environment
 * itself (we must draw the line somewhere), because of how messy it would
 * get to start talking about multiple screens. It's also not that tough to
 * do by hand w/ some Java code.</P>
 * @usage Web &amp; Application
 */
public class PApplet extends Applet
  implements PConstants, Runnable,
             MouseListener, MouseMotionListener, KeyListener, FocusListener
{
  /**
   * Full name of the Java version (i.e. 1.5.0_11).
   * Prior to 0125, this was only the first three digits.
   */
  public static final String javaVersionName =
    System.getProperty("java.version");

  /**
   * Version of Java that's in use, whether 1.1 or 1.3 or whatever,
   * stored as a float.
   * <P>
   * Note that because this is stored as a float, the values may
   * not be <EM>exactly</EM> 1.3 or 1.4. Instead, make sure you're
   * comparing against 1.3f or 1.4f, which will have the same amount
   * of error (i.e. 1.40000001). This could just be a double, but
   * since Processing only uses floats, it's safer for this to be a float
   * because there's no good way to specify a double with the preproc.
   */
  public static final float javaVersion =
    new Float(javaVersionName.substring(0, 3)).floatValue();

  /**
   * Current platform in use.
   * <P>
   * Equivalent to System.getProperty("os.name"), just used internally.
   */

  /**
   * Current platform in use, one of the
   * PConstants WINDOWS, MACOSX, MACOS9, LINUX or OTHER.
   */
  static public int platform;

  /**
   * Name associated with the current 'platform' (see PConstants.platformNames)
   */
  //static public String platformName;

  static {
    String osname = System.getProperty("os.name");

    if (osname.indexOf("Mac") != -1) {
      platform = MACOSX;

    } else if (osname.indexOf("Windows") != -1) {
      platform = WINDOWS;

    } else if (osname.equals("Linux")) {  // true for the ibm vm
      platform = LINUX;

    } else {
      platform = OTHER;
    }
  }

  /**
   * Modifier flags for the shortcut key used to trigger menus.
   * (Cmd on Mac OS X, Ctrl on Linux and Windows)
   */
  static public final int MENU_SHORTCUT =
    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

  /** The PGraphics renderer associated with this PApplet */
  public PGraphics g;

  //protected Object glock = new Object(); // for sync

  /** The frame containing this applet (if any) */
  public Frame frame;

  /**
   * The screen size when the applet was started.
   * <P>
   * Access this via screen.width and screen.height. To make an applet
   * run at full screen, use size(screen.width, screen.height).
   * <P>
   * If you have multiple displays, this will be the size of the main
   * display. Running full screen across multiple displays isn't
   * particularly supported, and requires more monkeying with the values.
   * This probably can't/won't be fixed until/unless I get a dual head
   * system.
   * <P>
   * Note that this won't update if you change the resolution
   * of your screen once the the applet is running.
   * <p>
   * This variable is not static, because future releases need to be better
   * at handling multiple displays.
   */
  public Dimension screen =
    Toolkit.getDefaultToolkit().getScreenSize();

  /**
   * A leech graphics object that is echoing all events.
   */
  public PGraphics recorder;

  /**
   * Command line options passed in from main().
   * <P>
   * This does not include the arguments passed in to PApplet itself.
   */
  public String args[];

  /** Path to sketch folder */
  public String sketchPath; //folder;

  /** When debugging headaches */
  static final boolean THREAD_DEBUG = false;

  /** Default width and height for applet when not specified */
  static public final int DEFAULT_WIDTH = 100;
  static public final int DEFAULT_HEIGHT = 100;

  /**
   * Minimum dimensions for the window holding an applet.
   * This varies between platforms, Mac OS X 10.3 can do any height
   * but requires at least 128 pixels width. Windows XP has another
   * set of limitations. And for all I know, Linux probably lets you
   * make windows with negative sizes.
   */
  static public final int MIN_WINDOW_WIDTH = 128;
  static public final int MIN_WINDOW_HEIGHT = 128;

  /**
   * Exception thrown when size() is called the first time.
   * <P>
   * This is used internally so that setup() is forced to run twice
   * when the renderer is changed. This is the only way for us to handle
   * invoking the new renderer while also in the midst of rendering.
   */
  static public class RendererChangeException extends RuntimeException { }

  /**
   * true if no size() command has been executed. This is used to wait until
   * a size has been set before placing in the window and showing it.
   */
  public boolean defaultSize;

  volatile boolean resizeRequest;
  volatile int resizeWidth;
  volatile int resizeHeight;

  /**
   * Array containing the values for all the pixels in the display window. These values are of the color datatype. This array is the size of the display window. For example, if the image is 100x100 pixels, there will be 10000 values and if the window is 200x300 pixels, there will be 60000 values. The <b>index</b> value defines the position of a value within the array. For example, the statment <b>color b = pixels[230]</b> will set the variable <b>b</b> to be equal to the value at that location in the array. <br><br> Before accessing this array, the data must loaded with the <b>loadPixels()</b> function. After the array data has been modified, the <b>updatePixels()</b> function must be run to update the changes. Without <b>loadPixels()</b>, running the code may (or will in future releases) result in a NullPointerException.
   * Pixel buffer from this applet's PGraphics.
   * <P>
   * When used with OpenGL or Java2D, this value will
   * be null until loadPixels() has been called.
   *
   * @webref image:pixels
   * @see processing.core.PApplet#loadPixels()
   * @see processing.core.PApplet#updatePixels()
   * @see processing.core.PApplet#get(int, int, int, int)
   * @see processing.core.PApplet#set(int, int, int)
   * @see processing.core.PImage
   */
  public int pixels[];

  /** width of this applet's associated PGraphics
   * @webref environment
   */
  public int width;

  /** height of this applet's associated PGraphics
   * @webref environment
   * */
  public int height;

  /**
   * The system variable <b>mouseX</b> always contains the current horizontal coordinate of the mouse.
   * @webref input:mouse
   * @see PApplet#mouseY
   * @see PApplet#mousePressed
   * @see PApplet#mousePressed()
   * @see PApplet#mouseReleased()
   * @see PApplet#mouseMoved()
   * @see PApplet#mouseDragged()
   *
   * */
  public int mouseX;

  /**
   * The system variable <b>mouseY</b> always contains the current vertical coordinate of the mouse.
   * @webref input:mouse
   * @see PApplet#mouseX
   * @see PApplet#mousePressed
   * @see PApplet#mousePressed()
   * @see PApplet#mouseReleased()
   * @see PApplet#mouseMoved()
   * @see PApplet#mouseDragged()
   * */
  public int mouseY;

  /**
   * Previous x/y position of the mouse. This will be a different value
   * when inside a mouse handler (like the mouseMoved() method) versus
   * when inside draw(). Inside draw(), pmouseX is updated once each
   * frame, but inside mousePressed() and friends, it's updated each time
   * an event comes through. Be sure to use only one or the other type of
   * means for tracking pmouseX and pmouseY within your sketch, otherwise
   * you're gonna run into trouble.
   * @webref input:mouse
   * @see PApplet#pmouseY
   * @see PApplet#mouseX
   * @see PApplet#mouseY
   */
  public int pmouseX;

  /**
   * @webref input:mouse
   * @see PApplet#pmouseX
   * @see PApplet#mouseX
   * @see PApplet#mouseY
   */
  public int pmouseY;

  /**
   * previous mouseX/Y for the draw loop, separated out because this is
   * separate from the pmouseX/Y when inside the mouse event handlers.
   */
  protected int dmouseX, dmouseY;

  /**
   * pmouseX/Y for the event handlers (mousePressed(), mouseDragged() etc)
   * these are different because mouse events are queued to the end of
   * draw, so the previous position has to be updated on each event,
   * as opposed to the pmouseX/Y that's used inside draw, which is expected
   * to be updated once per trip through draw().
   */
  protected int emouseX, emouseY;

  /**
   * Used to set pmouseX/Y to mouseX/Y the first time mouseX/Y are used,
   * otherwise pmouseX/Y are always zero, causing a nasty jump.
   * <P>
   * Just using (frameCount == 0) won't work since mouseXxxxx()
   * may not be called until a couple frames into things.
   */
  public boolean firstMouse;

  /**
   * Processing automatically tracks if the mouse button is pressed and which button is pressed.
   * The value of the system variable <b>mouseButton</b> is either <b>LEFT</b>, <b>RIGHT</b>, or <b>CENTER</b> depending on which button is pressed.
   * <h3>Advanced:</h3>
   * If running on Mac OS, a ctrl-click will be interpreted as
   * the righthand mouse button (unlike Java, which reports it as
   * the left mouse).
   * @webref input:mouse
   * @see PApplet#mouseX
   * @see PApplet#mouseY
   * @see PApplet#mousePressed()
   * @see PApplet#mouseReleased()
   * @see PApplet#mouseMoved()
   * @see PApplet#mouseDragged()
   */
  public int mouseButton;

  /**
   * Variable storing if a mouse button is pressed. The value of the system variable <b>mousePressed</b> is true if a mouse button is pressed and false if a button is not pressed.
   * @webref input:mouse
   * @see PApplet#mouseX
   * @see PApplet#mouseY
   * @see PApplet#mouseReleased()
   * @see PApplet#mouseMoved()
   * @see PApplet#mouseDragged()
   */
  public boolean mousePressed;
  public MouseEvent mouseEvent;

  /**
   * The system variable <b>key</b> always contains the value of the most recent key on the keyboard that was used (either pressed or released). <br><br>
   * For non-ASCII keys, use the <b>keyCode</b> variable.
   * The keys included in the ASCII specification (BACKSPACE, TAB, ENTER, RETURN, ESC, and DELETE) do not require checking to see if they key is coded, and you should simply use the <b>key</b> variable instead of <b>keyCode</b>
   * If you're making cross-platform projects, note that the ENTER key is commonly used on PCs and Unix and the RETURN key is used instead on Macintosh.
   * Check for both ENTER and RETURN to make sure your program will work for all platforms.
   * =advanced
   *
   * Last key pressed.
   * <P>
   * If it's a coded key, i.e. UP/DOWN/CTRL/SHIFT/ALT,
   * this will be set to CODED (0xffff or 65535).
   * @webref input:keyboard
   * @see PApplet#keyCode
   * @see PApplet#keyPressed
   * @see PApplet#keyPressed()
   * @see PApplet#keyReleased()
   */
  public char key;

  /**
   * The variable <b>keyCode</b> is used to detect special keys such as the UP, DOWN, LEFT, RIGHT arrow keys and ALT, CONTROL, SHIFT.
   * When checking for these keys, it's first necessary to check and see if the key is coded. This is done with the conditional "if (key == CODED)" as shown in the example.
   * <br><br>The keys included in the ASCII specification (BACKSPACE, TAB, ENTER, RETURN, ESC, and DELETE) do not require checking to see if they key is coded, and you should simply use the <b>key</b> variable instead of <b>keyCode</b>
   * If you're making cross-platform projects, note that the ENTER key is commonly used on PCs and Unix and the RETURN key is used instead on Macintosh.
   * Check for both ENTER and RETURN to make sure your program will work for all platforms.
   * <br><br>For users familiar with Java, the values for UP and DOWN are simply shorter versions of Java's KeyEvent.VK_UP and KeyEvent.VK_DOWN.
   * Other keyCode values can be found in the Java <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/awt/event/KeyEvent.html">KeyEvent</a> reference.
   *
   * =advanced
   * When "key" is set to CODED, this will contain a Java key code.
   * <P>
   * For the arrow keys, keyCode will be one of UP, DOWN, LEFT and RIGHT.
   * Also available are ALT, CONTROL and SHIFT. A full set of constants
   * can be obtained from java.awt.event.KeyEvent, from the VK_XXXX variables.
   * @webref input:keyboard
   * @see PApplet#key
   * @see PApplet#keyPressed
   * @see PApplet#keyPressed()
   * @see PApplet#keyReleased()
   */
  public int keyCode;

  /**
   * The boolean system variable <b>keyPressed</b> is <b>true</b> if any key is pressed and <b>false</b> if no keys are pressed.
   * @webref input:keyboard
   * @see PApplet#key
   * @see PApplet#keyCode
   * @see PApplet#keyPressed()
   * @see PApplet#keyReleased()
   */
  public boolean keyPressed;

  /**
   * the last KeyEvent object passed into a mouse function.
   */
  public KeyEvent keyEvent;

  /**
   * Gets set to true/false as the applet gains/loses focus.
   * @webref environment
   */
  public boolean focused = false;

  /**
   * true if the applet is online.
   * <P>
   * This can be used to test how the applet should behave
   * since online situations are different (no file writing, etc).
   * @webref environment
   */
  public boolean online = false;

  /**
   * Time in milliseconds when the applet was started.
   * <P>
   * Used by the millis() function.
   */
  long millisOffset;

  /**
   * The current value of frames per second.
   * <P>
   * The initial value will be 10 fps, and will be updated with each
   * frame thereafter. The value is not instantaneous (since that
   * wouldn't be very useful since it would jump around so much),
   * but is instead averaged (integrated) over several frames.
   * As such, this value won't be valid until after 5-10 frames.
   */
  public float frameRate = 10;
  /** Last time in nanoseconds that frameRate was checked */
  protected long frameRateLastNanos = 0;

  /** As of release 0116, frameRate(60) is called as a default */
  protected float frameRateTarget = 60;
  protected long frameRatePeriod = 1000000000L / 60L;

  protected boolean looping;

  /** flag set to true when a redraw is asked for by the user */
  protected boolean redraw;

  /**
   * How many frames have been displayed since the applet started.
   * <P>
   * This value is read-only <EM>do not</EM> attempt to set it,
   * otherwise bad things will happen.
   * <P>
   * Inside setup(), frameCount is 0.
   * For the first iteration of draw(), frameCount will equal 1.
   */
  public int frameCount;

  /**
   * true if this applet has had it.
   */
  public boolean finished;

  /**
   * true if exit() has been called so that things shut down
   * once the main thread kicks off.
   */
  protected boolean exitCalled;

  Thread thread;

  protected RegisteredMethods sizeMethods;
  protected RegisteredMethods preMethods, drawMethods, postMethods;
  protected RegisteredMethods mouseEventMethods, keyEventMethods;
  protected RegisteredMethods disposeMethods;

  // messages to send if attached as an external vm

  /**
   * Position of the upper-lefthand corner of the editor window
   * that launched this applet.
   */
  static public final String ARGS_EDITOR_LOCATION = "--editor-location";

  /**
   * Location for where to position the applet window on screen.
   * <P>
   * This is used by the editor to when saving the previous applet
   * location, or could be used by other classes to launch at a
   * specific position on-screen.
   */
  static public final String ARGS_EXTERNAL = "--external";

  static public final String ARGS_LOCATION = "--location";

  static public final String ARGS_DISPLAY = "--display";

  static public final String ARGS_BGCOLOR = "--bgcolor";

  static public final String ARGS_PRESENT = "--present";

  static public final String ARGS_EXCLUSIVE = "--exclusive";

  static public final String ARGS_STOP_COLOR = "--stop-color";

  static public final String ARGS_HIDE_STOP = "--hide-stop";

  /**
   * Allows the user or PdeEditor to set a specific sketch folder path.
   * <P>
   * Used by PdeEditor to pass in the location where saveFrame()
   * and all that stuff should write things.
   */
  static public final String ARGS_SKETCH_FOLDER = "--sketch-path";

  /**
   * When run externally to a PdeEditor,
   * this is sent by the applet when it quits.
   */
  //static public final String EXTERNAL_QUIT = "__QUIT__";
  static public final String EXTERNAL_STOP = "__STOP__";

  /**
   * When run externally to a PDE Editor, this is sent by the applet
   * whenever the window is moved.
   * <P>
   * This is used so that the editor can re-open the sketch window
   * in the same position as the user last left it.
   */
  static public final String EXTERNAL_MOVE = "__MOVE__";

  /** true if this sketch is being run by the PDE */
  boolean external = false;


  static final String ERROR_MIN_MAX =
    "Cannot use min() or max() on an empty array.";


  // during rev 0100 dev cycle, working on new threading model,
  // but need to disable and go conservative with changes in order
  // to get pdf and audio working properly first.
  // for 0116, the CRUSTY_THREADS are being disabled to fix lots of bugs.
  //static final boolean CRUSTY_THREADS = false; //true;


  public void init() {
//    println("Calling init()");

    // send tab keys through to the PApplet
    setFocusTraversalKeysEnabled(false);

    millisOffset = System.currentTimeMillis();

    finished = false; // just for clarity

    // this will be cleared by draw() if it is not overridden
    looping = true;
    redraw = true;  // draw this guy once
    firstMouse = true;

    // these need to be inited before setup
    sizeMethods = new RegisteredMethods();
    preMethods = new RegisteredMethods();
    drawMethods = new RegisteredMethods();
    postMethods = new RegisteredMethods();
    mouseEventMethods = new RegisteredMethods();
    keyEventMethods = new RegisteredMethods();
    disposeMethods = new RegisteredMethods();

    try {
      getAppletContext();
      online = true;
    } catch (NullPointerException e) {
      online = false;
    }

    try {
      if (sketchPath == null) {
        sketchPath = System.getProperty("user.dir");
      }
    } catch (Exception e) { }  // may be a security problem

    Dimension size = getSize();
    if ((size.width != 0) && (size.height != 0)) {
      // When this PApplet is embedded inside a Java application with other
      // Component objects, its size() may already be set externally (perhaps
      // by a LayoutManager). In this case, honor that size as the default.
      // Size of the component is set, just create a renderer.
      g = makeGraphics(size.width, size.height, getSketchRenderer(), null, true);
      // This doesn't call setSize() or setPreferredSize() because the fact
      // that a size was already set means that someone is already doing it.

    } else {
      // Set the default size, until the user specifies otherwise
      this.defaultSize = true;
      int w = getSketchWidth();
      int h = getSketchHeight();
      g = makeGraphics(w, h, getSketchRenderer(), null, true);
      // Fire component resize event
      setSize(w, h);
      setPreferredSize(new Dimension(w, h));
    }
    width = g.width;
    height = g.height;

    addListeners();

    // this is automatically called in applets
    // though it's here for applications anyway
    start();
  }


  public int getSketchWidth() {
    return DEFAULT_WIDTH;
  }


  public int getSketchHeight() {
    return DEFAULT_HEIGHT;
  }


  public String getSketchRenderer() {
    return JAVA2D;
  }


  /**
   * Called by the browser or applet viewer to inform this applet that it
   * should start its execution. It is called after the init method and
   * each time the applet is revisited in a Web page.
   * <p/>
   * Called explicitly via the first call to PApplet.paint(), because
   * PAppletGL needs to have a usable screen before getting things rolling.
   */
  public void start() {
    // When running inside a browser, start() will be called when someone
    // returns to a page containing this applet.
    // http://dev.processing.org/bugs/show_bug.cgi?id=581
    finished = false;

    if (thread != null) return;
    thread = new Thread(this, "Animation Thread");
    thread.start();
  }


  /**
   * Called by the browser or applet viewer to inform
   * this applet that it should stop its execution.
   * <p/>
   * Unfortunately, there are no guarantees from the Java spec
   * when or if stop() will be called (i.e. on browser quit,
   * or when moving between web pages), and it's not always called.
   */
  public void stop() {
    // bringing this back for 0111, hoping it'll help opengl shutdown
    finished = true;  // why did i comment this out?

    // don't run stop and disposers twice
    if (thread == null) return;
    thread = null;

    // call to shut down renderer, in case it needs it (pdf does)
    if (g != null) g.dispose();

    // maybe this should be done earlier? might help ensure it gets called
    // before the vm just craps out since 1.5 craps out so aggressively.
    disposeMethods.handle();
  }


  /**
   * Called by the browser or applet viewer to inform this applet
   * that it is being reclaimed and that it should destroy
   * any resources that it has allocated.
   * <p/>
   * This also attempts to call PApplet.stop(), in case there
   * was an inadvertent override of the stop() function by a user.
   * <p/>
   * destroy() supposedly gets called as the applet viewer
   * is shutting down the applet. stop() is called
   * first, and then destroy() to really get rid of things.
   * no guarantees on when they're run (on browser quit, or
   * when moving between pages), though.
   */
  public void destroy() {
    ((PApplet)this).stop();
  }


  /**
   * This returns the last width and height specified by the user
   * via the size() command.
   */
//  public Dimension getPreferredSize() {
//    return new Dimension(width, height);
//  }


//  public void addNotify() {
//    super.addNotify();
//    println("addNotify()");
//  }



  //////////////////////////////////////////////////////////////


  public class RegisteredMethods {
    int count;
    Object objects[];
    Method methods[];


    // convenience version for no args
    public void handle() {
      handle(new Object[] { });
    }

    public void handle(Object oargs[]) {
      for (int i = 0; i < count; i++) {
        try {
          //System.out.println(objects[i] + " " + args);
          methods[i].invoke(objects[i], oargs);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    public void add(Object object, Method method) {
      if (objects == null) {
        objects = new Object[5];
        methods = new Method[5];
      }
      if (count == objects.length) {
        objects = (Object[]) PApplet.expand(objects);
        methods = (Method[]) PApplet.expand(methods);
//        Object otemp[] = new Object[count << 1];
//        System.arraycopy(objects, 0, otemp, 0, count);
//        objects = otemp;
//        Method mtemp[] = new Method[count << 1];
//        System.arraycopy(methods, 0, mtemp, 0, count);
//        methods = mtemp;
      }
      objects[count] = object;
      methods[count] = method;
      count++;
    }


    /**
     * Removes first object/method pair matched (and only the first,
     * must be called multiple times if object is registered multiple times).
     * Does not shrink array afterwards, silently returns if method not found.
     */
    public void remove(Object object, Method method) {
      int index = findIndex(object, method);
      if (index != -1) {
        // shift remaining methods by one to preserve ordering
        count--;
        for (int i = index; i < count; i++) {
          objects[i] = objects[i+1];
          methods[i] = methods[i+1];
        }
        // clean things out for the gc's sake
        objects[count] = null;
        methods[count] = null;
      }
    }

    protected int findIndex(Object object, Method method) {
      for (int i = 0; i < count; i++) {
        if (objects[i] == object && methods[i].equals(method)) {
          //objects[i].equals() might be overridden, so use == for safety
          // since here we do care about actual object identity
          //methods[i]==method is never true even for same method, so must use
          // equals(), this should be safe because of object identity
          return i;
        }
      }
      return -1;
    }
  }


  public void registerSize(Object o) {
    Class<?> methodArgs[] = new Class[] { Integer.TYPE, Integer.TYPE };
    registerWithArgs(sizeMethods, "size", o, methodArgs);
  }

  public void registerPre(Object o) {
    registerNoArgs(preMethods, "pre", o);
  }

  public void registerDraw(Object o) {
    registerNoArgs(drawMethods, "draw", o);
  }

  public void registerPost(Object o) {
    registerNoArgs(postMethods, "post", o);
  }

  public void registerMouseEvent(Object o) {
    Class<?> methodArgs[] = new Class[] { MouseEvent.class };
    registerWithArgs(mouseEventMethods, "mouseEvent", o, methodArgs);
  }


  public void registerKeyEvent(Object o) {
    Class<?> methodArgs[] = new Class[] { KeyEvent.class };
    registerWithArgs(keyEventMethods, "keyEvent", o, methodArgs);
  }

  public void registerDispose(Object o) {
    registerNoArgs(disposeMethods, "dispose", o);
  }


  protected void registerNoArgs(RegisteredMethods meth,
                                String name, Object o) {
    Class<?> c = o.getClass();
    try {
      Method method = c.getMethod(name, new Class[] {});
      meth.add(o, method);

    } catch (NoSuchMethodException nsme) {
      die("There is no public " + name + "() method in the class " +
          o.getClass().getName());

    } catch (Exception e) {
      die("Could not register " + name + " + () for " + o, e);
    }
  }


  protected void registerWithArgs(RegisteredMethods meth,
                                  String name, Object o, Class<?> cargs[]) {
    Class<?> c = o.getClass();
    try {
      Method method = c.getMethod(name, cargs);
      meth.add(o, method);

    } catch (NoSuchMethodException nsme) {
      die("There is no public " + name + "() method in the class " +
          o.getClass().getName());

    } catch (Exception e) {
      die("Could not register " + name + " + () for " + o, e);
    }
  }


  public void unregisterSize(Object o) {
    Class<?> methodArgs[] = new Class[] { Integer.TYPE, Integer.TYPE };
    unregisterWithArgs(sizeMethods, "size", o, methodArgs);
  }

  public void unregisterPre(Object o) {
    unregisterNoArgs(preMethods, "pre", o);
  }

  public void unregisterDraw(Object o) {
    unregisterNoArgs(drawMethods, "draw", o);
  }

  public void unregisterPost(Object o) {
    unregisterNoArgs(postMethods, "post", o);
  }

  public void unregisterMouseEvent(Object o) {
    Class<?> methodArgs[] = new Class[] { MouseEvent.class };
    unregisterWithArgs(mouseEventMethods, "mouseEvent", o, methodArgs);
  }

  public void unregisterKeyEvent(Object o) {
    Class<?> methodArgs[] = new Class[] { KeyEvent.class };
    unregisterWithArgs(keyEventMethods, "keyEvent", o, methodArgs);
  }

  public void unregisterDispose(Object o) {
    unregisterNoArgs(disposeMethods, "dispose", o);
  }


  protected void unregisterNoArgs(RegisteredMethods meth,
                                  String name, Object o) {
    Class<?> c = o.getClass();
    try {
      Method method = c.getMethod(name, new Class[] {});
      meth.remove(o, method);
    } catch (Exception e) {
      die("Could not unregister " + name + "() for " + o, e);
    }
  }


  protected void unregisterWithArgs(RegisteredMethods meth,
                                    String name, Object o, Class<?> cargs[]) {
    Class<?> c = o.getClass();
    try {
      Method method = c.getMethod(name, cargs);
      meth.remove(o, method);
    } catch (Exception e) {
      die("Could not unregister " + name + "() for " + o, e);
    }
  }


  //////////////////////////////////////////////////////////////


  public void setup() {
  }


  public void draw() {
    // if no draw method, then shut things down
    //System.out.println("no draw method, goodbye");
    finished = true;
  }


  //////////////////////////////////////////////////////////////


  protected void resizeRenderer(int iwidth, int iheight) {
//    println("resizeRenderer request for " + iwidth + " " + iheight);
    if (width != iwidth || height != iheight) {
//      println("  former size was " + width + " " + height);
      g.setSize(iwidth, iheight);
      width = iwidth;
      height = iheight;
    }
  }


  /**
   * Defines the dimension of the display window in units of pixels. The <b>size()</b> function <em>must</em> be the first line in <b>setup()</b>. If <b>size()</b> is not called, the default size of the window is 100x100 pixels. The system variables <b>width</b> and <b>height</b> are set by the parameters passed to the <b>size()</b> function. <br><br>
   * Do not use variables as the parameters to <b>size()</b> command, because it will cause problems when exporting your sketch. When variables are used, the dimensions of your sketch cannot be determined during export. Instead, employ numeric values in the <b>size()</b> statement, and then use the built-in <b>width</b> and <b>height</b> variables inside your program when you need the dimensions of the display window are needed. <br><br>
   * The MODE parameters selects which rendering engine to use. For example, if you will be drawing 3D shapes for the web use <b>P3D</b>, if you want to export a program with OpenGL graphics acceleration use <b>OPENGL</b>. A brief description of the four primary renderers follows:<br><br><b>JAVA2D</b> - The default renderer. This renderer supports two dimensional drawing and provides higher image quality in overall, but generally slower than P2D.<br><br><b>P2D</b> (Processing 2D) - Fast 2D renderer, best used with pixel data, but not as accurate as the JAVA2D default. <br><br><b>P3D</b> (Processing 3D) - Fast 3D renderer for the web. Sacrifices rendering quality for quick 3D drawing.<br><br><b>OPENGL</b> - High speed 3D graphics renderer that makes use of OpenGL-compatible graphics hardware is available. Keep in mind that OpenGL is not magic pixie dust that makes any sketch faster (though it's close), so other rendering options may produce better results depending on the nature of your code. Also note that with OpenGL, all graphics are smoothed: the smooth() and noSmooth() commands are ignored. <br><br><b>PDF</b> - The PDF renderer draws 2D graphics directly to an Acrobat PDF file. This produces excellent results when you need vector shapes for high resolution output or printing. You must first use Import Library &rarr; PDF to make use of the library. More information can be found in the PDF library reference.
   * If you're manipulating pixels (using methods like get() or blend(), or manipulating the pixels[] array), P2D and P3D will usually be faster than the default (JAVA2D) setting, and often the OPENGL setting as well. Similarly, when handling lots of images, or doing video playback, P2D and P3D will tend to be faster.<br><br>
   * The P2D, P3D, and OPENGL renderers do not support strokeCap() or strokeJoin(), which can lead to ugly results when using strokeWeight(). (<a href="http://dev.processing.org/bugs/show_bug.cgi?id=955">Bug 955</a>) <br><br>
   * For the most elegant and accurate results when drawing in 2D, particularly when using smooth(), use the JAVA2D renderer setting. It may be slower than the others, but is the most complete, which is why it's the default. Advanced users will want to switch to other renderers as they learn the tradeoffs. <br><br>
   * Rendering graphics requires tradeoffs between speed, accuracy, and general usefulness of the available features. None of the renderers are perfect, so we provide multiple options so that you can decide what tradeoffs make the most sense for your project. We'd prefer all of them to have perfect visual accuracy, high performance, and support a wide range of features, but that's simply not possible. <br><br>
   * The maximum width and height is limited by your operating system, and is usually the width and height of your actual screen. On some machines it may simply be the number of pixels on your current screen, meaning that a screen that's 800x600 could support size(1600, 300), since it's the same number of pixels. This varies widely so you'll have to try different rendering modes and sizes until you get what you're looking for. If you need something larger, use <b>createGraphics</b> to create a non-visible drawing surface.
   * <br><br>Again, the size() method must be the first line of the code (or first item inside setup). Any code that appears before the size() command may run more than once, which can lead to confusing results.
   *
   * =advanced
   * Starts up and creates a two-dimensional drawing surface,
   * or resizes the current drawing surface.
   * <P>
   * This should be the first thing called inside of setup().
   * <P>
   * If using Java 1.3 or later, this will default to using
   * PGraphics2, the Java2D-based renderer. If using Java 1.1,
   * or if PGraphics2 is not available, then PGraphics will be used.
   * To set your own renderer, use the other version of the size()
   * method that takes a renderer as its last parameter.
   * <P>
   * If called once a renderer has already been set, this will
   * use the previous renderer and simply resize it.
   *
   * @webref structure
   * @param iwidth width of the display window in units of pixels
   * @param iheight height of the display window in units of pixels
   */
  public void size(int iwidth, int iheight) {
    size(iwidth, iheight, JAVA2D, null);
  }

  /**
   *
   * @param irenderer   Either P2D, P3D, JAVA2D, or OPENGL
   */
  public void size(int iwidth, int iheight, String irenderer) {
    size(iwidth, iheight, irenderer, null);
  }


  /**
   * Creates a new PGraphics object and sets it to the specified size.
   *
   * Note that you cannot change the renderer once outside of setup().
   * In most cases, you can call size() to give it a new size,
   * but you need to always ask for the same renderer, otherwise
   * you're gonna run into trouble.
   *
   * The size() method should *only* be called from inside the setup() or
   * draw() methods, so that it is properly run on the main animation thread.
   * To change the size of a PApplet externally, use setSize(), which will
   * update the component size, and queue a resize of the renderer as well.
   */
  public void size(final int iwidth, final int iheight,
                   String irenderer, String ipath) {
    // Run this from the EDT, just cuz it's AWT stuff (or maybe later Swing)
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // Set the preferred size so that the layout managers can handle it
        setPreferredSize(new Dimension(iwidth, iheight));
        setSize(iwidth, iheight);
      }
    });

    // ensure that this is an absolute path
    if (ipath != null) ipath = savePath(ipath);

    String currentRenderer = g.getClass().getName();
    if (currentRenderer.equals(irenderer)) {
      // Avoid infinite loop of throwing exception to reset renderer
      resizeRenderer(iwidth, iheight);
      //redraw();  // will only be called insize draw()

    } else {  // renderer is being changed
      // otherwise ok to fall through and create renderer below
      // the renderer is changing, so need to create a new object
      g = makeGraphics(iwidth, iheight, irenderer, ipath, true);
      width = iwidth;
      height = iheight;

      // fire resize event to make sure the applet is the proper size
//      setSize(iwidth, iheight);
      // this is the function that will run if the user does their own
      // size() command inside setup, so set defaultSize to false.
      defaultSize = false;

      // throw an exception so that setup() is called again
      // but with a properly sized render
      // this is for opengl, which needs a valid, properly sized
      // display before calling anything inside setup().
      throw new RendererChangeException();
    }
  }


  /**
   * Creates and returns a new <b>PGraphics</b> object of the types P2D, P3D, and JAVA2D. Use this class if you need to draw into an off-screen graphics buffer. It's not possible to use <b>createGraphics()</b> with OPENGL, because it doesn't allow offscreen use. The DXF and PDF renderers require the filename parameter.
   * <br><br>It's important to call any drawing commands between beginDraw() and endDraw() statements. This is also true for any commands that affect drawing, such as smooth() or colorMode().
   * <br><br>Unlike the main drawing surface which is completely opaque, surfaces created with createGraphics() can have transparency. This makes it possible to draw into a graphics and maintain the alpha channel. By using save() to write a PNG or TGA file, the transparency of the graphics object will be honored. Note that transparency levels are binary: pixels are either complete opaque or transparent. For the time being (as of release 0127), this means that text characters will be opaque blocks. This will be fixed in a future release (<a href="http://dev.processing.org/bugs/show_bug.cgi?id=641">Bug 641</a>).
   *
   * =advanced
   * Create an offscreen PGraphics object for drawing. This can be used
   * for bitmap or vector images drawing or rendering.
   * <UL>
   * <LI>Do not use "new PGraphicsXxxx()", use this method. This method
   * ensures that internal variables are set up properly that tie the
   * new graphics context back to its parent PApplet.
   * <LI>The basic way to create bitmap images is to use the <A
   * HREF="http://processing.org/reference/saveFrame_.html">saveFrame()</A>
   * function.
   * <LI>If you want to create a really large scene and write that,
   * first make sure that you've allocated a lot of memory in the Preferences.
   * <LI>If you want to create images that are larger than the screen,
   * you should create your own PGraphics object, draw to that, and use
   * <A HREF="http://processing.org/reference/save_.html">save()</A>.
   * For now, it's best to use <A HREF="http://dev.processing.org/reference/everything/javadoc/processing/core/PGraphics3D.html">P3D</A> in this scenario.
   * P2D is currently disabled, and the JAVA2D default will give mixed
   * results. An example of using P3D:
   * <PRE>
   *
   * PGraphics big;
   *
   * void setup() {
   *   big = createGraphics(3000, 3000, P3D);
   *
   *   big.beginDraw();
   *   big.background(128);
   *   big.line(20, 1800, 1800, 900);
   *   // etc..
   *   big.endDraw();
   *
   *   // make sure the file is written to the sketch folder
   *   big.save("big.tif");
   * }
   *
   * </PRE>
   * <LI>It's important to always wrap drawing to createGraphics() with
   * beginDraw() and endDraw() (beginFrame() and endFrame() prior to
   * revision 0115). The reason is that the renderer needs to know when
   * drawing has stopped, so that it can update itself internally.
   * This also handles calling the defaults() method, for people familiar
   * with that.
   * <LI>It's not possible to use createGraphics() with the OPENGL renderer,
   * because it doesn't allow offscreen use.
   * <LI>With Processing 0115 and later, it's possible to write images in
   * formats other than the default .tga and .tiff. The exact formats and
   * background information can be found in the developer's reference for
   * <A HREF="http://dev.processing.org/reference/core/javadoc/processing/core/PImage.html#save(java.lang.String)">PImage.save()</A>.
   * </UL>
   *
   * @webref rendering
   * @param iwidth width in pixels
   * @param iheight height in pixels
   * @param irenderer Either P2D (not yet implemented), P3D, JAVA2D, PDF, DXF
   *
   * @see processing.core.PGraphics
   *
   */
  public PGraphics createGraphics(int iwidth, int iheight,
                                  String irenderer) {
    PGraphics pg = makeGraphics(iwidth, iheight, irenderer, null, false);
    //pg.parent = this;  // make save() work
    return pg;
  }


  /**
   * Create an offscreen graphics surface for drawing, in this case
   * for a renderer that writes to a file (such as PDF or DXF).
   * @param ipath the name of the file (can be an absolute or relative path)
   */
  public PGraphics createGraphics(int iwidth, int iheight,
                                  String irenderer, String ipath) {
    if (ipath != null) {
      ipath = savePath(ipath);
    }
    PGraphics pg = makeGraphics(iwidth, iheight, irenderer, ipath, false);
    pg.parent = this;  // make save() work
    return pg;
  }


  /**
   * Version of createGraphics() used internally.
   *
   * @param ipath must be an absolute path, usually set via savePath()
   * @oaram applet the parent applet object, this should only be non-null
   *               in cases where this is the main drawing surface object.
   */
  protected PGraphics makeGraphics(int iwidth, int iheight,
                                   String irenderer, String ipath,
                                   boolean iprimary) {
    if (irenderer.equals(OPENGL)) {
      if (PApplet.platform == WINDOWS) {
        String s = System.getProperty("java.version");
        if (s != null) {
          if (s.equals("1.5.0_10")) {
            System.err.println("OpenGL support is broken with Java 1.5.0_10");
            System.err.println("See http://dev.processing.org" +
                               "/bugs/show_bug.cgi?id=513 for more info.");
            throw new RuntimeException("Please update your Java " +
                                       "installation (see bug #513)");
          }
        }
      }
    }

//    if (irenderer.equals(P2D)) {
//      throw new RuntimeException("The P2D renderer is currently disabled, " +
//                                 "please use P3D or JAVA2D.");
//    }

    String openglError =
      "Before using OpenGL, first select " +
      "Import Library > opengl from the Sketch menu.";

    try {
      /*
      Class<?> rendererClass = Class.forName(irenderer);

      Class<?> constructorParams[] = null;
      Object constructorValues[] = null;

      if (ipath == null) {
        constructorParams = new Class[] {
          Integer.TYPE, Integer.TYPE, PApplet.class
        };
        constructorValues = new Object[] {
          new Integer(iwidth), new Integer(iheight), this
        };
      } else {
        constructorParams = new Class[] {
          Integer.TYPE, Integer.TYPE, PApplet.class, String.class
        };
        constructorValues = new Object[] {
          new Integer(iwidth), new Integer(iheight), this, ipath
        };
      }

      Constructor<?> constructor =
        rendererClass.getConstructor(constructorParams);
      PGraphics pg = (PGraphics) constructor.newInstance(constructorValues);
      */

      Class<?> rendererClass =
        Thread.currentThread().getContextClassLoader().loadClass(irenderer);

      //Class<?> params[] = null;
      //PApplet.println(rendererClass.getConstructors());
      Constructor<?> constructor = rendererClass.getConstructor(new Class[] { });
      PGraphics pg = (PGraphics) constructor.newInstance();

      pg.setParent(this);
      pg.setPrimary(iprimary);
      if (ipath != null) pg.setPath(ipath);
      pg.setSize(iwidth, iheight);

      // everything worked, return it
      return pg;

    } catch (InvocationTargetException ite) {
      String msg = ite.getTargetException().getMessage();
      if ((msg != null) &&
          (msg.indexOf("no jogl in java.library.path") != -1)) {
        throw new RuntimeException(openglError +
                                   " (The native library is missing.)");

      } else {
        ite.getTargetException().printStackTrace();
        Throwable target = ite.getTargetException();
        if (platform == MACOSX) target.printStackTrace(System.out);  // bug
        // neither of these help, or work
        //target.printStackTrace(System.err);
        //System.err.flush();
        //System.out.println(System.err);  // and the object isn't null
        throw new RuntimeException(target.getMessage());
      }

    } catch (ClassNotFoundException cnfe) {
      if (cnfe.getMessage().indexOf("processing.opengl.PGraphicsGL") != -1) {
        throw new RuntimeException(openglError +
                                   " (The library .jar file is missing.)");
      } else {
        throw new RuntimeException("You need to use \"Import Library\" " +
                                   "to add " + irenderer + " to your sketch.");
      }

    } catch (Exception e) {
      //System.out.println("ex3");
      if ((e instanceof IllegalArgumentException) ||
          (e instanceof NoSuchMethodException) ||
          (e instanceof IllegalAccessException)) {
        e.printStackTrace();
        /*
        String msg = "public " +
          irenderer.substring(irenderer.lastIndexOf('.') + 1) +
          "(int width, int height, PApplet parent" +
          ((ipath == null) ? "" : ", String filename") +
          ") does not exist.";
          */
        String msg = irenderer + " needs to be updated " +
          "for the current release of Processing.";
        throw new RuntimeException(msg);

      } else {
        if (platform == MACOSX) e.printStackTrace(System.out);
        throw new RuntimeException(e.getMessage());
      }
    }
  }


  /**
   * Creates a new PImage (the datatype for storing images). This provides a fresh buffer of pixels to play with. Set the size of the buffer with the <b>width</b> and <b>height</b> parameters. The <b>format</b> parameter defines how the pixels are stored. See the PImage reference for more information.
   * <br><br>Be sure to include all three parameters, specifying only the width and height (but no format) will produce a strange error.
   * <br><br>Advanced users please note that createImage() should be used instead of the syntax <tt>new PImage()</tt>.
   * =advanced
   * Preferred method of creating new PImage objects, ensures that a
   * reference to the parent PApplet is included, which makes save() work
   * without needing an absolute path.
   *
   * @webref image
   * @param wide width in pixels
   * @param high height in pixels
   * @param format Either RGB, ARGB, ALPHA (grayscale alpha channel)
   *
   * @see processing.core.PImage
   * @see processing.core.PGraphics
   */
  public PImage createImage(int wide, int high, int format) {
    PImage image = new PImage(wide, high, format);
    image.parent = this;  // make save() work
    return image;
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public void update(Graphics screen) {
    paint(screen);
  }


  //synchronized public void paint(Graphics screen) {  // shutting off for 0146
  public void paint(Graphics screen) {
    // ignore the very first call to paint, since it's coming
    // from the o.s., and the applet will soon update itself anyway.
    if (frameCount == 0) {
//      println("Skipping frame");
      // paint() may be called more than once before things
      // are finally painted to the screen and the thread gets going
      return;
    }

    // without ignoring the first call, the first several frames
    // are confused because paint() gets called in the midst of
    // the initial nextFrame() call, so there are multiple
    // updates fighting with one another.

    // g.image is synchronized so that draw/loop and paint don't
    // try to fight over it. this was causing a randomized slowdown
    // that would cut the frameRate into a third on macosx,
    // and is probably related to the windows sluggishness bug too

    // make sure the screen is visible and usable
    // (also prevents over-drawing when using PGraphicsOpenGL)
    if ((g != null) && (g.image != null)) {
//      println("inside paint(), screen.drawImage()");
      screen.drawImage(g.image, 0, 0, null);
    }
  }


  // active paint method
  protected void paint() {
    try {
      Graphics screen = this.getGraphics();
      if (screen != null) {
        if ((g != null) && (g.image != null)) {
          screen.drawImage(g.image, 0, 0, null);
        }
        Toolkit.getDefaultToolkit().sync();
      }
    } catch (Exception e) {
      // Seen on applet destroy, maybe can ignore?
      e.printStackTrace();

//    } finally {
//      if (g != null) {
//        g.dispose();
//      }
    }
  }


  //////////////////////////////////////////////////////////////


  /**
   * Main method for the primary animation thread.
   *
   * <A HREF="http://java.sun.com/products/jfc/tsc/articles/painting/">Painting in AWT and Swing</A>
   */
  public void run() {  // not good to make this synchronized, locks things up
    long beforeTime = System.nanoTime();
    long overSleepTime = 0L;

    int noDelays = 0;
    // Number of frames with a delay of 0 ms before the
    // animation thread yields to other running threads.
    final int NO_DELAYS_PER_YIELD = 15;

    /*
      // this has to be called after the exception is thrown,
      // otherwise the supporting libs won't have a valid context to draw to
      Object methodArgs[] =
        new Object[] { new Integer(width), new Integer(height) };
      sizeMethods.handle(methodArgs);
     */

    while ((Thread.currentThread() == thread) && !finished) {
      // Don't resize the renderer from the EDT (i.e. from a ComponentEvent),
      // otherwise it may attempt a resize mid-render.
      if (resizeRequest) {
        resizeRenderer(resizeWidth, resizeHeight);
        resizeRequest = false;
      }

      // render a single frame
      handleDraw();

      if (frameCount == 1) {
        // Call the request focus event once the image is sure to be on
        // screen and the component is valid. The OpenGL renderer will
        // request focus for its canvas inside beginDraw().
        // http://java.sun.com/j2se/1.4.2/docs/api/java/awt/doc-files/FocusSpec.html
        //println("requesting focus");
        requestFocus();
      }

      // wait for update & paint to happen before drawing next frame
      // this is necessary since the drawing is sometimes in a
      // separate thread, meaning that the next frame will start
      // before the update/paint is completed

      long afterTime = System.nanoTime();
      long timeDiff = afterTime - beforeTime;
      //System.out.println("time diff is " + timeDiff);
      long sleepTime = (frameRatePeriod - timeDiff) - overSleepTime;

      if (sleepTime > 0) {  // some time left in this cycle
        try {
//          Thread.sleep(sleepTime / 1000000L);  // nanoseconds -> milliseconds
          Thread.sleep(sleepTime / 1000000L, (int) (sleepTime % 1000000L));
          noDelays = 0;  // Got some sleep, not delaying anymore
        } catch (InterruptedException ex) { }

        overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
        //System.out.println("  oversleep is " + overSleepTime);

      } else {    // sleepTime <= 0; the frame took longer than the period
//        excess -= sleepTime;  // store excess time value
        overSleepTime = 0L;

        if (noDelays > NO_DELAYS_PER_YIELD) {
          Thread.yield();   // give another thread a chance to run
          noDelays = 0;
        }
      }

      beforeTime = System.nanoTime();
    }

    stop();  // call to shutdown libs?

    // If the user called the exit() function, the window should close,
    // rather than the sketch just halting.
    if (exitCalled) {
      exit2();
    }
  }


  //synchronized public void handleDisplay() {
  public void handleDraw() {
    if (g != null && (looping || redraw)) {
      if (!g.canDraw()) {
        // Don't draw if the renderer is not yet ready.
        // (e.g. OpenGL has to wait for a peer to be on screen)
        return;
      }

      //System.out.println("handleDraw() " + frameCount);

      g.beginDraw();
      if (recorder != null) {
        recorder.beginDraw();
      }

      long now = System.nanoTime();

      if (frameCount == 0) {
        try {
          //println("Calling setup()");
          setup();
          //println("Done with setup()");

        } catch (RendererChangeException e) {
          // Give up, instead set the new renderer and re-attempt setup()
          return;
        }
        this.defaultSize = false;

      } else {  // frameCount > 0, meaning an actual draw()
        // update the current frameRate
        double rate = 1000000.0 / ((now - frameRateLastNanos) / 1000000.0);
        float instantaneousRate = (float) rate / 1000.0f;
        frameRate = (frameRate * 0.9f) + (instantaneousRate * 0.1f);

        preMethods.handle();

        // use dmouseX/Y as previous mouse pos, since this is the
        // last position the mouse was in during the previous draw.
        pmouseX = dmouseX;
        pmouseY = dmouseY;

        //println("Calling draw()");
        draw();
        //println("Done calling draw()");

        // dmouseX/Y is updated only once per frame (unlike emouseX/Y)
        dmouseX = mouseX;
        dmouseY = mouseY;

        // these are called *after* loop so that valid
        // drawing commands can be run inside them. it can't
        // be before, since a call to background() would wipe
        // out anything that had been drawn so far.
        dequeueMouseEvents();
        dequeueKeyEvents();

        drawMethods.handle();

        redraw = false;  // unset 'redraw' flag in case it was set
        // (only do this once draw() has run, not just setup())

      }

      g.endDraw();
      if (recorder != null) {
        recorder.endDraw();
      }

      frameRateLastNanos = now;
      frameCount++;

      // Actively render the screen
      paint();

//    repaint();
//    getToolkit().sync();  // force repaint now (proper method)

      postMethods.handle();
    }
  }


  //////////////////////////////////////////////////////////////



  synchronized public void redraw() {
    if (!looping) {
      redraw = true;
//      if (thread != null) {
//        // wake from sleep (necessary otherwise it'll be
//        // up to 10 seconds before update)
//        if (CRUSTY_THREADS) {
//          thread.interrupt();
//        } else {
//          synchronized (blocker) {
//            blocker.notifyAll();
//          }
//        }
//      }
    }
  }


  synchronized public void loop() {
    if (!looping) {
      looping = true;
    }
  }


  synchronized public void noLoop() {
    if (looping) {
      looping = false;
    }
  }


  //////////////////////////////////////////////////////////////


  public void addListeners() {
    addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);
    addFocusListener(this);

    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        Component c = e.getComponent();
        //System.out.println("componentResized() " + c);
        Rectangle bounds = c.getBounds();
        resizeRequest = true;
        resizeWidth = bounds.width;
        resizeHeight = bounds.height;
      }
    });
  }


  //////////////////////////////////////////////////////////////


  MouseEvent mouseEventQueue[] = new MouseEvent[10];
  int mouseEventCount;

  protected void enqueueMouseEvent(MouseEvent e) {
    synchronized (mouseEventQueue) {
      if (mouseEventCount == mouseEventQueue.length) {
        MouseEvent temp[] = new MouseEvent[mouseEventCount << 1];
        System.arraycopy(mouseEventQueue, 0, temp, 0, mouseEventCount);
        mouseEventQueue = temp;
      }
      mouseEventQueue[mouseEventCount++] = e;
    }
  }

  protected void dequeueMouseEvents() {
    synchronized (mouseEventQueue) {
      for (int i = 0; i < mouseEventCount; i++) {
        mouseEvent = mouseEventQueue[i];
        handleMouseEvent(mouseEvent);
      }
      mouseEventCount = 0;
    }
  }


  /**
   * Actually take action based on a mouse event.
   * Internally updates mouseX, mouseY, mousePressed, and mouseEvent.
   * Then it calls the event type with no params,
   * i.e. mousePressed() or mouseReleased() that the user may have
   * overloaded to do something more useful.
   */
  protected void handleMouseEvent(MouseEvent event) {
    int id = event.getID();

    // http://dev.processing.org/bugs/show_bug.cgi?id=170
    // also prevents mouseExited() on the mac from hosing the mouse
    // position, because x/y are bizarre values on the exit event.
    // see also the id check below.. both of these go together
    if ((id == MouseEvent.MOUSE_DRAGGED) ||
        (id == MouseEvent.MOUSE_MOVED)) {
      pmouseX = emouseX;
      pmouseY = emouseY;
      mouseX = event.getX();
      mouseY = event.getY();
    }

    mouseEvent = event;

    int modifiers = event.getModifiers();
    if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
      mouseButton = LEFT;
    } else if ((modifiers & InputEvent.BUTTON2_MASK) != 0) {
      mouseButton = CENTER;
    } else if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
      mouseButton = RIGHT;
    }
    // if running on macos, allow ctrl-click as right mouse
    if (platform == MACOSX) {
      if (mouseEvent.isPopupTrigger()) {
        mouseButton = RIGHT;
      }
    }

    mouseEventMethods.handle(new Object[] { event });

    // this used to only be called on mouseMoved and mouseDragged
    // change it back if people run into trouble
    if (firstMouse) {
      pmouseX = mouseX;
      pmouseY = mouseY;
      dmouseX = mouseX;
      dmouseY = mouseY;
      firstMouse = false;
    }

    //println(event);

    switch (id) {
    case MouseEvent.MOUSE_PRESSED:
      mousePressed = true;
      mousePressed();
      break;
    case MouseEvent.MOUSE_RELEASED:
      mousePressed = false;
      mouseReleased();
      break;
    case MouseEvent.MOUSE_CLICKED:
      mouseClicked();
      break;
    case MouseEvent.MOUSE_DRAGGED:
      mouseDragged();
      break;
    case MouseEvent.MOUSE_MOVED:
      mouseMoved();
      break;
    }

    if ((id == MouseEvent.MOUSE_DRAGGED) ||
        (id == MouseEvent.MOUSE_MOVED)) {
      emouseX = mouseX;
      emouseY = mouseY;
    }
  }


  /**
   * Figure out how to process a mouse event. When loop() has been
   * called, the events will be queued up until drawing is complete.
   * If noLoop() has been called, then events will happen immediately.
   */
  protected void checkMouseEvent(MouseEvent event) {
    if (loop
