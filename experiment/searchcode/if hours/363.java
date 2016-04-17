package fr.esir.nsoc.tsen.ade.object;

/**
 * Created by jbourcie on 12/03/2015.
 */
public class DayRoomUsage {
    private String roomID;
    private String RoomName;
    private int numberOfHoursUndefined;
    private int numberOfHoursESIR1;
    private int numberOfHoursESIR2;
    private int numberOfHoursESIR3;

    public DayRoomUsage(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomID() {
        return roomID;
    }

    public int getNumberOfHoursUndefined() {
        return numberOfHoursUndefined;
    }

    public void setNumberOfHoursUndefined(int numberOfHoursUndefined) {
        this.numberOfHoursUndefined = numberOfHoursUndefined;
    }

    public int getNumberOfHoursESIR1() {
        return numberOfHoursESIR1;
    }

    public void setNumberOfHoursESIR1(int numberOfHoursESIR1) {
        this.numberOfHoursESIR1 = numberOfHoursESIR1;
    }

    public int getNumberOfHoursESIR2() {
        return numberOfHoursESIR2;
    }

    public void setNumberOfHoursESIR2(int numberOfHoursESIR2) {
        this.numberOfHoursESIR2 = numberOfHoursESIR2;
    }

    public int getNumberOfHoursESIR3() {
        return numberOfHoursESIR3;
    }

    public void setNumberOfHoursESIR3(int numberOfHoursESIR3) {
        this.numberOfHoursESIR3 = numberOfHoursESIR3;
    }

    public int getEsirUsage (){
        return numberOfHoursESIR1 + numberOfHoursESIR2 + numberOfHoursESIR3;
    }

    public double getEsirUsageRatio(){
        double result =  100.0*(numberOfHoursESIR1 + numberOfHoursESIR2 + numberOfHoursESIR3) / ( numberOfHoursESIR1 + numberOfHoursESIR2 + numberOfHoursESIR3 + numberOfHoursUndefined);
        return result;
    }

    public double getRoomUsageRatio(){
        double result = 100.0*(numberOfHoursESIR1 + numberOfHoursESIR2 + numberOfHoursESIR3 + numberOfHoursUndefined)/8.0;
        return result;
    }

    public double getRoomEsirUsageRatio(){
        double result = 100.0*(numberOfHoursESIR1 + numberOfHoursESIR2 + numberOfHoursESIR3)/8.0;
        return result;
    }

    public void addCourseToDayRoomUsage(Course course){
        if (course.getStudentGroupName().equalsIgnoreCase("undefined")){
            this.numberOfHoursUndefined += course.duree;
        }
        if (course.getStudentGroupName().equalsIgnoreCase("ESIR1")){
            this.numberOfHoursESIR1 += course.duree;
        }
        if (course.getStudentGroupName().equalsIgnoreCase("ESIR2")){
            this.numberOfHoursESIR2 += course.duree;
        }
        if (course.getStudentGroupName().equalsIgnoreCase("ESIR3")){
            this.numberOfHoursESIR3 += course.duree;
        }
    }

    public String getRoomName() {
        return RoomName;
    }

    public void setRoomName(String roomName) {
        RoomName = roomName;
    }
}

