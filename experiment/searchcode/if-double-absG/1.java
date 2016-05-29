* elimination with {@link Double} numbers.
*/
public class DoubleGaussPivoting {

/**
* Returns a {@link AbsG} strategy instance.
* <p>
* Pivot, if <code>abs(value) > previous</code>
*/
public static GaussPivotingFactory<Double, double[]> ABS_G = new GaussPivotingFactory<Double, double[]>() {

