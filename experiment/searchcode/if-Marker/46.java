public class Counter {

protected int marker;

public void setMarker(int marker) {
this.marker = marker;
public void increment(){
marker++;
}

public boolean comesBefore(Counter other){ //if caller obj is LESS; return true

