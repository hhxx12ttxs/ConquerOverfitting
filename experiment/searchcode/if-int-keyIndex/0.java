public static boolean nextPermutation(int[] x) {
int N = x.length - 1;
int keyIndex = N, temp;
while (keyIndex > 0 &amp;&amp; x[keyIndex - 1] >= x[keyIndex])
keyIndex--;
keyIndex--;
if (keyIndex < 0)
return false;
int pivotIndex = N;

