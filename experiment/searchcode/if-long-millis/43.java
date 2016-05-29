public class IncrementalWaitTimeStrategy<T> implements WaitTimeStrategy<T> {

final long incrementMillis;
final long maxDelayMillis;
public long computeWaitTime(RetryContext<T> ctx) {
long res = value;
if (res >= maxDelayMillis)
return maxDelayMillis;
value += incrementMillis;
return res;
}

}

