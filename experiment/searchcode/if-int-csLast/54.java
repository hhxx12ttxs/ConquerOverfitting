public static boolean containsAny(CharSequence cs, char... searchChars) {
if (isEmpty(cs) || isEmpty(searchChars)) {
return false;
}
int csLength = cs.length();
int searchLength = searchChars.length;
int csLast = csLength - 1;
int searchLast = searchLength - 1;

