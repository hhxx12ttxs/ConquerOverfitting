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
package nl.cloudfarming.client.geoviewer.jxmap.render;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;

/**
 *
 * @author Timon Veenstra
 */
public class MultiPolygonRenderer implements GeometryRenderer<MultiPolygon> {

    @Override
    public Rectangle paint(MultiPolygon geometry, JXMapViewer mapViewer, Graphics2D g, boolean selected) {

        com.vividsolutions.jts.geom.Point centroid = geometry.getCentroid();

        //
        // determine the box the geometry will be drawn in
        //
        Rectangle viewportBounds = mapViewer.getViewportBounds();
        Rectangle bounds = new Rectangle(0, 0, -1, -1);
        boolean firstpoint = false;

        for (Coordinate boundryCoord : geometry.getBoundary().getCoordinates()) {
            GeoPosition geopoint = new GeoPosition(boundryCoord.y, boundryCoord.x);
            Point2D point2D = mapViewer.getTileFactory().geoToPixel(geopoint, mapViewer.getZoom());

            int x = (int) (point2D.getX() - viewportBounds.getX());
            int y = (int) (point2D.getY() - viewportBounds.getY());
            if (firstpoint) {
                bounds.setLocation(x, y);
            }
            bounds.add(x, y);
        }
        //
        // draw the geometry
        //
        Polygon polygon = new Polygon();
        int pointFirstX = 0;
        int pointFirstY = 0;
        if (geometry.getCoordinates().length > 1) {
            int pointX = 0;
            int pointY = 0;
            for (Coordinate coordinate : geometry.getCoordinates()) {
                GeoPosition geopoint = new GeoPosition(coordinate.y, coordinate.x);
                Point2D point2D = mapViewer.getTileFactory().geoToPixel(geopoint, mapViewer.getZoom());

                pointX = (int) (point2D.getX() - viewportBounds.getX());
                pointY = (int) (point2D.getY() - viewportBounds.getY());

                polygon.addPoint(pointX, pointY);
            }
            g.draw(polygon);
            if (selected) {
                int rgb = g.getColor().getRGB();
                Color c = new Color(makeTransparant(rgb, 80),true);
                g.setColor(c);
                g.fill(polygon);
            }


        } else {
            //
            // fallback
            //
            Rectangle rect = new Rectangle(100, 100, 100, 100);
            pointFirstX = (int) (centroid.getX() - viewportBounds.getX());
            pointFirstY = (int) (centroid.getY() - viewportBounds.getY());
            rect.setLocation(pointFirstX, pointFirstY);
            g.fill(rect);
            bounds = rect;
        }
        return bounds;
    }

    /**
     * make a color more transparent
     * 
     * @param rgb
     * @param alphaPercent
     * @return
     */
    private int makeTransparant(int rgb, int alphaPercent) {
        int a = (rgb >> 24) & 0xff;
        a *= ((double)(100-alphaPercent) / (double)100);
        return ((rgb & 0x00ffffff) | (a << 24));
    }
}

