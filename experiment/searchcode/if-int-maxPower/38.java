public int getCapacity() {
return maxPower;
}

public void addPower(int power) {
if ((this.power += power) <= maxPower)
public boolean canReceivePower(int power) {
if ((this.power += power) <= maxPower)
return true;
else
return false;
}
}

