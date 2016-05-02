package com.entropic.server.item;

/**
 * Represents a physical item in the game. This can range from gear, a
 * consumable, a pet reagent, a quest item, a pet egg, or a vendor object.
 * 
 * @author Rob
 * 
 */
public interface Item extends Cloneable
{

	public static final short STACK_SIZE = 10;

	/**
	 * The possible item types.
	 */
	public static enum ITEM_CATEGORY
	{
		NONE, CLOTHING, CLOTHING_CHEST, CLOTHING_LEGS, CLOTHING_HEAD, CLOTHING_HANDS, TOOLS, TOOLS_MINING, TOOLS_GATHERING, TOOLS_TRAPPING, TOOLS_FARMING, TOOLS_COOKING, RAW_MATERIALS, RM_METAL, RM_WOOD, RM_PLANTS, RM_SEEDS, CONSUMABLES, CONSUMABLES_MINING, CONSUMABLES_GATHERING, TRAINING_BOOK, VENDOR
	};

	/**
	 * Returns the name of the object.
	 * 
	 * @return the name of the object
	 */
	public String getName();

	/**
	 * Returns the creation time of the object. Used to ensure that a single
	 * item isn't getting duplicated over and over, although it is possible that
	 * several items have the same exact timestamp.
	 * 
	 * @return the creation time of the item
	 */
	public long getCreationTime();

	/**
	 * Returns the list of attributes tied to the item. This can be empty if the
	 * item is a vendor item
	 * 
	 * @return an array of attributes that are associated with this item
	 */
	public ItemAttribute[] getAttributes();

	/**
	 * Returns the default sell price of this item, before any price
	 * modifications.
	 * 
	 * The sell price is the price that the VENDOR WOULD PAY TO THE USER to
	 * exchange this item.
	 * 
	 * @return the default sell price of this item
	 */
	public int getDefaultVendorSellPrice();

	/**
	 * Returns the default buy price of this item, before any price
	 * modifications.
	 * 
	 * The buy price is the price that the PLAYER WOULD PAY TO THE VENDOR to
	 * exchange this item.
	 * 
	 * @return the default buy price of this item
	 */
	public int getDefaultVendorBuyPrice();

	/**
	 * Returns the sprite information of the item as shown in the inventory.
	 * 
	 * @return the sprite information of the item as shown in the inventory
	 */
	public String getInventorySprite();

	/**
	 * Returns the local ID of the item, which is unique to the inventory.
	 * 
	 * @return the local ID of the item
	 */
	public short getLocalId();

	/**
	 * Returns the inventory that the item belongs to.
	 * 
	 * @return the inventory that the item belongs to
	 */
	public BaseInventory getInventory();

	/**
	 * Sets the local ID of the item, which is unique to the inventory.
	 * 
	 * @param id
	 *            the local ID of the item
	 */
	public void setLocalId(short id);

	/**
	 * Returns the inventory that the item belongs to.
	 * 
	 * @return the inventory that the item belongs to
	 */
	public void setInventory(BaseInventory inventory);

	/**
	 * Returns the HTML as it will be displayed on the client.
	 * 
	 * @return the HTML for this item as it will be displayed on the client
	 */
	public String getInfoHtml();

	/**
	 * Returns the flag that indicates if a player owner has already received
	 * 
	 * @return
	 */
	public boolean isCachedByOwner();

	/**
	 * Sets the flag to indicate that the item has been cached by the client.
	 * 
	 * @param cached
	 *            true if the item has already been cached by the owner's client
	 */
	public void setCachedByOwner(boolean cached);

	/**
	 * Returns the item type for this item.
	 * 
	 * @return the type of item it is corresponding to the ITEM_TYPES enum
	 */
	public ITEM_CATEGORY getItemType();

	/**
	 * Retrieves the quantity of this item. If the item is stackable, but not
	 * stacked, the quantity is 1. If the item is NOT stackable, the quantity is
	 * 0.
	 * 
	 * @return the quantity of the item
	 */
	public short getQuantity();

	/**
	 * Sets the quantity of the item, if it is stackable.
	 * 
	 * @param quantity
	 *            the quantity of the item, if it is stackable
	 * @return true if the quantity was within acceptable bounds
	 */
	public boolean setQuantity(short quantity);

	/**
	 * Adds a quantity. Can be negative. Ensures that the amount is within
	 * acceptable bounds.
	 * 
	 * @param amount
	 *            the amount to add, (negative to remove)
	 * @return true if the new amount is within bounds
	 */
	public boolean addQuantity(short amount);

	/**
	 * Returns true if the item is stackable.
	 * 
	 * @return true if the item is stackable
	 */
	public boolean isStackable();

	public Item cloneItem();

	/**
	 * Returns the global item ID. This is NOT the pds ID, rather the item
	 * class.
	 * 
	 * @return the global item ID.
	 */
	public Integer getGlobalItemId();

	/**
	 * Sets the global item ID of the item.
	 * 
	 * @param globalId
	 *            the global item ID
	 */
	public void setGlobalItemId(int globalId);

	/**
	 * Sets the type of item this is.
	 * 
	 * @param cat
	 */
	public void setItemType(ITEM_CATEGORY cat);
}

