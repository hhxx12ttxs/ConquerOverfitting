public void init() {
if (!initialized) {
for (int i = 0; i < quadrant.length; i++) {
for (int j = 0; j < quadrant.length; j++) {
for (board.Sector[] secs : quadrant.getSectors()) {
for (board.Sector sec : secs) {
if (sec.getInhabitant() == spaceObject) {

