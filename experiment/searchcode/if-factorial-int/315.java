class WhileLoopFunctionCall{
    public static void main(String[] a){
        System.out.println(new AllFastRecursiveFactorial().getAllFactorial(10));
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

class AllFastRecursiveFactorial extends FastRecursiveFactorial{
    public int getAllFactorial(int num){
        int sum;
        int i;
        int naya;
        i=1;
        sum=0;
        num=num+1;
        while(i<num)
        {
        naya=this.getFactorial(i,1);
        sum=sum+naya;
        i=i+1;
        }
        return sum ;
    }
}
