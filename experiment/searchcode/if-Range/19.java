public static RangeParam merge(RangeParam a, RangeParam b) {
if (a == null) {
return b;
}

if (b == null) {
Object x = Json.getJson(key, obj);
if (x != null) {
return Range.parse(x);
}
return new Range();
}

}

