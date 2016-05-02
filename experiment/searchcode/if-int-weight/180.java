import java.io.*;
/**
 * An instance of Item simply holds an item, that can be used in the game
 *
 * @author Eva Vanessa Bolle 4528650 Gruppe 5b
 * @author Philipp 4569839 Gruppe 5b
 */
public class Item implements Secret<Item>, Serializable {

    /**
     * The Name
     */
    private String name;

    /**
     * The Value
     */
    private int value;

    /**
     * The Weight
     */
    private int weight;

    /**
     * Instantiates a new Item
     */
    public Item() {
        /*String[] names = {
            "A verry Random Item",
            "PseudoRandomItem",
            "fie7haeT",
            "eeR2aite",
            "one of %i Items",
            "B端rostuhl",
            "Stone Of Creativity"
        };

        this.name = names[(int) (Math.random() * (double) names.length)];
        this.value = (int) (Math.random() * 133.7);
        this.weight = (int) (Math.random() * 13.37);*/
        GenericLinkedList<Item> items = new GenericLinkedList<Item>();

        try {
            CSVHandler h = new CSVHandler("item.csv");
            h.getItems(items);
        } catch (FileNotFoundException e) {
            System.out.println("This is not the file you were looking for: " + e);
        } catch (IOException e) {
            System.out.println("Is it a plane, is it a bird no it is an IOException: " 
                    + e);
        }
        
        int r = (int) (Math.random() * items.length());
        Item i = items.getItem(r);
        this.name = i.getName();
        this.value = i.getValue();
        this.weight = i.getWeight();
    }

    /**
     * Instantiates a new Item
     *
     * @param name  the name
     * @param value the value
     * @param weight the weight
     */
    public Item(String name, int value, int weight) {
        this.name = name;
        this.value = value;
        this.weight = weight;
    }

    /**
     * method to clone object
     * @return cloned object
     */
    public Item clone() {
        return new Item(this.getName(), this.getValue(), this.getWeight());
    }
    
    /** @return the name */
    public String getName() { return this.name; }
    /** @param name the name */
    public void setName(String name) { this.name = name; }

    /** @return the value */
    public int getValue() { return this.value; }
    /** @param value the value */
    public void setValue(int value) { this.value = value; }

    /** @return the weight */
    public int getWeight() { return this.weight; }
    /** @param weight the weight */
    public void setWeight(int weight) { this.weight = weight; }

    /**
     * check for equality
     *
     * @param obj the object to compare to
     * @return equality
     */
    public boolean equals(Object obj) {
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        if (!((Item) obj).name.equals(this.name)) {
            return false;
        }
        if (((Item) obj).value != this.value) {
            return false;
        }
        if (((Item) obj).weight != this.weight) {
            return false;
        }

        // if none of these checks differs, they are probably equal
        return true;
    }

    /**
     * Stringify instance
     * @return "string" of instane
     */
    public String toString() {
        return String.format("%-20s - %3d EUR - %3d kg", this.name, this.value, this.weight);
    }

    /**
     * Stringify instance for shop
     * @return "string" with higher prices
     */
    public String toShopString() {
        return String.format("%-20s - %3d EUR - %3d kg", this.name, (int) (this.value * 1.1 + 1), this.weight);
    }
    
    /** 
     * compareTo method to compare two Items 
     *
     * @param itm the Object to compare to
     * @return how to sort these to Items
     */
    public int compareTo(Item itm) {
        final int EQUAL = 0;

        if (this == itm) { 
            return 0;
        }

        if (this.equals(itm)) {
            return 0;
        }

        if (this.name.compareTo(itm.name) != 0) {
            return this.name.compareTo(itm.name);
        }

        if (this.value != itm.value) {
            return this.value - itm.value;
        }

        if (this.weight != itm.weight) {
            return this.weight - itm.weight;
        }

        return 0;
    }

    /**
     * main method, mainly for testing
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        Item i1 = new Item("name", 100, 10);
        Item i2 = new Item("name", 100, 10);
        System.out.println(i1);
        System.out.println(i2);

        System.out.println("cmp i1, i2 : " + i1.compareTo(i2));

    
        // ## serializing test
        
        String itmfilename = "itm.save";

        // # writing

        Item itmtobesaved = new Item("cookies", 123, 456);

        System.out.println("itmtobesaved : " + itmtobesaved);

        try {
            FileOutputStream fs = new FileOutputStream(itmfilename);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(itmtobesaved);
            os.close();
            fs.close();
        } catch (IOException e) {
            System.out.println("IOException.. " + e);
        }

        System.out.println("written to file: " + itmfilename);

        // # reading

        Item thesavedone = null;

        try {
            FileInputStream fs = new FileInputStream(itmfilename);
            ObjectInputStream os = new ObjectInputStream(fs);
            thesavedone = (Item) os.readObject();
        } catch (ClassNotFoundException e) {
            thesavedone = new Item();
            System.out.println("class not found" + e);
        } catch (IOException e) {
            thesavedone = new Item();
            System.out.println("io exception" + e);
        }

        System.out.println("thesavedone  : " + thesavedone);

    }
}

