public int compareTo(ReadableDuration other) {
long thisMillis = this.getMillis();
long otherMillis = other.getMillis();
// cannot do (thisMillis - otherMillis) as it can overflow
if (thisMillis < otherMillis)
{
return -1;
}

if (thisMillis > otherMillis)

