int lineCount = 0;
int totalLines = 0;
long lastMillis = 0;
long thisMillis = 0;
// Handle the hashmap and stick them in the DB when one second of records has been parsed
if (!lastTime.equals(thisTime)) {

