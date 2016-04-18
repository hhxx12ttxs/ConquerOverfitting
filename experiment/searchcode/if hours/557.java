public class TimeDataS extends TimeData {
  public TimeDataS(long millis) {
    super(millis);
  }
  public boolean earlierThan(TimeData t) {
    if (
      (getHours() < t.getHours()) ||
      (getHours() == t.getHours() && getMinutes() < t.getMinutes()) ||
      (getHours() == t.getHours() && getMinutes() == t.getMinutes() && getSeconds() < t.getSeconds())
    ) {
      return true;
    } else {
      return false;
    }
  }
}

