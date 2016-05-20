package net.lunglet.matlab;

import com.sun.jna.Callback;
import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ToNativeContext;
import com.sun.jna.ToNativeConverter;
import com.sun.jna.TypeMapper;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.Map;

public interface MXLibrary extends MXConstants, Library {
    static final class Loader {
        private static MXLibrary loadLibrary() {
            Map<String, TypeMapper> options = new HashMap<String, TypeMapper>();
            DefaultTypeMapper mapper = new DefaultTypeMapper();
            mapper.addToNativeConverter(NativeLong[].class, new ToNativeConverter() {
                public Class<NativeLongArray> nativeType() {
                    return NativeLongArray.class;
                }

                public Object toNative(final Object obj, final ToNativeContext context) {
                    return new NativeLongArray((NativeLong[]) obj);
                }
            });
            options.put(Library.OPTION_TYPE_MAPPER, mapper);
            return (MXLibrary) Native.loadLibrary("libmx", MXLibrary.class, options);
        }

        private Loader() {
        }
    }

    interface MXFunctionPtr extends Callback {
        void callback(int nlhs, MXArray[] plhs, int nrhs, MXArray[] prhs);
    }

    static final class NativeLongArray extends Memory {
        public NativeLongArray(final NativeLong[] nativeLongs) {
            super(nativeLongs.length * NativeLong.SIZE);
            for (int i = 0; i < nativeLongs.length; i++) {
                setNativeLong(i * NativeLong.SIZE, nativeLongs[i]);
            }
        }
    }

    MXLibrary INSTANCE = Loader.loadLibrary();

    /**
     * Add a field to a structure array.
     * <p>
     * Returns field number on success or -1 if inputs are invalid or an out of
     * memory condition occurs.
     */
    int mxAddField(MXArray pa, String fieldname);

    /**
     * Create a NULL terminated C string from an mxArray of type mxCHAR_CLASS
     * Supports multibyte character sets. The resulting string must be freed
     * with mxFree. Returns NULL on out of memory or non-character arrays.
     */
    Pointer mxArrayToString(MXArray pa);

    /**
     * Return the offset (in number of elements) from the beginning of the array
     * to a given subscript.
     */
    NativeLong mxCalcSingleSubscript(MXArray pa, NativeLong nsubs, NativeLong[] subs);

    /**
     * Allocate cleared memory, notifying registered listener.
     *
     * @param n
     *                number of objects
     * @param size
     *                size of objects
     */
    Pointer mxCalloc(NativeLong n, NativeLong size);

    /**
     * Create an N-Dimensional cell array, with each cell initialized to NULL.
     */
    MXArray mxCreateCellArray(NativeLong ndim, NativeLong[] dims);

    /**
     * Create a 2-Dimensional cell array, with each cell initialized to NULL.
     */
    MXArray mxCreateCellMatrix(NativeLong m, NativeLong n);

    /**
     * Create an N-Dimensional array to hold string data; initialize all
     * elements to 0.
     */
    MXArray mxCreateCharArray(NativeLong ndim, NativeLong[] dims);

    /**
     * Create a string array initialized to the strings in str.
     */
    MXArray mxCreateCharMatrixFromStrings(NativeLong m, String[] str);

    /**
     * Create a two-dimensional array to hold double-precision floating-point
     * data; initialize each data element to 0.
     */
    MXArray mxCreateDoubleMatrix(NativeLong m, NativeLong n, int complexityFlag);

    /**
     * Create a double-precision scalar mxArray initialized to the value
     * specified
     */
    MXArray mxCreateDoubleScalar(double value);

    /**
     * Create a logical array and initialize its data elements to false.
     */
    MXArray mxCreateLogicalArray(NativeLong ndim, NativeLong[] dims);

    /**
     * Create a two-dimensional array to hold logical data and initializes each
     * data element to false.
     */
    MXArray mxCreateLogicalMatrix(NativeLong m, NativeLong n);

    /**
     * Create a logical scalar mxArray having the specified value.
     */
    MXArray mxCreateLogicalScalar(boolean value);

    /**
     * Create a numeric array and initialize all its data elements to 0.
     * <p>
     * Similar to mxCreateNumericMatrix, in a standalone application,
     * out-of-memory will mean a NULL pointer is returned.
     */
    MXArray mxCreateNumericArray(NativeLong ndim, NativeLong[] dims, int classid, int complexityFlag);

    /**
     * Create a numeric matrix and initialize all its data elements to 0.
     */
    MXArray mxCreateNumericMatrix(NativeLong m, NativeLong n, int classid, int complexityFlag);

    /**
     * Create a 2-Dimensional sparse array.
     */
    MXArray mxCreateSparse(NativeLong m, NativeLong n, NativeLong nzmax, int complexityFlag);

    /**
     * Create a 2-D sparse logical array
     */
    MXArray mxCreateSparseLogicalMatrix(NativeLong m, NativeLong n, NativeLong nzmax);

    /**
     * Create a 1-by-n string array initialized to null terminated string where
     * n is the length of the string.
     */
    MXArray mxCreateString(String str);

    /**
     * Create a 1-by-n string array initialized to str. The supplied string is
     * presumed to be in the local codepage encoding. The character data format
     * in the mxArray will be UTF-16.
     */
    MXArray mxCreateStringFromNChars(String str, NativeLong n);

    /**
     * Create an N-Dimensional structure array having the specified fields;
     * initialize all values to NULL.
     */
    MXArray mxCreateStructArray(NativeLong ndim, NativeLong[] dims, int nfields, String[] fieldnames);

    /**
     * Create a 2-Dimensional structure array having the specified fields;
     * initialize all values to NULL.
     */
    MXArray mxCreateStructMatrix(NativeLong m, NativeLong n, int nfields, String[] fieldnames);

    /**
     * mxArray destructor.
     */
    void mxDestroyArray(MXArray pa);

    /**
     * Make a deep copy of an array, return a pointer to the copy.
     */
    MXArray mxDuplicateArray(MXArray in);

    /**
     * Free memory, notifying registered listener.
     */
    void mxFree(Pointer ptr);

    /**
     * Get a pointer to the specified cell element.
     */
    MXArray mxGetCell(MXArray pa, NativeLong i);

    /**
     * Get string array data.
     */
    Pointer mxGetChars(MXArray pa);

    /**
     * Return the class (catergory) of data that the array holds.
     */
    int mxGetClassID(MXArray pa);

    /**
     * Return the name of an array's class.
     */
    String mxGetClassName(MXArray pa);

    /**
     * Get pointer to data.
     */
    Pointer mxGetData(MXArray pa);

    /**
     * Get pointer to dimension array.
     */
    Pointer mxGetDimensions(MXArray pa);

    /**
     * Get array data element size.
     */
    NativeLong mxGetElementSize(MXArray pa);

    /**
     * Function for obtaining MATLAB's concept of EPS
     */
    double mxGetEps();

    /**
     * Return a pointer to the contents of the named field for the ith element
     * (zero based). Returns NULL on no such field or if the field itself is
     * NULL
     */
    MXArray mxGetField(MXArray pa, NativeLong i, String fieldname);

    /**
     * Return a pointer to the contents of the named field for the ith element
     * (zero based).
     */
    MXArray mxGetFieldByNumber(MXArray pa, NativeLong i, int fieldnum);

    /**
     * Return pointer to the nth field name.
     */
    String mxGetFieldNameByNumber(MXArray pa, int n);

    /**
     * Get the index to the named field.
     */
    int mxGetFieldNumber(MXArray pa, String name);

    /**
     * Get imaginary data pointer for numeric array
     */
    Pointer mxGetImagData(MXArray pa);

    /**
     * Function for obtaining MATLAB's concept of INF (Used in MEX-File
     * callback).
     */
    double mxGetInf();

    /**
     * Get row data pointer for sparse numeric array
     */
    Pointer mxGetIr(MXArray pa);

    /**
     * Get column data pointer for sparse numeric array.
     */
    Pointer mxGetJc(MXArray pa);

    /**
     * Get a properly typed pointer to the elements of a logical array.
     */
    ByteBuffer mxGetLogicals(MXArray pa);

    /**
     * Get row dimension.
     */
    NativeLong mxGetM(MXArray pa);

    /**
     * Get column dimension.
     */
    NativeLong mxGetN(MXArray pa);

    /**
     * Function for obtaining MATLAB's concept of NaN (Used in MEX-File
     * callback).
     */
    double mxGetNaN();

    /**
     * Copies characters from a MATLAB array to a char array This function will
     * attempt to perform null termination if it is possible. nChars is the
     * number of bytes in the output buffer
     */
    void mxGetNChars(MXArray pa, byte[] buf, NativeLong nChars);

    /**
     * Get number of dimensions in array.
     */
    NativeLong mxGetNumberOfDimensions(MXArray pa);

    /**
     * Get number of elements in array.
     */
    NativeLong mxGetNumberOfElements(MXArray pa);

    /**
     * Get number of structure fields in array.
     */
    int mxGetNumberOfFields(MXArray pa);

    /**
     * Get maximum nonzero elements for sparse numeric array.
     */
    NativeLong mxGetNzmax(MXArray pa);

    /**
     * Get imaginary data pointer for numeric array
     */
    Pointer mxGetPi(MXArray pa);

    /**
     * Get real data pointer for numeric array
     */
    Pointer mxGetPr(MXArray pa);

    /**
     * Get the real component of the specified array's first data element.
     */
    double mxGetScalar(MXArray pa);

    /**
     * Converts a string array to a C-style string. The C-style string is in the
     * local codepage encoding. If the conversion for the entire Unicode string
     * cannot fit into the supplied character buffer, then the conversion
     * includes the last whole codepoint that will fit into the buffer. The
     * string is thus truncated at the greatest possible whole codepoint and
     * does not split code- points.
     */
    int mxGetString(MXArray pa, byte[] buf, NativeLong buflen);

    /**
     * Get 8 bits of user data stored in the mxArray header.
     * <p>
     * NOTE: This state of these bits are not guaranteed to be preserved after
     * API function calls.
     */
    int mxGetUserBits(MXArray pa);

    /**
     * Determine whether the given array is a cell array.
     */
    boolean mxIsCell(MXArray pa);

    /**
     * Determine whether the given array contains character data.
     */
    boolean mxIsChar(MXArray pa);

    /**
     * Determine whether an array is a member of the specified class.
     */
    boolean mxIsClass(MXArray pa, String name);

    /**
     * Determine whether the given array contains complex data.
     */
    boolean mxIsComplex(MXArray pa);

    /**
     * Determine whether the specified array represents its data as
     * double-precision floating-point numbers.
     */
    boolean mxIsDouble(MXArray pa);

    /**
     * Is array empty.
     */
    boolean mxIsEmpty(MXArray pa);

    /**
     * test for finiteness in a machine-independent manner
     */
    boolean mxIsFinite(double x);

    /**
     * Is the isFromGlobalWorkspace bit set?
     */
    boolean mxIsFromGlobalWS(MXArray pa);

    /**
     * Returns true if specified array is a function object.
     */
    boolean mxIsFunctionHandle(MXArray pa);

    /**
     * test for infinity in a machine-independent manner
     */
    boolean mxIsInf(double x);

    /**
     * Determine whether the specified array represents its data as signed
     * 16-bit integers.
     */
    boolean mxIsInt16(MXArray pa);

    /**
     * Determine whether the specified array represents its data as signed
     * 32-bit integers.
     */
    boolean mxIsInt32(MXArray pa);

    /**
     * Determine whether the specified array represents its data as signed
     * 64-bit integers.
     */
    boolean mxIsInt64(MXArray pa);

    /**
     * Determine whether the specified array represents its data as signed 8-bit
     * integers.
     */
    boolean mxIsInt8(MXArray pa);

    /**
     * Determine whether the given array's logical flag is on.
     */
    boolean mxIsLogical(MXArray pa);

    /**
     * Returns true if we have a valid logical scalar mxArray.
     */
    boolean mxIsLogicalScalar(MXArray pa);

    /**
     * Returns true if the logical scalar value is true.
     */
    boolean mxIsLogicalScalarTrue(MXArray pa);

    /**
     * test for NaN in a machine-independent manner
     */
    boolean mxIsNaN(double x);

    /**
     * Determine whether the specified array contains numeric (as opposed to
     * cell or struct) data.
     */
    boolean mxIsNumeric(MXArray pa);

    /**
     * Is array user defined object
     */
    boolean mxIsObject(MXArray pa);

    /**
     * Determine whether the given array is an opaque array.
     */
    boolean mxIsOpaque(MXArray pa);

    /**
     * Determine whether the specified array represents its data as
     * single-precision floating-point numbers.
     */
    boolean mxIsSingle(MXArray pa);

    /**
     * Determine whether the given array is a sparse (as opposed to full).
     */
    boolean mxIsSparse(MXArray pa);

    /**
     * Determine whether the given array is a structure array.
     */
    boolean mxIsStruct(MXArray pa);

    /**
     * Determine whether the specified array represents its data as unsigned
     * 16-bit integers.
     */
    boolean mxIsUint16(MXArray pa);

    /**
     * Determine whether the specified array represents its data as unsigned
     * 32-bit integers.
     */
    boolean mxIsUint32(MXArray pa);

    /**
     * Determine whether the specified array represents its data as unsigned
     * 64-bit integers.
     */
    boolean mxIsUint64(MXArray pa);

    /**
     * Determine whether the specified array represents its data as unsigned
     * 8-bit integers.
     */
    boolean mxIsUint8(MXArray pa);

    /**
     * Allocate memory, notifying registered listener.
     *
     * @param n
     *                number of bytes
     */
    Pointer mxMalloc(NativeLong n);

    /**
     * Reallocate memory, notifying registered listener.
     */
    Pointer mxRealloc(Pointer ptr, NativeLong size);

    /**
     * Remove a field from a structure array. Does nothing if no such field
     * exists. Does not destroy the field itself.
     */
    void mxRemoveField(MXArray pa, int field);

    /**
     * Set an element in a cell array to the specified value.
     */
    void mxSetCell(MXArray pa, NativeLong i, MXArray value);

    /**
     * Set classname of an unvalidated object array. It is illegal to call this
     * function on a previously validated object array. Return 0 for success, 1
     * for failure.
     */
    int mxSetClassName(MXArray pa, String classname);

    /**
     * Set pointer to data
     */
    void mxSetData(MXArray pa, Pointer newdata);

    /**
     * Set dimension array and number of dimensions.
     * <p>
     * Returns 0 on success and 1 if there was not enough memory available to
     * reallocate the dimensions array.
     */
    int mxSetDimensions(MXArray pa, NativeLong[] size, NativeLong ndims);

    /**
     * Set pa[i]->fieldname = value.
     */
    void mxSetField(MXArray pa, NativeLong i, String fieldname, MXArray value);

    /**
     * Set pa[i][fieldnum] = value
     */
    void mxSetFieldByNumber(MXArray pa, NativeLong i, int fieldnum, MXArray value);

    /**
     * Set the isFromGlobalWorkspace bit.
     */
    void mxSetFromGlobalWS(MXArray pa, boolean global);

    /**
     * Set imaginary data pointer for numeric array
     */
    void mxSetImagData(MXArray pa, Pointer newdata);

    /**
     * Set row data pointer for numeric array.
     */
    void mxSetIr(MXArray pa, NativeLong[] newir);

    /**
     * Set column data pointer for numeric array.
     */
    void mxSetJc(MXArray pa, NativeLong[] newjc);

    /**
     * Set row dimension.
     */
    void mxSetM(MXArray pa, NativeLong m);

    /**
     * Set column dimension
     */
    void mxSetN(MXArray pa, NativeLong n);

    /**
     * Set maximum nonzero elements for numeric array.
     */
    void mxSetNzmax(MXArray pa, NativeLong nzmax);

    /**
     * Set imaginary data pointer for numeric array.
     */
    void mxSetPi(MXArray pa, DoubleBuffer pi);

    /**
     * Set real data pointer for numeric array.
     */
    void mxSetPr(MXArray pa, DoubleBuffer pr);

    /**
     * Set 8 bits of user data stored in the mxArray header.
     * <p>
     * NOTE: This state of these bits are not guaranteed to be preserved after
     * API function calls.
     */
    void mxSetUserBits(MXArray pa, int value);
}

