package alt.beanmapper.compile;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

/**
 * 
 * @author Albert Shift
 *
 */

public final class Arrays {

	private static Map<Type, Type> primitiveIndex = new HashMap<Type, Type>();
	private static Map<Type, Type> wrapperIndex = new HashMap<Type, Type>();

	static {

		add(boolean[].class, Boolean[].class);
		add(byte[].class, Byte[].class);
		add(char[].class, Character[].class);
		add(short[].class, Short[].class);
		add(int[].class, Integer[].class);
		add(long[].class, Long[].class);
		add(float[].class, Float[].class);
		add(double[].class, Double[].class);

	}

	private Arrays() {
	}

	public static boolean isBoxing(Type srcType, Type destType) {
		Type wrapperType = primitiveIndex.get(srcType);
		if (wrapperType != null && wrapperType.equals(destType)) {
			return true;
		}
		return false;
	}

	public static boolean isUnboxing(Type srcType, Type destType) {
		Type primitiveType = wrapperIndex.get(srcType);
		if (primitiveType != null && primitiveType.equals(destType)) {
			return true;
		}
		return false;
	}

	public static boolean isPrimitiveArray(Type srcType) {
		return primitiveIndex.containsKey(srcType);
	}

	public static boolean isWrapperArray(Type srcType) {
		return wrapperIndex.containsKey(srcType);
	}

	private static void add(Class<?> primitive, Class<?> wrapper) {
		Type primitiveType = Type.getType(primitive);
		Type wrapperType = Type.getType(wrapper);

		primitiveIndex.put(primitiveType, wrapperType);
		wrapperIndex.put(wrapperType, primitiveType);
	}

}

