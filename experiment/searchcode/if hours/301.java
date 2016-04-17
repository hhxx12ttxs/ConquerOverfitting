/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mypizza.concrete;

import com.mypizza.entities.OpeningHours;
import com.mypizza.interfaces.OpeningHoursDAO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author szymon
 */
@Repository
public class OpeningHoursDAOImpl implements OpeningHoursDAO {

    @Autowired
    private SessionFactory sessionFactory;    

    @Override
    public OpeningHours get(Integer prid) {
        Query q;
        q = sessionFactory.getCurrentSession()
                .createQuery("from OpeningHours p where p.prid = :prid");
        q.setParameter("prid", prid);
        Iterator OpeningHoursIterator = q.iterate();
        OpeningHours openingHours = null;
        while ( OpeningHoursIterator.hasNext() ) {
            openingHours = (OpeningHours) OpeningHoursIterator.next();
            Hibernate.initialize(openingHours);
        }
        return openingHours;
    }

    @Override
    public OpeningHours get(Integer prid, Integer userid) {
        Query q;
        q = sessionFactory.getCurrentSession()
                .createQuery("from OpeningHours p where p.prid = :prid and p.userid = :userid");
        q.setParameter("prid", prid);
        q.setParameter("userid", userid);
        Iterator OpeningHoursIterator = q.iterate();
        OpeningHours openingHours = null;
        while ( OpeningHoursIterator.hasNext() ) {
            openingHours = (OpeningHours) OpeningHoursIterator.next();
            Hibernate.initialize(openingHours);
        }
        return openingHours;
    }    
    
    @Override
    public void add(OpeningHours openingHours) {
        sessionFactory.getCurrentSession().save(openingHours);
    }

    @Override
    public void remove(Integer prid) {
        OpeningHours openingHours = (OpeningHours) sessionFactory.getCurrentSession().load(
        OpeningHours.class, prid);
        if (null != openingHours) {
            sessionFactory.getCurrentSession().delete(openingHours);
        }
    }

    @Override
    public void update(OpeningHours openingHours) {
        sessionFactory.getCurrentSession().update(openingHours);      
    }

}

