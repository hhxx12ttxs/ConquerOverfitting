package nick.miros.BudgetControl.budgetcontrol.app;

import android.os.Parcelable;

/**
 * Created by admin on 11/29/2014.
 */
public abstract class Transaction implements Comparable<Transaction>, Parcelable {

    /**
     * Expense class with setters and getters for the fields
     */

    long id;
    int day;
    int month;
    int year;
    long timeStamp;
    double amount;
    String description;
    String type;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getTimeStamp() {
        return timeStamp;}

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return month + "/" + day + "/" + year + " " + amount + " " +  " " + description;
    }

    /**
     * Compares two expenses based on their full dates.
     * @param compareTransaction expense to be compared to.
     * @return comparison result
     */
    public int compareTo(Transaction compareTransaction) {

        String compareDay;
        String compareMonth;

        //add a zero before the compared date for later comparison
        if (compareTransaction.getDay() < 10) {
            compareDay = "0" + compareTransaction.getDay();
        }
        else {
            compareDay = compareTransaction.getDay() + "";
        }

        if (compareTransaction.getMonth() < 10) {
            compareMonth = "0" + compareTransaction.getMonth();
        }
        else {
            compareMonth = compareTransaction.getMonth() + "";
        }
        String compareYear = compareTransaction.getYear() + "";

        int compareFullDate = Integer.parseInt(compareYear + compareMonth + compareDay);

        String thisDay;
        String thisMonth;

        if (this.getDay() < 10) {
            thisDay = "0" + this.getDay();
        }
        else {
            thisDay = this.getDay() + "";
        }

        if (this.getMonth() < 10) {
            thisMonth = "0" + this.getMonth();
        }
        else {
            thisMonth = this.getMonth() + "";
        }
        String thisYear = this.getYear() + "";

        int thisFullDate = Integer.parseInt(thisYear + thisMonth + thisDay);

        //ascending order
        //return  thisFullDate - compareFullDate;

        //descending order
        return compareFullDate - thisFullDate;

    }
}

