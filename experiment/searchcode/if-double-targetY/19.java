private final int MAX_EXP_RADIUS = 50;

private double xpos, ypos;
private int targetx, targety, speed;
// check if we are within 2 pixels of target:
if (Math.abs(xpos - targetx) < 2 &amp;&amp; Math.abs(ypos - targety) < 2) {
// missile has reached target:

