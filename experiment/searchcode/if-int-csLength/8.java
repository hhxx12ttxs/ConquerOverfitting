static int[] cslength = new int[50];             // regexp[n]に完全マッチするパタンの長さ

static String[] wordStack = new String[20];
br = new BufferedReader(in);
while ((line = br.readLine()) != null) {
int c = line.charAt(0);
if(c == &#39;#&#39; || c == &#39; &#39; || c == &#39;\t&#39;) continue; // コメント行

