package com.roosterpark.rptime.selenium.control.complex.reports;

import com.roosterpark.rptime.selenium.control.complex.list.worker.WorkerLink;
import com.roosterpark.rptime.selenium.page.admin.WorkerPage;

/**
 * User: John
 * Date: 1/15/14
 * Time: 2:32 PM
 */
public class TotalHoursReportRow {

    private WorkerLink workerLink;
    private Double hours;
    private String worker;

    public TotalHoursReportRow(WorkerLink workerLink, Double hours, String worker) {
        this.workerLink = workerLink;
        this.hours = hours;
        this.worker = worker;
    }

    public TotalHoursReportRow(WorkerLink workerLink, String hours, String worker) {
        this.workerLink = workerLink;
        this.hours = Double.valueOf(hours);
        this.worker = worker;
    }

    public Double getHours() {
        return hours;
    }

    public WorkerPage clickWorkerLink() {
        return workerLink.click();
    }

    public String getWorker() {
        return worker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TotalHoursReportRow that = (TotalHoursReportRow) o;

        if (hours != null ? !hours.equals(that.hours) : that.hours != null) return false;
        if (worker != null ? !worker.equals(that.worker) : that.worker != null) return false;
        if (workerLink != null ? !workerLink.equals(that.workerLink) : that.workerLink != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = workerLink != null ? workerLink.hashCode() : 0;
        result = 31 * result + (hours != null ? hours.hashCode() : 0);
        result = 31 * result + (worker != null ? worker.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TotalHoursReportRow{" +
                "workerLink=" + workerLink +
                ", hours=" + hours +
                ", worker='" + worker + '\'' +
                '}';
    }
}

