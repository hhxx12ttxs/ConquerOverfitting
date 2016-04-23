public class Main {
	public static void main(String[] args) {
		new Main();
	}
	
	Main() {
		int[] factorial = new int[10];
		
		factorial[0] = 1;
		
		for(int i = 1; i < factorial.length; ++i) {
			factorial[i] = factorial[i - 1] * i;
		}
		
		int answer = 0;
		
		for(int i = 3; i < 100000; ++i) {
			String string = Integer.toString(i);
			
			int sum = 0;
			
			for(int j = 0; j < string.length(); ++j) {
				sum += factorial[string.charAt(j) - '0'];
			}
			
			if(sum == i) {
				answer += i;
			}
		}
		
		System.out.println(answer);
	}
}

