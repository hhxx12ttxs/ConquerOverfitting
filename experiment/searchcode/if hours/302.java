package tasks.employee;

public class Task {

   private String name;
   private int workingHours;

    Task(){
        this.name = "Programing";
        this.workingHours = 50;
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
}

