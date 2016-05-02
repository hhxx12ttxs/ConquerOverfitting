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
package org.formed.client.formula.elements;

import org.formed.client.formula.Drawer;
import org.formed.client.formula.FormulaItem;
import org.formed.client.formula.drawer.Metrics;
import org.formed.client.formula.editor.Cursor;

/**
 *
 * @author Bulat Sirazetdinov
 */
public final class PlaceElement extends BaseElement {

    boolean show = false;

    public PlaceElement() {
    }

    public PlaceElement(boolean show) {
        this.show = show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public boolean isComplex() {
        return false;
    }

    public FormulaItem makeClone() {
        PlaceElement clone = new PlaceElement(show);
        clone.setParent(parent);

        return clone;
    }

    @Override
    public Metrics draw(Drawer drawer, int x, int y, int size) {
        storedSize = size;
        storedX = x;
        storedY = y;

        Metrics metrics = drawer.measureText(".", size);
        drawer.addDrawnItem(this, x, y, metrics);

        if (show) {
            int x1 = x;
            int y1 = y - metrics.getHeightUp() + Math.max(1, metrics.getHeight() / 5);
            int x2 = x + metrics.getWidth();
            int y2 = y + metrics.getHeightDown() - Math.max(1, metrics.getHeight() / 5);

            drawer.drawDottedLine(x1, y1, x2, y1);
            drawer.drawDottedLine(x2, y1, x2, y2);
            drawer.drawDottedLine(x2, y2, x1, y2);
            drawer.drawDottedLine(x1, y2, x1, y1);

            return metrics;
        } else {
            return new Metrics(0, 0, 0);
        }

        /*        Metrics metrics = drawer.measureText(".", size);

        drawer.addDrawnItem(this, x, y, metrics);

        metrics.setWidth(0);
        return metrics;*/
    }

    @Override
    public Metrics measure(Drawer drawer, int size) {
        storedSize = size;
//        return new Metrics(0, 0, 0);

        Metrics metrics = drawer.measureText(".", size);
        if (!show) {
            metrics.setWidth(0);
            metrics.setHeightUp(0);
        }
        return metrics;
    }

    @Override
    public Cursor getMovementFirst() {
        return getCursor(0);
    }

    @Override
    public Cursor getLast() {
        return getCursor(0);
    }

    @Override
    public boolean isLastPosition(int position) {
        return position == 0;
    }
    
    /*
    @Override
    public void reMeasureCursor(Drawer drawer, Cursor cursor) {
    Metrics metrics = drawer.measureText(".", storedSize);
    cursor.setHeightUp(0);
    cursor.setHeightDown(metrics.getHeightDown());
    }
     */


}

