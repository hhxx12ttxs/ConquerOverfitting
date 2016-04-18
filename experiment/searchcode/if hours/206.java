package com.nicli.domain;

import com.google.common.base.Objects;

public class Booking {

    private final RangeHours hours;
    private final String Employee;

    public Booking(RangeHours hours, String employee) {
        this.hours = hours;
        Employee = employee;
    }

    public RangeHours getHours() {
        return hours;
    }

    public String getEmployee() {
        return Employee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Booking that = (Booking) o;

        return Objects.equal(this.hours, that.hours) &&
                Objects.equal(this.Employee, that.Employee);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hours, Employee);
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .addValue(hours)
                .addValue(Employee)
                .toString();
    }
}

