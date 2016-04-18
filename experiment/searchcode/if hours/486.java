/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inbadevs.swimmingpool.dao;

import com.inbadevs.swimmingpool.entities.CountLeftHoursFreeHoursPlan;
import com.inbadevs.swimmingpool.entities.Plan;
import com.inbadevs.swimmingpool.entities.User;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * @author gabriellopezsalinas
 */

@Component
public class CountLeftHoursFreeHoursPlanDao extends BaseGenericDAO<CountLeftHoursFreeHoursPlan>{


    @Autowired
    public CountLeftHoursFreeHoursPlanDao(@Qualifier("sessionFactory") SessionFactory em) {
        super(CountLeftHoursFreeHoursPlan.class, em);
    }

    public CountLeftHoursFreeHoursPlan find(User user, Plan plan) {
        Criteria criteria = getCurrentSession().createCriteria(CountLeftHoursFreeHoursPlan.class)
                .add(Restrictions.and(Restrictions.eq("user", user),
                        Restrictions.eq("plan", plan)));

        List<CountLeftHoursFreeHoursPlan> countLeftHoursFreeHoursPlanList = (List<CountLeftHoursFreeHoursPlan>) criteria.list();

        if(countLeftHoursFreeHoursPlanList != null && !countLeftHoursFreeHoursPlanList.isEmpty()){
            return countLeftHoursFreeHoursPlanList.get(0);
        }
        return null;
    }

}

