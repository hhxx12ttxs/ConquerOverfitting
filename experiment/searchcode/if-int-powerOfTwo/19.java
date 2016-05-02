public class PowerOfTwo
{
    public static void main(String[] args)
    {
        int[] testNumbers = {-5, -4, 0, 1, 2, 4, 5, 7, 9, 22, 1024};

        for (int i : testNumbers) {
            System.out.println(i + " " + isPowerOfTwo1(i));
            System.out.println(i + " " + isPowerOfTwo2(i));
        }
    }

    /**
     * @return true when n is a power of 2 and false otherwise.
     *
     * Discussion:
     *
     * A number is a power of two if only a single bit is set to 1. So check if the int n only has
     * one bit!
     */
    public static boolean isPowerOfTwo1(int n)
    {
        int bitCounter = 0;

        for (int i = 0; i < 32; i++) {
            int bit = n & 0x1;

            if (bit > 0) {
                // The bit is 1, inc the bit counter.
                bitCounter++;

                if (bitCounter > 1) {
                    return false;
                }
            }

            n >>= 1;
        }

        return bitCounter == 1;
    }

    public static boolean isPowerOfTwo2(int n)
    {
        // Bit things are tricky - watch out!
        return (n & (n-1)) == 0 || n == 1;
    }
}

