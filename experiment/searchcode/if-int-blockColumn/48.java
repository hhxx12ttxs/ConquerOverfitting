private void update(Block [] blocks, boolean filled) {
for (Block b: blocks) {
int blockRow = b.getRow();
int blockColumn = b.getColumn();
private void updateView(int row, int column, boolean filled) {
if (filled)
gridPanel.updateGrid(row, column, getRandomColor(), filled);

