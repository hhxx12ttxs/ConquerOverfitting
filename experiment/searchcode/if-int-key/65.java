protected void press(int keyValue) {
if (keyStates.containsKey(keyValue))
keyStates.put(keyValue, true);
}

protected void release(int keyValue) {
if (keyStates.containsKey(keyValue))
keyStates.put(keyValue, false);
}

}

