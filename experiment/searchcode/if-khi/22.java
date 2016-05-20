package com.lifetek.netmosys.database.DAO;

import com.lifetek.netmosys.client.form.BtsForm;
import com.lifetek.netmosys.client.form.LogServerForm;


import com.lifetek.netmosys.database.BO.BtsBO;
import com.lifetek.netmosys.database.BO.BtsConfigBO;
import com.lifetek.netmosys.database.BO.BtsFullBO;
import com.lifetek.netmosys.database.BO.BtsScheduleBO;
import com.lifetek.netmosys.database.BO.ExchAreaBO;
import com.lifetek.netmosys.database.BO.LevelBtsBO;
import com.lifetek.netmosys.server.paginated.PaginatedDAO;
import com.lifetek.netmosys.database.BO.LogServersBO;
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
 * The Class BtsDAO.
 * 
 * @author CuongNH
 */
public class BtsDAO extends BaseHibernateDAO {

    /** The Constant ADD_BSC_SERVERS. */
    private static final String ADD_BSC_SERVERS = "addLogServers";//Them moi node BSC
    //Role
    /** The Constant DELETE_BSC_SERVERS. */
    private static final String DELETE_BSC_SERVERS = "deleteLogServers";//Loai bo node BSC
    /** The Constant EDIT_BSC_SERVERS. */
    private static final String EDIT_BSC_SERVERS = "editLogServers";//EDIT BSC
    //message 
    /** The Constant ERROR_INFO_LOGIN_BTS_CONFIG. */
    private static final String ERROR_INFO_LOGIN_BTS_CONFIG =
            "B?n không có quy??n qu?n lý danh sách các tr?m BTS.";
    //user login
    /** The Constant INFO_USER_LOGIN. */
    private static final String INFO_USER_LOGIN = "ProvinceCode";
    /** The Constant PAGE_BSC_OVERSEE_POPUP. */
    private static final String PAGE_BSC_OVERSEE_POPUP = "overseePopupBscFail";//oversee bsc
    /** The Constant PAGE_BSC_POPUP_VIEW. */
    private static final String PAGE_BSC_POPUP_VIEW = "viewPopupBscFail";//view bscfall on popup
    /** The Constant SEARCH_BSC_FAIL. */
    private static final String SEARCH_BSC_FAIL = "searchBscFail";//Danh sach cac node BSC mat ket noi khi trang reload lai
    /** The Constant SEARCH_BSC_SERVERS. */
    private static final String SEARCH_BSC_SERVERS = "searchLogServers";//Tim kiem danh sach node BSC
    /** The Constant SEARCH_CONFIG_NAME. */
    private static final String SEARCH_CONFIG_NAME = "searchConfigBtsName";//load l?i trang
    /** The Constant SEARCH_TBS_CONFIG. */
    private static final String SEARCH_TBS_CONFIG = "searchBtsConfig";//load l?i trang
    /** The template file name. */
    // ---------------------------close page------------------------------------------------
    /** The Constant UPDATE_BTS_CONFIG. */
    private static final String UPDATE_BTS_CONFIG = "updateBtsConfig";//cap nhat thong tin tram Bts 
    /** The Constant UPDATE_CONFIG_NAME. */
    private static final String UPDATE_CONFIG_NAME = "updateConfigBtsName";//cap nhat thong tin tram Bts
    /** The Constant VIEW_BSC_FAIL. */
    private static final String VIEW_BSC_FAIL = "viewBscFail";//Danh sach cac node BSC mat ket noi
    /** The Constant VIEW_BSC_SERVERS. */
    private static final String VIEW_BSC_SERVERS = "viewLogServers";//Danh sach cac node BSC
    /*Các trang c?n forward sau khi x? lý theo công ngh? strusts - hibernate*/
    //Hi?n tr? danh sách các tr?m bts c?n qu?n lý
    /** The Constant VIEW_BTS_CONFIG. */
    private static final String VIEW_BTS_CONFIG = "viewBtsConfigSuccess";//load l?n ?âu
    //Hi?n tr? danh sách các tr?m bts c?n cau hěnh tęn
    /** The Constant VIEW_CONFIG_NAME. */
    private static final String VIEW_CONFIG_NAME = "viewConfigBtsNameSuccess";//load l?n ?âu    
    /** The VIE w_ assigne d_ bs c_ detail. */
    /** The VIE w_ use r_ bts. */
    /** The resource bundle. */
    ResourceBundle resourceBundle = null;
    /** The resource bundle utils. */
    private ResourceBundleUtils resourceBundleUtils = new ResourceBundleUtils();

    /**
     * Them moi node Bsc.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     * @throws Exception the exception
     */
    public ActionResultBO AddBsc(ActionForm form,
            HttpServletRequest req) throws
            Exception {

        resourceBundle = resourceBundleUtils.getResource(req);
        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = EDIT_BSC_SERVERS;
        actionResult.setPageForward(pageForward);




        //form log server
        LogServerForm logServerForm = (LogServerForm) form;

        //Set type
        logServerForm.setType(Constant.TYPE_BSC_ADD);
        try {
            if (req.getSession().getAttribute("ProvinceCode") != null) {//co user

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
                List lstBscExist = findByProperty(LogServersBO.class.getName(), "bscId", bscId.toUpperCase());
                if (lstBscExist.size() > 0) {//Exists

                    logServerForm.setResult(resourceBundle.getString("btsConfig.bsc.add.exist"));
                    req.getSession().setAttribute("faultType", "1");
                } else {

                    LogServersBO logServersBO = new LogServersBO();

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

                    //return value
                    logServerForm.setResult(resourceBundle.getString("btsConfig.bsc.add.success"));
                    req.getSession().setAttribute("faultType", "0");
                    actionResult.setPageForward(Constant.COMMON.UPDATE_AND_CLOSE);
//                    String result = this.restartBscServer(exchange);
//
//                    if(Constant.COMMON.CONTROL_SERVER_COMMAND.SUCCESS_RESULT.equals(result)){
//
//
//                    } else if (Constant.COMMON.CONTROL_SERVER_COMMAND.FAILED_RESULT.equals(result)){
//                        logServerForm.setResult(resourceBundle.getString("restart.bts.failResult"));
//                    }

                }

            } else {
                logServerForm.setResult(resourceBundle.getString("btsConfig.bsc.add.unauthorized"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logServerForm.setResult(resourceBundle.getString("btsConfig.bsc.add.fail"));
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
    public ActionResultBO CheckExits(
            ActionForm form, HttpServletRequest req) throws Exception {
        resourceBundle = resourceBundleUtils.getResource(req);
        String pageForward = "BtsSuccess";
        ActionResultBO actionResult = new ActionResultBO();
        actionResult.setPageForward(pageForward);
        BtsForm btsForm = (BtsForm) form;
        try {
            String provinceCode = null;
            AddressDAO.getLevelBts(req);
            if (req.getSession().getAttribute("ProvinceCode") != null) {
                if (!"ttdhkt".equals(req.getSession().getAttribute(
                        "ProvinceCode").toString())) {
                    provinceCode = req.getSession().getAttribute("ProvinceCode").
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
                String SQL = "from BtsBO where upper(btsId)= ? ";
                Query q = getSession().createQuery(SQL);
                q.setParameter(0, btsForm.getBtsId().trim().toUpperCase());
                List lstResult = q.list();
                if (lstResult.size() > 0) {
//                

                    btsForm.setResult(
                            resourceBundle.getString("btsConfig.checkexist.existed"));
                    btsForm = clearForm(btsForm);
                } else {
                    btsForm.setResult(
                            resourceBundle.getString("btsConfig.checkexist.noexist") + " " + btsForm.getBtsId() + " "
                            + resourceBundle.getString("btsConfig.checkexist.noexist2"));
                    btsForm = clearForm(btsForm);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Loi trong ham checkExits class BtsDAO");
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
        String pageForward = "updateAndClose";
        //Form
        BtsForm btsForm = (BtsForm) form;
        //Bts ID
        String btsId = null;

        actionResult.setPageForward(pageForward);
        actionResult.setClassName("BtsDAO");
        String areaCode = (String) req.getSession().getAttribute("ProvinceCode");
        try {
            btsId = QueryCryptUtils.getParameter(req, "btsId");
            if (btsId != null && !btsId.equals("")) {
                String SQL_SELECT_SCHEDULE = "from BtsScheduleBO where btsId = ?  and bscId = ?";
                Query qSchedule = getSession().createQuery(SQL_SELECT_SCHEDULE);
                qSchedule.setParameter(0, btsId);
                qSchedule.setParameter(1, btsForm.getBscId());

                List lstSchedule = qSchedule.list();
                BtsScheduleBO btsScheduleBO = null;
                //findByProperty(BtsBO.class.getName(), "btsId", btsForm.getBtsId());
                for (Object object : lstSchedule) {
                    btsScheduleBO = (BtsScheduleBO) object;
                    delete(btsScheduleBO);

                }
                String SQL_SELECT = "from BtsBO where btsId = ?  and bscId = ?";
                Query q = getSession().createQuery(SQL_SELECT);
                q.setParameter(0, btsId);
                q.setParameter(1, btsForm.getBscId());

                List lst = q.list();
                BtsBO btsBO = null;
                //findByProperty(BtsBO.class.getName(), "btsId", btsForm.getBtsId());
                for (Object object : lst) {
                    btsBO = (BtsBO) object;
                    delete(btsBO);
                    //delete(btsBO);
                    req.getSession().setAttribute("result", resourceBundle.getString("btsConfig.closeBtsConfig.bsc.success") + btsId);
                    btsForm.setResult(resourceBundle.getString("btsConfig.closeBtsConfig.bsc.success") + btsId);
//                 actionResult.setPageForward(Constant.COMMON.UPDATE_AND_CLOSE);
                }
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
            String pageForward = "closeBtsConfigDetail";//Cau hinh cho tram quan trong
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
            if (QueryCryptUtils.getParameter(req, "areaCode") != null) {//Tinh thanh

                btsForm.setAreaCode(QueryCryptUtils.getParameter(req, "areaCode").
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
     * Loai bo node Bsc.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     * @throws Exception the exception
     */
    public ActionResultBO deleteBsc(ActionForm form, HttpServletRequest req) throws Exception {

        resourceBundle = resourceBundleUtils.getResource(req);
        ActionResultBO actionResult = new ActionResultBO();

        String pageForward = DELETE_BSC_SERVERS;

        String bscId = null;

        actionResult.setPageForward(pageForward);
        actionResult.setClassName("BtsDAO");
        actionResult.setMethodName("searchBsc");
        try {
            bscId = QueryCryptUtils.getParameter(req, "bscId");
            if (bscId != null && !bscId.equals("")) {
                String SQL_SELECT = "from LogServersBO where bscId = ? ";
                Query q = getSession().createQuery(SQL_SELECT);
                q.setParameter(0, bscId);
                List lst = q.list();
                LogServersBO logServersBO = (LogServersBO) getSession().get(LogServersBO.class.getName(), bscId);

                delete(logServersBO);
                //  req.getSession().setAttribute("result", resourceBundle.getString("btsConfig.delete.bsc.success") + bscId);
//                 actionResult.setPageForward(Constant.COMMON.UPDATE_AND_CLOSE);
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
     * Delete BTS.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     * @throws Exception the exception
     */
    public ActionResultBO deleteBts(ActionForm form, HttpServletRequest req) throws Exception {

        ActionResultBO actionResult = new ActionResultBO();
        resourceBundle = resourceBundleUtils.getResource(req);
        String pageForward = "BtsSuccess";

        //Form
        BtsForm btsForm = (BtsForm) form;

        //Bts ID
        String btsId = btsForm.getBtsId();

        actionResult.setPageForward(pageForward);
        try {
            if (btsId != null && !btsId.equals("")) {
                String SQL_SELECT = "from BtsBO where btsId = ? and areaCode=?";
                Query q = getSession().createQuery(SQL_SELECT);
                q.setParameter(0, btsId);
                q.setParameter(1, btsForm.getProvinceCode());
                List lst = q.list();
                BtsBO btsBO = (BtsBO) getSession().get(BtsBO.class.getName(), btsId);

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
//        String pageForward = "exportBtsSuccess";
//        actionResult.setPageForward(pageForward);
        BtsForm btsForm = (BtsForm) form;
        try {
            String SQL = "from AreaBO where parentCode is null";
            Query q = getSession().createQuery(SQL);
            List lst = q.list();
            req.setAttribute("KvList", lst);
            if (req.getSession().getAttribute("ProvinceCode") != null) {

                String areaCode = btsForm.getAreaCode();
                if (!StringUtils.isNotNull(areaCode) || "0".equals(areaCode)) {
                    areaCode = req.getSession().getAttribute(
                            "ProvinceCode").toString();
                }



                String btsId = btsForm.getBtsId();
                String bscId = btsForm.getBscId();
                ArrayList parameterList = new ArrayList();

                String provinceName =
                        (String) req.getSession().getAttribute("ProvinceName");
                String SQL_SELECT = "from BtsFullBO where 1=1 ";

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
                    BtsFullBO btsFullBO = (BtsFullBO) lstBts.get(i);
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
                String realFileName = Constant.COMMON.templateFileName + Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.templateFileName + Constant.COMMON.FILE_EXTENTION;
               

                if ((new ResourceBundleUtils().checkVNLocale(req))) {
                    realFileName = Constant.COMMON.templateFileName + Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.templateFileNameViVN + Constant.COMMON.FILE_EXTENTION;
                 
                }

                String path = req.getRealPath(realFileName);

                String destFileName = "/share/report_out/List_BTS_"
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
                btsForm.setUrl(req.getContextPath() + destFileName);
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
     * Gets the element by parent code.
     * 
     * @param parentCode the parent code
     * 
     * @return the element by parent code
     * 
     * @throws Exception the exception
     */
    public List getElementByParentCode(
            String parentCode) throws Exception {
        try {

            Query query = getSession().createQuery(
                    "from AreaBO where parentCode= ? ");
            query.setParameter(0, parentCode);

            List list = query.list();
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
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
        provinceCode = req.getSession().getAttribute("ProvinceCode").toString();
        if (provinceCode != null) {
            String sqlGetnodeBSC = "SELECT distinct exchangeId,areaCode  FROM ExchAreaBO where areaCode=? ORDER BY NLSSORT(exchangeId,'NLS_SORT=vietnamese') ";
            Query node = getSession().createQuery(sqlGetnodeBSC);
            node.setParameter(0, provinceCode);
            List lstnode = node.list();

            String sqlGetnodeBSCInGroup = "SELECT distinct bscId,areaCode  FROM BtsBO where areaCode=? ORDER BY NLSSORT(bscId,'NLS_SORT=vietnamese') ";
            Query bscInGroup = getSession().createQuery(sqlGetnodeBSCInGroup);
            bscInGroup.setParameter(0, provinceCode);
            List lstnode2 = bscInGroup.list();


            req.getSession().setAttribute("NodeBSCList", AddressDAO.getListBsc(lstnode, lstnode2));
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


        //Phan quyen User
        if (req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE) != null) {
            String role = req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();
            if (Constant.ASSSIGN_TASK.STAFF.equals(role) || Constant.ASSSIGN_TASK.LEADER.equals(role)) { //Staff hoac Leader

                actionResult.setPageForward("notAuthorzied");
                return actionResult;
            }
        }
        AddressDAO.getLevelBts(req);

        String pageForward = "BtsSuccess";
        actionResult.setPageForward(pageForward);
        BtsForm btsForm = (BtsForm) form;
        try {


            if (req.getSession().getAttribute("ProvinceCode") != null) {
                if (!"ttdhkt".equals(req.getSession().getAttribute(
                        "ProvinceCode").toString())) {
                    String provinceCode = req.getSession().getAttribute(
                            "ProvinceCode").toString();
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
     * Kh?i t?o vi?c x? lý BSC
     * C?p nh?t, thęm m?i.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
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
            String pageForward = EDIT_BSC_SERVERS;//Cap nhat node BSC

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

                pageForward = ADD_BSC_SERVERS;
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
     * Kh?i t?o vi?c t?o trang ?? c?p nh?t thông tin tr?m BTS.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     * @throws Exception the exception
     */
    public ActionResultBO preparePageConfig(ActionForm form,
            HttpServletRequest req) throws
            Exception {

        //Khai báo Action
        ActionResultBO actionResult = new ActionResultBO();
        try {
            AddressDAO.getLevelBts(req);
//        String levelBtsSql2 = " from LevelBtsBO where typeImportant=1 or typeImportant=2";
//        List objLevelBts2 = getSession().createQuery(levelBtsSql2).list();
//        req.setAttribute("listLevelBts2", objLevelBts2);
//          String  typeImportant = QueryCryptUtils.getParameter(req, "typeImportant");
//            AddressDAO.getDescLevel(req, Long.parseLong(typeImportant));
            //Khai báo form bts ch?a d? li?u
            BtsForm btsForm = (BtsForm) form;

            //Khai báo trang forward
            String pageForward = UPDATE_BTS_CONFIG;//Cau hinh cho tram quan trong

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
                    "ProvinceCode").toString();
            req.getSession().setAttribute("groupList", new AddressDAO().getGroup(
                    provinceCode));

            //Load cac thong tin cua BtsForm
            String SQL_SELECT_BTS = "from BtsBO where upper(btsId)= ? and upper(bscId) = ? ";
            Query q = getSession().createQuery(SQL_SELECT_BTS);
            q.setParameter(0, StringUtils.trimAll(btsForm.getBtsId()).toUpperCase());
            q.setParameter(1, StringUtils.trimAll(btsForm.getBscId()).toUpperCase());
            List lstResult = q.list();
            if (lstResult != null && lstResult.size() > 0) {
                BtsBO btsBO = (BtsBO) lstResult.get(0);
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
                //   level important and description
                btsForm.setTypeImportant(btsBO.getImportant());
                if (btsBO.getImportant() != null) {
                    List objReturn = AddressDAO.getDescLevel(req, btsBO.getImportant());
                    req.setAttribute("listLevelBts2", objReturn);

                    if (btsBO.getDescLevelId() != null) {
                        btsForm.setDescLevel(btsBO.getDescLevelId().toString());
                    }
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
            String areaCodeCheck = (String) req.getSession().getAttribute("ProvinceCode");
            if (areaCodeCheck != null && ("ttdhkt".equals(areaCodeCheck) || "KV1".equals(areaCodeCheck.toUpperCase()) || "KV2".equals(areaCodeCheck.toUpperCase()) || "KV3".equals(areaCodeCheck.toUpperCase()) || "KV4".equals(areaCodeCheck.toUpperCase()))) {
                if (StringUtils.checkNotHasKv(req)) {
                    req.setAttribute("listProvince", AddressDAO.getListProvince(req));
                }

                pageForward = "updateBtsConfigNotAdmin";
            }


            if (QueryCryptUtils.getParameter(req, "areaCode") != null) {//Tinh thanh

                btsForm.setAreaCode(QueryCryptUtils.getParameter(req, "areaCode").
                        toString());
            }
            //Loai trang
            if (QueryCryptUtils.getParameter(req, "type") != null) {//Trang cap nhat ten cho tram
                //Chuyen den trang cap nhat ten tram

                pageForward = UPDATE_CONFIG_NAME;

                //Thong tin ve tinh thanh                
                // Query q = getSession().createSQLQuery(sqlGetAreaPrefix);
                //thuan da comment de phan quyen
                req.setAttribute("listProvince", AddressDAO.getListProvince(req));

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
     * Cap nhat node bsc
     * Update, add new bsc.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     * @throws Exception the exception
     */
    public ActionResultBO SaveBsc(
            ActionForm form, HttpServletRequest req) throws Exception {

        resourceBundle = resourceBundleUtils.getResource(req);
        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = "BtsSuccess";
        actionResult.setPageForward(pageForward);
        BtsForm btsForm = (BtsForm) form;

        Long important = btsForm.getTypeImportant();

        //neu la 0 thi la null
        if (important.equals(0L)) {
            important = null;
        }
        try {
            if (req.getSession().getAttribute("ProvinceCode") != null) {
                if (!"ttdhkt".equals(req.getSession().getAttribute(
                        "ProvinceCode").toString())) {
                    String provinceCode = req.getSession().getAttribute(
                            "ProvinceCode").toString();
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
            String SQL_SELECT_BTS_SCHEDULE = "from BtsScheduleBO where btsId= ?  ";
            Query qSchedule = getSession().createQuery(SQL_SELECT_BTS_SCHEDULE);
            qSchedule.setParameter(0, StringUtils.trimAll(btsForm.getBtsId()));
//            qSchedule.setParameter(1, StringUtils.trimAll(btsForm.getBscId()));


            String SQL_SELECT_BTS = "from BtsBO where btsId= ? ";
            Query q = getSession().createQuery(SQL_SELECT_BTS);
            q.setParameter(0, btsForm.getBtsId());
            List lstResult = q.list();
            if (lstResult.size() > 0) {
                BtsBO bts = (BtsBO) lstResult.get(0);
                bts.setBscId(StringUtils.trimAll(btsForm.getBscId()));
                bts.setAddress(StringUtils.trimAll(btsForm.getAddress()));
                bts.setAreaCode(StringUtils.trimAll(btsForm.getProvinceCode()));
                bts.setDistance(StringUtils.trimAll(btsForm.getDistance()));
                bts.setNote(StringUtils.trimAll(btsForm.getNote()));
                bts.setStaffId(btsForm.getStaffId());
                bts.setGroupId(btsForm.getGroupId());
                update(bts);
                btsForm.setResult(resourceBundle.getString("btsConfig.saveBts.update.success"));

            } else {
                BtsBO bts = new BtsBO();
                List<BtsBO> btsBOList = AddressDAO.getVenderByBscID(StringUtils.trimAll(btsForm.getBscId()));
                if (btsBOList != null && btsBOList.size() > 0) {
                    bts.setVender(btsBOList.get(0).getVender());
                }
                bts.setBtsId(StringUtils.trimAll(btsForm.getBtsId()));
                bts.setBscId(StringUtils.trimAll(btsForm.getBscId()));
                bts.setLogServerId(StringUtils.trimAll(btsForm.getBscId()));

                bts.setAddress(StringUtils.trimAll(btsForm.getAddress()));
                bts.setAreaCode(StringUtils.trimAll(btsForm.getProvinceCode()));
                bts.setDistance(StringUtils.trimAll(btsForm.getDistance()));

                bts.setNote(StringUtils.trimAll(btsForm.getNote()));
                bts.setStaffId(btsForm.getStaffId());
                bts.setGroupId(btsForm.getGroupId());
                important = btsForm.getTypeImportant();
                String descLevel = btsForm.getDescLevel();

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
                    BtsScheduleBO btsScheduleBO = new BtsScheduleBO();


                    if (btsBOList != null && btsBOList.size() > 0) {
                        btsScheduleBO.setVender(btsBOList.get(0).getVender());
                    }
                    btsScheduleBO.setLogServerId(StringUtils.trimAll(btsForm.getBscId()));

                    btsScheduleBO.setBtsId(StringUtils.trimAll(btsForm.getBtsId()));
                    btsScheduleBO.setBcfId(StringUtils.trimAll(btsForm.getBtsId()));
                    btsScheduleBO.setBscId(StringUtils.trimAll(btsForm.getBscId()));
                    btsScheduleBO.setAddress(StringUtils.trimAll(btsForm.getAddress()));

                    btsScheduleBO.setAreaCode(StringUtils.trimAll(btsForm.getProvinceCode()));
                    btsScheduleBO.setDistance(StringUtils.trimAll(btsForm.getDistance()));
                    btsScheduleBO.setNote(StringUtils.trimAll(btsForm.getNote()));

                    btsScheduleBO.setStaffId(btsForm.getStaffId());
                    btsScheduleBO.setGroupId(btsForm.getGroupId());
                    btsScheduleBO.setImportant(btsForm.getImportant());
                    save(btsScheduleBO);
                }
                String hqlUpdateNodeB = "update BtsScheduleBO set important = ? where btsId = ? ";
                Query sNodeB = getSession().createQuery(hqlUpdateNodeB);
                sNodeB.setParameter(0, important == null ? 0L : important);
                sNodeB.setParameter(1, StringUtils.trimAll(btsForm.getBtsId()));
//                sNodeB.setParameter(2, StringUtils.trimAll(btsForm.getBscId()));
                sNodeB.executeUpdate();

                String hqlUpdate = "update CurrentFaultBO set important = ? where btsid = ? ";
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
            btsForm.setResult(
                    resourceBundle.getString("btsConfig.saveBts.fail"));
            throw ex;

        }

        return actionResult;
    }

    /**
     * Cap nhat tram BTS
     * Update, add new BTS.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     * @throws Exception the exception
     */
    public ActionResultBO SaveBTS(
            ActionForm form, HttpServletRequest req) throws Exception {
        resourceBundle = resourceBundleUtils.getResource(req);
        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = "updateAndClose";
        actionResult.setPageForward(pageForward);
        BtsForm btsForm = (BtsForm) form;

        Long important = btsForm.getTypeImportant();

        //neu la 0 thi la null
        if (important.equals(0L)) {
            important = null;
        }
        try {
            // Query node = getSession().createQuery(sqlGetnodeBSC);
            // List lstnode = node.list();
            //  req.setAttribute("NodeBSCList", getListnodeBSC(lstnode));
            if (req.getSession().getAttribute("ProvinceCode") != null) {
                if (!"ttdhkt".equals(req.getSession().getAttribute(
                        "ProvinceCode").toString())) {
                    String provinceCode = req.getSession().getAttribute(
                            "ProvinceCode").toString();
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
                pageForward = "BtsSuccess";
                actionResult.setPageForward(pageForward);
                return actionResult;
            }
            // check bts and bsc have existed in bts or not
            String SQL_SELECT_BTS_SCHEDULE = "from BtsScheduleBO where upper(btsId)= ? ";
            Query qSchedule = getSession().createQuery(SQL_SELECT_BTS_SCHEDULE);
            qSchedule.setParameter(0, StringUtils.trimAll(btsForm.getBtsId()).toUpperCase());
//            qSchedule.setParameter(1, StringUtils.trimAll(btsForm.getBscId()).toUpperCase());


            String SQL_SELECT_BTS = "from BtsBO where upper(btsId)= ? ";
            Query q = getSession().createQuery(SQL_SELECT_BTS);
            q.setParameter(0, StringUtils.trimAll(btsForm.getBtsId()).toUpperCase());
            List lstResult = q.list();
            if (lstResult.size() > 0) { //trung ma BTS
//                BtsBO bts = (BtsBO) lstResult.get(0);
//
//                bts.setBscId(StringUtils.trimAll(btsForm.getBscId()));
//                bts.setAddress(StringUtils.trimAll(btsForm.getAddress()));
//
//                bts.setAreaCode(StringUtils.trimAll(btsForm.getProvinceCode()));
//                bts.setDistance(StringUtils.trimAll(btsForm.getDistance()));
//
//                bts.setNote(StringUtils.trimAll(btsForm.getNote()));
//                bts.setStaffId(btsForm.getStaffId());
//                bts.setGroupId(btsForm.getGroupId());
//                bts.setImportant(btsForm.getImportant());
//                update(bts);
//                btsForm.setResult(resourceBundle.getString("btsConfig.saveBts.update.success"));

                btsForm.setResult(resourceBundle.getString("btsConfig.checkexist.existed"));
                pageForward = "BtsSuccess";
                actionResult.setPageForward(pageForward);
                return actionResult;

            } else {
                BtsBO bts = new BtsBO();
                List<BtsBO> btsBOList = AddressDAO.getVenderByBscID(StringUtils.trimAll(btsForm.getBscId()));
                if (btsBOList != null && btsBOList.size() > 0) {
                    bts.setVender(btsBOList.get(0).getVender());
                }
                bts.setBtsId(StringUtils.trimAll(btsForm.getBtsId()));
                bts.setBscId(StringUtils.trimAll(btsForm.getBscId()));
                bts.setLogServerId(StringUtils.trimAll(btsForm.getBscId()));

                bts.setAddress(StringUtils.trimAll(btsForm.getAddress()));
                bts.setAreaCode(StringUtils.trimAll(btsForm.getProvinceCode()));
                bts.setDistance(StringUtils.trimAll(btsForm.getDistance()));

                bts.setNote(StringUtils.trimAll(btsForm.getNote()));
                bts.setStaffId(btsForm.getStaffId());
                bts.setGroupId(btsForm.getGroupId());
                important = btsForm.getTypeImportant();
                String descLevel = btsForm.getDescLevel();

                //neu la 0 thi la null
                if (important.equals(0L)) {
                    important = null;
                }
                if (important != null) {
                    bts.setImportant(important);
                }

                bts.setDescLevelId(Long.valueOf(descLevel));

                if (StringUtils.checkNotHasKv(req) && StringUtils.isNotNull(btsForm.getAreaCode())) {
                    bts.setAreaCode(btsForm.getAreaCode());
                }
                save(bts);

                List btsScheduleList = qSchedule.list();
                if (btsScheduleList == null || btsScheduleList.size() <= 0) {
                    BtsScheduleBO btsScheduleBO = new BtsScheduleBO();


                    if (btsBOList != null && btsBOList.size() > 0) {
                        btsScheduleBO.setVender(btsBOList.get(0).getVender());
                    }
                    btsScheduleBO.setLogServerId(StringUtils.trimAll(btsForm.getBscId()));

                    btsScheduleBO.setBtsId(StringUtils.trimAll(btsForm.getBtsId()));
                    btsScheduleBO.setBscId(StringUtils.trimAll(btsForm.getBscId()));
                    btsScheduleBO.setAddress(StringUtils.trimAll(btsForm.getAddress()));

                    btsScheduleBO.setAreaCode(StringUtils.trimAll(btsForm.getProvinceCode()));
                    btsScheduleBO.setDistance(StringUtils.trimAll(btsForm.getDistance()));
                    btsScheduleBO.setBcfId(StringUtils.trimAll(btsForm.getBtsId()));
                    btsScheduleBO.setNote(StringUtils.trimAll(btsForm.getNote()));

                    btsScheduleBO.setStaffId(btsForm.getStaffId());
                    btsScheduleBO.setGroupId(btsForm.getGroupId());
                    btsScheduleBO.setImportant(btsForm.getImportant());
                    save(btsScheduleBO);
                }
                String hqlUpdateNodeB = "update BtsScheduleBO set important = ? where btsId = ? ";
                Query sNodeB = getSession().createQuery(hqlUpdateNodeB);
                sNodeB.setParameter(0, important == null ? 0L : important);
                sNodeB.setParameter(1, StringUtils.trimAll(btsForm.getBtsId()));
//                sNodeB.setParameter(2, StringUtils.trimAll(btsForm.getBscId()));
                sNodeB.executeUpdate();

                String hqlUpdate = "update CurrentFaultBO set important = ? where btsid = ?  ";
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
            btsForm.setResult(
                    resourceBundle.getString("btsConfig.saveBts.fail"));
            throw ex;

        }

        return actionResult;
    }

    /**
     * Danh sách các node BSC m?t k?t n?i.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     * @throws Exception the exception
     */
    public ActionResultBO searchBsc(ActionForm form,
            HttpServletRequest req) throws
            Exception {
        //L?p x? lý action
        ActionResultBO actionResult = new ActionResultBO();

        //Cau lenh truy van du lieu
        StringBuffer sqlWhere = new StringBuffer();
        StringBuffer sqlFrom = new StringBuffer();
        List lstPaAdmin = new ArrayList();

        //Trang forward sau khi x? lý
        String pageForwar = SEARCH_BSC_SERVERS;//mac dinh la trang reload

        //form logserver
        LogServerForm logServerForm = (LogServerForm) form;


        try {
            //Check permission for user
            String strProvinceReq = req.getSession().getAttribute(
                    INFO_USER_LOGIN).toString();

            if (strProvinceReq != null) {//exists user

                //Check reload page
                if (QueryCryptUtils.getParameter(req, "ajax") == null) {//load lan dau

                    pageForwar = VIEW_BSC_SERVERS;
                }
                List tempList = this.getBscList(form, req);
                req.setAttribute("listBsc", tempList);

            } else {//error info user login

                logServerForm.setResult(ERROR_INFO_LOGIN_BTS_CONFIG);
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
     * Danh sách các node BSC m?t k?t n?i.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     * @throws Exception the exception
     */
    public List getBscList(ActionForm form,
            HttpServletRequest req) throws
            Exception {
        //L?p x? lý action
        ActionResultBO actionResult = new ActionResultBO();

        //Cau lenh truy van du lieu
        StringBuffer sqlWhere = new StringBuffer();
        StringBuffer sqlFrom = new StringBuffer();
        List lstPaAdmin = new ArrayList();

        //Trang forward sau khi x? lý

        //form logserver
        LogServerForm logServerForm = (LogServerForm) form;


        try {
            //Check permission for user
            String strProvinceReq = req.getSession().getAttribute(
                    INFO_USER_LOGIN).toString();


            /*Lay du lieu de hien thi*/

            int i = 0;//bien dem

            //List lst = null;//Ket qua tra ve

            //Cac dieu kien loc
            String typeCentral = logServerForm.getTypeCentral();//Tong dai

            String bscID = logServerForm.getBscId();//Node bsc

            //Check reload
            if (QueryCryptUtils.getParameter(req, "typeCentral") != null) {//load lan dau

                typeCentral = QueryCryptUtils.getParameter(req, "typeCentral").toString();
            }
            if (QueryCryptUtils.getParameter(req, "bscId") != null) {//load lan dau

                bscID = QueryCryptUtils.getParameter(req, "bscId").toString();
            }

            //Chuan hoa dieu kien loc
            if (typeCentral != null) {
                if (typeCentral.equals("0")) {
                    typeCentral = null;
                }
            }
            //Lay cac dieu kien loc
            boolean blnFirst = true;
            sqlWhere.append(" where 1=1 ");

            //Lay cac dieu kien loc
            if (typeCentral != null && !typeCentral.equals("")) {
                sqlWhere.append(" and a.exchange=? ");
                lstPaAdmin.add(typeCentral);
            }
            if (bscID != null && !bscID.equals("")) {
                if (blnFirst) {
                    sqlWhere.append(" and upper(a.bscId) like ? ");
                    lstPaAdmin.add(bscID.toUpperCase().trim() + "%");
                } else {
                    sqlWhere.append(" and upper(a.bscId) like ? ");
                    lstPaAdmin.add(bscID.toUpperCase().trim() + "%");
                }

            }

            if (!StringUtils.checkNotKV(strProvinceReq) && !"ttdhkt".equals(strProvinceReq)) {

                sqlFrom.append(" select distinct a from LogServersBO a , ExchAreaBO b ");//Cau lenh truy van
                sqlWhere.append(" and a.bscId = b.exchangeId ");
                sqlWhere.append(" and  b.areaCode like ?");
                sqlWhere.append(" and  ( a.serverTypeId = ? or a.serverTypeId = ? )");
                lstPaAdmin.add(strProvinceReq + "%");
                lstPaAdmin.add(Constant.COMMON.LOG_SERVER_TYPE.BSC);
                lstPaAdmin.add(Constant.COMMON.LOG_SERVER_TYPE.OMC);

            } else {
                sqlFrom.append(" from LogServersBO a");//Cau lenh truy van
                sqlWhere.append(" and  ( a.serverTypeId = ? or a.serverTypeId = ? )");
                // lstPaAdmin.add(strProvinceReq + "%");
                lstPaAdmin.add(Constant.COMMON.LOG_SERVER_TYPE.BSC);
                lstPaAdmin.add(Constant.COMMON.LOG_SERVER_TYPE.OMC);
            }


            //order by
            sqlWhere.append(" order by a.bscId");

            //Khoi tao cau lenh sql for excution
            Query q = getSession().createQuery(sqlFrom.append(sqlWhere.toString()).toString());



            for (int j = 0; j < lstPaAdmin.size(); j++) {

                q.setParameter(j, lstPaAdmin.get(j));
            }
            //set attribute
            List<LogServersBO> logServersBOList = q.list();

            return logServersBOList;


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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

                actionResult.setPageForward("notAuthorzied");
                return actionResult;
            }
        }

        //Trang forward sau khi x? lý
        String pageForwar = SEARCH_BSC_FAIL;//mac dinh la trang reload

        //form bts
        BtsForm btsForm = (BtsForm) form;

        try {
            //Check permission for user
            String strProvinceReq = req.getSession().getAttribute(
                    INFO_USER_LOGIN).toString();

            if (strProvinceReq != null) {//exists user
                //Check reload page

                String ajaxReq = QueryCryptUtils.getParameter(req, "ajax");
                if (ajaxReq == null) {//load lan dau

                    pageForwar = VIEW_BSC_FAIL;
                }
                if (QueryCryptUtils.getParameter(req, "typePage") != null) {
                    pageForwar = PAGE_BSC_POPUP_VIEW;

                    if (ajaxReq != null) {//over see bsc

                        pageForwar = PAGE_BSC_OVERSEE_POPUP;
                    }
                }
                /*Lay du lieu de hien thi*/
                Query q = getSession().createQuery(" from BscFailBO ");
                req.setAttribute("listBscFail", q.list());
            } else {//error info user login

                btsForm.setResult(ERROR_INFO_LOGIN_BTS_CONFIG);
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
     * Danh sách các tr?m BTS c?n qu?n lý rięng.
     * Các tr?m nŕy c?n ph?i ??i tęn tr?m theo tęn t?nh c?a nó.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     * @throws Exception the exception
     */
    public ActionResultBO searchBtsConfig(ActionForm form,
            HttpServletRequest req) throws
            Exception {
 
