public Individual() {}

public Individual(String type) {
if (&quot;allZeros&quot;.equals(type)){
for (int dim=0 ; dim < nbDimensions ; dim++) {
for(int count=0; count < length ; count++){ binaryString[dim][count] = 0; }
}
}
else if (&quot;allOnes&quot;.equals(type)){
for (int dim=0 ; dim < nbDimensions ; dim++) {

