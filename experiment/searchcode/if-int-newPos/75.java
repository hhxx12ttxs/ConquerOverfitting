String move = &quot;&quot;, oldPiece;
String newPos = &quot;&quot;;
int r=i/8, c=i%8;

for (int j=1; j>=-1; j-=2)
{
for (int k=1; k>=-1; k-=2)
{
if (j!=0 || k!=0)
{
try {
newPos = Board.chessBoard[(r+2*j)][(c+k)];
if (Character.isLowerCase(newPos.charAt(0)) || &quot; &quot;.equals(newPos))

