private static NanotimeStrategy strategy;

public static Long nanotime() {
return getStrategy().nanotime();
}

public static NanotimeStrategy getStrategy() {
if (strategy == null)
strategy = new SystemNanotimeStrategy();

