public class CWE190_Integer_Overflow__connect_tcp_add_52c
{

public void bad_sink(int data ) throws Throwable
{

int valueToAdd = (new SecureRandom()).nextInt(99)+1; /* adding at least 1 */

/* POTENTIAL FLAW: if (data+valueToAdd) > MAX_VALUE, this will overflow */

