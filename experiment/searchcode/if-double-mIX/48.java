if (mixDescriptor.getMixDataSource() == mixDataSource) {
return mixDescriptor;
}
}

if (forcePrivateMix) {
double[] volume = new double[4];
if (mixDescriptor.isPrivateMix() == true) {
double[] spatialValues = mixDescriptor.getSpatialValues();

if (MixDescriptor.isSpatiallyNeutral(spatialValues) &amp;&amp;

