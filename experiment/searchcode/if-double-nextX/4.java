Double currentX = 0.0;
Double nextX = start;
Integer counter = 1;
//printValue(counter, nextX, value, Math.abs(nextX - currentX));
} while (stopConditions.areNotSatisfied(Math.abs(nextX - currentX), Math.abs(value), counter));
return nextX;
}

public Double steffensen(Function<Double, Double> function, Double start, StopConditions stopConditions, BiConsumer<Double, Double> callback) {

