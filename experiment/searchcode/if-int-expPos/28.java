//all the matcher functions below have the invariant that
//they return the match at the start of the string str if one exists
private static int identPatternMatcher( String str, int start ) {
if( begin == str.charAt( nonSpace ) ){
++nonSpace;++runningLength;
int expPos = expPatternMatcher( str, nonSpace );
if( expPos == 0 ) {

