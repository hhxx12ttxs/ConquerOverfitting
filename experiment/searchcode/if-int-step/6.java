
public class runner {


//no dp
public static int stepCount(int step) {
if (step < 0)
return stepCount(step-1)+stepCount(step-2)+stepCount(step-3);
}
}
//dp
public static int stepCountDP(int[] cache, int step) {
if (step < 0)

