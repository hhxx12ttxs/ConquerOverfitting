public static ZDomain getFromDomain(final Domain domain) {
if (domain == null) {
return null;
}
if (domain == Domain.TIME) {
return ZDomain.TIME;
} else if (domain == Domain.DISTANCE) {

