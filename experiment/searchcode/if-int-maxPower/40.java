public float rotation = 0;

public int maxPower = 1000;
public float power = 0;

public float powerPerRotation = 0.1F;

public void updateEntity() {
if(this.getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord) > 6) {
rotation++;
}
power+=powerPerRotation;
if(power > maxPower) power = maxPower;
}

}

