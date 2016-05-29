long maxStart;
long minStart;

long bytesRead = 0 ;

public LoggerFileReader(InputStream in) {
this.in = in;
byte[] head = new byte[5];
int b = in.read(head);

if (b == -1) return null;
bytesRead = bytesRead + b;

long d = ((head[3] &amp; 0xFFL) << 24)	+ ((head[2] &amp; 0xFFL) << 16)	+ ((head[1] &amp; 0xFFL) << 8)	+ (head[0] &amp; 0xFFL);

