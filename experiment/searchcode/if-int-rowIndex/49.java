public class NewSolution {
public ArrayList<Integer> getRow(int rowIndex) {
ArrayList<Integer> ar = new ArrayList<Integer>();
int[] out = new int[rowIndex+1];
int half = rowIndex>>1;
for(int i=0;i<rowIndex+1;i++){

