public abstract class Scaler {

private boolean useDefault    = true;
private double  minVal;
private double  maxVal;
/** Provide a scaler with a specified scaling
*  range to a specified range of bytes.
*/
public Scaler(double minVal, double maxVal,

