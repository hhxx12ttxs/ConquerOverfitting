int offset = 0;
for (int i = 0 ; i < (currentFrame.planeCnt) ; i++) {
int cslen = currentFrame.lengths[i];
offset += cslen;
}
}

int frameno = -1;

public void setFrame(final RawFrame f) {
if (((Constants.DEBUG_DETECTOR) || (Constants.DUMP_RECEIVED_FRAMES)) || (Constants.SYNCHRONOUS_DETECTOR)) {

