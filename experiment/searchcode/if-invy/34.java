TileEntityChest chest = ((TileEntityChest) world.getTileEntity(x, y, z));

chest.direction = EntityUtil.get2dOrientation(entity);

if(stack.hasTagCompound() &amp;&amp; stack.getTagCompound().hasKey(&quot;size&quot;))
{
if(stack.getTagCompound().getTag(&quot;size&quot;) instanceof NBTTagIntArray)

