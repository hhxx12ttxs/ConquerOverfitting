/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lifetek.netmosys.database.DAO;

import com.lifetek.netmosys.client.form.SearchBtsForm;


import com.lifetek.netmosys.database.BO.Conditional;
import com.lifetek.netmosys.database.BO.VAssignedFaultBO;
import com.lifetek.netmosys.database.BO.vCurrentFaultBO;
import com.lifetek.netmosys.server.action.SMSTimerTask;
import com.lifetek.netmosys.server.paginated.CurrentPaginatedList;
import com.lifetek.netmosys.server.paginated.PaginatedDAO;
import com.lifetek.netmosys.util.Constant;
import com.lifetek.netmosys.util.DateTimeUtils;
import com.lifetek.netmosys.util.QueryCryptUtils;

import com.lifetek.netmosys.util.StringUtils;
import com.lifetek.database.config.BaseHibernateDAO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.hibernate.Query;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * The Class SearchNDAO.
 * 
 * @author ThanhNV1@lifetek.com.vn
 */
public class SearchNDAO extends BaseHibernateDAO {

    /** The Constant HQL_FAULT_CURRENT. */
    private final String HQL_FAULT_CURRENT = " select new CurrentFaultBO(btsid, bscid, faultId, starttime, bsctime, lastUpdate, staffname, faultname, deviceId, endtime, smsStatus, faultLogId, taskId, note, assignDate, reason, resolve, areaCode, provinceCode, provinceName, fullName, faultGroupId, groupId, staffId, dieselId, dieselName, kvCode, kvName, important, exchange) "
            + "from CurrentFaultBO a";
    /** The Constant HQL_FAULT_ASSIGN_POPUP. */
    private final String HQL_FAULT_ASSIGN_POPUP = " select new VAssignedFaultBO(btsid, bscid, faultId, starttime, bsctime, lastUpdate, staffname, faultname, deviceId, endtime, smsStatus, faultLogId, taskId, note, assignDate, reason, resolve, areaCode, provinceCode, provinceName, fullName, faultGroupId, groupId, staffId, dieselId, dieselName, kvCode, kvName, important, exchange) "
            + "from VAssignedFaultBO a ";
    /** The Constant HQL_FAULT_ASSIGN. */
    private final String HQL_FAULT_ASSIGN = " select new ViewAssignedFaultBO(btsid, bscid, faultId, starttime, bsctime, lastUpdate, staffname, faultname, deviceId, endtime, smsStatus, faultLogId, taskId, note, assignDate, reason, resolve, areaCode, provinceCode, provinceName, fullName, faultGroupId, groupId, staffId, dieselId, dieselName, kvCode, kvName, important, exchange) "
            + "from ViewAssignedFaultBO a ";
    /** The Constant HQL_FAULT_ASSIGN_COUNT_VIEW. */
    private final String HQL_FAULT_ASSIGN_COUNT_VIEW = " select count(*), faultGroupId " + "from VAssignedFaultBO a ";
    /** The Constant HQL_FAULT_HISTORY. */
    private final String HQL_FAULT_HISTORY = " from ViewFaultAllBO ";
    // HQL for total error
    /** The Constant HQL_TOTAL_ERROR. */
    private final String HQL_TOTAL_ERROR = " from ViewFaultAllBO ";
    /** The Constant HQL_FAULT_ALL. */
    private final String HQL_FAULT_ALL = " select new ViewFaultAllBO(btsid, bscid, faultid, starttime, bsctime, lastUpdate, staffname, faultname, deviceId, endtime, smsStatus, faultLogId, taskId, note, assignDate, reason, resolve, areaCode, provinceCode, provinceName, fullName, faultGroupId, groupId, staffId, dieselId, dieselName, kvCode, kvName, important, exchange,groupName) "
            + "from ViewFaultAllBO ";

    /**
     * Gets the current list to present.
     * 
     * @param httpSession the http session
     * @param req the req
     * @param lossType the loss type
     * @param typeExport the type export
     * @param dk the dk
     * 
     * @return the current list to present
     * 
     * @throws HibernateException the hibernate exception
     */
    public List getCurrentListToPresent(HttpSession httpSession, HttpServletRequest req, String lossType, int typeExport, Conditional dk) throws HibernateException {
        //Check to get error of Loss flow and other
        //set First param

        String areaCode = (String) httpSession.getAttribute(Constant.COMMON.PROVINCE_CODE);
        List allList = getListCurrent(SMSTimerTask.getCurrentList(), areaCode, dk);
        List lossflow = (List) allList.get(0);
        List lossPower = (List) allList.get(1);
        List lossOther = (List) allList.get(2);
        req.setAttribute("countBtsLossFlow", lossflow.size());
        req.setAttribute("countBtsOther", lossOther.size());
        req.setAttribute("countBtsFail", lossPower.size());
        if (lossType.equals("lossAdmin")) {
            //for admin area
            List lossAdmin = (List) allList.get(3);
            if (typeExport == Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.EXPORT_FAIL_ALL || typeExport == Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.EXPORT_LOST_POWER) {
                return lossAdmin;
            }
            CurrentPaginatedList AdminPaginatedList = AddressDAO.getCurrentPage(lossAdmin, req);
            req.setAttribute("listBtsFailAdmin", AdminPaginatedList);
        } else {
            if (typeExport == Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.EXPORT_LOST_OTHER) {
                return lossOther;
            }

            if (typeExport == Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.EXPORT_LOST_FLOW) {
                return lossflow;
            }
            if (typeExport == Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.EXPORT_LOST_POWER) {
                return lossPower;
            }
            //for 3 kind of error: loss flow, loss power, loss other
            CurrentPaginatedList paginatedList = AddressDAO.getCurrentPage(lossflow, req);
            req.setAttribute("listBtsLossFlow", paginatedList);
            CurrentPaginatedList paginatedListOther = AddressDAO.getCurrentPage(lossOther, req);
            req.setAttribute("listBtsOther", paginatedListOther);
            CurrentPaginatedList paginatedListAdmin = AddressDAO.getCurrentPage(lossPower, req);
            req.setAttribute("listBtsFail", paginatedListAdmin);


        }

        return (List) allList.get(3);
    }

    /**
     * Gets the current list to popup.
     * 
     * @param httpSession the http session
     * @param req the req
     * @param lossType the loss type
     * @param typeExport the type export
     * @param dk the dk
     * @param searchBtsForm the search bts form
     * 
     * @return the current list to popup
     * 
     * @throws HibernateException the hibernate exception
     * 
     * @author Nguyen Hung Cuong
     */
    public List getCurrentListToPopup(HttpSession httpSession, HttpServletRequest req, String lossType, int typeExport, Conditional dk, SearchBtsForm searchBtsForm) throws HibernateException {

        String notRing = null;
        try {
            //Check to get error of Loss flow and other
            //set First param
            // cuongnh start
            // cuongnh end
            notRing = QueryCryptUtils.getParameter(req, "notRing");
        } catch (Exception ex) {
            Logger.getLogger(SearchNDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        String areaCode = (String) httpSession.getAttribute(Constant.COMMON.PROVINCE_CODE);
        List currentFaultList = SMSTimerTask.getCurrentList();

        Map<String, Date> faultMap = (Map<String, Date>) req.getSession().getAttribute("lastBtsWarning");
        vCurrentFaultBO tempBO = new vCurrentFaultBO();

        if (faultMap != null && searchBtsForm.getIsRing() != null && !StringUtils.isNotNull(notRing)) {
            if (faultMap.containsKey(areaCode)) {
                tempBO.setStarttime((Date) faultMap.get(areaCode));
            } else {
                tempBO.setStarttime((Date) faultMap.values().toArray()[0]);
            }
        }

        if (currentFaultList != null && currentFaultList.size() > 1) {
            req.getSession().setAttribute("lastBtsWarning", (Map<String, Date>) SMSTimerTask.getStartTime());
        }


        List allList = getListCurrentPopup(currentFaultList, areaCode, dk, tempBO);
        List lossflow = (List) allList.get(0);
        List lossPower = (List) allList.get(1);
        List lossOther = (List) allList.get(2);
        req.setAttribute("countBtsLossFlow", lossflow.size());
        req.setAttribute("countBtsOther", lossOther.size());
        req.setAttribute("countBtsFail", lossPower.size());

        CurrentPaginatedList paginatedList = AddressDAO.getCurrentPagePopup((List) allList.get(4), req);
        req.setAttribute("listBtsFailAdmin", paginatedList);
        return null;
    }

    /**
     * Get bts error.
     * 
     * @param req the req
     * @param searchBtsForm the search bts form
     * @param typeExport 0:not Export,1,2,3:export
     * 
     * @return the search bts fail
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     */
    public List getSearchBtsFail(HttpServletRequest req, SearchBtsForm searchBtsForm, int typeExport) throws Exception {
        try {
            Conditional dk = new Conditional();
            //result
            List lstReturn = null;
            boolean isCache = false;
            HttpSession httpSession = req.getSession();
            //Type of page
            String typePage = QueryCryptUtils.getParameter(req, "isAdmin");
            String autoReload = QueryCryptUtils.getParameter(req, "autoReload");

            String lossType = QueryCryptUtils.getParameter(req, "lossType");

            String inpage = QueryCryptUtils.getParameter(req, "inpage");

            if (lossType == null) {
                lossType = "";
            }

            int statusError = searchBtsForm.getStatusError();//Trang thai
            /*
             * NGUYEN HUNG CUONG
             * DESCRIPTION : THIS CLAUSE TO SEARCH ERROR ACCORDING CLOSED OR TOTAL
             */
            if (Constant.BTS_ERROR_MONITORING.LOSS_ADMIN.equals(lossType) && (Constant.BTS_ERROR_MONITORING.CLOSED_ERROR == searchBtsForm.getTypeError() || Constant.BTS_ERROR_MONITORING.TOTAL_ERROR == searchBtsForm.getTypeError())) {
                getFaultHistoryForAdmin(req, searchBtsForm);
                // Get list provinces
                if (lossType.equals("") && inpage == null) {
                    req.setAttribute("listProvince", AddressDAO.getListProvince(req));
                    AddressDAO.getProvinceArea(req);
                }
                return new ArrayList();
            }

//            if (doRefresh == null) { 
//                SMSTimerTask.runRefresh();
//            }

            if (autoReload != null && SMSTimerTask.getCurrentList() != null && SMSTimerTask.getCurrentList().size() > 0) {

                dk = new Conditional();



                if ((httpSession.getAttribute(Constant.ASSSIGN_TASK.ROLE)) != null) {
                    String role = httpSession.getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();

                    //Truong hop chuc danh user la nhom truong: Loc theo group_id
                    if (role.equals(Constant.ASSSIGN_TASK.LEADER)) {
                        Long groupIdLogin = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.GROUP_ID);
                        dk.setGroupId(groupIdLogin.toString());
                    }

                    //Truong hop chuc danh user la nhan vien: Loc theo staff_id
                    if (role.equals(Constant.ASSSIGN_TASK.STAFF)) {
                        Long staffId = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.STAFF_ID);
                        dk.setStaffId(staffId.toString());
                    }
                }
                dk.setFaultId(Long.valueOf(searchBtsForm.getTypeError()));

                //Dieu kien loc theo nhom loi
                if (searchBtsForm.getArrId() != null) {
                    Map<Long, Long> mapFaultId = new HashMap<Long, Long>();
                    for (Long id : searchBtsForm.getArrId()) {
                        mapFaultId.put(id, 1L);
                    }
                    dk.setLstFaultId(mapFaultId);
                }


                // bts code
                if (statusError == Constant.MOBILE_2G.MOBILE_FAULT_STATUS.CURRENT) {
                    if (StringUtils.isNotNull(searchBtsForm.getPersistentLevel())) {//loc theo tong dai


                        if (SMSTimerTask.getSysdate() != null) {
                            Date date = SMSTimerTask.getSysdate();
                            Calendar dateCalendar = Calendar.getInstance();
                            dateCalendar.setTime(date);
                            dateCalendar.add(Calendar.SECOND, -1 * Integer.parseInt(searchBtsForm.getPersistentLevel()));

                            dk.setPersistentDate(dateCalendar.getTime());

                        }
                    }
                }


                getCurrentListToPresent(httpSession, req, lossType, typeExport, dk);

            } else {
                //Sql query
                //Fail for Loss Flow and other error
                StringBuffer sqlCurrent = new StringBuffer();
                StringBuffer sqlWhere = new StringBuffer();
                //Loss Power error
                //All error in table
                StringBuffer sqlWhereAdminArea = new StringBuffer();
                //Conditionals variable from Form
                Date fromDate = DateTimeUtils.convertStringToDateTime(searchBtsForm.getFromDate());//starTime

                if ((StringUtils.isNotNull(searchBtsForm.getFromDate())) && (fromDate == null)) { //Add more time info

                    searchBtsForm.setFromDate(searchBtsForm.getFromDate() + " 00:00:00");
                    fromDate = DateTimeUtils.convertStringToDateTime(searchBtsForm.getFromDate());
                }

                Date toDate = DateTimeUtils.convertStringToDateTime(searchBtsForm.getToDate());//endTime

                if ((StringUtils.isNotNull(searchBtsForm.getToDate())) && (toDate == null)) { //Add more time info

                    searchBtsForm.setToDate(searchBtsForm.getToDate() + " 23:59:59");
                    toDate = DateTimeUtils.convertStringToDateTime(searchBtsForm.getToDate());
                }

                String kvCode = searchBtsForm.getKvCode();//KV code

                if (kvCode != null) {//Get KV code

                    if (kvCode.equals("0")) {
                        kvCode = "";
                    }
                }

                String groupId = searchBtsForm.getGroupId();

                String areaCode = searchBtsForm.getAreaCode();
                if (areaCode == null || areaCode.equals("") || areaCode.equals("0")) {
                    areaCode = (String) httpSession.getAttribute(Constant.COMMON.PROVINCE_CODE);
                }
                if (areaCode.equals(Constant.COMMON.ADMIN_TTDHKT)) {
                    areaCode = "";
                }

                int typeError = searchBtsForm.getTypeError();//loai loi



                String typeCentral = searchBtsForm.getTypeCentral();//Loai tong dai
                //count Parammeter

                List lstPaLossFlow = new ArrayList();
                List lstPaLossPower = new ArrayList();
                List lstPaAdmin = new ArrayList();
                //Get condional query
                lstPaLossPower.add(Constant.FAULT_TYPE.POWER_LOSS);
                sqlWhereAdminArea.append(" where 1=1 ");
                //mat luong + loi tram
                //1.4s
                if (statusError == Constant.MOBILE_2G.MOBILE_FAULT_STATUS.CURRENT || autoReload != null) {
                    sqlCurrent.append(HQL_FAULT_CURRENT);
                    dk.setIsCurrent(true);
                    isCache = true;
                    sqlWhere.append(" where faultGroupId = ? ");
                    lstPaLossFlow.add(Constant.FAULT_TYPE.FLOW_LOSS);
                } else if (statusError == Constant.MOBILE_2G.MOBILE_FAULT_STATUS.CLEARED) {
                    dk.setIsCurrent(false);
                    //Loss flow and other error
                    sqlCurrent.append(HQL_FAULT_ASSIGN);
                    sqlWhere.append(" where faultGroupId = ? ");
                    lstPaLossFlow.add(Constant.FAULT_TYPE.FLOW_LOSS);

                    req.setAttribute("type", "cleared");


                } else {
                    dk.setIsCurrent(false);
                    //Loss flow and other error
                    sqlCurrent.append(HQL_FAULT_ALL);
                    sqlWhere.append(" where faultGroupId = ? ");
                    lstPaLossFlow.add(Constant.FAULT_TYPE.FLOW_LOSS);
                }
                //Kiem tra cac dieu kien loc de them vao sql
            /*chuan hoa du lieu*/
                if (areaCode != null) {
                    if (areaCode.equals("0")) {
                        areaCode = "";
                    }
                } else {
                    areaCode = "";
                }
                if (typeCentral != null) {
                    if (typeCentral.equals("0")) {
                        typeCentral = "";
                    }
                }

                if (fromDate != null) {
                    sqlWhere.append(" and bsctime >= ? ");
                    lstPaLossFlow.add(fromDate);
                    lstPaLossPower.add(fromDate);
                    sqlWhereAdminArea.append(" and bsctime >= ? ");
                    lstPaAdmin.add(fromDate);
                    dk.setStartDate(fromDate);
                }
                if (toDate != null) {
                    sqlWhere.append(" and bsctime <= ? ");
                    lstPaLossFlow.add(toDate);
                    lstPaLossPower.add(toDate);
                    sqlWhereAdminArea.append(" and  bsctime <= ? ");
                    lstPaAdmin.add(toDate);
                    dk.setEndDate(toDate);
                }

                /*Kiem tra dieu kien loc*/
                //Ma tinh thanh

                //Loc truong hop leader hoac staff             
                if ((httpSession.getAttribute(Constant.ASSSIGN_TASK.ROLE)) != null) {
                    String role = httpSession.getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();

                    //Truong hop chuc danh user la nhom truong: Loc theo group_id
                    if (role.equals(Constant.ASSSIGN_TASK.LEADER)) {
                        Long groupIdLogin = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.GROUP_ID);
                        dk.setGroupId(groupIdLogin.toString());
                        sqlWhere.append(" and groupId = ? ");
                        lstPaLossFlow.add(groupIdLogin);
                        lstPaLossPower.add(groupIdLogin);
                        sqlWhereAdminArea.append(" and groupId = ? ");
                        lstPaAdmin.add(groupIdLogin);
                    }

                    //Truong hop chuc danh user la nhan vien: Loc theo staff_id
                    if (role.equals(Constant.ASSSIGN_TASK.STAFF)) {
                        Long staffId = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.STAFF_ID);

                        dk.setStaffId(staffId.toString());
                        sqlWhere.append(" and staffId = ? ");
                        lstPaLossFlow.add(staffId);
                        lstPaLossPower.add(staffId);
                        sqlWhereAdminArea.append(" and staffId = ? ");
                        lstPaAdmin.add(staffId);
                    }
                }
                if (groupId != null && !groupId.equals("") && !groupId.equals("0")) {
                    dk.setGroupId(groupId.toString());
                    sqlWhere.append(" and groupId = ? ");
                    lstPaLossFlow.add(Long.valueOf(groupId));
                    lstPaLossPower.add(Long.valueOf(groupId));
                    sqlWhereAdminArea.append(" and groupId = ? ");
                    lstPaAdmin.add(Long.valueOf(groupId));
                }
                if (StringUtils.checkNotKV(areaCode)) {
                    dk.setAreaCode(areaCode);
                    sqlWhere.append(" and areaCode = ? ");
                    lstPaLossFlow.add(areaCode);
                    lstPaLossPower.add(areaCode);
                    sqlWhereAdminArea.append(" and areaCode = ? ");
                    lstPaAdmin.add(areaCode);
                }

                if (StringUtils.isNotNullSelect(searchBtsForm.getImportant())) {
                    dk.setImportant(Long.valueOf(searchBtsForm.getImportant()));
                    sqlWhere.append(" and important = ? ");
                    lstPaLossFlow.add(Long.valueOf(searchBtsForm.getImportant()));
                    lstPaLossPower.add(Long.valueOf(searchBtsForm.getImportant()));
                    sqlWhereAdminArea.append(" and important = ? ");
                    lstPaAdmin.add(Long.valueOf(searchBtsForm.getImportant()));
                }

                if (StringUtils.isNotNullSelect(searchBtsForm.getDescLevelId())) {
                    dk.setDescLevelId(Long.valueOf(searchBtsForm.getDescLevelId()));
                    sqlWhere.append(" and descLevelId = ? ");
                    lstPaLossFlow.add(Long.valueOf(searchBtsForm.getDescLevelId()));
                    lstPaLossPower.add(Long.valueOf(searchBtsForm.getDescLevelId()));
                    sqlWhereAdminArea.append(" and descLevelId = ? ");
                    lstPaAdmin.add(Long.valueOf(searchBtsForm.getDescLevelId()));
                }




                if (StringUtils.isNotNull(searchBtsForm.getReasonId())) {
                    sqlWhere.append(" and reasonId = ? ");
                    lstPaLossFlow.add(Long.valueOf(searchBtsForm.getReasonId()));
                    lstPaLossPower.add(Long.valueOf(searchBtsForm.getReasonId()));
                    sqlWhereAdminArea.append(" and  reasonId = ? ");
                    lstPaAdmin.add(Long.valueOf(searchBtsForm.getReasonId()));
                    dk.setReasonId(Long.valueOf(searchBtsForm.getReasonId()));
                }
//                if (kvCode != null && !kvCode.equals("") && !areaCode.equals(""))//Loc theo khu vuc
                if (kvCode != null && !kvCode.equals("") && !kvCode.equals("ttdhkt"))//Loc theo khu vuc
                {
                    sqlWhere.append(" and kvCode =?");
                    lstPaLossFlow.add(kvCode);
                    lstPaLossPower.add(kvCode);
                    sqlWhereAdminArea.append(" and kvCode =?");
                    lstPaAdmin.add(kvCode);
                    dk.setKvCode(kvCode);
                }
                //Tong dai
                if (typeCentral != null && !typeCentral.equals("") && !typeCentral.equals("0")) {//loc theo tong dai

                    sqlWhere.append(" and exchange=?");
                    lstPaLossFlow.add(typeCentral);
                    lstPaLossPower.add(typeCentral);
                    sqlWhereAdminArea.append(" and exchange=?");
                    lstPaAdmin.add(typeCentral);
                    dk.setExchange(typeCentral);
                }

                // Bts code and BSC code
                //Tong dai
                if (StringUtils.isNotNull(searchBtsForm.getBscid())) {//loc theo tong dai

                    sqlWhere.append(" and bscid=?");
                    lstPaLossFlow.add(searchBtsForm.getBscid().trim());
                    lstPaLossPower.add(searchBtsForm.getBscid().trim());
                    sqlWhereAdminArea.append(" and bscid=?");
                    lstPaAdmin.add(searchBtsForm.getBscid().trim());
                    dk.setBscid(searchBtsForm.getBscid().trim());
                }
                // bts code
                if (StringUtils.isNotNull(searchBtsForm.getBtsid())) {//loc theo tong dai

                    sqlWhere.append(" and btsid=?");
                    lstPaLossFlow.add(searchBtsForm.getBtsid().trim());
                    lstPaLossPower.add(searchBtsForm.getBtsid().trim());
                    sqlWhereAdminArea.append(" and btsid=?");
                    lstPaAdmin.add(searchBtsForm.getBtsid().trim());
                    dk.setBtsid(searchBtsForm.getBtsid().trim());
                }

                // bts code
                if (statusError == Constant.MOBILE_2G.MOBILE_FAULT_STATUS.CURRENT) {
                    if (StringUtils.isNotNull(searchBtsForm.getPersistentLevel())) {//loc theo tong dai


                        if (SMSTimerTask.getSysdate() != null) {
                            Date date = SMSTimerTask.getSysdate();
                            Calendar dateCalendar = Calendar.getInstance();
                            dateCalendar.setTime(date);
                            dateCalendar.add(Calendar.SECOND, -1 * Integer.parseInt(searchBtsForm.getPersistentLevel()));
                            dk.setPersistentDate(dateCalendar.getTime());
                        }


                        sqlWhere.append(" and bsctime >= sysdate - ? * (1/(24*60))");
                        lstPaLossFlow.add(Long.valueOf(searchBtsForm.getPersistentLevel().trim()));
                        lstPaLossPower.add(Long.valueOf(searchBtsForm.getPersistentLevel().trim()));
                        sqlWhereAdminArea.append(" and bsctime >= sysdate - ? * (1/(24*60))");
                        lstPaAdmin.add(Long.valueOf(searchBtsForm.getPersistentLevel().trim()));
                    }
                }

                //Check nhom loi
                if (searchBtsForm.getArrId() != null) {//loc theo tong dai

                    sqlWhere.append(" and fault_Id in (");
                    for (Long id : searchBtsForm.getArrId()) {
                        sqlWhere.append("?,");
                        lstPaLossFlow.add(id);
                        lstPaLossPower.add(id);
                    }
                    sqlWhere.append("-100)");

                    sqlWhereAdminArea.append(" and fault_Id in (");
                    for (Long id : searchBtsForm.getArrId()) {
                        sqlWhereAdminArea.append("?,");
                        lstPaAdmin.add(id);
                    }
                    sqlWhereAdminArea.append("-100)");

                    Long[] arrId = searchBtsForm.getArrId();
                    if (arrId != null) {
                        Map<Long, Long> mapFaultId = new HashMap<Long, Long>();
                        for (Long id : arrId) {
                            mapFaultId.put(id, 1L);
                        }
                        dk.setLstFaultId(mapFaultId);
                    }
                }


                //Check type error
                //Store run diesel
                Boolean blnRunDiesel = false;
                //Status of Error
                if (statusError == Constant.MOBILE_2G.MOBILE_FAULT_STATUS.CURRENT) {//error now

                    sqlWhere.append(" and endtime is null  ");
                    sqlWhereAdminArea.append(" and endtime is null   ");
                } else if (statusError == Constant.MOBILE_2G.MOBILE_FAULT_STATUS.CLOSED) {//error was complete

                    sqlWhere.append(" and endtime is not null  ");
                    sqlWhereAdminArea.append(" and endtime is not null   ");
                } else {
                    req.setAttribute("type", "cleared");
                }
                //Type of Error
                if (typeError == Constant.ERRO_NONE_ASSIGN) { // chua giao viec

                    dk.setIsTask(false);
                    sqlWhere.append(" and staffname is null  order by bsctime desc ");
                    sqlWhereAdminArea.append(" and staffname is null  ");
                } else if (typeError == Constant.ERRO_HAVE_ASSIGN) { // da giao viec
                    // task assign

                    dk.setIsTask(true);
                    sqlWhere.append(" and staffname is not null order by bsctime desc  ");
                    sqlWhereAdminArea.append(" and  staffname is not null  ");
                } else if (typeError == Constant.ERRO_NOT_COMPLETE) { // Loi chua xu ly xong

                    sqlWhere.append(" order by bsctime desc  ");
                } else if (typeError == Constant.RUN_DIESEL) { //Loi chay may no

                    sqlWhere.append(" and fault_Id = " + Constant.MOBILE_FAULT_TYPE.MAY_NO + " order by bsctime desc  "); //faultId =118 chay may no

                    sqlWhereAdminArea.append(" and fault_Id = " + Constant.MOBILE_FAULT_TYPE.MAY_NO + " ");

                    blnRunDiesel = true;
                    dk.setFaultId(Long.valueOf(Constant.RUN_DIESEL));
                } else {
                    sqlWhere.append(" order by bsctime desc  ");
                }

                if (dk.getIsCurrent()) {
                    lstReturn = getCurrentListToPresent(httpSession, req, lossType, typeExport, dk);
                } else {
                    //Concat String for loss flow and other error
                    StringBuffer sqlAdmin = new StringBuffer();
                    sqlAdmin.append(sqlCurrent.toString());
                    sqlAdmin.append(sqlWhereAdminArea);
                    sqlCurrent.append(sqlWhere);

                    //Loss Power

                    String SQL_COUNT_BTS = null;//Count number bts fail

                    String SQL_COUNT_BTS2 = null;//Count number bts fail

                    if (statusError == Constant.MOBILE_2G.MOBILE_FAULT_STATUS.CURRENT || (typePage != null && typePage.equals(Constant.COMMON.ADMIN_AREA))) {//Current Error

                        SQL_COUNT_BTS = "select count(*) from vCurrentFaultBO " + sqlWhere.toString();
                        SQL_COUNT_BTS2 = "select count(*) from vCurrentFaultBO " + sqlWhereAdminArea.toString();
                    } else if (statusError == Constant.MOBILE_2G.MOBILE_FAULT_STATUS.CLEARED) {
                        SQL_COUNT_BTS = "select count(*) from ViewAssignedFaultBO " + sqlWhere.toString();
                        SQL_COUNT_BTS2 = "select count(*) from vCurrentFaultBO " + sqlWhereAdminArea.toString();
                    } else {
                        SQL_COUNT_BTS = "select count(*) from ViewFaultAllBO " + sqlWhere.toString();
                        SQL_COUNT_BTS2 = "select count(*) from vCurrentFaultBO " + sqlWhereAdminArea.toString();
                    }
                    //Initial Sql
                    Query q = getSession().createQuery(sqlCurrent.toString());
                    Query qCount = getSession().createQuery(SQL_COUNT_BTS);

//                if (isCache) {
//                    q.setCacheable(isCache);
//                    qCount.setCacheable(isCache);
//                }

                    //Loss Power
                    if (lossType.equals("") || lossType.equals(SearchBtsDAO.LOSS_POWER)) {
                        if (typeExport == 0 || typeExport == Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.EXPORT_LOST_POWER) {
                            //Admin page
                            if (typePage != null) {
                                if (typePage.equals(Constant.COMMON.ADMIN_AREA)) {
                                    //Count fail bts
                                    Query q2 = getSession().createQuery(sqlAdmin.toString());
                                    Query qCount2 = getSession().createQuery(SQL_COUNT_BTS2);
//                                if (isCache) {
//                                    q2.setCacheable(isCache);
//                                    qCount2.setCacheable(isCache);
//                                }
                                    lstReturn = getBtsLossPowerAndAdminType(req, q2, qCount2, lstPaAdmin, 1, typeExport);
                                }
                                if (typeExport == Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.EXPORT_FAIL_ALL) {
                                    return lstReturn;
                                }
                            } else {
                                lstReturn = getBtsLossPowerAndAdminType(req, q, qCount, lstPaLossPower, 0, typeExport);
                            }
                        } else {
                        }
                        if (typeExport == Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.EXPORT_LOST_POWER) {
                            return lstReturn;
                        }
                    }

                    //Loss Flow
                    //Set param
                    for (int i = 1; i < lstPaLossFlow.size(); i++) {
                        q.setParameter(i, lstPaLossFlow.get(i));
                        qCount.setParameter(i, lstPaLossFlow.get(i));
                    }




                    //Count result
                    if (!blnRunDiesel)//Only view error Loss Power
                    {
                        int countLoss = 0;
                        int countOther = 0;
                        //Check to get error of Loss flow and other
                        if (typePage == null || typePage.equals(Constant.COMMON.NOT_ADMIN)) {
                            //Loss Flow get dat
                            if (typeExport == Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.EXPORT_LOST_OTHER) {
                                q.setParameter(0, Constant.FAULT_TYPE.BTS_FAULT);
                                List lst = q.list();
                                return lst;
                            }
                            if (typeExport == Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.EXPORT_LOST_FLOW) {
                                q.setParameter(0, Constant.FAULT_TYPE.FLOW_LOSS);
                                List lst = q.list();
                                return lst;
                            } else {
                                if (lossType.equals("") || lossType.equals(SearchBtsDAO.LOSS_FLOW)) {
                                    qCount.setParameter(0, Constant.FAULT_TYPE.FLOW_LOSS);
                                    List lCountBtsLossFlow = qCount.list();//LossFlow

                                    if (lCountBtsLossFlow.size() > 0) {
                                        req.setAttribute("countBtsLossFlow", lCountBtsLossFlow.get(0).toString());
                                        countLoss = Integer.parseInt(lCountBtsLossFlow.get(0).toString());
                                    }
                                    //khong export thi hien thi
                                    q.setParameter(0, Constant.FAULT_TYPE.FLOW_LOSS);
                                    CurrentPaginatedList paginatedList = AddressDAO.getCurrentPage(req, countLoss, q);
                                    req.setAttribute("listBtsLossFlow", paginatedList);
                                }
                            }
                            //Other error
                            if (typeExport == Constant.MOBILE_2G.EXPORT_MOBILE_FAULT.EXPORT_LOST_OTHER) {
                                q.setParameter(0, Constant.FAULT_TYPE.BTS_FAULT);
                                List lst = q.list();
                                return lst;
                            } else {
                                if (lossType.equals("") || lossType.equals(SearchBtsDAO.LOSS_OTHER)) {
                                    qCount.setParameter(0, Constant.FAULT_TYPE.BTS_FAULT);
                                    List lCountBtsOther = qCount.list();//Other error

                                    if (lCountBtsOther.size() > 0) {
                                        req.setAttribute("countBtsOther", lCountBtsOther.get(0).toString());
                                        countOther = Integer.parseInt(lCountBtsOther.get(0).toString());
                                    }
                                    //khong export thi hien thi
                                    q.setParameter(0, Constant.FAULT_TYPE.BTS_FAULT);
                                    CurrentPaginatedList paginatedListOther = AddressDAO.getCurrentPage(req, countOther, q);
                                    req.setAttribute("listBtsOther", paginatedListOther);
                                }
                            }
                        }
                    } else {
                        req.setAttribute("countBtsLossFlow", "0");
                        req.setAttribute("countBtsOther", "0");
                    }

                }
            }
            // Get list provinces
            if (lossType.equals("") && inpage == null) {
                req.setAttribute("listProvince", AddressDAO.getListProvince(req));
                AddressDAO.getProvinceArea(req);
            }

            //Get list group
            if ((req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) && (!req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).equals(Constant.COMMON.ADMIN_TTDHKT))) {
                if (req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE) != null && (Constant.ASSSIGN_TASK.LEADER.equals(req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE).toString()) || Constant.ASSSIGN_TASK.STAFF.equals(req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE).toString()))) { //Truong hop la Leader nhom

                    Long groupId = Long.parseLong(req.getSession().getAttribute(Constant.ASSSIGN_TASK.GROUP_ID).toString());
                    //Lay groupName
                    Query q = getSession().createQuery("from StaffGroupBO where groupId = ? and isEnable =1 ");
                    q.setParameter(0, groupId);
                    List listGroup = q.list();

                    req.setAttribute("listGroup", listGroup);

                } else {
                    AddressDAO a = new AddressDAO();
                    req.setAttribute("listGroup", a.getGroup(req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString()));
                }
            }
            return lstReturn;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * Get bts error.
     * 
     * @param req the req
     * @param searchBtsForm the search bts form
     * @param typeExport 0:not Export,1,2,3:export
     * 
     * @return the assigned fault
     * 
     * @throws java.lang.Exception 
     */
    public List getAssignedFault(HttpServletRequest req, SearchBtsForm searchBtsForm, int typeExport) throws Exception {
        try {
            //result

            Session hibernateSession = getSession();

            List lstReturn = null;

            HttpSession httpSession = req.getSession();
            //Type of page

            String lossType = QueryCryptUtils.getParameter(req, "type");
            if (lossType == null) {
                lossType = "";
            }


            //Sql query
            //Fail for Loss Flow and other error
            StringBuffer sqlCurrent = new StringBuffer();
            StringBuffer sqlWhere = new StringBuffer();
            //Loss Power error
            //All error in table

            //Conditionals variable from Form
            String areaCode = searchBtsForm.getAreaCode();
            if (areaCode == null || areaCode.equals("") || areaCode.equals("0")) {
                areaCode = (String) httpSession.getAttribute(Constant.COMMON.PROVINCE_CODE);
            }

            sqlCurrent.append(HQL_FAULT_ASSIGN_POPUP);

            //count Parammeter
            List lstPaAdmin = new ArrayList();
            //Get condional query
            sqlWhere.append(" where 1=1 ");
            //mat luong + loi tram
            //1.4s

            //Kiem tra cac dieu kien loc de them vao sql
            /*chuan hoa du lieu*/
            if (areaCode != null) {
                if (areaCode.equals("0")) {
                    areaCode = "";
                }
            } else {
                areaCode = "";
            }

            /*Kiem tra dieu kien loc*/
            //Ma tinh thanh
            if (areaCode != null) {
                //check area
                if (areaCode.equals("") || areaCode.equals("0") || areaCode.equals(Constant.COMMON.ADMIN_TTDHKT)) {
                    sqlWhere.append("  ");
                } else if (!StringUtils.checkNotKV(areaCode)) {
                    sqlWhere.append(" and a.kvCode=? ");
                    lstPaAdmin.add(areaCode);

                } else {
                    sqlWhere.append(" and a.areaCode=? ");
                    lstPaAdmin.add(areaCode);
                }
            }

            //Loc truong hop leader hoac staff
            HttpSession session = req.getSession();
            if ((session.getAttribute(Constant.ASSSIGN_TASK.ROLE)) != null) {
                String role = session.getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();

                //Truong hop chuc danh user la nhom truong: Loc theo group_id
                if (role.equals(Constant.ASSSIGN_TASK.LEADER)) {
                    Long groupId = (Long) session.getAttribute(Constant.ASSSIGN_TASK.GROUP_ID);

                    sqlWhere.append(" and a.groupId=?  ");
                    lstPaAdmin.add(groupId);
                }

                //Truong hop chuc danh user la nhan vien: Loc theo staff_id
                if (role.equals(Constant.ASSSIGN_TASK.STAFF)) {
                    Long staffId = (Long) session.getAttribute(Constant.ASSSIGN_TASK.STAFF_ID);

                    sqlWhere.append(" and a.staffId=?  ");
                    lstPaAdmin.add(staffId);
                }
            }


            if (StringUtils.isNotNull(searchBtsForm.getBtsid())) {
                sqlWhere.append(" and upper(a.btsid) like ?  ");
                lstPaAdmin.add(searchBtsForm.getBtsid().trim().toUpperCase() + "%");
            }

            if (StringUtils.isNotNull(searchBtsForm.getBscid())) {
                sqlWhere.append(" and upper(a.bscid) like ?  ");
                lstPaAdmin.add(searchBtsForm.getBscid().trim().toUpperCase() + "%");
            }
            if (StringUtils.isNotNull(searchBtsForm.getFaultGroupId())) {
                sqlWhere.append(" and a.faultGroupId=?  ");
                lstPaAdmin.add(Long.valueOf(searchBtsForm.getFaultGroupId()));
            }



            if (StringUtils.isNotNull(searchBtsForm.getGroupId()) && !"0".equals(searchBtsForm.getGroupId())) {
//                sqlCurrent.append(" , BtsBO b");
                sqlWhere.append(" and a.groupId=?  ");
                lstPaAdmin.add(Long.valueOf(searchBtsForm.getGroupId().trim()));
            }

            //Check nhom loi
            if (searchBtsForm.getArrId() != null) {//loc theo tong dai

                sqlWhere.append(" and a.fault_Id in (");
                for (Long id : searchBtsForm.getArrId()) {
                    sqlWhere.append("?,");
                    lstPaAdmin.add(id);
                }
                sqlWhere.append("-100)");
            }


            StringBuffer sqlAdminFlowLoss = new StringBuffer(HQL_FAULT_ASSIGN_COUNT_VIEW);
            sqlAdminFlowLoss.append(sqlWhere);


            sqlWhere.append("  order by bsctime desc");

            //Concat String for loss flow and other error
            StringBuffer sqlAdmin = new StringBuffer();
            sqlAdmin.append(sqlCurrent);
            sqlCurrent.append(sqlWhere);



            //Loss Power

            //Initial Sql
            Query q = hibernateSession.createQuery(sqlCurrent.toString());

            Query qFlowLossCount = hibernateSession.createQuery(sqlAdminFlowLoss.toString() + " group by faultGroupId ");
//

            for (int i = 0; i < lstPaAdmin.size(); i++) {
//                qStationErrorCount.setParameter(i, lstPaAdmin.get(i));
//                qPowerLossCount.setParameter(i, lstPaAdmin.get(i));
                qFlowLossCount.setParameter(i, lstPaAdmin.get(i));
                q.setParameter(i, lstPaAdmin.get(i));
            }

            CurrentPaginatedList paginatedList = PaginatedDAO.getPaginatedListFromRequest(req);

            q.setFirstResult(paginatedList.getFirstRecordIndex());

            q.setMaxResults(paginatedList.getPageSize());

            List currentFaultList = q.list();

//            List allList = getListCurrentPopupAssigned(currentFaultList, areaCode, new Conditional());
//            List lossflow = (List) allList.get(0);
//            List lossPower = (List) allList.get(1);
//            List lossOther = (List) allList.get(2);

            List resultList = qFlowLossCount.list();
            int flowLossCount = 0;
            int powerLossCount = 0;
            int stationErrorCount = 0;
            Object[] objArr = null;
            if (resultList != null && resultList.size() > 0) {
                for (Object obj : resultList) {
                    objArr = (Object[]) obj;
                    if (Constant.FAULT_TYPE.FLOW_LOSS.equals(Long.valueOf(objArr[1].toString()))) {
                        flowLossCount = Long.valueOf(objArr[0].toString()).intValue();
                    } else if (Constant.FAULT_TYPE.BTS_FAULT.equals(Long.valueOf(objArr[1].toString()))) {
                        powerLossCount = Long.valueOf(objArr[0].toString()).intValue();
                    } else if (Constant.FAULT_TYPE.POWER_LOSS.equals(Long.valueOf(objArr[1].toString()))) {
                        stationErrorCount = Long.valueOf(objArr[0].toString()).intValue();
                    }

                }
            }

            req.setAttribute("countBtsLossFlow1", flowLossCount);
            req.setAttribute("countBtsOther1", stationErrorCount);
            req.setAttribute("countBtsFail1", powerLossCount);
            paginatedList.setList(currentFaultList);

            paginatedList.setTotalNumberOfRows(flowLossCount + powerLossCount + stationErrorCount);

            req.setAttribute("listBtsFailAdmin1", paginatedList);


            return lstReturn;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * Get bts error.
     * 
     * @param req the req
     * @param searchBtsForm the search bts form  
     * @param typeExport 0:not Export,1,2,3:export
     * 
     * @return the bts fail popup
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     */
    public List getBtsFailPopup(HttpServletRequest req, SearchBtsForm searchBtsForm, int typeExport) throws Exception {
        try {
            //result
            List lstReturn = null;

            HttpSession httpSession = req.getSession();
            //Type of page

            String lossType = QueryCryptUtils.getParameter(req, "type");
            if (lossType == null) {
                lossType = "";
            }



            //Conditionals variable from Form
            String areaCode = searchBtsForm.getAreaCode();
            if (areaCode == null || areaCode.equals("") || areaCode.equals("0")) {
                areaCode = (String) httpSession.getAttribute(Constant.COMMON.PROVINCE_CODE);
            }


//                  try {
            Conditional dk = new Conditional();
            //result
//            List lstReturn = null;

            //Kiem tra cac dieu kien loc de them vao sql
            /*chuan hoa du lieu*/
            if (areaCode != null) {
                if (areaCode.equals("0")) {
                    areaCode = "";
                }
            } else {
                areaCode = "";
            }

            if (StringUtils.isNotNull(searchBtsForm.getPersistentLevel())) {//loc theo tong dai

                if (SMSTimerTask.getSysdate() != null) {
                    Date date = SMSTimerTask.getSysdate();
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.setTime(date);
                    dateCalendar.add(Calendar.SECOND, -1 * Integer.parseInt(searchBtsForm.getPersistentLevel()));


                    dk.setPersistentDate(dateCalendar.getTime());

                }
            }

            /*Kiem tra dieu kien loc*/
            //Ma tinh thanh
            if (areaCode != null) {
                //check area
                if (areaCode.equals("") || areaCode.equals("0") || areaCode.equals(Constant.COMMON.ADMIN_TTDHKT)) {
                } else if (!StringUtils.isNotNull(areaCode)) {

                    dk.setKvCode(areaCode);

                } else {
                    dk.setAreaCode(areaCode);
                }
            }
            if (SMSTimerTask.getSysdate() != null) {
                dk.setSysDate(SMSTimerTask.getSysdate());
            } else {
                dk.setSysDate(new Date());
            }

            if (StringUtils.isNotNullSelect(searchBtsForm.getImportant())) {
                dk.setImportant(Long.valueOf(searchBtsForm.getImportant()));
            }

            if (StringUtils.isNotNullSelect(searchBtsForm.getDescLevelId())) {
                dk.setDescLevelId(Long.valueOf(searchBtsForm.getDescLevelId()));
            }


            //Loc truong hop leader hoac staff
            HttpSession session = req.getSession();
            if ((session.getAttribute(Constant.ASSSIGN_TASK.ROLE)) != null) {
                String role = session.getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();

                //Truong hop chuc danh user la nhom truong: Loc theo group_id
                if (role.equals(Constant.ASSSIGN_TASK.LEADER)) {
                    Long groupId = (Long) session.getAttribute(Constant.ASSSIGN_TASK.GROUP_ID);

                    dk.setGroupId(groupId.toString());
                }

                //Truong hop chuc danh user la nhan vien: Loc theo staff_id
                if (role.equals(Constant.ASSSIGN_TASK.STAFF)) {
                    Long staffId = (Long) session.getAttribute(Constant.ASSSIGN_TASK.STAFF_ID);

                    dk.setStaffId(staffId.toString());
                }
            }


            if (StringUtils.isNotNull(searchBtsForm.getBtsid())) {
                dk.setBtsid(searchBtsForm.getBtsid().trim());
            }

            if (StringUtils.isNotNull(searchBtsForm.getFaultGroupId())) {
                dk.setFaultGroupId(Long.valueOf(searchBtsForm.getFaultGroupId()));
            }
//            dk.setFaultGroupId(CAN_NGUON);

            if (StringUtils.isNotNull(searchBtsForm.getBscid())) {
                dk.setBscid(searchBtsForm.getBscid().trim());
            }
            if (StringUtils.isNotNull(searchBtsForm.getGroupId()) && !"0".equals(searchBtsForm.getGroupId())) {
                dk.setGroupId(searchBtsForm.getGroupId().trim());
            }

            //Dieu kien loc theo nhom loi
            if (searchBtsForm.getArrId() != null) {
                Map<Long, Long> mapFaultId = new HashMap<Long, Long>();
                for (Long id : searchBtsForm.getArrId()) {
                    mapFaultId.put(id, 1L);
                }
                dk.setLstFaultId(mapFaultId);
            }

            if (lossType == null) {
                lossType = "";
            }

            getCurrentListToPopup(httpSession, req, lossType, typeExport, dk, searchBtsForm);

            return lstReturn;


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Gets the list current popup assigned.
     * 
     * @param sourceList the source list
     * @param areaCode the area code
     * @param dk the dk
     * 
     * @return the list current popup assigned
     */
    public List getListCurrentPopupAssigned(List sourceList, String areaCode, Conditional dk) { //Khong dung nen x?a di !!! commented by CuongNH
        List rerult = new ArrayList();
        List lostPower = new ArrayList();
        List lostFlow = new ArrayList();
        List lostOther = new ArrayList();
        List currentFaultList = new ArrayList();
        List lostAdmin = new ArrayList();
        Map<String, VAssignedFaultBO> objMap = new HashMap<String, VAssignedFaultBO>();


        Long lg = 10L;
        lg.compareTo(lg);

        for (Object object : sourceList) {
            VAssignedFaultBO currentTemp = (VAssignedFaultBO) object;
            if (currentTemp.getAreaCode() != null && currentTemp.getKvCode() != null && currentTemp.getBtsid() != null) {
                if (areaCode.equals("") || areaCode.equals(Constant.COMMON.ADMIN_TTDHKT)) {



                    //for TTDHKT
                    if (currentTemp.getFaultGroupId().intValue() == Constant.FAULT_TYPE.FLOW_LOSS.intValue() && currentTemp.isCheck(dk) && ((dk.getFaultId() != null ? dk.getFaultId().intValue() : 0) != Constant.RUN_DIESEL)) {
                        lostFlow.add(currentTemp);
                        currentFaultList.add(currentTemp);
                    } else if (currentTemp.getFaultGroupId().intValue() == Constant.FAULT_TYPE.BTS_FAULT.intValue() && currentTemp.isCheck(dk) && ((dk.getFaultId() != null ? dk.getFaultId().longValue() : 0) != Constant.RUN_DIESEL)) {
                        lostOther.add(currentTemp);
                        currentFaultList.add(currentTemp);
                    } else if (currentTemp.getFaultGroupId().intValue() == Constant.FAULT_TYPE.POWER_LOSS.intValue() && currentTemp.isCheck(dk)) {
                        //Group row for loss power
                        String key = currentTemp.getBscid() + "!!" + currentTemp.getBtsid() + "||" + currentTemp.getFaultGroupId();
                        if (objMap.containsKey(key)) {
                            VAssignedFaultBO objTemp = (VAssignedFaultBO) objMap.get(key);
                            if (currentTemp.getFaultId().longValue() == Constant.MOBILE_FAULT_TYPE.MAT_DIEN && (dk.getFaultId() != null ? dk.getFaultId().longValue() : 0) != Constant.MOBILE_FAULT_TYPE.MAY_NO) {
                                objTemp.setStarttimeLosePower(currentTemp.getBsctime());
                            } else if (currentTemp.getFaultId().longValue() == Constant.MOBILE_FAULT_TYPE.CAN_NGUON && (dk.getFaultId() != null ? dk.getFaultId().longValue() : 0) != Constant.MOBILE_FAULT_TYPE.MAY_NO) {
                                objTemp.setStarttimePowBattery(currentTemp.getBsctime());
                            } else if (currentTemp.getFaultId().longValue() == Constant.MOBILE_FAULT_TYPE.MAY_NO && (dk.getFaultId() != null ? dk.getFaultId().longValue() : 0) == Constant.MOBILE_FAULT_TYPE.MAY_NO) {
                                objTemp.setStartTimeRunDiesel(currentTemp.getBsctime());
                            }
                        } else {
                            if ((dk.getFaultId() != null ? dk.getFaultId().longValue() : 0) == Constant.RUN_DIESEL) {
                                if (currentTemp.getFaultId().longValue() == Constant.MOBILE_FAULT_TYPE.MAY_NO) {
                                    lostPower.add(currentTemp);
                                    currentFaultList.add(currentTemp);
                                    objMap.put(key, currentTemp);
                                }
                            } else {
                                lostPower.add(currentTemp);
                                currentFaultList.add(currentTemp);
                                objMap.put(key, currentTemp);
                            }
                        }
                    }

                } else if (currentTemp.getAreaCode().contains(areaCode)) {

                    //for KV
                    if (currentTemp.getFaultGroupId().intValue() == Constant.FAULT_TYPE.FLOW_LOSS.intValue() && currentTemp.isCheck(dk) && ((dk.getFaultId() != null ? dk.getFaultId().intValue() : 0) != Constant.RUN_DIESEL)) {
                        lostFlow.add(currentTemp);
                        lostAdmin.add(currentTemp);
                        currentFaultList.add(currentTemp);
                    } else if (currentTemp.getFaultGroupId().intValue() == Constant.FAULT_TYPE.BTS_FAULT.intValue() && currentTemp.isCheck(dk) && ((dk.getFaultId() != null ? dk.getFaultId().intValue() : 0) != Constant.RUN_DIESEL)) {
                        lostOther.add(currentTemp);
                        lostAdmin.add(currentTemp);
                        currentFaultList.add(currentTemp);

                    } else if (currentTemp.getFaultGroupId().intValue() == Constant.FAULT_TYPE.POWER_LOSS.intValue() && currentTemp.isCheck(dk)) {
                        //Group row for loss power
                        String key = currentTemp.getBscid() + "!!" + currentTemp.getBtsid() + "||" + currentTemp.getFaultGroupId();
                        if (objMap.containsKey(key)) {
                            VAssignedFaultBO objTemp = (VAssignedFaultBO) objMap.get(key);

                            if (currentTemp.getFaultId().longValue() == Constant.MOBILE_FAULT_TYPE.MAT_DIEN) {
                                objTemp.setStarttimeLosePower(currentTemp.getBsctime());
                            } else if (currentTemp.getFaultId().longValue() == Constant.MOBILE_FAULT_TYPE.CAN_NGUON) {
                                objTemp.setStarttimePowBattery(currentTemp.getBsctime());
                            } else if (currentTemp.getFaultId().longValue() == Constant.MOBILE_FAULT_TYPE.MAY_NO) {
                                objTemp.setStartTimeRunDiesel(currentTemp.getBsctime());
                            }
                        } else {
                            if ((dk.getFaultId() != null ? dk.getFaultId().longValue() : 0) == Constant.RUN_DIESEL) {
                                if (currentTemp.getFaultId().longValue() == Constant.MOBILE_FAULT_TYPE.MAY_NO) {
                                    lostPower.add(currentTemp);
                                    objMap.put(key, currentTemp);
                                    lostAdmin.add(currentTemp);
                                    currentFaultList.add(currentTemp);
                                }
                            } else {
                                lostPower.add(currentTemp);
                                objMap.put(key, currentTemp);
                                lostAdmin.add(currentTemp);
                                currentFaultList.add(currentTemp);
                            }

                        }
                    }

                }

            }
        }

        rerult.add(lostFlow);
        rerult.add(lostPower);
        rerult.add(lostOther);
        rerult.add(lostAdmin);
        rerult.add(currentFaultList);

        return rerult;
    }

    /**
     * Gets the list current popup.
     * 
     * @param sourceList the source list
     * @param areaCode the area code
     * @param dk the dk
     * @param faultBO the fault bo
     * 
     * @return the list current popup
     */
    public List getListCurrentPopup(List sourceList, String areaCode, Conditional dk, vCurrentFaultBO faultBO) {
        List rerult = new ArrayList();
        List lostPower = new ArrayList();
        List lostFlow = new ArrayList();
        List lostOther = new ArrayList();
        List currentFaultList = new ArrayList();
        List lostAdmin = new ArrayList();
        Map<String, vCurrentFaultBO> objMap = new HashMap<String, vCurrentFaultBO>();

        boolean hasRing = false;
        Long lg = 10L;
        lg.compareTo(lg);
        Calendar bsctime = Calendar.getInstance();
        Calendar sysDate = Calendar.getInstance();
        if (sourceList != null && sourceList.size() > 0) {
            for (Object object : sourceList) {
                vCurrentFaultBO currentTemp = (vCurrentFaultBO) object;
                if (currentTemp.getAreaCode() != null && currentTemp.getKvCode() != null && currentTemp.getBtsid() != null) {
                    bsctime.setTime(currentTemp.getBsctime());
                    sysDate.setTime(dk.getSysDate());

//                    System.out.println("==============================================================");
//                    System.out.println("bsctime : " + currentTemp.getBsctime());
//                    System.out.println("sysDate : " + dk.getSysDate());

                    lg = (sysDate.getTimeInMillis() - bsctime.getTimeInMillis()) / (1000 * 60);
//                    System.out.println("lg : " + lg);
                    currentTemp.setDurationTime(lg);
                    currentTemp.setDurationFormat(StringUtils.durationFormat(lg));
                    if (areaCode.equals("") || areaCode.equals(Constant.COMMON.ADMIN_TTDHKT)) {
//                        if (currentTemp.getRing() == null) {
//                         if (faultBO.getStarttime() != null && currentTemp.getStarttime() != null && currentTemp.getStarttime().after(faultBO.getStarttime())) {
//                                currentTemp.setRing(1l);
//                            }   
//                        } else {
//                           // currentTemp.setRing(null);
//                        }
                        //currentTemp.setRing(null);
                        if (!hasRing && faultBO.getStarttime() != null && currentTemp.getStarttime() != null && currentTemp.getStarttime().after(faultBO.getStarttime())) {
                            currentTemp.setRing(1l);
                            hasRing = true;
                        }
                        //for TTDHKT
                        if (currentTemp.getFaultGroupId().intValue() == Constant.FAULT_TYPE.FLOW_LOSS.intValue() && currentTemp.isCheck(dk) && ((dk.getFaultId() != null ? dk.getFaultId().intValue() : 0) != Constant.RUN_DIESEL)) {
                            lostFlow.add(currentTemp);
                            currentFaultList.add(currentTemp);
                        } else if (currentTemp.getFaultGroupId().intValue() == Constant.FAULT_TYPE.BTS_FAULT.intValue() && currentTemp.isCheck(dk) && ((dk.getFaultId() != null ? dk.getFaultId().longValue() : 0) != Constant.RUN_DIESEL)) {
                            lostOther.add(currentTemp);
                            currentFaultList.add(currentTemp);
                        } else if (currentTemp.getFaultGroupId().intValue() == Constant.FAULT_TYPE.POWER_LOSS.intValue() && currentTemp.isCheck(dk)) {
                            //Group row for loss power
                            String key = currentTemp.getBscid() + "!!" + currentTemp.getBtsid() + "||" + currentTemp.getFaultGroupId();
                            if (objMap.containsKey(key) && !Constant.COMMON.IS_DUPLICATE.equals(currentTemp.getIsDuplicated())) {
                                vCurrentFaultBO objTemp = (vCurrentFaultBO) objMap.get(key);
                                if (currentTemp.getFaultid().longValue() == Constant.MOBILE_FAULT_TYPE.MAT_DIEN && (dk.getFaultId() != null ? dk.getFaultId().longValue() : 0) != Constant.MOBILE_FAULT_TYPE.MAY_NO) {
                                    objTemp.setStarttimeLosePower(currentTemp.getBsctime());
                                } else if (currentTemp.getFaultid().longValue() == Constant.MOBILE_FAULT_TYPE.CAN_NGUON && (dk.getFaultId() != null ? dk.getFaultId().longValue() : 0) != Constant.MOBILE_FAULT_TYPE.MAY_NO) {
                                    objTemp.setStarttimePowBattery(currentTemp.getBsctime());
                                } else if (currentTemp.getFaultid().longValue() == Constant.MOBILE_FAULT_TYPE.MAY_NO && (dk.getFaultId() != null ? dk.getFaultId().longValue() : 0) == Constant.MOBILE_FAULT_TYPE.MAY_NO) {
                                    objTemp.setStartTimeRunDiesel(currentTemp.getBsctime());
                                }
                            } else {
                                if ((dk.getFaultId() != null ? dk.getFaultId().longValue() : 0) == Constant.RUN_DIESEL) {
                                    if (currentTemp.getFaultid().longValue() == Constant.MOBILE_FAULT_TYPE.MAY_NO) {
                                        lostPower.add(currentTemp);
                                        currentFaultList.add(currentTemp);
                                        objMap.put(key, currentTemp);
                                    }
                                } else {
                                    lostPower.add(currentTemp);
                                    currentFaultList.add(currentTemp);
                                    objMap.put(key, currentTemp);
 
