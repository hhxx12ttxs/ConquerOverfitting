public class ColorfulBricks {
public int countLayouts(String bricks) {
boolean[] seen = new boolean[50];
int count = 0;

for(int i = 0; i < bricks.length(); i++) {
if(!seen[bricks.charAt(i) - &#39;A&#39;]) {

