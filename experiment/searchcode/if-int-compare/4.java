public FastCompare() {
this.compare = NativeFastCompare.getInstance();
}

@Override
public <T> int compare(byte[] array1, int offset1, byte[] array2,
int offset2, int length) {
if (this.compare != null) {

