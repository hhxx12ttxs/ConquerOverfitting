package kafka.vm;

import static org.junit.Assert.assertEquals;
import kafka.ir.instances.Instance;
import kafka.ir.instances.Primitives;
import kafka.ir.types.Class;

import org.junit.Test;

public class ArrayTest extends VmTestBase {

	static final Class THIS = ir(ArrayTest.class);

	@Test
	public void testNewArray() {
		Primitives.Array result = (Primitives.Array)invoke(THIS, "newArray", "(I)[I", I3);
		assertEquals(typeForName("[I"), result.type());
		assertEquals(ir(3), result.size());
	}
	
	static int[] newArray(int size) {
		return new int[size];
	}
	
	@Test
	public void testArrayLength() {
		Instance result = invoke(THIS, "arrayLength", "(I)I", I1000);
		assertEquals(ir(1000), result);
	}
	
	static int arrayLength(int size) {
		return new int[size].length;
	}

	@Test
	public void testIntIndexing() {
		Instance result = invoke(THIS, "arrayIndexing", "(II)I", I0, I_10);
		assertEquals(I_10, result);
	}
	
	static int arrayIndexing(int index, int value) {
		int[] array = new int[2];
		array[index] = value;
		return array[index];
	}


	@Test
	public void testLongIndexing() {
		Instance result = invoke(THIS, "arrayIndexing", "(IJ)J", I0, L_10);
		assertEquals(L_10, result);
	}
	
	static long arrayIndexing(int index, long value) {
		long[] array = new long[2];
		array[index] = value;
		return array[index];
	}
	
	
	@Test
	public void testShortIndexing() {
		Instance result = invoke(THIS, "arrayIndexing", "(IS)S", I0, S_10);
		assertEquals(S_10, result);
	}
	
	static short arrayIndexing(int index, short value) {
		short [] array = new short[2];
		array[index] = value;
		return array[index];
	}

	
	@Test
	public void testByteIndexing() {
		Instance result = invoke(THIS, "arrayIndexing", "(IB)B", I0, B_10);
		assertEquals(B_10, result);
	}
	
	static byte arrayIndexing(int index, byte value) {
		byte [] array = new byte[2];
		array[index] = value;
		return array[index];
	}
	
	
	@Test
	public void testCharIndexing() {
		Instance result = invoke(THIS, "arrayIndexing", "(IC)C", I0, C_10);
		assertEquals(C_10, result);
	}
	
	static char arrayIndexing(int index, char value) {
		char[] array = new char[2];
		array[index] = value;
		return array[index];
	}
	
	
	@Test
	public void testDoubleIndexing() {
		Instance result = invoke(THIS, "arrayIndexing", "(ID)D", I0, D_10);
		assertEquals(D_10, result);
	}
	
	static double arrayIndexing(int index, double value) {
		double [] array = new double[2];
		array[index] = value;
		return array[index];
	}
	
	
	@Test
	public void testFloatIndexing() {
		Instance result = invoke(THIS, "arrayIndexing", "(IF)F", I0, F_10);
		assertEquals(F_10, result);
	}
	
	static float arrayIndexing(int index, float value) {
		float[] array = new float[2];
		array[index] = value;
		return array[index];
	}
	
	
	@Test
	public void testReferenceIndexing() {
		String objname = vmname(Object.class);
		Instance nullref = BUILDER.buildNull();
		Instance result = invoke(THIS, "arrayIndexing", "(I" + objname + ")" + objname, I1, nullref);
		assertEquals(nullref, result);
	}
	
	static Object arrayIndexing(int index, Object value) {
		Object[] array = new Object[2];
		array[index] = value;
		return array[index];
	}
}

