
public class FindFactorial {
	
	
	public int findFactorial(int find){
		int result = 0;
		if(find ==1){
			return 1;
		}
		result = findFactorial(find - 1)* find;
		return result;
	}
	
	
}

