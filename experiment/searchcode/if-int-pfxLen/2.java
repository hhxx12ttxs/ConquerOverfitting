int[] pi = new int[pattern.length()+1];
int nextChr, pfxLen;
pfxLen = 0; // Prefix we&#39;ve matched against ourselves so far
pfxLen = pi[pfxLen];

if (pattern.charAt(pfxLen) == pattern.charAt(nextChr))
pfxLen++;

