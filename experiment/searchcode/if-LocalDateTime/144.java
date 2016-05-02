package by.q64.promo.utils.pcabinet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.q64.promo.dao.BaseRequest;
import by.q64.promo.data.Action;
import by.q64.promo.data.Situation;
import by.q64.promo.domain.AppUser;
import by.q64.promo.domain.QFComplexData;
import by.q64.promo.domain.QFForm;
import by.q64.promo.domain.ReportView;
import by.q64.promo.domain.Flow;
import by.q64.promo.domain.FlowInterface;
import by.q64.promo.domain.FlowLog;
import by.q64.promo.domain.HemReport;
import by.q64.promo.domain.QFFormTemplate;
import by.q64.promo.domain.Region;
import by.q64.promo.domain.ReportPromoter;
import by.q64.promo.domain.ReportSupervisor;
import by.q64.promo.domain.UnitRegion;
import by.q64.promo.utils.form.PromoFormCheckException;
import by.q64.promo.utils.form.ReportConverter;
import by.q64.promo.view.domain.HemReportView;
import by.q64.promo.view.domain.ReportPromoterView;
import by.q64.promo.view.domain.ReportSupervisorView;

@Service
@Transactional
public class ReportCabinet {

    @Autowired
    BaseRequest baseRequest;

    @Autowired
    ReportConverter rc;
    @Autowired
    by.q64.promo.utils.workflow.AndrewService service;

    Logger logger = LoggerFactory.getLogger(getClass());

    public ReportView getPromoterTaskView(String email, int accepted, LocalDateTime dateUntill, LocalDateTime dateTill, String search) {
        List<Flow> flows = new ArrayList<>();
        List<FlowLog> flowLogs = new ArrayList<>();

        Organaiser organaiser = new Organaiser(email);
        boolean manager = organaiser.setFlows(flows, flowLogs, accepted, Situation.getFirstPartDituation(), QFFormTemplate.PROMOTER_FORM, dateTill, dateUntill,
                search);

        List<FlowInterface> tasks = new ArrayList<>();
        tasks.addAll(flows);
        tasks.addAll(flowLogs);
        ReportView result = new ReportView();
        result.setPromoterSetting();
        rc.getPromoterTaskView(result, tasks, accepted, manager);
        return result;
    }

//    public List<ReportPromoterView> getReportTasksPromoter(String email, int accepted) {
//        List<ReportPromoterView> reportPromoterViews = new ArrayList<ReportPromoterView>();
//        List<Flow> flows = new ArrayList<Flow>();
//        List<FlowLog> flowLogs = new ArrayList<FlowLog>();
//
//        Organaiser organaiser = new Organaiser(email);
//        organaiser.setFlows(flows, flowLogs, accepted, Situation.getFirstPartDituation(), QFFormTemplate.PROMOTER_FORM, null, null, null);
//        for (Flow flow : flows) {
//            ReportPromoterView report = getReportPromoterView(flow, email);
//            if (report != null) {
//                reportPromoterViews.add(report);
//            }
//        }
//
//        for (FlowLog flowLog : flowLogs) {
//            ReportPromoterView report = getReportPromoterView(flowLog.getFlow_id(), email, null);
//            if (report != null) {
//                reportPromoterViews.add(report);
//            }
//        }
//        return reportPromoterViews;
//    }

//    private ReportPromoterView getReportPromoterView(int flow_id, String email, List<Action> actions) {
//        ReportPromoter reportPromoter = null;
//        try {
//            reportPromoter = baseRequest.getEntity(ReportPromoter.class, flow_id);
//            if (reportPromoter == null) {
//                reportPromoter = rc.convertPromoter(flow_id, email);
//                baseRequest.saveOrUpdate(reportPromoter);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            reportPromoter = rc.convertPromoter(flow_id, email);
//            baseRequest.saveOrUpdate(reportPromoter);
//        }
//        ReportPromoterView reportPromoterView = new ReportPromoterView(reportPromoter, actions);
//        return reportPromoterView;
//    }

//    private ReportPromoterView getReportPromoterView(Flow flow, String email) {
//        Situation situation = Situation.getFromId(flow.getSituation());
//        if (situation.getFormTemplate() == QFFormTemplate.PROMOTER_FORM) {
//            List<Action> actions = situation.getActionsTo();
//            return getReportPromoterView(flow.getId(), email, actions);
//        } else {
//            return null;
//        }
//
//    }

//    public List<ReportSupervisorView> getReportTasksSupervisor(String email, int accepted) {
//        List<ReportSupervisorView> reportSupervisorViews = new ArrayList<ReportSupervisorView>();
//        List<Flow> flows = new ArrayList<Flow>();
//        List<FlowLog> flowLogs = new ArrayList<FlowLog>();
//        Organaiser organaiser = new Organaiser(email);
//        organaiser.setFlows(flows, flowLogs, accepted, Situation.getFirstPartDituation(), QFFormTemplate.SUPERVISOR_FORM, null, null, null);
//        for (Flow flow : flows)
//            reportSupervisorViews.addAll(getReportSupervisorView(flow, email));
//
//        for (FlowLog flowLog : flowLogs)
//            reportSupervisorViews.addAll(getReportSupervisorView(flowLog.getFlow_id(), email, null));
//
//        return reportSupervisorViews;
//    }

//    private List<ReportSupervisorView> getReportSupervisorView(int flow_id, String email, List<Action> actions) {
//        List<ReportSupervisorView> reportSupervisorViews = new ArrayList<ReportSupervisorView>();
//        List<ReportSupervisor> reportSupervisors = rc.convertSupervisor(flow_id);
//        int size = reportSupervisors.size();
//        for (int i = 0; i < size; i++) {
//            ReportSupervisor reportSupervisor = reportSupervisors.get(i);
//            ReportSupervisorView e = null;
//
//            if (i != size - 1) {
//                e = new ReportSupervisorView(reportSupervisor, null);
//            } else {
//                e = new ReportSupervisorView(reportSupervisor, actions);
//            }
//
//            reportSupervisorViews.add(e);
//        }
//        return reportSupervisorViews;
//    }

//    private List<ReportSupervisorView> getReportSupervisorView(Flow flow, String email) {
//        Situation situation = Situation.getFromId(flow.getSituation());
//        List<Action> actions = situation.getActionsTo();
//        return getReportSupervisorView(flow.getId(), email, actions);
//    }

    public List<HemReport> getHemReports(String email, int state, int type,boolean manager) {
        AppUser user = baseRequest.getUserFromEmail(email);
        List<HemReport> reports = null;
        switch (user.getRole()) {
        case AppUser.HEAD_PROMO_DEPARTMENT:
        case AppUser.MANAGER:
            if(manager)
                reports = baseRequest.getListEntity(HemReport.class, "state", state, "type", type);
            else{
                List<Region> regions=baseRequest.getListEntity(Region.class,"coordinator",user.getId());
                reports=new ArrayList<HemReport>();
                for(Region region:regions){
                    reports.addAll(baseRequest.getListEntity(HemReport.class, "region", region.getId(), "state", state, "type", type));
                }
                if(user.getId()==150){
                    reports.addAll(baseRequest.getListEntity(HemReport.class,"region",4,"state",state,"type",type));
                }
            }
            break;
        case AppUser.COORDINATOR:
            List<Region> regions=baseRequest.getListEntity(Region.class,"coordinator",user.getId());
            reports=new ArrayList<HemReport>();
            for(Region region:regions){
                reports.addAll(baseRequest.getListEntity(HemReport.class, "region", region.getId(), "state", state, "type", type));
            }
            break;
        default:
            return new ArrayList<HemReport>();
        }
        return reports;
    }

    public List<HemReportView> getHemReportsView(String email, int state, int type,boolean manager) {
        List<HemReport> reports = getHemReports(email, state, type,manager);
        if (state == HemReport.CREATED_STATE) {
            reports.addAll(getHemReports(email, HemReport.REFUSED_STATE, type,manager));
        }
        return convertHemReportsToViews(reports);
    }

    private List<HemReportView> convertHemReportsToViews(List<HemReport> reports) {
        List<HemReportView> result = new ArrayList<HemReportView>();
        AppUser reportFrom;
        String reportFromName;
        String cityName;
        String managerName;
        for (HemReport report : reports) {
            Region region=baseRequest.getEntity(Region.class, report.getRegion());
            if(report.getReportCreator()==150){//shapavalov
                reportFrom=baseRequest.getEntity(AppUser.class,150);
            }
            else reportFrom = baseRequest.getEntity(AppUser.class, region.getCoordinator());
            reportFromName = reportFrom.getFullName();
            cityName = region.getRegionName();
            if (report.getUserId() != 0) {
                managerName = baseRequest.getEntity(AppUser.class, report.getUserId()).getFullName();
            } else
                managerName = "";
            result.add(new HemReportView(report.getId(), reportFromName, managerName, cityName, report.getState(),report.getType(), report.getStarted(), report.getComment()));
        }
        return result;
    }

    public ReportView getHemReportTaskView(String email, int id,boolean manager) {
        AppUser user = baseRequest.getUserFromEmail(email);
        manager = (user.getRole() == AppUser.MANAGER || user.getRole() == AppUser.HEAD_PROMO_DEPARTMENT)&&manager;
        HemReport hemReport = baseRequest.getEntity(HemReport.class, id);
        ReportView result = new ReportView();
        if(hemReport.getType()==2){        
            result.setPromoterSetting();
            rc.getHemPromoterTaskView(result, hemReport.getFlows(), hemReport.getState(), manager);
        }
        else
        {
            result.setSupervisorSetting();
            rc.getHemSupervisorTaskView(result, hemReport.getFlows(), hemReport.getState(), manager);
        }
        return result;
    }
    @Transactional(rollbackFor={PromoFormCheckException.class,RuntimeException.class})
    public void sendHemReport(String email, int id, int state, String comment) throws PromoFormCheckException {
        HemReport hemReport = baseRequest.getEntity(HemReport.class, id);
            AppUser appUser = baseRequest.getUserFromEmail(email);
            hemReport.setState(state);
            int ready = 0;
            Action action = null;
            List<Flow> flows = null;
            Action actionDecline=null;
            int decline;
            switch (state) {
            case HemReport.SENT_STATE:
                ready = (hemReport.getType() == HemReport.PROMOTER_TYPE) ? 17 : 18;
                action = Action.getFromId(ready);
                flows = hemReport.getFlows();
                for (Flow flow : flows){
                	try{
                    service.makeAction(flow, action, "", appUser);
                	}
                	catch (PromoFormCheckException e){
                		if(e.getLocalizedMessage().equals("Заполните  наличие товара")){
                		QFForm form=flow.getQfforms().get(0);
                		List<QFComplexData> cds=baseRequest.getListEntity(QFComplexData.class, "qfba1",18,"qfba2",4,"qfform",form);
                		if(cds.size()!=0){
                			QFComplexData qfcd=cds.get(0);
                			qfcd.setDataValue("true");
                			baseRequest.saveOrUpdate(qfcd);
                			service.makeAction(flow, action, "", appUser);
                			}
                		}
                		else {
                			throw e;
                		}
                	}
                }
                break;
            case HemReport.COMPLETED_STATE:
                ready = (hemReport.getType() == HemReport.PROMOTER_TYPE) ? 19 : 22;
                decline = (hemReport.getType() == HemReport.PROMOTER_TYPE) ? 25 : 26;
                action = Action.getFromId(ready);
                hemReport.setUserId(appUser.getId());
                actionDecline = Action.getFromId(decline);
                flows = hemReport.getFlows();
                for (Flow flow : flows){
                    service.makeAction(flow, action, "", appUser);
                    service.makeAction(flow, actionDecline, flow.getComment(), appUser);
                }
                break;
            case HemReport.REFUSED_STATE:
                ready = (hemReport.getType() == HemReport.PROMOTER_TYPE) ? 21 : 24;
                decline = (hemReport.getType() == HemReport.PROMOTER_TYPE) ? 25 : 26;
                int ret=(hemReport.getType()==HemReport.PROMOTER_TYPE)?27:28;
                action = Action.getFromId(ready);
                Action returnToCoord=Action.getFromId(ret);
                hemReport.setUserId(appUser.getId());
                actionDecline = Action.getFromId(decline);
                flows = hemReport.getFlows();
                for (Flow flow : flows) {
                    service.makeAction(flow, action, "", appUser);
                    service.makeAction(flow, actionDecline, flow.getComment(), appUser);
                    service.makeAction(flow,returnToCoord,flow.getComment(),appUser);
                }
                break;
            }
            hemReport.setComment(comment);
            baseRequest.saveOrUpdate(hemReport);
    }
    
    public ReportView getSupervisorTaskView(String name, int accepted, LocalDateTime dateUntill, LocalDateTime dateTill, String search) {
        List<Flow> flows = new ArrayList<Flow>();
        List<FlowLog> flowLogs = new ArrayList<FlowLog>();
        Organaiser organaiser = new Organaiser(name);
        boolean manager = organaiser.setFlows(flows, flowLogs, accepted, Situation.getFirstPartDituation(), QFFormTemplate.SUPERVISOR_FORM, dateTill,
                dateUntill, search);
        List<FlowInterface> tasks = new ArrayList<>();
        tasks.addAll(flows);
        tasks.addAll(flowLogs);
        ReportView result = new ReportView();
        result.setSupervisorSetting();
        rc.getSupervisorTaskView(result, tasks, accepted, manager);
        return result;
    }

    public Map<String, Object> getFlowState(String email,int flowId,boolean likeManager) {
        AppUser user=baseRequest.getUserFromEmail(email);
        boolean manager=(user.getRole()==AppUser.MANAGER||user.getRole()==AppUser.HEAD_PROMO_DEPARTMENT)&&likeManager;
        Flow flow=baseRequest.getEntity(Flow.class, flowId);
        Map<String,Object> response=new HashMap<String,Object>();
        if(flow.getFormTemplate()==QFFormTemplate.PROMOTER_FORM) {
                response.put("actions", rc.getActionsPromoter(flow,flow.getHemReport().getState(),manager));
                rc.setStatePromoter(flow, response);
            }
        if(flow.getFormTemplate()==QFFormTemplate.SUPERVISOR_FORM) {
                response.put("actions", rc.getActionsSupervisor(flow, flow.getHemReport().getState(), manager));
                rc.setStateSupervisor(flow, response);
        }
        if(flow.getAccepted()==Flow.ACCEPTED_DECLINE){
            response.put("comment", flow.getComment());
        }
        else{
            response.put("comment","");
        }
        return response;
    }
    public Map<String, Object> getFlowStateSupervisor(String email,int flowId) {
        AppUser user=baseRequest.getUserFromEmail(email);
        boolean manager=(user.getRole()==12||user.getRole()==14);
        Flow flow=baseRequest.getEntity(Flow.class, flowId);
        Map<String,Object> response=new HashMap<String,Object>();
        List<Action> actions = null;
        actions = new ArrayList<Action>();
        if (manager) {         
            actions.add(Action.getFromId(23));
            response.put("actions", actions);
        } else{
            rc.setStateSupervisor(flow, response);
            switch(flow.getSituation()){
            case 13:
                actions.add(Action.getFromId(13));
                actions.add(Action.getFromId(14));
                break;
            case 18:
                actions.add(Action.getFromId(11));
                break;
            case 11:
                if(flow.getSituationPrev()==13||flow.getSituationPrev()==18)
                    actions.add(Action.getFromId(10));
                break;
            }
            response.put("actions", actions);
        }
        return response;
    }

    public void makeRegionsToHemReports() {
       List<HemReport> reports=baseRequest.getListEntity(HemReport.class);
       for(HemReport report:reports){
           AppUser creator=baseRequest.getEntity(AppUser.class,report.getReportCreator());
           report.setRegion(creator.getRegion());
       }    
    }



}

