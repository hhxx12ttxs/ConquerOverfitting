public class SRM477 {

public int bestSchedule(int N, int K, int[] workingDays) {

int ans = 10000;
for (int k = 0; k < workingDays.length; k++) {
if (workingDays[k] >= i &amp;&amp; workingDays[k] <= j)
c++;
}
ans = Math.min(ans, c);
}

return ans;
}

}

