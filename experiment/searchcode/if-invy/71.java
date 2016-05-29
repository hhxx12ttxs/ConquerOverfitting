private GameTimer currentTimer;
private int turnTime;

public World(int farmX, int farmY, int invX, int invY, int turnTime){

playerFarm = new Farm(farmX, farmY);
playerInventory = new Inventory<Berry>(invX, invY);

