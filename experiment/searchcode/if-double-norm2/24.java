public double dot(Vec3J that) {
return x*that.x + y*that.y + z*that.z;
}

public double norm2() {
return this.dot(this);
}

public double norm() {
return Math.sqrt(norm2());
}

public Vec3J normalize() {

