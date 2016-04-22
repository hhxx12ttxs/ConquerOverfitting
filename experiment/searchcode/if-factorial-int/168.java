
public class recursion3 {

	

	    public static int factorial(int x){
		
		if(x == 0){
		    return 1;
		}
		return x*factorial(x);
	    }


	    public static void main(String[] arguments){
		factorial(5);

	    }

	
}

