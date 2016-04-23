class FunctionRecursion{
    public static void main(String[] a){
        System.out.println(new RecursiveFactorial().getFactorial(10));
    }
}

class RecursiveFactorial {
    public int getFactorial(int num){
        int num_aux ;
        if (num < 1)
            num_aux = 1 ;
        else
            num_aux = new FastRecursiveFactorial().getFactorial(num,1) ;
        return num_aux ;
    }
}
class FastRecursiveFactorial {
    public int getFactorial(int num, int factorial){
        int num_aux ;
        if (num <2)
            num_aux=factorial;
        else
            num_aux = new FastRecursiveFactorial().getFactorial(num-1,factorial*num) ;
        return num_aux ;
    }
}

