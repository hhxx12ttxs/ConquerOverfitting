for (char ch = &#39;A&#39;; ch <= &#39;z&#39;; ch++) {
if (src[ch] > 0) {
if (t[ch] >= src[ch]) {
yay += src[ch];
t[ch] -= src[ch];
System.out.println(String.format(&quot;%d %d&quot;, yay, whoops));
}

}

private char reverseCase(char ch) {
if (Character.isUpperCase(ch)) {
return Character.toLowerCase(ch);

