IOUtil.readFully(in, buf);

// check for record without padding
int len = IOUtil.dec16be(buf, 0);
if ((len &amp; 0x8000) == 0) {
int certLen = IOUtil.dec16be(buf, 5);

// read cipher suites data length
int csLen = IOUtil.dec16be(buf, 7);

// read connection id data length

