/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package chessminion.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

/**
 *
 * @author ben
 */
@Entity
public class UserGroup implements Serializable {

    public static final String ADMIN_GROUPNAME = "admin";
    public static final String GENERAL_GROUPNAME = "player";

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    
    @ManyToMany(mappedBy="groups")
    protected List<ChessPlayer> userlist;

    /**
     * Get the value of userlist
     *
     * @return the value of userlist
     */
    public List<ChessPlayer> getUserlist() {
        return userlist;
    }

    /**
     * Set the value of userlist
     *
     * @param userlist new value of userlist
     */
    public void setUserlist(List<ChessPlayer> userlist) {
        this.userlist = userlist;
    }


    public UserGroup() {
    }

    public UserGroup (String name) {
        id = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        if (!(object instanceof UserGroup)) {
            return false;
        }
        UserGroup other = (UserGroup) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "chessminion.entities.UserGroup[id=" + id + "]";
    }

    
}

