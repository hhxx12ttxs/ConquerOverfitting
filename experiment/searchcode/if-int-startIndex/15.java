public class PrintAllCoins {
public static void print(int[] coins, int[] counts, int startIndex, int total){
if (startIndex>=coins.length){
for (int i = 0; i < coins.length-1; i++) {
System.out.print(counts[i] +&quot;*&quot;+coins[i]+&quot; + &quot;);

