class AssignAMethodArgument{
    public static void main(String[] a){
        System.out.println(new RecursiveFactorial().getFactorial(10));
    }
}

class RecursiveFactorial {
    public int getFactorial(int num){
        
        if (num < 1)
            num = 1 ;
        else
            num = new FastRecursiveFactorial().getFactorial(num,1) ;
        return num ;
    }
}
class FastRecursiveFactorial {
    public int getFactorial(int num, int factorial){
        
        if (num <2)
            num=factorial;
        else
            num = new FastRecursiveFactorial().getFactorial(num-1,factorial*num) ;
        return num ;
    }
}
