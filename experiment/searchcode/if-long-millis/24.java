private long startMillis;
private long startX;
public int xPerSecond;

// Update x/sec more often when the game starts, end with updating every 10 sec.
public void update(long millis, long x) {
long duration = millis - startMillis;

if (duration >= everyMillis[idx]) {

