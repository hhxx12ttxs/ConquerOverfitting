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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * graphicDsiplay.java
 *
 * Created on Mar 17, 2009, 8:20:18 AM
 */
package opticalraytracer;

import java.awt.*;


import java.awt.image.*;
import java.awt.Image.*;
import java.awt.event.*;
import java.util.*;

/**
 *
 * @author lutusp
 */
final public class GraphicDisplay extends javax.swing.JPanel {

    boolean mouseInside = false;
    OpticalRayTracer parent;
    RayTraceComputer rayTraceComputer;
    MutableDouble dx1 = new MutableDouble();
    MutableDouble dx2 = new MutableDouble();
    MutableDouble dy1 = new MutableDouble();
    MutableDouble dy2 = new MutableDouble();
    MutableDouble dx = new MutableDouble();
    MutableDouble dy = new MutableDouble();
    MutableInt ix = new MutableInt();
    MutableInt iy = new MutableInt();
    MutableDouble xa = new MutableDouble();
    MutableDouble ya = new MutableDouble();
    MutableDouble xb = new MutableDouble();
    MutableDouble yb = new MutableDouble();

    Cursor handCursor,moveCursor,defaultCursor;

    /** Creates new form graphicDsiplay */
    public GraphicDisplay(OpticalRayTracer p, RayTraceComputer rtc) {
        parent = p;
        rayTraceComputer = rtc;
        handCursor = new Cursor(Cursor.HAND_CURSOR);
        moveCursor = new Cursor(Cursor.MOVE_CURSOR);
        defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        initComponents();
    }

    @Override
    public void paintComponent(Graphics g) {
        int x = getWidth();
        int y = getHeight();
        rayTraceProcessCore(x, y);
        g.drawImage(parent.image, 0, 0, null);
    }

    // allows drawing any size
    void drawData(Graphics g, int x, int y) {
        rayTraceProcessCore(x, y);
        g.drawImage(parent.image, 0, 0, null);
    }

    void rayTraceProcess(boolean paint) {
        int x = getWidth();
        int y = getHeight();
        rayTraceProcessCore(x, y);
        if (paint) {
            repaint();
        }
    }

    void rayTraceProcessCore(int x, int y) {
        if (updateBuffer(x, y, false)) {
            parent.unselectButton.setEnabled(parent.selectedLens != null);
            Graphics2D bg = (Graphics2D) parent.image.getGraphics();
            bg.setColor(parent.inverted ? parent.sv_dispLoColor.getColor() : parent.sv_dispHiColor.getColor());
            bg.fillRect(0, 0, parent.xSize, parent.ySize);
            if (parent.beamWidth > 1) {
                bg.setStroke(new BasicStroke(parent.beamWidth));
            }
            if (parent.drawGrid) {
                rayTraceComputer.drawGrid(bg);
                rayTraceComputer.drawBaseline(bg);
            }
            if (parent.antiAlias) {
                RenderingHints rh = new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                bg.addRenderingHints(rh);
            }
            rayTraceComputer.drawLenses(bg);
            rayTraceComputer.drawBoxes(bg);
            rayTraceComputer.traceRays(bg, x, y);
            bg.dispose();
        }
    }

    boolean updateBuffer(int x, int y, boolean rayTrace) {
        boolean success = false;
        if (x > 0 && y > 0) {
            success = true;
            if (parent.image == null || parent.xSize != x || parent.ySize != y) {
                parent.xSize = x;
                parent.ySize = y;
                parent.image = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
                parent.xCenter = parent.xSize / 2;
                parent.yCenter = parent.ySize / 2;
            }
        }
        return success;
    }

    void updateStatusBar(int mx, int my, boolean erase) {
        String s = String.format("       %10s   %10s      %10s", "", "", "");
        if (!erase) {
            rayTraceComputer.displayToSpaceOffset(mx, my, dx, dy);
            String sx = dispRoundNum(dx.v);
            String sy = dispRoundNum(dy.v);
            String sz = dispRoundNum(parent.sv_dispScale);
            s = String.format("Pos: X:%10s Y:%10s Zoom:%10s", sx, sy, sz);
        }
        parent.statusLabel.setText(s);
    }

    String dispRoundNum(double v) {
        String result;
        double av = Math.abs(v);
        if (av > 100 || av < .1) {
            result = String.format("%4.3e", v);
        } else {
            result = String.format("%4.3f", v);
        }
        return result;
    }

    public boolean hasMouse() {
        return mouseInside;
    }

    void setMouseInside(boolean inside) {
        mouseInside = inside;
        if (!inside) {
            updateStatusBar(0, 0, true);
        }
    }

    void updateDisplay() {
        if (isVisible()) {
            parent.controlPanelManager.writeLensValues(parent.selectedLens);
            rayTraceProcess(true);
        }
    }

    void handleMouseMove(MouseEvent evt) {
        int mx = evt.getX();
        int my = evt.getY();
        rayTraceComputer.displayToSpace(mx, my, dx, dy);
        if (testMouseInsideLens(dx.v + parent.sv_xOffset, dy.v + parent.sv_yOffset, true) != null) {
            this.setCursor(handCursor);
        } else {
            this.setCursor(defaultCursor);
        }
        updateStatusBar(evt.getX(), evt.getY(), false);
    }

    void handleMouseDrag(MouseEvent evt) {
        updateStatusBar(evt.getX(), evt.getY(), false);
        int mx = evt.getX();
        int my = evt.getY();
        rayTraceComputer.displayToSpace(mx, my, dx, dy);
        //Lens p = testMouseInsideLens(dx.v + parent.xOffset, dy.v + parent.yOffset);
        if (parent.mouseTarget == null) {
            parent.sv_xOffset = -dx.v + parent.mousePressX;
            parent.sv_yOffset = -dy.v + parent.mousePressY;
        } else {
            parent.selectedLens.cx = dx.v + parent.mousePressX;
            parent.selectedLens.cy = dy.v + parent.mousePressY;
        }
        rayTraceProcess(true);
    }

    void handleMouseWheelEvent(MouseWheelEvent evt) {
        int mx = evt.getX();
        int my = evt.getY();
        double v = evt.getWheelRotation() * 0.1;
        v = (evt.isShiftDown()) ? v * 0.1 : v;
        v = (evt.isAltDown()) ? v * 0.01 : v;
        parent.sv_dispScale *= 1.0 - v;
        rayTraceProcess(true);
        updateStatusBar(mx, my, false);
    }

    void handleMousePressEvent(MouseEvent evt) {
        parent.pushUndo();
        boolean isPopup = evt.isPopupTrigger();
        this.setCursor(moveCursor);
        int mx = evt.getX();
        int my = evt.getY();
        rayTraceComputer.displayToSpace(mx, my, dx, dy);
        Lens p = testMouseInsideLens(dx.v + parent.sv_xOffset, dy.v + parent.sv_yOffset, isPopup);
        if (p == null) {
            parent.mousePressX = dx.v + parent.sv_xOffset;
            parent.mousePressY = dy.v + parent.sv_yOffset;
        } else {
            parent.mousePressX = -dx.v + p.cx;
            parent.mousePressY = -dy.v + p.cy;
            parent.setSelectedLens(p);
            rayTraceProcess(true);
        }
        parent.mouseTarget = p;
        if (isPopup) {
            this.setCursor(defaultCursor);
            parent.popupMouseX = evt.getX();
            parent.popupMouseY = evt.getY();
            popupMenu.show(evt.getComponent(), parent.popupMouseX, parent.popupMouseY);
        }
    }

    Lens testMouseInsideLens(double mx, double my, boolean isPopup) {
        Vector<Lens> lensSet = new Vector<Lens>();
        Iterator<Lens> it = parent.sv_lensList.iterator();
        while (it.hasNext()) {
            Lens lens = it.next();
            if (lens.inside(mx, my)) {
                lensSet.add(lens);
            }
        }
        if (lensSet.size() == 0) {
            return null;
        } else {
            // cycle between overlapped lenses
            // as the user presses the mouse repeatedly
            // but only if this is not a context-menu mouse press
            int i = (isPopup) ? parent.overlappedLensSelector : ++parent.overlappedLensSelector;
            return lensSet.get(i % lensSet.size());
        }
    }

    // force lens to Y baseline
    void snapLens(Lens lens) {
        if (Math.abs(lens.cy) <= parent.ySnap) {
            lens.cy = 0;
        }
    }

    void handleMouseReleaseEvent(MouseEvent evt) {
        this.setCursor(defaultCursor);
        if (parent.selectedLens != null) {
            snapLens(parent.selectedLens);
            parent.controlPanelManager.readLensValues(parent.selectedLens);
        }
        rayTraceProcess(true);
        if (evt.isPopupTrigger()) {
            parent.popupMouseX = evt.getX();
            parent.popupMouseY = evt.getY();
            popupMenu.show(evt.getComponent(), parent.popupMouseX, parent.popupMouseY);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        newLensMenuItem = new javax.swing.JMenuItem();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteCursorMenuItem = new javax.swing.JMenuItem();
        pasteAbsMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();

        newLensMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/opticalraytracer/icons/document-new.png"))); // NOI18N
        newLensMenuItem.setText("New Lens");
        newLensMenuItem.setToolTipText("Create new lens at cursor position");
        newLensMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                newLensMenuItemMouseReleased(evt);
            }
        });
        popupMenu.add(newLensMenuItem);

        cutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/opticalraytracer/icons/edit-cut.png"))); // NOI18N
        cutMenuItem.setText("Cut");
        cutMenuItem.setToolTipText("Cut Selected Lens");
        cutMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cutMenuItemMouseReleased(evt);
            }
        });
        popupMenu.add(cutMenuItem);

        copyMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/opticalraytracer/icons/edit-copy.png"))); // NOI18N
        copyMenuItem.setText("Copy");
        copyMenuItem.setToolTipText("Copy Selected Lens");
        copyMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                copyMenuItemMouseReleased(evt);
            }
        });
        popupMenu.add(copyMenuItem);

        pasteCursorMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/opticalraytracer/icons/edit-paste.png"))); // NOI18N
        pasteCursorMenuItem.setText("Paste: mouse cursor");
        pasteCursorMenuItem.setToolTipText("Paste lens to mouse cursor position");
        pasteCursorMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                pasteCursorMenuItemMouseReleased(evt);
            }
        });
        popupMenu.add(pasteCursorMenuItem);

        pasteAbsMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/opticalraytracer/icons/edit-paste.png"))); // NOI18N
        pasteAbsMenuItem.setText("Paste: defined position");
        pasteAbsMenuItem.setToolTipText("Paste lens to its defined position");
        pasteAbsMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                pasteAbsMenuItemMouseReleased(evt);
            }
        });
        popupMenu.add(pasteAbsMenuItem);

        deleteMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/opticalraytracer/icons/process-stop.png"))); // NOI18N
        deleteMenuItem.setText("Delete");
        deleteMenuItem.setToolTipText("Delete Selected Lens");
        deleteMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                deleteMenuItemMouseReleased(evt);
            }
        });
        popupMenu.add(deleteMenuItem);

        setBackground(java.awt.Color.white);
        setToolTipText("<html>\nZoom = mouse wheel<br/>\nPan = drag mouse<br/>\n</html>");
        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                formMouseWheelMoved(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                formMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
        // TODO add your handling code here:
        setMouseInside(true);
    }//GEN-LAST:event_formMouseEntered

    private void formMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseExited
        // TODO add your handling code here:
        setMouseInside(false);
    }//GEN-LAST:event_formMouseExited

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        // TODO add your handling code here:
        repaint();
    }//GEN-LAST:event_formComponentResized

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        // TODO add your handling code here:
        handleMouseMove(evt);
    }//GEN-LAST:event_formMouseMoved

    private void formMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_formMouseWheelMoved
        // TODO add your handling code here:
        handleMouseWheelEvent(evt);
    }//GEN-LAST:event_formMouseWheelMoved

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        // TODO add your handling code here:
        handleMousePressEvent(evt);
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        // TODO add your handling code here:
        handleMouseReleaseEvent(evt);
    }//GEN-LAST:event_formMouseReleased

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        // TODO add your handling code here:
        handleMouseDrag(evt);
    }//GEN-LAST:event_formMouseDragged

    private void cutMenuItemMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cutMenuItemMouseReleased
        // TODO add your handling code here:
        parent.clipboardCutLens();
    }//GEN-LAST:event_cutMenuItemMouseReleased

    private void copyMenuItemMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_copyMenuItemMouseReleased
        // TODO add your handling code here:
        parent.clipboardCopyLens();
    }//GEN-LAST:event_copyMenuItemMouseReleased

    private void pasteCursorMenuItemMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pasteCursorMenuItemMouseReleased
        // TODO add your handling code here:
        parent.clipboardPasteLens(true);
}//GEN-LAST:event_pasteCursorMenuItemMouseReleased

    private void newLensMenuItemMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newLensMenuItemMouseReleased
        // TODO add your handling code here:
        parent.makeNewLensPopup();
    }//GEN-LAST:event_newLensMenuItemMouseReleased

    private void deleteMenuItemMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteMenuItemMouseReleased
        // TODO add your handling code here:
        parent.deleteSelectedLens();
    }//GEN-LAST:event_deleteMenuItemMouseReleased

    private void pasteAbsMenuItemMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pasteAbsMenuItemMouseReleased
        // TODO add your handling code here:
        parent.clipboardPasteLens(false);
}//GEN-LAST:event_pasteAbsMenuItemMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem newLensMenuItem;
    private javax.swing.JMenuItem pasteAbsMenuItem;
    private javax.swing.JMenuItem pasteCursorMenuItem;
    private javax.swing.JPopupMenu popupMenu;
    // End of variables declaration//GEN-END:variables
}

