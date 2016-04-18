package us.abusewith.awuchopper;

import org.osbot.rs07.script.MethodProvider;

public class FatigueHandler {

    private static final FatigueHandler INSTANCE = new FatigueHandler();

    private long startTime;

    private long clicks;

    private FatigueHandler() {
    }

    public static FatigueHandler getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        startTime = System.currentTimeMillis();
    }

    public void clicked() {
        clicks++;
    }

    public long getReactionTime() {
        int hours = getHours();
        if (hours < 2) {
            return MethodProvider.random(260, 310);
        } else if (hours >= 2 && hours <= 4) {
            return MethodProvider.random(380, 420);
        } else if (hours > 4 && hours <= 10) {
            return MethodProvider.random(420, 450);
        } else {
            return MethodProvider.random(450, 510);
        }
    }

    public int getCameraActivity() {
        int hours = getHours();
        if (hours < 1) {
            return 21;
        } else if (hours >= 2 && hours <= 4) {
            return 50;
        } else if (hours > 4 && hours <= 10) {
            return 80;
        } else {
            return 110;
        }
    }

    public int getMouseActivity() {
        int hours = getHours();
        if (hours < 1) {
            return 18;
        } else if (hours >= 2 && hours <= 4) {
            return 50;
        } else if (hours > 4 && hours <= 10) {
            return 80;
        } else {
            return 110;
        }
    }

    public int getExcitement() {
        int hours = getHours();
        if (hours < 1) {
            return 80;
        } else if (hours >= 2 && hours <= 4) {
            return 100;
        } else if (hours > 4 && hours <= 10) {
            return 120;
        } else {
            return 140;
        }
    }

    private int getHours() {
        return (int) ((((System.currentTimeMillis() - startTime) / 1000) / 60) / 60);
    }

}

