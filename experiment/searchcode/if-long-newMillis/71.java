super(Calendar.class);
}

@Override
public Object encode(Object val, MappedField optionalExtraInfo) {
if (val == null) {
// . the date is UTC because
//   . timeZone.getOffset(millis) - timeZone.getOffset(newMillis)  may not be 0 (if we&#39;re close to DST limits)

