public void putFrameInternal(final float[] positions_, final int[] lengths_, final byte[] callsigns_) {
if ( (last + 1) % Constants.BUFFER_FRAMES == first) {
public void putFrame(final float[] positions_, final int[] lengths_, final byte[] callsigns_) {

if (Constants.SYNCHRONOUS_DETECTOR || Constants.DUMP_SENT_FRAMES) {

