public static EntityBounds createFor(LMultiplicity multiplicity) {
if (multiplicity == null) {
return new EntityBounds(LowerBound.ZERO, UpperBound.ONE);
UpperBound ubS = multiplicity.getUpper();
LowerBound lower = null;
UpperBound upper = null;
if (ubS == null || ubS.equals(&quot;&quot;)) {

