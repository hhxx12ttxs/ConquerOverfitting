package by.q64.promo.controller;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import by.q64.promo.dao.BaseRequest;
import by.q64.promo.data.Message;
import by.q64.promo.domain.AppUser;
import by.q64.promo.domain.Flow;
import by.q64.promo.domain.Instance;
import by.q64.promo.domain.QFFormTemplate;
import by.q64.promo.domain.ShopFormSchedule;
import by.q64.promo.domain.TrastPromoShedule;
import by.q64.promo.service.i.ReportService;
import by.q64.promo.utils.scheduler.Scheduler;
import by.q64.promo.utils.workflow.Worker;

@Controller
@Transactional
public class ScheduleController {

    private static Boolean busy = new Boolean(false);
    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass().getCanonicalName());

    @Autowired
    BaseRequest baseRequest;

    @Autowired
    Scheduler scheduler;

    @Autowired
    Worker worker;

    @Autowired
    private ReportService reportService;

    @RequestMapping(value = "api/sendTasks")
    public @ResponseBody Map<String, Object> sendTasks(Principal principal, String message, String date, int region) {
        logger.info(message);
        if (BaseRequest.instance == null) {
            BaseRequest.instance = baseRequest;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime monday = LocalDateTime.parse(date, dateTimeFormatter);
        int weekDay = monday.getDayOfWeek().getValue();
        weekDay--;
        monday = monday.minusDays(weekDay);

        // TODO: кнопка отправки заданий в расписании
        Map<String, Object> jsonObject = new HashMap<String, Object>();

        if (busy) {
            jsonObject.put("busy", true);
        } else {
            busy = true;
            try {
                jsonObject.put("form", "generated");
                String email = principal.getName();
                worker.clearDublicate();
                // worker.generateForms(email);
                AppUser appUser = baseRequest.getUserFromEmail(email);
                jsonObject.put("success", worker.executeScheduler(appUser, message, monday,region));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            busy = false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("INSTANCE START");
        }
        return jsonObject;
    }

    @RequestMapping(value = "api/schedule/get")
    public @ResponseBody Object getSchedule(Principal principal, int city, int project, String date) {
        return scheduler.getSchedule(city, project, date, principal.getName());
    }



    @RequestMapping(value = "api/schedule/edit")
    public @ResponseBody Map<String, Object> editSchedule(Principal principal, HttpServletRequest request, int id, int shop, int start, // 0-23
            int stop, // 0-23
            int promoter, int supervisor, int project, String date) {
        if (scheduler.canEditShedulle(principal.getName())) {
            Map<String, Object> map = scheduler.addTrastPromoShedule(id, shop, start, stop, promoter, project, date, supervisor, principal.getName());
            return map;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "api/schedule/supervisor/edit")
    public @ResponseBody Map<String, Object> editScheduleSUP(Principal principal, HttpServletRequest request, int sun, int mon, int tue, int wed, int thu,
            int fri, int sat, int supervisor) {
        if (scheduler.canEditShedulle(principal.getName())) {
            List<Integer> days = new ArrayList<Integer>();
            days.add(sun);
            days.add(mon);
            days.add(tue);
            days.add(wed);
            days.add(thu);
            days.add(fri);
            days.add(sat);
            Map<String, Object> map = scheduler.updateTPSWeekSuperviser(days, supervisor, principal.getName());
            return map;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "api/schedule/copy", method = RequestMethod.GET)
    public @ResponseBody Map<String, String> copyShedulleWeek(Principal principal, int region, int project,String date) {
        Map<String, String> map = new HashMap<String, String>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDateTime monday = LocalDate.parse(date, dateTimeFormatter).atStartOfDay();
        if (scheduler.canEditShedulle(principal.getName())) {
            map.put(Message.MESAGE, scheduler.copyWeek(region, project,monday));

        } else {
            map.put(Message.MESAGE, "нельзя");
        }
        return map;
    }

    @RequestMapping(value = "api/schedulle/setfalse", method = RequestMethod.GET)
    public void setfalse() {
        busy = false;
    }
    @RequestMapping(value= "api/schedule/shopForm/edit")
    public @ResponseBody void editShopForm(int value,int id,int shop,int project,long monday){
        scheduler.editShopForm(id,shop,project,monday,value);
    }

    @RequestMapping(value = "fixschedullecopy", method = RequestMethod.GET)
    public @ResponseBody Integer fixschedullecopy(Object model) {
        List<TrastPromoShedule> promoShedules = baseRequest.getTrastPromoSheduleSEVENDAYS(null, LocalDate.now(), null, null, null);
        for (TrastPromoShedule trastPromoShedule : promoShedules) {
            LocalDate date = trastPromoShedule.getStart().toLocalDateTime().toLocalDate();
            LocalTime time = trastPromoShedule.getStop().toLocalDateTime().toLocalTime();
            LocalDateTime localDateTime = LocalDateTime.of(date, time);
            trastPromoShedule.setStop(Timestamp.valueOf(localDateTime));
            baseRequest.saveOrUpdate(trastPromoShedule);
        }
        return promoShedules.size();
    }

    @RequestMapping(value = "deprecatethisweek", method = RequestMethod.GET)
    public @ResponseBody Integer deprecatethisweek() {
        List<TrastPromoShedule> promoShedules = baseRequest.getTrastPromoSheduleSEVENDAYS(4, LocalDate.now(), null, null, null);
        for (TrastPromoShedule trastPromoShedule : promoShedules) {
            //TODO hided cas is not used
            //trastPromoShedule.setColour("background-color: #fcfbb5;");
            List<Instance> instances = baseRequest.getListEntity(Instance.class, "schedule", trastPromoShedule.getId());
            for (Instance instance : instances) {
                // List<Flow> flows = instance.getFlows();
                List<Flow> flows = baseRequest.getFlowsFromInstance(instance);
                for (Flow flow : flows) {
                    switch (flow.getFormTemplate()) {
                    case QFFormTemplate.PROMOTER_FORM:
                        flow.setAppUser(new AppUser(AppUser.PROMOTER_NONE_ID));
                        break;
                    case QFFormTemplate.SUPERVISOR_FORM:
                        flow.setAppUser(new AppUser(AppUser.SUPERVISOR_NONE_ID));
                        break;
                    }
                    baseRequest.saveOrUpdate(flow);
                }
                instance.setSchedule(0);
                baseRequest.saveOrUpdate(instance);
            }
            baseRequest.saveOrUpdate(trastPromoShedule);
        }
        return promoShedules.size();
    }

}

