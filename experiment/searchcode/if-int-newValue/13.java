String current_count = String.valueOf(tap_count);

return current_count;
}

public void changeCount(int newValue)
{
tap_count = newValue;
changeCount(newValue);
}

public void decreaseCount()
{
int newValue = tap_count;

if(tap_count > 0)
{
newValue = tap_count - 1;

