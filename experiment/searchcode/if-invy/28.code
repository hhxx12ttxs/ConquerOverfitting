public String invId;
/**
* used to determine what inventory it&#39;s referring to
*/
public int invX, invY;
Inventory inv = GuiRegistry.instance().getInventory(invId, logic.world, invX, invY).getInventoryObject();

if (inv == null) return;

inv.setSlot(slotToSwap, changedItem);
}

}

