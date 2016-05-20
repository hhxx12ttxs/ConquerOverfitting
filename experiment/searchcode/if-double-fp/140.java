/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.waksiu.flexspring.domain;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author waksiu
 */
@Entity
@Table(name = "mxm_parametry_liczone_r", catalog = "dealer2008", schema = "")
@NamedQueries({
    @NamedQuery(name = "MxmParametryLiczoneR.findAll", query = "SELECT m FROM MxmParametryLiczoneR m"),
    @NamedQuery(name = "MxmParametryLiczoneR.findByIdMxm", query = "SELECT m FROM MxmParametryLiczoneR m WHERE m.idMxm = :idMxm"),
    @NamedQuery(name = "MxmParametryLiczoneR.findByFp", query = "SELECT m FROM MxmParametryLiczoneR m WHERE m.fp = :fp"),
    @NamedQuery(name = "MxmParametryLiczoneR.findByNp", query = "SELECT m FROM MxmParametryLiczoneR m WHERE m.np = :np"),
    @NamedQuery(name = "MxmParametryLiczoneR.findByNvp", query = "SELECT m FROM MxmParametryLiczoneR m WHERE m.nvp = :nvp"),
    @NamedQuery(name = "MxmParametryLiczoneR.findByLso", query = "SELECT m FROM MxmParametryLiczoneR m WHERE m.lso = :lso")})
public class MxmParametryLiczoneR implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id_mxm", nullable = false, length = 12)
    private String idMxm;
    @Basic(optional = false)
    @Column(name = "FP", nullable = false)
    private double fp;
    @Basic(optional = false)
    @Column(name = "NP", nullable = false)
    private int np;
    @Basic(optional = false)
    @Column(name = "NVP", nullable = false)
    private int nvp;
    @Basic(optional = false)
    @Column(name = "LSO", nullable = false)
    private int lso;

    public MxmParametryLiczoneR() {
    }

    public MxmParametryLiczoneR(String idMxm) {
        this.idMxm = idMxm;
    }

    public MxmParametryLiczoneR(String idMxm, double fp, int np, int nvp, int lso) {
        this.idMxm = idMxm;
        this.fp = fp;
        this.np = np;
        this.nvp = nvp;
        this.lso = lso;
    }

    public String getIdMxm() {
        return idMxm;
    }

    public void setIdMxm(String idMxm) {
        this.idMxm = idMxm;
    }

    public double getFp() {
        return fp;
    }

    public void setFp(double fp) {
        this.fp = fp;
    }

    public int getNp() {
        return np;
    }

    public void setNp(int np) {
        this.np = np;
    }

    public int getNvp() {
        return nvp;
    }

    public void setNvp(int nvp) {
        this.nvp = nvp;
    }

    public int getLso() {
        return lso;
    }

    public void setLso(int lso) {
        this.lso = lso;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idMxm != null ? idMxm.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MxmParametryLiczoneR)) {
            return false;
        }
        MxmParametryLiczoneR other = (MxmParametryLiczoneR) object;
        if ((this.idMxm == null && other.idMxm != null) || (this.idMxm != null && !this.idMxm.equals(other.idMxm))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.waksiu.flexspring.domain.MxmParametryLiczoneR[idMxm=" + idMxm + "]";
    }

}

