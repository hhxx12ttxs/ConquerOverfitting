
public class Task {
    
    public static final int DefaultPriority = 5;
    
    private int priority;
    private double hours;
    
    public Task(double hours) {
        this(hours, Task.DefaultPriority);
    }
    
    public Task(double hours, int priority) {
        this.setHours(hours);
        this.setPriority(priority);
    }
    
    public int getPriority() {
        return priority;
    }
    
    private void setPriority(int priority) {
        if (priority < 1 || priority > 10) {
            throw new RuntimeException("The priority must be between 1 and 10.");
        }
        this.priority = priority;
    }
    
    public double getHours() {
        return hours;
    }
    
    private void setHours(double hours) {
        if (hours < 0) {
            throw new RuntimeException("The hours must be a non-negative number");
        }
        this.hours = hours;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task) {
            Task other = (Task)obj;
            return (this.getHours() == other.getHours() && this.getPriority() == other.getPriority());
        }
        
        return false;
    }
}

