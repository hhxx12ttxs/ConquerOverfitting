this.noOfSuccessesToEraseFailure = noOfSuccessesToEraseFailure;
this.seedGenerator = random;
this.seeds = new LinkedList<ErroneousSeed>();
}

public long getNextSeed() {
if (seedIterator.hasNext()) {
return seedIterator.next().seed;
} else {

