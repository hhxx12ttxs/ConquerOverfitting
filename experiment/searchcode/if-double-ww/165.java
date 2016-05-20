//
// Berlin Brown
//
// $Id: BPSigmaConnection.java,v 1.1.1.1 2004/04/26 13:27:29 bigbinc Exp $
//
//
//....................................

package org.retro.neural;

class BPSigmaConnection implements BPConnection {


    private SNMatrix w;          // weights
    private SNMatrix dw;         // weight changes
    private SNMatrix dw1;        // previous weight changes        
    private SNMatrix bptt_dw;    // weight changes for back-prop thru time
    private BPLayer f, t;        // layers from, to

    public BPSigmaConnection(BPLayer from, BPLayer to) {
        f = from;
        t = to;
    }

    // reset copy of weights for momentum
    public void mureset() {
        dw1 = _zero_weights();
    }

    // ranodmize weights
    public void randomize(java.util.Random r) {
        w = SNMatrix.gaussianNoise(f.size(), t.size(), r);
    }

    // reset weight changes
    public void dwreset() {
        dw =  _zero_weights();
    }

    // accumulate weight changes
    public void dwadd(SNVector d) { 
        dw.add(_weight_changes(d));
    }

    // accumulate weight changes for Back-Prop Thru Time
    public void bptt_dwadd(SNVector d) {
       bptt_dw.add(_weight_changes(d));
    }

    // adds average over current pattern to total over all patterns
    public void bptt_dwavg(int patlen) {
	dw.add(SNMatrix.divide(bptt_dw, patlen));
    }
        
    // resets pattern weights for back-prop thru time
    public void bptt_dwreset() {
        bptt_dw = _zero_weights();
    }

    // multiply activations by weights
    public SNVector forward() {
	return f.a.multiply(w);
    }

    // make copy of weights for momentum
    public void mucopy() {
        dw1 = dw;
    }

    // check whether there's a connection to one layer from another
    public boolean connectedFrom(BPLayer to, BPLayer from) {
        return (this.f == from && this.t == to);
    }

    // check whether there's a connection to one layer from two others
    public boolean connectedFrom(BPLayer to, BPLayer from1, BPLayer from2) {
	// always false for sigma-pi (needs just one layer from)
	return false;
    }

    // return weights
    public SNMatrix getWeights() {
	return w;
    }

    // set weights, with size-mismatch check
    public void setWeights(SNMatrix ww) throws BPSizeException {
	int m = ww.getRows(), n = ww.getCols();
	if (t.size() !=  m || f.size() != n) {
          throw new BPSizeException("BPLayer sizes (" + t.size() + "x" +
				    f.size() + ") don't match matrix size (" +
				    m + "x" + n + ")");
	}
	w = ww.transpose();
    }

    // set weight changes
    public void dwset(SNVector d) {
	dw = _weight_changes(d);
    }

    // compute mean (over patterns) of accumulated weight changes
    public void dwavg(int npat) {
	dw.divide(npat);
    }

    // recursively activate inbound layer using pattern
    public void activate(int patno) {
        f.activate(patno);
    }

    // recursively activate layer-from using clamped input
    public void activate(BPLayer inp, SNVector clmp) {
        f.activate(inp, clmp); 
    }

    // recursively back-propagate error on layer-to using pattern
    public void backprop(int patno) {
        t.backprop(patno);
    }

    // get error on non-output layer, ignoring dummy argument for Sigma-Pi
    public SNVector getError(BPLayer f) {
        return t.d.multiply(w.transpose());
    }

    // add momentum term to weights
    public void muAdd(double mu) {
	w.add(SNMatrix.multiply(dw1, mu));
    }

    // add learning term to weights
    public void etaAdd(double eta, int npat) {
        if (npat != 1) { // optimization
	    dw.divide(npat);
        }
	w.add(SNMatrix.multiply(dw, eta));
    }

    // returns true if weights are empty (null), else false
    public boolean emptyWeights() {
        return w == null;
    }

    // return zero weight matrix
    private SNMatrix _zero_weights() {
	return new SNMatrix(f.size(), t.size());
    }

    // weight change is product of delta with incoming activation
    private SNMatrix _weight_changes(SNVector d) {
	return f.a.outer(d);
    }
} // end of the class ......................


