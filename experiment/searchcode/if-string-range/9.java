public class BeaconHolder {
private String macAddress;
private double minRange;
private double maxRange;

public BeaconHolder(String macAddress, double range){
this.macAddress=macAddress;
minRange = range;
}

public void compute(double range){

