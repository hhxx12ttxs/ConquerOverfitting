public ItemArray()
{

}

public Item addItem(String itemRef, int iValue)
{
Item item = new Item(itemRef, 0, iValue);
public Item getItem(String itemRef, int data)
{
for (int i = 0; i < this.size(); i++)
{
if (this.get(i).ItemRefData().equals(Item.makeItemRef(itemRef, data)))

