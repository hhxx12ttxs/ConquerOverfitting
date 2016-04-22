class SolveTheBracketProblem{
    public static void main(String[] a){
        System.out.println(new Square().getFactorial(1,15));
    }
}

class RecursiveFactorial {
    public int getFactorial(int num, int factorial){
        
        if (num <2)
            num = new FastRecursiveFactorial().getFactorial(15,factorial*num);
        else
            num = factorial*factorial ;
        return num ;
    }
}
class FastRecursiveFactorial extends RecursiveFactorial{
    
}

class Square extends FastRecursiveFactorial{
    
}
