private byte[] buffer;
private int bufferLength;

private byte[] deflated;

public OutputStreamDeflater(OutputStream inner) {
deflater.finish();

while(!deflater.finished()) {
int count = deflater.deflate(deflated);

if(count > 0) {

