// mWorld.setBlock(x1, y, z1, 98);
// }
// }
// });
// --------------------------------------------------------------------------

public static void generate(int x0, int z0, int radius,
StructureCallback callback) {
while (x >= z) {
generateBlocks(x0, z0, x, z, callback);
if (x != z) {
generateBlocks(x0, z0, z, x, callback);

