/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tct.dashboard.kpi.slideshow;

import com.appCinfigpage.bean.fc;
import com.mysql.jdbc.Connection;
import com.tct.dashboard.ReportBean;
import com.tct.data.*;
import com.tct.data.jpa.*;
import com.time.timeCurrent;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.persistence.Persistence;

/**
 *
 * @author Tawat
 *
 */
public class KpiSlideShowByLineControl {

    private List<slideshow> slideshowlist;
    private List<Target> targetlist;
//    private List<Line> linelist;
    private List<PicTureList> piclist;
    private List<KpiPicture> kpiPicturelist;
    private long contenpx = 10000;
    private long count;
    private List<NextModelList> nextmodel;
    private List<TargetResult> targetresult;
    private RadarColor radarColor;
    private RadarChartContent radarChartContent;
//    private List<Radar> radarlist;
    private Date getDate;// = new Date();
    private String headercomment;
    private List<Settimetorefresh> settimetorefresh;
    private String dataLine;
    private String dataUtlility;
    
    public KpiSlideShowByLineControl() {
//        loadSlideShow();
    }

    public int getRefreshLine() {
        print("=========== getseting ==============");
        try {
//            if (settimetorefresh == null) {
            settimetorefresh = getSettimetorefreshJpaController().findSettimetorefreshAllAndFrist();
//            }
            print("Refresh : " + settimetorefresh.get(0).getRefreshLine());
            return settimetorefresh.get(0).getRefreshLine().intValue() * 1000;
        } catch (Exception e) {
            print("========== Error : " + e);
            return 50000;
        }
    }

    public int getSlideLine() {
        print("============ getslideline ===========");
        try {
//            if (settimetorefresh == null) {
            settimetorefresh = getSettimetorefreshJpaController().findSettimetorefreshAllAndFrist();
//            }
            print("Slide : " + settimetorefresh.get(0).getSlideLine());
            return settimetorefresh.get(0).getSlideLine().intValue();
        } catch (Exception e) {
            print("========= Error : " + e);
            return 5;
        }
    }
    private SettimetorefreshJpaController settimetorefreshJpaController;

    private SettimetorefreshJpaController getSettimetorefreshJpaController() {
        if (settimetorefreshJpaController == null) {
            settimetorefreshJpaController = new SettimetorefreshJpaController(Persistence.createEntityManagerFactory("tct_projectxPU"));
        }
        return settimetorefreshJpaController;
    }

    public long getContentpx() {
        Locale.setDefault(new Locale("en", "US"));
        getDate = new Date();
//        loadRadarChart();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map params = facesContext.getExternalContext().getRequestParameterMap();
        String lineid = (String) params.get("lineid");
        print("Line id : " + lineid);
        loadPicList();
        loadSlideShow(lineid);
        return contenpx;
    }

    public List<PicTureList> getPiclist() {
        if (piclist == null) {
            piclist = new ArrayList<PicTureList>();
        }
        return piclist;
    }
    private int n = 2;

//    private void loadRadarChart() {
//        print("============ loadradartchart ====================================");
//        try {
//            radarlist = getRadarJpaController().findAllAndNotDeleted();
//            radarChartContent = null;
//            String ticksname = "[";
//            String actualvalue = "[";
//            String targetvalue = "[";
//            String percenvalue = "[";
//            int i = 0;
//            for (Radar radar : radarlist) {
//                if (radar.getShowRadar().equals("true")) {
//                    if (i == (radarlist.size() - 1)) {
//                        ticksname += "[" + i + ", '" + radar.getNameRadar() + "']";
//                        actualvalue += "[" + i + ", " + radar.getActualRadar() + "]";
//                        targetvalue += "[" + i + ", " + radar.getTargetRadar() + "]";
//                        percenvalue += "[" + i + ", " + radar.getPercentRadar() + "]";
//                    } else {
//                        ticksname += "[" + i + ", '" + radar.getNameRadar() + "'],";
//                        actualvalue += "[" + i + ", " + radar.getActualRadar() + "],";
//                        targetvalue += "[" + i + ", " + radar.getTargetRadar() + "],";
//                        percenvalue += "[" + i + ", " + radar.getPercentRadar() + "],";
//                    }
//                    i++;
//                }
//            }
//            ticksname += "]";
//            actualvalue += "]";
//            targetvalue += "]";
//            percenvalue += "]";
//            print("Ticksname : " + ticksname);
//            print("Actualvalue : " + actualvalue);
//            print("Targetvalue : " + targetvalue);
//            print("Percenvalue : " + percenvalue);
//            print("============= load radar color ==============================");
//            radarColor = getRadarColorJpaController().findAllOne();
//
//            radarChartContent = new RadarChartContent(ticksname, actualvalue, targetvalue, percenvalue);
//        } catch (Exception e) {
//            print("Error : " + e);
//        }
//    }
    private void loadPicList() {
        print("=========== load kpi picture list ===============================");
        try {
            piclist = null;
            kpiPicturelist = getKpiPictureJpaController().findAllNotDelete();
            for (KpiPicture kpipicture : kpiPicturelist) {
                if (kpipicture.getShowPicture().equals("true")) {
                    getPiclist().add(new PicTureList(kpipicture.getTitleKpiPicture().toString(), kpipicture.getPictureUrl(), n));
                    n++;
                }
            }
        } catch (Exception e) {
            print("=== Error load pic list : " + e);
        }
    }
    private KpiPictureJpaController kpiPictureJpaController;

    private KpiPictureJpaController getKpiPictureJpaController() {
        if (kpiPictureJpaController == null) {
            kpiPictureJpaController = new KpiPictureJpaController(Persistence.createEntityManagerFactory("tct_projectxPU"));
        }
        return kpiPictureJpaController;
    }

//    private Date getDateTomorrow() throws ParseException {
//        String dd = getDateFormat("dd", getDate);
//        String MM = getDateFormat("MM", getDate);
//        String YYYY = getDateFormat("yyyy", getDate);
//        int d = (Integer.parseInt(dd) + 1);
//        int m = Integer.parseInt(MM);
//        int y = Integer.parseInt(YYYY);
//        int numdate = getDateNum(m);
//        if (d > numdate) {
//            d = 1;
//            if (m == 12) {
//                m = 1;
//                y++;
//            }
//        }
//        dd = ConvertIntToString(d);
//        MM = ConvertIntToString(m);
//        YYYY = ConvertIntToString(y);
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
//        simpleDateFormat.setTimeZone(TimeZone.getDefault());
//        Date date = simpleDateFormat.parse(dd + "-" + MM + "-" + YYYY);
//        print("Tomorrow : " + simpleDateFormat.format(date));
//        return date;
//    }
//    private int getDateNum(int m) {
//        int numdate;
//        switch (m) {
//            case 1:
//                ;
//            case 3:
//                ;
//            case 5:
//                ;
//            case 7:
//                ;
//            case 8:
//                ;
//            case 10:
//                ;
//            case 12:
//                numdate = 31;
//                break;
//            case 2:
//                numdate = 29;
//                break;
//            case 4:
//                ;
//            case 6:
//                ;
//            case 9:
//                ;
//            case 11:
//                numdate = 30;
//                break;
//            default:
//                numdate = 31;
//                break;
//        }
//        return numdate;
//    }
//    private String ConvertIntToString(int n) {
//        if (n + "".length() == 1) {
//            return "0" + n;
//        } else {
//            return n + "";
//        }
//    }
    private double getSumResult(Line line, Target target) throws SQLException {
        double results = 0;
        try {
            String sql = "SELECT SUM(lot_control.qty_output) as qty"
                    + " FROM"
                    + " main_data"
                    + " INNER JOIN lot_control"
                    + " ON main_data.lot_control_id = lot_control.id"
                    + " INNER JOIN current_process"
                    + " ON current_process.main_data_id = main_data.id"
                    + " WHERE lot_control.model_pool_id = '" + target.getIdModelPool().getId() + "' AND"
                    + " current_process.status = 'Finished' AND"
                    + " current_process.line = '" + line.getLineId() + "' AND"
                    + " current_process.time_current >= '" + getDateFormat("yyyy-MM-dd", getDate) + " " + target.getStart() + ":00'  AND"
                    + " current_process.time_current <= '" + getDateFormat("yyyy-MM-dd", getDate) + " " + target.getEnd() + ":00'"
                    + " GROUP BY lot_control.model_pool_id";
            Connection conn = getConnection("mysql");
            print("Conn : " + conn.getHost());
            Statement stm = conn.createStatement();
            print("Step1 : ");
            print("Sql : " + sql);
            print("Step2 : ");
            if (stm.execute(sql)) {
                ResultSet result = stm.executeQuery(sql);
                while (result.next()) {
                    print("Step3 : " + result.getString(1));
                    results = Double.parseDouble(result.getObject(1).toString());
                    print("Step4 : ");
                }
                print("Step5 : ");
            }
            return results;
        } catch (Exception e) {
            print("Error : " + e);
            return 0;
        }
    }

    private double getSumDefect(Line line, Target target) {
        double defect = 0;
        try {
            String sql = "SELECT sum(ng_normal.qty) AS qty"
                    + " FROM"
                    + "  main_data"
                    + " INNER JOIN lot_control"
                    + " ON main_data.lot_control_id = lot_control.id"
                    + " INNER JOIN current_process"
                    + " ON current_process.main_data_id = main_data.id"
                    + " INNER JOIN ng_normal"
                    + " ON ng_normal.current_process_id = current_process.id"
                    + " WHERE"
                    + " lot_control.model_pool_id = '" + target.getIdModelPool().getId() + "'"
                    + " AND (current_process.status = 'Finished' OR current_process.status = 'close')"
                    + " AND current_process.line = '" + line.getLineId() + "'"
                    + " AND current_process.time_current >= '" + getDateFormat("yyyy-MM-dd", getDate) + " " + target.getStart() + ":00'"
                    + " AND current_process.time_current <= '" + getDateFormat("yyyy-MM-dd", getDate) + " " + target.getEnd() + ":00'"
                    + " GROUP BY"
                    + " lot_control.model_pool_id";
            Connection conn = getConnection("mysql");
            print("Conn : " + conn.getHost());
            Statement stm = conn.createStatement();
            print("Step1 : ");
            print("Sql : " + sql);
            print("Step2 : ");
            if (stm.execute(sql)) {
                ResultSet result = stm.executeQuery(sql);
                while (result.next()) {
                    print("Step3 : " + result.getString(1));
                    defect = Double.parseDouble(result.getObject(1).toString());
                    print("Step4 : ");
                }
                print("Step5 : ");
            }
            return defect;
        } catch (Exception e) {
            print("Error : " + e);
            return 0;
        }
    }

    private String roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return twoDForm.format(d);
    }

    private void loadSlideShow(String lineid) {
        print("============= load slide show ===================================");
        try {
            blink = "[";
            int i = 0;
            slideshowlist = null;
            nextmodel = null;
            targetresult = null;
//            linelist = getLineJpaController().findAllLineNotDeletd();
//            for (Line line : linelist) {
            Line line = getLineJpaController().findLine(lineid);
            headercomment = line.getHeaderComment();
            print("headercomment : " + headercomment);
            targetlist = getTargetJpaController().findByIdLineAndCurrentDate(line);
            String model = "";
            
            //@pang
            findDataLine(line);
            findDataUtlility(line);
            for (Target target : targetlist) {
                double sumresult = 0;
                sumresult += getSumResult(line, target);
                double sumdefect = 0;
                sumdefect += getSumDefect(line, target);
                double defectrate = 0;
                defectrate = getProcessDefectrate(sumdefect, sumresult);
                String resultclass = "black", defectclass = "black", defectrateclass = "black";
                if (sumresult < Double.parseDouble(target.getTargetQty())) {
                    resultclass = "red";
                }
                if (sumdefect > 0) {
                    blink += " 'defect" + i + "',";
                    defectclass = "red";
                }
                if (defectrate > Double.parseDouble(line.getTargetDefect())) {
                    blink += " 'defectrate" + i + "',";
                    defectrateclass = "red";
                }
                getTargetresult().add(new TargetResult(i, target.getStart() + "-" + target.getEnd(), target.getTargetQty(), roundTwoDecimals(sumresult), roundTwoDecimals(sumdefect), roundTwoDecimals(defectrate), resultclass, defectclass, defectrateclass));
                if (i >= 6) {
                    break;
                }
                ModelPool modelPool = target.getIdModelPool();
                model = modelPool.getModelType() + "-" + modelPool.getModelSeries() + "-" + modelPool.getModelName();
                i++;
            }
            List<Target> targets = getTargetJpaController().findAllByNotDeleted(line);
            Date date = new Date();
            String tmpM = getDateFormat("MM", date);
            String tmpD = "";
            long sum = 0;
            int j = 0;

            for (Target target : targets) {
                ModelPool modelPool = target.getIdModelPool();

                String modelString = modelPool.getModelType() + "-" + modelPool.getModelSeries() + "-" + modelPool.getModelName();
                if (getDateFormat("MM", target.getDateCreate()).equals(tmpM)) {

                    if (getDateFormat("dd", target.getDateCreate()).equals(tmpD)) {
                        sum += ConvertToLong(target.getTargetQty());
                        tmpD = getDateFormat("dd", target.getDateCreate());
                        getDate = target.getDateCreate();
                        j++;
                    } else {
                        if (!tmpD.isEmpty() && Integer.parseInt(tmpD) >= Integer.parseInt(getDateFormat("dd", new Date()))) {
                            String now = getDateFormat("dd", date);
                            String get = getDateFormat("dd", getDate);
                            print("================= Line : " + line.getLineName());
                            print("================= now : " + now);
                            print("================= get : " + get + " == " + target.getDateCreate());
                            if (now.equals(get)) {
                                getNextmodel().add(new NextModelList(getDateFormat("dd/MM/yyyy", getDate), modelString, String.valueOf(sum), "now"));
                            } else {
                                getNextmodel().add(new NextModelList(getDateFormat("dd/MM/yyyy", getDate), modelString, String.valueOf(sum), "next_model"));
                            }
                            sum = 0;
                        }
                        sum += ConvertToLong(target.getTargetQty());
                        tmpD = getDateFormat("dd", target.getDateCreate());
                        getDate = target.getDateCreate();
                        j++;
                    }
                }
                if (j >= targets.size()) {
                    String now = getDateFormat("dd", date);
                    String get = getDateFormat("dd", getDate);
                    print("================= Line : " + line.getLineName());
                    print("================= now : " + now);
                    print("================= get : " + get);
                    if (now.equals(get)) {
                        getNextmodel().add(new NextModelList(getDateFormat("dd/MM/yyyy", getDate), modelString, String.valueOf(sum), "now"));
                    } else {
                        getNextmodel().add(new NextModelList(getDateFormat("dd/MM/yyyy", getDate), modelString, String.valueOf(sum), "next_model"));
                    }
                    sum = 0;
                }
            }
            // ทำให้ next model ครบ 7
            for (int l = getNextmodel().size(); l < 7; l++) {
                print("L : " + l);
                getNextmodel().add(new NextModelList("-", "-", "-", "next_model"));
            }
            // ทำให้ targetresult ครบ 6
            for (int m = getTargetresult().size(); m < 6; m++) {
                print("M : " + m);
                getTargetresult().add(new TargetResult(i, "-", "-", "-", "-", "-", "black", "black", "black"));
                i++;
            }

            getSlideshowlist().add(new slideshow(nextmodel, targetresult, line.getLineName(), model, line.getStdTime(), line.getStdWorker(), n,null));
            n++;
//            }
        } catch (Exception e) {
            print("Error : " + e);
        }
        count = getSlideshowlist().size() + getPiclist().size() + 2;
        if (count > 0) {
            contenpx = count * 2000;
        }
        print("ContenPx : " + contenpx);
    }
     public String formatData(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }
 
    public String formatMonth(Date date)
    {
        return new SimpleDateFormat("MM").format(date);
    }
    public String  formatYear(Date date)
    {
        return new SimpleDateFormat("yyyy").format(date);
    }
    public void findDataLine(Line line)
    {
        String str ="";
        int sum = 0;
        Date currentNow   = new timeCurrent().getDate();
        for(int i =1 ; i<= 31 ; i++)
        {
//            for(Target objList:getTargetJpaController().findByIdLine(line))
//            {  
//                
//                
//            } 
            int sumTarget = 0;
            if(i==1)
            {
                str +="[";
            }
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                Date   current  = null;
                    try {
                        String  dateStr   = i+"/"+formatMonth(currentNow) +"/"+formatYear(currentNow)+"";
                        Date today = df.parse(dateStr);
                        current   = today;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String dateCurrent  = formatData(current);
                for(Target objList :getTargetJpaController().findByIdLine(line)){
                    //fc.print("=== Date ==== :"+formatData(objList.getDateCreate())+"date loop"+dateCurrent);
                    if(formatData(objList.getDateCreate()).equals(dateCurrent))
                    {
                        sumTarget += Integer.parseInt(objList.getTargetQty());
                        sum += sumTarget; 
                        fc.print("======== sum out ========== : "+sum);
                    }
                }  
                str  +=  "["+i+","+sum+"]";  
            
            if(i!=31)
            {
                str +=",";
            }
            if(i==31)
            {
                str +="]";
            }
        }
        dataLine = str;
        fc.print("dataline :"+str);
    } 
    public void findDataUtlility(Line line)
    {
        String str ="";
        int sum= 0;
        for(int i =1 ; i<= 31 ; i++)
        {
//            for(Target objList:getTargetJpaController().findByIdLine(line))
//            {  
//                
//                
//            } 
            int sumresult = 0;
            for (Target target : getTargetJpaController().findByIdLineAndCurrentDate(line)) {
                    try {
                        sumresult += getSumResult(line, target);
                        sum += sumresult;
                        fc.print("===== sum out model ==== : "+sum);
                    } catch (SQLException ex) {
                        Logger.getLogger(KpiSlideShowControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            if(i==1)
            {
                str +="[";
            }
            str += "["+i+","+sum+"]";
            
            if(i!=31)
            {
                str +=",";
            }
            if(i==31)
            {
                str +="]";
            }
        }
        dataUtlility  = str;
        fc.print("utlility data"+str);
    }
    public RadarColor getRadarColor() {
        return radarColor;
    }

    public RadarChartContent getRadarChartContent() {
        if (radarChartContent == null) {
            radarChartContent = new RadarChartContent();
        }
        return radarChartContent;
    }

    private Long ConvertToLong(String num) {
        return Long.parseLong(num);
    }

    public String getDateFormat(String pattern, Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        return simpleDateFormat.format(date);
    }
    private String blink;

    public String getBlink() {
//        blink = "[";
//        for (slideshow slideshow_ : slideshowlist) {
//            List<TargetResult> targetResultslist = slideshow_.getTargetresultlist();
//            for (TargetResult targetResult : targetResultslist) {
//                if (!targetResult.getDefect().equals("0")) {
//                    blink += "'defect" + targetResult.getIndex() + "', 'defectrate" + targetResult.getIndex() + "',";
//                }
//            }
//        }
        print(blink + "]");
        return blink + "]";
    }

    private void print(String str) {
        System.out.println(str);
    }

    public List<slideshow> getSlideshowlist() {
        if (slideshowlist == null) {
            slideshowlist = new ArrayList<slideshow>();
        }
        return slideshowlist;
    }
    private LineJpaController lineJpaController;

    private LineJpaController getLineJpaController() {
        if (lineJpaController == null) {
            lineJpaController = new LineJpaController(Persistence.createEntityManagerFactory("tct_projectxPU"));
        }
        return lineJpaController;
    }
    private TargetJpaController targetJpaController;

    private TargetJpaController getTargetJpaController() {
        if (targetJpaController == null) {
            targetJpaController = new TargetJpaController(Persistence.createEntityManagerFactory("tct_projectxPU"));
        }
        return targetJpaController;
    }
    private RadarColorJpaController radarColorJpaController;

    private RadarColorJpaController getRadarColorJpaController() {
        if (radarColorJpaController == null) {
            radarColorJpaController = new RadarColorJpaController(Persistence.createEntityManagerFactory("tct_projectxPU"));
        }
        return radarColorJpaController;
    }
    private RadarJpaController radarJpaController;

    private RadarJpaController getRadarJpaController() {
        if (radarJpaController == null) {
            radarJpaController = new RadarJpaController(Persistence.createEntityManagerFactory("tct_projectxPU"));
        }
        return radarJpaController;
    }

    public List<NextModelList> getNextmodel() {
        if (nextmodel == null) {
            nextmodel = new ArrayList<NextModelList>();
        }
        return nextmodel;
    }

    public List<TargetResult> getTargetresult() {
        if (targetresult == null) {
            targetresult = new ArrayList<TargetResult>();
        }
        return targetresult;
    }

    public Connection getConnection(String brand) {
        print(brand);
        if (brand.equals("mysql")) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                return (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/tct_project", "root", "root");
            } catch (Exception ex) {
                Logger.getLogger(ReportBean.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } else {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                return (Connection) DriverManager.getConnection("jdbc:sqlserver://localhost\\SQLEXPRESS:1433;databaseName=tct_project", "test", "1234");
            } catch (Exception ex) {
                Logger.getLogger(ReportBean.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

    }

    private double getProcessDefectrate(double d, double s) {
        /*
         * d :: defect --> NG s :: result --> sum all product
         */
        double processing = 0;
        if (s > 0) {
            processing = (d / s) * 1000000;
        }
        print("Defect rate : " + processing);
        return processing;
    }

    public String getHeadercomment() {
        return headercomment;//.replace(" ", "");
    }

    public void setHeadercomment(String headercomment) {
        this.headercomment = headercomment;
    }

    public String getDataLine() {
        return dataLine;
    }

    public void setDataLine(String dataLine) {
        this.dataLine = dataLine;
    }

    public String getDataUtlility() {
        return dataUtlility;
    }

    public void setDataUtlility(String dataUtlility) {
        this.dataUtlility = dataUtlility;
    }
    
 
}

