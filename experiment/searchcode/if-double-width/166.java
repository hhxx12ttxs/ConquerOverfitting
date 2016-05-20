/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package svg;

import dao.derivedmapdata.DerivedMapDataDao;
import domain.DerivativeStats;
import domain.DerivativeStats.gcm;
import domain.DerivativeStats.scenario;
import domain.DerivativeStats.stat_type;
import domain.DerivativeStats.temporal_aggregation;
import domain.DerivativeStats.time_period;
import domain.DerivativeStats.climatestat;
import domain.web.ShapeSvg;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wb385924
 */
public class SVGMapService {

    private final static Logger log = Logger.getLogger(SVGMapService.class.getName());
    private static SVGMapService service = null;

    public static SVGMapService get() {
        if (service == null) {
            service = new SVGMapService();
        }
        return service;
    }

    /**
     * Calculates the range for the classes by looking at one gcm
     * @param numberOfClasses
     * @param areaId
     * @param timePeriod
     * @param statType
     * @param climateStat
     * @param temporalAggregation
     * @param scenario
     * @param gcm
     * @param run
     * @param month
     * @return
     */
    public ArrayList<ShapeSvg> getSVGMapWithinGCM(int numberOfClasses, int areaId, time_period timePeriod, stat_type statType, climatestat climateStat, temporal_aggregation temporalAggregation, scenario scenario, gcm gcm, int run, int month) {
        // get max min avg
        DerivedMapDataDao dao = DerivedMapDataDao.get();
        ArrayList<Double> list = dao.getMaxMinAvgWithinGCM(areaId, timePeriod, statType, climateStat, temporalAggregation, scenario, gcm, run);
        ArrayList<ShapeSvg> svgs = new ArrayList<ShapeSvg>();
        
        String svg = null;
        if (list.size() < 3) {
            log.warning("did not get max min avg from dao");
        } else {
            log.log(Level.INFO, "searching between {0} {1}", new Object[]{list.get(0), list.get(1)});
            double[][] bounds = getEqualIntervalBounds(list.get(1), list.get(0), numberOfClasses);
            for (int i = 0; i < bounds.length; i++){
                log.log(Level.INFO, "class found between {0} {1}", new Object[]{bounds[i][0], bounds[i][1]});
                svg = dao.getMonthSVg(areaId, bounds[i][1], bounds[i][0], month, timePeriod, statType, climateStat, temporalAggregation, scenario, gcm, run);
                
                svgs.add(new ShapeSvg(svg, climateStat.toString(), new Double(bounds[i][0]).floatValue(), new Double(bounds[i][1]).floatValue()));
            }
        }
        return svgs;
    }


    /**
     * Calculates the range for the classes by looking at all gcms
     * 
     */
    public ArrayList<ShapeSvg> getSVGMapAllGCMS(int numberOfClasses, int areaId, time_period timePeriod, stat_type statType, climatestat climateStat, temporal_aggregation temporalAggregation, scenario scenario, gcm gcm, int run, int month) {
        // get max min avg
        DerivedMapDataDao dao = DerivedMapDataDao.get();
        ArrayList<Double> list = dao.getMaxMinAvgAllGCMs(areaId, timePeriod, statType, climateStat, temporalAggregation, scenario, gcm, run);
        ArrayList<ShapeSvg> svgs = new ArrayList<ShapeSvg>();

        String svg = null;
        if (list.size() < 3) {
            log.warning("did not get max min avg from dao");
        } else {
            log.log(Level.INFO, "searching between {0} {1}", new Object[]{list.get(0), list.get(1)});
            double[][] bounds = getEqualIntervalBounds(list.get(1), list.get(0), numberOfClasses);
            for (int i = 0; i < bounds.length; i++){
                log.log(Level.INFO, "class found between {0} {1}", new Object[]{bounds[i][0], bounds[i][1]});
                svg = dao.getMonthSVg(areaId, bounds[i][1], bounds[i][0], month, timePeriod, statType, climateStat, temporalAggregation, scenario, gcm, run);

                svgs.add(new ShapeSvg(svg, climateStat.toString(), new Double(bounds[i][0]).floatValue(), new Double(bounds[i][1]).floatValue()));
            }
        }
        return svgs;
    }

    private double[][] getEqualIntervalBounds(double min, double max, int numClasses) {
        double[][] bounds = new double[new Double(numClasses).intValue()][2];
        double width = (max - min) / numClasses;
        for (int i = 0; i < numClasses; i++) {
            bounds[i][0] = min + (width * i);
            bounds[i][1] = min + (width * i) + width;
        }

        return bounds;
    }

    private double[][] getTopEqualIntervalBoundsAroundMedianOrAverage(double min, double max, int numClasses, double middleNumber) {

        double[][] bounds = new double[new Double(numClasses).intValue()][2];
        double width = (max - middleNumber) / (new Double(numClasses));
        for (int i = 0; i < numClasses; i++) {
            bounds[i][0] = middleNumber + (width * i);
            bounds[i][1] = middleNumber + (width * i) + width;
        }

        return bounds;
    }

    private double[][] getBottomEqualIntervalBoundsAroundMedianOrAverage(double min, double max, int numClasses, double middleNumber) {

        double[][] bounds = new double[new Double(numClasses).intValue()][2];
        double width = (middleNumber - min) / (new Double(numClasses));
        for (int i = 0; i < numClasses; i++) {
            bounds[i][0] = min + (width * i);
            bounds[i][1] = min + (width * i) + width;
        }

        return bounds;
    }

    public static void main(String[] args) {


//        double[][] bottombounds = SVGMapService.get().getBottomEqualIntervalBoundsAroundMedianOrAverage(0, 100, 3, 47);
//        for (int i = 0; i < bottombounds.length; i++) {
//            System.out.println(bottombounds[i][0] + " " + bottombounds[i][1]);
//        }
//
//        System.out.println();
//        System.out.println();
//
//
//        double[][] topbounds = SVGMapService.get().getTopEqualIntervalBoundsAroundMedianOrAverage(0, 100, 5, 47);
//        for (int i = 0; i < topbounds.length; i++) {
//            System.out.println(topbounds[i][0] + " " + topbounds[i][1]);
//        }

//        System.out.println(bounds[2][0] + " " + bounds[2][1]);
//        System.out.println(bounds[3][0] + " " + bounds[3][1]);

        DerivativeStats.getInstance();
        SVGMapService ser = SVGMapService.get();
        List<ShapeSvg> svg = ser.getSVGMapAllGCMS(4,526, time_period.mid_century, stat_type.mean, DerivativeStats.tempstat.txx, temporal_aggregation.monthly, scenario.b1, gcm.cccma_cgcm3_1, 1, 1);
        System.out.println(svg.get(0).getSvg());
        System.out.println(svg.get(1).getSvg());
        System.out.println(svg.get(2).getSvg());
        System.out.println(svg.get(3).getSvg());
    }
}

