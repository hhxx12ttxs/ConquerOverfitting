/*
 * Created by IntelliJ IDEA.
 * User: sg426575
 * Date: Jun 29, 2004
 * Time: 2:35:06 AM
 */
package com.technoetic.xplanner.tags.displaytag;

/**
 * The Class HoursDecorator.
 */
public class HoursDecorator {
    
    /**
     * Gets the percent completed score.
     *
     * @param estimatedHours
     *            the estimated hours
     * @param actualHours
     *            the actual hours
     * @param remainingHours
     *            the remaining hours
     * @param completed
     *            the completed
     * @return the percent completed score
     */
    public static double getPercentCompletedScore(final double estimatedHours,
            final double actualHours, final double remainingHours,
            final boolean completed) {
        if (completed) {
            return actualHours + 2;
        }
        if (estimatedHours == 0) {
            return -1;
        } else {
            return 1 - remainingHours / estimatedHours;
        }
    }

    /**
     * Gets the remaining hours score.
     *
     * @param actualHours
     *            the actual hours
     * @param remainingHours
     *            the remaining hours
     * @param completed
     *            the completed
     * @return the remaining hours score
     */
    public static double getRemainingHoursScore(final double actualHours,
            final double remainingHours, final boolean completed) {
        if (completed) {
            return actualHours * -1;
        }
        return remainingHours;
    }

    /**
     * Format percent difference.
     *
     * @param originalHours
     *            the original hours
     * @param finalHours
     *            the final hours
     * @return the string
     */
    public static String formatPercentDifference(final double originalHours,
            final double finalHours) {
        final int error = HoursDecorator.getPercentDifference(originalHours,
                finalHours);
        String str = error >= 0 ? "+" : "-";
        str += Math.abs(error);
        str += '%';
        return str;
    }

    /**
     * Gets the percent difference.
     *
     * @param originalHours
     *            the original hours
     * @param finalHours
     *            the final hours
     * @return the percent difference
     */
    public static int getPercentDifference(final double originalHours,
            final double finalHours) {
        final double delta = finalHours - originalHours;
        final int error = (int) (delta / originalHours * 100);
        return error;
    }
}
