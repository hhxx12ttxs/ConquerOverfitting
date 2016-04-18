import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        String[] hoursSplitted = s.split(":");
        String hours = hoursSplitted[0];
        

        if(hoursSplitted[2].substring(2, 4).equals("AM")) {
            if(hours.equals("12"))
                hours = "00";
        } else
            if (!hours.equals("12"))
                hours = String.format("%2d", Integer.parseInt(hours) + 12);
        System.out.println(hours + ":" + hoursSplitted[1] + ":" + hoursSplitted[2].substring(0, 2));
    }
}

