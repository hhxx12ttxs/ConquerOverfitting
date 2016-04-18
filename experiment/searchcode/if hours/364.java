package tasks.employee;

public class Employee {

    public static final int DAILY_WORKING_HOURS = 8;
    private String name;
    private Task currentTask;
    private int hoursLeft;
    private static AllWork allWork;

    private Employee() {
    }

    Employee(String name) {
        this();
        if (name != null && !(name.equals(""))) {
            this.name = name;
        }

    }

    void startWorkingDay() {
        this.hoursLeft = DAILY_WORKING_HOURS;
    }


    void work() {

        if (this.currentTask == null) {
            this.currentTask = this.allWork.getNextTask();
            if (this.hoursLeft > 0) {
                this.work();
            }
        } else {
            if (hoursLeft > currentTask.getWorkingHours()) {
                hoursLeft = hoursLeft - currentTask.getWorkingHours();
                currentTask.setWorkingHours(0);
                this.currentTask = null;
                this.work();
            } else if (hoursLeft < currentTask.getWorkingHours()) {
                currentTask.setWorkingHours(currentTask.getWorkingHours() - hoursLeft);
                hoursLeft = 0;
            } else {
                currentTask.setWorkingHours(0);
                hoursLeft = 0;
                this.currentTask = null;
            }
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

    AllWork getAllWork() {
        return allWork;
    }

    void setAllWork(AllWork allWork) {
        if (allWork != null) {
            this.allWork = allWork;
        }
    }


}

