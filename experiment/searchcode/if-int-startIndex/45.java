public static int maxProfit(int[] prices) {
if(prices.length == 0){
return 0;
}
int totalProfit = 0;
int startIndex = 0;
int i;

for(i=1; i<prices.length; i++){

