List<ISearchResult> refResult, double optimalSimilarityValue) {
if(newResult.isEmpty()) {
return 0.0;
}
double intersectionCount = getIntersectionUriCount(newResult, refResult);
return 0.0;
}

double quality;
if (newResult < refResult) {
// +1 to avoid division by zero
quality = (((double)refResult - newResult) / (refResult + 1));

