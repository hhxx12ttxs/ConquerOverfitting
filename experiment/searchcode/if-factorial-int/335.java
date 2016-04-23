public class Factorial {
    private int x;
    
    Factorial(int value){
        x = value;    
    }
    
    public int getFactorial(int x){
        if(x<=0)
            return 1;
        else
            return x * getFactorial(x-1);
    }
    
    public void result(){
        System.out.println(x + "! is equal to " + getFactorial(x));
    }
}

