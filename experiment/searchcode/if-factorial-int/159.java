class Factorial {
    int getFactorial(int n){
        int result;
        if (n <= 1) {
            return 1;
        }
        result = n * getFactorial(n-1);
        return result;
    }
    public static void main(String[] args){
        Factorial fac = new Factorial();
        System.out.println(fac.getFactorial(4));
    }
}

