while (check < length) {
if (grid[startRow + check][startColumn - check] != value) {
while (check < length) {
if (grid[startRow + check][startColumn + check] != value) {
hit = false;

