* The top level algorithm
*/
public void setTiles(){
int searchLength = INITIAL_SEARCH_LENGTH;
while(searchLength >= MIN_LENGTH){
if(searchLength >= 2* MIN_LENGTH){
if(isDense)
searchLength = (int)(searchLength/1.414);

