import java.util.Arrays;

public class DaysChange {
public int[] Solution(int[] days, int n) {
if (days == null || n <= 0)	return days;
int length = days.length;
int[] rvalue = new int[length + 2];

