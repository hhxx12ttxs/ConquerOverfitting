package com.lifetek.netmosys.database.DAO;

import com.lifetek.netmosys.database.BO.AreaBO;
import com.lifetek.netmosys.database.BO.AreaView;
import com.lifetek.netmosys.database.BO.RoleBO;
import com.lifetek.netmosys.database.BO.StaffBO;
import com.lifetek.database.config.BaseHibernateDAO;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.Query;
import org.hibernate.Session;
import com.lifetek.netmosys.database.BO.LevelBtsBO;
import com.lifetek.netmosys.database.internet.BO.InterInterfaceCommonBO;
import com.lifetek.netmosys.util.ResourceBundleUtils;
import com.lifetek.netmosys.util.StringUtils;

/**
 * The Class AddressAjaxDAO.
 * 
 * @author ThanhNC
 */
public class AddressAjaxDAO extends BaseHibernateDAO {

    /**
     * Find district by province code.
     * 
     * @param kvCode the kv code
     * 
     * @return the province
     */
    @SuppressWarnings("unchecked")
    public List getProvince(String kvCode) {
        List list = new ArrayList();
        Session session = null;
        session = openThreadSession();
        try {
            if (kvCode == null || "".equals(kvCode) || "-1".equals(kvCode)) {
                Query query = session.createQuery("from AreaBO where provinceName is not null  order by provinceName asc");
                list = query.list();
            } else {
                Query query = session.createQuery("from AreaBO where parentCode= ? and provinceName is not null order by provinceName asc");
                query.setParameter(0, kvCode);
                list = query.list();
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return list;

    }

    public List getNodeBsc(String provinceCode) {
        //Get node BSC
        // Query query = getSession().createQuery(GET_AREA);
        //                query.setParameter(0, bts.getAreaCode());

        Session session = null;
        List lstTemp = null;
        session = openThreadSession();
        try {
            if (provinceCode != null) {
                String sqlGetnodeBSC = "SELECT distinct exchangeId,areaCode  FROM ExchAreaBO where areaCode=? ORDER BY NLSSORT(exchangeId,'NLS_SORT=vietnamese') ";
                Query node = session.createQuery(sqlGetnodeBSC);
                node.setParameter(0, provinceCode);
                List lstnode = node.list();

                String sqlGetnodeBSCInGroup = "SELECT distinct bscId,areaCode  FROM BtsBO where areaCode=? ORDER BY NLSSORT(bscId,'NLS_SORT=vietnamese') ";
                Query bscInGroup = getSession().createQuery(sqlGetnodeBSCInGroup);
                bscInGroup.setParameter(0, provinceCode);
                List lstnode2 = bscInGroup.list();


                lstTemp = AddressDAO.getListBsc(lstnode, lstnode2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return lstTemp;
    }

    public List getDistrict(String areaCode) {
        List list = new ArrayList();
        Session session = null;
        session = openThreadSession();
        try {
            if (areaCode != null || !"".equals(areaCode)) {
                Query query = session.createQuery("from DistrictBO where areaCode= ? and districtName is not null order by districtName asc");
                query.setParameter(0, areaCode);
                list = query.list();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return list;

    }

    @SuppressWarnings("unchecked")
    public List getCellProvince(String kvCode) {
        List list = new ArrayList();
        Session session = null;
        session = openThreadSession();
        try {

            if (kvCode == null || "".equals(kvCode) || "-1".equals(kvCode)) {
                Query query = session.createQuery("from ProvinceBO where provinceName is not null  order by provinceName asc");
                list = query.list();
            } else {
                Query query = session.createQuery("from ProvinceBO where areaCode= ? and provinceName is not null order by provinceName asc");
                query.setParameter(0, kvCode);
                list = query.list();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return list;

    }

    public List getCellDistrict(String areaCode) {
        List list = new ArrayList();
        Session session = null;
        session = openThreadSession();
        try {
            if (StringUtils.isNotNull(areaCode)) {
                Query query = session.createQuery("from CellDistrictBO where provinceCode= ? and districtName is not null order by districtName asc");
                query.setParameter(0, areaCode);
                list = query.list();
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return list;

    }

    public List getCellVillage(String districtCode) {
        List list = new ArrayList();
        Session session = null;
        session = openThreadSession();
        try {
            if (StringUtils.isNotNull(districtCode)) {
                Query query = session.createQuery("from VillageBO where districtCode= ? and villageName is not null order by villageName asc");
                query.setParameter(0, districtCode);
                list = query.list();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return list;

    }

    /**
     * Lay' thong tin ve tinh, khi duoc loc theo loai tram Bts.
     * 
     * @param typeBts the type bts
     * 
     * @return the province by type bts
     */
    @SuppressWarnings("unchecked")
    public List getProvinceByTypeBts(String typeBts) {
        List list = new ArrayList();
        Session session = null;
        session = openThreadSession();
        try {

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
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
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
    @SuppressWarnings("unchecked")
    public List<AreaBO> getAreaKvCode(String areaCode) {
        List<AreaBO> list = new ArrayList<AreaBO>();
        Session session = null;
        session = openThreadSession();
        try {

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
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public void getLevelBts(HttpServletRequest req) {
        Session session = null;
        session = openThreadSession();
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
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        req.setAttribute("listLevelBts", lstTemp);

    }
    // @SuppressWarnings("unchecked") <bean:message key="btsConfig.edit.important.bts" /><bean:message key="common.select.symbal"/>

    public List getDescLevel(HttpServletRequest req, Long typeImportant) {
        List lstReturn = new ArrayList();
        Session session = null;
        session = openThreadSession();
        try {
            Query query = session.createQuery("from LevelBtsBO where typeImportant = ? order by descLevel ");
            query.setParameter(0, typeImportant);
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return lstReturn;
    }

    public List getDescLevelSelect(HttpServletRequest req, Long typeImportant) {
        List lstReturn = new ArrayList();
        Session session = null;
        session = openThreadSession();
        try {
            Query query = session.createQuery("from LevelBtsBO where typeImportant = ? order by descLevel ");
            query.setParameter(0, typeImportant);
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

//        LevelBtsBO levelBtsBO = new LevelBtsBO();
//        levelBtsBO.setDescLevelId(0l);
//        levelBtsBO.setDescLevel(new ResourceBundleUtils().getResource(req).getString("common.select.symbal")
//                + new ResourceBundleUtils().getResource(req).getString("btsConfig.edit.important.bts")
//                + new ResourceBundleUtils().getResource(req).getString("common.select.symbal"));
        if (lstReturn == null) {
            lstReturn = new ArrayList();
        }
//        lstReturn.add(0, levelBtsBO);
        return lstReturn;
    }

    public void getLevelNodeB(HttpServletRequest req) {
        Session session = null;
        session = openThreadSession();
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
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        req.setAttribute("listLevelBts", lstTemp);

    }

    @SuppressWarnings("unchecked")
    public List getDescLevelNodeB(HttpServletRequest req, Long typeImportant) {
        List lstReturn = new ArrayList();
        Session session = null;
        session = openThreadSession();
        try {
            Query query = session.createQuery("from LevelBtsBO where typeImportant = ? order by descLevel");
            query.setParameter(0, typeImportant);
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return lstReturn;
    }

    /**
     * Find district by province code.
     * 
     * @param roleId the role id
     * @param areaCode the area code
     * 
     * @return the group by role type
     */
    @SuppressWarnings("unchecked")
    public List getGroupByRoleType(String roleId, String areaCode) {
        List list = new ArrayList();
        Session session = null;
        session = openThreadSession();
        List lstReturn = new ArrayList();
        try {

            Query query = session.createQuery("from RoleBO where roleId= ? ");
            query.setParameter(0, Long.valueOf(roleId));
            list = query.list();

            RoleBO roleBO = null;

            if (list != null && list.size() > 0) {
                roleBO = (RoleBO) list.get(0);

                // get group for employee and leader
                if (roleBO.getType() != null && (roleBO.getType() == 0l || roleBO.getType() == 2l)) {
                    query = session.createQuery("from StaffGroupBO where areaCode = ?  and parentId !=1 and isEnable=1 ");
                    query.setParameter(0, areaCode);
                    lstReturn = query.list();
                } else {
                    // get group for manager
                    if (roleBO.getType() != null && roleBO.getType() == 1l) {
                        query = session.createQuery("from StaffGroupBO where areaCode = ?  and parentId =1 and isEnable=1");
                        query.setParameter(0, areaCode);
                        lstReturn = query.list();
                    } else {
                        if (roleBO.getType() != null && (roleBO.getType() == -1l || roleBO.getType() == -2l)) {
                            query = session.createQuery("from StaffGroupBO where areaCode = ?   and isEnable=1");
                            query.setParameter(0, areaCode);
                            lstReturn = query.list();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
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
    @SuppressWarnings("unchecked")
    public List getGroup(String areaCode) {
        // Session session = null;
        List lstReturn = new ArrayList();
        Session session = null;
        session = openThreadSession();
        try {
            Query query = session.createQuery("from StaffGroupBO where areaCode like ?  and parentId !=1 and isEnable=1 ");
            query.setParameter(0, "%" + areaCode);
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return lstReturn;
    }

    /**
     * Gets the province group.
     * 
     * @param areaCode the area code
     * 
     * @return the province group
     */
    @SuppressWarnings("unchecked")
    public List getProvinceGroup(String areaCode) {
        // Session session = null;
        List lstReturn = new ArrayList();
        Session session = null;
        session = openThreadSession();
        try {
            Query query = session.createQuery("from StaffGroupBO where areaCode like ?  and parentId =1 and isEnable=1");
            query.setParameter(0, "%" + areaCode);
            lstReturn = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return lstReturn;
    }

    /**
     * Gets the staff.
     * 
     * @param groupId the group id
     * 
     * @return the staff
     */
    @SuppressWarnings("unchecked")
    public List getStaff(String groupId) {
        // Session session = null;
        ArrayList<StaffBO> lst = new ArrayList<StaffBO>();
        Session session = null;
        session = openThreadSession();
        try {
            String SQL = " SELECT "
                    + "   a, b.id.roleId "
                    + " FROM "
                    + "   StaffBO as a , "
                    + "   StaffInGroupBO as b "
                    + " WHERE a.isEnable=1 "
                    + "  and a.staffId = b.id.staffId "
                    + "   AND b.id.groupId = ?  order by a.name asc";

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
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return lst;
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
    @SuppressWarnings("unchecked")
    public static List<AreaBO> getListProvince(HttpServletRequest req) throws Exception {
        Session session = null;
        session = openThreadSession();
        try {
            String provinceCode =
                    req.getSession().getAttribute("ProvinceCode") != null ? (String) req.getSession().getAttribute("ProvinceCode") : "";

            String hql = " from AreaView a where lower(a.path) like ? ";
            Query qr = session.createQuery(hql);
            qr.setParameter(0, "%" + provinceCode.toLowerCase().trim() + "%");
            List lst = qr.list();
            AreaBO areaBO;
            ArrayList resultList = new ArrayList();
            AreaView ObjTemp;
            for (Object obj : lst) {
                ObjTemp = (AreaView) obj;
                areaBO = new AreaBO();
                areaBO.setAreaCode(ObjTemp.getAreaCode() == null ? "" : ObjTemp.getAreaCode());
                areaBO.setProvinceName(ObjTemp.getProvinceName() == null ? "" : ObjTemp.getProvinceName());
                resultList.add(areaBO);
            }
            return resultList;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Gets the lead staff.
     * 
     * @param groupId the group id
     * 
     * @return the lead staff
     */
    @SuppressWarnings("unchecked")
    public List getLeadStaff(String groupId) {
        // Session session = null;
        ArrayList<StaffBO> lst = new ArrayList<StaffBO>();
        Session session = null;
        session = openThreadSession();
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
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return lst;
    }

    @SuppressWarnings("unchecked")
    public List getListInterfaceCommonByType(String interfaceType) {
        List<InterInterfaceCommonBO> list = new ArrayList<InterInterfaceCommonBO>();
        if (!interfaceType.equals("")) {
            Session session = null;
            session = openThreadSession();
            String SQL = " FROM InterInterfaceCommonBO a WHERE a.interfaceTypeId = ? ";
            Query query = session.createQuery(SQL);
            query.setParameter(0, Long.parseLong(interfaceType));
            list=query.list();
        }
        return list;
    }
}

