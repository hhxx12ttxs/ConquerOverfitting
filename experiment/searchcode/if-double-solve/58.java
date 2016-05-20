import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Scanner;

public class postfix
{
	public static void main(String[] args)
	{
		Scanner text = new Scanner(System.in);
		boolean mode;
		
		while(true)
		{
			System.out.println("Solve infix or postfix?");
			String temp = text.nextLine();
			
			if(temp.equals("infix"))
			{
				mode = true;
				break;
			} else if(temp.equals("postfix")) {
				mode = false;
				break;
			} else {
				System.out.println("Invalid input. Type 'infix' or 'postfix'");
			}
		}
		
		System.out.println("\n");
		
		while(true)
		{
			String temp = text.nextLine();
			
			if(temp.equals("exit")) 
			{
				System.exit(0);
			} else if(temp.equals("change")) {
				mode = !mode;
				System.out.println("Now solving "+(mode?"infix":"postfix"));
			} else {
				
				try
				{
					System.out.println(mode?solveIn(temp):solve(temp));
				} catch(IllegalArgumentException ex) {
					System.out.println("Not a valid "+(mode?"infix":"postfix"));
				}
			}
		}
	}
	
	private static double eval(String post)
	{
		StringTokenizer tok = new StringTokenizer(post);
		double a = Double.parseDouble(tok.nextToken());
		double b = Double.parseDouble(tok.nextToken());
		char op = tok.nextToken().charAt(0);
		
		if(op=='*') return a*b;
		else if(op=='/') return a/b;
		else if(op=='+') return a+b;
		else if(op=='-') return a-b;
		else throw new IllegalArgumentException();
	}
	
	private static double solve(String post)
	{		
		Stack<String> exp = new Stack<String>(), dump = new Stack<String>();
		StringTokenizer tok = new StringTokenizer(post);
		
		int oporc=0,opanc=0;
		while(tok.hasMoreTokens())
		{
			String temp = tok.nextToken();
			
			if(isOperand(temp))
			{
				opanc++;
			} else if(isOperator(temp.charAt(0))) {
				if(temp.length()>1) throw new IllegalArgumentException();
				oporc++;
			} else {
				throw new IllegalArgumentException();
			}
		}
		if(oporc+1!=opanc) throw new IllegalArgumentException();
		
		
		tok = new StringTokenizer(post);
		
		while(tok.hasMoreTokens())
			exp.push(tok.nextToken());
		
		while(!exp.empty())
		{
			dump.push(exp.pop());
			
			if(exp.empty()) return Double.parseDouble(dump.pop());
			
			if(isOperand(dump.peek()) && isOperand(exp.peek()))
			{
				dump.push(exp.pop());
				String arg = dump.pop()+" "+dump.pop()+" "+dump.pop();
				dump.push(""+eval(arg));
				
				while(!dump.empty()) exp.push(dump.pop());
			}
		}
		
		return Double.parseDouble(dump.pop());
	}
	
	private static boolean isOperator(char x)
	{
		return x=='*'||x=='+'||x=='-'||x=='/'||x=='%'||x=='^';
	}
	
	private static boolean isOperand(String x)
	{
		try
		{
			Double.parseDouble(x);
		} catch(Exception ex) {
			return false;
		}
			
		return true;
	}
	
	private static int precedent(char x)
	{
		if(x=='^') return 5;
		else if(x=='*'||x=='/') return 4;
		else if(x=='+'||x=='-') return 3;
		else throw new IllegalArgumentException();
	}
	
	
	private static double solveIn(String x)
	{
		StringTokenizer tok = new StringTokenizer(x);
		Stack<String> exp = new Stack<String>();
		String post="";
		
		try{
		
		
		
			
		while(tok.hasMoreTokens())
		{
			String token = tok.nextToken();
			System.out.println("'"+token+"'");
			if(isOperand(token))
			{
				post += token+" ";
				
			} else if(isOperator(token.charAt(0))) {
				
				while(!exp.empty())
				{
					if(exp.peek().charAt(0)=='(')
					{
						break;
					} else if(precedent(exp.peek().charAt(0))>=precedent(token.charAt(0))) {
						post += exp.pop() + " ";
					} else {
						break;
					}
				}
				
				exp.push(token);
				
			} else if(token.charAt(0)=='(') {
				
				exp.push(token);
				
			} else if(token.charAt(0)==')') {
				
				
				while(true)
				{
					if(exp.peek().charAt(0)=='(')
					{
						exp.pop();
						break;
					}
					
					post += exp.pop()+" ";
				}
				
			}
		}
		
		while(!exp.empty())
		{
			post += exp.pop()+" ";
		}
		post = post.substring(0,post.length()-1);
		
		
		
		
		} catch(Exception ex) {
			throw new IllegalArgumentException();
		}
		
		
		return solve(post);
	}
}
