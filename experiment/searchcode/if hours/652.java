package ua.land.weekend2.mystudent.model;

/**

 */
public class ListItem extends RootListItem {
    //    - numberOfWorkedHours : int
    private int numberOfWorkedHours;
    //    - assessment : Assessment
    private Assessment assessment;
//    private RootListItem rootListItem;

    //    + getAssessment() : Assessment
//    + passExam() : boolean
    public ListItem(int numberOfWorkedHours, Assessment assessment,
                    Subject subject, int needNumberOfHours, int lowestPassingScore) {
        super(subject, needNumberOfHours, lowestPassingScore);
        this.numberOfWorkedHours = numberOfWorkedHours;
        this.assessment = assessment;
    }

    public int getNumberOfWorkedHours() {
        return numberOfWorkedHours;
    }

    public void setNumberOfWorkedHours(int numberOfWorkedHours) {
        this.numberOfWorkedHours = numberOfWorkedHours;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public boolean passExam() {
        if (numberOfWorkedHours >= getNeedNumberOfHours() ) {
            return true;
        }
        return false;
    }
}


