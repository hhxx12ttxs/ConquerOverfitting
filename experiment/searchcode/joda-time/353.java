
/**
 * Links two number displays together. Assumes the second display (the field) is the hours, as the 
 * minutes side is interacted with more often.
 * 
 * @author Charlotte Pierce
 * @version 3/11/2010
 */
public class LinkedDisplay extends NumberDisplay
{
    private NumberDisplay hours;
    
    public LinkedDisplay()
    {
        super(60);
        hours = new NumberDisplay(24);
    }
    
    /**
     * Simulates a tick of the clock. Increments the minutes, and also increments
     * the hours if the minutes tick over.
     */
    public void tick()
    {
        boolean didOverFlow = increment();
        if(didOverFlow){
            hours.increment();
        }
    }
    
    /**
     * Returns a string showing both the hours and the minutes.
     * @return The value of the clock, hours and minutes.
     */
    public String getClockValue()
    {
        return (hours.getDisplayValue() + ":" + getDisplayValue());
    }
    
    /**
     * Change the value of the hours.
     */
    public void setHours(int toWhat)
    {
        hours.setValue(toWhat);
    }
    
    /**
     * Change the value of the minutes.
     */
    public void setMinutes(int toWhat)
    {
        setValue(toWhat);
    }
}

