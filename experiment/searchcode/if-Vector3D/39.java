import ch.archilogic.solver.intersection.ILine;

public class Plane {
Vector3D x;
Vector3D u;
Vector3D v;
double detN = Vector3D.det(u, v, l.getDir());

if (detN == 0.0) {
// no intersection!

