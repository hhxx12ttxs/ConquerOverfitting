arr[4] = -x;
arr[5] = -y;
arr[0] = -y + x;
long result;
if(n%6==0){
result = arr[0];

}else{
result%=1000000007 ;
System.out.println(result);
}

public static long getNthfibo(long n) {

if (n <= 1)
return 0;

