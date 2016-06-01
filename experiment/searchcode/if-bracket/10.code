public static void bracketMatch(int n, int i, int[] bracket) {
if(n<=0) {
return;
}
if(i == 2*n) {
if(isSafe(bracket,i)){
counter++;
//	printBrakcet(bracket);
}
}
else {
bracket[i] = 1;
if(isSafe(bracket,i)){
bracketMatch(n, i+1, bracket);

