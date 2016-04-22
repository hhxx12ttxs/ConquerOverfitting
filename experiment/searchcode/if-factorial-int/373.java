public class Factorial 
{
   public Factorial() { }

   int calculate(int n)
   {
      if(n <= 1) { return 1; }
      else       { return n*calculate(n-1); }
   }

   public static void main (String args[]) 
   {   
      int result = 0, num = 5;

      Factorial fact = new Factorial(); 
      result = fact.calculate(num); 
      System.out.println("Factorial "+num+" = "+result);
   }   
}


