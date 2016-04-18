package edu.hanshm.zimmerapp.Model.input;

/**
 * @author Florian Hageneder
 * @version 2015-12-14
 */
public enum Sleeptime {
    Never("Never", 0), Less("Less", 1), Two_Hours("Two_Hours", 2), Three_Hours("Three_Hours", 3), Four_Hours(
            "Four_Hours", 4), Five_Hours("Five_Hours", 5), Six_Hours("Six_Hours", 6), Seven_Hours("Seven_Hours",
                    7), Eight_Hours("Eight_Hours", 8), More("More", 9), Always("Always", 10);

    /*
     * =========================================================== Constructor
     */
    private final String translate;
    private final int score;

    Sleeptime(String translation, int score) {
        this.translate = translation;
        this.score = score;
    }

    /*
     * ============================================================ Override
     */
    @Override
    public String toString() {
        return translate;
    }

    public int getScore() {
        return score;
    }

    public static Sleeptime getEnum(int score) {
        Sleeptime result = null;

        for (Sleeptime g : values()) {
            if (g.getScore() == score) {
                result = g;
                break;
            }
        }

        return result;
    }
}

