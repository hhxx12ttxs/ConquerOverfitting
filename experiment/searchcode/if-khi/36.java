/*
 * This class to manage NodeB and RNC for 3G 
 */
package com.lifetek.netmosys.database.DAO;

import com.lifetek.netmosys.client.form.BtsForm;
import com.lifetek.netmosys.client.form.LogServerForm;



import com.lifetek.netmosys.server.paginated.PaginatedDAO;
import com.lifetek.netmosys.database.g3.BO.ExchProvince3GBO;
import com.lifetek.netmosys.database.g3.BO.LogServers3GBO;
import com.lifetek.netmosys.database.g3.BO.NodeBBO;
import com.lifetek.netmosys.database.g3.BO.NodeBInGroupBO;
import com.lifetek.netmosys.database.g3.BO.NodeBConfigBO;
import com.lifetek.netmosys.database.g3.BO.NodeBFullBO;
import com.lifetek.netmosys.server.paginated.CurrentPaginatedList;
import com.lifetek.netmosys.util.Constant;
import com.lifetek.netmosys.util.DateTimeUtils;
import com.lifetek.netmosys.util.QueryCryptUtils;
import com.lifetek.netmosys.util.ResourceBundleUtils;
import com.lifetek.netmosys.util.StringUtils;
import com.lifetek.database.BO.ActionResultBO;

import com.lifetek.database.config.BaseHibernateDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import net.sf.jxls.transformer.XLSTransformer;

import org.apache.struts.action.ActionForm;
import org.hibernate.HibernateException;
import org.hibernate.Query;

/**
 * The Class NodeBRncManageDAO.
 * 
 * @author cuongnh
 */
public class NodeBRncManageDAO extends BaseHibernateDAO {

    /** The resource bundle. */
    ResourceBundle resourceBundle = null;
    /** The resource bundle utils. */
    private ResourceBundleUtils resourceBundleUtils = new ResourceBundleUtils();

    /**
     * Adds the bsc.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO addRnc(ActionForm form,
            HttpServletRequest req) throws
            Exception {

        resourceBundle = resourceBundleUtils.getResource(req);
        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.EDIT_BSC_SERVERS;
        actionResult.setPageForward(pageForward);

        //form log server
        LogServerForm logServerForm = (LogServerForm) form;

        //Set type
        logServerForm.setType(Constant.TYPE_BSC_ADD);
        try {
            if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {//co user

                String bscId = StringUtils.trimAll(logServerForm.getBscId());//Ma node bsc

                String region = StringUtils.trimAll(logServerForm.getRegion());//Vung

                String name = StringUtils.trimAll(logServerForm.getName());//Ten node Bsc

                String url = StringUtils.trimAll(logServerForm.getUrl());//Dia chi url

                String ip = StringUtils.trimAll(logServerForm.getIp());//Dia chi ip

                String port = StringUtils.trimAll(logServerForm.getPort());//Dia chi port

                String userName = StringUtils.trimAll(logServerForm.getUserName());//user name

                String passWord = StringUtils.trimAll(logServerForm.getPassWord());//password

                String exchange = StringUtils.trimAll(logServerForm.getExchange());//Tong dai

                Long enable = logServerForm.getEnable();//cho phep

                Long timeOut = logServerForm.getTimeOut();//timout

                Long timeScan = logServerForm.getTimeScan();//Time Scan

                //Check node Bsc exists

                if (StringUtils.isNotNull(bscId)) {
                    List lstBscExist = findByProperty(LogServers3GBO.class.getName(), "bscId", bscId.toUpperCase());
                    if (lstBscExist.size() > 0) {//Exists

                        logServerForm.setResult(resourceBundle.getString("rncmanagement.DAO.exist"));
                        req.getSession().setAttribute("faultType", "1");
                    } else {

                        LogServers3GBO logServersBO = new LogServers3GBO();

                        //set value
                        logServersBO.setBscId(bscId.toUpperCase());
                        logServersBO.setRegion(region);
                        logServersBO.setName(name);
                        logServersBO.setUrl(url);
                        logServersBO.setIp(ip);
                        logServersBO.setPort(port);
                        logServersBO.setUserName(userName);
                        logServersBO.setPassWord(passWord);
                        logServersBO.setExchange(exchange);
                        logServersBO.setEnable(enable);
                        logServersBO.setTimeOut(timeOut);
                        logServersBO.setTimeScan(timeScan);

                        logServersBO.setServerTypeId(Constant.COMMON.LOG_SERVER_TYPE.BSC);

                        //add new
                        save(logServersBO);

                        //Thong bao ket qua
                        AddressDAO.setResultRequest(req, "common.addSuccess");

                        //return value
                        logServerForm.setResult(resourceBundle.getString("btsConfig.bsc.add.success"));
                        req.getSession().setAttribute("faultType", "0");
                        actionResult.setPageForward(Constant.COMMON.UPDATE_AND_CLOSE);

                    }
                }

            } else {
                logServerForm.setResult(resourceBundle.getString("btsConfig.bsc.add.unauthorized"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logServerForm.setResult(resourceBundle.getString("rncmanagement.DAO.add.fail"));
        }
        return actionResult;
    }

    /**
     * Check exits.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO checkExistedNodeb(
            ActionForm form, HttpServletRequest req) throws Exception {
        resourceBundle = resourceBundleUtils.getResource(req);
        String pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.NODEB_SUCCESS;
        ActionResultBO actionResult = new ActionResultBO();
        actionResult.setPageForward(pageForward);
        BtsForm btsForm = (BtsForm) form;
        try {
            String provinceCode = null;
            AddressDAO.getLevelNodeB(req);
            if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
                if (!Constant.COMMON.ADMIN_TTDHKT.equals(req.getSession().getAttribute(
                        Constant.COMMON.PROVINCE_CODE).toString())) {
                    provinceCode = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).
                            toString();
                    btsForm.setAreaCode(provinceCode);
                    btsForm.setProvinceCode(provinceCode);
                    String provinceName =
                            (String) req.getSession().getAttribute("ProvinceName");
                    btsForm.setProvinceName(provinceName);
                    req.setAttribute("groupList", new AddressDAO().getGroup(
                            provinceCode));
                } else {
                    String SQL = "from AreaBO where parentCode is null";
                    Query q = getSession().createQuery(SQL);
                    List lst = q.list();
                    req.setAttribute("KvList", lst);
                }
            } else {
                btsForm.setResult(resourceBundle.getString("common.unauthorized"));
                return actionResult;
            }


            if (btsForm.getBtsId() != null && !btsForm.getBtsId().equals("")) {
                String SQL = "from NodeBInGroupBO where upper(btsId)= ? ";
                Query q = getSession().createQuery(SQL);
                q.setParameter(0, btsForm.getBtsId().trim().toUpperCase());
                List lstResult = q.list();
                if (lstResult.size() > 0) {

                    btsForm.setResult(
                            resourceBundle.getString("nodeB.management.checkexist.existed"));
                    btsForm = clearForm(btsForm);
                } else {
                    btsForm.setResult(
                            resourceBundle.getString("nodeB.management.checkexist.noexist") + " " + btsForm.getBtsId() + " "
                            + resourceBundle.getString("nodeB.management.checkexist.noexist2"));
                    btsForm = clearForm(btsForm);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Loi trong ham checkExistedNodeb class NodeBRncManageDAO");
        }

        return actionResult;
    }

    /**
     * Clean form.
     * 
     * @param frm the frm
     * 
     * @return the bts form
     */
    public BtsForm clearForm(BtsForm frm) {

        frm.setBscId("");
        frm.setKvCode("");
        frm.setProvinceCode("0");
        frm.setDistrictCode("0");
        frm.setAreaCode("");
        frm.setAddress("");
        frm.setNote("");
        frm.setDistance("");
        frm.setStaffId(0L);
        frm.setGroupId(0L);
        return frm;
    }

    /**
     * Close bts config.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO closeBtsConfig(ActionForm form,
            HttpServletRequest req) throws
            Exception {
        resourceBundle = resourceBundleUtils.getResource(req);
        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = Constant.COMMON.UPDATE_AND_CLOSE;
        //Form
        BtsForm btsForm = (BtsForm) form;
        //Bts ID
        String btsId = null;

        actionResult.setPageForward(pageForward);
        actionResult.setClassName(Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.ClASS_NAME);
        String areaCode = (String) req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE);
        try {
            btsId = QueryCryptUtils.getParameter(req, "btsId");
            if (btsId != null && !btsId.equals("")) {

                String SQL_SELECT_SCHEDULE = "from NodeBBO where btsId = ?  and bscId = ?";
                Query qSchedule = getSession().createQuery(SQL_SELECT_SCHEDULE);
                qSchedule.setParameter(0, btsId);
                qSchedule.setParameter(1, btsForm.getBscId());

                List lstSchedule = qSchedule.list();
                NodeBBO nodeBBO = null;
                //findByProperty(BtsBO.class.getName(), "btsId", btsForm.getBtsId());
                for (Object object : lstSchedule) {
                    nodeBBO = (NodeBBO) object;
                    delete(nodeBBO);

                }

                String SQL_SELECT = "from NodeBInGroupBO where btsId = ?  and bscId = ?";
                Query q = getSession().createQuery(SQL_SELECT);
                q.setParameter(0, btsId);
                q.setParameter(1, btsForm.getBscId());

                List lst = q.list();

                //findByProperty(NodeBInGroupBO.class.getName(), "btsId", btsForm.getBtsId());
                for (Object object : lst) {
                    NodeBInGroupBO btsBO = (NodeBInGroupBO) object;
                    delete(btsBO);
                    //delete(btsBO);
                    req.getSession().setAttribute("result", resourceBundle.getString("btsConfig.closeBtsConfig.bsc.success") + btsId);
                    btsForm.setResult(resourceBundle.getString("btsConfig.closeBtsConfig.bsc.success") + btsId);
//                 actionResult.setPageForward(Constant.COMMON.UPDATE_AND_CLOSE);
                }

                //Thong bao ket qua
                AddressDAO.setResultRequest(req, "common.deleteSuccess");

            }

        } catch (Exception e) {
            req.getSession().setAttribute("result", resourceBundle.getString("btsConfig.closeBtsConfig.bsc.fail") + btsId);
            btsForm.setResult(resourceBundle.getString("btsConfig.closeBtsConfig.bsc.fail") + btsId);
            e.printStackTrace();
            throw e;
        }

        return actionResult;
    }

    /**
     * Close prepare page config.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO closePreparePageConfig(ActionForm form,
            HttpServletRequest req) throws
            Exception {
        //Khai bĂĄo Action
        ActionResultBO actionResult = new ActionResultBO();
        try {
            //Khai bĂĄo form bts cháťŠa dáťŻ liáťu
            BtsForm btsForm = (BtsForm) form;
            //Khai bĂĄo trang forward
            String pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.NODEB_DELETE_FWD;//Cau hinh cho tram quan trong
            //Ma tram

            if (QueryCryptUtils.getParameter(req, "btsId") != null) {
                btsForm.setBtsId(QueryCryptUtils.getParameter(req, "btsId").
                        toString());
            }
            if (QueryCryptUtils.getParameter(req, "bscId") != null) {
                btsForm.setBscId(QueryCryptUtils.getParameter(req, "bscId").
                        toString());
            }
            String close = QueryCryptUtils.getParameter(req, "close");
            if (close != null) {
                req.setAttribute("close", close);
            }
            //Tram quan trong
            if (QueryCryptUtils.getParameter(req, "important") != null) {
                btsForm.setImportant(Long.parseLong(QueryCryptUtils.getParameter(
                        req, "important").toString()));
            }
            //Ma vung tinh thanh
            if (QueryCryptUtils.getParameter(req, Constant.COMMON.AREA_CODE) != null) {//Tinh thanh

                btsForm.setAreaCode(QueryCryptUtils.getParameter(req, Constant.COMMON.AREA_CODE).
                        toString());
            }
            //set page forward
            actionResult.setPageForward(pageForward);
        } catch (Exception e) {
            e.printStackTrace();
            actionResult.setPageForward("error");
        }
        return actionResult;
    }

    /**
     * Delete bsc.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO deleteBsc(ActionForm form, HttpServletRequest req) throws Exception {

        resourceBundle = resourceBundleUtils.getResource(req);
        ActionResultBO actionResult = new ActionResultBO();

//        String pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.DELETE_BSC_SERVERS;

        String bscId = null;

//        actionResult.setPageForward(pageForward);
//        actionResult.setClassName(Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.ClASS_NAME);
//        actionResult.setMethodName("searchBsc");
        try {
            bscId = req.getParameter("bscId");
            if (bscId != null && !bscId.equals("")) {
                String SQL_SELECT = "from LogServers3GBO where bscId = ? ";
                Query q = getSession().createQuery(SQL_SELECT);
                q.setParameter(0, bscId);
                List lst = q.list();
                LogServers3GBO logServersBO = (LogServers3GBO) getSession().get(LogServers3GBO.class.getName(), bscId);

                delete(logServersBO);

                //Thong bao ket qua
                AddressDAO.setResultRequest(req, "common.deleteSuccess");

                //req.getSession().setAttribute("result", resourceBundle.getString("btsConfig.delete.bsc.success") + bscId);
                actionResult.setPageForward(Constant.COMMON.UPDATE_AND_CLOSE);
            }


            //actionResult.setRedirect(true);

        } catch (Exception e) {
            req.getSession().setAttribute("result", resourceBundle.getString("btsConfig.delete.bsc.fail") + bscId);
            e.printStackTrace();
            throw e;
        }

        return actionResult;
    }

    /**
     * Delete bts.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO deleteBts(ActionForm form, HttpServletRequest req) throws Exception {

        ActionResultBO actionResult = new ActionResultBO();
        resourceBundle = resourceBundleUtils.getResource(req);
        String pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.NODEB_SUCCESS;

        //Form
        BtsForm btsForm = (BtsForm) form;

        //Bts ID
        String btsId = btsForm.getBtsId();

        actionResult.setPageForward(pageForward);
        try {
            if (btsId != null && !btsId.equals("")) {
                String SQL_SELECT = "from NodeBInGroupBO where btsId = ? and areaCode=?";
                Query q = getSession().createQuery(SQL_SELECT);
                q.setParameter(0, btsId);
                q.setParameter(1, btsForm.getProvinceCode());
                List lst = q.list();
                NodeBInGroupBO btsBO = (NodeBInGroupBO) getSession().get(NodeBInGroupBO.class.getName(), btsId);

                //delete(btsBO);
                req.getSession().setAttribute("result", resourceBundle.getString("btsConfig.delete.bsc.success") + btsId);
                btsForm.setResult(resourceBundle.getString("btsConfig.delete.bsc.success") + btsId);
//                 actionResult.setPageForward(Constant.COMMON.UPDATE_AND_CLOSE);
            }
        } catch (Exception e) {
            req.getSession().setAttribute("result", resourceBundle.getString("btsConfig.delete.bsc.fail") + btsId);
            btsForm.setResult(resourceBundle.getString("btsConfig.delete.bsc.fail") + btsId);
            e.printStackTrace();
            throw e;
        }

        return actionResult;
    }

    /**
     * Export bts.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO exportBts(
            ActionForm form, HttpServletRequest req) throws Exception {

        resourceBundle = resourceBundleUtils.getResource(req);
        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.EXPORT_SUCCESS;
        actionResult.setPageForward(pageForward);
        BtsForm btsForm = (BtsForm) form;
        try {
            String SQL = "from AreaBO where parentCode is null";
            Query q = getSession().createQuery(SQL);
            List lst = q.list();
            req.setAttribute("KvList", lst);
            if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
//                String provinceCode = req.getSession().getAttribute(
//                        Constant.COMMON.PROVINCE_CODE).toString();


                String areaCode = btsForm.getAreaCode();
                if (!StringUtils.isNotNull(areaCode) || "0".equals(areaCode)) {
                    areaCode = req.getSession().getAttribute(
                            Constant.COMMON.PROVINCE_CODE).toString();
                }


                String btsId = btsForm.getBtsId();
                String bscId = btsForm.getBscId();
                ArrayList parameterList = new ArrayList();

                String provinceName =
                        (String) req.getSession().getAttribute("ProvinceName");
                String SQL_SELECT = "from NodeBFullBO where 1=1 ";

                  if (!StringUtils.checkTtdhkt(areaCode)){
                     SQL_SELECT += " and  areaCode = ? ";
                      parameterList.add(areaCode);
                }
                
                if ((btsId != null) && (!"".equals(btsId.trim()))) {
                    SQL_SELECT += " and upper(btsId) like '%'||?||'%'";
                    parameterList.add(btsId.toUpperCase().trim());
                }
                if ((bscId != null) && (!"".equals(bscId.trim()))) {
                    SQL_SELECT += " and upper(bscId) like '%'||?||'%'";
                    parameterList.add(bscId.toUpperCase().trim());
                }
                Query qBts = getSession().createQuery(SQL_SELECT);
                for (int i = 0; i < parameterList.size(); i++) {
                    qBts.setParameter(i, parameterList.get(i));
                }
                List lstBts = qBts.list();
                List lstExport = new ArrayList();

                //Modify information
                for (int i = 0; i < lstBts.size(); i++) {
                    NodeBFullBO btsFullBO = (NodeBFullBO) lstBts.get(i);
                    if (btsFullBO.getTransmission() != null && btsFullBO.getTransmission() != 2L) {
                        if (btsFullBO.getTransmission() == 0L) {//Quang

                            btsFullBO.setTransmissionName(new ResourceBundleUtils().getResource(req).getString("btsConfig.edit.important.optical"));
                        } else { //Viba

                            btsFullBO.setTransmissionName(new ResourceBundleUtils().getResource(req).getString("btsConfig.edit.important.microwave"));
                        }
                    }
                    lstExport.add(btsFullBO);
                }

                //get template according to locale
                String realFileName = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.templateFileName;

                if ((new ResourceBundleUtils().checkVNLocale(req))) {
                    realFileName = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.templateFileNameViVN;
                }

                String path = req.getRealPath(realFileName);

                String destFileName = "/share/report_out/List_NodeB_"
                        + areaCode + ".xls";
                String pathOut = req.getRealPath(destFileName);
                Map beans = new HashMap();
                //set ngay tao
                beans.put("dateCreate", DateTimeUtils.convertStringToDate(
                        DateTimeUtils.getSysdate()));
                //set ten tinh
                beans.put("provinceName", provinceName);
                //Set danh sach nhan vien
                beans.put("BtsList", lstExport);

                XLSTransformer transformer = new XLSTransformer();
                transformer.transformXLS(path, beans, pathOut);
                req.setAttribute(Constant.COMMON.URL, req.getContextPath() + destFileName);
//    exchLogForm.setUrl(req.getContextPath() + destFileName);
                actionResult.setPageForward(Constant.COMMON.EXPORT_COMMON_SUCCESS);
                btsForm.setResult(resourceBundle.getString("btsConfig.export.success"));
            }

            return actionResult;
        } catch (Exception e) {
            e.printStackTrace();
            btsForm.setResult(resourceBundle.getString("btsConfig.export.fail"));
            return actionResult;
        }

    }

    /**
     * Gets the node bsc.
     * 
     * @param req the req
     * 
     * @return the node bsc
     * 
     * @throws HibernateException the hibernate exception
     */
    public void getNodeBsc(HttpServletRequest req) throws HibernateException {
        //Get node BSC
        // Query query = getSession().createQuery(GET_AREA);
        //                query.setParameter(0, bts.getAreaCode());
        String provinceCode = null;
        provinceCode = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString();
        if (provinceCode != null) {
            String sqlGetnodeBSC = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.HQL_LOAD_LIST_RNC;
            Query node = getSession().createQuery(sqlGetnodeBSC);
            node.setParameter(0, provinceCode);
            List lstnode = node.list();

            Query nodeNodeBInGroup = getSession().createQuery(Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.HQL_LOAD_LIST_RNC_FROM_NODEB);
            nodeNodeBInGroup.setParameter(0, provinceCode);
            List lstnode2 = nodeNodeBInGroup.list();

            req.getSession().setAttribute("NodeBSCList", AddressDAO.getListRNC(lstnode, lstnode2));
        }
    }

    /**
     * Prepare page.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO preparePage(ActionForm form, HttpServletRequest req)
            throws Exception {

        resourceBundle = resourceBundleUtils.getResource(req);
        ActionResultBO actionResult = new ActionResultBO();
        AddressDAO.getLevelNodeB(req);
        //Phan quyen User
        if (req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE) != null) {
            String role = req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();
            if (Constant.ASSSIGN_TASK.STAFF.equals(role) || Constant.ASSSIGN_TASK.LEADER.equals(role)) { //Staff hoac Leader

                actionResult.setPageForward(Constant.COMMON.UN_AUTHORZIED);
                return actionResult;
            }
        }

        String pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.NODEB_SUCCESS;
        actionResult.setPageForward(pageForward);
        BtsForm btsForm = (BtsForm) form;
        try {


            if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
                if (!Constant.COMMON.ADMIN_TTDHKT.equals(req.getSession().getAttribute(
                        Constant.COMMON.PROVINCE_CODE).toString())) {
                    String provinceCode = req.getSession().getAttribute(
                            Constant.COMMON.PROVINCE_CODE).toString();
                    btsForm.setAreaCode(provinceCode);
                    btsForm.setProvinceCode(provinceCode);
                    String provinceName =
                            (String) req.getSession().getAttribute("ProvinceName");
                    btsForm.setProvinceName(provinceName);
                    req.getSession().setAttribute("groupList", new AddressDAO().getGroup(
                            provinceCode));
                } else {
                    String SQL = "from AreaBO where parentCode is null";
                    Query q = getSession().createQuery(SQL);
                    List lst = q.list();
                    req.getSession().setAttribute("KvList", lst);
                }
                //Get node BSC list
                getNodeBsc(req);
            } else {
                btsForm.setResult(
                        resourceBundle.getString("common.unauthorized"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            actionResult.setPageForward("error");
            return actionResult;
        }
        return actionResult;
    }

    /**
     * Prepare page bsc.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO preparePageBsc(ActionForm form,
            HttpServletRequest req) throws
            Exception {

        //Khai báo Action
        ActionResultBO actionResult = new ActionResultBO();
        try {
            //Khai báo form bts ch?a d? li?u
            LogServerForm logServerForm = (LogServerForm) form;

            //Khai báo trang forward
            String pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.EDIT_BSC_SERVERS;//Cap nhat node BSC

            if (QueryCryptUtils.getParameter(req, "close") == null) {//Edit hoac Add
                //Ma node bsc
                if (QueryCryptUtils.getParameter(req, "bscId") != null) {
                    logServerForm.setBscId(QueryCryptUtils.getParameter(req, "bscId").toString());//ma node bsc

                    //Check null
                    if (QueryCryptUtils.getParameter(req, "ip") != null) {
                        logServerForm.setIp(QueryCryptUtils.getParameter(req, "ip").toString());
                    }
                    if (QueryCryptUtils.getParameter(req, "port") != null) {
                        logServerForm.setPort(QueryCryptUtils.getParameter(req, "port").toString());
                    }
                    if (QueryCryptUtils.getParameter(req, "exchange") != null) {
                        logServerForm.setExchange(QueryCryptUtils.getParameter(req, "exchange").toString());
                    }
                    if (QueryCryptUtils.getParameter(req, "timeOut") != null) {
                        logServerForm.setTimeOut(Long.parseLong(QueryCryptUtils.getParameter(req, "timeOut").toString()));
                    }
                    if (QueryCryptUtils.getParameter(req, "userName") != null) {
                        logServerForm.setUserName(QueryCryptUtils.getParameter(req, "userName").toString());
                    }
                    if (QueryCryptUtils.getParameter(req, "passWord") != null) {
                        logServerForm.setPassWord(QueryCryptUtils.getParameter(req, "passWord").toString());
                    }

                    //check null
                    if (QueryCryptUtils.getParameter(req, "region") != null) {
                        logServerForm.setRegion(QueryCryptUtils.getParameter(req, "region").toString());
                    }
                    if (QueryCryptUtils.getParameter(req, "name") != null) {
                        logServerForm.setName(QueryCryptUtils.getParameter(req, "name").toString());
                    }
                    if (QueryCryptUtils.getParameter(req, "url") != null) {
                        logServerForm.setUrl(QueryCryptUtils.getParameter(req, "url").toString());
                    }
                    if (QueryCryptUtils.getParameter(req, "timeScan") != null) {
                        logServerForm.setTimeScan(Long.parseLong(QueryCryptUtils.getParameter(req, "timeScan").toString()));
                    }
                    if (QueryCryptUtils.getParameter(req, "enable") != null) {
                        logServerForm.setEnable(Long.parseLong(QueryCryptUtils.getParameter(req, "enable").toString()));
                    }
                }
                //Loai trang
                if (QueryCryptUtils.getParameter(req, "type") != null) {//Trang cap nhat ten cho tram
                    //Chuyen den trang cap nhat ten tram

                    pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.ADD_BSC_SERVERS;
                }

                //Kieu delete
            } else {//Delete
                req.setAttribute("close", "true");
                req.setAttribute("bscId", QueryCryptUtils.getParameter(req, "bscId").toString());
                pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.EDIT_BSC_SERVERS;
            }
            //set page forward
            actionResult.setPageForward(pageForward);


        } catch (Exception e) {
            e.printStackTrace();
            actionResult.setPageForward("error");
        }
        return actionResult;
    }

    /**
     * Prepare page config.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO preparePageConfig(ActionForm form,
            HttpServletRequest req) throws
            Exception {

        //Khai báo Action
        ActionResultBO actionResult = new ActionResultBO();
        try {
            AddressDAO.getLevelNodeB(req);
            //Khai báo form bts ch?a d? li?u
            BtsForm btsForm = (BtsForm) form;

            //Khai báo trang forward
            String pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.UPDATE_BTS_CONFIG;//Cau hinh cho tram quan trong

            //Ma tram
            String btsId = QueryCryptUtils.getParameter(req, "btsId");
            if (btsId != null) {
                btsForm.setBtsId(btsId.toString());
            }
            if (QueryCryptUtils.getParameter(req, "bscId") != null) {
                btsForm.setBscId(QueryCryptUtils.getParameter(req, "bscId").
                        toString());
            }

            //Lay thong tin cac group
            String provinceCode = req.getSession().getAttribute(
                    Constant.COMMON.PROVINCE_CODE).toString();
            req.getSession().setAttribute("groupList", new AddressDAO().getGroup(
                    provinceCode));

            //Load cac thong tin cua BtsForm
            String SQL_SELECT_BTS = "from NodeBInGroupBO where upper(btsId)= ? and upper(bscId) = ? ";
            Query q = getSession().createQuery(SQL_SELECT_BTS);
            q.setParameter(0, StringUtils.trimAll(btsForm.getBtsId()).toUpperCase());
            q.setParameter(1, StringUtils.trimAll(btsForm.getBscId()).toUpperCase());
            List lstResult = q.list();
            if (lstResult != null && lstResult.size() > 0) {
                NodeBInGroupBO btsBO = (NodeBInGroupBO) lstResult.get(0);

                btsForm.setAddress(btsBO.getAddress());
                btsForm.setTransmission(btsBO.getTransmission());
                btsForm.setBscId(btsBO.getBscId());
                btsForm.setDistance(btsBO.getDistance());
                btsForm.setAreaCode(btsBO.getAreaCode());
                if (btsBO.getGroupId() != null) {
                    btsForm.setGroupId(btsBO.getGroupId());
                } else {
                    btsForm.setGroupId(0L);
                }
                btsForm.setTypeImportant(btsBO.getImportant());
                if (btsBO.getImportant() != null) {
                    List objReturn = AddressDAO.getDescLevelNodeB(req, btsBO.getImportant());
                    req.setAttribute("listLevelNodeB2", objReturn);
                }
                if (btsBO.getDescLevelId() != null) {
                    btsForm.setDescLevel(btsBO.getDescLevelId().toString());
                }
                btsForm.setNote(btsBO.getNote());
                if (btsBO.getStaffId() != null) {
                    btsForm.setStaffId(btsBO.getStaffId());
                } else {
                    btsForm.setStaffId(0L);
                }
                btsForm.setProvinceName((String) req.getSession().getAttribute("ProvinceName"));
            }

            //Get node BSC list
            getNodeBsc(req);

            //Lay thong tin nhan vien thuoc Group
            if (btsForm.getGroupId() != 0L) {
                List lstStaff = new AddressDAO().getStaff(btsForm.getGroupId().toString());
                req.setAttribute("staffList", lstStaff);
            }

            String close = QueryCryptUtils.getParameter(req, "close");
            if (close != null) {
                req.setAttribute("close", close);
            }

            //Ma vung tinh thanh
            String areaCodeCheck = (String) req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE);
            if (areaCodeCheck != null && (Constant.COMMON.ADMIN_TTDHKT.equals(areaCodeCheck) || !StringUtils.checkNotKV(areaCodeCheck))) {
                if (StringUtils.checkNotHasKv(req)) {
                    req.setAttribute("listProvince", AddressDAO.getListProvince(req));
                }
                pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.UPDATE_NODEB_NOT_ADMIN;
            }

            if (QueryCryptUtils.getParameter(req, Constant.COMMON.AREA_CODE) != null) {//Tinh thanh

                btsForm.setAreaCode(QueryCryptUtils.getParameter(req, Constant.COMMON.AREA_CODE).
                        toString());
            }
            //Loai trang
            if (QueryCryptUtils.getParameter(req, "type") != null) {//Trang cap nhat ten cho tram
                //Chuyen den trang cap nhat ten tram

                pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.UPDATE_CONFIG_NAME;

                //Thong tin ve tinh thanh                
                // Query q = getSession().createSQLQuery(sqlGetAreaPrefix);
                //thuan da comment de phan quyen
                req.setAttribute("listProvince", AddressDAO.getListProvince3G(req));

            }
            //set page forward
            actionResult.setPageForward(pageForward);

        } catch (Exception e) {
            e.printStackTrace();
            actionResult.setPageForward("error");
        }
        return actionResult;
    }

    /**
     * Save bsc.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO SaveBsc(
            ActionForm form, HttpServletRequest req) throws Exception {

        resourceBundle = resourceBundleUtils.getResource(req);
        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.NODEB_SUCCESS;
        actionResult.setPageForward(pageForward);
        BtsForm btsForm = (BtsForm) form;
        Long important = btsForm.getTypeImportant();
        AddressDAO.getLevelNodeB(req);
        //neu la 0 thi la null
        if (important.equals(0L)) {
            important = null;
        }
        try {
            if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
                if (!Constant.COMMON.ADMIN_TTDHKT.equals(req.getSession().getAttribute(
                        Constant.COMMON.PROVINCE_CODE).toString())) {
                    String provinceCode = req.getSession().getAttribute(
                            Constant.COMMON.PROVINCE_CODE).toString();
                    btsForm.setAreaCode(provinceCode);
                    btsForm.setProvinceCode(provinceCode);
                    String provinceName =
                            (String) req.getSession().getAttribute("ProvinceName");
                    btsForm.setProvinceName(provinceName);
                    req.setAttribute("groupList", new AddressDAO().getGroup(
                            provinceCode));
                } else {
                    String SQL = "from AreaBO where parentCode is null";
                    Query q = getSession().createQuery(SQL);
                    List lst = q.list();
                    req.setAttribute("KvList", lst);
                }
            } else {
                btsForm.setResult(resourceBundle.getString("common.unauthorized"));
                return actionResult;
            }
            // check bts and bsc have existed in bts or not
            String SQL_SELECT_BTS_SCHEDULE = "from NodeBBO where upper(btsId)= ?  ";
            Query qSchedule = getSession().createQuery(SQL_SELECT_BTS_SCHEDULE);
            qSchedule.setParameter(0, StringUtils.trimAll(btsForm.getBtsId()).toUpperCase());
//            qSchedule.setParameter(1, StringUtils.trimAll(btsForm.getBscId()).toUpperCase());


            String SQL_SELECT_BTS = "from NodeBInGroupBO where upper(btsId)= ?  ";
            Query q = getSession().createQuery(SQL_SELECT_BTS);
            q.setParameter(0, StringUtils.trimAll(btsForm.getBtsId()).toUpperCase());
//            q.setParameter(1, StringUtils.trimAll(btsForm.getBscId()).toUpperCase());
            List lstResult = q.list();
            if (lstResult.size() > 0) {
                btsForm.setResult(resourceBundle.getString("nodeB.management.checkexist.existed"));
                pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.NODEB_SUCCESS;
                actionResult.setPageForward(pageForward);
                return actionResult;

            } else {
                NodeBInGroupBO bts = new NodeBInGroupBO();
                List<NodeBBO> btsBOList = AddressDAO.getVenderByBscId3G(StringUtils.trimAll(btsForm.getBscId()));
                if (btsBOList != null && btsBOList.size() > 0) {
                    bts.setVender(btsBOList.get(0).getVender());
                }
                bts.setBtsId(StringUtils.trimAll(btsForm.getBtsId()));
                bts.setBscId(StringUtils.trimAll(btsForm.getBscId()));
                bts.setAddress(StringUtils.trimAll(btsForm.getAddress()));
                bts.setLogServerId(StringUtils.trimAll(btsForm.getBscId()));

                bts.setAreaCode(StringUtils.trimAll(btsForm.getProvinceCode()));
                bts.setDistance(StringUtils.trimAll(btsForm.getDistance()));
                bts.setNote(StringUtils.trimAll(btsForm.getNote()));

                bts.setStaffId(btsForm.getStaffId());
                bts.setGroupId(btsForm.getGroupId());
//                bts.setImportant(btsForm.getImportant());
                bts.setTransmission(btsForm.getTransmission());

                important = btsForm.getTypeImportant();
                String descLevel = btsForm.getDescLevel();
                if (StringUtils.checkNotHasKv(req) && StringUtils.isNotNull(btsForm.getAreaCode())) {
                    bts.setAreaCode(btsForm.getAreaCode());
                }
                //neu la 0 thi la null
                if (important.equals(0L)) {
                    important = null;
                }
                if (important != null) {
                    bts.setImportant(important);
                }

                bts.setDescLevelId(Long.valueOf(descLevel));
                save(bts);

                List btsScheduleList = qSchedule.list();
                if (btsScheduleList == null || btsScheduleList.size() <= 0) {
                    NodeBBO nodeBBO = new NodeBBO();

                    if (btsBOList != null && btsBOList.size() > 0) {
                        nodeBBO.setVender(btsBOList.get(0).getVender());
                    }
                    nodeBBO.setLogServerId(StringUtils.trimAll(btsForm.getBscId()));

                    nodeBBO.setBtsId(StringUtils.trimAll(btsForm.getBtsId()));
                    nodeBBO.setBcfId(StringUtils.trimAll(btsForm.getBtsId()));
                    nodeBBO.setBscId(StringUtils.trimAll(btsForm.getBscId()));
                    nodeBBO.setAddress(StringUtils.trimAll(btsForm.getAddress()));

                    nodeBBO.setAreaCode(StringUtils.trimAll(btsForm.getProvinceCode()));
                    nodeBBO.setDistance(StringUtils.trimAll(btsForm.getDistance()));
                    nodeBBO.setNote(StringUtils.trimAll(btsForm.getNote()));

                    nodeBBO.setStaffId(btsForm.getStaffId());
                    nodeBBO.setGroupId(btsForm.getGroupId());
                    nodeBBO.setImportant(btsForm.getImportant());
                    save(nodeBBO);
                }

                String hqlUpdateNodeB = "update NodeBBO set important = ? where btsId = ?  ";
                Query sNodeB = getSession().createQuery(hqlUpdateNodeB);
                sNodeB.setParameter(0, important == null ? 0L : important);
                sNodeB.setParameter(1, StringUtils.trimAll(btsForm.getBtsId()));
//                sNodeB.setParameter(2, StringUtils.trimAll(btsForm.getBscId()));
                sNodeB.executeUpdate();

                String hqlUpdate = "update CurrentFault3GBO set important = ? where btsid = ?  ";
                Query s = getSession().createQuery(hqlUpdate);
                s.setParameter(0, important == null ? 0L : important);
                s.setParameter(1, StringUtils.trimAll(btsForm.getBtsId()));
//                s.setParameter(2, StringUtils.trimAll(btsForm.getBscId()));
                s.executeUpdate();
                actionResult.setPageForward(Constant.COMMON.UPDATE_AND_CLOSE);
                btsForm.setResult(resourceBundle.getString("btsConfig.saveBts.add.success"));
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            btsForm.setResult(resourceBundle.getString("btsConfig.saveBts.fail"));
            throw ex;

        }

        return actionResult;
    }

    /**
     * Save bts.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO SaveBTS(
            ActionForm form, HttpServletRequest req) throws Exception {
        resourceBundle = resourceBundleUtils.getResource(req);
        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = Constant.COMMON.UPDATE_AND_CLOSE;
        actionResult.setPageForward(pageForward);
        BtsForm btsForm = (BtsForm) form;
        Long important = btsForm.getTypeImportant();

        //neu la 0 thi la null
        if (important.equals(0L)) {
            important = null;
        }
        try {
            AddressDAO.getLevelNodeB(req);
            // Query node = getSession().createQuery(sqlGetnodeBSC);
            // List lstnode = node.list();
            //  req.setAttribute("NodeBSCList", getListnodeBSC(lstnode));
            if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
                if (!Constant.COMMON.ADMIN_TTDHKT.equals(req.getSession().getAttribute(
                        Constant.COMMON.PROVINCE_CODE).toString())) {
                    String provinceCode = req.getSession().getAttribute(
                            Constant.COMMON.PROVINCE_CODE).toString();
                    btsForm.setAreaCode(provinceCode);
                    btsForm.setProvinceCode(provinceCode);
                    String provinceName =
                            (String) req.getSession().getAttribute("ProvinceName");
                    btsForm.setProvinceName(provinceName);
                    req.setAttribute("groupList", new AddressDAO().getGroup(
                            provinceCode));
                } else {
                    String SQL = "from AreaBO where parentCode is null";
                    Query q = getSession().createQuery(SQL);
                    List lst = q.list();
                    req.setAttribute("KvList", lst);
                }
            } else {
                btsForm.setResult(resourceBundle.getString("common.unauthorized"));
                pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.NODEB_SUCCESS;
                actionResult.setPageForward(pageForward);
                return actionResult;
            }
            // check bts and bsc have existed in bts or not
            String SQL_SELECT_BTS_SCHEDULE = "from NodeBBO where upper(btsId)= ?  ";
            Query qSchedule = getSession().createQuery(SQL_SELECT_BTS_SCHEDULE);
            qSchedule.setParameter(0, StringUtils.trimAll(btsForm.getBtsId()).toUpperCase());
//            qSchedule.setParameter(1, StringUtils.trimAll(btsForm.getBscId()).toUpperCase());


            String SQL_SELECT_BTS = "from NodeBInGroupBO where upper(btsId)= ? ";
            Query q = getSession().createQuery(SQL_SELECT_BTS);
            q.setParameter(0, StringUtils.trimAll(btsForm.getBtsId()).toUpperCase());
//            q.setParameter(1, StringUtils.trimAll(btsForm.getBscId()).toUpperCase());
            List lstResult = q.list();
            if (lstResult.size() > 0) {
                btsForm.setResult(resourceBundle.getString("nodeB.management.checkexist.existed"));
                pageForward = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.NODEB_SUCCESS;
                actionResult.setPageForward(pageForward);
                return actionResult;

            } else {
                NodeBInGroupBO bts = new NodeBInGroupBO();
                List<NodeBBO> btsBOList = AddressDAO.getVenderByBscId3G(StringUtils.trimAll(btsForm.getBscId()));
                if (btsBOList != null && btsBOList.size() > 0) {
                    bts.setVender(btsBOList.get(0).getVender());
                }
                bts.setBtsId(StringUtils.trimAll(btsForm.getBtsId()));
                bts.setBscId(StringUtils.trimAll(btsForm.getBscId()));
                bts.setAddress(StringUtils.trimAll(btsForm.getAddress()));
                bts.setLogServerId(StringUtils.trimAll(btsForm.getBscId()));

                bts.setAreaCode(StringUtils.trimAll(btsForm.getProvinceCode()));
                bts.setDistance(StringUtils.trimAll(btsForm.getDistance()));
                bts.setNote(StringUtils.trimAll(btsForm.getNote()));

                bts.setStaffId(btsForm.getStaffId());
                bts.setGroupId(btsForm.getGroupId());
//                bts.setImportant(btsForm.getImportant());
                bts.setTransmission(btsForm.getTransmission());
                important = btsForm.getTypeImportant();
                String descLevel = btsForm.getDescLevel();

                //neu la 0 thi la null
                if (important.equals(0L)) {
                    important = null;
                }
                if (important != null) {
                    bts.setImportant(important);
                }
                if (StringUtils.checkNotHasKv(req) && StringUtils.isNotNull(btsForm.getAreaCode())) {
                    bts.setAreaCode(btsForm.getAreaCode());
                }
                bts.setDescLevelId(Long.valueOf(descLevel));
                save(bts);

                List btsScheduleList = qSchedule.list();
                if (btsScheduleList == null || btsScheduleList.size() <= 0) {
                    NodeBBO nodeBBO = new NodeBBO();

                    if (btsBOList != null && btsBOList.size() > 0) {
                        nodeBBO.setVender(btsBOList.get(0).getVender());
                    }
                    nodeBBO.setLogServerId(StringUtils.trimAll(btsForm.getBscId()));

                    nodeBBO.setBtsId(StringUtils.trimAll(btsForm.getBtsId()));
                    nodeBBO.setBcfId(StringUtils.trimAll(btsForm.getBtsId()));
                    nodeBBO.setBscId(StringUtils.trimAll(btsForm.getBscId()));
                    nodeBBO.setAddress(StringUtils.trimAll(btsForm.getAddress()));

                    nodeBBO.setAreaCode(StringUtils.trimAll(btsForm.getProvinceCode()));
                    nodeBBO.setDistance(StringUtils.trimAll(btsForm.getDistance()));
                    nodeBBO.setNote(StringUtils.trimAll(btsForm.getNote()));

                    nodeBBO.setStaffId(btsForm.getStaffId());
                    nodeBBO.setGroupId(btsForm.getGroupId());
                    nodeBBO.setImportant(btsForm.getImportant());
                    save(nodeBBO);
                }
                String hqlUpdateNodeB = "update NodeBBO set important = ? where btsId = ?  ";
                Query sNodeB = getSession().createQuery(hqlUpdateNodeB);
                sNodeB.setParameter(0, important == null ? 0L : important);
                sNodeB.setParameter(1, StringUtils.trimAll(btsForm.getBtsId()));
//                sNodeB.setParameter(2, StringUtils.trimAll(btsForm.getBscId()));
                sNodeB.executeUpdate();

                String hqlUpdate = "update CurrentFault3GBO set important = ? where btsid = ?  ";
                Query s = getSession().createQuery(hqlUpdate);
                s.setParameter(0, important == null ? 0L : important);
                s.setParameter(1, StringUtils.trimAll(btsForm.getBtsId()));
//                s.setParameter(2, StringUtils.trimAll(btsForm.getBscId()));
                s.executeUpdate();
                actionResult.setPageForward(Constant.COMMON.UPDATE_AND_CLOSE);
                btsForm.setResult(resourceBundle.getString("btsConfig.saveBts.add.success"));

                //Thong bao ket qua
                AddressDAO.setResultRequest(req, "common.addSuccess");

            }


        } catch (Exception ex) {
            ex.printStackTrace();
            btsForm.setResult(
                    resourceBundle.getString("btsConfig.saveBts.fail"));
            throw ex;

        }

        return actionResult;
    }

    /**
     * Search bsc.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO searchBsc(ActionForm form,
            HttpServletRequest req) throws
            Exception {
        //L?p x? lý action
        ActionResultBO actionResult = new ActionResultBO();
        String[] permitedRole = {Constant.ASSSIGN_TASK.TTDHKT, Constant.COMMON.AREA_CODE_KV.KV1, Constant.COMMON.AREA_CODE_KV.KV2, Constant.COMMON.AREA_CODE_KV.KV3};
        actionResult = AddressDAO.checkPermission(actionResult, req, permitedRole);
        if (actionResult == null) {
            actionResult = new ActionResultBO();
        } else {
            return actionResult;
        }

        //Cau lenh truy van du lieu
        StringBuffer sqlFrom = new StringBuffer();
        StringBuffer sqlWhere = new StringBuffer();

        //Trang forward sau khi x? lý
        String pageForwar = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.SEARCH_BSC_SERVERS;//mac dinh la trang reload

        //form logserver
        LogServerForm logServerForm = (LogServerForm) form;

        try {
            //Check permission for user
            String strProvinceReq = req.getSession().getAttribute(
                    Constant.COMMON.PROVINCE_CODE).toString();

            if (strProvinceReq != null) {//exists user



                //set attribute
                req.setAttribute("listBsc", this.getRncList(form, req));

                //Check count result
                // if (lst.size() > 0) {
                //     req.setAttribute("nullData", "1");
                // }
            } else {//error info user login

                logServerForm.setResult(Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.ERROR_INFO_LOGIN_BTS_CONFIG);
            }

            //page forward
            actionResult.setPageForward(pageForwar);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return actionResult;
    }

    /**
     * Search bsc fail.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO searchBscFail(ActionForm form,
            HttpServletRequest req) throws
            Exception {
        //L?p x? lý action
        ActionResultBO actionResult = new ActionResultBO();

        //Phan quyen User
        if (req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE) != null) {
            String role = req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();
            if (Constant.ASSSIGN_TASK.ADMIN.equals(role) || Constant.ASSSIGN_TASK.STAFF.equals(role) || Constant.ASSSIGN_TASK.LEADER.equals(role)) { //Staff hoac Leader hoac Admin

                actionResult.setPageForward(Constant.COMMON.UN_AUTHORZIED);
                return actionResult;
            }
        }

        //Trang forward sau khi x? lý
        String pageForwar = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.SEARCH_BSC_FAIL;//mac dinh la trang reload

        //form bts
        BtsForm btsForm = (BtsForm) form;

        try {
            //Check permission for user
            String strProvinceReq = req.getSession().getAttribute(
                    Constant.COMMON.PROVINCE_CODE).toString();

            if (strProvinceReq != null) {//exists user
                //Check reload page

                String ajaxReq = QueryCryptUtils.getParameter(req, "ajax");
                if (ajaxReq == null) {//load lan dau

                    pageForwar = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.VIEW_BSC_FAIL;
                }
                if (QueryCryptUtils.getParameter(req, "typePage") != null) {
                    pageForwar = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.PAGE_BSC_POPUP_VIEW;

                    if (ajaxReq != null) {//over see bsc

                        pageForwar = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.PAGE_BSC_OVERSEE_POPUP;
                    }
                }
                /*Lay du lieu de hien thi*/
                Query q = getSession().createQuery(" from RncFail3GBO ");
                req.setAttribute("listBscFail", q.list());
            } else {//error info user login

                btsForm.setResult(Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.ERROR_INFO_LOGIN_BTS_CONFIG);
            }

            //page forward
            actionResult.setPageForward(pageForwar);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return actionResult;
    }

    /**
     * Search bts config.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO searchBtsConfig(ActionForm form,
            HttpServletRequest req) throws
            Exception {
        //L?p x? lý action
        ActionResultBO actionResult = new ActionResultBO();

        //Trang forward sau khi x? lý
        String pageForwar = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.SEARCH_TBS_CONFIG;

        //form bts
        BtsForm btsForm = (BtsForm) form;

        //Lay du lieu tu form client
        String btsId = btsForm.getBtsId();//Ma tram BTS

        String bscId = btsForm.getBscId();//Ma node BSC

        String areaCode = btsForm.getAreaCode();//Ma khu vuc

        String typeCentral = btsForm.getTypeCentral();//Loai tong dai

        try {
            //Check permission for user
            String strProvinceReq = req.getSession().getAttribute(
                    Constant.COMMON.PROVINCE_CODE).toString();//Thuoc tinh request

            if (QueryCryptUtils.getParameter(req, Constant.COMMON.AREA_CODE) != null) {//Goi tu trang popup

                areaCode = QueryCryptUtils.getParameter(req, Constant.COMMON.AREA_CODE).toString();
                if (QueryCryptUtils.getParameter(req, "btsId") != null) {//Goi tu trang popup

                    btsId = QueryCryptUtils.getParameter(req, "btsId").toString();
                }
                if (QueryCryptUtils.getParameter(req, "bscId") != null) {//Goi tu trang popup

                    bscId = QueryCryptUtils.getParameter(req, "bscId").toString();
                }
            }

            //Check reload
//            if (QueryCryptUtils.getParameter(req, "ajax") == null || "".equals(QueryCryptUtils.getParameter(req, "ajax").trim())) {//load lan dau
            if (req.getParameter("ajax") == null || "".equals(req.getParameter("ajax").trim())) {//load lan dau
                //forward den trang ban dau

                pageForwar = Constant.STATION_3G_MONITORING.NODEB_RNC_MANAGEMENT.VIEW_BTS_CONFIG;
            }
            if (strProvinceReq != null) {//exists user
                /*Lay du lieu de hien thi*/

                //Tao cau lenh sql
                StringBuffer sqlCurrent = new StringBuffer();
                StringBuffer sqlWhere = new StringBuffer();
                Long typeImportant = btsForm.getTypeImportant();
                String descLevel = btsForm.getDescLevel();
                sqlCurrent.append("from SearchNodeBConfigBO");

                boolean blnCheckFirst = true;

                //Kiem tra dieu kien
                if (btsId != null && !btsId.equals(""))//Ma tram
                {
                    if (blnCheckFirst) {
                        sqlWhere.append("  where upper(btsId) like '%'||?||'%'");
                        blnCheckFirst = false;
                    }
                }
                if (bscId != null && !bscId.equals("")) {//Ma node quan ly tram

                    if (blnCheckFirst) {
                        sqlWhere.append(" where upper(bscId) like '%'||?||'%'");
                        blnCheckFirst = false;
                    } else {
                        sqlWhere.append(" and upper(bscId) like '%'||?||'%'");
                    }
                }
                //T?nh
                String prefixProvince = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString(); //Check truong hop cac KV

                if (prefixProvince.equals("KV1") || prefixProvince.equals("KV2") || prefixProvince.equals("KV3")) {
                    if (areaCode != null && !areaCode.equals("")
                            && !areaCode.equals("0") && !areaCode.equals("KV1") && !areaCode.equals("KV2") && !areaCode.equals("KV3")) {
                        if (blnCheckFirst) {//dieu kien loc dau tien

                            sqlWhere.append(" where  areaCode = ? ");
                            blnCheckFirst = false;
                        } else {
                            sqlWhere.append(" and areaCode = ? ");
                        }

                    } else {
                        if (blnCheckFirst) {//dieu kien loc dau tien

                            sqlWhere.append(" where  areaCode like ?||'%'");
                            blnCheckFirst = false;
                        } else {
                            sqlWhere.append(" and areaCode like ?||'%'");
                        }
                    }
                } else { //Cac truong hop TTDHKT va Admin Tinh

                    if (areaCode != null && !areaCode.equals("")
                            && !areaCode.equals("0")) {

                        if (blnCheckFirst) {//dieu kien loc dau tien

                            sqlWhere.append(" where  areaCode = ? ");
                            blnCheckFirst = false;
                        } else {
                            sqlWhere.append(" and areaCode = ? ");
                        }
                    }
                }
                //Tong dai
                if (typeCentral != null && !typeCentral.equals("") && !typeCentral.equals("0")) {//loc theo tong dai

                    if (blnCheckFirst) {//tham so dau tien

                        sqlWhere.append(" where exchange=?");
                        blnCheckFirst = false;
                    } else {
                        sqlWhere.append(" and exchange=?");
                    }

                }

                if (typeImportant != null) {
                    if (blnCheckFirst) {
                        sqlWhere.append(" where typeImportant =? ");
                        blnCheckFirst = false;
                    } else {
                        sqlWhere.append(" and typeImportant =? ");
                    }
                }

                if (descLevel != null && !descLevel.equals("") && !descLevel.equals("0")) {
                    if (blnCheckFirst) {
                        sqlWhere.append(" where descLevel =? ");
                        blnCheckFirst = false;
                    } else {
                        sqlWhere.append(" and descLevel =? ");
                    }
                }
                //order by
                sqlWhere.append(" order by bscId");

                //sql current
                sqlCurrent.append(sqlWhere);

                //Thuc hien cau lenh sql                
                Query query = getSession().createQuery(sqlCurrent.toString());

                //Dem so luong ket qua
                String SQL_COUNT_RESULT =
                        "select count( btsId) from SearchNodeBConfigBO  "
                        + sqlWhere.toString();
                Query qCount = getSession().createQuery(SQL_COUNT_RESULT);

                //list to return result
                List lst = null;
                List lstCount = null;

                /*set parameter*/
                int j = 0;//count parameter

                int count = 0;//count result

                //Ma tram BTS
                if (btsId != null && !btsId.equals("")) {
