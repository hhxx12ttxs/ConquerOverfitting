package service.impl;

import service.IncreasebleService;

public class TimeServiceImpl implements IncreasebleService {
    private int hours;
    private int minutes;


    @Override
    public void increase() {        //каждую секунду добавляет 5 минут ко времени
        this.minutes = this.minutes + 5;
        if (this.minutes == 60) {
            this.hours = this.hours + 1;
            this.minutes = 0;
        }
        if (this.hours == 24) {
            this.hours = 0;
            this.minutes = 0;
        }

    }

    @Override
    public String toString() {
        String time = "" + this.hours + ":" + this.minutes;
        if (this.hours < 10) {
            time = "0" + time;
        }
        if (this.minutes < 10) {
            time = time.replaceAll(":", ":0");
        }
        return time;
    }

    /**
     * getters/setters
     */

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getHours() {

        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }
}

