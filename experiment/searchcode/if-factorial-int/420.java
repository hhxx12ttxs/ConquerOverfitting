public class Task10_41 {
    public static void main(String[] args) {
        Assert.assertEquals(24, getFactorial(4));
        Assert.assertEquals(1, getFactorial(1));
        Assert.assertEquals(1, getFactorial(0));
    }

    static int getFactorial(int n) {
        if (n == 0) {
            return 1;
        }
        return n * getFactorial(n - 1);
    }
}

