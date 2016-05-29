public int compare(SequenceWrapper o1, SequenceWrapper o2) {
int v;

if ((v = Double.compare(o1.penaltyValue, o2.penaltyValue)) != 0)
return v;

if ((v = Double.compare(o1.sequence.length, o2.sequence.length)) != 0)

