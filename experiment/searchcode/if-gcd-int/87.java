public class e1_1_24 {

	public static int gcd(int p, int q) {
		if(q == 0)
			return p;
		int r = p % q;
		return gcd(q, r);
	}

	public static void main(String[] args) {
		if(args.length < 2) {
			System.out.println("2 numbers required!");
			System.exit(0);
		}

		int a = Integer.parseInt(args[0]);
		int b = Integer.parseInt(args[1]);

		System.out.format("GCD of %d and %d is %d\n",a, b, gcd(a, b));
	}
}

