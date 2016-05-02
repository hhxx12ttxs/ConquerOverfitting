/**
 * This Class holds items found in the game
 * 
 * @author Aaron Ghent
 * @version 2007.10
 */

public class Item
{
    private String description;
    private String name;
    private int weight;
    private boolean moveable;
 
	 /**
	 * class constructor - init all infomation
	 */
    public Item(String Name, String Description, int Weight, boolean itemMoveable)
    {
        this.description = Description;
        this.name = Name;
        this.weight = Weight;
        this.moveable = itemMoveable;       
    }
 
 	 /**
	 *@return description on the item
	 */
    public String getDescription()
    {
        return description;
    }
    
  	 /**
	 *@return item name
	 */
    public String getName()
    {
        return name;
    }
    
     /**
	 *@return if the item if moveable return true
	 */
    public boolean isMoveable()
    {
        return moveable;
    }
    
  	 /**
	 *@return weight of the item
	 */
    public int getWeight()
    {
        return weight;
    }
}
