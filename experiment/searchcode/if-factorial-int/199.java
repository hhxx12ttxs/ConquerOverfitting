public class factorial {
public static void main (String args[]){
// factorial f = new factorial();
System.out.println("8!  =");
System.out.println(HitungFactorial(8));
}
public static int HitungFactorial (int x){
if (x==1){
return 1;
}
else{
return x * HitungFactorial(x-1);
}

}
}
