ZOOM_IN(38), ZOOM_OUT(40), TO_MAIN_MENU(10), START_GAME(10);

private int keyCode;

private ReservedKey(int key) {
setKeyCode(key);
}

public int getKeyCode() {
return keyCode;
}

public void setKeyCode(int keyCode) {
this.keyCode = keyCode;

