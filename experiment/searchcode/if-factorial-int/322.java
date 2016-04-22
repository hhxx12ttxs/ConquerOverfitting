import java.io.*;
import java.util.*;

public class Loops {

    private int factorial;

    public int fact (int n) {
	if (n == 0) {
	   return 1;
	}
	else {
	    return n * fact (n-1);
	}
    }

    
	    
    public int fact2 (int n) {
	factorial = 1;

	while (n != 0) {
	    factorial = n * factorial;
	    n = n - 1;

	}

	return factorial;

    }
}

