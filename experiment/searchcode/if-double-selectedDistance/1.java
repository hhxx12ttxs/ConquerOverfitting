public void distanceAction() {
distance = selectedDistance;
if (earthEffect &amp;&amp; simulator!=null) {
simulator.setDistance(distance);
public void onClick(ClickEvent event) {
selectedDistance--;
if (selectedDistance < 50) {
selectedDistance = 50;

