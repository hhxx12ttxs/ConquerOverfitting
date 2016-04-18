package cs4730.server.model;

import org.jetbrains.annotations.NotNull;

public class FlightTime {
  protected Integer hours;
  protected Integer minutes;

  public FlightTime() {
    this(0, 0);
  }

  public FlightTime(@NotNull Integer hours, @NotNull Integer minutes) {
    setHours(hours);
    setMinutes(minutes);
  }

  public Integer getHours() {
    return hours;
  }

  public void setHours(@NotNull Integer hours) {
    this.hours = (hours > 23 || hours < 0 ? 0 : hours);
  }

  public Integer getMinutes() {
    return minutes;
  }

  public void setMinutes(@NotNull Integer minutes) {
    this.minutes = (minutes > 59 || minutes < 0 ? 0 : minutes);
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    FlightTime that = (FlightTime) o;
    if(!hours.equals(that.hours)) return false;
    if(!minutes.equals(that.minutes)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = hours.hashCode();
    result = 31 * result + minutes.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format("%02d", hours) + String.format("%02d", minutes);
  }
}
