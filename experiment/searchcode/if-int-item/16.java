public void addItem(Item item)
{
addItem(item, 1);
}

public void addItem(Item item, int quantity)
{
if(item == null)
return;

for(int i=0; i<items.size(); i++)
{
if(items.get(i).item.name.equals(item.name))

