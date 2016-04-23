/* 
 Copyright (c) 2012 Shaya Potter <spotter@gmail.com>
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.StringTokenizer;

public class TicketLottery {
    static BigDecimal[] factorials = new BigDecimal[1001];
   
    public static void main(String[] args) throws Exception {
        initFactorial();
                
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
              
        String input = in.readLine();
        
        StringTokenizer st = new StringTokenizer(input);
        
        int submissions = Integer.parseInt(st.nextToken());
        int winners = Integer.parseInt(st.nextToken());
        int tickets = Integer.parseInt(st.nextToken());
        int party = Integer.parseInt(st.nextToken());
        
        /* the number we need to win is ceiling(party/tickets) */ 
        int needed;
        if ((party % tickets) != 0) {
            needed = party / tickets + 1;
        } else {
            needed = party / tickets;
        }
        
        //if we need more than possible winners, automatic zero probability
        if (needed > winners) {
            System.out.printf("%0$1.10f\n", (float) 0);
            return;
        }
        
        //if we have guaranteed winners (i.e. submissions - party < winners)
        if (submissions - party < winners) {
            int guaranteed_winners = winners - submissions + party;
            
            //do we have enough winners to have 100%?
            if (guaranteed_winners >= needed) {
                System.out.printf("%0$1.10f\n", (float) 1);
                return;
            }
        }
        
        BigDecimal total = combinations(submissions, winners);
        
        BigDecimal winning = new BigDecimal(0.0);
        for(int i = needed; (i <= winners) && (i <= party); i++) {
            winning = winning.add(combinations(party,i).multiply(combinations(submissions-party, winners-i)));
        }
        
        BigDecimal result = winning.divide(total, 20, RoundingMode.HALF_UP);
        System.out.printf("%0$1.10f\n", result);
    }   
    
    private static void initFactorial() {
        BigDecimal bd = new BigDecimal(1);
        factorials[0] = bd;
        
        for(int i = 1; i <= 1000; i++) {
            factorials[i] = factorials[i-1].multiply(new BigDecimal((double) i));
        }
    }

    private static BigDecimal factorial(int num) throws Exception {
        return factorials[num]; 
    }
    
    private static BigDecimal combinations(int submissions, int winners) throws Exception {
        return factorial(submissions).divide(factorial(submissions-winners), RoundingMode.HALF_UP).divide(factorial(winners), RoundingMode.HALF_UP);
    }
}
