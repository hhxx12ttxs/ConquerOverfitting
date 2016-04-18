package week2_2;

/**
 * Created by Dina on 29.01.2016.
 */
public class Subject {

    private String nameSubject;
    private int semestrHours;
    private int studentWorkedHours;
    private int studentGrade;

    public Subject(String nameSubject, int semestrHours) {
        this.nameSubject = nameSubject;
        this.semestrHours = semestrHours;
    }

    public int passExam (){
        int semestrHours75 = semestrHours / 4 * 3;
        int semestrHours50 = semestrHours / 2;
        int semestrHours25 = semestrHours / 4;

        if(semestrHours75 < studentWorkedHours){
            setStudentGrade(5);
            return 5;
        } if (semestrHours50 < studentWorkedHours && studentWorkedHours < semestrHours75){
            setStudentGrade(4);
            return 4;
        } if (semestrHours25 < studentWorkedHours && studentWorkedHours < semestrHours50){
            setStudentGrade(3);
            return 3;
        } else {
            setStudentGrade(2);
            return 2;
        }
    }

    public void subjectInfo (){
        System.out.println("Name Subject: " + getNameSubject() + ", number of hours per semester: " + getSemestrHours() +
                ",  the number of working hours of a student " + getStudentWorkedHours() + ", student grade: " + getStudentGrade() + "\n");
    }

    public String getNameSubject() {
        return nameSubject;
    }

    public int getSemestrHours() {
        return semestrHours;
    }

    public int getStudentGrade() {
        return studentGrade;
    }

    public int getStudentWorkedHours() {
        return studentWorkedHours;
    }

    public void setStudentWorkedHours(int studentWorkedHours) {
        this.studentWorkedHours = studentWorkedHours;
    }

    public void setStudentGrade(int studentGrade) {
        this.studentGrade = studentGrade;
    }
}

