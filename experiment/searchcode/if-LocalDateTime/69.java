package by.q64.promo.controller;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import by.q64.promo.dao.BaseRequest;
import by.q64.promo.data.Situation;
import by.q64.promo.domain.Activity;
import by.q64.promo.domain.Flow;
import by.q64.promo.domain.FlowLog;
import by.q64.promo.domain.Instance;
import by.q64.promo.domain.UnitActivity;
import by.q64.promo.domain.UnitRegion;

@Controller
@Transactional
public class UpgradeDatabaseController {
	
    @Autowired
    BaseRequest baseRequest;
    
    Logger  log = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "update/newcloums/flowlog/formtemplate")
    public @ResponseBody String newcloumsformtemplate(Principal principal) {
    	LocalDateTime dateTime = LocalDateTime.now();
    	List<FlowLog> flowLogs = baseRequest.getListEntity(FlowLog.class);
        for (FlowLog flowLog : flowLogs) {
        	flowLog.setFormTemplate(flowLog.getFlow().getFormTemplate());
            baseRequest.save(flowLog);
        }
        LocalDateTime dateTime2 = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, dateTime2);
        return "DONE " + flowLogs.size() + " : " + duration.toString();
    }
    
    
    @RequestMapping(value = "update/newcloums/flow")
    public @ResponseBody String newcloums(Principal principal) {
    	LocalDateTime dateTime = LocalDateTime.now();
        List<Flow> flows = baseRequest.getListEntity(Flow.class);
        for (Flow flow : flows) {
        	UnitRegion unitRegion = baseRequest.getEntity(UnitRegion.class, flow.getUnitRegion());
        	Instance instance=baseRequest.getEntity(Instance.class, flow.getInstance());
        	Activity activity=baseRequest.getLastActivity(unitRegion.getId(), instance.getProject().getId());
            flow.setShopActivityName(baseRequest.getEntity(UnitActivity.class, activity.getActivity()).getActivityName());
            flow.setInstanceName(instance.getInstanceName());
            flow.setChainName(unitRegion.getUnit().getUnitName());
            flow.setChainId(unitRegion.getUnit().getId());
            flow.setRegionName(unitRegion.getRegion().getRegionName());
            flow.setShopName(unitRegion.getName());
            baseRequest.save(flow);
        }
        LocalDateTime dateTime2 = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, dateTime2);
        return "DONE " + flows.size() + " : " + duration.toString();
    }
    
    @RequestMapping(value = "update/newcloums/FlowLog")
    public @ResponseBody String newcloumsLog(Principal principal) {
    	LocalDateTime dateTime = LocalDateTime.now();
        List<FlowLog> flows = baseRequest.getListEntity(FlowLog.class);
        for (FlowLog flow : flows) {
        	UnitRegion unitRegion = baseRequest.getEntity(UnitRegion.class, flow.getUnitRegion());
        	flow.setRegion(unitRegion.getRegion().getId());
            Instance instance = baseRequest.getEntity(Instance.class, flow.getInstance());
        	Activity activity = baseRequest.getLastActivity(unitRegion.getId(), instance.getProject().getId());
        	UnitActivity unitActivity = baseRequest.getEntity(UnitActivity.class, activity.getActivity());
            flow.setShopActivityName(unitActivity.getActivityName());
            flow.setInstanceName(instance.getInstanceName());
            flow.setChainName(unitRegion.getUnit().getUnitName());
            flow.setChainId(unitRegion.getUnit().getId());
            flow.setRegionName(unitRegion.getRegion().getRegionName());
            flow.setShopName(unitRegion.getName());
            baseRequest.save(flow);
        }
        LocalDateTime dateTime2 = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, dateTime2);
        return "DONE " + flows.size() + " : " + duration.toString();
    }
    
    
    
    @RequestMapping(value = "fix/formtemplate/flow")
    public @ResponseBody Object test(Principal principal) {
        List<Flow> flows = baseRequest.getListEntity(Flow.class);
        for (Flow flow : flows) {
            Situation situation = Situation.getFromId(flow.getSituation());
            if (flow.getFormTemplate() == 0) {
                flow.setFormTemplate(situation.getFormTemplate());
                baseRequest.saveOrUpdate(flow);
                log.info(flow.toString());
            }
        }
        return null;
    }
    
    
}

