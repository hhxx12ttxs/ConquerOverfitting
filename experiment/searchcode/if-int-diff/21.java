import java.util.Arrays;


public class ClosestDiffToZero {

public Difference getDifference(int[] a) {

Arrays.sort(a);

int minDiff = Integer.MAX_VALUE;
int currDiff ;
int i = 0;

