/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lifetek.netmosys.database.DAO;

import com.lifetek.netmosys.client.form.CataloguesForm;
import com.lifetek.netmosys.database.BO.CataloguesBO;
import com.lifetek.netmosys.database.BO.ViewCataloguesBO;
import com.lifetek.netmosys.util.Constant;
import com.lifetek.netmosys.util.QueryCryptUtils;
import com.lifetek.netmosys.util.ResourceBundleUtils;
import com.lifetek.netmosys.util.StringUtils;
import com.lifetek.database.BO.ActionResultBO;
import com.lifetek.database.config.BaseHibernateDAO;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionForm;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Query;

/**
 *
 * @author DTL
 */
public class CataloguesDAO extends BaseHibernateDAO {

    public ActionResultBO getCataloguesList(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        String[] permitedRole = {
            Constant.ASSSIGN_TASK.TTDHKT,
            Constant.COMMON.AREA_CODE_KV.KV1,
            Constant.COMMON.AREA_CODE_KV.KV2,
            Constant.COMMON.AREA_CODE_KV.KV3
        };
        actionResult = AddressDAO.checkPermission(actionResult, req, permitedRole);
        if (actionResult == null) {
            actionResult = new ActionResultBO();
        } else {
            return actionResult;
        }

        if (StringUtils.checkParameterNotNull(req, "updateStatus")) {
            req.getSession().setAttribute("updateStatus", "true");
        }
        List listCatalogues = getList(form, req);
        req.setAttribute("listCatalogues", listCatalogues);

        //list cho combobox
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from ViewCataloguesBO where id<>? ");
        Query q = hibernateSession.createQuery(sqlSearch.toString());
        q.setParameter(0, 1L);
        List catalogList = q.list();
        req.setAttribute("cataloguesList", catalogList);
        if (QueryCryptUtils.getParameter(req, "ajax") == null || "".equals(QueryCryptUtils.getParameter(req, "ajax").trim())) {
            actionResult.setPageForward(Constant.CATALOGUES.SHOW_CATALOGUES);
        } else {
            actionResult.setPageForward(Constant.CATALOGUES.LIST_CATALOGUES);
        }
        return actionResult;
    }

    private List getList(ActionForm form, HttpServletRequest req) throws Exception {
        CataloguesForm catalogForm = (CataloguesForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from ViewCataloguesBO where id<>? ");

        if (catalogForm.getParentId() != null && req.getSession().getAttribute("delete") == null) {
            sqlSearch.append(" and ordering like ?");
        }
        Query q = hibernateSession.createQuery(sqlSearch.toString());
        q.setParameter(0, 1L);
        if (catalogForm.getParentId() != null && req.getSession().getAttribute("delete") == null) {
            ViewCataloguesBO catalogObj = (ViewCataloguesBO) hibernateSession.get(ViewCataloguesBO.class.getName(), catalogForm.getParentId());
            q.setParameter(1, catalogObj.getOrdering() + "%");
        }

        List list = q.list();
        return list;
    }

    public ActionResultBO deleteCatalog(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        CataloguesForm formDelete = (CataloguesForm) form;
        String[] ids = formDelete.getCurrentIdArray();
        Session hibernateSession = getSession();
        String result = "";
        try {
            for (int i = 0; i < ids.length; i++) {
                Long id = Long.parseLong(ids[i].split(",")[0]);
                CataloguesBO catalogObj = (CataloguesBO) hibernateSession.get(CataloguesBO.class.getName(), id);
                StringBuffer sqlSearch = new StringBuffer();
                sqlSearch.append(" select count(*) from ViewCataloguesBO where parentId =?");
                Query q = hibernateSession.createQuery(sqlSearch.toString());
                q.setParameter(0, id);
                int count = Integer.parseInt(q.list().get(0).toString());
                if (count == 0) {
                    hibernateSession.delete(catalogObj);
                    result = new ResourceBundleUtils().getResource(req).getString("bsc.DAO.delete.success");
                    if (id.equals(formDelete.getParentId())) {
                        req.getSession().setAttribute("delete", "true");
                    }
                } else {
                    result = new ResourceBundleUtils().getResource(req).getString("coremobile.add.fail");
                    break;
                }
            }
            req.getSession().setAttribute("updateStatus", "");
            req.getSession().setAttribute("result", result);
            actionResult.setPageForward(Constant.CATALOGUES.LIST_CATALOGUES);
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("updateStatus", "");
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("coremobile.add.fail"));
            actionResult.setPageForward(Constant.CATALOGUES.LIST_CATALOGUES);
        }
        return actionResult;
    }

    public ActionResultBO preAddCatalog(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();

        sqlSearch.append(" from ViewCataloguesBO  where id<>? ");

        Query q = hibernateSession.createQuery(sqlSearch.toString());
        q.setParameter(0, 1L);
        List list = q.list();
        req.setAttribute("listCatalogues", list);
        actionResult.setPageForward(Constant.CATALOGUES.ADD_CATALOGUES);
        return actionResult;
    }

    public ActionResultBO addCatalog(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        CataloguesForm catalogForm = (CataloguesForm) form;
        try {
            CataloguesBO catalogObj = new CataloguesBO();
            catalogObj.setName(catalogForm.getName());
            catalogObj.setDescription(catalogForm.getDescription());
            catalogObj.setParentId(catalogForm.getParentId());
            catalogObj.setCatalogValue(catalogForm.getCatalogValue());

            Session hibernateSession = getSession();
            Long parentId = catalogForm.getParentId();
            ViewCataloguesBO parentObj = (ViewCataloguesBO) hibernateSession.get(ViewCataloguesBO.class.getName(), parentId);
            String ordering = parentObj.getOrdering() + ".";
            Long seqId = getSequence(" SQ_CATALOGUES ");
            catalogObj.setOrdering(ordering + seqId);
            catalogObj.setId(seqId);
            //StringBuffer sqlSearch = new StringBuffer();
            //sqlSearch.append(" select count(*) from ViewCataloguesBO where parentId =?");
            //StringBuffer sqlSearch2 = new StringBuffer();
            //sqlSearch2.append(" from ViewCataloguesBO where id =?");

            //Query q = hibernateSession.createQuery(sqlSearch.toString());
            //q.setParameter(0, parentId);
            //Query q2 = hibernateSession.createQuery(sqlSearch2.toString());
            //q2.setParameter(0, parentId);
            //int count = Integer.parseInt(q.list().get(0).toString());
            //String ordering = ((ViewCataloguesBO) q2.list().get(0)).getOrdering();


            hibernateSession.save(catalogObj);
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("common.addSuccess"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        actionResult.setPageForward(Constant.COMMON.UPDATE_AND_CLOSE);
        return actionResult;
    }

    public ActionResultBO prePageEditCatalog(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        CataloguesForm catalogForm = (CataloguesForm) form;
        Long id = Long.parseLong(catalogForm.getCurrentIdArray()[0].split(",")[0]);
        String parentOrdering = catalogForm.getCurrentIdArray()[0].split(",")[1];
        Session hibernateSession = getSession();
        StringBuffer sqlDownList = new StringBuffer();
        sqlDownList.append(" from ViewCataloguesBO where ordering not like ? and id <> ?");
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from ViewCataloguesBO where id =?");
        Query q = hibernateSession.createQuery(sqlSearch.toString());
        Query q2 = hibernateSession.createQuery(sqlDownList.toString());
        q.setParameter(0, id);
        q2.setParameter(0, parentOrdering + "%");
        q2.setParameter(1, 1L);
        ViewCataloguesBO catalogObj = (ViewCataloguesBO) q.list().get(0);
        catalogForm.setId(catalogObj.getId());
        catalogForm.setName(catalogObj.getName());
        catalogForm.setDescription(catalogObj.getDescription());
        catalogForm.setParentId(catalogObj.getParentId());
        List list = q2.list();
        req.setAttribute("listCatalogues", list);
        actionResult.setPageForward(Constant.CATALOGUES.EDIT_CATALOGUES);
        return actionResult;
    }

    public ActionResultBO updateCatalog(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        CataloguesForm catalogForm = (CataloguesForm) form;
        try {
            Session hibernateSession = getSession();
            Long parentId = catalogForm.getParentId();
            Long catalogId = catalogForm.getId();
            CataloguesBO catalogObj = (CataloguesBO) hibernateSession.get(CataloguesBO.class.getName(), catalogId);
            catalogObj.setDescription(catalogForm.getDescription());
            catalogObj.setParentId(parentId);
            ViewCataloguesBO parentObj = (ViewCataloguesBO) hibernateSession.get(ViewCataloguesBO.class.getName(), parentId);
            String parentOrdering = parentObj.getOrdering() + ".";
            String currentOrdering = catalogObj.getOrdering();
            Long seqId = getSequence(" SQ_CATALOGUES ");
            String newOrdering = parentOrdering + seqId;
            catalogObj.setOrdering(newOrdering);
            hibernateSession.save(catalogObj);

            //set lai ordering cho cac muc con cua muc hien tai neu co
            StringBuffer sqlSearch = new StringBuffer();
            sqlSearch.append(" from CataloguesBO where parentId = ?");
            Query q = hibernateSession.createQuery(sqlSearch.toString());
            q.setParameter(0, catalogId);
            List lst = q.list();
            for (int i = 0; i < lst.size(); i++) {
                CataloguesBO obj = (CataloguesBO) lst.get(i);
                String objOrdering = obj.getOrdering();
                objOrdering = objOrdering.replaceAll(currentOrdering, newOrdering);
                obj.setOrdering(objOrdering);
                hibernateSession.save(obj);
            }

            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("common.updateSuccess"));
            //req.getSession().setAttribute("updateStatus", new ResourceBundleUtils().getResource(req).getString("common.updateSuccess"));

        } catch (Exception ex) {
            ex.printStackTrace();
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("tktu.networkQuality.DAO.updateError"));
            req.getSession().setAttribute("updateStatus", new ResourceBundleUtils().getResource(req).getString("tktu.networkQuality.DAO.updateError"));
        }
        actionResult.setPageForward(Constant.COMMON.UPDATE_AND_CLOSE);
        return actionResult;
    }
}

