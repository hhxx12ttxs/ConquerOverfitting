public static final int MIN_MILLIS = 60 * SEC_MILLIS;
public static final int HOUR_MILLIS = 60 * MIN_MILLIS;

private long startSleep = System.currentTimeMillis();
final long sleep = millis - System.currentTimeMillis() + startSleep;
if (sleep > 0) {
Thread.sleep(sleep);
}
}
}

