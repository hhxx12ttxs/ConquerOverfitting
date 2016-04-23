package Expo1;


class Tester {

    public static void main(String[] args) {
        int num = 10;
        String cad = "anona";
        System.out.println("Factorial de " + num + ": " + Rec.factorial(num));
        System.out.println("Suma de los primeros " + num + " pares: " + Rec.sumaPares(num));
        System.out.println("Suma de los primeros " + num + " multiplos de 7: " + Rec.sumaMultiplos7(num));
        System.out.println("El numero " + num + " es primo?    ---> " + Rec.esPrimo(num));
        System.out.println("La cadena \"" + cad + "\" es palindroma? ---> " + Rec.esPalidromo(cad));
        System.out.println("El fibonnaci #" + num + " es: " + Rec.fibonacci(num));
    }
}

class Rec {

    private Rec() {
    }

    public static int factorial(int num) {
        if (num > 1) {
            return num * factorial(--num);
        }
        return 1;
    }

    public static int sumaPares(int num) {
        if (num == 0) {
            return num;
        }
        return sumaPares(num, 2);
    }

    private static int sumaPares(int num, int primerPar) {
        if (num > 0) {
            return primerPar + sumaPares(--num, primerPar + 2);
        }
        return 0;
    }

    public static int sumaMultiplos7(int num) {
        if (num == 0) {
            return 0;
        }
        return sumaMultiplos7(num, 7);
    }

    private static int sumaMultiplos7(int num, int primerMultiplo) {
        if (num > 0) {
            return primerMultiplo + sumaMultiplos7(--num, primerMultiplo + 7);
        }
        return 0;
    }

    public static boolean esPrimo(int num) {
        if (num < 2) {
            return false;
        }
        return esPrimo(num, 2);
    }

    private static boolean esPrimo(int num, int div) {
        if (div > 1 && div < num) {
            if (num % div == 0) {
                return false;
            } else {
                return esPrimo(num, ++div);
            }
        }
        return true;
    }

    public static boolean esPalidromo(String cad) {
        if (cad == null || cad.length() == 0) {
            return false;
        }
        return esPalindromo(cad, 0, cad.length() - 1);
    }

    private static boolean esPalindromo(String cad, int iniIndex, int finIndex) {
        if (iniIndex >= finIndex) {
            return true;
        }
        if (cad.charAt(iniIndex) == cad.charAt(finIndex)) {
            return esPalindromo(cad, ++iniIndex, --finIndex);
        }
        return false;
    }

    public static int fibonacci(int n) {
        if (n == 0 || n == 1) {
            return n;
        }
        return fibonacci(n - 2) + fibonacci(--n);
    }

    /*public static int fibonnaci2(int n) {
        if (n < 1) {
            return 0;
        }
        return fibonacci(n, 0, 0, 0);
    }*/

    /*private static int fibonacci(int n, int prev2, int prev1, int cont) {
        if (cont <= n) {
            return fibonacci()
    
        }
    }*/

    /*private static int fibonnaci(int n){
        int prev1 = 0;
        int prev2 = 0;
        int fib = 0;
        for(int i = 0; i <= n; i++){
            
        }
    }*/
}
