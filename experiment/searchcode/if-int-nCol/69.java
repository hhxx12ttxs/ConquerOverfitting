public static void main(String[] args) throws IOException{
int arnDist[][], nRow = 0, nCol = 0;
String sLine;
Scanner fin = new Scanner (new FileReader(&quot;pathdynamic.txt&quot;));
int[] arnX = {0, 0, -1, -1, -1, 1, 1, 1}, arnY = {-1, 1, -1, 0, 1, -1, 0, 1};
for (int i = 0;i<8;i++){
if (nRow+arnY[i]>=0 &amp;&amp; nCol+arnX[i]>=0 &amp;&amp; nRow+arnY[i]<8 &amp;&amp; nCol+arnX[i]<8 &amp;&amp; !ars2D[nRow+arnY[i]][nCol+arnX[i]].equals(&quot;#&quot;)){

