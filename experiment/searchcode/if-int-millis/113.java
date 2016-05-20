package com.synchrosinteractive.pomodoro;

import java.util.Calendar;

public class Timer implements Runnable {

    private long millis = 0, timeStartedMillis = 0;
    private boolean running = false;
    private long tickInterval = 1000;
    private Thread thread;
    private EventProvider eventProvider;
    public static final String EVENT_STARTED = "started";
    public static final String EVENT_STOPPED = "stopped";
    public static final String EVENT_DURATION_CHANGED = "duration-changed";
    public static final String EVENT_TICK = "tick";
    public static final String EVENT_COMPLETED = "completed";

    public Timer() {
        eventProvider = new EventProvider();
        eventProvider.createEvent(EVENT_STARTED);
        eventProvider.createEvent(EVENT_STOPPED);
        eventProvider.createEvent(EVENT_DURATION_CHANGED);
        eventProvider.createEvent(EVENT_TICK);
        eventProvider.createEvent(EVENT_COMPLETED);
    }

    public boolean subscribe(String eventName, EventHandler handler) {
        return eventProvider.subscribe(eventName, handler);
    }

    public long getTickInterval() {
        return this.tickInterval;
    }

    public void setTickInterval(long tickInterval) {
        this.tickInterval = tickInterval;
    }

    public void setTime(long millis) {
        this.millis = millis;
        eventProvider.fire(EVENT_DURATION_CHANGED, this);
    }

    public long getTime() {
        long time;
        if (running) {
            time = millis - (Calendar.getInstance().getTimeInMillis() - timeStartedMillis);
        } else {
            time = millis;
        }
        if (time < 0) {
            time = 0;
        }
        return time;
    }

    public int[] getTimeParts() {
        return getTimeParts(false);
    }

    public int[] getTimeParts(boolean rollUpMillis) {
        long time = getTime();
        int millis = (int) (time % 1000);
        int secs = (int) ((time / 1000) % 60);
        int mins = (int) ((getTime() / (1000 * 60)) % 60);
        if (rollUpMillis) {
            if (millis > 0 && ++secs == 60) {
                secs = 0;
                mins++;
            }
            return new int[]{mins, secs};
        } else {
            return new int[]{mins, secs, millis};
        }
    }

    public void start() {
        if (running) {
            return;
        }
        timeStartedMillis = Calendar.getInstance().getTimeInMillis();
        running = true;
        thread = new Thread(this);
        thread.start();
        eventProvider.fire(EVENT_STARTED, this);
    }

    public void stop() {
        if (!running) {
            return;
        }
        setTime(getTime());
        running = false;
        timeStartedMillis = 0;
        eventProvider.fire(EVENT_STOPPED, this);
    }

    public synchronized void handleTick() {
        eventProvider.fire(EVENT_TICK, this);
        if (getTime() == 0) {
            stop();
            eventProvider.fire(EVENT_COMPLETED, this);
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(getTickInterval());
            } catch (Exception e) {
                e.printStackTrace();
            }
            handleTick();
        }
    }
}

