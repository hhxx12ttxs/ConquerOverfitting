simulator.setHasTraffic(false);
simulator.resetLoc();
drawCase();
}

@Override
public void drawCase() {
distance = (double)selectedDistance;
showResult(endData.status);
}

public void runAction() {
hideResult();
distance = (double)selectedDistance;
time = selectedTime;

