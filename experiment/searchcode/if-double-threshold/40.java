public class LessThanThresholdObserver extends ParkingLotObserver{
private ParkingLotObserver observer;
private double threshold;
public void update(int numberOFCarsParked, int capacity) {
if((double)numberOFCarsParked/capacity <= threshold)
observer.update(numberOFCarsParked, capacity);
}
}

