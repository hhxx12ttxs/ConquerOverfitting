public static long checkedAdd(long a, long b) {
long result = a + b;
assertOverflowCondition((a ^ b) < 0 | (a ^ result) >= 0);
return result;
}

public static long checkedSubtract(long a, long b) {
long result = a - b;

