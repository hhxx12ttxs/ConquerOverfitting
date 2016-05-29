public static String[] findAndReplace(final String[] in,
final String[] what, final String[] with) {
if (in == null || what == null || with == null
|| what.length != with.length) {
return null;
}
final String[] copy = in.clone();

