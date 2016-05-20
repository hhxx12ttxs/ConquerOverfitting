/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.web;

import java.util.logging.Logger;

/**
 *
 * @author wb385924
 */
public class AnnualGcmDatum implements GcmDatum {

    private static final Logger log = Logger.getLogger(AnnualGcmDatum.class.getName());
    private static final String comma = ",";
    private String scenario;
//    private String model;
    private String gcm;
    private String variable;
    private Integer fromYear = null;
    private Integer toYear = null;
    private Double[] annualData = new Double[1];

    public AnnualGcmDatum(String gcm) {
        this.gcm = gcm;
    }

    public String getGcm() {
        return gcm;
    }

    public void setGcm(String gcm) {
        this.gcm = gcm;
    }

    public Double[] getMonthVals() {
        return annualData;
    }

    public void addVal(int month, double val) {
        annualData[0] = val;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getScenario() {
        return scenario;
    }

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
        sb.append(annualData[0]);

        return sb.toString();
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
        sb.append("annual");
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnnualGcmDatum other = (AnnualGcmDatum) obj;
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
        int hash = 3;
        hash = 89 * hash + (this.scenario != null ? this.scenario.hashCode() : 0);
        hash = 89 * hash + (this.gcm != null ? this.gcm.hashCode() : 0);
        hash = 89 * hash + (this.variable != null ? this.variable.hashCode() : 0);
        hash = 89 * hash + (this.fromYear != null ? this.fromYear.hashCode() : 0);
        return hash;
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
}

