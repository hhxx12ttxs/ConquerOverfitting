continue;
}
}

double kmDistance = result.getLocation().arcDistance(result2.getLocation());
if (kmDistance <= maxKmDistance) {
newResult.getAddressComponents().setFormattedAddress(locale.getLanguage(), locale.getCountry());
newResult.setPrecision(Precision.APPROXIMATE);

if (index > geocoding.getResults().size() - 1) {

