/*
<<<<<<< HEAD
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javafx.scene.shape;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.sg.PGBox;
import com.sun.javafx.sg.PGNode;
import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.input.PickResult;

/**
 * The {@code Box} class defines a 3 dimensional box with the specified size.
 * A {@code Box} is a 3D geometry primitive created with a given depth, width,
 * and height. It is centered at the origin.
 *
 * @since JavaFX 8.0
 */
public class Box extends Shape3D {

    private TriangleMesh mesh;

    /**
     * Creates a new instance of {@code Box} of dimension 2 by 2 by 2.
     */
    
    public static final double DEFAULT_SIZE = 2;
    
    public Box() {
        this(DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Creates a new instance of {@code Box} of dimension width by height 
     * by depth.
     */
    public Box(double width, double height, double depth) {
        setWidth(width);
        setHeight(height);
        setDepth(depth);
    }
    
    /**
     * Defines the depth or the Z dimension of the Box.
     *
     * @defaultValue 2.0
     */
    private DoubleProperty depth;

    public final void setDepth(double value) {
        depthProperty().set(value);
    }

    public final double getDepth() {
        return depth == null ? 2 : depth.get();
    }

    public final DoubleProperty depthProperty() {
        if (depth == null) {
            depth = new SimpleDoubleProperty(Box.this, "depth", DEFAULT_SIZE) {
                @Override
                public void invalidated() {
                    impl_markDirty(DirtyBits.MESH_GEOM);
                    manager.invalidateBoxMesh(key);
                    key = 0;
                }
            };
        }
        return depth;
    }

    /**
     * Defines the height or the Y dimension of the Box.
     *
     * @defaultValue 2.0
     */
    private DoubleProperty height;

    public final void setHeight(double value) {
        heightProperty().set(value);
    }

    public final double getHeight() {
        return height == null ? 2 : height.get();
    }

    public final DoubleProperty heightProperty() {
        if (height == null) {
            height = new SimpleDoubleProperty(Box.this, "height", DEFAULT_SIZE) {
                @Override
                public void invalidated() {
                    impl_markDirty(DirtyBits.MESH_GEOM);
                    manager.invalidateBoxMesh(key);
                    key = 0;
                }
            };
        }
        return height;
    }

    /**
     * Defines the width or the X dimension of the Box.
     *
     * @defaultValue 2.0
     */
    private DoubleProperty width;

    public final void setWidth(double value) {
        widthProperty().set(value);
    }

    public final double getWidth() {
        return width == null ? 2 : width.get();
    }

    public final DoubleProperty widthProperty() {
        if (width == null) {
            width = new SimpleDoubleProperty(Box.this, "width", DEFAULT_SIZE) {
                @Override
                public void invalidated() {
                    impl_markDirty(DirtyBits.MESH_GEOM);
                    manager.invalidateBoxMesh(key);
                    key = 0;
                }
            };
        }
        return width;
    }
    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override
    protected PGNode impl_createPGNode() {
        return Toolkit.getToolkit().createPGBox();
    }

    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    public void impl_updatePG() {
        super.impl_updatePG();
        if (impl_isDirty(DirtyBits.MESH_GEOM)) {
            PGBox pgBox = (PGBox) impl_getPGNode();
            final float w = (float) getWidth();
            final float h = (float) getHeight();
            final float d = (float) getDepth();
            if (w < 0 || h < 0 || d < 0) {
                pgBox.updateMesh(null);
            } else {
                if (key == 0) {
                    key = generateKey(w, h, d);
                }
                mesh = manager.getBoxMesh(w, h, d, key);
                mesh.impl_updatePG();
                pgBox.updateMesh(mesh.impl_getPGTriangleMesh());
            }
        }
    }
    
    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override
    public BaseBounds impl_computeGeomBounds(BaseBounds bounds, BaseTransform tx) {
        final float w = (float) getWidth();
        final float h = (float) getHeight();
        final float d = (float) getDepth();

        if (w < 0 || h < 0 || d < 0) {
            return bounds.makeEmpty();
        }

        final float hw = w * 0.5f;
        final float hh = h * 0.5f;
        final float hd = d * 0.5f;
        
        bounds = bounds.deriveWithNewBounds(-hw, -hh, -hd, hw, hh, hd);
        bounds = tx.transform(bounds, bounds);
        return bounds;
    }

    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override
    protected boolean impl_computeContains(double localX, double localY) {
        double w = getWidth();
        double h = getHeight();
        return -w <= localX && localX <= w && 
                -h <= localY && localY <= h;
    }

    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override
    protected boolean impl_computeIntersects(PickRay pickRay, PickResultChooser pickResult) {

        final double w = getWidth();
        final double h = getHeight();
        final double d = getDepth();
        final double hWidth = w / 2.0;
        final double hHeight = h / 2.0;
        final double hDepth = d / 2.0;
        final Vec3d dir = pickRay.getDirectionNoClone();
        final double invDirX = dir.x == 0.0 ? Double.POSITIVE_INFINITY : (1.0 / dir.x);
        final double invDirY = dir.y == 0.0 ? Double.POSITIVE_INFINITY : (1.0 / dir.y);
        final double invDirZ = dir.z == 0.0 ? Double.POSITIVE_INFINITY : (1.0 / dir.z);
        final Vec3d origin = pickRay.getOriginNoClone();
        final double originX = origin.x;
        final double originY = origin.y;
        final double originZ = origin.z;
        final boolean signX = invDirX < 0.0;
        final boolean signY = invDirY < 0.0;
        final boolean signZ = invDirZ < 0.0;

        double t0 = Double.NEGATIVE_INFINITY;
        double t1 = Double.POSITIVE_INFINITY;
        char side0 = '0';
        char side1 = '0';

        if (Double.isInfinite(invDirX)) {
            if (-hWidth <= originX && hWidth >= originX) {
                // move on, we are inside for the whole length
            } else {
                return false;
            }
        } else {
            t0 = ((signX ? hWidth : -hWidth) - originX) * invDirX;
            t1 = ((signX ? -hWidth : hWidth) - originX) * invDirX;
            side0 = signX ? 'X' : 'x';
            side1 = signX ? 'x' : 'X';
        }

        if (Double.isInfinite(invDirY)) {
            if (-hHeight <= originY && hHeight >= originY) {
                // move on, we are inside for the whole length
            } else {
                return false;
            }
        } else {
            final double ty0 = ((signY ? hHeight : -hHeight) - originY) * invDirY;
            final double ty1 = ((signY ? -hHeight : hHeight) - originY) * invDirY;

            if ((t0 > ty1) || (ty0 > t1)) {
                return false;
            }
            if (ty0 > t0) {
                side0 = signY ? 'Y' : 'y';
                t0 = ty0;
            }
            if (ty1 < t1) {
                side1 = signY ? 'y' : 'Y';
                t1 = ty1;
            }
        }

        if (Double.isInfinite(invDirZ)) {
            if (-hDepth <= originZ && hDepth >= originZ) {
                // move on, we are inside for the whole length
            } else {
                return false;
            }
        } else {
            double tz0 = ((signZ ? hDepth : -hDepth) - originZ) * invDirZ;
            double tz1 = ((signZ ? -hDepth : hDepth) - originZ) * invDirZ;

            if ((t0 > tz1) || (tz0 > t1)) {
                return false;
            }
            if (tz0 > t0) {
                side0 = signZ ? 'Z' : 'z';
                t0 = tz0;
            }
            if (tz1 < t1) {
                side1 = signZ ? 'z' : 'Z';
                t1 = tz1;
            }
        }

        char side = side0;
        double t = t0;
        final CullFace cullFace = getCullFace();
        final double minDistance = pickRay.getNearClip();
        final double maxDistance = pickRay.getFarClip();

        if (t0 > maxDistance) {
            return false;
        }
        if (t0 < minDistance || cullFace == CullFace.FRONT) {
            if (t1 >= minDistance && t1 <= maxDistance && cullFace != CullFace.BACK) {
                side = side1;
                t = t1;
            } else {
                return false;
            }
        }

        if (Double.isInfinite(t) || Double.isNaN(t)) {
            // We've got a nonsense pick ray or box size.
            return false;
        }

        if (pickResult != null && pickResult.isCloser(t)) {
            Point3D point = PickResultChooser.computePoint(pickRay, t);

            Point2D txtCoords = null;
            
            switch (side) {
                case 'x': // left
                    txtCoords = new Point2D(
                            0.5 - point.getZ() / d,
                            0.5 + point.getY() / h);
                    break;
                case 'X': // right
                    txtCoords = new Point2D(
                            0.5 + point.getZ() / d,
                            0.5 + point.getY() / h);
                    break;
                case 'y': // top
                    txtCoords = new Point2D(
                            0.5 + point.getX() / w,
                            0.5 - point.getZ() / d);
                    break;
                case 'Y': // bottom
                    txtCoords = new Point2D(
                            0.5 + point.getX() / w,
                            0.5 + point.getZ() / d);
                    break;
                case 'z': // front
                    txtCoords = new Point2D(
                            0.5 + point.getX() / w,
                            0.5 + point.getY() / h);
                    break;
                case 'Z': // back
                    txtCoords = new Point2D(
                            0.5 - point.getX() / w,
                            0.5 + point.getY() / h);
                    break;
                default:
                    // No hit with any of the planes. We must have had a zero
                    // pick ray direction vector. Should never happen.
                    return false;
            }

            pickResult.offer(this, t, PickResult.FACE_UNDEFINED, point, txtCoords);
        }
        
        return true;
    }

    static TriangleMesh createMesh(float w, float h, float d) {

        // NOTE: still create mesh for degenerated box       
        float hw = w / 2f;
        float hh = h / 2f;
        float hd = d / 2f;

        float points[] = {
            -hw, -hh, -hd,
             hw, -hh, -hd,
             hw,  hh, -hd,
            -hw,  hh, -hd,
            -hw, -hh,  hd,
             hw, -hh,  hd,
             hw,  hh,  hd,
            -hw,  hh,  hd};

        float texCoords[] = {0, 0, 1, 0, 1, 1, 0, 1};

        int faceSmoothingGroups[] = {
            1, 1, 1, 1, 2, 2, 2, 2, 4, 4, 4, 4
        };

        int faces[] = {
            0, 0, 2, 2, 1, 1,
            2, 2, 0, 0, 3, 3,            
            1, 0, 6, 2, 5, 1,
            6, 2, 1, 0, 2, 3,            
            5, 0, 7, 2, 4, 1,
            7, 2, 5, 0, 6, 3,
            4, 0, 3, 2, 0, 1,
            3, 2, 4, 0, 7, 3,            
            3, 0, 6, 2, 2, 1,
            6, 2, 3, 0, 7, 3,
            4, 0, 1, 2, 5, 1,
            1, 2, 4, 0, 0, 3,
        };

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(points);
        mesh.getTexCoords().setAll(texCoords);
        mesh.getFaces().setAll(faces);
        mesh.getFaceSmoothingGroups().setAll(faceSmoothingGroups);

        return mesh;
    }

    private static int generateKey(float w, float h, float d) {
        int hash = 3;
        hash = 97 * hash + Float.floatToIntBits(w);
        hash = 97 * hash + Float.floatToIntBits(h);
        hash = 97 * hash + Float.floatToIntBits(d);
        return hash;
=======
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.udig.tools.jgrass.profile;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ui.commands.AbstractDrawCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.tool.SimpleTool;
import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
import org.geotools.geometry.jts.JTS;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;

import eu.udig.tools.jgrass.JGrassToolsPlugin;
import eu.udig.tools.jgrass.profile.borrowedfromjgrasstools.CoverageUtilities;
import eu.udig.tools.jgrass.profile.borrowedfromjgrasstools.ProfilePoint;

/**
 * <p>
 * Tool to draw raster map profiles.
 * </p>
 * <p>
 * NOTE: this is an extention of the DistanceTool
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class ProfileTool extends SimpleTool {

    private int currentPointNumber = 0;

    private List<Point> points = new ArrayList<Point>();
    private ProfileFeedbackCommand command;
    private Point now;
    private double latestProgessiveDistance = 0;
    private boolean doubleClicked = false;

    private GridCoverage2D rasterMapResource;
    private ProfileView chartView;
    private Coordinate begin;

    public ProfileTool() {
        super(MOUSE | MOTION);
    }

    protected void onMouseMoved( MapMouseEvent e ) {
        if (!doubleClicked) {
            // saving value to display the distance
            now = e.getPoint();
            if (command == null || points.isEmpty())
                return;
            Rectangle area = command.getValidArea();
            if (area != null)
                getContext().getViewportPane().repaint(area.x, area.y, area.width, area.height);
            else {
                getContext().getViewportPane().repaint();
            }
        }
    }

    public void onMouseReleased( MapMouseEvent e ) {
        // necessary to restart from begin, having an empty view
        if (currentPointNumber == 0) {
            chartView.clearSeries();
            latestProgessiveDistance = 0;
            points.clear();
            disposeCommand();
            doubleClicked = false;
            chartView.clearMarkers();
        }

        Point current = e.getPoint();
        // if enough points are there, create the profile
        if (points.isEmpty() || !current.equals(points.get(points.size() - 1))) {
            points.add(current);
        }

        /*
         * run with backgroundable progress monitoring
         */
        if (command == null || !command.isValid()) {
            command = new ProfileFeedbackCommand();
            getContext().sendASyncCommand(command);
        }

        try {
            profile(null);
        } catch (Exception ex) {
            ex.printStackTrace();

            String message = "An error occurred while extracting the profile from the map.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, JGrassToolsPlugin.PLUGIN_ID, ex);
        }
    }

    protected void onMouseDoubleClicked( MapMouseEvent e ) {
        currentPointNumber = 0;
        doubleClicked = true;
    }

    /**
     * Removes all the line in the map
     */
    private void disposeCommand() {
        if (command != null) {
            command.setValid(false);
            Rectangle area = command.getValidArea();
            if (area != null)
                getContext().getViewportPane().repaint(area.x, area.y, area.width, area.height);
            else {
                getContext().getViewportPane().repaint();
            }
            command = null;
        }
    }

    /**
     * Creates the profile of the raster map.
     * 
     * @param monitor the progress monitor.
     * @throws IOException 
     */
    private void profile( IProgressMonitor monitor ) throws Exception {
        if (points.size() == currentPointNumber && points.size() > 1) {
            // no point added, do not read
            return;
        } else {
            if (!doubleClicked) {
                currentPointNumber = points.size();
            }
        }

        /*
         * need to get the profile of the last two clicked points
         */
        if (points.size() == 1) {
            Point beforeLastPoint = points.get(0);
            begin = getContext().pixelToWorld(beforeLastPoint.x, beforeLastPoint.y);
        } else if (points.size() > 1) {
            // monitor.beginTask("Extracting profile...", IProgressMonitor.UNKNOWN);

            Point lastPoint = points.get(points.size() - 1);
            Coordinate end = getContext().pixelToWorld(lastPoint.x, lastPoint.y);

            final List<ProfilePoint> profile = CoverageUtilities.doProfile(begin, end, rasterMapResource);
            begin = end;

            Display.getDefault().syncExec(new Runnable(){
                public void run() {
                    for( ProfilePoint profilePoint : profile ) {
                        double elevation = profilePoint.getElevation();
                        if (!Double.isNaN(elevation)) {
                            chartView.addToSeries(latestProgessiveDistance + profilePoint.getProgressive(), elevation);
                        } else {
                            chartView.addToSeries(latestProgessiveDistance + profilePoint.getProgressive(), 0.0);
                        }
                    }
                    ProfilePoint last = profile.get(profile.size() - 1);
                    chartView.addStopLine(latestProgessiveDistance + last.getProgressive());
                    latestProgessiveDistance = latestProgessiveDistance + last.getProgressive();
                }
            });

            // monitor.done();
        }

    }

    public void setActive( boolean active ) {
        
        if (!active) {
            // on tool deactivation
            rasterMapResource = null;
            if (command != null)
                command.setValid(false);
            return;
        } else {
            // on tool activation
            final ILayer selectedLayer = getContext().getSelectedLayer();
            final IGeoResource geoResource = selectedLayer.getGeoResource();
            if (geoResource.canResolve(GridCoverage.class)) {
                IRunnableWithProgress operation = new IRunnableWithProgress(){
                    public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                        try {
                            rasterMapResource = (GridCoverage2D) geoResource.resolve(GridCoverage.class,
                                    new NullProgressMonitor());
                            rasterMapResource = rasterMapResource.view(ViewType.GEOPHYSICS);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                PlatformGIS.runInProgressDialog("Reading map for profile...", false, operation, false);

            }

            if (rasterMapResource == null) {
                getContext().updateUI(new Runnable(){
                    public void run() {
                        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                        MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
                        msgBox.setMessage("The selected layer can't be read by the available datastores. Unable to create a profile on it.");
                        msgBox.open();
                    }
                });
                super.setActive(false);
                return;
            }

            final IStatusLineManager statusBar = getContext().getActionBars().getStatusLineManager();
            disposeCommand();
            if (statusBar == null)
                return; // shouldn't happen if the tool is being used.

            getContext().updateUI(new Runnable(){
                public void run() {
                    statusBar.setErrorMessage(null);
                    statusBar.setMessage(null);

                    try {
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ProfileView.ID);
                        chartView = ((ProfileView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                .findView(ProfileView.ID));
                    } catch (PartInitException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        super.setActive(active);
    }

    private double distance() throws TransformException {
        if (points.isEmpty())
            return 0;
        Iterator<Point> iter = points.iterator();
        Point start = iter.next();
        double distance = 0;
        while( iter.hasNext() ) {
            Point current = iter.next();
            Coordinate begin = getContext().pixelToWorld(start.x, start.y);
            Coordinate end = getContext().pixelToWorld(current.x, current.y);
            distance += JTS.orthodromicDistance(begin, end, getContext().getCRS());
            start = current;
        }

        if (now != null) {
            Point current = now;
            Coordinate begin = getContext().pixelToWorld(start.x, start.y);
            Coordinate end = getContext().pixelToWorld(current.x, current.y);
            distance += JTS.orthodromicDistance(begin, end, getContext().getCRS());
        }
        return distance;
    }

    private void displayError() {
        final IStatusLineManager statusBar = getContext().getActionBars().getStatusLineManager();

        if (statusBar == null)
            return; // shouldn't happen if the tool is being used.

        getContext().updateUI(new Runnable(){
            public void run() {
                statusBar.setErrorMessage("Profile Tool Error");
            }
        });
    }

    private void displayOnStatusBar( double distance ) {
        final IStatusLineManager statusBar = getContext().getActionBars().getStatusLineManager();

        if (statusBar == null)
            return; // shouldn't happen if the tool is being used.
        final String message = createMessage(distance);
        getContext().updateUI(new Runnable(){
            public void run() {
                statusBar.setErrorMessage(null);
                statusBar.setMessage(message);
            }
        });
    }

    /**
     * @param distance
     * @return
     */
    private String createMessage( double distance ) {
        String message = "";

        if (distance > 100000.0) {
            message = message.concat((int) (distance / 1000.0) + " km"); //$NON-NLS-1$
        } else if (distance > 10000.0) { // km + m
            message = message.concat(round(distance / 1000.0, 1) + " km"); //$NON-NLS-1$
        } else if (distance > 1000.0) { // km + m
            message = message.concat(round(distance / 1000.0, 2) + " km"); //$NON-NLS-1$
        } else if (distance > 100.0) { // m
            message = message.concat(round(distance, 1) + " m"); //$NON-NLS-1$
        } else if (distance > 1.0) { // m
            message = message.concat(round(distance, 2) + " m"); //$NON-NLS-1$
        } else { // mm
            message = message.concat(round(distance * 1000.0, 1) + " mm"); //$NON-NLS-1$
        }

        return message;
    }

    /**
     * Truncates a double to the given number of decimal places. Note: truncation at zero decimal
     * places will still show up as x.0, since we're using the double type.
     * 
     * @param value number to round-off
     * @param decimalPlaces number of decimal places to leave
     * @return the rounded value
     */
    private double round( double value, int decimalPlaces ) {
        double divisor = Math.pow(10, decimalPlaces);
        double newVal = value * divisor;
        newVal = (Long.valueOf(Math.round(newVal)).intValue()) / divisor;
        return newVal;
    }

    /**
     */
    class ProfileFeedbackCommand extends AbstractDrawCommand {

        public Rectangle getValidArea() {
            return null;
        }

        public void run( IProgressMonitor monitor ) throws Exception {
            if (points.isEmpty())
                return;
            graphics.setColor(Color.BLACK);
            Iterator<Point> iter = points.iterator();
            Point start = iter.next();
            while( iter.hasNext() ) {
                Point current = iter.next();
                graphics.drawLine(start.x, start.y, current.x, current.y);
                start = current;
            }
            if (start == null || now == null)
                return;
            graphics.drawLine(start.x, start.y, now.x, now.y);
            double distance = distance();
            displayOnStatusBar(distance);

        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}

