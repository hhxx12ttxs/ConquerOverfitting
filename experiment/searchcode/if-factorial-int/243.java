public class  B{

	public B(){
		
	}

	public int factorial(int i){
		
		if(i == 1){
			return 1;
		}

		int p = i;

		return i*factorial(--p);
	}
}

