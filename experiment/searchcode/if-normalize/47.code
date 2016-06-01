Vec3 m = new Vec3(0.0, 1.0, 0.0);
u.normalize();
this.u.assign(u);
this.v = this.u.cross(n);
if(v.length() < Vec3.epsilon) this.v = this.u.cross(m);
Vec3 m = new Vec3(0.0, 1.0, 0.0);
v.normalize();
this.v = v;
this.u = this.v.cross(n);
if(u.length() < Vec3.epsilon) u = this.v.cross(m);

