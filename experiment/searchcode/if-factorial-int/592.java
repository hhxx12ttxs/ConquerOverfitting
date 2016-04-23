//Progarm: Factorial by recursion
//Author: warmachine09
//Date: 23-02-2016

class Factorial{
int fact(int n){
int result;
if(n==1)
return 1;
result=fact(n-1)*n;
return result;
}
}

class Recursion{
public static void main(String args[]){
Factorial f = new Factorial();
System.out.println("Factorial 3  "+f.fact(3));
System.out.println("Factorial 7  "+f.fact(7));
System.out.println("Factorial 11 "+f.fact(11));
}
}

