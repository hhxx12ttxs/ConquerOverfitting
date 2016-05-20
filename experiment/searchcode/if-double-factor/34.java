package com.atlassian.jconnect.jira.customfields;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.math.DoubleRange;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Simple geographic calculations for the location CF.
 *
 */
public final class GeoCalculator {

    public static final DoubleRange LAT_RANGE = new DoubleRange(-90.0, 90.0);
    public static final DoubleRange LNG_RANGE = new DoubleRange(-180.0, 180.0);

    public static final double LAT_NORMALIZE_FACTOR = 90.0;
    public static final double LNG_NORMALIZE_FACTOR = 180.0;

    public static final DoubleRange NORMALIZED_LAT_RANGE = move(LAT_RANGE, LAT_NORMALIZE_FACTOR);
    public static final DoubleRange NORMALIZED_LNG_RANGE = move(LNG_RANGE, LNG_NORMALIZE_FACTOR);


    private static DoubleRange move(DoubleRange source, double factor) {
        return new DoubleRange(source.getMinimumDouble() + factor, source.getMaximumDouble() + factor);
    }

    private static final double KMS_IN_LAT_DEGREE = 111.0;

    // VERY approximate
    private static Map<DoubleRange, Double> KMS_IN_LNG_DEGREE = ImmutableMap.<DoubleRange, Double>builder()
            .put(newRange(0,10), 110.900)
            .put(newRange(10,20), 107.550)
            .put(newRange(20,30), 100.950)
            .put(newRange(30,40),  91.288)
            .put(newRange(40,50),  78.847)
            .put(newRange(50,60),  63.994)
            .put(newRange(60,70),  47.176)
            .put(newRange(70,80),  28.902)
            .put(newRange(80,90),   9.735)
            .build();

    private static DoubleRange newRange(long lower, long upper) {
        return new DoubleRange(lower, upper);
    }


    private GeoCalculator() {
        throw new AssertionError("Don't instantiate me");
    }

    public static double kmsToLngRatio(double atLatitude) {
        final double absLatitude = Math.abs(atLatitude);
        checkArgument(absLatitude >= 0 && absLatitude <= 90.0, "Latitude <" + atLatitude + "> must be in range 0-90");
        for (Map.Entry<DoubleRange,Double> ratios : KMS_IN_LNG_DEGREE.entrySet()) {
            if (ratios.getKey().containsDouble(absLatitude)) {
                return ratios.getValue();
            }
        }
        throw new AssertionError("dkordonski can't code right!");
    }

    public static double kmsToLongitude(double atLatitude, long kms) {
        final double ratio = kmsToLngRatio(atLatitude);
        return (double) kms / ratio;
    }

    public static double kmsToLatitude(long kms) {
        return (double) kms / KMS_IN_LAT_DEGREE;
    }

    public static double normalizeLat(double lat) {
        return lat + LAT_NORMALIZE_FACTOR;
    }

    public static double normalizeLng(double lng) {
        return lng + LNG_NORMALIZE_FACTOR;
    }
}

