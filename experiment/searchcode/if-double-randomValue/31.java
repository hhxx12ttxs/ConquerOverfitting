Random randObj = new Random();
double randomValue = randObj.nextDouble();

// This is the mathematical modle we use
double value = Math.round(Math.sqrt(Math.pow(randomValue, 2.4)) * randomValue) % 7.0 * randomValue * 1000.0;

