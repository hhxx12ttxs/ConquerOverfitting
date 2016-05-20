//
// Berlin Brown
//
// $Id: BPSigmaPiConnection.java,v 1.1.1.1 2004/04/26 13:27:29 bigbinc Exp $
//
//

package org.retro.neural;

class BPSigmaPiConnection implements BPConnection {

    private SNMatrix [] w;	      // weights
    private SNMatrix [] dw;	      // weight changes
    private SNMatrix [] dw1;	      // previous weight changes	
    private SNMatrix [] bptt_dw;      // weight changes for back-prop thru time
    protected BPLayer f1, f2;	      // layers from (needed by BPBiased...)
    private BPLayer t;	              // layer to

    BPSigmaPiConnection(BPLayer from1, BPLayer from2, BPLayer to) {
        f1 = from1;
        f2 = from2;
	t = to;
    }

    // reset copy of weights for momentum
    public void mureset() {
	dw1 = _zero_weights();
    }

    // ranodmize weights
    public void randomize(java.util.Random r) {
	w = new SNMatrix [t.size()];
	for (int k=0; k<t.size(); ++k) {
	    w[k] = SNMatrix.gaussianNoise(f1.size(), f2.size(), r);
	}
    }

    // reset weight changes
    public void dwreset() {
	dw = _zero_weights();
    }

    // accumulate weight changes
    public void dwadd(SNVector d) {
	_weight_changes(dw, d);
    }

    // accumulate weight changes for Back-Prop Thru Time
    public void bptt_dwadd(SNVector d) {
	_weight_changes(bptt_dw, d);
    }

    // adds average over current pattern to total over all patterns
    public void bptt_dwavg(int patlen) {
	for (int k=0; k<t.size(); ++k) {
	    dw[k].add(SNMatrix.divide(bptt_dw[k], patlen));
	}
    }

    // resets pattern weights for back-prop thru time
    public void bptt_dwreset() {
	bptt_dw = _zero_weights();
    }

    // set weight changes
    public void dwset(SNVector d) {
	dwreset(); // needed for on-line learning
	_weight_changes(dw, d);
    }

    // activation of unit-to is sum over element-wise product of weights on
    // that unit with outer product of layer-from activations
    public SNVector forward() {
	SNVector a = new SNVector(t.size());
	SNMatrix op = _outprod();
	for (int k=0; k<t.size(); ++k) {
	    SNMatrix ww = SNMatrix.arrayMultiply(op, w[k]);
	    a.setValue(k, ww.sum());
	}
	return a;
    }

    // make copy of weights for momentum
    public void mucopy() {
	dw1 = dw;
    }

    // check whether there's a connection to one layer from another
    public boolean connectedFrom(BPLayer to, BPLayer from) {
	// always false for sigma-pi (needs two layers from)
	return false;
    }

    // check whether there's a connection to one layer from two others
    public boolean connectedFrom(BPLayer to, BPLayer from1, BPLayer from2) {
	return (this.f1 == from1 && this.f2 == from2 && this.t == to);
    }

    // return weights
    public SNMatrix [] getWeights() {
	return w;
    }

    // set weights, with size-mismatch check
    public void setWeights(SNMatrix [] ww) throws BPSizeException {
      if (t.size() != ww.length || 
          f1.size() != ww[0].getRows() || f2.size() != ww[0].getCols()) {
          throw new BPSizeException("BPLayer sizes don't match argument");
      }
      w = new SNMatrix[t.size()];
      for (int k=0; k<t.size(); ++k) {
	  w[k] = ww[k].copy();
      }
    }

    // computes mean (over patterns) of accumulated weigh changes
    public void dwavg(int npat) {
	for (int k=0; k<t.size(); ++k) {
	    dw[k].divide(npat);
	}
    }

    // recursively activate inbound layer using pattern
    public void activate(int patno) {
	f1.activate(patno); 
	f2.activate(patno); 
    }

    // recursively activate layer-from using clamped input
    public void activate(BPLayer inp, SNVector clmp) {
	f1.activate(inp, clmp); 
	f2.activate(inp, clmp); 
    }

    // recursively back-propagate error on layer-to using pattern
    public void backprop(int patno) {
	t.backprop(patno);
    }

    // get error on non-output layer
    public SNVector getError(BPLayer f) {

	// need activation of other layer
	SNVector e = new SNVector(f.size());
	BPLayer ff = (f == f1) ? f2 : f1;

	for (int k=0; k<t.size(); ++k) {

	    SNMatrix wk = (f == f1) ? w[k] : w[k].transpose();
	    wk = SNMatrix.multiply(wk, t.d.getValue(k));
	    e.add(wk.multiply(ff.a));
	}

	return e;
    }

    // add momentum term to weights
    public void muAdd(double mu) {
        for (int k=0; k<t.size(); ++k) {
	    w[k].add(SNMatrix.multiply(dw1[k], mu));
	}
    }

    // add learning term to weights
    public void etaAdd(double eta, int npat) {
        for (int k=0; k<t.size(); ++k) {
            if (npat != 1) { // optimization
		dw[k].divide(npat);
	    }
	    w[k].add(SNMatrix.multiply(dw[k], eta));
        }
    }

    // returns true if weights are empty (null), else false
    public boolean emptyWeights() {
        return w == null;
    }

    // return array of zero weight matrices
    private SNMatrix [] _zero_weights() {
	SNMatrix [] zw  = new SNMatrix[t.size()];
	for (int k=0; k<zw.length; ++k) {
	    zw[k] = new SNMatrix(f1.size(), f2.size());
	}
	return zw;
    }

    // changes to weights on unit-to equal delta of unit-to times
    // outer product of layer-from activations
    private void _weight_changes(SNMatrix [] dw, SNVector d) {

	SNMatrix op = _outprod();
	
	for (int k=0; k<t.size(); ++k) {
	    dw[k].add(SNMatrix.multiply(op, d.getValue(k)));
	}
    }

    // outer product of layer-from activations
    private SNMatrix _outprod() {
	return f1.a.outer(f2.a);
    }

} // end of the class .........................

