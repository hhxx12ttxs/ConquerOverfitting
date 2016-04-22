package lesson3;

public class HW_factorial {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println(factorial(4));
	}
	
	static long factorial(int i){
		int j=1;
		long k=1;
		if(i>1){		
		 for(k=j; j<=i; j++){
			k*=j;
		 }
//		 k=i;
//		 k*=factorial(i-1);
		 return k;
		}
		else return 1;
		
	}

}

