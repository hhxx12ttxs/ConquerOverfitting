public static final XORShiftRandom global = new XORShiftRandom();


private long seed;

public XORShiftRandom() {
super();
this.seed = System.nanoTime();
// TODO Not thread-safe but if this function was synchronized it should be, do that in a subclass
long x = this.seed;

