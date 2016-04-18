package clock;

public class BerlinClock {
    private int hours;
    private int minutes;
    private int seconds;

    public BerlinClock(int hours, int minutes, int seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public static String getBerlinclockSeconds(int seconds) {
        String yellow = "O";
        if (seconds % 2 == 0) {
            yellow = "Y";
        }
        return yellow + " ";
    }

    public static String getBerlinClockHours(int hours) {
        String firstRow = "OOOO";
        String secondRow = "OOOO";
        if (hours == 5) {
            firstRow = "ROOO";
        } else if (hours == 10) {
            firstRow = "RROO";
        } else if (hours == 15) {
            firstRow = "RRRO";
        } else if (hours == 20) {
            firstRow = "RRRR";
        } else if (hours > 0 && hours < 5) {
            firstRow = "OOOO";
            secondRow = getSecondHourRow(0, hours);
        } else if (hours > 5 && hours < 10) {
            firstRow = "ROOO";
            secondRow = getSecondHourRow(5, hours);
        } else if (hours > 10 && hours < 15) {
            firstRow = "RROO";
            secondRow = getSecondHourRow(10, hours);
        } else if (hours > 15 && hours < 20) {
            firstRow = "RRRO";
            secondRow = getSecondHourRow(15, hours);
        } else if (hours > 15 && hours < 20) {
            firstRow = "RRRR";
            secondRow = getSecondHourRow(20, hours);
        } else if (hours > 20) {
            firstRow = "RRRR";
            secondRow = getSecondHourRow(20, hours);
        } 
        return firstRow + " " + secondRow + " ";
    }

    private static String getSecondHourRow(int startHour, int hours) {
        String secondRow = "";
        for (int i = startHour; i < hours; i++) {
            secondRow += "R";
        }
        secondRow += "OOOO".substring(hours-startHour);
        return secondRow;
    }

    public static String getBerlinClockMinutes(int minutes) {
        String firstRow = "OOOOOOOOOOO";
        char[] firstRowArray = null;
        String secondRow = "OOOO";
        int count = 0;
        int quarterType = minutes / 15 * 3;
        firstRowArray = firstRow.toCharArray();
        for (int i = 0; i < firstRowArray.length; i++){
            if (i < quarterType-1 && i != 2 && i != 5 && i != 8) {
                firstRowArray[i] = 'Y';
                count += 5;
            } else if (i <= quarterType-1 && i != 0) {
                firstRowArray[i] = 'R';
                count += 5;
            } else if (i > quarterType-1 && minutes-count >= 5) {
                firstRowArray[i] = 'Y';
                count += 5;
            }
        }
        if (minutes-count > 0 && (minutes-count) < 5) {
            secondRow = getSecondMinuteRow(minutes-count);
        }
        firstRow = String.valueOf(firstRowArray);
        return firstRow + " " + secondRow + "\n";
    }

    private static String getSecondMinuteRow(int minutesLeft) {
        String secondRow = "";
        for (int i = 0; i < minutesLeft; i++) {
            secondRow += "Y";
        }
        secondRow += "OOOO".substring(minutesLeft);
        return secondRow;
    }

    @Override
    public String toString() {
        return getBerlinclockSeconds(seconds) + getBerlinClockHours(hours) + getBerlinClockMinutes(minutes);
    }
}

