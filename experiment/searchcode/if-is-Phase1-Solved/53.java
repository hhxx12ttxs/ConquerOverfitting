static final short N_MOVE = 18;

// All coordinates are 0 for a solved cube except for UBtoDF, which is 114
short twist;
short flip;
// Move table for the three edges UR,UF and UL in phase1.
static short[][] URtoUL_Move = new short[N_URtoUL][N_MOVE];
static {
if(!loadTable(&quot;URtoUL_Move&quot;, URtoUL_Move)) {

