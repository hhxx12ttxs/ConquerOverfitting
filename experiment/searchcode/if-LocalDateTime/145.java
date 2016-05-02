package by.q64.promo.utils.pcabinet;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import by.q64.promo.dao.BaseRequest;
import by.q64.promo.data.Action;
import by.q64.promo.data.Situation;
import by.q64.promo.domain.AppUser;
import by.q64.promo.domain.Flow;
import by.q64.promo.domain.FlowLog;
import by.q64.promo.domain.HemReport;
import by.q64.promo.domain.QFFormTemplate;
import by.q64.promo.domain.Region;

@Transactional
public class Organaiser {

    // @Autowired
    BaseRequest baseRequest = BaseRequest.instance;

    private AppUser appUser;

    // private Logger logger = LoggerFactory.getLogger(getClass());

    public Organaiser(String email) {
        appUser = baseRequest.getUserFromEmail(email);
    }

    public AppUser getCurrentUser() {
        return appUser;
    }

    public List<Task> getTasks(int accepted, List<Situation> situations, LocalDateTime dateTill, LocalDateTime dateUntill, String search) {
        List<Task> tasks = new ArrayList<>();
        List<Flow> flows = new ArrayList<>();
        List<FlowLog> flowLogs = new ArrayList<>();

        boolean manager = setFlows(flows, flowLogs, accepted, situations, null, dateTill, dateUntill, search);

        for (Flow flow : flows) {
            Task task = getTask(flow, manager, accepted);
            tasks.add(task);
        }

        for (FlowLog flowLog : flowLogs) {
            Task task = getTask(flowLog, manager);
            tasks.add(task);
        }

        return tasks;
    }

    public boolean setFlows(List<Flow> flows, List<FlowLog> flowLogs, int accepted, List<Situation> situations, Integer formtemplate, LocalDateTime dateTill,
            LocalDateTime dateUntill, String search) {
        boolean manager = false;
        int role = appUser.getRole();
        if (((role == AppUser.COORDINATOR) ||(role == AppUser.MANAGER) || (role == AppUser.HEAD_PROMO_DEPARTMENT))
                && (accepted == Flow.ACCEPTED_SEND)) {
            // flows.addAll(baseRequest.getFlow(Flow.ACCEPTED_DECLINE, null));
            flows.addAll(baseRequest.getFlows(Flow.class, null, formtemplate, situations, dateTill, dateUntill, search, Flow.ACCEPTED_DECLINE));
            manager = true;
            return manager;
        }

        if (accepted == Flow.ACCEPTED_INBOX) {
            List<Flow> flows5and6 = baseRequest.getFlows(Flow.class, appUser, formtemplate, situations, dateTill, dateUntill, search, Flow.ACCEPTED_DECLINE);
            flows.addAll(flows5and6);
        }

        if ((role == AppUser.MANAGER) ||(role == AppUser.HEAD_PROMO_DEPARTMENT) || (role == AppUser.DIRECTOR)) {

            List<AppUser> coordinators = baseRequest.getListEntity(AppUser.class, "role", AppUser.COORDINATOR);
            flowLogs.addAll(baseRequest.getFlows(FlowLog.class, appUser, formtemplate, situations, dateTill, dateUntill, search, accepted));
            for (AppUser appUser : coordinators) {
                flows.addAll(baseRequest.getFlows(Flow.class, appUser, formtemplate, situations, dateTill, dateUntill, search, accepted));
                flowLogs.addAll(baseRequest.getFlows(FlowLog.class, appUser, formtemplate, situations, dateTill, dateUntill, search, accepted));
            }
        } else {
            flows.addAll(baseRequest.getFlows(Flow.class, appUser, formtemplate, situations, dateTill, dateUntill, search, accepted));
            flowLogs.addAll(baseRequest.getFlows(FlowLog.class, appUser, formtemplate, situations, dateTill, dateUntill, search, accepted));
        }
        return manager;
    }

    @Transactional
    private Task getTask(FlowLog flowLog, boolean manager) {
        Task task = new Task();

        if (manager) {
            task.setNameFrom(flowLog.getAppUser().getFullName());
        } else {
            task.setNameFrom(flowLog.getNamefrom());
        }

        task.setFlowId(flowLog.getFlow_id());
        task.setActivity(flowLog.getShopActivityName());
        task.setInstance(flowLog.getInstanceName());
        task.setInstanceId(flowLog.getInstance());
        task.setChain(flowLog.getChainName());
        task.setChainId(flowLog.getChainId());
        task.setRegion(flowLog.getRegionName());
        task.setRegionId(flowLog.getRegion());
        task.setShop(flowLog.getShopName());
        task.setShopId(flowLog.getUnitRegion());
        task.setStatusId(flowLog.getAccepted());
        Situation situation = Situation.getFromId(flowLog.getSituation());
        task.setSituation(situation.getNameState());
        task.setSituationId(situation.getId());
        task.setSituationType(situation.getTypeState());

        int formtemplID = situation.getFormTemplate();
        QFFormTemplate formTemplate = baseRequest.getEntity(QFFormTemplate.class, formtemplID);

        task.setFormTemplateDescription(formTemplate.getDescription());
        task.setFormtemplateId(formTemplate.getId());
        task.setFormTemplateName(formTemplate.getNameTemplate());

        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat worktimeSTR = new SimpleDateFormat("HH:mm");

        // дата: 12.02.14
        task.setDate(DATE_FORMAT.format(flowLog.getStarttime()));

        // время работы: 9:00-16:00
        task.setWorktime(worktimeSTR.format(flowLog.getStarttime()) + " - " + worktimeSTR.format(flowLog.getStoptime()));
        // адрес : М.Видео ул. Садовая-Спасская, 3

        task.setComment(flowLog.getComment());

        // заработок: 1 000р
        task.setEarnings("1560р");
        // общая сумма: 8 500р
        task.setTotalamount("1560р");

        task.setActions(new ArrayList<Action>());

        task.setLunch(getLunch(flowLog.getStarttime(), flowLog.getStoptime()));
        task.setChangeTime(flowLog.getChangeTime());
        return task;
    }

    private Task getTask(Flow flow, boolean manager, int accepted) {
        Task task = new Task();

        if (manager) {
            task.setNameFrom(flow.getAppUser().getFullName());
        } else {
            task.setNameFrom(flow.getNamefrom());
        }

        task.setFlowId(flow.getId());
        task.setActivity(flow.getShopActivityName());
        task.setInstance(flow.getInstanceName());
        task.setInstanceId(flow.getInstance());
        task.setChain(flow.getChainName());
        task.setChainId(flow.getChainId());
        task.setRegion(flow.getRegionName());
        task.setRegionId(flow.getRegion());
        task.setShop(flow.getShopName());
        task.setShopId(flow.getUnitRegion());
        task.setStatusId(flow.getAccepted());
        Situation situation = Situation.getFromId(flow.getSituation());
        task.setSituation(situation.getNameState());
        task.setSituationId(situation.getId());
        task.setSituationType(situation.getTypeState());

        int formtemplID = situation.getFormTemplate();
        QFFormTemplate formTemplate = baseRequest.getEntity(QFFormTemplate.class, formtemplID);

        task.setFormTemplateDescription(formTemplate.getDescription());
        task.setFormtemplateId(formTemplate.getId());
        task.setFormTemplateName(formTemplate.getNameTemplate());

        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat workTimeStr = new SimpleDateFormat("HH:mm");

        // дата: 12.02.14
        task.setDate(DATE_FORMAT.format(flow.getStarttime()));

        // время работы: 9:00-16:00
        task.setWorktime(workTimeStr.format(flow.getStarttime()) + " - " + workTimeStr.format(flow.getStoptime()));

        task.setComment(flow.getComment());

        if (appUser.getRole() == AppUser.PROMOTER) {
            task.setEarnings(getEarnings(flow.getStarttime(), flow.getStoptime()) + " p");
        } else {
            // заработок: 1 000р
            task.setEarnings("0 p");
        }
        // общая сумма: 8 500р
        task.setTotalamount("1560р");

        task.setLunch(getLunch(flow.getStarttime(), flow.getStoptime()));

        if (accepted == Flow.ACCEPTED_INBOX) {
            List<Action> actions=situation.getActionsTo();
            actions.remove(Action.getFromId(7));
            actions.remove(Action.getFromId(10));
            actions.remove(Action.getFromId(31));
            actions.remove(Action.getFromId(32));
            task.setActions(actions);
        } else {
            task.setActions(new ArrayList<Action>());
        }

        task.setAccepted(flow.getAccepted());
        task.setChangeTime(flow.getChangeTime());

        return task;
    }

    private String getLunch(Timestamp start, Timestamp stop) {
        // пол дня
        long a = (stop.getTime() - start.getTime()) / 2;
        long lunch = 30 * 60 * 1000;
        long startLunch = start.getTime() + a;
        long endLunch = start.getTime() + a + lunch;
        SimpleDateFormat worktimeSTR = new SimpleDateFormat("HH:mm");
        return worktimeSTR.format(new Date(startLunch)) + " - " + worktimeSTR.format(new Date(endLunch));
    }

    private int getEarnings(Timestamp start, Timestamp stop) {
        // Кол-во отработанных часов
        int hours = (int) ((stop.getTime() - start.getTime()) / 1000 / 60 / 60);
        // Бонус за выполнение плана
        int planTen = 10;
        // Ставка
        int rate = 260;
        // ШТРАФ
        int PENALTY = 0;

        int a = (hours * planTen * rate - PENALTY * 10) / 10;
        return a;
    }

    public int countTasks(int accepted, Integer formTemplate, List<Situation> situations) {
        Integer count = 0;

        int role = appUser.getRole();
        if ((role == AppUser.COORDINATOR) && (accepted == Flow.ACCEPTED_SEND)) {
            // flows.addAll(baseRequest.getFlow(Flow.ACCEPTED_DECLINE, null));
            // flows.addAll(baseRequest.getFlows(Flow.class, null,
            // Flow.ACCEPTED_DECLINE));
            count += baseRequest.getCountFlows(Flow.class, appUser.getRegion(), formTemplate, null, situations, Flow.ACCEPTED_DECLINE);
            return count;
        }
        if (((role == AppUser.MANAGER) || (role == AppUser.MANAGER) || (role == AppUser.HEAD_PROMO_DEPARTMENT)) && (accepted == Flow.ACCEPTED_SEND)) {
            // flows.addAll(baseRequest.getFlow(Flow.ACCEPTED_DECLINE, null));
            // flows.addAll(baseRequest.getFlows(Flow.class, null,
            // Flow.ACCEPTED_DECLINE));
            count += baseRequest.getCountFlows(Flow.class, null, formTemplate, null, situations, Flow.ACCEPTED_DECLINE);
            return count;
        }

        if (accepted == Flow.ACCEPTED_INBOX) {
            // List<Flow> flows5and6 = baseRequest.getFlows(Flow.class, appUser,
            // Flow.ACCEPTED_DECLINE, Flow.ACCEPTED_DECLINE_SYSTEM);
            // flows.addAll(flows5and6);
            count += baseRequest.getCountFlows(Flow.class, null, formTemplate, appUser, situations, Flow.ACCEPTED_DECLINE);
        }

        if ((role == AppUser.MANAGER) || (role == AppUser.MANAGER) || (role == AppUser.HEAD_PROMO_DEPARTMENT) || (role == AppUser.DIRECTOR)) {

            List<AppUser> coordinators = baseRequest.getListEntity(AppUser.class, "role", AppUser.COORDINATOR);

            // flowLogs = baseRequest.getFlows(FlowLog.class, appUser,
            // accepted);
            count += baseRequest.getCountFlows(FlowLog.class, null, formTemplate, appUser, situations, accepted);
            for (AppUser appUser : coordinators) {
                // flows.addAll(baseRequest.getFlows(Flow.class, appUser,
                // accepted));
                count += baseRequest.getCountFlows(Flow.class, null, formTemplate, appUser, situations, accepted);

                // flowLogs.addAll(baseRequest.getFlows(FlowLog.class, appUser,
                // accepted));
                count += baseRequest.getCountFlows(FlowLog.class, null, formTemplate, appUser, situations, accepted);
            }

        } else {
            // flows.addAll(baseRequest.getFlows(Flow.class, appUser,
            // accepted));
            count += baseRequest.getCountFlows(Flow.class, null, formTemplate, appUser, situations, accepted);

            // flowLogs.addAll(baseRequest.getFlows(FlowLog.class, appUser,
            // accepted));
            count += baseRequest.getCountFlows(FlowLog.class, null, formTemplate, appUser, situations, accepted);
        }
        return count;
    }

    public int countHemReports(int state, int type,boolean manager) {
        int result=0;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("state", state);
        params.put("type", type);
        if (!manager){
            if(appUser.getId()==150){//shapovalov
                params.put("region", 4);
                result+=baseRequest.getCountEntity(HemReport.class, params);
            }
            params.remove("region");
            List<Region> regions=baseRequest.getListEntity(Region.class,"coordinator",appUser.getId());
            params.put("region", 1);
            for(Region region : regions){
                params.replace("region", region.getId());
                result+=baseRequest.getCountEntity(HemReport.class, params);
            }
            if(state == HemReport.CREATED_STATE){
                params.replace("state", HemReport.REFUSED_STATE);
                for(Region region : regions){
                    params.replace("region", region.getId());
                    result+=baseRequest.getCountEntity(HemReport.class, params);
                }
            }
        }
        else{
        result = baseRequest.getCountEntity(HemReport.class, params);
        if (state == HemReport.CREATED_STATE) {
            params.replace("state", HemReport.REFUSED_STATE);
            result += baseRequest.getCountEntity(HemReport.class, params);
            }
        }
        return result;
    }

}

