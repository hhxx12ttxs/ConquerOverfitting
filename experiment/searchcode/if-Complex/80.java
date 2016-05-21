    return o;
if (o instanceof PyComplex)
    o = Py.newFloat(returnReal ? ((PyComplex)o).real : ((PyComplex)o).imag);
    Array.set(newArray.data, size*i, objectToJava(flatData[i], typecode, true));
// Set complex elements if array is complex.
if (size == 2)
    /** Return the imaginary part of this multiarray if its complex otherwise return null.*/
    final PyMultiarray getImag() {
    /** Return the number of elements for this typecode (2 if complex, otherwise 1).*/
    final static int typeToNElements(char typecode) {
    /** Convert a PyObject into a native Java type based on <code>typecode<\/code>.
this defaults to zero if the PyObject is not an instance of PyComplex.*/
    private final static Object objectToJava(PyObject o, char typecode, boolean returnReal) {

