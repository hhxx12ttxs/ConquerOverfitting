long otherMillis = otherField.getUnitMillis();
long thisMillis = getUnitMillis();
// cannot do (thisMillis - otherMillis) as can overflow
if (thisMillis == otherMillis) {
return 0;
}
if (thisMillis < otherMillis) {

