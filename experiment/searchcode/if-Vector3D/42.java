public static OrthonormalBasis constructFromU(Vector3D a) {
Vector3D u = a.makeUnitVector();
Vector3D v = u.getCrossProduct(n);
if (v.getLengthSquared() < EPSILON) {
Vector3D v = a.makeUnitVector();
Vector3D u = v.getCrossProduct(n);
if (u.getLengthSquared() < EPSILON) {

