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
public class MonthlyEnsembleDatum implements EnsembleDatum {

    private static final Logger log = Logger.getLogger(MonthlyEnsembleDatum.class.getName());
    private String scenario;
//    private String model;
    private Double[] monthVals = new Double[12];
    private Integer percentile;
    private Integer fromYear = null;
    private Integer toYear = null;

    public void addVal(int month, double val) {

        if (month == -1) {
            monthVals[0] = val;
        } else {
            if (month > monthVals.length - 1) {
                log.log(Level.SEVERE, "trying to add bogus month! {0 } ", month);
                return;
            } else {
                monthVals[month] = val;
            }
        }

    }

    public Double[] getMonthVals() {
        return monthVals;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getScenario() {
        return scenario;
    }

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
        for (int i = 0; i < monthVals.length; i++) {
            sb.append(monthVals[i]);
            if (i < monthVals.length - 1) {
                sb.append(comma);
            }
        }
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
        return toYear;
    }

    public void setToYear(int toYear) {
        this.toYear = toYear;
    }
}

