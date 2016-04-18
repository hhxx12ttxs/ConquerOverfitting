class WorkHours {
    static final int SUN = 1; // Sun
    static final int MON = 2; // Moon
    static final int TUE = 3; // Tyr (Nordic god of single combat)
    static final int WED = 4; // Woden aka Odin
    static final int THU = 5; // Thor
    static final int FRI = 6; // Frigg aka Venus
    static final int SAT = 7; // Saturn

    public static void main(String args[]) {
        try {
            for (int i=SUN; i<=SAT; i++) {
                System.out.println(i + " " + workHoursPerDay(i) +
                        " " + workHoursPerDay2(i));
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    static int workHoursPerDay(int day) throws Exception {
        if (day < SUN || day > SAT) {
            throw new Exception("Bad day: " + day);
        }

        int hours;
        if (day == SUN || day == SAT) {
            hours = 0;
        } else if (day == FRI) {
            hours = 7;
        } else {
            hours = 8;
        }
        return hours;
    }

    static int workHoursPerDay2(int day) throws Exception {
        int hours;
        switch (day) {
            case SUN: case SAT:
                hours = 0;
                break;
            case FRI:
                hours = 7;
                break;
            case MON: case TUE: case WED: case THU:
                hours = 8;
                break;
            default:
                throw new Exception("Bad day: " + day);
        }
        return hours;
    }
}

