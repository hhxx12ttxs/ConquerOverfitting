/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at LICENSE.html or
 * http://www.sun.com/cddl.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this License Header
 * Notice in each file.
 *
 * If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Alexandre (Shura) Iline. (shurymury@gmail.com)
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 */
package org.jemmy.input;


import org.jemmy.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.jemmy.Rectangle;
import org.jemmy.env.Timeout;
import org.jemmy.env.Environment;
import org.jemmy.image.Image;
import static org.jemmy.input.AWTRobotInputFactory.*;


/**
 * @author Alexandre Iline(alexandre.iline@sun.com), KAM <mrkam@mail.ru>
 */
public class RobotDriver {

    private static boolean haveOldPos = false;
    private static int smoothness;
    private static double oldX;
    private static double oldY;

    static {
        Environment.getEnvironment().setTimeout(
                new Timeout(ROBOT_DELAY_TIMEOUT_NAME, 10));
        Environment.getEnvironment().setPropertyIfNotSet(
                AWTRobotInputFactory.ROBOT_MOUSE_SMOOTHNESS_PROPERTY, 
                new Integer(Integer.MAX_VALUE).toString());
        smoothness =  Integer.parseInt(
                (String)Environment.getEnvironment().getProperty(
                AWTRobotInputFactory.ROBOT_MOUSE_SMOOTHNESS_PROPERTY));
    }

    /**
     * Sets mouse smoothness
     * @param mouseSmoothness the maximum distance in pixels between
     * mouse positions during movement
     * @see #moveMouse(Point)
     */
    public static void setMouseSmoothness(int mouseSmoothness) {
        smoothness = mouseSmoothness;
    }

    /**
     * Gets mouse smoothness
     * @return the maximum distance in pixels between
     * mouse positions during movement
     * @see #setMouseSmoothness(int)
     * @see #moveMouse(Point)
     */
    public static int getMouseSmoothness() {
        return smoothness;
    }

    /**
     * Constructs a RobotDriver object.
     * @param autoDelay Time for <code>Robot.setAutoDelay(long)</code> method.
     */
    public RobotDriver(Timeout autoDelay) {
        RobotExecutor.get().setAutoDelay(autoDelay);
    }

    /**
     * Constructs a RobotDriver object.
     * @param env Environment with ROBOT_DELAY_TIMEOUT_NAME timeout
     * @see AWTRobotInputFactory#ROBOT_DELAY_TIMEOUT_NAME
     */
    public RobotDriver(Environment env) {
        this(env.getTimeout(ROBOT_DELAY_TIMEOUT_NAME));
    }

    /**
     * Capture an image of specified rectangular area of screen
     * @param screenRect area on screen that will be captured
     * @return image of specified rectangular area of screen
     */
    public static Image createScreenCapture(Rectangle screenRect) {
        return RobotExecutor.get().createScreenCapture(screenRect);
    }

    /**
     * Presses mouse button specified by mouseButton preceding pressing of
     * modifier keys or buttons specified by modifiers
     * @param mouseButton One of MouseEvent.BUTTON*_MASK
     * @param modifiers Combination of InputEvent.*_DOWN_MASK
     * @see java.awt.event.InputEvent
     * @see java.awt.event.MouseEvent
     */
    public void pressMouse(int mouseButton, int modifiers) {
        pressModifiers(modifiers);
        makeAnOperation("mousePress",
                new Object[]{new Integer(mouseButton)},
                new Class[]{Integer.TYPE});
    }

    /**
     * Releases mouse button specified by mouseButton then releasing
     * modifier keys or buttons specified by modifiers
     * @param mouseButton One of MouseEvent.BUTTON*_MASK
     * @param modifiers Combination of InputEvent.*_DOWN_MASK
     * @see java.awt.event.InputEvent
     * @see java.awt.event.MouseEvent
     */
    public void releaseMouse(int mouseButton, int modifiers) {
        makeAnOperation("mouseRelease",
                new Object[]{new Integer(mouseButton)},
                new Class[]{Integer.TYPE});
        releaseModifiers(modifiers);
    }

    /**
     * Moves mouse to the specified mouse. When previous mouse location is
     * remembered mouse moved smoothly between the points according to
     * mouse smoothness parameter. Otherwise it jumps to the specified point
     * @param point Position on the screen where to move mouse
     * @see #setMouseSmoothness(int)
     * @see #getMouseSmoothness() 
     */
    public void moveMouse(Point point) {
        double targetX = point.x;
        double targetY = point.y;
        if (haveOldPos && (oldX != targetX || oldY != targetY)) {
            double currX = oldX;
            double currY = oldY;
            double hyp = Math.sqrt((targetX - currX) * (targetX - currX) +
                    (targetY - currY) * (targetY - currY));
            double steps = Math.ceil(hyp / Math.min(hyp, smoothness));
            double vx = (targetX - currX) / steps;
            double vy = (targetY - currY) / steps;
            assert (long)vx * vx + (long)vy * vy <= (long)smoothness * smoothness;
            while (Math.round(currX) != Math.round(targetX) ||
                    Math.round(currY) != Math.round(targetY)) {
                currX += vx;
                currY += vy;
                makeAnOperation("mouseMove", new Object[]{
                            new Integer((int) Math.round(currX)),
                            new Integer((int) Math.round(currY))},
                        new Class[]{Integer.TYPE, Integer.TYPE});
            }
        } else {
            makeAnOperation("mouseMove",
                    new Object[]{new Integer(point.x), new Integer(point.y)},
                    new Class[]{Integer.TYPE, Integer.TYPE});
        }
        haveOldPos = true;
        oldX = targetX;
        oldY = targetY;
    }

    /**
     * Clicks the mouse button specified by mouseButton at the specified point
     * specified number of times preceding it by pressing the modifiers key or
     * buttons and ending by releasing them. The last click is as long as
     * mouseClick timeout
     * @param point Screen location where to click mouse
     * @param clickCount Number of clicks
     * @param mouseButton One of MouseEvent.BUTTON*_MASK
     * @param modifiers Combination of InputEvent.*_DOWN_MASK
     * @param mouseClick Timeout of the last click
     * @see java.awt.event.InputEvent
     * @see java.awt.event.MouseEvent
     */
    public void clickMouse(Point point, int clickCount, int mouseButton,
            int modifiers, Timeout mouseClick) {
        pressModifiers(modifiers);
        moveMouse(point);
        makeAnOperation("mousePress", new Object[]{new Integer(mouseButton)}, new Class[]{Integer.TYPE});
        for (int i = 1; i < clickCount; i++) {
            makeAnOperation("mouseRelease", new Object[]{new Integer(mouseButton)}, new Class[]{Integer.TYPE});
            makeAnOperation("mousePress", new Object[]{new Integer(mouseButton)}, new Class[]{Integer.TYPE});
        }
        mouseClick.sleep();
        makeAnOperation("mouseRelease", new Object[]{new Integer(mouseButton)}, new Class[]{Integer.TYPE});
        releaseModifiers(modifiers);
    }

    /**
     * @deprecated Implementation doesn't seem to be correct as it ignores mouseButton and modifiers
     * @param point
     * @param mouseButton One of MouseEvent.BUTTON*_MASK
     * @param modifiers
     */
    public void dragMouse(Point point, int mouseButton, int modifiers) {
        moveMouse(point);
    }

    /**
     * Performs drag and drop from startPoint to endPoint using specified
     * mouseButton and modifiers to perform it.
     * @param startPoint Screen coordinates of drag start point
     * @param endPoint Screen coordinates of drag end point
     * @param mouseButton One of MouseEvent.BUTTON*_MASK
     * @param modifiers Combination of InputEvent.*_DOWN_MASK
     * @param before Timeout between pressing mouse at the startPoint and
     * mouse move
     * @param after Timeout between mouse move to the endPoint and mouse
     * release
     */
    public void dragNDrop(Point startPoint, Point endPoint,
            int mouseButton, int modifiers, Timeout before, Timeout after) {
        moveMouse(startPoint);
        pressMouse(mouseButton, modifiers);
        before.sleep();
        moveMouse(endPoint);
        after.sleep();
        releaseMouse(mouseButton, modifiers);
    }

    /**
     * Presses a key.
     * @param keyCode Key code (<code>KeyEventVK_*</code> field.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void pressKey(int keyCode, int modifiers) {
        pressModifiers(modifiers);
        makeAnOperation("keyPress",
                new Object[]{new Integer(keyCode)},
                new Class[]{Integer.TYPE});
    }

    /**
     * Releases a key.
     * @param keyCode Key code (<code>KeyEventVK_*</code> field.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void releaseKey(int keyCode, int modifiers) {
        makeAnOperation("keyRelease",
                new Object[]{new Integer(keyCode)},
                new Class[]{Integer.TYPE});
        releaseModifiers(modifiers);
    }

    /**
     * Turns the wheel.
     * @param p
     * @param amount Either positive or negative
     * @param modifiers 
     */
    public void turnWheel(Point p, int amount, int modifiers) {
        pressModifiers(modifiers);
        moveMouse(p);
        java.awt.Robot r = null;
        makeAnOperation("mouseWheel",
                new Object[]{amount},
                new Class[]{Integer.TYPE});
        releaseModifiers(modifiers);
    }

    /**
     * Performs a single operation.
     * @param method a name of <code>java.awt.Robot</code> method.
     * @param params method parameters
     * @param paramClasses method parameters classes
     */
    public void makeAnOperation(final String method, final Object[] params, final Class[] paramClasses) {
        RobotExecutor.get().makeAnOperation(method, params, paramClasses);
    }

    final static int SHIFT_MASK = InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK;
    final static int ALT_GRAPH_MASK = InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.ALT_GRAPH_MASK;
    final static int ALT_MASK = InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK;
    final static int META_MASK = InputEvent.META_DOWN_MASK | InputEvent.META_MASK;
    final static int CTRL_MASK = InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK;

    /**
     * Presses modifiers keys by robot.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    protected void pressModifiers(int modifiers) {
        if ((modifiers & SHIFT_MASK) != 0) {
            pressKey(KeyEvent.VK_SHIFT, modifiers & ~SHIFT_MASK);
        } else if ((modifiers & ALT_GRAPH_MASK) != 0) {
            pressKey(KeyEvent.VK_ALT_GRAPH, modifiers & ~ALT_GRAPH_MASK);
        } else if ((modifiers & ALT_MASK) != 0) {
            pressKey(KeyEvent.VK_ALT, modifiers & ~ALT_MASK);
        } else if ((modifiers & META_MASK) != 0) {
            pressKey(KeyEvent.VK_META, modifiers & ~META_MASK);
        } else if ((modifiers & CTRL_MASK) != 0) {
            pressKey(KeyEvent.VK_CONTROL, modifiers & ~CTRL_MASK);
        }
    }

    /**
     * Releases modifiers keys by robot.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    protected void releaseModifiers(int modifiers) {
        if ((modifiers & SHIFT_MASK) != 0) {
            releaseKey(KeyEvent.VK_SHIFT, modifiers & ~SHIFT_MASK);
        } else if ((modifiers & ALT_GRAPH_MASK) != 0) {
            releaseKey(KeyEvent.VK_ALT_GRAPH, modifiers & ~ALT_GRAPH_MASK);
        } else if ((modifiers & ALT_MASK) != 0) {
            releaseKey(KeyEvent.VK_ALT, modifiers & ~ALT_MASK);
        } else if ((modifiers & META_MASK) != 0) {
            releaseKey(KeyEvent.VK_META, modifiers & ~META_MASK);
        } else if ((modifiers & CTRL_MASK) != 0) {
            releaseKey(KeyEvent.VK_CONTROL, modifiers & ~CTRL_MASK);
        }
    }

    /**
     * If java.awt.Robot is running in other JVM, it shutdowns that JVM
     * @see AWTRobotInputFactory#runInOtherJVM(boolean)
     */
    public static void exit() {
        RobotExecutor.get().exit();
    }

}

