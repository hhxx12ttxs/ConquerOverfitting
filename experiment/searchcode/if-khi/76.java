package com.lifetek.netmosys.database.DAO;

import com.lifetek.netmosys.database.BO.UserFullBO;
import com.lifetek.netmosys.database.BO.UserToken;
import com.lifetek.netmosys.util.Constant;
import com.lifetek.netmosys.util.ResourceBundleUtils;
import com.lifetek.netmosys.util.StringUtils;
import com.lifetek.database.BO.ActionResultBO;
import com.lifetek.database.config.BaseHibernateDAO;

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lifetek.pp.client.GroupToken;
import lifetek.pp.client.MenuToken;
import lifetek.pp.client.PositionToken;
import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.hibernate.Query;
import org.hibernate.Session;


/**
 * The Class AuthenticateDAO.
 * 
 * @author NOCPRO
 */
public class AuthenticateDAO extends BaseHibernateDAO {

    /** The LOGI n_ er r_ page. */
    private final String LOGIN_ERR_PAGE = "loginError";
    /** The LOGI n_ succes s_ page. */
    private final String LOGIN_SUCCESS_PAGE = "loginSuccess";
    /** The LOGOU t_ page. */
    private final String LOGOUT_PAGE = "logout";
    /** The log. */
    private final Logger log = Logger.getLogger(AuthenticateDAO.class);

    /**
     * Action logout.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws Exception the exception
     */
    public ActionResultBO actionLogout(ActionForm form, HttpServletRequest req) throws Exception {

        log.info("User logout action...");
        log.debug("# Begin method user logout");

        HttpSession session = req.getSession();
        UserToken userToken = (UserToken) session.getAttribute("userToken");
        ActionResultBO actionResult = new ActionResultBO();
        String pageForward = null;

        if (userToken != null) {
            try {

                session.invalidate();
                pageForward = LOGOUT_PAGE;
            } catch (Exception ex) {

                log.info("Error while perform user login action..");
                log.error(ex.getMessage());
                pageForward = "error";
            }
        } else {

            pageForward = "sessionTimeout";
        }

        log.debug("# End method user logout action");
        log.info("User logout has been done!");

        actionResult.setPageForward(pageForward);
        return actionResult;
    }

    /**
     * Action to login System.
     * 
     * @param form the form
     * @param req the req
     * 
     * @return the action result bo
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     */
    public ActionResultBO actionLogin(ActionForm form, HttpServletRequest req) throws Exception {
        Session sessionHibernate = getSession();

        ActionResultBO actionResult = new ActionResultBO();
        ResourceBundleUtils resourceBundleUtils = new ResourceBundleUtils();

        log.info("User login action...");
        log.debug("# Begin method user login");

        req.setAttribute("headerTitle", "QUẢN LÝ LỖI BTS");

        String forwardPage = LOGIN_ERR_PAGE;
        String result = "error";
        UserToken userToken = null;


        try {

            HttpSession session = req.getSession();
            // set default locale equals vietnamese
            if (session.getAttribute(Constant.FLAG_LOCAL) == null || "".equals(session.getAttribute(Constant.FLAG_LOCAL))) {
                session.setAttribute(Globals.LOCALE_KEY, resourceBundleUtils.getDefaultLocale());
                session.setAttribute(Constant.STRUTS_LOCALE, resourceBundleUtils.getDefaultLocale());
            }


            lifetek.pp.client.UserToken vsaUserToken = (lifetek.pp.client.UserToken) session.getAttribute("vsaUserToken");


            if (vsaUserToken != null) {
                if (vsaUserToken.getMenuTokenUnion() != null) {

                    // get resourceBundle according to Locale
                    ResourceBundle resourceBundle = resourceBundleUtils.getResource(req);

                    for (Iterator it = vsaUserToken.getMenuTokenUnion().iterator(); it.hasNext();) {

                        MenuToken menuToken = (MenuToken) it.next();
                        try {
                            menuToken.setModuleName(resourceBundle.getString(menuToken.getModuleId().toString()));
                        } catch (Exception ex) {
                            System.err.println(menuToken.getModuleName());
                            ex.printStackTrace();
                        }
                        if (menuToken.getChildModules() != null && menuToken.getChildModules().size() > 0) {
                            for (Iterator it2 = menuToken.getChildModules().iterator(); it2.hasNext();) {
                                MenuToken menuToken2 = (MenuToken) it2.next();
                                try {
                                    menuToken2.setModuleName(resourceBundle.getString(menuToken2.getModuleId().toString()));


                                    if (menuToken2.getChildModules() != null && menuToken2.getChildModules().size() > 0) {
                                        for (Iterator it3 = menuToken2.getChildModules().iterator(); it3.hasNext();) {
                                            MenuToken menuToken3 = (MenuToken) it3.next();
                                            try {
                                                menuToken3.setModuleName(resourceBundle.getString(menuToken3.getModuleId().toString()));
                                                if (menuToken3.getChildModules() != null && menuToken3.getChildModules().size() > 0) {
                                                    for (Iterator it4 = menuToken3.getChildModules().iterator(); it4.hasNext();) {
                                                        MenuToken menuToken4 = (MenuToken) it4.next();
                                                        try {
                                                            menuToken4.setModuleName(resourceBundle.getString(menuToken4.getModuleId().toString()));
                                                        } catch (Exception ex) {
                                                            System.err.println(menuToken4.getModuleName());
                                                            ex.printStackTrace();
                                                        }

                                                    }
                                                }
                                            } catch (Exception ex) {
                                                System.err.println(menuToken3.getModuleName());
                                                ex.printStackTrace();
                                            }

                                        }
                                    }


                                } catch (Exception ex) {
                                    System.err.println(menuToken2.getModuleName());
                                    ex.printStackTrace();
                                }

                            }
                        }
                    }
                }


                userToken = new UserToken();
                userToken.setUserID(vsaUserToken.getUserID());
                userToken.setLoginName(vsaUserToken.getUserName());
                userToken.setFullName(vsaUserToken.getFullName());
                req.getSession().setAttribute("Address", req.getRemoteAddr());
                if (!getUserProvince(vsaUserToken.getUserName(), req)) {
                    req.setAttribute("errorMess", "Không lấy được mã tỉnh của user đăng nhập. Vui lòng liên lạc với người quản trị user để gán mã tỉnh cho user trước khi sử dụng");
                    forwardPage = result;
                    req.getSession().setAttribute("errorCode", 8686);
                    actionResult.setPageForward(forwardPage);
                    return actionResult;
                }
                if (vsaUserToken.getGroupTokenSize() > 1) {
                    forwardPage = LOGIN_ERR_PAGE;
                } else {
                    forwardPage = LOGIN_SUCCESS_PAGE;
                    GroupToken groupToken = (GroupToken) vsaUserToken.getGroupToken().iterator().next();
                    PositionToken positionToken = groupToken.getPositionToken();


                    userToken.setGroupID(groupToken.getGroupId());
                    userToken.setRoleID(positionToken.getPositionId());
                    userToken.setGroupLevel(groupToken.getLevel());
                    userToken.setRoleLevel(positionToken.getLevel());

                    userToken.setBelongToManyGroup(false);
                    session.setAttribute("isValidate", "true");
                    session.setAttribute("userToken", userToken);
                    session.setAttribute(Constant.COMMON.USER_LOGIN, userToken.getLoginName());

                    result = "success";
                }
            }
        } catch (Exception ex) {

            log.info("Error while perform user login action..");
            ex.printStackTrace();
            log.error(ex.getMessage());
            forwardPage = "error";
        } finally {
            //-------- save log
        }
        log.debug("# End method user login action");
        log.info("User login has been done!");

        actionResult.setPageForward(forwardPage);
        return actionResult;

    }

    /**
     * Set province for user to process.
     * 
     * @param userName the user name
     * @param req the req
     * 
     * @return the user province
     * 
     * @throws java.lang.Exception      * @throws Exception the exception
     */
    public boolean getUserProvince(String userName, HttpServletRequest req) throws Exception {

        //Tim danh sach dich vu
//        Query query = null;
        Session session = getSession();
//        List lstUser = new ArrayList();
        try {
            String provinceCode = "";
            String provinceName = "";

            if (StringUtils.isNotNull(userName)) {
                Query qUser = session.createQuery("from UserFullBO where lower(userLogin) = ? and isEnable =1 ");
                qUser.setParameter(0, userName.trim());

                List userList = qUser.list();

                if (userList != null && userList.size() > 0) {
                    UserFullBO userFullBO = (UserFullBO) userList.get(0);
                    if (userFullBO != null) {
                        provinceCode = userFullBO.getAreaCode();

                        req.getSession().setAttribute(Constant.ASSSIGN_TASK.GROUP_ID, userFullBO.getGroupId());
                        req.getSession().setAttribute(Constant.ASSSIGN_TASK.STAFF_ID, userFullBO.getStaffId());

                        if (Constant.ROLE_TYPE.MANAGER_GROUP.equals(userFullBO.getType())) {
                            req.getSession().setAttribute(Constant.ASSSIGN_TASK.ROLE, Constant.ASSSIGN_TASK.ADMIN);
                        } else if (Constant.ROLE_TYPE.LEADER_GROUP.equals(userFullBO.getType())) {
                            req.getSession().setAttribute(Constant.ASSSIGN_TASK.ROLE, Constant.ASSSIGN_TASK.LEADER);
                        } else if (Constant.ROLE_TYPE.STAFF_GROUP.equals(userFullBO.getType())) {
                            req.getSession().setAttribute(Constant.ASSSIGN_TASK.ROLE, Constant.ASSSIGN_TASK.STAFF);
                        }  else if (Constant.ROLE_TYPE.TTDHKT_GROUP.equals(userFullBO.getType())) {
                          
                        } else if (Constant.ROLE_TYPE.KV_GROUP.equals(userFullBO.getType())) {
                           
                        }  else if (Constant.ROLE_TYPE.TTDHKT_ADMIN_GROUP.equals(userFullBO.getType())) {
                            req.getSession().setAttribute(Constant.COMMON.AREA_CODE_KV.SUPPER_USER, Constant.COMMON.AREA_CODE_KV.SUPPER_USER);
                             req.getSession().setAttribute(Constant.COMMON.USER_ROLE_CODE, provinceCode);
                        } else if (Constant.ROLE_TYPE.KV_ADMIN_GROUP.equals(userFullBO.getType())) {
                            req.getSession().setAttribute(Constant.COMMON.AREA_CODE_KV.KV_ADMIN, Constant.COMMON.AREA_CODE_KV.KV_ADMIN);
                            req.getSession().setAttribute(Constant.COMMON.USER_ROLE_CODE, provinceCode);
                        } else {
                            req.getSession().setAttribute(Constant.ASSSIGN_TASK.ROLE_TTDHKT_KV, provinceCode);
                        }
                        req.getSession().setAttribute(Constant.COMMON.USER_ROLE_CODE, provinceCode);
//                        if (StringUtils.checkSuperUser(provinceCode)) {
//                            String superUser = provinceCode;
//                            provinceCode = Constant.COMMON.ADMIN_TTDHKT;
//                            req.getSession().setAttribute(Constant.COMMON.AREA_CODE_KV.SUPPER_USER, superUser);
//                            req.getSession().setAttribute(Constant.COMMON.USER_ROLE_CODE, superUser);
//                        }


//                        if (StringUtils.checkTktuUser(provinceCode)) {
//                            String tktu = provinceCode;
//                            provinceCode = Constant.COMMON.ADMIN_TTDHKT;
//                            req.getSession().setAttribute(Constant.COMMON.AREA_CODE_KV.TKTU, tktu);
//                            req.getSession().setAttribute(Constant.COMMON.USER_ROLE_CODE, tktu);
//                        }

//                        if (StringUtils.checkAdminArea(provinceCode)) {
//                            String tktu = provinceCode;
//                            provinceCode = provinceCode.substring(0, 3);
//                            req.getSession().setAttribute(Constant.COMMON.AREA_CODE_KV.KV_ADMIN, tktu);
//                            req.getSession().setAttribute(Constant.COMMON.USER_ROLE_CODE, tktu);
//                        }

                        if (provinceCode != null) {//co ma tinh

                            provinceName = userFullBO.getProvinceName();
                        }
                    } else {
                        provinceCode = "no";
                    }
                    req.getSession().setAttribute("ProvinceCode", provinceCode);
                    try {
                        String hasKv = ResourceBundleUtils.getConfigResource().getString("NOT_HAS_KV");
                        if (hasKv != null && "TRUE".equals(hasKv.toUpperCase())) {
                            req.getSession().setAttribute(Constant.COMMON.NOT_HAS_KV, Constant.COMMON.NOT_HAS_KV);
                        }

                    } catch (Exception e) {
                    }


                    req.getSession().setAttribute("ProvinceName", provinceName);
                    req.getSession().setAttribute("fullName1", userFullBO.getName());
                    req.getSession().setAttribute("userLogin", userFullBO.getUserLogin());
                } else {
                    return false;
                }

            } else {
                return false;
            }

//            String SELECT_PROVINCE = "From UsersBO where loginName = ?";
//            String provinceCode = "";
//            String provinceName = "";
//
//
//            query = session.createQuery(SELECT_PROVINCE);
//            query.setParameter(0, userName.trim().toLowerCase());
//            lstUser = query.list();
//            if (lstUser.size() > 0) {
//
//                UsersBO user = (UsersBO) lstUser.get(0);
//                provinceCode = user.getProvinceCode();
//
//                if (provinceCode != null) {//co ma tinh
//
//                    req.getSession().setAttribute("ProvinceCode", provinceCode);
//                    String SELECT_PROVINCE_NAME = "from AreaBO where areaCode= ?";
//                    Query q = session.createQuery(SELECT_PROVINCE_NAME);
//                    q.setParameter(0, provinceCode);
//                    List lst = q.list();
//                    if (lst.size() > 0) {
//
//                        AreaBO area = (AreaBO) lst.get(0);
//                        provinceName = area.getProvinceName();
//                    }
//
//                } else {
//
//                    provinceCode = "no";
//                }
//                req.getSession().setAttribute("ProvinceCode", provinceCode);
//                req.getSession().setAttribute("ProvinceName", provinceName);
//            } else {
//                return false;
//            }

        } catch (Exception ex) {

            ex.printStackTrace();
            return false;
        }
        return true;

    }
}

