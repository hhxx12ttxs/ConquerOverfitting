v = new double[3];
this.assign(newVec);
}

public Vec3 add(Vec3 v2){
return new Vec3(v[0] + v2.getX(), v[1] + v2.getY(), v[2] + v2.getZ());
v[2] = v2.getZ();
}

public Vec3 cross(Vec3 v2){
double x = v[1] * v2.getZ() - v[2] * v2.getY();
double y = v[2] * v2.getX() - v[0] * v2.getZ();

