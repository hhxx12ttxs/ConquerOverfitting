public class linearSearch {
public static int findMax(int[] a) {
int result = a[0];
for(int i = 1; i < a.length; ++i) {
if(a[i] > result) {
result = a[i];
}
return result;
}
}
}

