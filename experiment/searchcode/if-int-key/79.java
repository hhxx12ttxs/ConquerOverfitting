KEYBOARD_SIZE(256),
KEY_UNKNOWN(-1);

private final int keyCode;
private static final Map<Integer, Keyboard> lookupKeyCode = new HashMap<Integer, Keyboard>();
public int getKeyCode() {
return keyCode;
}

public static Keyboard getKey(int key) {
if (lookupKeyCode.containsKey(key)) {

