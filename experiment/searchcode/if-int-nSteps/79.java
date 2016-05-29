private ConflictHandler handler;
private final static int DT = 0;
private final static int NSTEPS =0;
private final static boolean RAW = true;
this.source = this.source + buildRequestString(DT, NSTEPS, RAW);
}

private String buildRequestString(int dt, int nsteps, boolean rawPrediction) {

