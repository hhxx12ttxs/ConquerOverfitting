return (this.nextIndex > 0) &amp;&amp; (this.nextIndex < this.listSize);
}

@Override
public int nextIndex() {
if (nextIndex < listSize)
do {
done = true;
for(int i = 0; i < listSize; i++) {
if (unprocessed[i]) {

