public long utime;
public int nsteps;
public double xy[][];

public motion_plan_t()
public void _encodeRecursive(DataOutput outs) throws IOException
{
outs.writeLong(this.utime);

outs.writeInt(this.nsteps);

