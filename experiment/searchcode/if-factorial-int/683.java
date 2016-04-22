class ProjectEuler34 {
	
	public static void main(String[] args) {
	
		int[] factorial = new int[10];
		
		for (int i = 0; i < 10; i++) 
			factorial[i] = findFactorial(i);
			
		for (int i = 3; i < 1000000; i++) {
			
			int sum = 0;
			
			for (int j = 0; j < Integer.toString(i).length(); j++)
				sum += factorial[Integer.parseInt(Integer.toString(i).substring(j, j+1))];
			
			if (sum == i)
				System.out.println(i);
		}		
	}
	
	public static int findFactorial(int f) {
		if (f <= 1)
			return 1;
		return f * findFactorial(f - 1);
	}
	
}
