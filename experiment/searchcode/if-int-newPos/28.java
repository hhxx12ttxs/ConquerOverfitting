extends BuiltInAIAgent {
public MediumBuiltInAIAgent(int agentId, String name) {
super(agentId, name);
Point newPos = new Point(state.myHead);
newPos.x+=move.x;
newPos.y+=move.y;
if (Math.random() < 0.99 &amp;&amp; (newPos.x < 0 || newPos.x >= this.width || newPos.y < 0 || newPos.y >= this.height || state.playArea[newPos.x][newPos.y] == 1.0) || (manhattanDist = Math.abs(state.apple.x - newPos.x) + Math.abs(state.apple.y - newPos.y)) > distance &amp;&amp; bestMove != -1) continue;

