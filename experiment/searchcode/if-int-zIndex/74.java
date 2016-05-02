/*
 Copyright (c) 2013, Ike Yousuf <admin@kudodev.com>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the <organization> nor the
 names of its contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.kudo.gdx.ui;

import com.badlogic.gdx.InputMultiplexer;
import com.kudo.gdx.ui.components.Frame;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.kudo.gdx.GDXConstants;
import com.kudo.gdx.Graphics;
import com.kudo.gdx.ui.event.KeyListener;
import com.kudo.gdx.ui.event.MouseListener;
import com.kudo.util.FloatPoint;
import java.awt.Point;

/**
 *
 * @author Ike
 */
public abstract class Component {

    private MultiInputProcessor input = null;
    protected UIDisplay display = null;
    private String name = "";
    private boolean isdebugcomp = false;
    private Rectangle bounds = new Rectangle(0, 0, 0, 0);
    private FloatPoint relativePosition = new FloatPoint();
    protected Container parent = null;
    protected String text = "";
    private int zIndex = 0;
    private boolean visible = true, enabled = true;
    private Color borderColor = Color.BLACK;
    private boolean hasBorder = true;
    private boolean canTarget = true;
    private Color backgroundcolor = null;
    private BitmapFont font = GDXConstants.defaultOutlinedFont;
    private Color foregroundColor = Color.WHITE;

    public Component(float x, float y, float w, float h) {
        bounds = new Rectangle(x, y, w, h);
        setLocation(x, y);
    }

    public Component(float x, float y) {
        bounds = new Rectangle(x, y, 0, 0);
        setLocation(x, y);
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setParent(Container c) {
        parent = c;
        if (c instanceof Frame) {
            relativePosition.y += 24;
        }
        setLocation(relativePosition.x, relativePosition.y);
        if (c instanceof DebugPanel) {
            isdebugcomp = true;
        }
    }

    public void setDisplay(UIDisplay d) {
        display = d;
    }

    public void setName(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }

    public UIDisplay getDisplay() {
        return display;
    }

    public Container getParent() {
        return parent;
    }

    public boolean getVisible() {
        return visible;
    }

    public Component() {
    }

    public void setCanTarget(boolean b) {
        canTarget = b;
    }

    public void setEnabled(boolean b) {
        enabled = b;
    }

    public void setHasBorder(boolean b) {
        hasBorder = b;
    }

    public boolean hasBorder() {
        return hasBorder;
    }

    public boolean hasBackgroundColor() {
        return backgroundcolor != null;
    }

    public Color getBackgroundColor() {
        return backgroundcolor;
    }

    public Color getForeground() {
        return foregroundColor;
    }

    public void setBackgroundColor(Color c) {
        backgroundcolor = c;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setForeground(Color c) {
        foregroundColor = c;
    }

    public BitmapFont getFont() {
        return font;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean getCanTarget() {
        return canTarget;
    }

    public void setVisible(boolean b) {
        visible = b;
    }

    public boolean contains(Point p) {
        if (canTarget) {
            return bounds.contains(p.x, p.y);
        } else {
            return false;
        }
    }

    public float getY() {
        return bounds.y;
    }

    public float getX() {
        return bounds.x;
    }

    public float getRelativeY() {
        return relativePosition.y;
    }

    public float getRelativeX() {
        return relativePosition.x;
    }

    public Rectangle getLocation() {
        return bounds;
    }

    public FloatPoint getRelativeLocation() {
        return relativePosition;
    }

    public boolean contains(int x, int y) {
        if (canTarget) {
            return bounds.contains(x, y);
        } else {
            return false;
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String t) {
        text = t;
    }

    public abstract void render(Graphics g);

    public void setX(float x) {
        bounds.x = x;
        if (parent != null) {
            relativePosition.x = bounds.x - parent.getX();
        }
    }

    public void setY(float y) {
        bounds.y = y;
        if (parent != null) {
            relativePosition.x = bounds.x - parent.getX();
        }
    }

    public void setLocation(float x, float y) {
        relativePosition.x = x;
        relativePosition.y = y;
        if (parent != null) {
            bounds.x = parent.getX() + relativePosition.x;
            bounds.y = parent.getY() + relativePosition.y;
        }
    }

    public void setBoundsLocation(float x, float y) {
        bounds.x = x;
        bounds.y = y;
        if (parent != null) {
            relativePosition.x = bounds.x - parent.getX();
            relativePosition.y = bounds.y - parent.getY();
        }
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int i) {
        zIndex = i;
    }

    public void setSize(float w, float h) {
        bounds.width = w;
        bounds.height = h;
    }

    public float getWidth() {
        return bounds.width;
    }

    public float getHeight() {
        return bounds.height;
    }

    public void setWidth(float i) {
        bounds.width = i;
    }

    public void setHeight(float i) {
        bounds.height = i;
    }

    public void addMouseListener(MouseListener l) {
        if (input == null) {
            input = new MultiInputProcessor(this);
        }
        input.addProcessor(l);
    }

    public void addKeyListener(KeyListener l) {
        if (input == null) {
            input = new MultiInputProcessor(this);
        }
        input.addProcessor(l);
    }

    public boolean getDebugComp() {
        return isdebugcomp;
    }

    public void setFont(BitmapFont f) {
        font = f;
    }

    public void renderDebug(Graphics g) {
        if (!isdebugcomp) {
            g.batchDrawRect(bounds);
        }
    }

    public InputMultiplexer getInput() {
        return input;
    }
}

