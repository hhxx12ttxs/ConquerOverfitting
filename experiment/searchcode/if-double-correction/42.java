final double weight = source.getWeight() * multiplier;

return new Iterable<Correction>() {
@Override
public CorrectionIterator(Correction correction, double weight) {
this.phrase = correction.getPhrase();
this.weight = weight;

