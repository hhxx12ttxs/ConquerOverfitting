package tasks.employee;

public class Employee {

    private String name;
    private Task currentTask;
    private int hoursLeft;

    private Employee() {
    }

    Employee(String name) {
        this();
        if (name != null && !(name.equals(""))) {
            this.name = name;
        }

    }

    String getName() {
        return name;
    }

    Task getCurrentTask() {
        return currentTask;
    }

    int getHoursLeft() {
        return hoursLeft;
    }

    void setName(String name) {
        if (name != null && !(name.equals(""))) {
            this.name = name;
        }
    }

    void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    void setHoursLeft(int hoursLeft) {
        if (hoursLeft > 0) {
            this.hoursLeft = hoursLeft;
        }
    }

    void work(){

        if (hoursLeft > currentTask.getWorkingHours()) {
            hoursLeft = hoursLeft - currentTask.getWorkingHours();
            currentTask.setWorkingHours(0);
        } else if (currentTask.getWorkingHours() > hoursLeft) {
            currentTask.setWorkingHours(currentTask.getWorkingHours() - hoursLeft);
            hoursLeft = 0;
        } else {
            currentTask.setWorkingHours(0);
            hoursLeft = 0;
        }
    }

    void showReport(){
        System.out.println("Name: " + getName());
        System.out.println("Current task: " + this.currentTask.getName());
        System.out.println("Hours left: " + getHoursLeft());
        System.out.println("Hours to finish: " + this.currentTask.getWorkingHours());
    }

}

