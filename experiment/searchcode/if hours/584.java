//Time Conversion

import java.io.*;
import java.util.*;

public class Solution {

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s = br.readLine();
        int hours = Integer.parseInt(s.substring(0,2));
        if(s.charAt(8) == 'P') {
            if(hours < 12) {
                hours += 12;
            }
        }
        if(s.charAt(8) == 'A') {
            hours %= 12;
        }
        if(hours < 10) {
            System.out.println("0" + (hours) + s.substring(2, 8));
        }
        else {
            System.out.println((hours) + s.substring(2, 8));
        }
    }
}

