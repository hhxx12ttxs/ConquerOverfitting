for(int i=0; i<nRow; i++) {
int j = 0;
if( board[i][j] == &#39;O&#39; ) {
queue.add(nCol*i+j);
isVisited[i][j] = true;
isVisited[i][j] = true;
}
}

for(int j=0; j<nCol; j++) {
int i = 0;
if( board[i][j] == &#39;O&#39; ) {
queue.add(nCol*i+j);

