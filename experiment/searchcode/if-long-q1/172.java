/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lifetek.netmosys.database.DAO;

import com.lifetek.netmosys.client.form.ConfigSendSMSForm;
import com.lifetek.netmosys.client.form.ConfigSendSmsSeriesFaultForm;
import com.lifetek.netmosys.database.core.BO.ConfigSendSmsIubBO;
import com.lifetek.netmosys.database.core.BO.CoreMobileConfigAlarm3GBO;
import com.lifetek.netmosys.database.core.BO.CoreMobileConfigAlarmBO;
import com.lifetek.netmosys.database.core.BO.CoreMobileConfigSendSmsViewBO;
import com.lifetek.netmosys.database.core.BO.CoreMobileConfigSmsSeriesFault3GBO;
import com.lifetek.netmosys.database.core.BO.CoreMobileConfigSmsSeriesFaultBO;
import com.lifetek.netmosys.database.core.BO.CoreMobileConfigVendor3GBO;
import com.lifetek.netmosys.util.Constant;
import com.lifetek.netmosys.util.QueryCryptUtils;
import com.lifetek.netmosys.util.ResourceBundleUtils;
import com.lifetek.netmosys.util.StringUtils;
import com.lifetek.database.BO.ActionResultBO;
import com.lifetek.database.config.BaseHibernateDAO;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionForm;
import org.hibernate.Session;
import org.hibernate.Query;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author DTL
 */
public class ConfigSendSmsDAO extends BaseHibernateDAO {

    public ActionResultBO prepageConfigSendSMS(ActionForm form, HttpServletRequest req) throws Exception {
        ConfigSendSMSForm configForm = (ConfigSendSMSForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from CoreMobileConfigAlarmBO where lower(type) =?");
        Query q = hibernateSession.createQuery(sqlSearch.toString());
        List lstPaAdmin = new ArrayList();
        if (QueryCryptUtils.getParameter(req, "type") == null) {
            lstPaAdmin.add("core");
        } else {
            lstPaAdmin.add("bsc");
        }
        q.setParameter(0, lstPaAdmin.get(0));
        List lst = q.list();

        CoreMobileConfigAlarmBO coreMobileConfigAlarmBO = new CoreMobileConfigAlarmBO();
        if (lst.size() > 0) {
            coreMobileConfigAlarmBO = (CoreMobileConfigAlarmBO) lst.get(0);
            Long hhmm = coreMobileConfigAlarmBO.getTimeToSendSms();
            Long mm = hhmm % 60;
            Long hh = (hhmm - mm) / 60;
            configForm.setTimeToSendHH(hh);
            configForm.setTimeToSendMM(mm);
            if (QueryCryptUtils.getParameter(req, "type") == null) {
                configForm.setNumberOfAppearL1(coreMobileConfigAlarmBO.getNumberOfAppearL1());
                configForm.setNumberOfAppearL2(coreMobileConfigAlarmBO.getNumberOfAppearL2());

                configForm.setCpuThresoldLoad1(coreMobileConfigAlarmBO.getCpuThresoldLoad1());
                configForm.setCpuThresoldLoad2(coreMobileConfigAlarmBO.getCpuThresoldLoad2());
            }
            configForm.setTimePersistL1(coreMobileConfigAlarmBO.getTimePersistL1());
            configForm.setTimePersistL2(coreMobileConfigAlarmBO.getTimePersistL2());

            configForm.setCpuLoadThreshold(coreMobileConfigAlarmBO.getCpuLoadThreshold() != null ? coreMobileConfigAlarmBO.getCpuLoadThreshold().toString() : "");
            configForm.setPermitPooltrapTotal(coreMobileConfigAlarmBO.getPermitPoolTrapTotal() != null ? coreMobileConfigAlarmBO.getPermitPoolTrapTotal().toString() : "");
            configForm.setSuddenPoolTrapTotalThreshold(coreMobileConfigAlarmBO.getSuddenPoolTrapTotalThreshold() != null ? coreMobileConfigAlarmBO.getSuddenPoolTrapTotalThreshold().toString() : "");


        }
        ActionResultBO actionResult = new ActionResultBO();
        if (QueryCryptUtils.getParameter(req, "type") == null) {
            actionResult.setPageForward(Constant.CORE_MOBILE.CONFIG_SEND_SMS);
        } else {
            actionResult.setPageForward(Constant.CORE_MOBILE.CONFIG_SEND_SMS_BSC);
        }
        return actionResult;
    }

    public ActionResultBO updateConfigSendSMS(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        ConfigSendSMSForm configSendSMSForm = (ConfigSendSMSForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from CoreMobileConfigAlarmBO where lower(type) =?");
        Query q = hibernateSession.createQuery(sqlSearch.toString());
        List lstPaAdmin = new ArrayList();
        if (QueryCryptUtils.getParameter(req, "type") == null) {
            lstPaAdmin.add("core");
        } else {
            lstPaAdmin.add("bsc");
        }
        q.setParameter(0, lstPaAdmin.get(0));
        List lst = q.list();
        CoreMobileConfigAlarmBO coreMobileConfigAlarmBO = null;
        if (lst != null && lst.size() > 0) {
            coreMobileConfigAlarmBO = (CoreMobileConfigAlarmBO) lst.get(0);
        } else {
            coreMobileConfigAlarmBO = new CoreMobileConfigAlarmBO();
            if (QueryCryptUtils.getParameter(req, "type") == null) {
               coreMobileConfigAlarmBO.setType(Constant.COMMON.NETWORK_LAYER.CORE);
            } else {
                coreMobileConfigAlarmBO.setType(Constant.COMMON.EXCHANGE_TYPE.BSC_TEXT);
            }
        }



        try {
            if (QueryCryptUtils.getParameter(req, "type") == null) {
                coreMobileConfigAlarmBO.setNumberOfAppearL1(configSendSMSForm.getNumberOfAppearL1());
                coreMobileConfigAlarmBO.setNumberOfAppearL2(configSendSMSForm.getNumberOfAppearL2());
                // new
                coreMobileConfigAlarmBO.setCpuThresoldLoad1(configSendSMSForm.getCpuThresoldLoad1());
                coreMobileConfigAlarmBO.setCpuThresoldLoad2(configSendSMSForm.getCpuThresoldLoad2());
            } else {
                if (StringUtils.isNotNull(configSendSMSForm.getCpuLoadThreshold())) {
                    coreMobileConfigAlarmBO.setCpuLoadThreshold(Float.valueOf(configSendSMSForm.getCpuLoadThreshold()));
                }
                if (StringUtils.isNotNull(configSendSMSForm.getSuddenPoolTrapTotalThreshold())) {
                    coreMobileConfigAlarmBO.setSuddenPoolTrapTotalThreshold(Float.valueOf(configSendSMSForm.getSuddenPoolTrapTotalThreshold()));
                }
                if (StringUtils.isNotNull(configSendSMSForm.getPermitPooltrapTotal())) {
                    coreMobileConfigAlarmBO.setPermitPoolTrapTotal(Float.valueOf(configSendSMSForm.getPermitPooltrapTotal()));
                }
            }
            coreMobileConfigAlarmBO.setTimePersistL1(configSendSMSForm.getTimePersistL1());
            coreMobileConfigAlarmBO.setTimePersistL2(configSendSMSForm.getTimePersistL2());
            Long hh = configSendSMSForm.getTimeToSendHH() * 60;
            Long mm = configSendSMSForm.getTimeToSendMM();
            coreMobileConfigAlarmBO.setTimeToSendSms(hh + mm);


            hibernateSession.save(coreMobileConfigAlarmBO);
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("coremobile.update.success"));
            actionResult.setPageForward(Constant.CORE_MOBILE.RESULT_SEND_SMS);
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("coremobile.update.fail"));
        }
        return actionResult;
    }

    public ActionResultBO prepageConfigSendSmsRnc(ActionForm form, HttpServletRequest req) throws Exception {
        ConfigSendSMSForm configForm = (ConfigSendSMSForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from CoreMobileConfigSendSmsViewBO order by id");
        Query q = hibernateSession.createQuery(sqlSearch.toString());
        List lst = q.list();

        CoreMobileConfigSendSmsViewBO coreMobileConfigSendSmsViewBO = new CoreMobileConfigSendSmsViewBO();
        if (lst.size() > 0) {
            coreMobileConfigSendSmsViewBO = (CoreMobileConfigSendSmsViewBO) lst.get(0);
            Long hhmm = coreMobileConfigSendSmsViewBO.getTimeToSendSms();
            Long mm = hhmm % 60;
            Long hh = (hhmm - mm) / 60;
            configForm.setTimeToSendHH(hh);
            configForm.setTimeToSendMM(mm);
            configForm.setTimePersistL1(coreMobileConfigSendSmsViewBO.getTimePersistL1());
            configForm.setTimePersistL2(coreMobileConfigSendSmsViewBO.getTimePersistL2());
            configForm.setEricssonLoadThreshold(coreMobileConfigSendSmsViewBO.getAlarmLoadThreshold());
            coreMobileConfigSendSmsViewBO = (CoreMobileConfigSendSmsViewBO) lst.get(1); //nokia
            configForm.setNokiaLoadThreshold(coreMobileConfigSendSmsViewBO.getAlarmLoadThreshold());
            coreMobileConfigSendSmsViewBO = (CoreMobileConfigSendSmsViewBO) lst.get(2); //Huawei
            configForm.setHuaweiLoadThreshold(coreMobileConfigSendSmsViewBO.getAlarmLoadThreshold());
        }
        ActionResultBO actionResult = new ActionResultBO();
        actionResult.setPageForward(Constant.CORE_MOBILE.CONFIG_SEND_SMS_RNC);

        return actionResult;
    }

    public ActionResultBO updateConfigSendSmsRnc(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        ConfigSendSMSForm configSendSmsForm = (ConfigSendSMSForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch1 = new StringBuffer();
        sqlSearch1.append(" from CoreMobileConfigAlarm3GBO ");
        Query q = hibernateSession.createQuery(sqlSearch1.toString());
        List lst = q.list();

        CoreMobileConfigAlarm3GBO coreMobileConfigAlarm3GBO = null;
        if (lst != null && lst.size() > 0) {
            coreMobileConfigAlarm3GBO = (CoreMobileConfigAlarm3GBO) lst.get(0);
        } else {
            coreMobileConfigAlarm3GBO = new CoreMobileConfigAlarm3GBO();
        }
//         coreMobileConfigAlarm3GBO = (CoreMobileConfigAlarm3GBO) lst.get(0);
        coreMobileConfigAlarm3GBO.setTimePersistL1(configSendSmsForm.getTimePersistL1());
        coreMobileConfigAlarm3GBO.setTimePersistL2(configSendSmsForm.getTimePersistL2());
        coreMobileConfigAlarm3GBO.setTimeToSendSms(configSendSmsForm.getTimeToSendHH() * 60 + configSendSmsForm.getTimeToSendMM());
        hibernateSession.save(coreMobileConfigAlarm3GBO);

        StringBuffer sqlSearch2 = new StringBuffer();
        sqlSearch2.append(" from CoreMobileConfigVendor3GBO order by id");
        Query q2 = hibernateSession.createQuery(sqlSearch2.toString());
        List listVendor = q2.list();

        CoreMobileConfigVendor3GBO ericsson = null;
        CoreMobileConfigVendor3GBO nokia = null;
        CoreMobileConfigVendor3GBO huawei = null;
        if (lst != null && lst.size() > 0) {
            ericsson = (CoreMobileConfigVendor3GBO) listVendor.get(0);
            nokia = (CoreMobileConfigVendor3GBO) listVendor.get(1);
            huawei = (CoreMobileConfigVendor3GBO) listVendor.get(2);
        } else {
            ericsson = new CoreMobileConfigVendor3GBO();
            ericsson.setType(Constant.COMMON.EXCHANGE_TYPE.ERICSSON);
            nokia = new CoreMobileConfigVendor3GBO();
            nokia.setType(Constant.COMMON.EXCHANGE_TYPE.NOKIA);
            huawei = new CoreMobileConfigVendor3GBO();
            huawei.setType(Constant.COMMON.EXCHANGE_TYPE.HUAWEI);
        }

        ericsson.setAlarmLoadThreshold(configSendSmsForm.getEricssonLoadThreshold());
        nokia.setAlarmLoadThreshold(configSendSmsForm.getNokiaLoadThreshold());
        huawei.setAlarmLoadThreshold(configSendSmsForm.getHuaweiLoadThreshold());
        hibernateSession.save(ericsson);
        hibernateSession.save(nokia);
        hibernateSession.save(huawei);

        req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("coremobile.update.success"));
        actionResult.setPageForward(Constant.CORE_MOBILE.RESULT_SEND_SMS);

        return actionResult;
    }

    public ActionResultBO prepageConfigSendSmsSeriesFault(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from CoreMobileConfigSendSmsSeriesFaultViewBO order by id");
        Query q = hibernateSession.createQuery(sqlSearch.toString());
        List lst = q.list();
        if (StringUtils.checkParameterNotNull(req, "updateStatus")) {
            req.getSession().setAttribute("updateStatus", "true");
        }
        req.setAttribute("listConfigSmsSeriesFault", lst);
//        AddressDAO.getFaultGroupList(req);
        Query q1 = getSession().createQuery("from FaultGroupBO where type = ? order by name");
        q1.setParameter(0, "BTS");
        req.setAttribute(Constant.COMMON.SET_COMMON_REQUEST.FAULT_GROUP_LIST, q1.list());
        if (QueryCryptUtils.getParameter(req, "ajax") == null || "".equals(QueryCryptUtils.getParameter(req, "ajax").trim())) {
            actionResult.setPageForward(Constant.CORE_MOBILE.SHOW_CONFIG_SEND_SMS_SERIES_FAULT);
        } else {
            actionResult.setPageForward(Constant.CORE_MOBILE.LIST_CONFIG_SEND_SMS_SERIES_FAULT);
        }
        return actionResult;
    }

    public ActionResultBO updateConfigSendSmsSeriesFault(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        ConfigSendSmsSeriesFaultForm configSendSmsForm = (ConfigSendSmsSeriesFaultForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch1 = new StringBuffer();
        sqlSearch1.append(" from CoreMobileConfigSmsSeriesFaultBO where faultGroupId =? ");
        List lstPaAdmin = new ArrayList();
        lstPaAdmin.add(configSendSmsForm.getFaultGroupId());
        Query q = hibernateSession.createQuery(sqlSearch1.toString());
        q.setParameter(0, lstPaAdmin.get(0));
        List lst = q.list();
        if (!lst.isEmpty()) {
            CoreMobileConfigSmsSeriesFaultBO configObj = (CoreMobileConfigSmsSeriesFaultBO) lst.get(0);
            configObj.setNumberOfBts(configSendSmsForm.getNumberOfBts());
            configObj.setStartTime(configSendSmsForm.getStartTime());
            configObj.setStopTime(configSendSmsForm.getStopTime());
            hibernateSession.save(configObj);
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("coremobile.update.success"));
        } else {
            CoreMobileConfigSmsSeriesFaultBO configObj = new CoreMobileConfigSmsSeriesFaultBO();
            configObj.setFaultGroupId(configSendSmsForm.getFaultGroupId());
            configObj.setNumberOfBts(configSendSmsForm.getNumberOfBts());
            configObj.setStartTime(configSendSmsForm.getStartTime());
            configObj.setStopTime(configSendSmsForm.getStopTime());
            hibernateSession.save(configObj);
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("common.addSuccess"));
        }
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from CoreMobileConfigSendSmsSeriesFaultViewBO order by id");
        Query q2 = hibernateSession.createQuery(sqlSearch.toString());
        List list = q2.list();

        req.setAttribute("listConfigSmsSeriesFault", list);
        actionResult.setPageForward(Constant.CORE_MOBILE.LIST_CONFIG_SEND_SMS_SERIES_FAULT);

        return actionResult;
    }

    public ActionResultBO deleteConfigSendSmsSeriesFault(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        ConfigSendSmsSeriesFaultForm formDelete = (ConfigSendSmsSeriesFaultForm) form;
        String[] ids = formDelete.getCurrentIdArray();
        Session hibernateSession = getSession();
        try {
            for (int i = 0; i < ids.length; i++) {
                Long id = Long.parseLong(ids[i]);
                CoreMobileConfigSmsSeriesFaultBO receiverSmsBO = (CoreMobileConfigSmsSeriesFaultBO) hibernateSession.get(CoreMobileConfigSmsSeriesFaultBO.class.getName(), id);

                hibernateSession.delete(receiverSmsBO);
            }
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("common.deleteSuccess"));
            actionResult.setPageForward(Constant.CORE_MOBILE.LIST_CONFIG_SEND_SMS_SERIES_FAULT);
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("faultManagement.close.fail"));
            actionResult.setPageForward(Constant.CORE_MOBILE.LIST_CONFIG_SEND_SMS_SERIES_FAULT);
        }
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from CoreMobileConfigSendSmsSeriesFaultViewBO order by id");
        Query q2 = hibernateSession.createQuery(sqlSearch.toString());
        List list = q2.list();
        req.setAttribute("listConfigSmsSeriesFault", list);
        return actionResult;
    }

    public ActionResultBO prepageConfigSendSmsSeriesFault3G(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from CoreMobileConfigSendSmsSeriesFaultView3GBO order by id");
        Query q = hibernateSession.createQuery(sqlSearch.toString());
        List lst = q.list();
        if (StringUtils.checkParameterNotNull(req, "updateStatus")) {
            req.getSession().setAttribute("updateStatus", "true");
        }
        req.setAttribute("listConfigSmsSeriesFault", lst);
        Query q1 = getSession().createQuery("from FaultGroup3GBO where upper(type) = ? order by name");
        q1.setParameter(0, "NODEB");
        req.setAttribute(Constant.COMMON.SET_COMMON_REQUEST.FAULT_GROUP_LIST, q1.list());

        if (QueryCryptUtils.getParameter(req, "ajax") == null || "".equals(QueryCryptUtils.getParameter(req, "ajax").trim())) {
            actionResult.setPageForward(Constant.CORE_MOBILE.SHOW_CONFIG_SEND_SMS_SERIES_FAULT_3G);
        } else {
            actionResult.setPageForward(Constant.CORE_MOBILE.LIST_CONFIG_SEND_SMS_SERIES_FAULT_3G);
        }
        return actionResult;
    }

    public ActionResultBO updateConfigSendSmsSeriesFault3G(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        ConfigSendSmsSeriesFaultForm configSendSmsForm = (ConfigSendSmsSeriesFaultForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch1 = new StringBuffer();
        sqlSearch1.append(" from CoreMobileConfigSmsSeriesFault3GBO where faultGroupId =? ");
        List lstPaAdmin = new ArrayList();
        lstPaAdmin.add(configSendSmsForm.getFaultGroupId());
        Query q = hibernateSession.createQuery(sqlSearch1.toString());
        q.setParameter(0, lstPaAdmin.get(0));
        List lst = q.list();
        if (!lst.isEmpty()) {
            CoreMobileConfigSmsSeriesFault3GBO configObj = (CoreMobileConfigSmsSeriesFault3GBO) lst.get(0);
            configObj.setNumberOfBts(configSendSmsForm.getNumberOfBts());
            configObj.setStartTime(configSendSmsForm.getStartTime());
            configObj.setStopTime(configSendSmsForm.getStopTime());
            hibernateSession.save(configObj);
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("coremobile.update.success"));
        } else {
            CoreMobileConfigSmsSeriesFault3GBO configObj = new CoreMobileConfigSmsSeriesFault3GBO();
            configObj.setFaultGroupId(configSendSmsForm.getFaultGroupId());
            configObj.setNumberOfBts(configSendSmsForm.getNumberOfBts());
            configObj.setStartTime(configSendSmsForm.getStartTime());
            configObj.setStopTime(configSendSmsForm.getStopTime());
            hibernateSession.save(configObj);
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("common.addSuccess"));
        }
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from CoreMobileConfigSendSmsSeriesFaultView3GBO order by id");
        Query q2 = hibernateSession.createQuery(sqlSearch.toString());
        List list = q2.list();

        req.setAttribute("listConfigSmsSeriesFault", list);
        actionResult.setPageForward(Constant.CORE_MOBILE.LIST_CONFIG_SEND_SMS_SERIES_FAULT_3G);

        return actionResult;
    }

    public ActionResultBO deleteConfigSendSmsSeriesFault3G(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        ConfigSendSmsSeriesFaultForm formDelete = (ConfigSendSmsSeriesFaultForm) form;
        String[] ids = formDelete.getCurrentIdArray();
        Session hibernateSession = getSession();
        try {
            for (int i = 0; i < ids.length; i++) {
                Long id = Long.parseLong(ids[i]);
                CoreMobileConfigSmsSeriesFault3GBO receiverSmsBO = (CoreMobileConfigSmsSeriesFault3GBO) hibernateSession.get(CoreMobileConfigSmsSeriesFault3GBO.class.getName(), id);

                hibernateSession.delete(receiverSmsBO);
            }
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("common.deleteSuccess"));
            actionResult.setPageForward(Constant.CORE_MOBILE.LIST_CONFIG_SEND_SMS_SERIES_FAULT_3G);
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("faultManagement.close.fail"));
            actionResult.setPageForward(Constant.CORE_MOBILE.LIST_CONFIG_SEND_SMS_SERIES_FAULT_3G);
        }
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from CoreMobileConfigSendSmsSeriesFaultView3GBO order by id");
        Query q2 = hibernateSession.createQuery(sqlSearch.toString());
        List list = q2.list();
        req.setAttribute("listConfigSmsSeriesFault", list);
        return actionResult;
    }

    public ActionResultBO prepageConfigSendSmsIub(ActionForm form, HttpServletRequest req) throws Exception {
        ///////////////////////////////////////////////////////
        ConfigSendSMSForm configForm = (ConfigSendSMSForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from ConfigSendSmsIubBO where id = 1");
        Query q = hibernateSession.createQuery(sqlSearch.toString());

        List lst = q.list();

        ConfigSendSmsIubBO objConfig = new ConfigSendSmsIubBO();
        if (lst.size() > 0) {
            objConfig = (ConfigSendSmsIubBO) lst.get(0);
            Long hhmm = objConfig.getTimeSendSms();
            Long mm = hhmm % 60;
            Long hh = (hhmm - mm) / 60;
            configForm.setTimeToSendHH(hh);
            configForm.setTimeToSendMM(mm);
            configForm.setTimeToGetData(objConfig.getTimeToGetData());
            configForm.setFlowUsedLevel1(objConfig.getFlowUsedLevel1());
            configForm.setFlowUsedLevel2(objConfig.getFlowUsedLevel2());
            configForm.setFlowUsedByDay(objConfig.getFlowUsedByDay());
            configForm.setNodeBCongestionNumber(objConfig.getNodeBCongestionNumber());
        }
        ActionResultBO actionResult = new ActionResultBO();
        actionResult.setPageForward(Constant.CORE_MOBILE.CONFIG_SEND_SMS_IUB);
        return actionResult;
    }

    public ActionResultBO updateConfigSendSmsIub(ActionForm form, HttpServletRequest req) throws Exception {
        ActionResultBO actionResult = new ActionResultBO();
        ConfigSendSMSForm configSendSMSForm = (ConfigSendSMSForm) form;
        Session hibernateSession = getSession();
        StringBuffer sqlSearch = new StringBuffer();
        sqlSearch.append(" from ConfigSendSmsIubBO ");
        Query q = hibernateSession.createQuery(sqlSearch.toString());
        List lst = q.list();
        ConfigSendSmsIubBO configSendSmsIubBO;
        if (lst.size() > 0) {
            configSendSmsIubBO = (ConfigSendSmsIubBO) lst.get(0);
        } else {
            configSendSmsIubBO = new ConfigSendSmsIubBO();
        }

        try {
            Long hh = configSendSMSForm.getTimeToSendHH() * 60;
            Long mm = configSendSMSForm.getTimeToSendMM();
            configSendSmsIubBO.setTimeSendSms(hh + mm);
            configSendSmsIubBO.setTimeToGetData(configSendSMSForm.getTimeToGetData());
            configSendSmsIubBO.setFlowUsedLevel1(configSendSMSForm.getFlowUsedLevel1());
            configSendSmsIubBO.setFlowUsedLevel2(configSendSMSForm.getFlowUsedLevel2());
            configSendSmsIubBO.setFlowUsedByDay(configSendSMSForm.getFlowUsedByDay());
            configSendSmsIubBO.setNodeBCongestionNumber(configSendSMSForm.getNodeBCongestionNumber());
            hibernateSession.save(configSendSmsIubBO);
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("coremobile.update.success"));
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("result", new ResourceBundleUtils().getResource(req).getString("coremobile.update.fail"));
        }
        actionResult.setPageForward(Constant.CORE_MOBILE.RESULT_SEND_SMS_IUB);
        return actionResult;
    }
}

