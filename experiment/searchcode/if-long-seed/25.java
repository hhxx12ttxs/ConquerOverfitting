protected long worldGenSeed;
protected TUMGenLayer parent;
protected long chunkSeed;
protected long baseSeed;

public TUMGenLayer(long Seed) {
this.baseSeed += Seed;
}
}

@Override
public void initWorldGenSeed(long Seed)
{
this.worldGenSeed = Seed;
if (this.parent!=null)

