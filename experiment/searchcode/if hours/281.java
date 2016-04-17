/**
 * Created by anthonymace on 12/31/14.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.io.*;

public class HourTrackerMain {

    public static Scanner console = new Scanner(System.in);

    public static void main(String[] args) {
        String weekOneHours = getHoursFromWeekTest("one");
        String weekTwoHours = getHoursFromWeekTest("two");
        int[] hoursMinutesWeek1 = parseHoursAndMinutesString(weekOneHours);
        int[] hoursMinutesWeek2 = parseHoursAndMinutesString(weekTwoHours);
        int addedHours = addHoursOrMinsFromTwoWeeks(hoursMinutesWeek1, hoursMinutesWeek2, true);
        int addedMins = addHoursOrMinsFromTwoWeeks(hoursMinutesWeek1, hoursMinutesWeek2, false);
        addedHours = convertMinsToHours(addedHours, addedMins);
        int convertedMins = leftOverMinutes(addedMins);
        double decimalTime = hoursToDecimals(addedHours, convertedMins);
        System.out.printf("Your time in decimal format is %.2f", decimalTime);
        writeToFile(addedHours, convertedMins);

    }

    public static String getHoursFromWeekTest(String weekNumber) {
        String hoursAndMinutes = "";
        System.out.printf("Enter your hours from week %s: ", weekNumber);
        hoursAndMinutes = console.nextLine();
        return hoursAndMinutes;
    }

    public static int[] parseHoursAndMinutesString(String hoursAndMinutes) {
        int[] hoursMinutes = new int[2];
        String[] hoursAndMinutesSplit = hoursAndMinutes.split(":");
        int hours = Integer.parseInt(hoursAndMinutesSplit[0]);
        int minutes = Integer.parseInt(hoursAndMinutesSplit[1]);
        hoursMinutes[0] = hours;
        hoursMinutes[1] = minutes;
        return hoursMinutes;
    }

    public static int[] getHoursFromWeek(String weekNumber) {
        int[] hoursMinutes = new int[2];
        System.out.printf("Enter your hours from week %s: ", weekNumber);
        int hours = console.nextInt();
        System.out.println();
        System.out.printf("Enter your minutes from week %s: ", weekNumber);
        int minutes = console.nextInt();
        System.out.println();
        hoursMinutes[0] = hours;
        hoursMinutes[1] = minutes;
        return hoursMinutes;
    }

    public static int addHoursOrMinsFromTwoWeeks(int[] weekOne, int[] weekTwo,boolean hours) {
        if (hours) {
            return weekOne[0] + weekTwo[0];
        } else {
            return weekOne[1] + weekTwo[1];
        }
    }

    public static int convertMinsToHours(int hours, int minutes) {
        if (minutes >= 60) {
            hours++;
        }
        return hours;
    }

    public static int leftOverMinutes(int mins) {
        if (mins >= 60) {
            mins -= 60;
        }
        return mins;
    }

    public static void printHoursAndMinutes(int hours, int mins) {
        if (mins >= 10) {
            System.out.printf("Your hours and minutes for 2 weeks: %d:%d", hours, mins);
        } else {
            System.out.printf("Your hours and minutes for 2 weeks: %d:0%d", hours, mins);
        }
    }


    public static double hoursToDecimals(int hours, int mins) {
        double convertedTime = hours;
        double minsDec = mins / 60.0;
        convertedTime += minsDec;
        return convertedTime;
    }

    public static void writeToFile(int hours, int mins) {
        File logHours = new File("logHours.txt");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        try {
            FileWriter fw = new FileWriter(logHours, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fw);
            bufferedWriter.write(dateFormat.format(date));
            bufferedWriter.write("\n");
            if (mins >= 10) {
                bufferedWriter.write("Your hours and minutes from 2 week pay period: " + hours + ":" + mins);
            } else {
                bufferedWriter.write("Your hours and minutes from 2 week pay period: " + hours + ":0" + mins);
            }
            bufferedWriter.write("\n");
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException exc) {
            System.out.println("Could not write to file");
        }
    }
}
