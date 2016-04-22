class Factorial {

    private static final int x=5;
    public static void main(String[] args) {

        Factorial o = new Factorial();
        System.out.println(o.factorial(x));
    }

    private int factorial(int n)
    {
        if(n<1){
            return 1;
        }
        else return n*factorial(n-1);
    }
}


