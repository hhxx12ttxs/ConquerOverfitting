return new LangIterator(sources);
}
};
}

class LangIterator extends AbstractIterator<Correction> {

double maxWeight = 0;
protected Correction computeNext() {
while (source.hasNext()) {
Correction next = source.next();

if (shortCircuit &amp;&amp; next.getWeight() * norm < maxWeight) {

