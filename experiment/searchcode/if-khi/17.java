package com.lifetek.netmosys.database.DAO;

import com.lifetek.netmosys.client.form.BtsForm;
import com.lifetek.netmosys.client.form.UpgradeErrorForm;

import com.lifetek.netmosys.database.BO.UpgradeErrorBO;
import com.lifetek.netmosys.database.BO.AreaBO;
import com.lifetek.netmosys.database.BO.BtsBO;
import com.lifetek.netmosys.util.Constant;
import com.lifetek.netmosys.util.QueryCryptUtils;
import com.lifetek.database.BO.ActionResultBO;

import com.lifetek.database.config.BaseHibernateDAO;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.hibernate.Query;


/**
 * The Class UpgradeErrorDAO.
 */
public class UpgradeErrorDAO extends BaseHibernateDAO {

    /*Các trang c?n forward sau khi x? lý theo công ngh? strusts - hibernate*/
    //Hi?n tr? danh sách các tr?m bts c?n qu?n lý
    /** The VIE w_ bs c_ servers. */
    private final String VIEW_BSC_SERVERS = "viewError";//Danh sach cac node BSC

    //Hi?n tr? danh sách các tr?m bts c?n cau hěnh tęn
    /** The SEARC h_ bs c_ servers. */
    private final String SEARCH_BSC_SERVERS = "listError";//Tim kiem danh sach node BSC
    
    /** The EDI t_ bs c_ servers. */
    private final String EDIT_BSC_SERVERS = "editError";//EDIT BSC

    //message thong bao
    /** The ERRO r_ inf o_ logi n_ bt s_ config. */
    private final String ERROR_INFO_LOGIN_BTS_CONFIG =
            "B?n không có quy?n qu?n lý danh sách các tr?m BTS.";

    /**
     * Bat dau load trang quan ly loi.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     */
    public ActionResultBO viewError(ActionForm form, HttpServletRequest req)
            throws Exception {
        //L?p x? lý action
        ActionResultBO actionResult = new ActionResultBO();
        //Trang forward sau khi x? lý
        String pageForwar = VIEW_BSC_SERVERS;
        UpgradeErrorForm upgradeErrorForm = (UpgradeErrorForm) form;

        try {
            //Check permission for user
            String strProvinceReq = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString();//Thuoc tinh request
            if (strProvinceReq != null) {//Ton tai user
                /*Hien thi thong tin tim kiem*/
                req.setAttribute("nullData", "1");
            } else {//error info user login
                upgradeErrorForm.setResult(ERROR_INFO_LOGIN_BTS_CONFIG);
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
     * Danh sách các l?i.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     */
    public ActionResultBO searchError(ActionForm form,
            HttpServletRequest req) throws
            Exception {
        //L?p x? lý action
        ActionResultBO actionResult = new ActionResultBO();
        //Cau lenh truy van du lieu
        StringBuffer sqlCurrent = new StringBuffer();
        //Trang forward sau khi x? lý
        String pageForwar = SEARCH_BSC_SERVERS;//mac dinh la trang reload
        //form UpgradeError
        UpgradeErrorForm upgradeErrorForm = (UpgradeErrorForm) form;
        try {
            //Check permission for user
            String strProvinceReq = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString();
            if (strProvinceReq != null) {//exists user
                //Check reload page
                if (QueryCryptUtils.getParameter(req, "ajax") == null) {//load lan dau
                    pageForwar = VIEW_BSC_SERVERS;
                }
                /*Lay du lieu de hien thi*/
                sqlCurrent.append(" from UpgradeErrorBO ");//Cau lenh truy van
                int i = 0;//bien dem
                List lst = null;//Ket qua tra ve
                //Cac dieu kien loc
                String typeCentral = upgradeErrorForm.getTypeCentral();//Tong dai
                Long faultID = upgradeErrorForm.getFaultId();
                //Check reload
                if (QueryCryptUtils.getParameter(req, "typeCentral") != null) {//load lan dau
                    typeCentral = QueryCryptUtils.getParameter(req, "typeCentral").toString();
                }
//                if (QueryCryptUtils.getParameter(req, Long.toString(faultID)) != null) {//load lan dau
//                    faultID = Long.parseLong(QueryCryptUtils.getParameter(req, Long.toString(faultID)));
//                }
                //Chuan hoa dieu kien loc
//                if (typeCentral != null) {
//                    if (typeCentral.equals("0")) {
//                        typeCentral = null;
//                    }
//                }
                //Lay cac dieu kien loc
                boolean blnFirst = true;
                if (typeCentral != null && typeCentral.trim().length() > 0) {
                    sqlCurrent.append(" where exchange=? and status = 1");
                    blnFirst = false;
                } else {
                    //upgradeErrorForm.setResult("B?n ph?i ch?n t?ng ?ŕi c?n těm !");
                    sqlCurrent.append(" where status = 1");
                    blnFirst = false;
                }
                //order by
                //sqlCurrent.append(" order by faultId");
                Query q = getSession().createQuery(sqlCurrent.toString());
                //lay cac tham so
                if (typeCentral != null && !typeCentral.equals("")) {
                    q.setParameter(i++, typeCentral);
                }
                //get data
                lst = q.list();
                //set attribute
                req.setAttribute("listBsc", lst);
                //Check count result
                if (lst.size() > 0) {
                    req.setAttribute("nullData", "1");
                }
            } else {//error info user login
                upgradeErrorForm.setResult(ERROR_INFO_LOGIN_BTS_CONFIG);
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
     * Kh?i t?o vi?c x? lý loi
     * C?p nh?t, thęm m?i.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     */
    public ActionResultBO preparePageError(ActionForm form,
            HttpServletRequest req) throws
            Exception {
        //Khai báo Action
        ActionResultBO actionResult = new ActionResultBO();
        try {
            //Khai báo form ch?a d? li?u
            UpgradeErrorForm upgradeErrorForm = (UpgradeErrorForm) form;
            //Khai báo trang forward
            String pageForward = EDIT_BSC_SERVERS;//Cap nhat
            //Ma node bsc
            if (QueryCryptUtils.getParameter(req, "faultId") != null) {
                upgradeErrorForm.setFaultId(Long.parseLong(QueryCryptUtils.getParameter(req, "faultId")));
                Long faultId = Long.parseLong(QueryCryptUtils.getParameter(req, "faultId"));
                List lstBscBO = findByProperty(UpgradeErrorBO.class.getName(), "faultId", faultId);
                if (lstBscBO != null) {//Exists
                    UpgradeErrorBO upgradeErrorBO = (UpgradeErrorBO) lstBscBO.get(0);
                    upgradeErrorForm.setExchange(upgradeErrorBO.getExchange());
                    upgradeErrorForm.setKeyword(upgradeErrorBO.getKeyword());
                    upgradeErrorForm.setType(Constant.TYPE_BSC_EDIT);
                }
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

        ActionResultBO actionResult = new ActionResultBO();
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
                    req.setAttribute("groupList", new AddressDAO().getGroup(
                            provinceCode));
                } else {
                    String SQL = "from AreaBO where parentCode is null";
                    Query q = getSession().createQuery(SQL);
                    List lst = q.list();
                    req.setAttribute("KvList", lst);
                }
            } else {
                btsForm.setResult(
                        "B?n không có quy?n c?p nh?p danh m?c tr?n BTS, Lięn h? v?i ng??i qu?n lý user ?? ???c c?p quy?n.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            actionResult.setPageForward("error");
            return actionResult;
        }
        return actionResult;
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
        String pageForward = "BtsSuccess";
        ActionResultBO actionResult = new ActionResultBO();
        actionResult.setPageForward(pageForward);
        BtsForm btsForm = (BtsForm) form;
        try {
            String provinceCode = null;
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
                btsForm.setResult(
                        "B?n không có quy?n c?p nh?p danh m?c tr?n BTS, Lięn h? v?i ng??i qu?n lý user ?? ???c c?p quy?n.");
                return actionResult;
            }


            if (btsForm.getBtsId() != null && !btsForm.getBtsId().equals("")) {
                String SQL = "from BtsBO where btsId= ? ";
                Query q = getSession().createQuery(SQL);
                q.setParameter(0, btsForm.getBtsId().trim());
                List lstResult = q.list();
                if (lstResult.size() > 0) {
                    BtsBO bts = (BtsBO) lstResult.get(0);
                    String areaName = "";
                    if (bts.getAreaCode() != null) {
                        btsForm.setAreaCode(bts.getAreaCode());
                        String GET_AREA = "from AreaBO where areaCode= ? ";
                        Query query = getSession().createQuery(GET_AREA);
                        query.setParameter(0, bts.getAreaCode());
                        List lst = query.list();
                        if (lst.size() > 0) {
                            AreaBO area = (AreaBO) lst.get(0);
                            areaName = area.getProvinceName();
                            if (provinceCode != null && "ttdhkt".equals(
                                    provinceCode)) {
                                if (area.getProvinceCode() != null) {
                                    req.setAttribute("groupList",
                                            new AddressDAO().getGroup(
                                            area.getAreaCode()));
                                }

                                if (area.getKvCode() != null) {
                                    req.setAttribute("provinceList",
                                            new AddressDAO().getProvince(area.getKvCode()));
                                }

                                btsForm.setKvCode(area.getKvCode());
                                btsForm.setProvinceCode(area.getAreaCode());
                            }
                        }

                    }
                    //BTS thuoc pham vi quan ly cua tinh khac
                    if (bts.getAreaCode() != null && provinceCode != null &&
                            !"ttdhkt".equals(provinceCode) && !bts.getAreaCode().
                            equals(provinceCode)) {
                        btsForm = clearForm(btsForm);
                        btsForm.setResult(
                                "Tr?m " + btsForm.getBtsId() +
                                " hi?n t?i ?ang do t?nh " + areaName +
                                " qu?n lý. B?n không có qu?n c?p nh?p thông tin tr?m c?a t?nh khác.");
                        return actionResult;
                    }
                    btsForm.setBscId(bts.getBscId());
                    btsForm.setAddress(bts.getAddress());
                    btsForm.setDistance(bts.getDistance());
                    btsForm.setGroupId(bts.getGroupId());
                    if (bts.getGroupId() != null) {
                        req.setAttribute("staffList", new AddressDAO().getStaff(bts.getGroupId().
                                toString()));
                    }

                    btsForm.setNote(bts.getNote());
                    btsForm.setStaffId(bts.getStaffId());
                } else {
                    btsForm.setResult(
                            "Ch?a t?n t?i tr?m có mă s? " + btsForm.getBtsId() +
                            " tręn h? th?ng, nh?p m?i các thông tin tr?m.");
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
     * Adds the error.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO AddError(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = EDIT_BSC_SERVERS;
        actionResult.setPageForward(pageForward);
        UpgradeErrorForm upgradeErrorForm = (UpgradeErrorForm) form;
        //Set type
        upgradeErrorForm.setType(Constant.TYPE_BSC_ADD);
        try {
            if (req.getSession().getAttribute("ProvinceCode") != null) {//co user
                //Long faultSearchId = upgradeErrorForm.getFaultSearchId();
                Long faultId = upgradeErrorForm.getFaultId();
                String keyword = upgradeErrorForm.getKeyword();
                String exchange = upgradeErrorForm.getExchange();//Tong dai
                Long status = upgradeErrorForm.getStatus();
                //Check
                //List lstBscExist=findByProperty(UpgradeErrorBO.class.getName(), "faultId", faultId);
                //if(lstBscExist.size()>0)
                //{//Exists
                //upgradeErrorForm.setResult("L?i nŕy ?ă t?n t?i !");
                //req.getSession().setAttribute("faultType", "1");
                //}else{
                UpgradeErrorBO upgradeErrorBO = new UpgradeErrorBO();
                //set value
                upgradeErrorBO.setFaultId(faultId);
                upgradeErrorBO.setKeyword(keyword);
                upgradeErrorBO.setExchange(exchange);
                upgradeErrorBO.setStatus(status);
                //add new
                save(upgradeErrorBO);
                //return value
                upgradeErrorForm.setResult("Thęm m?i thŕnh công l?i !");
                req.getSession().setAttribute("faultType", "0");
            //}
            } else {
                upgradeErrorForm.setResult("User ??ng nh?p không có quy?n giao vi?c.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            upgradeErrorForm.setResult("L?i !");
        }
        return actionResult;
    }

    /**
     * Update error.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO UpdateError(ActionForm form,
            HttpServletRequest req) throws
            Exception {

        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = EDIT_BSC_SERVERS;
        actionResult.setPageForward(pageForward);
        //form log server
        UpgradeErrorForm upgradeErrorForm = (UpgradeErrorForm) form;
        //Set type
        upgradeErrorForm.setType(Constant.TYPE_BSC_EDIT);
        try {
            if (req.getSession().getAttribute("ProvinceCode") != null) {//co user
                Long faultId = upgradeErrorForm.getFaultId();
                String keyword = upgradeErrorForm.getKeyword();//user name
                //String userName=upgradeErrorForm.getUserName();//user name
                //String passWord=upgradeErrorForm.getPassWord();//password
                String exchange = upgradeErrorForm.getExchange();//Tong dai
                //Lay ra node Bsc
                List lstBscBO = findByProperty(UpgradeErrorBO.class.getName(), "faultId", faultId);
                if (lstBscBO != null) {//Exists
                    UpgradeErrorBO upgradeErrorBO = (UpgradeErrorBO) lstBscBO.get(0);
                    //set value
                    upgradeErrorBO.setKeyword(keyword);
                    //upgradeErrorBO.setUserName(userName);
                    //upgradeErrorBO.setPassWord(passWord);
                    upgradeErrorBO.setExchange(exchange);
                    //update
                    update(upgradeErrorBO);
                    //return value
                    upgradeErrorForm.setResult("C?p nh?t thŕnh công!");
                } else {
                    upgradeErrorForm.setResult("Có l?i x?y ra trong khi c?p nh?t");
                }
            } else {
                upgradeErrorForm.setResult("User ??ng nh?p không có quy?n giao vi?c.");
            }
        } catch (Exception e) {

            upgradeErrorForm.setResult("Có l?i x?y ra trong khi c?p nh?t!");
        }
        return actionResult;
    }

    /**
     * Delete error.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO deleteError(ActionForm form,
            HttpServletRequest req) throws
            Exception {
        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = SEARCH_BSC_SERVERS;
        actionResult.setPageForward(pageForward);
        UpgradeErrorForm upgradeErrorForm = (UpgradeErrorForm) form;
        //Set type
        upgradeErrorForm.setType(Constant.TYPE_BSC_DELETE);
        try {
            if (req.getSession().getAttribute("ProvinceCode") != null) {//co user
                Long faultId = req.getParameter("faultId") != null ? Long.parseLong(req.getParameter("faultId")) : null;
                List lstBscBO = findByProperty(UpgradeErrorBO.class.getName(), "faultId", faultId);
                if (lstBscBO != null && lstBscBO.size() > 0) {//Exists
                    UpgradeErrorBO upgradeErrorBO = (UpgradeErrorBO) lstBscBO.get(0);
                    //set value
                    upgradeErrorBO.setStatus(0L);
                    update(upgradeErrorBO);
                    //return value
                    upgradeErrorForm.setResult("C?p nh?t thŕnh công!");

                } else {
                    upgradeErrorForm.setResult("Có l?i x?y ra trong khi c?p nh?t");
                }
            } else {
                upgradeErrorForm.setResult("User ??ng nh?p không có quy?n giao vi?c.");
            }
        } catch (Exception e) {
            upgradeErrorForm.setResult("Có l?i x?y ra trong khi c?p nh?t!");
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
}

