public int color;
public int invX;
public int invY;

public boolean init;

public ItemStack[] items;
/* INVENTORY START */
@Override
public int getSizeInventory()
{
return invX * invY;
}

@Override

