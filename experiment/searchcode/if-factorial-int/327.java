public class Factorial {
   public static void main(String[] args){
   
      System.out.print(Factorial(4));
      return;
   }
   
   public static int Factorial(int n){
      if(n==1){
         return 1;
      }
      else{
         return n*Factorial(n-1);
      }
   }
}
