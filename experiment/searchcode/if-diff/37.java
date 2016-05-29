Vector3f.add(this.dimensions, other.dimensions, diffSize);

diff.x = (Math.abs(diffPos.x) - diffSize.x);//abs diff
if(diff.x >=0)
{
return false;
}

diff.z = (Math.abs(diffPos.z) - diffSize.z);
if(diff.z >=0)

