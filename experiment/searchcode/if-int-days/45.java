CLEAN(2), INCOMPLETE(0), COMPLETE(0), REVIEWED(0), FINALIZED(2), ERROR(0);

private AssessmentExpirationDaysEnum(int expirationDays) {
this.expirationDays = expirationDays;
}

// assessment will expire if not attended for these many days. 0 means never expire

