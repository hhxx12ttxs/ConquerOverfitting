public static int normalizePlaceBet(int number, int amount) {
return normalizeBet(PLACE_DIVISORS[toIndex(number)], amount);
private static int normalizeBet(int divisor, int amount) {
if (amount % divisor != 0)
amount += divisor - (amount % divisor);

