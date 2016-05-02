import java.util.Scanner;

public class test_class {

	public static void main ( String [] args ){
		
		System.out.println ("Please choose the type of data you want to operate with (i=int, d=double, b=boolean)");
		Scanner dataType = new Scanner(System.in);
		String readType = dataType.nextLine();
		if ("i".equals(readType)) {
			
			System.out.println ("Please choose the operator so the result would be int or double (+,-,/,*,=,+=)");
			Scanner operandType1 = new Scanner(System.in);
			String readOperand1 = operandType1.next();
			
			System.out.println ("Please type value for A (A operand B)");
			Scanner Ascan = new Scanner(System.in);
			int A1 = Ascan.nextInt();
			
			System.out.println ("Please type value for B (A operand B)");
			Scanner Bscan = new Scanner(System.in);
			int B1 = Bscan.nextInt();		
			
				if ("+".equals(readOperand1)) {
				
					int resultInt = A1 + B1;
					System.out.println ("A+B="+ resultInt);
				}
				
				if ("-".equals(readOperand1)) {
					
					int resultInt = A1 - B1;
					System.out.println ("A-B="+ resultInt);
				}
				
				if ("/".equals(readOperand1)) {
					
					double resultDbl = A1 / B1;
					System.out.println ("A/B="+ resultDbl);
				}
				
				if ("*".equals(readOperand1)) {
					
					int resultInt = A1 * B1;
					System.out.println ("A*B="+ resultInt);
				}
				
				if ("=".equals(readOperand1)) {
					
					int resultInt = A1 = B1;
					System.out.println ("A=B="+ resultInt);
				}

				if ("+=".equals(readOperand1)) {
	
					int resultInt = A1 += B1;
					System.out.println ("A+=B="+ resultInt);
				}
				
				System.out.println ("Please choose the operator so the result would be boolean (==,!=,<,<=,>,>=)");
				Scanner operandType2 = new Scanner(System.in);
				String readOperand2 = operandType2.next();
				
				System.out.println ("Please type value for A (A operand B)");
				Scanner Ascan2 = new Scanner(System.in);
				int A2 = Ascan2.nextInt();
				
				System.out.println ("Please type value for B (A operand B)");
				Scanner Bscan2 = new Scanner(System.in);
				int B2 = Bscan2.nextInt();		
				
					if ("==".equals(readOperand2)) {
					
						boolean resultBool = A2 == B2;
						System.out.println ("A==B ="+ resultBool);
					}
					
					if ("!=".equals(readOperand2)) {
						
						boolean resultBool = A2 != B2;
						System.out.println ("A!=B ="+ resultBool);
					}
					
					if ("<".equals(readOperand2)) {
						
						boolean resultBool = A2 < B2;
						System.out.println ("A<B ="+ resultBool);
					}
					
					if ("<=".equals(readOperand2)) {
						
						boolean resultBool = A2 <= B2;
						System.out.println ("A<=B ="+ resultBool);
					}
					
					if (">".equals(readOperand2)) {
						
						boolean resultBool = A2 > B2;
						System.out.println ("A>B ="+ resultBool);
					}
					
					if (">=".equals(readOperand2)) {
						
						boolean resultBool = A2 >= B2;
						System.out.println ("A>=B ="+ resultBool);
					}
					
					System.out.println ("Please choose one of the unary (prefix) operators (++,---)");
					Scanner operandType3 = new Scanner(System.in);
					String readOperand3 = operandType3.next();
					
					System.out.println ("Please type value for A (A operand)");
					Scanner Ascan3 = new Scanner(System.in);
					int A3 = Ascan3.nextInt();		
					
						if ("++".equals(readOperand3)) {
						
							int resultInt = A3++;
							System.out.println ("++A="+ resultInt);
						}
						
						if ("--".equals(readOperand3)) {
							
							int resultInt = A3--;
							System.out.println ("++A"+ resultInt);
						}
		}
	
if ("d".equals(readType)) {
			
			System.out.println ("Please choose the operator so the result would be double (+,-,/,*,=,+=)");
			Scanner operandType1 = new Scanner(System.in);
			String readOperand1 = operandType1.next();
			
			System.out.println ("Please type value for A (A operand B)");
			Scanner Ascan = new Scanner(System.in);
			double A1 = Ascan.nextDouble();
			
			System.out.println ("Please type value for B (A operand B)");
			Scanner Bscan = new Scanner(System.in);
			double B1 = Bscan.nextDouble();		
			
				if ("+".equals(readOperand1)) {
				
					double resultInt = A1 + B1;
					System.out.println ("A+B="+ resultInt);
				}
				
				if ("-".equals(readOperand1)) {
					
					double resultInt = A1 - B1;
					System.out.println ("A-B="+ resultInt);
				}
				
				if ("/".equals(readOperand1)) {
					
					double resultDbl = A1 / B1;
					System.out.println ("A/B="+ resultDbl);
				}
				
				if ("*".equals(readOperand1)) {
					
					double resultInt = A1 * B1;
					System.out.println ("A*B="+ resultInt);
				}
				
				if ("=".equals(readOperand1)) {
					
					double resultInt = A1 = B1;
					System.out.println ("A=B="+ resultInt);
				}

				if ("+=".equals(readOperand1)) {
	
					double resultInt = A1 += B1;
					System.out.println ("A+=B="+ resultInt);
				}
				
				System.out.println ("Please choose the operator so the result would be boolean (==,!=,<,<=,>,>=)");
				Scanner operandType2 = new Scanner(System.in);
				String readOperand2 = operandType2.next();
				
				System.out.println ("Please type value for A (A operand B)");
				Scanner Ascan2 = new Scanner(System.in);
				double A2 = Ascan2.nextDouble();
				
				System.out.println ("Please type value for B (A operand B)");
				Scanner Bscan2 = new Scanner(System.in);
				double B2 = Bscan2.nextDouble();		
				
					if ("==".equals(readOperand2)) {
					
						boolean resultBool = A2 == B2;
						System.out.println ("A==B ="+ resultBool);
					}
					
					if ("!=".equals(readOperand2)) {
						
						boolean resultBool = A2 != B2;
						System.out.println ("A!=B ="+ resultBool);
					}
					
					if ("<".equals(readOperand2)) {
						
						boolean resultBool = A2 < B2;
						System.out.println ("A<B ="+ resultBool);
					}
					
					if ("<=".equals(readOperand2)) {
						
						boolean resultBool = A2 <= B2;
						System.out.println ("A<=B ="+ resultBool);
					}
					
					if (">".equals(readOperand2)) {
						
						boolean resultBool = A2 > B2;
						System.out.println ("A>B ="+ resultBool);
					}
					
					if (">=".equals(readOperand2)) {
						
						boolean resultBool = A2 >= B2;
						System.out.println ("A>=B ="+ resultBool);
					}
					
					System.out.println ("Please choose one of the unary (prefix) operators (++,---)");
					Scanner operandType3 = new Scanner(System.in);
					String readOperand3 = operandType3.next();
					
					System.out.println ("Please type value for A (A operand)");
					Scanner Ascan3 = new Scanner(System.in);
					double A3 = Ascan3.nextDouble();		
					
						if ("++".equals(readOperand3)) {
						
							double resultInt = A3++;
							System.out.println ("++A="+ resultInt);
						}
						
						if ("--".equals(readOperand3)) {
							
							double resultInt = A3--;
							System.out.println ("++A"+ resultInt);
						}
				}
if ("b".equals(readType)) {
	
	System.out.println ("Please choose the operator (!,||,&&)");
	Scanner operandType1 = new Scanner(System.in);
	String readOperand1 = operandType1.next();
	
	System.out.println ("Please type value for A (A operand B)");
	Scanner Ascan = new Scanner(System.in);
	boolean A1 = Ascan.nextBoolean();
	
	System.out.println ("Please type value for B (A operand B)");
	Scanner Bscan = new Scanner(System.in);
	boolean B1 = Bscan.nextBoolean();		
	
		if ("!".equals(readOperand1)) {
		
			boolean resultInt =!A1;
			System.out.println ("!A="+ resultInt);
		}
		
		if ("||".equals(readOperand1)) {
			
			boolean resultInt = A1 || B1;
			System.out.println ("A||B="+ resultInt);
		}
		
		if ("&&".equals(readOperand1)) {
			
			boolean resultDbl = A1 && B1;
			System.out.println ("A&&B="+ resultDbl);
		}
}
	}

}

