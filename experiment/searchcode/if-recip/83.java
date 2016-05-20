import java.math.*;
import java.util.regex.*;

//Using java here because of the need for big-decimals and regular expressions.
//The brute force is AWFUL. It isn't even correct; except it works for this problem
//with these parameters. Runs in 2 minutes.
class Main{
    public static void main(String... args){
        int s=0;
        for(int a=2; a<=3000; a++)
            if(isprime(a))
                if(repeat(a) == a-1)
                    s++;
        System.out.println(s);
    }

    static boolean isprime(int i){
        for(int a=2; a*a<=i; a++)
            if(i%a==0) return false;
        return true;
    }

    //matches 142857 in 0.1428571528571429.
    //This regex isn't really correct but is tuned for this particular problem.
    static Pattern pattern = Pattern.compile("\\d*?(\\d{6,}?)\\1+\\d*");
    static int repeat(int d){
        BigDecimal recip = BigDecimal.ONE.divide(new BigDecimal(d), 10000, RoundingMode.UP);
        String recipstr = removeZeros(recip.toString());
        if(recipstr.length()<9000) //Just skip if it doesn't repeat.
            return 0;
        else recipstr = recipstr.substring(1000);
        Matcher m = pattern.matcher(recipstr);
        m.find();
        String cycle = m.group(1);
        return cycle.length();
    }

    static String removeZeros(String s){
        StringBuilder sb = new StringBuilder(s);
        while(sb.charAt(sb.length()-1)=='0')
            sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
}

