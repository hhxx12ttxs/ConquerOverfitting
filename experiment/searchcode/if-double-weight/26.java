package net.jnickg.dnd.inv;

import java.util.*;

import net.jnickg.dnd.inv.Item;


/** This class defines a Character's Inventory,
 * which contains Items as well as Money. ..
 * test change!!!
 */
public class Inventory {

/* Variable Declarations */
	private				double		maxWeight;
	private				double		weight;
	private				int			numItems;
	private				int			money;
	
	private			List<Item>			itemList	= new ArrayList<>();
	private	static	Map<String, Item>	stdGenItm	= new HashMap<>();
	
// Load item files and initialize all standard item libraries
// TODO Get real support for loading and reading a file.
	static {
		//NAME	NOTE	WEIGHT	MAXHP	HARDNESS
		Item item = new GeneralItem("NAME", "NOTE", 0.0, 1, 0);
		stdGenItm.put(item.getItemName(), item);
//		try {
//			BufferedReader br = new BufferedReader(new FileReader("stdItm.txt"));
//			try {
//				String line = br.readLine();
//				if (line.equals("NAME	NOTE	WEIGHT	MAXHP	HARDNESS") continu
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
	}
	
/* Constructors */
	public Inventory() {
		maxWeight = 0.0;
		weight = 0.0;
		numItems = 0;
		money = 0;
	}
	
	public Inventory(double maxWeight) {	// Null inventory
		this.maxWeight = maxWeight;
		numItems = 0;
		weight = 0;
		money = 0;
	}
	
	public Inventory(double maxWeight, int money) { // Used for an ECL1 character 
		this.maxWeight = maxWeight;
		numItems = 0;
		weight = 0;
		this.money = money;
	}
	
	// TODO Add constructor for copying an existing inventory
	
/* Printing Functions */
	// Returns a string with the inventory's weight and number of items
	public String toString() {
		StringBuilder invStr = new StringBuilder();
		invStr.append(String.format("Inventory: %.2f lbs, %d items\n", getWeight(), getNumItems()));
		return invStr.toString();
	}
	
	// Returns a String containing each item in the inventory
	public String itemString() {
		StringBuilder itemStr = new StringBuilder();
		if (itemList.isEmpty()) {
			itemStr.append("This inventory is empty.");
			return itemStr.toString();
		}
		else {
			for (Item i : itemList) {
				itemStr.append(i.infoString() + "\n");
			}
		}
		return itemStr.toString();
	}
	
/* Add Item functions */
	// Takes an instance of an item and attempts to add it to the main inventory.
	public boolean addItem(Item thisItem, int quantity) {
		// TODO Should this add as many as possible if sending over capacity, or none?
		
		if(weight + (thisItem.getItemWeight() * quantity) <= maxWeight) {	//if there is enough weight-room
			// Add the items
			for(int i = 0; i < quantity; i++) {
				itemList.add(thisItem);
			}
			// Update instance stats
			addWeight(thisItem.getItemWeight() * quantity);
			incNumItems(quantity);
			return true;
		}
		else {
			return false;
		}
	}
	
	// Creates and attempts to add a GeneralItem to the main inventory. 
	public boolean addGeneralItem(String thisName, String thisNote, Double thisWeight,
			Integer thisHPmax, Integer thisHardness,
			Integer quantity) {
		GeneralItem thisGItem = new GeneralItem(thisName, thisNote, thisWeight,
				thisHPmax, thisHardness);
		if (addItem(thisGItem, quantity)) return true;
		return false;
	}

	// Creates and attempts to add a Weapon to the main inventory. 
	public boolean addWeapon(String thisName, String thisNote, Double thisWeight,
			Integer thisHPmax, Integer thisHardness, String wpnDmg,
			Boolean ranged, Double wpnRange, String wpnDmgType,
			Integer quantity) {
		Weapon thisWeapon = new Weapon(thisName, thisNote, thisWeight,
				thisHPmax, thisHardness, wpnDmg, ranged, wpnRange,
				wpnDmgType);
		if (addItem(thisWeapon, quantity)) return true;
		return false;
	}
	
	// Creates and attempts to add an Armor to the main inventory. 
	public boolean addArmor(String thisName, String thisNote, Double thisWeight,
			Integer thisHPmax, Integer thisHardness, Integer bonusAC,
			Integer maxDEX, Integer penaltyACheck, String armorType,
			Integer quantity) {
		Armor thisArmor = new Armor(thisName, thisNote, thisWeight,
				thisHPmax, thisHardness, bonusAC, maxDEX, penaltyACheck,
				armorType);
		if (addItem(thisArmor, quantity)) return true;
		return false;
	}
	
	// Checks if there is an item of the given name and, if so, attempts
	// to add it to the main inventory
	public boolean addStdGenItem(String thisItem, int quantity) {
		if (isStdItem(thisItem, stdGenItm)) {
			if (addItem(getStdItem(thisItem, stdGenItm), quantity)) return true;
			else return false;
		}
		else return false;
	}
	
/* Find Item Methods */
	// Searches the main inventory for every instance of an item. Case sensitive.
	public List<Item> findItem(String thisItem) {
		List<Item> foundItems = new ArrayList<Item>();
		for(Item i: itemList) {
			if (i.getItemName().equals(thisItem)) foundItems.add(i);
		}
		return foundItems;
	}
	
/* Standard Item Methods */
	// Searches for an item, by name, in a standard library, and returns
	// whether it exists.
	public boolean isStdItem(String thisName, Map<String, Item> thisMap) {
		if (thisMap.containsKey(thisName)) return true;
		else return false;
	}
	
	// Searches for an item, by name, in a standard library, and returns it
	public Item getStdItem(String thisName, Map<String, Item> thisMap) {
		return thisMap.get(thisName);
	}
	
/* weight Functions */
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public double addWeight(double moreWeight) {
		weight += moreWeight;
		return weight;
	}
	
	public double rmvWeight(double lessWeight) {
		weight -= lessWeight;
		return weight;
	}
	
	// Resets weight to zero and adds the weights for all items
	public double calculateWeight() {
		weight = 0;
		if (itemList.isEmpty()) return weight;	//if there are no items return 0
		
		// Otherwise, iterate through all items in the list,
		// and add their weight times their quantity 
		for (Item i : itemList) {
			weight += (i.getItemWeight());
		}
		weight = Math.round(weight*10)/10;
		return weight;
	}
	
	// For use when a Character gains strength or bonus encumbrance
	public void setMaxWeight(double maxWeight) {
		this.maxWeight = maxWeight;
	}
	
	public double getMaxWeight() {
		return maxWeight;
	}

/* numItems Functions */
	public int getNumItems() {
		return numItems;
	}
	
	public void setNumItems(int numItems) {
		this.numItems = numItems;
	}	
	
	public int incNumItems(int newItems) {
		numItems += newItems;
		return numItems;
	}
	
	public int decNumItems(int fwrItems) {
		numItems -= fwrItems;
		return numItems;
	}
	
	// Resets the number of items to zero and adds the quantity for all items
	public int calculateNumItems() {
		numItems = 0;
		if (itemList.isEmpty()) return numItems;	//if there are no items just return 0
		return itemList.size();
	}

/* money Functions */
// TODO Take weight into account when adding coins.
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	public int addMoney(int amount) {
		money += amount;
		return money;
	}
	public int rmvMoney(int amount) {
		money -= amount;
		return money;
	}
}

