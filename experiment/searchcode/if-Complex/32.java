if (!complex) {
// value and type upwards from the MULT and UNARY.
/**
 * An expression is considered complex if it contains anything other than a
 * simple constant or variable.
 */
boolean complex = false;
/**
 * Only used if complex == false. This means that only one UNARY exist.
 */
children.add(OP.PLUS);
complex = true;
}
public static OP parse(String id) {
if (id.equals(PLUS.val))
return PLUS;
complex = mult.isComplex();

