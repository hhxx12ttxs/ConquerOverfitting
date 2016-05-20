/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lifetek.netmosys.database.DAO;

import com.lifetek.netmosys.client.form.CellManagementForm;
import com.lifetek.netmosys.database.cell.BO.Cell2gDetailBO;
import com.lifetek.netmosys.database.cell.BO.Cell3gDetailBO;
import com.lifetek.netmosys.database.cell.BO.CellBtsBO;
import com.lifetek.netmosys.database.cell.BO.CellBtsNotApproveBO;
import com.lifetek.netmosys.database.cell.BO.CellNodebBO;
import com.lifetek.netmosys.database.cell.BO.CellNodebNotApproveBO;
import com.lifetek.netmosys.util.Constant;
import com.lifetek.netmosys.util.QueryCryptUtils;
import com.lifetek.netmosys.util.StringUtils;
import com.lifetek.database.BO.ActionResultBO;
import com.lifetek.database.config.BaseHibernateDAO;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionForm;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.Query;

/**
 *
 * @author LEDT
 */
public class CellApproveDAO extends BaseHibernateDAO {

    /**
     * 
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO showApproveProvince(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        List lst = null;
        CellManagementForm cellManagementForm = (CellManagementForm) form;
        if (QueryCryptUtils.getParameter(req, "ajax") != null && !"".equals(QueryCryptUtils.getParameter(req, "ajax").trim())) {
            lst = getListProvinceApprove(cellManagementForm, req);
            req.setAttribute("listBtsApproveProvince", lst);
            actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_PROVINCE);
        } else {
            lst = getListProvinceApprove(cellManagementForm, req);
            req.setAttribute("listBtsApproveProvince", lst);
            actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.SHOW_CELL_APPROVE_PROVINCE);
        }
        if (QueryCryptUtils.getParameter(req, "updateStatus") != null && !"".equals(QueryCryptUtils.getParameter(req, "updateStatus").trim())) {
            req.getSession().setAttribute("updateStatus", "Thông qua thŕnh công!");
        }
        return actionResult;
    }

    /**
     *
     * @param form
     * @param req
     * @return
     */
    public List getListProvinceApprove(ActionForm form, HttpServletRequest req) {
        CellManagementForm cellForm = (CellManagementForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        StringBuffer sqlSearchWhere = new StringBuffer();
        sqlSearch.append(" from ViewProvinceNotApproveBO a ");
        List lstPaAdmin = new ArrayList();
        sqlSearchWhere.append(" where 1=1 ");

        if (StringUtils.isNotNull(cellForm.getApproveStatus())) {
            sqlSearchWhere.append(" and btsPApproveStatus = ? ");
            lstPaAdmin.add(cellForm.getApproveStatus());
        }

        String provinceCode = "";
        if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
            provinceCode = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString();
        }

        sqlSearchWhere.append(" and lower(provinceCode) = ? ");
        lstPaAdmin.add(provinceCode.toLowerCase().substring(3));
//        sqlSearchWhere.append("  ORDER BY provinceName ");

        HttpSession httpSession = req.getSession();

        if ((req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE)) != null) {
            String role = httpSession.getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();

            //Truong hop chuc danh user la nhom truong: Loc theo group_id
            if (role.equals(Constant.ASSSIGN_TASK.LEADER)) {
                Long groupIdLogin = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.GROUP_ID);

                sqlSearchWhere.append(" and groupId =  ? ");
                lstPaAdmin.add(groupIdLogin);
            }
            //Truong hop chuc danh user la nhan vien: Loc theo staff_id
            if (role.equals(Constant.ASSSIGN_TASK.STAFF)) {
                Long staffId = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.STAFF_ID);
                sqlSearchWhere.append(" and staffId =  ? ");
                lstPaAdmin.add(staffId);
            }
        }

        StringBuffer sqlAdmin = new StringBuffer();
        sqlAdmin.append(sqlSearch);
        sqlAdmin.append(sqlSearchWhere);

        Query q = hibernateSession.createQuery(sqlAdmin.toString());

        for (int i = 0; i < lstPaAdmin.size(); i++) {

            q.setParameter(i, lstPaAdmin.get(i));
        }

        List listProvince = q.list();
        return listProvince;
    }

    /**
     *
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO rejectProvinceLevel(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();

        CellManagementForm cellManagementForm = (CellManagementForm) form;
        String[] checkRows = cellManagementForm.getCurrentIdArray();
        String sqlBts = " from CellBtsNotApproveBO where cellBtsId = ? ";
        Query qBts = getSession().createQuery(sqlBts);

        for (String id : checkRows) {
            // reject cac bts
            if (id.split(",")[0] != null && id.split(",")[0].equals("")) {
                qBts.setParameter(0, id.split(",")[0]);
                List lst = qBts.list();
                if (lst != null && lst.size() > 0) {
                    CellBtsNotApproveBO obj = (CellBtsNotApproveBO) lst.get(0);
                    obj.setpApproveStatus(Constant.CELL_MANAGEMENT.CELL_STATUS.REJECT);
                    getSession().save(obj);
                }
            }

        }
        req.getSession().setAttribute("result", "Reject thŕnh công!");

        actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_PROVINCE);
        return actionResult;
    }

    /**
     *
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO approveProvinceLevel(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        CellManagementForm cellManagementForm = (CellManagementForm) form;
        String[] checkRows = cellManagementForm.getCurrentIdArray();

        for (String id : checkRows) {
            String[] ids = id.split(",");
            // xet cac bts
            if (id.length() > 1 && !ids[0].equals("") && !ids[1].equals("")) {
                updateBts(ids[0], ids[1]);
            }
        }
        req.getSession().setAttribute("result", "Thông qua thŕnh công!");
        actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_PROVINCE);
        return actionResult;
    }

    /**
     *
     * @param btsCode
     */
    private void updateBts(String cellBtsId, String btsId) {
        String cellSql = " from CellBtsNotApproveBO where cellBtsId =? and pApproveStatus != ?";
        Query qCell = getSession().createQuery(cellSql);
        qCell.setParameter(0, cellBtsId);
        qCell.setParameter(1, "APPROVED");
        List cellBtsList = qCell.list();
        if (cellBtsList != null && cellBtsList.size() > 0) {
            CellBtsNotApproveBO tempObj = (CellBtsNotApproveBO) cellBtsList.get(0);

            // xet trang thai can approve
            if (tempObj.getpApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.DELETED)) {
                // xoa ca trong bang tam va trong bang that
                String cellSearch = " from Cell2gDetailBO where cell2gId =? ";
                Query qCellSearch = getSession().createQuery(cellSearch);
                qCellSearch.setParameter(0, cellBtsId);
                List listCell = qCellSearch.list();
                if (listCell != null && listCell.size() > 0) {
                    Cell2gDetailBO cellObj = (Cell2gDetailBO) listCell.get(0);
                    getSession().delete(cellObj);

                    // neu bts khong con cell nao nua thi xoa
                    String btsToDelete = " from Cell2gDetailBO where cellBts = ? ";
                    Query qDelete = getSession().createQuery(btsToDelete);
                    qDelete.setParameter(0, btsId);
                    List listBtsDelete = qDelete.list();
                    if (listBtsDelete == null || listBtsDelete.isEmpty()) { // khong con cell nao thuoc bts nay
                        String btsSearch = " from CellBtsBO where cellBtsCode = ? ";
                        Query queryBts = getSession().createQuery(btsSearch);
                        queryBts.setParameter(0, btsId);
                        List lstBts = queryBts.list();
                        if (lstBts != null & lstBts.size() > 0) {
                            CellBtsBO obj = (CellBtsBO) lstBts.get(0);
                            getSession().delete(obj);
                        }
                    }
                }

                //xoa trong bang tam - chuyen luon trang thai sang da approve
                tempObj.setpApproveStatus("APPROVED");
            } else if (tempObj.getpApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.UPDATED)) {

                // lay tu bang that ra
                String cellSearch = " from Cell2gDetailBO where cell2gId =? ";
                Query qCellSearch = getSession().createQuery(cellSearch);
                qCellSearch.setParameter(0, cellBtsId);
                List listCell = qCellSearch.list();
                if (listCell != null && listCell.size() > 0) {
                    Cell2gDetailBO cellObj = (Cell2gDetailBO) listCell.get(0);
                    //cap nhat lai thong tin cell
                    cellObj.setPAntennaHeight(tempObj.getPAntennaHeight());
                    cellObj.setPAzimuth(tempObj.getPAzimuth());
                    cellObj.setPConfig(tempObj.getPConfig());
                    cellObj.setPTiltTotal(tempObj.getPTiltTotal());

                    getSession().save(cellObj);
                }

                // chuyentrang thai
                tempObj.setpApproveStatus("APPROVED");

                // cap nhat thong tin cho bts
                String btsSql = " from CellBtsBO where cellBtsCode = ? ";
                Query btsSearch = getSession().createQuery(btsSql);
                btsSearch.setParameter(0, btsId);
                List btsList = btsSearch.list();
                if (btsList != null && btsList.size() > 0) {
                    CellBtsBO btsObj = (CellBtsBO) btsList.get(0);
                    btsObj.setPAntennaType(tempObj.getPAntennaType());
                    btsObj.setPBtsTypeId(tempObj.getPBtsTypeId());
                    btsObj.setPCabinetType(tempObj.getPCabinetType());
                    btsObj.setPIntegratedDate(tempObj.getPIntegratedDate());
                    btsObj.setPVendor(tempObj.getPVendor());
                    btsObj.setStationHouseCode(tempObj.getStationHouseCode());
                    getSession().save(btsObj);
                }
            } else if (tempObj.getpApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.CREATED)) {
                //them moi cell
                Cell2gDetailBO cell2gObj = new Cell2gDetailBO();
                cell2gObj.setPAntennaHeight(tempObj.getPAntennaHeight());
                cell2gObj.setPAzimuth(tempObj.getPAzimuth());
                cell2gObj.setPConfig(tempObj.getPConfig());
                cell2gObj.setPTiltTotal(tempObj.getPTiltTotal());

                //kiem tra xem da co bts chua cell nay chua
                String btsSql = " from CellBtsBO where cellBtsCode = ? ";
                Query btsSearch = getSession().createQuery(btsSql);
                btsSearch.setParameter(0, btsId);
                List btsList = btsSearch.list();
                if (btsList == null || btsList.size() <= 0) {
                    // tao bts moi
                    CellBtsBO btsObj = new CellBtsBO();
                    btsObj.setCellBtsCode(btsId);
                    btsObj.setPAntennaType(tempObj.getPAntennaType());
                    btsObj.setPBtsTypeId(tempObj.getPBtsTypeId());
                    btsObj.setPCabinetType(tempObj.getPCabinetType());
                    btsObj.setPIntegratedDate(tempObj.getPIntegratedDate());
                    btsObj.setPVendor(tempObj.getPVendor());
                    btsObj.setStationHouseCode(tempObj.getStationHouseCode());
                    getSession().save(btsObj);
                }

                cell2gObj.setCell2gId(tempObj.getCellBtsId());
                cell2gObj.setCellBts(tempObj.getCellBtsCode());
                getSession().save(cell2gObj);
                tempObj.setpApproveStatus("APPROVED");
            } else if (tempObj.getpApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.REJECT)) {
                // khong duoc approve cell dang reject
            }
        }
    }

    /**
     *
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO showApproveArea(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        List listAlarmType = null;
        CellManagementForm cellManagementForm = (CellManagementForm) form;

        if (QueryCryptUtils.getParameter(req, "ajax") != null && !"".equals(QueryCryptUtils.getParameter(req, "ajax").trim())) {
            listAlarmType = getListAreaApprove(cellManagementForm, req);
            req.setAttribute("listBtsApproveArea", listAlarmType);
            actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_AREA);
        } else {
            String provinceCode = "";

            if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
                provinceCode = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString();
            }

            String sql = " from ProvinceBO where areaCode = ?";
            Query q = getSession().createQuery(sql);
            q.setParameter(0, provinceCode);
            req.setAttribute("listProvince", q.list());
            listAlarmType = getListAreaApprove(cellManagementForm, req);
            req.setAttribute("listBtsApproveArea", listAlarmType);
            actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.SHOW_CELL_APPROVE_AREA);
        }
        if (QueryCryptUtils.getParameter(req, "updateStatus") != null && !"".equals(QueryCryptUtils.getParameter(req, "updateStatus").trim())) {
            req.getSession().setAttribute("updateStatus", "Thông qua thŕnh công!");
        }
        return actionResult;
    }

    /**
     * 
     * @param form
     * @param req
     * @return
     */
    public List getListAreaApprove(ActionForm form, HttpServletRequest req) {
        CellManagementForm cellForm = (CellManagementForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        StringBuffer sqlSearchWhere = new StringBuffer();
        sqlSearch.append(" from ViewAreaNotApproveBO a ");
        List lstPaAdmin = new ArrayList();
        sqlSearchWhere.append(" where 1=1 ");
        if (StringUtils.isNotNull(cellForm.getApproveStatus())) {
            sqlSearchWhere.append(" and btsAApproveStatus = ? ");
            lstPaAdmin.add(cellForm.getApproveStatus());
        }

        if (StringUtils.isNotNull(cellForm.getVendor())) {
            sqlSearchWhere.append(" and btsVendor = ?  ");
            lstPaAdmin.add(cellForm.getVendor());
        }

        if (StringUtils.isNotNull(cellForm.getProvinceCode())) {
            sqlSearchWhere.append(" and provinceCode =? ");
            lstPaAdmin.add(cellForm.getProvinceCode());
        }

        String provinceCode = "";
        if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
            provinceCode = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString();
        }

        sqlSearchWhere.append(" and lower(kvCode) = ? ");
        lstPaAdmin.add(provinceCode.toLowerCase());
//        sqlSearchWhere.append("  ORDER BY provinceName ");

        HttpSession httpSession = req.getSession();

        if ((req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE)) != null) {
            String role = httpSession.getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();

            //Truong hop chuc danh user la nhom truong: Loc theo group_id
            if (role.equals(Constant.ASSSIGN_TASK.LEADER)) {
                Long groupIdLogin = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.GROUP_ID);

                sqlSearchWhere.append(" and groupId =  ? ");
                lstPaAdmin.add(groupIdLogin);
            }
            //Truong hop chuc danh user la nhan vien: Loc theo staff_id
            if (role.equals(Constant.ASSSIGN_TASK.STAFF)) {
                Long staffId = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.STAFF_ID);
                sqlSearchWhere.append(" and staffId =  ? ");
                lstPaAdmin.add(staffId);
            }
        }
        
        StringBuffer sqlAdmin = new StringBuffer();
        sqlAdmin.append(sqlSearch);
        sqlAdmin.append(sqlSearchWhere);

        Query q = hibernateSession.createQuery(sqlAdmin.toString());
        for (int i = 0; i < lstPaAdmin.size(); i++) {
            q.setParameter(i, lstPaAdmin.get(i));
        }
        List listProvince = q.list();
        return listProvince;
    }

    /**
     *
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO approveAreaLevel(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();

        CellManagementForm cellManagementForm = (CellManagementForm) form;
        String[] checkRows = cellManagementForm.getCurrentIdArray();

        for (String id : checkRows) {
            String[] ids = id.split(",");

            // xet cac bts
            if (ids.length > 1 && !ids[0].equals("") && !ids[1].equals("")) {
                updateBtsArea(ids[0], ids[1]);
            }
        }

        req.getSession().setAttribute("result", "Thông qua thŕnh công!");

        actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_AREA);
        return actionResult;
    }

    /**
     *
     * @param btsCode
     */
    private void updateBtsArea(String cellBtsId, String btsId) {
        String cellSql = " from CellBtsNotApproveBO where cellBtsId =? and aApproveStatus != ?";
        Query qCell = getSession().createQuery(cellSql);
        qCell.setParameter(0, cellBtsId);
        qCell.setParameter(1, "APPROVED");
        List cellBtsList = qCell.list();
        if (cellBtsList != null && cellBtsList.size() > 0) {
            CellBtsNotApproveBO tempObj = (CellBtsNotApproveBO) cellBtsList.get(0);

            // xet trang thai can approve
            if (tempObj.getaApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.DELETED)) {
                // xoa ca trong bang tam va trong bang that
                String cellSearch = " from Cell2gDetailBO where cell2gId =? ";
                Query qCellSearch = getSession().createQuery(cellSearch);
                qCellSearch.setParameter(0, cellBtsId);
                List listCell = qCellSearch.list();
                if (listCell != null && listCell.size() > 0) {
                    Cell2gDetailBO cellObj = (Cell2gDetailBO) listCell.get(0);
                    getSession().delete(cellObj);

                    // neu bts khong con cell nao nua thi xoa
                    String btsToDelete = " from Cell2gDetailBO where cellBts = ? ";
                    Query qDelete = getSession().createQuery(btsToDelete);
                    qDelete.setParameter(0, btsId);
                    List listBtsDelete = qDelete.list();
                    if (listBtsDelete == null || listBtsDelete.isEmpty()) { // khong con cell nao thuoc bts nay
                        String btsSearch = " from CellBtsBO where cellBtsCode = ? ";
                        Query queryBts = getSession().createQuery(btsSearch);
                        queryBts.setParameter(0, btsId);
                        List lstBts = queryBts.list();
                        if (lstBts != null & lstBts.size() > 0) {
                            CellBtsBO obj = (CellBtsBO) lstBts.get(0);
                            getSession().delete(obj);
                        }
                    }
                }

                //xoa trong bang tam
                tempObj.setaApproveStatus("APPROVED");
            } else if (tempObj.getaApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.UPDATED)) {

                // lay tu bang that ra
                String cellSearch = " from Cell2gDetailBO where cell2gId =? ";
                Query qCellSearch = getSession().createQuery(cellSearch);
                qCellSearch.setParameter(0, cellBtsId);
                List listCell = qCellSearch.list();
                if (listCell != null && listCell.size() > 0) {
                    Cell2gDetailBO cellObj = (Cell2gDetailBO) listCell.get(0);
                    //cap nhat lai thong tin cell
                    cellObj.setABcch(tempObj.getABcch());
                    cellObj.setABsi(tempObj.getABsic());
                    cellObj.setACi(tempObj.getACi());
                    cellObj.setATch(tempObj.getATch());

                    getSession().save(cellObj);
                }

                // chuyen luon trang thai
                tempObj.setaApproveStatus("APPROVED");

                // cap nhat thong tin cho bts
                String btsSql = " from CellBtsBO where cellBtsCode = ? ";
                Query btsSearch = getSession().createQuery(btsSql);
                btsSearch.setParameter(0, btsId);
                List btsList = btsSearch.list();
                if (btsList != null && btsList.size() > 0) {
                    CellBtsBO btsObj = (CellBtsBO) btsList.get(0);
                    btsObj.setABandUse(tempObj.getABandUse());
                    btsObj.setABscCode(tempObj.getABscCode());
                    btsObj.setALac(tempObj.getALac());
                    btsObj.setAMscCode(tempObj.getAMscCode());
                    btsObj.setAStationStatusId(tempObj.getAStationStatusId());

                    btsObj.setStationHouseCode(tempObj.getStationHouseCode());
                    getSession().save(btsObj);
                }
            } else if (tempObj.getaApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.CREATED)) {
                //them moi cell
                Cell2gDetailBO cell2gObj = new Cell2gDetailBO();
                cell2gObj.setABcch(tempObj.getABcch());
                cell2gObj.setABsi(tempObj.getABsic());
                cell2gObj.setACi(tempObj.getACi());
                cell2gObj.setATch(tempObj.getATch());

                //kiem tra xem da co bts chua cell nay chua
                String btsSql = " from CellBtsBO where cellBtsCode = ? ";
                Query btsSearch = getSession().createQuery(btsSql);
                btsSearch.setParameter(0, btsId);
                List btsList = btsSearch.list();
                if (btsList == null || btsList.size() <= 0) {
                    // tao bts moi
                    CellBtsBO btsObj = new CellBtsBO();
                    btsObj.setCellBtsCode(btsId);
                    btsObj.setABandUse(tempObj.getABandUse());
                    btsObj.setABscCode(tempObj.getABscCode());
                    btsObj.setALac(tempObj.getALac());
                    btsObj.setAMscCode(tempObj.getAMscCode());
                    btsObj.setAStationStatusId(tempObj.getAStationStatusId());

                    btsObj.setStationHouseCode(tempObj.getStationHouseCode());
                    getSession().save(btsObj);
                }

                cell2gObj.setCell2gId(tempObj.getCellBtsId());
                cell2gObj.setCellBts(tempObj.getCellBtsCode());
                getSession().save(cell2gObj);

                // neu chi update bts thi chuyen luon trang thai
                tempObj.setaApproveStatus("APPROVED");

            } else if (tempObj.getaApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.CREATED)) {
                // khong duoc approve cell dang reject
            }
        }
    }

    /**
     *
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO rejectAreaLevel(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        List listAlarmType = null;
        CellManagementForm cellManagementForm = (CellManagementForm) form;
        String[] checkRows = cellManagementForm.getCurrentIdArray();
        String sqlBts = " from CellBtsNotApproveBO where cellBtsId = ? ";
        Query qBts = getSession().createQuery(sqlBts);
        for (String id : checkRows) {
            if (id.split(",")[0] != null && !id.split(",")[0].equals("")) {
                qBts.setParameter(0, id.split(",")[0]);
                List lst = qBts.list();
                if (lst != null && lst.size() > 0) {
                    CellBtsNotApproveBO obj = (CellBtsNotApproveBO) lst.get(0);
                    obj.setaApproveStatus(Constant.CELL_MANAGEMENT.CELL_STATUS.REJECT);
                    getSession().save(obj);
                }
            }
        }
        req.getSession().setAttribute("result", "Reject thŕnh công!");
        listAlarmType = getListAreaApprove(cellManagementForm, req);
        req.setAttribute("listBtsApproveProvince", listAlarmType);
        actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_AREA);
        return actionResult;
    }

    //====================================NodeB======================================//
    /**
     * 
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO showApproveProvinceNodeB(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        List lst = null;
        CellManagementForm cellManagementForm = (CellManagementForm) form;
        if (QueryCryptUtils.getParameter(req, "ajax") != null && !"".equals(QueryCryptUtils.getParameter(req, "ajax").trim())) {
            lst = getListProvinceApproveNodeB(cellManagementForm, req);
            req.setAttribute("listBtsApproveProvince", lst);
            actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_PROVINCE_NODEB);
        } else {
            lst = getListProvinceApproveNodeB(cellManagementForm, req);
            req.setAttribute("listBtsApproveProvince", lst);
            actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.SHOW_CELL_APPROVE_PROVINCE_NODEB);
        }
        if (QueryCryptUtils.getParameter(req, "updateStatus") != null && !"".equals(QueryCryptUtils.getParameter(req, "updateStatus").trim())) {
            req.getSession().setAttribute("updateStatus", "Thông qua thŕnh công!");
        }
        return actionResult;
    }

    /**
     *
     * @param form
     * @param req
     * @return
     */
    public List getListProvinceApproveNodeB(ActionForm form, HttpServletRequest req) {
        CellManagementForm cellForm = (CellManagementForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        StringBuffer sqlSearchWhere = new StringBuffer();
        sqlSearch.append(" from ViewProvinceNotApproveNodeBBO a ");
        List lstPaAdmin = new ArrayList();
        sqlSearchWhere.append(" where 1=1 ");

        if (StringUtils.isNotNull(cellForm.getApproveStatus())) {
            sqlSearchWhere.append(" and nodebPApproveStatus = ? ");
            lstPaAdmin.add(cellForm.getApproveStatus());
        }

        String provinceCode = "";
        if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
            provinceCode = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString();
        }

        sqlSearchWhere.append(" and lower(provinceCode) = ? ");
        lstPaAdmin.add(provinceCode.toLowerCase().substring(3));
//        sqlSearchWhere.append("  ORDER BY provinceName ");

        HttpSession httpSession = req.getSession();

        if ((req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE)) != null) {
            String role = httpSession.getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();

            //Truong hop chuc danh user la nhom truong: Loc theo group_id
            if (role.equals(Constant.ASSSIGN_TASK.LEADER)) {
                Long groupIdLogin = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.GROUP_ID);

                sqlSearchWhere.append(" and groupId =  ? ");
                lstPaAdmin.add(groupIdLogin);
            }
            //Truong hop chuc danh user la nhan vien: Loc theo staff_id
            if (role.equals(Constant.ASSSIGN_TASK.STAFF)) {
                Long staffId = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.STAFF_ID);
                sqlSearchWhere.append(" and staffId =  ? ");
                lstPaAdmin.add(staffId);
            }
        }
        
        StringBuffer sqlAdmin = new StringBuffer();
        sqlAdmin.append(sqlSearch);
        sqlAdmin.append(sqlSearchWhere);

        Query q = hibernateSession.createQuery(sqlAdmin.toString());

        for (int i = 0; i < lstPaAdmin.size(); i++) {
            q.setParameter(i, lstPaAdmin.get(i));
        }

        List listProvince = q.list();
        return listProvince;
    }

    /**
     *
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO rejectProvinceLevelNodeB(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();

        CellManagementForm cellManagementForm = (CellManagementForm) form;
        String[] checkRows = cellManagementForm.getCurrentIdArray();

        String sqlNodeB = " from CellNodebNotApproveBO where cellNodebId = ? ";

        Query qNodeB = getSession().createQuery(sqlNodeB);
        for (String id : checkRows) {

            // reject cac nodeb
            if (id.split(",")[0] != null && !id.split(",")[0].equals("")) {
                qNodeB.setParameter(0, id.split(",")[0]);
                List lst = qNodeB.list();
                if (lst != null && lst.size() > 0) {
                    CellNodebNotApproveBO obj = (CellNodebNotApproveBO) lst.get(0);
                    obj.setpApproveStatus(Constant.CELL_MANAGEMENT.CELL_STATUS.REJECT);
                    getSession().save(obj);
                }
            }
        }
        req.getSession().setAttribute("result", "Reject thŕnh công!");

        actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_PROVINCE);
        return actionResult;
    }

    /**
     *
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO approveProvinceLevelNodeB(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        CellManagementForm cellManagementForm = (CellManagementForm) form;
        String[] checkRows = cellManagementForm.getCurrentIdArray();

        for (String id : checkRows) {
            String[] ids = id.split(",");
            // xet cac nodeb
            if (ids.length > 1 && !ids[0].equals("") && !ids[1].equals("")) {
                updateNodeb(ids[0], ids[1]);
            }
        }

        req.getSession().setAttribute("result", "Thông qua thŕnh công!");
        actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_PROVINCE);
        return actionResult;
    }

    /**
     *
     * @param nodebCode
     */
    private void updateNodeb(String cellNodebId, String nodebId) {
        String cellSql = " from CellNodebNotApproveBO where cellNodebId =? and pApproveStatus != ?";
        Query qCell = getSession().createQuery(cellSql);
        qCell.setParameter(0, cellNodebId);
        qCell.setParameter(1, "APPROVED");
        List cellNodebList = qCell.list();
        if (cellNodebList != null && cellNodebList.size() > 0) {
            CellNodebNotApproveBO tempObj = (CellNodebNotApproveBO) cellNodebList.get(0);

            // xet trang thai can approve
            if (tempObj.getpApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.DELETED)) {
                // xoa ca trong bang tam va trong bang that
                String cellSearch = " from Cell3gDetailBO where cell3gId =? ";
                Query qCellSearch = getSession().createQuery(cellSearch);
                qCellSearch.setParameter(0, cellNodebId);
                List listCell = qCellSearch.list();
                if (listCell != null && listCell.size() > 0) {
                    Cell3gDetailBO cellObj = (Cell3gDetailBO) listCell.get(0);
                    getSession().delete(cellObj);

                    // neu nodeb khong con cell nao nua thi xoa
                    String btsToDelete = " from Cell3gDetailBO where cell3gId = ? ";
                    Query qDelete = getSession().createQuery(btsToDelete);
                    qDelete.setParameter(0, nodebId);
                    List listBtsDelete = qDelete.list();
                    if (listBtsDelete == null || listBtsDelete.isEmpty()) { // khong con cell nao thuoc nodeb nay
                        String btsSearch = " from CellNodebBO where nodebCode = ? ";
                        Query queryBts = getSession().createQuery(btsSearch);
                        queryBts.setParameter(0, nodebId);
                        List lstBts = queryBts.list();
                        if (lstBts != null & lstBts.size() > 0) {
                            CellNodebBO obj = (CellNodebBO) lstBts.get(0);
                            getSession().delete(obj);
                        }
                    }
                }

                //xoa trong bang tam - chuyen trang thai sang da approve
                tempObj.setpApproveStatus("APPROVED");
            } else if (tempObj.getpApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.UPDATED)) {

                // lay tu bang that ra
                String cellSearch = " from Cell3gDetailBO where cell3gId =? ";
                Query qCellSearch = getSession().createQuery(cellSearch);
                qCellSearch.setParameter(0, cellNodebId);
                List listCell = qCellSearch.list();
                if (listCell != null && listCell.size() > 0) {
                    Cell3gDetailBO cellObj = (Cell3gDetailBO) listCell.get(0);
                    //cap nhat lai thong tin cell
                    cellObj.setPAntennaHeight(tempObj.getPAntennaHeight());
                    cellObj.setPAzimuth(tempObj.getPAzimuth());
                    cellObj.setPTiltTotal(tempObj.getPTitlTotal());

                    getSession().save(cellObj);
                }

                //chuyen trang thai trong bang tam
                tempObj.setpApproveStatus("APPROVED");

                // cap nhat thong tin cho nodeb
                String nodebSql = " from CellNodebBO where nodebCode = ? ";
                Query nodebSearch = getSession().createQuery(nodebSql);
                nodebSearch.setParameter(0, nodebId);
                List nodebList = nodebSearch.list();
                if (nodebList != null && nodebList.size() > 0) {
                    CellNodebBO nodebObj = (CellNodebBO) nodebList.get(0);
                    nodebObj.setPAntennaType(tempObj.getPAntennaType());
                    nodebObj.setPCabinetNodebType(tempObj.getPCabinetNodebType());
                    nodebObj.setPIntegratedDate(tempObj.getPIntegratedDate());
                    nodebObj.setPInterfaceTransmit(tempObj.getPInterfaceTransmit());
                    nodebObj.setPRruPlaceDetail(tempObj.getPRruPlaceDetail());
                    nodebObj.setPTransmitCapacity(tempObj.getPTransmitCapacity());
                    nodebObj.setPVendor(tempObj.getPVendor());
                    nodebObj.setStationHouseCode(tempObj.getStationHouseCode());
                    nodebObj.setPNodeBTypeId(tempObj.getPNodebTypeId());
                    getSession().save(nodebObj);
                }
            } else if (tempObj.getpApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.CREATED)) {
                //them moi cell
                Cell3gDetailBO cell3gObj = new Cell3gDetailBO();
                cell3gObj.setPAntennaHeight(tempObj.getPAntennaHeight());
                cell3gObj.setPAzimuth(tempObj.getPAzimuth());
                cell3gObj.setPTiltTotal(tempObj.getPTitlTotal());
                cell3gObj.setCell3gId(tempObj.getCellNodebId());

                //kiem tra xem da co bts chua cell nay chua
                String nodebSql = " from CellNodebBO where nodebCode = ? ";
                Query nodebSearch = getSession().createQuery(nodebSql);
                nodebSearch.setParameter(0, nodebId);
                List nodebList = nodebSearch.list();
                if (nodebList == null || nodebList.size() <= 0) {
                    // tao bts moi
                    CellNodebBO nodebObj = new CellNodebBO();
                    nodebObj.setNodebCode(nodebId);
                    nodebObj.setPAntennaType(tempObj.getPAntennaType());
                    nodebObj.setPCabinetNodebType(tempObj.getPCabinetNodebType());
                    nodebObj.setPIntegratedDate(tempObj.getPIntegratedDate());
                    nodebObj.setPInterfaceTransmit(tempObj.getPInterfaceTransmit());
                    nodebObj.setPRruPlaceDetail(tempObj.getPRruPlaceDetail());
                    nodebObj.setPTransmitCapacity(tempObj.getPTransmitCapacity());
                    nodebObj.setPVendor(tempObj.getPVendor());

                    nodebObj.setStationHouseCode(tempObj.getStationHouseCode());
                    getSession().save(nodebObj);
                }


                cell3gObj.setCellNodeb(nodebId);
                getSession().save(cell3gObj);
                tempObj.setpApproveStatus("APPROVED");
            } else if (tempObj.getpApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.REJECT)) {
                // khong duoc approve cell dang reject
            }
        }
    }

    /**
     *
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO showApproveAreaNodeB(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        List listAlarmType = null;
        CellManagementForm cellManagementForm = (CellManagementForm) form;

        if (QueryCryptUtils.getParameter(req, "ajax") != null && !"".equals(QueryCryptUtils.getParameter(req, "ajax").trim())) {
            listAlarmType = getListAreaApproveNodeB(cellManagementForm, req);
            req.setAttribute("listBtsApproveArea", listAlarmType);
            actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_AREA_NODEB);
        } else {
            String provinceCode = "";

            if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
                provinceCode = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString();
            }

            String sql = " from ProvinceBO where areaCode = ?";
            Query q = getSession().createQuery(sql);
            q.setParameter(0, provinceCode);
            req.setAttribute("listProvince", q.list());
            listAlarmType = getListAreaApproveNodeB(cellManagementForm, req);
            req.setAttribute("listBtsApproveArea", listAlarmType);
            actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.SHOW_CELL_APPROVE_AREA_NODEB);
        }
        if (QueryCryptUtils.getParameter(req, "updateStatus") != null && !"".equals(QueryCryptUtils.getParameter(req, "updateStatus").trim())) {
            req.getSession().setAttribute("updateStatus", "Thông qua thŕnh công!");
        }
        return actionResult;
    }

    /**
     *
     * @param form
     * @param req
     * @return
     */
    public List getListAreaApproveNodeB(ActionForm form, HttpServletRequest req) {
        CellManagementForm cellForm = (CellManagementForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        StringBuffer sqlSearchWhere = new StringBuffer();
        sqlSearch.append(" from ViewAreaNotApproveNodeBBO a ");
        List lstPaAdmin = new ArrayList();
        sqlSearchWhere.append(" where 1=1 ");
        if (StringUtils.isNotNull(cellForm.getApproveStatus())) {
            sqlSearchWhere.append(" and nodebAApproveStatus = ? ");
            lstPaAdmin.add(cellForm.getApproveStatus());
        }

        if (StringUtils.isNotNull(cellForm.getVendor())) {
            sqlSearchWhere.append(" and nodebVendor = ?  ");
            lstPaAdmin.add(cellForm.getVendor());
        }

        if (StringUtils.isNotNull(cellForm.getProvinceCode())) {
            sqlSearchWhere.append(" and provinceCode =? ");
            lstPaAdmin.add(cellForm.getProvinceCode());
        }

        String provinceCode = "";
        if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
            provinceCode = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString();
        }

        sqlSearchWhere.append(" and lower(kvCode) = ? ");
        lstPaAdmin.add(provinceCode.toLowerCase());
//        sqlSearchWhere.append("  ORDER BY provinceName ");

        HttpSession httpSession = req.getSession();

        if ((req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE)) != null) {
            String role = httpSession.getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();

            //Truong hop chuc danh user la nhom truong: Loc theo group_id
            if (role.equals(Constant.ASSSIGN_TASK.LEADER)) {
                Long groupIdLogin = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.GROUP_ID);

                sqlSearchWhere.append(" and groupId =  ? ");
                lstPaAdmin.add(groupIdLogin);
            }
            //Truong hop chuc danh user la nhan vien: Loc theo staff_id
            if (role.equals(Constant.ASSSIGN_TASK.STAFF)) {
                Long staffId = (Long) httpSession.getAttribute(Constant.ASSSIGN_TASK.STAFF_ID);
                sqlSearchWhere.append(" and staffId =  ? ");
                lstPaAdmin.add(staffId);
            }
        }
        
        StringBuffer sqlAdmin = new StringBuffer();
        sqlAdmin.append(sqlSearch);
        sqlAdmin.append(sqlSearchWhere);

        Query q = hibernateSession.createQuery(sqlAdmin.toString());
        for (int i = 0; i < lstPaAdmin.size(); i++) {
            q.setParameter(i, lstPaAdmin.get(i));
        }
        List listProvince = q.list();
        return listProvince;
    }

    /**
     *
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO approveAreaLevelNodeB(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();

        CellManagementForm cellManagementForm = (CellManagementForm) form;
        String[] checkRows = cellManagementForm.getCurrentIdArray();

        for (String id : checkRows) {
            String[] ids = id.split(",");

            // xet cac bts
            if (ids.length > 1 && !ids[0].equals("") && !ids[1].equals("")) {
                updateNodebArea(ids[0], ids[1]);
            }
        }

        req.getSession().setAttribute("result", "Thông qua thŕnh công!");

        actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_AREA_NODEB);
        return actionResult;
    }

    /**
     *
     * @param nodebCode
     */
    private void updateNodebArea(String cellNodebId, String nodebId) {
        String cellSql = " from CellNodebNotApproveBO where cellNodebId =? and aApproveStatus != ?";
        Query qCell = getSession().createQuery(cellSql);
        qCell.setParameter(0, cellNodebId);
        qCell.setParameter(1, "APPROVED");
        List cellNodebList = qCell.list();
        if (cellNodebList != null && cellNodebList.size() > 0) {
            CellNodebNotApproveBO tempObj = (CellNodebNotApproveBO) cellNodebList.get(0);

            // xet trang thai can approve
            if (tempObj.getaApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.DELETED)) {
                // xoa ca trong bang tam va trong bang that
                String cellSearch = " from Cell3gDetailBO where cell3gId =? ";
                Query qCellSearch = getSession().createQuery(cellSearch);
                qCellSearch.setParameter(0, cellNodebId);
                List listCell = qCellSearch.list();
                if (listCell != null && listCell.size() > 0) {
                    Cell3gDetailBO cellObj = (Cell3gDetailBO) listCell.get(0);
                    getSession().delete(cellObj);

                    // neu nodeb khong con cell nao nua thi xoa
                    String btsToDelete = " from Cell3gDetailBO where cell3gId = ? ";
                    Query qDelete = getSession().createQuery(btsToDelete);
                    qDelete.setParameter(0, nodebId);
                    List listBtsDelete = qDelete.list();
                    if (listBtsDelete == null || listBtsDelete.size() == 0) { // khong con cell nao thuoc nodeb nay
                        String btsSearch = " from CellNodebBO where nodebCode = ? ";
                        Query queryBts = getSession().createQuery(btsSearch);
                        queryBts.setParameter(0, nodebId);
                        List lstBts = queryBts.list();
                        if (lstBts != null & lstBts.size() > 0) {
                            CellNodebBO obj = (CellNodebBO) lstBts.get(0);
                            getSession().delete(obj);
                        }
                    }
                }

                //xoa trong bang tam
                tempObj.setaApproveStatus("APPROVED");
            } else if (tempObj.getaApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.UPDATED)) {

                // lay tu bang that ra
                String cellSearch = " from Cell3gDetailBO where cell3gId =? ";
                Query qCellSearch = getSession().createQuery(cellSearch);
                qCellSearch.setParameter(0, cellNodebId);
                List listCell = qCellSearch.list();
                if (listCell != null && listCell.size() > 0) {
                    Cell3gDetailBO cellObj = (Cell3gDetailBO) listCell.get(0);
                    //cap nhat lai thong tin cell
                    cellObj.setACi(tempObj.getACi());
                    cellObj.setASc(tempObj.getASc());

                    getSession().save(cellObj);
                }

                //chuyen trang thai trong bang tam
                tempObj.setaApproveStatus("APPROVED");

                // cap nhat thong tin cho nodeb
                String nodebSql = " from CellNodebBO where nodebCode = ? ";
                Query nodebSearch = getSession().createQuery(nodebSql);
                nodebSearch.setParameter(0, nodebId);
                List nodebList = nodebSearch.list();
                if (nodebList != null && nodebList.size() > 0) {
                    CellNodebBO nodebObj = (CellNodebBO) nodebList.get(0);
                    //set cac gia tri cho cell
                    nodebObj.setABandUse(tempObj.getABandUse());
                    nodebObj.setALac(tempObj.getALac());
                    nodebObj.setAMscCode(tempObj.getAMscCode());
                    nodebObj.setARac(tempObj.getARac());
                    nodebObj.setARncCode(tempObj.getARncCode());
                    nodebObj.setARncId(tempObj.getARncId());
                    nodebObj.setASgsn(tempObj.getASgsn());
                    nodebObj.setAStationStatusId(tempObj.getAStationStatusId());
                    nodebObj.setAVendorMsc(tempObj.getAVendorMsc());
                    nodebObj.setAVendorRnc(tempObj.getAVendorRnc());
                    nodebObj.setAVendorSgsn(tempObj.getAVendorSgsn());
                    nodebObj.setStationHouseCode(tempObj.getStationHouseCode());

                    getSession().save(nodebObj);
                }
            } else if (tempObj.getaApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.CREATED)) {
                //them moi cell
                Cell3gDetailBO cell3gObj = new Cell3gDetailBO();
                //set gia tri cua cell cho area
                cell3gObj.setCell3gId(tempObj.getCellNodebId());
                cell3gObj.setACi(tempObj.getACi());
                cell3gObj.setASc(tempObj.getASc());

                //kiem tra xem da co nodeb chua cell nay chua
                String nodebSql = " from CellNodebBO where nodebCode = ? ";
                Query nodebSearch = getSession().createQuery(nodebSql);
                nodebSearch.setParameter(0, nodebId);
                List nodebList = nodebSearch.list();
                if (nodebList == null || nodebList.size() <= 0) {
                    // tao nodeb moi
                    CellNodebBO nodebObj = new CellNodebBO();
                    nodebObj.setNodebCode(nodebId);
                    nodebObj.setABandUse(tempObj.getABandUse());
                    nodebObj.setALac(tempObj.getALac());
                    nodebObj.setAMscCode(tempObj.getAMscCode());
                    nodebObj.setARac(tempObj.getARac());
                    nodebObj.setARncCode(tempObj.getARncCode());
                    nodebObj.setARncId(tempObj.getARncId());
                    nodebObj.setASgsn(tempObj.getASgsn());
                    nodebObj.setAStationStatusId(tempObj.getAStationStatusId());
                    nodebObj.setAVendorMsc(tempObj.getAVendorMsc());
                    nodebObj.setAVendorRnc(tempObj.getAVendorRnc());
                    nodebObj.setAVendorSgsn(tempObj.getAVendorSgsn());
                    nodebObj.setStationHouseCode(tempObj.getStationHouseCode());
                    getSession().save(nodebObj);
                }


                cell3gObj.setCellNodeb(nodebId);
                getSession().save(cell3gObj);

                //doi trang thai sau khi approved
                tempObj.setaApproveStatus("APPROVED");
            } else if (tempObj.getaApproveStatus().equals(Constant.CELL_MANAGEMENT.CELL_STATUS.CREATED)) {
                // khong duoc approve cell dang reject
            }
        }
    }

    /**
     *
     * @param form
     * @param req
     * @return
     * @throws Exception
     */
    public ActionResultBO rejectAreaLevelNodeB(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        List listAlarmType = null;
        CellManagementForm cellManagementForm = (CellManagementForm) form;
        String[] checkRows = cellManagementForm.getCurrentIdArray();
        String sqlNodeB = " from CellNodebNotApproveBO where cellNodebId = ? ";
        Query qNodeB = getSession().createQuery(sqlNodeB);
        for (String id : checkRows) {
            // reject cac nodeb
            if (id.split(",")[0] != null && !id.split(",")[0].equals("")) {
                qNodeB.setParameter(0, id.split(",")[0]);
                List lst = qNodeB.list();
                if (lst != null && lst.size() > 0) {
                    CellNodebNotApproveBO obj = (CellNodebNotApproveBO) lst.get(0);
                    obj.setaApproveStatus(Constant.CELL_MANAGEMENT.CELL_STATUS.REJECT);
                    getSession().save(obj);
                }
            }
        }
        req.getSession().setAttribute("result", "Reject thŕnh công!");
        listAlarmType = getListAreaApprove(cellManagementForm, req);
        req.setAttribute("listBtsApproveProvince", listAlarmType);
        actionResult.setPageForward(Constant.CELL_MANAGEMENT.CELL_APPROVED.LIST_CELL_APPROVE_AREA_NODEB);
        return actionResult;
    }
}

