private long value;

public MutableLong() {}

public MutableLong(long value)
{
this.value = value;
return new Long(longValue());
}

public boolean equals(Object obj)
{
if ((obj instanceof MutableLong)) {
return this.value == ((MutableLong)obj).longValue();

