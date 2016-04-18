package com.mclava.bazaar.util;

public class Messages {

    public static final String PREFIX = "§8(§aBazaar§8) ";

    public static String getReadableDifference(int seconds) {
        int minutes = seconds / 60;
        int hours = minutes / 60;
        minutes %= 60;
        int days = hours / 24;
        hours %= 24;

        String total = "";

        if(days > 0) {
            total += (days + " Days");
            if(hours > 0) {
                total += ("," + hours + " Hours");
            }
        }
        else {
            if(hours > 0) {
                total += (hours + " Hours");
                if(minutes > 0) {
                    total += ("," + minutes + " Minutes ");
                }
            }
            else {
                if(minutes > 0) {
                    total += (minutes + " Minutes ");
                }
            }

        }

        return total;
    }

}

