private final ResourceType type;
private final int maxAmount;
private int currAmount;

public ResourcePatch(ResourceType type, int amount) {
return currAmount;
}

@Override
public int harvest(int amount) {
int harvested;
if(currAmount > amount) {

