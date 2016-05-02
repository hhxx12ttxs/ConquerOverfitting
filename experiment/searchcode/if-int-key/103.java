/*  
 * Copyright (c) 2009  Christian Riekoff <info@texone.org>  
 *  
 *  This file is free software: you may copy, redistribute and/or modify it  
 *  under the terms of the GNU General Public License as published by the  
 *  Free Software Foundation, either version 2 of the License, or (at your  
 *  option) any later version.  
 *  
 *  This file is distributed in the hope that it will be useful, but  
 *  WITHOUT ANY WARRANTY; without even the implied warranty of  
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  
 *  General Public License for more details.  
 *  
 *  You should have received a copy of the GNU General Public License  
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 *  
 * This file incorporates work covered by the following copyright and  
 * permission notice:  
 */
package cc.creativecomputing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cc.creativecomputing.events.CCDisposeListener;
import cc.creativecomputing.events.CCEventListener;
import cc.creativecomputing.events.CCKeyEvent;
import cc.creativecomputing.events.CCKeyListener;
import cc.creativecomputing.events.CCMouseEvent;
import cc.creativecomputing.events.CCMouseListener;
import cc.creativecomputing.events.CCMouseMotionListener;
import cc.creativecomputing.events.CCPostListener;
import cc.creativecomputing.events.CCSetupListener;
import cc.creativecomputing.events.CCSizeListener;
import cc.creativecomputing.events.CCUpdateListener;

/**
 * @author info
 *
 */
public abstract class CCAbstractApp implements
	KeyListener, 
	MouseListener, 
	MouseMotionListener{

	protected List<CCSetupListener> _mySetupListener;
	protected List<CCSizeListener> _mySizeListener;
	protected List<CCUpdateListener> _myUpdateListener;
	protected List<CCPostListener> _myPostListener;
	protected List<CCDisposeListener> _myDisposeListener;
	protected List<CCMouseListener> _myMouseListener;
	protected List<CCMouseMotionListener> _myMouseMotionListener;
	protected List<CCKeyListener> _myKeyListener;
	
	private Queue<CCMouseEvent> _myMouseEventQueue = new LinkedList<CCMouseEvent>();
	private Queue<KeyEvent> _myKeyEventQueue = new LinkedList<KeyEvent>();

	
	/**
	 * System variable which stores the width of the display window. 
	 * This value is set by the first parameter of the <code>setSize</code> 
	 * function of the <code>CCApplicationManager</code>. For example, 
	 * the function call <code>setSize(320, 240)</code> sets the 
	 * <code>width</code> variable to the value 320. 
	 * @shortdesc System variable which stores the width of the display window. 
	 * @see #height
	 * @see CCApplicationSettings#size(int, int)
	 * @example basics.CCWidthHeight
	 */
	public int width;
	
	/**
	 * System variable which stores the height of the display window. 
	 * This value is set by the second parameter of the <code>setSize</code> 
	 * function of the <code>CCApplicationManager</code>. For example, 
	 * the function call <code>setSize(320, 240)</code> sets the 
	 * <code>width</code> variable to the value 240. 
	 * @shortdesc System variable which stores the height of the display window. 
	 * @see #width
	 * @see CCApplicationSettings#size(int, int)
	 * @example basics.CCWidthHeight
	 */
	public int height;
	
	protected CCAbstractApp() {
		_mySetupListener = new ArrayList<CCSetupListener>();
		_mySizeListener = new ArrayList<CCSizeListener>();
		_myUpdateListener = new ArrayList<CCUpdateListener>();
		_myPostListener = new ArrayList<CCPostListener>();
		_myDisposeListener = new ArrayList<CCDisposeListener>();
		_myMouseListener = new ArrayList<CCMouseListener>();
		_myMouseMotionListener = new ArrayList<CCMouseMotionListener>();
		_myKeyListener = new ArrayList<CCKeyListener>();
	}

	/**
	 * Returns the current x coordinate of the application window.
	 * @return x coordinate of the application window
	 */
	public abstract int windowX();

	/**
	 * Returns the current y coordinate of the application window.
	 * @return y coordinate of the application window
	 */
	public abstract int windowY();

	/**
	 * Returns the current width of the application window.
	 * @return width of the application window
	 */
	public abstract int windowWidth();

	/**
	 * Returns the current height of the application window.
	 * @return height of the application window
	 */
	public abstract int windowHeight();

	/**
	 * Adds a listener to the application, to react to the initialization of the app. 
	 * This can be used to forward the setup event to 
	 * different objects. To do so the class needs to implement
	 * the {@link CCSetupListener} interface and must be added
	 * as listener.
	 * @param theSizeListener the listener for size events
	 * @see #resize(int, int)
	 * @see #addUpdateListener(CCUpdateListener)
	 * @see CCSizeListener
	 */
	public void addSetupListener(final CCSetupListener theSetupListener) {
		_mySetupListener.add(theSetupListener);
	}
	
	public void removeSetupListener(final CCSetupListener theSetupListener) {
		_mySetupListener.remove(theSetupListener);
	}

	/**
	 * Adds a listener to the application, to react to changes on the
	 * window size. This can be used to forward the size event to 
	 * different objects. To do so the class needs to implement
	 * the <code>CCSizeListener</code> interface and must be added
	 * as listener.
	 * @param theSizeListener the listener for size events
	 * @see #resize(int, int)
	 * @see #addUpdateListener(CCUpdateListener)
	 * @see CCSizeListener
	 */
	public void addSizeListener(final CCSizeListener theSizeListener) {
		_mySizeListener.add(theSizeListener);
	}

	/**
	 * Adds a listener to the application, to call program updates. 
	 * This can be used to forward the update event to 
	 * different objects. To do so the class needs to implement
	 * the <code>CCUpdateListener</code> interface and must be added
	 * as listener. The update call happens before the draw call.
	 * So this can be used for physics or similar simulation processes.
	 * @shortdesc adds a listener reacting to update events
	 * @param theUpdateListener the listener for update events
	 * @see #update(float)
	 * @see CCUpdateListener
	 */
	public void addUpdateListener(final CCUpdateListener theUpdateListener) {
		_myUpdateListener.add(theUpdateListener);
	}
	
	public void removeUpdateListener(final CCUpdateListener theUpdateListener) {
		_myUpdateListener.remove(theUpdateListener);
	}

	/**
	 * Adds a listener to react to application stop. This should shut down
	 * any threads, disconnect from the net, unload memory, etc. 
	 * @shortdesc adds a listener reacting to application stop
	 * @param theDisposeListener the listener for dispose events
	 * @see #finish()
	 * @see CCDisposeListener
	 */
	public void addDisposeListener(final CCDisposeListener theDisposeListener) {
		_myDisposeListener.add(theDisposeListener);
	}
	
	public void removeDisposeListener(final CCDisposeListener theDisposeListener) {
		_myDisposeListener.remove(theDisposeListener);
	}

	/**
	 * Add a post listener to execute expressions after the end of draw
	 * @shortdesc adds a listener reacting to post events
	 * @param thePostListener the listener for post events
	 * @see CCPostListener
	 */
	public void addPostListener(final CCPostListener thePostListener) {
		_myPostListener.add(thePostListener);
	}
	
	/**
	 * The system variable <code>pmouseX</code> always contains the previous 
	 * horizontal coordinate of the mouse. This is the horizontal 
	 * position of the mouse in the frame previous to the current frame.
	 * @shortdesc horizontal position of the mouse in the previous frame
	 * @see #mouseX
	 * @see #mouseY
	 * @see #pMouseY
	 * @see #mousePressed
	 * @example basics.CCMousePosition
	 */
	public int pMouseX = 0;
	
	/**
	 * The system variable <code>pmouseY</code> always contains the previous 
	 * vertical coordinate of the mouse. This is the vertical position of 
	 * the mouse in the frame previous to the current frame.
	 * @shortdesc vertical position of the mouse in the previous frame
	 * @see #mouseX
	 * @see #mouseY
	 * @see #pMouseX
	 * @see #mousePressed
	 * @example basics.CCMousePosition
	 */
	public int pMouseY = 0;
	
	/**
	 * The system variable <code>mouseX</code> always contains 
	 * the current horizontal coordinate of the mouse.
	 * @shortdesc current horizontal coordinate of the mouse
	 * @see #mouseY
	 * @see #pMouseX
	 * @see #pMouseY
	 * @see #mousePressed
	 * @example basics.CCMousePosition
	 */
	public int mouseX = 0;
	
	/**
	 * The system variable <code>mouseY</code> always contains 
	 * the current vertical coordinate of the mouse.
	 * @shortdesc current vertical coordinate of the mouse
	 * @see #mouseX
	 * @see #pMouseX
	 * @see #pMouseY
	 * @see #mousePressed
	 * @example basics.CCMousePosition
	 */
	public int mouseY = 0;
	
	/**
	 * Variable storing if a mouse button is pressed. The value 
	 * of the system variable mousePressed is true if a mouse 
	 * button is pressed and false if a button is not pressed.
	 * @shortdesc Variable storing if a mouse button is pressed
	 * @see #mouseX
	 * @see #mouseY
	 * @see #pMouseX
	 * @see #pMouseY
	 * @example basics.CCMousePosition
	 */
	public boolean mousePressed = false;
	
	protected boolean _myIsFirstMouse = true;

	/**
	 * Add a listener to react to mouse events.
	 * @shortdesc adds a listener reacting to mouse events
	 * @param theMouseListener the listener for mouse events
	 * @see #mouseClicked(CCMouseEvent)
	 * @see #mouseEntered(CCMouseEvent)
	 * @see #mouseExited(CCMouseEvent)
	 * @see #mousePressed(CCMouseEvent)
	 * @see #mouseReleased(CCMouseEvent)
	 * @see CCMouseListener
	 */
	public void addMouseListener(final CCMouseListener theMouseListener) {
		if (_myMouseListener.contains(theMouseListener)) {
			return;
		}
		
		_myMouseListener.add(theMouseListener);
	}
	
	public void removeMouseListener(final CCMouseListener theMouseListener){
		_myMouseListener.remove(theMouseListener);
	}

	/**
	 * Add a listener to react to mouse motion events.
	 * @shortdesc adds a listener reacting o mouse motion events
	 * @param theMouseMotionListener the listener for mouse motion events
	 * @see #mouseDragged(CCMouseEvent)
	 * @see #mouseMoved(CCMouseEvent)
	 * @see CCMouseMotionListener
	 */
	public void addMouseMotionListener(final CCMouseMotionListener theMouseMotionListener) {
		if (_myMouseMotionListener.contains(theMouseMotionListener)) {
			return;
		}
		
		_myMouseMotionListener.add(theMouseMotionListener);
	}
	
	public void removeMouseMotionListener(final CCMouseMotionListener theMouseMotionListener) {
		_myMouseMotionListener.remove(theMouseMotionListener);
	}
	
	//////////////////////////////////////////////////////////////
	//
	// KEY HANDLING
	//
	//////////////////////////////////////////////////////////////
	
	/**
	 * The key variable always contains the value of the most recent 
	 * key on the keyboard that was used (either pressed or released).
	 * For non-ASCII keys, use the keyCode variable. The keys included in the 
	 * ASCII specification (BACKSPACE, TAB, ENTER, RETURN, ESC, and DELETE) do 
	 * not require checking to see if they key is coded, and you should simply 
	 * use the key variable instead of keyCode If you're making cross-platform 
	 * projects, note that the ENTER key is commonly used on PCs and Unix and 
	 * the RETURN key is used instead on Macintosh. Check for both ENTER and 
	 * RETURN to make sure your program will work for all platforms.
	 * @shortdesc most recent ASCII key on the keyboard that was used
	 * @see #keyCode
	 * @see #keyPressed
	 * @see #keyPressed(CCKeyEvent)
	 * @see #keyReleased(CCKeyEvent)
	 * @see #keyTyped(CCKeyEvent)
	 * @see CCKeyListener
	 * @see CCKeyEvent
	 */
	public char key;

	/**
	 * The variable keyCode is used to detect special keys such as the UP, DOWN, 
	 * LEFT, RIGHT arrow keys and ALT, CONTROL, SHIFT.
	 * 
	 * The keys included in the ASCII specification (BACKSPACE, TAB, ENTER, RETURN, ESC, and DELETE) 
	 * do not require checking to see if they key is coded, and you should 
	 * simply use the key variable instead of keyCode If you're making 
	 * cross-platform projects, note that the ENTER key is commonly used on 
	 * PCs and Unix and the RETURN key is used instead on Macintosh. Check for 
	 * both ENTER and RETURN to make sure your program will work for all platforms.
	 * @shortdesc key code of the most recent key on the keyboard that was used
	 * @see #key
	 * @see #keyPressed
	 * @see #keyPressed(CCKeyEvent)
	 * @see #keyReleased(CCKeyEvent)
	 * @see #keyTyped(CCKeyEvent)
	 * 
	 */
	public int keyCode;

	/**
	 * The boolean variable keyPressed is true if any key is pressed and false if no keys are pressed.
	 * @shortdesc variable to see if a key is pressed
	 * @see #key
	 * @see #keyCode
	 * @see #keyPressed(CCKeyEvent)
	 * @see #keyReleased(CCKeyEvent)
	 * @see #keyTyped(CCKeyEvent)
	 * 
	 */
	public boolean keyPressed;

	/**
	 * Add a listener to react to key events.
	 * @shortdesc adds a listener reacting to key events
	 * @param theKeyListener the listener for key events 
	 * @see #keyPressed(CCKeyEvent)
	 * @see #keyReleased(CCKeyEvent)
	 * @see #keyTyped(CCKeyEvent)
	 * @see CCKeyListener
	 */
	public void addKeyListener(final CCKeyListener theKeyListener) {
		_myKeyListener.add(theKeyListener);
	}
	
	public void removeKeyListener(final CCKeyListener theKeyListener) {
		_myKeyListener.remove(theKeyListener);
	}

	/**
	 * Adds an event listener to the application. Classes implementing the <code>CCEventListener</code>
	 * interface need must take care to listen to all needed events.
	 * @shortdesc adds an event listener to the application
	 * @param theEventListener
	 */
	public void addEventListener(final CCEventListener theEventListener) {
		theEventListener.addAsListenerTo(this);
	}

	private void enqueueMouseEvent(CCMouseEvent theEvent) {
		synchronized (_myMouseEventQueue){
			_myMouseEventQueue.add(theEvent);
		}
	}

	protected void dequeueMouseEvents() {
		synchronized (_myMouseEventQueue){
			while(!_myMouseEventQueue.isEmpty()) {
				handleMouseEvent(_myMouseEventQueue.poll());
			}
		}
	}

	private void handleMouseEvent(CCMouseEvent theEvent) {
	
		switch (theEvent.eventType()){
			case MOUSE_PRESSED:
				mousePressed = true;
				
				for(CCMouseListener myMouseListener: _myMouseListener){
					myMouseListener.mousePressed(theEvent);
				}
				break;
			case MOUSE_RELEASED:
				mousePressed = false;
				
				for(CCMouseListener myMouseListener: _myMouseListener){
					myMouseListener.mouseReleased(theEvent);
				}
				break;
			case MOUSE_ENTERED:
				for(CCMouseListener myMouseListener: _myMouseListener){
					myMouseListener.mouseEntered(theEvent);
				}
				break;
			case MOUSE_EXITED:
				for(CCMouseListener myMouseListener: _myMouseListener){
					myMouseListener.mouseExited(theEvent);
				}
				break;
			case MOUSE_DRAGGED:
				pMouseX = mouseX;
				pMouseY = mouseY;
			
				for(CCMouseMotionListener myMouseListener: _myMouseMotionListener){
					myMouseListener.mouseDragged(theEvent);
				}
				break;
			case MOUSE_MOVED:
				if(_myIsFirstMouse){
					pMouseX = theEvent.x();
					pMouseY = theEvent.y();
					_myIsFirstMouse = false;
				}else{
					pMouseX = mouseX;
					pMouseY = mouseY;
				}
				
				for(CCMouseMotionListener myMouseListener: _myMouseMotionListener){
					myMouseListener.mouseMoved(theEvent);
				}
				break;
			case MOUSE_CLICKED:
				for(CCMouseListener myMouseListener: _myMouseListener){
					myMouseListener.mouseClicked(theEvent);
				}
				break;
		}
	
		mouseX = theEvent.x();
		mouseY = theEvent.y();
	}
	
	private int _myLastPressedButton = 0;

	/**
	 * If you override this or any function that takes a "MouseEvent e" without
	 * calling its super.mouseXxxx() then mouseX, mouseY, mousePressed, and
	 * mouseEvent will no longer be set.  
	 * @invisible
	 */
	public void mousePressed(final MouseEvent theMouseEvent) {
		_myLastPressedButton = theMouseEvent.getButton();
		enqueueMouseEvent(new CCMouseEvent(theMouseEvent,pMouseX, pMouseY));
	}

	/** @invisible */
	public void mouseReleased(final MouseEvent theMouseEvent) {
		enqueueMouseEvent(new CCMouseEvent(theMouseEvent,pMouseX, pMouseY));
	}

	/** @invisible */
	public void mouseClicked(final MouseEvent theMouseEvent) {
		enqueueMouseEvent(new CCMouseEvent(theMouseEvent,pMouseX, pMouseY));
	}

	/** @invisible */
	public void mouseEntered(final MouseEvent theMouseEvent) {
		enqueueMouseEvent(new CCMouseEvent(theMouseEvent,pMouseX, pMouseY));
	}

	/** @invisible */
	public void mouseExited(final MouseEvent theMouseEvent) {
		enqueueMouseEvent(new CCMouseEvent(theMouseEvent,pMouseX, pMouseY));
	}

	/** @invisible */
	public void mouseDragged(final MouseEvent theMouseEvent) {
		enqueueMouseEvent(new CCMouseEvent(theMouseEvent,pMouseX, pMouseY,_myLastPressedButton));
	}

	/**
	 *   
	 * @invisible
	 */
	public void mouseMoved(final MouseEvent theMouseEvent) {
		enqueueMouseEvent(new CCMouseEvent(theMouseEvent,pMouseX, pMouseY));
	}

	private void enqueueKeyEvent(KeyEvent theEvent) {
		synchronized (_myKeyEventQueue){
			_myKeyEventQueue.add(theEvent);
		}
	}

	protected void dequeueKeyEvents() {
		synchronized (_myKeyEventQueue){
			while(!_myKeyEventQueue.isEmpty()) {
				handleKeyEvent(_myKeyEventQueue.poll());
			}
		}
	}

	private void handleKeyEvent(KeyEvent theEvent) {
		key = theEvent.getKeyChar();
		keyCode = theEvent.getKeyCode();
	
		CCKeyEvent myEvent = new CCKeyEvent(theEvent);
	
		switch (theEvent.getID()){
			case KeyEvent.KEY_PRESSED:
				keyPressed = true;
				for(CCKeyListener myKeyListener:new ArrayList<CCKeyListener>(_myKeyListener))myKeyListener.keyPressed(myEvent);
				break;
			case KeyEvent.KEY_RELEASED:
				keyPressed = false;
				for(CCKeyListener myKeyListener:new ArrayList<CCKeyListener>(_myKeyListener))myKeyListener.keyReleased(myEvent);
				break;
			case KeyEvent.KEY_TYPED:
				for(CCKeyListener myKeyListener:new ArrayList<CCKeyListener>(_myKeyListener))myKeyListener.keyTyped(myEvent);
				break;
		}
	}

	/**
	 * @invisible
	 */
	public void keyPressed(KeyEvent theKeyEvent) {
		enqueueKeyEvent(theKeyEvent);
	}

	/**
	 * @invisible
	 */
	public final void keyReleased(KeyEvent theKeyEvent) {
		enqueueKeyEvent(theKeyEvent);
	}

	/**
	 * @invisible
	 */
	public final void keyTyped(KeyEvent theKeyEvent) {
		enqueueKeyEvent(theKeyEvent);
	}

}
