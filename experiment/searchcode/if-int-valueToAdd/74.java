int data = data_container.a;

int valueToAdd = (new SecureRandom()).nextInt(99)+1; /* adding at least 1 */
/* POTENTIAL FLAW: if (data+valueToAdd) > MAX_VALUE, this will overflow */
int result = (data + valueToAdd);

