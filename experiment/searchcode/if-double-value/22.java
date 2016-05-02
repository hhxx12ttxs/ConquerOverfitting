<<<<<<< HEAD
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class AprioriMiner {

	static List<List<String>> transactionList = new ArrayList<List<String>>();
	static List<Rule> ruleList = new ArrayList<Rule>();
	static List<Set<String>> largeItemList = new ArrayList<Set<String>>();
	
	public static void main(String[] args) {

		double minSupport = 0, minConfidence = 0;
		String inputCsv = "";
		if(args.length > 0)
			inputCsv = args[0];
		if(args.length > 1)
			minSupport = Double.parseDouble(args[1]);
		if(args.length > 2)
			minConfidence = Double.parseDouble(args[2]);
		
		
		File inputFile = new File(inputCsv);
		List<Set<Set<String>>> largeItemSetList = new ArrayList<Set<Set<String>>>();
		
		//Getting individual Items and their counts
		Set<String> itemSet = new HashSet<String>();
		Map<String,Integer> itemSupportMap = new HashMap<String,Integer>();
		Map<Set<String>,Integer> largeItemSupportMap = new HashMap<Set<String>,Integer>();
		ValueComparator bvc =  new ValueComparator(largeItemSupportMap);
		TreeMap<Set<String>, Integer> sortedSupportMap = new TreeMap<Set<String>, Integer>(bvc);
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line;
			while((line = br.readLine()) != null)
			{
				String[] items = line.split(",");
				List<String> transaction = new ArrayList<String>();
				for(String i : items)
				{
					if(itemSupportMap.containsKey(i))
					{
						int newCount = (itemSupportMap.get(i))+1;
						itemSupportMap.remove(i);
						itemSupportMap.put(i, newCount);
						itemSet.add(i);
					}
					else
					{
						itemSupportMap.put(i, 1);
					}
					transaction.add(i);
				}
				transactionList.add(transaction);
			}
			br.close();
			
			//Enter all level 0 large item sets in largeItemSetList
			Iterator<Entry<String, Integer>> iterator = itemSupportMap.entrySet().iterator();
			Set<String> temp = new HashSet<String>();
			while(iterator.hasNext())
			{
				Entry<String, Integer> entry = iterator.next();
				String key = (String)entry.getKey();
				Integer value = (Integer)entry.getValue();
				if((double)(value)/transactionList.size() >= minSupport)
				{
					temp.add(key);
				}
			}
			Set<Set<String>> setString = new HashSet<Set<String>>();
			setString.add(temp);
			largeItemSetList.add(setString);
			
			int k = 1;
			while(largeItemSetList.get(k-1).size() != 0)
			{
				Set<Set<String>> addToLargeItemSetList = new HashSet<Set<String>>(); //APRIORI-GEN---new candidates
				ArrayList<String> entryList = new ArrayList<String>(65536);
				ArrayList<Set<String>> preCkList = new ArrayList<Set<String>>(65536);
				ArrayList<Set<String>> ckList = new ArrayList<Set<String>>(65536);

				Iterator<Set<String>> iterator3 = largeItemSetList.get(k-1).iterator();
				while(iterator3.hasNext())
				{
					Set<String> s = iterator3.next();
					for(String string : s)
					{
						entryList.add(string);
					}
				}
				
				preCkList = getSubsets(entryList);
				
				//pruning candidates not included in previous level
				for(int i = 0; i < preCkList.size(); i ++)
				{
					if(preCkList.get(i).size() == k+1)
					{
						ckList.add(preCkList.get(i));
					}
				}
				
				for(int i = 0; i < ckList.size(); i ++)
				{
					//ELIMINATING ONES OCCURING IN PREVIOUS LARGE ITEM SET LIST (k-1)
					if(!largeItemSetList.get(k-1).containsAll(ckList.get(i)))
					{
						ckList.remove(i);
						continue;
					}
				}
				
				//candidates contained in transactions
				for(int i = 0; i < ckList.size(); i ++)
				{
					int count = 0;
					for(int j = 0; j < transactionList.size(); j ++)
					{
						if(transactionList.get(j).containsAll(ckList.get(i)))
							count ++;
					}
					if((double)((double)count/transactionList.size()) > minSupport)
					{
						addToLargeItemSetList.add(ckList.get(i));
					}
				}
				k ++;
				largeItemSetList.add(addToLargeItemSetList);
			}
			
			largeItemSetList.remove(largeItemSetList.size()-1);
			Set<Set<String>> tempSet = largeItemSetList.get(0);
			Iterator<Set<String>> tempIter = tempSet.iterator();
			Set<String> tempTempSet = tempIter.next();
			Iterator<String> tempTempIter = tempTempSet.iterator();
			Map<String, Integer> refMap = new HashMap<String, Integer>();
			while(tempTempIter.hasNext())
			{
				String enterStringInSet = tempTempIter.next();
				Set<String> newEntry = new HashSet<String>();
				newEntry.add(enterStringInSet);
				Integer newEntryCount = -1;
				
				Iterator<Entry<String, Integer>> innerIter = itemSupportMap.entrySet().iterator();
				while(innerIter.hasNext())
				{
					Entry<String, Integer> entry = innerIter.next();
					String key = (String)entry.getKey();
					Integer value = (Integer)entry.getValue();
					if(key.equals(enterStringInSet))
					{
						newEntryCount = value;
						refMap.put(key, value);
					}
				}
				largeItemSupportMap.put(newEntry, newEntryCount);
			}
			
			//populate largeItemSupportMap with k > 1
			for(int i = 1; i < largeItemSetList.size(); i ++)
			{
				Set<Set<String>> setSetString = largeItemSetList.get(i);
				Iterator<Set<String>> innerIter = setSetString.iterator();
				while(innerIter.hasNext())
				{
					Set<String> keySet = innerIter.next();
					Integer supportValue = 0;
					supportValue = getSupportCount(keySet);
					largeItemSupportMap.put(keySet, supportValue);
				}
			}

			//generating confidences:
			//get all rule possibilities in tempRuleList
			List<Rule> tempRuleList = new ArrayList<Rule>();
			Iterator<Entry<Set<String>, Integer>> iter = largeItemSupportMap.entrySet().iterator();
			while(iter.hasNext())
			{
				Entry<Set<String>, Integer> entry = iter.next();
				Set<String> key = (Set<String>)entry.getKey();
				Integer value = (Integer)entry.getValue();
				Iterator<Entry<Set<String>, Integer>> inneriter = largeItemSupportMap.entrySet().iterator();
				while(inneriter.hasNext())
				{
					Entry<Set<String>, Integer> innerEntry = inneriter.next();
					Set<String> innerKey = (Set<String>)innerEntry.getKey();
					Integer innerValue = (Integer)innerEntry.getValue();
					
					if(union(key, innerKey).size() != 0 && innerKey.size() == 1)
					{
						Rule rule = new Rule(transactionList);
						rule.setLhs(key);
						rule.setRhs(innerKey);
						rule.setLhsCount(value);
						rule.setRhsCount(innerValue);
						tempRuleList.add(rule);
					}
				}
			}
			
			//Removing rules that have rHS in LHS and has conf. < minConf.
			for(int i = 0; i < tempRuleList.size(); i ++)
			{
				Rule currentRule = tempRuleList.get(i);
				if(currentRule.getLhs().containsAll(currentRule.getRhs()))
				{
					tempRuleList.remove(currentRule);
					continue;
				}
				if(currentRule.computeAndGetConfidence() < minConfidence)
				{
					tempRuleList.remove(currentRule);
					continue;
				}
				ruleList.add(currentRule);
			}
			sortedSupportMap.putAll(largeItemSupportMap);
			
			Comparator<Rule> comparator = new Comparator<Rule>() {
				public int compare(Rule o1, Rule o2) {
					if(o1.confidence <= o2.confidence)
						return 1;
					return -1;
				}
			}; 

			Collections.sort(ruleList, comparator);
			
			File outputFile = new File("output.txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			bw.write("Large ItemSets (decreasing order of support) :\n\n");
			Iterator<Map.Entry<Set<String>, Integer>> finalItemSetIterator = sortedSupportMap.entrySet().iterator();
			while(finalItemSetIterator.hasNext())
			{
				Entry<Set<String>, Integer> entry = finalItemSetIterator.next();
				Set<String> key = entry.getKey();
				Integer value = entry.getValue();
				double support = (value / (double)transactionList.size()) * 100.0;
				bw.write(Arrays.toString(key.toArray()) + ", " + support + "%\n");
			}
			bw.write("\n\nHigh-Confidence Rules (decreasing order of confidence) with support(LHS union RHS) :\n\n");
			for(int i = 0; i < ruleList.size(); i ++)
			{
				bw.write(ruleList.get(i) + "\n");
			}
			bw.close();
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static ArrayList<Set<String>> getSubsets(ArrayList<String> set)
	{

		ArrayList<Set<String>> subsetCollection = new ArrayList<Set<String>>();

		if (set.size() == 0) {
			subsetCollection.add(new HashSet<String>());
		} else {
			ArrayList<String> reducedSet = new ArrayList<String>();

			reducedSet.addAll(set);

			String first = reducedSet.remove(0);
			ArrayList<Set<String>> subsets = getSubsets(reducedSet);
			subsetCollection.addAll(subsets);

			subsets = getSubsets(reducedSet);

			for (Set<String> subset : subsets) {
				subset.add(first);
			}

			subsetCollection.addAll(subsets);
		}

		return subsetCollection;
	}
	
	public static <T>Set<T> union(Set<T> setA, Set<T> setB) {
	    Set<T> tmp = new HashSet<T>(setA);
	    tmp.addAll(setB);
	    return tmp;
	  }
	
	private static int getSupportCount(Set<String> set)
	{
		int count = 0;
		for(int i = 0; i < transactionList.size(); i ++)
		{
			if(transactionList.get(i).containsAll(set))
			{
				count ++;
			}
		}
		return count;
	}
}

class Rule
{
	Set<String> lhs;
	Set<String> rhs;
	int lhsCount;
	int rhsCount;
	double confidence;
	int support;
	List<List<String>> transactionList = new ArrayList<List<String>>();
	
	Rule(List<List<String>> tl)
	{
		transactionList = tl;
		lhs = new HashSet<String>();
		rhs = new HashSet<String>();
	}
	
	private int getSupportCount(Set<String> set)
	{
		int count = 0;
		for(int i = 0; i < transactionList.size(); i ++)
		{
			if(transactionList.get(i).containsAll(set))
			{
				count ++;
			}
		}
		return count;
	}
	
	public void setLhs(Set<String> lhs) {
		this.lhs = lhs;
	}
	
	public Set<String> getLhs() {
		return lhs;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(lhs.toArray()) + " ==> " + Arrays.toString(rhs.toArray()) + " (Conf: " + confidence * 100 + "%, Supp: " + (getSupportCount(union(lhs,rhs)) / (double)transactionList.size()) * 100 + "%)";
	}
	
	public int getLhsCount() {
		return lhsCount;
	}
	
	public Set<String> getRhs() {
		return rhs;
	}
	
	public int getRhsCount() {
		return rhsCount;
	}
	
	public void setLhsCount(int lhsSupport) {
		this.lhsCount = lhsSupport;
	}
	
	public void setRhs(Set<String> rhs) {
		this.rhs = rhs;
	}
	
	public void setRhsCount(int rhsSupport) {
		this.rhsCount = rhsSupport;
	}
	
	public double computeAndGetConfidence()
	{
		confidence = getSupportCount(union(lhs, rhs));
		support = (int)confidence;
		confidence /= getSupportCount(lhs);
		return confidence;
	}
	
	public <T>Set<T> union(Set<T> setA, Set<T> setB) {
	    Set<T> tmp = new HashSet<T>(setA);
	    tmp.addAll(setB);
	    return tmp;
	  }
}

class ValueComparator implements Comparator<Set<String>> {

    Map<Set<String>, Integer> base;
    public ValueComparator(Map<Set<String>, Integer> base) {
        this.base = base;
    }

    public int compare(Set<String> a, Set<String> b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        }
    }
=======
/* Copyright (c) 2007 Timothy Wall, All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.  
 */
package com.sun.jna;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.sun.jna.ArgumentsMarshalTest.TestLibrary.CheckFieldAlignment;

/** Exercise a range of native methods.
 *
 * @author twall@users.sf.net
 */
//@SuppressWarnings("unused")
public class ArgumentsMarshalTest extends TestCase {

    private static final String UNICODE = "[\0444]";

    public static interface TestLibrary extends Library {
        
        class CheckFieldAlignment extends Structure {
            public static class ByValue extends CheckFieldAlignment 
                implements Structure.ByValue { }
            public static class ByReference extends CheckFieldAlignment
                implements Structure.ByReference { }
        
            public byte int8Field;
            public short int16Field;
            public int int32Field;
            public long int64Field;
            public float floatField;
            public double doubleField;
            
            public List getFieldOrder() {
                return Arrays.asList(new String[] { "int8Field", "int16Field", "int32Field", "int64Field", "floatField", "doubleField" });
            }
            public CheckFieldAlignment() {
                int8Field = (byte)fieldOffset("int8Field");
                int16Field = (short)fieldOffset("int16Field");
                int32Field = fieldOffset("int32Field");
                int64Field = fieldOffset("int64Field");
                floatField = fieldOffset("floatField");
                doubleField = fieldOffset("doubleField");
            }
        }

        String returnStringArgument(Object arg);
        boolean returnBooleanArgument(boolean arg);
        byte returnInt8Argument(byte arg);
        char returnWideCharArgument(char arg);
        short returnInt16Argument(short arg);
        int returnInt32Argument(int i);
        long returnInt64Argument(long l);
        NativeLong returnLongArgument(NativeLong l);
        float returnFloatArgument(float f);
        double returnDoubleArgument(double d);
        String returnStringArgument(String s);
        WString returnWStringArgument(WString s);
        Pointer returnPointerArgument(Pointer p);
        String returnStringArrayElement(String[] args, int which);
        WString returnWideStringArrayElement(WString[] args, int which);
        Pointer returnPointerArrayElement(Pointer[] args, int which);

        public static class TestPointerType extends PointerType {
            public TestPointerType() { }
            public TestPointerType(Pointer p) { super(p); }
        }
        TestPointerType returnPointerArrayElement(TestPointerType[] args, int which);
        CheckFieldAlignment returnPointerArrayElement(CheckFieldAlignment.ByReference[] args, int which);
        int returnRotatedArgumentCount(String[] args);

        long checkInt64ArgumentAlignment(int i, long j, int i2, long j2);
        double checkDoubleArgumentAlignment(float i, double j, float i2, double j2);
        Pointer testStructurePointerArgument(CheckFieldAlignment p);
        int testStructureByValueArgument(CheckFieldAlignment.ByValue p);
        int testStructureArrayInitialization(CheckFieldAlignment[] p, int len);
        int testStructureByReferenceArrayInitialization(CheckFieldAlignment.ByReference[] p, int len);
        void modifyStructureArray(CheckFieldAlignment[] p, int length);
        void modifyStructureByReferenceArray(CheckFieldAlignment.ByReference[] p, int length);
        
        int fillInt8Buffer(byte[] buf, int len, byte value);
        int fillInt16Buffer(short[] buf, int len, short value);
        int fillInt32Buffer(int[] buf, int len, int value);
        int fillInt64Buffer(long[] buf, int len, long value);
        int fillFloatBuffer(float[] buf, int len, float value);
        int fillDoubleBuffer(double[] buf, int len, double value);

        // Nonexistent functions 
        boolean returnBooleanArgument(Object arg);

        // Structure
        class MinTestStructure extends Structure {
            public int field;
            protected List getFieldOrder() {
                return Arrays.asList(new String[] { "field" });
            }
        }
        Pointer testStructurePointerArgument(MinTestStructure s);

        class VariableSizedStructure extends Structure {
            public int length;
            public byte[] buffer;
            protected List getFieldOrder() {
                return Arrays.asList(new String[] { "length", "buffer" });
            }
            public VariableSizedStructure(String arg) {
                length = arg.length() + 1;
                buffer = new byte[length];
                System.arraycopy(arg.getBytes(), 0, buffer, 0, arg.length());
            }
        }
        String returnStringFromVariableSizedStructure(VariableSizedStructure s);
        class CbStruct extends Structure {
            public static interface TestCallback extends Callback {
                int callback(int arg1, int arg2);
            }
            public TestCallback cb;
            protected List getFieldOrder() {
                return Arrays.asList(new String[] { "cb" });
            }
        }
        void setCallbackInStruct(CbStruct cbstruct);
    }

    TestLibrary lib;
    protected void setUp() {
        lib = (TestLibrary)Native.loadLibrary("testlib", TestLibrary.class);
    }
    
    protected void tearDown() {
        lib = null;
    }
    
    public void testJavaObjectArgument() {
        Object o = this;
        try {
            lib.returnStringArgument(o);
            fail("Java Object arguments should throw IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            assertTrue("Exception should include Object type description: " + e,
                       e.getMessage().indexOf(o.getClass().getName()) != -1);
        }
        catch(Throwable e) {
            fail("Java Object arguments should throw IllegalArgumentException, not " + e);
        }
    }

    public void testBooleanArgument() {
        assertTrue("True argument should be returned", 
                   lib.returnBooleanArgument(true));
        assertFalse("False argument should be returned", 
                    lib.returnBooleanArgument(false));
    }

    public void testInt8Argument() {
        byte b = 0;
        assertEquals("Wrong value returned", 
                     b, lib.returnInt8Argument(b));
        b = 127;
        assertEquals("Wrong value returned", 
                     b, lib.returnInt8Argument(b));
        b = -128;
        assertEquals("Wrong value returned", 
                     b, lib.returnInt8Argument(b));
    }
    
    public void testWideCharArgument() {
        char c = 0;
        assertEquals("Wrong value returned",
                     c, lib.returnWideCharArgument(c));
        c = 0xFFFF;
        assertEquals("Wrong value returned",
                     c, lib.returnWideCharArgument(c));
        c = 0x7FFF;
        assertEquals("Wrong value returned",
                     c, lib.returnWideCharArgument(c));
    }

    public void testInt16Argument() {
        short v = 0;
        assertEquals("Wrong value returned", 
                     v, lib.returnInt16Argument(v));
        v = 32767;
        assertEquals("Wrong value returned", 
                     v, lib.returnInt16Argument(v));
        v = -32768;
        assertEquals("Wrong value returned", 
                     v, lib.returnInt16Argument(v));
    }

    public void testIntArgument() {
        int value = 0;
        assertEquals("Should return 32-bit argument", 
                     value, lib.returnInt32Argument(value));
        value = 1;
        assertEquals("Should return 32-bit argument", 
                     value, lib.returnInt32Argument(value));
        value = 0x7FFFFFFF;
        assertEquals("Should return 32-bit argument", 
                     value, lib.returnInt32Argument(value));
        value = 0x80000000;
        assertEquals("Should return 32-bit argument", 
                     value, lib.returnInt32Argument(value));
    }

    public void testLongArgument() {
        long value = 0L;
        assertEquals("Should return 64-bit argument", 
                     value, lib.returnInt64Argument(value));
        value = 1L;
        assertEquals("Should return 64-bit argument", 
                     value, lib.returnInt64Argument(value));
        value = 0x7FFFFFFFL;
        assertEquals("Should return 64-bit argument", 
                     value, lib.returnInt64Argument(value));
        value = 0x80000000L;
        assertEquals("Should return 64-bit argument", 
                     value, lib.returnInt64Argument(value));
        value = 0x7FFFFFFF00000000L;
        assertEquals("Should return 64-bit argument", 
                     value, lib.returnInt64Argument(value));
        value = 0x8000000000000000L;
        assertEquals("Should return 64-bit argument", 
                     value, lib.returnInt64Argument(value));
    }

    public void testNativeLongArgument() {
        NativeLong value = new NativeLong(0);
        assertEquals("Should return 0", 
                     value, lib.returnLongArgument(value));
        value = new NativeLong(1);
        assertEquals("Should return 1", 
                     value, lib.returnLongArgument(value));
        value = new NativeLong(0x7FFFFFFF);
        assertEquals("Should return 0x7FFFFFFF", 
                     value, lib.returnLongArgument(value));
        value = new NativeLong(0x80000000);
        assertEquals("Should return 0x80000000", 
                     value, lib.returnLongArgument(value));
    }

    public interface NativeMappedLibrary extends Library {
        int returnInt32Argument(Custom arg);
    }
    public static class Custom implements NativeMapped {
        private int value;
        public Custom() { }
        public Custom(int value) {
            this.value = value;
        }
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            return new Custom(((Integer)nativeValue).intValue());
        }
        public Class nativeType() {
            return Integer.class;
        }
        public Object toNative() {
            return new Integer(value);
        }
    }
    protected NativeMappedLibrary loadNativeMappedLibrary() {
        return (NativeMappedLibrary)
            Native.loadLibrary("testlib", NativeMappedLibrary.class);
    }
    public void testNativeMappedArgument() {
        NativeMappedLibrary lib = loadNativeMappedLibrary();
        final int MAGIC = 0x12345678;
        Custom arg = new Custom(MAGIC);
        assertEquals("Argument not mapped", MAGIC, lib.returnInt32Argument(arg));
    }
    
    public void testPointerArgumentReturn() {
        assertEquals("Expect null pointer",
                     null, lib.returnPointerArgument(null));
        Structure s = new TestLibrary.CheckFieldAlignment();
        assertEquals("Expect structure pointer",
                     s.getPointer(), 
                     lib.returnPointerArgument(s.getPointer()));
    }

    static final String MAGIC = "magic" + UNICODE;
    public void testStringArgumentReturn() {
        assertEquals("Expect null pointer", null, lib.returnStringArgument(null));
        assertEquals("Expect string magic", MAGIC, lib.returnStringArgument(MAGIC));
    }

    static final WString WMAGIC = new WString("magic" + UNICODE);
    public void testWStringArgumentReturn() {
        assertEquals("Expect null pointer", null, lib.returnWStringArgument(null));
        assertEquals("Expect string magic", WMAGIC.toString(), lib.returnWStringArgument(WMAGIC).toString());
    }
    
    public void testInt64ArgumentAlignment() {
        long value = lib.checkInt64ArgumentAlignment(0x10101010, 0x1111111111111111L, 
                                                     0x01010101, 0x2222222222222222L);
        assertEquals("Improper handling of interspersed int32/int64",
                     0x3333333344444444L, value);
    }

    public void testDoubleArgumentAlignment() {
        double value = lib.checkDoubleArgumentAlignment(1f, 2d, 3f, 4d);
        assertEquals("Improper handling of interspersed float/double",
                     10d, value, 0);
    }

    public void testStructurePointerArgument() {
        TestLibrary.CheckFieldAlignment struct = new TestLibrary.CheckFieldAlignment();
        assertEquals("Native address of structure should be returned",
                     struct.getPointer(), 
                     lib.testStructurePointerArgument(struct));
        // ensure that even if the argument is ByValue, it's passed as ptr
        struct = new TestLibrary.CheckFieldAlignment.ByValue();
        assertEquals("Structure argument should be passed according to method "
                     + "parameter type, not argument type",
                     struct.getPointer(),
                     lib.testStructurePointerArgument(struct));

        struct = null;
        assertNull("Null argument should be returned",
                   lib.testStructurePointerArgument(struct));
    }

    public void testStructureByValueArgument() {
        TestLibrary.CheckFieldAlignment.ByValue struct = 
            new TestLibrary.CheckFieldAlignment.ByValue();
        assertEquals("Wrong alignment in " + struct.toString(true),
                     "0", Integer.toHexString(lib.testStructureByValueArgument(struct)));
    }
    
    public void testStructureByValueTypeInfo() {
        class TestStructure extends Structure implements Structure.ByValue {
            public byte b;
            public char c;
            public short s;
            public int i;
            public long j;
            public float f;
            public double d;
            public Pointer[] parray = new Pointer[2];
            public byte[] barray = new byte[2];
            protected List getFieldOrder() {
                return Arrays.asList(new String[] { "b", "c", "s", "i", "j", "f", "d", "parray", "barray" });
            }
        }
        Structure s = new TestStructure();
        // Force generation of type info
        s.size();
    }

    
    public void testWriteStructureArrayArgumentMemory() {
        final int LENGTH = 10;
        TestLibrary.CheckFieldAlignment block = new TestLibrary.CheckFieldAlignment();
        TestLibrary.CheckFieldAlignment[] array = 
            (TestLibrary.CheckFieldAlignment[])block.toArray(LENGTH);
        for (int i=0;i < array.length;i++) {
            array[i].int32Field = i;
        }
        assertEquals("Structure array memory not properly initialized",
                     -1, lib.testStructureArrayInitialization(array, array.length));
        
    }
    
    public void testUninitializedStructureArrayArgument() {
        final int LENGTH = 10;
        TestLibrary.CheckFieldAlignment[] block = 
            new TestLibrary.CheckFieldAlignment[LENGTH];
        lib.modifyStructureArray(block, block.length);
        for (int i=0;i < block.length;i++) {
            assertNotNull("Structure array not initialized at " + i, block[i]);
            assertEquals("Wrong value for int32 field of structure at " + i,
                         i, block[i].int32Field);
            assertEquals("Wrong value for int64 field of structure at " + i,
                         i + 1, block[i].int64Field);
            assertEquals("Wrong value for float field of structure at " + i,
                         i + 2, block[i].floatField, 0);
            assertEquals("Wrong value for double field of structure at " + i,
                         i + 3, block[i].doubleField, 0);
        }
    }

    public void testRejectNoncontiguousStructureArrayArgument() {
	TestLibrary.CheckFieldAlignment s1, s2, s3;
	s3 = new TestLibrary.CheckFieldAlignment();
	s1 = new TestLibrary.CheckFieldAlignment();
	s2 = new TestLibrary.CheckFieldAlignment();
        TestLibrary.CheckFieldAlignment[] block = { s1, s2, s3 };
        try {
            lib.modifyStructureArray(block, block.length);
            fail("Library invocation should fail");
        }
        catch(IllegalArgumentException e) {
        }
    }
    
    public void testRejectIncompatibleStructureArrayArgument() {
        TestLibrary.CheckFieldAlignment s1 = new TestLibrary.CheckFieldAlignment.ByReference();
        TestLibrary.CheckFieldAlignment[] autoArray = (TestLibrary.CheckFieldAlignment[])s1.toArray(3);
        try {
            lib.modifyStructureArray(autoArray, autoArray.length);
        }
        catch(IllegalArgumentException e) {
        }
        TestLibrary.CheckFieldAlignment.ByReference[] byRefArray =
            (TestLibrary.CheckFieldAlignment.ByReference[])s1.toArray(3);
        try {
            lib.modifyStructureArray(byRefArray, byRefArray.length);
        }
        catch(IllegalArgumentException e) {
        }
        TestLibrary.CheckFieldAlignment[] arrayWithRefElements = { autoArray[0], autoArray[1], autoArray[2] };
        try {
            lib.modifyStructureArray(arrayWithRefElements, arrayWithRefElements.length);
        }
        catch(IllegalArgumentException e) {
        }
    }

    /** When passing an array of <code>struct*</code> to native, be sure to
        invoke <code>Structure.write()</code> on each of the elements. */
    public void testWriteStructureByReferenceArrayArgumentMemory() {
        TestLibrary.CheckFieldAlignment.ByReference[] array = {
            new TestLibrary.CheckFieldAlignment.ByReference(),
            new TestLibrary.CheckFieldAlignment.ByReference(),
            new TestLibrary.CheckFieldAlignment.ByReference(),
            new TestLibrary.CheckFieldAlignment.ByReference(),
        };
        for (int i=0;i < array.length;i++) {
            array[i].int32Field = i;
        }
        assertEquals("Structure.ByReference array memory not properly initialized",
                     -1, lib.testStructureByReferenceArrayInitialization(array, array.length));
    }

    public void testReadStructureByReferenceArrayArgumentMemory() {
        TestLibrary.CheckFieldAlignment.ByReference[] array = {
            new TestLibrary.CheckFieldAlignment.ByReference(),
            new TestLibrary.CheckFieldAlignment.ByReference(),
            new TestLibrary.CheckFieldAlignment.ByReference(),
            new TestLibrary.CheckFieldAlignment.ByReference(),
        };
        lib.modifyStructureByReferenceArray(array, array.length);
        for (int i=0;i < array.length;i++) {
            assertEquals("Wrong value for int32 field of structure at " + i,
                         i, array[i].int32Field);
            assertEquals("Wrong value for int64 field of structure at " + i,
                         i + 1, array[i].int64Field);
            assertEquals("Wrong value for float field of structure at " + i,
                         i + 2, array[i].floatField, 0);
            assertEquals("Wrong value for double field of structure at " + i,
                         i + 3, array[i].doubleField, 0);
        }
    }

    public void testByteArrayArgument() {
        byte[] buf = new byte[1024];
        final byte MAGIC = (byte)0xED;
        assertEquals("Wrong return value", buf.length, 
                     lib.fillInt8Buffer(buf, buf.length, MAGIC));
        for (int i=0;i < buf.length;i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf[i]);
        }
    }
    
    public void testShortArrayArgument() {
        short[] buf = new short[1024];
        final short MAGIC = (short)0xABED;
        assertEquals("Wrong return value", buf.length, 
                     lib.fillInt16Buffer(buf, buf.length, MAGIC));
        for (int i=0;i < buf.length;i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf[i]);
        }
    }
    
    public void testIntArrayArgument() {
        int[] buf = new int[1024];
        final int MAGIC = 0xABEDCF23;
        assertEquals("Wrong return value", buf.length, 
                     lib.fillInt32Buffer(buf, buf.length, MAGIC));
        for (int i=0;i < buf.length;i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf[i]);
        }
    }
    
    public void testLongArrayArgument() { 
        long[] buf = new long[1024];
        final long MAGIC = 0x1234567887654321L;
        assertEquals("Wrong return value", buf.length, 
                     lib.fillInt64Buffer(buf, buf.length, MAGIC));
        for (int i=0;i < buf.length;i++) {
            assertEquals("Bad value at index " + i, MAGIC, buf[i]);
        }
    }
    
    public void testUnsupportedJavaObjectArgument() {
        try {
            lib.returnBooleanArgument(this);
            fail("Unsupported Java objects should be rejected");
        }
        catch(IllegalArgumentException e) {
        }
    }
    
    public void testStringArrayArgument() {
        String[] args = { "one"+UNICODE, "two"+UNICODE, "three"+UNICODE };
        assertEquals("Wrong value returned", args[0], lib.returnStringArrayElement(args, 0));
        assertNull("Native String array should be null terminated", 
                   lib.returnStringArrayElement(args, args.length));
    }
    
    public void testWideStringArrayArgument() {
        WString[] args = { new WString("one"+UNICODE), new WString("two"+UNICODE), new WString("three"+UNICODE) };
        assertEquals("Wrong value returned", args[0], lib.returnWideStringArrayElement(args, 0));
        assertNull("Native WString array should be null terminated",
                   lib.returnWideStringArrayElement(args, args.length));
    }
    
    public void testPointerArrayArgument() {
        Pointer[] args = { 
            new NativeString(getName()).getPointer(),
            null,
            new NativeString(getName()+"2").getPointer(),
        };
        Pointer[] originals = new Pointer[args.length];
        System.arraycopy(args, 0, originals, 0, args.length);

        assertEquals("Wrong value returned", args[0], lib.returnPointerArrayElement(args, 0));
        assertNull("Wrong value returned", lib.returnPointerArrayElement(args, 1));
        assertEquals("Wrong value returned", args[2], lib.returnPointerArrayElement(args, 2));
        assertNull("Native array should be null terminated", lib.returnPointerArrayElement(args, 3));

        assertSame("Argument pointers should remain unmodified [0]",
                   originals[0], args[0]);
        assertSame("Argument pointers should remain unmodified [2]",
                   originals[2], args[2]);
    }

    public void testNativeMappedArrayArgument() {
        TestLibrary.TestPointerType[] args = {
            new TestLibrary.TestPointerType(new NativeString(getName()).getPointer()),
            null,
            new TestLibrary.TestPointerType(new NativeString(getName()+"2").getPointer()),
        };
        assertEquals("Wrong value returned", args[0], lib.returnPointerArrayElement(args, 0));
        assertNull("Wrong value returned", lib.returnPointerArrayElement(args, 1));
        assertEquals("Wrong value returned", args[2], lib.returnPointerArrayElement(args, 2));
    };

    public void testStructureByReferenceArrayArgument() {
        CheckFieldAlignment.ByReference[] args = { 
            new CheckFieldAlignment.ByReference(),
            null,
            new CheckFieldAlignment.ByReference(),
        };
        assertEquals("Wrong value returned (0)", args[0], lib.returnPointerArrayElement(args, 0));
        assertNull("Wrong value returned (1)", lib.returnPointerArrayElement(args, 1));
        assertEquals("Wrong value returned (2)", args[2], lib.returnPointerArrayElement(args, 2));
        assertNull("Native array should be null terminated", lib.returnPointerArrayElement(args, 3));
    }

    public void testModifiedCharArrayArgument() {
        String[] args = { "one", "two", "three" };
        assertEquals("Wrong native array count", args.length, lib.returnRotatedArgumentCount(args));
        assertEquals("Modified array argument not re-read",
                     Arrays.asList(new String[] { "two", "three", "one" }),
                     Arrays.asList(args));
    }
    
    public void testReadFunctionPointerAsCallback() {
        TestLibrary.CbStruct s = new TestLibrary.CbStruct();
        assertNull("Function pointer field should be null", s.cb);
        lib.setCallbackInStruct(s);
        assertNotNull("Callback field not set", s.cb);
    }

    public void testCallProxiedFunctionPointer() {
        TestLibrary.CbStruct s = new TestLibrary.CbStruct();
        lib.setCallbackInStruct(s);
        assertEquals("Proxy to native function pointer failed: " + s.cb,
                     3, s.cb.callback(1, 2));
    }

    public void testVariableSizedStructureArgument() {
        String EXPECTED = getName();
        TestLibrary.VariableSizedStructure s =
            new TestLibrary.VariableSizedStructure(EXPECTED);
        assertEquals("Wrong string returned from variable sized struct",
                     EXPECTED, lib.returnStringFromVariableSizedStructure(s));
    }

    public void testDisableAutoSynch() {
        TestLibrary.MinTestStructure s = new TestLibrary.MinTestStructure();
        final int VALUE = 42;
        s.field = VALUE;
        s.setAutoWrite(false);
        lib.testStructurePointerArgument(s);
        assertEquals("Auto write should be disabled", 0, s.field);

        final int EXPECTED = s.field;
        s.getPointer().setInt(0, VALUE);
        s.setAutoRead(false);
        lib.testStructurePointerArgument(s);
        assertEquals("Auto read should be disabled", EXPECTED, s.field);
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(ArgumentsMarshalTest.class);
    }
    
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

