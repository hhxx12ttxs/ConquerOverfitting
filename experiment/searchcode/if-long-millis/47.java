long nowNanos = 0, startNanos = 0;
long startMillis = System.currentTimeMillis();
long nowMillis = startMillis;
startMillis = nowMillis;

double maxDrift = 0;
long lastMillis;

while (true) {

