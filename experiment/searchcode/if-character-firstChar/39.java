public static String startWithLowerCase(String methodName) {
if(methodName.length() > 1 &amp;&amp; Character.isUpperCase(methodName.charAt(1))) return methodName;
char firstChar = Character.toLowerCase(methodName.charAt(0));

