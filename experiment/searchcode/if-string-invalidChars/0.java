public InvalidPathException(String path, char[] invalidChars, String[] aditionalValidations) {
super(createMessage(path, invalidChars, aditionalValidations));
}

private static String createMessage(String path, char[] invalidChars, String[] aditionalValidations) {

