public Standings getStandings(FinalTwoBracket finalTwoBracket, FinalOneBracket finalOneBracket, LoserBracket loserBracket) {
Standings standings = new Standings();
if (finalTwoBracket.getHead().getGame().isInGameState(GameState.Finished)) {
standings.addPlace(&quot;2&quot;, finalTwoBracket.getHead().getGame().getLoser());
} else if (finalOneBracket.getHead().getGame().isInGameState(GameState.Finished) &amp;&amp; (!finalTwoBracket.getHead().getGame().isOccupied())) {

