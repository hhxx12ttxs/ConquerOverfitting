public static void main(String[] args) throws NumberFormatException {
long parse = 0;
double bits = 0;
if(args.length > 0) {
parse = (long)Double.parseDouble(args[0]);
System.out.println((bits = Double.longBitsToDouble(parse)));
System.out.println(Double.doubleToLongBits(bits));

