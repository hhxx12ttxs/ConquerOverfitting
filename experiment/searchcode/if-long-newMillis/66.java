public Object encode(Object val, MappedField optionalExtraInfo) {
if (val == null) {
return null;
}
Calendar calendar = (Calendar) val;
long millis = calendar.getTimeInMillis();
// . a date so that we can see it clearly in MongoVue
// . the date is UTC because
//   . timeZone.getOffset(millis) - timeZone.getOffset(newMillis)  may not be 0 (if we&#39;re close to DST limits)

