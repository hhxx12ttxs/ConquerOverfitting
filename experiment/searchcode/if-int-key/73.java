public class TripleDESKey extends SecurityKey {

private static final int KEY_LENGTH = 24;

/**
* 会检查密钥的长度，如果密钥长度不是 KEY_LENGTH,默认24，会对key的内容进行填充或者截断
*
* @param key
*/
public void setKey(byte[] key) {
if (key.length != KEY_LENGTH) {
byte[] newkey = new byte[KEY_LENGTH];

