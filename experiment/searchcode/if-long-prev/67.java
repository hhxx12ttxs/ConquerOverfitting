private static PrevTrafValueHolder instance;
private Long prevTrafValueIn = 0L;
private Long prevTrafValueOut = 0L;

public static PrevTrafValueHolder getInstance() {
if (instance == null) {

