public void bad_sink(CWE190_Integer_Overflow__URLConnection_add_67a.Container data_container ) throws Throwable
{
int data = data_container.a;

int valueToAdd = (new SecureRandom()).nextInt(99)+1; /* adding at least 1 */

/* POTENTIAL FLAW: if (data+valueToAdd) > MAX_VALUE, this will overflow */

