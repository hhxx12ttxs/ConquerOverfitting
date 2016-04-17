package OOPHomeWeek2;

public abstract class Subject {

    protected String name;
    protected int hoursTotal;
    protected int hoursWorked;
    protected int scoreSubject;

    public Subject(String name, int hoursTotal, int hoursWorked, int scoreSubject) {
        this.name = name;
        this.hoursTotal = hoursTotal;
        this.hoursWorked = hoursWorked;
        this.scoreSubject = scoreSubject;
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

    public int getScoreSubject() {
        return scoreSubject;
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

    public void setScoreSubject(int scoreSubject) {
        this.scoreSubject = scoreSubject;
    }

    public abstract void study();

    //operations
    public boolean passExamSubject(int hoursWorked) {

        if (hoursWorked < 0 || hoursWorked > this.hoursTotal) {
            System.out.println("Wrong data: hoursWorked");
            return false;
        }

        this.hoursWorked = hoursWorked;
        return (hoursWorked >= (hoursTotal * 0.5)) ? true : false;
    }

    public void showInfoSubject() {

        if (!this.equals(null)) {
            System.out.printf("Subject %s, Hours Total = %d, Hours Worked = %d, Score = %d\n",
                    name, hoursTotal, hoursWorked, scoreSubject);

        } else {
            System.out.print("Wrong data: nameSubject");
        }
    }

    public int getScoreSubject(int scoreSubject) {

        if (scoreSubject <= 0 && scoreSubject > 10) {
            System.out.println("Wrong data: scoreSubject must be from 1 to 10!");
        }

        if (passExamSubject(this.hoursWorked) == true) {
            this.scoreSubject = scoreSubject;
        } else {
            System.out.println("Exam must be passed before input scoreSubject!");
        }

        return scoreSubject;
    }


}

