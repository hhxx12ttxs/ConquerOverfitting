class Program {
    public static void main(String[] a) {
        System.out.println(new Factorial().CalcularFactorial(10));
    }
}
class Factorial {
    public int CalcularFactorial(int Num) {
        int aux;
        if (Num < 1)
            aux = 1;
        else
            aux = Num * (this.CalcularFactorial(Num - 1));
        return aux;
    }
}

