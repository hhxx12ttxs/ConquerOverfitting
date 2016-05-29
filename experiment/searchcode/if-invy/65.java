if (hasFull()) { Bank.depositInventory(); Time.sleep(250, 1000); currentTask = &quot;Depositing Inventory&quot;; money += invyPrice; }
if (emptyInvy()) { Bank.withdraw(Vials.x.EMPTYVIAL.getId(), 0); Time.sleep(250, 1000); currentTask = &quot;Withdrawing Vials&quot;; }
if (Inventory.getCount(Vials.x.VIALOFWATER.getId()) == 28) { return true; }
return false;
}

public boolean emptyInvy() {
if (Inventory.getCount() == 0) { return true; }
return false;
}

}

