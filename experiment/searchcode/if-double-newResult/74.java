final HashMap<String, Double> scores = new HashMap<String, Double>();

for (SearchResult result : results.getData()) {
if (!scorer.isSignificant(result.getQuery())) {
newResult.setQuery(result.getQuery());
newResult.setDiscoveries(discoveries);
newResults.add(newResult);
double score = scorer.score(result);

