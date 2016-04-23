import java.util.LinkedList;
import java.util.LinkedList;

//This is the factorial calcualtor
public class Factorial {

	LinkedList factorialList;
	int factorialNumber;
	String factorialResult;

	public Factorial() {
		factorialList = new LinkedList<Integer>();
		factorialNumber = 0;
		factorialResult = "";
	}

	public void calculate() {

		factorialList.add(1);

		int num = 2;
		do {
			factorial(num);
			num++;
		} while (num < factorialNumber + 1);

		String eachNode;

		for (int i = factorialList.size() - 1; i >= 0; i--) {
			int number = (int) factorialList.get(i);
			eachNode = Integer.toString(number);

			int size = eachNode.length();

			if (i != factorialList.size() - 1) {
				while (size < 3) {
					factorialResult += "0";
					size++;
				}
			}

			factorialResult += eachNode;

			if (i != 0)
				factorialResult += ",";

		}

	}

	public String getFactorialResult() {
		return factorialResult;
	}

	public int getNumberField()
	{
		return factorialNumber;
	}
	public void setFactorialNumber(int number) {
		factorialNumber = number;
	}

	public void clearAll() {
		while (!factorialList.isEmpty())
			factorialList.removeFirst();

		factorialResult = "";
		factorialNumber = 0;

	}

	public void factorial(int num) {

		int temp;
		int totalTemp;
		int carry = 0;

		for (int i = 0; i < factorialList.size(); i++) {
			totalTemp = 1;
			temp = (int) factorialList.get(i);
			totalTemp *= temp * num + carry;
			carry = 0;
			if (totalTemp % 1000 != totalTemp) {
				int temp2 = totalTemp % 1000;
				factorialList.set(i, temp2);
				carry = (totalTemp - temp2) / 1000;
				if (carry != 0) {
					if (i == factorialList.size() - 1) {
						factorialList.add(carry);
						return;
					}
				}
			} else {
				factorialList.set(i, totalTemp);
			}

		}
	}

}

