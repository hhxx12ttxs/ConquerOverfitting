for (int z = 0; z < EndZ; z++)     {
if (world.getBlockId(StartX + x, StartY + y, StartZ + z) != Blockids[i][0] || world.getBlockMetadata(StartX + x, StartY + y, StartZ + z) != Blockids[i][1])   {
for (int y = 0; y < sizeY; y++)     {
for (int z = 0; z < sizeZ; z++)     {
if (canIMakeMB(world, Blockids, StartX - x, StartY - y, StartZ - z, sizeX, sizeY, sizeZ))    {

