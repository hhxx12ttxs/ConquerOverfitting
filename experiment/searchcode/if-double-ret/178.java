package noituk.calculator;

public class Calculator {
	private double firstNum;
	private double secondNum;
	private double memoryNum;
	private char operation;
	private double result;
	private boolean comma;
	
	
	
	private String current;
	
	
	
	
	public String getCurrent() {
		return current;
	}


	public void setCurrent(String current) {
		this.current = current;
	}


	public double getResult() {
		return result;
	}


	public Calculator(){
		this.firstNum = 0.0;
		this.secondNum = 0.0;
		this.memoryNum = 0.0;
		this.operation = ' ';
		this.result = 0.0;
		this.current = "0";
	}
	

	public void setOperation(char c) {
		this.operation = c;
		this.firstNum = Double.parseDouble(this.current);
		this.current = "0";
	}
	
	
	
	public double returnCurrentValue(){
		double ret = 0.0;
		if(operation == ' ')ret = this.firstNum;
		else ret = this.secondNum;
		return ret;
		
	}


	public void addNumToCurrent(char c) {
		if(this.current.equals("0") || this.current.equals(""))this.current = ""+c;
		else this.current = this.current+""+c;
	}


	public void solve() {
		this.secondNum = Double.parseDouble(this.current);
		switch(this.operation){
		case '/':
			this.result=this.firstNum / this.secondNum;
			this.firstNum = this.result;
			this.current = Double.toString(this.result);
			break;
		case 'x':
			this.result=this.firstNum * this.secondNum;
			this.firstNum = this.result;
			this.current = Double.toString(this.result);
			break;
		case '+':
			this.result=this.firstNum + this.secondNum;
			this.firstNum = this.result;
			this.current = Double.toString(this.result);
			break;
		case '-':
			this.result=this.firstNum - this.secondNum;
			this.firstNum = this.result;
			this.current = Double.toString(this.result);
			break;
		default:
			break;
		}
	}


	public void memoryAction(char c) {
		switch(c){
		case 'c':
			this.memoryNum = 0.0;
			break;
		case '+':
			this.memoryNum = this.memoryNum + Double.parseDouble(current);
			break;
		case '-':
			this.memoryNum = this.memoryNum - Double.parseDouble(current);
			break;
		case 'r':
			this.current = Double.toString(this.memoryNum);
			break;
		default:
			break;
		}
	}
	
}

