public static Direction fromYaw(double direction) {
if (direction < 0) {
direction += 360;
public static Direction fromYawCardinal(double direction) {
if (direction < 0) {
direction += 360;
}
if (direction >= 225 &amp;&amp; direction < 315) {

