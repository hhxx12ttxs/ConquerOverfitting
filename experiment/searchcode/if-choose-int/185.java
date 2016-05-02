package a5.s100502004;
import java.util.Scanner;

public class A51 {
	public static void main(String[] args){
		int choose;
		int conter=0;
		Scanner input = new Scanner(System.in);
		StackOfIntegers s =new StackOfIntegers();
		for(;;){
			System.out.println("1.push"+"\n"+"2.pop"+"\n"+"3.Show all"+"\n"+"4.exit");		
			choose = input.nextInt();
			
			switch(choose){//choose which method you need
				case 1:
					System.out.print("push ");
					int number1 = input.nextInt();
					s.Push(number1);
					conter=conter+number1;
					break;
				
				case 2:
					System.out.print("pop ");
					int number2 = input.nextInt();
					s.Pop(number2);
					conter=conter-number2;
					break;
				
				case 3:
					System.out.println("show all");
					System.out.println("the stack here is : ");
					s.showAll();
					System.out.println();
					break;
				case 4:
					System.out.println("Goodbye!");
					return;
				default://if you enter the wrong answer 
					System.out.println("You enter the wrong choose! ");
					
			}
		}
	}
}

