int certLen = CipherSuiteUtil.dec16be(buf, 5);
int csLen = CipherSuiteUtil.dec16be(buf, 7);
int connIdLen = CipherSuiteUtil.dec16be(buf, 9);
if (len != 11 + certLen + csLen + connIdLen) {
throw new IOException(
&quot;not a SSLv2 server hello&quot;);

