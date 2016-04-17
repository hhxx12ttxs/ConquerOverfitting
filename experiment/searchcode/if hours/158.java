package io.ipoli.android.quest.ui.formatters;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 1/28/16.
 */
public class DurationFormatter {

    public static String format(Context context, int duration) {
        long hours = 0;
        long mins = 0;
        if (duration > 0) {
            hours = TimeUnit.MINUTES.toHours(duration);
            mins = duration - hours * 60;
        }
        if (hours > 0 && mins == 0) {
            return context.getString(R.string.quest_item_hours_duration, hours);
        } else if (hours > 0) {
            return context.getString(R.string.quest_item_full_duration, hours, mins);
        } else {
            return context.getString(R.string.quest_item_minutes_duration, mins);
        }
    }

    public static String formatReadable(int duration) {
        long hours = TimeUnit.MINUTES.toHours(duration);
        long mins = duration - hours * 60;
        if (hours <= 0 && mins <= 0) {
            return "";
        }
        if (hours > 0 && mins > 0) {
            return hours + "h and " + mins + " min";
        }

        if (hours > 0 && mins == 0) {
            return hours == 1 ? "1 hour" : hours + " hours";
        }

        return mins == 1 ? "1 minute" : mins + " minutes";
    }
}

