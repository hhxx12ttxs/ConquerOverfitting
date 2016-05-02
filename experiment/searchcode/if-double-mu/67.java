//
// Berlin Brown
//
// $Id: BPNetwork.java,v 1.1.1.1 2004/04/26 13:27:29 bigbinc Exp $
//
//

package org.retro.neural;
    
import java.util.HashSet;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.Timer;
import java.io.PrintStream;

// Support class for BPLayer.  Each BPLayer in a network points to that
// network.

class BPNetwork {

    // milliseconds between error reports
    private static final long REPORT_MSEC = 1000;

    private HashSet _layers;	// set of all layers
    private HashSet _src;	// set of layers with outbound connections
    private HashSet _dst;       // set of layers with inbound connections
    private HashSet _dly;       // set of layers with inbound delays
    private int _epoch;         // epoch counter for error reporting

    // creates an empty neural network
    protected BPNetwork() {

	_layers = new HashSet();
	_src = new HashSet();
	_dst = new HashSet();
	_dly = new HashSet();
    }

    // add connection from layer 1 to layer 2
    protected void connect(BPLayer layer1, BPLayer layer2) {

	_src.add(layer1);
	_dst.add(layer2);

	_layers.add(layer1);
	_layers.add(layer2);	
    }

    // add sigma-pi connection from layers 1,2 to 3
    protected void connect(BPLayer layer1, BPLayer layer2, BPLayer layer3) {

	_src.add(layer1);
	_src.add(layer2);
	_dst.add(layer3);

	_layers.add(layer1);
	_layers.add(layer2);	
	_layers.add(layer3);	
    }

    // creates a delay connection
    protected void delay(BPLayer to) {
	_dly.add(to);
    }

    // batch training with report every REPORT_MSEC milliseconds
    protected void batch(BPLayer out, int nep, double eta, double mu)
	throws BPInitException, BPSizeException {

	BatchTimedTrainer trainer = new BatchTimedTrainer();
	trainer.run(out, nep, eta, mu);
    }

    // batch training with specified report interval
    protected void batch(BPLayer out, int nep, double eta, double mu, 
			 int report) throws BPInitException, BPSizeException {

	BatchFixedTrainer trainer = new BatchFixedTrainer();
	trainer.run(out, nep, report, eta, mu);
    }

    // runs one step of back-propagation in batch mode
    protected void batch(double eta, double mu)
	throws BPInitException, BPSizeException {

	int patlen = trainCheck();

	_batch(eta, mu, patlen);
    }

    // online training with specified report interval
    protected void online(BPLayer out,int nep, double eta, double mu, 
			  int report) throws BPInitException, BPSizeException {

	OnlineFixedTrainer trainer = new OnlineFixedTrainer();
	trainer.run(out, nep, report, eta, mu);
    }

    // online training with report every REPORT_MSEC milliseconds
    protected void online(BPLayer out,int nep, double eta, double mu)
	throws BPInitException, BPSizeException {

	OnlineTimedTrainer trainer = new OnlineTimedTrainer();
	trainer.run(out, nep, eta, mu);
    }

    // resets previous weight and bias changes for momentum
    protected void _mureset() {
	for (Iterator i = _layers.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.mureset();
	}
    }

    // updates weight,bias on all layers with input
    protected void _update(double eta, double mu, int npat) {
	for (Iterator i = _dst.iterator(); i.hasNext(); ) {
	    BPLayer layer = (BPLayer)i.next();
	    layer.update(eta, mu, npat);
	}	
    }

    // reconciles other network to this by copying layers and relations
    protected void reconcile(BPNetwork other) {

	_copy_layers(this._layers, other._layers);
	_copy_layers(this._src, other._src);
	_copy_layers(this._dst, other._dst);
	_copy_layers(this._dly, other._dly);

	// point back to network from all its layers
	for (Iterator i = _layers.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.net = this;
	}
    }


    // steps through one pattern for limited Back-Prop Thru Time
    protected void bptt(int n) 
	throws BPInitException, BPSizeException, IllegalArgumentException {

	int patlen = trainCheck();

	if (n < 0 || n > (patlen-1)) {
		throw new IllegalArgumentException("n must be in range [0," +
						   (patlen-1) + "]");
	}

	_bptt(n+1, patlen);
    }


    // steps through one pattern for full Back-Prop Thru Time
    protected void bptt() throws BPInitException, BPSizeException {

	int patlen = trainCheck();
	_bptt(patlen, patlen);
    }


    // tests layer for full input sequence
    protected SNMatrix test(BPLayer layer) 

	throws BPInitException, BPSizeException {

	int patlen = _input_check();

	_actinit();  // set all activations to neutral values

	return _test(layer, patlen);
    }


    // tests layer for specified number of inputs with intial activation
    // on other layers
    protected SNMatrix test(BPLayer layer, int n, double a) 

	throws BPInitException {

	_actinit(a);

	return _test(layer, n);
    }

    // checks whether network has been initialized and whether all
    // patterns have same length, which is returned
    protected int trainCheck() 
	throws BPInitException, BPSizeException {

	_init_check();

	int patlen = _input_check();

	for (Iterator i = _layers.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    if (l.hasPattern() && (l.patlen() != patlen)) {
		throw new BPPatternSizeException();
	    }
	}

	return patlen;
    }

    // computes  squared errors if necessary
    protected void makesqrerrs(int patlen) {
	for (Iterator i = _layers.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    if (l.nosqrerrs()) {
		_gensqrerrs(patlen);
		break;
	    }
	}
    }


    // set up for training, returning pattern length
    private int _train_setup() {

	int patlen = trainCheck();

	// reset previous weight,bias changes for momentum
	_mureset();

	// initialize delay-layer activations	
	_actinit();

	// initialize squared errors
	_sqrerrinit(patlen);

	return patlen;
    }

    // tests layer for specified number pf iterations
    private SNMatrix _test(BPLayer layer, int n) 

	throws BPInitException, BPSizeException {

	_init_check();

	SNMatrix out = new SNMatrix(n, layer.size());

	for (int i=0; i<n; ++i) {
	    _unmark();
	    _activate(i);
	    out.setRow(i, layer.a);
	    _delayact();
	}

	return out;
    }

    // generate squared errors via backprop
    private void _gensqrerrs(int patlen) {

	_sqrerrinit(patlen);

	for (int i=0; i<patlen; ++i) {
	    _activate(i);
	    _errreset();
	    _backprop(i);
	}
    }

    // reports a value in a friendly way
    protected static void reportval(int iter, int maxit, int rep, String v,
				    PrintStream s) {

	if (_report(iter, maxit, rep)) {
	    _dump(iter, v, s);
	}
    }

    // checks whether to report
    private static boolean _report(int iter, int maxit, int rep) {
	return (iter == 0 || iter == maxit-1 || ((iter+1)%rep) == 0);
    }

    // report iteration plus value
    private static void _dump(int iter, String v, PrintStream s) {
	    s.println((iter+1) + "\t" + v);
    }

    // checks whether network weights have been initialized
    private void _init_check() throws BPInitException {

	for (Iterator i = _dst.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    if (l.getBias() == null ||
                l.emptyWeights()) throw new BPInitException();
	}
    }


    // checks whether all input patterns have same length, which is returned
    private int _input_check() throws BPPatternSizeException {

	int patlen = 0;
	for (Iterator i = _layers.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    if (l.in.isEmpty() && l.hasPattern()) {
		if (patlen == 0) {
		    patlen = l.patlen();
		}
		else if (patlen != l.patlen()) {
		    throw new BPPatternSizeException();
		}
	    }
	}
	return patlen;
    }

    // runs one epoch of back-prop in batch mode
    private void _batch(double eta, double mu, int patlen) {

	// reset weight changes
	_dwreset();

	// recursively activate from layers with incoming connections
	_batch_step(patlen);

	// compute average weight changes over patterns
	_dwavg(patlen);

	// update weights and biases
	_update(eta, mu, 1);

    }

    // runs one iteration of on-line backprop
    private void _online(double eta, double mu, int patlen) {

	// recursively activate from layers with incoming connections
	for (int i=0; i<patlen; ++i) {

	    _activate(i);

	    _errreset();

	    _backprop(i);

	    // compute weight changes immediately for this pattern
	    _dwset();

	    // update weights and biases
	    _update(eta, mu, 1);

	    // update activations on delayed-to layers
	    _delayact();	
	}
    }

    // run one step of batch training thru all patterns
    private void _batch_step(int patlen) {

	for (int i=0; i<patlen; ++i) {

	    _activate(i);

	    _errreset();

	    _backprop(i);

	    // accumulate weight changes
	    _dwadd();

	    // update activations on delayed-to layers
	    _delayact();	
	}
    }

    // run back-prop-thru-time on one pattern
    private void _bptt(int n, int patlen) {

	// initialize delay-layer activations	
	_actinit();

	// set up activation histories
	_bptt_inita();

	// reset pattern weight changes
	_bptt_dwreset();

	for (int i=0; i<patlen; ++i) {

	    // activate all layers, using current pattern
	    _activate(i);

	    // add to activation histories
	    if (i >= (patlen-n)) {
		_bptt_seta();
	    }

	    // update activations on delayed-to layers
	    _delayact();
	}

	// reset previous errors on delayed-to layers
	_bptt_errinit();

	// initialize squared errors
	_sqrerrinit(patlen);

	// start from final pattern
	for (int i=patlen-1; i>=patlen-n; --i) {

	    // use activation histories for current activations
	    _bptt_geta();

	    // reset all errors to zero
	    _errreset();

	    // update errors on delayed-from layers
	    _bptt_delayerr();

	    // compute errors by backprop
	    _backprop(i);

	    // store delayed-to errors for next time
	    _bptt_saverr();

	    // accumulate weight changes
	    _bptt_dwadd();
	}

	// add average for this pattern to weight change over all patterns
	_bptt_dwavg(patlen);
    }


    // accumulates weight,bias changes for all layers with input
    private void _dwadd() {
	
	for (Iterator i = _dst.iterator(); i.hasNext(); ) {
	    BPLayer layer = (BPLayer)i.next();
	    layer.dwadd();
	}	
    }

    // sets weight,bias changes for all layers with input
    private void _dwset() {
	
	for (Iterator i = _dst.iterator(); i.hasNext(); ) {
	    BPLayer layer = (BPLayer)i.next();
	    layer.dwset();
	}	
    }

    // averages weight,bias changes for all layers with input
    private void _dwavg(int npat) {
	
	for (Iterator i = _dst.iterator(); i.hasNext(); ) {
	    BPLayer layer = (BPLayer)i.next();
	    layer.dwavg(npat);
	}	
    }

    // initialize delay-layer activations to neutral value
    private void _actinit() {
	for (Iterator i = _dly.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.actinit();
	}
    }

    // initialize delay-layer activations to specified value
    private void _actinit(double a) {
	for (Iterator i = _dly.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.actinit(a);
	}
    }

    // initialize squared errors
    private void _sqrerrinit(int patlen) {
	for (Iterator i = _layers.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.sqrerrinit(patlen);
	}
    }

    // update activations on delayed-to layers
    private void _delayact() {
	for (Iterator i = _dly.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.delayact();
	}
    }

    // update error on delayed-from layers
    private void _bptt_delayerr() {
	for (Iterator i = _dly.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.bptt_delayerr();
	}
    }

    // store delayed-to errors for next time
    private void _bptt_saverr() {
	for (Iterator i = _dly.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.bptt_saverr();
	}
    }

    // reset weight and bias changes for momentum
    private void _dwreset() {
	
	for (Iterator i = _dst.iterator(); i.hasNext(); ) {
	    BPLayer layer = (BPLayer)i.next();
	    layer.dwreset();
	}	
    }

    private void _errreset() {
	for (Iterator i = _layers.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.errreset();
	}	
    }

    // set up activation histories for Back-Prop-Thru-Time
    private void _bptt_inita() {
	for (Iterator i = _layers.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.bptt_inita();
	}
    }

    // records current activations for Back-Prop-Thru-Time
    private void _bptt_seta() {
	for (Iterator i = _layers.iterator(); i.hasNext(); ) {
	    BPLayer layer = (BPLayer)i.next();
	    layer.bptt_seta();
	}	
    }

    // retrieves activation histories for Back-Prop-Thru-Time
    private void _bptt_geta() {
	for (Iterator i = _layers.iterator(); i.hasNext(); ) {
	    BPLayer layer = (BPLayer)i.next();
	    layer.bptt_geta();
	}	
    }

    // adds weight changes for current BPTT pattern to total for pattern
    private void _bptt_dwadd() {
	for (Iterator i = _dst.iterator(); i.hasNext(); ) {
	    BPLayer layer = (BPLayer)i.next();
	    layer.bptt_dwadd();
	}	
    }


    // adds average over current pattern to total over all patterns
    private void _bptt_dwavg(int patlen) {
	for (Iterator i = _dst.iterator(); i.hasNext(); ) {
	    BPLayer layer = (BPLayer)i.next();
	    layer.bptt_dwavg(patlen);
	}	
    }

    // resets pattern weights, total pattern count for back-prop thru time
    private void _bptt_dwreset() {
	for (Iterator i = _dst.iterator(); i.hasNext(); ) {
	    BPLayer layer = (BPLayer)i.next();
	    layer.bptt_dwreset();
	}	
    }

    // reset previous errors on delayed-to layers
    private void _bptt_errinit() {
	for (Iterator i = _dly.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.bptt_errinit();
	}	
    }

    // activate layers
    private void _activate(int patno) {

	_unmark();

	// activate layers in list
	for (Iterator i=_layers.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.activate(patno);
	}	
    }

    // backprop error
    private void _backprop(int patno) {

	_unmark();

	for (Iterator i=_layers.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.backprop(patno);
	}	
    }

    // reset marked flags
    private void _unmark() {
	for (Iterator i = _layers.iterator(); i.hasNext(); ) {
	    BPLayer l = (BPLayer)i.next();
	    l.unmark();
	}
    }

    // report RMS, max error
    private void _reporterr(BPLayer out, int patlen, int iter) {

      String s = "\trmserr = " + out.rmserr(patlen) +
	         "\tmaxerr = " + out.maxerr(patlen);

      _dump(iter, s, System.err);
    }

    // copy layer set from other network to this
    private static void _copy_layers(HashSet these, HashSet others) {

	for (Iterator i = others.iterator(); i.hasNext(); ) {
	    these.add(i.next());
	}
    }

    // inner class for reporting errors at fixed intervals
    class ErrorTimerTask extends TimerTask {

	private int nep, patlen;
	private BPLayer out;

	public ErrorTimerTask(int nep, BPLayer out, int patlen) {
	    this.nep = nep;
	    this.out = out;
	    this.patlen = patlen;
	}

	public void run() {
	    if (_epoch > 0) { // else error undefined
		_reporterr(out, patlen, _epoch);
	    }
	}
    }


    // inner class for training with fixed-epoch reporting
    abstract class FixedTrainer {

	abstract void step(double eta, double mu, int patlen);

	public void run(BPLayer out, int nep, int report, 
			double eta, double mu) {

	    int patlen = _train_setup();

	    for (int i=0; i<nep; ++i) {

		step(eta, mu, patlen);

		if (_report(i, nep, report)) {

		    _reporterr(out, patlen, i);
		}
	    }
	}
    }

    // inner class for online training with fixed-epoch reporting
    class OnlineFixedTrainer extends FixedTrainer {

	public void run(BPLayer out, int nep, int report, 
			double eta, double mu) {
	    super.run(out, nep, report, eta, mu);
	}

	void step(double eta, double mu, int patlen) {
	    _online(eta, mu, patlen);
	}
    }

    // inner class for batch training with fixed-epoch reporting
    class BatchFixedTrainer extends FixedTrainer {

	public void run(BPLayer out, int nep, int report,
			double eta, double mu) {
	    super.run(out, nep, report, eta, mu);
	}

	void step(double eta, double mu, int patlen) {
	    _batch(eta, mu, patlen);
	}
    }

    // inner class for timed training
    abstract class TimedTrainer {

	abstract void step(double eta, double mu, int patlen);

	public void run(BPLayer out, int nep, double eta, double mu) {

            int patlen = _train_setup();

	    // set up a task that will fire every REPORT_MSEC milliseconds
	    ErrorTimerTask task = new ErrorTimerTask(nep, out, patlen);
	    Timer timer = new Timer();
	    timer.schedule(task, 0, REPORT_MSEC);

	    for (_epoch=0; _epoch<=nep; ++_epoch) {

		step(eta, mu, patlen);

		// report initial error
		if (_epoch == 0) {
		    _reporterr(out, patlen, 0);
		}
	    }

	    // let's not run forever!
	    timer.cancel();

	    // report final error
	    _reporterr(out, patlen, nep-1);
	}
    }

    // inner class for timed online training
    class OnlineTimedTrainer extends TimedTrainer {

	public void run(BPLayer out, int nep, double eta, double mu) {
	    super.run(out, nep, eta, mu);
	}

	void step(double eta, double mu, int patlen) {
	    _online(eta, mu, patlen);
	}
    }

    // inner class for timed batch training
    class BatchTimedTrainer extends TimedTrainer {

	public void run(BPLayer out, int nep, double eta, double mu) {
	    super.run(out, nep, eta, mu);
	}

	void step(double eta, double mu, int patlen) {
	    _batch(eta, mu, patlen);
	}
    }

} // end of the class ...................

