for (int j = 0; j < grid[0].length; j++) {
if (grid[i][j]) {
int blockRow = posY + i;
int blockCol = posX + j;
if (blockRow >= gameWorld.blocks.length - 1) {

