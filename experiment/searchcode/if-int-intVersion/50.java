return 0;

intVersion = b[off+3] &amp; 0xff;

if(intVersion > 2 &amp;&amp; (b[off+5] &amp; 0x40) != 0)
intExHeaderSize = 1;		//设置为1表示有扩展头
private int getText(byte[] b, int off, int max_size)  {
int id_part = 4, frame_header = 10;
if(intVersion == 2) {

