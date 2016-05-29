public Vector3f Reflect(Vector3f Normal, boolean reverse) {
Vector3f n = Normal;
if (Normal.dot(this) < 0)
n = Normal.Scale(-1);
float costheta = this.dot(n);
return new Vector3f(x / length, y / length, z / length);
}

public boolean isParallel(Vector3f v) {
if (this.Normalize().x == v.Normalize().x

