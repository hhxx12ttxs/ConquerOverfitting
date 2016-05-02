package Math24;

public class FourNumbers {
	
private int num[];

public FourNumbers(int a, int b, int c, int d) {
    num = new int[4];
   	num[0] = a;
   	num[1] = b;
   	num[2] = c;
    num[3] = d;
}

public String Sum() {
	String s = new String();
	if (num[0] + num[1] + num[2] + num[3] == 24) {
		s += " (" + num[0] + "+" + num[1] + "+" + num[2] + "+" + num[3] + ");";
	}
	return s;
}

public String MinusOne(int i) {
	String s = new String();
	if (num[0] + num[1] + num[2] + num[3] - 2 * num[i] == 24) {
		s += " (" + num[0];
		for (int j = 1; j < i; j++)
			s += "+" + num[j];
		for (int j = i + 1; j < 4; j++)
			s += "+" + num[j];
		s += "-" + num[i] + ")";
	}
	return s;
}

public String Compute() {
	String s = new String();
	// add all up
	s += Sum();
	// add 3 minus 1
	for (int i = 0; i < 4; i++)
		s += MinusOne(i);

	return s;
}
}

