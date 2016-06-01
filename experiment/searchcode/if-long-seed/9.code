public abstract class WorldGenLayer {

protected WorldGenLayer parent;

private long baseSeed;
private long globalSeed;
private long localSeed;

public WorldGenLayer(long seed) {
this.baseSeed = seed;
this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;

