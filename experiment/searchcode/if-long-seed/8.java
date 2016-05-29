seed = x * 341873128712L + z * 132897987541L + worldSeed + 872169178519L;
if (seed == 0) seed = 2010817090135L;
}

@Override
public long nextLong(long max) {
int res = (int) ((seed >> 24) % max);
if (res < 0) res += max;
return res;
}

public ZoomGenerator(long worldSeed, ZoomGenerator parent) {

