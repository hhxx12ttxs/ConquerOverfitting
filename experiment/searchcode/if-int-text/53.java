public static String removeText(String text, String beginText, String endText) {
int begin = 0;
int end = 0;
while (begin >= 0 &amp;&amp; end >= 0) {
begin = text.indexOf(beginText);
if (begin >= 0) {
end = text.indexOf(endText);

