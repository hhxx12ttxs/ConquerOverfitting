public void init(int[][] position, boolean useOldSubtree) {
GameState s = new GameState(position);

if (useOldSubtree &amp;&amp; rootNod != null) {
// certain depth)
TreeNode nod = rootNod.find(s, 6);
rootNod = nod;
if (rootNod != null)
return;

