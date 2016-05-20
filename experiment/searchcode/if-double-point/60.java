/**
 * Copyright (C) 2010 Cloudfarming <info@cloudfarming.nl>
 *
 * Licensed under the Eclipse Public License - v 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.cloudfarming.client.geoviewer.edit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import nl.cloudfarming.client.geoviewer.Layer;
import nl.cloudfarming.client.geoviewer.SurfaceEditor;
import nl.cloudfarming.client.model.Surface;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * controller for the surface editor view 
 * 
 * @author Timon Veenstra
 */
@ServiceProvider(service = SurfaceEditor.class)
@Messages("geo_editor.action_add_point=Add point")
public class GeoEditorController implements SurfaceEditor {

    private static final double FORM_FACTOR = 100000.0;
    private static final int MARGIN_X = 100;
    private static final int MARGIN_Y = 100;
    private final GeoEditorScene scene = new GeoEditorScene();
    private JComponent view = scene.createView();
    private Surface surface;
    private Coordinate base;

    public GeoEditorController() {
        scene.getActions().addAction(ActionFactory.createPopupMenuAction(new EditPopupProvider()));
        scene.getActions().addAction(ActionFactory.createAcceptAction(new AcceptProvider() {

            @Override
            public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
                if (transferable.isDataFlavorSupported(Surface.DATA_FLAVOR)) {
                    return ConnectorState.ACCEPT;
                }
                return ConnectorState.REJECT;
            }

            @Override
            public void accept(Widget widget, Point point, Transferable transferable) {
                try {
                    Surface s = (Surface) transferable.getTransferData(Surface.DATA_FLAVOR);
                    if (s != null) {
                        edit(s);
                    }
                } catch (UnsupportedFlavorException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        }));
//        scene.getActions().addAction(new SceneCreateAction());
    }

    public JComponent getView() {
        return view;
    }

    @Override
    public void edit(Surface surface) {
        assert surface.getGeometry() != null;
        Geometry boundingBox = surface.getGeometry().getEnvelope();
//        assert boundingBox.getCoordinates().length == 2;

        this.surface = surface;
        base = boundingBox.getCoordinates()[0];

        GeoNode prev = null;
        GeoNode first = null;

        for (int i = 0; i < surface.getGeometry().getCoordinates().length - 1; i++) {
            Coordinate c = surface.getGeometry().getCoordinates()[i];
            GeoNode geoNode = new GeoNode(new Coordinate(c.x, c.y));
            Widget w = scene.addNode(geoNode);
            //translate to coordinate to a point
            Point location = coordinateToPoint(geoNode.getCoordinate());
            // replace the point based on the widget size
            location.x = location.x - (w.getPreferredSize().width/2);
            location.y = location.y - (w.getPreferredSize().height/2);
            w.setPreferredLocation(location);

            if (first == null) {
                first = geoNode;
            } else {
                scene.connectNodes(geoNode, prev);

            }
            prev = geoNode;
        }
        scene.connectNodes(prev, first);
    }

    /**
     * translates a coordinate to a point on the screen
     * 
     * @param position
     * @return 
     */
    private Point coordinateToPoint(Coordinate position) {
        assert base != null;
        assert position != null;
        double relativeX = position.x - base.x;
        double relativeY = position.y - base.y;
        int x = (int) (relativeX * FORM_FACTOR) + MARGIN_X;
        int y = (int) (relativeY * FORM_FACTOR) + MARGIN_Y;
        System.out.println("x: " + x + "y : " + y);
        return new Point(x, y);
    }

    /**
     * translates a point on the screen into a coordinate
     * 
     * @param point
     * @return 
     */
    private Coordinate pointToCoordinate(Point point) {
        assert base != null;
        assert point != null;
        double x = (double) point.x / FORM_FACTOR + base.x;
        double y = (double) point.y / FORM_FACTOR + base.y;
        System.out.println("x: " + x + "y : " + y);
        return new Coordinate(x, y);
    }

    /**
     * Provides a popup menu for the edit component
     * 
     */
    private class EditPopupProvider implements PopupMenuProvider {

        @Override
        public JPopupMenu getPopupMenu(Widget widget, final Point localLocation) {
            JPopupMenu menu = new JPopupMenu();
            menu.add(new AbstractAction(NbBundle.getMessage(this.getClass(), "geo_editor.action_add_point")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    assert SwingUtilities.isEventDispatchThread();
                    Point p = localLocation;
                    p.x = p.x - MARGIN_X;
                    p.y = p.y - MARGIN_Y;
                    GeoNode geoNode = new GeoNode(pointToCoordinate(localLocation));
                    scene.addNode(geoNode).setPreferredLocation(coordinateToPoint(geoNode.getCoordinate()));
                    scene.validate();
                }
            });
            return menu;
        }
    }
}

