public Object add(Object key, Object value)
{
Object result = null;

int keyIndex = locateIndex(key);

if ( (keyIndex < currentSize)
&amp;&amp; key.equals(entries[keyIndex].getKey()) )
{
// key found; return and replace old value

