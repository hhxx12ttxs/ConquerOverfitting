double yA    = y[signChangeIndex - 1];
double absYA = FastMath.abs(yA);
int agingA   = 0;
double xB    = x[signChangeIndex];
// target for the next evaluation point
double targetY;
if (agingA >= MAXIMAL_AGING) {

