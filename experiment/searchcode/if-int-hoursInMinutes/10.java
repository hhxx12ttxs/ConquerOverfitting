return zone;
}
if (id.startsWith(&quot;+&quot;) || id.startsWith(&quot;-&quot;)) {
int offset = parseOffset(id);
throw new IllegalArgumentException(&quot;Minutes out of range: &quot; + minutesOffset);
}
int offset = 0;
try {
int hoursInMinutes = FieldUtils.safeMultiply(hoursOffset, 60);

