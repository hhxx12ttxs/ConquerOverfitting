package me.hretsam.gills.objects;

import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Hretsam
 */
public class GillsItem {

    private int id;
    private Byte data;
    private int amount;
    private boolean consume;

    /**
     * Converts a string to an itemid,
     * if there is a data value it gets seperated
     * @param blockString
     * @param amount
     * @throws NumberFormatException 
     */
    public GillsItem(String blockString, int amount, boolean consume) throws NumberFormatException {
        if (blockString.contains("-")) {
            decodeString(blockString, consume);
        } else {
            this.id = Integer.parseInt(blockString);
            this.data = new Byte("0");
            this.consume = consume;
        }
        this.amount = amount;
    }

    private void decodeString(String blockString, boolean consume) throws NumberFormatException {
        String[] split = blockString.split("-");
        this.id = Integer.parseInt(split[0]);
        this.consume = consume;
        if (split.length == 2) {
            this.data = Byte.parseByte(String.valueOf(Integer.parseInt(split[1]) % 16));
        } else {
            this.data = new Byte("0");
        }
    }

    /**
     * Converts a string to an itemid,
     * if there is a data value it gets seperated
     * @param blockString
     * @throws NumberFormatException 
     */
    public GillsItem(String blockString, boolean consume) throws NumberFormatException {
        if (blockString.contains("-")) {
            decodeString(blockString, consume);
        } else {
            this.id = Integer.parseInt(blockString);
            this.data = new Byte("0");
            this.consume = consume;
        }
        this.amount = 1;
    }

    public GillsItem(int id, Byte data, int amount) {
        this.id = id;
        this.data = data;
        this.amount = amount;
    }

    public GillsItem(int id, Byte data) {
        this.id = id;
        this.data = data;
        amount = 1;
    }

    public int getAmount() {
        return amount;
    }

    public Byte getData() {
        return data;
    }

    public int getId() {
        return id;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void addAmount() {
        amount++;
    }

    public void setData(Byte data) {
        this.data = data;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setConsume(boolean consume) {
        this.consume = consume;
    }

    public boolean doConsume() {
        return consume;
    }

    /**
     * Turns the item into an itemstack
     * @return 
     */
    public ItemStack toItemStack() {
        return new ItemStack(id, amount, (short) 0.00, data);
    }

    /**
     * Compares if both objects are the same items 
     * @param item
     * @return 
     */
    public boolean compare(GillsItem item) {
        if (item.getId() == getId() && item.doConsume() == doConsume()) {
            if (getId() < 256 || getId() == 351) {
                return (item.getData() == getData());
            } else {
                return true;
            }
        }
        return false;

    }

    @Override
    public String toString() {
        return "GI [" + id + ":" + data + " x " + amount + "]";
    }
}

