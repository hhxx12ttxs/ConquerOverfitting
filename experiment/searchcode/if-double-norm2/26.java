public double dot(Vec3 that) {
return x*that.x + y*that.y + z*that.z;
}

public double norm2() {
return this.dot(this);
}

public double norm() {
return Math.sqrt(norm2());
}

public Vec3 normalize() {
if (norm2() == 0)

