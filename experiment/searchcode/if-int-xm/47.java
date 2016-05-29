mLevels.put(LEVEL_WARN, WARN);
mLevels.put(LEVEL_ERROR, ERROR);
}

public XmLoggerLevel(String name, int level) {
return mLevels.get(key.toUpperCase());
}

public static int compare(XmLoggerLevel level1, XmLoggerLevel level2) {
return level1.getLevel() - level2.getLevel();

