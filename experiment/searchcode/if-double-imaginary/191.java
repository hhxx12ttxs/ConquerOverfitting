package net.lunglet.matlab;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

/*
 * TODO automatically cleanup MXArrays as outlined here:
 * http://java.sun.com/developer/technicalArticles/javase/finalization/
 *
 * TODO check return error codes and throw exceptions instead of
 * exposing the error code in the API
 *
 * TODO make sure that setPr and setPi keep references to their
 * arguments. It would probably also make sense to ensure that they
 * only accept direct buffers.
 *
 * TODO provide a getPi and getPr that doesn't copy.
 */

public final class MXArray extends PointerType {
    public static MXArray createDoubleMatrix(final int m, final int n) {
        return MXLibrary.INSTANCE.mxCreateDoubleMatrix(new NativeLong(m), new NativeLong(n), 0);
    }

    public MXArray() {
    }

    public MXArray(final Pointer p) {
        super(p);
    }

    /**
     * Add a field to a structure array.
     * <p>
     * Returns field number on success or -1 if inputs are invalid or an out of
     * memory condition occurs.
     */
    public void addField(final String fieldname) {
        int err = MXLibrary.INSTANCE.mxAddField(this, fieldname);
        if (err < 0) {
            throw new RuntimeException("mxAddField failed");
        }
    }

    /**
     * Create a NULL terminated C string from an mxArray of type mxCHAR_CLASS
     * Supports multibyte character sets. The resulting string must be freed
     * with mxFree. Returns NULL on out of memory or non-character arrays.
     */
    public Pointer arrayToString() {
        return MXLibrary.INSTANCE.mxArrayToString(this);
    }

    /**
     * Return the offset (in number of elements) from the beginning of the array
     * to a given subscript.
     */
    public NativeLong calcSingleSubscript(final MXArray pa, final NativeLong nsubs, final NativeLong[] subs) {
        return MXLibrary.INSTANCE.mxCalcSingleSubscript(this, nsubs, subs);
    }

    /**
     * mxArray destructor.
     */
    public void destroyArray() {
        MXLibrary.INSTANCE.mxDestroyArray(this);
    }

    /**
     * Make a deep copy of an array, return a pointer to the copy.
     */
    public MXArray duplicateArray() {
        return MXLibrary.INSTANCE.mxDuplicateArray(this);
    }

    /**
     * Get a pointer to the specified cell element.
     */
    public MXArray getCell(final NativeLong i) {
        return MXLibrary.INSTANCE.mxGetCell(this, i);
    }

    /**
     * Get string array data.
     */
    public Pointer getChars() {
        return MXLibrary.INSTANCE.mxGetChars(this);
    }

    /**
     * Return the class (catergory) of data that the array holds.
     */
    public int getClassID() {
        return MXLibrary.INSTANCE.mxGetClassID(this);
    }

    /**
     * Return the name of an array's class.
     */
    public String getClassName() {
        return MXLibrary.INSTANCE.mxGetClassName(this);
    }

    /**
     * Get pointer to data.
     */
    public Pointer getData() {
        return MXLibrary.INSTANCE.mxGetData(this);
    }

    /**
     * Get pointer to dimension array.
     */
    public Pointer getDimensions() {
        return MXLibrary.INSTANCE.mxGetDimensions(this);
    }

    /**
     * Get array data element size.
     */
    public NativeLong getElementSize() {
        return MXLibrary.INSTANCE.mxGetElementSize(this);
    }

    /**
     * Return a pointer to the contents of the named field for the ith element
     * (zero based). Returns NULL on no such field or if the field itself is
     * NULL
     */
    public MXArray getField(final NativeLong i, final String fieldname) {
        return MXLibrary.INSTANCE.mxGetField(this, i, fieldname);
    }

    /**
     * Return a pointer to the contents of the named field for the ith element
     * (zero based).
     */
    public MXArray getFieldByNumber(final NativeLong i, final int fieldnum) {
        return MXLibrary.INSTANCE.mxGetFieldByNumber(this, i, fieldnum);
    }

    /**
     * Return pointer to the nth field name.
     */
    public String getFieldNameByNumber(final int n) {
        return MXLibrary.INSTANCE.mxGetFieldNameByNumber(this, n);
    }

    /**
     * Get the index to the named field.
     */
    public int getFieldNumber(final String name) {
        return MXLibrary.INSTANCE.mxGetFieldNumber(this, name);
    }

    /**
     * Get imaginary data pointer for numeric array
     */
    public Pointer getImagData() {
        return MXLibrary.INSTANCE.mxGetImagData(this);
    }

    /**
     * Get row data pointer for sparse numeric array
     */
    public Pointer getIr() {
        return MXLibrary.INSTANCE.mxGetIr(this);
    }

    /**
     * Get column data pointer for sparse numeric array.
     */
    public Pointer getJc() {
        return MXLibrary.INSTANCE.mxGetJc(this);
    }

    /**
     * Get a properly typed pointer to the elements of a logical array.
     */
    public ByteBuffer getLogicals() {
        return MXLibrary.INSTANCE.mxGetLogicals(this);
    }

    /**
     * Get row dimension.
     */
    public NativeLong getM() {
        return MXLibrary.INSTANCE.mxGetM(this);
    }

    /**
     * Get column dimension.
     */
    public NativeLong getN() {
        return MXLibrary.INSTANCE.mxGetN(this);
    }

    /**
     * Copies characters from a MATLAB array to a char array This function will
     * attempt to perform null termination if it is possible. nChars is the
     * number of bytes in the output buffer
     */
    public void getNChars(final byte[] buf, final NativeLong nChars) {
        MXLibrary.INSTANCE.mxGetNChars(this, buf, nChars);
    }

    /**
     * Get number of dimensions in array.
     */
    public NativeLong getNumberOfDimensions() {
        return MXLibrary.INSTANCE.mxGetNumberOfDimensions(this);
    }

    /**
     * Get number of elements in array.
     */
    public NativeLong getNumberOfElements() {
        return MXLibrary.INSTANCE.mxGetNumberOfElements(this);
    }

    /**
     * Get number of structure fields in array.
     */
    public int getNumberOfFields() {
        return MXLibrary.INSTANCE.mxGetNumberOfFields(this);
    }

    /**
     * Get maximum nonzero elements for sparse numeric array.
     */
    public NativeLong getNzmax() {
        return MXLibrary.INSTANCE.mxGetNzmax(this);
    }

    /**
     * Get imaginary data pointer for numeric array
     */
    public double[] getPi() {
        int m = getM().intValue();
        int n = getN().intValue();
        return MXLibrary.INSTANCE.mxGetPi(this).getDoubleArray(0, m * n);
    }

    /**
     * Get real data pointer for numeric array
     */
    public double[] getPr() {
        int m = getM().intValue();
        int n = getN().intValue();
        return MXLibrary.INSTANCE.mxGetPr(this).getDoubleArray(0, m * n);
    }

    /**
     * Get the real component of the specified array's first data element.
     */
    public double getScalar() {
        return MXLibrary.INSTANCE.mxGetScalar(this);
    }

    /**
     * Converts a string array to a C-style string. The C-style string is in the
     * local codepage encoding. If the conversion for the entire Unicode string
     * cannot fit into the supplied character buffer, then the conversion
     * includes the last whole codepoint that will fit into the buffer. The
     * string is thus truncated at the greatest possible whole codepoint and
     * does not split code- points.
     */
    public int getString(final byte[] buf, final NativeLong buflen) {
        return MXLibrary.INSTANCE.mxGetString(this, buf, buflen);
    }

    /**
     * Get 8 bits of user data stored in the mxArray header.
     * <p>
     * NOTE: This state of these bits are not guaranteed to be preserved after
     * API function calls.
     */
    public int getUserBits() {
        return MXLibrary.INSTANCE.mxGetUserBits(this);
    }

    /**
     * Determine whether the given array is a cell array.
     */
    public boolean isCell() {
        return MXLibrary.INSTANCE.mxIsCell(this);
    }

    /**
     * Determine whether the given array contains character data.
     */
    public boolean isChar() {
        return MXLibrary.INSTANCE.mxIsChar(this);
    }

    /**
     * Determine whether an array is a member of the specified class.
     */
    public boolean isClass(final String name) {
        return MXLibrary.INSTANCE.mxIsClass(this, name);
    }

    /**
     * Determine whether the given array contains complex data.
     */
    public boolean isComplex() {
        return MXLibrary.INSTANCE.mxIsComplex(this);
    }

    /**
     * Determine whether the specified array represents its data as
     * double-precision floating-point numbers.
     */
    public boolean isDouble() {
        return MXLibrary.INSTANCE.mxIsDouble(this);
    }

    /**
     * Is array empty.
     */
    public boolean isEmpty() {
        return MXLibrary.INSTANCE.mxIsEmpty(this);
    }

    /**
     * Is the isFromGlobalWorkspace bit set?
     */
    public boolean isFromGlobalWS() {
        return MXLibrary.INSTANCE.mxIsFromGlobalWS(this);
    }

    /**
     * Returns true if specified array is a function object.
     */
    public boolean isFunctionHandle() {
        return MXLibrary.INSTANCE.mxIsFunctionHandle(this);
    }

    /**
     * Determine whether the specified array represents its data as signed
     * 16-bit integers.
     */
    public boolean isInt16() {
        return MXLibrary.INSTANCE.mxIsInt16(this);
    }

    /**
     * Determine whether the specified array represents its data as signed
     * 32-bit integers.
     */
    public boolean isInt32() {
        return MXLibrary.INSTANCE.mxIsInt32(this);
    }

    /**
     * Determine whether the specified array represents its data as signed
     * 64-bit integers.
     */
    public boolean isInt64() {
        return MXLibrary.INSTANCE.mxIsInt64(this);
    }

    /**
     * Determine whether the specified array represents its data as signed 8-bit
     * integers.
     */
    public boolean isInt8() {
        return MXLibrary.INSTANCE.mxIsInt8(this);
    }

    /**
     * Determine whether the given array's logical flag is on.
     */
    public boolean isLogical() {
        return MXLibrary.INSTANCE.mxIsLogical(this);
    }

    /**
     * Returns true if we have a valid logical scalar mxArray.
     */
    public boolean isLogicalScalar() {
        return MXLibrary.INSTANCE.mxIsLogicalScalar(this);
    }

    /**
     * Returns true if the logical scalar value is true.
     */
    public boolean isLogicalScalarTrue() {
        return MXLibrary.INSTANCE.mxIsLogicalScalarTrue(this);
    }

    /**
     * Determine whether the specified array contains numeric (as opposed to
     * cell or struct) data.
     */
    public boolean isNumeric() {
        return MXLibrary.INSTANCE.mxIsNumeric(this);
    }

    /**
     * Is array user defined object
     */
    public boolean isObject() {
        return MXLibrary.INSTANCE.mxIsObject(this);
    }

    /**
     * Determine whether the given array is an opaque array.
     */
    public boolean isOpaque() {
        return MXLibrary.INSTANCE.mxIsOpaque(this);
    }

    /**
     * Determine whether the specified array represents its data as
     * single-precision floating-point numbers.
     */
    public boolean isSingle() {
        return MXLibrary.INSTANCE.mxIsSingle(this);
    }

    /**
     * Determine whether the given array is a sparse (as opposed to full).
     */
    public boolean isSparse() {
        return MXLibrary.INSTANCE.mxIsSparse(this);
    }

    /**
     * Determine whether the given array is a structure array.
     */
    public boolean isStruct() {
        return MXLibrary.INSTANCE.mxIsStruct(this);
    }

    /**
     * Determine whether the specified array represents its data as unsigned
     * 16-bit integers.
     */
    public boolean isUint16() {
        return MXLibrary.INSTANCE.mxIsUint16(this);
    }

    /**
     * Determine whether the specified array represents its data as unsigned
     * 32-bit integers.
     */
    public boolean isUint32() {
        return MXLibrary.INSTANCE.mxIsUint32(this);
    }

    /**
     * Determine whether the specified array represents its data as unsigned
     * 64-bit integers.
     */
    public boolean isUint64() {
        return MXLibrary.INSTANCE.mxIsUint64(this);
    }

    /**
     * Determine whether the specified array represents its data as unsigned
     * 8-bit integers.
     */
    public boolean isUint8() {
        return MXLibrary.INSTANCE.mxIsUint8(this);
    }

    /**
     * Remove a field from a structure array. Does nothing if no such field
     * exists. Does not destroy the field itself.
     */
    public void removeField(final int field) {
        MXLibrary.INSTANCE.mxRemoveField(this, field);
    }

    /**
     * Set an element in a cell array to the specified value.
     */
    public void setCell(final NativeLong i, final MXArray value) {
        MXLibrary.INSTANCE.mxSetCell(this, i, value);
    }

    /**
     * Set classname of an unvalidated object array. It is illegal to call this
     * function on a previously validated object array. Return 0 for success, 1
     * for failure.
     */
    public void setClassName(final String classname) {
        int err = MXLibrary.INSTANCE.mxSetClassName(this, classname);
        if (err != 0) {
            throw new RuntimeException("mxSetClassName failed");
        }
    }

    /**
     * Set pointer to data
     */
    public void setData(final Pointer newdata) {
        MXLibrary.INSTANCE.mxSetData(this, newdata);
    }

    /**
     * Set dimension array and number of dimensions.
     * <p>
     * Returns 0 on success and 1 if there was not enough memory available to
     * reallocate the dimensions array.
     */
    public void setDimensions(final NativeLong[] size, final NativeLong ndims) {
        int err = MXLibrary.INSTANCE.mxSetDimensions(this, size, ndims);
        if (err != 0) {
            throw new RuntimeException("mxSetDimensions failed");
        }
    }

    /**
     * Set pa[i]->fieldname = value.
     */
    public void setField(final NativeLong i, final String fieldname, final MXArray value) {
        MXLibrary.INSTANCE.mxSetField(this, i, fieldname, value);
    }

    /**
     * Set pa[i][fieldnum] = value
     */
    public void setFieldByNumber(final NativeLong i, final int fieldnum, final MXArray value) {
        MXLibrary.INSTANCE.mxSetFieldByNumber(this, i, fieldnum, value);
    }

    /**
     * Set the isFromGlobalWorkspace bit.
     */
    public void setFromGlobalWS(final boolean global) {
        MXLibrary.INSTANCE.mxSetFromGlobalWS(this, global);
    }

    /**
     * Set imaginary data pointer for numeric array.
     */
    public void setImagData(final Pointer newdata) {
        MXLibrary.INSTANCE.mxSetImagData(this, newdata);
    }

    /**
     * Set row data pointer for numeric array.
     */
    public void setIr(final NativeLong[] newir) {
        MXLibrary.INSTANCE.mxSetIr(this, newir);
    }

    /**
     * Set column data pointer for numeric array.
     */
    public void setJc(final NativeLong[] newjc) {
        MXLibrary.INSTANCE.mxSetJc(this, newjc);
    }

    /**
     * Set row dimension.
     */
    public void setM(final NativeLong m) {
        MXLibrary.INSTANCE.mxSetM(this, m);
    }

    /**
     * Set column dimension.
     */
    public void setN(final NativeLong n) {
        MXLibrary.INSTANCE.mxSetN(this, n);
    }

    /**
     * Set maximum nonzero elements for numeric array.
     */
    public void setNzmax(final NativeLong nzmax) {
        MXLibrary.INSTANCE.mxSetNzmax(this, nzmax);
    }

    /**
     * Set imaginary data pointer for numeric array.
     */
    public void setPi(final DoubleBuffer pi) {
        MXLibrary.INSTANCE.mxSetPi(this, pi);
    }

    /**
     * Set real data pointer for numeric array.
     */
    public void setPr(final DoubleBuffer pr) {
        MXLibrary.INSTANCE.mxSetPr(this, pr);
    }

    /**
     * Set 8 bits of user data stored in the mxArray header.
     * <p>
     * NOTE: This state of these bits are not guaranteed to be preserved after
     * API function calls.
     */
    public void setUserBits(final int value) {
        MXLibrary.INSTANCE.mxSetUserBits(this, value);
    }

    @Override
    public String toString() {
        return "MXArray: " + getM() + "x" + getN() + ", isNumeric?=" + isNumeric();
    }
}

