@Override
public void putData(SemanticSet source, float[] dest, int offset, int initialIndex, int n) {
for(int i = 0; i < n; i++) {
if(vertices[initialIndex + i][2] == -1) {

