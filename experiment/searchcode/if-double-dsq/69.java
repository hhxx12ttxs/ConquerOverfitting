static final public int getLoweNMatches(ALocalFeaturesGroup<SURF> sg1, ALocalFeaturesGroup<SURF> sg2, double conf) {
if ( sg2.size() < 2 ) return 0;
int nMatches = 0;
SURF[] arr = sg1.lfArr;
static final public double getLoweFactor_avg(ALocalFeaturesGroup<SURF> sg1, ALocalFeaturesGroup<SURF> sg2) {
if ( sg2.size() < 2 ) return 0;
double sum = 0;
SURF[] arr = sg1.lfArr;
for (int i=0; i<arr.length; i++ ) {

