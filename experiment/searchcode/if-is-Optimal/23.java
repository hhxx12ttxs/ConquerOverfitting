candidates.add(ad);
}

//If no one compatible
if (candidates.isEmpty()) {
throw new NoVideoAvailableException();
else {
//Replace optimal result if current set is better
if (amountOfAd > optimalVideoSetAmount) {

