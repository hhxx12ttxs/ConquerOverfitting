seen[b] = true;
}

for(int i = 0; i < seen.length; ++i) {
if(!seen[i]) {
int current = bfs.get(j);
if(!seen[current]) {
for(int k = 0; k < graph[current].length(); ++k) {

