int startIndex = (int) ((exclusion ? board.getExclusionKey() : board.getKey()) >>> (64 - sizeBits));
int replaceIndex = startIndex;
int replaceImportance = Integer.MAX_VALUE; // A higher value, so the first entry will be the default
} else if (keys[i] == key2) { // Replace the same position
replaceIndex = i;
if (bestMove == Move.NONE) { // Keep previous best move

