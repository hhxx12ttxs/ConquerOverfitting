protected PenaltyBO(final UserContext userContext, final String name, final PenaltyCategoryEntity categoryEntity,
final PenaltyPeriodEntity periodEntity, final Integer duration, final Double min, final Double max,
private void validateMinAndMax(final Double min, final Double max) throws PenaltyException {
if (min == null) {
throw new PenaltyException(PenaltyConstants.INVALID_PENALTY_MINIMUM);

