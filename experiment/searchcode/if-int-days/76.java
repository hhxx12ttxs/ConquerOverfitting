private int time;
private int days;

public Time(int dayLength)
{
this.dayLength = dayLength;
time = 0;
setDays(0);
}

public void add()
{
time++;
if(time >= dayLength)
{
setDays(getDays() + 1);
time = 0;

