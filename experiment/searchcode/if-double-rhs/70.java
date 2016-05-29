public static PhysicsScalarType _DIV(double lhs, PhysicsScalarType rhs) {
if (rhs instanceof Mass) {
return _DIV(new Mass(lhs), rhs);
public static PhysicsScalarType _MULT(PhysicsScalarType lhs, double rhs) {
if (lhs instanceof Mass) {
return _MULT(lhs, new Mass(rhs));

