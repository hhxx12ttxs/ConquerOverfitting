
public class Rettangolo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		setAltezza(1+(int)(Math.random()*100));
		setBase(1+(int)(Math.random()*100));
		
		System.out.println("La base del rettangolo č: "+getBase());
		System.out.println("L'altezza del rettangolo č: "+getAltezza());
		System.out.println("\nL'area del rettangolo č: "+area(getBase(), getAltezza()));
		System.out.println("Il perimetro del rettangolo č: "+perimetro(getBase(), getAltezza()));
		System.out.println("La diagonale del rettangolo č: "+diagonale(getBase(), getAltezza()));
		System.out.println("E' un quadrato? "+isQuadrato(getBase(), getAltezza()));
	}
	
	static double altezza, base;
	
	public static void setAltezza(double a){
		altezza = a;
	}
	
	public static double getAltezza(){
		return altezza;
	}
	
	public static void setBase(double b){
		base = b;
	}
	
	public static double getBase(){
		return base;
	}
	
	public static double area(double b, double a){
		double area = a*b;
		
		return area;
	}
	
	public static double perimetro(double b, double a){
		double perimetro = (a+b)*2;
		
		return perimetro;
	}
	
	public static double diagonale(double b, double a){
		double diagonale = Math.sqrt((Math.pow(a, 2))+(Math.pow(b, 2)));
		
		return diagonale;
	}
	
	public static boolean isQuadrato(double b, double a){
		if(a==b)
			return true;
		else
			return false;
	}

}

