for (int i=0;i<n;i++) {
int x = in.nextInt();
int y = in.nextInt();
if (x == y) {
if (x % 2 == 0)
System.out.println(x * 2);
public static void main(String[] args) throws Exception {
if (args.length >= 1)
System.setIn(new FileInputStream(args[0]));
new NSTEPS().run();
}

}

