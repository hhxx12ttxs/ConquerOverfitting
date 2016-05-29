public class Curve<O extends ValuedAttribute> extends Span<O>
{
double startValue = 0.0;
double endValue = 0.0;
public Curve( double onset, double endTime, double startValue, double endValue )
{
super( null, onset, endTime );
this.startValue = startValue;

