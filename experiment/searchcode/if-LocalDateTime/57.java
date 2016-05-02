package by.q64.promo.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import by.q64.promo.dao.BaseRequest;
import by.q64.promo.data.Situation;
import by.q64.promo.domain.AppUser;
import by.q64.promo.domain.AppUserData;
import by.q64.promo.domain.Flow;
import by.q64.promo.domain.ReportView;
import by.q64.promo.service.PromoterSalaryService;
import by.q64.promo.service.UserDataService;
import by.q64.promo.utils.pcabinet.Organaiser;
import by.q64.promo.utils.pcabinet.ReportCabinet;
import by.q64.promo.utils.pcabinet.Task;

@Controller
public class PrivatePromoCabinet {

    @Autowired
    UserDataService uds;

    @Autowired
    ReportCabinet reportCabinet;

    @Autowired
    BaseRequest br;
    
    @Autowired
    PromoterSalaryService promoSalaryService;

    Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "api/task/get/inbox")
    public @ResponseBody List<Task> getInboxTasks(Principal principal, HttpServletRequest request, String untill, String till, String search) {
        Organaiser organaiser = new Organaiser(principal.getName());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime dateUntill = parseDate(untill, dateTimeFormatter);
        LocalDateTime dateTill = parseDate(till, dateTimeFormatter);
        List<Task> tasks = organaiser.getTasks(Flow.ACCEPTED_INBOX, Situation.getFirstPartDituation(), dateTill, dateUntill, search);
        return tasks;
    }

    @RequestMapping(value = "api/task/get/sent")
    public @ResponseBody List<Task> getSendTasks(Principal principal, HttpServletRequest request, String untill, String till, String search) {
        Organaiser organaiser = new Organaiser(principal.getName());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime dateUntill = parseDate(untill, dateTimeFormatter);
        LocalDateTime dateTill = parseDate(till, dateTimeFormatter);
        List<Task> tasks = organaiser.getTasks(Flow.ACCEPTED_SEND, Situation.getFirstPartDituation(), dateTill, dateUntill, search);
        return tasks;
    }

    @RequestMapping(value = "api/task/get/completed")
    public @ResponseBody List<Task> getCompletedTasks(Principal principal, HttpServletRequest request, String untill, String till, String search) {
        Organaiser organaiser = new Organaiser(principal.getName());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime dateUntill = parseDate(untill, dateTimeFormatter);
        LocalDateTime dateTill = parseDate(till, dateTimeFormatter);
        List<Task> tasks = organaiser.getTasks(Flow.ACCEPTED_ARCHIVE, Situation.getFirstPartDituation(), dateTill, dateUntill, search);
        return tasks;
    }

    @RequestMapping(value = "api/user/get")
    public @ResponseBody AppUser getUser(Principal principal) {
        return uds.getUser(principal.getName());
    }

    @RequestMapping(value = "api/user/get/data", method = RequestMethod.GET)
    public @ResponseBody AppUserData getUserData(Principal principal,boolean clientMode) {
        AppUserData result = uds.getUserData(principal.getName(),clientMode);
        return result;
    }

//    @RequestMapping(value = "api/supervisor/inbox")
//    public @ResponseBody List<ReportSupervisorView> getInboxReportsSuperviser(Principal principal, HttpServletRequest request) {
//        return reportCabinet.getReportTasksSupervisor(principal.getName(), Flow.ACCEPTED_INBOX);
//    }
//
//    @RequestMapping(value = "api/supervisor/sent")
//    public @ResponseBody List<ReportSupervisorView> getSendReportsSuperviser(Principal principal, HttpServletRequest request) {
//        return reportCabinet.getReportTasksSupervisor(principal.getName(), Flow.ACCEPTED_SEND);
//    }
//
//    @RequestMapping(value = "api/supervisor/completed")
//    public @ResponseBody List<ReportSupervisorView> getCompletedReportsSuperviser(Principal principal, HttpServletRequest request) {
//        return reportCabinet.getReportTasksSupervisor(principal.getName(), Flow.ACCEPTED_ARCHIVE);
//    }
    
    @RequestMapping(value = "api/supervisor/inbox")
    public @ResponseBody ReportView getInboxReportsSuperviser(Principal principal, HttpServletRequest request, String untill, String till, String search) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime dateUntill = parseDate(untill, dateTimeFormatter);
        LocalDateTime dateTill = parseDate(till, dateTimeFormatter);
        ReportView result = reportCabinet.getSupervisorTaskView(principal.getName(), Flow.ACCEPTED_INBOX, dateUntill, dateTill, search);
        return result;
    }    

    @RequestMapping(value = "api/supervisor/sent")
    public @ResponseBody ReportView getSendReportsSuperviser(Principal principal, HttpServletRequest request, String untill, String till, String search) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime dateUntill = parseDate(untill, dateTimeFormatter);
        LocalDateTime dateTill = parseDate(till, dateTimeFormatter);
        ReportView result = reportCabinet.getSupervisorTaskView(principal.getName(), Flow.ACCEPTED_SEND, dateUntill, dateTill, search);
        return result;
    }
    
    @RequestMapping(value = "api/supervisor/completed")
    public @ResponseBody ReportView getCompletedReportsSuperviser(Principal principal, HttpServletRequest request, String untill, String till, String search) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime dateUntill = parseDate(untill, dateTimeFormatter);
        LocalDateTime dateTill = parseDate(till, dateTimeFormatter);
        ReportView result = reportCabinet.getSupervisorTaskView(principal.getName(), Flow.ACCEPTED_ARCHIVE, dateUntill, dateTill, search);
        return result;
    }

    @RequestMapping(value = "api/promoter/inbox")
    public @ResponseBody ReportView getInboxReports(Principal principal, HttpServletRequest request, String untill, String till, String search) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime dateUntill = parseDate(untill, dateTimeFormatter);
        LocalDateTime dateTill = parseDate(till, dateTimeFormatter);
        ReportView result = reportCabinet.getPromoterTaskView(principal.getName(), Flow.ACCEPTED_INBOX, dateUntill, dateTill, search);
        return result;
    }

    @RequestMapping(value = "api/promoter/sent")
    public @ResponseBody ReportView getSendReports(Principal principal, HttpServletRequest request, String untill, String till, String search) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime dateUntill = parseDate(untill, dateTimeFormatter);
        LocalDateTime dateTill = parseDate(till, dateTimeFormatter);
        return reportCabinet.getPromoterTaskView(principal.getName(), Flow.ACCEPTED_SEND, dateUntill, dateTill, search);
    }

    @RequestMapping(value = "api/promoter/completed")
    public @ResponseBody ReportView getCompletedReports(Principal principal, HttpServletRequest request, String untill, String till, String search) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime dateUntill = parseDate(untill, dateTimeFormatter);
        LocalDateTime dateTill = parseDate(till, dateTimeFormatter);
        return reportCabinet.getPromoterTaskView(principal.getName(), Flow.ACCEPTED_ARCHIVE, dateUntill, dateTill, search);
    }
    
    @RequestMapping(value = "api/promoter/report/salary")
    public @ResponseBody Map<String,Object> getCompletedPromoterReports(Principal principal, String untill, String till, String search, int idForValikMode) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime dateUntill = parseDate(untill, dateTimeFormatter);
        LocalDateTime dateTill = parseDate(till, dateTimeFormatter);
        return promoSalaryService.getPromoterSalary(principal.getName(), dateUntill, dateTill, search,idForValikMode);
    }
    @RequestMapping(value="api/flow/getState")
    public @ResponseBody Map<String,Object> getState(Principal principal,int flowId,boolean likeManager){
        
        return reportCabinet.getFlowState(principal.getName(),flowId,likeManager);
    }
    private LocalDateTime parseDate(String date, DateTimeFormatter format) {
        LocalDateTime result = null;
        try {
            if(date.length() > 1) {
                result = LocalDateTime.parse(date, format);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}

