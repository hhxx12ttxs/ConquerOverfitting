long newMillis = System.currentTimeMillis();
long sleep = 15 - (newMillis - lastMillis);
lastMillis = newMillis;

if (sleep > 1)
try {Thread.sleep(sleep);} catch (InterruptedException ex) {}

