out.putLong(pageNum);
final short pfxLen = (short) prefix.getLength();
out.putShort(pfxLen);
if (pfxLen > 0)
pageNum = in.getLong();
final short pfxLen = in.getShort();
if (pfxLen > 0) {

