public class NumberRange {
private volatile int lower, upper;

public int getLower() { return lower; }
lower = value;
}

public void setUpper(int value) throws Exception {
if (value < lower)
throw new Exception();
upper = value;
}
}

