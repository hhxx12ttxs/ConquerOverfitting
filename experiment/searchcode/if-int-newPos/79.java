public class BodyWhite extends Pattern {

@Override
public boolean match(Matcher m) {
int pos = m.getTextPos();
if(pos==0 || m.text.charAt(pos-1)==&#39;\n&#39;) {
char c = m.text.charAt(newPos);
if(c == &#39; &#39;||c == &#39;\t&#39;) {
int n = newPos - pos;
if(n < m.white.length()) {

