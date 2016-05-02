/*
 * Copyright (c) 2008-2010, JFXtras Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of JFXtras nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jfxtras.stage;

import com.sun.javafx.sg.PGNode;
import com.sun.javafx.tk.TKStage;
import com.sun.javafx.tk.TKStageListener;
import com.sun.javafx.tk.swing.SwingScene;
import com.sun.javafx.tk.swing.WindowStage;
import com.sun.scenario.Settings;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;

/**
 * Collection of utility methods that need to be called from Java rather than
 * JavaFX code.
 *
 * @author Stephen Chin
 * @author Keith Combs
 */
public class WindowHelper {
    private static Boolean transparencySupported = null;

    private static boolean isMac() {
        return "Mac OS X".equals(System.getProperty("os.name"));
    }

    private static boolean isLinux() {
        return "Linux".equalsIgnoreCase(System.getProperty("os.name"));
    }

    private static boolean isSolaris() {
        return "Solaris".equalsIgnoreCase(System.getProperty("os.name"));
    }

    private static boolean isTransparencySupported() {
        if (transparencySupported == null) {
            if ((isLinux()) || (isSolaris())) {
                boolean trans = false;

                transparencySupported = Boolean.valueOf(Settings.getBoolean("javafx.allowTransparentStage", trans));
            } else {
                transparencySupported = Boolean.valueOf(true);
            }

            if (!(transparencySupported.booleanValue())) {
                System.err.println("Warning: Transparent windows are not supported by the current platform.");
            }

        }

        return transparencySupported.booleanValue();
    }

    /**
     * Retrieves a window from a stage delegate object, effectively working around
     * JavaFX permissions via a direct Java call to the code generated method.
     * 
     * @param delegate A WindowStageDelegate or subclass JavaFX object
     * @return The current value of the window var
     */
    public static Window extractWindow(Stage stage) {
        return stage == null ? null : ((WindowStage) stage.get$Stage$impl_peer()).window;
    }

    static TKStage setStagePeer(Stage stage, TKStage peer) {
        TKStage oldPeer = stage.get$Stage$impl_peer();
        if (oldPeer != null) {
            ((WindowStage) oldPeer).window.dispose();
        }
        stage.$Stage$impl_peer = peer;
        peer.setTKStageListener((TKStageListener) stage.get$(Stage.VOFF$stageListener));
        peer.setTitle(stage.get$title());
        peer.setResizable(stage.get$resizable());
        if (stage.get$Stage$impl_peer() instanceof WindowStage) {
            Window oldWindow = ((WindowStage) stage.get$Stage$impl_peer()).window;
            try {
                Method iconImagesMethod = Window.class.getMethod("getIconImages", (Class[]) null);
                if (iconImagesMethod != null) {
                    peer.setIcons((List) iconImagesMethod.invoke(oldWindow));
                }
            } catch (IllegalAccessException ex) {
                Logger.getLogger(WindowHelper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(WindowHelper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(WindowHelper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(WindowHelper.class.getName()).log(Level.SEVERE, "Icons on Dialogs only supported on Java 6 or later", ex);
            } catch (SecurityException ex) {
                Logger.getLogger(WindowHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        peer.setOpacity(stage.get$opacity());
        peer.setIconified(stage.get$iconified());
        peer.setFullScreen(stage.get$fullScreen());
        if (stage.get$scene() != null) {
            peer.setScene(stage.get$scene().impl_getPeer());
        }
        peer.initSecurityContext();
        return oldPeer;
    }

    static SwingScene setTransparentScenePeer(Scene scene) {
        if (scene == null) {
            return null;
        }
        SwingScene oldPeer = (SwingScene) scene.impl_getPeer();
        SwingScene newPeer = new TransparentSwingScene(scene);
        newPeer.setRoot((PGNode) oldPeer.scenePanel.getScene());
        newPeer.setCursor(oldPeer.scenePanel.getCursor());
        newPeer.setFillPaint(oldPeer.scenePanel.getBackgroundPaint());
        Dimension size = oldPeer.scenePanel.getPreferredSize();
        newPeer.setContentSize(size.width, size.height);
        scene.$Scene$impl_peer = newPeer;
        return newPeer;
    }

    static GraphicsConfiguration getTransparentGraphicsConfiguration() {
			try {
				Class<?> awtUtilitiesType = Class.forName("com.sun.awt.AWTUtilities");
				Method isTC = awtUtilitiesType.getMethod("isTranslucencyCapable", new Class[]{GraphicsConfiguration.class});

            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = env.getScreenDevices();
            GraphicsConfiguration translucencyCapableGC = null;

            for (int i = 0; (i < devices.length) && (translucencyCapableGC == null); ++i) {
                GraphicsConfiguration[] configs = devices[i].getConfigurations();
                for (int j = 0; (j < configs.length) && (translucencyCapableGC == null); ++j) {
                    Boolean res = (Boolean) isTC.invoke(null, new Object[]{configs[j]});
                    if (res.booleanValue()) {
                        translucencyCapableGC = configs[j];
                        System.out.println("FOUND translucency capable GC: " + configs[j]);
                    }
                }
            }
            return translucencyCapableGC;
        } catch (ClassNotFoundException cnfe) {
            return null;
        } catch (NoSuchMethodException nsme) {
            return null;
        } catch (IllegalAccessException iae) {
            return null;
        } catch (InvocationTargetException ite) {
        }
        return null;
    }

    static JFrame createJFrame(StageStyle style) {
        JFrame frame;
        if (isMac()) {
            frame = new JFrame() {
                @Override
                protected JRootPane createRootPane() {
                    JRootPane rp = new JRootPane() {
                        @Override
                        public void paint(Graphics g) {
                            g.clearRect(0, 0, getWidth(), getHeight());
                            super.paint(g);
                        }
                    };
                    rp.setOpaque(true);
                    return rp;
                }
            };
        } else if ((((isLinux()) || (isSolaris()))) && (style == StageStyle.TRANSPARENT) && (isTransparencySupported())) {
            GraphicsConfiguration xpgc = getTransparentGraphicsConfiguration();
            frame = new JFrame(xpgc);
        } else {
            frame = new JFrame();
        }
        frame.setBackground(Color.WHITE);
        return frame;
    }

    static javax.swing.JFrame createJFrame(Window owner) {
        javax.swing.JFrame dialog = new javax.swing.JFrame();
        dialog.setBackground(Color.WHITE);
        return dialog;
    }

    static JDialog createJDialog(StageStyle style, Window owner) {
        JDialog dialog;
        if (isMac()) {
            // explicitly cast to a Frame/Dialog so we get the benefit of parent-less dialogs
            if (owner instanceof Frame) {
                dialog = new MacJDialog((Frame) owner);
            } else if (owner instanceof Dialog) {
                dialog = new MacJDialog((Frame) owner);
            } else {
                dialog = new MacJDialog(owner);
            }
        } else if ((((isLinux()) || (isSolaris()))) && (style == StageStyle.TRANSPARENT) && (isTransparencySupported())) {
            GraphicsConfiguration xpgc = getTransparentGraphicsConfiguration();
            // explicitly cast to a Frame/Dialog so we get the benefit of parent-less dialogs
            if (owner instanceof Frame) {
                dialog = new JDialog((Frame) owner, null, false, xpgc);
            } else if (owner instanceof Dialog) {
                dialog = new JDialog((Dialog) owner, null, false, xpgc);
            } else {
                // todo - handle transparent dialogs on linux that have a Window parent
                dialog = new JDialog();
            }
        } else {
            // explicitly cast to a Frame/Dialog so we get the benefit of parent-less dialogs
            if (owner instanceof Frame) {
                dialog = new JDialog((Frame) owner);
            } else if (owner instanceof Dialog) {
                dialog = new JDialog((Dialog) owner);
            } else {
                dialog = new JDialog();
            }
        }
        dialog.setBackground(Color.WHITE);
        return dialog;
    }

    private static class MacJDialog extends JDialog {
        public MacJDialog(Frame owner) {
            super(owner);
        }

        public MacJDialog(Dialog owner) {
            super(owner);
        }

        public MacJDialog(Window owner) {
            super();
        }

        @Override
        protected JRootPane createRootPane() {
            JRootPane rp = new JRootPane() {

                @Override
                public void paint(Graphics g) {
                    g.clearRect(0, 0, getWidth(), getHeight());
                    super.paint(g);
                }
            };
            rp.setOpaque(true);
            return rp;
        }
    }
}

