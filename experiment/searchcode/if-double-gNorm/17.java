//this is online training in each individual user
@Override
public double train(){
double gNorm, gNormOld = Double.MAX_VALUE;;
// prepare to adapt: initialize gradient
Arrays.fill(m_g, 0);
calculateGradients(user);
gNorm = gradientTest();

if (m_displayLv==1) {

