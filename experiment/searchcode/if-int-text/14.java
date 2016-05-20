/*
Copyright 2010 Bulat Sirazetdinov
Copyright 2009 Bulat Sirazetdinov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.formed.client.formula.impl;

import com.google.gwt.widgetideas.graphics.client.Color;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.kornr.abstractcanvas.client.CanvasPainter;
import net.kornr.abstractcanvas.client.ICanvasExt;
import net.kornr.abstractcanvas.client.TextMetrics;
import net.kornr.abstractcanvas.client.gwt.CanvasPanelExt;
import org.formed.client.formula.Formula;
import org.formed.client.formula.drawer.Metrics;

/**
 *
 * @author Bulat Sirazetdinov
 */
public final class AbstractCanvasDrawer {
    /*extends BaseDrawer {

    private final CanvasPanelExt canvas;

    public AbstractCanvasDrawer(CanvasPanelExt canvas, Formula formula) {
        super(formula);
        this.canvas = canvas;

        canvas.addCanvasPainter(new CanvasPainter() {

            public void drawCanvas(ICanvasExt canvas) {
                redraw();
            }
        });
    }

    public void fillRect(int x1, int y1, int x2, int y2, int r, int g, int b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class SizedText {

        private String text;
        private int size;

        public SizedText(String text, int size) {
            this.text = text;
            this.size = size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SizedText other = (SizedText) obj;
            if ((this.text == null) ? (other.text != null) : !this.text.equals(other.text)) {
                return false;
            }
            if (this.size != other.size) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + (this.text != null ? this.text.hashCode() : 0);
            hash = 47 * hash + this.size;
            return hash;
        }
    }
    private Map<SizedText, TextMetrics> cachedMetrics = new HashMap<SizedText, TextMetrics>();

    public Metrics textMetrics(String text, int size) {
        canvas.setFontSize(size);
        TextMetrics cached = cachedMetrics.get(new SizedText(text, size));
        if (cached == null) {
            cached = canvas.measureText(text);
            cachedMetrics.put(new SizedText(text, size), cached);
        }
        return new Metrics(cached.getWidth(), cached.getHeight() / 2, cached.getHeight() / 2);
    }

    public void drawText(String text, int size, int x, int y) {
        canvas.setFontSize(size);
        TextMetrics metrics = canvas.measureText(text);
        canvas.strokeText(text, (int) (x + metrics.getWidth()), (int) (y - metrics.getHeight() / 2));
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        canvas.beginPath();
        canvas.moveTo(x1, y1);
        canvas.lineTo(x2, y2);
        canvas.stroke();
    }

    public void drawDottedLine(int x1, int y1, int x2, int y2) {
        canvas.beginPath();
        canvas.moveTo(x1, y1);
        canvas.lineTo(x2, y2);
        canvas.stroke();
    }

    public int getSmallerSize(int size) {
        return size * 3 / 4;
    }

    public void redraw() {
        canvas.clear();

        canvas.setAlign(net.kornr.abstractcanvas.client.gwt.CanvasPanelExt.ALIGN_END);
        canvas.setFillStyle(Color.WHITE);
        canvas.setGlobalAlpha(1.0);
        canvas.fillRect(0, 0, canvas.getCoordWidth(), canvas.getCoordHeight());

        Date from = new Date();
        Metrics metrics = formula.drawAligned(this, 10, 10, 20, Align.TOP);
        Date till = new Date();
        drawText((till.getTime() - from.getTime()) + "ms", 20, 0, 10);

        canvas.setStrokeStyle(Color.BLACK);
        canvas.setLineWidth(1);

        canvas.beginPath();
        canvas.rect(9, 9, 2 + metrics.getWidth(), 2 + metrics.getHeight());
        canvas.stroke();

        redrawCursor();
    }

    @Override
    public void redrawCursor() {
        canvas.beginPath();
        canvas.moveTo(cursor.getX(), cursor.getY() - cursor.getHeightUp());
        canvas.lineTo(cursor.getX(), cursor.getY() + cursor.getHeightDown());
        canvas.stroke();
    }*/
}

