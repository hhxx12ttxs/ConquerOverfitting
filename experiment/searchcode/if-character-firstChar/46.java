char nextChar = str.charAt(index + 1);
if (Character.getNumericValue(curChar) + 1 < Character.getNumericValue(nextChar)) {
return nextInt;
}


private String getResultWithoutChangeFirstChar(String str) {
if (Character.getNumericValue(str.charAt(0)) + str.length() > 9) {

