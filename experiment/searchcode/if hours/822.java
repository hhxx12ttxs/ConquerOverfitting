package clock.commands;

import clock.UtcClockSingleton;

/**
 * @author Alex
 */
public class SetTimeCommand extends Command{


    private int prevHours, prevMinutes, prevSeconds, hours, minutes, seconds;

    public SetTimeCommand(int hours, int minutes, int seconds){
        super.setName("Set");
        prevHours = UtcClockSingleton.getInstance().getHours();
        prevMinutes = UtcClockSingleton.getInstance().getMinutes();
        prevSeconds = UtcClockSingleton.getInstance().getSeconds();
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    @Override
    public void doCommand() {
        if(hours != -1) UtcClockSingleton.getInstance().setHours(hours);
        if(minutes != -1 ) UtcClockSingleton.getInstance().setMinutes(minutes);
        if(seconds != -1) UtcClockSingleton.getInstance().setSeconds(seconds);
    }

    @Override
    public void undoCommand() {
        if(hours != -1) UtcClockSingleton.getInstance().setHours(prevHours);
        if(minutes != -1)UtcClockSingleton.getInstance().setMinutes(prevMinutes);
        if(seconds != -1) UtcClockSingleton.getInstance().setSeconds(prevSeconds);
    }
}

