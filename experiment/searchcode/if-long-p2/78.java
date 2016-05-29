abstract class SkeinSmallCore implements Digest {

private static final int BLOCK_LEN = 32;

private byte[] buf, tmpOut;
private int ptr;
private long h0, h1, h2, h3;
long p1 = m1;
long p2 = m2;
long p3 = m3;
long h4 = (h0 ^ h1) ^ (h2 ^ h3) ^ 0x1BD11BDAA9FC1A22L;
long t0 = (bcount << 5) + (long)extra;

