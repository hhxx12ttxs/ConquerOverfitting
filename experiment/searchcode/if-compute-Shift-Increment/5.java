Calendar nextDateShift = Calendar.getInstance();
nextDateShift.setTime(nextExtremDate);
QuotationsFactories.getFactory().incrementDate(nextDateShift, -2*noticablePeriodBand);
QuotationsFactories.getFactory().incrementDate(nextDateShift, nbOpenIncrementBetween/2);
}

if (currentTime.compareTo(prevExtremDate) == 0) {

