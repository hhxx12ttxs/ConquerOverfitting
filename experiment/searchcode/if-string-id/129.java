/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.italtel.patchfinder.objects;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author ale-X
 */
@Entity
@Table(name = "centrale")
@NamedQueries({
    @NamedQuery(name = "Centrale.findAll", query = "SELECT c FROM Centrale c group by c.name"),
    @NamedQuery(name = "Centrale.findById", query = "SELECT c FROM Centrale c WHERE c.id = :id"),
    @NamedQuery(name = "Centrale.findByName", query = "SELECT c FROM Centrale c WHERE c.name = :name order by c.impianto asc"),
    @NamedQuery(name = "Centrale.findByImpianto", query = "SELECT c FROM Centrale c WHERE c.impianto = :impianto"),
    @NamedQuery(name = "Centrale.findByServizio", query = "SELECT c FROM Centrale c WHERE c.servizio = :servizio"),
    @NamedQuery(name = "Centrale.findByNameAndImpianto", query = "SELECT c FROM Centrale c WHERE c.impianto = :impianto AND c.name = :name order by c.name asc")
})
public class Centrale implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name")
    private String name;
    @Column(name = "impianto")
    private String impianto;
    @Basic(optional = false)
    @Column(name = "servizio")
    private String servizio;

    public Centrale() {
    }

    public Centrale(Integer id) {
        this.id = id;
    }

    public Centrale(Integer id, String servizio) {
        this.id = id;
        this.servizio = servizio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImpianto() {
        return impianto;
    }

    public void setImpianto(String impianto) {
        this.impianto = impianto;
    }

    public String getServizio() {
        return servizio;
    }

    public void setServizio(String servizio) {
        this.servizio = servizio;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Centrale)) {
            return false;
        }
        Centrale other = (Centrale) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
//
//    @Override
//    public String toString() {
//        return "com.italtel.patchfinder.objectsJPA.Centrale[id=" + id + "]";
//    }

    @Override
    public String toString() {
        return name;
    }

    public String[] getServizioSplitted() {
        return servizio.split(";");
    }
}

