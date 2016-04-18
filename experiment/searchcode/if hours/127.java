package OOPHomeWeek2.Study;

public class Subject {

    private String name;
    private int hoursTotal;
    private int hoursWorked;
    private int score;

    public Subject(String name, int hoursTotal, int hoursWorked, int score) {
        this.name = name;
        this.hoursTotal = hoursTotal;
        this.hoursWorked = hoursWorked;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getHoursTotal() {
        return hoursTotal;
    }

    public int getHoursWorked() {
        return hoursWorked;
    }

    public int getScore() {
        return score;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHoursTotal(int hoursTotal) {
        this.hoursTotal = hoursTotal;
    }

    public void setHoursWorked(int hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public void setScore(int score) {
        this.score = score;
    }

    //operations
    public void study() {
        System.out.println(" study!");
    }

    public void showInfoSubject() {

        if (!this.equals(null)) {
            System.out.printf("Subject %s, Hours Total = %d, Hours Worked = %d, Score = %d\n",
                    name, hoursTotal, hoursWorked, score);

        } else {
            System.out.print("Wrong data: subject");
        }
    }

    public boolean passExamSubject(int hoursWorked) {

        if (hoursWorked < 0 || hoursWorked > this.hoursTotal) {
            System.out.println("Wrong data: hoursWorked");
            return false;
        }

        return (hoursWorked >= (hoursTotal * 0.5)) ? true : false;
    }

    public int getScoreSubject() {

        if (hoursWorked >= (hoursTotal * 0.5)) {
            score = 1 + (int) (Math.random() * 9);
        } else {
            System.out.println("Exam must be passed before input scoreSubject!");
        }
        return score;
    }
}




