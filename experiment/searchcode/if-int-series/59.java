public class FibonacciSeries {

public static void main(String[] args) {

int n = 10;
for(int i=1; i<=n; i++) {
public static int computeFibonnaciSeries(int n) {

if(n==1 || n==2)
return 1;
else
return computeFibonnaciSeries(n-1)+computeFibonnaciSeries(n-2);
}
}

