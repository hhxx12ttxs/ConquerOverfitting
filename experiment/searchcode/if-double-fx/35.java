public class MovingAverage {
private Chart _chart;
private long _bound;
private long _fxTime;
private double _total;

public MovingAverage(Chart chart, long bound) {
_total += _chart.getMid(FxTime.fxTimeToSec(fxTime));
fxTime--;
}
}

public void move(long fxTime) {
if(fxTime <= _fxTime - _bound ||

