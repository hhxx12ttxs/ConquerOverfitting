public static DateTimeZone getDefault() {
DateTimeZone zone = cDefault.get();

if (zone == null) {
try
long offsetBefore = getOffset(instantBefore);
long offsetAfter = getOffset(instantAfter);

if (offsetBefore <= offsetAfter)

