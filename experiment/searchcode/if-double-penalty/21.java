private Hashtable <String, double[]> penalty_buffer = new Hashtable <String, double[]>();
public void insert_penalty(String state_key, double pnlty){
//convert penalty
double[] penalty = new double[1];
penalty[0] = pnlty;

