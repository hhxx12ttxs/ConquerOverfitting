for ( int i = 0; i < 150; i++) {
double randomValue = 400 + random.nextInt(50) + random.nextDouble();
double randomValue = 900 + random.nextInt(100) + random.nextDouble();
graphite.send(topic, String.valueOf(randomValue), incrementingTime);

