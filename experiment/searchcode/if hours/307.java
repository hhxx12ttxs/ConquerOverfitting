package com.inbadevs.swimmingpool.manager.controlAccess;

import com.inbadevs.swimmingpool.dao.AssistanceFreeHoursPlanDao;
import com.inbadevs.swimmingpool.dao.CountLeftHoursFreeHoursPlanDao;
import com.inbadevs.swimmingpool.entities.*;
import com.inbadevs.swimmingpool.exceptions.ControlEntranceException;
import com.inbadevs.swimmingpool.exceptions.ControlExitException;
import com.inbadevs.swimmingpool.service.entityresponse.ControlAccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by macbook on 8/8/15.
 */
@Component
public class FreeHoursAccess implements Access{

    private AssistanceFreeHoursPlanDao assistanceFreeHoursPlanDao;
    private CountLeftHoursFreeHoursPlanDao countLeftHoursFreeHoursPlanDao;

    @Autowired
    public  FreeHoursAccess (AssistanceFreeHoursPlanDao assistanceFreeHoursPlanDao,
                             CountLeftHoursFreeHoursPlanDao countLeftHoursFreeHoursPlanDao){

        this.assistanceFreeHoursPlanDao = assistanceFreeHoursPlanDao;
        this.countLeftHoursFreeHoursPlanDao = countLeftHoursFreeHoursPlanDao;
    }

    public ControlAccessResponse controlEntrance(User user, Product product, Date entranceDate) throws ControlEntranceException {

        Plan plan = product.getProductPK().getPlan();
        assistanceFreeHoursPlanDao.save(new AssistanceFreeHoursPlan(user, product.getProductPK().getPlan(), entranceDate));
        final int hours = plan.getHoursPerWeek() * 4;
        int hoursLeft = hours;

        CountLeftHoursFreeHoursPlan countHoursLeft = countLeftHoursFreeHoursPlanDao.find(user, plan);

        if (countHoursLeft != null) {
            hoursLeft = countHoursLeft.getHoursLeft();
        }
        else{
            countLeftHoursFreeHoursPlanDao.save(new CountLeftHoursFreeHoursPlan(user, plan, hours));
        }

        return new ControlAccessResponse(user.getNames(), plan.getName(), hours, hoursLeft, null, null);
    }


    public ControlAccessResponse controlExit(User user, Product product, Date exitDate ) throws ControlExitException {

        Plan plan = product.getProductPK().getPlan();
        final int hoursOfPlan = plan.getHoursPerWeek() * 4;
        AssistanceFreeHoursPlan assistanceFreeHoursPlan = assistanceFreeHoursPlanDao.findLastEntrance(user, plan);

        if(assistanceFreeHoursPlan == null ){
            throw new ControlExitException("never entrance");
        }
        else if(!assistanceFreeHoursPlan.getEntrance()){
            throw new ControlExitException("entrance before exit");
        }

        CountLeftHoursFreeHoursPlan countHoursLeft = countLeftHoursFreeHoursPlanDao.find(user, plan);

        int leftHours = subtractTime(exitDate, assistanceFreeHoursPlan.getEntranceDate(), countHoursLeft.getHoursLeft());
        countHoursLeft.setHoursLeft(leftHours);

        assistanceFreeHoursPlan.setEntrance(false);
        assistanceFreeHoursPlan.setExitDate(exitDate);

        return new ControlAccessResponse(user.getNames(), plan.getName(), hoursOfPlan,
                leftHours, null, null);

    }

    private Integer subtractTime(Date exitDate, Date entranceDate, int totalHours) {

        Long difference = exitDate.getTime() - entranceDate.getTime();
        Long minutes = difference / (60 * 1000);

        if(minutes > 20){
            totalHours --;
        }

        int penaltyMinutes = 100;

        while (penaltyMinutes < minutes){

            if(minutes > penaltyMinutes){
                totalHours --;
            }
            penaltyMinutes = penaltyMinutes + 60;
        }

        return  totalHours;

    }
}

