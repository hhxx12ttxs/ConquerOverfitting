package lu.etat.pch.gis.utils.json.geometry;

import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.json.JSONException;
import com.esri.arcgis.server.json.JSONObject;
import com.esri.arcgis.system.ServerUtilities;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: schullto
 * Date: Jul 8, 2010
 * Time: 5:51:57 AM
 */
public class AgsJsonEnvelope extends AgsJsonGeometry {
    private double xmin, ymin, xmax, ymax;
    private int spatialReferenceWKID;

    public AgsJsonEnvelope(double xmin, double ymin, double xmax, double ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
        this.spatialReferenceWKID = 0;
    }

    public AgsJsonEnvelope(double xmin, double ymin, double xmax, double ymax, int spatialReferenceWKID) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
        this.spatialReferenceWKID = spatialReferenceWKID;
    }

    public AgsJsonEnvelope(JSONObject json) {
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
                        this.spatialReferenceWKID = 0;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public AgsJsonEnvelope(IEnvelope envelope) {
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject grapchics1Geom = new JSONObject();
        grapchics1Geom.put("height",ymax-ymin);
        grapchics1Geom.put("width",xmax-xmin);
        /*
        grapchics1Geom.put("xmin", xmin);
        grapchics1Geom.put("ymin", ymin);
        grapchics1Geom.put("xmax", xmax);
        grapchics1Geom.put("ymax", ymax);
        */
        if (spatialReferenceWKID > -1) {
            JSONObject spatialRefObj = new JSONObject();
            spatialRefObj.put("wkid", spatialReferenceWKID);
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
        if (spatialReferenceWKID > 0) {
            try {
                ISpatialReference spatRef = ServerUtilities.getSRFromString("" + spatialReferenceWKID);
                envelope.setSpatialReferenceByRef(spatRef);
            } catch (Exception e) {
                System.err.println("Error in spatialReferenceWKID: " + spatialReferenceWKID);
                e.printStackTrace();
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

