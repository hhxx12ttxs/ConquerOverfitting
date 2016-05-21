 */
boolean complex = false;
/**
 * Only used if complex == false. This means that only one UNARY exist.
 */
// set to complex if the unary is a functions, expression or other than
// a CONSTANT or Variable.
if (!complex) {
complex = unaryType == UNARY.TYPE.MIXED || children.size() > 1;
/**
 * An expression is considered complex if it contains anything other than a
 * simple constant or variable.

