package jp.co.sevenandinm.kenshuu2015.seven008;

public class Class20150414_02c{
	public static void main(String[] args){
		
		int in_Factorial = Integer.parseInt(args[0]);
		String message = "";
		
		message = "値を入力してください";
		System.out.println(message);
		
		int in_Factorial_Total = factorial_evaluate(in_Factorial);
		
		System.out.println(in_Factorial + "の階乗は" + in_Factorial_Total + "です");
	}
	
	
	public static int factorial_evaluate(int in_Factorial){
		//再帰呼び出しを使った場合
		int in_Factorial_Total = 1;
		if(in_Factorial == 0){
			;
		}else{
			in_Factorial_Total = in_Factorial * factorial_evaluate(in_Factorial - 1);
			System.out.println(in_Factorial);
			System.out.println("この時点での合計" + in_Factorial_Total);
		}
		
		return in_Factorial_Total;
	}
}

