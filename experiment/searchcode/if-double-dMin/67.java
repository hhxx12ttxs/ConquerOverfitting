/***************************************************************************
 *   Copyright (C) 2009 by Paul Lutus                                      *
 *   lutusp@arachnoid.com                                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
package opticalraytracer;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author lutusp
 */
final public class UserActionManager {

    //int min, max;
    JTextField field = null;
    JComboBox box = null;
    OpticalRayTracer parent;
    double sens = 1;
    double dmin, dmax;
    int imin, imax;

    public UserActionManager(double sens, double min, double max, JTextField field, OpticalRayTracer p) {
        this.field = field;
        dmin = min;
        dmax = max;
        init(sens, p);
        assignHandlers(field);
    }

    public UserActionManager(JComboBox box, int min, int max, OpticalRayTracer p) {
        this.box = box;
        imin = min;
        imax = max;
        init(1, p);
        assignHandlers(box);
    }

    void init(double sens, OpticalRayTracer p) {
        parent = p;
        this.sens = sens;
    }

    public void assignHandlers(Component comp) {
        comp.addMouseWheelListener(new java.awt.event.MouseWheelListener() {

            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                handleMouseWheelMoved(evt);
            }
        });
        comp.addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                handleKeyReleased(evt);
            }
        });
    }

    private void handleKeyReleased(KeyEvent evt) {
        int n = 0;
        double sign = 0;
        double v = sens;
        v = (evt.isShiftDown()) ? v * 0.1 : v;
        v = (evt.isAltDown()) ? v * 0.01 : v;
        int kcode = evt.getKeyCode();
        //String code = KeyEvent.getKey_Text(kcode);
        //System.out.println("key: " + code);
        if (kcode == KeyEvent.VK_ENTER) {
            sign = 1;
        } else if (kcode == KeyEvent.VK_HOME) {
            n = 100;
            sign = 1;
        } else if (kcode == KeyEvent.VK_END) {
            n = -100;
            sign = 1;
        } else if (kcode == KeyEvent.VK_PAGE_UP) {
            n = 10;
            sign = 1;
        } else if (kcode == KeyEvent.VK_PAGE_DOWN) {
            n = -10;
            sign = 1;
        } else if (kcode == KeyEvent.VK_DOWN) {
            n = -1;
            sign = 1;
        } else if (kcode == KeyEvent.VK_UP) {
            n = 1;
            sign = 1;
        } else if (kcode == KeyEvent.VK_ESCAPE) {
            n = 0;
            sign = -1;
        }
        handleIncrement(n, sign, v);
    }

    private void handleMouseWheelMoved(MouseWheelEvent evt) {
        double v = sens;
        v = (evt.isShiftDown()) ? v * 0.1 : v;
        v = (evt.isAltDown()) ? v * 0.01 : v;
        handleIncrement(-evt.getWheelRotation(), 1, v);
    }

    void handleIncrement(int n, double sign, double sv) {
        if (sign != 0) {
            if (field != null) {
                String text = field.getText();
                double dv = 0;
                try {
                    dv = parent.getDouble(text);
                } catch (Exception e) {
                    System.out.println(getClass().getName() + ": Error: " + e);
                }
                dv += (n * sv);
                dv *= sign;
                dv = Math.min(dmax, dv);
                dv = Math.max(dmin, dv);
                String s = parent.formatNum(dv);
                field.setText(s);

            } else if (box != null) {
                int v = box.getSelectedIndex();
                v += n;
                int top = box.getItemCount();
                v = (v < imin) ? imin : v;
                v = (v >= imax) ? imax : v;
                v = (v >= top) ? top - 1 : v;
                box.setSelectedIndex(v);
            }
            if (parent != null) {
                parent.readControls();
            }
        }
    }
}

