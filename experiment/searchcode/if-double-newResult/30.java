Cluster[] clusters = kMeans.calculate(kMeans.getCentroids(i, 0, 1), 0.0);
Result newResult = new Result(clusters, kMeans.calculateSumOfSquaredErrors(clusters));
Result newResult = new Result(clusters, kMeans.calculateSumOfSquaredErrors(clusters));

if (result == null || newResult.getSSE() < result.getSSE()) {

