package com.lifetek.netmosys.database.DAO;

import com.lifetek.netmosys.client.form.CellProvinceForm;
import com.lifetek.netmosys.database.BO.AreaBO;

import com.lifetek.netmosys.database.BO.AreaView;

import com.lifetek.netmosys.database.BO.BtsBO;
import com.lifetek.netmosys.database.BO.DistrictBO;
import com.lifetek.netmosys.database.BO.LevelBtsBO;

import com.lifetek.netmosys.database.BO.LogServersBO;
import com.lifetek.netmosys.database.BO.ServerTypeBO;

import com.lifetek.netmosys.database.BO.StaffBO;
import com.lifetek.netmosys.database.BO.ViewCataloguesBO;
import com.lifetek.netmosys.database.g3.BO.Area3GBO;

import com.lifetek.netmosys.database.g3.BO.NodeBBO;

import com.lifetek.netmosys.database.g3.BO.NodeBInGroupBO;
import com.lifetek.netmosys.database.cell.BO.CellDistrictBO;
import com.lifetek.netmosys.database.cell.BO.CellReferenceDataBO;
import com.lifetek.netmosys.database.cell.BO.ProvinceBO;
import com.lifetek.netmosys.database.cell.BO.VStationHouseDetailBO;

import com.lifetek.netmosys.database.common.BO.ComboBO;

import com.lifetek.netmosys.database.common.BO.ReferenceDataBO;
import com.lifetek.netmosys.database.vasin.BO.VasInAlarmTypeBO;

import com.lifetek.netmosys.server.action.ReferenceDataTimerTask;

import com.lifetek.netmosys.server.paginated.CurrentPaginatedList;

import com.lifetek.netmosys.server.paginated.PaginatedDAO;

import com.lifetek.netmosys.util.Constant;

import com.lifetek.netmosys.util.ResourceBundleUtils;

import com.lifetek.netmosys.util.StringUtils;
import com.lifetek.database.BO.ActionResultBO;

import com.lifetek.database.config.BaseHibernateDAO;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import jxl.Sheet;
import jxl.Workbook;
import org.apache.struts.action.ActionForm;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * The Class AddressDAO.
 * 
 * @author ThanhNC
 */
public class AddressDAO extends BaseHibernateDAO {

    public static String createStandardSms(String provinceCode, String errType, String address) {
        return provinceCode + "*" + errType + "*" + address;
    }

    /**
     * Lay ra danh sach khu vuc.
     *
     * @param lst the lst
     *
     * @return the list province kv
     */
    public static List<AreaBO> getListProvinceKV(List lst) {
        List<AreaBO> resultList = new ArrayList<AreaBO>();
        AreaBO areaBO = null;
        for (int i = 0; i
                < lst.size(); i++) {
            Object obj[] = (Object[]) lst.get(i);
            areaBO = new AreaBO();
            if (obj[0] != null) {
                areaBO.setKvCode(obj[0].toString());
            }

            if (obj[1] != null) {
                areaBO.setKvName(obj[1].toString());
            }

            resultList.add(areaBO);
        }

        return resultList;
    }

    public static String getUnloadUser(HttpServletRequest req) {

        String path = req.getRealPath(Constant.COMMON.templateFileName + "Unload_users.xls");
        File fp = new File(path);
        String userTemp = "'";
        if (fp.exists()) {
            try {

                Workbook wb = Workbook.getWorkbook(fp);
                Sheet sheet = wb.getSheet(0);
                int rows = sheet.getRows();

                String userLogin = "";

                for (int row = 0; row < rows; row++) {
                    userLogin = sheet.getCell(0, row).getContents();
                    if (StringUtils.isNotNull(userLogin)) {
                        userTemp += userLogin + "','";
                    }

                }



            } catch (Exception ioe) {
                System.out.println("Error: " + ioe);
            } finally {
//                fp.();
            }
        }
        userTemp += " undefined '";
        return userTemp;
    }

    /**
     * Check not kv.
     *
     * @param name the name
     *
     *  public static boolean checkNotKV(String name) {



    }@return true, if successful
     */
    public static ActionResultBO checkPermission(ActionResultBO actionResult, HttpServletRequest req, String[] permitedrole) {
        boolean isPermited = false;
        String role = null;
        String provinceCode = null;
        String superUser = null;
        if (req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE) != null) {
            role = req.getSession().getAttribute(Constant.ASSSIGN_TASK.ROLE).toString();

        }
        if (req.getSession().getAttribute(Constant.COMMON.AREA_CODE_KV.SUPPER_USER) != null) {
            superUser = req.getSession().getAttribute(Constant.COMMON.AREA_CODE_KV.SUPPER_USER).toString();
        }

        for (int i = 0; i < permitedrole.length; i++) {
            if (permitedrole[i] != null && Constant.ASSSIGN_TASK.TTDHKT.trim().toLowerCase().equals(permitedrole[i].trim().toLowerCase())) {
                if (!StringUtils.isNotNull(role)) {
                    isPermited = true;
                }
            } else if (!StringUtils.isNotNull(role) && StringUtils.isNotNull(provinceCode)
                    && permitedrole[i].trim().toLowerCase().equals(provinceCode.trim().toLowerCase())) {
                isPermited = true;
            } else if (role != null && permitedrole[i] != null && permitedrole[i].trim().toLowerCase().equals(role.trim().toLowerCase())) {
                isPermited = true;
            }
        }

        if (StringUtils.checkSuperUser(superUser)) {
            isPermited = true;
        }

        if (!isPermited) {
            actionResult.setPageForward(Constant.COMMON.UN_AUTHORZIED);
            return actionResult;
        }
        return null;
    }

    /**
     * Find district by province code.
     * 
     * @param kvCode the kv code
     * 
     * @return the province
     */
    public List getProvince(String kvCode) {
        List list = new ArrayList();
        try {
            Session session = getSession();
            Query query = session.createQuery("from AreaBO where parentCode= ? order by provinceName asc");
            query.setParameter(0, kvCode);
            list = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;

    }

    @SuppressWarnings("unchecked")
    public static List getCellProvince(String kvCode) {
        List list = new ArrayList();
        Session session = null;
        session = getSession();
        try {

            if (kvCode == null || "".equals(kvCode) || "-1".equals(kvCode)) {
                Query query = session.createQuery("from ProvinceBO where provinceName is not null  ORDER BY NLSSORT( provinceName,'NLS_SORT=vietnamese')");
                list = query.list();
            } else {
                Query query = session.createQuery("from ProvinceBO where areaCode= ? and provinceName is not null ORDER BY NLSSORT( provinceName,'NLS_SORT=vietnamese')");
                query.setParameter(0, kvCode);
                list = query.list();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return list;

    }

    @SuppressWarnings("unchecked")
    public static List getCellProvinceByProvinceCode(String provinceCode) {
        List list = new ArrayList();
        Session session = null;
        session = getSession();
        try {

            if (provinceCode == null || "".equals(provinceCode) || "-1".equals(provinceCode)) {
                Query query = session.createQuery("from ProvinceBO where provinceName is not null  order by provinceName asc");
                list = query.list();
            } else {
                Query query = session.createQuery("from ProvinceBO where provinceCode= ? and provinceName is not null  ORDER BY NLSSORT(provinceName,'NLS_SORT=vietnamese')");
                query.setParameter(0, provinceCode);
                list = query.list();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return list;

    }

    public static ProvinceBO getCellProvinceByZoneCode(String zoneCode) {
        List list = new ArrayList();
        Session session = null;
        session = getSession();
        ProvinceBO provinceBO = new ProvinceBO();
        try {


            if (StringUtils.isNotNull(zoneCode)) {
                Query query = session.createQuery("select a from ProvinceBO a, CellDistrictBO b where a.provinceCode = b.provinceCode  and b.zoneCode = ? ");
                query.setParameter(0, zoneCode);
                list = query.list();
                if (list != null && list.size() > 0) {
                    provinceBO = (ProvinceBO) list.get(0);
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return provinceBO;

    }

    public static List getCellDistrict(String provinceCode) {
        List list = new ArrayList();
        Session session = null;
        session = getSession();
        try {
            if (StringUtils.isNotNull(provinceCode)) {
                Query query = session.createQuery("from CellDistrictBO where provinceCode= ? and districtName is not null ORDER BY NLSSORT(districtName,'NLS_SORT=vietnamese')");
                query.setParameter(0, provinceCode);
                list = query.list();
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return list;

    }

    public static CellDistrictBO getCellDistrictBOByDistrictCode(String areaCode, String districtCode) {
        List list = new ArrayList();
        Session session = null;
        session = getSession();
        CellDistrictBO cellDistrictBO = null;
        try {
            if (StringUtils.isNotNull(areaCode) && StringUtils.isNotNull(districtCode)) {
                Query query = session.createQuery("from CellDistrictBO where provinceCode= ? and districtCode = ?");
                query.setParameter(0, areaCode);
                query.setParameter(1, districtCode);
                list = query.list();
                if (list != null && list.size() > 0) {
                    cellDistrictBO = (CellDistrictBO) list.get(0);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return cellDistrictBO;

    }

    public static List getCellVillage(String districtCode) {
        List list = new ArrayList();
        Session session = null;
        session = getSession();
        try {
            if (StringUtils.isNotNull(districtCode)) {
                Query query = session.createQuery("from VillageBO where districtCode = ? and villageName is not null order by villageName asc");
                query.setParameter(0, districtCode);
                list = query.list();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return list;

    }

    public static void getLevelBts(HttpServletRequest req) {
        String levelBtsSql = " select distinct a.typeImportant from LevelBtsBO a";

        List<LevelBtsBO> lstTemp = new ArrayList<LevelBtsBO>();
        LevelBtsBO levelBtsBO = null;
        List objLevelBts = getSession().createQuery(levelBtsSql).list();
        if (objLevelBts != null && objLevelBts.size() > 0) {
            for (Long lg : (List<Long>) objLevelBts) {
                levelBtsBO = new LevelBtsBO();
                levelBtsBO.setTypeImportant(lg);
                lstTemp.add(levelBtsBO);
            }
        }

        req.setAttribute("listLevelBts", lstTemp);

    }

    public static void getCataloguesList(HttpServletRequest req) {
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from ViewCataloguesBO where id<>? ");
        Query q = hibernateSession.createQuery(sqlSearch.toString());
        q.setParameter(0, 1L);
        List catalogList = q.list();
        req.setAttribute("cataloguesList", catalogList);

    }

    public static List getDescLevel(HttpServletRequest req, Long typeImportant) {
//        String levelBtsSql = " select a.descLevel from LevelBtsBO a where a.typeImportant=? order by a.descLevel";
//
//        List<LevelBtsBO> lstTempLevel = new ArrayList<LevelBtsBO>();
//        LevelBtsBO levelBtsBO = null;
//        List objLevelBts = getSession().createQuery(levelBtsSql).list();
//        if (objLevelBts != null && objLevelBts.size() > 0) {
//            for (String lg : (List<String>) objLevelBts) {
//                levelBtsBO = new LevelBtsBO();
//                levelBtsBO.setDescLevel(lg);
//                lstTempLevel.add(levelBtsBO);
//            }
//        }
//
//        req.setAttribute("listLevelBts2", lstTempLevel);
//        return(lstTempLevel);
        List lstReturn = new ArrayList();

        try {
            Query query = getSession().createQuery("from LevelBtsBO where typeImportant = ? order by descLevel");
            query.setParameter(0, typeImportant);
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstReturn;
    }

    public static void getLevelNodeB(HttpServletRequest req) {
        String levelBtsSql = " select distinct a.typeImportant from LevelBtsBO a";

        List<LevelBtsBO> lstTemp = new ArrayList<LevelBtsBO>();
        LevelBtsBO levelNodeBBO = null;
        List objLevelBts = getSession().createQuery(levelBtsSql).list();
        if (objLevelBts != null && objLevelBts.size() > 0) {
            for (Long lg : (List<Long>) objLevelBts) {
                levelNodeBBO = new LevelBtsBO();
                levelNodeBBO.setTypeImportant(lg);
                lstTemp.add(levelNodeBBO);
            }
        }

        req.setAttribute("listLevelNodeB", lstTemp);

    }

    public static List getDescLevelNodeB(HttpServletRequest req, Long typeImportant) {
//        String levelBtsSql = " select a.descLevel from LevelBtsBO a where a.typeImportant=? order by a.descLevel";
//
//        List<LevelBtsBO> lstTempLevel = new ArrayList<LevelBtsBO>();
//        LevelBtsBO levelNodeBBO = null;
//        List objLevelBts = getSession().createQuery(levelBtsSql).list();
//        if (objLevelBts != null && objLevelBts.size() > 0) {
//            for (String lg : (List<String>) objLevelBts) {
//                levelNodeBBO = new LevelBtsBO();
//                levelNodeBBO.setDescLevel(lg);
//                lstTempLevel.add(levelNodeBBO);
//            }
//        }
//
//        req.setAttribute("listLevelNodeB2", lstTempLevel);
//        return (lstTempLevel);
        List lstReturn = new ArrayList();

        try {
            Query query = getSession().createQuery("from LevelBtsBO where typeImportant = ? order by descLevel");
            query.setParameter(0, typeImportant);
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstReturn;
    }

    public static List<ComboBO> getListReferenceData(HttpServletRequest req, List<ReferenceDataBO> referenceDataList, String functionName) {

        List<ComboBO> comboList = new ArrayList<ComboBO>();
        ComboBO comboBO = null;
        if (referenceDataList != null && referenceDataList.size() > 0) {
            for (ReferenceDataBO referenceDataBO : referenceDataList) {
                if (referenceDataBO != null && StringUtils.isNotNull(functionName) && StringUtils.isNotNull(referenceDataBO.getFunctionType()) && functionName.equals(referenceDataBO.getFunctionType().trim().toUpperCase())) {
                    comboBO = new ComboBO();
                    if (referenceDataBO.getValueNo() != null) {

                        if (Constant.REFERENCE_DATA.HOUR.equals(referenceDataBO.getValueStr().toUpperCase())) {
                            comboBO.setValueNo(Long.valueOf(referenceDataBO.getValueNo() * 60 * 60));
                        } else if (Constant.REFERENCE_DATA.MINUTE.equals(referenceDataBO.getValueStr().toUpperCase())) {
                            comboBO.setValueNo(Long.valueOf(referenceDataBO.getValueNo() * 60));
                        } else if (Constant.REFERENCE_DATA.SECOND.equals(referenceDataBO.getValueStr().toUpperCase())) {
                            comboBO.setValueNo(Long.valueOf(referenceDataBO.getValueNo()));
                        } else {
                            comboBO.setValueNo(Long.valueOf(referenceDataBO.getValueNo()));
                        }
                    }
                    comboBO.setCode(referenceDataBO.getCode());
                    comboBO.setValueStr(referenceDataBO.getValueStr());
                    String prefix = "";
                    String subfix = "";
                    try {
                        if (StringUtils.isNotNull(referenceDataBO.getPrefix())) {
                            prefix = new ResourceBundleUtils().getResource(req).getString(referenceDataBO.getPrefix().trim().toLowerCase());
                        }

                    } catch (Exception ex) {
                        prefix = referenceDataBO.getPrefix();
                        //ex.printStackTrace();
                    }
                    try {
                        if (StringUtils.isNotNull(referenceDataBO.getSubfix())) {
                            subfix = new ResourceBundleUtils().getResource(req).getString(referenceDataBO.getSubfix().trim().toLowerCase());
                        }

                    } catch (Exception ex) {
                        subfix = referenceDataBO.getSubfix();
                        //ex.printStackTrace();
                    }
                    String temp = "";
                    if (referenceDataBO.getValueNo() != null) {
                        if (StringUtils.isNotNull(subfix) && StringUtils.isNotNull(prefix)) {
                            temp = prefix + " " + referenceDataBO.getValueNo() + " " + subfix;
                        } else if (StringUtils.isNotNull(prefix)) {
                            temp = prefix + " " + referenceDataBO.getValueNo();
                        } else if (StringUtils.isNotNull(subfix)) {
                            temp = referenceDataBO.getValueNo() + " " + subfix;
                        } else {
                            temp = String.valueOf(referenceDataBO.getValueNo());
                        }
                    } else {
                        if (StringUtils.isNotNull(subfix) && StringUtils.isNotNull(prefix)) {
                            temp = prefix + " " + subfix;
                        } else if (StringUtils.isNotNull(prefix)) {
                            temp = prefix;
                        } else if (StringUtils.isNotNull(subfix)) {
                            temp = subfix;
                        } else {
                            temp = String.valueOf(referenceDataBO.getValueNo());
                        }
                    }


                    comboBO.setDescription(temp);

                    comboList.add(comboBO);
                }

            }


        }
        if (comboList == null || comboList.size() <= 0) {
            return null;
        }
        return comboList;
    }

    /**
     * Lay' thong tin ve tinh, khi duoc loc theo loai tram Bts.
     * 
     * @param typeBts the type bts
     * 
     * @return the province by type bts
     */
    public List getProvinceByTypeBts(String typeBts) {
        List list = new ArrayList();
        try {
            Session session = getSession();
            if (typeBts.equals("1")) {//Cac tram nay da duoc xac dinh
                Query query = session.createSQLQuery("   SELECT  distinct area.area_code,  area.province_name, area.province_code "
                        + "   FROM area"
                        + "   WHERE "
                        + "          area.province_code is not null "
                        + "   AND    area.province_name is not null "
                        + "   ORDER BY NLSSORT( area.province_name,'NLS_SORT=vietnamese')");
                list = query.list();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;

    }

    /**
     * Tra ve khu vu va ma khu vuc
     * ung voi tinh thanh tuong ung.
     * 
     * @param areaCode the area code
     * 
     * @return the area kv code
     */
    public List<AreaBO> getAreaKvCode(String areaCode) {
        List<AreaBO> list = new ArrayList<AreaBO>();
        try {
            Session session = getSession();
            //Lay ma khu vuc tu ma tinh thanh
            String kvCode = "";
            if (areaCode.length() >= 3) {
                //Lay ra ma kv
                kvCode = areaCode.substring(0, 3);//Ma khu vuc gom 3 ky tu 'KVx'
            }
            //Bien luu tru cau lenh sql
            StringBuffer buffer = new StringBuffer();
            buffer.append("    SELECT distinct kvCode, kvName"
                    + "   FROM AreaBO"
                    + "   WHERE "
                    + "          kvCode is not null "
                    + "   AND    kvName is not null ");
            if (!kvCode.equals("")) {
                buffer.append("   AND    kvCode=? ");
            }
            buffer.append("   ORDER BY kvName");

            Query query = session.createQuery(buffer.toString());

            //Thiet lap tham so
            if (!kvCode.equals("")) {
                query.setParameter(0, kvCode);
            }

            //Tra lai ket qua
            list = query.list();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Find precinct by district code.
     * 
     * @param areaCode the area code
     * 
     * @return the group
     */
    public List getGroup(String areaCode) {
        // Session session = getSession();
        List lstReturn = new ArrayList();
        try {
            Session session = getSession();
            Query query = session.createQuery("from StaffGroupBO where areaCode = ?  and parentId !=1 and isEnable=1 ORDER BY NLSSORT(name,'NLS_SORT=vietnamese')");
            query.setParameter(0, areaCode);
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstReturn;
    }

    /**
     * Find precinct by district code.
     * 
     * @param areaCode the area code
     * 
     * @return the group
     */
    public List getAllGroup(String areaCode) {
        // Session session = getSession();
        List lstReturn = new ArrayList();
        try {
            Session session = getSession();
            Query query = session.createQuery("from StaffGroupBO where areaCode = ? and isEnable=1 ORDER BY NLSSORT(name,'NLS_SORT=vietnamese')");
            query.setParameter(0, areaCode);
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstReturn;
    }

    public static List getServerTypeList() {
        // Session session = getSession();
        List lstReturn = new ArrayList();
        try {
            Session session = getSession();
            Query query = session.createQuery("from ServerTypeBO  ORDER BY NLSSORT(server_type_name,'NLS_SORT=vietnamese')");
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstReturn;
    }

    public static List getServerTypeListByType(String type) {
        // Session session = getSession();
        List lstReturn = new ArrayList();
        try {
            Session session = getSession();
            Query query = null;
            if (type != null && type.toLowerCase().contains("core_access")) {
                query = session.createQuery("from ServerTypeBO a where (a.type=? or a.type=? )  ORDER BY NLSSORT(server_type_name,'NLS_SORT=vietnamese')");
                query.setParameter(0, Constant.COMMON.NETWORK_LAYER.CORE);
                query.setParameter(1, Constant.COMMON.NETWORK_LAYER.ACCESS);
                lstReturn = query.list();
            } else if (type != null) {
                query = session.createQuery("from ServerTypeBO a where a.type=?   ORDER BY NLSSORT(server_type_name,'NLS_SORT=vietnamese')");
                query.setParameter(0, type);
                lstReturn = query.list();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstReturn;
    }

    public static List getVasInLoadRefercenceData(String type) {
        // Session session = getSession();
        List lstReturn = new ArrayList();
        try {
            Session session = getSession();
            Query query = null;

            query = session.createQuery("from VasInLoadRefercenceDataBO a where a.type=?   ORDER BY NLSSORT(name,'NLS_SORT=vietnamese')");
            query.setParameter(0, type);
            lstReturn = query.list();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstReturn;
    }

    public static List getCellReferenceData(String type) {
        // Session session = getSession();
        List lstReturn = new ArrayList();
        try {
            Session session = getSession();
            Query query = null;

            query = session.createQuery("from CellReferenceDataBO a where a.type=?   ORDER BY NLSSORT(value,'NLS_SORT=vietnamese')");
            query.setParameter(0, type);
            lstReturn = query.list();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstReturn;
    }

    public static CellReferenceDataBO getCellReferenceDataByValue(String type, String value) {
        // Session session = getSession();
        List lstReturn = new ArrayList();
        CellReferenceDataBO cellReferenceDataBO = null;
        try {
            Session session = getSession();
            Query query = null;

            query = session.createQuery("from CellReferenceDataBO a where a.type=?  and value = ?");
            query.setParameter(0, type);
            query.setParameter(1, value);
            lstReturn = query.list();
            if (lstReturn != null && lstReturn.size() > 0) {
                cellReferenceDataBO = (CellReferenceDataBO) lstReturn.get(0);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return cellReferenceDataBO;
    }

    public static ServerTypeBO getServerTypeBO(Long serverTypeId) {
        // Session session = getSession();
        List lstReturn = new ArrayList();
        ServerTypeBO server = null;
        try {
            Session session = getSession();
            Query query = session.createQuery("from ServerTypeBO  where server_type_id = ?");
            query.setParameter(0, serverTypeId);
            lstReturn = query.list();
            if (lstReturn != null && lstReturn.size() > 0) {
                server = (ServerTypeBO) lstReturn.get(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return server;
    }

    public static List getFaultLevelList() {
        // Session session = getSession();
        List lstReturn = new ArrayList();
        try {
            Session session = getSession();
            Query query = session.createQuery("from FaultLevelBO  ORDER BY NLSSORT(faultLevelName,'NLS_SORT=vietnamese')");
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstReturn;
    }

    public static List getListCatalogs(Long faultGroupId) throws Exception {

        Session hibernateSession = getSession();

        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from ViewCataloguesBO  where catalogValue='" + faultGroupId + "'");
        Query q = hibernateSession.createQuery(sqlSearch.toString());

        List list = q.list();
        List listTemp = null;

        if (list != null && list.size() > 0) {
            ViewCataloguesBO cataloguesBO = (ViewCataloguesBO) list.get(0);
            sqlSearch = new StringBuffer();
            sqlSearch.append(" from ViewCataloguesBO  ");
            sqlSearch.append(" where parentId != 1 and ordering like '" + cataloguesBO.getOrdering() + "%'");
            q = hibernateSession.createQuery(sqlSearch.toString());
            listTemp = q.list();
        }

        return listTemp;
    }

    public static ViewCataloguesBO getCatalog(Long id) throws Exception {

        Session hibernateSession = getSession();

        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from ViewCataloguesBO  where  id='" + id + "'");
        Query q = hibernateSession.createQuery(sqlSearch.toString());

        List list = q.list();
        return (ViewCataloguesBO) list.get(0);
    }

    /**
     * Gets the province group.
     * 
     * @param areaCode the area code
     * 
     * @return the province group
     */
    public List getProvinceGroup(String areaCode) {
        // Session session = getSession();
        List lstReturn = new ArrayList();
        try {
            Session session = getSession();
            Query query = session.createQuery("from StaffGroupBO where areaCode = ?  and parentId =1 and isEnable=1 ORDER BY NLSSORT(name,'NLS_SORT=vietnamese')");
            query.setParameter(0, areaCode);
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstReturn;
    }

    /**
     * Gets the role.
     *
     * @param req the req
     *
     * @return the role
     */
    public static List getRole(HttpServletRequest req) {
        try {
            String SQL_SELECT = "";
            if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {



                String provinceCode = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString();
                if (provinceCode != null && StringUtils.checkNotKV(provinceCode)) {
                    //SQL_SELECT = "from RoleBO where roleId in (4,6,7,8,9) ";
                    SQL_SELECT = "from RoleBO where type >= 0 ORDER BY NLSSORT( name,'NLS_SORT=vietnamese')";
                } else {
                    //  SQL_SELECT = "from RoleBO where roleId in (1,2,3,5) ";
                    SQL_SELECT = "from RoleBO where type < 0 ORDER BY NLSSORT( name,'NLS_SORT=vietnamese')";
                }
                Query q = getSession().createQuery(SQL_SELECT);
                List lst = q.list();
                return lst;
            }


        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return null;
    }

    /**
     * Gets the staff.
     * 
     * @param groupId the group id
     * 
     * @return the staff
     */
    public List getStaff(String groupId) {
        // Session session = getSession();
        ArrayList<StaffBO> lst = new ArrayList<StaffBO>();
        try {
            String SQL = " SELECT "
                    + "   a, b.id.roleId "
                    + " FROM "
                    + "   StaffBO as a , "
                    + "   StaffInGroupBO as b "
                    + " WHERE a.isEnable=1 "
                    + "  and a.staffId = b.id.staffId "
                    + "   AND b.id.groupId = ?  order by a.name asc";
            Session session = getSession();
            Query query = session.createQuery(SQL);
            query.setParameter(0, Long.parseLong(groupId));
            List lstResult = query.list();
            for (int i = 0; i < lstResult.size(); i++) {
                Object[] obj = (Object[]) lstResult.get(i);
                StaffBO staff = (StaffBO) obj[0];
                lst.add(staff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lst;
    }

    public static List getStaffByStaffId(String staffId) {
        // Session session = getSession();
        ArrayList<StaffBO> lst = new ArrayList<StaffBO>();
        try {
            String SQL = " SELECT "
                    + "   a, b.id.roleId "
                    + " FROM "
                    + "   StaffBO as a , "
                    + "   StaffInGroupBO as b "
                    + " WHERE a.isEnable=1 "
                    + "  and a.staffId = b.id.staffId "
                    + "   AND b.id.staffId = ?  order by a.name asc";
            Session session = getSession();
            Query query = session.createQuery(SQL);
            query.setParameter(0, Long.parseLong(staffId));
            List lstResult = query.list();
            for (int i = 0; i < lstResult.size(); i++) {
                Object[] obj = (Object[]) lstResult.get(i);
                StaffBO staff = (StaffBO) obj[0];
                lst.add(staff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lst;
    }

    /**
     * Gets the listnode bsc.
     *
     * @param lst the lst
     *
     * @return the listnode bsc
     */
    public static List<NodeBInGroupBO> getListRNC(List lst, List lst2) {
        List<NodeBInGroupBO> resultList = new ArrayList<NodeBInGroupBO>();
        NodeBInGroupBO btsBO = null;
        Map<String, String> rncMap = new HashMap<String, String>();

        if (lst != null && lst.size() > 0) {
            for (int i = 0; i < lst.size(); i++) {
                Object obj[] = (Object[]) lst.get(i);
                if (obj[0] != null) {
                    rncMap.put(obj[0].toString(), obj[0].toString());
                }
            }
        }
        if (lst2 != null && lst2.size() > 0) {
            for (int i = 0; i < lst2.size(); i++) {
                Object obj[] = (Object[]) lst2.get(i);
                // btsBO = new NodeBInGroupBO();
                if (obj[0] != null) {
                    rncMap.put(obj[0].toString(), obj[0].toString());
                }
            }
        }

        int mapsize = rncMap.size();

        Iterator keyValuePairs = rncMap.entrySet().iterator();
        for (int i = 0; i < mapsize; i++) {
            Map.Entry entry = (Map.Entry) keyValuePairs.next();
            String key = entry.getKey().toString();
            String value = (String) entry.getValue();
            btsBO = new NodeBInGroupBO();
            btsBO.setBscId(key);

            resultList.add(btsBO);
        }


        return resultList;
    }

    /**
     * Gets the listnode bsc.
     * 
     * @param lst the lst
     * 
     * @return the listnode bsc
     */
    public static List<BtsBO> getListBsc(List lst, List lst2) {
        List<BtsBO> resultList = new ArrayList<BtsBO>();
        BtsBO btsBO = null;

        Map<String, String> bscMap = new HashMap<String, String>();

        if (lst != null && lst.size() > 0) {
            for (int i = 0; i < lst.size(); i++) {
                Object obj[] = (Object[]) lst.get(i);

                if (obj[0] != null) {
                    bscMap.put(obj[0].toString(), obj[0].toString());
                }
            }
        }
        if (lst2 != null && lst2.size() > 0) {
            for (int i = 0; i < lst2.size(); i++) {
                Object obj[] = (Object[]) lst2.get(i);
                // btsBO = new NodeBInGroupBO();
                if (obj[0] != null) {
                    bscMap.put(obj[0].toString(), obj[0].toString());
                }
            }
        }

        int mapsize = bscMap.size();

        Iterator keyValuePairs = bscMap.entrySet().iterator();
        for (int i = 0; i < mapsize; i++) {
            Map.Entry entry = (Map.Entry) keyValuePairs.next();
            String key = entry.getKey().toString();
            String value = (String) entry.getValue();
            btsBO = new BtsBO();
            btsBO.setBscId(key);

            resultList.add(btsBO);
        }



        return resultList;
    }

    /**
     *
     * @return
     */
    public static Map<String, String> getProvinceMap() {
        Map<String, String> provinceMap = new HashMap<String, String>();
        List areas = null;
        if (ReferenceDataTimerTask.getAreaList() == null || ReferenceDataTimerTask.getAreaList().size() <= 0) {
            Session session = getSession();
            String hql = " from AreaView ";
            Query qr = session.createQuery(hql);
            areas = qr.list();
        } else {
            areas = ReferenceDataTimerTask.getAreaList();
        }
        AreaView areaView;
        if (areas != null && areas.size() > 0) {
            for (Object obj : areas) {
                areaView = (AreaView) obj;
                provinceMap.put(areaView.getAreaCode() == null ? "" : areaView.getAreaCode(), areaView.getProvinceName() == null ? "" : areaView.getProvinceName());

            }
        }

        return provinceMap;
    }

    /**
     * Gets the list province.
     * 
     * @param req the req
     * 
     * @return the list province
     * 
     * @throws Exception the exception
     */
    public static List<AreaBO> getListProvince(HttpServletRequest req) throws Exception {
        try {
            ArrayList resultList = new ArrayList();
            String provinceCode = req.getSession().getAttribute("ProvinceCode") != null ? (String) req.getSession().getAttribute("ProvinceCode") : "";
            if (ReferenceDataTimerTask.getAreaList() != null) {
                List areas = ReferenceDataTimerTask.getAreaList();
                AreaBO areaBO;
                AreaView areaView;
                for (Object obj : areas) {
                    areaView = (AreaView) obj;
                    if (areaView.getPath().contains(provinceCode)) {
                        areaBO = new AreaBO();
                        areaBO.setAreaCode(areaView.getAreaCode() == null ? "" : areaView.getAreaCode());
                        areaBO.setProvinceName(areaView.getProvinceName() == null ? "" : areaView.getProvinceName());
                        resultList.add(areaBO);
                    }
                }
            } else {
                Session session = getSession();
                String hql = " from AreaView a where lower(a.path) like ? ";
                Query qr = session.createQuery(hql);
                qr.setParameter(0, "%" + provinceCode.toLowerCase().trim() + "%");
                List lst = qr.list();
                AreaBO areaBO;

                AreaView ObjTemp;
                for (Object obj : lst) {
                    ObjTemp = (AreaView) obj;
                    areaBO = new AreaBO();
                    areaBO.setAreaCode(ObjTemp.getAreaCode() == null ? "" : ObjTemp.getAreaCode());
                    areaBO.setProvinceName(ObjTemp.getProvinceName() == null ? "" : ObjTemp.getProvinceName());
                    resultList.add(areaBO);
                }
            }
            return resultList;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public static List<AreaBO> getListProvince3G(HttpServletRequest req) throws Exception {
        try {
            ArrayList resultList = new ArrayList();
            String provinceCode = req.getSession().getAttribute("ProvinceCode") != null ? (String) req.getSession().getAttribute("ProvinceCode") : "";
            if (ReferenceDataTimerTask.getAreaList() != null) {
                List areas = ReferenceDataTimerTask.getAreaList();
                AreaBO areaBO;
                AreaView areaView;
                for (Object obj : areas) {
                    areaView = (AreaView) obj;
                    if (areaView.getPath().contains(provinceCode)) {
                        areaBO = new AreaBO();
                        areaBO.setAreaCode(areaView.getAreaCode() == null ? "" : areaView.getAreaCode());
                        areaBO.setProvinceName(areaView.getProvinceName() == null ? "" : areaView.getProvinceName());
                        resultList.add(areaBO);
                    }
                }
            } else {
                Session session = getSession();
                String hql = " from AreaView a where lower(a.path) like ? ";
                Query qr = session.createQuery(hql);
                qr.setParameter(0, "%" + provinceCode.toLowerCase().trim() + "%");
                List lst = qr.list();
                AreaBO areaBO;

                AreaView ObjTemp;
                for (Object obj : lst) {
                    ObjTemp = (AreaView) obj;
                    areaBO = new AreaBO();
                    areaBO.setAreaCode(ObjTemp.getAreaCode() == null ? "" : ObjTemp.getAreaCode());
                    areaBO.setProvinceName(ObjTemp.getProvinceName() == null ? "" : ObjTemp.getProvinceName());
                    resultList.add(areaBO);
                }
            }
            return resultList;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Gets the lead staff.
     * 
     * @param groupId the group id
     * 
     * @return the lead staff
     */
    public List getLeadStaff(String groupId) {
        // Session session = getSession();
        ArrayList<StaffBO> lst = new ArrayList<StaffBO>();
        try {
            String SQL = " SELECT "
                    + "   a, b.id.roleId "
                    + " FROM "
                    + "   StaffBO as a , "
                    + "   StaffInGroupBO as b "
                    + " WHERE a.isEnable=1 "
                    + "  and a.staffId = b.id.staffId "
                    + "  and b.id.roleId = 7"
                    + "   AND b.id.groupId = ?  order by a.name asc";
            Session session = getSession();
            Query query = session.createQuery(SQL);
            query.setParameter(0, Long.parseLong(groupId));
            List lstResult = query.list();
            for (int i = 0; i < lstResult.size(); i++) {
                Object[] obj = (Object[]) lstResult.get(i);
                StaffBO staff = (StaffBO) obj[0];
                lst.add(staff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lst;
    }

    /**
     * Gets the kV code by area code.
     * 
     * @param areaCode the area code
     * 
     * @return the kV code by area code
     */
    public List getKVCodeByAreaCode(String areaCode) {
        // Session session = getSession();
        ArrayList<AreaBO> lst = new ArrayList<AreaBO>();
        try {
            String SQL = " SELECT distinct kvCode, kvName"
                    + " FROM "
                    + "   AreaBO "
                    + " WHERE areaCode = ? ";

            Session session = getSession();
            Query query = session.createQuery(SQL);
            query.setParameter(0, areaCode);
            List lstResult = query.list();
            AreaBO areaBO = null;
            for (int i = 0; i < lstResult.size(); i++) {
                Object[] obj = (Object[]) lstResult.get(i);
                String kvCode = (String) obj[0];
                String kvName = (String) obj[1];
                areaBO = new AreaBO();
                areaBO.setKvCode(kvCode);
                areaBO.setKvName(kvName);
                lst.add(areaBO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lst;
    }
    /** The Constant sqlGetArea. */
    private static final String sqlGetArea = "    SELECT distinct area.kv_code, area.kv_name"
            + "   FROM area"
            + "   WHERE "
            + "          area.kv_code is not null"
            + "   AND    area.kv_name is not null  "
            + "   ORDER BY NLSSORT(area.kv_name,'NLS_SORT=vietnamese')";
    private static final String sqlGetAreaKV = "    SELECT distinct area.kv_code, area.kv_name"
            + "   FROM area"
            + "   WHERE "
            + "          area.kv_code = ?"
            + "   AND    area.kv_name is not null "
            + "   AND    area.province_code is null  ";

    /**
     * Get province area.
     *
     * @param req the req
     *
     * @throws org.hibernate.HibernateException 	 * @throws HibernateException the hibernate exception
     * @throws HibernateException the hibernate exception
     */
    public static void getListArea(HttpServletRequest req)
            throws HibernateException {
        String provinceCode = (String) req.getSession().getAttribute("ProvinceCode");
        if (!StringUtils.checkTtdhkt(provinceCode) && provinceCode.length() > 3) {
            provinceCode = provinceCode.substring(0, 3);

        }
        if (provinceCode != null) {
            if ((provinceCode.equals("") || provinceCode.equals("0") || provinceCode.equals(Constant.COMMON.ADMIN_TTDHKT))) {
                getProvinceArea(req);
            } else if (provinceCode.equals(Constant.COMMON.AREA_CODE_KV.KV1) || provinceCode.equals(Constant.COMMON.AREA_CODE_KV.KV2)
                    || provinceCode.equals(Constant.COMMON.AREA_CODE_KV.KV3)) {

                Query q1 = getSession().createSQLQuery(sqlGetAreaKV);
                q1.setParameter(0, provinceCode);

                req.setAttribute("listProvinceKV", getListProvinceKV(q1.list()));
            }
        }



    }

    public static void getProvinceArea(HttpServletRequest req)
            throws HibernateException {
        // Get list KV Code
        if (ReferenceDataTimerTask.getKvList() != null && ReferenceDataTimerTask.getKvList().size() > 0) {
            req.setAttribute("listProvinceKV", getListProvinceKV(ReferenceDataTimerTask.getKvList()));
        } else {
            Query q1 = getSession().createSQLQuery(sqlGetArea);
            req.setAttribute("listProvinceKV", getListProvinceKV(q1.list()));
        }

    }

    public static void getNodeBsc(HttpServletRequest req) throws HibernateException {
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


            req.getSession().setAttribute("listBsc", AddressDAO.getListBsc(lstnode, lstnode2));
        }
    }

    /**
     *
     * @param form
     * @param req
     * @return List of Provinces
     */
    public static List getListProvince(ActionForm form, HttpServletRequest req) {
        CellProvinceForm provinceForm = (CellProvinceForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        StringBuffer sqlSearchWhere = new StringBuffer();
        sqlSearch.append(" from ViewProvinceBO a ");
        List lstPaAdmin = new ArrayList();
        sqlSearchWhere.append(" where 1=1 ");

        String provinceCode = "";
        if (req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE) != null) {
            provinceCode = req.getSession().getAttribute(Constant.COMMON.PROVINCE_CODE).toString();
        }
        if (StringUtils.isNotNull(provinceForm.getKvCode())) {
            sqlSearchWhere.append(" and lower(areaCode) =  ? ");
            lstPaAdmin.add(provinceForm.getKvCode().trim().toLowerCase());
        }

        if (StringUtils.isNotNull(provinceForm.getProvinceKey())) {
            sqlSearchWhere.append(" and lower(provinceKey) like  ? ");
            lstPaAdmin.add("%" + provinceForm.getProvinceKey().trim().toLowerCase() + "%");
        }

        if (StringUtils.isNotNull(provinceForm.getTerrainType())) {
            sqlSearchWhere.append(" and lower(terrainId) =  ? ");
            lstPaAdmin.add(provinceForm.getTerrainType());
        }

        if (StringUtils.isNotNull(provinceForm.getProvinceCode())) {
            sqlSearchWhere.append(" and lower(provinceCode) like  ? ");
            lstPaAdmin.add("%" + provinceForm.getProvinceCode().trim().toLowerCase() + "%");
        }

        if (StringUtils.isNotNull(provinceForm.getZoneCode())) {
            sqlSearchWhere.append(" and lower(zoneCode) like  ? ");
            lstPaAdmin.add("%" + provinceForm.getZoneCode().trim().toLowerCase() + "%");
        }

        if (StringUtils.isNotNull(provinceForm.getAdministrativeType())) {
            sqlSearchWhere.append(" and lower(administrativeId) =  ? ");
            lstPaAdmin.add(provinceForm.getAdministrativeType());
        }

        if (StringUtils.isNotNull(provinceForm.getProvinceName())) {
            sqlSearchWhere.append(" and lower(provinceName) like  ? ");
            lstPaAdmin.add("%" + provinceForm.getProvinceName().trim().toLowerCase() + "%");
        }

        if (!provinceCode.equalsIgnoreCase("ttdhkt")) {
            sqlSearchWhere.append(" and lower(areaCode) like ? ");
            lstPaAdmin.add(provinceCode.toLowerCase() + "%");
        }
        sqlSearchWhere.append("  ORDER BY provinceName ");

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

    public static List getListGroup(String areaCode) {
        String SQL_SELECT = "from StaffGroupBO where isEnable=1 and areaCode = ? and parentId != 1 order by groupId desc ";
        try {
            Query q = getSession().createQuery(SQL_SELECT);
            q.setParameter(0, areaCode);
            return q.list();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static List getListGroupByAreaCode(String areaCode) {
        String SQL_SELECT = "from StaffGroupBO where isEnable=1  and parentId != 1 and areaCode = ?   order by name ";
        try {
            Query q = getSession().createQuery(SQL_SELECT);
            q.setParameter(0, areaCode);
            return q.list();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static List getListGroupByGroupId(String groupId) {
        String SQL_SELECT = "from StaffGroupBO where isEnable=1 and groupId = ? order by groupId desc ";
        try {
            Query q = getSession().createQuery(SQL_SELECT);
            q.setParameter(0, Long.valueOf(groupId));
            return q.list();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static List getListCommentType() {
        String SQL_SELECT = "from CommentTypeBO  ORDER BY NLSSORT(name,'NLS_SORT=vietnamese')";
        try {
            Query q = getSession().createQuery(SQL_SELECT);
            return q.list();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // get list province
    /**
     * Gets the list province.
     *
     * @param lst the lst
     *
     * @return the list province
     */
    public static List<AreaBO> getListProvince(List lst) {
        List<AreaBO> resultList = new ArrayList<AreaBO>();
        AreaBO areaBO = null;
        for (int i = 0; i < lst.size(); i++) {
            Object obj[] = (Object[]) lst.get(i);
            areaBO = new AreaBO();
            if (obj[1] != null) {
                areaBO.setAreaCode(obj[1].toString());
            }
            if (obj[2] != null) {
                areaBO.setProvinceName(obj[2].toString());
            }
            resultList.add(areaBO);
        }
        return resultList;
    }

    /**
     * Gets the current page.
     * 
     * @param req the req
     * @param countLoss the count loss
     * @param q the q
     * 
     * @return the current page
     * 
     * @throws HibernateException the hibernate exception
     */
    public static CurrentPaginatedList getCurrentPage(HttpServletRequest req, int countLoss, Query q) throws HibernateException {

        CurrentPaginatedList paginatedList = PaginatedDAO.getPaginatedListFromRequest(req);
        paginatedList.setTotalNumberOfRows(countLoss);
        q.setFirstResult(paginatedList.getFirstRecordIndex());
        q.setMaxResults(paginatedList.getPageSize());
        paginatedList.setList(q.list());

        return paginatedList;
    }

    /**
     * Gets the current page popup.
     * 
     * @param objs the objs
     * @param req the req
     * 
     * @return the current page popup
     * 
     * @throws org.hibernate.HibernateException      * @throws HibernateException the hibernate exception
     * @throws HibernateException the hibernate exception
     */
    public static CurrentPaginatedList getCurrentPagePopup(List objs, HttpServletRequest req) throws HibernateException {

        CurrentPaginatedList paginatedList = PaginatedDAO.getPaginatedListFromRequestPopup(req);
        paginatedList.setTotalNumberOfRows(objs.size());

        List subList = new ArrayList();

        int index = paginatedList.getFirstRecordIndex();
        int endRow = index + paginatedList.getPageSize();

        for (int i = index; i < endRow; i++) {
            if (i < objs.size()) {
                subList.add(objs.get(i));
            }
        }

        paginatedList.setList(subList);

        return paginatedList;
    }

    /**
     * Gets the current page.
     * 
     * @param objs the objs
     * @param req the req
     * 
     * @return the current page
     * 
     * @throws HibernateException the hibernate exception
     */
    public static CurrentPaginatedList getCurrentPage(List objs, HttpServletRequest req) throws HibernateException {

        CurrentPaginatedList paginatedList = PaginatedDAO.getPaginatedListFromRequest(req);
        paginatedList.setTotalNumberOfRows(objs.size());

        List subList = new ArrayList();

        int index = paginatedList.getFirstRecordIndex();
        int endRow = index + paginatedList.getPageSize();

        for (int i = index; i < endRow; i++) {
            if (i < objs.size()) {
                subList.add(objs.get(i));
            }
        }
        paginatedList.setList(subList);


        return paginatedList;
    }

    /**
     * Gets the current page assigned.
     * 
     * @param objs the objs
     * @param req the req
     * 
     * @return the current page assigned
     * 
     * @throws HibernateException the hibernate exception
     */
    public static CurrentPaginatedList getCurrentPageAssigned(List objs, HttpServletRequest req) throws HibernateException {

        CurrentPaginatedList paginatedList = PaginatedDAO.getPaginatedListFromRequest(req);
        paginatedList.setTotalNumberOfRows(objs.size());

        List subList = new ArrayList();

        int index = 0;//paginatedList.getFirstRecordIndex();

        int endRow = 25;//index + paginatedList.getPageSize();

        for (int i = index; i < endRow; i++) {
            if (i < objs.size()) {
                subList.add(objs.get(i));
            }
        }
        paginatedList.setList(subList);


        return paginatedList;
    }

    /**
     * Get province area.
     *
     * @param req the req
     *
     * @throws org.hibernate.HibernateException 	 * @throws HibernateException the hibernate exception
     * @throws HibernateException the hibernate exception
     */
    public static void getFaultGroupList(HttpServletRequest req)
            throws HibernateException {
        Query q1 = getSession().createQuery("from FaultGroupBO where type = '" + Constant.COMMON.NETWORK_LAYER.BTS + "' order by name");
        req.setAttribute(Constant.COMMON.SET_COMMON_REQUEST.FAULT_GROUP_LIST, q1.list());
    }

    public static void getFaultGroup3GList(HttpServletRequest req)
            throws HibernateException {
        Query q1 = getSession().createQuery("from FaultGroup3GBO where  upper(type) = '" + Constant.COMMON.NETWORK_LAYER.NODEB + "' order by name");
        req.setAttribute(Constant.COMMON.SET_COMMON_REQUEST.FAULT_GROUP_LIST, q1.list());
    }

//    public static void getSubFaultList(HttpServletRequest req)
//            throws HibernateException {
//        Query q1 = getSession().createQuery("from SubFaultBO order by name");
//        req.setAttribute(Constant.COMMON.SET_COMMON_REQUEST.SUB_FAULT_LIST, q1.list());
//    }
//
//    public static void getSubFault3GList(HttpServletRequest req)
//            throws HibernateException {
//        Query q1 = getSession().createQuery("from SubFault3GBO order by name");
//        req.setAttribute(Constant.COMMON.SET_COMMON_REQUEST.SUB_FAULT_LIST, q1.list());
//    }
    public static List getVenderByBscID(String bscId) {
        List list = new ArrayList();
        BtsBO btsBO = null;
        List<BtsBO> btsBOList = new ArrayList<BtsBO>();
        try {
            Session session = getSession();
            Query query = session.createQuery("Select distinct bscId,vender from BtsBO where bscId = ? and vender is not null");
            query.setParameter(0, bscId);
            list = query.list();
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    btsBO = new BtsBO();
                    Object obj[] = (Object[]) list.get(i);

                    if (obj[0] != null) {
                        btsBO.setBscId(obj[0].toString());
                    }
                    if (obj[1] != null) {
                        btsBO.setVender(obj[1].toString());
                    }
                    btsBOList.add(btsBO);
                }
            }

            Query query1 = session.createQuery("from LogServersBO where bscId = ? ");
            query1.setParameter(0, bscId);
            list = query1.list();
            if (list != null && list.size() > 0) {
                LogServersBO logServersBO = (LogServersBO) list.get(0);
                if (logServersBO != null) {
                    btsBO = new BtsBO();
                    btsBO.setBscId(logServersBO.getBscId());
                    btsBO.setVender(logServersBO.getExchange());
                    btsBOList.add(btsBO);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return btsBOList;

    }

    public static List getVenderByBscId3G(String bscId) {
        List list = new ArrayList();
        NodeBBO btsBO = null;
        List<NodeBBO> btsBOList = new ArrayList<NodeBBO>();
        try {
            Session session = getSession();
            Query query = session.createQuery("Select distinct bscId,vender from NodeBInGroupBO where bscId = ? and vender is not null");
            query.setParameter(0, bscId);
            list = query.list();
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    btsBO = new NodeBBO();
                    Object obj[] = (Object[]) list.get(i);

                    if (obj[0] != null) {
                        btsBO.setBscId(obj[0].toString());
                    }
                    if (obj[1] != null) {
                        btsBO.setVender(obj[1].toString());
                    }
                    btsBOList.add(btsBO);
                }
            }

            Query query1 = session.createQuery("from LogServers3GBO where bscId = ? ");
            query1.setParameter(0, bscId);
            list = query1.list();
            if (list != null && list.size() > 0) {
                LogServersBO logServersBO = (LogServersBO) list.get(0);
                if (logServersBO != null) {
                    btsBO = new NodeBBO();
                    btsBO.setBscId(logServersBO.getBscId());
                    btsBO.setVender(logServersBO.getExchange());
                    btsBOList.add(btsBO);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return btsBOList;

    }

    /*
     * type = 0 : 2G
     * type = 1 : 3G
     */
    public String getProvinceNameByAreaCode(String areaCode, Long type) {
        String provinceName = "";
        try {
            List list = new ArrayList();

            Session session = getSession();
            Query query = null;
            if (type == 0L) {
                query = session.createQuery("from AreaBO where areaCode = ?");

                query.setParameter(0, areaCode);
                list = query.list();

                if (list.size() > 0) {
                    AreaBO areaBO = (AreaBO) list.get(0);
                    provinceName = areaBO.getProvinceName();
                }
            } else {
                query = session.createQuery("from Area3GBO where areaCode = ?");

                query.setParameter(0, areaCode);
                list = query.list();

                if (list.size() > 0) {
                    Area3GBO area3GBO = (Area3GBO) list.get(0);
                    provinceName = area3GBO.getProvinceName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return provinceName;

    }

    /*
     * type = 2 : 2G
     * type = 3 : 3G
     */
    public List getListFaultId(Long type) {
        List list = new ArrayList();
        try {
            Session session = getSession();
            Query query = null;
            if (type == 2L) { //2G
                query = session.createQuery("from FaultBO where faultGroupId in (" + Constant.FAULT_TYPE.FLOW_LOSS + ","
                        + Constant.FAULT_TYPE.POWER_LOSS + "," + Constant.FAULT_TYPE.BTS_FAULT + ") and name is not null order by name asc");
                list = query.list();

            } else { //3G
                query = session.createQuery("from Fault3GBO where faultGroupId in (" + Constant.FAULT_TYPE_3G.FLOW_LOSS + ","
                        + Constant.FAULT_TYPE_3G.POWER_LOSS + "," + Constant.FAULT_TYPE_3G.NODEB_FAULT + ") and name is not null order by name asc");
                list = query.list();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public DistrictBO getAreaByDistrictCode(String districtCode) {
        DistrictBO districtBO = new DistrictBO();
        try {
            List list = new ArrayList();

            Session session = getSession();
            Query query = null;
            query = session.createQuery("from DistrictBO where districtCode = ?");

            query.setParameter(0, districtCode);
            list = query.list();

            if (list != null && list.size() > 0) {
                districtBO = (DistrictBO) list.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return districtBO;

    }

    public static void setResultRequest(HttpServletRequest req, String resultKey) {
        req.getSes
