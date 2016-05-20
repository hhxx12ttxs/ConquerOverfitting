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
public class AnnualEnsembleDatum implements EnsembleDatum {

    private static final Logger log = Logger.getLogger(AnnualEnsembleDatum.class.getName());
    private String scenario;
    private Integer fromYear = null;
    private Integer toYear = null;
    private Double[] annualVal = new Double[1];
    private Integer percentile;

    public void addVal(int month, double val) {

        if (month == -1) {
            annualVal[0] = val;
        } else {
            if (month > annualVal.length - 1) {
                log.log(Level.SEVERE, "trying to add bogus month! {0 } ", month);
                return;
            } else {
                annualVal[month] = val;
            }
        }

    }

    public Double[] getMonthVals() {
        return annualVal;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getScenario() {
        return scenario;
    }
//
//    public void setModel(String model) {
//        this.model = model;
//    }
//
//    public String getModel() {
//        return model;
//    }

    public void setPercentile(int percentile) {
        this.percentile = percentile;
    }

    public int getPercentile() {
        return percentile;
    }

    public String getCsvLine() {
        String comma = ",";
        StringBuilder sb = new StringBuilder();
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
        
        sb.append(getPercentile());
        sb.append(comma);
        sb.append(annualVal[0]);
        return sb.toString();
    }

    public String getCsvHeader() {
        String comma = ",";
        StringBuilder sb = new StringBuilder();
        if (scenario != null) {
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
       
        sb.append("percentile");
        sb.append(comma);

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
        return toYear;
    }

    public void setToYear(int toYear) {
        this.toYear = toYear;
    }
}

