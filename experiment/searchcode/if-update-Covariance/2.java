Covariance covariance = uuidCovariance.getCovariance();
if (covariance == null) {
throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
covarianceManager.beginTransaction();
covariance = covarianceManager.saveOrUpdate(uuidCovariance, true);

