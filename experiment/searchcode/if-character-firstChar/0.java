public abstract class GUID {

/**
* Generates a Google App Engine compatible UUID. The first character cannot
* be a number. If the UUID class generates a number first it is replaced
public static String generate() {
String uuid = UUID.randomUUID().toString();

char firstChar = uuid.charAt(0);

if (Character.isDigit(firstChar)) {

