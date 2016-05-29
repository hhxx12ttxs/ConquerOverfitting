pents.put(n, (n * (3 * n - 1) / 2));
}
private static boolean isPent(long l) {
if (((1 + Math.sqrt(24 * l + 1)) / 6) % 1 == 0) {
pents = new HashMap<Long, Long>();
n = 0;
addPent();
addPent();
long smallestDiff = Long.MAX_VALUE;

