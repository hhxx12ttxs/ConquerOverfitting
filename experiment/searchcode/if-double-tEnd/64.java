public Task constructInstance(Person person) {
return new TendGreenhouse(person);
}

@Override
public double getProbability(Person person) {

double result = 0D;

if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

