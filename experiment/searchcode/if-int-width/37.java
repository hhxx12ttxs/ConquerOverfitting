public void setTileId(int x, int y, int id) {
if (x < 0 || x >= width || y < 0 || y >= height) return;

tiles[width * y + x] = id;
}

public int getTileId(int x, int y) {
if (x < 0 || x >= width || y < 0 || y >= height) return -1;

