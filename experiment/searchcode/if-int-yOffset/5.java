int height = rand.nextInt(4) + 5;
for(int yOffset = 0; yOffset <= height; yOffset++)
{
setBlock(world, x, y + yOffset, z, ScapecraftBlocks.willowLog, 0, ScapecraftBlocks.willowTreeSpawn);
for(int xOffset = -2; xOffset <= 2; xOffset++)
{
for(int zOffset = -2; zOffset <= 2; zOffset++)
{
if(rand.nextInt(height - yOffset + 1) < 2 &amp;&amp; world.getBlock(x + xOffset, y + yOffset, z + zOffset).isReplaceable(world, x + xOffset, y + yOffset, z + zOffset))

