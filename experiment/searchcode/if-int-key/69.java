int scanCode = Integer.parseInt(scanCodeStr);
int intKeyMap = 0;
if (mapKey.contains(&quot;SOFT_LEFT&quot;)) {
intKeyMap = KeyEvent.KEYCODE_SOFT_LEFT;
} else if (mapKey.contains(&quot;SOFT_RIGHT&quot;)) {
intKeyMap = KeyEvent.KEYCODE_SOFT_RIGHT;

