package wingkwai.core;

import wingkwai.game.MainMenu;


public class Player
{
	private Vehicle vehicle;
	private Item item, usedItem;
	
	private long startTime = 0;
	private boolean isUseItem = false;
	
	private int lab = 1;
	private int rank = 1;
	
	private int currentCheckpoint = 0;
	private int checkpointCount = 0;
	
	private boolean isEnd = false;
	private boolean isAI = false;
	
	public Player(Vehicle vehicle)
	{
		this.vehicle = vehicle;
	}
	
	public Player instantiate()
	{
		Vehicle clone = vehicle.instantiate();
		Player pClone = new Player(clone);

		if(hasItem())
		{
			pClone.item = item;
		}
		
		return pClone;
	}
	
	public void update()
	{
		vehicle.update();
		
		if(isUseItem)
		{
			long currentMillis = System.currentTimeMillis() - startTime;
			
			if(currentMillis >= usedItem.getTime() * 1000)
			{
				disposeUsedItem();
			}
		}
	}
	
	private void disposeUsedItem()
	{
		usedItem.getOnItemEventListener().onEffectEnd(this);
		usedItem = null;
		isUseItem = false;
		
		if(isAI)
		{
			vehicle.setVelocityMultiplier(1 + ((rank-1) * RANK_SPEED) + ((rank-1) * AI_HACKINGSPEED));
		}
		else
		{
			vehicle.setVelocityMultiplier(1 + ((rank-1) * RANK_SPEED));
		}
		
		vehicle.setAngularVelocityMultiplier(1);
	}

	public Vehicle getVehicle()
	{
		return vehicle;
	}
	
	public int getRank()
	{
		return rank;
	}
	
	public void setRank(int rank)
	{
		int oldRank = this.rank;
		this.rank = rank;
		
		int dist = rank - oldRank;
		float value = dist * RANK_SPEED;
		if(isAI)
		{
			value += ((rank-1) * AI_HACKINGSPEED);
//			
//			if(MainMenu.profile.getMatch() > 0)
//			{
//				value += (((float)MainMenu.profile.getWin()/MainMenu.profile.getMatch()) * 0.05f);
//			}
		}
		vehicle.setVelocityMultiplier(vehicle.getVelocityMultiplier() + value);
	}
	
	public int getLab()
	{
		return lab;
	}
	
	public void setLab(int lab)
	{
		this.lab = lab;
	}
	
	public Item getItem()
	{
		return item;
	}
	
	public void setItem(Item item)
	{
		this.item = item;
	}
	
	public boolean hasItem()
	{
		return item != null;
	}
	
	public void useItem()
	{
		if(hasItem())
		{
			if(isUseItem)
			{
				disposeUsedItem();
			}
			
			isUseItem = true;
			startTime = System.currentTimeMillis();
			
			vehicle.setVelocityMultiplier(item.getSpeed());
			vehicle.setAngularVelocityMultiplier(item.getControl());
			
			usedItem = item;
			usedItem.getOnItemEventListener().onEffectStart(this);
			
			item = null;
		}
	}
	
	public int getCheckpointCount()
	{
		return checkpointCount;
	}
	
	public int getCurrentCheckpoint()
	{
		return currentCheckpoint;
	}
	
	public void setCurrentCheckpoint(int currentCheckpoint)
	{
		this.currentCheckpoint = currentCheckpoint;
	}
	
	public void setCheckpointCount(int checkpointCount)
	{
		this.checkpointCount = checkpointCount;
	}
	
	public boolean isEnd()
	{
		return isEnd;
	}
	
	public void setEnd(boolean isEnd)
	{
		this.isEnd = isEnd;
	}
	
	public void setAI(boolean isAI)
	{
		this.isAI = isAI;
	}
	
	public boolean isAI()
	{
		return isAI;
	}
	
	private static float RANK_SPEED = 0.0175f;
	private static float AI_HACKINGSPEED = 0.00475f;
}

