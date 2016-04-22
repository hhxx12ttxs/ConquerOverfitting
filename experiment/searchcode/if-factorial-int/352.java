public class factorial {
 public static void main(String[] args) {
  for (int i=0;i<=10;i++){
   System.out.println(i + "!\t= " + factorial(i));
  }
 }
 public static int factorial (int t1){
  //if (t1 > 1){return t1*factorial(t1-1);}else{return 0;}
  return (t1 > 1) ? t1*factorial(t1-1) : 1; //shorthand if/else
 }
}

