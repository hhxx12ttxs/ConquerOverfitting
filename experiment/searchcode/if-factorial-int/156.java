public class Factorial {

    public int factorial(int n) {

        int myFactorialResult=1;
        if(n<0 || n>12){
            throw new IllegalArgumentException();
        }
        else if (n == 0)
        {
            return 1;
        }
        else{
            while (n!=0){
                myFactorialResult = myFactorialResult * n;
                n-=1;
            }
            return myFactorialResult;
        }
    }
}
