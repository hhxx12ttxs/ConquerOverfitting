public void put(int key, String name) {

MapItem item = new MapItem(key,name);

if (firstItem == null) firstItem = item;
else recRemove(key, firstItem);
}
}

private void recRemove(int key, MapItem item)
{
if (item.getNextItem() != null &amp;&amp; item.getNextItem().getKey() == key) item.setNextItem(item.getNextItem().getNextItem());

