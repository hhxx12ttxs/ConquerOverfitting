public class BeaconHolder {
private String macAddress;
private double minRange;
private double maxRange;

public BeaconHolder(String macAddress, double range){
public void compute(double range){
if(range < minRange){
minRange=range;
}else{
if(range>maxRange){

