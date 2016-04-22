package viajero;

/**
 *
 * @author Jose Andres
 */
public class Funciones {
    
    public static int factorial(int n){
        if (n == 1){
            return 1;
        }
        else{
            return n * factorial(n-1);
        }
    }
    
}

