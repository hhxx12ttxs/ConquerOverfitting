package com.py110.pubs.ulit;

import com.py110.user.Pyuser;
import org.zkoss.text.MessageFormats;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zul.Messagebox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 根据 zk i3lable 获取信息
 */
public class Ulits {
    public static String getFormatString(String domain,String p,String rejectedValue,Object[] b){
        String[] np = new String[b.length];
        String t = "";
        for (int i =0;i<b.length;i++) {
            Object o =  b[i] ;
            if (o instanceof String) {
                t = o.toString();
                if (t.equals(rejectedValue)) {
                    np[i] = rejectedValue;
                }else{
                    np[i] = getLabel(domain.toLowerCase()+"."+t);
                }
            }else if (o instanceof Class) {
                t =  ((Class) o).getName();
                np[i] = getLabel(t.toLowerCase());
            }

        }
        return MessageFormats.format(p, np);
    }

    public static String getLabel(String name) {
        return Labels.getLabel(name.toLowerCase(), name.toLowerCase()) ;
    }

    public static Pyuser getUser() {
        if (getSession().getAttribute(Constants.LOGIN_USER) != null) {
            return (Pyuser) getSession().getAttribute(Constants.LOGIN_USER);
        }else{
            return null;
        }
    }

    public static String getZgbh() {
        Pyuser pyuser = getUser();
        return pyuser !=null?pyuser.getFbh():"";
    }
    public static String getZgxm() {
        Pyuser pyuser = getUser();
        return pyuser !=null?pyuser.getFname():"";
    }


    /**
     * 组织机构
     * @return
     */
    public static String getDwstru(){
        String bhStru = "";// "无法取得组织机构编码结构，请联系系统管理员，否则系统运行中可能会出现不可预知错误！";
        Map configMap = getConfigMap();
        if (configMap != null) {
            bhStru = (String) configMap.get(Constants.BD_DWSTRU);
        }
        return bhStru;
    }

    /**
     * 费用编号
     * @return
     */
    public static String getYsstru(){
        String bhStru = "";//"无法取得预算项目编码结构，请联系系统管理员，否则系统运行中可能会出现不可预知错误！";
        Map configMap = getConfigMap();
        if (configMap != null) {
            bhStru = (String) configMap.get(Constants.BD_YSXMSTRU);
        }
        return bhStru;
    }
    public static String getStruByType(String sType) {
        String bhStru = "";//"无法取得预算项目编码结构，请联系系统管理员，否则系统运行中可能会出现不可预知错误！";
        if (sType.startsWith("@")) {
            String s = sType.substring(1);
            Map configMap = getConfigMap();
            if (configMap != null) {
                bhStru = (String) configMap.get(s);
            }
        }
        return bhStru;
    }

    public static Map getConfigMap(){
        Session session = getSession();
        Map configMap = (Map)session.getAttribute(Constants.LOGON_CONFIG);
        return configMap;
    }

    public static Session getSession(){
        return  Executions.getCurrent().getSession();
    }

    public static String getLoginIp() {
        String ip = "";
        ip = getSession().getRemoteAddr();
        return ip;
    }

    public static String getCurCwrq(){
        String s = "";
        Map  map = getConfigMap();
        s = (String) map.get(Constants.gsCurCwrq);
        return s;
    }

    public static String getYear(){
        String s = "";
        Map  map = getConfigMap();
        s = (String) map.get(Constants.gsYear);
        return s;
    }

    public static String getMon(){
        String s = "";
        Map  map = getConfigMap();
        s = (String) map.get(Constants.gsMon);
        return s;
    }
    // 登录日期
    public static String getLogonDate() {
        String sd = ""  ;
        Session s = getSession();
        Date d = (Date) s.getAttribute(Constants.LOGON_DATE);
        if (d != null) {
            sd = new SimpleDateFormat(Constants.DATE_SHOW_FORMAT).format(d);
        }
        return sd;
    }

    public static void showMessage(String stext) throws InterruptedException {
        Messagebox.show(stext, "提示", Messagebox.OK, Messagebox.INFORMATION);
    }

    public static int getDwstruLength(int ijs){
        int i = 0,ilen = 0 ;
        String dwStru = getDwstru();
        if (ijs > dwStru.length()) {
            ilen = dwStru.length();
        }else {
            ilen = ijs;
        }
        for (int j = 0;j<ilen;j++) {
            i += Integer.valueOf(dwStru.substring(j, j + 1)).intValue();
        }
        return i;
    }

    public static int getYsstruLength(int ijs){
        int i = 0,ilen = 0 ;
        String bhStru = getYsstru();
        if (ijs > bhStru.length()) {
            ilen = bhStru.length();
        }else {
            ilen = ijs;
        }
        for (int j = 0;j<ilen;j++) {
            i += Integer.valueOf(bhStru.substring(j, j + 1)).intValue();
        }
        return i;
    }
    public static int getStruLength(String bhStru,int ijs){
        int i = 0,ilen = 0 ;
        if (ijs > bhStru.length()) {
            ilen = bhStru.length();
        }else {
            ilen = ijs;
        }
        for (int j = 0;j<ilen;j++) {
            i += Integer.valueOf(bhStru.substring(j, j + 1)).intValue();
        }
        return i;
    }

    public static String getDateTime() {
        String sdate = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        Date date = new Date();
        sdate = dateFormat.format(date);
        return   sdate;
    }

    /*  public static Map getParameters(JCO.Client client,String fname) {
    HashMap allps = new HashMap();
    JCO.Repository mRepository = new JCO.Repository("Sapclient", client);
    IFunctionTemplate functionTemplate = mRepository.getFunctionTemplate(fname);
    JCO.Function function = functionTemplate.getFunction();

//    ips << function.getImportParameterList().name
//        String[] iname =  function.getImportParameterList().getMetaData()
//        String[] idesc =  function.import_parameters.description

    *//*for(int i= 0;i< iname.length; i++){
            allps[iname[i]] = idesc[i]
        }

        String[] ename =  function.export_parameters.name
        String[] edesc =  function.export_parameters.description

        for(int j= 0;j< ename.length; j++){
            allps[ename[j]] = edesc[j]
        }
        String[] tname = function.tableParameterList.name
        String[] tdesc = function.tableParameterList.description

        for(int k= 0;k< iname.length; k++){
            allps[iname[k]] = idesc[k]
        }*//*
        return allps;

    }*/
}

