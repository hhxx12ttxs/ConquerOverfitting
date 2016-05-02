package org.bridj;
import org.bridj.util.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.*;
import java.lang.annotation.Annotation;
import java.util.*;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import static org.bridj.SizeT.safeIntCast;
import java.util.logging.Level;

/**
 * Pointer to a native memory location.<br>
 * Pointer is the entry point of any pointer-related operation in BridJ.
 * <p>
 * <u><b>Manipulating memory</b></u>
 * <p>
 * <ul>
 *	<li>Wrapping a memory address as a pointer : {@link Pointer#pointerToAddress(long)}
 *  </li>
 *	<li>Reading / writing a primitive from / to the pointed memory location :<br>
#foreach ($prim in $primitives)
 *		{@link Pointer#get${prim.CapName}()} / {@link Pointer#set${prim.CapName}(${prim.Name})} ; With an offset : {@link Pointer#get${prim.CapName}AtOffset(long)} / {@link Pointer#set${prim.CapName}AtOffset(long, ${prim.Name})}<br>
#end
#foreach ($sizePrim in ["SizeT", "CLong"])
 *		{@link Pointer#get${sizePrim}()} / {@link Pointer#set${sizePrim}(long)} ; With an offset : {@link Pointer#get${sizePrim}AtOffset(long)} / {@link Pointer#set${sizePrim}AtOffset(long, long)} <br>
#end
 *  </li>
 *	<li>Reading / writing an array of primitives from / to the pointed memory location :<br>
#foreach ($prim in $primitives)
 *		{@link Pointer#get${prim.CapName}s(int)} / {@link Pointer#set${prim.CapName}s(${prim.Name}[])} ; With an offset : {@link Pointer#get${prim.CapName}sAtOffset(long, int)} / {@link Pointer#set${prim.CapName}sAtOffset(long, ${prim.Name}[])}<br>
#end
#foreach ($sizePrim in ["SizeT", "CLong"])
 *		{@link Pointer#get${sizePrim}s(int)} / {@link Pointer#set${sizePrim}s(long[])} ; With an offset : {@link Pointer#get${sizePrim}sAtOffset(long, int)} / {@link Pointer#set${sizePrim}sAtOffset(long, long[])}<br>
#end
 *  </li>
 *	<li>Reading / writing an NIO buffer of primitives from / to the pointed memory location :<br>
#foreach ($prim in $primitivesNoBool)
*		{@link Pointer#get${prim.BufferName}(long)} (can be used for writing as well) / {@link Pointer#set${prim.CapName}s(${prim.BufferName})}<br>
#end
 *  </li>
 *  <li>Reading / writing a String from / to the pointed memory location using the default charset :<br>
#foreach ($string in ["C", "WideC"])
*		{@link Pointer#get${string}String()} / {@link Pointer#set${string}String(String)} ; With an offset : {@link Pointer#get${string}StringAtOffset(long)} / {@link Pointer#set${string}StringAtOffset(long, String)}<br>
#end
 *  </li>
 *  <li>Reading / writing a String with control on the charset :<br>
 *		{@link Pointer#getStringAtOffset(long, StringType, Charset)} / {@link Pointer#setStringAtOffset(long, String, StringType, Charset)}<br>
 * </ul>
 * <p>
 * <u><b>Allocating memory</b></u>
 * <p>
 * <ul>
 *	<li>Getting the pointer to a struct / a C++ class / a COM object :
 *		{@link Pointer#pointerTo(NativeObject)}
 *  </li>
 *  <li>Allocating a dynamic callback (without a static {@link Callback} definition, which would be the preferred way) :<br>
 *      {@link Pointer#allocateDynamicCallback(DynamicCallback, org.bridj.ann.Convention.Style, Type, Type[])}
 *  </li>
 *	<li>Allocating a primitive with / without an initial value (zero-initialized) :<br>
#foreach ($prim in $primitives)
 *		{@link Pointer#pointerTo${prim.CapName}(${prim.Name})} / {@link Pointer#allocate${prim.CapName}()}<br>
#end
#foreach ($sizePrim in ["SizeT", "CLong"])
 *		{@link Pointer#pointerTo${sizePrim}(long)} / {@link Pointer#allocate${sizePrim}()}<br>
#end
 *  </li>
 *	<li>Allocating an array of primitives with / without initial values (zero-initialized) :<br>
#foreach ($prim in $primitivesNoBool)
 *		{@link Pointer#pointerTo${prim.CapName}s(${prim.Name}[])} or {@link Pointer#pointerTo${prim.CapName}s(${prim.BufferName})} / {@link Pointer#allocate${prim.CapName}s(long)}<br>
#end
#foreach ($sizePrim in ["SizeT", "CLong"])
 *		{@link Pointer#pointerTo${sizePrim}s(long[])} / {@link Pointer#allocate${sizePrim}s(long)}<br>
#end
 *		{@link Pointer#pointerToBuffer(Buffer)} / n/a<br>
 *  </li>
 *  <li>Allocating a native String :<br>
#foreach ($string in ["C", "WideC"])
*		{@link Pointer#pointerTo${string}String(String) } (default charset)<br>
#end
 *		{@link Pointer#pointerToString(String, StringType, Charset) }<br>
 *  </li>
 *  <li>Allocating a {@link ListType#Dynamic} Java {@link java.util.List} that uses native memory storage  (think of getting back the pointer with {@link NativeList#getPointer()} when you're done mutating the list):<br>
 *		{@link Pointer#allocateList(Class, long) }
 *  </li>
 *  <li>Transforming a pointer to a Java {@link java.util.List} that uses the pointer as storage (think of getting back the pointer with {@link NativeList#getPointer()} when you're done mutating the list, if it's {@link ListType#Dynamic}) :<br>
 *		{@link Pointer#asList(ListType) }<br>
 *		{@link Pointer#asList() }<br>
 *  </li>
 * </ul>
 * <p>
 * <u><b>Casting pointers</b></u>
 * <p>
 * <ul>
 *	<li>Cast a pointer to a {@link DynamicFunction} :<br>
 *		{@link Pointer#asDynamicFunction(org.bridj.ann.Convention.Style, java.lang.reflect.Type, java.lang.reflect.Type[]) }
 *  </li>
 *	<li>Cast a pointer to a {@link StructObject} or a {@link Callback} (as the ones generated by <a href="http://code.google.com/p/jnaerator/">JNAerator</a>) <br>:
 *		{@link Pointer#as(Class) }
 *  </li>
 *	<li>Cast a pointer to a complex type pointer (use {@link org.bridj.cpp.CPPType#getCPPType(Object[])} to create a C++ template type, for instance) :<br>
 *		{@link Pointer#as(Type) }
 *  </li>
 * </ul>
 */
public class Pointer<T> implements Comparable<Pointer<?>>, Iterable<T>
{
	
#macro (docAllocateCopy $cPrimName $primWrapper)
	/**
     * Allocate enough memory for a single $cPrimName value, copy the value provided in argument into it and return a pointer to that memory.<br>
     * The memory will be automatically be freed when the pointer is garbage-collected or upon manual calls to {@link Pointer#release()}.<br>
     * The pointer won't be garbage-collected until all its views are garbage-collected themselves ({@link Pointer#offset(long)}, {@link Pointer#next(long)}, {@link Pointer#next()}).<br>
     * @param value initial value for the created memory location
     * @return pointer to a new memory location that initially contains the $cPrimName value given in argument
     */
#end
#macro (docAllocateArrayCopy $cPrimName $primWrapper)
	/**
     * Allocate enough memory for values.length $cPrimName values, copy the values provided as argument into it and return a pointer to that memory.<br>
     * The memory will be automatically be freed when the pointer is garbage-collected or upon manual calls to {@link Pointer#release()}.<br>
     * The pointer won't be garbage-collected until all its views are garbage-collected themselves ({@link Pointer#offset(long)}, {@link Pointer#next(long)}, {@link Pointer#next()}).<br>
     * The returned pointer is also an {@code Iterable<$primWrapper>} instance that can be safely iterated upon :
     <pre>{@code
     for (float f : pointerTo(1f, 2f, 3.3f))
     	System.out.println(f); }</pre>
     * @param values initial values for the created memory location
     * @return pointer to a new memory location that initially contains the $cPrimName consecutive values provided in argument
     */
#end
#macro (docAllocateArray2DCopy $cPrimName $primWrapper)
    /**
     * Allocate enough memory for all the values in the 2D $cPrimName array, copy the values provided as argument into it as packed multi-dimensional C array and return a pointer to that memory.<br>
     * Assumes that all of the subarrays of the provided array are non null and have the same size.<br>
     * The memory will be automatically be freed when the pointer is garbage-collected or upon manual calls to {@link Pointer#release()}.<br>
     * The pointer won't be garbage-collected until all its views are garbage-collected themselves ({@link Pointer#offset(long)}, {@link Pointer#next(long)}, {@link Pointer#next()}).<br>
     * @param values initial values for the created memory location
     * @return pointer to a new memory location that initially contains the $cPrimName values provided in argument packed as a 2D C array would be
     */
#end
#macro (docAllocateArray3DCopy $cPrimName $primWrapper)
    /**
     * Allocate enough memory for all the values in the 3D $cPrimName array, copy the values provided as argument into it as packed multi-dimensional C array and return a pointer to that memory.<br>
     * Assumes that all of the subarrays of the provided array are non null and have the same size.<br>
     * The memory will be automatically be freed when the pointer is garbage-collected or upon manual calls to {@link Pointer#release()}.<br>
     * The pointer won't be garbage-collected until all its views are garbage-collected themselves ({@link Pointer#offset(long)}, {@link Pointer#next(long)}, {@link Pointer#next()}).<br>
     * @param values initial values for the created memory location
     * @return pointer to a new memory location that initially contains the $cPrimName values provided in argument packed as a 3D C array would be
     */
#end
#macro (docAllocate $cPrimName $primWrapper)
	/**
     * Allocate enough memory for a $cPrimName value and return a pointer to it.<br>
     * The memory will be automatically be freed when the pointer is garbage-collected or upon manual calls to {@link Pointer#release()}.<br>
     * @return pointer to a single zero-initialized $cPrimName value
     */
#end
#macro (docAllocateArray $cPrimName $primWrapper)
	/**
     * Allocate enough memory for arrayLength $cPrimName values and return a pointer to that memory.<br>
     * The memory will be automatically be freed when the pointer is garbage-collected or upon manual calls to {@link Pointer#release()}.<br>
     * The pointer won't be garbage-collected until all its views are garbage-collected themselves ({@link Pointer#offset(long)}, {@link Pointer#next(long)}, {@link Pointer#next()}).<br>
     * The returned pointer is also an {@code Iterable<$primWrapper>} instance that can be safely iterated upon.
     * @return pointer to arrayLength zero-initialized $cPrimName consecutive values
     */
#end
#macro (docAllocateArray2D $cPrimName $primWrapper)
	/**
     * Allocate enough memory for dim1 * dim2 $cPrimName values in a packed multi-dimensional C array and return a pointer to that memory.<br>
     * The memory will be automatically be freed when the pointer is garbage-collected or upon manual calls to {@link Pointer#release()}.<br>
     * The pointer won't be garbage-collected until all its views are garbage-collected themselves ({@link Pointer#offset(long)}, {@link Pointer#next(long)}, {@link Pointer#next()}).<br>
     * @return pointer to dim1 * dim2 zero-initialized $cPrimName consecutive values
     */
#end
#macro (docAllocateArray3D $cPrimName $primWrapper)
	/**
     * Allocate enough memory for dim1 * dim2 * dim3 $cPrimName values in a packed multi-dimensional C array and return a pointer to that memory.<br>
     * The memory will be automatically be freed when the pointer is garbage-collected or upon manual calls to {@link Pointer#release()}.<br>
     * The pointer won't be garbage-collected until all its views are garbage-collected themselves ({@link Pointer#offset(long)}, {@link Pointer#next(long)}, {@link Pointer#next()}).<br>
     * @return pointer to dim1 * dim2 * dim3 zero-initialized $cPrimName consecutive values
     */
#end
#macro (docGet $cPrimName $primWrapper)
	/**
     * Read a $cPrimName value from the pointed memory location
     */
#end
#macro (docGetOffset $cPrimName $primWrapper $signatureWithoutOffset)
	/**
     * Read a $cPrimName value from the pointed memory location shifted by a byte offset
     * @deprecated Avoid using the byte offset methods variants unless you know what you're doing (may cause alignment issues). Please favour {@link $signatureWithoutOffset} over this method. 
	 */
#end
#macro (docGetArray $cPrimName $primWrapper)
	/**
     * Read an array of $cPrimName values of the specified length from the pointed memory location
     */
#end
#macro (docGetRemainingArray $cPrimName $primWrapper)
	/**
     * Read the array of remaining $cPrimName values from the pointed memory location
     */
#end
#macro (docGetArrayOffset $cPrimName $primWrapper $signatureWithoutOffset)
	/**
     * Read an array of $cPrimName values of the specified length from the pointed memory location shifted by a byte offset
     * @deprecated Avoid using the byte offset methods variants unless you know what you're doing (may cause alignment issues). Please favour {@link $signatureWithoutOffset} over this method. 
	 */
#end
#macro (docSet $cPrimName $primWrapper)
	/**
     * Write a $cPrimName value to the pointed memory location
     */
#end
#macro (docSetOffset $cPrimName $primWrapper $signatureWithoutOffset)
    /**
     * Write a $cPrimName value to the pointed memory location shifted by a byte offset
     * @deprecated Avoid using the byte offset methods variants unless you know what you're doing (may cause alignment issues). Please favour {@link $signatureWithoutOffset} over this method. 
	 */
#end
#macro (docSetArray $cPrimName $primWrapper)
	/**
     * Write an array of $cPrimName values to the pointed memory location
     */
#end
#macro (docSetArrayOffset $cPrimName $primWrapper $signatureWithoutOffset)
	/**
     * Write an array of $cPrimName values to the pointed memory location shifted by a byte offset
     * @deprecated Avoid using the byte offset methods variants unless you know what you're doing (may cause alignment issues). Please favour {@link $signatureWithoutOffset} over this method. 
	 */
#end
	
	/** The NULL pointer is <b>always</b> Java's null value */
    public static final Pointer NULL = null;
	
    /** 
     * Size of a pointer in bytes. <br>
     * This is 4 bytes in a 32 bits environment and 8 bytes in a 64 bits environment.<br>
     * Note that some 64 bits environments allow for 32 bits JVM execution (using the -d32 command line argument for Sun's JVM, for instance). In that case, Java programs will believe they're executed in a 32 bits environment. 
     */
    public static final int SIZE = Platform.POINTER_SIZE;
    
	static {
        JNI.initLibrary();
    }
    
    
	private static long UNKNOWN_VALIDITY = -1;
	private static long NO_PARENT = 0/*-1*/;
	
	private final PointerIO<T> io;
	private final long peer, offsetInParent;
	private final Pointer<?> parent;
	private volatile Object sibling;
	private final long validStart, validEnd;
	private final boolean ordered;

	/**
	 * Object responsible for reclamation of some pointed memory when it's not used anymore.
	 */
	public interface Releaser {
		void release(Pointer<?> p);
	}
	
	Pointer(PointerIO<T> io, long peer) {
		this(io, peer, true, UNKNOWN_VALIDITY, UNKNOWN_VALIDITY, null, 0, null);
	}
	Pointer(PointerIO<T> io, long peer, boolean ordered, long validStart, long validEnd, Pointer<?> parent, long offsetInParent, Object sibling) {
		this.io = io;
		this.peer = peer;
		this.ordered = ordered;
		this.validStart = validStart;
		this.validEnd = validEnd;
		this.parent = parent;
		this.offsetInParent = offsetInParent;
		this.sibling = sibling;
		if (BridJ.debugPointers)
			creationTrace = new RuntimeException().fillInStackTrace();
	}
	Throwable creationTrace;
	
	/**
	 * Create a {@code Pointer<T>} type. <br>
	 * For Instance, {@code Pointer.pointerType(Integer.class) } returns a type that represents {@code Pointer<Integer> }  
	 */
	public static Type pointerType(Type targetType) {
		return org.bridj.util.DefaultParameterizedType.paramType(Pointer.class, targetType);	
	}
	/**
	 * Create a {@code IntValuedEnum<T>} type. <br>
	 * For Instance, {@code Pointer.intEnumType(SomeEnum.class) } returns a type that represents {@code IntValuedEnum<SomeEnum> }  
	 */
	public static <E extends Enum<E>> Type intEnumType(Class<? extends IntValuedEnum<E>> targetType) {
		return org.bridj.util.DefaultParameterizedType.paramType(IntValuedEnum.class, targetType);	
	}
	
	/**
	 * Manually release the memory pointed by this pointer if it was allocated on the Java side.<br>
	 * If the pointer is an offset version of another pointer (using {@link Pointer#offset(long)} or {@link Pointer#next(long)}, for instance), this method tries to release the original pointer.<br>
	 * If the memory was not allocated from the Java side, this method does nothing either.<br>
	 * If the memory was already successfully released, this throws a RuntimeException.
	 * @throws RuntimeException if the pointer was already released
	 */
	public synchronized void release() {
		Object sibling = this.sibling;
		this.sibling = null;
		if (sibling instanceof Pointer)
			((Pointer)sibling).release();
	}

	/**
	 * Compare to another pointer based on pointed addresses.
	 * @param p other pointer
	 * @return 1 if this pointer's address is greater than p's (or if p is null), -1 if the opposite is true, 0 if this and p point to the same memory location.
	 */
	//@Override
    public int compareTo(Pointer<?> p) {
		if (p == null)
			return 1;
		
		long p1 = getPeer(), p2 = p.getPeer();
		return p1 == p2 ? 0 : p1 < p2 ? -1 : 1;
	}
	
	/**
	* Compare the byteCount bytes at the memory location pointed by this pointer to the byteCount bytes at the memory location pointer by other using the C @see <a href="http://www.cplusplus.com/reference/clibrary/cstring/memcmp/">memcmp</a> function.<br>
	 * @return 0 if the two memory blocks are equal, -1 if this pointer's memory is "less" than the other and 1 otherwise.
	 */
	public int compareBytes(Pointer<?> other, long byteCount) {
		return compareBytesAtOffset(0, other, 0, byteCount);	
	}
	
	/**
	 * Compare the byteCount bytes at the memory location pointed by this pointer shifted by byteOffset to the byteCount bytes at the memory location pointer by other shifted by otherByteOffset using the C @see <a href="http://www.cplusplus.com/reference/clibrary/cstring/memcmp/">memcmp</a> function.<br>
	 * @deprecated Avoid using the byte offset methods variants unless you know what you're doing (may cause alignment issues)
	 * @return 0 if the two memory blocks are equal, -1 if this pointer's memory is "less" than the other and 1 otherwise.
	 */
	public int compareBytesAtOffset(long byteOffset, Pointer<?> other, long otherByteOffset, long byteCount) {
		return JNI.memcmp(getCheckedPeer(byteOffset, byteCount), other.getCheckedPeer(otherByteOffset, byteCount), byteCount);	
	}
	
    /**
	 * Compute a hash code based on pointed address.
	 */
	@Override
    public int hashCode() {
		int hc = new Long(getPeer()).hashCode();
		return hc;
    }
    
    @Override 
    public String toString() {
    		return "Pointer(peer = 0x" + Long.toHexString(getPeer()) + ", targetType = " + Utils.toString(getTargetType()) + ")";
    }
    
    private final long getCheckedPeer(long byteOffset, long validityCheckLength) {
		long offsetPeer = getPeer() + byteOffset;
		///*
		if (validStart != UNKNOWN_VALIDITY) {
			if (offsetPeer < validStart || (offsetPeer + validityCheckLength) > validEnd)
				throw new IndexOutOfBoundsException("Cannot access to memory data of length " + validityCheckLength + " at offset " + (offsetPeer - getPeer()) + " : valid memory start is " + validStart + ", valid memory size is " + (validEnd - validStart));
		}
		//*/
		return offsetPeer;
    }

    /**
	 * Returns a pointer which address value was obtained by this pointer's by adding a byte offset.<br>
	 * The returned pointer will prevent the memory associated to this pointer from being automatically reclaimed as long as it lives, unless Pointer.release() is called on the originally-allocated pointer.
	 * @param byteOffset offset in bytes of the new pointer vs. this pointer. The expression {@code p.offset(byteOffset).getPeer() - p.getPeer() == byteOffset} is always true.
	 */
    public Pointer<T> offset(long byteOffset) {
    	return offset(byteOffset, getIO());
    }

    <U> Pointer<U> offset(long byteOffset, PointerIO<U> pio) {
		if (byteOffset == 0)
			return pio == this.io ? (Pointer<U>)this : as(pio);
		
		long newPeer = getPeer() + byteOffset;
		
		Object newSibling = getSibling() != null ? getSibling() : this;
		if (validStart == UNKNOWN_VALIDITY)
			return newPointer(pio, newPeer, ordered, UNKNOWN_VALIDITY, UNKNOWN_VALIDITY, null, NO_PARENT, null, newSibling);	
		if (newPeer > validEnd || newPeer < validStart)
			throw new IndexOutOfBoundsException("Invalid pointer offset : " + byteOffset + " (validBytes = " + getValidBytes() + ") !");
		
		return newPointer(pio, newPeer, ordered, validStart, validEnd, null, NO_PARENT, null, newSibling);	
	}
	
	/**
	 * Creates a pointer that has the given number of valid bytes ahead.<br>
	 * If the pointer was already bound, the valid bytes must be lower or equal to the current getValidBytes() value.
	 */
	public Pointer<T> validBytes(long byteCount) {
		long peer = getPeer();
		long newValidEnd = peer + byteCount;
		if (validStart == 0 && validEnd == newValidEnd)
			return this;
		
		if (validEnd != UNKNOWN_VALIDITY && newValidEnd > validEnd)
			throw new IndexOutOfBoundsException("Cannot extend validity of pointed memory from " + validEnd + " to " + newValidEnd);
		
		Object newSibling = getSibling() != null ? getSibling() : this;
		return newPointer(getIO(), peer, ordered, peer, newValidEnd, parent, offsetInParent, null, newSibling);    	
	}
	
	/**
	* Creates a copy of the pointed memory location (allocates a new area of memory) and returns a pointer to it.<br>
	* The pointer's bounds must be known (see {@link Pointer#getValidBytes()}, {@link Pointer#validBytes(long)} or {@link Pointer#validElements(long)}).
	 */
	public Pointer<T> clone() {
		long length = getValidElements();
		if (length < 0)
			throw new UnsupportedOperationException("Number of bytes unknown, unable to clone memory (use validBytes(long))");
		
		Pointer<T> c = allocateArray(getIO(), length);
		copyTo(c);
		return c;    	
	}
	
	/**
	 * Creates a pointer that has the given number of valid elements ahead.<br>
	 * If the pointer was already bound, the valid bytes must be lower or equal to the current getValidElements() value.
	 */
	public Pointer<T> validElements(long elementCount) {
		return validBytes(elementCount * getIO("Cannot define elements validity").getTargetSize());
    }   
	
	/**
	 * Returns a pointer to this pointer.<br>
	 * It will only succeed if this pointer was dereferenced from another pointer.<br>
	 * Let's take the following C++ code :
	 * <pre>{@code
	int** pp = ...;
	int* p = pp[10];
	int** ref = &p;
	ASSERT(pp == ref);
	 }</pre>
	 * Here is its equivalent Java code :
	 * <pre>{@code
	Pointer<Pointer<Integer>> pp = ...;
	Pointer<Integer> p = pp.get(10);
	Pointer<Pointer<Integer>> ref = p.getReference();
	assert pp.equals(ref);
	 }</pre>
	 */
    public Pointer<Pointer<T>> getReference() {
		if (parent == null)
			throw new UnsupportedOperationException("Cannot get reference to this pointer, it wasn't created from Pointer.getPointer(offset) or from a similar method.");
		
		PointerIO io = getIO();
		return parent.offset(offsetInParent).as(io == null ? null : io.getReferenceIO());
	}
	
	/**
	 * Get the address of the memory pointed to by this pointer ("cast this pointer to long", in C jargon).<br>
	 * This is equivalent to the C code {@code (size_t)&pointer}
	 * @return Address of the memory pointed to by this pointer
	 */
	public final long getPeer() {
		return peer;
	}
    
	/**
	 * Create a native callback which signature corresponds to the provided calling convention, return type and parameter types, and which redirects calls to the provided Java {@link org.bridj.DynamicCallback} handler.<br/>
	 * For instance, a callback of C signature <code>double (*)(float, int)</code> that adds its two arguments can be created with :<br>
     * <code>{@code 
     * Pointer callback = Pointer.allocateDynamicCallback(
	 *	  new DynamicCallback<Integer>() {
	 *	      public Double apply(Object... args) {
	 *	          float a = (Float)args[0];
	 *	          int b = (Integer)args[1];
	 *	          return (double)(a + b);
	 *	      }
	 *	  }, 
	 *    null, // Use the platform's default calling convention
	 *    int.class, // return type
	 *    float.class, double.class // parameter types
	 * );
     * }</code><br>
     * For the <code>void</code> return type, you can use {@link java.lang.Void} :<br>
     * <code>{@code 
     * Pointer callback = Pointer.allocateDynamicCallback(
	 *	  new DynamicCallback<Void>() {
	 *	      public Void apply(Object... args) {
	 *	          ...
	 *	          return null; // Void cannot be instantiated anyway ;-)
	 *	      }
	 *	  }, 
	 *    null, // Use the platform's default calling convention
	 *    int.class, // return type
	 *    float.class, double.class // parameter types
	 * );
     * }</code><br>
	 * @return Pointer to a native callback that redirects calls to the provided Java callback instance, and that will be destroyed whenever the pointer is released (make sure you keep a reference to it !)
	 */
	public static <R> Pointer<DynamicFunction<R>> allocateDynamicCallback(DynamicCallback<R> callback, org.bridj.ann.Convention.Style callingConvention, Type returnType, Type... parameterTypes) {
		if (callback == null)
			throw new IllegalArgumentException("Java callback handler cannot be null !");
		if (returnType == null)
			throw new IllegalArgumentException("Callback return type cannot be null !");
		if (parameterTypes == null)
			throw new IllegalArgumentException("Invalid (null) list of parameter types !");
		try {
			MethodCallInfo mci = new MethodCallInfo(returnType, parameterTypes, false);
			Method method = DynamicCallback.class.getMethod("apply", Object[].class);
			mci.setMethod(method);
			mci.setJavaSignature("([Ljava/lang/Object;)Ljava/lang/Object;");
			mci.setCallingConvention(callingConvention);
			mci.setGenericCallback(true);
			mci.setJavaCallback(callback);
			
			//System.out.println("Java sig
			
			return CRuntime.createCToJavaCallback(mci, DynamicCallback.class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to allocate dynamic callback for convention " + callingConvention + ", return type " + Utils.toString(returnType) + " and parameter types " + Arrays.asList(parameterTypes) + " : " + ex, ex);
		}
	}
    
    /**
     * Cast this pointer to another pointer type
     * @param newIO
     */
    public <U> Pointer<U> as(PointerIO<U> newIO) {
    	return viewAs(isOrdered(), newIO);
    }
    /**
     * Create a view of this pointer that has the byte order provided in argument, or return this if this pointer already uses the requested byte order.
     * @param order byte order (endianness) of the returned pointer
     */
    public Pointer<T> order(ByteOrder order) {
		if (order.equals(ByteOrder.nativeOrder()) == isOrdered())
			return this;
		
		return viewAs(!isOrdered(), getIO());
	}
    
	/**
     * Get the byte order (endianness) of this pointer.
     */
    public ByteOrder order() {
		return isOrdered() ? ByteOrder.nativeOrder() : ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }

    <U> Pointer<U> viewAs(boolean ordered, PointerIO<U> newIO) {
    	if (newIO == io && ordered == isOrdered())
    		return (Pointer<U>)this;
    	else
    		return newPointer(newIO, getPeer(), ordered, getValidStart(), getValidEnd(), getParent(), getOffsetInParent(), null, getSibling() != null ? getSibling() : this);
    }

    /**
     * Get the PointerIO instance used by this pointer to get and set pointed values.
     */
    public final PointerIO<T> getIO() {
		return io;
	}
    
	/**
     * Whether this pointer reads data in the system's native byte order or not.
     * See {@link Pointer#order()}, {@link Pointer#order(ByteOrder)}
     */
    final boolean isOrdered() {
    	return ordered;
    }
    
    final long getOffsetInParent() {
		return offsetInParent;
	}
    final Pointer<?> getParent() {
		return parent;
	}
    final Object getSibling() {
		return sibling;
	}
    
    final long getValidEnd() {
		return validEnd;
	}
    final long getValidStart() {
		return validStart;
	}

    /**
     * Cast this pointer to another pointer type<br>
     * Synonym of {@link Pointer#as(Class)}<br>
     * The following C code :<br>
     * <code>{@code 
     * T* pointerT = ...;
     * U* pointerU = (U*)pointerT;
     * }</code><br>
     * Can be translated to the following Java code :<br>
     * <code>{@code 
     * Pointer<T> pointerT = ...;
     * Pointer<U> pointerU = pointerT.as(U.class);
     * }</code><br>
     * @param <U> type of the elements pointed by the returned pointer
     * @param type type of the elements pointed by the returned pointer
     * @return pointer to type U elements at the same address as this pointer
     */
    public <U> Pointer<U> as(Type type) {
    	PointerIO<U> pio = PointerIO.getInstance(type);
    	return as(pio);
    }

    /**
     * Cast this pointer to another pointer type.<br>
     * Synonym of {@link Pointer#as(Type)}<br>
     * The following C code :<br>
     * <code>{@code 
     * T* pointerT = ...;
     * U* pointerU = (U*)pointerT;
     * }</code><br>
     * Can be translated to the following Java code :<br>
     * <code>{@code 
     * Pointer<T> pointerT = ...;
     * Pointer<U> pointerU = pointerT.as(U.class); // or pointerT.as(U.class);
     * }</code><br>
     * @param <U> type of the elements pointed by the returned pointer
     * @param type type of the elements pointed by the returned pointer
     * @return pointer to type U elements at the same address as this pointer
     */
    public <U> Pointer<U> as(Class<U> type) {
    	return as((Type)type);
    }
    
    /**
     * Cast this pointer as a function pointer to a function that returns the specified return type and takes the specified parameter types.<br>
     * See for instance the following C code that uses a function pointer :
     * <pre>{@code
     *	  double (*ptr)(int, const char*) = someAddress;
     *    double result = ptr(10, "hello");
     * }</pre>
     * Its Java equivalent with BridJ is the following :
     * <pre>{@code
     *	  DynamicFunction ptr = someAddress.asDynamicFunction(null, double.class, int.class, Pointer.class);
     *    double result = (Double)ptr.apply(10, pointerToCString("hello"));
     * }</pre>
     * Also see {@link CRuntime#getDynamicFunctionFactory(org.bridj.NativeLibrary, org.bridj.ann.Convention.Style, java.lang.reflect.Type, java.lang.reflect.Type[])  } for more options.
     * @param callingConvention calling convention used by the function (if null, default is typically {@link org.bridj.ann.Convention.Style#CDecl})
     * @param returnType return type of the function
     * @param parameterTypes parameter types of the function
     */
    public <R> DynamicFunction<R> asDynamicFunction(org.bridj.ann.Convention.Style callingConvention, Type returnType, Type... parameterTypes) {
    		return CRuntime.getInstance().getDynamicFunctionFactory(null, callingConvention, returnType, parameterTypes).newInstance(this);
    }
    
    /**
     * Cast this pointer to an untyped pointer.<br>
     * Synonym of {@code ptr.as((Class<?>)null)}.<br>
     * See {@link Pointer#as(Class)}<br>
     * The following C code :<br>
     * <code>{@code 
     * T* pointerT = ...;
     * void* pointer = (void*)pointerT;
     * }</code><br>
     * Can be translated to the following Java code :<br>
     * <code>{@code 
     * Pointer<T> pointerT = ...;
     * Pointer<?> pointer = pointerT.asUntyped(); // or pointerT.as((Class<?>)null);
     * }</code><br>
     * @return untyped pointer pointing to the same address as this pointer
     */
    public Pointer<?> asUntyped() {
    	return as((Class<?>)null);
    }

    /**
     * Get the amount of memory known to be valid from this pointer, or -1 if it is unknown.<br>
     * Memory validity information is available when the pointer was allocated by BridJ (with {@link Pointer#allocateBytes(long)}, for instance), created out of another pointer which memory validity information is available (with {@link Pointer#offset(long)}, {@link Pointer#next()}, {@link Pointer#next(long)}) or created from a direct NIO buffer ({@link Pointer#pointerToBuffer(Buffer)}, {@link Pointer#pointerToInts(IntBuffer)}...)
     * @return amount of bytes that can be safely read or written from this pointer, or -1 if this amount is unknown
     */
    public long getValidBytes() {
    	long ve = getValidEnd();
    	return ve == UNKNOWN_VALIDITY ? -1 : ve - getPeer();
    }
    
    /**
    * Get the amount of memory known to be valid from this pointer (expressed in elements of the target type, see {@link Pointer#getTargetType()}) or -1 if it is unknown.<br>
     * Memory validity information is available when the pointer was allocated by BridJ (with {@link Pointer#allocateBytes(long)}, for instance), created out of another pointer which memory validity information is available (with {@link Pointer#offset(long)}, {@link Pointer#next()}, {@link Pointer#next(long)}) or created from a direct NIO buffer ({@link Pointer#pointerToBuffer(Buffer)}, {@link Pointer#pointerToInts(IntBuffer)}...)
     * @return amount of elements that can be safely read or written from this pointer, or -1 if this amount is unknown
     */
    public long getValidElements() {
    	long bytes = getValidBytes();
    	long elementSize = getTargetSize();
    	if (bytes < 0 || elementSize <= 0)
    		return -1;
    	return bytes / elementSize;
    }
    
    /**
     * Returns an iterator over the elements pointed by this pointer.<br>
     * If this pointer was allocated from Java with the allocateXXX, pointerToXXX methods (or is a view or a clone of such a pointer), the iteration is safely bounded.<br>
     * If this iterator is just a wrapper for a native-allocated pointer (or a view / clone of such a pointer), iteration will go forever (until illegal areas of memory are reached and cause a JVM crash).
     */
    public ListIterator<T> iterator() {
    	return new ListIterator<T>() {
    		Pointer<T> next = Pointer.this.getValidElements() != 0 ? Pointer.this : null;
    		Pointer<T> previous;
    		//@Override
			public T next() {
				if (next == null)
					throw new NoSuchElementException();
                T value = next.get();
                previous = next;
                long valid = next.getValidElements();
				next = valid < 0 || valid > 1 ? next.next(1) : null;
				return value;
			}
			//@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			//@Override
			public boolean hasNext() {
				long rem;
				return next != null && ((rem = next.getValidBytes()) < 0 || rem > 0);
			}
			//@Override
			public void add(T o) {
				throw new UnsupportedOperationException();
			}
			//@Override
			public boolean hasPrevious() {
				return previous != null;
			}
			//@Override
			public int nextIndex() {
				throw new UnsupportedOperationException();
			}
			//@Override
			public T previous() {
				//TODO return previous;
				throw new UnsupportedOperationException();
			}
			//@Override
			public int previousIndex() {
				throw new UnsupportedOperationException();
			}
			//@Override
			public void set(T o) {
				if (previous == null)
					throw new NoSuchElementException("You haven't called next() prior to calling ListIterator.set(E)");
				previous.set(o);
			} 
    	};
    }
    
    
    /**
     * Get a pointer to a native object (C++ or ObjectiveC class, struct, union, callback...) 
     */
    public static <N extends NativeObject> Pointer<N> pointerTo(N instance) {
    		return pointerTo(instance, null);
    }
    /**
     * Get a pointer to a native object, specifying the type of the pointer's target.<br>
     * In C++, the address of the pointer to an object as its canonical class is not always the same as the address of the pointer to the same object cast to one of its parent classes. 
     */
    public static <R extends NativeObject> Pointer<R> pointerTo(NativeObject instance, Type targetType) {
		return instance == null ? null : (Pointer<R>)instance.peer;
    }
    /**
    * Get the address of a native object, specifying the type of the pointer's target (same as {@code pointerTo(instance, targetType).getPeer()}, see {@link Pointer#pointerTo(NativeObject, Class)}).<br>
     * In C++, the address of the pointer to an object as its canonical class is not always the same as the address of the pointer to the same object cast to one of its parent classes. 
     */
    public static long getAddress(NativeObject instance, Class targetType) {
		return getPeer(pointerTo(instance, targetType));
    }
    
#docGetOffset("native object", "O extends NativeObject", "Pointer#getNativeObject(Type)")
	public <O extends NativeObject> O getNativeObjectAtOffset(long byteOffset, Type type) {
		return (O)BridJ.createNativeObjectFromPointer((Pointer<O>)(byteOffset == 0 ? this : offset(byteOffset)), type);
	}
#docSet("native object", "O extends NativeObject")
	public <O extends NativeObject> Pointer<T> setNativeObject(O value, Type type) {
		BridJ.copyNativeObjectToAddress(value, type, (Pointer)this);
		return this;
	}
#docGetOffset("native object", "O extends NativeObject", "Pointer#getNativeObject(Class)")
	 public <O extends NativeObject> O getNativeObjectAtOffset(long byteOffset, Class<O> type) {
		return (O)getNativeObjectAtOffset(byteOffset, (Type)type);
	}
#docGet("native object", "O extends NativeObject")
    public <O extends NativeObject> O getNativeObject(Class<O> type) {
		return getNativeObjectAtOffset(0, type);
	}
#docGet("native object", "O extends NativeObject")
    public <O extends NativeObject> O getNativeObject(Type type) {
		O o = (O)getNativeObjectAtOffset(0, type);
		return o;
	}
	
	/**
	 * Check that the pointer's peer is aligned to the target type alignment.
	 * @throws RuntimeException If the target type of this pointer is unknown
	 * @return getPeer() % alignment == 0
	 */
	public boolean isAligned() {
        return isAligned(getIO("Cannot check alignment").getTargetAlignment());
	}
	
	/**
	 * Check that the pointer's peer is aligned to the given alignment.
	 * If the pointer has no peer, this method returns true.
	 * @return getPeer() % alignment == 0
	 */
	public boolean isAligned(long alignment) {
		return isAligned(getPeer(), alignment);
	}
	
	/**
	 * Check that the provided address is aligned to the given alignment.
	 * @return address % alignment == 0
	 */
	protected static boolean isAligned(long address, long alignment) {
		switch ((int)alignment) {
		case 1:
			return true;
		case 2:
			return (address & 1) == 0;
		case 4:
			return (address & 3) == 0;
		case 8:
			return (address & 7) == 0;
		case 16:
			return (address & 15) == 0;
		case 32:
			return (address & 31) == 0;
		case 64:
			return (address & 63) == 0;
		default:
			return (address % alignment) == 0;
		}
	}
	
	/**
	 * Dereference this pointer (*ptr).<br>
     Take the following C++ code fragment :
     <pre>{@code
     int* array = new int[10];
     for (int index = 0; index < 10; index++, array++) 
     	printf("%i\n", *array);
     }</pre>
     Here is its equivalent in Java :
     <pre>{@code
     import static org.bridj.Pointer.*;
     ...
     Pointer<Integer> array = allocateInts(10);
     for (int index = 0; index < 10; index++) { 
     	System.out.println("%i\n".format(array.get()));
     	array = array.next();
	 }
     }</pre>
     Here is a simpler equivalent in Java :
     <pre>{@code
     import static org.bridj.Pointer.*;
     ...
     Pointer<Integer> array = allocateInts(10);
     for (int value : array) // array knows its size, so we can iterate on it
     	System.out.println("%i\n".format(value));
     }</pre>
     @throws RuntimeException if called on an untyped {@code Pointer<?>} instance (see {@link  Pointer#getTargetType()}) 
	 */
    public T get() {
        return get(0);
    }
    
    /**
     Gets the n-th element from this pointer.<br>
     This is equivalent to the C/C++ square bracket syntax.<br>
     Take the following C++ code fragment :
     <pre>{@code
	int* array = new int[10];
	int index = 5;
	int value = array[index];
     }</pre>
     Here is its equivalent in Java :
     <pre>{@code
	import static org.bridj.Pointer.*;
	...
	Pointer<Integer> array = allocateInts(10);
	int index = 5;
	int value = array.get(index);
     }</pre>
     @param index offset in pointed elements at which the value should be copied. Can be negative if the pointer was offset and the memory before it is valid.
     @throws RuntimeException if called on an untyped {@code Pointer<?>} instance ({@link  Pointer#getTargetType()}) 
	 */
	public T get(long index) {
        return getIO("Cannot get pointed value").get(this, index);
    }
    
    /**
	 Assign a value to the pointed memory location, and return it (different behaviour from {@link List\#set(int, Object)} which returns the old value of that element !!!).<br>
     Take the following C++ code fragment :
     <pre>{@code
	int* array = new int[10];
	for (int index = 0; index < 10; index++, array++) { 
		int value = index;
		*array = value;
	}
     }</pre>
     Here is its equivalent in Java :
     <pre>{@code
	import static org.bridj.Pointer.*;
	...
	Pointer<Integer> array = allocateInts(10);
	for (int index = 0; index < 10; index++) {
		int value = index;
		array.set(value);
		array = array.next();
	}
     }</pre>
     @throws RuntimeException if called on a raw and untyped {@code Pointer} instance (see {@link Pointer#asUntyped()} and {@link  Pointer#getTargetType()}) 
	 @return The value that was given (not the old value as in {@link List\#set(int, Object)} !!!)
	 */
    public T set(T value) {
        return set(0, value);
    }
    
    static void throwBecauseUntyped(String message) {
    	throw new RuntimeException("Pointer is not typed (call Pointer.as(Type) to create a typed pointer) : " + message);
    }
    static void throwUnexpected(Throwable ex) {
    	throw new RuntimeException("Unexpected error", ex);
    }
	/**
     Sets the n-th element from this pointer, and return it (different behaviour from {@link List\#set(int, Object)} which returns the old value of that element !!!).<br>
     This is equivalent to the C/C++ square bracket assignment syntax.<br>
     Take the following C++ code fragment :
     <pre>{@code
     float* array = new float[10];
     int index = 5;
     float value = 12;
     array[index] = value;
     }</pre>
     Here is its equivalent in Java :
     <pre>{@code
     import static org.bridj.Pointer.*;
     ...
     Pointer<Float> array = allocateFloats(10);
     int index = 5;
     float value = 12;
     array.set(index, value);
     }</pre>
     @param index offset in pointed elements at which the value should be copied. Can be negative if the pointer was offset and the memory before it is valid.
     @param value value to set at pointed memory location
     @throws RuntimeException if called on a raw and untyped {@code Pointer} instance (see {@link Pointer#asUntyped()} and {@link  Pointer#getTargetType()})
     @return The value that was given (not the old value as in {@link List\#set(int, Object)} !!!)
	 */
	public T set(long index, T value) {
        getIO("Cannot set pointed value").set(this, index, value);
        return value;
    }
	
    /**
     * Get a pointer's peer (see {@link Pointer#getPeer}), or zero if the pointer is null.
     */
	public static long getPeer(Pointer<?> pointer) {
        return pointer == null ? 0 : pointer.getPeer();
    }
	
    /**
     * Get the unitary size of the pointed elements in bytes.
     * @throws RuntimeException if the target type is unknown (see {@link Pointer#getTargetType()})
     */
	public long getTargetSize() {
        return getIO("Cannot compute target size").getTargetSize();
	}
	
	/**
	 * Returns a pointer to the next target.
	 * Same as incrementing a C pointer of delta elements, but creates a new pointer instance.
	 * @return next(1)
	 */
	public Pointer<T> next() {
		return next(1);
	}
	
	/**
	 * Returns a pointer to the n-th next (or previous) target.
	 * Same as incrementing a C pointer of delta elements, but creates a new pointer instance.
	 * @return offset(getTargetSize() * delta)
	 */
	public Pointer<T> next(long delta) {
        return offset(getIO("Cannot get pointers to next or previous targets").getTargetSize() * delta);
	}
	
	/**
     * Release pointers, if they're not null (see {@link Pointer#release}).
     */
	public static void release(Pointer... pointers) {
    		for (Pointer pointer : pointers)
    			if (pointer != null)
    				pointer.release();
	}

    /**
	 * Test equality of the pointer using the address.<br>
	 * @return true if and only if obj is a Pointer instance and {@code obj.getPeer() == this.getPeer() }
	 */
	@Override
    public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Pointer))
			return false;
		
		Pointer p = (Pointer)obj;
		return getPeer() == p.getPeer();
	}
	
	/**
     * Create a pointer out of a native memory address
     * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == address }
     */
    @Deprecated
    public static Pointer<?> pointerToAddress(long peer) {
        return newPointer(null, peer, true, UNKNOWN_VALIDITY, UNKNOWN_VALIDITY, null, NO_PARENT, null, null);
    }

    /**
     * Create a pointer out of a native memory address
     * @param size number of bytes known to be readable at the pointed address 
	 * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == peer }
     */
    @Deprecated
    public static Pointer<?> pointerToAddress(long peer, long size) {
        return newPointer(null, peer, true, peer, peer + size, null, NO_PARENT, null, null);
    }
    
    /**
     * Create a pointer out of a native memory address
     * @param targetClass type of the elements pointed by the resulting pointer 
	 * @param releaser object responsible for reclaiming the native memory once whenever the returned pointer is garbage-collected 
	 * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == peer }
     */
    public static <P> Pointer<P> pointerToAddress(long peer, Class<P> targetClass, final Releaser releaser) {
        return pointerToAddress(peer, (Type)targetClass, releaser);
    }
    /**
     * Create a pointer out of a native memory address
     * @param targetType type of the elements pointed by the resulting pointer 
	 * @param releaser object responsible for reclaiming the native memory once whenever the returned pointer is garbage-collected 
	 * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == peer }
     */
    public static <P> Pointer<P> pointerToAddress(long peer, Type targetType, final Releaser releaser) {
    		PointerIO<P> pio = PointerIO.getInstance(targetType);
        return newPointer(pio, peer, true, UNKNOWN_VALIDITY, UNKNOWN_VALIDITY, null, -1, releaser, null);
    }
    /**
     * Create a pointer out of a native memory address
     * @param io PointerIO instance that knows how to read the elements pointed by the resulting pointer 
	 * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == peer }
     */
    static <P> Pointer<P> pointerToAddress(long peer, PointerIO<P> io) {
    	return newPointer(io, peer, true, UNKNOWN_VALIDITY, UNKNOWN_VALIDITY, null, NO_PARENT, null, null);
	}
	/**
     * Create a pointer out of a native memory address
     * @param io PointerIO instance that knows how to read the elements pointed by the resulting pointer 
	 * @param releaser object responsible for reclaiming the native memory once whenever the returned pointer is garbage-collected 
	 * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == peer }
     */
    static <P> Pointer<P> pointerToAddress(long peer, PointerIO<P> io, Releaser releaser) {
    	return newPointer(io, peer, true, UNKNOWN_VALIDITY, UNKNOWN_VALIDITY, null, NO_PARENT, releaser, null);
	}
	
	/**
     * Create a pointer out of a native memory address
     * @param releaser object responsible for reclaiming the native memory once whenever the returned pointer is garbage-collected 
	 * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == peer }
     */
    @Deprecated
    public static Pointer<?> pointerToAddress(long peer, Releaser releaser) {
		return newPointer(null, peer, true, UNKNOWN_VALIDITY, UNKNOWN_VALIDITY, null, NO_PARENT, releaser, null);
	}
    
	/**
     * Create a pointer out of a native memory address
     * @param releaser object responsible for reclaiming the native memory once whenever the returned pointer is garbage-collected 
	 * @param size number of bytes known to be readable at the pointed address 
	 * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == peer }
     */
    public static Pointer<?> pointerToAddress(long peer, long size, Releaser releaser) {
        return newPointer(null, peer, true, peer, peer + size, null, NO_PARENT, releaser, null);
    }
	
	/**
     * Create a pointer out of a native memory address
     * @param targetClass type of the elements pointed by the resulting pointer 
	 * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == peer }
     */
    @Deprecated
    public static <P> Pointer<P> pointerToAddress(long peer, Class<P> targetClass) {
    		return pointerToAddress(peer, (Type)targetClass);
    }
    
	/**
     * Create a pointer out of a native memory address
     * @param targetType type of the elements pointed by the resulting pointer 
	 * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == peer }
     */
    @Deprecated
    public static <P> Pointer<P> pointerToAddress(long peer, Type targetType) {
    	return newPointer((PointerIO<P>)PointerIO.getInstance(targetType), peer, true, UNKNOWN_VALIDITY, UNKNOWN_VALIDITY, null, -1, null, null);
    }
    
	/**
     * Create a pointer out of a native memory address
     * @param size number of bytes known to be readable at the pointed address 
	 * @param io PointerIO instance that knows how to read the elements pointed by the resulting pointer 
	 * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == peer }
     */
    static <U> Pointer<U> pointerToAddress(long peer, long size, PointerIO<U> io) {
    	return newPointer(io, peer, true, peer, peer + size, null, NO_PARENT, null, null);
	}
	
	/**
     * Create a pointer out of a native memory address
     * @param releaser object responsible for reclaiming the native memory once whenever the returned pointer is garbage-collected 
	 * @param peer native memory address that is to be converted to a pointer
	 * @return a pointer with the provided address : {@code pointer.getPeer() == peer }
     */
    static <U> Pointer<U> newPointer(
		PointerIO<U> io, 
		long peer, 
		boolean ordered, 
		long validStart, 
		long validEnd, 
		Pointer<?> parent, 
		long offsetInParent, 
		final Releaser releaser,
		Object sibling)
	{
		if (peer == 0)
			return null;
		
		if (validEnd != UNKNOWN_VALIDITY) {
			long size = validEnd - validStart;
			if (size <= 0)
				return null;
		}
		
		if (releaser == null)
			return new Pointer<U>(io, peer, ordered, validStart, validEnd, parent, offsetInParent, sibling);
		else {
			assert sibling == null;
			return new Pointer<U>(io, peer, ordered, validStart, validEnd, parent, offsetInParent, sibling) {
				private volatile Releaser rel = releaser;
				//@Override
				public synchronized void release() {
					if (rel != null) {
						Releaser rel = this.rel;
						this.rel = null;
						rel.release(this);
					}
				}
				protected void finalize() {
					release();
				}
				
				@Deprecated
				public synchronized Pointer<U> withReleaser(final Releaser beforeDeallocation) {
					final Releaser thisReleaser = rel;
					rel = null;
					return newPointer(getIO(), getPeer(), isOrdered(), getValidStart(), getValidEnd(), null, NO_PARENT, beforeDeallocation == null ? thisReleaser : new Releaser() {
						//@Override
						public void release(Pointer<?> p) {
							beforeDeallocation.release(p);
							if (thisReleaser != null)
								thisReleaser.release(p);
						}
					}, null);
				}
			};
		}
    }
	
#docAllocate("typed pointer", "P extends TypedPointer")
    public static <P extends TypedPointer> Pointer<P> allocateTypedPointer(Class<P> type) {
    	return (Pointer<P>)(Pointer)allocate(PointerIO.getInstance(type));
    }
#docAllocateArray("typed pointer", "P extends TypedPointer")
    public static <P extends TypedPointer> Pointer<P> allocateTypedPointers(Class<P> type, long arrayLength) {
    	return (Pointer<P>)(Pointer)allocateArray(PointerIO.getInstance(type), arrayLength);
    }
    /**
     * Create a memory area large enough to hold a pointer.
     * @param targetType target type of the pointer values to be stored in the allocated memory 
     * @return a pointer to a new memory area large enough to hold a single typed pointer
     */
    public static <P> Pointer<Pointer<P>> allocatePointer(Class<P> targetType) {
    	return (Pointer<Pointer<P>>)(Pointer)allocate(PointerIO.getPointerInstance(targetType)); 
    }
    /**
     * Create a memory area large enough to hold a pointer.
     * @param targetType target type of the pointer values to be stored in the allocated memory 
     * @return a pointer to a new memory area large enough to hold a single typed pointer
     */
    public static <P> Pointer<Pointer<P>> allocatePointer(Type targetType) {
    	return (Pointer<Pointer<P>>)(Pointer)allocate(PointerIO.getPointerInstance(targetType)); 
    }
    /**
     * Create a memory area large enough to hold a pointer to a pointer
     * @param targetType target type of the values pointed by the pointer values to be stored in the allocated memory 
     * @return a pointer to a new memory area large enough to hold a single typed pointer
     */
    public static <P> Pointer<Pointer<Pointer<P>>> allocatePointerPointer(Type targetType) {
    	return allocatePointer(pointerType(targetType)); 
    }/**
     * Create a memory area large enough to hold a pointer to a pointer
     * @param targetType target type of the values pointed by the pointer values to be stored in the allocated memory 
     * @return a pointer to a new memory area large enough to hold a single typed pointer
     */
    public static <P> Pointer<Pointer<Pointer<P>>> allocatePointerPointer(Class<P> targetType) {
    	return allocatePointerPointer(targetType); 
    }
#docAllocate("untyped pointer", "Pointer<?>")
    /**
     * Create a memory area large enough to hold an untyped pointer.
     * @return a pointer to a new memory area large enough to hold a single untyped pointer
     */
    public static <V> Pointer<Pointer<?>> allocatePointer() {
    	return (Pointer)allocate(PointerIO.getPointerInstance());
    }
#docAllocateArray("untyped pointer", "Pointer<?>")
    public static Pointer<Pointer<?>> allocatePointers(int arrayLength) {
		return (Pointer<Pointer<?>>)(Pointer)allocateArray(PointerIO.getPointerInstance(), arrayLength); 
	}
	
    /**
     * Create a memory area large enough to hold an array of arrayLength typed pointers.
     * @param targetType target type of element pointers in the resulting pointer array. 
     * @param arrayLength size of the allocated array, in elements
     * @return a pointer to a new memory area large enough to hold an array of arrayLength typed pointers
     */
    public static <P> Pointer<Pointer<P>> allocatePointers(Class<P> targetType, int arrayLength) {
		return (Pointer<Pointer<P>>)(Pointer)allocateArray(PointerIO.getPointerInstance(targetType), arrayLength); // TODO 
	}
	
    /**
     * Create a memory area large enough to hold an array of arrayLength typed pointers.
     * @param targetType target type of element pointers in the resulting pointer array. 
     * @param arrayLength size of the allocated array, in elements
     * @return a pointer to a new memory area large enough to hold an array of arrayLength typed pointers
     */
    public static <P> Pointer<Pointer<P>> allocatePointers(Type targetType, int arrayLength) {
		return (Pointer<Pointer<P>>)(Pointer)allocateArray(PointerIO.getPointerInstance(targetType), arrayLength); // TODO 
	}
	
    
    /**
     * Create a memory area large enough to a single items of type elementClass.
     * @param elementClass type of the array elements
     * @return a pointer to a new memory area large enough to hold a single item of type elementClass.
     */
    public static <V> Pointer<V> allocate(Class<V> elementClass) {
        return allocate((Type)elementClass);
    }

    /**
     * Create a memory area large enough to a single items of type elementClass.
     * @param elementClass type of the array elements
     * @return a pointer to a new memory area large enough to hold a single item of type elementClass.
     */
    public static <V> Pointer<V> allocate(Type elementClass) {
        return allocateArray(elementClass, 1);
    }

    /**
     * Create a memory area large enough to hold one item of the type associated to the provided PointerIO instance (see {@link PointerIO#getTargetType()})
     * @param io PointerIO instance able to store and retrieve the element
     * @return a pointer to a new memory area large enough to hold one item of the type associated to the provided PointerIO instance (see {@link PointerIO#getTargetType()})
     */
    public static <V> Pointer<V> allocate(PointerIO<V> io) {
    	long targetSize = io.getTargetSize();
    	if (targetSize < 0)
    		throwBecauseUntyped("Cannot allocate array ");
		return allocateBytes(io, targetSize, null);
    }
    /**
     * Create a memory area large enough to hold arrayLength items of the type associated to the provided PointerIO instance (see {@link PointerIO#getTargetType()})
     * @param io PointerIO instance able to store and retrieve elements of the array
     * @param arrayLength length of the array in elements
     * @return a pointer to a new memory area large enough to hold arrayLength items of the type associated to the provided PointerIO instance (see {@link PointerIO#getTargetType()})
     */
    public static <V> Pointer<V> allocateArray(PointerIO<V> io, long arrayLength) {
		long targetSize = io.getTargetSize();
    	if (targetSize < 0)
    		throwBecauseUntyped("Cannot allocate array ");
		return allocateBytes(io, targetSize * arrayLength, null);
    }
    /**
     * Create a memory area large enough to hold arrayLength items of the type associated to the provided PointerIO instance (see {@link PointerIO#getTargetType()})
     * @param io PointerIO instance able to store and retrieve elements of the array
     * @param arrayLength length of the array in elements
     * @param beforeDeallocation fake releaser that should be run just before the memory is actually released, for instance in order to call some object destructor
     * @return a pointer to a new memory area large enough to hold arrayLength items of the type associated to the provided PointerIO instance (see {@link PointerIO#getTargetType()})
     */
    public static <V> Pointer<V> allocateArray(PointerIO<V> io, long arrayLength, final Releaser beforeDeallocation) {
		long targetSize = io.getTargetSize();
    	if (targetSize < 0)
    		throwBecauseUntyped("Cannot allocate array ");
		return allocateBytes(io, targetSize * arrayLength, beforeDeallocation);
    }
    /**
     * Create a memory area large enough to hold byteSize consecutive bytes and return a pointer to elements of the type associated to the provided PointerIO instance (see {@link PointerIO#getTargetType()})
     * @param io PointerIO instance able to store and retrieve elements of the array
     * @param byteSize length of the array in bytes
     * @param beforeDeallocation fake releaser that should be run just before the memory is actually released, for instance in order to call some object destructor
     * @return a pointer to a new memory area large enough to hold byteSize consecutive bytes
     */
    public static <V> Pointer<V> allocateBytes(PointerIO<V> io, long byteSize, final Releaser beforeDeallocation) {
        if (byteSize == 0)
        	return null;
        if (byteSize < 0)
        	throw new IllegalArgumentException("Cannot allocate a negative amount of memory !");
        
        long address = JNI.mallocNulled(byteSize);
        if (address == 0)
        	throw new RuntimeException("Failed to allocate " + byteSize);

		return newPointer(io, address, true, address, address + byteSize, null, NO_PARENT, beforeDeallocation == null ? freeReleaser : new Releaser() {
        	//@Override
        	public void release(Pointer<?> p) {
        		beforeDeallocation.release(p);
        		freeReleaser.release(p);
        	}
        }, null);
    }
    /**
     * Create a pointer that depends this pointer and will call a releaser prior to release this pointer, when it is GC'd.<br>
     * This pointer MUST NOT be used anymore.
     * @deprecated This method can easily be misused and is reserved to advanced users.
     * @param beforeDeallocation releaser that should be run before this pointer's releaser (if any).
     * @return a new pointer to the same memory location as this pointer
     */
    @Deprecated
    public synchronized Pointer<T> withReleaser(final Releaser beforeDeallocation) {
    		return newPointer(getIO(), getPeer(), isOrdered(), getValidStart(), getValidEnd(), null, NO_PARENT, beforeDeallocation, null);
    }
    static Releaser freeReleaser = new FreeReleaser();
    static class FreeReleaser implements Releaser {
    	//@Override
		public void release(Pointer<?> p) {
			assert p.getSibling() == null;
			assert p.validStart == p.getPeer();
			
		if (BridJ.debugPointers)
			BridJ.log(Level.INFO, "Freeing pointer " + p + "\n(Creation trace = \n\t" + Utils.toString(p.creationTrace).replaceAll("\n", "\n\t") + "\n)", new RuntimeException().fillInStackTrace());
		
			if (!BridJ.debugNeverFree)
				JNI.free(p.getPeer());
    	}
    }
    
    /**
     * Create a memory area large enough to hold arrayLength items of type elementClass.
     * @param elementClass type of the array elements
     * @param arrayLength length of the array in elements
     * @return a pointer to a new memory area large enough to hold arrayLength items of type elementClass.  
     */
    public static <V> Pointer<V> allocateArray(Class<V> elementClass, long arrayLength) {
        return allocateArray((Type)elementClass, arrayLength);
    }
    /**
     * Create a memory area large enough to hold arrayLength items of type elementClass.
     * @param elementClass type of the array elements
     * @param arrayLength length of the array in elements
     * @return a pointer to a new memory area large enough to hold arrayLength items of type elementClass.
     */
    public static <V> Pointer<V> allocateArray(Type elementClass, long arrayLength) {
		if (arrayLength == 0)
			return null;
		
		PointerIO pio = PointerIO.getInstance(elementClass);
		if (pio == null)
			throw new UnsupportedOperationException("Cannot allocate memory for t
