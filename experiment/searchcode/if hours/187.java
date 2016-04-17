class NoMagicNumbers {
    public static void main(String args[]) {
        final int DAYS_IN_A_WEEK = 7;
        final int SUNDAY = 0;
        final int FRIDAY = 5;
        final int HOURS_MIDWEEK_DAY = 8;
        final int HOURS_FRIDAY = 7;
        final int HOURS_WEEKEND = 0;

        int h = 0;
        for (int d=0; d<DAYS_IN_A_WEEK; d++) {
            if (d == SUNDAY) {
                h += HOURS_WEEKEND;
            } else if (d < FRIDAY) {
                h += HOURS_MIDWEEK_DAY;
            } else if (d == FRIDAY) {
                h += HOURS_FRIDAY;
            } else {
                h += HOURS_WEEKEND;
            }
        }
        System.out.println(h);
    }
}

