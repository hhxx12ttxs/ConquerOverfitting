import java.io.*;

public class Factorial {

    public int factorial(int num) {
	if(num == 1) {
	    return num;
	}
	return num * factorial(num-1);
    }

    public static void main(String []args) {
	Factorial ffac = new Factorial();
	System.out.println(ffac.factorial(5));
    }
}

