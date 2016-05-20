/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2006 Prodevelop and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *   +34 963862235
 *   gvsig@gva.es
 *   www.gvsig.gva.es
 *
 *    or
 *
 *   Prodevelop Integraci?n de Tecnolog?as SL
 *   Conde Salvatierra de ?lava , 34-10
 *   46004 Valencia
 *   Spain
 *
 *   +34 963 510 612
 *   +34 963 510 968
 *   gis@prodevelop.es
 *   http://www.prodevelop.es
 */

/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2006 Prodevelop and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *   +34 963862235
 *   gvsig@gva.es
 *   www.gvsig.gva.es
 *
 *    or
 *
 *   Prodevelop Integraci?n de Tecnolog?as SL
 *   Conde Salvatierra de ?lava , 34-10
 *   46004 Valencia
 *   Spain
 *
 *   +34 963 510 612
 *   +34 963 510 968
 *   gis@prodevelop.es
 *   http://www.prodevelop.es
 */
package org.gvsig.remoteClient.arcims;

import org.apache.log4j.Logger;

import org.gvsig.remoteClient.arcims.utils.GetFeaturesTags;
import org.gvsig.remoteClient.arcims.utils.ServiceInfoTags;
import org.gvsig.remoteClient.arcims.utils.ServiceInformation;

import java.awt.geom.Rectangle2D;


/**
 * @author jsanz
 *
 */
public class ArcXMLFeatures extends ArcXML {
    private static Logger logger = Logger.getLogger(ArcXMLFeatures.class.getName());

    /**
     * Creates a complete request in ArcXML for a FeatureService
     * from an ArcImsStatus; including extent, SRS and so on
     * @see org.gvsig.remoteClient.arcims.ArcImsStatus#ArcImsStatus()
     * @param status
     * @return
     */
    public static String getFeatureLayerRequest(ArcImsVectStatus status) {
        return getFeatureLayerRequest(status, 0);
    }

    /**
     * Creates a complete request in ArcXML for a FeatureService
     * from an ArcImsStatus; including extent, SRS and so on
     * @see org.gvsig.remoteClient.arcims.ArcImsStatus#ArcImsStatus()
     * @param status
     * @param featCount
     * @return string
     */
    public static String getFeatureLayerRequest(ArcImsVectStatus status,
        int featCount) {
        /**
             * The layer to retrieve
             */
        String layerid = (String) status.getLayerIds().get(0);

        /**
        * Gets the Decimal Separator from the status.ServiceInfo
        */
        ServiceInformation si = status.getServiceInfo();
        char ds = si.getSeparators().getDs();

        /**
        * The EPSG code that image requested will have,
        * the ArcIMS server will reproject data into this
        * code, see <a href="http://www.epsg.org">EPSG</a>
        */
        String srsView = new String();

        /**
             * Is the ServiceInfo FeatureCoordsys assumed?
             */
        boolean srsAssumed = si.isSrsAssumed();

        /**
             * We suppose that status.getSrs() always will give a
             * string started by this string
             */
        String ini_srs = ServiceInfoTags.vINI_SRS;

        /**
         * Get the minimum distance between points as the mean distance per pixel
         */
        Rectangle2D geoExtent = status.getExtent();
        double iX = geoExtent.getWidth() / status.getWidth();
        double iY = geoExtent.getHeight() / status.getHeight();
        double accuracy = 1.0 * ((iX + iY) / 2.0);

        /**
             * Assign the srs from the status
             * @see org.gvsig.remoteClient.RemoteClientStatus#getSrs()
             */
        if (!srsAssumed && status.getSrs().startsWith(ini_srs)) {
            srsView = status.getSrs().substring(ini_srs.length()).trim();
        } else {
            srsView = "";
        }

        /**
         * Where clause to pass to the server
         */
        String where = status.getWhere();

        /**
        * Finally, we can retrieve the correct ArcXML
        */
        return getFeaturesRequest(layerid, //layer to retrieve
            featCount, //beginrecord
            status.getSubfields(), //subfields
            where, //where
            ArcXML.getFilterCoordsys(srsView), //SRS of the service
            ArcXML.getFeatureCoordsys(srsView), //SRS of the view
            ArcXML.getEnvelope(geoExtent, ds), //envelope
            ArcXML.parseNumber(accuracy * GetFeaturesTags.ACCURACY_RATIO, ds), //accuracy multiplied by an arbitrary ratio
            false, //globalenvelope
            false, //skipfeatures
            true //geometries
        );
    }

    /**
     * Method that buids a proper ArcXML based on basi parameters as fields to retrieve,
     * where condition, etc.
     * @param layerId
     * @param subfields
     * @param where
     * @param filterCoordsys
     * @param featureCoordsys
     * @param envelope
     * @param globalEnvelope
     * @param skipfeatures
     * @param geometry
     * @return ArcXML request
     */
    private static String getFeaturesRequest(String layerId, int featCount,
        String[] subfields, String where, String filterCoordsys,
        String featureCoordsys, String envelope, String accuracy,
        boolean globalEnvelope, boolean skipfeatures, boolean geometry) {
        String request = new String();

        /**
         * Build the SUBFIELDS list
         */
        String strSubf = new String();

        if (subfields == null) {
            subfields = new String[1];
            subfields[0] = "#ALL#";
        } else {
            for (int i = 0; i < subfields.length; i++) {
                strSubf += (subfields[i] + " ");
            }

            strSubf = strSubf.substring(0, strSubf.length() - 1);
        }

        /*
         * With newxml we get FIELDS tag with inner FIELD tag for every field of the attribute table,
         * this way, we get attribute="#SHAPE#" instead of getting #SHAPE# as a name for an attribute
         */

        /*
        request = "<?xml version = '1.0' encoding = 'UTF-8'?>\r\n"
                        + ArcXML.startRequest("1.1")
                        + "\t\t<GET_FEATURES envelope=\"true\" checkesc=\"true\" outputmode=\"newxml\" compact=\"true\"";
        */
        request = ArcXML.startRequest("1.1") +
            "\t\t<GET_FEATURES envelope=\"true\" checkesc=\"true\" outputmode=\"newxml\" compact=\"true\"";

        if (featCount > 0) {
            request += (" beginrecord=\"" + featCount + "\"");
        }

        if (globalEnvelope) {
            request += " globalenvelope=\"true\"";
        } else {
            request += " globalenvelope=\"false\"";
        }

        if (skipfeatures) {
            request += " skipfeatures=\"true\"";
        } else {
            request += " skipfeatures=\"false\"";
        }

        if (geometry) {
            request += " geometry=\"true\"";
        } else {
            request += " geometry=\"false\"";
        }

        request += (">\r\n" + "\t\t\t<LAYER id=\"" + layerId + "\" />\r\n");

        //Start SPATIALQUERY
        request += "\t\t\t\t<SPATIALQUERY  searchorder =\"attributefirst\" ";

        //Put accuracy
        if (!accuracy.equals("")) {
            request += ("accuracy=\"" + accuracy + "\" ");
        }

        //Put subfields
        if (!subfields.equals("")) {
            request += ("subfields=\"" + strSubf + "\" ");
        }

        //Put where
        if (!where.equals("")) {
            request += ("where=\"" + where + "\" ");
        }

        //Close SPATIALQUERY
        request += ">\r\n";

        //Put featureCoordsys
        if (!featureCoordsys.equals("")) {
            request += ("\t\t\t\t" + featureCoordsys + "\r\n");
        }

        //Put filterCoordsys
        if (!filterCoordsys.equals("")) {
            request += ("\t\t\t\t" + filterCoordsys + "\r\n");
        }

        //If envelope exists, put SPATIALFILTER
        if (!envelope.equals("")) {
            request += "\t\t\t\t<SPATIALFILTER relation=\"envelope_intersection\">\r\n";
            request += ("\t\t\t\t" + envelope + "\r\n");
            request += "\t\t\t\t</SPATIALFILTER>\r\n";
        }

        //Close SPATIALQUERY
        request += "\t\t\t\t</SPATIALQUERY>\r\n";

        //Close GET_FEATURES
        request += "\t\t\t</GET_FEATURES>\r\n";

        //Close request and ArcXML
        request += ArcXML.endRequest();

        //Return request
        logger.info("\n\tSUBFIELDS=\t" + strSubf + "\n\tWHERE=\t" + where);
        return request;
    }

    /**
     * @param status
     * @return
     */
    public static String getLayerExtentRequest(ArcImsVectStatus status) {
        /*
        <?xml version = '1.0' encoding = 'UTF-8'?>
        <ARCXML version="1.1">
        <REQUEST>
        <GET_FEATURES outputmode="newxml" globalenvelope="true" skipfeatures="true">
                <LAYER id="1" />
                        <SPATIALQUERY subfields="#ALL#" >
                                <FEATURECOORDSYS id="4326"/>
                        </SPATIALQUERY>
                </GET_FEATURES>
        </REQUEST>
        </ARCXML>
                         */

        /**
             * The layer to retrieve
             */
        String layerid = (String) status.getLayerIds().get(0);

        /**
        * Gets the ServiceInfo from the status
        */
        ServiceInformation si = status.getServiceInfo();

        /**
        * The EPSG code that image requested will have,
        * the ArcIMS server will reproject data into this
        * code, see <a href="http://www.epsg.org">EPSG</a>
        */
        String srsView = new String();
        String srsServ = si.getFeaturecoordsys();

        /**
             * Is the ServiceInfo FeatureCoordsys assumed?
             */
        boolean srsAssumed = si.isSrsAssumed();

        /**
             * We suppose that status.getSrs() always will give a
             * string started by this string
             */
        String ini_srs = ServiceInfoTags.vINI_SRS;

        /**
             * Assign the srs from the status
             * @see org.gvsig.remoteClient.RemoteClientStatus#getSrs()
             */
        if (!srsAssumed && status.getSrs().startsWith(ini_srs)) {
            srsView = status.getSrs().substring(ini_srs.length()).trim();
        } else {
            srsView = "";
            srsServ = "";
        }

        /**
             * Create a void subfields array
             */
        String[] strSubf = new String[1];
        strSubf[0] = "";

        /**
        * Finally, we can retrieve the correct ArcXML
        */
        return getFeaturesRequest(layerid, //layer to retrieve
            0, //beginrecord
            strSubf, //subfields
            "", //where
            ArcXML.getFilterCoordsys(srsServ), //SRS of the service
            ArcXML.getFeatureCoordsys(srsView), //SRS of the view
            "", //envelope
            "", //accuracy
            true, //globalenvelope
            true, //skipfeatures
            false //geometry
        );
    }

    public static String getAttributesRequest(ArcImsVectStatus status,
        int featCount) {
        /*
        <?xml version = '1.0' encoding = 'UTF-8'?>
        <ARCXML version="1.1">
        <REQUEST>
        <GET_FEATURES outputmode="newxml" geometry="false" envelope="true" globalenvelope="true">
        <LAYER id="1" />
        <SPATIALQUERY subfields="#ALL#" >
        <FILTERCOORDSYS id="4326"/>
        <FEATURECOORDSYS id="23030"/>
        <SPATIALFILTER relation="area_intersection">
                <ENVELOPE minx="-10" miny="38" maxx="2" maxy="41"/>
        </SPATIALFILTER>
        </SPATIALQUERY>
        </GET_FEATURES>
        </REQUEST>
        </ARCXML>
         */

        /**
        * The layer to retrieve
        */
        String layerid = (String) status.getLayerIds().get(0);

        /**
        * Gets the Decimal Separator from the status.ServiceInfo
        */
        ServiceInformation si = status.getServiceInfo();
        char ds = si.getSeparators().getDs();

        /**
        * The EPSG code that image requested will have,
        * the ArcIMS server will reproject data into this
        * code, see <a href="http://www.epsg.org">EPSG</a>
        */
        String srsView = new String();

        /**
             * Is the ServiceInfo FeatureCoordsys assumed?
             */
        boolean srsAssumed = si.isSrsAssumed();

        /**
             * We suppose that status.getSrs() always will give a
             * string started by this string
             */
        String ini_srs = ServiceInfoTags.vINI_SRS;

        /**
             * Assign the srs from the status
             * @see org.gvsig.remoteClient.RemoteClientStatus#getSrs()
             */
        if (!srsAssumed && status.getSrs().startsWith(ini_srs)) {
            srsView = status.getSrs().substring(ini_srs.length()).trim();
        } else {
            srsView = "";
        }

        /**
        * Finally, we can retrieve the correct ArcXML
        */
        return getFeaturesRequest(layerid, //layer to retrieve
            featCount + 1, status.getSubfields(), //subfields
            status.getWhere(), //where
            ArcXML.getFilterCoordsys(srsView), //SRS of the service
            ArcXML.getFeatureCoordsys(srsView), //SRS of the view
            ArcXML.getEnvelope(status.getExtent(), ds), //envelope
            "", //accuracy
            true, //GlobalEnvelope
            false, //SkipFeatures
            false //geometry
        );
    }

    public static String getAttributesRequest(ArcImsVectStatus statusCloned) {
        return getAttributesRequest(statusCloned, 0);
    }

    public static String getIdsRequest(ArcImsVectStatus status, int featCount,
        String idName) {
        String[] idNameA = { idName };

        /**
        * Finally, we can retrieve the correct ArcXML
        */
        return getFeaturesRequest((String) status.getLayerIds().get(0), //layer to retrieve
            featCount, idNameA, //subfields
            status.getWhere(), //where
            "", //SRS of the service
            "", //SRS of the view
            "", //envelope
            "", //accuracy
            true, //GlobalEnvelope
            false, //SkipFeatures
            false //geometry
        );
    }
}

