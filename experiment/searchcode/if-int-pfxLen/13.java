final short pfxLen = (short) prefix.getLength();
out.putShort(pfxLen);
if (pfxLen > 0)
{out.put(prefix.data(), prefix.start(), pfxLen);}
out.putShort((short) nValues);
short len;
for (int i = 0; i < nValues; i++) {

