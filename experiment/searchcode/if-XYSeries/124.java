package net.atlefren.heightprofile.service;

import com.vividsolutions.jts.geom.Coordinate;
import net.atlefren.heightprofile.model.TrackPoint;
import org.geotools.referencing.GeodeticCalculator;
import org.jfree.data.xy.XYSeries;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: atle
 * Date: 7/30/11
 * Time: 6:04 PM
 */
public class SeriesGenerator {

    public SeriesGenerator() {
    }

    public XYSeries generateSeries(ArrayList<TrackPoint> points){
        GeodeticCalculator e = new GeodeticCalculator();
        Iterator it = points.iterator ();
        XYSeries series = new XYSeries("XYGraph");

        Double totalDistOrt = 0.0;
        TrackPoint last = null;
        while (it.hasNext ()) {
            TrackPoint point = (TrackPoint)it.next();
            Double distO;
            if(last != null){
                e.setStartingGeographicPoint(last.getLon(), last.getLat());
                e.setDestinationGeographicPoint(point.getLon(), point.getLat());
                distO = e.getOrthodromicDistance();
            }else {

                distO = 0.0;
            }
            totalDistOrt += distO;
            series.add(totalDistOrt/1000.0,(Double)point.getEle());
            last = point;
        }
        return series;
    }

}

