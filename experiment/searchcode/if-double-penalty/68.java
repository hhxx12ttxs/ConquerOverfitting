private double startTime = 0;
private double endTime = 0;
private FacilityPenalty facilityPenalty = null;
public double getPenalty() {

double penaltyFactor = 0.0;
if (this.facilityPenalty != null) {
this.facilityPenalty.finish(); // is this still needed? we have a call in EventsToFacilityLoad

