byte[] buf = new byte[2];
readFully(in, buf);
int len = dec16be(buf, 0);
if ((len &amp; 0x8000) == 0) {
int certLen = dec16be(buf, 5);
int csLen = dec16be(buf, 7);
int connIdLen = dec16be(buf, 9);
if (len != 11 + certLen + csLen + connIdLen) {

