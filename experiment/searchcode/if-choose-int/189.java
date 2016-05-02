package ce1002.E3.s101502508;

import ce1002.E3.s101502508.stack ;
import java.util.Scanner ;

public class E31 {
	public static void main (String[] args){
		
		Scanner input = new Scanner(System.in) ;
		int choose, n ;
		
		stack stack = new stack() ;

		while (true) {
			System.out.println("1: push \n2: pop \n3: show \n4: exit") ;
			choose = input.nextInt() ;
			
			if (choose == 1){
				stack.push() ;
			}
			
			else if (choose == 2){
				stack.pop() ;
			}
			
			else if (choose == 3){
				stack.show();
			}
			
			else if (choose == 4){
				System.out.print("Good Bye") ;
				break ;
			}
		}
	}
}

