//
//	Imprime Factoriales
//
public class Factoriales {
			static long factorial(int v){
				if (v>1)	
					return(v*factorial(v-1));
				return(1);
			}
	public static void main(String[] args) {
			int k;
			for(k=1;k<=10;k++)
				System.out.println(factorial(k));
		
	}

}

