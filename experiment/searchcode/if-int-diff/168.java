package dk.itu.KF13.TheSim.Game.Model.Physical.Class;

import java.util.List;

import dk.itu.KF13.TheSim.Game.Model.Physical.Class.ObjBottle.BottleType;
import dk.itu.KF13.TheSim.Game.Model.Physical.Interface.GameObject;
import dk.itu.KF13.TheSim.Game.Model.Physical.Interface.Player;
import dk.itu.KF13.TheSim.Game.Model.World.Interface.Location;
import dk.itu.KF13.TheSim.Game.Model.World.Interface.Location.Direction;
import dk.itu.KF13.TheSim.Game.View.GameView;
/**
 * HumanPlayer is responsible for all actions the human player can do
 * @author Simon & Thelle
 *
 */
public class HumanPlayer implements Player {

	private Location myLocation;
	private Backpack myBackpack;
	private int alcoholLevel;
	private GameView view;
	
	public HumanPlayer(GameView gameView){
		myBackpack = new Backpack();
		alcoholLevel = 4;
		view = gameView;
	}
	
	/**
	 * addBeersToBackpack adds the given number of beers to the player's backpack.
	 * This method does not take the beers from the location and can be called whenever new
	 * beers should be added.
	 * @param numberOfBeers - the number of beers to be added
	 */
	public void addBeersToBackpack(int numberOfBeers){
		for ( int i = 0; i < numberOfBeers; i++){
			ObjBottle beer = new ObjBottle(true, BottleType.MASTERBREW);
			beer.putInBackpack(myBackpack);
		}
	}

	public void announceArrival(){
		myLocation.playerHasArrived();
	}

	public void changeAlcoholLevel(int diff) {
		alcoholLevel = alcoholLevel + diff;
	}

	public int getAlcoholLevel() {
		return alcoholLevel;
	}
	
	public Location getLocation() {
		return myLocation;
	}
	
	public int lookForSpecificItem(String descriptionOfItem){
		return myBackpack.numberOfSpecificItemsInBackpack(descriptionOfItem);
	}
	
	public void lookInBackpack(){
		List<GameObject> objectsInBackpack = this.returnContentOfBackpack();
		view.print("Objects in backpack:");
		for(int i = 0; i < objectsInBackpack.size();i++){
			view.print(objectsInBackpack.get(i).getDescription());
		}
	}
	public boolean move(Direction direction) {
		Location requestedLocation = myLocation.getExits(direction);
		if (requestedLocation == null){
			return false;
		}
		else{
			myLocation = requestedLocation;
			this.changeAlcoholLevel(-1);
			this.announceArrival();
			return true;
		}
	}

	/**
	 * removeBeersFromBackpack removes the given number of beers from the player's backpack.
	 * @param numberOfBeers - number of beers to be removed.
	 */
	public void removeBeersFromBackpack(int numberOfBeers){
		List<GameObject> objectsInBackpack = this.returnContentOfBackpack();
		int beersRemoved = 0;
		for(int i = 0; i < objectsInBackpack.size() && beersRemoved < numberOfBeers;i++){
			String description = objectsInBackpack.get(i).getDescription();
			if(description.equalsIgnoreCase("a masterbrew")){
				removeFromBackPack(objectsInBackpack.get(i));
				beersRemoved++;
				i--; //Subtracting one from i, because the size of the list 
					//is one less after deleting an element
			}
		}
	}
	
	public void removeFromBackPack(GameObject object) {
		myBackpack.removeFromBackpack(object);
	}
	
	public List<GameObject> returnContentOfBackpack() {
		return myBackpack.getContent();
	}

	public boolean setLocation(Location location) {
		myLocation = location;
		return true;
	}

	public boolean takeObject(GameObject object) {
		boolean status;
		status = object.putInBackpack(myBackpack);
		if (status){
			myLocation.removeObject(object);
			view.print("You have taken the object");
			return true;
		}else{
			view.print("There was no room in your backpack or you can't take the item");
			return false;
		}		
	}
	
	public boolean takeObject(String input){
		//Get objects from location
		Location playerLocation = getLocation();
		List<GameObject> objectsAtLocation = playerLocation.getObjects();
		
		for(int i = 0; i < objectsAtLocation.size();i++){
			//Search string is found
			String searchString = getSearchString(objectsAtLocation.get(i));
			//Searchstring is tested
			if(input.equalsIgnoreCase(searchString)){
				takeObject(objectsAtLocation.get(i));				
				return true;
			} 
		}
		view.print("No such item at this location");
		return false;

	}
	
	public void useObject(GameObject object){
		//The returned value is subtracted from the alcohol level because
		//the use method returns the price of the action
		int diff = -object.use();
		this.changeAlcoholLevel(diff);
		myBackpack.removeFromBackpack(object);
		view.print("You used "+ object.getDescription());
	}
	
	public void useObject(String input){
		List<GameObject> objectsInBackpack = returnContentOfBackpack();
		for(int i = 0; i < objectsInBackpack.size();i++){
			String objectName = objectsInBackpack.get(i).getDescription();
			objectName = objectName.replaceFirst("a ", "");
			if(input.equalsIgnoreCase("use " + objectName)){
				useObject(objectsInBackpack.get(i));
				return;
			}
		}
		view.print("No such item in backpack");
	}
	
	/**
	 * getSearchString takes the description of the object and returns
	 * the search  string that is used to compared to the player command.
	 * @param gameObject - the object that is to be changed
	 * @return Returns the string which is used to find the object
	 */
	private String getSearchString(GameObject gameObject){
		String objectDescription = gameObject.getDescription();
		objectDescription = objectDescription.replaceFirst("a ", "");
		objectDescription = "take "+ objectDescription;
		return objectDescription;
		
	}	
}

