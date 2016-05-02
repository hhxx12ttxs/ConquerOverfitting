package au.com.ephox.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Core class of EMap system that used to perform map initialization, the shortest distance calculation, new routes addition
 * existing route modification, existing routes deletion
 */
public class EMap {

    private static final Logger LOG = LoggerFactory.getLogger(EMapUtils.class);
     
    //store all routes of the map
    private Map allRoutesMap;
    
    
    //store all visited cities
    private List visitedCities;

    /**
     * Constructor used to initialize map.
     *
     * @param initMapJson json string used to initialize map
     */
    public EMap(String initMapJson) {
        LOG.debug("Initialize map with json " + initMapJson);
        this.allRoutesMap = new HashMap();
        this.visitedCities = new ArrayList();
        EMapUtils.addRouteFromJson(initMapJson, this);
    }

    /**
     * Store bidirectional routes in the routes map
     *
     * @param cityFrom "from" city of the route
     * @param cityTo "to" city of the route
     * @param distance distance between "from" city and "to" city                               
     */
    public void addRoute(String cityFrom, String cityTo, int distance) {
        LOG.debug("Add a new route cityFrom = " + cityFrom + " cityTo = " + cityTo + " distance = " + distance);
        List cityListFrom = (List) allRoutesMap.get(cityFrom);
        associateAllCityList(cityListFrom, cityFrom, cityTo, distance, true);
        List cityListTo = (List) allRoutesMap.get(cityTo);
        associateAllCityList(cityListTo, cityFrom, cityTo, distance, false);
    }

    /**
     * Associate list of cities that are connected to from/to cities
     *
     * @param cityList list of cities that are connected to from/to cities
     * @param cityFrom "from" city
     * @param cityTo   "to" city
     * @param distance distance between from/to cities   
     * @param isFrom                 
     */
    public void associateAllCityList(List cityList, String cityFrom, String cityTo, int distance, boolean isFrom) {
        LOG.debug("Associate city list " + cityList);
        if (cityList == null) {
            cityList = new ArrayList();
            if (isFrom)
                allRoutesMap.put(cityFrom, cityList);
            else
                allRoutesMap.put(cityTo, cityList);
        }
        ERoute ERoute = new ERoute();
        if (isFrom) {
            ERoute.setFromCity(cityFrom);
            ERoute.setToCity(cityTo);
        } else {
            ERoute.setFromCity(cityTo);
            ERoute.setToCity(cityFrom);
        }
        ERoute.setDistance(distance);
        if (!cityList.contains(ERoute)) {
            cityList.add(ERoute);
        }
    }

    /**
     * Update a existing route with a new distance
     *
     * @param cityFrom  "from" city of the route
     * @param cityTo    "to" city of the route
     * @param distance  a new distance between "from" city and "to" city                               
     */
    public void updateRoute(String cityFrom, String cityTo, int distance) {
        LOG.debug("Update route cityFrom = " + cityFrom + " cityTo = " + cityTo + " with new distance = " + distance);
        deleteRoute(cityFrom, cityTo);
        addRoute(cityFrom, cityTo, distance);
    }

    /**
     * Delete a existing route on the map
     *
     * @param cityFrom  "from" city of the route to be deleted
     * @param cityTo    "to" city of the route to be deleted
     */
    public void deleteRoute(String cityFrom, String cityTo) {
        LOG.debug("delete route cityFrom = " + cityFrom + " cityTo = " + cityTo);
        Iterator it = allRoutesMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            List<ERoute> cityList = (List<ERoute>) pair.getValue();
            Iterator<ERoute> iter = cityList.iterator();
            while (iter.hasNext()) {
                ERoute ERoute = iter.next();
                //ensure deleting bidirectional routes
                if ((ERoute.getFromCity().equals(cityFrom) && ERoute.getToCity().equals(cityTo)) ||
                        (ERoute.getFromCity().equals(cityTo) && ERoute.getToCity().equals(cityFrom)))
                    iter.remove();
            }
        }
    }

    /**
     * Iteratively calculate the shortest distance between any cities
     *
     * @param cityFrom  "from" city of the route
     * @param cityTo    "to" city of the route
     */
    public void calculateTheShortestDistance(String cityFrom, String cityTo, Map routeMap) {
        LOG.debug("Calculate the shortest distance between " + cityFrom +  " and " + cityTo);
        // temporarily store distance for one route
        int tempDistance = 0;

        //if the city has been visited, then go next city
        if (visitedCities.contains(cityFrom)) {
            return;
        }
        
        //store visited cities
        visitedCities.add(cityFrom);
        if (visitedCities.size() > 1) {
            List initList = (List) allRoutesMap.get(visitedCities.get(0));

            for (int j = 0; j < initList.size(); j++) {
                ERoute w = (ERoute) initList.get(j);
                if (w.getToCity().equals(visitedCities.get(1))) {
                    tempDistance += w.getDistance();
                }
            }

            //calculate total distances of all visited cities
            for (int i = 1; i < visitedCities.size(); i++) {
                List toList = (List) allRoutesMap.get(visitedCities.get(i));
                for (int j = 0; j < toList.size(); j++) {
                    ERoute w = (ERoute) toList.get(j);
                    if (i + 1 < visitedCities.size()) {
                        if (w.getToCity().equals(visitedCities.get(i + 1))) {
                            tempDistance += w.getDistance();
                        }
                    }
                }
            }
        }

        // if reach the destination, then print this route and total distance
        if (cityFrom.equals(cityTo)) {
            String route = visitedCities.get(0).toString();
            for (int i = 1; i < visitedCities.size(); i++) {
                route += "->" + visitedCities.get(i).toString();
            }
            System.out.println(route + "\t distance : " + tempDistance + " KM");
            routeMap.put(tempDistance, route);
            tempDistance = 0;
            visitedCities.remove(visitedCities.size() - 1);
            return;
        }

        // if doesn't reach the destination yet, then get list of cities that "from" city can reach and start another
        // round of calculation based on a new route (iterative calculation)
        List routeList = (List) allRoutesMap.get(cityFrom);
        for (Iterator iterator = routeList.iterator(); iterator.hasNext(); ) {
            ERoute ERoute = (ERoute) iterator.next();
            calculateTheShortestDistance(ERoute.getToCity(), cityTo, routeMap);
        }
        visitedCities.remove(visitedCities.size() - 1);
    }


    /**
     * Output the shortest routes and the shortest distance
     *
     * @param cityFrom     "from" city of the routes
     * @param cityTo       "to" city of the routes
     * @shortestRoutesMap  return a map that store the routes and the shortest distance
     */
    public Map<String, Integer> showTheShortetRoute(String cityFrom, String cityTo) {
        
        Map<String, Integer> shortestRoutesMap = new HashMap<String, Integer>();
        Map routeMap = new HashMap(); //Store all visited cities and distance
        calculateTheShortestDistance(cityFrom, cityTo, routeMap);
        Set s = routeMap.keySet();
        Object[] a = s.toArray();
        int shortestDistance = (Integer) a[0];
        for (int i = 0; i < a.length; i++) {
            if ((Integer) a[i] < shortestDistance) {
                shortestDistance = (Integer) a[i];
            }
        }
        shortestRoutesMap.put(routeMap.get(shortestDistance).toString(), shortestDistance);
        return shortestRoutesMap;
    }
}

