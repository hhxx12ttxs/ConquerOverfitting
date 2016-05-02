/*
 *
 *  *  PCHPrintSOE - Advanced printing SOE for ArcGIS Server
 *  *  Copyright (C) 2010-2012 Tom Schuller
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU Lesser General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU Lesser General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU Lesser General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package lu.etat.pch.gis.utils.json.geometry;

import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.json.JSONException;
import com.esri.arcgis.server.json.JSONObject;
import com.esri.arcgis.system.ServerUtilities;
import lu.etat.pch.gis.utils.SOELogger;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: schullto
 * Date: Jul 8, 2010
 * Time: 5:51:57 AM
 */
public class AgsJsonEnvelope extends AgsJsonGeometry {
    private static final String TAG = "AgsJsonEnvelope";
    private double xmin, ymin, xmax, ymax;
    private Integer spatialReferenceWKID;
    private String spatialReferenceTXT;

    public AgsJsonEnvelope(SOELogger logger, double xmin, double ymin, double xmax, double ymax) {
        super(logger);
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    public AgsJsonEnvelope(SOELogger logger, double xmin, double ymin, double xmax, double ymax, Integer spatialReferenceWKID) {
        super(logger);
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
        this.spatialReferenceWKID = spatialReferenceWKID;
    }

    public AgsJsonEnvelope(SOELogger logger, JSONObject json) {
        super(logger);
        try {
            if (json != null && !json.isNull("xmin") && !json.isNull("ymin") && !json.isNull("xmax") && !json.isNull("ymax")) {
                xmin = (json.getDouble("xmin"));
                ymin = (json.getDouble("ymin"));
                xmax = (json.getDouble("xmax"));
                ymax = (json.getDouble("ymax"));
                if (!json.isNull("spatialReference")) {
                    JSONObject spatRefObj = json.getJSONObject("spatialReference");
                    if (!spatRefObj.isNull("wkid"))
                        spatialReferenceWKID = spatRefObj.getInt("wkid");
                    else
                        try {
                            ISpatialReference spRef = ServerUtilities.getSRFromJSON(spatRefObj);
                            if (spRef.getFactoryCode() > 0) {
                                spatialReferenceWKID = spRef.getFactoryCode();
                            } else {
                                spatialReferenceTXT = spatRefObj.toString();
                            }
                        } catch (Exception e) {
                            logger.error(TAG, "AgsJsonEnvelope(JSONObject).getSRFromJSON.Exception", e);
                        }
                }
            }
        } catch (JSONException e) {
            logger.error(TAG, "AgsJsonEnvelope(JSONObject)", e);
        }
    }

    public AgsJsonEnvelope(SOELogger logger, IEnvelope envelope) {
        super(logger);
        try {
            this.xmin = envelope.getXMin();
            this.ymin = envelope.getYMin();
            this.xmax = envelope.getXMax();
            this.ymax = envelope.getYMax();
            if (envelope.getSpatialReference() != null) {
                this.spatialReferenceWKID = envelope.getSpatialReference().getFactoryCode();
            } else
                this.spatialReferenceWKID = 0;
        } catch (AutomationException e) {
            logger.error(TAG, "AgsJsonEnvelope(IEnvelope)", e);
        } catch (IOException e) {
            logger.error(TAG, "AgsJsonEnvelope(IEnvelope)", e);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject grapchics1Geom = new JSONObject();
        grapchics1Geom.put("height", ymax - ymin);
        grapchics1Geom.put("width", xmax - xmin);
        grapchics1Geom.put("xmin", xmin);
        grapchics1Geom.put("ymin", ymin);
        grapchics1Geom.put("xmax", xmax);
        grapchics1Geom.put("ymax", ymax);

        if (spatialReferenceWKID != null) {
            JSONObject spatialRefObj = new JSONObject();
            spatialRefObj.put("wkid", spatialReferenceWKID);
            grapchics1Geom.put("spatialReference", spatialRefObj);
        } else if (spatialReferenceTXT != null) {
            JSONObject spatialRefObj = new JSONObject();
            spatialRefObj.put("wkt", spatialReferenceTXT);
            grapchics1Geom.put("spatialReference", spatialRefObj);
        }
        return grapchics1Geom;
    }

    public IEnvelope toArcObject() throws JSONException, IOException {
        Envelope envelope = new Envelope();
        envelope.setXMin(xmin);
        envelope.setYMin(ymin);
        envelope.setXMax(xmax);
        envelope.setYMax(ymax);
        if (spatialReferenceWKID != null) {
            try {
                ISpatialReference spatRef = ServerUtilities.getSRFromString("" + spatialReferenceWKID);
                envelope.setSpatialReferenceByRef(spatRef);
            } catch (Exception e) {
                logger.error(TAG, "AgsJsonEnvelope.toArcObject.Error in spatialReferenceWKID", spatialReferenceWKID);
                logger.error(TAG, "AgsJsonEnvelope.toArcObject.spatialReferenceWKID", e);
            }
        } else if (spatialReferenceTXT != null) {
            try {
                ISpatialReference spatRef = ServerUtilities.getSRFromString("" + spatialReferenceTXT);
                envelope.setSpatialReferenceByRef(spatRef);
            } catch (Exception e) {
                logger.error(TAG, "AgsJsonEnvelope.toArcObject.Error in spatialReferenceTXT", spatialReferenceTXT);
                logger.error(TAG, "AgsJsonEnvelope.toArcObject,spatialReferenceTXT", e);
            }
        }
        return envelope;
    }

    public double getXmin() {
        return xmin;
    }

    public void setXmin(double xmin) {
        this.xmin = xmin;
    }

    public double getYmin() {
        return ymin;
    }

    public void setYmin(double ymin) {
        this.ymin = ymin;
    }

    public double getXmax() {
        return xmax;
    }

    public void setXmax(double xmax) {
        this.xmax = xmax;
    }

    public double getYmax() {
        return ymax;
    }

    public void setYmax(double ymax) {
        this.ymax = ymax;
    }

    public int getSpatialReferenceWKID() {
        return spatialReferenceWKID;
    }

    public void setSpatialReferenceWKID(int spatialReferenceWKID) {
        this.spatialReferenceWKID = spatialReferenceWKID;
    }
}

