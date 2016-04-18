package HourTracker;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Controller {

    public TextField week1Hours;
    public TextField week2Hours;
    public Label hourTotal;
    public Label toFile;
    public Text title;

    public void calculateHours(ActionEvent actionEvent) {
        String week1 = week1Hours.getText();
        String week2 = week2Hours.getText();
        if (week1.length() == 0 || week2.length() == 0) {
            Alert alertBox = new Alert(Alert.AlertType.INFORMATION);
            alertBox.setTitle("Information Dialog");
            alertBox.setHeaderText(null);
            alertBox.setContentText("Make sure input fields have text!");
            alertBox.showAndWait();
        } else {
            week1 = week1Hours.getText();
            week2 = week2Hours.getText();
            int[] hoursMinutesWeek1 = parseHoursAndMinutesString(week1);
            int[] hoursMinutesWeek2 = parseHoursAndMinutesString(week2);
            int addedHours = addHoursOrMinsFromTwoWeeks(hoursMinutesWeek1, hoursMinutesWeek2, true);
            int addedMins = addHoursOrMinsFromTwoWeeks(hoursMinutesWeek1, hoursMinutesWeek2, false);
            addedHours = convertMinsToHours(addedHours, addedMins);
            int convertedMins = leftOverMinutes(addedMins);
            double decimalTime = hoursToDecimals(addedHours, convertedMins);
            printHours(decimalTime);
            writeHoursToFile(decimalTime, convertedMins);
        }
    }

    public int[] parseHoursAndMinutesString(String hoursAndMinutes) {
        int[] hoursMinutes = new int[2];
        String[] hoursAndMinutesSplit = hoursAndMinutes.split(":");
        int hours = Integer.parseInt(hoursAndMinutesSplit[0]);
        int minutes = Integer.parseInt(hoursAndMinutesSplit[1]);
        hoursMinutes[0] = hours;
        hoursMinutes[1] = minutes;
        return hoursMinutes;
    }

    public int addHoursOrMinsFromTwoWeeks(int[] weekOne, int[] weekTwo,boolean hours) {
        if (hours) {
            return weekOne[0] + weekTwo[0];
        } else {
            return weekOne[1] + weekTwo[1];
        }
    }

    public int convertMinsToHours(int hours, int minutes) {
        if (minutes >= 60) {
            hours++;
        }
        return hours;
    }

    public int leftOverMinutes(int mins) {
        if (mins >= 60) {
            mins -= 60;
        }
        return mins;
    }

    public double hoursToDecimals(int hours, int mins) {
        double convertedTime = hours;
        double minsDec = mins / 60.0;
        convertedTime += minsDec;
        return convertedTime;
    }

    public void printHours(double hoursTotal) {
        String hoursString = String.format("Hours total: %.2f", hoursTotal);
        hourTotal.setText(hoursString);
    }

    public void writeHoursToFile(double hours, int mins) {
        String formattedHours = String.format("Hours total: %.2f", hours);
        File logHours = new File("logHours.txt");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        try {
            FileWriter fw = new FileWriter(logHours, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fw);
            bufferedWriter.write(dateFormat.format(date));
            bufferedWriter.write("\n");
            bufferedWriter.write("Your hours and minutes from 2 week pay period: " + formattedHours);
            bufferedWriter.write("\n");
            bufferedWriter.write("\n");
            bufferedWriter.close();
            toFile.setText("Wrote hours to logHours.txt");
        } catch (IOException exc) {
            toFile.setText("Could not write hours to file");
        }
    }
}

