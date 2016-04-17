package app.com.ledsavingcalculator.util;

public class Hours {

    private final long numberOfHours;
    private final long numberOfMins;

    public Hours(long diffHours, long diffMinutes) {
        this.numberOfHours = diffHours;
        this.numberOfMins = diffMinutes;
    }

    public long getNumberOfHours() {
        return numberOfHours;
    }

    public long getNumberOfMins() {
        return numberOfMins;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hours hours = (Hours) o;

        if (numberOfHours != hours.numberOfHours) return false;
        return numberOfMins == hours.numberOfMins;

    }

    @Override
    public int hashCode() {
        int result = (int) (numberOfHours ^ (numberOfHours >>> 32));
        result = 31 * result + (int) (numberOfMins ^ (numberOfMins >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Hours{" +
                "numberOfHours=" + numberOfHours +
                ", numberOfMins=" + numberOfMins +
                '}';
    }
}

