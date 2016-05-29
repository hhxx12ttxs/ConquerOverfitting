XmlObject xb_obsDoc = null;

if (absObs instanceof SosMeasurement) {
xb_obsDoc = createMeasurement(absObs);
}
else if (absObs instanceof SosCategoryObservation) {
xb_obsDoc = createCategoryObservation(absObs);

