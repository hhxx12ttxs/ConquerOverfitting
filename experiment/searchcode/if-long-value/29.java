return true;
}
if (o instanceof LongObjectValue) {
LongObjectValue other = (LongObjectValue) o;
return (longValue.equals(other.longValue));
} else if (o instanceof LongValue) {
LongValue otherVal = (LongValue) o;

