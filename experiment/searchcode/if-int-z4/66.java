ArrayList<Zone> config = new ArrayList<Zone>();
if (numDogs == 1) {
config.add(Zone.addZones(Z1, Z2, Z3, Z4, Z5, Z6));
config.addAll(Arrays.asList(leftHalf, rightHalf));
} else if (numDogs == 3) {
Zone middle = Zone.addZones(Z3, Z4);

