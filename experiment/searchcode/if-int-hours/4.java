package office;

public class Task {

private String name;
private int workingHours;

public Task(String name, int workingHours){
setWorkingHours(workingHours);

}

public int getWorkingHours() {
return workingHours;
}

void setWorkingHours(int workingHours) {
if(workingHours >= 0)

