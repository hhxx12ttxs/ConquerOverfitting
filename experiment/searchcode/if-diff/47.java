int numDistinct(String S, String T) {

int m = S.length();
int n = T.length();

if (m == 0 || n == 0)
return 0;

int[] diff = new int[n + 1];
diff[n] = 1;

for (int i = m - 1; i > -1; i--) {

