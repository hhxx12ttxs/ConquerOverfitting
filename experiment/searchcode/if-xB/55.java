private AbstractUncertaintyDocument encodeUncertainty(IUncertainty element) throws UnsupportedUncertaintyTypeException, UncertaintyEncoderException {
AbstractUncertaintyDocument doc = null;
if (element instanceof IDistribution) {
doc = encodeDistribution((IDistribution) element);
} else if (element instanceof IStatistic) {
doc = encodeStatistic((IStatistic) element);

