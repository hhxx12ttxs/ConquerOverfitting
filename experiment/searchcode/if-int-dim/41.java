public VoxelGradient getGradient(int x, int y, int z) {
if (x <= 0 || x >= volume.getDimX() || y <= 0 || y >= volume.getDimY()
return data[x + dimX * (y + dimY * z)];
}


public void setGradient(int x, int y, int z, VoxelGradient value) {

