package week2.univer;

/**
 * Created by Iurii on 24.01.2016.
 */
public class Subject {
    private String name;
    private int totalHours;
    private int studentHours = 0;
    private int studentMark = 0;

    public Subject(int totalHours, String name) {
        this.totalHours=totalHours;
        this.name = name;
    }

    public void passTest () {
        double progress = 100*studentHours/totalHours;
        if (progress < 40) {
            studentMark=2;
        } else {
            if (progress >= 40 && progress < 60) {
                studentMark=3;
            } else { if (progress >= 60 && progress <80) {
                    studentMark=4;
                    }else studentMark=5;

            }
        }

    }
    public String getInfo () {
        return null;
    }

    public String getName() {
        return name;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public int getStudentHours() {
        return studentHours;
    }

    public int getStudentMark() {
        return studentMark;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }

    public void setStudentHours(int studentHours) {
        this.studentHours = studentHours;
    }

    public void setStudentMark(int studentMark) {
        this.studentMark = studentMark;
    }
}

