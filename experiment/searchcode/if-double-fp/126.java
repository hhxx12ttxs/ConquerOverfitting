/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waksiu.flexspring.service;

import java.util.List;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.flex.remoting.RemotingDestination;
import org.waksiu.flexspring.daos.CenyDao;
import org.waksiu.flexspring.daos.ParametryLiczoneDao;
import org.waksiu.flexspring.domain.MxmExport2;
import org.waksiu.flexspring.domain.MxmCenaHand;
import org.waksiu.flexspring.service.i.ICenyService;

/**
 * @author waksiu
 */
@Service
//@RemotingDestination
public class CenyService implements ICenyService {

    @Autowired
    private CenyDao cenyDao;
    @Autowired
    private ParametryLiczoneDao parametryLiczoneDao;
    private final static Log log = LogFactory.getLog(CenyService.class);

    public void setCenyDao(CenyDao cenyDao) {
        this.cenyDao = cenyDao;
    }

    public void setParametryLiczoneDao(ParametryLiczoneDao parametryLiczoneDao) {
        this.parametryLiczoneDao = parametryLiczoneDao;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public List<MxmExport2> getMxmExport2() {
        return cenyDao.getMxmExport2();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<MxmExport2> getMxmExport2ByNodeKey(String nodeKey) {
        return cenyDao.getMxmExport2ByNodeKey(nodeKey);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void saveCenaHand(MxmCenaHand cenaHand) {
        Double MMr = null;
        Double MM = null;
        Double FPr = parametryLiczoneDao.getFPr(cenaHand.getIdMxm());
        Double FP = parametryLiczoneDao.getFP(cenaHand.getIdMxm());
        if (FPr != null) {
            MMr = (cenaHand.getCenaHand() - FPr) / FPr;
        }
        if (FP != null) {
            MM = (cenaHand.getCenaHand() - FP) / FP;
        }
        cenaHand.setMmR(MMr);
        cenaHand.setMm(MM);
        cenyDao.saveCenaHand(cenaHand);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void updateCenaHand(String idMxm, Double cenaHand) {
        MxmCenaHand ch = (MxmCenaHand) cenyDao.getCenaHandById(idMxm);
        ch.setCenaHand(cenaHand);
        Double MMr = null;
        Double MM = null;
        Double FPr = parametryLiczoneDao.getFPr(idMxm);
        Double FP = parametryLiczoneDao.getFP(idMxm);
        if (FPr != null) {
            MMr = (ch.getCenaHand() - FPr) / FPr;
        }
        if (FP != null) {
            MM = (ch.getCenaHand() - FP) / FP;
        }
        ch.setMmR(MMr);
        ch.setMm(MM);
        cenyDao.updateCenaHand(ch);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void updateDataHand(String idMxm, Date dataHand) {
        MxmCenaHand ch = (MxmCenaHand) cenyDao.getCenaHandById(idMxm);
        ch.setDataEnd(dataHand);
        Double MMr = null;
        Double MM = null;
        Double FPr = parametryLiczoneDao.getFPr(idMxm);
        Double FP = parametryLiczoneDao.getFP(idMxm);
        if (FPr != null) {
            MMr = (ch.getCenaHand() - FPr) / FPr;
        }
        if (FP != null) {
            MM = (ch.getCenaHand() - FP) / FP;
        }
        ch.setMmR(MMr);
        ch.setMm(MM);
        cenyDao.updateCenaHand(ch);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteCenaHandList(List<String> idsMxm) {
        cenyDao.deleteCenyHand(idsMxm);
    }
}

