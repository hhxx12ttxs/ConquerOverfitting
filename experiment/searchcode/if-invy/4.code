@Override
public void render(SpriteBatch batch)
{
Item item = player.getItemInSlot(invX, invY);
if(item != null)
batch.setColor(1, 1, 1, 1);
item.render(batch, pos[0], pos[1], invY==0);
if(item.getMaxDurability() > 1)

