final BandShiftCorrection bandShiftCorrection = new BandShiftCorrection(context);

double[] rrs_corrected = bandShift(spectrum, qaaAt443, bandShiftCorrection);
if (isCorrected(rrs_corrected)) {
return bandShiftCorrection.weightedAverageEqualCorrectionProducts(rrs_corrected);

