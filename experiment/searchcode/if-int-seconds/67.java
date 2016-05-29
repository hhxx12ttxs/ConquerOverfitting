public abstract class ScheduleOnceTask extends ScheduleTask {

private int delay;

public ScheduleOnceTask(int delaySeconds) {
if (delaySeconds < 0)
this.delay = delaySeconds;
}

@Override
public int getScheduleDelaySeconds() {
return delay;
}

@Override

