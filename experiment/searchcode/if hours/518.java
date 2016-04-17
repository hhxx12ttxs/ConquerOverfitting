package tasks.employee;

public class Task {

    private String name;
    private int workingHours;
    private boolean isAssigned;


    Task(String newName, int newWorkingHours) {
        this.name = newName;
        this.workingHours = newWorkingHours;
    }

    public String getName() {
        return name;
    }

    public int getWorkingHours() {
        return workingHours;
    }

    public void setName(String name) {
        if (name != null && !(name.equals("")))
            this.name = name;
    }

    public void setWorkingHours(int workingHours) {
        if (workingHours > 0) {
            this.workingHours = workingHours;
        }
    }

    public boolean getIsAssigned() {
        return isAssigned;
    }

    public void setIsAssigned(boolean assigned) {
        isAssigned = assigned;
    }
}
