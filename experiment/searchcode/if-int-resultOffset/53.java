private final long[] resultArray;
private final int resultOffset;
private final byte[] counter, recvCounter;
private final IoBuffer ioBuffer;
long sendTime;

EchoProtocolHandler(int messageCount, long[] resultArray, int resultOffset) {
this.messageCount = messageCount;

