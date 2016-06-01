public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i1, float f1, float f2, float f3)
{
if (world.isRemote)
{
return true;
}

if (player.isSneaking())
{
APIUtils.openFilteringGUI(player, world, x, y, z);

