double offset = Double.valueOf(comps[1]);
Double f0 = Double.valueOf(comps[2]);
if (f0 == null){
throw new IllegalArgumentException(&quot;Couldn&#39;t parse NemaNote from String: &quot; + noteString);
public boolean isEqualOnsetOffset(NemaNote otherNote,double onsetThreshold, double  offsetThreshold, double f0ThresholdLower, double f0ThresholdHigher){

if (!(Math.abs(this.onset-otherNote.getF0()) < onsetThreshold) )

