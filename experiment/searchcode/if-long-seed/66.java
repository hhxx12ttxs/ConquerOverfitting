//public static final XORShiftRandom global = new XORShiftRandom();


private long seed;

public XORShiftRandom() {
seed = System.nanoTime();
}

public XORShiftRandom(long seed) {
super(seed);
this.seed = seed;

