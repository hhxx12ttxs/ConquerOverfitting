int resultLimit = 0;
int resultOffset = 0;
int listSize = 0;

int int_offset = 0;
if (offset != null) {
int_offset = Integer.valueOf(offset);
}

if (pageSize == null) {
listSize = twitter.searchOperations().search(query).getTweets().size();

