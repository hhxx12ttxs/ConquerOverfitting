
public class Ray3d {
Vector3d o;
Vector3d d;

public Ray3d() {
o = new Vector3d();
d = new Vector3d();
}

public Ray3d(Ray3d r) {
o = r.o;
d = r.d;
}

public Ray3d(Vector3d o, Vector3d d) {

