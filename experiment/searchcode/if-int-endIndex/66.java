board.add(new Space(i));
}
}

public void move (int startIndex, int endIndex) {
if (board.get(endIndex).getIsSlideStartSpace() == true &amp;&amp; board.get(endIndex).getSlideStartColor() != board.get(startIndex).getPeg().getColor()) {
for (int k = endIndex; k <= board.get(endIndex).getSlideEndIndex(); k++) {

