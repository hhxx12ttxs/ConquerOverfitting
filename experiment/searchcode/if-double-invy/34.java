Item item=level.getPlayer().getItems().get(i);
if((i==selectedBox-1))
{
if(!(item instanceof InvyItemBlank))
{
Player.setHoldItem(item,i);
Item item = level.getPlayer().getItem(mouseItemPosition);
if(item instanceof InvyItemBlank)
{
mouseHas=false;
mouseItemPosition=0;

