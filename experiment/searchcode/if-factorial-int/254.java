
public class FactorialOfAnInteger {

	public static void main(String[] args){
		System.out.println("5 's Factorial? "+calculateFactorialRecursive(12));
		System.out.println("5 's Factorial? "+calculateFactorialIterative(12));
	}
	
	public static int calculateFactorialRecursive(int inputNum){
		int factorialNum = 0;
		if(inputNum >1){
			factorialNum = inputNum*calculateFactorialRecursive(inputNum-1);
		}else{
			factorialNum =1;
		}
		return factorialNum;
	}
	
	public static int calculateFactorialIterative(int inputNum){
		int factorialNum = 0;
		if(inputNum ==1){
			factorialNum =1;
		}
		else{
			factorialNum =1;
			for(int i =2; i<=inputNum; i++){
				factorialNum = factorialNum*i;
		    }
		}
		return factorialNum;
	}
}

