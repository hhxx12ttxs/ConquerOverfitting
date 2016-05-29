public static final double EPS_THRESH = 1E-6;

Double[] m_coeff;

public Polynomial(int sz){
m_coeff = new Double[sz];
int sz = this.getSize() - p.getSize() + 1;
if (sz < 1){
Polynomial q = new Polynomial(1);
q.m_coeff[0] = 0.;

