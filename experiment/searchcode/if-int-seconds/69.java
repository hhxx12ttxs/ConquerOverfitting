private int hours;
private int minutes;
private int seconds;

public Clock()
{
setTime();
}

public void setTime()
seconds = currentTime.get(Calendar.SECOND);
}

public Clock(int hours, int minutes, int seconds)
{
this.hours = hours;
this.minutes = minutes;

