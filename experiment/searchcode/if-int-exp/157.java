package oberon.model.players.skills;

import oberon.event.Event;
import oberon.event.EventContainer;
import oberon.event.EventManager;
import oberon.model.items.Item;
import oberon.model.players.Client;
import oberon.util.Misc;
/**
 * Fishing.java
 *
 * @author Sanity
 *
 **/
 
public class Fishing {
	
	private Client c;
	
	private enum FishData {
        SHRIMP(317, 1, 10, 303, 621),//NET
        SARDINE(327, 5, 20, 313, 622),//BAIT
        HERRING(345, 10, 30, 313, 622),//BAIT
        ANCHOVIES(319, 15, 40, 303, 621),//NET
        TROUT(335, 20, 50, 314, 623),//FEATHER
        PIKE(349, 25, 60, 313, 622),//BAIT
        SALMON(331, 30, 70, 314, 623),//FEATHER
        TUNA(359, 35, 80, 311, 618),//HARPOON
        LOBSTER(377, 40, 90, 301, 619),//CAGE
        SWORDFISH(371, 50, 100, 311, 618),//HARPOON
        SHARK(383, 76, 110, 311, 618),//HARPOON
        LAVAEEL(2148, 99, 120, 313, 622);//BAIT

		private int fish, level, exp, item, anim;
		
		private FishData(int fish, int level, int exp, int item, int anim) {
		        this.fish = fish;
		        this.level = level;
		        this.exp = exp;
		        this.item = item;
		        this.anim = anim;
		}
		
		public int getFish() {
		        return fish;
		}
		
		public int getLevel() {
		        return level;
		}
		
		public int getExp() {
		        return exp;
		}
		
		public int getItem() {
		        return item;
		        }
		
		public int getAnim() {
		        return anim;
		}
	}
	
	public Fishing(Client c) {
		this.c = c;
	}
		       
	public FishData checkFish(int fish) {
		for (FishData f : FishData.values()) {
			if (f.getFish() == fish) {
				return f;
			}
		}
		return null;
	}
		
	public void setupFishing(FishData f) {
		if (c.playerLevel[c.playerFishing] >= f.getLevel()) {
			if (c.getItems().playerHasItem(f.getItem())) {
				c.fishing = true;
		        c.fishTimer = (int) System.currentTimeMillis();
		        startFishing(f);
		    } else {
		    	c.sendMessage("You do not have the proper items to fish here.");
		    }
		} else {
			c.sendMessage("You need a fishing level of at least "+f.getLevel()+" to fish here.");
		}
	}

	public void startFishing(final FishData f) {
		EventManager.getSingleton().addEvent(new Event() {
			@Override
	        public void execute(EventContainer container) {
				c.startAnimation(f.getAnim());
				if((int) System.currentTimeMillis() - c.fishTimer > fishingTimer()){
					c.sendMessage("You catch some "+Item.getItemName(f.getFish()).toLowerCase().replace("_", " ")+".");
			        c.getItems().addItem(f.getFish(), 1);
			        c.getPA().addSkillXP(f.getExp(), c.playerFishing);
			        c.fishTimer = (int) System.currentTimeMillis();
			        if (f.fish == 327 || f.fish == 339) {
			        	if(c.getItems().playerHasItem(313, 1)){
			            	c.getItems().deleteItem(313, 1);
			        	}
			        }
				}
	            
	            if (!c.getItems().playerHasItem(f.getItem()))
	            	container.stop();

	            if(!c.getItems().hasFreeSlots(1))
	            	container.stop();
	            
	            if(!c.fishing){
	            	c.startAnimation(65535);
	            	container.stop();
	            }
	            
	        }
		}, 1000);
        
    }
	
	private int fishingTimer(){
		return (int)((5000*Math.random()+1000) + (5000 - (int)Math.floor((c.playerLevel[c.playerFishing] * 1000) / 20)));
	}
}
