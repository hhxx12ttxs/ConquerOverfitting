public class SpiralMatrix_2 {
public int[][] generateMatrix(int n) {
if (n == 0) {
return new int[0][0];
}
int[][] result = new int[n][n];
int begin = 1;
for(int i = 0; i < n /2; i++) {

