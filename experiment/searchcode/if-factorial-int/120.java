class ClassInheritence{
    public static void main(String[] a){
        System.out.println(new FastRecursiveFactorial().getFactorial(10,1));
    }
}

class RecursiveFactorial {
    public int getFactorial(int num, int factorial){
        
        if (num <2)
            num=factorial;
        else
            num = new FastRecursiveFactorial().getFactorial(num-1,factorial*num) ;
        return num ;
    }
}
class FastRecursiveFactorial extends RecursiveFactorial{
    
}
