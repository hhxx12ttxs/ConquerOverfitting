private Covariance delete(final byte[] uuid, final Covariance covariance) {
// Covariance deletedSet = null;
if (!transactionStarted) {
throw new ResourceException(Status.CONNECTOR_ERROR_CONNECTION,
&quot;Transaction has not been started.&quot;);
}
try {
if (covariance != null) {

