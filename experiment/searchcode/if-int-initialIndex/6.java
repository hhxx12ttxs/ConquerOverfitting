return null;
}

public static String getJoinedStrings(int initialIndex, String[] args) {
StringBuilder buffer = new StringBuilder(args[initialIndex]);
for (int i = initialIndex + 1; i < args.length; ++i) {

