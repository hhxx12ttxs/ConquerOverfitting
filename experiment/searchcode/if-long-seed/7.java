public class Random extends java.util.Random {
/**
*
*/
private static final long serialVersionUID = 1L;
public static long RANDOM_SEED = 0;
public static boolean ESCAPE_RANDOM_SEED = true;
private long _mySeed;
public Random() {
this(RANDOM_SEED, true);

