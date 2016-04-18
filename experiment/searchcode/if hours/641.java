package br.tcc.webapp.model;

import org.appfuse.model.BaseObject;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: Gleison
 * Date: 17/07/13
 * Time: 21:38
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Activity extends BaseObject {

    private Long id;
    private String name;
    private float hoursToYellow;
    private float hoursToRed;

// --------------------- GETTER / SETTER METHODS ---------------------

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getHoursToYellow() {
        return hoursToYellow;
    }

    public void setHoursToYellow(float hoursToYellow) {
        this.hoursToYellow = hoursToYellow;
    }

    public float getHoursToRed() {
        return hoursToRed;
    }

    public void setHoursToRed(float hoursToRed) {
        this.hoursToRed = hoursToRed;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Activity activity = (Activity) o;

        if (!id.equals(activity.id)) return false;
        if (!name.equals(activity.name)) return false;
        if (hoursToYellow != activity.hoursToYellow) return false;
        if (hoursToRed != activity.hoursToRed) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hoursToYellow='" + hoursToYellow + '\'' +
                ", hoursToRed='" + hoursToRed + '\'' +
                '}';
    }
}

