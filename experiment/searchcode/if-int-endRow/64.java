res[startRow][i] = num++;
}
for(int i = startRow + 1; i < endRow; i++){
res[i][endCol] = num++;
}
if(endRow != startRow){
for(int i = endCol; i >= startCol; i--){
res[endRow][i] = num++;
}
}
if(endCol != startCol){

