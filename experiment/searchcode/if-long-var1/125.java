/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package netcdfloader;

import dao.GeoDao;
import database.DBUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import sdnis.wb.util.AreaMonth;
import sdnis.wb.util.BasicAverager;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author wb385924
 */
public class CachedPramaterizedLoader {

    private static Logger log = Logger.getLogger(CachedPramaterizedLoader.class.getName());
    private final String LAT = "lat";
    private final String LON = "lon";
    private final String TIME = "time";
    private final String DAYS_SINCE = "days since";
    private final String MINUTES_SINCE = "minutes since";
    private String timeUnits = null;
    private Connection cxtodb = null;
    private HashMap<AreaMonth, BasicAverager> averagerMap = new HashMap<AreaMonth,BasicAverager>();
    private Date minDate = null;
    public CachedPramaterizedLoader(Connection c, Date md){
        this.cxtodb = c;
        minDate  = md;
        variableNameMap.put("precipitation", "pr");
        variableNameMap.put("tasmax", "tasmax");
        variableNameMap.put("tasmin", "tasmin");
        variableNameMap.put("wet_days", "r02");
        variableNameMap.put("cooling_days", "cd18");
        variableNameMap.put("txx", "txx");
        variableNameMap.put("tnn", "tnn");
    }
    public static void main(String[] args) {

//        DBUtils.get();



//        new PrecipitationLoader().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.CD18_BCSD_0.5_2deg_1961-1999.monthly.nc", 3, 1, "cooling_days");
//        new ParameterizedLoader().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.R02_BCSD_0.5_2deg_1961-1999.monthly.nc", 3, 1, "wet_days");




        

        



//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.pr_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "pr");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.tasmax_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "tasmax");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.tasmin_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "tasmin");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.GD10_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "tasmin");
//
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.FD_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "fd");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.HD18_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "hd18");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.R02_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "r02");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.R90P_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "r90p");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.R90PTOT_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "r90ptot");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.SDII_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "sdii");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.TN10P_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "tn10p");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.TN90P_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "tn90p");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.TNN_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "tnn");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.TX10P_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "tx10p");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.TX90P_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "tx90p");
//        new NetCDFtoCSVConverter().readNCData("C:\\MASSIVE_DATA\\bccr\\out_stats\\bccr_bcm2_0.20c3m.run1.TXX_BCSD_0.5_2deg_1961-1999.monthly.nc", 2, 1, "txx");



//        new PrecipitationLoader().readNCData("D:\\WorldBankDaily\\bccr_bcm2_0\\out_stats\\bccr_bcm2_0.20c3m.run1.pr_BCSD_0.5_2deg_1961-1999.monthly.nc",2,1,"precipitation");

//        new PrecipitationLoader().readNCData("D:\\WorldBankDaily\\bccr_bcm2_0\\out_stats\\bccr_bcm2_0.20c3m.run1.R02_BCSD_0.5_2deg_1961-1999.monthly.nc",2,1,"wetdays");


//        DBUtils.get().closeAll();
    }
    private HashMap<String, String> variableNameMap = new HashMap<String, String>();

    public CachedPramaterizedLoader() {
//        variableNameMap.put("precipitation", "pr");
//        variableNameMap.put("tasmax", "tasmax");
//        variableNameMap.put("tasmin", "tasmin");
//        variableNameMap.put("wet_days", "r02");
//        variableNameMap.put("cooling_days", "cd18");
//        variableNameMap.put("txx", "txx");
//        variableNameMap.put("tnn", "tnn");

    }

    private String getVariableName(String entityType) {
        return variableNameMap.get(entityType);
    }

    private Connection getConnection(){
//        if(cxtodb == null){
//            cxtodb = DBUtils.get().getConnection();
//        }
        return cxtodb;
    }
    private Date computeThisDate(Date startDateForTime, Long time) {
        Calendar cal = Calendar.getInstance();
        if (timeUnits.equals(DAYS_SINCE)) {
            cal.setTimeInMillis(startDateForTime.getTime());
            cal.add(Calendar.DATE, time.intValue());
            cal.add(Calendar.DAY_OF_MONTH, -cal.get(Calendar.DAY_OF_MONTH) + 1);

            return cal.getTime();
        } else if (timeUnits.equals(MINUTES_SINCE)) {
            cal.setTimeInMillis(startDateForTime.getTime());
            cal.add(Calendar.MINUTE, time.intValue());
            cal.add(Calendar.DAY_OF_MONTH, -cal.get(Calendar.DAY_OF_MONTH) + 1);

            return cal.getTime();
        }
        return cal.getTime();
    }
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

    private String formatDate(Date date) {
        return sdf.format(date);
    }

    private Date parseTimeUnits(String unitsString, SimpleDateFormat format) {
        String dateString = null;
        Date returnDate = null;
        try {
            if (unitsString.indexOf(DAYS_SINCE) != -1) {
                dateString = unitsString.substring(DAYS_SINCE.length()).trim();
                timeUnits = DAYS_SINCE;
                returnDate = format.parse(dateString);
            } else if (unitsString.indexOf(MINUTES_SINCE) != -1) {
                dateString = unitsString.substring(MINUTES_SINCE.length()).trim();
                timeUnits = MINUTES_SINCE;
                returnDate = format.parse(dateString);
            }
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return returnDate;
    }

    private Date getStartDAte(NetcdfFile ncFile) {
        Date startDate = null;

//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-dd HH:mm:ss");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-dd");

//            String date = ncFile.findVariable(TIME).getUnitsString().substring(10).trim();
        startDate = parseTimeUnits(ncFile.findVariable(TIME).getUnitsString(), dateFormat);

        System.out.println("tried to parse " + startDate);
        System.out.println("time is " + startDate);



        return startDate;
    }

   

    public void readNCData(String filename, int scenarioId, int runNumber, String entityType, int from, int to) {
        NetcdfFile ncfile = null;
        getConnection();
        try {
            // setup
            ncfile = NetcdfFile.open(filename);
            Variable data = null;
            List<Variable> variables = ncfile.getVariables();
            Date startDateForTime = getStartDAte(ncfile);

            int counter = 0;
            for (Variable v : variables) {

                int[] shape = v.getShape();
                if (shape.length == 3) {
                    System.out.println(counter++ + " This is our three dimensional variable");
                    System.out.println(v.getName().toString() + '\t' + v.getDimensionsString());
                    for (int i = 0; i < shape.length; i++) {
                        System.out.println(shape[i]);
                    }
                    data = v;
                }


            }


            // initialize origin and ranges
            int[] origin = {0, 0, 0};
            int[] offset = {1, 1, 1};
            int[] range = new int[3];
            for (int i = 0; i < range.length; i++) {

                range[i] = data.getDimension(i).getLength() - 1;
                System.out.println(data.getDimension(i).getName() + " max index is " + range[i]);
            }
            log.info("about to read " + filename  + " from " + from + " to " + to);
            long t0 = 0;
            // part 3: iterate through the data
            for (origin[0] = from; origin[0] < to; origin[0]++) {
                t0 = new Date().getTime();
                for (origin[1] = 0; origin[1] < range[1]; origin[1]++) {
                   
                    for (origin[2] = 0; origin[2] < range[2]; origin[2]++) {
                        
                        // System.out.println("0 is " + variables.get(0).getName());
                        // double var0 = variables.get(0).read(new int[]{origin[0]}, new int[]{1}).getFloat(0);


//                        System.out.println("2 is " + variables.get(2).getName()  +  " " + variables.get(2).getNameAndDimensions());
                        double var2 = variables.get(2).read(new int[]{origin[0]}, new int[]{1}).getFloat(0);

//                        System.out.println("1 is " + variables.get(1).getName());
                        double var1 = variables.get(1).read(new int[]{origin[1]}, new int[]{1}).getFloat(0);

//                        System.out.println("0 is " + variables.get(0).getName());
                        double var0 = variables.get(0).read(new int[]{origin[2]}, new int[]{1}).getFloat(0);


                        // System.out.println("origin is " + origin[0] + " " + origin[1] + " " + origin[2]);
                        //System.out.println("value is " + data.read(origin, offset) + "at" + variables.get(1).getName()+" " + var1 + " " + var2 + " " /**+ var3**/);

                        String datavalstring = data.read(origin, offset).toString().trim();
                        if (!datavalstring.equals("1.0E20")) {

                            double dataval = Double.parseDouble(datavalstring);

                            HashMap<String, Double> variableMap = new HashMap<String, Double>();


                            variableMap.put(variables.get(3).getName(), dataval);
//                            System.out.println("storing " + variables.get(3).getNameAndDimensions() + " " + dataval);

                            variableMap.put(variables.get(0).getName(), var0);
//                            System.out.println("storing " + variables.get(1).getName() + " " + var1);

                            variableMap.put(variables.get(1).getName(), var1);
                            //System.out.println("storing " + variables.get(2).getName() + " " + var2);

                            variableMap.put(variables.get(2).getName(), var2);
                            // System.out.println("storing " + variables.get(3).getName() + " " + var3);



//                            if( (getCountryContainingPoint(var2, var3) == 537)){
//                                System.out.println("Found Country data");
//
//                                FileExportHelper.appendToFile("MOZ_" + filename.substring(filename.lastIndexOf("\\")+1,filename.lastIndexOf(".")) + ".txt"  ,dataval + "," + var1 + "," + var2 + "," + var3 + System.getProperty("line.separator"));
//
//                            }



                            storeStatistic(variableMap, startDateForTime, scenarioId, runNumber, entityType);



                        }


                    }

                }
                log.log(Level.INFO, "made it around the world in {0}", (new Date().getTime() - t0));

            }

            log.log(Level.INFO, "about to write this many averages: " +averagerMap.size());
            writeAverages(scenarioId, runNumber, entityType);


        } catch (IOException ex) {
            Logger.getLogger(CachedPramaterizedLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(CachedPramaterizedLoader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                    MultiLoaderManager.getInstance().notifyJobFinished();
                } catch (IOException ioe) {
                    //log("trying to close " + filename, ioe);
                }
            }
        }

    }

    private double flipPostiveLongitude(double lon) {
        if (lon > 180) {
            return lon - 360;
        }
        return lon;
    }

//    private int getCountryContainingPoint(double latitude, double longitude) {
//        Connection c = DBUtils.get().getConnection();
//
//        int id = GeoDao.getIdOfRegionContainingPoint(c, latitude, longitude, "country_boundary", "shape", "country_id");
//
//        DBUtils.get().close(c);
//
//        return id;
//
//    }
    private String createQuery(String queryTemplate, String entityParameter) {

        return queryTemplate.replaceAll("\\@", entityParameter);
    }

    private void insertInitialStatisticRecord(int countryId, double sum, int count,   double min, double max, Date monthDate, int scenarioId, int runNumber, String entityParamter) {
        //String insertStatement = "insert into precipitation (precipitation_sum , precipitation_reading_count, precipitation_country_id, precipitation_date,  precipitation_scenario_id,precipitation_min,precipitation_max) values (?,?,?,?,?,?,?)";

        String insertStatement = "insert into @ (@_sum , @_count, @_area_id, @_date,  @_scenario_id,@_min,@_max, @_run) values (?,?,?,?,?,?,?,?)";

        insertStatement = createQuery(insertStatement, entityParamter);

        Connection c1 = getConnection();
        PreparedStatement ps = null;


        try {
            ps = c1.prepareStatement(insertStatement);
            ps.setDouble(1, sum);
            ps.setInt(2, count);
            ps.setInt(3, countryId);
            ps.setDate(4, new java.sql.Date(monthDate.getTime()));
            ps.setInt(5, scenarioId);
            ps.setDouble(6, min);
            ps.setDouble(7, max);
            ps.setInt(8, runNumber);
            ps.executeUpdate();


        } catch (SQLException ex) {
            Logger.getLogger(CachedPramaterizedLoader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DBUtils.close(ps);
        }


    }

   

    private void updateStatisticRecord(int countryId, Date monthDate, float data) {
       AreaMonth am = new AreaMonth(monthDate, countryId);
       if(!averagerMap.containsKey(am)){
           BasicAverager ba = new BasicAverager();
           averagerMap.put(am, ba);
       }
       averagerMap.get(am).update(data);
    }

    private void writeAverages(int scenarioId, int run, String entityParameter){
        Set<AreaMonth> keys = averagerMap.keySet();
        Iterator<AreaMonth> ami =  keys.iterator();
        while(ami.hasNext()){
            AreaMonth temp = ami.next();
            BasicAverager stat = averagerMap.get(temp);

            insertInitialStatisticRecord(temp.getAreaId(),stat.getSum(),stat.getCount(),stat.getMin(),stat.getMax(), temp.getDate(), scenarioId, run, entityParameter);
            ami.remove();
        }

    }
   



    private void storeStatistic(HashMap<String, Double> dataMap, Date startDateForTime, int scenarioId, int runNumber, String entityType) {
       
        StringBuilder sb = new StringBuilder();

        sb.append("POINT(");
        sb.append(flipPostiveLongitude(dataMap.get(LON)));
        sb.append(" ");
        sb.append(dataMap.get(LAT));
        sb.append(")");


        // get data reading
        String ncvarname = getVariableName(entityType);

//        if(dataMap == null){
//            System.out.println("data map is null");
//        }else{
//            System.out.println(dataMap.size() + " is the size of the map");
//            System.out.println("ncvarname param is " + ncvarname);
//            System.out.println("variables are ");
//            Set<String> keys = dataMap.keySet();
//            for(String k : keys){
//                System.out.println(k);
//            }
//        }
        float precip = dataMap.get(ncvarname).floatValue();


        Date thisDate = computeThisDate(startDateForTime, (dataMap.get(TIME).longValue()));

        if(minDate != null && thisDate.before(minDate)){
            return;
        }



        //System.out.println("adding " + dataMap.get("time").intValue() + " days to " + startDateFortime + " = " + cal.getTime());
        // get coordinates
        double latitude = dataMap.get(LAT).doubleValue();
        double longitude = flipPostiveLongitude(dataMap.get(LON));


         Connection c1 = getConnection();
        long t0 = new Date().getTime();
        int areaId = GeoDao.getIdOfRegionContainingPoint(c1, latitude, longitude, "boundary", "shape", "area_id");
        long t1 = new Date().getTime();
        float gettingIdofPoint = (t1 - t0)/1000;
        
//        log.info("getting id of point: " + gettingIdofPoint);

        if (areaId != -1) {

           
            updateStatisticRecord(areaId, thisDate, precip);



        }
        // int pointId = GeoDao.storeGeometryChild(c, "precipitation", "location", 133, "country", sb.toString());
        // @todo insert the study value, date, and id into the data field

        //DBUtils.get().close(c);
    }

    public void readNCFileIteratively(String filename) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filename);
            ncfile.writeCDL(System.out, true);


            String fileInfo = ncfile.getDetailInfo();
            System.out.println(fileInfo);
            getStartDAte(ncfile);

            Variable v = ncfile.findVariable("pr");

            Variable time = ncfile.findVariable(TIME);
            Variable lat = ncfile.findVariable(LAT);
            Variable lon = ncfile.findVariable(LON);

            int[] origin = new int[]{3650, 89, 179};
            int[] size = new int[]{1, 1, 1};
            Array data3D = v.read(origin, size);

            Array timeVal = time.read(new int[]{3650}, new int[]{1});
            Array latVal = lat.read(new int[]{0}, new int[]{1});
            Array lonVal = lon.read(new int[]{179}, new int[]{1});
//            double[] ja = (double []) data3D.get1DJavaArray( double.class);

            int count = 0;
            while (data3D.hasNext()) {
                System.out.println(data3D.nextDouble());
                count++;
            }
            while (timeVal.hasNext()) {
                System.out.println(timeVal.next());
            }
            while (latVal.hasNext()) {
                System.out.println(latVal.next());
            }
            while (lonVal.hasNext()) {
                System.out.println(lonVal.next());
            }

            System.out.println(count + " is the size");
//            int count = 0;
//            while (data3D.hasNext()) {
//
//            }
            System.out.println("total are: " + count);

        } catch (ucar.ma2.InvalidRangeException ex) {
            Logger.getLogger(CachedPramaterizedLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            //log("trying to open " + filename, ioe);
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ioe) {
                    //log("trying to close " + filename, ioe);
                }
            }
        }

    }
}

