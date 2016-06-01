199), KEY_UP(200), KEY_PRIOR(201), KEY_LEFT(203), KEY_RIGHT(205), KEY_END(207), KEY_DOWN(208), KEY_NEXT(209), KEY_INSERT(210), KEY_DELETE(211), KEY_LMETA(219), KEY_LWIN(219), KEY_RMETA(220), KEY_RWIN(220), KEY_APPS(221), KEY_POWER(222), KEY_SLEEP(223), KEYBOARD_SIZE(256), KEY_UNKNOWN(-1);

private final int keyCode;
private static final Map<Integer, Keyboard> lookupKeyCode = new HashMap<Integer, Keyboard>();
for (final Keyboard key : values()) {
lookupKeyCode.put(key.keyCode, key);
}
}

public static Keyboard getKey(int key) {
if (lookupKeyCode.containsKey(key)) {

