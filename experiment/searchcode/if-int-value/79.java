public class ShoppingSurveyDiv1 {

public int minValue(int N, int K, int[] s) {
for (int i = 0; i < N + 1; i++) {
int left = 0;
for (int j = 0; j < s.length; j++)
if (s[j] > i)
left += s[j] - i;

