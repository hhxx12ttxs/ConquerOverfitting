public static final void toXYZValues(final double[][] IPTValuesArray,
boolean recoverCIELabScale) {
int size = IPTValuesArray.length;
//    IPT ipt = new IPT();

for (int x = 0; x < size; x++) {
if (recoverCIELabScale) {
//        ipt.setValues(IPTValuesArray[x]);

