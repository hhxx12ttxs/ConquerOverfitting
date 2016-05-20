/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.web;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wb385924
 */
public class MonthlyGcmDatum implements GcmDatum, Comparable<GcmDatum> {

    private static final Logger log = Logger.getLogger(MonthlyGcmDatum.class.getName());
    private String scenario;
    private static final String comma = ",";
//    private String model;
    private String gcm;
    private String variable;
    private Double[] monthVals = new Double[12];
    private Integer fromYear = null;
    private Integer toYear = null;

    public MonthlyGcmDatum(String gcm) {
        this.gcm = gcm;
    }

    public String getGcm() {
        return gcm;
    }

    public void setGcm(String gcm) {
        this.gcm = gcm;
    }

    public Double[] getMonthVals() {
        return monthVals;
    }

    public void addVal(int month, double val) {
        if (month == -1) {
            monthVals[0] = val;
        } else {
            if (month > monthVals.length - 1) {
                log.log(Level.SEVERE, "trying to add bogus month! {0} ", month);
                return;
            } else {
                monthVals[month] = val;
            }
        }
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getScenario() {
        return scenario;
    }

    public void setVariable(String varname) {
        this.variable = varname;
    }

    public String getVariable() {
        return this.variable;
    }

//    public void setModel(String model) {
//        this.model = model;
//    }
//
//    public String getModel() {
//        return this.model;
//    }
    public String getCsvLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(getGcm());
        sb.append(comma);

        if(getVariable() != null){
            sb.append(getVariable());
            sb.append(comma);
        }
        if (getScenario() != null) {
            sb.append(getScenario());
            sb.append(comma);
        }
        if (fromYear != null) {
            sb.append(getFromYear());
            sb.append(comma);
        }
        if (toYear != null) {
            sb.append(getToYear());
            sb.append(comma);
        }
        for (int i = 0; i < monthVals.length; i++) {
            sb.append(monthVals[i]);
            if (i < monthVals.length - 1) {
                sb.append(comma);
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MonthlyGcmDatum other = (MonthlyGcmDatum) obj;
        if ((this.scenario == null) ? (other.scenario != null) : !this.scenario.equals(other.scenario)) {
            return false;
        }
        if ((this.gcm == null) ? (other.gcm != null) : !this.gcm.equals(other.gcm)) {
            return false;
        }
        if ((this.variable == null) ? (other.variable != null) : !this.variable.equals(other.variable)) {
            return false;
        }
        if (this.fromYear != other.fromYear && (this.fromYear == null || !this.fromYear.equals(other.fromYear))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.scenario != null ? this.scenario.hashCode() : 0);
        hash = 97 * hash + (this.gcm != null ? this.gcm.hashCode() : 0);
        hash = 97 * hash + (this.variable != null ? this.variable.hashCode() : 0);
        hash = 97 * hash + (this.fromYear != null ? this.fromYear.hashCode() : 0);
        return hash;
    }

  

    public String getCsvHeader() {
        StringBuilder sb = new StringBuilder();

        sb.append("GCM");
        sb.append(comma);
        if (getVariable() != null) {
            sb.append("var");
            sb.append(comma);
        }
        if (getScenario() != null) {
            sb.append("scenario");
            sb.append(comma);
        }
        if (fromYear != null) {
            sb.append("from_year");
            sb.append(comma);
        }
        if (toYear != null) {
            sb.append("to_year");
            sb.append(comma);
        }
        sb.append("Jan");
        sb.append(comma);
        sb.append("Feb");
        sb.append(comma);
        sb.append("Mar");
        sb.append(comma);
        sb.append("Apr");
        sb.append(comma);
        sb.append("May");
        sb.append(comma);
        sb.append("Jun");
        sb.append(comma);
        sb.append("Jul");
        sb.append(comma);
        sb.append("Aug");
        sb.append(comma);
        sb.append("Sep");
        sb.append(comma);
        sb.append("Oct");
        sb.append(comma);
        sb.append("Nov");
        sb.append(comma);
        sb.append("Dec");
        return sb.toString();
    }

    public Integer getFromYear() {
        return fromYear;
    }

    public void setFromYear(int fromYear) {
        this.fromYear = fromYear;
    }

    public Integer getToYear() {
        return this.toYear;
    }

    public void setToYear(int toYear) {
        this.toYear = toYear;
    }

    public int compareTo(GcmDatum o) {
        if (o == null) {
            return 1;
        }

        String thisgcm = getGcm();
        if (thisgcm != null && !thisgcm.equals(o.getGcm())) {
            return thisgcm.compareTo(o.getGcm());
        }


        String thiscenario = getScenario();
        if (thiscenario != null && !thiscenario.equals(o.getScenario())) {
            return thiscenario.compareTo(o.getScenario());
        }

        int thisfromyear = getFromYear();
        if (!(thisfromyear == o.getFromYear())) {
            return thisfromyear - o.getFromYear();
        }
        return 0;
    }
}

