octStr = (ASN1OctetString)e.nextElement();
iterationCount = (ASN1Integer)e.nextElement();

if (e.hasMoreElements())
v.add(octStr);
v.add(iterationCount);

if (keyLength != null)
{
v.add(keyLength);

