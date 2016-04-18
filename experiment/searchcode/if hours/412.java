/**
* This class represents Student object which contains info about students.
* @author Gyungyoon Yoo
* @version 1.0
*/
public class Student extends Person {
    /**
    * @param studyHours is how much a student studied.
    * @param nonStudyHours is how much a student relaxed.
    */
    private int studyHours;
    private int nonStudyHours;
    /**
    * Constructor for Student.
    * @param firstName is a firstname of a person.
    * @param lastName is a lastname of a person.
    * @param username is a username of a person.
    */

    public Student(String firstName, String lastName, String username) {
        this(firstName, lastName, username, 0, 0);
    }
    /**
    * Constructor for Student.
    * @param firstName is a firstname of a person.
    * @param lastName is a lastname of a person.
    * @param username is a username of a person.
    * @param studyHours is how much a student studied.
    * @param nonStudyHours is how much a student relaxed.
    */
    public Student(String firstName, String lastName, String username,
                    int studyHours, int nonStudyHours) {
        super(firstName, lastName, username);

        if (studyHours >= 0) {
            this.studyHours = studyHours;
        }
        if (nonStudyHours >= 0) {
            this.nonStudyHours = nonStudyHours;
        }
    }
    /**
     * @return studyHours of a student.
     */
    public int getStudyHours() {
        return studyHours;
    }
    /**
     * @return nonStudyHours of a student.
     */
    public int getNonStudyHours() {
        return nonStudyHours;
    }
    /**
     * @return studyPercentage of a student.
     */
    public double getStudyPercentage() {
        double studyPercentage =  (double) studyHours
                                  / (studyHours + nonStudyHours)
                                   * 100;

        return studyPercentage;
    }
    /**
     * @param hours is an int value for how many hours the student studied.
     */
    public void study(int hours) {
        if (studyHours >= 0) {
            studyHours += hours;
        }
    }
    /**
     * @param hours is an int value for how many hours the student relaxed.
     */
    public void relax(int hours) {
        if (nonStudyHours >= 0) {
            nonStudyHours += hours;
        }
    }
}

