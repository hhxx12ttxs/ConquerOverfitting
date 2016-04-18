import java.util.Scanner;

public class Spavanac {
    public static void main(String[] args) {
        int interval = 45;
        Scanner in = new Scanner(System.in);
        int hours = in.nextInt();
        int minutes = in.nextInt();
        minutes -= interval;
        if (minutes < 0) {
            minutes = minutes + 60;
            hours--;
        }
        if (hours < 0) {
            hours = hours + 24;
        }
        System.out.println(hours + " " + minutes);
    }
}

