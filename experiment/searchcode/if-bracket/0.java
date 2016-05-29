private void expandLoserBracket(Bracket bracket) {
if (bracket.getHomeBracket() != null) {
expandLoserBracket(bracket.getHomeBracket());
}
if (bracket.getAwayBracket() != null) {
expandLoserBracket(bracket.getAwayBracket());

