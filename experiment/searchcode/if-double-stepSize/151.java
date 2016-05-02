package nl.robocodewarriors.robot.valuenormalizers;

public class ValueNormalizer {

    public static final double DEFAULT_HIGH_VALUE = 1.0;
    public static final double DEFAULT_LOW_VALUE = -1.0;
    public static final double ZERO = 0.0;

    public double scaleValueByDefault(double value, double inputLow, double inputHigh) {
        return scaleValue(value, inputLow, inputHigh, DEFAULT_LOW_VALUE, DEFAULT_HIGH_VALUE);
    }

    public double unScaleValueByDefault(double value, double lowValue, double highValue) {
        return scaleValue(value, DEFAULT_LOW_VALUE, DEFAULT_HIGH_VALUE, lowValue, highValue);
    }

    public double scaleValue(double value, double inputLow, double inputHigh, double lowValue, double highValue) {
        double realValue = value;
        if (inputLow < inputHigh) {
            if (value < inputLow) {
                realValue = inputLow;
            } else if (value > inputHigh) {
                realValue = inputHigh;
            }
        } else {
            if (value > inputLow) {
                realValue = inputLow;
            } else if (value < inputHigh) {
                realValue = inputHigh;
            }
        }

        double stepSize = (highValue - lowValue) / (inputHigh - inputLow);
        double baseValue = realValue - inputLow;
        return lowValue + (baseValue * stepSize);
    }
}

