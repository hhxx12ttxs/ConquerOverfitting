public int velocity;
public long startMillis;
public long endMillis;
public boolean on;

public Note(int tempC, int tempP, int tempV, long tempS, boolean tempOn) {
velocity = tempV;
startMillis = tempS;
endMillis = -1;
on = tempOn;
}

Note(int channel, int pitch, int velocity) {

