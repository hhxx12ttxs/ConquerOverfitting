/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waksiu.flexspring.daos;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.waksiu.flexspring.domain.MxmParametryLiczone;
import org.waksiu.flexspring.domain.MxmParametryLiczoneR;

/**
 * @author waksiu
 */
@Repository
public class ParametryLiczoneDao extends HibernateDaoSupport {

    @Autowired
    @Qualifier(value = "hibernateTemplateMy")
    public void setMyHibernateTemplate(HibernateTemplate hibernateTemplate) {
        setHibernateTemplate(hibernateTemplate);
    }
    private final static Log log = LogFactory.getLog(ParametryLiczoneDao.class);

    public Double getFPr(String idMxm) {
        Double fpr = null;
        Criteria criteria = getHibernateTemplate().getSessionFactory().getCurrentSession().createCriteria(MxmParametryLiczoneR.class);
        criteria.add(Restrictions.eq("idMxm", idMxm));
        List list = criteria.list();
        if (!list.isEmpty()) {
            MxmParametryLiczoneR m = (MxmParametryLiczoneR) list.get(0);
            fpr = m.getFp();
        }
        return fpr;
    }

    public Double getFP(String idMxm) {
        Double fp = null;
        Criteria criteria = getHibernateTemplate().getSessionFactory().getCurrentSession().createCriteria(MxmParametryLiczone.class);
        criteria.add(Restrictions.eq("idMxm", idMxm));
        List list = criteria.list();
        if (!list.isEmpty()) {
            MxmParametryLiczone m = (MxmParametryLiczone) list.get(0);
            fp = m.getFp();
        }
        return fp;
    }

    public MxmParametryLiczone getParametryLiczone(String idMxm) {
        Criteria criteria = getHibernateTemplate().getSessionFactory().getCurrentSession().createCriteria(MxmParametryLiczone.class);
        criteria.add(Restrictions.eq("idMxm", idMxm));
        List list = criteria.list();
        if (list.isEmpty()) {
            return null;
        } else {
            return (MxmParametryLiczone) list.get(0);
        }
    }

    public MxmParametryLiczoneR getParametryLiczoneR(String idMxm) {
        Criteria criteria = getHibernateTemplate().getSessionFactory().getCurrentSession().createCriteria(MxmParametryLiczoneR.class);
        criteria.add(Restrictions.eq("idMxm", idMxm));
        List list = criteria.list();
        if (list.isEmpty()) {
            return null;
        } else {
            return (MxmParametryLiczoneR) list.get(0);
        }
    }
}

