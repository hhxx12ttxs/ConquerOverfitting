public class Trimming {

public Trimming() {

}

public String lastOutput(String text) {
if (text.equals(&quot;&quot;))return text;
text = check(text, &#39;\t&#39;);
text = check(text, &#39;\n&#39;);

for (int i = text.length() - 1; i >= 0; i--) {

