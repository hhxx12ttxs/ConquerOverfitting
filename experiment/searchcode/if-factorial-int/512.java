public class Factorial {
//4. �������� ���������� ���������� ����� n ����� �������� ++
	private int countFactorial(int n){
	if (n==0) 
		return 0;
	else{
		if(n>0){
			if (n==1) 
				return 1;
			else return n*countFactorial(n-1);
		}
		else{
			if (n==-1) 
			return -1;
			else{	
				return n*countFactorial(n+1);
				}	
			}
		}
	}
	public static void main(String[] args) {
	Factorial i=new Factorial();
	for(int t=-4;t<10;t++){
		System.out.println(i.countFactorial(t));
	}
  }
}

