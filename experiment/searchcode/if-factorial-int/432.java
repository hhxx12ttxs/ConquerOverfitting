public class Tarea2Lenguajes{
	public static void main(String[] args) {
		int x = 5;	//inicializamos 3 variables
		int y = 3;
		int z = 6;
		int r = factorial(z,(factorial(x,y))); //en r gurdamos la expresion factorial(z,factorial(x,y))
		z = 1; //ahora modificamos los valores que recibira factorial
		x=3;
		y=9;

		System.out.println(r); //devuelve el valor de factorial con los valores de inicio, siendo que nuestra evaluacion es lazy y esperabamos 720
	}						   //aqui necesitamos r y r es igual a factorial(z,(factorial(x,y))), z = 1, x = 3 y y = 9 --> r = 1 pero nosotros queriamos el estado
							   //inicial x = 5, y = 3, z = 6 donde r = 720
	static int factorial(int n, int m){
		if(n==1)
			return 1;
		return (n*factorial(n-1,m));
	}
}
