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

import com.sun.javafx.tk.desktop.PerformanceTrackerImpl;
import com.sun.javafx.tk.swing.FullScreenFrame;
import com.sun.javafx.tk.swing.SwingScene;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javafx.scene.Scene;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * This class overrides the default JSGPanelImpl implementation of isOpaque
 *
 * @author Stephen Chin
 */
class TransparentSwingScene extends SwingScene {

    public TransparentSwingScene(Scene scene) {
        PerformanceTrackerImpl.logEvent("TransparentSwingScene constructor");
        this.scenePanel = new TransparentSwingScenePanel();

        this.scene = scene;
        Scene.ScenePeerListener scenePeerListener = new Scene.ScenePeerListener();
        // todo 1.3 - generated code has changed, but something may still be needed here
        // scenePeerListener.addTriggers$();
        int count = scenePeerListener.count$();
        int sceneOffset = Scene.ScenePeerListener.VOFF$scene;
        for (int i = 0; i < count; i++) {
            if (i == sceneOffset) {
                scenePeerListener.set$scene(scene);
            } else {
                scenePeerListener.applyDefaults$(i);
            }
        }
        scenePeerListener.complete$();
        listener = scenePeerListener;

        this.scenePanel.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                if (listener != null) {
                    Dimension size = scenePanel.getSize();
                    listener.changedSize(size.width, size.height);
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if (listener != null) {
                    Point sceneRelativeToStage = SwingUtilities.convertPoint(scenePanel, 0, 0, SwingUtilities.getWindowAncestor(scenePanel));

                    listener.changedLocation(sceneRelativeToStage.x, sceneRelativeToStage.y);
                }
            }
        });

        PerformanceTrackerImpl.logEvent("TransparentSwingScene constructor: (Swing panel created) - finished");
    }

    class TransparentSwingScenePanel extends SwingScene.SwingScenePanel {

        /**
         * Overrides the default implementation, which incorrectly sets the window
         * to opaque when running inside a dialog (or other non-JFrame).  For reference,
         * here is the code this is intended to replace:
         * <p>
         * <blockquote><pre>
         * public boolean isOpaque() {
         *   Window ancestor = SwingUtilities.getWindowAncestor(this.this$0.scenePanel);
         *   return ((ancestor instanceof FullScreenFrame) || (!(ancestor instanceof JFrame)));
         * }</blockquote></pre>
         *
         * @return
         */
        @Override
        public boolean isOpaque() {
            Window ancestor = SwingUtilities.getWindowAncestor(scenePanel);
            return ((ancestor instanceof FullScreenFrame) || (!(ancestor instanceof JFrame || ancestor instanceof JDialog)));
        }
    }
}
