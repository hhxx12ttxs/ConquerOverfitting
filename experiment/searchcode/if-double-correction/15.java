* The overriding maximum value encoders can change the current speed per cycle
*/

public final static double MAX_CORRECTION = 0.5;
return (variable < -deadzoneSize || variable > deadzoneSize) ? variable : 0;
}

public static double limitSpeedTo(double speed, double maxLimit, double minLimit) {
if (speed > maxLimit) return maxLimit;

