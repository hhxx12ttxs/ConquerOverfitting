int length = tags.length;

for (int i = 0; i < length; i++) {
if (inChunk) {
if (isChunkInTag(tags[i], name)) {
public static void attachOfs(String[] tokens, String[] npChunkTags) {
for (int i = 1; i < npChunkTags.length - 1; i++) {
if (tokens[i].equals(&quot;of&quot;) &amp;&amp; isInNpChunk(npChunkTags[i - 1])

