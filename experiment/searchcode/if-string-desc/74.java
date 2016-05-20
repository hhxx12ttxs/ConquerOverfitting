/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataBuilder;

import com.mysql.jdbc.Connection;
import com.sun.corba.se.spi.orb.DataCollector;
import dataPresentation.BarChart;
import dataPresentation.LineChart;
import dataPresentation.SupervisorPie;
import Servlets.DBConn;
import dataPresentation.SectorBarChart;
import dataPresentation.SectorPie;
import dataPresentation.SemesterBarChart;
import dataPresentation.doubleLineChart;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.data.statistics.Statistics;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author epp2115
 */
///
public class DataBuilder {

    private Vector dataVector;
    DBConn conna = new DBConn();
    ResultSet uprs;
    Hashtable<String, int[]> table;
    int[] values;
    int counter;
    private Enumeration<String> keys;
    private String desc;
    Connection conn;

    public DataBuilder() throws ClassNotFoundException, SQLException {
        conn = conna.getC();
    }

    // Xriimopoieitai gia na vro tous kathigites
    public String getItemsBySupervisor() {
        table = new Hashtable<String, int[]>();
        values = new int[1];
        dataVector = new Vector();
        counter = 0;

        desc = "Παρουσίαση πτυχιακών ανά καθηγητή. Στο Pie Chart φαίνεται το ποσοστό συμμετοχής του κάθε καθηγητή χωρίς να λαμβάνεται υπ'όψην το Status της πτυχιακής.";

        try {
            Statement stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            uprs = stmt.executeQuery("SELECT * from mydb.thesis join mydb.user where thSupervisor = user.userId ");
            while (uprs.next()) {
                String name = (uprs.getString("userFname") + " " + (uprs.getString("userLname")));
                dataVector.add(name);
                table.put(name, new int[]{0, 0, 0});
            }
            uprs = stmt.executeQuery("SELECT * from mydb.thesis join mydb.user where thSupervisor = user.userId ");

            while (uprs.next()) {
                counter++;
                String name = (uprs.getString("userFname") + " " + (uprs.getString("userLname")));
                System.out.println("Doublepost");
                int[] sum = table.get(name);
                sum[0] = sum[0] + 1;
                table.put(name, sum);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }


        JSONObject obj = new JSONObject();
        JSONArray objArray = new JSONArray();

        keys = table.keys();
        while (keys.hasMoreElements()) {
            String data = keys.nextElement().toString();
            int[] array = table.get(data);
            obj = new JSONObject();
            obj.put("Name", data);
            obj.put("thesis", array[0]);
            System.out.println(array[0]);
            float k = (array[0] * 100 / counter);
            obj.put("value", k);
            objArray.add(obj);
        }
        SupervisorPie pie = new SupervisorPie(objArray.toJSONString());
        System.out.println(objArray.toJSONString());
        JSONObject returnObj = new JSONObject();
        returnObj.put("action", "statsSupervisor");
        returnObj.put("iconURL", pie.getIconURL());
        returnObj.put("desc", desc);
        return returnObj.toJSONString();
    }

    // Pita ptixiakon ana tomea
    public String getItemsBySector() {
        table = new Hashtable<String, int[]>();
        values = new int[1];
        dataVector = new Vector();
        counter = 0;

        desc = " Παρουσίαση πτυχιακών ανα τομέα, "
                + "Στο Pie Chart φαίνεται το ποσοστό "
                + "συμμετοχής του κάθε τομέα "
                + "ανεξαρτήτως του status της κάθε πτυχιακής to status της πτυχιακής";
        try {
            Statement stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            uprs = stmt.executeQuery("SELECT * FROM sector Where secId=1 || secId=2");
            while (uprs.next()) {
                table.put(uprs.getString("secTitle"), new int[]{0});

            }
            stmt.close();

            stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            uprs = stmt.executeQuery("SELECT * FROM thesis join user Where thSupervisor=user.userId");
            while (uprs.next()) {
                counter++;
                if (uprs.getInt("secId") == 1) {
                    values = table.get(("Software"));
                    values[0]++;
                    table.put("Software", values);


                } else {
                    values = table.get(("Communication"));
                    values[0]++;
                    table.put("Communication", values);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, null, ex);
        }

        JSONObject obj = new JSONObject();
        JSONArray objArray = new JSONArray();

        keys = table.keys();
        while (keys.hasMoreElements()) {
            String data = keys.nextElement().toString();
            int[] array = table.get(data);
            obj = new JSONObject();
            obj.put("Name", data);
            obj.put("thesis", array[0]);
            System.out.println(array[0]);
            float k = (array[0] * 100 / counter);
            obj.put("value", k);
            objArray.add(obj);
        }
        SectorPie pie = new SectorPie(objArray.toJSONString());
        System.out.println(objArray.toJSONString());
        JSONObject returnObj = new JSONObject();
        returnObj.put("action", "statsSector");
        returnObj.put("iconURL", pie.getIconURL());
        returnObj.put("desc", desc);

        return returnObj.toJSONString();
    }

    //Double line chart
    public String getSemesterSector() {

        table = new Hashtable<String, int[]>();
        dataVector = new Vector();


       desc= "Πτυχιακές τομέα ανα εξάμηνο , στο DoubleLine Chart φαίνονται όλες οι πτυχιακές του κάθε τομέα χωριστά"+
                " ανα εξάμηνο άσχετα απο το status που έχει η πτυχιακή .. Στον οριζόντιο άξονα φαίνεται το εξάμηνο "+
                " και στον κάθετο το Συνολο των πτυχιακών";
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            uprs = stmt.executeQuery("SELECT * FROM semester ");

            while (uprs.next()) {
                table.put(uprs.getString("semTitle"), new int[]{0, 0});
                dataVector.add(uprs.getString("semTitle"));
            }
            stmt.close();


            stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            uprs = stmt.executeQuery("SELECT * from mydb.thesis left join (mydb.semester,mydb.`user`,mydb.sector) on (semester.semId = thesis.semId and`user`.userId = thesis.thSupervisor and  `user`.secId = sector.secId)");

            while (uprs.next()) {

                if (uprs.getInt("secId") == 1) {

                    values = table.get(uprs.getString("semTitle"));
                    values[0]++;
                    table.put(uprs.getString("semTitle"), values);

                } else if (uprs.getInt("secId") == 2) {
                    int status = uprs.getInt("thStatusId");
                    values = table.get(uprs.getString("semTitle"));
                    values[1]++;
                    table.put(uprs.getString("semTitle"), values);

                }

            }
            System.out.println(values);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JSONObject obj = new JSONObject();
        JSONArray objArray = new JSONArray();
        for (int i = 0; i < dataVector.size(); i++) {
            int[] array = table.get(dataVector.elementAt(i).toString());
            obj = new JSONObject();
            obj.put("Semester", dataVector.elementAt(i));
            obj.put("Software", array[0]);
            obj.put("Multimedia", array[1]);

            objArray.add(obj);
        }

        // VISUALILZE
        doubleLineChart doubleLine = new doubleLineChart(objArray.toJSONString());
        JSONObject returnObj = new JSONObject();
        returnObj.put("action", "statsSemesterSector");
        returnObj.put("iconURL", doubleLine.getIconURL());
        returnObj.put("desc", desc);

        return returnObj.toJSONString();
    }

    //Xrisimopoieitai gia na Bgalo to BarChart Thesis/Semester/Status
    public String getItemsBySemester() {
        // Xriimopoieitai gia na vro ta eksamina
        JSONObject obj = new JSONObject();
        JSONArray objArray = new JSONArray();
        dataVector = new Vector();
        table = new Hashtable<String, int[]>();
        values = new int[3];

        desc = "Παρουσίαση πτυχιακών ανά εξάμηνο. Στο Bar Chart φαίνεται ανά εξάμηνο πόσες πτυχιακές είναι accepted, πόσες rejected και πόσες pending. Στον οριζόντιο άξονα φαίνονται τα εξάμηνα, ενώ στον κάθετο το πλήθος των πτυχιακών.";

        try {
            Statement stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            uprs = stmt.executeQuery("SELECT * FROM semester ");

            while (uprs.next()) {

                table.put(uprs.getString("semTitle"), new int[]{0, 0, 0});
                dataVector.add(uprs.getString("semTitle"));
            }
            stmt.close();

            stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            uprs = stmt.executeQuery("SELECT * FROM thesis join semester on semester.semId = thesis.semId ");

            while (uprs.next()) {

                int status = uprs.getInt("thStatusId");
                System.out.println("STATUS   " + status);
                if ((status == 3) || (status == 7) || (status == 11)) {
                    String poss = uprs.getString("semTitle");
                    values = table.get(poss);
                    values[0]++;
                    table.put(poss, values);
                } else if ((status == 13) || (status == 14)) {
                    values = table.get(uprs.getString("semTitle"));
                    values[2]++;

                    table.put(uprs.getString("semTitle"), values);
                } else {
                    values = table.get(uprs.getString("semTitle"));
                    values[1]++;
                    table.put(uprs.getString("semTitle"), values);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        for (int i = 0; i < dataVector.size(); i++) {
            int[] array = table.get(dataVector.elementAt(i).toString());
            obj = new JSONObject();
            obj.put("Semester", dataVector.elementAt(i));
            obj.put("rejected", array[0]);
            obj.put("pending", array[1]);
            obj.put("accepted", array[2]);

            objArray.add(obj);
        }

        //
        //VISUALIZE
        SemesterBarChart SemestersBar = new SemesterBarChart(objArray.toJSONString());
        JSONObject returnObj = new JSONObject();
        returnObj.put("action", "statsThesisSemester");
        returnObj.put("iconURL", SemestersBar.getIconURL());
        returnObj.put("desc", desc);

        return returnObj.toJSONString();

    }

    //Oles oi ptixiakes se line chart ana eksamino
    public String getLineSemester() {

        // Xriimopoieitai gia na vro ta eksamina
        JSONObject obj = new JSONObject();
        JSONArray objArray = new JSONArray();
        dataVector = new Vector();
        table = new Hashtable<String, int[]>();



        desc = "Συνολικές πτυχιακές ανα εξάμηνο. Στο Line Chart φαίνονται όλες οι πτυχιακές ανά εξάμηνο ανεξαρτήτως του Status τους. Στον οριζόντιο άξονα φαίνεται το εξάμηνο και στον κάθετο το σύνολο των πτυχιακών.";
        try {

            Statement stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            uprs = stmt.executeQuery("SELECT * FROM semester ");

            while (uprs.next()) {
                table.put(uprs.getString("semTitle"), new int[]{0});
                dataVector.add(uprs.getString("semTitle"));
            }
            stmt.close();

            System.out.println("Came here");
            stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            uprs = stmt.executeQuery("SELECT * FROM thesis join semester on semester.semId like thesis.semId ");

            while (uprs.next()) {

                int status = uprs.getInt("thStatusId");
                values = table.get(uprs.getString("semTitle"));
                values[0]++;
                table.put(uprs.getString("semTitle"), values);

            }



            for (int i = 0; i < dataVector.size(); i++) {
                int[] array = table.get(dataVector.elementAt(i).toString());
                obj = new JSONObject();
                obj.put("Semester", dataVector.elementAt(i));
                obj.put("total", array[0]);
                objArray.add(obj);
            }

            // VISUALILZE




        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        LineChart lineChart = new LineChart(objArray.toJSONString());
        JSONObject returnObj = new JSONObject();
        returnObj.put("action", "statsLineSemester");
        returnObj.put("iconURL", lineChart.getIconURL());
        returnObj.put("desc", desc);

        return returnObj.toJSONString();
    }

    //Sinolika statuses ptixiakon
    public String getThesisByStatus() {

        JSONObject obj = new JSONObject();
        JSONArray objArray = new JSONArray();
        dataVector = new Vector();
        table = new Hashtable<String, int[]>();
        values = new int[]{0, 0, 0};
        desc = "Καταστάσεις πτυχιακών ανά τομέα. Στο Bar Chart παρουσιάζεται συνολικά το πλήθος των πτυχιακών ανά Status ανεξαρτήτως του τομέα.";

        try {
            Statement stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            table = new Hashtable<String, int[]>();
            table.put("thesisStatus", values);
            uprs = stmt.executeQuery("SELECT * FROM thesis ");

            while (uprs.next()) {

                int status = uprs.getInt("thStatusId");
                values = table.get("thesisStatus");
                if (status == 3 || status == 7 || status == 11) {
                    values[0]++;
                    table.put("thesisStatus", values);
                } else if (status == 13 || status == 14) {
                    //values = table.get("thesisStatus");
                    values[2]++;
                    table.put("thesisStatus", values);
                } else {
                    values[1]++;
                    table.put("thesisStatus", values);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        obj = new JSONObject();
        obj.put("status", "thesisStatus");
        obj.put("rejected", values[0]);
        obj.put("pending", values[1]);
        obj.put("accepted", values[2]);
        objArray.add(obj);

        BarChart thesisStatus = new BarChart(objArray.toJSONString());

        JSONObject returnObj = new JSONObject();
        returnObj.put("action", "statsThesisByStatus");
        returnObj.put("iconURL", thesisStatus.getIconURL());
        returnObj.put("desc", desc);

        return returnObj.toJSONString();
    }

    // Statuses Simetoxon tomea
    public String getSectorParticipationByStatus() {
        JSONObject obj = new JSONObject();
        JSONArray objArray = new JSONArray();
        dataVector = new Vector();
        table = new Hashtable<String, int[]>();
        values = new int[]{0, 0, 0};

        desc = "Πτυχιακές ανά τομέα. Στο Bar Chart φαίνεται το πλήθος των πτυχιακών του κάθε τομέα χωριστά, επιπλέον φαίνονται τα Statuses των πτυχιακών για τον κάθε τομέα. Στον οριζόντιο άξονα φαίνονται οι τομείς και στον κατακόρυφο το σύνολο των πτυχιακών.";
        try {
            Statement stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            uprs = stmt.executeQuery("SELECT * FROM sector Where secId=1 ||secId=2");
            while (uprs.next()) {
                table.put(uprs.getString("secTitle"), new int[]{0, 0, 0});
                dataVector.add(uprs.getString("secTitle"));
            }
            stmt.close();

            stmt = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            uprs = stmt.executeQuery("SELECT * FROM thesis join user Where thSupervisor=user.userId");
            while (uprs.next()) {
                if (uprs.getInt("secId") == 1) {
                    values = table.get(("Software"));
                    int status = uprs.getInt("thStatusId");
                    if (status == 3 || status == 7 || status == 11) {
                        values[0]++;
                        table.put(("Software"), values);
                    } else if (status == 13 || status == 14) {
                        values[2]++;
                        table.put(("Software"), values);
                    } else {
                        values[1]++;
                        table.put(("Software"), values);
                    }


                } else {
                    values = table.get(("Communication"));
                    int status = uprs.getInt("thStatusId");
                    if (status == 3 || status == 7 || status == 11) {
                        values[0]++;
                        table.put(("Communication"), values);
                    } else if (status == 13 || status == 14) {
                        values[2]++;
                        table.put(("Communication"), values);
                    } else {
                        values[1]++;
                        table.put(("Communication"), values);
                    }
                }


            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        for (int i = 0; i < dataVector.size(); i++) {
            int[] array = table.get(dataVector.elementAt(i).toString());
            obj = new JSONObject();
            obj.put("Sector", dataVector.elementAt(i));
            obj.put("rejected", array[0]);
            obj.put("pending", array[1]);
            obj.put("accepted", array[2]);

            objArray.add(obj);
        }

        SectorBarChart sectorPart = new SectorBarChart(objArray.toJSONString());
        JSONObject returnObj = new JSONObject();
        returnObj.put("action", "statsSectorStatusParticipation");
        returnObj.put("iconURL", sectorPart.getIconURL());
        returnObj.put("desc", desc);

        System.out.println("");

        System.out.println(returnObj.toJSONString());

        return returnObj.toJSONString();
    }
}

