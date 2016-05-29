super(constraintData, blenderContext);
if (blenderContext.getBlenderKey().isFixUpAxis()) {
int y = flag &amp; LOCLIKE_Y;
int invY = flag &amp; LOCLIKE_Y_INVERT;
int z = flag &amp; LOCLIKE_Z;
int invZ = flag &amp; LOCLIKE_Z_INVERT;

