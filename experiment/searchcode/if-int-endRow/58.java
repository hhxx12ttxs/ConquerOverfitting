int startRow = Integer.parseInt(coordinates[0]);
int endRow = Integer.parseInt(coordinates[1]);
int endCol = Integer.parseInt(coordinates[2]);
int freeCol;

if (!isRowAvailable(parkingLot, endRow)) {

