//clase Numeros.java que contiene
//una serie de métodos para hacer
//operaciones con números

public class Numeros{

//método que calcula el factorial de un número:
//no vale para números negativos, devolvería 1
//al igual que el factorial de cero
    public static int calcularFactorial1(int numero){
	int factorial = 1; //inicializamos la variable a devolver
	//código empezando por numero y acabando en 1
	if (numero > 0){
	   for(int i= numero; i> 1; i--){
		factorial = factorial * i; //factorial *=i;
	   } 
	}
	return factorial;
    }

//repetimos método pero que multiplique desde 1 a numero:
//1*2*3*4*5*numero
    public static int calcularFactorial2(int numero){
	int factorial = 1;
	if (numero > 0){
	    for (int i=1; i <= numero; i++){
		factorial = factorial * i; //factorial *= i;
	    }
	}
	return factorial;
    } 
}

