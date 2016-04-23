import java.util.List;
import java.util.ArrayList;


public class recursion_test {
	public static void main(String [] args){
		System.out.println(fibonacci(10));
		System.out.println(factorial(6));
		System.out.println(stairHop(4));
		System.out.println(robot(0, 0, 3, 3));
		List<String> perms = new ArrayList<String>();
		perms = stringPerm("abc");
		for(String s : perms){
			System.out.println(s);
		}
	}
	
	public static int fibonacci(int n){
		if(n == 0)
			return 0;
		if(n == 1)
			return 1;
		return fibonacci(n-1) + fibonacci(n-2);
	}
	
	public static int factorial(int n){
		if(n < 1)
			return 1;
		return n * factorial(n-1);
	}
	
	/**
	 * A child is running up a staircase with n steps. He can hop either 1, 2, or 3 steps each time. 
	 * Count how many possible ways he can run up the stairs.
	 * @param n
	 * @return
	 */
	public static int stairHop(int n){
		if(n == 0)
			return 1;
		if(n == 1)
			return 1;
		if(n == 2)
			return stairHop(n-2) + stairHop(n-1);
		else
			return stairHop(n-3) + stairHop(n-2) + stairHop(n-1);
	}
	
	/**
	 * How many ways can a robot move from (0,0) to (X,Y) if he can only move right and down.
	 * @param x
	 * @param y
	 * @return
	 */
	public static int robot(int currX, int currY, int x, int y){
		// If arrived at destination, count as one way
		if(currX == x && currY == y){
			return 1;
		}
		// If can only move right
		if(currX == x && currY != y){
			return robot(currX, currY + 1, x, y);
		}
		// If can only move down
		if(currX != x && currY == y){
			return robot(currX + 1, currY, x, y);
		}
		// Can move either direction
		return robot(currX + 1, currY, x, y) + robot(currX, currY + 1, x, y);
	}
	
	public static List<String> stringPerm(String str){
		List<String> perms = new ArrayList<String>();
		if(str.length() == 1){
			perms.add(str);
			return perms;
		}
		
		String firstChar = Character.toString(str.charAt(0));
		String rest = str.substring(1);
		List<String> mid = stringPerm(rest);
		List<String> full = new ArrayList<String>();
		for(int c = 0; c < mid.size(); c++){
			// Insert the first Character into all the permutations
			for(int d = 0; d <= rest.length(); d++){
				String newstr = rest.substring(0,d) + firstChar + rest.substring(d);
				full.add(newstr);
			}
		}
		return full;
	}
}

