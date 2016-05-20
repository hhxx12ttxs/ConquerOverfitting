/*
 * @(#)SensorTrack.java
 *
 * Copyright 2011 MBARI
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */



package org.mbari.dss.client.data;

//~--- non-JDK imports --------------------------------------------------------

import com.allen_sauer.gwt.log.client.Log;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Timer;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.util.JSObject;

import org.mbari.dss.client.Dss;
import org.mbari.dss.client.action.GotKmlLayerSpec;
import org.mbari.dss.client.action.GotSensorParameter;
import org.mbari.dss.client.events.EventFirer;
import org.mbari.dss.client.events.MapViewCreatedEvent;
import org.mbari.dss.client.events.QueryTimerEvent;
import org.mbari.dss.client.events.SensorDataRetrievedEvent;
import org.mbari.dss.client.events.SensorRecordsChanged;
import org.mbari.dss.client.model.KmlSpec;
import org.mbari.dss.client.util.DssDateTimeUtil;
import org.mbari.dss.client.views.MapView;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Date;

/**
 * Handles the retrieval of sensor tracks and notification of corresponding events.
 *
 * @author dcline
 */
public class SensorTrack {
    private SensorTrack() {}

    /**
     * Sets up a handler of the MapViewCreatedEvent to retrieve the layers
     * and fire a corresponding WmsLayersRetrievedEvent.
     */
    static void init(final EventBus eventBus) {
        Log.info("SensorTrack init");
        eventBus.addHandler(MapViewCreatedEvent.TYPE, new MapViewCreatedEvent.Handler() {
            public void onRetrieved(MapViewCreatedEvent event) {
                Log.info("SensorTrack: onMapViewCreated called");
                retrieveSensorParams();
            }
        });
    }

    /**
     * Retrieves the sensor measurement parameters available (nitrate, salinity, etc.)
     * from the server and updates the view
     */
    public static void retrieveSensorParams() {
        Log.info("SensorTrack: getting sensor parameter list ...");
        Dss.sensorService.execute(new GotSensorParameter() {
            @Override
            public void got(ArrayList<String> param) {
                Log.info("Layers: GotKmlLayerSpecs: " + param.size());
                SensorTrackDS.getInstance().add(param);
                _setTimerHandler(param);
                EventFirer.fireEvent(new SensorRecordsChanged());
            }
        });
    }

    /**
     * Clear sensor tracks. This destroys all features in the sensor track
     * vectors, but does not delete the vector layer itself.
     */
    private static void clearTracks() {
        Log.debug("Resetting sensor tracks");

        Record records[] = SensorTrackDS.getInstance().getCacheData();

        if (records != null) {
            for (Record r : records) {
                Vector l1 = (Vector) r.getAttributeAsObject(SensorTrackDS.VECTOR_LAYER_FIELD);

                if (l1 != null) {
                    l1.destroyFeatures();
                }
            }
        }

        SensorTrackDS.getInstance().setCacheData(records);
    }

    public static void replaceSensor(ListGridRecord r, double startDepth, double endDepth, Date eventStartDate,
                                     Date eventEndDate) {
        Vector l1 = (Vector) r.getAttributeAsObject(SensorTrackDS.VECTOR_LAYER_FIELD);

        if (l1 != null) {
            l1.destroyFeatures();
        }

        updateSensor(r, startDepth, endDepth, eventStartDate, eventEndDate);
    }

    private static void updateSensor(Record r, double startDepth, double endDepth, Date eventStartDate,
                                     Date eventEndDate) {
        final Vector vector      = (Vector) r.getAttributeAsObject(SensorTrackDS.VECTOR_LAYER_FIELD);
        String       sensorParam = r.getAttribute(SensorTrackDS.NAME_FIELD);
        KmlSpec      spec        = (KmlSpec) r.getAttributeAsObject(SensorTrackDS.KML_SPEC_FIELD);
        String       startDate, endDate;

        // Convert local time to gmt for query
        startDate = DssDateTimeUtil.formatGmtTimeParameter(spec.getTimeFormat(), eventStartDate);
        endDate   = DssDateTimeUtil.formatGmtTimeParameter(spec.getTimeFormat(), eventEndDate);

        Log.info("Updating sensor " + sensorParam + " for range: " + startDate + " to " + endDate);
        if (!startDate.equals(endDate)) {

            // Get sensor track for the given sensor parameter
            Dss.sensorService.executeGetSensorTrackKml(sensorParam, startDate, endDate, startDepth, endDepth,
                    new GotKmlLayerSpec() {
                @Override
                public void got(final KmlSpec spec) {
                    if (spec != null) {
                        Log.debug("Getting kml from: " + spec.getUrl() + "...");

                        final String mapP = MapView.getInstance().getWidget().getMap().getProjection();

                        // add new features from the kml specified to the existing vector
                        JSObject request = KmlLayers.appendKml(vector.getJSObject(), spec.getProjectionCode(), mapP,
                                               spec.getUrl());
                        Timer timer = _createTimeoutTimer(spec.getUrl(), request);

                        timer.schedule(100);
                    }
                }
            });
        }
    }

    private static void updateSensors(Date eventStartDate, Date eventEndDate) {
        Record records[] = SensorTrackDS.getInstance().getCacheData();

        if (records != null) {
            for (Record r : records) { 
                double       endDepth   = r.getAttributeAsDouble(SensorTrackDS.END_DEPTH_FIELD);
                double       startDepth = r.getAttributeAsDouble(SensorTrackDS.START_DEPTH_FIELD);

                // Get sensor track for the given sensor parameter
                updateSensor(r, startDepth, endDepth, eventStartDate, eventEndDate);
            }
        }
    }

    /**
     * Create 2 minute timer checking if connection finished every 10 seconds
     * Kills the request if still running after 2 minutes.
     *
     * @param url
     * @return
     */
    private static Timer _createTimeoutTimer(final String url, final JSObject request) {
        return new Timer() {
            int iter = 0;
            public void run() {
                iter++;

                if (iter >= 12) {
                    Log.debug(" killed GET request for : " + url + " after not responding for 2 minutes");
                    KmlLayers.killRequest(request);

                    return;
                } else {
                    int status = KmlLayers.getRequestStatus(request);

                    if ((request == null) || (status == 200)) {
                        Log.debug(" Request for " + url + " finished");

                        return;
                    }
                }

                schedule(10000);
            }
        };
    }

    /**
     * Sets handlers to react to the {@link QueryTimerEvent}.
     * This will update the sensor track when a {@link QueryTimerEvent}
     * arrives
     *
     * @param param list of names of all sensor parameters
     */
    private static void _setTimerHandler(final ArrayList<String> param) {
        Dss.clientFactory.getEventBus().addHandler(QueryTimerEvent.TYPE, new QueryTimerEvent.Handler() {
            @Override
            public void onEventChanged(final QueryTimerEvent event) {
                Log.info("SensorTracks: QueryTimerEvent");

                if (event.getEvent().equals(QueryTimerEvent.Event.END_DATE_REACHED)) { 
                    // clearTracks();
                } else if (event.getEvent().equals(QueryTimerEvent.Event.QUERY_RANGE_CHANGED)) {
                    clearTracks(); 
                } else if (event.getEvent().equals(QueryTimerEvent.Event.TIMER_FIRED)) {
                    if (event.getStartDate().getTime() < event.getEndDate().getTime()) {
                        updateSensors(event.getStartDate(), event.getEndDate());
                    }
                }
            }
        });
    }
}

