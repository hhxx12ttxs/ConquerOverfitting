class Factorial{
public static int factorial (int n){
 if (n==0){
  return 1;
 }
 int a=n;
 while(n > 1){
  a = a* n-1; 
  n=n-1;
 }
 return a;
}
public static void main(String[]args){
 System.out.println(factorial(3));
}
}  

















/*   (n == 0) {
  return 1;
} else {
  return n * factorial (n-1);

 }*/
}


