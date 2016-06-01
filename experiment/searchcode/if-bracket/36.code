Bracket bracket = getCachedBracket(bracketOID);
if(bracket == null) {
Tournament tournament = sp.getSingleton().getTournamentManager().getTournament(tournamentOID);
public Bracket getBracket(int bracketOID) {
Bracket bracket = getCachedBracket(bracketOID);
if(bracket == null) {
new BracketGetBroker(sp, bracketOID).execute();

