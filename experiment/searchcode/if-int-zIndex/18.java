/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.choosel.visualization_component.map.client;

import org.thechiselgroup.choosel.core.client.ui.CSS;
import org.thechiselgroup.choosel.core.client.ui.Color;

import com.google.gwt.maps.client.MapPane;
import com.google.gwt.maps.client.MapPaneType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

public class DefaultOverlay extends AbstractMapItemOverlay {

    protected static final String CSS_RESOURCE_ITEM_ICON = "resourceItemIcon";

    private Element element;

    private MapWidget map;

    private MapPane pane;

    private Point locationPoint;

    private EventListener eventListener;

    private int zIndex;

    public DefaultOverlay(LatLng location, int radius, Color color,
            Color borderColor, int zIndex, EventListener eventListener) {

        super(location, radius, color, borderColor);

        assert location != null;
        assert eventListener != null;

        this.zIndex = zIndex;
        this.location = location;
        this.eventListener = eventListener;

        this.element = DOM.createDiv();
        this.element.setClassName(CSS_RESOURCE_ITEM_ICON);

        DOM.sinkEvents(element, Event.MOUSEEVENTS | Event.ONCLICK);
        DOM.setEventListener(element, eventListener);
        CSS.setPosition(element, CSS.ABSOLUTE);

        updateSizeCssProperties();

        updateColor();
        updateBorderColor();

        setZIndex(zIndex);

        element.setInnerText("");
    }

    @Override
    protected final Overlay copy() {
        return new DefaultOverlay(location, radius, color, borderColor, zIndex,
                eventListener);
    }

    @Override
    protected final void initialize(MapWidget map) {
        this.map = map;

        pane = map.getPane(MapPaneType.MARKER_PANE);
        pane.getElement().appendChild(element);

        updatePosition(map.convertLatLngToDivPixel(location));
    }

    @Override
    protected final void redraw(boolean force) {
        /*
         * We check if the location has changed, because Google Maps allows
         * infinite panning along the east-west-axis (see issue 38) and requires
         * an updated widget location in this case, although it will not force
         * redrawing.
         */
        Point newLocationPoint = map.convertLatLngToDivPixel(location);

        if (!force && sameLocation(newLocationPoint)) {
            return;
        }

        updatePosition(newLocationPoint);
    }

    @Override
    protected final void remove() {
        element.removeFromParent();
    }

    private boolean sameLocation(Point newLocationPoint) {
        assert newLocationPoint != null;
        return locationPoint != null
                && locationPoint.getX() == newLocationPoint.getX()
                && locationPoint.getY() == newLocationPoint.getY();
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
        CSS.setZIndex(element, zIndex);
    }

    @Override
    protected void updateBorderColor() {
        CSS.setBorderColor(element, borderColor.toRGBa());
    }

    @Override
    protected void updateColor() {
        CSS.setBackgroundColor(element, color.toRGBa());
    }

    @Override
    protected void updateLocation() {
        redraw(true);
    }

    private void updatePosition(Point newLocationPoint) {
        assert newLocationPoint != null;
        locationPoint = newLocationPoint;
        CSS.setLeft(element, locationPoint.getX() - radius);
        CSS.setTop(element, locationPoint.getY() - radius);
    }

    @Override
    protected void updateRadius() {
        updateSizeCssProperties();
        updatePosition(map.convertLatLngToDivPixel(location));
    }

    public void updateSizeCssProperties() {
        CSS.setWidth(element, radius * 2);
        CSS.setHeight(element, radius * 2);
        CSS.setBorderRadius(element, radius);
    }
}
