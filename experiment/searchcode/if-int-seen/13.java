private volatile long timestamp;
private volatile boolean seen[];
private volatile int offset;
(nonceCount > count - offset + seen.length)) {
return false;
}
int checkIndex = (int) ((nonceCount + offset) % seen.length);

