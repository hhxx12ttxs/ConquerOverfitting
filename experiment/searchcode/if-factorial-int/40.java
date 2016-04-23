package euler;

import java.util.ArrayList;

public class Pbt24 {

	public static void main(String[] args) {

		ArrayList<Integer> nums = new ArrayList<Integer>(10);
		for (int i = 0; i < 10; i++) {
			nums.add(i);
		}

		String resp = "";
		int tope=1000000;
		int contador=9;
		while(!nums.isEmpty() && contador>=0){
			int i = 1;
			boolean termino = false;
			while(i<nums.size() && !termino){
				int mult = i*factorial(contador);
				if(mult<tope)
					i++;
				else{
					i--;
					tope-=i*factorial(contador);
					resp+=nums.remove(i);
					termino=true;
				}
				if(i==nums.size()){
					i--;
					tope-=i*factorial(contador);
					resp+=nums.remove(nums.size()-1);
					termino=true;
				}	
			}
			contador--;
		}

		System.out.println(resp+nums.get(0));
	}

	public static int factorial(int n){
		int result=1;
		for (int i = 1; i < n+1; i++) {
			result*=i;
		}
		return result;
	}
}

