public void putFrame(final float[] positions_, final int[] lengths_, final byte[] callsigns_) {
if (Constants.FRAME_ON_THE_GO) return;
for (int i=0;i<lengths_.length;i++) {

int cslen = lengths_[i];
System.out.println(prefix+new String( callsigns_, offset, cslen )+&quot; &quot;+

