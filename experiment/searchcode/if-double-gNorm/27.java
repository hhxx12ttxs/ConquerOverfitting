//this is online training in each individual user
@Override
public double train(){
double gNorm, gNormOld = Double.MAX_VALUE;;
int predL, trueL;
calculateGradients(user);
gNorm = gradientTest();

if (m_displayLv==1) {
if (gNorm<gNormOld)

