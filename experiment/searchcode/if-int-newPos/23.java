int y = pos.getY();
int z = pos.getZ();

BlockPos newPos = pos;
if (filter.apply(newPos)) return newPos;
newPos = new BlockPos(x, y, z);
if (filter.apply(newPos)) return newPos;
}
for (int stepZN = 0; stepZN < step; stepZN++) {

