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

import com.sun.javafx.tk.TKScene;
import com.sun.javafx.tk.swing.FrameStage;
import com.sun.javafx.tk.swing.WindowStage;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javafx.scene.Scene;
import javafx.stage.StageStyle;
import javax.swing.JDialog;

/**
 * @author Stephen Chin
 * @author Keith Combs
 */
class DialogStage extends WindowStage {
    protected JDialog dialog;
    protected FrameStage frameStage;

    protected DialogStage(JDialog dialog, StageStyle stageStyle, Scene scene) {
        super(dialog, stageStyle, false);
        this.dialog = dialog;
        dialog.addPropertyChangeListener("resizable", new PropertyChangeListener() {            
            public void propertyChange(PropertyChangeEvent pce) {
                if (listener != null) {
                    listener.changedResizable(DialogStage.this.dialog.isResizable());
                }
            }
        });
        setJavaFXScene(scene);
    }

    @Override
    protected void initializeStyle(Window win, StageStyle style) {
        JDialog dialog1 = (JDialog) win;
        if ((style == StageStyle.TRANSPARENT) || (style == StageStyle.UNDECORATED)) {
            dialog1.setUndecorated(true);
        }

        super.initializeStyle(win, style);
    }

    @Override
    public void setResizable(boolean resizable) {
        if (dialog.isResizable() != resizable) {
            dialog.setResizable(resizable);
        }
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        dialog.setTitle(title);
    }

    @Override
    public void setScene(TKScene scene) {
    }

    public final void setJavaFXScene(Scene scene) {
        super.setScene(WindowHelper.setTransparentScenePeer(scene));
    }

    public void setFrameStage(FrameStage frameStage) {
        this.frameStage = frameStage;
    }

    @Override
    public void close() {
        super.close();
        StageHelper.removeStage(frameStage);
    }
}

