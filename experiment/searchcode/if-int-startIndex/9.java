public static void printChanges(int[] coins, int[] counts, int startIndex, int totalAmount) {
if(startIndex >= coins.length) {
System.out.println();
return;
}

if(startIndex == coins.length - 1) {
if(totalAmount % coins[startIndex] == 0) {

