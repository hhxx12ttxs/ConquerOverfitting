this.conveyorDirection = direction;
}

@Override
public void paint(ScalableGraphics g) {
if (conveyorDirection == ConveyorDirection.RIGHT) {
state++;
if (state > 3) {
state = 0;
}
} else {

