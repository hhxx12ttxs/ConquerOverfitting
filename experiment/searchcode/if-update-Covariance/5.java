* Check : empty Covariance Set .
*/
Set<Covariance> set = covarianceSet.getCovarianceSet();
if (set == null || set.isEmpty()) {
covarianceSet = covarianceSetManager.saveOrUpdate(covarianceSet,
true);
covarianceSetManager.commit();

