this.bits = new boolean[bits.length];
for (int i = 0; i < bits.length; i++) {
if (bits[i] != 0)
this.bits[i] = true;
bits[i] = !bits[i];
}
}

public long value() {
long result = 0L;
for (int i = bits.length - 1; i >= 0; i--) {
if (bits[i])

