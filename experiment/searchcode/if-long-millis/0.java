public abstract class BFixedDurationAnimation extends BAnimation{

protected int _totalMillis;
protected int _currentMillis;

protected int currentMillis() {
return _currentMillis;
public final void stepMillis(long millis){
_currentMillis += millis;
if( _currentMillis > _totalMillis ){
millis = _totalMillis-_currentMillis;

