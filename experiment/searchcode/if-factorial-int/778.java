public class Factorial{
 

 public static int Factorial(int n){

  int result;

  if(n<=1){
   return 1;
  }else{
   return n*Factorial(n-1);
  }

 }
 public static void main(String[] args){
   int newthing= Factorial(10); 
    System.out.println(newthing);
}
}
