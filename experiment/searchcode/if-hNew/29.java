package main.model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Container for the player's ship
 * @author Drew Galbraith
 * @version 1.0
 */
public class Ship implements Serializable {

    /**
     * maximum Cargo
     */
    private int maxCargo;
    /**
     * name of ship
     */
    private String name;
    /**
     * hull Strength
     */
    private HullStrength strength;
    /**
     * number of sheilds
     */
    private int shieldSlots;
    /**
     * number of gadgets
     */
    private int gadgetSlots;
    /**
     * number of weapon slots
     */
    private int weaponSlots;
    /**
     * number of crew spots
     */
    private int crewQuarters;
    /**
     * initial fuel
     */
    private int fuel = 1000;
    /**
     * parsec
     */
    private int parsec;
    /**
     * initial health
     */
    private int health = 100;
    /**
     * maximum health
     */
    private int maxHealth = 100;
    /**
     * cost of the ship
     */
    private int cost;

    /**
     * The strength of the hull
     */
    private enum HullStrength {WEAK, MEDIUM, STRONG}

    /**
     * The cargo of the ship
     */
    private HashMap<TradeGood, Integer> cargo = new HashMap<>();

    public static final HashMap<String, Ship> SHIPS = new HashMap<>();
    static {
        //TODO Escape Pod?
        SHIPS.put("Flea", new Ship("Flea", 5, HullStrength.WEAK, 0, 0, 0, 20, 0, 10));
        SHIPS.put("Firefly", new Ship("Firefly", 20, HullStrength.WEAK, 1, 1, 1, 17, 0, 20));
        SHIPS.put("Mosquito", new Ship("Mosquito", 15, HullStrength.MEDIUM, 2, 1, 1, 13, 0, 30));
        SHIPS.put("Bumblebee", new Ship("Bumblebee", 20, HullStrength.WEAK, 1, 2, 2, 15, 0, 40));
        SHIPS.put("Beetle", new Ship("Beetle", 50, HullStrength.WEAK, 0, 1, 1, 14, 0, 50));
        SHIPS.put("Hornet", new Ship("Hornet", 20, HullStrength.MEDIUM, 3, 2, 1, 16, 2, 60));
        SHIPS.put("GrassHopper", new Ship("Grasshopper", 30, HullStrength.MEDIUM, 2, 2, 3, 15, 3, 70));
        SHIPS.put("Termite", new Ship("Termite", 60, HullStrength.STRONG, 1, 3, 2, 13, 3, 80));
        SHIPS.put("Wasp", new Ship("Wasp", 35, HullStrength.STRONG, 3, 2, 2, 14, 3, 90));
    }

    /**
     * Create the default ship.
     */
    public Ship() {
        this("Gnat", 10, HullStrength.WEAK, 1, 0, 4, 100, 0, 20);
    }

    /**
     * Create a new ship.
     * @param maxCargo the maximum amount of cargo the ship can hold.
     * @param name the name of the ship.
     */
    public Ship(String name, int maxCargo, HullStrength strength, int weaponSlots,
                int shieldSlots, int gadgetSlots, int parsec, int crewQuarters, int cost) {
        this.maxCargo = maxCargo;
        this.name = name;
        this.strength = strength;
        this.weaponSlots = weaponSlots;
        this.shieldSlots = shieldSlots;
        this.gadgetSlots = gadgetSlots;
        this.parsec = parsec;
        this.crewQuarters = crewQuarters;
        this.cost = cost;
    }

    /**
     * Checks if the ship has space for the goods from a transaction.
     * @param count the amount of goods in the transaction.
     * @return whether or not there is enough room for the goods.
     */
    public boolean hasCargoSpace(int count) {
        if (count <= 0) {
            return true;
        }
        int total = 0;
        for (Integer i : cargo.values()) {
            total += i;
        }
        return total + count < maxCargo;
    }

    /**
     * Updates the cargo hold with the purchased or sold goods.
     * @param good the good that is being purchased or sold.
     * @param amt the amount of good being purchased or sold.
     */
    public void goodTransaction(TradeGood good, int amt) {
        int current = 0;

        if (!hasCargoSpace(amt)) {
            throw new IllegalArgumentException("Can't add that many trade goods");
        } else if (cargo.containsKey(good)) {
            current = cargo.get(good);
        }

        if (current + amt < 0) {
            throw new IllegalArgumentException("Can't remove that many goods");
        }

        cargo.put(good, current + amt);
    }

    /**
     * Gets the amount of one type of goods within the cargo.
     * @param good the type of good whose amount is being checked.
     * @return the amount of goods.
     */
    public int getAmountOfGoods(TradeGood good) {
        return cargo.get(good);
    }

    /**
     * Gets the goods held within the cargo.
     * @return A map of the goods within the cargo.
     */
    public HashMap<TradeGood, Integer> getCargo() {
        return cargo;
    }

    /**
     * Gets the fuel level of the ship.
     * @return the fuel level of the ship.
     */
    public int getFuel() {
        return fuel;
    }

    /**
     * Sets the ship's fuel level.
     * @param fuelInt the fuel level of the ship.
     */
    public void setFuel(int fuelInt) {
        this.fuel = fuelInt;
    }

    /**
     * Gets the health of the ship.
     * @return the health of the ship.
     */
    public int getHealth()
    {
        return health;
    }

    /**
     * Sets the health of the ship.
     * @param changeInHealth the amount the health will be changed by.
     */
    public void setHealth(int changeInHealth)
    {
        health += changeInHealth;

        if (health <= 0)
        {
            health = 0;
        }
        else if (health >= maxHealth)
        {
            health = maxHealth;
        }
    }

    /**
     * Gets the cost of the ship.
     * @return the cost of the ship.
     */
    public int getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getGadgetSlots()
    {
        return gadgetSlots;
    }

    public void setCargoSize(int s)
    {
        maxCargo = s;
    }

    public int getMaxCargo()
    {
        return maxCargo;
    }

    public void setMaxHealth(int hNew)
    {
        maxHealth += hNew;
    }

    public int getMaxHealth()
    {
        return maxHealth;
    }

    public void changeGadgetSlots(int g)
    {
        gadgetSlots += g;
    }
}

