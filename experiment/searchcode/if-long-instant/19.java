public long add(long instant, int years) {
long added;

if (years == 0)
{
added = instant;
return add(instant, FieldUtils.safeToInt(years));
}

public long addWrapField(long instant, int years) {
if (years == 0)

