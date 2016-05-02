package roborally.entity.item;

import roborally.entity.Entity;
import roborally.position.Board;
import roborally.position.Position;
import roborally.value.WeightAmount;
import be.kuleuven.cs.som.annotate.Basic;

/**
 * A class representing items that can be picked up by a robot and have a certain weight.
 * 
 * @invar		The weight of an item must be valid.
 * 				| isValidWeight(weight)
 * 
 * @version     1.0
 * @author     	Niels De Bock, Michael Vincken (Bachelor of Computer Science)
 */
public abstract class Item extends Entity implements Comparable<Item>{
	/**
	 * Initializes this new item with a given amount of weight.
	 * 
	 * @param 	weight
	 * 			The weight for this new battery.
	 * @pre		This item must be able to have the given weight as weight.
	 * 			| isValidWeight(weight)
	 */
	protected Item(WeightAmount weight){
		assert(isValidWeight(weight));
		setWeight(weight);
	}
	
	/**
	 * @return	the weight of this item.
	 * 			| result == weight
	 */
	@Basic
	public WeightAmount getWeight(){
		return weight;
	}

	/**
	 * Sets the weight of this item to the given weight.
	 * @param 	weight     
	 * 			The weight to set the weight of this item to.
	 * @post    If the given weight is a valid weight, the new weight of this item is equal to the given weight.  
	 * 			| if(isValidWeight(weight)) then  
	 * 			|	new.getWeight() == weight
	 * @post	else this item's weight is set to 0g.
	 * 			| if(!isValidWeight(weight)) then  
	 * 			|	new.getWeight().equals(new WeightAmount(0))
	 */
	protected void setWeight(WeightAmount weight){
		if(isValidWeight(weight))
			this.weight = weight;
		else
			this.weight = new WeightAmount(0);
	}
	
	/**
	 * @param	weight
	 * 			The weight to check.
	 * @return 	| result == (weight != null)
	 */
	public static boolean isValidWeight(WeightAmount weight){
		return (weight != null);
	}
	
	/**
	 * a variable registering the weight of this item.
	 */
	private WeightAmount weight;

	/**
	 * Checks whether this item can be put on the given board and given position.
	 * 
	 * @param 	board
	 * 			The board to put the item on.
	 * @param 	position
	 * 			The position to put the item on.
	 * @return	true if and only if this item can be put on the given board and given position.
	 *			| result == (board.canHaveAsEntity(this) && 
	 *			|	(board.isInsideDimensions(position) && canHaveAsBoard(board))
	 *			|	 	&& (board.getNbWallsOnPosition(position) == 0 )
	 */			
	@Override
	public boolean canBePutOn(Board board, Position position){
		if(canHaveAsBoard(board) && canHaveAsPosition(position) && board.canHaveAsEntity(this) && (board.isInsideDimensions(position))){
			return (board.getNbWallsOnPosition(position) == 0);
		}
		return false;
	}

	@Override
	public int compareTo(Item other) {
		if(other == null)
			throw new ClassCastException("Non-effective item");
		if(other instanceof Battery)
			return new Integer(this.getWeight().getNumeral()).compareTo(new Integer(other.getWeight().getNumeral()));
		return 0;
	}
	
	/**
	 * @effect	| super.terminate()
	 * @post	This entity is terminated
	 * 			| new.isTerminated()
	 */
	@Override
	public void terminate(){
		super.terminate();
	}
	
}

