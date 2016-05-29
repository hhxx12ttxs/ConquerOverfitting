board.clear();
}
else if (e.getSource() == stopB) {
playB.setEnabled(true);
stepB.setEnabled(true);
clearB.setEnabled(true);
t.stop();
}
else if (e.getSource() == stepB) {
playB.setEnabled(true);

